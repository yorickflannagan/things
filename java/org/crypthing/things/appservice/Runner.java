package org.crypthing.things.appservice;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import org.apache.commons.digester3.Digester;
import org.crypthing.things.SNMPTrap;
import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.ConfigProperties;
import org.crypthing.things.appservice.config.ConfigReader;
import org.crypthing.things.appservice.config.ConnectorsConfig;
import org.crypthing.things.appservice.config.DataSourcesConfig;
import org.crypthing.things.appservice.config.JDBCConfigFactory;
import org.crypthing.things.appservice.config.JNDIConfig;
import org.crypthing.things.appservice.config.Property;
import org.crypthing.things.appservice.config.RunnerConfig;
import org.crypthing.things.appservice.config.SNMPConfig;
import org.crypthing.things.appservice.config.WorkerConfigFactory;
import org.crypthing.things.events.LifecycleEvent;
import org.crypthing.things.events.LifecycleEvent.LifecycleEventType;
import org.crypthing.things.events.LifecycleEventDispatcher;
import org.crypthing.things.events.LogTrapperListener;
import org.crypthing.things.events.ProcessingEvent;
import org.crypthing.things.events.ProcessingEvent.ProcessingEventType;
import org.crypthing.things.events.ProcessingEventDispatcher;
import org.xml.sax.SAXException;


public final class Runner
implements	RunnerMBean,
		ShutdownEventListener,
		ReleaseResourceEventDispatcher,
		InterruptEventDispatcher
{
	private class ShutdownHook implements Runnable { @Override public void run() { if (!hasShutdown) shutdown(); } }
	public static final String MBEAN_PATTERN = "org.crypthing.things.appservice:type=Runner,name=";
	private static final Logger log = Logger.getLogger(Runner.class.getName());
	private static ObjectName mbName;

	private final ReentrantLock lock = new ReentrantLock();
	private final RunnerConfig config;
	private final Set<ReleaseResourceListener> resourceListeners;
	private final Set<InterruptEventListener> interruptListeners;
	private final LifecycleEventDispatcher lcDispatcher;
	private final ProcessingEventDispatcher pDispatcher;
	private boolean hasShutdown = false;
	private Map<Integer, Sandbox> workers = new ConcurrentHashMap<Integer, Sandbox>();
	private boolean ready = false;
	private int maxWorker = 0;
	private int minWorker = 0;
	private long acumulatedSucess;
	private long acumulatedFailure;

	public Runner(final RunnerConfig cfg) throws ConfigException
	{
		try { Class.forName(cfg.getWorker().getImpl()).newInstance(); }
		catch (final Throwable e) { throw new ConfigException(e); }
		config = cfg;
		lcDispatcher = new LifecycleEventDispatcher();
		pDispatcher = new ProcessingEventDispatcher();
		final LogTrapperListener traps = new LogTrapperListener(SNMPTrap.createTrap(config.getSnmp()));
		lcDispatcher.addListener(traps);
		pDispatcher.addListener(traps);
		resourceListeners = new HashSet<ReleaseResourceListener>();
		interruptListeners = new HashSet<InterruptEventListener>();
	}

	private void start()
	{
		try
		{
			for (int i = 0, threads = config.getWorker().getThreads(); i < threads; i++) workers.put(maxWorker++, newWorker());
			lcDispatcher.fire(new LifecycleEvent(this, LifecycleEventType.start, "Runner initialization succeeded"));
			ready = true;
			int goal = config.getWorker().getGoal();
			if(goal >0)
			{
				long sucessCurrent = 0;
				long sucessLast = 0;
				int ramp = config.getWorker().getRamp();
				int adjustDelay = config.getWorker().getGoalMeasure();
				ThreadAdvisor ted = new ThreadAdvisor(goal, ramp); 
				try{Thread.sleep(adjustDelay);}catch(InterruptedException e){}
				while(ready)
				{
					sucessCurrent = getSuccessCount();
					int go = ted.whichWay(workers.size(), sucessCurrent - sucessLast);
					sucessLast = sucessCurrent;
					if(go!=0)
					{
						if(go>0)
						{
							addWorker();
						}
						else
						{
							removeWorker();
						}
					}
					try{Thread.sleep(adjustDelay);}catch(InterruptedException e){}
				} 
			}
		}
		catch (final Throwable e)
		{
			pDispatcher.fire(new ProcessingEvent(this, ProcessingEventType.error, "Could not launch new worker", e));
			shutdown();
		}
	}

	private void addWorker() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigException {
		ReentrantLock _lock = lock;
		lock.lock();
		try
		{
			workers.put(maxWorker++, newWorker());
		}finally
		{
			_lock.unlock();
		}		
	}

	private void removeWorker() {
		Sandbox worker = null; 
		long accSuccess = 0;
		long accFailure = 0;
		
		ReentrantLock _lock = lock;
		lock.lock();
		try
		{
			worker = workers.remove(minWorker++);
			if(worker == null) return;
			accSuccess = worker.getSuccess();
			accFailure = worker.getFailure();
			acumulatedFailure +=accFailure;
			acumulatedSucess +=accSuccess;
		}finally
		{
			_lock.unlock();
		}				
		
		worker.shutdown();
		int waiting = 79;
		Thread.yield();
		while(worker.isAlive() && waiting > -40)
		{
			// Total de 60 segundos -> 79 ==> -39 = 60 steps de -2 em -2
			// sleep de  410((120-79)*10) a 1590((120-(-39))*10) ==> (410+1590)/2*60 = 60000ms = 60s
			try{Thread.sleep((120-waiting)*10);}catch(InterruptedException e){ }
			waiting-=2;
		}
		if(worker.isAlive())
		{
			log.severe("It's a sad thing, but worker " + worker.getId() + " was killed. Regret nothing, until its too late. Then regret everything.");
			try{worker.interrupt();}catch(Throwable t){}
		}
		lock.lock();
		try
		{
			acumulatedSucess  -=accSuccess;
			acumulatedFailure -=accFailure;
			accSuccess = worker.getSuccess();
			accFailure = worker.getFailure();
			acumulatedFailure +=accFailure;
			acumulatedSucess  +=accSuccess;
		}finally
		{
			_lock.unlock();
		}
	}

	private Class<? extends Sandbox> sandboxClass = null;
	private Sandbox newWorker() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigException
	{
		if(sandboxClass == null)
		{
			sandboxClass = Class.forName(config.getWorker().getImpl()).asSubclass(Sandbox.class);
		}
		final Sandbox worker = sandboxClass.newInstance();
		worker.setShutdownEventListener(this);
		addInterruptEventListener(worker);
		worker.startup(config.getWorker(), lcDispatcher, pDispatcher, config.getSandbox());
		worker.start();
		return worker;
	}

	/*
	 * RunnerMBean
	 * * * * * * * * * * * * * * * * * * * *
	 */
	@Override
	public void shutdown()
	{
		
		ready = false;
		lcDispatcher.fire(new LifecycleEvent(this, LifecycleEventType.stop, "Runner shutdown signal received"));
		final Iterator<InterruptEventListener> it = interruptListeners.iterator();
		while (it.hasNext()) it.next().shutdown();
		try
		{
			final ArrayList<String> names = new ArrayList<String>();
			final InitialContext ctx = new InitialContext();
			final NamingEnumeration<Binding> bindings = ctx.listBindings("java");
			while (bindings.hasMore()) names.add(bindings.next().getName());
			final Iterator<String> itNames = names.iterator();
			while (itNames.hasNext()) ctx.unbind(itNames.next());
			if (mbName != null) ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbName);
		}
		catch (final Throwable e) { pDispatcher.fire(new ProcessingEvent(this, ProcessingEventType.warning, "Error during shutdown", e)); }
		hasShutdown = true;
	}

	@Override public long getSuccessCount()
	{
		if (!ready) return -1;
		long success = 0;
		ReentrantLock _lock = lock;
		lock.lock();
		try
		{
			success = acumulatedSucess;
		}finally
		{
			_lock.unlock();
		}
		for (int i = minWorker; i < maxWorker; i++) 
		{
			Sandbox worker = workers.get(i);
			if(worker != null) { success += worker.getSuccess(); }
		}
		return success;
	}
	@Override public long getErrorCount()
	{
		if (!ready) return -1;
		long failure = 0;
		ReentrantLock _lock = lock;
		lock.lock();
		try
		{
			failure = acumulatedFailure;
		}finally
		{
			_lock.unlock();
		}
		for (int i = minWorker; i < maxWorker; i++) 
		{
			Sandbox worker = workers.get(i);
			if(worker != null) { failure += worker.getFailure(); }
		}
		return failure;
	}

	/*
	 * ReleaseResourceEventDispatcher
	 * InterruptEventDispatcher
	 * * * * * * * * * * * * * * * * * * * *
	 */
	@Override public void addReleaseResourceListener(final ReleaseResourceListener listener) { resourceListeners.add(listener); }
	@Override public void addInterruptEventListener(final InterruptEventListener listener) { interruptListeners.add(listener); }

	/*
	 * ShutdownEventListener
	 * * * * * * * * * * * * * * * * * * * *
	 */
	private void releaseResources(final Sandbox worker)
	{
		final Iterator<ReleaseResourceListener> it = resourceListeners.iterator();
		while (it.hasNext()) it.next().release(worker.getId());
		worker.release();
	}
	@Override
	public void signal(final Sandbox worker)
	{
		releaseResources(worker);
	}

	@Override
	public void abandon(final Sandbox worker)
	{
		releaseResources(worker);
	}

	@Override
	public void abort(final Sandbox worker)
	{
		releaseResources(worker);
		shutdown();
	}


	public static void main(String[] args)
	{
    		if (args.length < 1) usage();
		final File config = new File(args[0]);
		if (!config.exists()) usage();
		try
		{
			final RunnerConfig cfg = getConfig(config);
			final Runner instance = new Runner(cfg);
			final JNDIConfig jndi = cfg.getJndi();
			String jndiImpl;
			if (jndi == null || (jndiImpl = jndi.getImplementation()) == null) throw new ConfigException("Config entry required: /config/jndi/implementation");
			if (jndi.size() > 0) System.getProperties().putAll(jndi);
			((BindServices) Class.forName(jndiImpl).newInstance()).bind ( cfg, instance, new LogTrapperListener(SNMPTrap.createTrap(cfg.getSnmp())));
			Runtime.getRuntime().addShutdownHook(new Thread(instance.new ShutdownHook()));
			ManagementFactory.getPlatformMBeanServer().registerMBean
			(
				instance,
				mbName = new ObjectName
				(
					(new StringBuilder(256))
					.append(MBEAN_PATTERN)
					.append(ManagementFactory.getRuntimeMXBean().getName())
					.toString()
				)
			);
			instance.start();
		}
		catch (final Throwable e)
		{
			log.log(Level.SEVERE, "Runner start failure", e);
			usage();
		}
	}
	private static void usage()
	{
		log.log(Level.SEVERE, "Usage: Runner <config-xml>, where\t<config-xml> is the configuration XML file.");
		System.exit(1);
	}
	
	private static RunnerConfig getConfig(final File config) throws ConfigException
	{
		RunnerConfig ret;
		try
		{
			final Digester digester = new Digester();
			digester.setValidating(false);
			digester.addObjectCreate("config", RunnerConfig.class);
			digester.addFactoryCreate("config/worker", WorkerConfigFactory.class);
			digester.addBeanPropertySetter("config/worker/threads", "threads");
			digester.addBeanPropertySetter("config/worker/goal", "goal");
			digester.addBeanPropertySetter("config/worker/goalMeasure", "goalMeasure");
			digester.addBeanPropertySetter("config/worker/ramp", "ramp");
			digester.addBeanPropertySetter("config/worker/delay", "delay");
			digester.addBeanPropertySetter("config/worker/restartable", "restartable");
			digester.addBeanPropertySetter("config/worker/sleep", "sleep");
			digester.addBeanPropertySetter("config/worker/heartbeat", "heartbeat");
			digester.addSetNext("config/worker", "setWorker");

			JNDIConfig.setConfig(digester, "config", "setJndi");

			digester.addObjectCreate("config/sandbox", ConfigProperties.class);
			digester.addObjectCreate("config/sandbox/property", Property.class);
			digester.addSetProperties("config/sandbox/property");
			digester.addSetNext("config/sandbox/property", "add");
			digester.addSetNext("config/sandbox", "setSandbox");
			
			SNMPConfig.setConfig(digester, "config", "setSnmp");
			
			digester.addObjectCreate("config/datasources", DataSourcesConfig.class);
			digester.addFactoryCreate("config/datasources/jdbc", JDBCConfigFactory.class);
			digester.addObjectCreate ("config/datasources/jdbc/property", Property.class);
			digester.addSetProperties("config/datasources/jdbc/property");
			digester.addSetNext("config/datasources/jdbc/property", "add");
			digester.addSetNext("config/datasources/jdbc", "addJDBC");
			digester.addSetNext("config/datasources", "setDatasources");

			ConnectorsConfig.setConfig(digester, "config", "setConnectors");

			if ((ret = digester.parse(new ConfigReader(config))) == null) throw new ConfigException("Unexpected error: could not set configuration map");
			return ret;
		}
		catch (SAXException | IOException e)
		{
			throw new ConfigException("Invalid XML configuration file", e);
		}
	}

	@Override
	public int getWorkerCount() {
		return workers.size();
	}
}

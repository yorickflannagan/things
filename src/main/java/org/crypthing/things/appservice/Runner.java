package org.crypthing.things.appservice;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import org.crypthing.things.appservice.config.JNDIConfig;
import org.crypthing.things.appservice.config.RunnerConfig;
import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.snmp.EncodableString;
import org.crypthing.things.snmp.LifecycleEvent;
import org.crypthing.things.snmp.LifecycleEvent.LifecycleEventType;
import org.crypthing.things.snmp.LifecycleEventDispatcher;
import org.crypthing.things.snmp.LogTrapperListener;
import org.crypthing.things.snmp.ProcessingEvent;
import org.crypthing.things.snmp.ProcessingEvent.ProcessingEventType;
import org.crypthing.things.snmp.ProcessingEventDispatcher;
import org.crypthing.things.snmp.SNMPBridge;


public class Runner
implements	RunnerMBean,
		ShutdownEventListener,
		ReleaseResourceEventDispatcher,
		InterruptEventDispatcher
{
	private class ShutdownHook implements Runnable { @Override public void run() { if (!hasShutdown) shutdown(); } }
	private static Runner instance; 
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

	private final long heartbeat;
	private final String jmxaddr;
	private final String jmxport; 

	public Runner(final RunnerConfig cfg) throws ConfigException
	{
		try { Class.forName(cfg.getWorker().getImpl()).newInstance(); }
		catch (final Throwable e) { throw new ConfigException(e); }
		config = cfg;
		jmxaddr = config.getJVM().getJmx().getHost();
		jmxport = config.getJVM().getJmx().getPort();
		heartbeat = config.getJVM().getHeartbeat();
		lcDispatcher = new LifecycleEventDispatcher();
		pDispatcher = new ProcessingEventDispatcher();
		SNMPBridge bridge = null;
		try
		{
			bridge = SNMPBridge.newInstance
			(
				config.getSnmp().getProperty("org.crypthing.things.SNMPTrap"),
				config.getSnmp().getProperty("org.crypthing.things.batch.udpAddress"),
				config.getSnmp().getProperty("org.crypthing.things.batch.rootOID")
			);
		}
		catch (final Exception swallowed) { bridge = null; }
		final LogTrapperListener traps;
		try { traps = new LogTrapperListener(log, bridge); }
		catch (final Throwable e) { throw new ConfigException("Invalid SNMP config properties"); }
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
			lcDispatcher.fire(new LifecycleEvent(LifecycleEventType.start, new EncodableString("Runner initialization succeeded")));
			ready = true;
			Thread t = new Thread(new Heart());
			t.start();
			
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
						if (go>0) { addWorker(); }
						else { removeWorker(); }
					}
					try{Thread.sleep(adjustDelay);}catch(InterruptedException e){}
				} 
			}
		}
		catch (final Throwable e)
		{
			pDispatcher.fire(new ProcessingEvent(ProcessingEventType.error, "Could not launch new worker", e));
			shutdown();
		}
	}

	private void addWorker() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigException {
		ReentrantLock _lock = lock;
		lock.lock();
		try { workers.put(maxWorker++, newWorker()); }
		finally { _lock.unlock(); }		
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
		}
		finally { _lock.unlock(); }				
		
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
		lcDispatcher.fire(new LifecycleEvent(LifecycleEventType.stop, new EncodableString("Runner shutdown signal received")));
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
		catch (final Throwable e) { pDispatcher.fire(new ProcessingEvent(ProcessingEventType.warning, "Error during shutdown", e)); }
		hasShutdown = true;
	}
	@Override
	public long getSuccessCount()
	{
		if (!ready) return -1;
		long success = 0;
		ReentrantLock _lock = lock;
		lock.lock();
		try { success = acumulatedSucess; }
		finally { _lock.unlock(); }
		for (int i = minWorker; i < maxWorker; i++) 
		{
			Sandbox worker = workers.get(i);
			if(worker != null) { success += worker.getSuccess(); }
		}
		return success;
	}
	@Override
	public long getErrorCount()
	{
		if (!ready) return -1;
		long failure = 0;
		ReentrantLock _lock = lock;
		lock.lock();
		try { failure = acumulatedFailure; }
		finally { _lock.unlock(); }
		for (int i = minWorker; i < maxWorker; i++) 
		{
			Sandbox worker = workers.get(i);
			if(worker != null) { failure += worker.getFailure(); }
		}
		return failure;
	}
	@Override
	public String getInputArguments()
	{
		final StringBuilder builder = new StringBuilder(1024);
		final ListIterator<String> list = ManagementFactory.getRuntimeMXBean().getInputArguments().listIterator();
		while (list.hasNext()) builder.append(list.next()).append(" ");
		return builder.toString();
	}
	@Override
	public String[] getSystemProperties()
	{
		final Set<Entry<String, String>> map = ManagementFactory.getRuntimeMXBean().getSystemProperties().entrySet();
		final int size = map.size();
		final ArrayList<String> env = new ArrayList<String>(size);
		final Iterator<Entry<String, String>> it = map.iterator();
		while (it.hasNext())
		{
			final Entry<String, String> entry = it.next();
			env.add(entry.getKey() + "=" + entry.getValue());
		}
		final String[] ret = new String[size];
		return env.toArray(ret);
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
	@Override public void signal(final Sandbox worker) { releaseResources(worker); }
	@Override public void abandon(final Sandbox worker) { releaseResources(worker); }
	@Override
	public void abort(final Sandbox worker)
	{
		releaseResources(worker);
		shutdown();
	}
	@Override public int getWorkerCount() { return workers.size(); }


	public static void main(String[] args)
	{
    		if (args.length < 1) usage();
		try
		{
			final RunnerConfig cfg = getConfig(new FileInputStream(args[0]), Bootstrap.getSchema());
			instance = new Runner(cfg);
			instance.configfile = args[0];
			final JNDIConfig jndi = cfg.getJndi();
			String jndiImpl;
			if (jndi == null || (jndiImpl = jndi.getImplementation()) == null) throw new ConfigException("Config entry required: /config/jndi/implementation");
			if (jndi.size() > 0) System.getProperties().putAll(jndi);
			SNMPBridge bridge = null;
			try
			{
				bridge = SNMPBridge.newInstance
				(
					cfg.getSnmp().getProperty("org.crypthing.things.SNMPTrap"),
					cfg.getSnmp().getProperty("org.crypthing.things.batch.udpAddress"),
					cfg.getSnmp().getProperty("org.crypthing.things.batch.rootOID")
				);
			}
			catch (final Exception swallowed) { bridge = null; }
			((BindServices) Class.forName(jndiImpl).newInstance()).bind(cfg, instance, new LogTrapperListener(log, bridge));
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

	public static Runner getInstance() { return instance; }
	private static void usage()
	{
		log.log(Level.SEVERE, "Usage: Runner <config-xml>, where\t<config-xml> is the configuration XML file.");
		System.exit(1);
	}
	public static RunnerConfig getConfig(final InputStream config, final InputStream schema) throws ConfigException
	{
		return new RunnerConfig(new Config(config, schema));
	}

	private String[] env; 
	@Override
	public String[] getEnvironment() {
		if(env ==null)
		{
			env = Bootstrap.getEnv();
		}
		return env;
	}

	private String configfile;
	@Override
	public String getConfigFile() {
		return configfile;
	}
	
	private class Heart implements Runnable
	{

		@Override
		public void run() {
			while(!hasShutdown && heartbeat > 0)
			{
				final String msg = new StringBuilder().append("{\"jmx\":{\"address\":\"").append(jmxaddr).append("\",\"port\":")
									.append(jmxport).append("},\"success\":").append(getSuccessCount())
									.append(",\"failures\":").append(getErrorCount()).append(",\"workers\":")
									.append(getWorkerCount()).append("}").toString();
				lcDispatcher.fire(new LifecycleEvent(LifecycleEventType.heart, new EncodableString(msg)));
				try{Thread.sleep(heartbeat);}catch(InterruptedException e){}
				
			}
		}
		
	}
	
	
	
}

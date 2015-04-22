package org.crypthing.things.appservice;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.ObjectName;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.digester3.Digester;
import org.crypthing.things.SNMPTrap;
import org.crypthing.things.appservice.config.ConfigErrorHandler;
import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.ConfigProperties;
import org.crypthing.things.appservice.config.ConnectorConfigFactory;
import org.crypthing.things.appservice.config.ConnectorsConfig;
import org.crypthing.things.appservice.config.DataSourcesConfig;
import org.crypthing.things.appservice.config.JDBCConfigFactory;
import org.crypthing.things.appservice.config.JNDIConfig;
import org.crypthing.things.appservice.config.JNDIConfigFactory;
import org.crypthing.things.appservice.config.Property;
import org.crypthing.things.appservice.config.QueueConfigFactory;
import org.crypthing.things.appservice.config.RunnerConfig;
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
			BillingEventListener,
			ReleaseResourceEventDispatcher,
			InterruptEventDispatcher
{
	private class ShutdownHook implements Runnable { @Override public void run() { shutdown(); } }
	private static ObjectName mbName;

	private final RunnerConfig config;
	private final Set<ReleaseResourceListener> resourceListeners;
	private final Set<InterruptEventListener> interruptListeners;
	private final LifecycleEventDispatcher lcDispatcher;
	private final ProcessingEventDispatcher pDispatcher;
	private long success;
	private long failure;

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
			for (int i = 0, threads = config.getWorker().getThreads(); i < threads; i++) newWorker();
			lcDispatcher.fire(new LifecycleEvent(this, LifecycleEventType.start, "Runner initialization succeeded"));
		}
		catch (final Throwable e)
		{
			pDispatcher.fire(new ProcessingEvent(this, ProcessingEventType.error, "Could not launch new worker", e));
			shutdown();
		}
	}

	private void newWorker() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigException
	{
		final Sandbox worker = (Sandbox) Class.forName(config.getWorker().getImpl()).newInstance();
		worker.setShutdownEventListener(this);
		addInterruptEventListener(worker);
		worker.init(config.getWorker(), lcDispatcher, pDispatcher, this);
		worker.init(config.getSandbox(), lcDispatcher, pDispatcher, this);
		worker.start();
	}

	/*
	 * RunnerMBean
	 * * * * * * * * * * * * * * * * * * * *
	 */
	@Override
	public void shutdown()
	{
		lcDispatcher.fire(new LifecycleEvent(this, LifecycleEventType.stop, "Runner shutdown signal received"));
		final Iterator<InterruptEventListener> it = interruptListeners.iterator();
		while (it.hasNext()) it.next().interrupt();
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
	}

	@Override public long getSuccessCount() { return success; }
	@Override public long getErrorCount() { return failure; }

	/*
	 * BillingEventListener
	 * * * * * * * * * * * * * * * * * * * *
	 */
	@Override public void incSuccess() { success++; }
	@Override public void incFailure() { failure++; }

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
		worker.destroy();
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
    		if (args.length != 2) usage();
		final File config = new File(args[0]);
		final File schema = new File(args[1]);
		if (!config.exists() || !schema.exists()) usage();
		try
		{
			final RunnerConfig cfg = getConfig(config, schema);
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
					.append("org.crypthing.things.appservice:type=Runner,name=")
					.append(ManagementFactory.getRuntimeMXBean().getName())
					.toString()
				)
			);
			instance.start();
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
			usage();
		}
	}
	private static void usage()
	{
		System.err.println("Usage: Runner <config-xml> <config-xsd>, where");
		System.err.println("\t<config-xml> is the configuration XML file and");
		System.err.println("\t<config-xsd> is the configuration schema file.");
		System.exit(1);
	}
	private static RunnerConfig getConfig(final File config, final File schema) throws ConfigException
	{
		RunnerConfig ret;
		final SchemaFactory fac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			dbf.setSchema(fac.newSchema(schema));
			final DocumentBuilder doc = dbf.newDocumentBuilder();
			doc.setErrorHandler(new ConfigErrorHandler());
			doc.parse(config);

			final Digester digester = new Digester();
			digester.setValidating(false);
			digester.addObjectCreate("config", RunnerConfig.class);
			digester.addFactoryCreate("config/worker", WorkerConfigFactory.class);
			digester.addBeanPropertySetter("config/worker/threads", "threads");
			digester.addBeanPropertySetter("config/worker/delay", "delay");
			digester.addBeanPropertySetter("config/worker/restartable", "restartable");
			digester.addBeanPropertySetter("config/worker/sleep", "sleep");
			digester.addSetNext("config/worker", "setWorker");

			digester.addFactoryCreate("config/jndi", JNDIConfigFactory.class);
			digester.addObjectCreate("config/jndi/environment/property", Property.class);
			digester.addSetProperties("config/jndi/environment/property");
			digester.addSetNext("config/jndi/environment/property", "add");
			digester.addSetNext("config/jndi", "setJndi");
			
			digester.addObjectCreate("config/sandbox", ConfigProperties.class);
			digester.addObjectCreate("config/sandbox/property", Property.class);
			digester.addSetProperties("config/sandbox/property");
			digester.addSetNext("config/sandbox/property", "add");
			digester.addSetNext("config/sandbox", "setSandbox");
			
			digester.addObjectCreate("config/snmp", ConfigProperties.class);
			digester.addObjectCreate("config/snmp/property", Property.class);
			digester.addSetProperties("config/snmp/property");
			digester.addSetNext("config/snmp/property", "add");
			digester.addSetNext("config/snmp", "setSnmp");
			
			digester.addObjectCreate("config/datasources", DataSourcesConfig.class);
			digester.addFactoryCreate("config/datasources/jdbc", JDBCConfigFactory.class);
			digester.addObjectCreate ("config/datasources/jdbc/property", Property.class);
			digester.addSetProperties("config/datasources/jdbc/property");
			digester.addSetNext("config/datasources/jdbc/property", "add");
			digester.addSetNext("config/datasources/jdbc", "addJDBC");
			digester.addSetNext("config/datasources", "setDatasources");

			digester.addObjectCreate("config/mqxconnectors", ConnectorsConfig.class);
			digester.addFactoryCreate("config/mqxconnectors/mqxconnector", ConnectorConfigFactory.class);
			digester.addObjectCreate("config/mqxconnectors/mqxconnector/context", ConfigProperties.class);
			digester.addObjectCreate("config/mqxconnectors/mqxconnector/context/property", Property.class);
			digester.addSetProperties("config/mqxconnectors/mqxconnector/context/property");
			digester.addSetNext("config/mqxconnectors/mqxconnector/context/property", "add");
			digester.addSetNext("config/mqxconnectors/mqxconnector/context", "setContext");
			digester.addFactoryCreate("config/mqxconnectors/mqxconnector/queues/queue", QueueConfigFactory.class);
			digester.addObjectCreate("config/mqxconnectors/mqxconnector/queues/queue/property", Property.class);
			digester.addSetProperties("config/mqxconnectors/mqxconnector/queues/queue/property");
			digester.addSetNext("config/mqxconnectors/mqxconnector/queues/queue/property", "add");
			digester.addSetNext("config/mqxconnectors/mqxconnector/queues/queue", "add");
			digester.addSetNext("config/mqxconnectors/mqxconnector", "add");
			digester.addSetNext("config/mqxconnectors", "setConnectors");
			if ((ret = digester.parse(config)) == null) throw new ConfigException("Unexpected error: could not set configuration map");
			return ret;
		}
		catch (SAXException | ParserConfigurationException | IOException e)
		{
			throw new ConfigException("Invalid XML configuration file", e);
		}
	}
}

package org.crypthing.things.appservice;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.digester3.Digester;
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
import org.crypthing.things.appservice.config.WorkerConfig;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public final class Runner implements RunnerMBean
{
	private class ShutdownHook implements Runnable { @Override public void run() { shutdown(); } }
	private static ObjectName mbName;

	public Runner(final RunnerConfig cfg) throws ConfigException
	{
		// TODO:
		System.out.println(cfg.toString());
	}

	@Override
	public void shutdown()
	{
		// TODO: unbind JNDI
		// TODO Auto-generated method stub
		try { if (mbName != null) ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbName); }
		catch (final Throwable e) {}
	}

	@Override
	public long getCounter()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWorkers()
	{
		// TODO Auto-generated method stub
		return 0;
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
			final JNDIConfig jndi = cfg.getJndi();
			String jndiImpl;
			if (jndi == null || (jndiImpl = jndi.getImplementation()) == null) throw new ConfigException("Config entry required: /config/jndi/implementation");
			if (jndi.size() > 0) System.getProperties().putAll(jndi);
			final BindServices bnd =  (BindServices) Class.forName(jndiImpl).newInstance();
			bnd.bind(cfg);
			final Runner instance = new Runner(cfg);
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


			final InitialContext ctx = new InitialContext();
			Object fac = ctx.lookup("java:snmp");
			System.out.println(fac.getClass().getName());
			
			ctx.unbind("java:snmp");
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
		class ConfigErrorHandler implements ErrorHandler
		{
			@Override
			public void warning(SAXParseException exception) throws SAXException {}
			@Override
			public void error(SAXParseException exception) throws SAXException { throw new SAXException(exception); }
			@Override
			public void fatalError(SAXParseException exception) throws SAXException { throw new SAXException(exception); }
		}

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
			digester.addObjectCreate("config/worker", WorkerConfig.class);
			digester.addBeanPropertySetter("config/worker/class", "impl");
			digester.addBeanPropertySetter("config/worker/threads", "threads");
			digester.addBeanPropertySetter("config/worker/delay", "delay");
			digester.addSetNext("config/worker", "setWorker");

			digester.addFactoryCreate("config/jndi", JNDIConfigFactory.class);
			digester.addObjectCreate("config/jndi/environment/property", Property.class);
			digester.addSetProperties("config/jndi/environment/property");
			digester.addSetNext("config/jndi/environment/property", "add");
			digester.addSetNext("config/jndi", "setJndi");
			
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

package org.crypthing.things.appservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.digester3.Digester;
import org.crypthing.things.appservice.config.ClasspathConfig;
import org.crypthing.things.appservice.config.ConfigErrorHandler;
import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.ConfigProperties;
import org.crypthing.things.appservice.config.JMXConfig;
import org.crypthing.things.appservice.config.JMXConfigFactory;
import org.crypthing.things.appservice.config.JVMConfig;
import org.crypthing.things.appservice.config.Property;
import org.xml.sax.SAXException;

public final class Bootstrap
{
	public static void main(String[] args) throws ConfigException
	{
		if (args.length < 1) usage();
		final File schema = getSchema();
		for (int i = 0; i < args.length; i++)
		{
			System.out.print("Loading Runner for " + args[i] + "... ");
			try
			{
				final File config = new File(args[i]);
				if (!config.exists()) throw new ConfigException("Could not find configuration file " + args[i], new FileNotFoundException());
				final JVMConfig cfg = getJVMConfig(config, schema);
				Runtime.getRuntime().exec(getCmdLine(cfg, args[i]));
				System.out.println("Done!");
			}
			catch (final Throwable e)
			{
				System.out.println("Failed!");
				System.err.println("-------------------------------\n");
				e.printStackTrace();
				System.err.println("\n-------------------------------\n\n");
				usage();
			}
		}
	}
	private static void usage()
	{
		System.err.println("Usage: Bootstrap <config-xml-list>, where");
		System.err.println("\t<config-xml-list>... is a list of at least one service launching configuration XML file.");
		System.exit(1);
	}

	private static String getCmdLine(final JVMConfig config, final String arg)
	{
		/*
		 * see http://docs.oracle.com/cd/E26576_01/doc.312/e24936/tuning-java.htm#GSPTG00069
		 */
		final StringBuilder builder = new StringBuilder(256);
		builder.append(System.getProperty("java.home")).append("/bin/");
		builder.append(System.getProperty("os.name", "").toLowerCase().indexOf("win") >= 0 ? "java.exe" : "java");
		builder.append(" -server -Xbatch");
		if (config.getMinMemory() > 0) builder.append(" -Xms").append(config.getMinMemory()).append("m");
		if (config.getMaxMemory() > 0) builder.append(" -Xmx").append(config.getMaxMemory()).append("m");
		if (config.getVmflags() != null) builder.append(" ").append(config.getVmflags());
		final JMXConfig jmx = config.getJmx();
		builder.append(" -Djava.rmi.server.hostname=").append(jmx.getHost());
		builder.append(" -Dcom.sun.management.jmxremote.port=").append(jmx.getPort());
		if (jmx.getProperty("com.sun.management.jmxremote.ssl") == null) builder.append(" -Dcom.sun.management.jmxremote.ssl=false");
		if (jmx.getProperty("com.sun.management.jmxremote.authenticate") == null) builder.append(" -Dcom.sun.management.jmxremote.authenticate=false");
		addProperties(jmx.stringPropertyNames().iterator(), jmx, builder);
		builder.append(" -cp ").append(config.getClasspath().getClasspath());
		if (config.getProperties() != null) addProperties(config.getProperties().stringPropertyNames().iterator(), config.getProperties(), builder);
		builder.append(" ").append(Runner.class.getName()).append(" ").append(arg);
		return builder.toString();
	}
	private static void addProperties(final Iterator<String> keys, final Properties source, final StringBuilder builder)
	{
		while (keys.hasNext())
		{
			final String key = keys.next();
			builder.append(" -D").append(key).append("=").append(source.getProperty(key));
		}
	}

	private static final String CONFIG_SCHEMA_PATH = "/org/crypthing/things/appservice/config/config.xsd";
	static File getSchema() throws ConfigException
	{
		File ret;
		final URL url = Bootstrap.class.getResource(CONFIG_SCHEMA_PATH);
		if (url == null) throw new ConfigException("Could not find config schema", new FileNotFoundException());
		try
		{
			ret = new File(url.toURI());
			if (!ret.exists()) throw new ConfigException("Could not find config schema", new FileNotFoundException());
		}
		catch (final URISyntaxException e) { throw new ConfigException("Could not find config schema", e); }
		return ret;
	}

	static JVMConfig getJVMConfig(final File config, final File schema) throws ConfigException
	{
		JVMConfig ret;
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
			digester.addObjectCreate("config/jvm", JVMConfig.class);
			digester.addBeanPropertySetter("config/jvm/minMemory", "minMemory");
			digester.addBeanPropertySetter("config/jvm/maxMemory", "maxMemory");
			digester.addBeanPropertySetter("config/jvm/vmflags", "vmflags");

			digester.addFactoryCreate("config/jvm/jmx", JMXConfigFactory.class);
			digester.addObjectCreate("config/jvm/jmx/property", Property.class);
			digester.addSetProperties("config/jvm/jmx/property");
			digester.addSetNext("config/jvm/jmx/property", "add");
			digester.addSetNext("config/jvm/jmx", "setJmx");

			digester.addObjectCreate("config/jvm/classpath", ClasspathConfig.class);
			digester.addCallMethod("config/jvm/classpath/path", "add", 0);
			digester.addSetNext("config/jvm/classpath", "setClasspath");

			digester.addObjectCreate("config/jvm/properties", ConfigProperties.class);
			digester.addObjectCreate("config/jvm/properties/property", Property.class);
			digester.addSetProperties("config/jvm/properties/property");
			digester.addSetNext("config/jvm/properties/property", "add");
			digester.addSetNext("config/jvm/properties", "setProperties");

			if ((ret = digester.parse(config)) == null) throw new ConfigException("Unexpected error: could not set configuration map");
			return ret;
		}
		catch (SAXException | ParserConfigurationException | IOException e) { throw new ConfigException("Invalid XML configuration file", e); }
	}
}

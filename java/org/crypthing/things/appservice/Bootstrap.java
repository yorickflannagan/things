package org.crypthing.things.appservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
import org.crypthing.things.appservice.config.JVMConfig;
import org.crypthing.things.appservice.config.Property;
import org.xml.sax.SAXException;

public final class Bootstrap
{
	public static void main(String[] args)
	{
		if (args.length < 1) usage();
		try
		{
			final File config = new File(args[0]);
			final File schema = getSchema();
			if (!config.exists() || !schema.exists()) throw new ConfigException("Invalid configuration files", new FileNotFoundException());
			final JVMConfig cfg = getConfig(config, schema);
			System.out.println(cfg);
			System.out.println("Schema = " + schema.getAbsolutePath());
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
			usage();
		}
	}
	private static void usage()
	{
		System.err.println("Usage: Bootstrap <config-xml> [JMXName], where");
		System.err.println("\t<config-xml> is the configuration XML file and");
		System.err.println("\t[JMXName] is the JMX name to launch Runner.");
		System.exit(1);
	}

	private static final String CONFIG_SCHEMA_FILE = "config.xsd";
	private static final String CONFIG_SCHEMA_PATH = "/org/crypthing/things/appservice/config/";
	private static File getSchema() throws ConfigException
	{
		final File workaround = new File(CONFIG_SCHEMA_FILE);
		if (workaround.exists()) workaround.delete();
		final File schema = new File(CONFIG_SCHEMA_FILE);
		schema.deleteOnExit();
		try
		{
			final OutputStream out = new FileOutputStream(schema);
			try
			{
				final InputStream in = Bootstrap.class.getResourceAsStream(CONFIG_SCHEMA_PATH + CONFIG_SCHEMA_FILE);
				if (in == null) throw new ConfigException("Could not find config schema in JAR file");
				try
				{
					final byte[] buffer = new byte[4096];
					int i;
					while ((i = in.read(buffer)) != -1) out.write(buffer, 0, i);
				}
				finally { in.close(); }
			}
			finally { out.close(); }
		}
		catch (final Throwable e) { throw new ConfigException("Could not load config schema", e); }
		return schema;
	}

	private static JVMConfig getConfig(final File config, final File schema) throws ConfigException
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

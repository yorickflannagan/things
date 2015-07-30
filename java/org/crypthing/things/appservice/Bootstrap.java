package org.crypthing.things.appservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
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
import org.crypthing.things.appservice.config.ConfigReader;
import org.crypthing.things.appservice.config.JMXConfig;
import org.crypthing.things.appservice.config.JMXConfigFactory;
import org.crypthing.things.appservice.config.JVMConfig;
import org.crypthing.things.appservice.config.Property;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class Bootstrap
{
	public static void main(String[] args) throws ConfigException
	{
		if (args.length < 1) usage();
		final File schema = getSchema();
		final String[] env = getEnv();
		for (int i = 0; i < args.length; i++)
		{
			System.out.println("Loading Runner for " + args[i] + "... ");
			try
			{
				final File config = new File(args[i]);
				if (!config.exists()) throw new ConfigException("Could not find configuration file " + args[i], new FileNotFoundException());
				final JVMConfig cfg = getJVMConfig(config, schema);
				final String cmd = getCmdLine(cfg, args[i]);
				System.out.print("***************\nwith command line: [");
				System.out.print(cmd);
				System.out.println("]");

				final Process pid = Runtime.getRuntime().exec(cmd, env);
				int exitCode = waitForProcess(pid);

				String out = readFully(pid.getErrorStream());
				System.out.println("***************\nError output:");
				System.out.println(out);
				out = readFully(pid.getInputStream());
				System.out.println("***************\nDefault output:");
				System.out.println(out);
				if (exitCode == 0) System.out.println("***************\nDone!\n");
				else System.out.println("***************\nFailed with exit code " + exitCode + "\n");
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
	private static String readFully(final InputStream stream)
	{
		final char[] buffer = new char[128];
		final InputStreamReader reader = new InputStreamReader(stream);
		final StringBuilder builder = new StringBuilder(512);
		int i;
		try { while (stream.available() > 0 && (i = reader.read(buffer)) > 0) builder.append(buffer, 0, i); }
		catch (final Exception e) {}
		return builder.toString();
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
		final String filesep = System.getProperty("file.separator", "/");
		final StringBuilder builder = new StringBuilder(256);
		builder.append(System.getProperty("java.home")).append(filesep).append("bin").append(filesep);
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
	private static String[] getEnv()
	{
		final int size = System.getenv().size();
		final ArrayList<String> env = new ArrayList<String>(size);
		final Iterator<String> keys = System.getenv().keySet().iterator();
		while (keys.hasNext())
		{
			final String key = keys.next();
			env.add(key + "=" + System.getenv(key));
		}
		final String[] ret = new String[size];
		return env.toArray(ret);
	}
	private static int waitForProcess(final Process p)
	{
		int ret;
		try
		{
			Thread.sleep(3000);
			ret = p.exitValue();
		}
		catch (final Throwable e) { ret = 0; }
		return ret;
	}

	private static final String CONFIG_SCHEMA_PATH = "config.xsd";
	static File getSchema() throws ConfigException
	{
		final File workaround = new File(CONFIG_SCHEMA_PATH);
		if (workaround.exists()) workaround.delete();
		final File ret = new File(CONFIG_SCHEMA_PATH);
		ret.deleteOnExit();
		try
		{
			final OutputStream out = new FileOutputStream(ret);
			try
			{
				final InputStream in = Bootstrap.class.getResourceAsStream("/" + CONFIG_SCHEMA_PATH);
				if (in == null) throw new ConfigException("Could not find configuration schema in JAR file");
				try
				{
					final byte[] buffer = new byte[4096];
					int i;
					while ((i = in.read(buffer)) != -1) out.write(buffer, 0, i);
				}
				finally { in.close(); }
			}
			finally { out.close(); }
			return ret;
		}
		catch (final IOException e) { throw new ConfigException("Could not load configuration schema", e); }
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
			doc.parse(new InputSource(new ConfigReader(config)));

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

			if ((ret = digester.parse(new ConfigReader(config))) == null) throw new ConfigException("Unexpected error: could not set configuration map");
			return ret;
		}
		catch (SAXException | ParserConfigurationException | IOException e) { throw new ConfigException("Invalid XML configuration file", e); }
	}
}

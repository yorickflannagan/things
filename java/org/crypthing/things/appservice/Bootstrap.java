package org.crypthing.things.appservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.JMXConfig;
import org.crypthing.things.appservice.config.JVMConfig;
import org.crypthing.things.config.Config;

public final class Bootstrap
{
	public static void main(String[] args) throws ConfigException
	{
		if (args.length < 1) usage();
		final InputStream schema = getSchema();
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
	public static InputStream getSchema() throws ConfigException
	{
		final InputStream ret;
		if ((ret = Bootstrap.class.getResourceAsStream(CONFIG_SCHEMA_PATH)) == null) throw new ConfigException("Could not load configuration schema");
		return ret;
	}
	public static JVMConfig getJVMConfig(final File config, final InputStream schema) throws ConfigException
	{
		try
		{
			final InputStream cfg = new FileInputStream(config);
			try
			{
				final Config xml = new Config(cfg, schema);
				return new JVMConfig(xml, xml.getNodeValue("/config/jvm"));
			}
			finally { cfg.close(); }
		}
		catch (final IOException | org.crypthing.things.config.ConfigException e) { throw new ConfigException("Invalid XML configuration file", e); }
	}
}

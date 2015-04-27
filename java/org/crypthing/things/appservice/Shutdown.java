package org.crypthing.things.appservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.JVMConfig;

public final class Shutdown
{

	public static void main(String[] args) throws ConfigException
	{
		if (args.length < 1) usage();
		final File schema = Bootstrap.getSchema();
		for (int i = 0; i < args.length; i++)
		{
			System.out.print("Shutting down service specified in " + args[i] + "... ");
			try
			{
				final File config = new File(args[i]);
				if (!config.exists()) throw new ConfigException("Could not find configuration file " + args[i], new FileNotFoundException());
				final JVMConfig cfg = Bootstrap.getJVMConfig(config, schema);
				if (cfg.getJmx() == null) throw new ConfigException("Configuration must have a JMX entry");
				shutdown(cfg.getJmx().getHost(), cfg.getJmx().getPort());
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
		System.err.println("Usage: Shutdown <config-xml-list>, where");
		System.err.println("\t<config-xml-list>... is a list of at least one service launching configuration XML file.");
		System.exit(1);
	}
	private static void shutdown(final String host, final String port) throws IOException, JMException
	{
		final JMXConnector jmxc = JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi"));
		try
		{
			final MBeanServerConnection mbean = jmxc.getMBeanServerConnection();
			final Iterator<ObjectName> it = mbean.queryNames(new ObjectName(Runner.MBEAN_PATTERN + "*"), null).iterator(); 
			while (it.hasNext()) mbean.invoke(it.next(), "shutdown", new Object[] {}, new String[] {});
		}
		finally { jmxc.close(); }
	}
}

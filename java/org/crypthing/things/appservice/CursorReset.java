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
import org.crypthing.things.appservice.db.Cursor;

public class CursorReset {
	public static void main(String[] args) throws ConfigException
	{
		if (args.length != 2) usage();
		System.out.print("Reseting  cursor " + args[1] + "specified in " + args[0] + "... ");
		try
		{
			final File config = new File(args[0]);
			if (!config.exists()) throw new ConfigException("Could not find configuration file " + args[0], new FileNotFoundException());
			final JVMConfig cfg = Bootstrap.getJVMConfig(config, Bootstrap.getSchema());
			if (cfg.getJmx() == null) throw new ConfigException("Configuration must have a JMX entry");
			reset(cfg.getJmx().getHost(), cfg.getJmx().getPort(), args[1]);
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
	private static void usage()
	{
		System.err.println("Usage: CursorReset <config-xml> <cursorname>, where");
		System.err.println("\t<config-xml> is the service launching configuration XML file;");
		System.err.println("\t<cursorname> is the name of the cursor wanted to reset.");
		System.exit(1);
	}
	private static void reset(final String host, final String port, String name) throws IOException, JMException
	{
		final JMXConnector jmxc = JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi"));
		try
		{
			final MBeanServerConnection mbean = jmxc.getMBeanServerConnection();
			final Iterator<ObjectName> it = mbean.queryNames(new ObjectName(Cursor.MBEAN_PATTERN + name), null).iterator(); 
			while (it.hasNext()) mbean.invoke(it.next(), "reset", new Object[] {}, new String[] {});
		}
		finally { jmxc.close(); }
	}

}

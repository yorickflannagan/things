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

public final class Status
{

	public static void main(String[] args)
	{
		if (args.length < 1) usage();
		try
		{
			final File schema = Bootstrap.getSchema();
			final File config = new File(args[0]);
			if (!config.exists()) throw new ConfigException("Could not find configuration file " + args[0], new FileNotFoundException());
			final JVMConfig cfg = Bootstrap.getJVMConfig(config, schema);
			if (cfg.getJmx() == null) throw new ConfigException("Configuration must have a JMX entry");
			viewStatus
			(
				cfg.getJmx().getHost(),
				cfg.getJmx().getPort(),
				args.length > 1 ? Integer.parseInt(args[1]) : 1,
				args.length > 2 ? Long.parseLong(args[2]) : 0
			);
		}
		catch (final Throwable e)
		{
			System.out.println("Failed!");
			System.err.println("-------------------------------\n");
			e.printStackTrace();
			System.err.println("\n-------------------------------\n\n");
		}
	}
	private static void usage()
	{
		System.err.println("Usage: Status <config-xml> <count> <interval>, where");
		System.err.println("\t<config-xml> is the service launching configuration XML file;");
		System.err.println("\t<count> is the number of samples to take before terminating; ");
		System.err.println("\t<interval> is the interval in miliseconds between each sample.");
		System.exit(1);
	}
	private static void viewStatus(final String host, final String port, final int count, final long interval)  throws IOException, JMException, InterruptedException
	{
		final String service = host + ":" + port;
		final JMXConnector jmxc = JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + service + "/jmxrmi"));
		try
		{
			final MBeanServerConnection mbean = jmxc.getMBeanServerConnection();
			final Iterator<ObjectName> it = mbean.queryNames(new ObjectName(Runner.MBEAN_PATTERN + "*"), null).iterator();
			final ObjectName obName = it.hasNext() ? it.next() : null;
			if (obName == null)
			{
				System.err.println("Given URL has no JMX service listening...");
				usage();
			}
			long lastMeasure = getValue(mbean, obName, "SuccessCount");
			long lastStamp = System.currentTimeMillis();
			int i = 0;
			System.out.println("Listening to service " + service);
			System.out.println("Success\tError\tThroughput");
			do
			{
				Thread.sleep(interval);
				long currentMeasure = getValue(mbean, obName, "SuccessCount");
				long currentStamp = System.currentTimeMillis();
				StringBuilder builder = new StringBuilder(128);
				builder.append(currentMeasure).append("\t");
				builder.append(getValue(mbean, obName, "ErrorCount")).append("\t");
				long hit = (currentStamp - lastStamp) / 1000;
				if (hit > 0) builder.append((currentMeasure - lastMeasure) / hit).append("/s");
				else builder.append("?");
				lastMeasure = currentMeasure;
				lastStamp = currentStamp;
				System.out.println(builder.toString());
			}
			while (++i < count);
		}
		finally { jmxc.close(); }
	}
	private static long getValue(final MBeanServerConnection mbean, ObjectName obName, final String method) throws IOException, JMException
	{
		final Object ret = mbean.getAttribute(obName, method);
		if (!(ret instanceof Long)) throw new JMException(method + " method has returned an invalid value");
		return (long) ret;
	}
}

package org.crypthing.things.appservice;

import java.io.IOException;
import java.util.Iterator;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.crypthing.things.appservice.JMXConnection.ConnectionHolder;
import org.crypthing.things.appservice.config.JVMConfig;
import org.crypthing.things.appservice.diagnostic.Network;
import org.crypthing.things.config.ConfigException;

public final class Status
{
	public static void main(String[] args)
	{
		if (args.length < 1) usage();
		try
		{
			final JVMConfig cfg = Bootstrap.getJVMConfig(args[0]);
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
		ConnectionHolder ch = JMXConnection.getConnection(host, port);
		final JMXConnector jmxc = ch.ret == 0 ? ch.connection : null;
		if(jmxc != null)
		{
			try
			{
				final MBeanServerConnection mbean = jmxc.getMBeanServerConnection();
				final Iterator<ObjectName> it = mbean.queryNames(new ObjectName(Runner.MBEAN_PATTERN + "*"), null).iterator();
				final ObjectName obName = it.hasNext() ? it.next() : null;
				if (obName == null)
				{
					System.err.println("Given URL has no Runner " + Runner.MBEAN_PATTERN + " or it was not started yet");
					System.exit(1);
				}
	
				long lastMeasure = getValue(mbean, obName, "SuccessCount");
				long lastStamp = System.currentTimeMillis();
				int i = 0;
				System.out.println("Listening to [" + host + ":" +  port + "]");
				System.out.println("Success\tError\tThroughput\tWorkers");
				do
				{
					Thread.sleep(interval);
					long currentMeasure = getValue(mbean, obName, "SuccessCount");
					long currentStamp = System.currentTimeMillis();
					StringBuilder builder = new StringBuilder(128);
					builder.append(currentMeasure).append("\t");
					builder.append(getValue(mbean, obName, "ErrorCount").toString()).append("\t");
					long hit = (currentStamp - lastStamp) / 1000;
					if (hit > 0) builder.append((currentMeasure - lastMeasure) / hit).append("/s");
					else builder.append("?");
					builder.append("\t\t").append(getValue(mbean, obName, "WorkerCount").toString()).append("\t");
					lastMeasure = currentMeasure;
					lastStamp = currentStamp;
					System.out.println(builder.toString());
				}
				while (++i < count);
			}
			finally { jmxc.close(); }
		}
		else
		{
			System.out.println(Network.getMessage(ch.ret, host, port));
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getValue(final MBeanServerConnection mbean, ObjectName obName, final String method) throws IOException, JMException
	{
		final Object ret = mbean.getAttribute(obName, method);
		return (T) ret;
	}
}

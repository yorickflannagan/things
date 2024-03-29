package org.crypthing.things.appservice;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.crypthing.things.appservice.JMXConnection.ConnectionHolder;
import org.crypthing.things.appservice.config.JVMConfig;
import org.crypthing.things.appservice.db.Cursor;
import org.crypthing.things.appservice.diagnostic.Network;
import org.crypthing.things.config.ConfigException;

public class CursorInfo {

	public static void main(String[] args)
	{
		if (args.length < 2) usage();
		try
		{
			final JVMConfig cfg = Bootstrap.getJVMConfig(new FileInputStream(args[0]), Bootstrap.getSchema());
			if (cfg.getJmx() == null) throw new ConfigException("Configuration must have a JMX entry");
			viewStatus
			(
				cfg.getJmx().getHost(),
				cfg.getJmx().getPort(),
				args[1],
				args.length > 2 ? Integer.parseInt(args[2]) : 1,
				args.length > 3 ? Long.parseLong(args[3]) : 0
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
		System.err.println("Usage: CursorInfo <config-xml> <cursorname> <count> <interval>, where");
		System.err.println("\t<config-xml> is the service launching configuration XML file;");
		System.err.println("\t<cursorname> is the name of the cursor wanted; ");
		System.err.println("\t<count> is the number of samples to take before terminating; ");
		System.err.println("\t<interval> is the interval in miliseconds between each sample.");
		System.exit(1);
	}
	private static void viewStatus(final String host, final String port, String name, final int count, final long interval)  throws IOException, JMException, InterruptedException
	{
		ConnectionHolder ch = JMXConnection.getConnection(host, port);
		final JMXConnector jmxc = ch.ret == 0 ? ch.connection : null;
		if(jmxc != null)
		{
			try
			{
				final MBeanServerConnection mbean = jmxc.getMBeanServerConnection();
				final Iterator<ObjectName> it = mbean.queryNames(new ObjectName(Cursor.MBEAN_PATTERN + name), null).iterator();
				final ObjectName obName = it.hasNext() ? it.next() : null;
				if (obName == null)
				{
					System.err.println("Given URL has no Cursor " + Cursor.MBEAN_PATTERN + name + " or it was not started yet");
					System.exit(1);
				}
				int i = 0;
				System.out.println("Cursor "+ name + " from [" + host + ":" +  port + "]");
				String sql = getValue(mbean, obName, "SQLStatement");
				System.out.print("SQL:\t[");
				System.out.println(sql);
				StringBuilder builder = new StringBuilder(128);
				do
				{
					Thread.sleep(interval);
					builder.setLength(0);
									
					long remainder = getValue(mbean, obName, "Remainder");
					long stock = getValue(mbean, obName, "Stock");
					long lastRecord = getValue(mbean, obName, "LastRecord");
					
					builder.append("***********************************************\n");
					builder.append("Registros em memória    :\t");
					builder.append(stock).append("\n");
					builder.append("Registros a Processar   :\t");
					builder.append(remainder).append("\n");
					builder.append("Último Indice Processado:\t");
					builder.append(lastRecord).append("\n");
					
					System.out.println(builder.toString());
				}
				while (++i < count);
				System.out.println("***********************************************");
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

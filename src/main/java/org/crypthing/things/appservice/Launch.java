package org.crypthing.things.appservice;

import java.io.IOException;
import java.util.Iterator;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.crypthing.things.appservice.JMXConnection.ConnectionHolder;
import org.crypthing.things.appservice.diagnostic.Network;

public final class Launch
{
	public static void main(String[] args)
	{
		try
		{
			if (args.length == 0) throw new IndexOutOfBoundsException("Invalid arguments");
			int i = 0;
			String host = null, port = null, cfgFile = null, home = null;
				while (i < args.length)
				{
					final String cmd = args[i++];
					if (cmd.compareToIgnoreCase("--agent") == 0)
					{
						if (i + 2 > args.length) throw new IndexOutOfBoundsException("Invalid arguments");
						host = args[i++];
						port = args[i++];
					}
					else if (cmd.compareToIgnoreCase("--home") == 0)
					{
						if (i + 1 > args.length) throw new IndexOutOfBoundsException("Invalid arguments");
						home = args[i++];
					}
					else if (cmd.compareToIgnoreCase("--runner") == 0)
					{
						if (i + 1 > args.length) throw new IndexOutOfBoundsException("Invalid arguments");
						cfgFile = args[i++];
					}
					else cfgFile = cmd;
					if (host != null && port != null && cfgFile != null && home != null ) launch(host, port, cfgFile, home);
				}
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
		System.err.println("Usage: Launch --agent java.rmi.server.hostname=<server> com.sun.management.jmxremote.port=<port> --runner <config-xml-list>, where");
		System.err.println("\t--agent  asks for launch through agent running at java.rmi.server.hostname and com.sun.management.jmxremote.port. Required.");
		System.err.println("\t--home  working directory  to launch from Required.");
		System.err.println("\t--runner <config-xml-list>... is a list of at least one service launching configuration XML file. Required.");
		System.err.println("\tBoth options should be specified in that order");
		System.exit(1);
	}
	private static void launch(final String host, final String port, final String arg, final String home)  throws IOException, JMException
	{
		System.out.print("Launching service " + arg + " through host " + host + " and port " + port + " with home " + home + " - ");
		ConnectionHolder holder = JMXConnection.getConnection(host, port);
		if(holder.ret !=0)
		{
			System.out.println(Network.getMessage(holder.ret, host, port));
			return;
		}
		final JMXConnector jmxc = holder.connection;
		try
		{
			final MBeanServerConnection mbean = jmxc.getMBeanServerConnection();
			final Iterator<ObjectName> it = mbean.queryNames(new ObjectName(Bootstrap.MBEAN_PATTERN + "*"), null).iterator(); 
			while (it.hasNext())
			{
				ObjectName mb = it.next();
				int ret = (Integer) mbean.invoke(mb, "launch", new Object[] { arg, home, Bootstrap.getEnv() }, new String[] { String.class.getName(), String.class.getName(), String[].class.getName() });
				if (ret == 0) System.out.println("... lauched! with " + mb.getCanonicalName());
				else System.err.println("... failed! Ret:"+ ret + ". Obname: " + mb.getCanonicalName());
			}
		}
		finally { try{ jmxc.close();} catch(Exception e) {} }
	}
}
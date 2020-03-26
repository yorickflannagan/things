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
import org.crypthing.things.appservice.diagnostic.Network;
import org.crypthing.things.config.ConfigException;

public final class Shutdown
{
	public static void main(String[] args)
	{
		try
		{
			if (args.length == 0) throw new IndexOutOfBoundsException("Invalid arguments");
			int i = 0;
			String host = null, port = null, pattern = null, name = null;
			int timeout = 0;
			boolean force = false;
			name = null;

			if (args[0].startsWith("--"))
			{
				while (i < args.length)
				{
					force = false;
					timeout = 0;
					final String cmd = args[i++];
					if (cmd.compareToIgnoreCase("--stop_by_agent") == 0)
					{
						if (i + 4 > args.length) throw new IndexOutOfBoundsException("Invalid arguments");
						host = args[i++];
						port = args[i++];
						name = args[i++];
						timeout = Integer.parseInt(args[i++]);
						pattern = Bootstrap.MBEAN_PATTERN;
					}
					else if (cmd.compareToIgnoreCase("--force_stop_by_agent") == 0)
					{
						if (i + 4 > args.length) throw new IndexOutOfBoundsException("Invalid arguments");
						host = args[i++];
						port = args[i++];
						name = args[i++];
						timeout = Integer.parseInt(args[i++]);
						pattern = Bootstrap.MBEAN_PATTERN;
						force = true;
					}
					else if (cmd.compareToIgnoreCase("--agent") == 0)
					{
						if (i + 2 > args.length) throw new IndexOutOfBoundsException("Invalid arguments");
						host = args[i++];
						port = args[i++];
						pattern = Bootstrap.MBEAN_PATTERN;
					}
					else if (cmd.compareToIgnoreCase("--runner") == 0)
					{
						if (i + 1 > args.length) throw new IndexOutOfBoundsException("Invalid arguments");
						final JVMConfig cfg = Bootstrap.getJVMConfig(new FileInputStream(args[i++]),
								Bootstrap.getSchema());
						if (cfg.getJmx() == null) throw new ConfigException("Configuration must have a JMX entry");
						host = cfg.getJmx().getHost();
						port = cfg.getJmx().getPort();
						pattern = Runner.MBEAN_PATTERN;
					}
					else throw new IndexOutOfBoundsException("Invalid arguments");
					if (host == null || port == null || pattern == null)
						throw new IndexOutOfBoundsException("Invalid arguments");
					shutdown(host, port, pattern, name, force, timeout);
				}
			}
			else
			{
				pattern = Runner.MBEAN_PATTERN;
				for (i = 0; i < args.length; i++)
				{
					final JVMConfig cfg = Bootstrap.getJVMConfig(new FileInputStream(args[i++]), Bootstrap.getSchema());
					if (cfg.getJmx() == null) throw new ConfigException("Configuration must have a JMX entry");
					host = cfg.getJmx().getHost();
					port = cfg.getJmx().getPort();
					if (host == null || port == null || pattern == null)
						throw new IndexOutOfBoundsException("Invalid arguments");
					shutdown(host, port, pattern, null, false, 0);
				}
			}
		}
		catch (final Throwable e)
		{
			System.out.println("Failed!");
			System.err.println("-------------------------------\n");
			e.printStackTrace();
			System.err.println("\n-------------------------------\n\n");
			usage();
			System.exit(-1);
		}
	}

	private static void usage()
	{
		System.err.println(
				"Usage: Shutdown --agent <server> <port> --runner <config-xml> --stop_by_agent <host> <port> <name> <timeout in seconds> --force_stop_by_agent <host> <port> <name> <timeout in seconds>, where");
		System.err.println("\t--agent  asks for shutdown agent running at server and port. Optional.");
		System.err.println("\t--stop_by_agent ask for an agent to stop configuration with a name. Optional.");
		System.err.println(
				"\t--force_stop_by_agent  as stop_by_agent, but kills the process if it does not end. Optional.");
		System.err.println("\tOptions can be repeated.");
		System.exit(1);
	}

	private static void shutdown(final String host, final String port, final String pattern, String name, boolean force,
			int timeout) throws IOException, JMException
	{
		System.out.println("Shutting down service at host " + host + " and port " + port
				+ (name != null ? (" with agent. by name: " + name) : ""));
		ConnectionHolder holder = JMXConnection.getConnection(host, port);
		if(holder.ret !=0)
		{
			System.out.println(Network.getMessage(holder.ret, host, port));
			System.exit(holder.ret);
		}
		final JMXConnector jmxc = holder.connection;
		boolean done = false;
		int ret=0; 
		try
		{
			final MBeanServerConnection mbean = jmxc.getMBeanServerConnection();
			Iterator<ObjectName> it = mbean.queryNames(new ObjectName(pattern + "*"), null).iterator(); 
			
			while (it.hasNext()) 
			{
				if(name != null)
				{
					if(force)
					{
						done = true;
						System.out.println("Calling ForceStop.");
						ret = (Integer) mbean.invoke(it.next(), "forceStop", new Object[] { name, (Integer)timeout }, new String[] { String.class.getName(), "int"});
					}
					else
					{
						done = true;
						System.out.println("Calling Stop.");
						ret = (Integer) mbean.invoke(it.next(), "stop", new Object[] { name,  (Integer)timeout }, new String[] { String.class.getName(), "int"});
					}
				}
				else
				{
					done = true;
					System.out.println("Calling Shutdown.");
					mbean.invoke(it.next(), "shutdown", new Object[] {}, new String[] {});
				}
			}
		}
		finally { try{ jmxc.close();} catch(Exception e) {} }
		if(done)
		{
			switch (ret)
			{
				case BootstrapMBean.OK:
					System.out.println("Done.");
					break;
				case BootstrapMBean.ALREADY_STOPPED:
					System.out.println("Shutdown already called.");
					break;
				case BootstrapMBean.COULD_NOT_STOP:
					System.out.println("Could not stop process.");
					break;
				case BootstrapMBean.EXCEPTION_WHILE_FORCE_STOP:
					System.out.println("Could not stop process. Exception on Forced Stop.");
					break;
				case BootstrapMBean.EXCEPTION_WHILE_STOP:
					System.out.println("Could not stop process. Exception on Stop.");
					break;
				case BootstrapMBean.NO_PROCESS_FOUND:
					System.out.println("Could not stop process. No process found.");
					break;
				// case BootstrapMBean.STOP_FORCED:
				// 	System.out.println("Stop was forced.");
				// 	break;
				case BootstrapMBean.STOP_TIMEOUT:
					System.out.println("Stop timed out. Wait a little and list instances that are alive or kill it with --force_stop_by_agent.");
					break;
				default:
					System.out.println("Something happened, I don't know why and I don't know how:" + ret);
				break;
			}
			System.exit(ret);
		}
		else  System.out.println("Done Nothing? MBEAN Pattern: [" + pattern + "*" + "]");
		System.exit(-10);
	}
}

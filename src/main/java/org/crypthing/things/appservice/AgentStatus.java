package org.crypthing.things.appservice;

import java.util.Iterator;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.crypthing.things.appservice.JMXConnection.ConnectionHolder;
import org.crypthing.things.appservice.diagnostic.Network;


public final class AgentStatus
{
	public static void main(String[] args)
	{
		String host = null, port = null, pattern = null;

		if (args.length < 1) usage();
		try
		{
			String cmd = args[0];
			if(cmd.compareToIgnoreCase("--agent") == 0)
			{
				if (3 != args.length) throw new IndexOutOfBoundsException("Invalid arguments");
				host = args[1];
				port = args[2];
				pattern = Bootstrap.MBEAN_PATTERN;
			}
			else throw new IndexOutOfBoundsException("Invalid arguments");


			ConnectionHolder holder = JMXConnection.getConnection(host, port);
			if(holder.ret !=0)
			{
				System.out.println(Network.getMessage(holder.ret, host, port));
				System.exit(holder.ret);
			}
			final JMXConnector jmxc = holder.connection;
			try
			{
				final MBeanServerConnection mbean = jmxc.getMBeanServerConnection();
				Iterator<ObjectName> it = mbean.queryNames(new ObjectName(pattern + "*"), null).iterator(); 
				String[] processes = (String[])mbean.invoke(it.next(), "list", new Object[] {}, new String[] {});
				if(processes ==null) System.exit(-10);
				for(int i =0; i < processes.length ; i++)
				{
					System.out.println(processes[i]);
				}
			}
			finally { try{ jmxc.close();} catch(Exception e) {} }
	
		}
		catch (final Throwable e)
		{
			System.out.println("Failed!");
			System.err.println("-------------------------------\n");
			e.printStackTrace();
			System.err.println("\n-------------------------------\n\n");
			System.exit(-2);
		}
	}
	private static void usage()
	{
		System.err.println("Usage: AgentStatus --agent <server> <port>:");
		System.err.println("\t--agent  asks for a list of running instances.");
		System.exit(-1);
	}
	
}

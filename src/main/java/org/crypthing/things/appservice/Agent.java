package org.crypthing.things.appservice;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public final class Agent implements AgentMBean
{
    public static final String MBEAN_PATTERN = "org.crypthing.things.appservice:type=Agent,name=";
    private static ObjectName mbName;

    public static void main(String[] args)
    {
        final Agent instance = new Agent();
        try
        {
            ManagementFactory.getPlatformMBeanServer().registerMBean(instance,
                    mbName = new ObjectName((new StringBuilder(256)).append(MBEAN_PATTERN)
                            .append(ManagementFactory.getRuntimeMXBean().getName()).toString()));
            instance.go();
        }
        catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException
                | MalformedObjectNameException e)
        {
            e.printStackTrace();
        }
    }

    private boolean isRunning = true;

    public void go()
    {
        synchronized (this)
        {
            while (isRunning)
            {
                try { wait(); }
                catch (InterruptedException e) {}
            }
        }
    }

    @Override
    public void shutdown()
    {
        synchronized (this)
        {
            if (mbName != null)
            {
                try { ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbName); }
                catch (MBeanRegistrationException | InstanceNotFoundException e) {}
            }
            isRunning = false;
            notifyAll();
        }
    }

    @Override
    public int execute(final String arguments, final String[] env)
    {
        int ret;
        try { ret = waitForProcess(Runtime.getRuntime().exec(arguments, env)); }
        catch (IOException e) { ret = 1; }
        return ret;
    }
	private int waitForProcess(final Process p)
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
}
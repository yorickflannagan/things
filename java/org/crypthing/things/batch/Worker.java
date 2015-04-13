package org.crypthing.things.batch;

import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.management.ObjectName;

import org.crypthing.things.SNMPTrap;
import org.crypthing.things.appservice.SandboxMBean;

public abstract class Worker extends Thread implements SandboxMBean

{
	private boolean isRunning = true;
	private final long sleep;
	private long counter;
	private ObjectName mbName = null;

	protected final ResourceFactory factory;
	protected final Properties config;

	void initializeMBean()
	{
		try
		{
			ManagementFactory.getPlatformMBeanServer().registerMBean
			(
				this,
				mbName = new ObjectName
				(
					(new StringBuilder(256)).append("org.crypthing.things.batch:type=")
					.append(getClass().getName())
					.append(",name=")
					.append(getName())
					.append(",id=")
					.append(getId()).toString()
				)
			);
		}
		catch (Throwable e) { if (trap != null) trap.send(SNMPTrap.EVENT_ERROR, "Could not register MBean to Worker", e); }
	}

	protected final SNMPTrap trap;
	public Worker(final ResourceFactory factory, final Properties cfg, final long sleep, final String name)
	{
		super(name);
		config = cfg;
		this.sleep = sleep;
		this.factory = factory;
		counter = 0;
		trap = SNMPTrap.createTrap(cfg);
	}

	@Override
	public void run()
	{
		SourcePoint source = null;
		EndPointList endpoints = null;
		TransactionCoordinator transaction = null;
		do
		{
			try
			{
				if (source == null) source = factory.createSource();
				if (endpoints == null) endpoints = factory.createEndPoints();
				if (transaction == null) transaction = factory.createTransaction();
				try
				{
					transaction.begin();
					Object o = source.read();
					while (isRunning && o != null)
					{
						ActionChain next = getJobDone(o);
						while (next != null)
						{
							final EndPoint pt = endpoints.get(next.getAction());
							if (pt == null) throw new NoSuchEndPointException("No such end point " + next.getAction());
							pt.write(next.getBean());
							next = next.getNext();
						}
						transaction.commit();
						counter++;

						transaction.begin();
						o = source.read();
					}
					if (!isRunning && o != null) transaction.rollback();
					else if (sleep > 0 && isRunning) Thread.sleep(sleep);
					else shutdown();
				}
				catch (final RecoverableResourceException e)
				{
					transaction.rollback();
					if (transaction != null) transaction.destroy();
					if (endpoints != null) endpoints.destroy();
					if (source != null) source.destroy();
					source = null;
					endpoints = null;
					transaction = null;
				}
				catch (final Throwable e)
				{
					transaction.rollback();
					shutdown();
					if (trap != null) trap.send(SNMPTrap.EVENT_ERROR, "Unrecoverable error during work", e);
				}
			}
			catch (final Throwable e)
			{
				shutdown();
				if (trap != null) trap.send(SNMPTrap.EVENT_ERROR, "Create resource unrecoverable error", e);
			}
		}
		while (isRunning);
		try 
		{
			if (transaction != null) transaction.destroy();
			if (endpoints != null) endpoints.destroy();
			if (source != null) source.destroy();
			if (mbName != null) ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbName);
		}
		catch (final Throwable e) { if (trap != null) trap.send(SNMPTrap.EVENT_ERROR, "Destroy resource error", e); }
	}

	@Override
	public void shutdown() { isRunning = false; }

	@Override
	public long getCounter() { return counter; }

	public abstract ActionChain getJobDone(Object input);
}

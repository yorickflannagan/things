package org.crypthing.things.batch;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import org.crypthing.things.SNMPTrap;
import org.crypthing.things.appservice.SandboxMBean;

public final class Runner implements SandboxMBean
{

	private static ObjectName mbName = null;
	public static void main(String[] args)
	{
		if (args.length != 1) usage();
		final File cfg = new File(args[0]);
		if (!cfg.exists()) usage();
		try
		{
			final FileInputStream in = new FileInputStream(cfg);
			try	
			{
				final Properties prop = new Properties();
				prop.load(in);
				final Runner instance = new Runner(prop);
				Runtime.getRuntime().addShutdownHook(new Thread(instance.new ShutdownHook()));
				try
				{
					ManagementFactory.getPlatformMBeanServer().registerMBean
					(
						instance,
						mbName = new ObjectName
						(
							(new StringBuilder(256))
							.append("org.crypthing.things.batch:type=Runner")
							.append(",name=")
							.append(ManagementFactory.getRuntimeMXBean().getName())
							.toString()
						)
					);
				}
				catch (final Throwable e) { e.printStackTrace(); }
			}
			finally { in.close(); }
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
			usage();
		}
	}
	private static void usage()
	{
		System.err.println
		(
			"Usage: Runner <propertiesFile>, where propertiesFile is the complete file to application configuration"
		);
		System.exit(1);
	}


	private static final String WORKER_FACTORY_ENTRY = "org.crypthing.things.batch.WorkerFactory";
	private static final String THREADS_ENTRY = "org.crypthing.things.batch.Runner.threads";
	private static final String DELAY_ENTRY = "org.crypthing.things.batch.Runner.delay";

	private final WorkerFactory factory;
	private final Worker[] workers;
	private final ScheduledExecutorService service;
	private final ScheduledFuture<?> checker;
	private final SNMPTrap trap;
	public Runner(final Properties cfg) throws ConfigurationException, InitializeException, CreateResourceException
	{
		final int threads;
		final long delay;
		final String entry = cfg.getProperty(WORKER_FACTORY_ENTRY);
		if (entry == null || entry.length() == 0) throw new ConfigurationException(WORKER_FACTORY_ENTRY + " entry not found");
		try { threads = Integer.parseInt(cfg.getProperty(THREADS_ENTRY)); }
		catch (final Throwable e) { throw new ConfigurationException(THREADS_ENTRY + " entry is not valid", e); }
		try { delay = Long.parseLong(cfg.getProperty(DELAY_ENTRY)); }
		catch (final Throwable e) { throw new ConfigurationException(DELAY_ENTRY + " entry is not valid", e); }
		try { factory = (WorkerFactory) Class.forName(entry).newInstance(); }
		catch (final Throwable e) { throw new InitializeException("Could not instantiate specified worker factory", e); }
		factory.initialize(cfg);
		service = Executors.newSingleThreadScheduledExecutor();
		workers = new Worker[threads];
		try { for (int i = 0; i < threads; i++) workers[i] = factory.createWorker(); }
		catch (final CreateResourceException e) { shutdown(); throw e; }
		for (int i = 0; i < threads; i++) workers[i].start();
		checker = service.scheduleWithFixedDelay(new SanityChecker(), delay, delay, TimeUnit.SECONDS);
		trap = SNMPTrap.createTrap(cfg);
		if (trap != null) trap.send(SNMPTrap.EVENT_START, "Runner has started successfully");
	}

	@Override
	public void shutdown()
	{
		for (int i = 0; i < workers.length; i++) if (workers[i] != null) workers[i].shutdown();
		factory.destroy();
		checker.cancel(true);
		service.shutdown();
		try { if (mbName != null) ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbName); }
		catch (final Throwable e) {}
		if (trap != null) trap.send(SNMPTrap.EVENT_END, "Runner has shutdown");
	}

	@Override
	public long getCounter()
	{
		long counter;
		int i;
		for (i = 0, counter = 0; i < workers.length; counter += workers[i] != null ? workers[i].getCounter() : 0, i++);
		return counter;
	}

	private class ShutdownHook implements Runnable
	{
		@Override
		public void run() { shutdown(); }
	}

	private class SanityChecker implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				for
				(
					int i = 0; i < workers.length; i++
				)	if (workers[i] == null || !workers[i].isAlive()) workers[i] = factory.createWorker();
			}
			catch (final Throwable e)
			{
				if (trap != null) trap.send(SNMPTrap.EVENT_ERROR, "Could not recicle worker thread", e);
				e.printStackTrace();
				shutdown();
			}
		}
	}
}

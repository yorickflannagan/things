package org.crypthing.things.appservice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.WorkerConfig;
import org.crypthing.things.events.LifecycleEvent;
import org.crypthing.things.events.LifecycleEvent.LifecycleEventType;
import org.crypthing.things.events.LifecycleEventDispatcher;
import org.crypthing.things.events.ProcessingEvent;
import org.crypthing.things.events.ProcessingEvent.ProcessingEventType;
import org.crypthing.things.events.ProcessingEventDispatcher;

public abstract class Sandbox extends Thread implements ShutdownEventDispatcher, InterruptEventListener
{
	private boolean isRunning = true;
	private long sleeptime = 0;
	private boolean isRestartable = false;
	private ShutdownEventListener shutdownListner;
	private LifecycleEventDispatcher lcDispatcher;
	private ProcessingEventDispatcher pDispatcher;
	private BillingEventListener billingListener;

	final void init
	(
		final WorkerConfig config,
		final LifecycleEventDispatcher lcDispatcher,
		final ProcessingEventDispatcher pDispatcher,
		final BillingEventListener billing
	) throws ConfigException
	{
		sleeptime = config.getSleep();
		isRestartable = config.isRestartable();
		this.lcDispatcher = lcDispatcher;
		this.pDispatcher = pDispatcher;
		billingListener = billing;
	}

	@Override public void setShutdownEventListener(final ShutdownEventListener listener) { this.shutdownListner = listener; }
	@Override public final void interrupt() { isRunning = false; }

	@Override
	final public void run()
	{
		lcDispatcher.fire(new LifecycleEvent(this, LifecycleEventType.start, "Sandbox thread " + getId() + "has started"));
		try
		{
			boolean onceMore = true;
			do
			{
				try
				{
					onceMore = execute();
					billingListener.incSuccess();
				}
				catch (final IOException | SQLException e)
				{
					pDispatcher.fire(new ProcessingEvent(this, ProcessingEventType.warning, "Unhandled IO or SQLException", e));
					billingListener.incFailure();
					if (!isRestartable) throw e;
				}
				if (isRunning && onceMore && sleeptime > 0) Thread.sleep(sleeptime);
			}
			while (isRunning && onceMore);

			if (!isRunning) shutdownListner.signal(this);
			else shutdownListner.abandon(this);
		}
		catch (final Throwable e)
		{
			pDispatcher.fire(new ProcessingEvent(this, ProcessingEventType.error, "Unhandled exception received", e));
			billingListener.incFailure();
			shutdownListner.abort(this);
		}
		lcDispatcher.fire(new LifecycleEvent(this, LifecycleEventType.stop,  "Sandbox thread " + getId() + "has ended"));
		interrupt();
	}

	public void init(final Properties props, final LifecycleEventDispatcher lcDispatcher, final ProcessingEventDispatcher pDispatcher) throws ConfigException {}
	public void destroy() {}
	public abstract boolean execute() throws IOException, SQLException;
}

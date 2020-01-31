package org.crypthing.things.appservice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.crypthing.things.config.ConfigException;
import org.crypthing.things.appservice.config.WorkerConfig;
import org.crypthing.things.snmp.EncodableString;
import org.crypthing.things.snmp.LifecycleEvent;
import org.crypthing.things.snmp.LifecycleEvent.LifecycleEventType;
import org.crypthing.things.snmp.LifecycleEventDispatcher;
import org.crypthing.things.snmp.ProcessingEvent;
import org.crypthing.things.snmp.ProcessingEvent.ProcessingEventType;
import org.crypthing.things.snmp.ProcessingEventDispatcher;

public abstract class Sandbox extends Thread implements ShutdownEventDispatcher, InterruptEventListener
{
	private boolean running = true;
	private long sleeptime;
	private boolean isRestartable = false;
	private long heartbeat;
	private ShutdownEventListener shutdownListner;
	private LifecycleEventDispatcher lcDispatcher;
	private ProcessingEventDispatcher pDispatcher;
	private long iSuccess, iFailure;
	private Properties props;

	public final void startup
	(
		final WorkerConfig config,
		final LifecycleEventDispatcher lcDispatcher,
		final ProcessingEventDispatcher pDispatcher,
		final Properties sbProps
	) throws ConfigException
	{
		sleeptime = config.getSleep();
		isRestartable = config.isRestartable();
		heartbeat = config.getHeartbeat();
		this.lcDispatcher = lcDispatcher;
		this.pDispatcher = pDispatcher;
		props = sbProps;
	}

	@Override public void setShutdownEventListener(final ShutdownEventListener listener) { this.shutdownListner = listener; }
	@Override public final void shutdown() { running = false; if(this.getState() != State.RUNNABLE) this.interrupt(); }

	@Override
	final public void run()
	{
		lcDispatcher.fire(new LifecycleEvent(LifecycleEventType.start, new EncodableString("Sandbox thread " + getId() + "has started")));
		try
		{
			startup(props);
			long lastSignal = System.currentTimeMillis();
			boolean onceMore = true;
			do
			{
				try { onceMore = execute(); }
				catch (final IOException | SQLException e)
				{
					pDispatcher.fire(new ProcessingEvent(ProcessingEventType.warning, "Unhandled IO or SQLException", e));
					if (!isRestartable) throw e;
				}
				if (heartbeat > 0 && System.currentTimeMillis() - lastSignal > heartbeat)
				{
					lcDispatcher.fire(new LifecycleEvent(LifecycleEventType.work, new EncodableString("Sandbox thread " + getId() + "is alive and running")));
					lastSignal = System.currentTimeMillis();
				}
				if (running && onceMore && sleeptime > 0) try { Thread.sleep(sleeptime);} catch(InterruptedException e) {}
			}
			while (running && onceMore);
			release();
			if (!running) shutdownListner.signal(this);
			else shutdownListner.abandon(this);
		}
		catch (final Throwable e)
		{
			pDispatcher.fire(new ProcessingEvent(ProcessingEventType.error, "Unhandled exception received", e));
			iFailure++;
			shutdownListner.abort(this);
		}
		lcDispatcher.fire(new LifecycleEvent(LifecycleEventType.stop, new EncodableString("Sandbox thread " + getId() + "has ended")));
		interrupt();
	}

	protected void fire(final LifecycleEvent evt) { if (evt != null) lcDispatcher.fire(evt); }
	protected void fire(final ProcessingEvent evt) { if (evt != null) pDispatcher.fire(evt); }
	protected boolean isRunning () { return running; }

	protected void success() { iSuccess++; }
	protected void failure() { iFailure++; }
	public long getSuccess() { return iSuccess; }
	public long getFailure() { return iFailure; }
	
	public void startup(final Properties props) throws ConfigException {}
	public void release() {}
	protected abstract boolean execute() throws IOException, SQLException;
}

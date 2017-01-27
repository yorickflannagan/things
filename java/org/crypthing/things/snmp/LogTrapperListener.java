package org.crypthing.things.snmp;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LogTrapperListener implements LifecycleEventListener, ProcessingEventListener
{
	private final Logger logger;
	private final SNMPBridge trapper;
	public LogTrapperListener(final Logger logger) { this(logger, null); }
	public LogTrapperListener(final Logger logger, final SNMPBridge trapper)
	{
		if (logger == null) throw new NullPointerException();
		this.logger = logger;
		this.trapper = trapper;
	}
	private void dispatch(final ProcessingEvent e, final Level level)
	{
		String msg = e.getData().encode();
		final Throwable ex = e.getThroable();
		if (ex != null) msg = "Message ID: " + UUID.randomUUID().toString() + " - " + msg;
		logger.log(level, msg, ex);
		if (trapper != null) trapper.notify(e);
	}
	private void dispatch(final LifecycleEvent e)
	{
		logger.info(e.getData().encode());
		if (trapper != null) trapper.notify(e);
	}
	@Override public void info(final ProcessingEvent e) { dispatch(e, Level.INFO); }
	@Override public void warning(final ProcessingEvent e) { dispatch(e, Level.WARNING); }
	@Override public void error(final ProcessingEvent e) { dispatch(e, Level.SEVERE); }
	@Override public void start(final LifecycleEvent e) { dispatch(e); }
	@Override public void work(final LifecycleEvent e) { dispatch(e); }
	@Override public void stop(final LifecycleEvent e) { dispatch(e); }
}
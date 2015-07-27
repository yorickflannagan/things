package org.crypthing.things.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.crypthing.things.SNMPTrap;

public class LogTrapperListener implements LifecycleEventListener, ProcessingEventListener
{
	private static final Map<String, Logger> loggers = new HashMap<String, Logger>();
	private final SNMPTrap trap;
	public LogTrapperListener(final SNMPTrap trap) { this.trap = trap; }
	@Override
	public void info(final ProcessingEvent e)
	{
		String msg = e.getMessage();
		final Throwable ex = e.getThroable();
		if (ex != null) msg = "Message ID: " + UUID.randomUUID().toString() + " - " + msg;
		getLogger(e).info(msg);
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), msg, ex);
	}
	@Override
	public void warning(final ProcessingEvent e)
	{
		String msg = e.getMessage();
		final Throwable ex = e.getThroable();
		if (ex != null) msg = "Message ID: " + UUID.randomUUID().toString() + " - " + msg;
		getLogger(e).log(Level.WARNING, msg, ex);
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), msg, ex);
	}
	
	@Override
	public void error(final ProcessingEvent e)
	{
		String msg = e.getMessage();
		final Throwable ex = e.getThroable();
		if (ex != null) msg = "Message ID: " + UUID.randomUUID().toString() + " - " + msg;
		getLogger(e).log(Level.SEVERE, msg, ex);
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), msg, ex);
	}

	@Override
	public void start(final LifecycleEvent e)
	{
		getLogger(e).info(e.getMessage());
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), e.getMessage(), null);
	}
	@Override
	public void work(final LifecycleEvent e)
	{
		getLogger(e).info(e.getMessage());
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), e.getMessage(), null);
	}
	@Override
	public void stop(final LifecycleEvent e)
	{
		getLogger(e).info(e.getMessage());
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), e.getMessage(), null);
	}
	private Logger getLogger(final Event e)
	{
		final String name = e.getLauncher().getClass().getName();
		Logger ret = loggers.get(name);
		if (ret == null)
		{
			ret = Logger.getLogger(name);
			loggers.put(name, ret);
		}
		return ret;
	}
}

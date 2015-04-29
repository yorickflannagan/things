package org.crypthing.things.events;

import java.util.HashMap;
import java.util.Map;
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
		final Logger log = getLogger(e);
		log.info(e.getMessage());
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), e.getMessage(), e.getThroable());
	}
	@Override
	public void warning(final ProcessingEvent e)
	{
		final Logger log = getLogger(e);
		log.log(Level.WARNING, e.getMessage(), e.getThroable());
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), e.getMessage(), e.getThroable());
	}
	
	@Override
	public void error(final ProcessingEvent e)
	{
		final Logger log = getLogger(e);
		//TODO: Remover isto.
		log.log(Level.SEVERE, e.getMessage(), e.getThroable());
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), e.getMessage(), e.getThroable());
	}
	@Override
	public void start(final LifecycleEvent e)
	{
		final Logger log = getLogger(e);
		log.info(e.getMessage());
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), e.getMessage(), null);
	}
	@Override
	public void work(final LifecycleEvent e)
	{
		final Logger log = getLogger(e);
		log.info(e.getMessage());
		if (trap != null) trap.send(e.isRelativeOID() ? trap.getRootOID() + "." + e.getOID() : e.getOID(), e.getMessage(), null);
	}
	@Override
	public void stop(final LifecycleEvent e)
	{
		final Logger log = getLogger(e);
		log.info(e.getMessage());
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

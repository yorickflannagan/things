package org.crypthing.things.snmp;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.crypthing.things.snmp.LifecycleEvent.LifecycleEventType;
import org.crypthing.things.snmp.ProcessingEvent.ProcessingEventType;

public class Monitor extends TicketClerk implements LifecycleEventListener, ProcessingEventListener
{
	private static final long serialVersionUID = -533126638825126981L;
	private static final String SNMP_ERROR = "Could not create an SNMP dispatcher";
	private static final LifecycleEvent MON_START = new LifecycleEvent(LifecycleEventType.start, new EncodableString("Application monitor has started"));
	private static final LifecycleEvent MON_END = new LifecycleEvent(LifecycleEventType.stop, new EncodableString("Application monitor has ended"));
	private static final TimeUnit UNIT = TimeUnit.MINUTES;
	private class Dispenser extends Thread
	{
		private final TicketClerk clerk;
		private int tickets;
		private Dispenser(final TicketClerk clerk) { if ((this.clerk = clerk) == null) throw new NullPointerException(); }
		@Override
		public final void run()
		{
			final int total = clerk.accrue();
			try
			{
				final SNMPBridge trap = new SNMPBridge(udpAddress, oidRoot);
				trap.notify(new BillingEvent(total - tickets));
				tickets = total;
			}
			catch (final IOException e) { Logger.getLogger(Dispenser.class.getName()).log(Level.SEVERE, SNMP_ERROR, e); }
		}
	}
	private final Logger logger;
	private final String udpAddress;
	private final String oidRoot;
	public Monitor(final Logger logger, final String udpAddress, final String oidRoot, final long delay) throws IOException
	{
		if ((this.logger = logger) == null || (this.udpAddress = udpAddress) == null || (this.oidRoot = oidRoot) == null) throw new NullPointerException();
		new SNMPBridge(udpAddress, oidRoot);
		final Dispenser dispenser = new Dispenser(this);
		dispenser.setDaemon(true);
		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(dispenser, delay, delay, UNIT);
		start(MON_START);
	}
	public void end()
	{
		final  int tickets = accrue();
		try
		{
			final SNMPBridge trap = new SNMPBridge(udpAddress, oidRoot);
			trap.notify(new GatheredBillingEvent(tickets));
		}
		catch (final IOException e) { error(new ProcessingEvent(ProcessingEventType.error, SNMP_ERROR, e)); }
		stop(MON_END);
	}
	private void dispatch(final ProcessingEvent e, final Level level)
	{
		if (e == null) throw new NullPointerException();
		final Encodable encode = e.getData();
		String msg;
		if (encode != null && encode.getEncoding() == Encodable.Type.STRING) msg = (String) encode.encode();
		else msg = "";
		final Throwable ex = e.getThroable();
		if (ex != null) msg = "Message ID: " + UUID.randomUUID().toString() + " [ " + ex.getClass().getName() + " ] - " + msg;
		logger.log(level, msg, ex);
		try
		{
			final SNMPBridge trap = new SNMPBridge(udpAddress, oidRoot);
			trap.notify(e);
		}
		catch (final IOException except) { logger.log(Level.SEVERE, SNMP_ERROR, except); }
	}
	private void dispatch(final LifecycleEvent e)
	{
		if (e == null) throw new NullPointerException();
		final Encodable encode = e.getData();
		String msg;
		if (encode != null && encode.getEncoding() == Encodable.Type.STRING) msg = (String) encode.encode();
		else msg = "";
		logger.info(msg);
		try
		{
			final SNMPBridge trap = new SNMPBridge(udpAddress, oidRoot);
			trap.notify(e);
		}
		catch (final IOException except) { logger.log(Level.SEVERE, SNMP_ERROR, except);		}
	}
	@Override public void info(final ProcessingEvent e) { dispatch(e, Level.INFO); }
	@Override public void warning(final ProcessingEvent e) { dispatch(e, Level.WARNING); }
	@Override public void error(final ProcessingEvent e) { dispatch(e, Level.SEVERE); }
	@Override public void start(final LifecycleEvent e) { dispatch(e); }
	@Override public void work(final LifecycleEvent e) { dispatch(e); }
	@Override public void stop(final LifecycleEvent e) { dispatch(e); }
}

package org.crypthing.things.snmp;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.crypthing.things.snmp.LifecycleEvent.LifecycleEventType;
import org.json.JSONException;
import org.json.JSONObject;


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
		if (e == null) throw new NullPointerException();
		final Encodable encode = e.getData();
		String msg;
		if (encode != null && encode.getEncoding() == Encodable.Type.STRING) msg = (String) encode.encode();
		else msg = "";
		try
		{
			final JSONObject json = new JSONObject(msg);
			msg = "Message ID: " + UUID.randomUUID().toString() + " [ " + json.getString("who") + " ] - " + msg;
		}
		catch (final JSONException err) {}
		logger.log(level, msg);
		if (trapper != null) trapper.notify(e);
	}
	private void dispatch(final LifecycleEvent e)
	{
		if (e == null) throw new NullPointerException();
		final Encodable encode = e.getData();
		String msg;
		if (encode != null && encode.getEncoding() == Encodable.Type.STRING) msg = (String) encode.encode();
		else msg = "";
		if (e.getType().equals(LifecycleEventType.heart)) logger.fine(msg);
		else logger.info(msg);
		if (trapper != null) trapper.notify(e);
	}
	@Override public void info(final ProcessingEvent e) { dispatch(e, Level.INFO); }
	@Override public void warning(final ProcessingEvent e) { dispatch(e, Level.WARNING); }
	@Override public void error(final ProcessingEvent e) { dispatch(e, Level.SEVERE); }
	@Override public void start(final LifecycleEvent e) { dispatch(e); }
	@Override public void work(final LifecycleEvent e) { dispatch(e); }
	@Override public void stop(final LifecycleEvent e) { dispatch(e); }
	@Override public void heart(final LifecycleEvent e) { dispatch(e); }
}
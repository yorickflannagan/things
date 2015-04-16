package org.crypthing.things.events;

public class ProcessingEvent extends Event
{
	public enum ProcessingEventType
	{
		info,
		warning,
		error
	}
	private final ProcessingEventType type;
	private Throwable throable;
	public ProcessingEvent(final Object launcher, final ProcessingEventType type) { this(launcher, type, null); }
	public ProcessingEvent(final Object launcher, final ProcessingEventType type, final String message) { this(launcher, type, message, null); }
	public ProcessingEvent(final Object launcher, final ProcessingEventType type, final String message, final Throwable e)
	{
		if (launcher == null) throw new IllegalArgumentException(new NullPointerException());
		setLauncher(launcher);
		setMessage(message);
		setThroable(e);
		this.type = type;
		final String oid;
		switch(type)
		{
		case info:
			oid = "3.1";
			break;
		case warning:
			oid = "3.2";
			break;
		default:
			oid = "3.3";
		}
		setOID(oid);
	}
	public ProcessingEventType getType() { return type; }
	public Throwable getThroable() { return throable; }
	public void setThroable(final Throwable throable) { this.throable = throable; }
}

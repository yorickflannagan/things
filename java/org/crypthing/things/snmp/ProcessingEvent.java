package org.crypthing.things.snmp;

public class ProcessingEvent extends Event
{
	public enum ProcessingEventType { info, warning, error }
	private final ProcessingEventType type;
	private Throwable throable;
	public ProcessingEvent(final ProcessingEventType type) { this(type, null); }
	public ProcessingEvent(final ProcessingEventType type, final String message) { this(type, message, null); }
	public ProcessingEvent(final ProcessingEventType type, final String message, final Throwable e)
	{
		if (message != null)
		{
			setData(new Encodable()
			{
				@Override public String encode() { return message; }
				@Override public void decode(final String data) {}
			});
		}
		this.type = type;
		final String oid;
		switch(type)
		{
		case info:
			oid = ".3.1";
			break;
		case warning:
			oid = ".3.2";
			break;
		default:
			oid = ".3.3";
		}
		setThroable(e).setRelativeOID(oid);
	}
	public ProcessingEventType getType() { return type; }
	public Throwable getThroable() { return throable; }
	public ProcessingEvent setThroable(final Throwable throable) { this.throable = throable; return this; }
}

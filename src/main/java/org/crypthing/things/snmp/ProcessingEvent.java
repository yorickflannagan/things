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
		final String out;
		if (e != null && message != null)
		{
			final StringBuilder builder = new StringBuilder(256);
			if (e != null) builder.append(e.getClass().getName()).append(": ");
			if (message != null) builder.append(message);
			out = builder.toString();
		}
		else if (message != null) out = message;
		else if (e != null) out = e.getClass().getName() + ": " + e.getMessage();
		else out = "";
		setData(new Encodable()
		{
			@Override public Object encode() { return out; }
			@Override public void decode(final String data) {}
			@Override public void decode(final byte[] data) throws EncodeException {}
			@Override public void decode(final int data) throws EncodeException {}
			@Override public Type getEncoding() { return Encodable.Type.STRING; }
		});
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

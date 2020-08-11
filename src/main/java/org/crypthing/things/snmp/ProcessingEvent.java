package org.crypthing.things.snmp;

public class ProcessingEvent extends Event
{
	public enum ProcessingEventType { info, warning, error }
	private final ProcessingEventType type;
	public ProcessingEvent(final ProcessingEventType type) { this(type, null); }
	public ProcessingEvent(final ProcessingEventType type, final Encodable data)
	{
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
		case error:
			oid = ".3.3";
			break;
		default:
			throw new IllegalArgumentException("Unknow event type:" + type);
		}
		setRelativeOID(oid).setData(data);
	}
	public ProcessingEventType getType() { return type; }
}

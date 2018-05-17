package org.crypthing.things.snmp;

public class LifecycleEvent extends Event
{
	public enum LifecycleEventType { start, work, stop, heart }
	private final LifecycleEventType type;
	public LifecycleEvent(final LifecycleEventType type) { this(type, null); }
	public LifecycleEvent(final LifecycleEventType type, final Encodable data)
	{
		this.type = type;
		final String oid;
		switch(type)
		{
		case start:
			oid = ".2.1";
			break;
		case work:
			oid = ".2.2";
			break;
		case stop:
			oid = ".2.3";
			break;
		case heart:
			oid = ".2.4";
			break;
		default:
			throw new IllegalArgumentException("Unknow eventtype:" + type);
			// Não deve acontecer...
		}
		setRelativeOID(oid).setData(data);
	}
	public LifecycleEventType getType() { return type; }
}

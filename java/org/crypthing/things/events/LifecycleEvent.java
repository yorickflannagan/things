package org.crypthing.things.events;

public class LifecycleEvent extends Event
{
	public enum LifecycleEventType
	{
		start,
		work,
		stop
	}
	private final LifecycleEventType type;
	public LifecycleEvent(final Object launcher, final LifecycleEventType type) { this(launcher, type, null); }
	public LifecycleEvent(final Object launcher, final LifecycleEventType type, final String message)
	{
		if (launcher == null) throw new IllegalArgumentException(new NullPointerException());
		setLauncher(launcher);
		setMessage(message);
		this.type = type;
		final String oid;
		switch(type)
		{
		case start:
			oid = "2.1";
			break;
		case work:
			oid = "2.2";
			break;
		default:
			oid = "2.3";
		}
		setOID(oid);
	}
	public LifecycleEventType getType() { return type; }
}

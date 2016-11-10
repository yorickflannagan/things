package org.crypthing.things.snmp;

public class NotificationEvent extends Event
{
	public NotificationEvent(final Encodable object)
	{
		if (object == null) throw new IllegalArgumentException(new NullPointerException());
		setRelativeOID("4").setData(object);
	}
}

package org.crypthing.things.snmp;

import java.util.EventListener;

public interface NotificationEventListener extends EventListener
{
	void notify(Event e);
}

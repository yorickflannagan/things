package org.crypthing.things.snmp;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public class NotificationEventDispatcher extends EventDispatcher<NotificationEvent, NotificationEventListener>
{
	@Override
	public void fire(final NotificationEvent event)
	{
		final ReentrantLock mutex = this.mutex;
		mutex.lock();
		try
		{
			final Iterator<NotificationEventListener> it = listeners.iterator();
			while (it.hasNext()) it.next().notify(event);
		}
		finally { mutex.unlock(); }
	}
}

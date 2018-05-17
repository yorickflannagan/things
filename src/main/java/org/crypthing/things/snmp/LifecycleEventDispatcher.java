package org.crypthing.things.snmp;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public final class LifecycleEventDispatcher extends EventDispatcher<LifecycleEvent, LifecycleEventListener>
{
	@Override
	public void fire(final LifecycleEvent event)
	{
		final ReentrantLock mutex = this.mutex;
		mutex.lock();
		try
		{
			final Iterator<LifecycleEventListener> it = listeners.iterator();
			switch (event.getType())
			{
			case start:
				while (it.hasNext()) it.next().start(event);
				break;
			case work:
				while (it.hasNext()) it.next().work(event);
				break;
			case stop:
				while (it.hasNext()) it.next().stop(event);
				break;
			case heart:
				while (it.hasNext()) it.next().stop(event);
				break;
			}
		}
		finally { mutex.unlock(); }
	}
}

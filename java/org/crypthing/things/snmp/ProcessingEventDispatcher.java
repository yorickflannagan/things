package org.crypthing.things.snmp;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public final class ProcessingEventDispatcher extends EventDispatcher<ProcessingEvent, ProcessingEventListener>
{
	@Override
	public void fire(final ProcessingEvent event)
	{
		final ReentrantLock mutex = this.mutex;
		mutex.lock();
		try
		{
			final Iterator<ProcessingEventListener> it = listeners.iterator();
			switch (event.getType())
			{
			case info:
				while (it.hasNext()) it.next().info(event);
				break;
			case warning:
				while (it.hasNext()) it.next().warning(event);
				break;
			case error:
				while (it.hasNext()) it.next().error(event);
				break;
			}
		}
		finally { mutex.unlock(); }
	}
}

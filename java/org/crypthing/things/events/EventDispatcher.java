package org.crypthing.things.events;

import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public abstract class EventDispatcher<E extends Event, L extends EventListener>
{
	protected final Set<L> listeners = new HashSet<L>();
	protected final ReentrantLock mutex = new ReentrantLock();
	public void addListener(final L listener)
	{
		final ReentrantLock mutex = this.mutex;
		mutex.lock();
		try { listeners.add(listener); }
		finally { mutex.unlock(); }
	}
	public abstract void fire(E event);
}

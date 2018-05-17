package org.crypthing.things.snmp;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TicketClerk implements Serializable
{
	private static final long serialVersionUID = 6436025788421874503L;
	private class Counter
	{
		private int value = 0;
		void inc() { ++value; }
		int get() { return value; }
	}
	private static final int MIN_CAPACITY = 256;
	private static final ThreadLocal<Counter> _storage = new ThreadLocal<Counter>();
	private static final ConcurrentHashMap<Long, Counter> _accrued = new ConcurrentHashMap<Long, Counter>(MIN_CAPACITY);
	public void bill()
	{
		Counter counter = _storage.get();
		if (counter == null)
		{
			_storage.set(new Counter());
			counter = _storage.get();
			_accrued.put(Thread.currentThread().getId(), counter);
		}
		counter.inc();
	}
	protected int accrue()
	{
		int ret = 0;
		final Iterator<Long> it = _accrued.keySet().iterator();
		while (it.hasNext()) ret += _accrued.get(it.next()).get();
		return ret;
	}
}

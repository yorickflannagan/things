/**
 * The contents of this file are subject to the GNU GENERAL PUBLIC LICENSE
 * Version 3 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.gnu.org/licenses/gpl.txt
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */

package org.crypthing.things.pool;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.crypthing.things.DaemonThreadFactory;
import org.crypthing.things.asynthings.EventConsumer;
import org.crypthing.things.asynthings.Overseer;
import org.crypthing.things.asynthings.QueuedEvent;

/**
 * A pool of P objects
 * @author yorick.flannagan & dsohsten
 *
 * @param <P> - any P
 */
public final class Pool<P> extends Overseer<Pool<P>, Pool<P>>
{
	private void writeObject(ObjectOutputStream stream) throws IOException { throw new NotSerializableException(); }
	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException { throw new NotSerializableException(); }
	@SuppressWarnings("unused")
	private void readObjectNoData() throws ObjectStreamException { throw new NotSerializableException(); }
	private static final long serialVersionUID = 7032900581477823048L;
	private static final Logger log = Logger.getLogger(EventConsumer.class.getName());

	private final LinkedBlockingDeque<PooledObject> stack;
	private int maxWaterLevel;
	private int minWaterLevel;
	private int increment;
	private long timeout;
	private final ObjectFactory<P> factory;
	private long lifeTime;
	private ScheduledExecutorService service;
	private boolean interrupted = false;
	private boolean interruptAsSoonPossible = false;
	private final ReentrantLock mutex = new ReentrantLock();


	/**
	 * Create a new pool with default values:
	 * <pre>
	 * maximum amount of objects instances in the pool: 50
	 * minimum amount of objects instances in the pool: 10
	 * amount of new objects to create when the pool is empty: 5
	 * time to wait for a pooled object: 10000 ms
	 * minimum lifetime of a pooled object without use: 300000 ms
	 * interval to verify if the pool must be resized: 150000 ms
	 * </pre>
	 */
	public Pool(final ObjectFactory<P> factory)
	{
		this(factory, 10, 50, 5, 10000, 300000, 150000);
	}

	/**
	 * Creates a new pool with specified parameters
	 * @param minWaterLevel - minimum amount of objects instances in the pool
	 * @param maxWaterLevel - maximum amount of objects instances in the pool
	 * @param increment - amount of new objects to create when the pool is empty
	 * @param timeout - time to wait for a pooled object (in milliseconds)
	 * @param lifeTime - minimum lifetime (in milliseconds) of a pooled object without use
	 * @param delay - interval (in milliseconds) to verify if the pool must be resized 
	 */
	public Pool
	(
		final ObjectFactory<P> factory,
		final int minWaterLevel,
		final int maxWaterLevel,
		final int increment,
		final long timeout,
		final long lifeTime,
		final long delay
	)
	{
		super
		(
			new EventConsumer<Pool<P>, Pool<P>>(new ProcessorFactory<P>(factory)),
			new LinkedBlockingQueue<QueuedEvent<Pool<P>>>()
		);
		
		this.stack = new LinkedBlockingDeque<PooledObject>();
		this.maxWaterLevel = maxWaterLevel;
		this.minWaterLevel = minWaterLevel;
		this.increment = increment;
		this.timeout = timeout;
		this.factory = factory;
		this.lifeTime = lifeTime;
		service = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
		service.scheduleWithFixedDelay(new DayGirl(this), delay, delay, TimeUnit.MILLISECONDS);
	}


	LinkedBlockingDeque<PooledObject> getStack()
	{
		return stack;
	}


	/**
	 * Get the maximum amount of objects instances in the pool
	 * @return the maxWaterLevel itself
	 */
	public int getMaxWaterLevel()
	{
		return maxWaterLevel;
	}

	/**
	 * Set the maximum amount of objects instances in the pool
	 * @param maxWaterLevel the maxWaterLevel to set
	 */
	public void setMaxWaterLevel(final int maxWaterLevel)
	{
		if (maxWaterLevel <= 0 || maxWaterLevel <= minWaterLevel)
		{
			throw new IllegalArgumentException("The argument must be a positive integer greater than minWaterLevel");
		}
		this.maxWaterLevel = maxWaterLevel;
	}

	/**
	 * Get the minimum amount of objects instances in the pool
	 * @return the minWaterLevel itself
	 */
	public int getMinWaterLevel()
	{
		return minWaterLevel;
	}

	/**
	 * Set the minimum amount of objects instances in the pool
	 * @param minWaterLevel the minWaterLevel to set
	 */
	public void setMinWaterLevel(final int minWaterLevel)
	{
		if (minWaterLevel <= 0 || minWaterLevel >= maxWaterLevel)
		{
			throw new IllegalArgumentException("The argument must be a positive integer lesser than maxWaterLevel");
		}
		this.minWaterLevel = minWaterLevel;
	}

	/**
	 * Get the amount of new objects to create when the pool is empty
	 * @return the increment
	 */
	public int getIncrement()
	{
		return increment;
	}

	/**
	 * Set amount of new objects to create when the pool is empty
	 * @param increment the increment to set
	 */
	public void setIncrement(final int increment)
	{
		if (increment <= 0)
		{
			throw new IllegalArgumentException("The argument must be a positive integer");
		}
		this.increment = increment;
	}

	/**
	 * Get the time (in milliseconds) to wait for a pooled object
	 * @return the timeout
	 */
	public long getTimeout()
	{
		return timeout;
	}

	/**
	 * Set the time (in milliseconds) to wait for a pooled object
	 * @param timeout the timeout to set
	 */
	public void setTimeout(final long timeout)
	{
		if (timeout < 0)
		{
			throw new IllegalArgumentException("The argument must be a positive long");
		}
		this.timeout = timeout;
	}

	/**
	 * Get the minimum lifetime (in milliseconds) of a pooled object without use
	 * @return it
	 */
	public long getLifeTime()
	{
		return lifeTime;
	}

	/**
	 * Set the minimum lifetime (in milliseconds) of a pooled object without use
	 * @param lifeTime the lifetime to set
	 */
	public void setLifeTime(final long lifeTime)
	{
		if (lifeTime < 0)
		{
			throw new IllegalArgumentException("The argument must be a positive long");
		}
		this.lifeTime = lifeTime;
	}

	/**
	 * Borrows a P object from Pool
	 * @return the big P
	 * @throws PoolOverflowException - thrown if cannot supply more objects
	 */
	public P borrow() throws PoolOverflowException
	{
		final ReentrantLock mutex = this.mutex;
		mutex.lock();
		try { if (interrupted || interruptAsSoonPossible) throw new PoolOverflowException("Cannot borrow an interrupted pool"); }
		finally { mutex.unlock(); }
		P ret = null;
		if (stack.size() == 0)
		{
			final QueuedEvent<Pool<P>> event = new QueuedEvent<Pool<P>>()
			{
				private static final long serialVersionUID = -483739223729765679L;
			};
			event.setName("Pool de P");
			event.setPayload(this);
			queue.offer(event);
		}
		try
		{
			
			final PooledObject temp = stack.poll(timeout, TimeUnit.MILLISECONDS);
			if (temp == null) throw new PoolOverflowException("Cannot supply more objects");
			ret = temp.getObject();
		}
		catch (final InterruptedException e)
		{
			throw new PoolOverflowException("Cannot supply more objects", e);
		}
		return ret;
	}
	/**
	 * Give back the borrowed p object to the pool
	 * @param p itself
	 */
	public void giveBack(final P p)
	{
		try
		{
			factory.checkSanity(p);
			stack.giveBack(new PooledObject(p));
		}
		catch (final PoolException swallowed)
		{
			log.warning("Required to relase instance of " + p.getClass().getName());
			factory.release(p);
			stack.decrementWaterLevel();
		}
		if (interruptAsSoonPossible && stack.isFull()) interrupt();
	}

	@Override
	public void interrupt()
	{
		final ReentrantLock mutex = this.mutex;
		mutex.lock();
		try
		{
			if (interrupted) return;
			if (stack.isFull())
			{
				service.shutdownNow();
				super.interrupt();
				final Iterator<PooledObject> it = stack.iterator();
				while (it.hasNext()) factory.release(it.next().getObject());
				interrupted = true;
				interruptAsSoonPossible = false;
			}
			else interruptAsSoonPossible = true;
		}
		finally { mutex.unlock(); }
	}
	
	public ExecutionState getStatus()
	{
		if (interrupted) return ExecutionState.Interrupted;
		if (interruptAsSoonPossible) return ExecutionState.WillInterrupt;
		return ExecutionState.Running;
	}
	
	class PooledObject
	{
		private long lastReturned;
		private P object;
		PooledObject()
		{
			this(null);
		}
		PooledObject(final P object)
		{
			this(object, System.currentTimeMillis());
		}
		PooledObject(final P object, final long lastReturned)
		{
			this.object = object;
			this.lastReturned = lastReturned;
		}
		long getLastReturned()
		{
			return lastReturned;
		}
		void setLastReturned(final long lastReturned)
		{
			this.lastReturned = lastReturned;
		}
		P getObject()
		{
			return object;
		}
		void setObject(final P object)
		{
			this.object = object;
		}
	}

	class DayGirl implements Runnable
	{
		private Pool<P> pool;
		DayGirl(final Pool<P> pool)
		{
			this.pool = pool;
		}

		public void run()
		{
			final QueuedEvent<Pool<P>> event = new QueuedEvent<Pool<P>>()
			{
				private static final long serialVersionUID = -8162746286829823454L;
			};
			event.setName("Pool de P");
			event.setPayload(pool);
			queue.offer(event);
		}
	}
}



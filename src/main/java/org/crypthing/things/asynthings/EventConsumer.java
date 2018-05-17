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

/**
 * Models an event queue of anything
 */
package org.crypthing.things.asynthings;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.crypthing.things.Interruptible;

/**
 * Consumes the event queue
 * @author yorick.flannagan & dsohsten
 *
 */
public final class EventConsumer<P, M> implements Interruptible, Serializable, Runnable
{
	private static final long serialVersionUID = 2624963983730199904L;
	private static final Logger log = Logger.getLogger(EventConsumer.class.getName());

	private final Map<String, EventProcessor<P, M>> map;
	private LinkedBlockingQueue<QueuedEvent<P>> queue;
	private final EventProcessorFactory<P, M> factory;
	private boolean isRunning = true;
	private final ReentrantLock mutex = new ReentrantLock();


	public EventConsumer(final EventProcessorFactory<P, M> factory)
	{
		this.factory = factory;
		this.map = new ConcurrentHashMap<String, EventProcessor<P,M>>();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		try
		{
			log.info("Queued event consumer is now running...");
			while (isRunning)
			{
				final QueuedEvent<P> event = queue.take();
				final String name = event.getName();
				if (name != null)
				{
					EventProcessor<P, M> processor = map.get(name);
					if (processor == null)
					{
						processor = factory.getProcessor();
						map.put(name, processor);
					}
					processor.unlade(event.getPayload());
				}
			}
		}
		catch (final InterruptedException e)
		{
			log.log(Level.SEVERE, "Queued event consumer interrupted.", e);
		}
		finally
		{
			isRunning = true;
			log.info("Queued event consumer has now shut down...");
		}
	}

	/* (non-Javadoc)
	 * @see org.crypthing.things.Interruptible#interrupt()
	 */
	public void interrupt()
	{
		final ReentrantLock mutex = this.mutex;
		mutex.lock();
		isRunning = false;
		mutex.unlock();
		final QueuedEvent<P> event = new QueuedEvent<P>()
		{
			private static final long serialVersionUID = 5284524332768520239L;
		};
		queue.offer(event);
	}

	/**
	 * Gets a list of processors
	 * @return the list
	 */
	public Collection<String> getProcessors() {
		return new HashSet<String>(map.keySet());
	}

	/**
	 * Gets a processor
	 * @param processor - the processor name
	 * @return it
	 */
	public EventProcessor<P, M> getProcessor(final String processor) {
		return map.get(processor);
	}

	/**
	 * Gets the queue from which the events are taken 
	 * @return the queue
	 */
	public LinkedBlockingQueue<QueuedEvent<P>> getQueue()
	{
		return queue;
	}

	/**
	 * Sets the queue from which the events are taken
	 * @param queue the queue to set
	 */
	public void setQueue(LinkedBlockingQueue<QueuedEvent<P>> queue)
	{
		this.queue = queue;
	}

	public EventProcessorFactory<P, M> getFactory()
	{
		return factory;
	}
}

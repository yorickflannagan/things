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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.crypthing.things.DaemonThreadFactory;
import org.crypthing.things.Interruptible;


/**
 * Manages the consumer thread
 * @author yorick.flannagan & dsohsten
 *
 * @param <P> the event payload
 * @param <M> the events manifest
 */
public abstract class Overseer<P, M> implements Bulletin<M>, Serializable, Interruptible
{
	private static final long serialVersionUID = -4358784203298375624L;
	private static final Logger log = Logger.getLogger(Overseer.class.getName());

	protected final EventConsumer<P, M> consumer;
	protected final LinkedBlockingQueue<QueuedEvent<P>> queue;
	private final ExecutorService foreman;
	private Future<?> trainee;


	/**
	 * Creates a new boss
	 * @param consumer
	 */
	protected Overseer(
		final EventConsumer<P, M> consumer,
		final LinkedBlockingQueue<QueuedEvent<P>> queue
	)
	{
		this.consumer = consumer;
		this.consumer.setQueue(queue);
		this.queue = queue;
		foreman = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
		trainee = foreman.submit(consumer);
		log.info("Overseer has been lauched.");
	}


	/**
	 * Checks if the mob is doing their job.
	 * @return true if they are; false, if you must rack them
	 */
	public boolean isServantWorking()
	{
		return !trainee.isDone();
	}

	/**
	 * Resubmit the consuming job
	 */
	public void hireServant()
	{
		trainee.cancel(true);
		trainee = foreman.submit(consumer);
		log.warning("Overseer has lauched a new servant by request!");
	}

	public void interrupt()
	{
		consumer.interrupt();
		trainee.cancel(true);
		foreman.shutdown();
		log.info("Overseer has been interrupted by request.");
	}


	public Collection<String> getEvents()
	{
		return consumer.getProcessors();
	}


	public M getEvent(String name)
	{
		M ret = null;
		final EventProcessor<P, M> processor = consumer.getProcessor(name);
		if (processor != null)
		{
			ret = processor.getManifest();
		}
		return ret;
	}


	public long queueDepth()
	{
		return queue.size();
	}

}

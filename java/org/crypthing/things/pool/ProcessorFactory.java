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


import java.util.concurrent.LinkedBlockingDeque;

import org.crypthing.things.asynthings.EventProcessor;
import org.crypthing.things.asynthings.EventProcessorFactory;


class ProcessorFactory<P> implements EventProcessorFactory<Pool<P>, Pool<P>>
{
	private Worker worker = new Worker();
	private ObjectFactory<P> factory;

	public ProcessorFactory(final ObjectFactory<P> factory)
	{
		this.factory = factory;
	}
	

	public EventProcessor<Pool<P>, Pool<P>> getProcessor()
	{
		return worker;
	}

	private class Worker implements EventProcessor<Pool<P>, Pool<P>>
	{
		Pool<P> pooldepe;

		public void unlade(final Pool<P> payload)
		{
			if (pooldepe == null) pooldepe = payload;
			final LinkedBlockingDeque<Pool<P>.PooledObject> stack = payload.getStack();
			int newSize = 0;
			int waterLevel = payload.getWaterLevel();
			int minWaterLevel = payload.getMinWaterLevel();
			int maxWaterLevel = payload.getMaxWaterLevel();
			int increment = payload.getIncrement();
			long lifeTime = payload.getLifeTime();
			if (waterLevel < minWaterLevel)
			{
				newSize = minWaterLevel - waterLevel;
			}
			if (stack.size() == 0 && waterLevel < maxWaterLevel)
			{
				newSize = Math.min(increment, maxWaterLevel - waterLevel);
			}
			for (int i = 0; i < newSize; i++)
			{
				stack.offerFirst(payload.new PooledObject(factory.createObject()));
				payload.incrementWaterLevel();
			}
			if (newSize == 0)
			{
				while (stack.size() > 2 && waterLevel > minWaterLevel)
				{
					final Pool<P>.PooledObject object = stack.pollLast();
					if (object != null)
					{
						if
						(
							System.currentTimeMillis() - object.getLastReturned() > lifeTime ||
							waterLevel > maxWaterLevel
						)
						{
							payload.decrementWaterLevel();
							waterLevel--;
						}
						else
						{
							stack.offerLast(object);
						}
					}
					else break;
				}
			}
		}


		public Pool<P> getManifest()
		{
			return pooldepe;
		}
		
	}
}

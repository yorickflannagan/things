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

import java.util.Collection;

/**
 * The service interface
 * @author yorick.flannagan & dsohsten
 *
 */
public interface Bulletin<M>
{
	/**
	 * Gets the events currently available
	 * @return a java.util.Collection of event names.
	 */
	Collection<String> getEvents();

	/**
	 * Gets event statistics
	 * @param name - event name
	 * @return a invoice clone
	 */
	M getEvent(String name);

	/**
	 * Checks if event queue consumer is still alive
	 * @return true if the slave is alive (we hope); otherwise, false
	 */
	boolean isServantWorking();

	/**
	 * Signals to service that the event queue consumer must be restarted.
	 */
	void hireServant();

	/**
	 * Gets the event queue size
	 * @return current amount of messages still in queue
	 */
	long queueDepth();
}

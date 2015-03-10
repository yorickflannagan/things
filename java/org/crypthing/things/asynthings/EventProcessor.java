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

/**
 * Any event processor (naturally)
 * @author yorick.flannagan & dsohsten
 *
 * @param <P> the event payload
 * @param <M> the events manifest
 */
public interface EventProcessor<P, M>
{
	/**
	 * Do here whatever you must do (to payload)
	 * @param payload - itself
	 */
	void unlade(P payload);

	/**
	 * Show your cards!
	 * @return whatever you should return...
	 */
	M getManifest();
}

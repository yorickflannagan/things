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
 * A factory (of course) of an event processor
 * @author yorick.flannagan & dsohsten
 *
 * @param <P> the event payload
 * @param <M> the events manifest
 */
public interface EventProcessorFactory<P, M>
{
	/**
	 * Creates an event processor of P & M
	 * @return the stuff itself
	 */
	EventProcessor<P, M> getProcessor();
}

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

/**
 * Any event to queue
 * @author yorick.flannagan & dsohsten
 *
 * @param <P> the event payload
 */
public abstract class QueuedEvent<P>
	implements Serializable
{
	private static final long serialVersionUID = -8850110226483113944L;

	private String name;
	private P payload;


	/**
	 * Gets the event name
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the event name
	 * @param name the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * Gets the event payload
	 * @return the payload
	 */
	public P getPayload()
	{
		return payload;
	}

	/**
	 * Sets the event payload
	 * @param payload the payload to set
	 */
	public void setPayload(final P payload)
	{
		this.payload = payload;
	}
}

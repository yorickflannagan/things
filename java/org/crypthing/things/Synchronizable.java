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
 * General crypthings
 */
package org.crypthing.things;


/**
 * A synchronizable (of course) object.
 * @author yorick.flannagan
 *
 */
public interface Synchronizable
{
	/**
	 * Causes the current thread to wait until some condition has been reached.
	 * @param timeout - the maximum time to wait.
	 */
	void await(long timeout);

	/**
	 * Releases the current thread lock, signaling that the condition has been reached.
	 */
	void release();
}

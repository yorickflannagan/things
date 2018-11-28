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

/**
 * An F of Ps...
 * @author yorick.flannagan & dsohsten
 *
 * @param <P> - any P
 */
public interface ObjectFactory<P>
{
	/**
	 * Creates a new instance of P 
	 * @return the son of P
	 */
	P createObject();


	/**
	 * Checks if the object is alive, became a brain eater zombie or 
	 * something else (whatever it is)
	 * @param p (who is?)
	 * @throws PoolException if the object is insane
	 */
	void checkSanity(P p) throws PoolException;

	/**
	 * Releases the object (if necessary), even if it is insane.
	 * @param p (who the hell is him?)
	 */
	void release(P p);
}

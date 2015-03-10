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
 * Any things
 */
package org.crypthing.things;

/**
 * An interruptible thread behavior.
 * @author yorick.flannagan & dsohsten
 *
 */
public interface Interruptible
{
	/**
	 * Signals that the current thread must be interrupted as soon as possible.
	 */
	void interrupt();
}

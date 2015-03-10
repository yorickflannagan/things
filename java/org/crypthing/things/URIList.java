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

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A single queue of java.net.URI.
 * @author yorick.flannagan
 *
 */
public class URIList extends LinkedList<URI>
{
	private static final long serialVersionUID = -387813899506132405L;

	/**
	 * Constructs an empty URI list.
	 */
	public URIList()
	{
		super();
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in the order they 
	 * are returned by the collection's iterator.
	 * @param c - the collection whose elements are to be placed into this list
	 */
	public URIList(final Collection<? extends URI> c)
	{
		super(c);
	}

	@Override
	public int hashCode()
	{
		int hash = super.hashCode();
		final Iterator<URI> it = iterator();
		while (it.hasNext())
		{
			hash ^= it.next().hashCode();
		}
		return hash;
	}

	@Override
	public Object clone()
	{
		return new URIList(this);
	}
}

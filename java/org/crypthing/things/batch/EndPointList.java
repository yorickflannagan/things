package org.crypthing.things.batch;

import java.util.HashMap;
import java.util.Iterator;

public class EndPointList extends HashMap<Integer, EndPoint>
{
	private static final long serialVersionUID = -5201485580900883094L;

	void destroy()
	{
		final Iterator<Integer> it = keySet().iterator();
		while (it.hasNext())
		{
			final EndPoint pt = get(it.next());
			if (pt != null) pt.destroy();
		}
	}
}

package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

public class CursorsConfig extends HashMap<String, CursorConfig> {
	private static final long serialVersionUID = -651905407141095927L;
	public CursorConfig addCursor(CursorConfig cursor)
	{
		return put(cursor.getName(), cursor);	
	}
	
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		final Iterator<String> it = this.keySet().iterator();
		while (it.hasNext())
		{
			final String key = it.next();
			builder.append(key).append("={").append(get(key).toString()).append("}");
		}
		return builder.toString();
	}	
}

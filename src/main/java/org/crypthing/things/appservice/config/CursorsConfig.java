package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public class CursorsConfig extends HashMap<String, CursorConfig>
{
	private static final long serialVersionUID = -651905407141095927L;
	public CursorsConfig(final Config xml, final Node root) throws ConfigException
	{
		if (root != null)
		{
			final Iterator<CursorConfig> it = xml.getValueCollection("./cursor", root, new Converter<CursorConfig>()
			{
				@Override
				public CursorConfig convert(Object value) throws ClassCastException
				{
					try { return new CursorConfig(xml, (Node) value); }
					catch (final ConfigException e) { throw (ClassCastException)(new ClassCastException(e.getMessage())).initCause(e); }
				}
				
			}).iterator();
			while (it.hasNext()) addCursor(it.next());
		}
	}
	public CursorConfig addCursor(CursorConfig cursor) { return put(cursor.getName(), cursor); 	}
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

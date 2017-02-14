package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public class CursorsConfig extends HashMap<String, CursorConfig>
{
	private static final long serialVersionUID = -651905407141095927L;

	public CursorsConfig() { super(); }
	public CursorsConfig(final Config cfg, final Node node)
	{
		this();
		final Iterator<CursorConfig> cursors = cfg.getValueCollection("./cursors", node, new Converter<CursorConfig>()
		{
			@Override public CursorConfig convert(final Object value) throws ClassCastException { return new CursorConfig(cfg, (Node) value); }
		}).iterator();
		while (cursors.hasNext()) addCursor(cursors.next());
	}

	public CursorConfig addCursor(final CursorConfig cursor) { return put(cursor.getName(), cursor); 	}
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

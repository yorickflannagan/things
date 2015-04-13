package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

public final class DataSourcesConfig extends HashMap<String, JDBCConfig>
{
	private static final long serialVersionUID = -3495959150998336098L;
	public JDBCConfig addJDBC(final JDBCConfig cfg) { return put(cfg.getName(), cfg); }
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

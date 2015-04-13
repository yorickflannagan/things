package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

public final class ConnectorsConfig extends HashMap<String, ConnectorConfig>
{
	private static final long serialVersionUID = 6184597492041490665L;
	public ConnectorConfig add(final ConnectorConfig qfg) { return put(qfg.getName(), qfg); }
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		final Iterator<String> it = keySet().iterator();
		while (it.hasNext())
		{
			final String key = it.next();
			builder.append(key).append("= {").append(get(key).toString()).append("}");
		}
		return builder.toString();
	}
}

package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public final class ConnectorsConfig extends HashMap<String, ConnectorConfig>
{
	private static final long serialVersionUID = 6184597492041490665L;
	public ConnectorsConfig() { super(); }
	public ConnectorsConfig(final Config cfg, final Node node)
	{
		this();
		final Iterator<ConnectorConfig> connectors = cfg.getValueCollection("./mqxconnector", node, new Converter<ConnectorConfig>()
		{
			@Override public ConnectorConfig convert(final Object value) throws ClassCastException { return new ConnectorConfig(cfg, (Node) value); }
		}).iterator();
		while (connectors.hasNext()) add(connectors.next());
	}

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

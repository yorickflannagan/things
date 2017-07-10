package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public final class ConnectorsConfig extends HashMap<String, ConnectorConfig>
{
	public ConnectorsConfig(final Config xml, final Node root) throws ConfigException
	{
		if (root != null)
		{
			final Iterator<ConnectorConfig> it = xml.getValueCollection("./mqxconnector", root, new Converter<ConnectorConfig>()
			{
				@Override
				public ConnectorConfig convert(Object value) throws ClassCastException
				{
					try { return new ConnectorConfig(xml, (Node) value); }
					catch (final ConfigException e) { throw (ClassCastException)(new ClassCastException(e.getMessage())).initCause(e); }
				}
				
			}).iterator();
			while (it.hasNext()) add(it.next());
		}
	}
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

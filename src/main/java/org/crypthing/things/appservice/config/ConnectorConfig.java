package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public final class ConnectorConfig extends HashMap<String, QueueConfig>
{
	private static final long serialVersionUID = -1588464613738084352L;
	private final ConfigProperties context;
	private final String name;
	private final String driver;
	public ConnectorConfig(final Config xml, final Node root) throws ConfigException
	{
		context = new ConfigProperties(xml, xml.getNodeValue("./context", root));
		name = xml.getValue("./@name", root);
		driver = xml.getValue("./@driver", root);
		final Iterator<QueueConfig> it = xml.getValueCollection("./queues/queue", root, new Converter<QueueConfig>()
		{
			@Override
			public QueueConfig convert(Object value) throws ClassCastException
			{
				try { return new QueueConfig(xml, (Node) value); }
				catch (final ConfigException e) { throw (ClassCastException)(new ClassCastException(e.getMessage())).initCause(e); }
			}
			
		}).iterator();
		while (it.hasNext()) add(it.next());
	}
	public String getName() { return name; }
	public String getDriver() { return driver; }
	public ConfigProperties getContext() { return context; }
	private QueueConfig add(final QueueConfig qfg) { return put(qfg.getName(), qfg); }
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		if (name != null) builder.append("name=").append(name);
		if (driver != null) builder.append(", driver=").append(driver);
		builder.append(", {");
		final Iterator<String> it = keySet().iterator();
		while (it.hasNext())
		{
			final String key = it.next();
			builder.append(key).append("= {").append(get(key).toString()).append("}");
		}
		return builder.toString();
	}
}

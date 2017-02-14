package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public final class ConnectorConfig extends HashMap<String, QueueConfig>
{
	private static final long serialVersionUID = -1588464613738084352L;
	private String name;
	private String driver;
	private ConfigProperties context;

	public ConnectorConfig(final String name, final String driver)
	{
		super();
		this.name = name;
		this.driver = driver;
		context = new ConfigProperties();
	}
	public ConnectorConfig(final Config cfg, final Node node)
	{
		name = cfg.getValue("./@name", node);
		driver = cfg.getValue("./@driver", node);
		context = new ConfigProperties(cfg, cfg.getNodeValue("./context", node));
		final Iterator<QueueConfig> queues = cfg.getValueCollection("./queues/queue", node, new Converter<QueueConfig>()
		{
			@Override public QueueConfig convert(final Object value) throws ClassCastException { return new QueueConfig(cfg, (Node) value); }
		}).iterator();
		while (queues.hasNext()) add(queues.next());
	}

	public String getName() { return name; }
	public void setName(final String name) { this.name = name; }
	public String getDriver() { return driver; }
	public void setDriver(final String driver) { this.driver = driver; }
	public ConfigProperties getContext() { return context; }
	public void setContext(final ConfigProperties context) { this.context = context; }
	public QueueConfig add(final QueueConfig qfg) { return put(qfg.getName(), qfg); }
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

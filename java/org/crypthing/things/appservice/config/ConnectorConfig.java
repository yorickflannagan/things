package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

public final class ConnectorConfig extends HashMap<String, QueueConfig>
{
	private static final long serialVersionUID = -1588464613738084352L;
	private String name;
	private String driver;
	private ConfigProperties context = new ConfigProperties();
	public ConnectorConfig(final String name, final String driver) { this.name = name; this.driver = driver; }
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

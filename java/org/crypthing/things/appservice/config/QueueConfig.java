package org.crypthing.things.appservice.config;

import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.w3c.dom.Node;


public final class QueueConfig extends ConfigProperties
{
	private static final long serialVersionUID = -4871598702526422748L;
	private String name;

	public QueueConfig(final String name) { this.name = name; }
	public QueueConfig(final Config cfg, final Node node)
	{
		name = cfg.getValue("./@name", node);
		final Iterator<Property> props = cfg.getValueCollection("./property", node, new PropertyFactory(cfg)).iterator();
		while (props.hasNext()) add(props.next());
	}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	@Override public String toString()
	{
		final StringBuilder builder = new StringBuilder(512);
		if (name != null) builder.append("name=").append(name);
		builder.append(", properties={").append(super.toString()).append("}");
		return builder.toString();
	}
}

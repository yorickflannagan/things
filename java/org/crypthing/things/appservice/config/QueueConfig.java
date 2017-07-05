package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.w3c.dom.Node;

public final class QueueConfig extends ConfigProperties
{
	private static final long serialVersionUID = -4871598702526422748L;
	private final String name;
	public QueueConfig(final Config xml, final Node root) throws ConfigException
	{
		super(xml, root);
		name = xml.getValue("./@name", root);
	}
	public String getName() { return name; }
	@Override public String toString()
	{
		final StringBuilder builder = new StringBuilder(512);
		if (name != null) builder.append("name=").append(name);
		builder.append(", properties={").append(super.toString()).append("}");
		return builder.toString();
	}
}

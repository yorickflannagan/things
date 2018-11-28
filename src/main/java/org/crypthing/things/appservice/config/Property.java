package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.w3c.dom.Node;

public final class Property
{
	private String name;
	private final String value;
	public Property(final Config xml, final Node root) throws ConfigException
	{
		name = xml.getValue("./@name", root);
		value = xml.getValue("./@value", root);
	}
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getValue() { return value; }
}

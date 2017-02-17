package org.crypthing.things.appservice.config;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.crypthing.things.config.Config;
import org.w3c.dom.Node;

public class ConfigProperties extends Properties
{
	private static final long serialVersionUID = -5215347350360688948L;
	public void add(final Property prop) { setProperty(prop.getName(), prop.getValue());}
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		final Enumeration<?> it = propertyNames();
		while (it.hasMoreElements())
		{
			final String key = (String) it.nextElement();
			builder.append(key).append("=").append(getProperty(key)).append("\n");
		}
		return builder.toString();
	}
	public ConfigProperties() {}
	public ConfigProperties(final Config cfg, final Node node)
	{
		final Iterator<Property> props = cfg.getValueCollection("./property", node, new PropertyFactory(cfg)).iterator();
		while (props.hasNext()) add(props.next());
	}
}

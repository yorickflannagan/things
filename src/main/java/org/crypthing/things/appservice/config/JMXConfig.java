package org.crypthing.things.appservice.config;

import java.util.Enumeration;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.w3c.dom.Node;

public class JMXConfig extends ConfigProperties
{
	private static final long serialVersionUID = 3230906824839606641L;
	private final String host;
	private final String port;
	public JMXConfig(final Config xml, final Node root) throws ConfigException
	{
		super(xml, root);
		host = xml.getValue("./@host", root);
		port = xml.getValue("./@port", root);
	}
	public String getHost() { return host; }
	public String getPort() { return port; }
	@Override
	public void add(final Property prop)
	{
		if (prop.getName().startsWith("com.sun.management.jmxremote.")) super.add(prop);
		else
		{
			prop.setName("com.sun.management.jmxremote." + prop.getName());
			super.add(prop);
		}
	}
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder(512);
		if (host != null) builder.append("host=").append(host);
		if (port != null) builder.append(", port=").append(port);
		builder.append(", properties={\n");
		final Enumeration<?> it = propertyNames();
		while (it.hasMoreElements())
		{
			final String key = (String) it.nextElement();
			builder.append(key).append("=").append(getProperty(key)).append("\n");
		}
		builder.append("}");
		return builder.toString();
	}
}

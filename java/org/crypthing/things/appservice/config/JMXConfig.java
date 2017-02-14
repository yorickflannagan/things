package org.crypthing.things.appservice.config;

import java.util.Enumeration;
import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.w3c.dom.Node;

public class JMXConfig extends ConfigProperties
{
	private static final long serialVersionUID = 3230906824839606641L;
	private String host;
	private String port;

	public JMXConfig(String host, String port) { this.host = host; this.port = port; }
	public JMXConfig(final Config cfg, final Node node)
	{
		host = cfg.getValue("./@host", node);
		port = cfg.getValue("./@port", node);
		final Iterator<Property> props = cfg.getValueCollection("./property", new PropertyFactory(cfg)).iterator();
		while (props.hasNext()) add(props.next());
	}

	public String getHost() { return host; }
	public void setHost(String host) { this.host = host; }
	public String getPort() { return port; }
	public void setPort(String port) { this.port = port; }
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

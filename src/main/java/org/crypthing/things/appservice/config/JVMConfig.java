package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.ConvertToInt;
import org.crypthing.things.config.ConvertToLong;
import org.w3c.dom.Node;

public class JVMConfig
{
	private final int minMemory;
	private final int maxMemory;
	private final String vmflags;
	private final JMXConfig jmx;
	private final String name;
	private final long heartbeat;
	private final ClasspathConfig classpath;
	private final ConfigProperties properties;
	public JVMConfig(final Config xml, final Node root) throws ConfigException
	{
		try
		{
			final ConvertToInt converter = new ConvertToInt(0);
			final ConvertToLong longConv = new ConvertToLong(0);
			minMemory = xml.getValue("./minMemory", root, converter);
			maxMemory = xml.getValue("./maxMemory", root, converter);
			name= xml.getValue("./name", root);
			heartbeat = xml.getValue("./heartbeat", root, longConv);
			vmflags = xml.getValue("./vmflags", root);
			jmx = new JMXConfig(xml, xml.getNodeValue("./jmx", root));
			classpath = new ClasspathConfig(xml, xml.getNodeValue("./classpath", root));
			properties = new ConfigProperties(xml, xml.getNodeValue("./properties", root));
		}
		catch (final Throwable e) { throw new ConfigException(e); }
	}
	
	public int getMinMemory() { return minMemory; }
	public int getMaxMemory() { return maxMemory; }
	public String getVmflags() { return vmflags; }
	public String getName() { return name; }
	public JMXConfig getJmx() { return jmx; }
	public long getHeartbeat() { return heartbeat; }
	public ClasspathConfig getClasspath() { return classpath; }
	public ConfigProperties getProperties() { return properties; }
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		if (minMemory > 0) builder.append("minMemory=").append(minMemory).append(", ");
		if (maxMemory > 0) builder.append("maxMemory=").append(maxMemory).append(", ");
		if (vmflags != null) builder.append("vmflags=").append(vmflags).append(", ");
		if (jmx != null)
		{
			builder.append("jmx-host=").append(jmx.getHost()).append(", jmx-port=").append(jmx.getPort()).append(", ");
			if (jmx.size() > 0) builder.append("properties={\n").append(jmx).append("}, ");
		}
		if (classpath != null) builder.append("classpath=").append(classpath.getClasspath()).append(", ");
		if (properties != null) builder.append("properties={\n").append(properties).append("}");
		return builder.toString();
	}
}

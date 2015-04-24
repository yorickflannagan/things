package org.crypthing.things.appservice.config;

public class JVMConfig
{
	private int minMemory;
	private int maxMemory;
	private String vmflags;
	private JMXConfig jmx;
	private ClasspathConfig classpath;
	private ConfigProperties properties;
	public int getMinMemory() { return minMemory; }
	public void setMinMemory(int minMemory) { this.minMemory = minMemory; }
	public int getMaxMemory() { return maxMemory; }
	public void setMaxMemory(int maxMemory) { this.maxMemory = maxMemory; }
	public String getVmflags() { return vmflags; }
	public void setVmflags(String vmflags) { this.vmflags = vmflags; }
	public JMXConfig getJmx() { return jmx; }
	public void setJmx(JMXConfig jmx) { this.jmx = jmx; }
	public ClasspathConfig getClasspath() { return classpath; }
	public void setClasspath(ClasspathConfig classpath) { this.classpath = classpath; }
	public ConfigProperties getProperties() { return properties; }
	public void setProperties(ConfigProperties properties) { this.properties = properties; }
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

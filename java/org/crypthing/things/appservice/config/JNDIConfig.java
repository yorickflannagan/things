package org.crypthing.things.appservice.config;

public class JNDIConfig extends ConfigProperties
{
	private static final long serialVersionUID = -5641513766776895743L;
	private String implementation;
	public JNDIConfig(final String impl) { implementation = impl; }
	public String getImplementation() { return implementation; }
	public void setImplementation(final String impl) { implementation = impl; }
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder(512);
		if (implementation != null) builder.append("implementation=").append(implementation);
		builder.append(", properties={").append(super.toString()).append("}");
		return builder.toString();
	}
}

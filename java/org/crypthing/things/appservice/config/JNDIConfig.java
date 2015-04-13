package org.crypthing.things.appservice.config;

public class JNDIConfig extends ConfigProperties
{
	private static final long serialVersionUID = -5641513766776895743L;
	private String implementation;
	public JNDIConfig(final String impl) { implementation = impl; }
	public String getImplementation() { return implementation; }
	public void setImplementation(final String impl) { implementation = impl; }
	@Override
	public String toString() { return (new StringBuilder()).append("implementation=").append(implementation).append(", {").append(super.toString()).append("}").toString(); }
}

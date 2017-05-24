package org.crypthing.things.appservice.config;

import org.apache.commons.digester3.Digester;

public class JNDIConfig extends ConfigProperties
{
	public static void setConfig(final Digester digester, final String xmlPath, final String setMethod)
	{
		digester.addFactoryCreate(xmlPath + "/jndi", JNDIConfigFactory.class);
		digester.addObjectCreate(xmlPath + "/jndi/environment/property", Property.class);
		digester.addSetProperties(xmlPath + "/jndi/environment/property");
		digester.addSetNext(xmlPath + "/jndi/environment/property", "add");
		if (setMethod != null) digester.addSetNext(xmlPath + "/jndi", setMethod);
	}

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

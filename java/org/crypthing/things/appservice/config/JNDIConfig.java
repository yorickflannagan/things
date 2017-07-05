package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.w3c.dom.Node;

public class JNDIConfig extends ConfigProperties
{
	private static final long serialVersionUID = -5641513766776895743L;
	private final String implementation;
	public JNDIConfig(final Config xml, final Node root) throws ConfigException
	{
		super(xml, xml.getNodeValue("./environment", root));
		implementation = xml.getValue("./@implementation", root);
	}
	public String getImplementation() { return implementation; }
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder(512);
		if (implementation != null) builder.append("implementation=").append(implementation);
		builder.append(", properties={").append(super.toString()).append("}");
		return builder.toString();
	}
}

package org.crypthing.things.appservice.config;

import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.w3c.dom.Node;

public class JNDIConfig extends ConfigProperties
{
	private static final long serialVersionUID = -5641513766776895743L;
	private String implementation;

	public JNDIConfig(final String impl) { implementation = impl; }
	public JNDIConfig(final Config cfg, final Node node)
	{
		implementation = cfg.getValue("./@implementation", node);
		final Iterator<Property> props = cfg.getValueCollection("./environment/property", node, new PropertyFactory(cfg)).iterator();
		while (props.hasNext()) add(props.next());
	}

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

package org.crypthing.things.appservice.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public class JNDIConfig extends ConfigProperties
{
	private static final long serialVersionUID = -5641513766776895743L;
	private final String implementation;
	private final List<JDNIObject> objects;
	public JNDIConfig(final Config xml, final Node root) throws ConfigException
	{
		super(xml, xml.getNodeValue("./environment", root));
		implementation = xml.getValue("./@implementation", root);
		objects = new ArrayList<>();
		final Iterator<JDNIObject> it = xml.getValueCollection("./object", root, new Converter<JDNIObject>()
		{
			@Override
			public JDNIObject convert(final Object value) throws ClassCastException
			{
				try { return new JDNIObject(xml,(Node) value); }
				catch (final ConfigException e) { throw (ClassCastException)(new ClassCastException(e.getMessage())).initCause(e); }
			}
		}).iterator();
		while (it.hasNext())
		{
			final JDNIObject me = it.next();
			final Properties props = new Properties(this);
			props.putAll(me);
			me.putAll(props);
			objects.add(me);
		}
	}
	public String getImplementation() { return implementation; }
	public List<JDNIObject> getObjects() { return objects; }
	public static class JDNIObject extends ConfigProperties
	{
		private static final long serialVersionUID = 6084703502060562540L;
		private final String name;
		private final String factory;
		public JDNIObject(final Config xml, final Node root) throws ConfigException
		{
			super(xml, xml.getNodeValue("./environment", root));
			name = xml.getValue("./@name", root);
			factory = xml.getValue("./@factory", root);
		}
		public String getName() { return name; }
		public String getFactory() { return factory; }
		@Override
		public String toString()
		{
			final StringBuilder builder = new StringBuilder(512);
			if (name != null) builder.append("name=").append(name);
			if (factory != null) builder.append(", factory=").append(factory);
			builder.append(", properties={").append(super.toString()).append("}");
			return builder.toString();
		}
	}
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder(512);
		if (implementation != null) builder.append("implementation=").append(implementation);
		builder.append(", properties={").append(super.toString()).append("}");
		return builder.toString();
	}
}

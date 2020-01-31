package org.crypthing.things.appservice.config;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public class ConfigProperties extends Properties
{
	private static final long serialVersionUID = -5215347350360688948L;
	public ConfigProperties(final Config xml, final Node root) throws ConfigException
	{
		if (root != null)
		{
			final Iterator<Property> it = xml.getValueCollection("./property", root, new Converter<Property>()
			{
				@Override
				public Property convert(final Object value) throws ClassCastException
				{
					try { return new Property(xml, (Node) value); }
					catch (final ConfigException e) { throw (ClassCastException)(new ClassCastException(e.getMessage())).initCause(e); }
				}
			}).iterator();
			while (it.hasNext()) add(it.next());
		}
	}
	public void add(final Property prop) { if(prop != null && prop.getName() !=null && prop.getValue() != null) setProperty(prop.getName(), prop.getValue());}
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		final Enumeration<?> it = propertyNames();
		while (it.hasMoreElements())
		{
			final String key = (String) it.nextElement();
			builder.append(key).append("=").append(getProperty(key)).append("\n");
		}
		return builder.toString();
	}
}

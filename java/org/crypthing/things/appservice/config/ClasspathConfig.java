package org.crypthing.things.appservice.config;

import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public class ClasspathConfig
{
	private static final String PATH_SEPARATOR = System.getProperty("path.separator", ":");
	private final StringBuilder builder = new StringBuilder(256);
	public ClasspathConfig(final Config xml, final Node root) throws ConfigException
	{
		final Iterator<String> it = xml.getValueCollection("./path", root, new Converter<String>()
		{
			@Override public String convert(final Object value) throws ClassCastException
			{
				return ((Node) value).getTextContent();
			}
		}).iterator();
		while (it.hasNext()) add(it.next());
	}
	private void add(final String classpath) { builder.append(PATH_SEPARATOR).append(classpath); }
	public String getClasspath() { return builder.toString(); }
	@Override public String toString() { return getClasspath(); }
}

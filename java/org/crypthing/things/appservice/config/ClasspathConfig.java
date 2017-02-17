package org.crypthing.things.appservice.config;

import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public class ClasspathConfig
{
	private static final String PATH_SEPARATOR = System.getProperty("path.separator", ":");
	private final StringBuilder builder = new StringBuilder(256);
	public void add(final String classpath) { builder.append(PATH_SEPARATOR).append(classpath); }
	public String getClasspath() { return builder.toString(); }
	public ClasspathConfig() {}
	public ClasspathConfig(final Config cfg, final Node node)
	{
		final Iterator<String> paths = cfg.getValueCollection("./path", node, new Converter<String>()
		{
			@Override
			public String convert(final Object node) throws ClassCastException
			{
				return cfg.getValue(".", (Node) node);
			}
		}).iterator();
		while (paths.hasNext()) add(paths.next());
	}
}

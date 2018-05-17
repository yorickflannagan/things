package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public final class DataSourcesConfig extends HashMap<String, JDBCConfig>
{
	private static final long serialVersionUID = -3495959150998336098L;
	public DataSourcesConfig(final Config xml, final Node root) throws ConfigException
	{
		if(root != null)
		{
			final Iterator<JDBCConfig> it = xml.getValueCollection("./jdbc", root, new Converter<JDBCConfig>()
			{
				@Override
				public JDBCConfig convert(Object value) throws ClassCastException
				{
					try { return new JDBCConfig(xml, (Node) value); }
					catch (final ConfigException e) { throw (ClassCastException)(new ClassCastException(e.getMessage())).initCause(e); }
				}
				
			}).iterator();
			while (it.hasNext()) addJDBC(it.next());
		}
	}
	public JDBCConfig addJDBC(final JDBCConfig cfg) { return put(cfg.getName(), cfg); }
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		final Iterator<String> it = this.keySet().iterator();
		while (it.hasNext())
		{
			final String key = it.next();
			builder.append(key).append("={").append(get(key).toString()).append("}");
		}
		return builder.toString();
	}
}

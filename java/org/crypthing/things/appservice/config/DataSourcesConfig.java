package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public final class DataSourcesConfig extends HashMap<String, JDBCConfig>
{
	private static final long serialVersionUID = -3495959150998336098L;
	public DataSourcesConfig() { super(); }
	public DataSourcesConfig(final Config cfg, final Node node)
	{
		this();
		final Iterator<JDBCConfig> sources = cfg.getValueCollection("./jdbc", node, new Converter<JDBCConfig>()
		{
			@Override public JDBCConfig convert(final Object value) throws ClassCastException { return new JDBCConfig(cfg, (Node) value); }
		}).iterator();
		while (sources.hasNext()) addJDBC(sources.next());
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
			builder.append("jdbc={").append(get(key).toString()).append("}");
		}
		return builder.toString();
	}
}

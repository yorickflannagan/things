package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.Converter;
import org.w3c.dom.Node;

public class PropertyFactory implements Converter<Property>
{
	private final Config cfg;
	public PropertyFactory(final Config cfg) { this.cfg = cfg; }
	@Override
	public Property convert(final Object value) throws ClassCastException
	{
		return new Property(cfg.getValue("./@name", (Node) value), cfg.getValue("./@value", (Node) value));
	}
}

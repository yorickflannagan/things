package org.crypthing.things.config;

/**
 * Utility to convert a string do Short with default. 
 * @author magut
 *
 */
public class ConvertToShort implements Converter<Short>
{
	private final short def;
	public ConvertToShort(final short def) { this.def = def; }
	@Override public Short convert(final String value) throws ClassCastException
	{
		if (value == null) return def;
		try { return Short.parseShort(value); }
		catch (final NumberFormatException e) { throw new ClassCastException(e.getMessage()); }
	}
}

package org.crypthing.things.config;

/**
 * Utility to convert a string do Long with default. 
 * @author magut
 *
 */
public class ConvertToLong implements Converter<Long>
{
	private final long def;
	public ConvertToLong(final long def) { this.def = def; }
	@Override public Long convert(final Object value) throws ClassCastException
	{
		if (value == null) return def;
		try { return Long.parseLong((String) value); }
		catch (final NumberFormatException e) { throw new ClassCastException(e.getMessage()); }
	}
}

package org.crypthing.things.config;

/**
 * Utility to convert a string do Integer with default. 
 * @author magut
 *
 */
public final class ConvertToInt implements Converter<Integer>
{
	private final int def;
	public ConvertToInt(final int defaultValue) { def = defaultValue; }
	@Override public Integer convert(String value) throws ClassCastException
	{
		if (value == null) return def;
		try { return Integer.parseInt(value); }
		catch (final NumberFormatException e) { throw new ClassCastException(e.getMessage()); }
	}
}

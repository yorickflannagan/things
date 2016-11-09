package org.crypthing.things.config;

/**
 * Utility to convert a string do Float with default. 
 * @author magut
 *
 */
public final class ConvertToFloat implements Converter<Float>
{
	private final float def;
	public ConvertToFloat(final float def) { this.def = def; }
	@Override public Float convert(final Object value) throws ClassCastException
	{
		if (value == null) return def;
		try { return Float.parseFloat((String) value); }
		catch (final NumberFormatException e) { throw new ClassCastException(e.getMessage()); }
	}
}

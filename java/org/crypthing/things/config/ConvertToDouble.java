package org.crypthing.things.config;

/**
 * Utility to convert a string do Double with default. 
 * @author magut
 *
 */
public final class ConvertToDouble implements Converter<Double>
{
	private final double def;
	public ConvertToDouble(final double def) { this.def = def; }
	@Override public Double convert(final Object value) throws ClassCastException
	{
		if (value == null) return def;
		try { return Double.parseDouble((String) value); }
		catch (final NumberFormatException e) { throw new ClassCastException(e.getMessage()); }
	}
}

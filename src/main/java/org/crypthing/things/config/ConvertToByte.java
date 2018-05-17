package org.crypthing.things.config;

/**
 * Utility to convert a string do Byte with default. 
 * @author magut
 *
 */
public final class ConvertToByte implements Converter<Byte>
{
	private final byte def;
	public ConvertToByte(final byte def) { this.def = def; }
	@Override public Byte convert(final Object value) throws ClassCastException
	{
		if (value == null) return def;
		try { return Byte.parseByte((String) value); }
		catch (final NumberFormatException e) { throw new ClassCastException(e.getMessage()); }
	}
}

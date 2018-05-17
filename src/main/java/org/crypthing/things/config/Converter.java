package org.crypthing.things.config;

/**
 * Converts a configuration entry value to any given type.
 * @author magut
 *
 */
public interface Converter<T>
{
	/**
	 * Converts entry value to type T.
	 * @param value: entry value.
	 * @return the converted type.
	 * @throws ClassCastException
	 */
	T convert(Object value) throws ClassCastException;
}

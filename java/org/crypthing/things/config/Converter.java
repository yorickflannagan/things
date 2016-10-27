package org.crypthing.things.config;

/**
 * Converts a configuration entry value to any given type.
 * @author magut
 *
 */
public interface Converter<T>
{
	T convert(String value) throws ClassCastException;
}

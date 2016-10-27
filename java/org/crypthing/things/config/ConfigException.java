package org.crypthing.things.config;

/**
 * Thrown if an error occurs while trying to read configuration (whatever it is).
 * @author magut
 *
 */
public class ConfigException extends Exception
{
	private static final long serialVersionUID = 5968396243758615614L;

	public ConfigException(final String message) { super(message); }
	public ConfigException(final Throwable cause) { super(cause); }
	public ConfigException(final String message, final Throwable cause) { super(message, cause); }
}

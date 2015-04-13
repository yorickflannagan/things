package org.crypthing.things.appservice.config;

public class ConfigException extends Exception
{
	private static final long serialVersionUID = -8940975281085378577L;
	public ConfigException()
	{
		super();
	}
	public ConfigException(String message)
	{
		super(message);
	}
	public ConfigException(Throwable cause)
	{
		super(cause);
	}
	public ConfigException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public ConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

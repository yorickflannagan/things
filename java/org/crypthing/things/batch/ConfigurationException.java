package org.crypthing.things.batch;

public class ConfigurationException extends InitializeException
{
	private static final long serialVersionUID = 3121244619120810857L;
	public ConfigurationException()
	{
		super();
	}
	public ConfigurationException(String message)
	{
		super(message);
	}
	public ConfigurationException(Throwable cause)
	{
		super(cause);
	}
	public ConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}

package org.crypthing.things.batch;

public class InitializeException extends ResourceException
{
	private static final long serialVersionUID = 8258591645641338166L;
	public InitializeException()
	{
		super();
	}
	public InitializeException(String message)
	{
		super(message);
	}
	public InitializeException(Throwable cause)
	{
		super(cause);
	}
	public InitializeException(String message, Throwable cause)
	{
		super(message, cause);
	}
}

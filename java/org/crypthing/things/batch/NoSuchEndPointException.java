package org.crypthing.things.batch;

public class NoSuchEndPointException extends ResourceException
{
	private static final long serialVersionUID = 1530612531064200601L;
	public NoSuchEndPointException()
	{
		super();
	}
	public NoSuchEndPointException(String message)
	{
		super(message);
	}
	public NoSuchEndPointException(Throwable cause)
	{
		super(cause);
	}
	public NoSuchEndPointException(String message, Throwable cause)
	{
		super(message, cause);
	}
}

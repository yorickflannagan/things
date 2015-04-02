package org.crypthing.things.batch;

public final class CreateResourceException extends ResourceException
{
	private static final long serialVersionUID = -113707324824905254L;
	public CreateResourceException()
	{
		super();
	}
	public CreateResourceException(String message)
	{
		super(message);
	}
	public CreateResourceException(Throwable cause)
	{
		super(cause);
	}
	public CreateResourceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}

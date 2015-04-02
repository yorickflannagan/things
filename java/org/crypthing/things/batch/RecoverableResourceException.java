package org.crypthing.things.batch;

public final class RecoverableResourceException extends ResourceException
{
	private static final long serialVersionUID = 577218214560182005L;
	public RecoverableResourceException()
	{
		super();
	}
	public RecoverableResourceException(String message)
	{
		super(message);
	}
	public RecoverableResourceException(Throwable cause)
	{
		super(cause);
	}
	public RecoverableResourceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}

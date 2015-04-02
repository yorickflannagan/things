package org.crypthing.things.batch;

import java.io.IOException;

public class ResourceException extends IOException
{
	private static final long serialVersionUID = -7677216152372442690L;
	public ResourceException()
	{
		super();
	}
	public ResourceException(String message)
	{
		super(message);
	}
	public ResourceException(Throwable cause)
	{
		super(cause);
	}
	public ResourceException(String message, Throwable cause)
	{
		super(message, cause);
	}
}

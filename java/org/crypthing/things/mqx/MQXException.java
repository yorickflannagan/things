package org.crypthing.things.mqx;

import java.io.IOException;

public class MQXException extends IOException
{
	private static final long serialVersionUID = -4438285755318872700L;
	public MQXException()
	{
		super();
	}
	public MQXException(final String message)
	{
		super(message);
	}
	public MQXException(final Throwable cause)
	{
		super(cause);
	}
	public MQXException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}

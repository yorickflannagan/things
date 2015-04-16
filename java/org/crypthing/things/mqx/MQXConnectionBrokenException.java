package org.crypthing.things.mqx;

public final class MQXConnectionBrokenException extends MQXConnectionException
{
	private static final long serialVersionUID = -3663414544485167167L;
	public MQXConnectionBrokenException()
	{
		super();
	}
	public MQXConnectionBrokenException(final String message)
	{
		super(message);
	}
	public MQXConnectionBrokenException(final Throwable cause)
	{
		super(cause);
	}
	public MQXConnectionBrokenException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
	public MQXConnectionBrokenException(final String message, final int reason)
	{
		super(message, reason);
	}
	public MQXConnectionBrokenException(final String message, final Throwable cause, int reason)
	{
		super(message, cause, reason);
	}
}

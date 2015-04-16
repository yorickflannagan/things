package org.crypthing.things.mqx;

public class MQXConnectionException extends MQXException
{
	private static final long serialVersionUID = -3763214749222504723L;
	private int _reason;
	public MQXConnectionException()
	{
		super();
		_reason = 0;
	}
	public MQXConnectionException(final String message)
	{
		super(message);
		_reason = 0;
	}
	public MQXConnectionException(final Throwable cause)
	{
		super(cause);
		_reason = 0;
	}
	public MQXConnectionException(final String message, final Throwable cause)
	{
		super(message, cause);
		_reason = 0;
	}
	public MQXConnectionException(final String message, final int reason)
	{
		super(message);
		_reason = reason;
	}
	public MQXConnectionException(final String message, final Throwable cause, final int reason)
	{
		super(message, cause);
		_reason = reason;
	}
	public int getReason()
	{
		return _reason;
	}
}

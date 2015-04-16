package org.crypthing.things.mqx;

public final class MQXShutDownException extends MQXConnectionException
{
	private static final long serialVersionUID = 3444706876390260971L;
	public MQXShutDownException()
	{
		super();
	}
	public MQXShutDownException(String message)
	{
		super(message);
	}
	public MQXShutDownException(Throwable cause)
	{
		super(cause);
	}
	public MQXShutDownException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public MQXShutDownException(String message, int reason)
	{
		super(message, reason);
	}
	public MQXShutDownException(String message, Throwable cause, int reason)
	{
		super(message, cause, reason);
	}
}

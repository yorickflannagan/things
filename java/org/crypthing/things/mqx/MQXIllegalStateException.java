package org.crypthing.things.mqx;


public final class MQXIllegalStateException extends MQXException
{
	private static final long serialVersionUID = -9133590716916809637L;
	public MQXIllegalStateException()
	{
		super();
	}
	public MQXIllegalStateException(final String message)
	{
		super(message);
	}
	public MQXIllegalStateException(final Throwable cause)
	{
		super(cause);
	}
	public MQXIllegalStateException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}

package org.crypthing.things.messaging;

public final class MQXIllegalArgumentException extends MQXException
{
	private static final long serialVersionUID = 8832075227073090655L;
	public MQXIllegalArgumentException(String message) { super(message); }
	public MQXIllegalArgumentException(Throwable cause) { super(cause); }
	public MQXIllegalArgumentException(String message, Throwable cause) { super(message, cause); }
}

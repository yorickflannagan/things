package org.crypthing.things.messaging;


public final class MQXIllegalStateException extends MQXException
{
	private static final long serialVersionUID = -9133590716916809637L;
	public MQXIllegalStateException(String message) { super(message); }
	public MQXIllegalStateException(Throwable cause) { super(cause); }
	public MQXIllegalStateException(String message, Throwable cause) { super(message, cause); }
}

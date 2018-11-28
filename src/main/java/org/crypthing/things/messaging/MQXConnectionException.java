package org.crypthing.things.messaging;

public class MQXConnectionException extends MQXException
{
	private static final long serialVersionUID = -3763214749222504723L;
	public MQXConnectionException(String message) { super(message); }
	public MQXConnectionException(Throwable cause) { super(cause); }
	public MQXConnectionException(String message, Throwable cause) { super(message, cause); }
}

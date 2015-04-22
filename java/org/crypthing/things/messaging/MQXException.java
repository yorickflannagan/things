package org.crypthing.things.messaging;

import java.io.IOException;

public class MQXException extends IOException
{
	private static final long serialVersionUID = -4438285755318872700L;
	public MQXException(String message) { super(message); }
	public MQXException(Throwable cause) { super(cause); }
	public MQXException(String message, Throwable cause) { super(message, cause);}
}

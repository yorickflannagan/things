package org.crypthing.things.snmp;

public class EncodeException extends Exception
{
	private static final long serialVersionUID = -6194038137015859721L;
	public EncodeException() {}
	public EncodeException(final String message) { super(message); }
	public EncodeException(final Throwable cause) { super(cause); }
	public EncodeException(final String message, final Throwable cause) { super(message, cause); }
}

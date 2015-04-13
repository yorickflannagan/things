package org.crypthing.things.appservice;

import java.io.IOException;

public class ResourceException extends IOException
{
	private static final long serialVersionUID = 4424905733426928345L;
	public ResourceException() { super(); }
	public ResourceException(String message) { super(message); }
	public ResourceException(Throwable cause) { super(cause); }
	public ResourceException(String message, Throwable cause) { super(message, cause); }
}

package org.crypthing.things.events;

public class Event
{
	private String message;
	private Object launcher;
	private Object data;
	private String OID;
	private boolean relativeOID = true;
	public String getMessage() { return message; }
	public void setMessage(final String message) { this.message = message; }
	public Object getLauncher() { return launcher; }
	public void setLauncher(final Object launcher) { this.launcher = launcher; }
	public Object getData() { return data; }
	public void setData(final Object data) { this.data = data; }
	public String getOID() { return OID; }
	public void setOID(final String oID) { OID = oID; }
	public boolean isRelativeOID() { return relativeOID; }
	public void setRelativeOID(final boolean relativeOID) { this.relativeOID = relativeOID; }
}

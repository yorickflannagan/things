package org.crypthing.things.snmp;

public class Event
{
	private Encodable data;
	private String relativeOID;
	public Encodable getData() { return data; }
	public Event setData(final Encodable data) { this.data = data; return this; }
	public String getRelativeOID() { return relativeOID; }
	public Event setRelativeOID(final String relativeOID) { this.relativeOID = relativeOID; return this; }
}

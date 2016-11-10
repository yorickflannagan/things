package org.crypthing.things.snmp;

public interface Encodable
{
	void decode(String data) throws EncodeException;
	String encode();
}

package org.crypthing.things.snmp;

public interface Encodable
{
	public enum Type
	{
		STRING,
		OCTET_STRING,
		INTEGER32
	};
	void decode(String data) throws EncodeException;
	void decode(byte[] data) throws EncodeException;
	void decode(int data) throws EncodeException;
	Object encode();
	Type getEncoding();
}

package org.crypthing.things.snmp;

public class EncodableString implements Encodable
{
	final String msg;
	public EncodableString(final String msg) { this.msg = msg; }
	@Override public void decode(final String data) throws EncodeException {}
	@Override public void decode(final byte[] data) throws EncodeException {}
	@Override public void decode(final int data) throws EncodeException {}
	@Override public Object encode() { return msg; }
	@Override public Type getEncoding() { return Encodable.Type.STRING; }
}

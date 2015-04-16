package org.crypthing.things.mqx;

public interface IMQXMessage
{
	byte[] getMsgId();
	IMQXMessage setMsgId(byte[] msgId);
	byte[] getCorrelId();
	IMQXMessage setCorrelId(byte[] correlId);
	byte[] getMessage();
	IMQXMessage setMessage(byte[] message);
}
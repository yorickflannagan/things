package org.crypthing.things.messaging;


public class MQXMessage
{
	private byte[] _msgId;
	private byte[] _correlId;
	private byte[] _message;

	public MQXMessage()
	{
		this(null);
	}
	public MQXMessage(final byte[] message)
	{
		this(message, null);
	}
	public MQXMessage(final byte[] message, final byte[] msgId)
	{
		this(message, msgId, null);
	}
	public MQXMessage(final byte[] message, final byte[] msgId, final byte[] correlId)
	{
		_message = message;
		_msgId = msgId;
		_correlId = correlId;
	}

	public byte[] getMsgId()
	{
		return _msgId;
	}
	public MQXMessage setMsgId(final byte[] msgId)
	{
		_msgId = msgId;
		return this;
	}
	public byte[] getCorrelId()
	{
		return _correlId;
	}
	public MQXMessage setCorrelId(final byte[] correlId)
	{
		_correlId = correlId;
		return this;
	}
	public byte[] getMessage()
	{
		return _message;
	}
	public MQXMessage setMessage(final byte[] message)
	{
		_message = message;
		return this;
	}
}

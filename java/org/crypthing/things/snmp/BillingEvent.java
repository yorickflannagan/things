package org.crypthing.things.snmp;


public class BillingEvent extends NotificationEvent
{
	private static class Score implements Encodable
	{
		private int data;
		private Score(final int count) { data = count; }
		@Override public Object encode() { return data; }
		@Override public void decode(byte[] data) throws EncodeException { throw new EncodeException(); }
		@Override public void decode(int data) throws EncodeException { this.data = data; }
		@Override
		public void decode(String data) throws EncodeException
		{
			try { this.data = Integer.parseInt(data); }
			catch (final NumberFormatException e) { throw new EncodeException(e); }
		}
		@Override public Type getEncoding() { return Encodable.Type.INTEGER32; }
	}
	public BillingEvent(final int count)
	{
		super(new Score(count));
		setRelativeOID(getRelativeOID() + ".1");
	}
}

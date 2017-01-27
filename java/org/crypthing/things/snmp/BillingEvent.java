package org.crypthing.things.snmp;


public class BillingEvent extends NotificationEvent
{
	private static class Score implements Encodable
	{
		private String data;
		private Score(final int count) { data = Integer.toString(count); }
		@Override
		public void decode(String data) throws EncodeException
		{
			try { Integer.parseInt(data); }
			catch (final NumberFormatException e) { throw new EncodeException(e); }
			this.data = data;
		}
		@Override public String encode() { return data; }
	}
	public BillingEvent(final int count)
	{
		super(new Score(count));
		setRelativeOID(getRelativeOID() + ".1");
	}
}

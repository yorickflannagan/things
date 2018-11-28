package org.crypthing.things.snmp;

public class GatheredBillingEvent extends BillingEvent
{
	public GatheredBillingEvent(final int count)
	{
		super(count);
		setRelativeOID(".4.2");
	}
}

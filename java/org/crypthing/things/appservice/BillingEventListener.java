package org.crypthing.things.appservice;

import java.util.EventListener;

public interface BillingEventListener extends EventListener
{
	void incSuccess();
	void incFailure();
}

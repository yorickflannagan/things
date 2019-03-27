package org.crypthing.things.appservice;

import java.io.IOException;
import java.sql.SQLException;

import org.crypthing.things.snmp.EncodableString;
import org.crypthing.things.snmp.LifecycleEvent;
import org.crypthing.things.snmp.LifecycleEvent.LifecycleEventType;

public class Sandman extends Sandbox
{

	@Override
	protected boolean execute() throws IOException, SQLException
	{
		success();
		fire(new LifecycleEvent(LifecycleEventType.work, new EncodableString("Sandman is alive!")));
		return true;
	}
}
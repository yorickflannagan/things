package org.crypthing.things.snmp;

import java.io.IOException;

public class SNMPBridge implements NotificationEventListener
{
	private final Trap trap;
	public SNMPBridge(final String udpAddress, final String oidRoot) throws IOException { trap = new Trap(udpAddress, oidRoot); }
	@Override public void notify(final Event e) { trap.send(e); }
}

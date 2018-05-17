package org.crypthing.things.snmp;

import java.io.IOException;

public class SNMPBridge implements NotificationEventListener
{
	public static SNMPBridge newInstance(final String impl, final String udpAddress, final String oidRoot) throws IOException
	{
		if (impl == null) throw new NullPointerException();
		try
		{
			return (SNMPBridge) Thread
				.currentThread()
				.getContextClassLoader()
				.loadClass(impl)
				.getConstructor(String.class, String.class)
				.newInstance(udpAddress, oidRoot);
		}
		catch (final Throwable e) { return new SNMPBridge(udpAddress, oidRoot); }
	}
	private final Trap trap;
	public SNMPBridge(final String udpAddress, final String oidRoot) throws IOException { trap = new Trap(udpAddress, oidRoot); }
	@Override public void notify(final Event e) { trap.send(e); }
}

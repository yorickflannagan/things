package org.crypthing.things.appservice.config;

import java.util.Properties;

import org.apache.commons.digester3.Digester;
import org.crypthing.things.SNMPTrap;

public class SNMPConfig
{
	public static void setConfig(final Digester digester, final String xmlPath, final String setMethod)
	{
		digester.addObjectCreate(xmlPath + "/snmp", ConfigProperties.class);
		digester.addObjectCreate(xmlPath + "/snmp/property", Property.class);
		digester.addSetProperties(xmlPath + "/snmp/property");
		digester.addSetNext(xmlPath + "/snmp/property", "add");
		if (setMethod != null) digester.addSetNext(xmlPath + "/snmp", setMethod);
	}

	private static final String TRAP_ENTRY = "org.crypthing.things.SNMPTrap";
	private static final String UDP_ADDRESS_ENTRY = "org.crypthing.things.batch.udpAddress";
	private static final String ROOT_OID_ENTRY = "org.crypthing.things.batch.rootOID";
	private static final String RETRY_ENTRY = "org.crypthing.things.batch.retry";
	private static final String TIMEOUT_ENTRY = "org.crypthing.things.batch.timeout";
	public static SNMPTrap newInstance(final Properties cfg)
	{
		final String udpAddress = cfg.getProperty(UDP_ADDRESS_ENTRY);
		if (udpAddress == null) return null;
		final String rootOID = cfg.getProperty(ROOT_OID_ENTRY);
		if (rootOID == null) return null;
		final int retry = getIntValue(cfg.getProperty(RETRY_ENTRY), 2);
		final long timeout = getLongValue(cfg.getProperty(TIMEOUT_ENTRY), 5000);
		try
		{
			return (SNMPTrap) Thread.currentThread().getContextClassLoader().loadClass(cfg.getProperty(TRAP_ENTRY))
					.getConstructor(String.class, String.class, int.class, long.class)
					.newInstance(udpAddress, rootOID, retry, timeout);
		}
		catch (final Throwable e) { return new SNMPTrap(udpAddress, rootOID, retry, timeout); }
	}
	private static int getIntValue(final String value, final int defaultValue)
	{
		int ret = defaultValue;
		try { ret = Integer.parseInt(value); }
		catch (final Throwable e) {}
		return ret;
	}
	private static long getLongValue(final String value, final long defaultValue)
	{
		long ret = defaultValue;
		try { ret = Long.parseLong(value); }
		catch (final Throwable e) {}
		return ret;
	}
}

package org.crypthing.things;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


public class SNMPTrap implements Serializable
{
	private static final String TRAP_ENTRY = "org.crypthing.things.SNMPTrap";
	private static final String UDP_ADDRESS_ENTRY = "org.crypthing.things.batch.udpAddress";
	private static final String ROOT_OID_ENTRY = "org.crypthing.things.batch.rootOID";
	private static final String RETRY_ENTRY = "org.crypthing.things.batch.retry";
	private static final String TIMEOUT_ENTRY = "org.crypthing.things.batch.timeout";
	public static SNMPTrap createTrap(final Properties cfg)
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


	private static final String VAR_TIMER_ENTRY		= "eventTimer";
	private static final String VAR_MESSAGE_ENTRY		= "eventMessage";
	private static final String VAR_CAUSE_ENTRY		= "eventCause";
	private static final String VAR_CAUSE_MESSAGE_ENTRY	= "eventCauseMessage";
	private static final String VAR_JVM_NAME_ENTRY		= "JVMName";

	public static final String TIMER_RELATIVE_OID		= ".1.1";
	public static final String MESSAGE_RELATIVE_OID		= ".1.2";
	public static final String CAUSE_RELATIVE_OID		= ".1.3";
	public static final String CAUSEMSG_RELATIVE_OID	= ".1.4";
	public static final String JVM_NAME_RELATIVE_OID	= ".1.5";

	private static final long serialVersionUID = 7827169216381363122L;

	private final CommunityTarget comtarget;
	private final long startTime;
	private final OID enterprise;
	private transient Snmp snmp;

	protected final Map<String, OID> vars;
	protected final String rootOID;

	public SNMPTrap(final String udpAddress, final String oidRoot, int retry, long timeout)
	{
		if (udpAddress == null || udpAddress.length() == 0 || oidRoot == null || oidRoot.length() == 0) throw new IllegalArgumentException();
		vars = new HashMap<String, OID>();
		vars.put(VAR_TIMER_ENTRY,		new OID(oidRoot + TIMER_RELATIVE_OID));
		vars.put(VAR_MESSAGE_ENTRY,		new OID(oidRoot + MESSAGE_RELATIVE_OID));
		vars.put(VAR_CAUSE_ENTRY,		new OID(oidRoot + CAUSE_RELATIVE_OID));
		vars.put(VAR_CAUSE_MESSAGE_ENTRY,	new OID(oidRoot + CAUSEMSG_RELATIVE_OID));
		vars.put(VAR_JVM_NAME_ENTRY,		new OID(oidRoot + JVM_NAME_RELATIVE_OID));
		rootOID = oidRoot;
		comtarget = new CommunityTarget(new UdpAddress(udpAddress), new OctetString("public"));
		comtarget.setVersion(SnmpConstants.version2c);
		startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
		enterprise = new OID(oidRoot + ".0");
		try { snmp = new Snmp(new DefaultUdpTransportMapping()); }
		catch (final IOException e) { snmp = null; }
	}

	public void send(final String evtOID, final String message, final Throwable e)
	{
		try { send(addEventBindings(getDefaultBindings(), evtOID, message, e)); }
		catch (final Throwable f) {}
	}
	public void send(final PDU pdu)
	{
		if (snmp == null) return;
		try { snmp.send(pdu, comtarget); }
		catch (final Throwable e) {}
	}
	public String getRootOID() { return rootOID; }

	protected PDU getDefaultBindings()
	{
		final PDU pdu = new PDU();
		long time = System.currentTimeMillis();
		pdu.setType(PDU.TRAP);
		pdu.add(new VariableBinding(vars.get(VAR_TIMER_ENTRY), new Counter64(time)));
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks((time - startTime)/10)));
		pdu.add(new VariableBinding(SnmpConstants.sysObjectID, enterprise));
		pdu.add(new VariableBinding(vars.get(VAR_JVM_NAME_ENTRY), new OctetString(ManagementFactory.getRuntimeMXBean().getName())));
		return pdu;
	}

	protected PDU addEventBindings(final PDU pdu, final String evtOID, final String message, final Throwable e)
	{
		if (evtOID != null) pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(evtOID)));
		if (message != null) pdu.add(new VariableBinding(vars.get(VAR_MESSAGE_ENTRY), new OctetString(message)));
		if (e != null)
		{
			pdu.add(new VariableBinding(vars.get(VAR_CAUSE_ENTRY), new OctetString(e.getClass().getName())));
			final String msg = e.getMessage();
			if (msg != null && msg.length() > 0) pdu.add(new VariableBinding(vars.get(VAR_CAUSE_MESSAGE_ENTRY), new OctetString(msg)));
		}
		return pdu;
	}
}

package org.crypthing.things.snmp;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

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

public class Trap implements Serializable
{
	private class Bindings implements Serializable
	{
		private static final long serialVersionUID = -4032969166607803162L;
		private static final String ENTERPRISE_RELATIVE_OID = ".0";
		private static final String TIMER_RELATIVE_OID = ".1.1";
		private static final String JVM_NAME_RELATIVE_OID = ".1.2";
		private final OID timer;
		private final VariableBinding enterprise;
		private final VariableBinding jvm;
		private Bindings(final String root)
		{
			timer = new OID(root + TIMER_RELATIVE_OID);
			enterprise = new VariableBinding(SnmpConstants.sysObjectID, new OID(root + ENTERPRISE_RELATIVE_OID));
			jvm = new VariableBinding(new OID(root + JVM_NAME_RELATIVE_OID), new OctetString(ManagementFactory.getRuntimeMXBean().getName()));
		}
	}
	private static final long serialVersionUID = -5215304197662514761L;
	private static final long START_TIME = ManagementFactory.getRuntimeMXBean().getStartTime();
	private static final OctetString PUBLIC_TARGET = new OctetString("public");
	private static final Map<String, CommunityTarget> TARGETS = new HashMap<String, CommunityTarget>();
	private static final Map<String, Bindings> DEFAULTS = new HashMap<String, Trap.Bindings>();
	private Bindings defaults;
	private CommunityTarget comtarget;
	private final transient Snmp snmp;
	protected final String rootOID;
	public Trap(final String udpAddress, final String oidRoot) throws IOException
	{
		if (udpAddress == null || udpAddress.length() == 0 || oidRoot == null || oidRoot.length() == 0) throw new NullPointerException();
		if ((defaults = DEFAULTS.get(oidRoot)) == null)
		{
			defaults = new Bindings(oidRoot);
			DEFAULTS.put(oidRoot, defaults);
		}
		if ((comtarget = TARGETS.get(udpAddress)) == null)
		{
			comtarget = new CommunityTarget(new UdpAddress(udpAddress), PUBLIC_TARGET);
			comtarget.setVersion(SnmpConstants.version2c);
			TARGETS.put(udpAddress, comtarget);
		}
		rootOID = oidRoot + ".";
		snmp = new Snmp(new DefaultUdpTransportMapping());
	}
	public void send(final PDU pdu)
	{
		if (pdu == null) throw new NullPointerException();
		try { snmp.send(pdu, comtarget); }
		catch (final Throwable e) {}
	}
	public void send(final Event evt)
	{
		if (evt == null) throw new NullPointerException();
		final PDU pdu = getDefaultBindings();
		pdu.add(new VariableBinding(new OID(rootOID + evt.getRelativeOID()), new OctetString(evt.getData().encode())));
		send(pdu);
	}
	protected PDU getDefaultBindings()
	{
		final PDU pdu = new PDU();
		long time = System.currentTimeMillis();
		pdu.setType(PDU.TRAP);
		pdu.add(new VariableBinding(defaults.timer, new Counter64(time)));
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks((time - START_TIME)/10)));
		pdu.add(defaults.enterprise);
		pdu.add(defaults.jvm);
		return pdu;
	}
}

package org.crypthing.things.snmp;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;

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
	private static final long serialVersionUID = -5215304197662514761L;
	private static final String TIMER_RELATIVE_OID		= "1.1";
	private static final String JVM_NAME_RELATIVE_OID	= "1.2";

	private final CommunityTarget comtarget;
	private final long startTime;
	private final OID enterprise;
	private final transient Snmp snmp;
	protected final String rootOID;
	public Trap(final String udpAddress, final String oidRoot) throws IOException
	{
		if (udpAddress == null || udpAddress.length() == 0 || oidRoot == null || oidRoot.length() == 0) throw new NullPointerException();
		rootOID = oidRoot + ".";
		comtarget = new CommunityTarget(new UdpAddress(udpAddress), new OctetString("public"));
		comtarget.setVersion(SnmpConstants.version2c);
		startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
		enterprise = new OID(oidRoot + "0");
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
		pdu.add(new VariableBinding(new OID(rootOID + TIMER_RELATIVE_OID), new Counter64(time)));
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks((time - startTime)/10)));
		pdu.add(new VariableBinding(SnmpConstants.sysObjectID, enterprise));
		pdu.add(new VariableBinding(new OID(rootOID + JVM_NAME_RELATIVE_OID), new OctetString(ManagementFactory.getRuntimeMXBean().getName())));
		return pdu;
	}
}

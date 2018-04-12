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
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Trap implements Serializable
{
	private class Bindings implements Serializable
	{
		private static final long serialVersionUID = -4032969166607803162L;
		public static final String TIMER_RELATIVE_OID = ".1.1";
		public static final String JVM_NAME_RELATIVE_OID = ".1.2";
		public static final String JBOSS_RELATIVE_OID = ".1.3";
		private static final String JBOSS_SERVER = "jboss.server.name";
		private static final String THING_SERVER = "thing.server.name";
		private final OID timer;
		private final VariableBinding enterprise;
		private final VariableBinding jvm;
		private final VariableBinding jboss;
		private Bindings(final String root)
		{
			timer = new OID(root + TIMER_RELATIVE_OID);
			enterprise = new VariableBinding(SnmpConstants.sysObjectID, new OID(root));
			jvm = new VariableBinding(new OID(root + JVM_NAME_RELATIVE_OID), new OctetString(System.getProperty(THING_SERVER, ManagementFactory.getRuntimeMXBean().getName())));
			jboss = new VariableBinding(new OID(root + JBOSS_RELATIVE_OID), new OctetString(System.getProperty(JBOSS_SERVER, "")));
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
		final PDU pdu = addEventBindings(getDefaultBindings(), evt);
		final Encodable encode = evt.getData();
		final Variable var;
		if (encode != null)
		{
			switch (encode.getEncoding())
			{
			case STRING:
				var = new OctetString((String) encode.encode());
				break;
			case INTEGER32:
				var = new Integer32((int) encode.encode());
				break;
			default: var = new OctetString((byte[]) encode.encode());
			}
		}
		else var = new OctetString();
		pdu.add(new VariableBinding(new OID(rootOID + evt.getRelativeOID()), var));
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
		pdu.add(defaults.jboss);
		return pdu;
	}
	protected PDU addEventBindings(final PDU pdu, final Event evt) { return pdu; }
}

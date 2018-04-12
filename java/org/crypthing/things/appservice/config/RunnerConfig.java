package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;

public final class RunnerConfig
{
	private final JNDIConfig jndi;
	private final WorkerConfig worker;
	private final ConfigProperties sandbox;
	private final ConfigProperties snmp;
	private final DataSourcesConfig datasources;
	private final CursorsConfig cursors;
	private final ConnectorsConfig connectors;
	private final JVMConfig jvm;
	
	public RunnerConfig(final Config xml) throws ConfigException
	{
		jvm =  new JVMConfig(xml, xml.getNodeValue("/config/jvm"));
		jndi = new JNDIConfig(xml, xml.getNodeValue("/config/jndi"));
		worker = new WorkerConfig(xml, xml.getNodeValue("/config/worker"));
		sandbox = new ConfigProperties(xml, xml.getNodeValue("/config/sandbox"));
		snmp = new ConfigProperties(xml, xml.getNodeValue("/config/snmp"));
		datasources = new DataSourcesConfig(xml, xml.getNodeValue("/config/datasources"));
		cursors = new CursorsConfig(xml, xml.getNodeValue("/config/cursors"));
		connectors = new ConnectorsConfig(xml, xml.getNodeValue("/config/mqxconnectors"));
	}
	public WorkerConfig getWorker() { return worker; }
	public JNDIConfig getJndi() { return jndi; }
	public JVMConfig getJVM() { return jvm; }
	public ConfigProperties getSnmp() { return snmp; }
	public ConfigProperties getSandbox() { return sandbox; }
	public DataSourcesConfig getDatasources() { return datasources; }
	public ConnectorsConfig getConnectors() { return connectors; }
	public CursorsConfig getCursors() {return cursors;}
	@Override public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		if (jvm != null) builder.append("jvm={").append(worker.toString());
		if (worker != null) builder.append("worker={").append(worker.toString()).append("}, jndi={").append(jndi.toString()).append("}");
		if (snmp != null) builder.append(", snmp={").append(snmp.toString()).append("}");
		if (sandbox != null) builder.append(", sandbox={").append(sandbox.toString()).append("}");
		if (datasources != null) builder.append(", datasources={").append(datasources.toString()).append("}");
		if (connectors != null) builder.append(", connectors={").append(connectors.toString()).append("}");
		if (cursors != null) builder.append(", cursors={").append(cursors.toString()).append("}");
		return builder.toString();
	}
}

package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.w3c.dom.Node;


public final class RunnerConfig
{
	private JNDIConfig jndi;
	private WorkerConfig worker;
	private ConfigProperties sandbox;
	private ConfigProperties snmp;
	private DataSourcesConfig datasources;
	private CursorsConfig cursors;
	private ConnectorsConfig connectors;

	public RunnerConfig() {}
	public RunnerConfig(final Config cfg, final Node node)
	{
		jndi = new JNDIConfig(cfg, cfg.getNodeValue("./jndi", node));
		worker = new WorkerConfig(cfg, cfg.getNodeValue("./worker", node));
		sandbox = new ConfigProperties(cfg, cfg.getNodeValue("./sandbox", node));
		snmp = new ConfigProperties(cfg, cfg.getNodeValue("./snmp", node));
		datasources = new DataSourcesConfig(cfg, cfg.getNodeValue("./datasources", node));
		cursors = new CursorsConfig(cfg, cfg.getNodeValue("./cursors", node));
		connectors = new ConnectorsConfig(cfg, cfg.getNodeValue("./mqxconnectors", node));
	}

	public WorkerConfig getWorker() { return worker; }
	public void setWorker(WorkerConfig worker) { this.worker = worker; }
	public JNDIConfig getJndi() { return jndi; }
	public void setJndi(JNDIConfig jndi) { this.jndi = jndi; }
	public ConfigProperties getSnmp() { return snmp; }
	public ConfigProperties getSandbox() { return sandbox; }
	public void setSandbox(ConfigProperties sandbox) { this.sandbox = sandbox; }
	public void setSnmp(ConfigProperties snmp) { this.snmp = snmp; }
	public DataSourcesConfig getDatasources() { return datasources; }
	public void setDatasources(DataSourcesConfig datasources) { this.datasources = datasources; }
	public ConnectorsConfig getConnectors() { return connectors; }
	public void setConnectors(ConnectorsConfig connectors) { this.connectors = connectors; }
	public CursorsConfig getCursors() {return cursors;}
	public void setCursors(CursorsConfig cursors) {this.cursors = cursors;}
	@Override public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		if (worker != null) builder.append("worker={").append(worker.toString()).append("}, jndi={").append(jndi.toString()).append("}");
		if (snmp != null) builder.append(", snmp={").append(snmp.toString()).append("}");
		if (sandbox != null) builder.append(", sandbox={").append(sandbox.toString()).append("}");
		if (datasources != null) builder.append(", datasources={").append(datasources.toString()).append("}");
		if (connectors != null) builder.append(", connectors={").append(connectors.toString()).append("}");
		if (cursors != null) builder.append(", cursors={").append(cursors.toString()).append("}");
		return builder.toString();
	}
}

package org.crypthing.things.appservice.config;


public final class RunnerConfig
{
	private WorkerConfig worker;
	private JNDIConfig jndi;
	private ConfigProperties snmp;
	private ConfigProperties sandbox;
	private DataSourcesConfig datasources;
	private ConnectorsConfig connectors;
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
	@Override public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("worker={").append(worker.toString()).append("}, jndi={").append(jndi.toString()).append("}");
		if (snmp != null) builder.append(", snmp={").append(snmp.toString()).append("}");
		if (sandbox != null) builder.append(", sandbox={").append(sandbox.toString()).append("}");
		if (datasources != null) builder.append(", datasources={").append(datasources.toString()).append("}");
		if (connectors != null) builder.append(", connectors={").append(connectors.toString()).append("}");
		return builder.toString();
	}
}

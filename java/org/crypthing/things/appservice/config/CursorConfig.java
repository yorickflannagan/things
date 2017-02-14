package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.w3c.dom.Node;

public class CursorConfig {

	private String name;
	private String datasource;
	private String implementation;
	private int maxMemoryRecords;
	private long sleepBeetwenRun;

	public CursorConfig() {}
	public CursorConfig(final Config cfg, final Node node)
	{
		
	}

	public  String getName() {return name; }
	public  void setName(String name) { this.name = name;}
	public  String getDatasource() { return datasource;}
	public  void setDatasource(String datasource) { this.datasource = datasource;}
	public String getImplementation() {return implementation;}
	public void setImplementation(String implementation) {this.implementation = implementation;}	
	public int getMaxMemoryRecords() {return maxMemoryRecords; }
	public void setMaxMemoryRecords(int maxMemoryRecords) { this.maxMemoryRecords = maxMemoryRecords;}
	public long getSleepBeetwenRun() { return sleepBeetwenRun;}
	public void setSleepBeetwenRun(long sleepBeetwenRun) { this.sleepBeetwenRun = sleepBeetwenRun;}
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder(512);
		if (name != null) builder.append("name=").append(name).append(", ");
		if (datasource != null) builder.append("datasource=").append(datasource).append(", ");
		if (implementation != null) builder.append("implementation=").append(implementation).append(", ");
		builder.append("maxMemoryRecords=").append(maxMemoryRecords).append(", ");
		builder.append("sleepBeetwenRun=").append(sleepBeetwenRun);
		return builder.toString();
	}
	
	
}

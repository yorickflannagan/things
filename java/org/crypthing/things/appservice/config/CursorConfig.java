package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.ConvertToInt;
import org.crypthing.things.config.ConvertToLong;
import org.w3c.dom.Node;

public class CursorConfig
{

	private final String name;
	private final String datasource;
	private final String implementation;
	private final long sleepBeetwenRun;
	private final int maxMemoryRecords;
	
	public CursorConfig(final Config xml, final Node root) throws ConfigException
	{
		name = xml.getValue("./name", root);
		datasource = xml.getValue("./datasource", root);
		implementation = xml.getValue("./implementation", root);
		sleepBeetwenRun = xml.getValue("./sleepBeetwenRun", root, new ConvertToLong(0));
		maxMemoryRecords = xml.getValue("./maxMemoryRecords", root, new ConvertToInt(0));
	}
	public  String getName() {return name; }
	public  String getDatasource() { return datasource;}
	public String getImplementation() {return implementation;}
	public int getMaxMemoryRecords() {return maxMemoryRecords; }
	public long getSleepBeetwenRun() { return sleepBeetwenRun;}
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

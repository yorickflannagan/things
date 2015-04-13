package org.crypthing.things.appservice.config;


public final class QueueConfig extends ConfigProperties
{
	private static final long serialVersionUID = -4871598702526422748L;
	private String name;
	public QueueConfig(final String name) { this.name = name; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	@Override public String toString() { return (new StringBuilder()).append("name=").append(name).append("{").append(super.toString()).append("}").toString(); }
}
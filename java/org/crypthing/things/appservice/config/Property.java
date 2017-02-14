package org.crypthing.things.appservice.config;

public final class Property
{
	private String name;
	private String value;
	public Property() {}
	public Property(final String name, final String value) { this.name = name; this.value = value; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value;}
}

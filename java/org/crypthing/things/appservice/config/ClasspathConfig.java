package org.crypthing.things.appservice.config;

public class ClasspathConfig
{
	private static final String PATH_SEPARATOR = System.getProperty("path.separator", ":");
	private final StringBuilder builder = new StringBuilder(256);
	public void add(final String classpath) { builder.append(PATH_SEPARATOR).append(classpath); }
	public String getClasspath() { return builder.toString(); }
}

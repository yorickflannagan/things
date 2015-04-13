package org.crypthing.things.appservice;

import java.util.Properties;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.crypthing.things.SNMPTrap;
import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.ConfigValidation;

public class SNMPFactory extends Reference implements ConfigValidation
{
	private static final long serialVersionUID = -4418710602442275346L;
	private final Properties config;
	public SNMPFactory(final Properties cfg, final String name)
	{
		super(SNMPFactory.class.getName(), new StringRefAddr("java:snmp", name));
		config = cfg;
	}
	public SNMPTrap newTrap() { return SNMPTrap.createTrap(config); }
	@Override
	public void validate() throws ConfigException
	{
		if (SNMPTrap.createTrap(config) == null) throw new ConfigException("Invalid configuration for SNMP");
	}
}

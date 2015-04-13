package org.crypthing.things.appservice;

import javax.naming.NamingException;

import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.RunnerConfig;

public interface BindServices
{
	void bind(RunnerConfig cfg) throws ConfigException, NamingException;
}

package org.crypthing.things.appservice;

import javax.naming.NamingException;

import org.crypthing.things.config.ConfigException;
import org.crypthing.things.appservice.config.RunnerConfig;
import org.crypthing.things.snmp.ProcessingEventListener;

public interface BindServices
{
	void bind(RunnerConfig cfg, ReleaseResourceEventDispatcher subscriber, ProcessingEventListener trap) throws ConfigException, NamingException;
}

package org.crypthing.things.appservice;

import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.snmp.ProcessingEventListener;

public interface ResourceProvider
{
	void init(Object config, ReleaseResourceEventDispatcher subscriber, ProcessingEventListener trap) throws ConfigException;
}

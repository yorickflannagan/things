package org.crypthing.things.appservice;

import org.crypthing.things.appservice.config.ConnectorConfig;

public interface MQXConnectionWrapper
{
	void setConfig(ConnectorConfig cfg);
	void close() throws ResourceException;
}

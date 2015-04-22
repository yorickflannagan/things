package org.crypthing.things.messaging;

import java.util.Properties;

public interface MQXConnection
{
	void initConnection(Properties props) throws MQXIllegalArgumentException;
	void close() throws MQXIllegalStateException, MQXConnectionException;
	MQXQueue openQueue(Properties props) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException;
	MQXQueue openQueue(String name) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException;
	void begin() throws MQXConnectionException;
	void commit() throws MQXConnectionException;
	void back() throws MQXConnectionException;
	boolean isValid();
}

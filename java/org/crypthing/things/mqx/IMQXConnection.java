package org.crypthing.things.mqx;

import java.util.Properties;


public interface IMQXConnection
{
	void initConnection(Properties props) throws MQXIllegalArgumentException;
	void close() throws MQXIllegalStateException, MQXConnectionException;
	IMQXQueue openQueue(Properties props) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException;
	IMQXQueue openQueue(String name) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException;
	void begin() throws MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException;
	void commit() throws MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException;
	void back() throws MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException;
	boolean isValid();
}
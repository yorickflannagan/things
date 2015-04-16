package org.crypthing.things.mqx;


public interface IMQXQueue
{
	void close() throws MQXIllegalStateException, MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException;
	IMQXMessage send(IMQXMessage msg) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException;
	IMQXMessage receive(IMQXMessage msg) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException;
	IMQXMessage receive(IMQXMessage msg, int timeout) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException;
}
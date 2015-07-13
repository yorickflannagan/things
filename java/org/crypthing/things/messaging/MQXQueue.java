package org.crypthing.things.messaging;


public interface MQXQueue
{
	void close() throws MQXIllegalStateException, MQXConnectionException;
	MQXMessage send(MQXMessage msg) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException;
	MQXMessage send(MQXMessage msg, int expire) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException;
	MQXMessage receive(MQXMessage msg) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException;
	MQXMessage receive(MQXMessage msg, int timeout) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException;
}

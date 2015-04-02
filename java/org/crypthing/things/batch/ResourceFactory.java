package org.crypthing.things.batch;

public interface ResourceFactory extends Resource
{
	SourcePoint createSource() throws CreateResourceException;
	EndPointList createEndPoints() throws CreateResourceException;
	TransactionCoordinator createTransaction() throws CreateResourceException;
}

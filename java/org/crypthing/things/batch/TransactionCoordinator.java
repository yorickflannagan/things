package org.crypthing.things.batch;

public interface TransactionCoordinator extends Resource
{
	void begin();
	void commit();
	void rollback();
}

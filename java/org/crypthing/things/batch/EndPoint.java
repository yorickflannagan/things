package org.crypthing.things.batch;


public interface EndPoint extends Resource
{
	void write(Object o) throws RecoverableResourceException, ResourceException;
}

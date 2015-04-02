package org.crypthing.things.batch;


public interface SourcePoint extends Resource
{
	Object read() throws RecoverableResourceException, ResourceException;
}

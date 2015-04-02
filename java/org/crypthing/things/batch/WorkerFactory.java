package org.crypthing.things.batch;


public interface WorkerFactory extends Resource
{
	Worker createWorker() throws CreateResourceException;
}

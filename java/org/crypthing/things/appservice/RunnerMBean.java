package org.crypthing.things.appservice;

public interface RunnerMBean
{
	void shutdown();
	long getCounter();
	int getWorkers();
}

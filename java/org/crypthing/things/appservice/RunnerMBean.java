package org.crypthing.things.appservice;

public interface RunnerMBean
{
	void shutdown();
	long getSuccessCount();
	long getErrorCount();
	int getWorkerCount();
}

package org.crypthing.things.appservice;

public interface ServiceMBean
{
	void shutdown();
	void shutdown(String key);
	long getSuccessCount();
	long getErrorCount();
	int getWorkerCount();
	String[] getEnvironment();
	String getConfigFile();
}

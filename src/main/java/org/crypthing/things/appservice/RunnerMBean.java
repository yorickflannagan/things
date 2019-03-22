package org.crypthing.things.appservice;

public interface RunnerMBean
{
	void shutdown();
	long getSuccessCount();
	long getErrorCount();
	int getWorkerCount();
	String[] getEnvironment();
	String getConfigFile();

	String getInputArguments();		// RuntimeMXBean.getInputArguments
	String[] getSystemProperties();	// RuntimeMXBean.getSystemProperties
}

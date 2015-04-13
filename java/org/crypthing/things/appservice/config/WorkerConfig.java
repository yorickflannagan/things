package org.crypthing.things.appservice.config;

public final class WorkerConfig
{
	private String impl;
	private int threads;
	private long delay;
	public String getImpl() { return impl; }
	public void setImpl(String impl) { this.impl = impl; }
	public int getThreads() { return threads; }
	public void setThreads(int threads) { this.threads = threads; }
	public long getDelay() { return delay; }
	public void setDelay(long delay) { this.delay = delay; }
	@Override public String toString() { return (new StringBuilder()).append("impl=").append(impl).append(", threads=").append(threads).append(", delay=").append(delay).toString(); }
}

package org.crypthing.things.appservice.config;

public final class WorkerConfig
{
	private String impl;
	private int threads;
	private long delay;
	private boolean restartable;
	private long sleep;
	public WorkerConfig(String implementation) { impl = implementation; }
	public String getImpl() { return impl; }
	public void setImpl(String impl) { this.impl = impl; }
	public int getThreads() { return threads; }
	public void setThreads(int threads) { this.threads = threads; }
	public long getDelay() { return delay; }
	public void setDelay(long delay) { this.delay = delay; }
	public boolean isRestartable() { return restartable; }
	public void setRestartable(boolean restartable) { this.restartable = restartable; }
	public long getSleep() { return sleep; }
	public void setSleep(long sleep) { this.sleep = sleep; }
	@Override public String toString()
	{
		final StringBuilder builder = new StringBuilder(128);
		if (impl != null) builder.append("impl=").append(impl);
		return builder.append(", threads=").append(threads)
			.append(", delay=").append(delay)
			.append(", restartable=").append(restartable)
			.append(", sleep=").append(sleep)
			.toString();
	}
}

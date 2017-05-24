package org.crypthing.things.appservice.config;

public final class WorkerConfig
{
	private String impl;
	private int threads;
	private int goal;
	private int ramp = 10;
	private int goalMeasure = 1000;
	private long delay;
	private boolean restartable;
	private long sleep;
	private long heartbeat;
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
	public long getHeartbeat() { return heartbeat; }
	public void setHeartbeat(final long heartbeat) { this.heartbeat = heartbeat; }
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder(128);
		if (impl != null) builder.append("impl=").append(impl);
		return builder.append(", threads=").append(threads)
			.append(", delay=").append(delay)
			.append(", restartable=").append(restartable)
			.append(", sleep=").append(sleep)
			.append(", heartbeat=").append(heartbeat)
			.toString();
	}
	public int getGoal() {
		return goal;
	}
	public void setGoal(int goal) {
		this.goal = goal;
	}
	public int getRamp() {
		return ramp;
	}
	public void setRamp(int ramp) {
		this.ramp = ramp;
	}
	public int getGoalMeasure() {
		return goalMeasure;
	}
	public void setGoalMeasure(int goalMeasure) {
		this.goalMeasure = goalMeasure;
	}
}

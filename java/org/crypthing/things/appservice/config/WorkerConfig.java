package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConvertToInt;
import org.crypthing.things.config.ConvertToLong;
import org.w3c.dom.Node;

public final class WorkerConfig
{
	private String impl;
	private int threads;
	private boolean restartable;
	private long sleep;
	private long heartbeat;
	private int goal;
	private int ramp = 10;
	private int goalMeasure = 1000;

	public WorkerConfig(String implementation) { impl = implementation; }
	public WorkerConfig(final Config cfg, final Node node)
	{
		impl = cfg.getValue("./@implementation", node);
		threads = cfg.getValue("./threads", node, new ConvertToInt(1));
		restartable = Boolean.parseBoolean(cfg.getValue("./restartable", node));
		sleep = cfg.getValue("./sleep", node, new ConvertToLong(0));
		heartbeat = cfg.getValue("./heartbeat", node, new ConvertToLong(0));
		goal = cfg.getValue("./goal", node, new ConvertToInt(0));
		ramp = cfg.getValue("./ramp", node, new ConvertToInt(10));
		goalMeasure = cfg.getValue("./goalMeasure", node, new ConvertToInt(1000));
	}

	public String getImpl() { return impl; }
	public void setImpl(String impl) { this.impl = impl; }
	public int getThreads() { return threads; }
	public void setThreads(int threads) { this.threads = threads; }
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
			.append(", restartable=").append(restartable)
			.append(", sleep=").append(sleep)
			.append(", heartbeat=").append(heartbeat)
			.append(", goal=").append(goal)
			.append(", ramp=").append(ramp)
			.append(", goalMeasure=").append(goalMeasure)
			.toString();
	}
	public int getGoal() { return goal; }
	public void setGoal(int goal) { this.goal = goal; }
	public int getRamp() { return ramp; }
	public void setRamp(int ramp) { this.ramp = ramp; }
	public int getGoalMeasure() { return goalMeasure; }
	public void setGoalMeasure(int goalMeasure) { this.goalMeasure = goalMeasure; }
}

package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.config.ConvertToInt;
import org.crypthing.things.config.ConvertToLong;
import org.w3c.dom.Node;

public final class WorkerConfig
{
	private final String impl;
	private final int threads;
	private final boolean restartable;
	private final long sleep;
	private final long heartbeat;
	private final int goal;
	private final int ramp;
	private final int goalMeasure;
	public WorkerConfig(final Config xml, final Node root) throws ConfigException
	{
		final ConvertToInt intConv = new ConvertToInt(0);
		final ConvertToLong longConv = new ConvertToLong(0);
		impl = xml.getValue("./@implementation", root);
		threads = xml.getValue("./threads", root, intConv);
		restartable = Boolean.parseBoolean(xml.getValue("./restartable", root));
		sleep = xml.getValue("./sleep", root, longConv);
		heartbeat = xml.getValue("./heartbeat", root, longConv);
		goal = xml.getValue("./goal", root, intConv);
		ramp = xml.getValue("./ramp", root, new ConvertToInt(10));
		goalMeasure = xml.getValue("./goalMeasure", root, new ConvertToInt(1000));
	}
	public String getImpl() { return impl; }
	public int getThreads() { return threads; }
	public boolean isRestartable() { return restartable; }
	public long getSleep() { return sleep; }
	public long getHeartbeat() { return heartbeat; }
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder(128);
		if (impl != null) builder.append("impl=").append(impl);
		return builder.append(", threads=").append(threads)
			.append(", restartable=").append(restartable)
			.append(", sleep=").append(sleep)
			.append(", heartbeat=").append(heartbeat)
			.toString();
	}
	public int getGoal() { return goal; }
	public int getRamp() { return ramp; }
	public int getGoalMeasure() { return goalMeasure; }
}

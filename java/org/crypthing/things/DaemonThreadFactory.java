package org.crypthing.things;

import java.util.concurrent.ThreadFactory;

public final class DaemonThreadFactory implements ThreadFactory
{

	@Override
	public Thread newThread(Runnable r)
	{
		final Thread ret = new Thread(r);
		ret.setDaemon(true);
		return ret;
	}

}

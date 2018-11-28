package org.crypthing.things.stress;

import java.util.concurrent.CountDownLatch;

public interface Worker extends Runnable {
    public void doWork() throws Exception;
	public long[] getAvail();
	public int getErrors();
	public long getAverage();
	public long[][] getMeasures();
	public int getDone();
	public int getTotal();
	public void setStartSignal(CountDownLatch startSignal);
	public void setDoneSignal(CountDownLatch doneSignal);
	public void reset();
	
}

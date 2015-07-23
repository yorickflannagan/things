package org.crypthing.things.stress;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractSimpleWorkerFactory implements WorkerFactory {

	protected CountDownLatch startSignal;
	protected CountDownLatch doneSignal;
	protected int times;
	protected Properties prop;

	public AbstractSimpleWorkerFactory() {
		// TODO Auto-generated constructor stub
	}

	public Worker getWorker() {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(CountDownLatch startSignal, CountDownLatch doneSignal,
			int times, Properties p) {
		this.startSignal = startSignal;
		this.doneSignal = doneSignal;
		this.times = times;
		this.prop = p;

	}

}

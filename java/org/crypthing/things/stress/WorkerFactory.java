package org.crypthing.things.stress;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public interface WorkerFactory {

	Worker getWorker();
	void init(CountDownLatch startSignal, CountDownLatch doneSignal, int times,
			Properties p);
	void end();
	
}

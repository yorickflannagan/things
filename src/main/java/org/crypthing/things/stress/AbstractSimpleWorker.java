package org.crypthing.things.stress;

import java.util.concurrent.CountDownLatch;

public abstract class AbstractSimpleWorker implements Worker {
	protected long[] avail;
	protected long[][] measures;
	protected int errors = 0;
	protected long average;
	protected int done = 0;
	protected int total = 0;
	private CountDownLatch startSignal;
	private CountDownLatch doneSignal;	
	
	protected AbstractSimpleWorker(CountDownLatch startSignal, CountDownLatch doneSignal,int times) {
		avail = new long[times];
		measures = new long[times][2];
		this.startSignal=startSignal;
		this.doneSignal = doneSignal;
		
	}
	
	public final long[] getAvail() {
		return avail;
	}
	public final int getErrors() {
		return errors;
	}
	public final long getAverage() {
		return average;
	}
	public final long[][] getMeasures() {
		return measures;
	}
	
	public final int getDone() {
		return done;
	}
   public final int getTotal() {
		return total;
   }
   
   public void run() {
		  total = 0;
	      try {
	        startSignal.await();
	        for(int i = 0;i< avail.length;i++) {
	        	measures[i][0] = System.nanoTime();
		        try {
		        	doWork();
			        done++;
		        }
		        catch(Exception e) {
		        	errors++;
		        	e.printStackTrace();
		        }
		        finally {
		        	measures[i][1] = System.nanoTime();
			        avail[i] = measures[i][1] - measures[i][0];
			        total += avail[i];
			        average +=avail[i]/avail.length;
		        }
		        Thread.yield();
	        }
	        //average = total/done; == overflow
	       
	      } catch (InterruptedException ex) {
	      }
	      finally{
		        doneSignal.countDown(); // return;
	      }
	   }
   
	public void setStartSignal(CountDownLatch startSignal)
	{
		this.startSignal=startSignal;
	}
	
	public void setDoneSignal(CountDownLatch doneSignal)
	{
		this.doneSignal=doneSignal;		
	}

	public void reset()
	{
		for(int i = 0; i < avail.length; i++)
		{
			avail[i] = 0;
		}
		errors = 0;
		average = 0;
		done = 0;
		total = 0;		
		
	}
	
	
}

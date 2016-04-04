package org.crypthing.things.appservice;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadAdvisor {
	
	private long goal;
	private Map<Integer, Average> performance = new HashMap<Integer, Average>();
	private static Logger log = Logger.getLogger(ThreadAdvisor.class.getName());
	private int soften;
	private int softenRatio;
	private long accRatio;
	
	private static final int EXTASIS_RATIO = 3;
	private static final int WISE_EXTASIS_RATIO = 10;
	private static final int WISE_RATIO = 50;
	private static final int TOO_WISE_RATIO = 100;
	
	
	private int sandwichExtasis = EXTASIS_RATIO;
	private int wiseBoredomExtasis = WISE_EXTASIS_RATIO;
	
	public class Average
	{
		private double average;
		private int weigth;
		public final int threads;
		public Average(int threads)
		{
			this.threads = threads;
		}
		
		public double getAverage()
		{
			return average;
		}
		
		public void acumulate(double value)
		{
			if(value!=0) // Ignore if value = 0;
			{
				// Atenuate, but don't ignore;
				if(average > 0)
				{
					if(value < average*1/3) value = (value + 3*average)/4;
					if(value > average*3)   value = (value + 3*average)/4;					
				}
				double result = value + average*weigth++;
				average =result/weigth;
				if(weigth>TOO_WISE_RATIO) weigth = WISE_RATIO;
			}
		}
	}

	public ThreadAdvisor(long goal, int softenRatio)
	{
		this.goal = goal;
		this.softenRatio = softenRatio;
	}
	
	private double accConsume;
	public int whichWay(int threads, double consume)
	{
		int r = 0;	
		try
		{
			if(log.isLoggable(Level.FINE)) log.fine("Advisor: Threads=>" + threads + ", Consume=>" + format(consume) );
			Average current = performance.get(threads);
			if(current==null)
			{
					current = new Average(threads);
					performance.put(threads, current);
					if(log.isLoggable(Level.FINE)) log.fine("Advisor: Current Null, created.");
					soften = softenRatio;
			}
			current.acumulate(consume);
			accConsume += consume;
			accRatio++;
			
			if(soften > 0)
			{
				if(log.isLoggable(Level.FINE)) log.fine("Advisor: soften period:" + soften);
				soften--;
				return 0;
			}
			
			
			if(accConsume <= (current.average*accRatio)*0.1)
			{
				// Consume is too low compared to average consume. 
				// Lowering threads, don't remember why, but there's a reason, so don't touch that if you are not sure.
				if(log.isLoggable(Level.FINE)) 
				{
					log.fine("Advisor: Will return -1 because of below statistics:");
					log.fine("Advisor: AccConsume:" + format(accConsume));
					log.fine("Advisor: current.average:" + format(current.average));
					log.fine("Advisor: accRatio:" + accRatio);
				}
				r = -1;
			}
			else if(current.average < goal)
			{
				if(log.isLoggable(Level.FINE)) log.fine("Advisor: goal:" + goal + " not achieved. Current average: " + format(current.average) + " Finding best direction.");
				Average best = findBest(current);
				if(best.threads != current.threads)
				{
					r = (best.threads -  current.threads) > 0 ? 1 : threads > 1 ? -1: 0;
					if(log.isLoggable(Level.FINE)) log.fine("Advisor: Going from " + current.threads + " to " + best.threads + " direction.");
				} 
				else
				{
					//We are the best and yet, not good enough. Look around for expansion... 
					r = performance.get(threads+1) == null || accConsume/accRatio > current.average * 1.1  || performance.get(threads+1).weigth < WISE_RATIO  ? 1 : threads > 1 && (performance.get(threads-1)==null  || accConsume/accRatio < current.average * 0.9 || performance.get(threads-1).weigth < WISE_RATIO) ? -1:0;
					if(log.isLoggable(Level.FINE)) 
					{
						log.fine("Advisor: Currently at the best thread count, but still don't achieve the goal. Lets do something returning :" + r);
						log.fine("Current Average:" + format(current.average));
						log.fine("Thread " + (current.threads + 1) + ((performance.get(threads+1)==null) ? " Not" : "") + " Present");
						log.fine("Thread " + (current.threads -1 ) + ((performance.get(threads-1)==null) ? " Not" : "") + " Present");
					}
					if(r==0)
					{
						if(sandwichExtasis > 0)
						{
							if(log.isLoggable(Level.FINE)) log.fine("Advisor: 0 for long times can be harmful. Extasis on:" + sandwichExtasis);
						}
						sandwichExtasis--;
						if(sandwichExtasis < 0)
						{
							if(log.isLoggable(Level.FINE)) log.fine("Advisor: 0 for long times can be harmful. Breaking boundaries.");
							sandwichExtasis = EXTASIS_RATIO;
							performance.remove(threads-1);
							performance.remove(threads+1);
						}
					}
					else
					{
						if(sandwichExtasis != EXTASIS_RATIO)
						{
							sandwichExtasis = EXTASIS_RATIO;
							if(log.isLoggable(Level.FINE)) log.fine("Advisor: Arms streched again. SandwichExtasis on:" + sandwichExtasis);
						}
					}
				}
			}
			else
			{
				if(log.isLoggable(Level.FINE)) log.fine("Advisor: Goal achieved!!!");
				if(current.average > goal*1.05)
				{
					if(log.isLoggable(Level.FINE)) log.fine("Advisor: Perhaps too much. Current average:" + format(current.average) + " Goal: " + goal + ". Lets see statistics if we can fire someone.");
					Average projection = performance.get(threads-1);
					r = (projection != null && projection.weigth >= WISE_RATIO && projection.average < goal) ? 0 :-1;
					if(log.isLoggable(Level.FINE)) 
					{
						if(projection !=null) {
							log.fine("Advisor: Projection Average :" + format(projection.average) + " for " +  (threads-1) + " workers.");
							log.fine("Advisor: Projection Weigth  :" + projection.weigth + " for " +  (threads-1) + " workers.");
						} else
						{
							log.fine("Advisor: No Projection for:" +  (threads-1) + " workers.");
						}
						if(r < 0) log.fine("Advisor: Statistics say we can fire :" + r + " workers.");
						else
						{
							wiseBoredomExtasis--;
							log.fine("Advisor: Statistics say we better stay were we are.");
							if(wiseBoredomExtasis <= 0)
							{
								log.fine("Advisor: Perhaps they think thenselves too smart. Next time think again.");
								projection.weigth = WISE_RATIO - softenRatio;
								wiseBoredomExtasis = WISE_EXTASIS_RATIO;
							}
						}
					}
				}
			}
		}
		catch(Throwable t)
		{
			log.log(Level.SEVERE, "Error on ThreadAdvisor, returning 0",t);
			r = 0;
		}
		
		if(threads ==1 && r == -1)
		{
			if(log.isLoggable(Level.FINE)) log.fine("Advisor: Can't reduce threads to 0.");
			r = 0;
		}

		if(r != 0 )
		{
			sandwichExtasis = EXTASIS_RATIO;
			wiseBoredomExtasis = WISE_EXTASIS_RATIO;
		}
		
		
		accConsume = 0;
		accRatio=0;
		soften = softenRatio;
		return r;
	}


	private Average findBest(Average what) {
		Average best = what;
		for(Average a: performance.values())
		{
			if(a.average > best.average) best = a;
		}
		return best;
	}
	public void printAverages()
	{
		System.out.println("Thread\t Average");
		
		Average[] avg = new Average[performance.size()];
		avg = performance.values().toArray(avg);
		Arrays.sort(avg, new Comparator<Average>() {

			@Override
			public int compare(Average o1, Average o2) {
				if(o1.average < o2.average) return -1;
				if(o1.average > o2.average) return +1;
				return 0;
			}
		});
		for(Average a: avg)
		{
			System.out.println(a.threads + "\t"+ format(a.average));
		}
	}
	
	public Collection<Average> getAverages()
	{
		return performance.values();
	}
	
	static NumberFormat nf = NumberFormat.getInstance();
	static {
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
	}
	
	private static String format(double value)
	{
		return nf.format(value);
	}
	
	

}

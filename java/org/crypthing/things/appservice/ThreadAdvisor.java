package org.crypthing.things.appservice;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ThreadAdvisor {
	
	private long goal;
	private Map<Integer, Average> performance = new HashMap<Integer, Average>();
	private int soften;
	private int softenRatio;
	
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
					if(value < average*2/3) value = (value + 5*average)/6;
					if(value > average*3/2) value = (value + 3*average)/4;					
				}
				double result = value + average*weigth++;
				average =result/weigth;
			}
		}
	}

	public ThreadAdvisor(long goal, int softenRatio)
	{
		this.goal = goal;
		this.softenRatio = softenRatio;
	}
	
	
	public int whichWay(int threads, double consume)
	{
		Average current = performance.get(threads);
		if(current==null)
		{
				current = new Average(threads);
				performance.put(threads, current);
		}
		current.acumulate(consume);
		
		if(soften > 0)
		{
			soften--;
			return 0;
		}
		
		if(consume < goal && ((int)consume) > 0)
		{
			Average best = findBest(current);
			soften = softenRatio;
			if(best.average > current.average)
			{
				return (best.threads -  current.threads) > 0 ? 1 : threads > 1 ? -1: 0;
			} 
			else
			{
				//We are the best and yet, not good enough. Look around for expansion... 
				return performance.get(threads+1) == null ? 1 : threads > 1 && performance.get(threads-1)==null ? -1:0;
			}
		}
		else
		{
			soften = softenRatio;
			if(threads > 1)
			{
				if(consume > goal*1.05)
				{
					Average projection = performance.get(threads-1);
					if(projection != null && projection.weigth > 30 && projection.average < goal) return 0; else return  -1;
				}
				else
				{
					if((int)consume == 0)
					{
						return -1;
					}
				}
			}
		}
		return 0;
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
		NumberFormat nf = NumberFormat.getInstance(Locale.ITALY);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
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
			System.out.println(a.threads + "\t"+ nf.format(a.average));
		}
	}
	
	public Collection<Average> getAverages()
	{
		return performance.values();
	}
	
	
	

}

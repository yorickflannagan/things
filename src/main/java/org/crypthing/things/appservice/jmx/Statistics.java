package org.crypthing.things.appservice.jmx;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

/**
 * Statistic
 */
public class Statistics implements StatisticsMBean {

    private final int longStatCount;
    private final int doubleStatCount;
    private final Map<Thread, StatJoin> stats = new HashMap<>();
    private final String[] longStatsName;
    private final String[] doubleStatsName;
    public static final String MBEAN_PATTERN = "org.crypthing.things.appservice.jmx:type=Statistics,name=";

    public Statistics(String name, String[] longStats, String[] doubleStats) {
        longStatCount = (longStats != null) ? longStats.length : 0;
        doubleStatCount = (doubleStats != null) ? doubleStats.length : 0;
        longStatsName = Arrays.copyOf(longStats, longStats.length);
        doubleStatsName = Arrays.copyOf(doubleStats, longStats.length);
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(this,
                    new ObjectName((new StringBuilder(256)).append(MBEAN_PATTERN).append(name).toString()));
        } catch (Exception e) {
            throw new RuntimeException("Could not start statistics for [" + name + "]",e);
        }

    }

    public void join()
    {
        if(stats.get(Thread.currentThread())== null)
        {
            StatJoin me = new StatJoin(longStatCount, doubleStatCount);
            stats.put(Thread.currentThread(), me);
        }
    }


    @Override public int getLongStatCount() { return longStatCount; }
    @Override public int getDoubleStatCount() { return doubleStatCount; }
    @Override public long getLongStat(int statpos) {
        if(statpos >= longStatCount) return Long.MIN_VALUE;
        StatJoin[] stattmp = stats.values().toArray(new StatJoin[stats.size()]);
        long acc=0;
        for (int i = 0; i < stattmp.length; i++) {
            acc+=(stattmp[i].ls[statpos]);
        }
        return acc;
    }

    @Override
    public double getDoubleStat(int statpos) {
        if(statpos >= doubleStatCount) return Double.NaN;
        StatJoin[] stattmp = stats.values().toArray(new StatJoin[stats.size()]);
        double acc=0;
        for (int i = 0; i < stattmp.length; i++) {
            acc+=(stattmp[i].ds[statpos]);
        }
        return acc;
    }

    public long getCurrentLongStat(int statpos) {
        final StatJoin stattmp = stats.get(Thread.currentThread());
        if(stattmp ==null || statpos >= longStatCount)  return Long.MIN_VALUE;
         return (stattmp.ls[statpos]);
    }

    public double getCurrentDoubleStat(int statpos) {
        final StatJoin stattmp = stats.get(Thread.currentThread());
        if(stattmp ==null || statpos >= doubleStatCount) return Double.NaN;
        return (stattmp.ds[statpos]);
    }

    public void setCurrentLongStat(int statpos, long value) {
        final StatJoin stattmp = stats.get(Thread.currentThread());
        if(stattmp ==null || statpos >= longStatCount)  return;
        stattmp.ls[statpos] = value;
    }

    public void setCurrentDoubleStat(int statpos, double value) {
        final StatJoin stattmp = stats.get(Thread.currentThread());
        if(stattmp ==null || statpos >= doubleStatCount) return;
        stattmp.ds[statpos] = value;
    }

    @Override
    public String getLongStatName(int statpos) {
        if(statpos >= longStatCount) return null;
        return longStatsName[statpos];
    }

    @Override
    public String getDoubleStatNAme(int statpos) {
        if(statpos >= doubleStatCount) return null;
        return doubleStatsName[statpos];
    }

    /**
     * InnerStatistics
     */
    public class StatJoin {
        StatJoin(int longLen, int doubleLen)
        {
            ds = new double[doubleLen];
            ls = new long[longLen];            
        }
        private final double[] ds;
        private final long[] ls;
    }

    
}
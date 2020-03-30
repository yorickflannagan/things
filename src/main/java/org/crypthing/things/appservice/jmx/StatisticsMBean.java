package org.crypthing.things.appservice.jmx;

/**
 * StatisticsMBean
 */
public interface StatisticsMBean {
    int getLongStatCount();
    int getDoubleStatCount();
    long getLongStat(int statpos);
    double getDoubleStat(int statpos);
    String getLongStatName(int statpos);
    String getDoubleStatNAme(int statpos);
}
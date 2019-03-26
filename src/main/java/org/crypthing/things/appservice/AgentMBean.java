package org.crypthing.things.appservice;

public interface AgentMBean
{
    void shutdown();
    int launch(String config);
}
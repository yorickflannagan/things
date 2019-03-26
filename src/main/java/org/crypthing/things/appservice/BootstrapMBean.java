package org.crypthing.things.appservice;

public interface BootstrapMBean
{
    void shutdown();
    int launch(String config);
}
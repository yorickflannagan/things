package org.crypthing.things.appservice;

public interface BootstrapMBean
{
    int ALIVE = 1;
    int DEAD = 0;
    int UNKNOW = -1; 

    void shutdown();
    int launch(String config);
    int launch(String config, String home, String[] env);
    int stop(String name);
    int forceStop(String name, int timeout);
    int isAlive(String name);
    String[] list();

}
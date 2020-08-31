package org.crypthing.things.appservice;

import org.crypthing.things.config.ConfigException;

public interface BootstrapMBean
{
    
    //transform in a enum? Probably not. JMX won't like.
    
	int NO_CONFIG_FOUND = 1;
	int COULD_NOT_STOP = 2;
	int NO_PROCESS_FOUND = 3;
	int EXCEPTION_WHILE_STOP = 4;
	int ALREADY_STOPPED = 5;
	int EXCEPTION_WHILE_FORCE_STOP = 6;
    int EXCEPTION_ON_LAUNCH = 7;
    int STOP_TIMEOUT = 8;
    int SERVER_ALREADY_STARTED = 9;
    int SERVER_ALIVE=10;
    int EXCEPTION_ON_WAIT=11;

	int OK = 0;
    
    int ALIVE = 0;
    int DEAD = 1;
    int UNKNOW = -1; 

    void shutdown();
    int launch(String config);
    int launch(String config, String home, String[] env);
    String getLaunch(String config, String[] env)  throws ConfigException;
    int stop(String name, int timeout);
    int forceStop(String name, int timeout);
    int isAlive(String name);
    String[] list();
    int exitCode(String name);
    int waitFor(String name, long timeout);

}
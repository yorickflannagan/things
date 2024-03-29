package org.crypthing.things.snmp;

import java.util.EventListener;

public interface LifecycleEventListener extends EventListener
{
	void start(LifecycleEvent e);
	void work(LifecycleEvent e);
	void stop(LifecycleEvent e);
	void heart(LifecycleEvent e);
}

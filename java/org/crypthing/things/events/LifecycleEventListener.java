package org.crypthing.things.events;

import java.util.EventListener;

public interface LifecycleEventListener extends EventListener
{
	void start(LifecycleEvent e);
	void work(LifecycleEvent e);
	void stop(LifecycleEvent e);
}

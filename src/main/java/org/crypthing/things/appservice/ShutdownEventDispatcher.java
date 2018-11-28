package org.crypthing.things.appservice;

public interface ShutdownEventDispatcher
{
	void setShutdownEventListener(ShutdownEventListener listener);
}

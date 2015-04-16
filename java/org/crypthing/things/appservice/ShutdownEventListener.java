package org.crypthing.things.appservice;

import java.util.EventListener;

public interface ShutdownEventListener extends EventListener
{
	void signal(Sandbox worker);
	void abandon(Sandbox worker);
	void abort(Sandbox worker);
}

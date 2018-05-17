package org.crypthing.things.appservice;

import java.util.EventListener;

public interface ReleaseResourceListener extends EventListener
{
	void release(long resource);
}

package org.crypthing.things.events;

import java.util.EventListener;

public interface ProcessingEventListener extends EventListener
{
	void info(ProcessingEvent e);
	void warning(ProcessingEvent e);
	void error(ProcessingEvent e);
}

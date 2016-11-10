package org.crypthing.things.snmp;

import java.util.EventListener;

public interface ProcessingEventListener extends EventListener
{
	void info(ProcessingEvent e);
	void warning(ProcessingEvent e);
	void error(ProcessingEvent e);
}

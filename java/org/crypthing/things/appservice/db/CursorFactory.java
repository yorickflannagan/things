package org.crypthing.things.appservice.db;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.crypthing.things.appservice.ReleaseResourceEventDispatcher;
import org.crypthing.things.appservice.ResourceProvider;
import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.CursorConfig;
import org.crypthing.things.snmp.ProcessingEventListener;

public class CursorFactory  extends Reference implements ResourceProvider {
	private static final long serialVersionUID = -1093196193015654010L;
	private Cursor cursor;
	private boolean initiated = false;

	public CursorFactory(String name) {super(CursorFactory.class.getName(), new StringRefAddr("java:cursor", name));}

	@Override
	public synchronized void init(Object config, ReleaseResourceEventDispatcher subscriber, ProcessingEventListener trap) throws ConfigException {
		if(initiated) throw new RuntimeException("Can't init more than once.");
		initiated = true;
		if (!(config instanceof CursorConfig)) throw new ConfigException(new IllegalArgumentException());
		cursor = new Cursor((CursorConfig) config, trap);
	}

	
	public Cursor getCursor()
	{
		return cursor;
	}
	
}

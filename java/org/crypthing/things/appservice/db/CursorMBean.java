package org.crypthing.things.appservice.db;

public interface CursorMBean {

	long getStock();
	String getSQLStatement();
	long getLastRecord();
	void reset();
	
}

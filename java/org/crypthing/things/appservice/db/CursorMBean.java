package org.crypthing.things.appservice.db;

public interface CursorMBean {

	long getStock();
	long getRemainder();
	String getSQLStatement();
	long getLastRecord();
	void reset();
	
}

package org.crypthing.things.appservice.db;

import javax.sql.DataSource;

public interface Resetable {
	
	long getStartRecord(DataSource ds);

}
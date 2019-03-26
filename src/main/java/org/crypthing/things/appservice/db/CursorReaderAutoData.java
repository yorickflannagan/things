package org.crypthing.things.appservice.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * CursorReaderAutoData
 */
public abstract class CursorReaderAutoData extends CursorReader {

	/**
	 * 
	 * @param rs - Connection to read
	 * @param list - List to fill
	 * @return token to remember(like: last record index)
	 * @throws SQLException
	 *  A good way to implement in a resultsetlike  result is 
     *  to call the fill(ResultSet rs, List<Object> list) method upon it.
	 */
	public abstract long fill(Connection rs, List<Object> list, long token) throws SQLException;

    

}
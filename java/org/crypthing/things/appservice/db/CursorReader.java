package org.crypthing.things.appservice.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;


public abstract class CursorReader {
	protected String[] columns;
	public abstract String getSQL();
	public abstract int fetchSize();
	
	/**
	 * 
	 * @param rs - ResultSet to read
	 * @param list - List to fill
	 * @return last record index
	 * @throws SQLException
	 * 
	 */
	public long fill(ResultSet rs, List<Object> list) throws SQLException
	{
		if(columns == null) fillColumns(rs.getMetaData());
		long lastRecord = 0;
		while(rs.next())
		{
			lastRecord = rs.getLong(1);
			list.add(readLine(rs));
		}
		return lastRecord;
	}
	
	
	private synchronized void fillColumns(ResultSetMetaData metaData) throws SQLException {
		if(columns != null) return;
		final int columncount = metaData.getColumnCount();
		String[]_columns = new String[columncount +1];
		for(int i = 1; i <= columncount; i++)
		{
			_columns[i] = metaData.getColumnLabel(i);
			if(_columns[i] == null)
			{
				throw new SQLException("Column " + i + " name is null");
			}
		}
		columns = _columns;
	}
	
	public Object readLine(ResultSet rs) throws SQLException
	{
		final DBRow row = new DBRow();
		for (int i = 1; i < columns.length; i++)
		{
			final String data = rs.getString(i);
			row.setProperty(columns[i], data == null ? "" : data );
		}
		return row;
	}
}

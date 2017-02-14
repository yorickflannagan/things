package org.crypthing.things.appservice;

import java.io.PrintWriter;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.JDBCConfig;
import org.crypthing.things.snmp.ProcessingEvent;
import org.crypthing.things.snmp.ProcessingEventListener;
import org.crypthing.things.snmp.ProcessingEvent.ProcessingEventType;

public class DataSourceFactory extends Reference implements DataSource, ResourceProvider, ReleaseResourceListener
{
	private static final long serialVersionUID = 8191301701768048738L;
	private static Map<Long, ConnectionWraper> instances = new ConcurrentHashMap<Long, ConnectionWraper>();
	private JDBCConfig config;
	private PrintWriter logWriter = new PrintWriter(System.out);
	private ProcessingEventListener trap;

	public DataSourceFactory(final String name) { super(DataSourceFactory.class.getName(), new StringRefAddr("java:jdbc", name)); }

	@Override
	public void init(final Object config, final ReleaseResourceEventDispatcher subscriber, final ProcessingEventListener trap) throws ConfigException
	{
		if (!(config instanceof JDBCConfig)) throw new ConfigException(new IllegalArgumentException());
		this.config = (JDBCConfig) config;
		this.trap = trap;
		try
		{
			Class.forName(this.config.getDriver());
			final ConnectionWraper conn = new ConnectionWraper(DriverManager.getConnection(this.config.getUrl(), this.config));
			conn.close();
			conn.forceClose();
		}
		catch (final Throwable e) { throw new ConfigException("Could not load JDBC driver", e); }
		subscriber.addReleaseResourceListener(this);
	}

	@Override
	public void release(final long resource)
	{
		final ConnectionWraper conn = instances.get(resource);
		if (conn != null)
		{
			try { conn.forceClose(); }
			catch (SQLException e) { trap.error(new ProcessingEvent(ProcessingEventType.error, "Could not close JDBC connection", e)); }
			instances.remove(resource);
		}
	}

	@Override 
	public Connection getConnection() throws SQLException
	{
		long id = Thread.currentThread().getId();
		ConnectionWraper ret = instances.get(id);
		if (ret == null || !isValid(ret))
		{
			ret = new ConnectionWraper(DriverManager.getConnection(this.config.getUrl(), this.config));
			instances.put(id, ret);
		}
		return ret;
	}
	@Override
	public Connection getConnection(final String username, final String password) throws SQLException
	{
		long id = Thread.currentThread().getId();
		ConnectionWraper ret = instances.get(id);
		if (ret == null || !isValid(ret))
		{
			ret = new ConnectionWraper(DriverManager.getConnection(config.getUrl(), username, password));
			instances.put(id, ret);
		}
		return ret;
	}
	private boolean isValid(final Connection conn)
	{
		boolean ret = false;
		final String query = config.getValidationQuery();
		try
		{
			if (query == null)
			{
				conn.setAutoCommit(!conn.getAutoCommit());
				conn.setAutoCommit(!conn.getAutoCommit());
			}
			else
			{
				final Statement statement = conn.createStatement();
				try { statement.execute(query); }
				finally { statement.close(); }
			}
			ret = true;
		}
		catch (final Throwable e) {}
		return ret;
	}
	@Override public PrintWriter getLogWriter() throws SQLException { return logWriter; }
	@Override public void setLogWriter(final PrintWriter out) throws SQLException { logWriter = out; }
	@Override public void setLoginTimeout(final int seconds) throws SQLException { DriverManager.setLoginTimeout(seconds); }
	@Override public int getLoginTimeout() throws SQLException { return DriverManager.getLoginTimeout(); }
	@Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }
	@Override public <T> T unwrap(final Class<T> iface) throws SQLException { throw new SQLException(); }
	@Override public boolean isWrapperFor(final Class<?> iface) throws SQLException { return false; }

	public class ConnectionWraper implements Connection
	{
		private final Connection conn;
		private ConnectionWraper(final Connection it) { conn = it;}
		@Override public <T> T unwrap(final Class<T> iface) throws SQLException { return conn.unwrap(iface); }
		@Override public boolean isWrapperFor(final Class<?> iface) throws SQLException { return conn.isWrapperFor(iface); }
		@Override public Statement createStatement() throws SQLException { return conn.createStatement(); }
		@Override public PreparedStatement prepareStatement(final String sql) throws SQLException { return conn.prepareStatement(sql); }
		@Override public CallableStatement prepareCall(final String sql) throws SQLException { return conn.prepareCall(sql); }
		@Override public String nativeSQL(final String sql) throws SQLException { return conn.nativeSQL(sql);}
		@Override public void setAutoCommit(final boolean autoCommit) throws SQLException { conn.setAutoCommit(autoCommit);}
		@Override public boolean getAutoCommit() throws SQLException { return conn.getAutoCommit();}
		@Override public void commit() throws SQLException { conn.commit(); }
		@Override public void rollback() throws SQLException { conn.rollback(); }
		@Override public boolean isClosed() throws SQLException { return conn.isClosed(); }
		@Override public DatabaseMetaData getMetaData() throws SQLException { return conn.getMetaData(); }
		@Override public void setReadOnly(final boolean readOnly) throws SQLException { conn.setReadOnly(readOnly); }
		@Override public boolean isReadOnly() throws SQLException { return conn.isReadOnly(); }
		@Override public void setCatalog(final String catalog) throws SQLException { conn.setCatalog(catalog); }
		@Override public String getCatalog() throws SQLException { return conn.getCatalog(); }
		@Override public void setTransactionIsolation(final int level) throws SQLException { conn.setTransactionIsolation(level); }
		@Override public int getTransactionIsolation() throws SQLException { return conn.getTransactionIsolation(); }
		@Override public SQLWarning getWarnings() throws SQLException { return conn.getWarnings(); }
		@Override public void clearWarnings() throws SQLException { conn.clearWarnings(); }
		@Override public Statement createStatement(final int resultSetType, int resultSetConcurrency) throws SQLException { return conn.createStatement(resultSetType, resultSetConcurrency); }
		@Override public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException { return conn.prepareStatement(sql, resultSetType, resultSetConcurrency); }
		@Override public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException { return conn.prepareCall(sql, resultSetType, resultSetConcurrency); }
		@Override public Map<String, Class<?>> getTypeMap() throws SQLException { return conn.getTypeMap(); }
		@Override public void setTypeMap(final Map<String, Class<?>> map) throws SQLException { conn.setTypeMap(map); }
		@Override public void setHoldability(final int holdability) throws SQLException { conn.setHoldability(holdability); }
		@Override public int getHoldability() throws SQLException { return conn.getHoldability(); }
		@Override public Savepoint setSavepoint() throws SQLException { return conn.setSavepoint(); }
		@Override public Savepoint setSavepoint(final String name) throws SQLException { return conn.setSavepoint(name); }
		@Override public void rollback(final Savepoint savepoint) throws SQLException { conn.rollback(savepoint); }
		@Override public void releaseSavepoint(final Savepoint savepoint) throws SQLException { conn.releaseSavepoint(savepoint); }
		@Override public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException { return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability); }
		@Override public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException { return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability); }
		@Override public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException { return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability); }
		@Override public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException { return conn.prepareStatement(sql, autoGeneratedKeys); }
		@Override public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException { return conn.prepareStatement(sql, columnIndexes); }
		@Override public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException { return conn.prepareStatement(sql, columnNames); }
		@Override public Clob createClob() throws SQLException { return conn.createClob(); }
		@Override public Blob createBlob() throws SQLException { return conn.createBlob(); }
		@Override public NClob createNClob() throws SQLException { return conn.createNClob(); }
		@Override public SQLXML createSQLXML() throws SQLException { return conn.createSQLXML(); }
		@Override public boolean isValid(final int timeout) throws SQLException { return conn.isValid(timeout); }
		@Override public void setClientInfo(final String name, final String value) throws SQLClientInfoException { conn.setClientInfo(name, value); }
		@Override public void setClientInfo(final Properties properties) throws SQLClientInfoException { conn.setClientInfo(properties); }
		@Override public String getClientInfo(final String name) throws SQLException { return conn.getClientInfo(name); }
		@Override public Properties getClientInfo() throws SQLException { return conn.getClientInfo(); }
		@Override public Array createArrayOf(final String typeName,final  Object[] elements) throws SQLException { return conn.createArrayOf(typeName, elements); }
		@Override public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException { return conn.createStruct(typeName, attributes); }
		@Override public void setSchema(final String schema) throws SQLException { conn.setSchema(schema); }
		@Override public String getSchema() throws SQLException { return conn.getSchema(); }
		@Override public void abort(final Executor executor) throws SQLException { conn.abort(executor); }
		@Override public void setNetworkTimeout(final Executor executor, int milliseconds) throws SQLException { conn.setNetworkTimeout(executor, milliseconds); }
		@Override public int getNetworkTimeout() throws SQLException { return conn.getNetworkTimeout(); }
		@Override public void close() throws SQLException {}
		private void forceClose() throws SQLException { conn.close(); }
	}
}

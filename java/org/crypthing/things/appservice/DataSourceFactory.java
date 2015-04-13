package org.crypthing.things.appservice;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.ConfigValidation;
import org.crypthing.things.appservice.config.JDBCConfig;

public class DataSourceFactory extends Reference implements DataSource, ConfigValidation
{
	private static final long serialVersionUID = 8191301701768048738L;
	final JDBCConfig config;
	private PrintWriter logWriter = new PrintWriter(System.out);
	public DataSourceFactory(final JDBCConfig cfg, final String name)
	{
		super(DataSourceFactory.class.getName(), new StringRefAddr("java:jdbc", name));
		config = cfg;
	}

	@Override
	public void validate() throws ConfigException
	{
		try { Class.forName(config.getDriver()); }
		catch (final ClassNotFoundException e) { throw new ConfigException("Could not load JDBC driver", e); }
	}
	@Override
	public PrintWriter getLogWriter() throws SQLException { return logWriter; }
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException { logWriter = out; }
	@Override
	public void setLoginTimeout(int seconds) throws SQLException { DriverManager.setLoginTimeout(seconds); }
	@Override
	public int getLoginTimeout() throws SQLException { return DriverManager.getLoginTimeout(); }
	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException { throw new SQLException(); }
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
	@Override
	public Connection getConnection() throws SQLException { return DriverManager.getConnection(config.getUrl(), config); }
	@Override
	public Connection getConnection(String username, String password) throws SQLException { return DriverManager.getConnection(config.getUrl(), username, password);}
}

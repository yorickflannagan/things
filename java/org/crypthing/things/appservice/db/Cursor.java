package org.crypthing.things.appservice.db;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.crypthing.things.appservice.InterruptEventListener;
import org.crypthing.things.appservice.Runner;
import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.CursorConfig;
import org.crypthing.things.events.ProcessingEvent;
import org.crypthing.things.events.ProcessingEvent.ProcessingEventType;
import org.crypthing.things.events.ProcessingEventListener;


public class Cursor implements CursorMBean, InterruptEventListener {
	
	private SQLMinion minion;
	private boolean hasShutdown = false;
	private long lastRecord = 0;
	private DataSource ds;
	private Object[] current;
	private long stock = 0;
	private int currentIndex = -1;
	private List<Object[]> stack = new LinkedList<Object[]>();
	private final ReentrantLock currentIndiceMutex = new ReentrantLock();
	private final ReentrantLock swapMutex = new ReentrantLock();
	ProcessingEventListener trap;
	public static final String MBEAN_PATTERN = "org.crypthing.things.appservice:type=Cursor,name=";
	
	private CursorReader cr;
	private CursorConfig config;

	public Cursor(CursorConfig config, ProcessingEventListener trap) throws  ConfigException
	{
		this.trap = trap;
		this.config = config;
		try
		{
			InitialContext ctx = new InitialContext();
			Object ds =  ctx.lookup("java:jdbc/" + config.getDatasource());
			if(!(ds instanceof DataSource)) throw new ConfigException("Datasource lookup failure. Object bound is not an datasource. [java:jdbc/" + this.config.getDatasource() +"]"); 
			cr = Class.forName(this.config.getImplementation()).asSubclass(CursorReader.class).newInstance();
			getData();
			
			ManagementFactory.getPlatformMBeanServer().registerMBean
			(
				this,
				new ObjectName
				(
					(new StringBuilder(256))
					.append(MBEAN_PATTERN)
					.append(config.getName())
					.toString()
				)
			);
		}
		catch(NamingException e)
		{
			trap.error(new ProcessingEvent(this, ProcessingEventType.error, "Could not initialize Cursor. Datasource not Found", e));			
			throw new ConfigException("Could not initialize Cursor. Datasource not Found", e);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			trap.error(new ProcessingEvent(this, ProcessingEventType.error, "Could not initialize Cursor. Problem with CursorReader class.", e));			
			throw new ConfigException("Could not initialize Cursor. Problem with CursorReader class.", e);
		} catch (SQLException e) {
			trap.error(new ProcessingEvent(this, ProcessingEventType.error, "Could not initialize Cursor. Problems fetching data.", e));			
			throw new ConfigException("Could not initialize Cursor. Problems fetching data.", e);
		} catch (InstanceAlreadyExistsException
				| MBeanRegistrationException | NotCompliantMBeanException
				| MalformedObjectNameException e) {
			throw new ConfigException("Could not initialize Cursor. Problems registering MBean.", e);
		}
		
		minion = new SQLMinion();
		minion.start();
		Runner.getInstance().addInterruptEventListener(this);
		trap.info(new ProcessingEvent(this, ProcessingEventType.info, "Cursor "+ config.getName()  + " initialized."));
	}
	
	public Object next()
	{
		ReentrantLock mutex = currentIndiceMutex;
		Object o = null;
		mutex.lock();
		try
		{
			if(currentIndex != -1 && currentIndex < current.length)
			{
				o = current[currentIndex++];
			} else
			{
				swap();
				if(currentIndex != -1 && currentIndex < current.length)
				{
					o = current[currentIndex++];
				}
			}
		}
		finally
		{
			mutex.unlock();
		}
		return o;
	}
	
	
	private void swap() {
		ReentrantLock _currentIndiceMutex = currentIndiceMutex;
		ReentrantLock _swapMutex = swapMutex;
		
		verify();
		_swapMutex.lock();
		try {
			_currentIndiceMutex.lock();
			try {
				if(stack.size()>0)
				{
					stock -= current.length;
					current = stack.get(0);
					currentIndex = 0;
					stack.remove(0);
				}
			} finally {
				_currentIndiceMutex.unlock();
			}
		} finally {
			_swapMutex.unlock();
		}
	}

	private void verify()
	{
		if(!minion.isAlive())minion.start();
		
	}
	
	private int getData() throws SQLException
	{
		ReentrantLock _swapMutex = swapMutex;
		String sql = cr.getSQL();
		int lenData = 0;
		List<Object> ret;
		Connection conn = ds.getConnection();
		try
		{
			PreparedStatement stm = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			try
			{
				stm.setLong(1, lastRecord); 
				ResultSet rs = stm.executeQuery();
				try
				{
					ret = new ArrayList<Object>(cr.fetchSize());
					long tmp = cr.fill(rs, ret);
					if(tmp != 0) lastRecord = tmp;
					if(!ret.isEmpty())
					{
						_swapMutex.lock();
						try
						{
							final Object[] oa = ret.toArray();
							lenData = oa.length;
							stock+= lenData;
							stack.add(oa);
						}
						finally
						{
							_swapMutex.unlock();
						}
					}
				}
				finally
				{
					rs.close();
				}
			}
			finally
			{
				stm.close();
			}
		}
		finally
		{
			conn.close();
		}
		return lenData;
	}
	
	private class SQLMinion implements Runnable
	{
		Thread sweetThreadOfMine;
		
		@Override
		public void run() {
			while(!(hasShutdown))
			{
				try
				{
					while(!(hasShutdown) && stock < config.getMaxMemoryRecords() && getData() > 0);
					if(!(hasShutdown)) Thread.sleep(config.getSleepBeetwenRun());
				}
				catch(Throwable t)	
				{
					trap.error(new ProcessingEvent(this, ProcessingEventType.error, "Problems fetching data.", t));			
				}
			}
			
		}
		
		public boolean isAlive()
		{
			return (sweetThreadOfMine != null && sweetThreadOfMine.isAlive());
		}
		
		
		public synchronized void start()
		{
			if((sweetThreadOfMine == null || !sweetThreadOfMine.isAlive()) && !hasShutdown) 
			{
				sweetThreadOfMine = new Thread(this);
				sweetThreadOfMine.setDaemon(true);
				sweetThreadOfMine.start();
			}
			
		}
	}

	@Override
	public void shutdown() {
		hasShutdown = true;
	}

	@Override
	public long getStock() {
		return stock;
	}

	@Override
	public String getSQLStatement() {
		return cr.getSQL();
	}

	@Override
	public long getLastRecord() {
		return lastRecord;
	}
}

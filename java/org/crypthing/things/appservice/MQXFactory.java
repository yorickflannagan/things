package org.crypthing.things.appservice;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.ConnectorConfig;
import org.crypthing.things.appservice.config.QueueConfig;
import org.crypthing.things.events.ProcessingEvent;
import org.crypthing.things.events.ProcessingEventListener;
import org.crypthing.things.events.ProcessingEvent.ProcessingEventType;
import org.crypthing.things.mqx.IMQXConnection;
import org.crypthing.things.mqx.IMQXQueue;
import org.crypthing.things.mqx.MQXConnectionBrokenException;
import org.crypthing.things.mqx.MQXConnectionException;
import org.crypthing.things.mqx.MQXIllegalArgumentException;
import org.crypthing.things.mqx.MQXIllegalStateException;
import org.crypthing.things.mqx.MQXShutDownException;

public class MQXFactory extends Reference implements ResourceProvider, ReleaseResourceListener
{
	private static final long serialVersionUID = 8707524334543571525L;
	private static final Map<Long, MQXConnectionWrapper> instances = new ConcurrentHashMap<Long, MQXConnectionWrapper>();
	private ConnectorConfig config;
	private ProcessingEventListener trap;

	public MQXFactory(final String name) { super(MQXFactory.class.getName(), new StringRefAddr("java:mqx", name)); }
	
	@Override
	public void init(final Object config, final ReleaseResourceEventDispatcher subscriber, final ProcessingEventListener trap) throws ConfigException
	{
		if (!(config instanceof ConnectorConfig)) throw new ConfigException(new IllegalArgumentException());
		this.config = (ConnectorConfig) config;
		this.trap = trap;
		try
		{
			final MQXConnectionWrapper tconn = newConnection();
			try
			{
				final Iterator<String> it = this.config.keySet().iterator();
				while (it.hasNext())
				{
					final IMQXQueue queue = tconn.openQueue(this.config.get(it.next()));
					queue.close();
				}
			}
			finally { tconn.forceClose(); }
		}
		catch (final Throwable e) { throw new ConfigException(e); }
	}

	@Override
	public void release(final long resource)
	{
		final MQXConnectionWrapper mq = instances.get(resource);
		if (mq != null)
		{
			try { mq.forceClose(); }
			catch (final Throwable e) { trap.error(new ProcessingEvent(this, ProcessingEventType.error, "Could not close MQX connection", e)); }
			instances.remove(resource);
		}
	}

	public IMQXConnection getConnection() throws MQXConnectionException
	{
		long id = Thread.currentThread().getId();
		MQXConnectionWrapper ret = instances.get(id);
		if (ret == null || !ret.isValid())
		{
			ret = newConnection();
			instances.put(id, ret);
		}
		return ret;
	}
	private MQXConnectionWrapper newConnection() throws MQXConnectionException
	{
		MQXConnectionWrapper ret = null;
		try
		{
			final IMQXConnection conn = (IMQXConnection) Class.forName(config.getDriver()).newInstance();
			conn.initConnection(config.getContext());
			ret = new MQXConnectionWrapper(conn);
		}
		catch (final Throwable e) { throw new MQXConnectionException(e); }
		return ret;
	}

	public class MQXConnectionWrapper implements IMQXConnection
	{
		private final IMQXConnection conn;
		private MQXConnectionWrapper(final IMQXConnection connection) { conn = connection; }
		@Override public void initConnection(final Properties props) throws MQXIllegalArgumentException { throw new MQXIllegalArgumentException(); }
		@Override public IMQXQueue openQueue(final Properties props) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException { throw new MQXIllegalArgumentException(); }
		@Override public void begin() throws MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException { conn.begin(); }
		@Override public void commit() throws MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException { conn.commit(); }
		@Override public void back() throws MQXConnectionBrokenException, MQXShutDownException, MQXConnectionException { conn.back(); }
		@Override public void close() throws MQXIllegalStateException, MQXConnectionException {}
		private void forceClose() throws MQXIllegalStateException, MQXConnectionException { conn.close(); }
		@Override public boolean isValid() { return conn.isValid(); }
		@Override
		public IMQXQueue openQueue(final String name) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException
		{
			final QueueConfig queue = config.get(name);
			if (queue == null) throw new MQXIllegalArgumentException("Queue name not found");
			return conn.openQueue(queue);
		}
	}
}

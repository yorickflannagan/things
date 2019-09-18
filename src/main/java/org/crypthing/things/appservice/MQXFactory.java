package org.crypthing.things.appservice;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.crypthing.things.config.ConfigException;
import org.crypthing.things.appservice.config.ConnectorConfig;
import org.crypthing.things.appservice.config.QueueConfig;
import org.crypthing.things.snmp.ProcessingEvent;
import org.crypthing.things.snmp.ProcessingEventListener;
import org.crypthing.things.snmp.ProcessingEvent.ProcessingEventType;
import org.crypthing.things.messaging.MQXConnection;
import org.crypthing.things.messaging.MQXConnectionException;
import org.crypthing.things.messaging.MQXIllegalArgumentException;
import org.crypthing.things.messaging.MQXIllegalStateException;
import org.crypthing.things.messaging.MQXMessage;
import org.crypthing.things.messaging.MQXQueue;

public class MQXFactory extends Reference implements ResourceProvider, ReleaseResourceListener
{
	private static final long serialVersionUID = 8707524334543571525L;
	private static final Map<Long, MQXConnectionWrapper> instances = new ConcurrentHashMap<Long, MQXConnectionWrapper>();
	private ConnectorConfig config;
	private ProcessingEventListener trap;
	private boolean inited = false;
	private Class<? extends MQXConnection> driverClass;
	public MQXFactory(final String name) { super(MQXFactory.class.getName(), new StringRefAddr("java:mqx", name)); }
	
	@Override
	public void init(final Object config, final ReleaseResourceEventDispatcher subscriber, final ProcessingEventListener trap) throws ConfigException
	{
		if(inited)throw new ConfigException("Already started");
		inited = true;
		if (!(config instanceof ConnectorConfig)) throw new ConfigException(new IllegalArgumentException());
		this.config = (ConnectorConfig) config;
		this.trap = trap;
		try
		{
			driverClass = Class.forName(this.config.getDriver()).asSubclass(MQXConnection.class);
			final MQXConnectionWrapper tconn = newConnection();
			try
			{
				final Iterator<String> it = this.config.keySet().iterator();
				while (it.hasNext())
				{
					final MQXQueue queue = tconn.openQueue(this.config.get(it.next()).getName());
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
			catch (final Throwable e) { trap.error(new ProcessingEvent(ProcessingEventType.error, "Could not close MQX connection", e)); }
			instances.remove(resource);
		}
	}

	public MQXConnection getConnection() throws MQXConnectionException
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
			final MQXConnection conn = driverClass.newInstance();
			conn.initConnection(config.getContext());
			ret = new MQXConnectionWrapper(conn);
		}
		catch (final Throwable e) { throw new MQXConnectionException(e); }
		return ret;
	}

	public class MQXConnectionWrapper implements MQXConnection
	{
		private final MQXConnection conn;
		private MQXConnectionWrapper(final MQXConnection connection) { conn = connection; }
		@Override public void initConnection(final Properties props) throws MQXIllegalArgumentException { throw new MQXIllegalArgumentException("Unsupported operation"); }
		@Override public MQXQueue openQueue(final Properties props) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException { throw new MQXIllegalArgumentException("Unsupported operation"); }
		@Override public void begin() throws MQXConnectionException { conn.begin();  }
		@Override public void commit() throws MQXConnectionException { conn.commit();  }
		@Override public void back() throws MQXConnectionException { conn.back(); }
		@Override public void close() throws MQXIllegalStateException, MQXConnectionException {  if(isTransaction()) back(); }
		@Override public boolean isValid() { return conn.isValid(); }
		@Override public boolean isTransaction() { return conn.isTransaction(); }
		private void forceClose() throws MQXIllegalStateException, MQXConnectionException { conn.close(); }
		@Override
		public MQXQueue openQueue(final String name) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException
		{
			final QueueConfig qCfg = config.get(name);
			if (qCfg == null) throw new MQXIllegalArgumentException("Queue name [" + name + "] not found.");
			return conn.openQueue(qCfg);
		}
		@Override public MQXMessage call(final String putQueue, final String getQueue, final MQXMessage msg) throws MQXIllegalStateException, MQXIllegalArgumentException, MQXConnectionException
		{
			if (msg == null || msg.getMessage() == null) throw new MQXIllegalArgumentException("Request message must not be null");
			final MQXQueue out = openQueue(putQueue);
			final MQXQueue in = openQueue(getQueue);
			final MQXMessage fMsg = out.send(msg);
			fMsg.setCorrelId(fMsg.getMsgId());
			fMsg.setMsgId(null);
			fMsg.setMessage(null);
			return in.receive(fMsg);
		}
	}
}

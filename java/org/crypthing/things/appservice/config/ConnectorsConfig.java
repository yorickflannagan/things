package org.crypthing.things.appservice.config;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.digester3.Digester;

public final class ConnectorsConfig extends HashMap<String, ConnectorConfig>
{
	public static void setConfig(final Digester digester, final String xmlPath, final String setMethod)
	{
		digester.addObjectCreate(xmlPath + "/mqxconnectors", ConnectorsConfig.class);
		digester.addFactoryCreate(xmlPath + "/mqxconnectors/mqxconnector", ConnectorConfigFactory.class);
		digester.addObjectCreate(xmlPath + "/mqxconnectors/mqxconnector/context", ConfigProperties.class);
		digester.addObjectCreate(xmlPath + "/mqxconnectors/mqxconnector/context/property", Property.class);
		digester.addSetProperties(xmlPath + "/mqxconnectors/mqxconnector/context/property");
		digester.addSetNext(xmlPath + "/mqxconnectors/mqxconnector/context/property", "add");
		digester.addSetNext(xmlPath + "/mqxconnectors/mqxconnector/context", "setContext");
		digester.addFactoryCreate(xmlPath + "/mqxconnectors/mqxconnector/queues/queue", QueueConfigFactory.class);
		digester.addObjectCreate(xmlPath + "/mqxconnectors/mqxconnector/queues/queue/property", Property.class);
		digester.addSetProperties(xmlPath + "/mqxconnectors/mqxconnector/queues/queue/property");
		digester.addSetNext(xmlPath + "/mqxconnectors/mqxconnector/queues/queue/property", "add");
		digester.addSetNext(xmlPath + "/mqxconnectors/mqxconnector/queues/queue", "add");
		digester.addSetNext(xmlPath + "/mqxconnectors/mqxconnector", "add");
		if (setMethod != null) digester.addSetNext(xmlPath + "/mqxconnectors", setMethod);
	}

	private static final long serialVersionUID = 6184597492041490665L;
	public ConnectorConfig add(final ConnectorConfig qfg) { return put(qfg.getName(), qfg); }
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		final Iterator<String> it = keySet().iterator();
		while (it.hasNext())
		{
			final String key = it.next();
			builder.append(key).append("= {").append(get(key).toString()).append("}");
		}
		return builder.toString();
	}
}

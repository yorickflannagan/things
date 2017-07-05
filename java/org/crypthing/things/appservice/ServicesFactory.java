package org.crypthing.things.appservice;

import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.crypthing.things.config.ConfigException;
import org.crypthing.things.appservice.config.RunnerConfig;
import org.crypthing.things.appservice.db.CursorFactory;
import org.crypthing.things.snmp.ProcessingEventListener;

public class ServicesFactory implements BindServices
{
	@Override
	public void bind(final RunnerConfig cfg, final ReleaseResourceEventDispatcher subscriber, final ProcessingEventListener trap) throws ConfigException, NamingException
	{
		System.getProperties().setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.crypthing.things.jndi.InitialContextFactory");
		final InitialContext context = new InitialContext();
		if (cfg.getDatasources() != null)
		{
			final Iterator<String> it = cfg.getDatasources().keySet().iterator();
			while (it.hasNext())
			{
				final String key = it.next();
				final DataSourceFactory ds = new DataSourceFactory(key);
				ds.init(cfg.getDatasources().get(key), subscriber, trap);
				context.bind("java:jdbc/" + key, ds);
			}
		}
		if (cfg.getConnectors() != null)
		{
			final Iterator<String> it = cfg.getConnectors().keySet().iterator();
			while (it.hasNext())
			{
				final String key = it.next();
				final MQXFactory connector = new MQXFactory(key);
				connector.init(cfg.getConnectors().get(key), subscriber, trap);
				context.bind("java:mqx/" + key, connector);
			}
		}
		if(cfg.getCursors() !=null)
		{
			final Iterator<String> it = cfg.getCursors().keySet().iterator();
			while (it.hasNext())
			{
				final String key = it.next();
				final CursorFactory cursorf = new CursorFactory(key);
				cursorf.init(cfg.getCursors().get(key), subscriber, trap);
				context.bind("java:cursor/" + key, cursorf);
			}
		}
		
	}
}

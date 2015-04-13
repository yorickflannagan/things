package org.crypthing.things.appservice;

import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.crypthing.things.appservice.config.ConfigException;
import org.crypthing.things.appservice.config.RunnerConfig;

public class ServicesFactory implements BindServices
{
	@Override
	public void bind(final RunnerConfig cfg) throws ConfigException, NamingException
	{
		System.getProperties().setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.crypthing.things.jndi.InitialContextFactory");
		final InitialContext context = new InitialContext();

		if (cfg.getSnmp() != null)
		{
			final SNMPFactory snmp = new SNMPFactory(cfg.getSnmp(), "defaultSNMP");
			snmp.validate();
			context.bind("java:snmp", snmp);
		}
		if (cfg.getDatasources() != null)
		{
			final Iterator<String> it = cfg.getDatasources().keySet().iterator();
			while (it.hasNext())
			{
				final String key = it.next();
				final DataSourceFactory ds = new DataSourceFactory(cfg.getDatasources().get(key), key);
				ds.validate();
				context.bind("java:jdbc/" + key, ds);
			}
		}
	}
}

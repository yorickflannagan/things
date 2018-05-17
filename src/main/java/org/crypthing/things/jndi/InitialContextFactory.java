package org.crypthing.things.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

public class InitialContextFactory implements javax.naming.spi.InitialContextFactory
{
	private static  MemoryContext ctx = new  MemoryContext();
	@Override
	public Context getInitialContext(Hashtable<?, ?> arg0) throws NamingException { return ctx; }
}

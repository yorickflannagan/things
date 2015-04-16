package org.crypthing.things.jndi;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class MemoryContext implements Context
{
	private Map<String, Object> env = new ConcurrentHashMap<String, Object>(); 
	public MemoryContext() {}
	@Override public Object lookupLink(Name arg0) throws NamingException { return null; }
	@Override public Object lookupLink(String arg0) throws NamingException { return null; }
	@Override public Object addToEnvironment(String arg0, Object arg1) throws NamingException { return null; }
	@Override public void close() throws NamingException {}
	@Override public Name composeName(Name arg0, Name arg1) throws NamingException { return null; }
	@Override public String composeName(String arg0, String arg1) throws NamingException { return null;}
	@Override public Context createSubcontext(Name arg0) throws NamingException { return null; }
	@Override public Context createSubcontext(String arg0) throws NamingException { return null; }
	@Override public void destroySubcontext(Name arg0) throws NamingException {}
	@Override public void destroySubcontext(String arg0) throws NamingException {}
	@Override public Hashtable<?, ?> getEnvironment() throws NamingException { return null;}
	@Override public String getNameInNamespace() throws NamingException { return null; }
	@Override public NameParser getNameParser(Name arg0) throws NamingException { return null; }
	@Override public NameParser getNameParser(String arg0) throws NamingException { return null; }

	@Override
	public void bind(Name name, Object obj) throws NamingException
	{
		bind(name.toString(), obj);
	}

	@Override
	public void bind(String name, Object obj) throws NamingException
	{
		if(env.containsKey(name)) throw new NameAlreadyBoundException(name);
		env.put(name, obj);
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name arg0) throws NamingException
	{
		return list("");
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String arg0) throws NamingException
	{
		return new NamingEnumeration<NameClassPair>()
		{
			private Iterator<String> it = env.keySet().iterator();
			@Override
			public NameClassPair nextElement()
			{
				final String key = it.next();
				return new NameClassPair(key, env.get(key).getClass().getName());
			}
			@Override public boolean hasMoreElements() { return it.hasNext(); }
			@Override public NameClassPair next() throws NamingException { return nextElement(); }
			@Override public boolean hasMore() throws NamingException { return hasMoreElements(); }
			@Override public void close() throws NamingException {}
		};
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name arg0) throws NamingException
	{
		return listBindings("");
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String arg0) throws NamingException
	{
		return new NamingEnumeration<Binding>()
		{
			private Iterator<String> it = env.keySet().iterator();
			@Override
			public Binding nextElement()
			{
				final String key = it.next();
				return new Binding(key, env.get(key));
			}
			@Override public boolean hasMoreElements() { return it.hasNext(); }
			@Override public Binding next() throws NamingException { return nextElement(); }
			@Override public boolean hasMore() throws NamingException { return hasMoreElements(); }
			@Override public void close() throws NamingException {}
		};
	}

	@Override
	public Object lookup(Name key) throws NamingException
	{
		return lookup(key.toString());
	}

	@Override
	public Object lookup(String name) throws NamingException
	{
		Object o = env.get(name);
		if(o==null) throw new NameNotFoundException(name);
		return o;
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException
	{
		rebind(name.toString(), obj);
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException
	{
		env.put(name, obj);
	}

	@Override
	public Object removeFromEnvironment(String name) throws NamingException
	{
		return null;
	}

	@Override
	public void rename(Name old, Name newone) throws NamingException
	{
		rename(old.toString(), newone.toString());
	}

	@Override
	public void rename(String old, String newone) throws NamingException
	{
		Object oldo = lookup(old);
		bind(newone, oldo);
		unbind(old);
	}

	@Override
	public void unbind(Name name) throws NamingException
	{
		unbind(name.toString());
	}

	@Override
	public void unbind(String name) throws NamingException
	{
		if (env.remove(name) == null) throw new NameNotFoundException(name);
	}
}

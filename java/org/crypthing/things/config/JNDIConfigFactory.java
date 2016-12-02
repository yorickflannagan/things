package org.crypthing.things.config;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

/**
 * ConfigFactory JNDI lookup factory. This implementation requires two environment variables:<br>
 * org.crypthing.config.xml: complete path to XML configuration file;<br>
 * org.crypthing.config.xsd: complete path to configuration schema file.
 * @author magut
 *
 */
public class JNDIConfigFactory implements ObjectFactory, ConfigFactory
{
	private static final String XML_ENTRY = "org.crypthing.config.xml";
	private static final String XSD_ENTRY = "org.crypthing.config.xsd";
	private String xml;
	private String xsd;
	private Config cfg;
	@Override
	public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment) throws Exception
	{
		if (environment != null && xml == null && xsd == null)
		{
			xml = (String) environment.get(XML_ENTRY);
			xsd = (String) environment.get(XSD_ENTRY);
		}
		if (xml == null || xsd == null) throw new NullPointerException();
		return this;
	}
	@Override
	public Config getConfig() throws ConfigException
	{
		if (cfg == null)
		{
			try { cfg = new Config((new URL(xml)).openStream(), (new URL(xsd)).openStream()); }
			catch (final IOException e) { throw new ConfigException(e); }
		}
		return cfg;
	}
}

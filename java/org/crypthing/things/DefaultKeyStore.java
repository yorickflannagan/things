package org.crypthing.things;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class DefaultKeyStore
{
	private static final String KS_TYPE_ENTRY = "javax.net.ssl.keyStoreType";
	private static final String KS_FILE_ENTRY = "javax.net.ssl.keyStore";
	private static final String KS_PWD_ENTRY = "javax.net.ssl.keyStorePassword";
	private static KeyStore _default = null;
	private static char[] _pwd = null;
	static
	{
		final String kstype = System.getProperty(KS_TYPE_ENTRY, "JKS");
		final String ksfile = System.getProperty(KS_FILE_ENTRY);
		final String kspwd = System.getProperty(KS_PWD_ENTRY);
		if (ksfile != null && kspwd != null)
		{
			_pwd = kspwd.toCharArray();
			try
			{
				_default = KeyStore.getInstance(kstype);
				final InputStream stream = new FileInputStream(ksfile);
				try { _default.load(stream, _pwd); }
				finally { stream.close(); }
			}
			catch (final Throwable e) { _default = null; _pwd = null; }
		}
	}
	public static Key getPrivateKey()
	{
		if (_default == null || _pwd == null) return null;
		try
		{
			final Enumeration<String> aliases = _default.aliases();
			while (aliases.hasMoreElements())
			{
				final String alias = aliases.nextElement();
				if (_default.isKeyEntry(alias)) return _default.getKey(alias, _pwd);
			}
		}
		catch (final Throwable e) {}
		return null;
	}
	public static Key getPrivateKey(final String alias)
	{
		if (_default == null || _pwd == null) return null;
		try { return _default.getKey(alias, _pwd); }
		catch (final Throwable e) {}
		return null;
	}
	public static List<String> keyAliases()
	{
		final List<String> ret = new ArrayList<String>();
		if (_default != null && _pwd != null)
		{
			try
			{
				final Enumeration<String> aliases = _default.aliases();
				while (aliases.hasMoreElements())
				{
					final String alias = aliases.nextElement();
					if (_default.isKeyEntry(alias)) ret.add(alias);
				}
			}
			catch (final Throwable e) {}
		}
		return ret;
	}
}

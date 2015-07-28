package org.crypthing.things.appservice.config;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigReader extends Reader
{
	private final String value;
	private int offset;
	public ConfigReader(final File file) throws IOException, ConfigException
	{
		if (file == null) throw new NullPointerException("Argument must not be null");
		final FileReader loader = new FileReader(file);
		final char[] buffer = new char[(int) file.length()];
		int buffLen;
		try
		{
			buffLen = loader.read(buffer);
			value = expand(buffer, buffLen);
		}
		finally { loader.close(); }
	}
	public ConfigReader(final Reader reader) throws IOException, ConfigException
	{
		final CharArrayWriter writer = new CharArrayWriter(16384);
		final char[] buffer = new char[1024];
		int read;
		while ((read = reader.read(buffer)) != -1) writer.write(buffer, 0, read);
		value = expand(writer.toCharArray(), writer.size());
	}
	public ConfigReader(final InputStream input) throws IOException, ConfigException
	{
		this(new InputStreamReader(input));
	}
	public static String expand(final char[] input, final int len) throws ConfigException
	{
		final CharBuffer wraper = CharBuffer.wrap(input, 0, len);
		final Pattern pattern = Pattern.compile("\\$\\{(ENV|PROP)\\.[^\\}]*\\}");
		int pos = 0;
		final StringBuilder sb = new StringBuilder(len * 2);  
		final Matcher m = pattern.matcher(wraper);
		while (m.find())
		{
			sb.append(input, pos, m.start() - pos);
			pos = m.end();
			sb.append(replace(input, m.start(), m.end()));
		}
		sb.append(input, pos, len - pos);
		return sb.toString();
	}
	private static String replace(char[] buffer, int start, int end) throws ConfigException
	{
		String key, value;
		if (buffer[start + 2] == 'E')
		{
			key = new String(buffer, start + 6, end - start - 7);
			value = System.getenv(key);
		}
		else
		{
			key = new String(buffer, start + 7, end - start - 8);
			value = System.getProperty(key);
		}
		if (value == null) throw new ConfigException("Environment variable not found: " + key);
		return value;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		if (cbuf == null) throw new NullPointerException("Argument must not be null");
		if (offset >= value.length()) return -1;
		int read;
		if (offset + len > value.length()) read = value.length() - offset;
		else read = len;
		value.getChars(offset, offset + read, cbuf, off);
		offset += read;
		return read;
	}
	@Override public void close() throws IOException {}
}

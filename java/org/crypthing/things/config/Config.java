package org.crypthing.things.config;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Implements an application configuration facility based on XML documents. The configuration document is
 * validated against an XML schema. The access of configuration leaf nodes is based on XML XPath.
 * @author magut
 *
 */
public class Config implements Serializable
{
	private static final long serialVersionUID = -326226312678955321L;
	private static final String ENV_NOT_FOUND = "Environment variable not found: ";
	private static final String INVALID_ARG = "Arguments must not be null and must have valid values";
	private static final String CONFIG_NOT_LOADED = "Could not load configuration file";
	private class ConfigReader extends Reader
	{
		private final String value;
		private int offset;
		ConfigReader(final Reader reader) throws IOException, ConfigException
		{
			final CharArrayWriter writer = new CharArrayWriter(16384);
			final char[] buffer = new char[1024];
			int read;
			while ((read = reader.read(buffer)) != -1) writer.write(buffer, 0, read);
			value = expand(writer.toCharArray(), writer.size());
		}
		ConfigReader(final InputStream input) throws IOException, ConfigException { this(new InputStreamReader(input)); }
		private String expand(final char[] input, final int len) throws IOException
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
		private String replace(char[] buffer, int start, int end) throws IOException
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
			if (value == null) throw new IOException(ENV_NOT_FOUND + key);
			return value;
		}
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException
		{
			if (cbuf == null) throw new NullPointerException(INVALID_ARG);
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

	private Document doc;
	private XPathFactory xFactory;
	/**
	 * 
	 * Creates a new instance based on specified resources: 
	 * @param config: XML configuration file.
	 * @param schema: XML schema file
	 * @throws ConfigException if configuration cannot be loaded.
	 */
	public Config(final File config, final File schema) throws ConfigException
	{
		try { parse(new FileInputStream(config), new FileInputStream(schema)); }
		catch (final FileNotFoundException e) { throw new ConfigException(CONFIG_NOT_LOADED, e);}
	}
	/**
	 * Creates a new instance based on specified resources: 
	 * @param config: XML configuration.
	 * @param schema: XML schema.
	 * @throws ConfigException if configuration cannot be loaded.
	 */
	public Config(final InputStream config, final InputStream schema) throws ConfigException { parse(config, schema); }
	protected void parse(final InputStream document, final InputStream schema) throws ConfigException
	{
		try
		{
			if (document == null || schema == null) throw new NullPointerException(INVALID_ARG);
			final SchemaFactory fac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setSchema(fac.newSchema(new StreamSource(schema)));
			final DocumentBuilder builder = dbf.newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler()
			{
				@Override public void warning(final SAXParseException exception) throws SAXException {}
				@Override public void error(final SAXParseException exception) throws SAXException { throw new SAXException(exception); }
				@Override public void fatalError(final SAXParseException exception) throws SAXException { throw new SAXException(exception); }
			});
			doc = builder.parse(new InputSource(new ConfigReader(document)));
			xFactory = XPathFactory.newInstance();
		}
		catch (final Throwable e) { throw new ConfigException(CONFIG_NOT_LOADED, e); }
	}

	/**
	 * Gets specified entry
	 * @param path: XPath expression.
	 * @return the entry value or null.
	 */
	public String getValue(final String path)
	{
		if (path == null) throw new NullPointerException(INVALID_ARG);
		String ret;
		try
		{
			final XPath xPath = xFactory.newXPath();
			ret = xPath.compile(path).evaluate(doc);
			if (ret != null && ret.length() == 0) ret = null;
		}
		catch (final Throwable swallowed) { ret = null; } 
		return ret;
	}

	/**
	 * Gets entry as specified type. 
	 * @param path: XPath expression.
	 * @param conv: result conversion utility.
	 * @return: converted entry value.
	 */
	public <T> T getValue(final String path, final Converter<T> conv)
	{
		if (conv == null) throw new NullPointerException(INVALID_ARG);
		return conv.convert(getValue(path));
	}
}

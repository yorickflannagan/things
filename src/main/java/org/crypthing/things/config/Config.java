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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
	private static final String INVALID_ARG_CFG = "Config must not be null and must have valid value";
	private static final String INVALID_ARG_SCH = "Schema must not be null and must have valid value";
	private static final String CONFIG_NOT_LOADED = "Could not load configuration file";
	private class ConfigReader extends Reader
	{
		private final String value;
		private int offset;
		private final Map<String, String> env;
		ConfigReader(final Reader reader, Map<String, String> env) throws IOException, ConfigException
		{
			final CharArrayWriter writer = new CharArrayWriter(16384);
			final char[] buffer = new char[1024];
			int read;
			while ((read = reader.read(buffer)) != -1) writer.write(buffer, 0, read);
			value = expand(writer.toCharArray(), writer.size());
			this.env = env;
		}
		ConfigReader(final InputStream input, Map<String, String> env) throws IOException, ConfigException { this(new InputStreamReader(input, StandardCharsets.UTF_8), env); }
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
				value = env !=null ? env.get(key) : System.getenv(key);
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

	/**
	 * Creates a new instance based on specified resources: 
	 * @param config: XML configuration.
	 * @param schema: XML schema.
	 * @param env: Target environment, see: System.getEnv()
	 * @throws ConfigException if configuration cannot be loaded.
	 */
	public Config(final InputStream config, final InputStream schema, Map<String, String> env) throws ConfigException { parse(config, schema,env); }


	protected void parse(final InputStream document, final InputStream schema) throws ConfigException
	{
		parse(document, schema, null);
	}


	protected void parse(final InputStream document, final InputStream schema, Map<String, String> env) throws ConfigException
	{
		try
		{
			if (schema == null) throw new NullPointerException(INVALID_ARG_SCH);
			if (document == null) throw new NullPointerException(INVALID_ARG_CFG);
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
			doc = builder.parse(new InputSource(new ConfigReader(document, env)));
			xFactory = XPathFactory.newInstance();
		}
		catch (final Throwable e) { throw new ConfigException(CONFIG_NOT_LOADED, e); }
	}
	/**
	 * Gets specified entry
	 * @param path: XPath expression.
	 * @return the entry value or null if path is an invalid XPath expression.
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
	 * Gets entry as specified type from DOM node. 
	 * @param path: XPath expression.
	 * @param conv: result conversion utility.
	 * @return: converted entry value or null if path is an invalid XPath expression.
	 */
	public <T> T getValue(final String path, final Converter<T> conv)
	{
		if (conv == null) throw new NullPointerException(INVALID_ARG);
		return conv.convert(getValue(path));
	}
	/**
	 * Gets entry as specified type. 
	 * @param path: XPath expression.
	 * @param node: search start node.
	 * @param conv: result conversion utility.
	 * @return entry value or null if path is an invalid XPath expression.
	 */
	public <T> T getValue(final String path, final Node node, final Converter<T> conv)
	{
		if (conv == null) throw new NullPointerException(INVALID_ARG);
		return conv.convert(getValue(path, node));
	}
	/**
	 * Gets entry value from DOM node.
	 * @param path: XPath expression.
	 * @param node: search start node.
	 * @return entry value or null if path is an invalid XPath expression.
	 */
	public String getValue(final String path, final Node node)
	{
		final XPath xPath = xFactory.newXPath();
		String ret;
		try
		{
			ret = xPath.compile(path).evaluate(node);
			if (ret != null && ret.length() == 0) ret = null;
		}
		catch (final XPathExpressionException e) { ret = null; }
		return ret;
	}
	/**
	 * Gets entry value as a DOM node.
	 * @param path: XPath expression.
	 * @return entry value or null if path is an invalid XPath expression.
	 */
	public Node getNodeValue(final String path) { return getNodeValue(path, doc); }
	public Node getNodeValue(final String path, final Node node)
	{
		final XPath xPath = xFactory.newXPath();
		try { return (Node) xPath.compile(path).evaluate(node, XPathConstants.NODE); }
		catch (final XPathExpressionException e) { return null; }
	}
	private <T> Collection<T> getObjectValue(final String path, final Object domNode, final Converter<T> conv)
	{
		if (path == null || conv == null) throw new NullPointerException(INVALID_ARG);
		final ArrayList<T> ret = new ArrayList<T>();
		final XPath xPath = xFactory.newXPath();
		try
		{
			final NodeList nodes = (NodeList) xPath.compile(path).evaluate(domNode, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++)
			{
				final Node item = nodes.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE)
				{
					final T value = conv.convert(item);
					if (value != null) ret.add(value);
				}
			}
			return ret;
		}
		catch (final ClassCastException | XPathExpressionException swallowed) { return null; }
	}
	/**
	 * Gets entry as a collection of specified type.
	 * @param path: XPath expression.
	 * @param node: DOM node where search should start.
	 * @param conv: result conversion utility. convert() method argument must be of type org.w3c.dom.Node
	 * @return a (possibly empty) collection or null if path is an invalid XPath expression.
	 */
	public <T> Collection<T> getValueCollection(final String path, final Node node, final Converter<T> conv) { return getObjectValue(path, node, conv); }
	/**
	 * Gets entry as a collection of specified type.
	 * @param path: XPath expression.
	 * @param conv: result conversion utility. convert() method argument must be of type org.w3c.dom.Node
	 * @return a (possibly empty) collection or null if path is an invalid XPath expression.
	 */
	public <T> Collection<T> getValueCollection(final String path, final Converter<T> conv) { return getObjectValue(path, doc, conv); }
}

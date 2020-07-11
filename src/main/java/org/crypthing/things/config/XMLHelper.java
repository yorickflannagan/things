package org.crypthing.things.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

public class XMLHelper {

	private static final String INVALID_ARG = "Arguments must not be null and must have valid values";


	protected Document doc;
	protected XPathFactory xFactory = XPathFactory.newInstance();

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder;


    XMLHelper() 
    {
        try {
            builder = dbf.newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Document Builder.", e);
        }
    }

    public void  parse(byte[] data) throws SAXException, IOException
    {
        builder.setErrorHandler(new ErrorHandler()
        {
            @Override public void warning(final SAXParseException exception) throws SAXException {}
            @Override public void error(final SAXParseException exception) throws SAXException { throw new SAXException(exception); }
            @Override public void fatalError(final SAXParseException exception) throws SAXException { throw new SAXException(exception); }
        });
        doc = builder.parse(new InputSource(new ByteArrayInputStream(data)));
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
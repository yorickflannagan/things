package org.crypthing.things.appservice.config;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ConfigErrorHandler implements ErrorHandler
{
	@Override public void warning(SAXParseException exception) throws SAXException {}
	@Override public void error(SAXParseException exception) throws SAXException { throw new SAXException(exception); }
	@Override public void fatalError(SAXParseException exception) throws SAXException { throw new SAXException(exception); }
}

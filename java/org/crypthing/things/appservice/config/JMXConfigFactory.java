package org.crypthing.things.appservice.config;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreationFactory;
import org.xml.sax.Attributes;

public class JMXConfigFactory implements ObjectCreationFactory<JMXConfig>
{
	private Digester me;
	@Override
	public JMXConfig createObject(Attributes attr) throws Exception { return new JMXConfig(attr.getValue("host"), attr.getValue("port")); }
	@Override public Digester getDigester() { return me; }
	@Override public void setDigester(Digester dgst) { me = dgst; }
}

package org.crypthing.things.appservice.config;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreationFactory;
import org.xml.sax.Attributes;

public class JNDIConfigFactory implements ObjectCreationFactory<JNDIConfig>
{
	Digester me;
	@Override
	public JNDIConfig createObject(Attributes attr) throws Exception { return new JNDIConfig(attr.getValue("implementation")); }
	@Override
	public Digester getDigester() { return me; }
	@Override
	public void setDigester(Digester dgst) { me = dgst; }
}

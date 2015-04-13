package org.crypthing.things.appservice.config;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreationFactory;
import org.xml.sax.Attributes;

public final class JDBCConfigFactory implements ObjectCreationFactory<JDBCConfig>
{
	private Digester me;
	@Override
	public JDBCConfig createObject(Attributes attr) throws Exception { return new JDBCConfig(attr.getValue("name"), attr.getValue("driver"), attr.getValue("url")); }
	@Override
	public Digester getDigester() { return me; }
	@Override
	public void setDigester(Digester dgst) { me = dgst; }
}

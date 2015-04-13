package org.crypthing.things.appservice.config;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreationFactory;
import org.xml.sax.Attributes;

public final class ConnectorConfigFactory implements ObjectCreationFactory<ConnectorConfig>
{
	Digester me;
	@Override
	public ConnectorConfig createObject(Attributes attr) throws Exception { return new ConnectorConfig(attr.getValue("name"), attr.getValue("driver")); }
	@Override
	public Digester getDigester() { return me; }
	@Override
	public void setDigester(Digester dgst) { me = dgst; }
}

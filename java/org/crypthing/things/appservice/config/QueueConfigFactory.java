package org.crypthing.things.appservice.config;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreationFactory;
import org.xml.sax.Attributes;

public final class QueueConfigFactory implements ObjectCreationFactory<QueueConfig>
{
	Digester me;
	@Override
	public QueueConfig createObject(Attributes attr) throws Exception { return new QueueConfig(attr.getValue("name")); }
	@Override
	public Digester getDigester() { return me; }
	@Override
	public void setDigester(Digester dgst) { me = dgst; }
}

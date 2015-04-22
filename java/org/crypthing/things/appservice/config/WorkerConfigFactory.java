package org.crypthing.things.appservice.config;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreationFactory;
import org.xml.sax.Attributes;

public class WorkerConfigFactory implements ObjectCreationFactory<WorkerConfig>
{
	private Digester me;
	@Override public WorkerConfig createObject(Attributes attr) throws Exception { return new WorkerConfig(attr.getValue("implementation")); }
	@Override public Digester getDigester() { return me; }
	@Override public void setDigester(Digester digester) { me = digester; }
}

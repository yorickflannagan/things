package org.crypthing.things.batch;

import java.util.Properties;

public interface Resource
{
	void initialize(Properties cfg) throws InitializeException;
	void destroy();
}

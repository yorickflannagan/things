package org.crypthing.things.config;


/**
 * Factory of configurations.
 * @author magut
 *
 */
public interface ConfigFactory
{
	/**
	 * Gets a configuration instance.
	 * @return product configuration.
	 * @throws ConfigException if an error occurs.
	 */
	Config getConfig() throws ConfigException;
}

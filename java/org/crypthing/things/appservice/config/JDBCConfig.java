package org.crypthing.things.appservice.config;

import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.w3c.dom.Node;

public final class JDBCConfig extends ConfigProperties
{
	private static final long serialVersionUID = 158330280175117539L;
	private final String name;
	private final String driver;
	private final String url;
	private final String validationQuery;
	public JDBCConfig(final Config xml, final Node root) throws ConfigException
	{
		super(xml, root);
		name = xml.getValue("./@name", root);
		driver = xml.getValue("./@driver", root);
		url = xml.getValue("./@url", root);
		validationQuery = xml.getValue("./@validationQuery", root);
	}
	public String getName() { return name; }
	public String getDriver() { return driver; }
	public String getUrl() { return url; }
	public String getValidationQuery() { return validationQuery; }
	@Override public String toString()
	{
		final StringBuilder builder = new StringBuilder(256);
		if (name != null) builder.append("name=").append(name);
		if (driver != null) builder.append(", driver=").append(driver);
		if (url != null) builder.append(", url=").append(url);
		builder.append(", {").append(super.toString()).append("}").toString();
		return builder.toString();
	}
}

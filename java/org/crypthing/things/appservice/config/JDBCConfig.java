package org.crypthing.things.appservice.config;

import java.util.Iterator;

import org.crypthing.things.config.Config;
import org.w3c.dom.Node;

public final class JDBCConfig extends ConfigProperties
{
	private static final long serialVersionUID = 158330280175117539L;
	private String name;
	private String driver;
	private String url;
	private String validationQuery;

	public JDBCConfig(final String name, final String driver, final String url, final String query) { this.name = name; this.driver = driver; this.url = url; validationQuery = query; }
	public JDBCConfig(final Config cfg, final Node node)
	{
		name = cfg.getValue("./@name", node);
		driver = cfg.getValue("./@driver", node);
		url = cfg.getValue("./@url", node);
		validationQuery = cfg.getValue("./@validationQuery", node);
		final Iterator<Property> props = cfg.getValueCollection("./property", node, new PropertyFactory(cfg)).iterator();
		while (props.hasNext()) add(props.next());
	}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDriver() { return driver; }
	public void setDriver(String driver) { this.driver = driver; }
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url;}
	public String getValidationQuery() { return validationQuery; }
	public void setValidationQuery(String validationQuery) { this.validationQuery = validationQuery; }
	@Override public String toString()
	{
		final StringBuilder builder = new StringBuilder(256);
		if (name != null) builder.append("name=").append(name);
		if (driver != null) builder.append(", driver=").append(driver);
		if (url != null) builder.append(", url=").append(url);
		if (validationQuery != null) builder.append(", validationQuery=").append(validationQuery);
		builder.append(", {").append(super.toString()).append("}").toString();
		return builder.toString();
	}
}

package org.crypthing.things.appservice.config;

public final class JDBCConfig extends ConfigProperties
{
	private static final long serialVersionUID = 158330280175117539L;
	private String name;
	private String driver;
	private String url;
	public JDBCConfig(final String name, final String driver, final String url) { this.name = name; this.driver = driver; this.url = url;}
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDriver() { return driver; }
	public void setDriver(String driver) { this.driver = driver; }
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url;}
	@Override public String toString() { return (new StringBuilder()).append("name=").append(name).append(", driver=").append(driver).append(", url=").append(url).append(", {").append(super.toString()).append("}").toString(); }
}

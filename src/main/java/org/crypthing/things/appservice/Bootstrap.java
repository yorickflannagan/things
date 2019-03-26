package org.crypthing.things.appservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.management.ObjectName;

import org.crypthing.things.appservice.config.JMXConfig;
import org.crypthing.things.appservice.config.JVMConfig;
import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;


public final class Bootstrap implements AgentMBean
{
    public static final String MBEAN_PATTERN = "org.crypthing.things.appservice:type=Agent,name=";
	private static final String CONFIG_SCHEMA_PATH = "/org/crypthing/things/appservice/config.xsd";
    private static ObjectName mbName;
	public static void main(String[] args) throws Exception
	{
		final Bootstrap instance = new Bootstrap();
		ManagementFactory.getPlatformMBeanServer().registerMBean
		(
			instance,
			mbName = new ObjectName((new StringBuilder(256)).append(MBEAN_PATTERN).append(ManagementFactory.getRuntimeMXBean().getName()).toString())
		);
		for (int i = 0; i < args.length; i++) instance.launch(args[i]);
		instance.run();
	}
	public static InputStream getSchema() throws ConfigException 
	{
		return Bootstrap.class.getClass().getResourceAsStream(CONFIG_SCHEMA_PATH);
	}
	public static JVMConfig getJVMConfig(final InputStream cfgFile, final InputStream cfgSchema) throws ConfigException
	{
		final Config config = new Config(cfgFile, cfgSchema);
		return new JVMConfig(config, config.getNodeValue("/config/jvm"));
	}
	public static String[] getEnv()
	{
		final int size = System.getenv().size();
		final ArrayList<String> env = new ArrayList<String>(size);
		final Iterator<String> keys = System.getenv().keySet().iterator();
		while (keys.hasNext())
		{
			final String key = keys.next();
			env.add(key + "=" + System.getenv(key));
		}
		final String[] ret = new String[size];
		return env.toArray(ret);
	}


	private boolean isRunning = true;
	public void run()
	{
        synchronized (this)
        {
            while (isRunning)
            {
                try { wait(); }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
	}
	@Override
	public void shutdown()
	{
        synchronized (this)
        {
            if (mbName != null)
            {
                try { ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbName); }
                catch (Exception e) { e.printStackTrace(); }
            }
            isRunning = false;
            notifyAll();
        }
	}
	@Override
	public int launch(final String cfgFile)
	{
		int ret;
		try
		{
			final FileInputStream config = new FileInputStream(cfgFile);
			try
			{
				final JVMConfig cfg = getJVMConfig(config, getSchema());
				final List<String> cmds = getCommands(cfg, cfgFile);
				final ProcessBuilder builder = new ProcessBuilder(cmds);
				if (cfg.getRedirectTo() != null)
				{
					builder.redirectErrorStream(true);
					builder.redirectOutput(Redirect.appendTo(new File(cfg.getRedirectTo())));
				}
				builder.start();
				ret = 0;
				System.out.println(cfgFile + " configuration launched!");
			}
			finally { config.close(); }
		}
		catch (final Exception e)
		{
			ret = 1;
			System.out.println("Launch failed!");
			System.err.println("-------------------------------\n");
			e.printStackTrace();
			System.err.println("\n-------------------------------\n\n");
		}
		return ret;
	}
	/*
	 * see http://docs.oracle.com/cd/E26576_01/doc.312/e24936/tuning-java.htm#GSPTG00069
	 */
	private List<String> getCommands(final JVMConfig config, final String arg)
	{
		final ArrayList<String> ret = new ArrayList<String>(128);
		final String filesep = System.getProperty("file.separator", "/");
		ret.add((new StringBuilder(256)).append(System.getProperty("java.home")).append(filesep).append("bin").append(filesep).append(System.getProperty("os.name", "").toLowerCase().indexOf("win") >= 0 ? "java.exe" : "java").toString());
		ret.add("-server");
		ret.add("-Xbatch");
		if (config.getMinMemory() > 0) ret.add((new StringBuilder(256)).append("-Xms").append(config.getMinMemory()).append("m").toString());
		if (config.getMaxMemory() > 0) ret.add((new StringBuilder(256)).append("-Xmx").append(config.getMaxMemory()).append("m").toString());
		if (config.getVmflags() != null) ret.add(config.getVmflags());

		final JMXConfig jmx = config.getJmx();
		ret.add((new StringBuilder(256)).append("-Djava.rmi.server.hostname=").append(jmx.getHost()).toString());
		ret.add((new StringBuilder(256)).append("-Dcom.sun.management.jmxremote.port=").append(jmx.getPort()).toString());
		if(config.getName() != null) ret.add((new StringBuilder(256)).append("-Dthing.server.name=").append(config.getName()).toString());
		if (jmx.getProperty("com.sun.management.jmxremote.ssl") == null) ret.add("-Dcom.sun.management.jmxremote.ssl=false");
		if (jmx.getProperty("com.sun.management.jmxremote.authenticate") == null) ret.add("-Dcom.sun.management.jmxremote.authenticate=false");
		addProperties(jmx.stringPropertyNames().iterator(), jmx, ret);
		if (config.getProperties() != null) addProperties(config.getProperties().stringPropertyNames().iterator(), config.getProperties(), ret);

		ret.add("-cp");
		ret.add(config.getClasspath().getClasspath());
		ret.add(Runner.class.getName());
		ret.add(arg);
		return ret;
	}
	private void addProperties(final Iterator<String> keys, final Properties source, final List<String> cmd)
	{
		while (keys.hasNext())
		{
			final String key = keys.next();
			cmd.add((new StringBuilder(256)).append("-D").append(key).append("=").append(source.getProperty(key)).toString());
		}
	}
}

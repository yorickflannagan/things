package org.crypthing.things.appservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.crypthing.things.snmp.EncodableString;
import org.crypthing.things.snmp.LifecycleEvent;
import org.crypthing.things.snmp.LifecycleEventDispatcher;
import org.crypthing.things.snmp.LifecycleEventListener;
import org.crypthing.things.snmp.SNMPBridge;
import org.crypthing.things.snmp.LifecycleEvent.LifecycleEventType;

public final class Bootstrap implements BootstrapMBean
{
	private static final class Trapper implements LifecycleEventListener
	{
		private final SNMPBridge trapper;
		public Trapper(final String udpAddress) throws IOException { trapper = new SNMPBridge(udpAddress, "0.1.4320"); }
		@Override public void start(final LifecycleEvent e)	{ trapper.notify(e); }
		@Override public void work(final LifecycleEvent e) 	{ trapper.notify(e); }
		@Override public void stop(final LifecycleEvent e)	{ trapper.notify(e); }
		@Override public void heart(LifecycleEvent e)		{ trapper.notify(e); }
	}
    public static final String MBEAN_PATTERN = "org.crypthing.things.appservice:type=Agent,name=";
	private static final String CONFIG_SCHEMA_PATH = "/org/crypthing/things/appservice/config.xsd";
    private static ObjectName mbName;
	public static void main(String[] args) throws Exception
	{
		if (args.length < 1 || System.getProperty("java.rmi.server.hostname") == null || System.getProperty("com.sun.management.jmxremote.port") == null) usage();
		final LifecycleEventDispatcher dispatcher = new LifecycleEventDispatcher();
		dispatcher.addListener(new Trapper(args[0]));
		final Bootstrap instance = new Bootstrap(dispatcher);
		ManagementFactory.getPlatformMBeanServer().registerMBean
		(
			instance,
			mbName = new ObjectName((new StringBuilder(256)).append(MBEAN_PATTERN).append(ManagementFactory.getRuntimeMXBean().getName()).toString())
		);
		for (int i = 1; i < args.length; i++) instance.launch(args[i]);
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
	private static void usage()
	{
		System.err.println("Usage: org.crypthing.things.appservice.Bootstrap [udpAddress] [cfgFileList...], where");
		System.err.println("\tudpAddress: IP/port for SNMP traps");
		System.err.println("\tcfgFileList...: an optional list of configuration files to launch");
		System.err.println("\tRequired system properties:");
		System.err.println("\tjava.rmi.server.hostname: JMX host");
		System.err.println("\tcom.sun.management.jmxremote.port: JMX port");
		System.exit(1);
	}


	private boolean isRunning = true;
	private final LifecycleEventDispatcher evt;
	private final LifecycleEvent heart;
	public Bootstrap(final LifecycleEventDispatcher evt)
	{
		this.evt = evt;
		heart = new LifecycleEvent(LifecycleEventType.heart, new EncodableString("java.rmi.server.hostname=" + System.getProperty("java.rmi.server.hostname") + "\ncom.sun.management.jmxremote.port=" + System.getProperty("com.sun.management.jmxremote.port")));
	}
	public void run()
	{
		evt.fire(new LifecycleEvent(LifecycleEventType.start, new EncodableString("Bootstrap has begun")));
		while (isRunning)
		{
			try { Thread.sleep(10000); evt.fire(heart); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		evt.fire(new LifecycleEvent(LifecycleEventType.stop, new EncodableString("Bootstrap has endend")));
	}
	@Override
	public void shutdown()
	{
		if (mbName != null)
		{
			try { ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbName); }
			catch (Exception e) { e.printStackTrace(); }
		}
		isRunning = false;
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
				evt.fire(new LifecycleEvent(LifecycleEventType.work, new EncodableString(cfgFile + " configuration launched!")));
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

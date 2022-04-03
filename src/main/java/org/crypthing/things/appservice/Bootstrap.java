package org.crypthing.things.appservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import org.crypthing.things.appservice.JMXConnection.ConnectionHolder;
import org.crypthing.things.appservice.config.JMXConfig;
import org.crypthing.things.appservice.config.JVMConfig;
import org.crypthing.things.appservice.diagnostic.Network;
import org.crypthing.things.config.Config;
import org.crypthing.things.config.ConfigException;
import org.crypthing.things.snmp.EncodableString;
import org.crypthing.things.snmp.LifecycleEvent;
import org.crypthing.things.snmp.LifecycleEventDispatcher;
import org.crypthing.things.snmp.LifecycleEventListener;
import org.crypthing.things.snmp.SNMPBridge;
import org.crypthing.things.snmp.SignalBean;
import org.crypthing.things.snmp.LifecycleEvent.LifecycleEventType;

public final class Bootstrap implements BootstrapMBean
{

	public static String PARENT_JMX_HOST = "PARENT_JMX_HOST";
	public static String PARENT_JMX_PORT = "PARENT_JMX_PORT";
	public static int TIMEOUT = 5;

	private static final class Trapper implements LifecycleEventListener
	{
		private final SNMPBridge trapper;

		public Trapper(final String udpAddress, final String oidRoot) throws IOException
		{
			trapper = new SNMPBridge(udpAddress, oidRoot);
		}

		@Override
		public void start(final LifecycleEvent e)
		{
			trapper.notify(e);
		}

		@Override
		public void work(final LifecycleEvent e)
		{
			trapper.notify(e);
		}

		@Override
		public void stop(final LifecycleEvent e)
		{
			trapper.notify(e);
		}

		@Override
		public void heart(LifecycleEvent e)
		{
			trapper.notify(e);
		}
	}

	public static final String MBEAN_PATTERN = "org.crypthing.things.appservice:type=Agent,name=";
	private static final String CONFIG_SCHEMA_PATH = "/org/crypthing/things/appservice/config.xsd";
	private static final String OVERRIDE = "things.override.";
	private static final String OVERRIDE_ENV = "override";
	private static final String SCHEMA_LOCATION = "things.schema";
	private static ObjectName mbName;
	private static final Map<String, Process> processes = new HashMap<>();
	private static final Map<String, JVMConfig> configs = new HashMap<>();
	private static final String jmxkey = System.getProperty("java.rmi.server.hostname")
			+ System.getProperty("com.sun.management.jmxremote.port");
	private static final Object ENV_OVERRIDE_KEY = "org_crypthing_things_appservice_env";
	private static final boolean dumpEnv = Boolean.getBoolean("org.crypthing.things.dumpEnvironment");

	public static void main(String[] args) throws Exception
	{
		if
		(
			args.length < 2 ||
			System.getProperty("java.rmi.server.hostname") == null ||
			System.getProperty("com.sun.management.jmxremote.port") == null
		)	usage();
		final LifecycleEventDispatcher dispatcher = new LifecycleEventDispatcher();
		dispatcher.addListener(new Trapper(args[0], args[1]));
		final Bootstrap instance = new Bootstrap(dispatcher);
		ManagementFactory.getPlatformMBeanServer().registerMBean
		(
			instance,
			mbName = new ObjectName
			(
				(new StringBuilder(256))
					.append(MBEAN_PATTERN)
					.append(ManagementFactory.getRuntimeMXBean().getName())
					.toString()
			)
		);
		System.out.println("Agent Mbean registered [" + mbName + "]");
		for (int i = 2; i < args.length; i++) instance.launch(args[i]);
		instance.run();
	}

	public static InputStream getSchema() throws ConfigException
	{
		InputStream ret = null;
		try
		{
			final String loc = System.getProperty(SCHEMA_LOCATION); 
			if (loc != null) ret = new FileInputStream(loc);
			if (ret == null) throw new RuntimeException("Could no load schema from:[" + loc + "] - will try locally.");
		}
		catch (final Exception e)
		{
			System.err.print(e.getMessage());
			System.err.println(" Will try again...");
			ret = Bootstrap.class.getClassLoader().getResourceAsStream(CONFIG_SCHEMA_PATH);
			if (ret == null) ret = Bootstrap.class.getResourceAsStream(CONFIG_SCHEMA_PATH);
			if (ret == null) throw new ConfigException("Things appservice schema not loaded");
		}
		return ret;
	}

	public static JVMConfig getJVMConfig(final String cfgFile) throws ConfigException, IOException
	{
		return getJVMConfig(new FileInputStream(cfgFile), Bootstrap.getSchema());
	}
	public static JVMConfig getJVMConfig(final InputStream cfgFile, final InputStream cfgSchema) throws ConfigException
	{
		return getJVMConfig(cfgFile, cfgSchema, System.getenv());
	}

	public static JVMConfig getJVMConfig(final InputStream cfgFile, final InputStream cfgSchema,
			Map<String, String> env) throws ConfigException
	{
		final Config config = new Config(cfgFile, cfgSchema, env);
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

	public static Map<String, String> parseEnv(String[] env)
	{
		Map<String, String> ret = new HashMap<>();
		if (env == null) return ret;
		for (int i = 0; i < env.length; i++)
		{
			final int p = env[i] != null ? env[i].indexOf("=") : -1;
			if (p > -1)
			{
				ret.put(env[i].substring(0, p), env[i].substring(p + 1));
			}
		}
		return ret;
	}

	private static void usage()
	{
		System.err.println("Usage: org.crypthing.things.appservice.Bootstrap [udpAddress] [oidRoot], where");
		System.err.println("\tudpAddress: IP/port for SNMP traps");
		System.err.println("\toidRoot: root OID for SNMP traps");
		System.err.println("\tRequired system properties:");
		System.err.println("\tjava.rmi.server.hostname: JMX host");
		System.err.println("\tcom.sun.management.jmxremote.host: JMX host - Same as java.rmi.server.hostname");
		System.err.println("\tcom.sun.management.jmxremote.port: JMX port");
		System.exit(1);
	}

	private class LaunchSpec
	{
		String cfgFile;
		String home;
		String[] env;
		int ret = Integer.MIN_VALUE;

		LaunchSpec(String cfgFile, String home, String[] env)
		{
			this.cfgFile = cfgFile;
			this.home = home;
			this.env = env;
		}

	}

	private Deque<LaunchSpec> launchspec = new ConcurrentLinkedDeque<>();

	private boolean isRunning = true;
	private final LifecycleEventDispatcher evt;
	private final LifecycleEvent heart;

	public Bootstrap(final LifecycleEventDispatcher evt)
	{
		this.evt = evt;
		heart = new LifecycleEvent(LifecycleEventType.heart,
				new EncodableString(
						"{\"jmx\":{\"address\":\"" + System.getProperty("java.rmi.server.hostname") + "\",\"port\":"
								+ System.getProperty("com.sun.management.jmxremote.port") + "},\"type\":\"AGENT\"}"));
	}

	public void run()
	{
		int i=0;
		evt.fire(new LifecycleEvent(LifecycleEventType.start, new EncodableString("Bootstrap has begun")));
		while (isRunning)
		{
			i=0;
			try { 
				while(i++<50 && isRunning)
				{
					LaunchSpec ls = launchspec.pollFirst();
					if(ls !=null)
					{
						synchronized(ls)
						{
							try {
								ls.ret = _launch(ls.cfgFile, ls.home, ls.env);
							} catch (Exception e) {
								ls.ret = EXCEPTION_ON_LAUNCH;
								e.printStackTrace();
							}
							ls.notifyAll();
						}
					}
					Thread.sleep(200);
				}
				evt.fire(heart); 
			}
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		System.out.println("Exiting now.");
		evt.fire(new LifecycleEvent(LifecycleEventType.stop, (new SignalBean(Bootstrap.class.getName(), "Bootstrap has ended")).encode()));
	}

	@Override
	public void shutdown()
	{

		boolean waiting = false;
		for (Entry<String, Process> entry : processes.entrySet())
		{
			waiting = true;
			if (entry.getValue().isAlive())
			{
				stop(entry.getKey(), TIMEOUT * 2);
			}
		}

		if(waiting)
		{
			try
			{
				Thread.sleep(TIMEOUT);
			}
			catch (Exception e)
			{
			};
	
			for (Entry<String, Process> entry : processes.entrySet())
			{
				if (entry.getValue().isAlive())
				{
					forceStop(entry.getKey(), TIMEOUT * 2);
				}
			}
		}

		if (mbName != null)
		{
			try
			{
				ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbName);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		isRunning = false;
	}

	@Override
	public int launch(final String cfgFile)
	{
		return launch(cfgFile, System.getProperty("user.dir"), null);
	}

	@Override
	public int launch(String cfgFile, String home, String[] env)
	{

		LaunchSpec ls = new LaunchSpec(cfgFile, home, env);
		launchspec.offer(ls);
		synchronized (ls)
		{
			while (ls.ret == Integer.MIN_VALUE)
			{
				try
				{
					ls.wait();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		return ls.ret; 
	}

	private Map<String,String> prepareEnv(String[] env)
	{
		Map<String,String> _env = Bootstrap.parseEnv(env);
		Map<String,String> __env = new HashMap<>();

		String inherit = (String)_env.get(ENV_OVERRIDE_KEY);
		if(OVERRIDE_ENV.equals(inherit))
		{
			__env.putAll(System.getenv());
			__env.putAll(_env);
		}
		else
		{
			__env.putAll(_env);
			__env.putAll(System.getenv());
		}
		

		__env.put(PARENT_JMX_HOST, System.getProperty("java.rmi.server.hostname"));
		__env.put(PARENT_JMX_PORT, System.getProperty("com.sun.management.jmxremote.port"));


		for(String key : _env.keySet())
		{
			if(key.startsWith(OVERRIDE))
			{
				__env.put(key.substring(OVERRIDE.length()), _env.get(key));
			}
		}
		return  __env;
	}

	public String getLaunch(String cfgFile, String[] env)  throws ConfigException
	{
		Map<String,String> __env = prepareEnv(env);
		FileInputStream config;
		try
		{
			config = new FileInputStream(cfgFile);
			final JVMConfig cfg = getJVMConfig(config, getSchema(), __env);
			final List<String> cmds = getCommands(cfg, cfgFile);
			final StringBuilder sb = new StringBuilder();
			for(String c : cmds)
			{
				sb.append(c).append(' ');
			}
			return  sb.toString();
		}
		catch (FileNotFoundException e)
		{
			throw new ConfigException("Config não encontrado: [" +  cfgFile + "]", e);
		}
	}


	private int _launch(String cfgFile, String home, String[] env)
	{
		Map<String,String> __env = prepareEnv(env);
		if(dumpEnv)
		{
			System.out.println("Environment that will be passed for " + cfgFile);
			for(Entry<String, String> pair : __env.entrySet())
			{
				System.out.println(pair.getKey() + "=" + pair.getValue());
			}
		}
		String _home = home != null ?  home  : System.getProperty("user.dir");
		int ret;
		try
		{
			final FileInputStream config = new FileInputStream(cfgFile);
			try
			{
				final JVMConfig cfg = getJVMConfig(config, getSchema(), __env);

				Process process;
				String name = cfg.getName();
				if((process = processes.get(name)) != null && process.isAlive())
				{
					return SERVER_ALREADY_STARTED;
				}

				final List<String> cmds = getCommands(cfg, cfgFile);
				final ProcessBuilder builder = new ProcessBuilder(cmds);

				builder.environment().putAll(__env);

				final StringBuilder sb = new StringBuilder();
				for(String c : cmds)
				{
					sb.append(c).append(' ');
				}
				System.out.println("CMD to execute: [" + sb.toString() + "] at " + _home);

				if (cfg.getRedirectTo() != null)
				{
					builder.redirectErrorStream(true);
					builder.redirectOutput(Redirect.appendTo(new File(cfg.getRedirectTo())));
				} else {
					builder.inheritIO();
				}
				process = builder.directory(new File(_home)).start();
				processes.put(name, process);
				configs.put(name, cfg);
				ret = 0;
				System.out.println(cfgFile + " configuration launched! With name:[" + name + "]");
				evt.fire(new LifecycleEvent(LifecycleEventType.work, new EncodableString(cfgFile + " configuration launched! With name:[" + name + "]")));
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
		ret.add((new StringBuilder(256)).append("-Dcom.sun.management.jmxremote.host=").append(jmx.getHost()).toString());
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


	@Override
	public int stop(String name, int timeout)
	{
		Process p = processes.get(name);
		if(p == null ) return NO_PROCESS_FOUND;
		if(!p.isAlive()) return ALREADY_STOPPED;
		JVMConfig cfg = configs.get(name);
		
		String port;
		String host;
		if(cfg !=null)
		{
			host = cfg.getJmx().getHost();
			port = cfg.getJmx().getPort();
		}
		else return NO_CONFIG_FOUND;
		try{
			ConnectionHolder holder = JMXConnection.getConnection(host, port);
			if(holder.ret !=0)
			{
				System.out.println(Network.getMessage(holder.ret, host, port));
				return EXCEPTION_WHILE_STOP;
			}
			final JMXConnector jmxc = holder.connection;
			try
			{
				final MBeanServerConnection mbean = jmxc.getMBeanServerConnection();
				final Iterator<ObjectName> it = mbean.queryNames(new ObjectName(Runner.MBEAN_PATTERN + "*"), null).iterator(); 
				ObjectName o;
				while (it.hasNext()) 
				{
					o =  it.next();
					try
					{
						mbean.invoke(o, "shutdown", new Object[] { jmxkey }, new String[] {  String.class.getName() });
					}
					catch (ReflectionException e) {
						//TODO: old versions doesnt have  an string as parameter.
						System.err.println("You can't teach an old dog new tricks, but you can throw them a bone.");
						mbean.invoke(o, "shutdown", new Object[] { }, new String[] { });
					}
				}
				if(!p.waitFor(timeout, TimeUnit.SECONDS))
				{
					return STOP_TIMEOUT;
				}
			}
			finally { try{ jmxc.close();} catch(Exception e) {} }
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return EXCEPTION_WHILE_STOP;
		}

		return OK;
	}

	@Override
	public int forceStop(String name, int timeout)
	{
		int ret = stop(name, timeout);
		int count = timeout;
		if(ret!=OK && ret!=EXCEPTION_WHILE_STOP && ret!=STOP_TIMEOUT) return ret;

		Process p = processes.get(name);
		while(count -- > 0 && ret!=EXCEPTION_WHILE_STOP)
		{
			try {Thread.sleep(1000); } catch(Exception e){};
			if(!p.isAlive()) return OK;
		}

		if(p.isAlive())
		{
			try { 
				if(p.destroyForcibly().waitFor(timeout, TimeUnit.SECONDS))
				{
					return OK;
				}
			} 
			catch(Exception e) { return EXCEPTION_WHILE_FORCE_STOP; };
		}
		return COULD_NOT_STOP;
	}

	@Override
	public int isAlive(String name)
	{
		Process process;
		if((process = processes.get(name)) != null)
		{
			return  process.isAlive() ? BootstrapMBean.ALIVE : BootstrapMBean.DEAD;
		}
		return BootstrapMBean.UNKNOW;
	}

	@Override
	public String[] list()
	{
		ArrayList<String> list = new ArrayList<>(processes.size());
		for(Entry<String, Process> entry : processes.entrySet())
		{
			if(entry.getValue().isAlive())
			{
				list.add(entry.getKey());
			}
		}
		
		return list.toArray(new String[list.size()]);
	}


	public int exitCode(String name)
	{
		Process p = processes.get(name);
		if(p == null ) return NO_PROCESS_FOUND;
		if(!p.isAlive()) return SERVER_ALIVE;
		return p.exitValue();
	}

	public int waitFor(String name, long timeout)
	{
		Process p = processes.get(name);
		if(p == null ) return NO_PROCESS_FOUND;
		if(!p.isAlive()) return 0;
		try { p.waitFor(timeout, TimeUnit.SECONDS); }
		catch (InterruptedException e) { return EXCEPTION_ON_WAIT; }
		return 0;
	}



}

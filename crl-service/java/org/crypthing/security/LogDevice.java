
package org.crypthing.security;
import java.util.logging.Level;
import java.util.logging.Logger;
@SuppressWarnings("unused") public final class LogDevice
{
	public static final int LOG_LEVEL_TRACE = 1;
	public static final int LOG_LEVEL_DEBUG = 2;
	public static final int LOG_LEVEL_INFO = 3;
	public static final int LOG_LEVEL_WARNING = 4;
	public static final int LOG_LEVEL_ERROR = 5;
	public static final int LOG_LEVEL_FATAL = 6;
	public static final int LOG_LEVEL_NONE = LOG_LEVEL_FATAL + 1;
	public static final int LOG_LEVEL = LOG_LEVEL_ERROR;
	private static final int DEFAULT_STACK_LEVEL = 7;
	private Logger log;
	public LogDevice(final String name) { log = Logger.getLogger(name);}
	public void trace(final String msg) { if (LOG_LEVEL < LOG_LEVEL_DEBUG) log.log(Level.FINER, msg); }
	public void debug(final String msg) { if (LOG_LEVEL < LOG_LEVEL_INFO) log.log(Level.FINE, msg); }
	public void info(final String msg) { if (LOG_LEVEL < LOG_LEVEL_WARNING) log.log(Level.CONFIG, msg); }
	public void warning(final String msg) { if (LOG_LEVEL < LOG_LEVEL_ERROR) log.log(Level.INFO, msg); }
	public void error(final String msg) { if (LOG_LEVEL < LOG_LEVEL_FATAL) log.log(Level.WARNING, msg); }
	public void error(final String msg, final Throwable e) { if (LOG_LEVEL < LOG_LEVEL_FATAL) log.log(Level.WARNING, msg, e); }
	public void fatal(final String msg) { if (LOG_LEVEL < LOG_LEVEL_NONE) log.log(Level.SEVERE, msg); }
	public void fatal(final String msg, final Throwable e) { if (LOG_LEVEL < LOG_LEVEL_NONE) log.log(Level.SEVERE, msg, e); }
	public void printStack() { if (LOG_LEVEL < LOG_LEVEL_DEBUG) printStack(DEFAULT_STACK_LEVEL); }
	public void printStack(int level)
	{
		if (LOG_LEVEL < LOG_LEVEL_DEBUG)
		{
			Exception e = new Exception();
			StackTraceElement[] b = e.getStackTrace();
			StringBuilder sb = new StringBuilder(256);
			sb.append("Call stack for: ");
			for(int i = 1; i < level & i < b.length; i++ )
			{
				sb.append(b[i].getClassName()).append(".");
				sb.append(b[i].getMethodName());
				if(!b[i].getMethodName().equals("<init>")) sb.append("()");
				if(b[i].isNativeMethod()) sb.append("[native]");
				else
				{
					sb.append("[");
					sb.append(b[i].getLineNumber());
					sb.append("]");
				}
				sb.append("\n");
			}
			System.out.println(sb.toString());
		}
	}
}
		
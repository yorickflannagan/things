package org.crypthing.things.snmp;

public class ProcessingEvent extends Event
{

	private static String me = ProcessingEvent.class.getName();
	private static String kaller()
	{
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		for (int i=1; i<stElements.length; i++) {
			StackTraceElement ste = stElements[i];
			if (!ste.getClassName().equals(me) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
					return ste.getClassName();
			}
		}
		return null;		
	}
	public enum ProcessingEventType { info, warning, error }
	private final ProcessingEventType type;
	public ProcessingEvent(final ProcessingEventType type) { this(type, (Encodable)null); }
	public ProcessingEvent(final ProcessingEventType type, final String data) { this(type, new EncodableString(data)); }
	public ProcessingEvent(final ProcessingEventType type, final String message, final Exception e) { this(type, new ErrorBean(kaller(), message, e).encode()); }
	public ProcessingEvent(final ProcessingEventType type, final Encodable data)
	{
		this.type = type;
		final String oid;
		switch(type)
		{
		case info:
			oid = ".3.1";
			break;
		case warning:
			oid = ".3.2";
			break;
		case error:
			oid = ".3.3";
			break;
		default:
			throw new IllegalArgumentException("Unknow event type:" + type);
		}
		setRelativeOID(oid).setData(data);
	}
	public ProcessingEventType getType() { return type; }
}
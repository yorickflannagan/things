package org.crypthing.things.snmp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class ProcessingEvent extends Event
{
	public enum ProcessingEventType { info, warning, error }
	private ProcessingEventType type;
	private Throwable throable;
	private String message;
	private boolean dirty=true;
	public ProcessingEvent() {}
	public ProcessingEvent(final ProcessingEventType type) { this(type, null); }
	public ProcessingEvent(final ProcessingEventType type, final String message) { this(type, message, null); }
	public ProcessingEvent(final ProcessingEventType type, final String message, final Throwable e)
	{
		this.message = message;
		this.throable = e;
		this.type = type;
	}



	@Override
	public Encodable getData() { if(dirty) update(); return super.getData(); }

	private void update() 
	{
		if(!dirty) return;

		PrintStream pout = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try { pout = new PrintStream(out, true, "UTF-8"); } catch(UnsupportedEncodingException e) {pout = new PrintStream(out);}
		if (message != null) pout.print(message);
		pout.print('\n');
		if (throable != null) throable.printStackTrace(pout);
		pout.close();
		setData(new ByteEncodable(out.toByteArray()));
		final String oid;
		switch(type)
		{
		case info:
			oid = ".3.1";
			break;
		case warning:
			oid = ".3.2";
			break;
		default:
			oid = ".3.3";
		}
		setThroable(throable).setRelativeOID(oid);
		dirty = false;
	}

	private class ByteEncodable implements Encodable
	{
		
			private byte[] value;
			ByteEncodable(byte[] value)
			{
				this.value = value;
			}
			@Override public Object encode() { return value; }
			@Override public void decode(final String data) {}
			@Override public void decode(final byte[] data) throws EncodeException {}
			@Override public void decode(final int data) throws EncodeException {}
			@Override public Type getEncoding() { return Encodable.Type.OCTET_STRING; }
	}

	public ProcessingEventType getType() { return type; }
	public ProcessingEvent setType(ProcessingEventType type){ dirty = true; this.type=type; return this;}
	public Throwable getThroable() { return throable; }
	public ProcessingEvent setThroable(final Throwable throable) { dirty = true; this.throable = throable; return this; }
	public ProcessingEvent setMessage(final String message) { dirty = true; this.message = message; return this; }
}

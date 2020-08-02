package org.crypthing.things.snmp;

import org.json.JSONObject;

public class Bean
{
	@Override public String toString() { return (new JSONObject(this)).toString(); }
	public EncodableString encode() { return new EncodableString(toString()); }
}
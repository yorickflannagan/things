package org.crypthing.things.snmp;


/**
 * An encodable bean to structure event messages
 * {
 *     "$schema": "http://json-schema.org/draft-07/schema",
 *     "$id": "http://crypthing.org/signal-bean.json",
 *     "type": "object",
 *     "title": "SignalBean",
 *     "description": "Strucutured event message.",
 *     "default": {},
 *     "examples": [
 *         {
 *             "who": "org.crypthing.things.snmp.SignalBean",
 *             "message": "Structured signal message example"
 *         }
 *     ],
 *     "required": [],
 *     "properties": {
 *         "who": {
 *             "$id": "#/properties/who",
 *             "type": "string",
 *             "title": "Event emitter",
 *             "description": "Event emitter class name.",
 *             "default": "",
 *             "examples": [ "org.crypthing.things.snmp.SignalBean" ]
 *         },
 *         "message": {
 *             "$id": "#/properties/message",
 *             "type": "string",
 *             "title": "Event message",
 *             "description": "Descriptive message on event",
 *             "default": "",
 *             "examples": [ "Structured signal message example" ]
 *         }
 *     },
 *     "additionalProperties": true
 * }
 */
public class SignalBean extends Bean
{
	private String who;
	private String message;
	public SignalBean() { who = ""; message = ""; }
	public SignalBean(final String who, final String message) { setWho(who); setMessage(message); }
	public String getWho() { return who; }
	public String getMessage() { return message; }
	public void setMessage(final String signal) { this.message = signal; }
	public void setWho(final String who) { this.who = who; }
}
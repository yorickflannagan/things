package org.crypthing.things.snmp;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * {
 *     "$schema": "http://json-schema.org/draft-07/schema",
 *     "$id": "http://crypthing.org/error-bean.json",
 *     "type": "object",
 *     "title": "ErrorBean",
 *     "description": "Structured error event message.",
 *     "default": {},
 *     "examples": [
 *         {
 *             "stacktrace": "java.io.FileNotFoundException: absent.file (Arquivo ou diretório não encontrado)\n\tat java.base/java.io.FileInputStream.open0(Native Method)\n\tat java.base/java.io.FileInputStream.open(FileInputStream.java:212)\n\tat java.base/java.io.FileInputStream.<init>(FileInputStream.java:154)\n\tat org.crypthing.Concept.main(Concept.java:16)\n",
 *             "errorMessage": "absent.file (Arquivo ou diretório não encontrado)",
 *             "cause": "java.io.FileNotFoundException",
 *             "message": "Processing error",
 *             "who": "org.crypthing.Concept"
 *         }
 *     ],
 *     "required": [ ],
 *     "properties": {
 *         "who": {
 *             "$id": "#/properties/who",
 *             "type": "string",
 *             "title": "Event emitter",
 *             "description": "Event emitter class name.",
 *             "default": "",
 *             "examples": [ "org.crypthing.things.snmp.ErrorBean" ]
 *         },
 *         "message": {
 *             "$id": "#/properties/message",
 *             "type": "string",
 *             "title": "Event message",
 *             "description": "Descriptive message on event",
 *             "default": "",
 *             "examples": [ "Processing error message" ]
 *         },
 *         "cause": {
 *             "$id": "#/properties/cause",
 *             "type": "string",
 *             "title": "Java Exception",
 *             "description": "Class name of the exception.",
 *             "default": "",
 *             "examples": [ "java.io.FileNotFoundException" ]
 *         },
  *         "errorMessage": {
 *             "$id": "#/properties/errorMessage",
 *             "type": "string",
 *             "title": "Error Message",
 *             "description": "Exception message.",
 *             "default": "",
 *             "examples": [ "absent.file (Arquivo ou diretório não encontrado)" ]
 *         },
 *         "stacktrace": {
 *             "$id": "#/properties/stacktrace",
 *             "type": "string",
 *             "title": "Stacktrace",
 *             "description": "Printed version of the exception stack trace.",
 *             "default": "",
 *             "examples": [ "java.io.FileNotFoundException: absent.file (Arquivo ou diretório não encontrado)\n\tat java.base/java.io.FileInputStream.open0(Native Method)\n\tat java.base/java.io.FileInputStream.open(FileInputStream.java:212)\n\tat java.base/java.io.FileInputStream.<init>(FileInputStream.java:154)\n\tat org.crypthing.Concept.main(Concept.java:16)\n" ]
 *         }
*     },
 *     "additionalProperties": true
 * }
 */
public class ErrorBean extends SignalBean
{
	private String cause;
	private String stacktrace;
	private String errorMessage;
	public ErrorBean() { super(); cause = ""; stacktrace = ""; errorMessage = ""; }
	public ErrorBean(final String who, final String message) { this(who, message, null); }
	public ErrorBean(final String who, final String message, final Throwable e)
	{
		super(who, message);
		if (e != null)
		{
			cause = e.getClass().getName();
			errorMessage = e.getMessage();
			final StringWriter writer = new StringWriter(1024);
			e.printStackTrace(new PrintWriter(writer));
			stacktrace = writer.toString();
		}
	}
	public String getStacktrace() { return stacktrace; }
	public void setStacktrace(final String stacktrace) { this.stacktrace = stacktrace; }
	public String getCause() { return cause; }
	public void setCause(final String cause) { this.cause = cause; }
	public String getErrorMessage() { return errorMessage; }
	public void setErrorMessage(final String errorMessage) { this.errorMessage = errorMessage; }
}
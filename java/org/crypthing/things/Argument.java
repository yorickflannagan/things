package org.crypthing.things;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class Argument
	implements Cloneable
{
	private String key;
	private String usage;
	private int type;
	private boolean optional;

	private String value;
	private int intValue;
	private long longValue;
	private boolean boolValue;


	public Argument()
	{
		this("", "", "");
	}

	public Argument(
			final String key,
			final String usage,
			final String value
	)
	{
		this(key, usage, value, STRING_TYPE, true);
	}

	public Argument(
		final String key,
		final String usage,
		final String value,
		final int type,
		final boolean optional
	)
	{
		this.key = key;
		this.usage = usage;
		this.value = value;
		this.type = type;
		this.optional = optional;
	}


	public String getKey()
	{
		return key;
	}

	public void setKey(final String key)
	{
		this.key = key;
	}

	public int getType()
	{
		return type;
	}

	public void setType(final int type)
	{
		this.type = type;
	}

	public String getUsage()
	{
		return usage;
	}

	public void setUsage(final String usage)
	{
		this.usage = usage;
	}

	public boolean getOptional()
	{
		return optional;
	}

	public void setOptional(final boolean optional)
	{
		this.optional = optional;
	}

	public String getValue()
	{
		return value;
	}

	public int getIntValue()
	{
		return intValue;
	}

	public long getLongValue()
	{
		return longValue;
	}

	public boolean getBoolValue()
	{
		return boolValue;
	}

	@Override
	public Object clone()
	{
		final Argument ret = new Argument(key, usage, value, type, optional);
		ret.intValue = intValue;
		ret.boolValue = boolValue;
		ret.longValue = longValue;
		return ret;
	}


	public static final int STRING_TYPE = 0;
	public static final int INT_TYPE = 1;
	public static final int LONG_TYPE = 2;
	public static final int BOOLEAN_TYPE = 3;

	/**
	 * 
	 * @param args
	 * An argument must be of the form: [someKey][separator]someValue
	 * @param params
	 * @param separator: must not be an space char
	 * @return
	 */
	public static Map<String, Argument> parse(
		final String[] args,
		final Map<String, Argument> params,
		final String separator
	) throws IllegalArgumentException
	{
		final Map<String, Argument> ret = new HashMap<String, Argument>();
		for (int i = 0; i < args.length; i++)
		{
			final String[] arg = args[i].split(separator);
			if (arg.length != 2)
			{
				throw new IllegalArgumentException("Invalid argument " + args[i]);
			}
			final Argument lookup = params.get(arg[0]);
			if (lookup == null)
			{
				throw new IllegalArgumentException("Unknown argument " + args[i]);
			}

			final Argument argument = (Argument) lookup.clone();
			switch (lookup.getType())
			{
			case INT_TYPE:
				try
				{
					argument.intValue = Integer.parseInt(arg[1]);
				}
				catch (final NumberFormatException e)
				{
					throw new IllegalArgumentException("Argument " + arg[0] + " value " + arg[1] + " should be an integer", e);
				}
				break;
			case LONG_TYPE:
				try
				{
					argument.longValue = Long.parseLong(arg[1]);
				}
				catch (final NumberFormatException e)
				{
					throw new IllegalArgumentException("Argument " + arg[0] + " value " + arg[1] + " should be a long", e);
				}
				break;
			case BOOLEAN_TYPE:
				if (!((argument.boolValue = arg[1].equalsIgnoreCase("true")) ||
						arg[1].equalsIgnoreCase("false")))
				{
					throw new IllegalArgumentException("Argument " + arg[0] + " value " + arg[1] + " should be a boolean");
				}
			}
			argument.value = arg[1];
			ret.put(arg[0], argument);
		}

		final Iterator<String> keys = params.keySet().iterator();
		while (keys.hasNext())
		{
			final String key = keys.next();
			if (!params.get(key).getOptional() && ret.get(key) == null)
			{
				throw new IllegalArgumentException("Argument " + key + " is not optional");
			}
		}
		return ret;
	}
}

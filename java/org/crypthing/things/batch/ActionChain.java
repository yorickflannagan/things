package org.crypthing.things.batch;

public class ActionChain
{
	private final int action;
	private final Object bean;
	private ActionChain next;

	public ActionChain(final int action, final Object bean)
	{
		this.action = action;
		this.bean = bean;
	}

	public ActionChain addNext(final ActionChain next)
	{
		this.next = next;
		return next;
	}

	public int getAction()
	{
		return action;
	}

	public Object getBean()
	{
		return bean;
	}

	public ActionChain getNext()
	{
		return next;
	}
}

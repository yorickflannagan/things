package org.crypthing.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

public final class PooledSSLSocketFactory extends SSLSocketFactory
{

	public PooledSSLSocketFactory()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getDefaultCipherSuites()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportedCipherSuites()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

}

package org.crypthing.things.appservice.diagnostic;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * NetworkDiagnostic
 */
public class Network
{
    //TODO: unificar os codigos de erro 4 bits mais significativos cliente/ 4 bits menos significativos servidor. ou algo do tipo.

    public static final int OK =                 0;
    public static final int UNKNOW_HOST =        1 << 4;
    public static final int CONNECTION_REFUSED = 2 << 4;
    public static final int OTHER =              3 << 4;
    public static final int INVALID_PORT =       4 << 4;


    public static int verifyConnection(String host, String port)
    {
        try   { new Socket(host, Integer.parseInt(port)).close();}
        catch (UnknownHostException e)      { return UNKNOW_HOST; }
        catch (ConnectException e)          { return CONNECTION_REFUSED; }
        catch (NumberFormatException e)     { return INVALID_PORT; }
        catch (IllegalArgumentException e)  { return INVALID_PORT; }
        catch (IOException e)               { return OTHER; }
        return 0;
    }


    public static String getMessage(int code, String host, String port)
    {
        switch(code)
        {
            case Network.OK:
                return "OK";
            case Network.CONNECTION_REFUSED:
                return "Connection Refused";
            case Network.INVALID_PORT:
                return "Port invalid:[" + port + "]";
            case Network.UNKNOW_HOST:
                return "Unknow host:[" + host + "]";
            case Network.OTHER:
                return "Other IOException";
            default:
                return "Unknow error: " + code;

        }
    }



}
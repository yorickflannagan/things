package org.crypthing.things.appservice;

import java.io.IOException;
import java.sql.SQLException;

/**
 * GoodGuy
 */
public class GoodGuy extends Sandbox
{

    @Override
    protected boolean execute() throws IOException, SQLException
    {
        while(isRunning())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        return true;
    }

    
}
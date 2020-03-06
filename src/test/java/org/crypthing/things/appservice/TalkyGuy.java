package org.crypthing.things.appservice;

import java.io.IOException;
import java.sql.SQLException;

/**
 * GoodGuy
 */
public class TalkyGuy extends Sandbox
{

    @Override
    protected boolean execute() throws IOException, SQLException
    {
        while(isRunning())
        {
            try
            {
                System.out.println("Howdy!");
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        return true;
    }

    
}
package org.crypthing.things.appservice;

import java.io.IOException;
import java.sql.SQLException;

/**
 * GoodGuy
 */
public class EvilGuy extends Sandbox
{

    @Override
    protected boolean execute() throws IOException, SQLException
    {
        while(true)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
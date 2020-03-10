package org.crypthing.things.appservice;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.crypthing.things.appservice.diagnostic.Network;

/**
 * JMXConnection
 */
public class JMXConnection {
    private static JMXConnection instance = new JMXConnection();

    public class ConnectionHolder
    {
        public ConnectionHolder(JMXConnector connection, int ret)
        {
            this.connection = connection;
            this.ret = ret;
        }

        public final JMXConnector connection;
        public final int ret;
    }

    public static ConnectionHolder getConnection(String host, String port)
    {
        JMXConnector con = null;
        int ret = 0;
        try {
            con = JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi"));
        } catch (Exception e) {
            ret = Network.verifyConnection(host, port);
        }
        return  instance.new ConnectionHolder(con, ret);
    }
}
import java.net.*;
import java.io.*;
import java.util.*;

public class Server {//implements ServerInterface {

    // System Configuration
    public static int DEFAULT_PORT = 3000;


    // Server Private Data
    int m_port = DEFAULT_PORT;
    Server_ProcThread m_procThread;

    public Server (int port) {
        m_port = port;
    }
    
    public void setProcThread (Server_ProcThread procThread) {
        m_procThread = procThread;
    }

    public static void main (String args[]) throws Exception {

        int port = DEFAULT_PORT;

        // argument check
        if (args.length != 1) {
            System.out.println ("Error: Incorrect Argument Count, " + args.length);
            return;
        } else {
            try {
                port = Integer.parseInt (args[0]);
                System.out.println ("Using port: " + port);

            } catch (Exception e) {
                System.out.println ("Exception: " + e);
                e.printStackTrace ();
                System.exit (0);
            }
        }

        // prepare server object
        Server s = new Server (port);

        // @lfred: Start the main Proc thread
        s.setProcThread (Server_ProcThread.getServProcThread ());

        ServerSocket skt = new ServerSocket (port);
        StringBuffer buf = new StringBuffer ();

        // @lfred: always ready to serve the clients
        while (true) {

            try {
                Socket sc = skt.accept ();
                System.out.println ("Server: incoming link");

                // @lfred: push the new connection object
                Server_ProcThread.getServProcThread ().enqueueCmd (
                    new Server_Command_NewConn (
                        Server_Command.M_CMD_INCOMING_CONN,
                        sc));

                System.out.println (
                    "A client from " +
                    sc.getInetAddress().toString() + ":" +
                    Integer.toString (sc.getPort ()) +
                    " is connected");

            } catch (Exception e) {
                System.out.println (e);
                e.printStackTrace ();
            }
        }
    }
}
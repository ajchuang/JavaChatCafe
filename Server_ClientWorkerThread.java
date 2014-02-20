import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Server_ClientWorkerThread implements Runnable {

    // @lfred: the queue used to received server thread command - write only when server asks.
    LinkedBlockingQueue<Server_Command> m_cmdQueue;
    Socket m_socket;
    int m_userId;
    
    public Server_ClientWorkerThread (int userId, Socket skt) {
        m_socket = skt;
        m_userId = userId;
        m_cmdQueue = new LinkedBlockingQueue<Server_Command> ();
    }
    
    public void run () {
    
        while (true) {
            Server_Command sCmd;

            try {
                sCmd = m_cmdQueue.take ();
            } catch (Exception e) {
                System.out.println ("Dequeue Exception - " + e);
                e.printStackTrace ();
                continue;
            }
            
            System.out.println ("Incoming command @ client worker.");
            
            switch (sCmd.getServCmd ()) {
                
            }
            
        } // while (true)
    
    }
}
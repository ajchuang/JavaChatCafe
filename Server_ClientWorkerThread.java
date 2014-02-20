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
    
    public void enqueueCmd (Server_Command sCmd) {
    
        try {
            m_cmdQueue.put (sCmd);
        } catch (Exception e) {
            Server.logBug ("enqueueCmd: Exception - " + e);
            e.printStackTrace ();
        }
    }
    
    // @lfred: tell the client that Auth passed.
    void handleAuthOk (Server_Command sCmd) {
    }
    
    // @lfred: tell the client that Auth failed.
    void handleAuthFail (Server_Command sCmd) {
    }

    public void run () {
        
        // @lfred: create corresponding reader thread.
        Server_ClientReaderThread scr = new Server_ClientReaderThread (m_socket, m_userId);
        scr.start ();
    
        // @lfred: enter the loop to process the commands sent from server main thread.
        while (true) {
            
            Server_Command sCmd;

            try {
                sCmd = m_cmdQueue.take ();
            } catch (Exception e) {
                Server.logBug ("Dequeue Exception - " + e);
                e.printStackTrace ();
                continue;
            }
            
            System.out.println ("Incoming command @ client worker.");
            
            switch (sCmd.getServCmd ()) {
                case M_SERV_CMD_RESP_AUTH_OK:
                    handleAuthOk (sCmd);
                break;
                
                case M_SERV_CMD_RESP_AUTH_FAIL:
                    handleAuthFail (sCmd);
                break;
            }
            
        } // while (true)
    
    }
}
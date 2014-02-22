import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Server_ClientWorkerThread implements Runnable {

    // @lfred: the queue used to received server thread command - write only when server asks.
    LinkedBlockingQueue<Server_Command> m_cmdQueue;
    Socket m_socket;
    ObjectOutputStream m_oStream;
    //Server_ClientState m_state;

    int m_userId;
    
    public Server_ClientWorkerThread (int userId, Socket skt) {
        m_socket = skt;
        
        m_userId = userId;
        m_cmdQueue = new LinkedBlockingQueue<Server_Command> ();
        
        try {
            m_oStream = new ObjectOutputStream (m_socket.getOutputStream ());
        } catch (Exception e) {
            Server.logBug ("Should not happen");
            e.printStackTrace ();
        }
        //m_state = M_CLIENT_STATE_CONNECTED;
    }
    
    public void enqueueCmd (Server_Command sCmd) {
    
        try {
            m_cmdQueue.put (sCmd);
        } catch (Exception e) {
            Server.logBug ("enqueueCmd: Exception - " + e);
            e.printStackTrace ();
        }
    }
    
    void sendToClient (CommObject co) {
        
        try {
            m_oStream.writeObject (co);
            
        } catch (Exception e) {
            Server.logBug ("Failed to send CommObject");
            e.printStackTrace ();
            
            // @lfred: TODO - send a CLNT_DOWN back to main thread.
        }
        
        return;
    }
    
    // @lfred: tell the client that Auth passed.
    void handleAuthOk (Server_Command sCmd) {
        
        CommObject co = new CommObject (CommObjectType.E_COMM_RESP_LOGIN_OK);
        sendToClient (co);
    }
    
    // @lfred: tell the client that Auth failed.
    void handleAuthFail (Server_Command sCmd) {
        
        CommObject co = new CommObject (CommObjectType.E_COMM_RESP_LOGIN_FAIL);
        sendToClient (co);  
    }
    
    void handleWhoelseRsp (Server_Command sCmd) {
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.logBug ("Bad Type @ handleWhoelseRsp");
            return;
        }
        
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec) sCmd;
        CommObject co = new CommObject (CommObjectType.E_COMM_RESP_WHOELSE);
        
        for (int i=0; i<sCmd_v.getStrCount () ; ++i)
            co.pushString (sCmd_v.getStringAt (i));
        
        sendToClient (co);
    }
    
    void handleBroadcastRsp (Server_Command sCmd) {
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.logBug ("Bad Type @ handleBroadcastRsp");
            return;
        } 
        
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec) sCmd;
        CommObject co = new CommObject (CommObjectType.E_COMM_RESP_BROADCAST);
        co.pushString (sCmd_v.getStringAt (0));
        co.pushString (sCmd_v.getStringAt (1));        
        sendToClient (co);
    }
    
    void handleLogoutReq (Server_Command sCmd) {
        
        try {
            m_oStream.close ();
            m_socket.close ();
        } catch (Exception e) {
            e.printStackTrace ();
        }
        
        Server_Command sc = new Server_Command (Server_CmdType.M_SERV_CMD_LOGOUT_DONE, m_userId);
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }

    public void run () {
        
        // @lfred: create corresponding reader thread.
        Server_ClientReaderThread scr = new Server_ClientReaderThread (m_socket, m_userId);
        Thread x = new Thread (scr);
        x.start ();
    
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
                
                case M_SERV_CMD_RESP_WHOELSE:
                    handleWhoelseRsp (sCmd);
                break;
                
                case M_SERV_CMD_RESP_BROADCAST:
                    handleBroadcastRsp (sCmd);
                break;
                
                case M_SERV_CMD_REQ_LOGOUT:
                    handleLogoutReq (sCmd);
                return;
            }
            
        } // while (true)
    
    }
}
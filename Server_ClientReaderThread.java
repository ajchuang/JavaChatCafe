import java.net.*;
import java.io.*;
import java.util.*;

//  Each client has its own state:
//      CONNECTED - not login yet
//      READY - LOGIN
public class Server_ClientReaderThread implements Runnable {

    Socket m_clientSocket;

    // @lfred: user data
    ObjectInputStream m_inputStream;
    int m_cid;    
    int m_loginCount;

    public Server_ClientReaderThread (Socket s, int id) {

        try {
            m_clientSocket = s;
            m_inputStream = new ObjectInputStream (s.getInputStream ());
            m_cid = id;
            
        } catch (Exception e) {
            System.out.println ();
        }
    }
    
    void handleLogin (CommObject co) {
        
        String pass = co.getStringAt (1);
        String name = co.getStringAt (0);
        
        System.out.println (
            "Incoming login request: " + name + ":" + pass);
            
        // @lfred: we should send the server command to the main server processor
        Server_Command_AuthReq sca = 
            new Server_Command_AuthReq (m_cid, name, pass);
                
        Server_ProcThread.getServProcThread ().enqueueCmd (sca);
    }
    
    void handleWhoelse (CommObject co) {        
        Server.log ("handleWhoelse");
        
        Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_RESP_WHOELSE, m_cid);
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }
    
    void handleMsgReq (CommObject co) {
        
        String name = co.getStringAt (0);
        String msg  = co.getStringAt (1);
                        
        Server.log ("handleMsgReq: " + name + ":" + msg);
        
        // @lfred: create server command
        Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_IND_MSG, m_cid);
        sc.pushString (name);
        sc.pushString (msg);
        
        // enqueue to handle
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }
    
    void handleBroadcast (CommObject co) {
        
        String msg = co.getStringAt (0);
        
        Server.log ("handleBroadcast: " + msg);
        
        // @lfred: create server command
        Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_RESP_BROADCAST, m_cid);
        sc.pushString (msg);
        
        // enqueue to handle
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }
    
    void handleWhoLastHr (CommObject co) {
        
        Server.log ("handleWhoLastHr");
        
        Server_Command sc = new Server_Command (Server_CmdType.M_SERV_CMD_RESP_WHOELSELASTHR, m_cid);
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }
    
    void handleLogoutReq (CommObject co) {
        
        Server.log ("handleLogoutReq");
        
        // @lfred: close the input stream ?
        try {
            m_inputStream.close ();
        } catch (Exception e) {
            e.printStackTrace ();
        }
        
        Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_REQ_LOGOUT, m_cid);
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }

    public void run () {

        System.out.println ("Server_ClientThread is ON");

        try {
            while (true) {
                // keep reading data from user
                Object ro = m_inputStream.readObject ();

                if (ro instanceof CommObject) {
                    CommObject co = (CommObject) ro;

                    switch (co.getOpCode ()) {
                    
                        case E_COMM_REQ_LOGIN:
                            handleLogin (co);
                        break;
                        
                        case E_COMM_REQ_WHOELSE:
                            handleWhoelse (co);
                        break;
                        
                        case E_COMM_RESP_WHOLASTHR:
                            handleWhoLastHr (co);
                        break;
                        
                        case E_COMM_REQ_MESSAGE:
                            handleMsgReq (co);
                        break;
                        
                        case E_COMM_REQ_BROADCAST:
                            handleBroadcast (co);
                        break;
                        
                        case E_COMM_REQ_LOGOUT:
                            handleLogoutReq (co);
                        return;
                        
                        default:
                            Server.log ("Not handled event: " + co.getOpCode().name());
                        break;
                    }
                } else {
                    Server.logBug ("What are you sending ?");
                }
            }
        } catch (Exception e) {
            System.out.println ("User drop the connection: send disconnect");
            e.printStackTrace ();
            return;
        }
    }
}
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
        
        Server_Command sc = new Server_Command (Server_CmdType.M_SERV_CMD_REQ_LOGOUT, m_cid);
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }
    
    void handleWholastHrReq (CommObject co) {
        
        Server.log ("handleWholastHrReq");
        
        Server_Command_StrVec sc = 
            new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_RESP_WHOELSELASTHR, m_cid);
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }
    
    void handleBlockReq (CommObject co) {
        
        Server.log ("handleBlockReq");
        
        Server_Command_StrVec sc = 
            new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_BLOCK_REQ, m_cid);
        sc.pushString (co.getStringAt (0));
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
        
    }
    
    void handleUnblockReq (CommObject co) {
        
        Server.log ("handleUnblockReq");
        
        Server_Command_StrVec sc = 
            new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_UNBLOCK_REQ, m_cid);
        sc.pushString (co.getStringAt (0));
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }
    
    void handleAddUserReq (CommObject co) {
        
        Server.log ("handleAddUserReq");
        
        Server_Command_StrVec sc = 
            new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_ADDUSER_REQ, m_cid);
        sc.pushString (co.getStringAt (0));
        sc.pushString (co.getStringAt (1));
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }
    
    void handleChangePassReq (CommObject co) {
        
        Server.log ("handleChangePassReq");
        
        Server_Command_StrVec sc = 
            new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_CHANGE_PWD_REQ, m_cid);
        sc.pushString (co.getStringAt (0));
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }
    
    void handleSyncReq (CommObject co) {
        
        Server.log ("handleSyncReq");
        
        Server_Command sc = 
            new Server_Command (Server_CmdType.M_SERV_CMD_SYNC_REQ, m_cid);
        Server_ProcThread.getServProcThread().enqueueCmd (sc);
    }

    public void run () {

        System.out.println ("Server_ClientThread is ON");

        try {
            while (true) {
                // keep reading data from user
                Object ro = null;
                
                try {
                    
                    // @lfred: polling for objects
                    ro  = m_inputStream.readObject ();
                    
                } catch (EOFException eof) {
                    Server.log ("User drop the connection: send disconnect");
                    handleLogoutReq (null);
                    return;
                } catch (SocketException se) {
                    Server.log ("Socket closed already");
                    return;
                } catch (Exception e) {
                    Server.logBug ("Something I dont know.");
                    e.printStackTrace ();
                    handleLogoutReq (null);
                    return;
                } 
                
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
                        
                        case E_COMM_REQ_WHOLASTHR:
                            handleWholastHrReq (co);
                        break;
                        
                        case E_COMM_REQ_BLOCK_USR:
                            handleBlockReq (co);
                        break;
                        
                        case E_COMM_REQ_UNBLOCK_USR:
                            handleUnblockReq (co);
                        break;
                        
                        case E_COMM_ADD_USER_REQ:
                            handleAddUserReq (co);
                        break;
                        
                        case E_COMM_CHANGE_PASS_REQ:
                            handleChangePassReq (co);
                        break;
                        
                        case E_COMM_SYNC_DB_REQ:
                            handleSyncReq (co); 
                        break;
                        
                        case E_COMM_REQ_LOGOUT:
                            handleLogoutReq (co);
                            Server.log ("Server_ClientReader is off");
                        return;
                        
                        default:
                            Server.log ("Not handled event: " + co.getOpCode().name());
                        break;
                    }
                } else {
                    Server.logBug ("What are you sending ?");
                }
            }
        } finally {
            Server.log ("Clean up before leaving ReaderThread."); 
        }
    }
}
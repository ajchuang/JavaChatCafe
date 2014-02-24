import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// @lfred: This server thread is used to write to the Clients
public class Server_ProcThread implements Runnable {

    // @lfred: all user list. Usr related info
    Server_UserDatabase m_userDB;
    
    // @lfred: the key data structure in async processing
    LinkedBlockingQueue<Server_Command> m_cmdQueue;
    
    // @lfred: system user management
    Hashtable<Integer, Socket> m_clients;           //  <cid, socket>
    Hashtable<Integer, Server_ClientWorkerThread> m_clntThreadPool; //  <cid, thread>
    
    // static
    static Server_ProcThread s_tProc = null;
    static int s_cid = 1;

    // @lfred: Singleton pattern here.
    static Server_ProcThread getServProcThread () {

        if (s_tProc == null) {
            s_tProc = new Server_ProcThread ();
            Thread x = new Thread (s_tProc);
            x.start ();
        }

        return s_tProc;
    }

    private Server_ProcThread () {
        
        // command queue
        m_cmdQueue = new LinkedBlockingQueue<Server_Command> ();
        
        // user database
        m_userDB = new Server_UserDatabase ();
        
        // system databse
        m_clients  = new Hashtable<Integer, Socket> ();
        m_clntThreadPool = new Hashtable<Integer, Server_ClientWorkerThread> ();
    }

    public void enqueueCmd (Server_Command cmd) {
        try {
            m_cmdQueue.put (cmd);
        } catch (Exception e) {
            System.out.println ("enqueueCmd: Exception - " + e);
            e.printStackTrace ();
        }
    }
    
    int genClientID () {
        s_cid++;
        return s_cid;
    }
    
    boolean addClientSocket (Socket s, int v) {
        m_clients.put (v, s);
        return true;
    }
    
    void handleNewConn (Server_Command sCmd) {
        
        if (sCmd instanceof Server_Command_NewConn) {
            
            Server_Command_NewConn c = (Server_Command_NewConn)sCmd;
            int id = genClientID ();
            addClientSocket (c.getSocket (), id);

            // @lfred: create new client thread,
            Server_ClientWorkerThread cwt = new Server_ClientWorkerThread (id, c.getSocket ());
            m_clntThreadPool.put (id, cwt);
                
            // start the user thread.
            Thread t = new Thread (cwt);
            t.start ();
                
        } else {
            Server.logBug ("incorrect msg @ M_CMD_INCOMING_CONN");
        }
    }
    
    // @lfred: handle the authentication request - also update server side info
    void handleAuthReq (Server_Command sCmd) {
        
        if (sCmd instanceof Server_Command_AuthReq == false) {
            Server.logBug ("Incorrect Command Type");
            return;
        }
        
        boolean isAuth = false, isAllowed = false;
        Server_ClientWorkerThread cwt = m_clntThreadPool.get (sCmd.getMyCid ());
        Server_Command_AuthReq scaq = (Server_Command_AuthReq) sCmd;
        
        InetAddress ip = m_clients.get (sCmd.getMyCid ()).getInetAddress ();        
        String name = scaq.getUserName ();
        String pwd  = scaq.getPasswd ();
        int    cid  = sCmd.getMyCid ();
        
        // 0. Check if logging-in already
        if (m_userDB.nameToCid (name) != 0) {
            Server.log ("already logged-in: " + name);
            Server_Command_StrVec sc = 
                new Server_Command_StrVec (
                    Server_CmdType.M_SERV_CMD_RESP_AUTH_REJ, 
                    sCmd.getMyCid ());
            sc.pushString (name);
            cwt.enqueueCmd (sc);
            Server.logBug ("Logging not allowed");
            return;
        }
        
        // 1. check credentials
        Server.log ("Incoming usr:" + name + ":" + pwd);
        isAuth      = m_userDB.authenticateUsr (name, pwd);
        isAllowed   = m_userDB.isAllowLogin (name, ip);
        
        if (isAuth == true && isAllowed == true) {
            
            // TODO: we should do the accounting here (for login users)
            m_userDB.addCidMapping (name, cid); 
            m_userDB.updateLoginRecord (name, new Date ());
            
            Server_Command sc = 
                new Server_Command (
                    Server_CmdType.M_SERV_CMD_RESP_AUTH_OK, 
                    sCmd.getMyCid ());
                
            cwt.enqueueCmd (sc);
            
            // start to dump offline msg to user
            Vector<Server_UserOfflineMsg> offlineMsg = m_userDB.getAndClearOfflineMsg (name);
            
            if (offlineMsg.size () != 0) {
                
                Server_Command_StrVec cc = 
                    new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_OFFLINE_MSG_IND, sCmd.getMyCid ());
                    
                for (int i=0; i < offlineMsg.size (); ++i) {
                    cc.pushString (offlineMsg.elementAt (i).m_sender);
                    cc.pushString (offlineMsg.elementAt (i).m_msg);
                }
                
                cwt.enqueueCmd (cc);
            }
                    
        } else {
            
            if (isAllowed == false) {
                Server_Command_StrVec sc = 
                    new Server_Command_StrVec (
                        Server_CmdType.M_SERV_CMD_RESP_AUTH_REJ, 
                        sCmd.getMyCid ());
                sc.pushString (name);
                cwt.enqueueCmd (sc);
                Server.logBug ("Logging not allowed");
            } else {
                Server_Command_StrVec sc = 
                    new Server_Command_StrVec (
                        Server_CmdType.M_SERV_CMD_RESP_AUTH_FAIL, 
                        sCmd.getMyCid ());
                sc.pushString (name);
                cwt.enqueueCmd (sc);
                Server.logBug ("Authentication failed");
            }
        }
    }
    
    void handleWhoelseRsp (Server_Command sCmd) {
        
        Server.log ("handleWhoelseRsp");
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.logBug ("Bad Type");
            return;
        }
        
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec)sCmd; 
        Server_ClientWorkerThread wt = m_clntThreadPool.get (sCmd_v.getMyCid ());
        
        if (wt == null) {
            Server.logBug ("WorkerThread not presented @ handleWhoelseRsp");
            return;
        }
        
        Set<String> users = m_userDB.getActiveUsers ();
        Iterator<String> it = users.iterator ();
        
        while (it.hasNext ()) {
            sCmd_v.pushString (it.next ());
        }
        
        wt.enqueueCmd (sCmd_v);
    }
    
    void handleWholasthrRes (Server_Command sCmd) {
        
        Server.log ("handleWhoelseRsp");
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.logBug ("Bad Type");
            return;
        }
        
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec)sCmd; 
        Server_ClientWorkerThread wt = m_clntThreadPool.get (sCmd_v.getMyCid ());
        
        Date now = new Date ();
        now.setTime (now.getTime () - SystemParam.LAST_HOUR * 1000);
        Vector<String> ret = m_userDB.onLineAfterTime (now);
        
        for (int i=0; i<ret.size(); ++i)
            sCmd_v.pushString (ret.elementAt(i));
            
        wt.enqueueCmd (sCmd_v);
    }
    
    void handleBroadcastRsp (Server_Command sCmd) {
        
        Server.log ("handleBroadcastRsp");
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.logBug ("Bad Type @ handleBroadcastRsp");
            return;
        }
     
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec) sCmd;
        Server_Command_StrVec sCmd_v2 = 
            new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_RESP_BROADCAST, sCmd.getMyCid ());
            
        String usr = m_userDB.cidToName (sCmd_v.getMyCid ());
        String msg = sCmd_v.getStringAt (0);
        
        sCmd_v2.pushString (usr);
        sCmd_v2.pushString (msg);
        
        // send event to everybody
        Enumeration <Server_ClientWorkerThread> wtPool = m_clntThreadPool.elements ();
        while (wtPool.hasMoreElements ())
            wtPool.nextElement ().enqueueCmd (sCmd_v2); 
    }
    
    void handleMsgInd (Server_Command sCmd) {
        
        Server.log ("handleMsgInd");
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.logBug ("Bad Type @ handleMsgInd");
            return;
        }
        
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec) sCmd;
        String receiver = sCmd_v.getStringAt (0);
        String sender   = m_userDB.cidToName (sCmd.getMyCid ());
        String msg      = sCmd_v.getStringAt (1);
        
        // 0. check receiver
        if (m_userDB.isValidUser (receiver) == false) {
            Server.log ("No such a user @ handleMsgInd");
            return;
        }
        
        // 1. check if the sender is blocked by the receiver.
        if (m_userDB.isAllowedSender (receiver, sender) == false) {
            
            // you are blocked by the receiver;
            Server_Command_StrVec sc = 
                new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_MSG_REJ_RSP, sCmd.getMyCid());
            sc.pushString (receiver);
            m_clntThreadPool.get (sCmd.getMyCid ()).enqueueCmd (sc);
            
            return;
        }
        
        // 2. check destination user is online or not
        int r_cid = m_userDB.nameToCid (receiver);
        Server_Command_StrVec sc2 = 
            new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_RSP_MSG, sCmd.getMyCid());
        sc2.pushString (sender);
        sc2.pushString (receiver);
        sc2.pushString (msg);
        m_clntThreadPool.get (sCmd.getMyCid ()).enqueueCmd (sc2);
         
        if (r_cid != 0) {
            // rx is online
            m_clntThreadPool.get (r_cid).enqueueCmd (sc2);
            
        } else {
            // rx is offline
            m_userDB.addOfflineMsg (receiver, sender, msg);
        }
    } 
    
    void handleLogout (Server_Command sCmd) {
        
        Server.log ("handleLogout");
        
        int cid = sCmd.getMyCid ();
        Server_ClientWorkerThread cwt = m_clntThreadPool.get (cid);
        
        if (cwt != null)
            cwt.enqueueCmd (sCmd);
        
        // TODO: mark the user as offline here - start to store offline msg since here.
        String name = m_userDB.cidToName (cid);
        
        if (name != null) {
            m_userDB.updateLoginRecord (name, new Date ());
            m_userDB.removeCidMapping (name, cid);
        }
    }
    
    void handleLogoutDone (Server_Command sCmd) {
        // @lfred: client is leaving
        // do stats update
    }
    
    // clean up for NON-LOGIN clients
    void handleForceCleanReq (Server_Command sCmd) {
        
        Server.log ("handleForceCleanReq");
        
        int cid = sCmd.getMyCid ();
        
        // clear the data structure
        Socket s = m_clients.remove (cid);
        Server_ClientWorkerThread cwt = m_clntThreadPool.remove (cid);

        // close socket
        try { s.close (); }
        catch (Exception e) {}
        
        // send final command
        Server_Command sc = new Server_Command (Server_CmdType.M_SERV_CMD_FORCE_CLEAN_IND, cid);
        cwt.enqueueCmd (sc);
    }

    public void run () {

        Server.log ("Server_ProcThread: started");

        // 1. Load user files
        m_userDB.init ();
        
        // 2. start while loop
        while (true) {
            Server_Command sCmd;

            try {
                sCmd = m_cmdQueue.take ();
            } catch (Exception e) {
                Server.logBug ("Dequeue Exception - " + e);
                e.printStackTrace ();
                continue;
            }

            Server.log ("Incoming Server_Command");
            
            switch (sCmd.getServCmd ()) {

                // @lfred: To register the user to the main thread
                case M_SERV_CMD_INCOMING_CONN:
                    handleNewConn (sCmd);
                break;
                
                case M_SERV_CMD_REQ_AUTH:
                    handleAuthReq (sCmd);
                break;

                case M_SERV_CMD_SEND_COMM_OBJ:                    
                break;
                
                case M_SERV_CMD_RESP_WHOELSE:      
                    handleWhoelseRsp (sCmd);               
                break;
                
                case M_SERV_CMD_RESP_BROADCAST:
                    handleBroadcastRsp (sCmd);
                break;
                
                case M_SERV_CMD_IND_MSG:
                    handleMsgInd (sCmd);
                break;
                
                case M_SERV_CMD_REQ_LOGOUT:
                    handleLogout (sCmd);
                break;
                
                case M_SERV_CMD_LOGOUT_DONE:
                    handleLogoutDone (sCmd);
                break;
                
                case M_SERV_CMD_FORCE_CLEAN_REQ:
                    handleForceCleanReq (sCmd);
                break;
                
                case M_SERV_CMD_RESP_WHOELSELASTHR:
                    handleWholasthrRes (sCmd);
                break;
                
                default:
                    System.out.println ("!!! BUG: UNKNOWN MSG !!!");
                break;
            }
        }
    }

}
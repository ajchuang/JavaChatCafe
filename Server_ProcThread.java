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
        m_userDB.setUserLoginAddr (name, ip);
        isAuth      = m_userDB.authenticateUsr (name, pwd);
        isAllowed   = m_userDB.isAllowLogin (name, ip);
        
        Server.log ("Auth: " + isAuth + " isAllowed: " + isAllowed);
        
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
        String user = m_userDB.cidToName (sCmd_v.getMyCid ());
        
        if (wt == null) {
            Server.logBug ("WorkerThread not presented @ handleWhoelseRsp");
            return;
        }
        
        Set<String> users = m_userDB.getActiveUsers ();
        Iterator<String> it = users.iterator ();
        
        while (it.hasNext ()) {
            String n = it.next ();
            
            // @alfred: per request, skipp the current user
            if (user.equals (n) == false)
                sCmd_v.pushString (n);
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
        String currentUsr = m_userDB.cidToName (sCmd_v.getMyCid ()); 
        Server_ClientWorkerThread wt = m_clntThreadPool.get (sCmd_v.getMyCid ());
        Vector<String> returnNames = new Vector<String> (); 
        
        // Step 1. get all current users
        Set<String> users = m_userDB.getActiveUsers ();
        Iterator<String> it = users.iterator ();
        
        while (it.hasNext ()) {
            String n = it.next ();
            
            // @alfred: per request, skipp the current user
            if (currentUsr.equals (n) == false)
                returnNames.add (n);
        }
        
        // Step 2. get all offline users
        Date now = new Date ();
        now.setTime (now.getTime () - SystemParam.LAST_HOUR * 1000);
        Vector<String> ret = m_userDB.onLineAfterTime (now);
        
        for (int i=0; i<ret.size(); ++i) {
            
            String n = ret.elementAt (i);
            // @lfred: per request of the spec, skip the current user.
            if (currentUsr.equals (n) == false && returnNames.contains (n) == false)
                returnNames.add (n);
        }
        
        // Step 3. Put everything inside
        for (int j = 0; j < returnNames.size(); ++j)
            sCmd_v.pushString (returnNames.elementAt (j));
            
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
    
    void handleBlockReq (Server_Command sCmd) {
        
        Server.log ("handleBlockReq");
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.log ("Bad type @ handleBlockReq");
            return;
        }
        
        int cid = sCmd.getMyCid ();
        Server_ClientWorkerThread cwt = m_clntThreadPool.get (cid);
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec) sCmd;
        
        String blocked = sCmd_v.getStringAt (0);
        String user = m_userDB.cidToName (cid);
        
        if (blocked.equals (user) == true) {
            // you dont block yourself.
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_BLOCK_REJ, cid);
            sc.pushString (blocked);
            sc.pushString ("you're trying to block yourself");
            cwt.enqueueCmd (sc);
            return;
        }
        
        if (m_userDB.isValidUser (blocked) == false) {
            // reject the request - user not exist
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_BLOCK_REJ, cid);
            sc.pushString (blocked);
            sc.pushString ("not a valid user");
            cwt.enqueueCmd (sc);
            return;
        }
        
        if (m_userDB.setBlockUser (user, blocked) == true) {
            // ok
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_BLOCK_RSP, cid);
            sc.pushString (blocked);
            cwt.enqueueCmd (sc);
            return;
        } else {
            // failed
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_BLOCK_REJ, cid);
            sc.pushString (blocked);
            sc.pushString ("possible Internal error");
            cwt.enqueueCmd (sc);
            return;
        }
    }
    
    void handleUnblockReq (Server_Command sCmd) {
        
        Server.log ("handleUnblockReq");
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.log ("Bad type @ handleBlockReq");
            return;
        }
        
        int cid = sCmd.getMyCid ();
        Server_ClientWorkerThread cwt = m_clntThreadPool.get (cid);
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec) sCmd;
        
        String unblock = sCmd_v.getStringAt (0);
        String user = m_userDB.cidToName (cid);
        
        if (unblock.equals (user)) {
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_UNBLOCK_REJ, cid);
                sc.pushString (unblock);
                sc.pushString ("you're trying to block yourself");
                cwt.enqueueCmd (sc);
        }
        
        if (m_userDB.isValidUser (unblock) == false) {
            // reject the request - user not exist
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_UNBLOCK_REJ, cid);
            sc.pushString (unblock);
            sc.pushString ("not a valid user");
            cwt.enqueueCmd (sc);
            return;
        }
        
        if (m_userDB.setUnblockUser (user, unblock) == true) {
            // ok
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_UNBLOCK_RSP, cid);
            sc.pushString (unblock);
            cwt.enqueueCmd (sc);
            return;
        } else {
            // failed
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_UNBLOCK_REJ, cid);
            sc.pushString (unblock);
            sc.pushString ("possible Internal errors");
            cwt.enqueueCmd (sc);
            return;
        }
    }
    
    void handleAddUserReq (Server_Command sCmd) {
        
        Server.log ("handleAddUserReq");
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.logBug ("bad type @ handleAddUserReq");
            return;
        }
        
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec) sCmd;
        int cid = sCmd.getMyCid ();
        Server_ClientWorkerThread cwt = m_clntThreadPool.get (cid);
        String newUserName = sCmd_v.getStringAt (0);
        String newPassword = sCmd_v.getStringAt (1);
        String execUser    = m_userDB.cidToName (cid);
        
        if (m_userDB.addUsr (execUser, newUserName, newPassword) == true) {            
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_ADDUSER_RSP, cid);
            sc.pushString (newUserName);
            cwt.enqueueCmd (sc); 
        } else {
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_ADDUSER_REJ, cid);
            sc.pushString (newUserName);
            cwt.enqueueCmd (sc); 
        }
    }
    
    void handleLogoutDone (Server_Command sCmd) {
        // @lfred: client is leaving
        // do stats update
    }
    
    // clean up for NON-LOGIN clients
    void handleForceCleanReq (Server_Command sCmd) {
        
        Server.log ("handleForceCleanReq");
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.log ("bad type @ handleForceCleanReq");
            return;
        }
        
        Server_Command_StrVec v = (Server_Command_StrVec) sCmd;
        
        int cid = sCmd.getMyCid ();
        String name = v.getStringAt (0);
        Date now = new Date ();
        Socket sk = m_clients.get (cid);
        
        // actually bar user
        m_userDB.barUsr (name, now, sk.getInetAddress ());
        
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
    
    void handleChangePwdReq (Server_Command sCmd) {
        
        Server.log ("handleChangePwdReq");
        
        if (sCmd instanceof Server_Command_StrVec == false) {
            Server.logBug ("bad type @ handleChangePwdReq");
            return;
        }
        
        Server_Command_StrVec sCmd_v = (Server_Command_StrVec) sCmd;
        int cid = sCmd.getMyCid ();
        Server_ClientWorkerThread cwt = m_clntThreadPool.get (cid);
        
        String newPassword = sCmd_v.getStringAt (0);
        String execUser    = m_userDB.cidToName (cid);
        
        if (m_userDB.changePwd (execUser, execUser, newPassword) == true) {            
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_CHANGE_PWD_RSP, cid);
            sc.pushString (execUser);
            cwt.enqueueCmd (sc); 
        } else {
            Server_Command_StrVec sc = new Server_Command_StrVec (Server_CmdType.M_SERV_CMD_CHANGE_PWD_REJ, cid);
            sc.pushString (execUser);
            cwt.enqueueCmd (sc); 
        }
    }
    
    void handleSyncReq (Server_Command sCmd) {
        
        int cid = sCmd.getMyCid ();
        Server_ClientWorkerThread cwt = m_clntThreadPool.get (cid);
        String n = m_userDB.cidToName (cid);
        
        if (m_userDB.sync (n) == true) {
            Server_Command sc = new Server_Command (Server_CmdType.M_SERV_CMD_SYNC_RSP, cid);
            cwt.enqueueCmd (sc);
        } else {
            Server_Command sc = new Server_Command (Server_CmdType.M_SERV_CMD_SYNC_REJ, cid);
            cwt.enqueueCmd (sc);
        }
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
                
                case M_SERV_CMD_BLOCK_REQ:
                    handleBlockReq (sCmd);
                break;
                    
                case M_SERV_CMD_UNBLOCK_REQ:
                    handleUnblockReq (sCmd);
                break;
                
                case M_SERV_CMD_ADDUSER_REQ:
                    handleAddUserReq (sCmd);
                break;
                
                case M_SERV_CMD_CHANGE_PWD_REQ:
                    handleChangePwdReq (sCmd);
                break;
                
                case M_SERV_CMD_SYNC_REQ:
                    handleSyncReq (sCmd);
                break;
                
                default:
                    System.out.println ("!!! BUG: UNKNOWN MSG !!!");
                break;
            }
        }
    }

}
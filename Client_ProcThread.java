import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// @lfred: This is the main thread used to execute the client task
public class Client_ProcThread implements Runnable {

    // @lfred: a public command set
    protected final static String m_supportedCmd[] = {
        new String ("whoelse"),
        new String ("wholasthr"),
        new String ("broadcast"),
        new String ("message"),
        new String ("block"),
        new String ("unblock"),
        new String ("logout"),
        new String ("add_user"),
        new String ("change_pass"),
        new String ("sync"),
        
        new String ("m"),
        new String ("b"),
        new String ("lo"),
        new String ("help") };
        
    // @lfred: The singleton trick
    static Client_ProcThread m_procThread = null;
    static Hashtable <String, Client_CmdType> m_supportedCmdTable;
    
    // @lfred: UI components
    Client_LoginWindow m_loginWindow;

    // @lfred: data members
    LinkedBlockingQueue<Client_Command> m_cmdQueue;
    HashSet<String> m_blockedUsers;
    Socket m_socket;
    ObjectOutputStream m_outputStream;
    ObjectInputStream m_inputStream;
    
    public static Client_CmdType isCmdSupported (String cmd) {
        
        Client.log ("user cmd : " + cmd);
        Client_CmdType c = Client_CmdType.E_CMD_INVALID_CMD;
        
        try {
            
            // @lfred: trick here -> You can NOT receive a NULL enum
            Object o = m_supportedCmdTable.get (cmd); 
            
            if (o != null)
                c = (Client_CmdType) o; 
            
        } catch (Exception e) {
            Client.log ("Null pointer");
            return Client_CmdType.E_CMD_INVALID_CMD;
        }
            
        return c;
    }

    private Client_ProcThread (String ip, int port) {

        try {
            m_loginWindow = null;
            
            // @lfred: create socket & I/O
            m_socket = new Socket (ip, port);
            m_outputStream = new ObjectOutputStream (m_socket.getOutputStream ());
            m_inputStream = new ObjectInputStream (m_socket.getInputStream ());
            
            // @lfred: init data structure.
            m_cmdQueue = new LinkedBlockingQueue<Client_Command> ();
            m_supportedCmdTable = new Hashtable <String, Client_CmdType> ();
            m_blockedUsers = new HashSet<String> ();
            
            // @lfred: init data structure
            initCmdTable ();
            
        } catch (Exception e) {
            Client.logBug ("Serious problem - can not start up");
            e.printStackTrace ();
            System.exit (-1);
        }
    }
    
    void initCmdTable () {
        
        // main command
        m_supportedCmdTable.put (m_supportedCmd [0], Client_CmdType.E_CMD_WHOELSE_REQ);
        m_supportedCmdTable.put (m_supportedCmd [1], Client_CmdType.E_CMD_WHOLASTH_REQ);
        m_supportedCmdTable.put (m_supportedCmd [2], Client_CmdType.E_CMD_BROADCAST_REQ);
        m_supportedCmdTable.put (m_supportedCmd [3], Client_CmdType.E_CMD_MESSAGE_REQ);
        m_supportedCmdTable.put (m_supportedCmd [4], Client_CmdType.E_CMD_BLOCK_REQ);
        m_supportedCmdTable.put (m_supportedCmd [5], Client_CmdType.E_CMD_UNBLOCK_REQ);
        m_supportedCmdTable.put (m_supportedCmd [6], Client_CmdType.E_CMD_LOGOUT_REQ);
        m_supportedCmdTable.put (m_supportedCmd [7], Client_CmdType.E_CMD_ADD_USER_REQ);
        m_supportedCmdTable.put (m_supportedCmd [8], Client_CmdType.E_CMD_CHANGE_PASS_REQ);
        m_supportedCmdTable.put (m_supportedCmd [9], Client_CmdType.E_CMD_SYNC_DB_REQ);
        
        
        // shorthand command
        m_supportedCmdTable.put (m_supportedCmd[10], Client_CmdType.E_CMD_MESSAGE_REQ);
        m_supportedCmdTable.put (m_supportedCmd[11], Client_CmdType.E_CMD_BROADCAST_REQ);
        m_supportedCmdTable.put (m_supportedCmd[12], Client_CmdType.E_CMD_LOGOUT_REQ);
        m_supportedCmdTable.put (m_supportedCmd[13], Client_CmdType.E_CMD_HELP_CMD);
    }
    
    public String[] getSupportedCommand () {
        return m_supportedCmd;
    }

    public ObjectInputStream getInputStream () {
        if (m_procThread == null) {
            System.out.println ("!!! BUG: m_procThread is NULL !!!");
        }
        return m_inputStream;
    }

    public static Client_ProcThread initProcThread (String ip, int port) {
        
        if (m_procThread == null) {
            m_procThread = new Client_ProcThread (ip, port);
        }

        return m_procThread;
    }

    public static Client_ProcThread getProcThread () {
        return m_procThread;
    }

    public boolean enqueueCmd (Client_Command cmd) {

        try {
            m_cmdQueue.put (cmd);
        } catch (Exception e) {
            System.out.println ("enqueueCmd: Exception - " + e);
            e.printStackTrace ();
            return false;
        }
        
        return true;
    }

    public void setLoginWindow (Client_LoginWindow clw) {
        m_loginWindow = clw;
    }
    
    void sendToServer (CommObject co) {
        
        try {
            
            m_outputStream.writeObject (co);
            
        } catch (Exception e) {
            Client.logBug ("Connection Failure");
            System.exit (0);
        }
    }
    
    void handleLoginReq (Client_Command cCmd) {
        
        String name = cCmd.getStringAt (0);
        String pass = cCmd.getStringAt (1);
        
        Client.log ("handleLoginReq: " + name + ":" + pass);
        
        CommObject co = new CommObject (CommObjectType.E_COMM_REQ_LOGIN);
        co.pushString (name);
        co.pushString (pass);
        sendToServer (co);
    }
    
    void handleMsgReq (Client_Command cCmd) {
        
        String name = cCmd.getStringAt (0);
        String msg  = cCmd.getStringAt (1);
        
        Client.log ("handleMsgReq: " + name + ":" + msg);
        
        CommObject co = new CommObject (CommObjectType.E_COMM_REQ_MESSAGE);
        co.pushString (name);
        co.pushString (msg);
        sendToServer (co);
    }
    
    void handleBroadcastReq (Client_Command cCmd) {
        
        String msg = cCmd.getStringAt (0);
        Client.log ("handleBroadcastReq: " + msg);
        
        CommObject co = new CommObject (CommObjectType.E_COMM_REQ_BROADCAST);
        co.pushString (msg);
        sendToServer (co);
    }
    
    void handleBlockReq (Client_Command cCmd) {
        
        String user = cCmd.getStringAt (0);
        Client.log ("handleBlockReq: " + user);
        //m_blockedUsers.add (user); 
        //Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        //cWin.displayBlockingInfo (user, true);
        
        CommObject cc = new CommObject (CommObjectType.E_COMM_REQ_BLOCK_USR);
        cc.pushString (user);
        sendToServer (cc);              
    }
    
    void handleUnblockReq (Client_Command cCmd) {
        
        String user = cCmd.getStringAt (0);
        Client.log ("handleUnblockReq: " + user);
        //m_blockedUsers.remove (user);    
        //Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        //cWin.displayBlockingInfo (user, false);
        
        CommObject cc = new CommObject (CommObjectType.E_COMM_REQ_UNBLOCK_USR);
        cc.pushString (user);
        sendToServer (cc);
    }
    
    void handleWhoelseReq (Client_Command cCmd) {
        
        Client.log ("handleWhoelseReq");
        
        CommObject co = new CommObject (CommObjectType.E_COMM_REQ_WHOELSE);
        sendToServer (co);
    }
    
    void handleWholasthrReq (Client_Command cCmd) {
        
        Client.log ("handleWholasthrReq");
        
        CommObject co = new CommObject (CommObjectType.E_COMM_REQ_WHOLASTHR);
        sendToServer (co);
    }
    
    void handleWhoelseRsp (Client_Command cCmd) {
        
        Client.log ("handleWhoelseRsp");
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.incomingUsrList (cCmd.getStringVector (), false);
    }
    
    void handleWholasthr (Client_Command cCmd) {
        Client.log ("handleWholasthr");
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.incomingUsrList (cCmd.getStringVector (), true);
    }
    
    void handleBcastRsp (Client_Command cCmd) {
        
        String usr = cCmd.getStringAt (0);
        String msg = cCmd.getStringAt (1);
        
        Client.log ("handleBcastRsp");
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.incomingMsg (usr, null, msg, true);
    }
    
    void handleMsgRej (Client_Command cCmd) {
        
        String rx = cCmd.getStringAt (0);
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayBlockedInfo (cCmd.getStringAt (0));
    }
    
    void handleOfflineMsgInd (Client_Command cCmd) {
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        
        for (int i=0; i<cCmd.getNumOfStr (); i+=2) {
            String sender = cCmd.getStringAt (i);
            String msg = cCmd.getStringAt (i+1);
            
            cWin.displayOfflineMsg (sender, msg);
        }
    }
    
    void handleMsgRsp (Client_Command cCmd) {
        
        String sender = cCmd.getStringAt (0);
        String receiver = cCmd.getStringAt (1);
        String msg = cCmd.getStringAt (2);
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.incomingMsg (sender, receiver, msg, false);
    }
    
    void handleLogoutReq (Client_Command cCmd) {
        
        Client.log ("Bye-bye");
        
        CommObject co = new CommObject (CommObjectType.E_COMM_REQ_LOGOUT);
        sendToServer (co);
        
        try { m_outputStream.close (); }
        catch (Exception e) {
            Client.log ("output stream is down.");
            e.printStackTrace ();
        }
        
        try { m_inputStream.close (); }
        catch (Exception e) {
            Client.log ("input stream is down.");
            e.printStackTrace ();
        }
        
        try { m_socket.close (); } 
        catch (Exception e) {
            Client.log ("socket is down.");
            e.printStackTrace ();
        }
        
        System.exit (0);
    }
    
    void handleBlockRsp (Client_Command cCmd) {
        
        Client.log ("handleBlockRsp");
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayBlockingInfo (cCmd.getStringAt (0), true);
    }
    
    void handleBlockRej (Client_Command cCmd) {
        
        Client.log ("handleBlockRej");
        
        String u = cCmd.getStringAt (0);
        String reason = cCmd.getStringAt (1);
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayBlockingFailInfo (u, reason, true);
    }
    
    void handleUnblockRsp (Client_Command cCmd) {
        Client.log ("handleUnblockRsp");
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayBlockingInfo (cCmd.getStringAt (0), false);
    }

    void handleUnblockRej (Client_Command cCmd) {
        Client.log ("handleUnblockRej");
        
        String u = cCmd.getStringAt (0);
        String reason = cCmd.getStringAt (1);
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayBlockingFailInfo (u, reason, false);
    }
    
    void handleAddUserReq (Client_Command cCmd) {
        
        String user = cCmd.getStringAt (0);
        String pass = cCmd.getStringAt (1);
        
        Client.log ("handleAddUserReq: " + user + ":" + pass);
        
        CommObject cc = new CommObject (CommObjectType.E_COMM_ADD_USER_REQ);
        cc.pushString (user);
        cc.pushString (pass);
        sendToServer (cc);
    }
    
    void handleAddUserRsp (Client_Command cCmd) {
        
        String newUsr = cCmd.getStringAt (0);
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayAddUserInfo (newUsr, true);
    }
    
    void handleAddUserRej (Client_Command cCmd) {
        
        String newUsr = cCmd.getStringAt (0);
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayAddUserInfo (newUsr, false);
    }
    
    void handleChangePassReq (Client_Command cCmd) {
        
        String pass = cCmd.getStringAt (0);
        Client.log ("handleChangePassReq: " + pass);
        
        CommObject cc = new CommObject (CommObjectType.E_COMM_CHANGE_PASS_REQ);
        cc.pushString (pass);
        sendToServer (cc);
    }
    
    void handleChangePwdRsp (Client_Command cCmd) {
        String user = cCmd.getStringAt (0);
        Client.log ("handleChangePassRsp: " + user);
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayChangePwdInfo (user, true);
    }
    
    void handleChangePwdRej (Client_Command cCmd) {
        String user = cCmd.getStringAt (0);
        Client.log ("handleChangePassRsp: " + user);
        
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayChangePwdInfo (user, false);
    }
    
    void handleSyncReq (Client_Command cCmd) {
        
        Client.log ("handleSyncReq");
        
        CommObject cc = new CommObject (CommObjectType.E_COMM_SYNC_DB_REQ);
        sendToServer (cc);
    }
    
    void handleSyncRsp (Client_Command cCmd) {
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displaySyncInfo (true);
    }
    
    void handleSyncRej (Client_Command cCmd) {
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displaySyncInfo (false);
    }
    
    void handleUpdateUiReq (Client_Command cCmd) {
        Client_ChatWindow cWin = Client_ChatWindow.getChatWindow ();
        cWin.displayLocalInfo (cCmd.getStringAt (0));
    }

    public void run () {
        
        Client.log ("Client_ProcThread starts");
        
        while (true) {
        
            Client_Command cCmd;

            try {
                cCmd = m_cmdQueue.take ();
            } catch (Exception e) {
                Client.log ("Dequeue Exception - " + e);
                e.printStackTrace ();
                continue;
            }

            System.out.println ("Incoming Client_Command: " + cCmd.getCmdType().name());
            
            switch (cCmd.getCmdType ()) {
            
                // @lfred: client init requests
                //--------------------------------------------------------------
                case E_CMD_BLOCK_REQ:
                    handleBlockReq (cCmd);
                break;
                    
                case E_CMD_UNBLOCK_REQ:
                    handleUnblockReq (cCmd);
                break;
                
                case E_CMD_LOGIN_REQ: 
                    handleLoginReq (cCmd);
                break;
                
                case E_CMD_MESSAGE_REQ:
                    handleMsgReq (cCmd);
                break;
                
                case E_CMD_BROADCAST_REQ:
                    handleBroadcastReq (cCmd);
                break;
                
                case E_CMD_WHOELSE_REQ:
                    handleWhoelseReq (cCmd);
                break;
                
                case E_CMD_WHOLASTH_REQ:
                    handleWholasthrReq (cCmd);
                break;
                
                case E_CMD_ADD_USER_REQ:
                    handleAddUserReq (cCmd);
                break;
                
                case E_CMD_CHANGE_PASS_REQ:
                    handleChangePassReq (cCmd);
                break;
                
                case E_CMD_SYNC_DB_REQ:
                    handleSyncReq (cCmd);
                break;
                
                case E_CMD_LOGOUT_REQ:
                    handleLogoutReq (cCmd);
                break;
                
                case E_UPDATE_UI_REQ:
                    handleUpdateUiReq (cCmd);
                break;
                
                //--------------------------------------------------------------
                // @lfred: server responses
                //--------------------------------------------------------------
                case E_CMD_WHOELSE_RSP:
                    handleWhoelseRsp (cCmd);
                break;
                
                case E_CMD_BROADCAST_RSP:
                    handleBcastRsp (cCmd);
                break;
                
                case E_CMD_MESSAGE_RSP:
                    handleMsgRsp (cCmd);
                break;
                
                case E_CMD_MESSAGE_REJ:
                    handleMsgRej (cCmd);
                break;
                
                case E_CMD_OFFLINE_MSG_IND:
                    handleOfflineMsgInd (cCmd);
                break;
                
                case E_CMD_WHOLASTH_RSP:
                    handleWholasthr (cCmd);
                break;
                
                case E_CMD_BLOCK_RSP:
                    handleBlockRsp (cCmd);
                break;
                
                case E_CMD_BLOCK_REJ:
                    handleBlockRej (cCmd);
                break;
                
                case E_CMD_UNBLOCK_RSP:
                    handleUnblockRsp (cCmd);
                break;
                
                case E_CMD_UNBLOCK_REJ:
                    handleUnblockRej (cCmd);
                break;
                
                case E_CMD_ADD_USER_RSP:
                    handleAddUserRsp (cCmd);
                break;
                
                case E_CMD_ADD_USER_REJ:
                    handleAddUserRej (cCmd);
                break;
                
                case E_CMD_CHANGE_PASS_RSP:
                    handleChangePwdRsp (cCmd);
                break;
                
                case E_CMD_CHANGE_PASS_REJ:
                    handleChangePwdRej (cCmd);
                break;
                
                case E_CMD_SYNC_DB_RSP:
                    handleSyncRsp (cCmd);
                break;
                
                case E_CMD_SYNC_DB_REJ:
                    handleSyncRej (cCmd);
                break;
                
                
                //--------------------------------------------------------------
                default:
                     Client.log ("Unhandled client command thus far");
                break;
            }
        }
    }
}

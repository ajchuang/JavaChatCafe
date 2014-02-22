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
        new String ("logout")};
        
    // @lfred: The singleton trick
    static Client_ProcThread m_procThread = null;
    static Hashtable <String, Client_CmdType> m_supportedCmdTable;
    
    // @lfred: UI components
    Client_LoginWindow m_loginWindow;

    // @lfred: data members
    LinkedBlockingQueue<Client_Command> m_cmdQueue;
    Socket m_socket;
    ObjectOutputStream m_outputStream;
    ObjectInputStream m_inputStream;
    
    public static Client_CmdType isCmdSupported (String cmd) {
        
        Client.log ("user cmd : " + cmd);
        Client_CmdType c = Client_CmdType.E_CMD_INVALID_CMD;
        
        try {
            c = m_supportedCmdTable.get (cmd);
        } catch (Exception e) {
            Client.log ("User input: " + cmd);
        }
            
        return c;
    }

    private Client_ProcThread (String ip, int port) {

        try {
            m_loginWindow = null;
            m_socket = new Socket (ip, port);
            
            m_outputStream = new ObjectOutputStream (m_socket.getOutputStream ());
            m_inputStream = new ObjectInputStream (m_socket.getInputStream ());
            m_cmdQueue = new LinkedBlockingQueue<Client_Command> ();
            m_supportedCmdTable = new Hashtable <String, Client_CmdType> ();
            
            initCmdTable ();
            
        } catch (Exception e) {
            Client.logBug ("Serious problem - can not start up");
            e.printStackTrace ();
            System.exit (-1);
        }
    }
    
    void initCmdTable () {
        
        m_supportedCmdTable.put (m_supportedCmd[0], Client_CmdType.E_CMD_WHOELSE_REQ);
        m_supportedCmdTable.put (m_supportedCmd[1], Client_CmdType.E_CMD_WHOLASTH_REQ);
        m_supportedCmdTable.put (m_supportedCmd[2], Client_CmdType.E_CMD_BROADCAST_REQ);
        m_supportedCmdTable.put (m_supportedCmd[3], Client_CmdType.E_CMD_MESSAGE_REQ);
        m_supportedCmdTable.put (m_supportedCmd[4], Client_CmdType.E_CMD_BLOCK_REQ);
        m_supportedCmdTable.put (m_supportedCmd[5], Client_CmdType.E_CMD_UNBLOCK_REQ);
        m_supportedCmdTable.put (m_supportedCmd[6], Client_CmdType.E_CMD_LOGOUT_REQ);
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
        
        Client.log ("handleLoginReq");
        
        String name = cCmd.getStringAt (0);
        String pass = cCmd.getStringAt (1);
        
        CommObject co = new CommObject (CommObjectType.E_COMM_REQ_LOGIN);
        co.pushString (name);
        co.pushString (pass);
        sendToServer (co);
    }
        
    public void run () {
        System.out.println ("Client_ProcThread starts");
        
        while (true) {
        
            Client_Command sCmd;

            try {
                sCmd = m_cmdQueue.take ();
            } catch (Exception e) {
                System.out.println ("Dequeue Exception - " + e);
                e.printStackTrace ();
                continue;
            }

            System.out.println ("Incoming Client_Command");
            
            switch (sCmd.getCmdType ()) {
            
                case E_CMD_BLOCK_REQ:
                case E_CMD_UNBLOCK_REQ: {
                    // @lfred locally handled
                }
                break;
                
                case E_CMD_LOGIN_REQ: 
                    handleLoginReq (sCmd);
                break;

                default:
                     // @lfred: nothing so far
                break;
            }
        }
    }
}

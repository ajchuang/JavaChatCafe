import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// @lfred: This server thread is used to write to the Clients
public class Server_ProcThread implements Runnable {

    // CONST
    private static String M_CONST_USER_DB = new String ("user_pass.txt");
    private static int M_MAX_LOGIN_TRIES = 3;

    // @lfred: all user list
    Hashtable<String, String> m_userList;
    
    
    LinkedBlockingQueue<Server_Command> m_cmdQueue;
    
    // @lfred: Runtime user databases.
    // @lfred: blocking thing need to check spec.
    // Hashtable<String,  Time> m_blockingList;
    
    Hashtable<Integer, String> m_loginClients;
    Hashtable<Integer, Socket> m_clients;
    Hashtable<Integer, ObjectOutputStream> m_bos;
    Hashtable<Integer, Server_ClientWorkerThread> m_clntThreadPool;
    
    // static
    static Server_ProcThread s_tProc = null;
    static int s_cid = 0;

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
        m_cmdQueue = new LinkedBlockingQueue<Server_Command> ();
        m_userList = new Hashtable<String, String> ();
        m_clients  = new Hashtable<Integer, Socket> ();
        m_bos      = new Hashtable<Integer, ObjectOutputStream> ();
        m_clntThreadPool = new Hashtable<Integer, Server_ClientWorkerThread> ();
    }

    private boolean loadUserFile () {
        String line;

        try {
            // read user database
            BufferedReader br =
                new BufferedReader (new FileReader (M_CONST_USER_DB));

            while ((line = br.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer (line);

                String userName = tok.nextToken ();
                String passWord = tok.nextToken ();

                System.out.println ("Legal user: " + userName + ":" +passWord);
                m_userList.put (userName, passWord);
            }
        } catch (Exception e) {
            System.out.println ("Exception: " + e);
            e.printStackTrace ();
            return false;
        }

        return true;
    }
    
    // @lfred: used to authenticate users
    boolean authenticateUser (String name, String pwd) {

        boolean res = false;

        //  Check blacklist
        //  TODO:
        //  Check the user list
        
        if (m_userList.containsKey (name) == true) {    
            String k = (String) m_userList.get (name);
            res = k.equals (pwd);
        }
        
        // Debug message
        if (res == true)
            Server.log ("User: " + name + " authenticated");

        return res;
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
        return s_cid;
    }
    
    boolean addClientSocket (Socket s, int v) {

        ObjectOutputStream bos;
        try {
            bos = new ObjectOutputStream (s.getOutputStream ());
        } catch (Exception e) {
            System.out.println ("Bad Client");
            e.printStackTrace ();
            return false;
        }
        
        m_bos.put (v, bos);
        m_clients.put (v, s);
        return true;
    }
    
    void handleNewConn (Server_Command sCmd) {
        
        if (sCmd instanceof Server_Command_NewConn) {
            
            Server_Command_NewConn c = (Server_Command_NewConn)sCmd;
            int id = genClientID ();
            addClientSocket (c.getSocket (), id);

            // @lfred: create new client thread,
            Server_ClientWorkerThread cwt = new Server_ClientWorkerThread (c.getSocket (), id);
            m_clntThreadPool.put (id, cwt);
                
            // start the user thread.
            Thread t = new Thread (cwt);
            t.start ();
                
        } else {
            Server.logBug ("incorrect msg @ M_CMD_INCOMING_CONN");
        }
    }
    
    void handleSendCommObj (Server_Command sCmd) {
                            
        System.out.println ("Server_Command.M_CMD_SEND_COMM_OBJ");

        try {
            ObjectOutputStream out = m_bos.get (sCmd.getMyCid ());
            out.writeObject (sCmd.getCommObj ());
        } catch (Exception e) {
            System.out.println ("Exception: Server_Command.M_CMD_SEND_COMM_OBJ");
            e.printStackTrace ();
        }    
    }
    
    // @lfred: handle the authentication request - also update server side info
    void handleAuthReq (Server_Command sCmd) {
        
        if (sCmd instanceof Server_Command_AuthReq == false) {
            Server.logBug ("Incorrect Command Type");
            return;
        }
        
        Server_Command_AuthReq scaq = (Server_Command_AuthReq) sCmd;
        boolean res = authenticateUser (scaq.getUserName (), scaq.getPasswd ());
        
        Server_ClientWorkerThread cwt = m_clntThreadPool.get (sCmd.getMyCid ());
        
        if (cwt != null) {
        
            if (res == true) {
                Server_Command sc = new Server_Command (M_SERV_CMD_RESP_AUTH_OK, sCmd.getMyCid ());
                cwt.enqueueCmd (sc);
                
                // TODO: we should do the accounting here (for login users)
                m_loginClients.add (sCmd.getMyCid (), scaq.getUserName ());
                
            } else {
                Server_Command sc = new Server_Command (M_SERV_CMD_RESP_AUTH_FAIL, sCmd.getMyCid ());
                cwt.enqueueCmd (sc);
                
                // TODO: we should do the accounting here (for failed logins)
            }
        } else {
            // @lfred: user diconnects before authentication.
            return;
        }
    }

    public void run () {

        System.out.println ("Server_ProcThread: started");

        // 1. Load user files
        loadUserFile ();
        
        // 2. start while loop
        while (true) {
            Server_Command sCmd;

            try {
                sCmd = m_cmdQueue.take ();
            } catch (Exception e) {
                System.out.println ("Dequeue Exception - " + e);
                e.printStackTrace ();
                continue;
            }

            System.out.println ("Incoming MSG - " + Integer.toString (sCmd.getServCmd ()));
            
            switch (sCmd.getServCmd ()) {

                // @lfred: To register the user to the main thread
                case M_SERV_CMD_INCOMING_CONN:
                    handleNewConn (sCmd);
                break;
                
                case M_SERV_CMD_REQ_AUTH:
                break;

                case M_SERV_CMD_SEND_COMM_OBJ:
                    handleSendCommObj (sCmd);
                break;
                
                case M_CMD_RESP_WHOELSE: {
                    
                }
                break;
                
                default:
                    System.out.println ("!!! BUG: UNKNOWN MSG !!!");
                break;
            }
        }
    }

}
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// @lfred: This server thread is used to write to the Clients
public class Server_ProcThread implements Runnable {

    // CONST
    private static String M_CONST_USER_DB = new String ("user_pass.txt");

    LinkedBlockingQueue<Server_Command> m_cmdQueue;
    Hashtable<String, String> m_userList;
    
    Hashtable<Integer, Socket> m_clients;
    Hashtable<Integer, ObjectOutputStream> m_bos;
    
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
    public boolean authenticateUser (String name, String pwd) {

        boolean res = false;

        //  Check blacklist
        //  TODO:
        //  Check the user list
        synchronized (m_userList) {
            if (m_userList.containsKey (name) == true) {
                String k = (String) m_userList.get (name);
                res = k.equals (pwd);
            }
        }

        // Debug message
        if (res == true)
            System.out.println ("User: " + name + " authenticated");

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
                case Server_Command.M_CMD_INCOMING_CONN: {
                
                    if (sCmd instanceof Server_Command_NewConn) {
                        Server_Command_NewConn c = (Server_Command_NewConn)sCmd;
                        int id = genClientID ();
                        addClientSocket (c.getSocket (), id);

                        // @lfred: create new client thread,
                        Thread t = new Thread (new Server_ClientThread (c.getSocket (), id));
                        t.start ();
                    } else {
                        System.out.println ("!!! incorrect msg @ M_CMD_INCOMING_CONN !!!");
                    }
                }
                break;

                // @lfred: send "string" string - testing purpose
                case Server_Command.M_CMD_SEND_STRING: {

                    if (sCmd instanceof Server_Command_SendString) {
                    
                        try {
                            Server_Command_SendString m = (Server_Command_SendString) sCmd;
                            System.out.println ("SEND MSG: " + m.getMsg ());

                            if (m.getMsgType () == Server_Command_SendString.M_TYPE_SINGLE) {
                                ObjectOutputStream out = m_bos.get (m.getToCid ());
                                out.writeObject (
                                    new CommObject (
                                        CommObject.M_COMM_SEND_STRING,
                                        m.getMsg(),
                                        null));
                                        
                                out.flush();
                            } else {
                                // we only handle the single and non-boxed NOW
                                System.out.println ("@@@ To Implement @@@");
                            }
                        } catch (Exception e) {
                            e.printStackTrace ();
                        }
                    } else {
                        System.out.println ("!!! incorrect msg @ M_CMD_SEND_STRING !!!");
                    }
                }
                break;

                case Server_Command.M_CMD_SEND_COMM_OBJ: {
                    System.out.println ("Server_Command.M_CMD_SEND_COMM_OBJ");

                    try {
                        ObjectOutputStream out = m_bos.get (sCmd.getMyCid ());
                        out.writeObject (sCmd.getCommObj ());
                    } catch (Exception e) {
                        System.out.println ("Exception: Server_Command.M_CMD_SEND_COMM_OBJ");
                        e.printStackTrace ();
                    }
                }
                break;
                
                case Server_Command.M_CMD_SEND_WHOELSE: {
                    
                }
                break;
                
                default:
                    System.out.println ("!!! BUG: UNKNOWN MSG !!!");
                break;
            }
        }
    }

}
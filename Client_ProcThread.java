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
    
    // @lfred: UI components
    Client_LoginWindow m_loginWindow;

    // @lfred: data members
    LinkedBlockingQueue<Client_Command> m_cmdQueue;
    Socket m_socket;
    ObjectOutputStream m_outputStream;
    ObjectInputStream m_inputStream;
    
    public static int isCmdSupported (String cmd) {
    
        for (int i = 0; i < m_supportedCmd.length; ++i) {
            if (cmd.equals (m_supportedCmd[i]) == true)
                return i + 3;
        }
        
        return -1;
    }
    


    private Client_ProcThread (String ip, int port) {

        try {
            m_loginWindow = null;
            m_socket = new Socket (ip, port);
            m_outputStream = new ObjectOutputStream (m_socket.getOutputStream ());
            m_inputStream = new ObjectInputStream (m_socket.getInputStream ());
            m_cmdQueue = new LinkedBlockingQueue<Client_Command> ();
        } catch (Exception e) {
            System.out.println ("Serious problem - can not start up");
            System.exit (-1);
        }
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
    
    protected int translateCCtoCO (int CC) {
    
        switch (CC) {
            case Client_Command.M_CMD_TYPE_LOGIN:
                return CommObject.M_COMM_SEND_LOGIN;
            case Client_Command.M_CMD_TYPE_SEND_WHOELSE:
                return
    public final static int M_CMD_TYPE_SEND_WHOLASTH = 4;
    public final static int M_CMD_TYPE_SEND_BROADCAST = 5;
    public final static int M_CMD_TYPE_SEND_MESSAGE = 6;

    public final static int M_CMD_TYPE_SEND_LOGOUT = 9;
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

            System.out.println ("Incoming Client_Command - ");
            switch (sCmd.getCmdType ()) {
            
                case Client_Command.M_CMD_TYPE_SEND_BLOCK:
                case Client_Command.M_CMD_TYPE_SEND_UNBLOCK: {
                    // @lfred locally handled
                }
                break

                default:
                    int coCmd = translateCCtoCO (sCmd.getCmdType ());
                    CommObject co =
                        new CommObject (
                            coCmd,
                            sCmd.getString (),
                            sCmd.getSubString ());

                    try {
                        m_outputStream.writeObject (co);
                    } catch (Exception e) {
                        // TODO
                        System.out.println ("Serious Exception - to exit");
                    }
                break;
            }
        }
    }
}
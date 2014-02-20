import java.net.*;
import java.io.*;
import java.util.*;

//  Each client has its own state:
//      CONNECTED - not login yet
//      READY - LOGIN
public class Server_ClientReaderThread implements Runnable {

    static final int M_CLIENT_STATE_CONNECTED = 0;
    //static final int M_CLIENT_STATE_WAIT_FOR_LOGIN = 0;
    static final int M_CLIENT_STATE_READY = 0;
    static final int M_CLIENT_STATE_DISCONNECTING = 0;

    // @lfred: command protocol header
    public static int M_MAX_LOGIN_TRY = 3;

    Socket m_clientSocket;

    // @lfred: user data
    ObjectInputStream m_inputStream;
    int m_cid;
    int m_state;
    int m_loginCount;

    Server_ClientReaderThread (Socket s, int id) {

        try {
            m_clientSocket = s;
            m_inputStream = new ObjectInputStream (s.getInputStream ());
            m_cid = id;
            m_state = M_CLIENT_STATE_CONNECTED;

        } catch (Exception e) {
            System.out.println ();
        }
    }
    
    void handleLogin (CommObject co) {
        
        if (m_state == M_CLIENT_STATE_CONNECTED) {
            System.out.println (
                "Incoming login request: " + co.getString () +
                ":" + co.getSubString ());

            // TODO: send authenticate request to procMain
            boolean auth =
                Server_ProcThread.getServProcThread().authenticateUser (
                    co.getString (),
                    co.getSubString ());

            if (auth == false) {
                System.out.println ("### Auth Failed ###");
                Server_Command sc = new Server_Command (M_CMD_RESP_LOGIN_FAIL, m_cid, null);
                Server_ProcThread.getServProcThread().enqueueCmd (sc);
            } else {
                System.out.println ("### Auth Passed ###");
                m_state = M_CLIENT_STATE_READY;
                Server_Command sc = new Server_Command (M_CMD_RESP_LOGIN_FAIL, m_cid, null);
                Server_ProcThread.getServProcThread().enqueueCmd (sc);
            }
        } else {
            System.out.println ("!!! Bug: incorrect state. !!!");
        }
    }
    
    void handleWhoelse (CommObject co) {
        System.out.println ("handleWhoelse");
        Server_Command sc = new Server_Command (M_SERV_CMD_RESP_WHOELSE, m_cid, null);
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
                    
                        case M_COMM_SEND_LOGIN:
                            handleLogin (co);
                        break;
                        
                        case M_COMM_SEND_WHOELSE:
                            handleWhoelse (co);
                        break;
                    }
                } else {
                    System.out.println ("!!! BUG: What are you sending ? !!!");
                }
            }
        } catch (Exception e) {
            System.out.println ("User drop the connection: send disconnect");
            e.printStackTrace ();
            return;
        }
    }
}
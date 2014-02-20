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
    Server_ClientState m_state;
    
    int m_cid;    
    int m_loginCount;

    public Server_ClientReaderThread (Socket s, int id) {

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
        
        if (m_state == SERV_CLNT_STATE_CONNECTED) {
            
            String name = co.getStringAt (0);
            String pass = co.getStringAt (1);
            
            System.out.println (
                "Incoming login request: " + name + ":" + pass);
                
            // @lfred: we should send the server command to the main server processor
            Server_Command_Auth sca = 
                new Server_Command_Auth (
                    M_SERV_CMD_REQ_AUTH, 
                    m_cid, 
                    name, 
                    pass);
                    
            Server_ProcThread.getServProcThread ().enqueueCmd (sca);
            m_state = SERV_CLNT_STATE_AUTHENTICATING;        

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
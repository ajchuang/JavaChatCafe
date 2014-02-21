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
        Server_Command sc = new Server_Command (Server_CmdType.M_SERV_CMD_RESP_WHOELSE, m_cid);
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
                    
                        case E_COMM_REQ_LOGIN:
                            handleLogin (co);
                        break;
                        
                        case E_COMM_REQ_WHOELSE:
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
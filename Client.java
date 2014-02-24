import java.net.*;
import java.io.*;

public class Client {
    
    public static void logBug (String s) {
        System.out.println ("!!! C-Bug: " + s + " !!!");
    }
    
    public static void log (String s) {
        System.out.println ("   C-Info: " + s);
    }
    
    void handleWhoelseRsp (CommObject co) {
        
        Client.log ("handleWhoelseRsp");
        
        Client_Command cCmd = new Client_Command (Client_CmdType.E_CMD_WHOELSE_RSP);
        
        for (int i=0; i<co.getNumOfStrings (); ++i) {
            cCmd.pushString (co.getStringAt (i));
        }
        
        Client_ProcThread.getProcThread ().enqueueCmd (cCmd);
    }
    
    void handleWholasthrRsp (CommObject co) {
        
        Client.log ("handleWholasthrRsp");
        
        Client_Command cCmd = new Client_Command (Client_CmdType.E_CMD_WHOLASTH_RSP);
        
        for (int i=0; i<co.getNumOfStrings (); ++i) {
            cCmd.pushString (co.getStringAt (i));
        }
        
        Client_ProcThread.getProcThread ().enqueueCmd (cCmd);
    }
    
    void handleBroadcastRsp (CommObject co) {
        
        Client.log ("handleBroadcastRsp");
        
        Client_Command cCmd = new Client_Command (Client_CmdType.E_CMD_BROADCAST_RSP);
        cCmd.pushString (co.getStringAt (0));
        cCmd.pushString (co.getStringAt (1));
        Client_ProcThread.getProcThread ().enqueueCmd (cCmd);
    }
    
    void handleMsgRej (CommObject co) {
        
        Client.log ("handleMsgRej");
        
        Client_Command cCmd = new Client_Command (Client_CmdType.E_CMD_MESSAGE_REJ);
        cCmd.pushString (co.getStringAt (0));
        Client_ProcThread.getProcThread ().enqueueCmd (cCmd);
    }
    
    void handleMsgRsp (CommObject co) {
        Client.log ("handleMsgRsp");
        
        Client_Command cc = new Client_Command (Client_CmdType.E_CMD_MESSAGE_RSP);
        
        for (int i=0; i<co.getNumOfStr(); ++i)
            cc.pushString (co.getStringAt (i));
            
        Client_ProcThread.getProcThread ().enqueueCmd (cc);
    }
    
    void handleOfflineMsgInd (CommObject co) {
        Client.log ("handleOfflineMsgInd");
        
        Client_Command cc = new Client_Command (Client_CmdType.E_CMD_OFFLINE_MSG_IND);
        
        for (int i=0; i<co.getNumOfStr(); ++i)
            cc.pushString (co.getStringAt (i));
            
        Client_ProcThread.getProcThread ().enqueueCmd (cc);
    }
    
    void handleLoginRej (CommObject co) {
        // just turn it off.
        System.exit (0);
    }
    
    public void msgHandler (CommObject co, Client_LoginWindow clw) {
        
        switch (co.getOpCode ()) {
                
            case E_COMM_RESP_LOGIN_OK:
                clw.reportLoginStatus (true);
            break;

            case E_COMM_RESP_LOGIN_FAIL:
                clw.reportLoginStatus (false);
            break;
            
            case E_COMM_RESP_LOGIN_REJ:
                handleLoginRej (co);
            break;
            
            case E_COMM_RESP_WHOELSE:
                handleWhoelseRsp (co);
            break;
        
            case E_COMM_RESP_WHOLASTHR:
                handleWholasthrRsp (co);
            break;
        
            case E_COMM_RESP_BROADCAST:
                handleBroadcastRsp (co);
            break;
        
            case E_COMM_IND_MESSAGE:
                handleMsgRsp (co);
            break;
            
            case E_COMM_REJ_MESSAGE:
                handleMsgRej (co);
            break;
            
            case E_COMM_IND_OFFLINE_MSG:
                handleOfflineMsgInd (co);
            break;
            
            default:
                Client.log ("Unhandled msg: " + co.getOpCode ().name ());
            break;
        }
    }
    
    public static void main (String args[]) throws Exception {

        String ip;
        int port;

        // Step 0: extract params
        if (args.length != 2) {
            Client.log ("Usage: java Client [host address] [port number]");
            return;
        }
        
        ip = args[0];
        
        try {
            port = Integer.parseInt (args[1]);
        } catch (Exception e) {
            Client.log ("Port number has to be integer.");
            return;
        }
        
        Client.log ("Connecting to " + ip + ":" + args[1]);

        // @lfred: Before anything starts -
        //         we start the UI input thread and Proc thread
        Client clnt = new Client ();
        Client_LoginWindow clw = new Client_LoginWindow ();
        Client_ProcThread pt = Client_ProcThread.initProcThread (ip, port);
        pt.setLoginWindow (clw);

        Thread t1 = new Thread (pt);
        t1.start ();

        ObjectInputStream in = pt.getInputStream ();

        while (true) {
            try {
                Object o = in.readObject ();

                if (o instanceof CommObject == false) {
                    Client.logBug ("BUG: BAD COMM OBJECT");
                    continue;
                }

                CommObject co = (CommObject) o;
                clnt.msgHandler (co, clw);

            } catch (ConnectException ce) {
                Client.logBug ("Connection refused - Please check server settings");
                in.close ();
                return;
            } catch (SocketException se) {
                Client.logBug ("Socket already closed.");
                return;
            } catch (Exception e) {
                e.printStackTrace ();
                in.close ();
                break;
            }
        }

        //client.close ();
    }
}
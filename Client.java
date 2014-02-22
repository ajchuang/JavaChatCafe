import java.net.*;
import java.io.*;

public class Client {
    
    public static void logBug (String s) {
        System.out.println ("!!! C-Bug: " + s + " !!!");
    }
    
    public static void log (String s) {
        System.out.println ("   C-Info: " + s);
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
                    System.out.println ("BUG: BAD COMM OBJECT");
                    break;
                }

                CommObject co = (CommObject) o;
                System.out.println (co.getStringAt (0));


                switch (co.getOpCode ()) {
                
                    case E_COMM_RESP_LOGIN_OK:
                        System.out.println ("Login - OK");
                        clw.reportLoginStatus (true);
                    break;

                    case E_COMM_RESP_LOGIN_FAIL:
                        System.out.println ("Login - FAIL");
                        clw.reportLoginStatus (false);
                    break;
                }

                //System.out.println ("Prepare to read object done");
            } catch (ConnectException ce) {
                System.out.println
                    ("Connection refused - Please check server settings");
                in.close ();
                break;
            } catch (Exception e) {
                e.printStackTrace ();
                in.close ();
                break;
            }
        }

        //client.close ();
    }
}
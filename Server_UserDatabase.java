import java.util.*;
import java.net.*;

public class Server_UserDatabase {
    
    final static String M_CONST_USER_DB = new String ("user_pass.txt");
    static Server_UserDatabase m_usrDb;
    
    
    // internal data structures
    LinkedList<Server_UserLoginRec>   m_loginRecord;    //  <name, loginOrlogoutTime> --> to process lastHr
    
    Vector<Server_UserObject> m_users;       //  real memory is here
    Hashtable <String, Server_UserObject> m_nameIdx;   //  it's a index: name -> usrObj
    
    Hashtable <Integer, String> m_cidToName;    // index architecture
    Hashtable <String, Integer> m_nameToCid;    // index architecture
    
    
    public static void log (String str) {
        System.log.println ("  [DB] " + str);
    }
    
    public static Server_UserDatabase getUsrDatabse () {
        
        if (m_usrDb == null) {
            m_usrDb = new Server_UserDatabase ();
            if (m_usrDb.init () != true) {
                Server_UserDatabase.log ("Fatal - init DB failed");
                System.exit (0);
            }
        }
        
        return m_usrDb;
    }
    
    // c-tor - sigleton again
    Server_UserDatabase () {
        m_loginRecord = new LinkedList<String, Date> ();
        m_users = new Vector<Server_UserObject> ();
        m_nameIdx = new Hashtable <String, Server_UserObject> ();
        
        m_cidToName = new Hashtable <Integer, String> ();
        m_nameToCid = new Hashtable <String, Integer> ();         
    }
    
    boolean init () {
        
        String line;

        try {
            // read user database
            BufferedReader br =
                new BufferedReader (new FileReader (M_CONST_USER_DB));

            while ((line = br.readLine()) != null) {
                
                StringTokenizer tok = new StringTokenizer (line);

                String userName = tok.nextToken ();
                String passWord = tok.nextToken ();

                Server_UserDatabase.log ("user: " + userName + ":" +passWord);
                
                Server_UserObject uo = new Server_UserObject (userName, passWord);
                
                if (userName.equals ("admin"))
                    uo.setAdmin (true);
                    
                m_users.add (uo);
                m_nameIdx.put (userName, uo);
                
            }
        } catch (Exception e) {
            Server_UserDatabase.log ("init - Exception: " + e);
            e.printStackTrace ();
            return false;
        }

        return true;
    }
    
    public booelan authenticateUsr (String usr, String pwd) {
        
        Server_UserDatabase.log ("authenticateUsr");
        
        Server_UserObject uo = m_nameIdx.get (usr);
        
        if (uo == null) {
            Server_UserDatabase.log ("no such user");
            return false;
        } else {
            return uo.authenticate (pwd);
        } 
    }
    
    // add a new login/logout record to the database
    public void updateLoginRecord (String usr, Date time) {
        Server_UserDatabase.log ("updateLoginRecord");        
        Server_UserLoginRec r = new Server_UserLoginRec (usr, time);
        m_loginRecord.addLast (r);
    }
    
    public Vector<String> onLineBeforeTime (Date t) {
        Server_UserDatabase.log ("onLineBeforeTime");
        
        Vector <String> ans = new Vector <String> ();
        Iterator<Server_UserLoginRec> it = m_loginRecord.descendingIterator ();
        long time = t.getTime ();
        
        while (it.hasNext ()) {
            
            Server_UserLoginRec r = it.next ();
            
            if (r.m_time.getTime () >= t.getTime ()) {
                if (ans.contains (r.m_usr) == false)
                    ans.add (r.m_usr);
            } else
                break;
        } 
        
        return ans;
    }  
    
    //  cid-name mapping
    public boolean addCidMapping (String name, int cid) {
        
        Server_UserDatabase.log ("addCidMapping");
        
        if (m_cidToName.contains (name) == true || m_cidToName.containsKey (cid) == true) {
            Server_UserDatabase.log ("FATAL - DB async");
        }
        
        if (m_nameToCid.contains (cid) == true || m_nameToCid.containsKey (name) == true) {
            Server_UserDatabase.log ("FATAL - DB async");
        }
                
        m_cidToName.put (cid, name);
        m_nameToCid.put (name, cid);
    }
    
    public boolean removeCidMapping (String name, int cid) {
        
        Server_UserDatabase.log ("removeCidMapping");
        m_cidToName.remove (cid);
        m_nameToCid.remove (name);
    }
    
    public String cidToName (int cid) {
        return m_cidToName.get (cid);
    } 
    
    public int nameToCid (String name) {
        return m_nameToCid (name);
    }
    
    // login permission check
    public boolean isAllowLogin (String name, InetAddress ip) {
        
        // check if the user is barred, and the barred address
    }
    
    //  offline msg:
    public boolean addOfflineMsg (String receiver, String sender, String msg) {
        
        Server_UserDatabase.log ("addOfflineMsg");
        Server_UserObject rec = m_nameIdx.get (receiver);
        
        
        if (rec == null) {
            Server_UserDatabase.log ("No such user: " + receiver);
            return false;
        }
            
        Server_UserOfflineMsg m = new Server_UserOfflineMsg (sender, msg);
        rec.sendOffLineMsg (sendser, msg);
        
        return true;
    }
    //      void    clearOfflineMsg ();
    //      Vector<Server_UserOfflineMsg> getOfflineMsg (String name);
    
    //  @lfred: methods
    //  db system processing
    //      boolean init (); // loading DB, and init.
    
    //  auth:
    //      boolean authenticateUsr (String usr, String pwd);
    
    //  last hr processing
    //      void updateLoginRecord (String usr, Date time);
    //      void updateLogoutRecord (String usr, Date time);
    //      Vector<String> onLineBeforeTime (Date);
    
    //  login fail processing
    //      boolean isAllowLogin  (String name, InetAddress);
    //      void    barUsr      (String name, Date time, InetAddress);
    
    //  cid-name mapping
    //      boolean addOnlineUsr (String name, int cid);
    //      boolean removeOnlineUsr (int cid);
    //      String  cidToName (int cid);
    //      int     nameTocid (String name);
    
    //  offline msg:
    //      boolean addOfflineMsg (String name, String msg);
    //      void    clearOfflineMsg ();
    //      Vector<Server_UserOfflineMsg> getOfflineMsg (String name);
    
         
    //  admin: operation
    //      void    changePwd   (String usr, String newPwd);
    //      boolean addUser     (String usr, String pwd);
    //      boolean syncDB      ();
    //      boolean delUser     (String usr); --> difficult (considering current users)
    
}
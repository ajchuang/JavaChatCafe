import java.util.*;
import java.net.*;
import java.io.*;

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
        System.out.println ("  [DB] " + str);
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
        
        m_loginRecord = new LinkedList<Server_UserLoginRec> ();
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

                Server_UserDatabase.log ("user: " + userName + ":" + passWord);
                
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
    
    public boolean isValidUser (String usr) {
        
        if (m_nameIdx.get (usr) == null)
            return false;
        else
            return true;
    }
    
    public boolean authenticateUsr (String usr, String pwd) {
        
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
    
    public Vector<String> onLineAfterTime (Date t) {
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
    public Set<String> getActiveUsers () {
        return m_nameToCid.keySet ();
    }
    
    public boolean addCidMapping (String name, int cid) {
        
        Server_UserDatabase.log ("addCidMapping");
        
        if (m_cidToName.contains (name) == true || m_cidToName.containsKey (cid) == true) {
            Server_UserDatabase.log ("FATAL - DB async");
            return false;
        }
        
        if (m_nameToCid.contains (cid) == true || m_nameToCid.containsKey (name) == true) {
            Server_UserDatabase.log ("FATAL - DB async");
            return false;
        }
                
        m_cidToName.put (cid, name);
        m_nameToCid.put (name, cid);
        return true;
    }
    
    public boolean removeCidMapping (String name, int cid) {
        
        Server_UserDatabase.log ("removeCidMapping");
        m_cidToName.remove (cid);
        m_nameToCid.remove (name);
        return true;
    }
    
    public String cidToName (int cid) {
        return m_cidToName.get (cid);
    } 
    
    public int nameToCid (String name) {
        Integer i = m_nameToCid.get (name);
        
        if (i == null)
            return 0;
        else
            return i.intValue ();
    }
    
    //  offline msg:
    public boolean addOfflineMsg (String receiver, String sender, String msg) {
        
        Server_UserDatabase.log ("addOfflineMsg");
        Server_UserObject rec = m_nameIdx.get (receiver);
        
        if (rec == null) {
            Server_UserDatabase.log ("No such user: " + receiver);
            return false;
        }
            
        rec.sendOffLineMsg (sender, msg);
        
        return true;
    }
    
    public Vector<Server_UserOfflineMsg> getAndClearOfflineMsg (String name) {
        
        Server_UserDatabase.log ("getAndClearOfflineMsg");
        Server_UserObject owner = m_nameIdx.get (name);
        
        if (owner == null) {
            Server_UserDatabase.log ("No such user: " + owner);
            return null;
        }
        
        Server_UserOfflineMsg msg;
        Vector <Server_UserOfflineMsg> ret = new Vector<Server_UserOfflineMsg> ();
        
        while ((msg = owner.takeNextOfflineMsg ()) != null) {
            ret.add (msg);
        }
        
        return ret;
    }
    
    public boolean isAllowedSender (String receiver, String sender) {
        
        //Server_UserObject r = m_nameIdx.get (receiver);
        Server_UserObject s = m_nameIdx.get (sender);
        
        if (s == null) {
            Server_UserDatabase.log ("isAllowedSender: no such a user: " + receiver);
            return false;
        } else
            return s.am_I_blocked_by_usr (receiver);
    }
    
    public boolean setBlockUser (String user, String blocker) {
        
        Server_UserObject owner     = m_nameIdx.get (user);
        Server_UserObject blocked   = m_nameIdx.get (blocker);
        
        if (owner == null || blocked == null) 
            return false;
        else {
            owner.addBlockList (blocker);
            blocked.beingBlockedBy (user);
            return true;
        } 
    }
    
    public boolean setUnblockUser (String user, String unblock) {
        
        Server_UserObject owner     = m_nameIdx.get (user);
        Server_UserObject unblocked = m_nameIdx.get (unblock);
        
        if (owner == null || unblocked == null) 
            return false;
        else {
            owner.removeBlockList (unblock);
            unblocked.removeBlockedBy (user);
            return true;
        }
    }
    
    // login permission check
    public boolean isAllowLogin  (String name, InetAddress ip) {
        
        Server_UserDatabase.log ("isAllowLogin");
        Server_UserObject usr = m_nameIdx.get (name);
        
        if (usr == null) {
            Server_UserDatabase.log ("No such user: " + usr);
            return false;
        }
        
        if (usr.isBarred (ip) == true)
            return false;
        else
            return true;
    }
    
    public boolean setUserLoginAddr (String name, InetAddress ip) {
        Server_UserDatabase.log ("isAllowLogin");
        Server_UserObject usr = m_nameIdx.get (name);
        
        if (usr == null) {
            Server_UserDatabase.log ("No such user: " + usr);
            return false;
        }
        
        usr.setLoginAddr (ip);
        return true;
    }
    
    public void barUsr (String name, Date time, InetAddress ip) {
        
        Server_UserDatabase.log ("barUsr: " + name + " till " + Long.toString (time.getTime()) + " from " + ip.toString ());
        Server_UserObject usr = m_nameIdx.get (name);
        
        if (usr == null) {
            Server_UserDatabase.log ("No such user: " + usr);
            return;
        }
        
        Date t = new Date (time.getTime () + SystemParam.BLOCK_TIME * 1000);
        usr.setBarredTill (t, ip);
    }
    
    // @lfred: advanced feature - 
    public boolean addUsr (String exec_usr, String name, String pwd) {
        
        Server_UserObject exe_usr = m_nameIdx.get (exec_usr);
        
        if (exe_usr == null || exe_usr.isAdmin () == false)
            return false;
        
        // check all consitions    
        if (name == null || name.length () == 0 || m_nameIdx.get (name) != null || pwd == null || pwd.length() == 0)
            return false;
            
        Server_UserObject newUser = new Server_UserObject (name, pwd);
        m_users.add (newUser);
        m_nameIdx.put (name, newUser);
        return true;
    }
    
    // @lfred: advanced feature - change PWD
    public boolean changePwd (String exec_usr, String dest, String newPwd) {
        
        Server_UserObject exe_usr = m_nameIdx.get (exec_usr);
        Server_UserObject dst_usr = m_nameIdx.get (dest);
        
        if (newPwd == null || newPwd.length () == 0) 
            return false;
        
        if (exec_usr == null || dst_usr == null)
            return false;
        
        // allowed to change pwd   
        if (exe_usr.isAdmin () == true || dest.equals (exec_usr) == true)
            dst_usr.newPasswd (newPwd);
        
        return true;
    }
    
    public boolean sync (String exec_user) {
        
        Server_UserObject exe_usr = m_nameIdx.get (exec_user);
        
        if (exe_usr == null || exe_usr.isAdmin () == false)
            return false;
            
        // write to database
        try {
            PrintWriter out
                = new PrintWriter (new BufferedWriter (new FileWriter (M_CONST_USER_DB)));
                
            for (int i=0; i<m_users.size(); ++i) {
                Server_UserObject u = m_users.elementAt (i);
                String s = u.myNameIs () + " " + u.myPassIs ();
                out.println (s);
            }
            
            out.flush ();
            out.close ();
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }
    
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
    //      Vector<Server_UserOfflineMsg> getOfflineMsg (String name);
    //      boolean isAllowedSender (String receiver, String sender);
    
    //  Advanced features
    //  admin: operation
    //      void    changePwd   (String usr, String newPwd);
    //      boolean addUser     (String usr, String pwd);
    //      boolean syncDB      ();
    //      boolean delUser     (String usr); --> difficult (considering current users)
    
}
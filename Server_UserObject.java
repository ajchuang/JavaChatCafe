import java.util.*;
import java.net.*;

public class Server_UserObject {

    String  m_name; // identity
    String  m_pass;
    
    boolean m_isAdmin;

    Date    m_barredSince; //  the start time of block-login
    InetAddress m_loginIp;  //  last login IP (fail or success)
    
    HashSet<String>  m_blockList; // blocking who
    HashSet<String>  m_blockedBy;    // blocked by whom

    LinkedList<Server_UserOfflineMsg>   m_offlineMsgs;
    
    public Server_UserObject (String name, String pwd) {
        
        m_name = new String (name);
        m_pass = new String (pwd);
        m_isAdmin = false;
            
        // init as a very early date
        m_barredSince = new Date (0);  
        
        m_blockList = new HashSet<String> ();
        m_blockedBy = new HashSet<String> ();
        
        m_offlineMsgs = new LinkedList<Server_UserOfflineMsg> ();
        
        Server_UserDatabase.log ("user: " + m_name + " pass: " + m_pass);
    }
    
    public String myNameIs () {
        return m_name;
    } 
    
    public String myPassIs () {
        return m_pass;
    }
    
    public boolean isAdmin () {
        return m_isAdmin;
    }
    
    public void setAdmin (boolean ad) {
        m_isAdmin = ad;
    }
    
    public boolean authenticate (String pwd) {
        
        Server_UserDatabase.log ("authenticate: " + pwd + ":" + m_pass);
        
        if (pwd.equals (m_pass))
            return true;
        else
            return false;
    }
    
    public boolean setBarredTill (Date d, InetAddress ip) {
        
        if (d.after (m_barredSince) == true) {
            m_barredSince.setTime (d.getTime ());
            m_loginIp = ip;
            return true;
        }
        
        return false;
    }
    
    public boolean isBarred (InetAddress ip) {
        Date now = new Date ();
        long diff = now.getTime () - m_barredSince.getTime ();
        
        Server_UserDatabase.log ("isBarred: " + Long.toString (diff));
        
        if (diff < 0 && ip == m_loginIp)
            return true;
        else
            return false;
    }
    
    // block list
    public void addBlockList (String usr) {
        
        if (m_blockList.contains (usr) == false)
            m_blockList.add (usr);
    }
    
    public void removeBlockList (String usr) {
        m_blockList.remove (usr);
    }
    
    public boolean do_I_block_usr (String usr) {
        return m_blockList.contains (usr);
    } 
    
    // blocked by
    public void beingBlockedBy (String usr) {
        
        if (m_blockedBy.contains (usr) == false)
            m_blockedBy.add (usr);   
    }
    
    public void removeBlockedBy (String usr) {
        m_blockedBy.remove (usr);
    }
    
    public boolean am_I_blocked_by_usr (String usr) {
        
        if (m_blockedBy.contains (usr) == true)
            return false;
        else
            return true; 
    }
    
    // set Login IP
    public void setLoginAddr (InetAddress ad) {
        m_loginIp = ad;
    }
    
    public void sendOffLineMsg (String usr, String msg) {
        Server_UserOfflineMsg m = new Server_UserOfflineMsg (usr, msg);
        m_offlineMsgs.addLast (m);    
    }
    
    public Server_UserOfflineMsg takeNextOfflineMsg () {
        
        Server_UserOfflineMsg msg = null;
        
        try {
            msg = m_offlineMsgs.removeFirst ();
        } catch (Exception e) {
            Server_UserDatabase.log ("Empty list");
        }
        
        return msg;
    }
    
    public void newPasswd (String newPwd) {
        m_pass = new String (newPwd);
    }
}
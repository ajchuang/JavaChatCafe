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
        pwd = new String (pwd);
        m_isAdmin = false;
            
        // init as a very early date
        m_barredSince = new Date (0);  
        
        m_blockList = new HashSet<String> ();
        m_blockedBy = new HashSet<String> ();
        
        m_offlineMsgs = new Vector<Server_UserOfflineMsg> ();
    }
    
    public String myNameIs () {
        return m_name;
    } 
    
    public boolean isAdmin () {
        return m_isAdmin;
    }
    
    public void setAdmin (boolean ad) {
        m_isAdmin = ad;
    }
    
    public boolean authenticate (String pwd) {
        
        if (pwd.equals (m_pass))
            return true;
        else
            return false;
    }
    
    public boolean setBarredTill (Date d) {
        
        if (d.after (m_barredSince) == true) {
            m_barredSince.setTime (d.getTime ());
            return true;
        }
        
        return false;
    }
    
    public boolean isBarred () {
        Date now;
        long diff = now.getTime () - m_barredSince.getTime ();
        
        if (diff >= (SystemParam.BLOCK_TIME * 1000))
            return false;
        else
            return true;
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
        return m_blockedBy.contains (usr);
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
}
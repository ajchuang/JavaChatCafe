import java.util.*;
import java.net.*;

public class Server_UserDatabase {

    Hashtable<String, String> m_userPass;       //  <name, pass> --> user name and pass
    Hashtable<String, Date>   m_loginRecord;    //  <name, loginOrlogoutTime> --> to process lastHr
    
    Hashtable <Integer, Server_UserObject> m_cidIdx;    //  it's a index: cid  -> usrObj
    Hashtable <String,  Server_UserObject> m_nameIdx;   //  it's a index: name -> usrObj
    
    LinkedHashSet<Server_UserObject> m_users;       //  real memory is here.
    LinkedHashSet<Server_UserObject> m_onlineUsers; //  another reference set, just for fast retrieval  
    
    
    //  @lfred: methods
    //      boolean isUserOnline (String usr);
    //      boolean authenticateUsr (String usr, String pwd);
    //      boolean setUsrOnline (String usr);
    //      boolean setUsrOffline (String usr);
    //      void sendCommObj (String usr);
    //      void sendCommObjToAll ();
    
    //  admin: operation
    //      void changePwd (String usr, String newPwd);
    //      void kickUsr (String usr);
    //      
    
}
import java.util.*;
import java.net.*;

public class Server_UserObject {

    
    String  m_name; // identity
    
    boolean m_isAdmin;

    Date    m_blockedSince; //  the start time of block-login
    InetAddress m_loginIp;  //  last login IP (fail or success)
    
    Vector<String>  m_blockingList; // blocking who
    Vector<String>  m_blockedBy;    // blocked by whom

    Vector<Server_UserOfflineMsg>   m_offlineMsgs;
    
    // @lfred: system data structures - assigned when online
    int m_cid;  // assigned when connected
    Socket m_socket;  // socket
    Server_ClientWorkerThread m_workerThread;  // the worker
    
    //  @lfred: user methods
    //      setConnected
    //      setDisconnected
    //      setLogingProhibited
    //      addBlockList
    //      beingBlockedBy
    //      sendCommObject
}
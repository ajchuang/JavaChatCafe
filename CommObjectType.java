
public enum CommObjectType {
    
    E_COMM_REQ_LOGIN,           // from client
    E_COMM_RESP_LOGIN_OK,       // back to client
    E_COMM_RESP_LOGIN_FAIL,     // back to client
    E_COMM_RESP_LOGIN_REJ,      // back to client
    
    E_COMM_REQ_WHOELSE,         // from client
    E_COMM_RESP_WHOELSE,        // back to client
    
    E_COMM_REQ_WHOLASTHR,       // from client
    E_COMM_RESP_WHOLASTHR,      // back to client
    
    E_COMM_REQ_MESSAGE,         // to Server
    E_COMM_IND_MESSAGE,         // to Client
    E_COMM_REJ_MESSAGE,         // to Client
    E_COMM_IND_OFFLINE_MSG,     // to Client
    
    E_COMM_REQ_BROADCAST,       // to Server
    E_COMM_RESP_BROADCAST,      // to Client
    
    E_COMM_REQ_BLOCK_USR,       // to Client
    E_COMM_RSP_BLOCK_USR,       // to Client
    E_COMM_REJ_BLOCK_USR,       // to Client
    
    E_COMM_REQ_UNBLOCK_USR,     // to Client
    E_COMM_RSP_UNBLOCK_USR,     // to Client
    E_COMM_REJ_UNBLOCK_USR,     // to Client
    
    E_COMM_ADD_USER_REQ,        // to Server
    E_COMM_ADD_USER_RSP,        // to Client
    E_COMM_ADD_USER_REJ,        // to Client
    
    E_COMM_CHANGE_PASS_REQ,     // to Server
    E_COMM_CHANGE_PASS_RSP,     // to Client
    E_COMM_CHANGE_PASS_REJ,     // to Client
    
    E_COMM_SYNC_DB_REQ,         // to Server
    E_COMM_SYNC_DB_RSP,         // to Client
    E_COMM_SYNC_DB_REJ,         // to Client
    
    E_COMM_REQ_LOGOUT,          // from client
    
    E_COMM_ERROR                // used to indicate error
}
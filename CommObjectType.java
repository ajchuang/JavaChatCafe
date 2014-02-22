
public enum CommObjectType {
    
    E_COMM_REQ_LOGIN,           // from client
    E_COMM_RESP_LOGIN_OK,       // back to client
    E_COMM_RESP_LOGIN_FAIL,     // back to client
    
    E_COMM_REQ_WHOELSE,         // from client
    E_COMM_RESP_WHOELSE,        // back to client
    
    E_COMM_REQ_WHOLASTHR,       // from client
    E_COMM_RESP_WHOLASTHR,      // back to client
    
    E_COMM_REQ_MESSAGE,         // to Server
    E_COMM_IND_MESSAGE,         // to Client
    
    E_COMM_REQ_BROADCAST,       // to Server
    E_COMM_RESP_BROADCAST,      // to Client
    
    E_COMM_ERROR                // used to indicate error
}
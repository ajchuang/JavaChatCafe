public enum CommObjectType {
    E_COMM_REQ_LOGIN,          // from client
    E_COMM_RESP_LOGIN_OK,       // back to client
    E_COMM_RESP_LOGIN_FAIL,     // back to client
    
    E_COMM_REQ_WHOELSE,        // from client
    E_COMM_RESP_WHOELSE,        // back to client
    
    E_COMM_REQ_WHOLASTHR,      // from client
    E_COMM_RESP_WHOLASTHR,      // back to client
    
    E_COMM_REQ_MESSAGE,        // from and back to client
    E_COMM_REQ_BROADCAST       // from and back to client
}
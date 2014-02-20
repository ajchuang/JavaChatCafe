public enum CommObjectType {

    //M_COMM_SEND_STRING    = 0;
    //M_COMM_SEND_COMMAND   = 1;
    
    M_COMM_SEND_LOGIN,          // from client
    M_COMM_RESP_LOGIN_OK,       // back to client
    M_COMM_RESP_LOGIN_FAIL,     // back to client
    
    M_COMM_SEND_WHOELSE,        // from client
    M_COMM_RESP_WHOELSE,        // back to client
    
    M_COMM_SEND_WHOLASTHR,      // from client
    M_COMM_RESP_WHOLASTHR,      // back to client
    
    M_COMM_SEND_MESSAGE,        // from and back to client
    M_COMM_SEND_BROADCAST       // from and back to client
}
 
 public enum Server_CmdType {
     
    M_SERV_CMD_INCOMING_CONN,       // incoming connection
    M_SERV_CMD_SEND_COMM_OBJ,       // test purpose
    M_SERV_CMD_FORCE_CLEAN_IND,     // force to clear a certain client
    M_SERV_CMD_FORCE_CLEAN_REQ,     // force to clear a certain client
    
    M_SERV_CMD_REQ_AUTH,
    M_SERV_CMD_RESP_AUTH_OK,
    M_SERV_CMD_RESP_AUTH_FAIL,
    M_SERV_CMD_RESP_AUTH_REJ,
    
    M_SERV_CMD_RESP_WHOELSE,
    M_SERV_CMD_RESP_WHOELSELASTHR,
    
    M_SERV_CMD_IND_MSG,
    M_SERV_CMD_RSP_MSG,
    M_SERV_CMD_MSG_REJ_RSP,
    M_SERV_CMD_OFFLINE_MSG_IND,
    
    M_SERV_CMD_RESP_BROADCAST,
    
    M_SERV_CMD_BLOCK_REQ,
    M_SERV_CMD_BLOCK_RSP,
    M_SERV_CMD_BLOCK_REJ,
    
    M_SERV_CMD_UNBLOCK_REQ,
    M_SERV_CMD_UNBLOCK_RSP,
    M_SERV_CMD_UNBLOCK_REJ,
    
    M_SERV_CMD_ADDUSER_REQ,
    M_SERV_CMD_ADDUSER_RSP,
    M_SERV_CMD_ADDUSER_REJ,
    
    M_SERV_CMD_CHANGE_PWD_REQ,
    M_SERV_CMD_CHANGE_PWD_RSP,
    M_SERV_CMD_CHANGE_PWD_REJ,
    
    M_SERV_CMD_SYNC_REQ,
    M_SERV_CMD_SYNC_RSP,
    M_SERV_CMD_SYNC_REJ,
    
    M_SERV_CMD_CLNT_DOWN,           // used in some case that the user is dead or not responding.
    
    M_SERV_CMD_REQ_LOGOUT,
    M_SERV_CMD_LOGOUT_DONE
}
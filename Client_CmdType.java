
public enum Client_CmdType {

    E_CMD_LOGIN_REQ,
    
    E_CMD_WHOELSE_REQ,
    E_CMD_WHOELSE_RSP,
    
    E_CMD_WHOLASTH_REQ,
    E_CMD_WHOLASTH_RSP,
    
    E_CMD_BROADCAST_REQ,
    E_CMD_BROADCAST_RSP,
    
    E_CMD_MESSAGE_REQ,
    E_CMD_MESSAGE_RSP,
    E_CMD_MESSAGE_REJ,
    E_CMD_OFFLINE_MSG_IND,
    
    E_CMD_LOGOUT_REQ,

    E_CMD_BLOCK_REQ,
    E_CMD_BLOCK_RSP,
    E_CMD_BLOCK_REJ,
    
    E_CMD_UNBLOCK_REQ,
    E_CMD_UNBLOCK_RSP,
    E_CMD_UNBLOCK_REJ,
    
    // @lfred: admin feature
    E_CMD_ADD_USER_REQ,
    E_CMD_ADD_USER_RSP,
    E_CMD_ADD_USER_REJ,
    
    // @lfred: admin feature
    E_CMD_NEW_PWD_REQ,
    E_CMD_NEW_PWD_RSP,
    E_CMD_NEW_PWD_REJ,
    
    E_CMD_NEW_PASS_REQ,
    E_CMD_NEW_PASS_RSP,
    E_CMD_NEW_PASS_REJ,
    
    E_CMD_HELP_CMD,
    E_CMD_INVALID_CMD
}
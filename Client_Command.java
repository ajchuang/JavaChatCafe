

public class Client_Command {

    public final static int M_CMD_TYPE_LOGIN    = 1;
    //public final static int M_CMD_TYPE_SEND_STR = 2;
    public final static int M_CMD_TYPE_SEND_WHOELSE = 3;
    public final static int M_CMD_TYPE_SEND_WHOLASTH = 4;
    public final static int M_CMD_TYPE_SEND_BROADCAST = 5;
    public final static int M_CMD_TYPE_SEND_MESSAGE = 6;
    public final static int M_CMD_TYPE_SEND_BLOCK = 7;
    public final static int M_CMD_TYPE_SEND_UNBLOCK = 8;
    public final static int M_CMD_TYPE_SEND_LOGOUT = 9;
    

    int m_cmdType;
    String m_str;
    String m_subStr;

    Client_Command (int cmd, String str, String subStr) {
        m_cmdType = cmd;
        m_str = str;
        m_subStr = subStr;
    }
    
    int getCmdType () {
        return m_cmdType;
    }

    String getString () {
        return m_str;
    }

    String getSubString () {
        return m_subStr;
    }
}
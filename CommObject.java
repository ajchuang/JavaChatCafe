import java.io.*;
import java.util.*;

// @lfred: The object sends to and from SERVER and CLIENT.
class CommObject implements Serializable {

    public static final int M_COMM_SEND_STRING    = 0;
    public static final int M_COMM_SEND_COMMAND   = 1;
    public static final int M_COMM_SEND_LOGIN     = 2;
    public static final int M_COMM_RES_LOGIN_OK   = 3;
    public static final int M_COMM_RES_LOGIN_FAIL = 4;
    public static final int M_COMM_SEND_WHOELSE   = 5;

    int m_opCode;
    String m_str;
    String m_subStr;


    CommObject (int opCode, String str, String subStr) {
        m_opCode = opCode;
        m_str = str;
        m_subStr = subStr;
    }
    
    int getOpCode () { return m_opCode; }
    String getString () { return m_str; }
    String getSubString () { return m_subStr; }
}
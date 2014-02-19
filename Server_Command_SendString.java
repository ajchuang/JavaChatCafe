
// @lfred: a general class used to send string to client
public class Server_Command_SendString extends Server_Command {

    public static final int M_TYPE_BROADCAST = 2;
    public static final int M_TYPE_SINGLE = 4;

    String  m_string;
    int     m_toCid;
    int     m_msgType;
    boolean m_needStored;
    boolean m_needNewLine;

    public Server_Command_SendString (
        int myCid,
        String str,
        int toCid,
        int msgType,
        boolean needStored,
        boolean needNewLine) {

        super (Server_Command.M_CMD_SEND_STRING, myCid, null);
        m_string = str;
        m_toCid = toCid;
        m_msgType = msgType;
        m_needStored = needStored;
        m_needNewLine = needNewLine;
    }

    String getMsg () { return m_string; }
    int getToCid () { return m_toCid; }
    int getMsgType () { return m_msgType; }
    
    boolean getNeedStored () { return m_needStored; }
    boolean getNewLineNeeded () { return m_needNewLine; }
}
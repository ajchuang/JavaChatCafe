public class Server_Command {

    // Constants
    public static final int M_CMD_INCOMING_CONN = 0;
    public static final int M_CMD_SEND_COMM_OBJ = 1;
    public static final int M_CMD_SEND_STRING   = 2;

    // protected
    protected int   m_cmd;
    protected int   m_cid;
    protected CommObject m_co;

    Server_Command (int cmd, int cid, CommObject co) {
        m_cmd = cmd;
        m_cid = cid;
        m_co = co;
    }

    int getServCmd () {
        return m_cmd;
    }

    int getMyCid () {
        return m_cid;
    }

    CommObject getCommObj () {
        return m_co;
    }
}
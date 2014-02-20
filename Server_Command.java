public class Server_Command {

    // protected
    protected Server_CmdType m_cmd;
    protected int   m_cid;
    protected CommObject m_co;

    Server_Command (Server_CmdType cmd, int cid, CommObject co) {
        m_cmd = cmd;
        m_cid = cid;
        m_co = co;
    }

    Server_CmdType getServCmd () {
        return m_cmd;
    }

    int getMyCid () {
        return m_cid;
    }

    CommObject getCommObj () {
        return m_co;
    }
}
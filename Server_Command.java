public class Server_Command {

    // protected
    protected Server_CmdType m_cmd;
    protected int   m_cid;
    
    Server_Command (Server_CmdType cmd, int cid) {
        m_cmd = cmd;
        m_cid = cid;
    }

    Server_CmdType getServCmd () {
        return m_cmd;
    }

    int getMyCid () {
        return m_cid;
    }
}
public class Server_Command_AuthReq extends Server_Command {

    String m_userName;
    String m_passwd;

    public Server_Command_AuthReq (int cid, String user, String pwd) {
        super (Server_CmdType.M_SERV_CMD_REQ_AUTH, cid);
        m_userName = user;
        m_passwd = pwd;
    }
    
    public String getUserName () {
        return m_userName;
    }


    public String getPasswd () {
        return m_passwd;
    }
}
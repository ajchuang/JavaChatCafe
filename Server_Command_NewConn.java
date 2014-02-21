import java.net.*;

public class Server_Command_NewConn extends Server_Command {

    // protected
    Socket m_skt;

    Server_Command_NewConn (Socket skt) {
        super (Server_CmdType.M_SERV_CMD_INCOMING_CONN, 0);
        m_skt = skt;
    }

    Server_CmdType getServCmd () {
        return m_cmd;
    }
    
    Socket getSocket () {
        return m_skt;
    }
}
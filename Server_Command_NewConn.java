import java.net.*;
import java.io.*;
import java.util.*;

public class Server_Command_NewConn extends Server_Command {

    // protected
    Socket m_skt;

    Server_Command_NewConn (int cmd, Socket skt) {
        super (cmd, 0, null);
        m_skt = skt;
    }

    int getServCmd () {
        return m_cmd;
    }
    
    Socket getSocket () {
        return m_skt;
    }
}
import java.util.*;

public class Server_UserLoginRec {
    public String m_usr;
    public Date m_time;
    
    public Server_UserLoginRec (String u, Date t) {
        m_usr = new String (u);
        m_time = new Date (t.getTime ());
    }
}
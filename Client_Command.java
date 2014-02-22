import java.util.*;

public class Client_Command {

    Client_CmdType m_cmdType;
    Vector <String> m_strVec;
    
    public Client_Command (Client_CmdType cmd) {
        m_cmdType = cmd;
        m_strVec = new Vector <String> ();
    }
    
    public Client_Command (Client_CmdType cmd, String str, String subStr) {
        m_cmdType = cmd;
        m_strVec = new Vector <String> ();
        m_strVec.add (0, str);
        m_strVec.add (1, subStr);
    }
    
    public Client_CmdType getCmdType () {
        return m_cmdType;
    }
    
    // @lfred: should we do this ?
    public Vector<String> getStringVector () {
        return m_strVec;
    }
    
    public String getStringAt (int i) {
        
        String r = null;
        
        try {
            r = m_strVec.get (i);
        } catch (Exception e) {
            Client.logBug ("getStringAt error");
        }
        
        return r;
    }

    public void pushString (String s) {
        m_strVec.add (s);
    }
    
}
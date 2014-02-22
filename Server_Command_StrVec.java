import java.util.*;

public class Server_Command_StrVec extends Server_Command {
    
    // protected
    Vector<String> m_strVec;
    
    Server_Command_StrVec (Server_CmdType cmd, int cid) {
        super (cmd, cid);
        m_strVec = new Vector<String> ();
    }

    void pushString (String s) {
        m_strVec.add (s);
    } 
    
    int getStrCount () {
        return m_strVec.size ();
    }
    
    String getStringAt (int idx) {
        
        String s = null;
        
        try {
            s = m_strVec.elementAt (idx);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
        
        return s;
    }
}
import java.io.*;
import java.util.*;

// @lfred: The object sends to and from SERVER and CLIENT.
class CommObject implements Serializable {
        
    CommObjectType m_opCode;
    Vector<String> m_strVec;
    
    CommObject (CommObjectType opCode) {
        m_opCode = opCode;
        m_strVec = new Vector<String> ();        
    }
    
    CommObjectType getOpCode () { 
        return m_opCode; 
    }
    
    int getNumOfStr () { 
        return m_strVec.size (); 
    }
    
    void pushString (String str) {
        m_strVec.add (str);
    }
    
    void setStringAt (int idx, String str) {
        m_strVec.add (idx, str);
    }
    
    int getNumOfStrings () {
        return m_strVec.size ();
    }
    
    String getStringAt (int idx) {
        
        String s = null;
        
        try { s = m_strVec.elementAt (idx); } 
        catch (Exception e) {
            Server.logBug ("!!! You got no such string !!!");
        }
        
        return s;
    }
}
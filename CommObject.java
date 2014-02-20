import java.io.*;
import java.util.*;

// @lfred: The object sends to and from SERVER and CLIENT.
class CommObject implements Serializable {

    
    CommObjectType m_opCode;
    
    Vector<String> m_strVec;
    String m_str;
    String m_subStr;


    CommObject (CommObjectType opCode, String str, String subStr) {
        m_opCode = opCode;
        m_strVec = new Vector<String> ();
        
        if (str != null)
            m_strVec.add (str);
        
        if (subStr != null)
            m_strVec.add (str);
    }
    
    CommObjectType getOpCode () { return m_opCode; }
    
    int getNumOfStr () { return m_strVec.size (); }
    
    void setStringAt (int idx, String str) {
        m_strVec.add (idx, str);
    }
    
    String getStringAt (int idx) {
        
        String s = null;
        
        try { s = m_strVec.elementAt (idx); } 
        catch (Exception e) {
            System.out.println ("!!! You got no such string !!!");
        }
        
        return s;
    }
}
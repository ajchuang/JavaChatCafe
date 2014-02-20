import java.io.*;
import java.util.*;

// @lfred: The object sends to and from SERVER and CLIENT.
class CommObject implements Serializable {

    public static final int M_COMM_SEND_STRING    = 0;
    public static final int M_COMM_SEND_COMMAND   = 1;
    
    public static final int M_COMM_SEND_LOGIN     = 2;
    public static final int M_COMM_RES_LOGIN_OK   = 3;
    public static final int M_COMM_RES_LOGIN_FAIL = 4;
    
    public static final int M_COMM_SEND_WHOELSE   = 5;
    public static final int M_COMM_RES_WHOELSE    = 6;
    
    public static final int M_COMM_SEND_MESSAGE   = 7;
    public static final int M_COMM_SEND_BROADCAST = 8 ;

    int m_opCode;
    
    Vector<String> m_strVec;
    String m_str;
    String m_subStr;


    CommObject (int opCode, String str, String subStr) {
        m_opCode = opCode;
        m_strVec = new Vector<String> ();
        
        if (str != null)
            m_strVec.add (str);
        
        if (subStr != null)
            m_strVec.add (str);
    }
    
    int getOpCode () { return m_opCode; }
    int getNumOfStr () { return m_strVec.size (); }
    
    String getString () {        
        return getStringAt (0);  
    }
    
    String getSubString () { 
        
        return getStringAt (1);  
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
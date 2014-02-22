import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Client_ChatWindow extends JFrame implements ActionListener {

    // @lfred: well, again this is a singleton
    final static String NEW_LINE = new String ("\n");
    static Client_ChatWindow mp_chatWin;

    // @lfred: UI components
    JTextArea   m_chatBoard;
    JTextField  m_cmdText;
    JButton     m_sendBtn;
    JButton     m_logoutBtn;
    
    // @lfred: idle timer
    javax.swing.Timer m_idleTimer;
    
    public static Client_ChatWindow getChatWindow () {
        
        if (mp_chatWin == null) {
            mp_chatWin = new Client_ChatWindow ();
        }
        
        return mp_chatWin;
    }

    private Client_ChatWindow () {
        
        setLayout (new GridBagLayout ());

        m_chatBoard = new JTextArea (10, 46);
        m_chatBoard.setEditable (false);
        JScrollPane jp = new JScrollPane (m_chatBoard);
        GridBagConstraints c1 = new GridBagConstraints ();
        c1.gridx = 0;
        c1.gridy = 0;
        c1.gridwidth = 20;
        c1.gridheight = 10;
        c1.fill = GridBagConstraints.NONE;
        c1.anchor = GridBagConstraints.CENTER;
        add (jp, c1);

        m_cmdText = new JTextField (33);
        GridBagConstraints c2 = new GridBagConstraints ();
        c2.gridx = 0;
        c2.gridy = 11;
        c2.gridwidth = 16;
        c2.gridheight = 1;
        c2.fill = GridBagConstraints.NONE;
        c2.anchor = GridBagConstraints.CENTER;
        add (m_cmdText, c2);

        m_sendBtn = new JButton (new String ("Send"));
        m_sendBtn.addActionListener (this);
        GridBagConstraints c3 = new GridBagConstraints ();
        c3.gridx = 16;
        c3.gridy = 11;
        c3.gridwidth = 2;
        c3.gridheight = 1;
        c3.fill = GridBagConstraints.NONE;
        c3.anchor = GridBagConstraints.CENTER;
        add (m_sendBtn, c3);

        m_logoutBtn = new JButton (new String ("Quit"));
        m_logoutBtn.addActionListener (this);
        GridBagConstraints c4 = new GridBagConstraints ();
        c4.gridx = 18;
        c4.gridy = 11;
        c4.gridwidth = 2;
        c4.gridheight = 1;
        c4.fill = GridBagConstraints.NONE;
        c4.anchor = GridBagConstraints.CENTER;
        add (m_logoutBtn, c4);

        pack ();
        setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
        setVisible (true);
        
        // @lfred: idle timer
        m_idleTimer = new javax.swing.Timer (SystemParam.TIME_OUT * 1000, this);
        m_idleTimer.setRepeats (false);
        m_idleTimer.start(); 
    }
    
    private Client_Command parsingCommand (String str) {

        StringTokenizer strToken = new StringTokenizer (str);
        
        // empty string
        if (strToken.hasMoreElements () == false)
            return null;
        
        String cmd = strToken.nextToken ();
        Client_CmdType cmdIdx;
        Client_Command cc = null;
        String p1 = null, p2 = null;
        
        if (cmd == null)
            return null;
        
        cmdIdx = Client_ProcThread.isCmdSupported (cmd);
        
        if (cmdIdx == Client_CmdType.E_CMD_INVALID_CMD)
            return null;
        
        switch (cmdIdx) {
            // @lfred: commands without params
            case E_CMD_WHOELSE_REQ:
            case E_CMD_WHOLASTH_REQ:
            case E_CMD_LOGOUT_REQ:
                cc = new Client_Command (cmdIdx);
            break;

            // @lfred: commands with 1 param
            case E_CMD_BLOCK_REQ:
            case E_CMD_UNBLOCK_REQ:
            case E_CMD_BROADCAST_REQ: {
                
                if (strToken.hasMoreElements ()) {
                    
                    p1 = strToken.nextToken ();
                    
                    if (p1 != null) {
                        cc = new Client_Command (cmdIdx);
                        cc.pushString (p1);
                    } else {
                        Client.logBug ("Empty P1 - incorrect format");
                        return null;
                    }
                } else {
                    Client.logBug ("Incorrect User Command");
                    return null;
                }
            } 
            break;

            // @lfred: commands with 2 params
            case E_CMD_MESSAGE_REQ: {

                if (strToken.hasMoreElements ()) {
                    
                    p1 = strToken.nextToken ();
                    
                    if (p1 != null)
                        p2 = str.substring (str.indexOf (p1) + p1.length());
                    else {
                        Client.logBug ("Empty P1");
                        return null;
                    }
                    
                    if (p1 != null && p2 != null)
                        cc = new Client_Command (cmdIdx, p1, p2);                                        
                    else {
                        Client.logBug ("Empty P2");
                        return null;
                    }
                        
                } else {
                    Client.logBug ("!!! Incorrect User Command !!!");
                    return null;
                }
            } break;
        }
    
        return cc;
    }
    
    void doLogout () {
        
        m_chatBoard.append ("*System Info* Logging out" + NEW_LINE);
        Client_Command cmd = new Client_Command (Client_CmdType.E_CMD_LOGOUT_REQ);
        Client_ProcThread.getProcThread ().enqueueCmd (cmd);
    }

    public void actionPerformed (ActionEvent e) {
    
        if (e.getSource () == m_sendBtn) {
            
            String t;
            
            try {
                t = m_cmdText.getText ();
            } catch (Exception ee) {
                // @lfred: empty string - just return;
                return;
            }
            
            Client_Command cc = parsingCommand (t);
            
            if (cc != null) {
                Client_ProcThread.getProcThread ().enqueueCmd (cc);
                m_cmdText.setText (null);
                
                // @lfred: restart idle timer
                m_idleTimer.restart ();
            } else
                Client.log ("Incorrect Command Format");
            
            // @lfred: clear the text anyway            
            m_cmdText.setText (null);
            
        } else if (e.getSource () == m_logoutBtn) {
            
            Client.log ("Logout Btn triggered");
            
            // @lfred: TODO - send logout command
            doLogout ();
            
        } else if (e.getSource () == m_idleTimer) {
            
            Client.log ("idle timer time-out");
            
            // @lfred: do log-out here.
            doLogout ();
            
        } else {
            Client.logBug ("Bad event");
            Thread.dumpStack ();
        }
    }
    
    // @lfred:  the function is used to receive the server commands and 
    //          display info on the textArea 
    public void receiveEvent (CommObject cObj) {
        
        System.out.println ("receiveEvent - updateing UI.");
         
        switch (cObj.getOpCode ()) {
            
            case E_COMM_REQ_MESSAGE:
                // update UI: when somebody sends a message to you.
            break;
            
            case E_COMM_REQ_BROADCAST:
                // update UI: when somebody sends a broadcast
            break;
            
            case E_COMM_RESP_WHOELSE:
                // receive the response of 'whoelse'
            break;
        }
    }
    
    public void incomingMsg (String usr, String msg, boolean isBroadcast) {
        
        String output;
        
        if (isBroadcast)
            output = usr + " is broadcasting" + ": " + msg + NEW_LINE;
        else
            output = usr + ": " + msg + NEW_LINE;
            
        m_chatBoard.append (output);
    }
    
    public void incomingUsrList (Vector<String> vStr, boolean isLastHr) {
        
        String subStr = Integer.toString (vStr.size ()) + NEW_LINE;
        
        if (isLastHr == true)
            m_chatBoard.append ("Total users last hour: " + subStr);
        else
            m_chatBoard.append ("Total active users: " + subStr);
        
        for (int i=0; i<vStr.size(); ++i)
            m_chatBoard.append (vStr.elementAt (i) + NEW_LINE);
    }
    
    public void displayBlockingInfo (String usr, boolean isBlocked) {
        
        if (isBlocked == true) 
            m_chatBoard.append ("* System Info* You blocked " + usr + NEW_LINE);
        else
            m_chatBoard.append ("* System Info* You unblocked " + usr + NEW_LINE);
    }
}


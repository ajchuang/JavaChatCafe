import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Client_ChatWindow extends JFrame implements ActionListener {

    // @lfred: UI components
    JTextArea   m_chatBoard;
    JTextField  m_cmdText;
    JButton     m_sendBtn;
    JButton     m_logoutBtn;

    public Client_ChatWindow () {
        setLayout (new GridBagLayout ());

        m_chatBoard = new JTextArea (10, 45);
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
    }
    
    private Client_Command parsingCommand (String str) {

        StringTokenizer strToken = new StringTokenizer (str);
        String cmd = strToken.nextToken ();
        int cmdIdx;
        Client_Command cc = null;
        String p1 = null, p2 = null;
        
        if (cmd != null) {
            if ((cmdIdx = Client_ProcThread.isCmdSupported (cmd)) != -1) {
            
                switch (cmdIdx) {
                    // @lfred: commands without params
                    case Client_Command.M_CMD_TYPE_SEND_WHOELSE:
                    case Client_Command.M_CMD_TYPE_SEND_WHOLASTH:
                    case Client_Command.M_CMD_TYPE_SEND_LOGOUT:
                        cc = new Client_Command (cmdIdx, null, null);
                    break;

                    // @lfred: commands with 1 param
                    case Client_Command.M_CMD_TYPE_SEND_BLOCK:
                    case Client_Command.M_CMD_TYPE_SEND_UNBLOCK:
                    case Client_Command.M_CMD_TYPE_SEND_BROADCAST:
                        if (strToken.hasMoreElements ()) {
                            p1 = strToken.nextToken ();
                            cc = new Client_Command (cmdIdx, p1, null);
                        } else {
                            System.out.println ("!!! Incorrect User Command !!!");
                        }
                    break;

                    // @lfred: commands with 2 params
                    case Client_Command.M_CMD_TYPE_SEND_MESSAGE:

                        if (strToken.hasMoreElements ()) {
                            p1 = strToken.nextToken ();
                            
                            // @lfred: POTENTIAL BUG HERE
                            p2 = str.substring (str.indexOf (p1) + p1.length());
                            cc = new Client_Command (cmdIdx, p1, p2);
                        } else {
                            System.out.println ("!!! Incorrect User Command !!!");
                        }
                    break;

                    default:
                        System.out.println ("!!! Incorrect User Command !!!");
                    break;
                }
            }
        }
        
        return cc;
    }

    public void actionPerformed (ActionEvent e) {
    
        if (e.getSource () == m_sendBtn) {
            Client_Command cc = parsingCommand (m_cmdText.getText ());
            
            if (cc != null) {
                Client_ProcThread.getProcThread ().enqueueCmd (cc);
                m_cmdText.setText (null);
            }
            
        } else if (e.getSource () == m_logoutBtn) {
        } else {
            System.out.println ("!!! BUG: Bad event !!!");
            Thread.dumpStack ();
        }
    }
}
README.txt  - by Jen-Chieh Huang (jh3478)

DESCRIPTION
This is a cute little client-server chat room program. In this program, a message-based is exploited to solve the 
concurrency and synchronization issues. The following part of this document explains the architecture.

1. Server
    1.  Server Processing Thread 
        This thread takes care of most jobs, and is designed as a singleton class. All server messages are processed by 
        this thread. The thread has a blocking-queue as a major communication mechanism. In other words, the thread will
        suspend till some body throws a message to the queue. Note that the queue supports concurrency.
        
    2.  Server Accepting Thread
        The major task of this thread is to accept the incoming connections. This thread will be blocked by the call, 
        socket.accept (). Whenever a new client is connected, the thread throws a Server_Command_NewConn object to the 
        server processing thread, and go back to wait till the next client is connected.
        
    3.  Server Client Worker Thread and Reader Thread.
        In the server side, every client has a worker thread and a reader thread to server them. The worker thread is 
        responsible for sending data to the corresponding client and the reader thread reads data from tne client. The 
        major reason to design these threads is to provide full duplex access to the clients. In the very early design,
        only one worker thread is designed for all clients, but after a second thought, since the client can cause a 
        server TX congestion, a seperate worker thread is then designed to avoid such a case.
        
2. Client
    1.  UI threads
        The chat room client has a simple Swing-based UI. The UI consists two parts, the login window and the chat 
        window. As the name suggests, the login window provides the interface for users to input the name and the 
        password. Note that after the 1st login attempt, the user name field is locked. This design is used to prevent
        the malicious user (TA!?) to attack the system. If the user can not enter the right name and pass combination in
        3 times, the client side will be shutdown. The chatroom UI is quite simple. You can enter the commands in the 
        input field below, and use the "send" button to send the command. All system and user messages will be displayed 
        in the chat board (the text area.)
        
    2.  Client Reader Thread
        The reader thread is similiar to the one in the server side. The thread will be blocked till a new message/
        command is sent to the client. It will transfer a communication object into a client side command, and let the 
        worker thread do the processing.
        
    3.  Client Worker Thread
        The client worker thread is also similar to the server side worker thread. The thread processes the input from
        the UI threads, and also updates the server reponses to the UI.
        
DEVELOPMENT ENVIRONEMT
    1.  The program is developed by java 1.7 (however, I dont think I use the new features in Java 7.) 
    2.  The code is managed by github. (Well, you can see my check-in activity @ github/ajchuang/JavaChatCafe
    3.  The editor I am using is Editra (open source python editor @ MAC) and PSPad (shareware @ windows)
    4.  The build scripts include mac version (make.sh) and windows version (make.bat).
    
HOW TO RUN
    1.  Please use ./make.sh (@ OS X, Linux, Unix...) or ./make.bat (@ windows) to build whole system. It should be 
        completed without any error or warning.
    2.  Start the server with the command, java Server [port]. For example, java Server 5566.
    3.  Start the client with the command, java Client [server ip] [port]. For example, java Client localhost 5566.
    4.  Enjoy chatting.
    5.  All functions in the requirement are implemented. For example, message facebook hello. 
    
BONUS FEATURES
    1.  a set of shorthand command is implemented.
        1. m = message, message facebook test = m facebook test
        2. b = broadcast, broadcast test = b test
        3. lo = logout, logout = lo
        
    2.  a set of administrative features are also implemented. Please login with admin/admin to run these features. 
        1.  add new users: add_user [name] [password]
        2.  sync the data to the database (cause a permenent change in user_pass.txt): sync
        
    3.  For a common user, one can also change the pass for himself/herself.
        1.  change_pass [new pass]
    
        
         
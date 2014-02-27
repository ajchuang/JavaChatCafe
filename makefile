JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Client.java \
	Client_ChatWindow.java \
	Client_CmdType.java \
	Client_Command.java \
	Client_LoginWindow.java \
	Client_ProcThread.java \
	CommObject.java \
	CommObjectType.java \
	Server.java \
	Server_ClientReaderThread.java \
	Server_ClientState.java \
	Server_ClientWorkerThread.java \
	Server_CmdType.java \
	Server_Command.java \
	Server_Command_AuthReq.java \
	Server_Command_NewConn.java \
	Server_Command_StrVec.java \
	Server_ProcThread.java \
	Server_UserDatabase.java \
	Server_UserLoginRec.java \
	Server_UserObject.java \
	Server_UserOfflineMsg.java \
	SystemParam.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
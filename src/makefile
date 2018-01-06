JFLAGS = -g
JC = javac

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
ChatServer.java \
ChatClient.java 


default: classes
classes: $(CLASSES:.java=.class)
clean:
	$(RM) *.class

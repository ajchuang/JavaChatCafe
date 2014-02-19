@echo off
del *.class
javac *.java 2>log.txt
start notepad log.txt
@echo on

@echo off

javac CommObject.java
javac Client*.java
java Client localhost 5566

@echo on
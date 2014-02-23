#!/bin/bash
rm *.class
javac *.java 2>&1 | tee log.txt

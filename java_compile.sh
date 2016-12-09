#!/bin/sh
set -x
javac -Xlint -cp java -d java/classes java/nlp/*.java

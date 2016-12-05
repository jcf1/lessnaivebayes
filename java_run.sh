#!/bin/sh
set -x
javac -cp java/classes nlp.NaiveBayesTextClassifier -d "$1" -t "$2"

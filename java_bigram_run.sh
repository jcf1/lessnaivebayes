#!/bin/sh
set -x
java -cp java/classes nlp.NaiveBayesTextClassifier -d "$1" -t "$2" -bi

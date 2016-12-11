#!/bin/sh
set -x
[ ${ALREADY_COMPILED+1} ] || sh java_compile.sh
java -cp java/classes nlp.NaiveBayesTextClassifier -d "$1" -t "$2" -bi

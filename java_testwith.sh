#!/bin/sh
exec 2> tmp/out.x
set -x
javac -Xlint -cp java -d java/classes java/nlp/*.java

grams='1 2'

pids=''
for gram in $grams; do
	time {
		java -cp java/classes nlp.NaiveBayesTextClassifier "-$gram" -d "$1" -t "$2" > "tmp/out.$gram"
		diff "$2" "tmp/out.$gram" > "tmp/out.$gram.wrong"
		num=`grep '^>' "tmp/out.$gram.wrong" | wc -l | awk '{print $1}'`
		sum=`wc -l "$2" | awk '{print $1}'`
		pct=$((100*num/sum))
		dec=$((10000*num/sum-100*pct))
		printf "$gram-gram results: %2d.%02d%% ($num/$sum)\\n" $pct $dec
	} &
	pids="${pids# } $!"
done

for pid in $pids; do
	wait $pid
done

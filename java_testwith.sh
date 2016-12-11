#!/bin/sh
exec 2> tmp/out$$.x
set -x
[ ${ALREADY_COMPILED+1} ] || sh java_compile.sh

grams='1 2'

pids=''
for gram in $grams; do
	time {
	java -cp java/classes nlp.NaiveBayesTextClassifier "-$gram" -d "$1" -t "$2" > "tmp/out$$.$gram"
	diff "$2" "tmp/out$$.$gram" > "tmp/out$$.$gram.wrong"

	#accuracy
	total=$((`wc -l "$2" | awk '{print $1}'` - `head -1 "$2"` - 2))
	wrong=`grep '^>' "tmp/out$$.$gram.wrong" | wc -l | awk '{print $1}'`
	right=$((total-wrong))
	percent=$((100*right/total))
	decimal=$((10000*right/total-100*percent))
	printf "$$ $gram-gram * accuracy %2d.%02d%% ($right/$total) %s %s\\n" $percent $decimal "$1" "$2"

	read cats
	while [ $((cats-=1)) -ge 0 ]; do
		read cat
		export cat
		pos=`sed "1,$(($(head -1 "$2")+1))d" "tmp/out$$.$gram" | tee pos | awk '$1==ENVIRON["cat"]' | wc -l | awk '{print $1}'`
		fp=`awk '$1==">" && $2==ENVIRON["cat"]' "tmp/out$$.$gram.wrong" | wc -l | awk '{print $1}'`
		fn=`awk '$1=="<" && $2==ENVIRON["cat"]' "tmp/out$$.$gram.wrong" | wc -l | awk '{print $1}'`
		tp=$((pos-fp))

		#precision
		percent=$((100*tp/pos))
		decimal=$((10000*tp/pos-100*percent))
		printf "$$ $gram-gram $cat precision %2d.%02d%% ($tp/$pos) %s %s\\n" $percent $decimal "$1" "$2"

		#recall

		denom=$((tp+fn))
		percent=$((100*tp/denom))
		decimal=$((10000*tp/denom-100*percent))
		printf "$$ $gram-gram $cat recall %2d.%02d%% ($tp/$denom) %s %s\\n" $percent $decimal "$1" "$2"
	done
} < "$2" &
pids="${pids# } $!"
done

for pid in $pids; do
	wait $pid
done

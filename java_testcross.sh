#!/bin/sh
exec 2> tmp/out$$.xx
set -x
if ! [ ${KEEP_DONE+1} ]; then
	for ea in tmp/out*.done=*; do
		rm -f "$ea" "tmp/out${ea##*=}."*
	done
fi
[ ${ALREADY_COMPILED+1} ] || sh java_compile.sh
export ALREADY_COMPILED=1

pids=''
for fst; do
	for snd; do
		sh java_testwith.sh "$fst" "$snd" &
		pids="${pids# } $!"
	done
done

for pid in $pids; do
	wait $pid
	touch "tmp/out$$.done=$pid"
done
touch "tmp/out$$.done=$$"

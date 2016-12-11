#!/bin/sh
set -x
[ ${ALREADY_COMPILED+1} ] || sh java_compile.sh
ALREADY_COMPILED=1

pids=''
for fst; do
	for snd; do
		sh java_testwith.sh "$fst" "$snd" &
		pids="${pids# } $!"
	done
done

for pid in $pids; do
	wait $pid
done

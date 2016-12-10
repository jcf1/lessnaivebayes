#!/bin/sh
set -x

for tag; do
	n=1
	while [ -f "$tag-$n.csv" ]; do
		n=$((n+1))
	done

	time t search all -cn 10000 "#$tag" > "$tag-$n.csv"
done

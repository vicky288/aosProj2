#!/bin/bash

netid=axg131530

PROJDIR=$HOME/AOS/RCAlgo/bin
CONFIG=$PROJDIR/config.txt

n=0

for line in `sed -n '/#Nodes/, /#Keys/p' $CONFIG | sed '/^#.*/d'`
do
	#echo $line
	host1=$( echo $line | cut -d ':' -f2)
	host=$(echo $host1 | cut -d '@' -f1)
	echo $host
	ssh -l "$netid" "$host" "kill -9 `ps -ef | grep Application_ABL | grep -v grep | awk '{print $2}'`" &
	n=$(( n + 1 ))
done

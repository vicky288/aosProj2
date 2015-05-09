#!/bin/bash
# This Script calculates the critical sections executed by the launcher.
if [ $# -ne 1 ]; then
    echo "Wrng number of parameters passed."
    echo "Usage: $0 <config file>"
    exit 2
fi

PROJDIR=$HOME/aos2_2
# Read the config file to get number of nodes.
no_of_nodes=`grep -i -A 1 '\#Number of nodes' $1 | tail -1 | sed 's/\r//g'`

# Read the log files and count the number of critical sections executed.
let file_end=$no_of_nodes-1

count=0
for node in $(seq 0 $file_end)
do
    sub_count=`grep -ic 'cs execution finished' $PROJDIR/n${node}.txt`
    let count=$count+$sub_count
    echo "$sub_count numbers of critical sections excuted by node $node"
done

echo $count

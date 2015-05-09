#!/bin/bash


# Change this to your netid
netid=bxp131030

#
# Root directory of your project
PROJDIR=$HOME/aos2_2

#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
echo "Enter the topology file: "
read name

CONFIG=$PROJDIR/$name

#
# Directory your java classes are in
#
BINDIR=$PROJDIR

#
# Your main project class
#
PROG=NodeStarter
isNodeInfo=0
isCSData=0
isResourceInfo=0
resource_id=""
resource_server=""
resource_port=""
#n=1

cat $CONFIG | sed -e "/^\s*$/d" | 
(
    read i
    #echo $i
    while read line 
    do
    isComment=0
	#echo $line
    if [[ $line == *"ResourceInfo"* ]] 
    then
       isResourceInfo=1
       isComment=1
       #echo $line
    fi
    
    if [[ $line == *"Number of nodes"* ]]
    then
       isResourceInfo=0
    fi
	
	if [[ $line == *"Nodes"* ]] 
    then
       isNodeInfo=1
       isComment=1
       #echo $line
    fi

    if [[ $line == *"Keys"* ]]
    then
       isNodeInfo=0
    fi
	
    if [[ $line == *"Number of CS requests per node"* ]]
    then
       isCSData=1
	   isComment=1
    fi	
	
	if [[ $line == *"OtherInfo"* ]]
    then
       isCSData=0
    fi	
	
   if [ $isResourceInfo == 1 ] && [ $isComment == 0 ]  
    then
    #echo $line
	resource_id=$( echo $line | cut -f1 -d":" )
	resource_server=$( echo $line | cut -f2 -d":" | cut -f1 -d"@" )
	resource_port=$( echo $line | cut -f2 -d":" | cut -f2 -d"@" )
	echo $resource_id
	echo $resource_server
	echo $resource_port
	echo "****"
	ssh -l "$netid" "$resource_server" "cd $BINDIR;java Resource_ABL $resource_id $resource_server $resource_port " &

    fi
	
   if [ $isNodeInfo == 1 ] && [ $isComment == 0 ]  
    then
    	#echo $line
	node=$( echo $line | cut -f1 -d":" )
	host=$( echo $line | cut -f2 -d":" | cut -f1 -d"@" )
	port=$( echo $line | cut -f2 -d":" | cut -f2 -d"@" )
	#echo $node
	#echo $host
	#echo $port
	#echo "!!"
	#ssh -l "$netid" "$host" "cd $BINDIR;java Starter $node $host $port " &
    fi
	
	if [ $isCSData == 1 ] && [ $isComment == 0 ]  
    then
    	#echo $line
	node=$( echo $line | cut -f1 -d":" )
	host=$( echo $line | cut -f5 -d":" | cut -f1 -d"@" )
	port=$( echo $line | cut -f5 -d":" | cut -f2 -d"@" )
	CSReqNo=$( echo $line | cut -f2 -d":" )
	StartingDelay=$( echo $line | cut -f3 -d":" )
	CSDelay=$( echo $line | cut -f4 -d":" )
	echo $node
	echo $host
	echo $CSReqNo
	echo $StartingDelay
	echo $CSDelay
	echo "##"
	#echo $resource_id
	#echo $resource_server
	#echo $resource_port
	#echo "****"
	ssh -l "$netid" "$host" "cd $BINDIR;java Application_ABL $CSReqNo $StartingDelay $CSDelay $node $host $port $resource_id $resource_server $resource_port " &
    fi
    done
   
)



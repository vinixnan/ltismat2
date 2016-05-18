#! /bin/bash

source header.sh

if [ -z $1 ]; then
	echo "Please specify a directory."
	echo "Usage: sh startGraphFileViewer.sh directory"
        exit 1
else
	directory=$1
fi

java -Xss20000k -cp $package massim.competition2015.monitor.GraphFileViewer -dir $directory 

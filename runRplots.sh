#! /bin/bash

set -e
set -o xtrace
	
for file in `ls schedule*.R`;
do 
	Rscript $file
done

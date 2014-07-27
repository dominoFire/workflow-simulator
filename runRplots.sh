#! /bin/bash

set -e
	
for file in `ls *.R`
do 
	Rscript $file
done
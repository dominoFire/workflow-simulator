#! /bin/bash

set -e
	
for file in `ls *.R`;
do 
	Rscript $file
done

for dot_file in `ls *.dot`;
do
    OUT_FILENAME="`basename $dot_file`.ps"
    echo $OUT_FILENAME
    dot -Tps  $dot_file -o $OUT_FILENAME
done

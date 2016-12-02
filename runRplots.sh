#! /bin/bash

set -e
set -o xtrace
	
for file in `ls schedule*.R`;
do 
	Rscript $file
done

for dot_file in `ls *.dot`;
do
    OUT_FILENAME="`basename $dot_file`.png"
    echo $OUT_FILENAME
    dot -Tpng  $dot_file -o $OUT_FILENAME
done

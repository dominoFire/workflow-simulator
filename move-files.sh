#! /bin/bash

set -e

INPUT_DIR=$1

if [ -z "${INPUT_DIR}" ]; then
	echo "Uso: ./move-files.sh DIRECTORIO"
	exit 1
fi

mkdir -p "${INPUT_DIR}"

mv schedule*.R "${INPUT_DIR}"
mv schedule*.csv "${INPUT_DIR}"
mv workflow*.obj "${INPUT_DIR}"
mv workflow*.gexf "${INPUT_DIR}"
mv resources*.csv "${INPUT_DIR}"
mv results.csv "${INPUT_DIR}"
mv MaxMin*.pdf "${INPUT_DIR}"
mv MinMin*.pdf "${INPUT_DIR}"
mv Myopic*.pdf "${INPUT_DIR}"
mv Blind*.pdf "${INPUT_DIR}"
mv workflow*.dot.png "${INPUT_DIR}"
mv workflow*.dot "${INPUT_DIR}"
mv workflow*.seed "${INPUT_DIR}"

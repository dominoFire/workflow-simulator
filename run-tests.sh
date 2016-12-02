#! /bin/bash

set -e

DIR_SUFFIX=$1

if [ -z "${DIR_SUFFIX}" ]; then
  (>&2 echo "Uso: ./run-tests.sh FOLDER_SUFFIX")
  exit 1
fi

echo "Compilando"
mvn clean compile

echo "Corriendo tests para optimizacion de costo"  
mvn test -e -Dtest=org.perez.workflow.scheduler.TestAll#testBlindCost
./runRplots.sh
./move-files.sh results-cost-${DIR_SUFFIX}

echo "Corriendo tests para optimizacion de makespan"
mvn test -e -Dtest=org.perez.workflow.scheduler.TestAll#testBlindMakespan
./runRplots.sh
./move-files.sh results-makespan-${DIR_SUFFIX}

echo "Listo"

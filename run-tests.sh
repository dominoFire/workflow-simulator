#! /bin/bash

set -e
set -o xtrace

mvn clean compile test -e -Dtest=org.perez.workflow.scheduler.TestAll#testBlind


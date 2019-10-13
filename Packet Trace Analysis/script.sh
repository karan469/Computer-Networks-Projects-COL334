#!/bin/bash

count=`ls -1 *.csv 2>/dev/null | wc -l`
if [ $count != 0 ]; then rm *.csv;echo Removed residual csv files; fi;
cp ./apache_logs/*.csv  ./;
g++ -o first csvparse.cpp; g++ -o second hisogram_generator.cpp;
./first; ./second

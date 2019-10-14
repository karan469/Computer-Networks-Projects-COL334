#!/bin/bash

countt=`ls -1 *.csv 2>/dev/null | wc -l`
if [ $countt != 0 ]; then mkdir apache_logs; cp *csv ./apache_logs; fi;

if test ! -d ./outputs; then mkdir ./outputs; fi
if test ! -d ./outputs/q1; then mkdir ./outputs/q1; fi
if test ! -d ./outputs/q2; then mkdir ./outputs/q2; fi
if test ! -d ./outputs/q3; then mkdir ./outputs/q3; fi
if test ! -d ./outputs/q4; then mkdir ./outputs/q4; fi
if test ! -d ./outputs/q5; then mkdir ./outputs/q5; fi
if test ! -d ./outputs/q6; then mkdir ./outputs/q6; fi
if test ! -d ./outputs/q7; then mkdir ./outputs/q7; fi
if test ! -d ./outputs/q8; then mkdir ./outputs/q8; fi
if test ! -d ./outputs/q9; then mkdir ./outputs/q9; fi
if test ! -d ./outputs/q10; then mkdir ./outputs/q10; fi

count=`ls -1 *.csv 2>/dev/null | wc -l`
if [ $count != 0 ]; then rm *.csv;echo Removed residual csv files; fi;
cp ./apache_logs/*.csv  ./;
g++ -o first csvparse.cpp; g++ -o second hisogram_generator.cpp;
./first; ./second
g++ -o third connection_duration.cpp; ./third;
g++ -o fourth incomingNoutgoing.cpp; ./fourth;
rm ./outputs/q4/*_letsee_all.csv
rm *.csv
rm first
rm second
rm third
rm fourth
Rscript q10.R

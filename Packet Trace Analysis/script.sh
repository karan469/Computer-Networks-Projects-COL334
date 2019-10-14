#!/bin/bash

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

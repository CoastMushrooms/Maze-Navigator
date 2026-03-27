#!/bin/bash
set -e
cd "$(dirname "$0")"

echo "Compiling..."
javac *.java

echo "Running AverageBotTester..."
java AverageBotTester

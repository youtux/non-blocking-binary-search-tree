#!/bin/sh

# Log levels:
# INFO: Show only infos and errors
# FINE: Show entering and exiting parameters for main
#       methods (find, insert, delete)
# FINER: Show entering and exiting parameters for all
#       the methods
# FINEST: Trace also the various CAS operations
LOGLEVEL="FINE"

CLASSPATH="$(pwd)/src"
MAIN_CLASS_NAME="it.unitn.studenti.alessiobogon.concurrency.Main"
MAIN_FILE_PATH="$CLASSPATH/it/unitn/studenti/alessiobogon/concurrency/Main.java"

echo "Compiling classes..."
javac -cp "$CLASSPATH" "$MAIN_FILE_PATH"

echo "Running $MAIN_CLASS_NAME"
java -DlogLevel=$LOGLEVEL -cp "$CLASSPATH" "$MAIN_CLASS_NAME"

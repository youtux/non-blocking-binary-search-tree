#!/bin/sh

CLASSPATH="$(pwd)/src"

MAIN_CLASS="it.unitn.studenti.alessiobogon.concurrency.Main"
MAIN_FILE_PATH="$CLASSPATH/it/unitn/studenti/alessiobogon/concurrency/Main.java"

# Log levels:
# INFO: Show only infos and errors
# FINE: Show entering and exiting parameters for main
#       methods (find, insert, delete)
# FINER: Show entering and exiting parameters for all
#       the methods
# FINEST: Trace also the various CAS operations
LOGLEVEL="FINE"

echo "Compiling classes..."
javac -cp "$CLASSPATH" "$MAIN_FILE_PATH"

echo "Running $MAIN_CLASS"
java -DlogLevel=$LOGLEVEL -cp "$CLASSPATH" "$MAIN_CLASS"

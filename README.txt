Make sure to use Java 7 and to have "javac" and "java" in your PATH.
In order to compile/run the application, you can just run:
    ./run.sh

Alternatively, you can infer what are the necessary steps from the script.
Basically, to compile you need to run:
    javac -cp src src/it/unitn/studenti/alessiobogon/concurrency/Main.java
and to run it:
    java -DlogLevel=FINE -cp src it.unitn.studenti.alessiobogon.concurrency.Main

The "-DlogLevel=FINE" argument defines the logLevel property FINE, which is the default. You can omit it, or you can specify finer log levels:
- INFO: Show only infos and errors
- FINE: Show entering and exiting parameters for main
        methods (find, insert, delete)
- FINER: Show entering and exiting parameters for all
         the methods
- FINEST: Trace also the various CAS operations

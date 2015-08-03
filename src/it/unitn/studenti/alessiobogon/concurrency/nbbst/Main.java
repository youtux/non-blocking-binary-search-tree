package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import it.unitn.studenti.alessiobogon.concurrency.ConcurrentFormatter;
import it.unitn.studenti.alessiobogon.concurrency.Set;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.logging.*;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
public class Main {
    public static final String GRAPH_OUTPUT_PATH = "graph.dot";

    public static void main(String[] args) throws NoSuchMethodException, InterruptedException {
        Handler ch = new ConsoleHandler();
        ch.setLevel(Level.FINEST);
        ch.setFormatter(new ConcurrentFormatter());

        Logger bstlogger = Logger.getLogger(NonBlockingBinarySearchTree.class.getName());
        bstlogger.setLevel(Level.ALL);
        bstlogger.addHandler(ch);

        final Logger mainLogger = Logger.getAnonymousLogger();
        mainLogger.setLevel(Level.ALL);
        mainLogger.addHandler(ch);


        final NonBlockingBinarySearchTree bst = new NonBlockingBinarySearchTree<>();


        Thread t1 = new Thread(){
            public void run(){
                bst.insert(1);
                bst.insert(2);
                bst.insert(0);
            }
        };
        Thread t2 = new Thread(){
            public void run(){
                bst.delete(2);
            }
        };
        Thread t3 = new Thread(){
            public void run(){
                bst.insert(3);
            }
        };
        t1.start();
        t1.join();
        t2.start();
        t3.start();
        t2.join();
        t3.join();


        try {
            PrintWriter graphOutput = new PrintWriter(GRAPH_OUTPUT_PATH);
            graphOutput.write(bst.dotify());
            graphOutput.close();
            mainLogger.info("Graph written into " + GRAPH_OUTPUT_PATH);
        } catch (FileNotFoundException e) {
            System.err.println("Unable to write into " + GRAPH_OUTPUT_PATH);
            e.printStackTrace();
        }
    }
}

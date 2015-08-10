package it.unitn.studenti.alessiobogon.concurrency;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.*;

import it.unitn.studenti.alessiobogon.concurrency.nbbst.NonBlockingBinarySearchTree;

/**
 * Created by Alessio Bogon on 11/06/15.
 */

/**
 * Main class to test the behaviour of the Non-blocking Binary Search Tree.
 */
public class Main {
    public static final String GRAPH_OUTPUT_PATH = "graph.dot";
    public static final Level defaultLogLevel = Level.FINE;

    private static final Handler logHandler = new ConsoleHandler();

    private static void setupConsoleHandler() {
        Level logLevel;
        try {
            logLevel = Level.parse(System.getProperty("logLevel"));
        } catch (NullPointerException e) {
            System.err.println("No logLevel provided, using default loglevel (" + defaultLogLevel.getName() + ")");
            logLevel = defaultLogLevel;
        } catch (IllegalArgumentException e) {
            System.err.println("Not valid logLevel provided, using default logLevel (" + defaultLogLevel.getName() + ")");
            logLevel = defaultLogLevel;
        }
        logHandler.setLevel(logLevel);
        logHandler.setFormatter(new ConcurrentFormatter());
    }

    private static void setupLogger(Logger logger){
        logger.setLevel(Level.ALL);
        logger.addHandler(logHandler);
    }

    /**
     * The main procedure will instantiate a tree and will start some threads that will perform concurrent operations.
     * The results are printed on the console using a custom logger formatter <tt>ConcurrentFormatter</tt>.
     * When threads finish, a representation of the tree in "dot" format is saved inside the <tt>GRAPH_OUTPUT_PATH</tt>
     * file.
     * The log level can be changed by passing it through a VM option (<tt>-DlogLevel=FINEST</tt>).
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        setupConsoleHandler();

        Logger bstLogger = Logger.getLogger(NonBlockingBinarySearchTree.class.getName());
        setupLogger(bstLogger);

        Logger mainLogger = Logger.getAnonymousLogger();
        setupLogger(mainLogger);

        final NonBlockingBinarySearchTree<Integer> bst = new NonBlockingBinarySearchTree<>();


        Thread t1 = new Thread(){
            public void run(){
                bst.insert(20);
                bst.find(0);
                bst.insert(10);
                bst.delete(30);
                bst.insert(-20);
            }
        };
        Thread t2 = new Thread(){
            public void run(){
                bst.insert(0);
                bst.delete(0);
                bst.find(20);
                bst.insert(30);
                bst.insert(-10);
            }
        };
        Thread t3 = new Thread(){
            public void run(){
                bst.delete(20);
                bst.insert(30);
                bst.insert(20);
                bst.insert(0);
                bst.find(30);
            }
        };
        Thread t4 = new Thread(){
            public void run(){
                bst.insert(30);
                bst.find(20);
                bst.insert(15);
                bst.find(20);
                bst.insert(5);
            }
        };

        Thread t5 = new Thread(){
            public void run(){
                bst.insert(25);
                bst.insert(-5);
                bst.insert(-15);
                bst.delete(20);
                bst.delete(5);
            }
        };

        Thread[] threads = {t1, t2, t3, t4, t5};

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t: threads) {
            t.join();
        }

        System.out.println("The resulting tree is: " + bst);

        try {
            PrintWriter graphOutput = new PrintWriter(GRAPH_OUTPUT_PATH);
            graphOutput.write(bst.dotify());
            graphOutput.close();

            mainLogger.info("Graph written into " + GRAPH_OUTPUT_PATH);
        } catch (FileNotFoundException e) {
            mainLogger.log(Level.SEVERE, "Unable to write into " + GRAPH_OUTPUT_PATH, e);
        }
    }
}

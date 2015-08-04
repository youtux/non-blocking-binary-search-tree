package it.unitn.studenti.alessiobogon.concurrency;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.*;

import it.unitn.studenti.alessiobogon.concurrency.nbbst.NonBlockingBinarySearchTree;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
public class Main {
    public static final String GRAPH_OUTPUT_PATH = "graph.dot";
    public static final Level logLevel = Level.FINEST;

    private static final Handler logHandler = new ConsoleHandler();

    private static void setupConsoleHandler() {
        logHandler.setLevel(logLevel);
        logHandler.setFormatter(new ConcurrentFormatter());
    }

    private static void setupLogger(Logger logger){
        logger.setLevel(Level.ALL);
        logger.addHandler(logHandler);
    }

    public static void main(String[] args) throws NoSuchMethodException, InterruptedException {
        setupConsoleHandler();

        Logger bstLogger = Logger.getLogger(NonBlockingBinarySearchTree.class.getName());
        setupLogger(bstLogger);

        Logger mainLogger = Logger.getAnonymousLogger();
        setupLogger(mainLogger);

        final NonBlockingBinarySearchTree<Integer> bst = new NonBlockingBinarySearchTree<>();


        Thread t1 = new Thread(){
            public void run(){
                bst.insert(1);
                bst.insert(0);
                bst.delete(3);
                bst.insert(7);
                bst.insert(8);
                bst.find(1);
            }
        };
        Thread t2 = new Thread(){
            public void run(){
                bst.insert(2);
                bst.find(1);
                bst.delete(2);
                bst.delete(5);
                bst.insert(9);
                bst.delete(4);
                bst.insert(5);
                bst.find(3);
            }
        };
        Thread t3 = new Thread(){
            public void run(){
                bst.insert(3);
                bst.delete(9);
                bst.insert(1);
                bst.find(1);
                bst.insert(6);
                bst.delete(1);
            }
        };
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();

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

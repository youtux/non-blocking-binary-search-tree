package it.unitn.studenti.alessiobogon.concurrency.nbbst;

import it.unitn.studenti.alessiobogon.concurrency.ConcurrentFormatter;
import it.unitn.studenti.alessiobogon.concurrency.Set;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.logging.*;

/**
 * Created by Alessio Bogon on 11/06/15.
 */
public class Main {
    static class Operation {
        final Method method;
        final Object value;
        final Object object;

        Operation(Object obj, Method method, Object value) {
            this.object = obj;
            this.method = method;
            this.value = value;
        }
    }
    static class MyThread implements Runnable {
        final Operation[] operations;

        MyThread(Operation[] operations){
            this.operations = operations;
        }
        @Override
        public void run() {
            for (Operation op : operations){
                try {
                    op.method.invoke(op.object, op.value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
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
//        System.out.println(bst.dotify());
    }
}

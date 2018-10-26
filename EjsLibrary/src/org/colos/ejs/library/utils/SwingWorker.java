package org.colos.ejs.library.utils;

import javax.swing.SwingUtilities;

import org.colos.ejs.library.Animation;
import org.opensourcephysics.display.OSPRuntime;

/**
 * An abstract class that you subclass to perform
 * GUI-related work in a dedicated thread.
 * For instructions on using this class, see
 * http://java.sun.com/products/jfc/swingdoc/threads.html
 */

public abstract class SwingWorker {
    private Object value;
    private Thread thread;

    /**
     * Compute the value to be returned by the <code>get</code> method.
     */
    public abstract Object construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * Return the value created by the <code>construct</code> method.
     */
    public Object get() {
        while (true) {  // keep trying if we're interrupted
            Thread t;
            synchronized (SwingWorker.this) {
                t = thread;
                if (t == null) {
                    return value;
                }
            }
            try {
                t.join();
            }
            catch (InterruptedException e) {
            }
        }
    }

    /**
     * Start a thread that will call the <code>construct</code> method
     * and then exit.
     */
    public SwingWorker() {
        final Runnable doFinished = new Runnable() {
           public void run() { finished(); }
        };

        Runnable doConstruct = new Runnable() {
            public void run() {
                synchronized(SwingWorker.this) {
                    value = construct();
                    thread = null;
                }
                SwingUtilities.invokeLater(doFinished);
            }
        };

        thread = OSPRuntime.isAppletMode() ? new Thread(doConstruct) : new Thread(Animation.getThreadGroup(),doConstruct);
        thread.start();
    }
}

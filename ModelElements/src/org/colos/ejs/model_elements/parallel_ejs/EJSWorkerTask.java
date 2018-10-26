package org.colos.ejs.model_elements.parallel_ejs;

import org.colos.ejs.library.Model;

/**
 * Code that can be run in parallel to an EJS model
 * @author Francisco Esquembre
 * @version 1.0, June 2011
 *
 */
public class EJSWorkerTask {
  private Model mModel;
  private Runnable mRunnable;

  private boolean mIsRunning=false;
  private long mInitTime=-1;
  private Thread mThread;

  /**
   * Standard constructor to be called by the simulation
   * @param model the EJS model that invokes it
   * @param runnable the Runnable to execute
   */
  public EJSWorkerTask(Model model, Runnable runnable) {
    mModel = model;
    mRunnable = runnable;
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
    release();
  }
  
  /**
   * Stops all running threads
   */
  public void release() {
    if (mThread!=null) {
      mThread.interrupt();
      while (mThread.isAlive()) {
        try { Thread.sleep(100); } 
        catch (Exception exc) {} ;
      }
    }
  }
  
  /**
   * Whether the code is being executed
   * @return
   */
  public boolean isAlive() {
    return mIsRunning;
  }

  // --------------------------
  // execution methods
  // --------------------------

  /**
   * Executes the region's run code in a separate Thread.
   * However, the previousCode and postProcessingCode methods 
   * are called within the EJS model's execution thread.
   * The postProcessingCode() method can ask for the execution time 
   * using the getExecutionTime() method.
   * @return true if a new thread is started, false if the thread was already alive
   */
  public boolean startAndReturn() {
    if (mThread!=null && mThread.isAlive()) return false;
    mIsRunning = true;
    mInitTime = System.currentTimeMillis();
    mThread = new Thread() {
      public void run() {
        try {
          mIsRunning = true;
          mRunnable.run();
          mIsRunning = false;
          if (mModel._isPaused()) mModel.getSimulation().update();
        }
        catch (Exception exc) {
          exc.printStackTrace();
          mIsRunning = false;
          return;
        }
      }
    };
    mThread.start();
    return true;
  }

  /**
   * Resets the initial time used to compute the execution time
   */
  public void resetInitialTime() {
    mInitTime = System.currentTimeMillis();
  }
  
  /**
   * Returns the time from the start of the last execution of the parallel code
   * @return the time in milliseconds. 0 if there was an exception.
   */
  public long getExecutionTime() { 
    if (mInitTime==-1) return -1;
    return System.currentTimeMillis()-mInitTime; 
  }

  /**
   * Returns the running thread
   * @return
   */
  public Thread getThread() { return mThread; }
  
}

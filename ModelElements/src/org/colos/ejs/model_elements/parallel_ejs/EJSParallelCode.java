package org.colos.ejs.model_elements.parallel_ejs;

import org.colos.ejs.library.DelayedAction;
import org.colos.ejs.library.Model;

import edu.rit.pj.ParallelTeam;

/**
 * Code that can be run in parallel to an EJS model
 * @author Francisco Esquembre
 * @version 1.0, June 2011
 *
 */
public abstract class EJSParallelCode {
  private String mName;
  private Model mModel;
  private EJSParallelRegion mEJSRegion;

  private boolean mIsRunning=false;
  private long mInitTime=-1;
  private Thread mThread;
  private int mNumberOfThreads=-1;
  private ParallelTeam mTeam;
  private boolean mSimulationUpdate = false; 
//  private boolean mReleaseAfterFinalCode = false;

  /**
   * Standard constructor to be called by the simulation
   * @param region the ParallelRegion to execute
   */
  public EJSParallelCode(String name, Model model) {
    mName = name;
    mModel = model;
    mEJSRegion = createRegion();
  }
  
  abstract public EJSParallelRegion createRegion();
  
  static public int getAvailableProcessors() {
    return Runtime.getRuntime().availableProcessors();
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
    release();
  }

  /**
   * Stops all running threads
   */
  public void release() {
    mThread = null;
    if (mTeam!=null) mTeam.terminateAll();
    mIsRunning = false;
    mEJSRegion = createRegion();
    mTeam = null;
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
   * Executes the region's pre+run+post code
   * @return the total execution time
   */
  @Deprecated
  public long execute(int nThreads) {
    return startAndWait(nThreads);
  }

  /**
   * Executes the parallel code using the given number of threads
   * @param nThreads
   * @retuns -1 if there was any error, the execution time in milliseconds it successful
   */
  public long startAndWait(int nThreads) {
    try {
      mIsRunning = true;
      prepareThreads(nThreads);
      mTeam.execute(mEJSRegion);
      mIsRunning = false;
      finalCode();
    }
    catch (Exception e) {
      e.printStackTrace();
      mIsRunning = false;
      return -1;
    }
    return System.currentTimeMillis()-mInitTime;
  }
  
  private void prepareThreads(int nThreads) throws Exception {
    mInitTime = System.currentTimeMillis();
    if (nThreads<=0) nThreads = ParallelTeam.getDefaultThreadCount();
    if (nThreads!=mNumberOfThreads || mTeam==null) {
      mNumberOfThreads = nThreads;
      if (mTeam!=null) mTeam.terminateAll();
      mTeam = new ParallelTeam(mName,mNumberOfThreads);
    }
    mEJSRegion._preliminaryCode();
  }
  
  /**
   * Executes the region's run in parallel with nThreads starting in a separate Thread.
   * However, the EJSRegion's _preliminaryCode() and _finalCode() methods 
   * are called within the EJS model's execution thread.
   * @param nThreads
   * @return true if a new thread is started, false if the thread was already alive
   */
  public boolean startAndReturn(int nThreads) {
    if (mIsRunning) { // do not start if the parallel region is being executed
//      System.err.println(mName + " parallel threads already running");
      return false;
    }
    mIsRunning = true;
    try { 
      prepareThreads(nThreads); 
    }
    catch (Exception exc) {
      exc.printStackTrace();
      mIsRunning = false;
      return false;
    }
    mThread = new Thread() {
      public void run() {
        try {
          if (mIsRunning && mThread==Thread.currentThread()) {
            mIsRunning = true;
        	  mTeam.execute(mEJSRegion);
        	  mIsRunning = false;
        	  finalCode();
          }
        }
        catch (Exception exc) {
          exc.printStackTrace();
          mIsRunning = false;
        }
      }
    };
    mThread.setName(mName+" start and return thread");
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
   * Resets the initial time used to compute the execution time
   */
  public void setAutoSimulationUpdate(boolean update) {
    this.mSimulationUpdate = update;
  }

  /**
   * Resets the initial time used to compute the execution time
   *
  public void setReleaseAfterFinalCode(boolean release) {
    this.mReleaseAfterFinalCode = release;
  }*/
  
  /**
   * Returns the time from the start of the last execution of the parallel code
   * @return the time in milliseconds. 0 if there was an exception.
   */
  public long getExecutionTime() { 
    if (mInitTime==-1) return -1;
    return System.currentTimeMillis()-mInitTime; 
  }

  /**
   * Returns the actual number of threads of the current PJTeam
   * @return
   */
  public int getThreadCount() { 
    try {
      return mTeam.getThreadCount();
    }
    catch (Exception exc) {
      return 0;
    }
  }
  
  /**
   * Returns the current ParallelTeam. Notice the team changes if the number of threads changes.
   * @return
   */
  public ParallelTeam getTeam () { return mTeam; }
  
  /**
   * Returns the running thread
   * @return
   */
  public Thread getThread() { return mThread; }
  
  // --------------------------
  // private methods
  // --------------------------

  private void finalCode() {
    mModel.getSimulation().invokeMethodWhenIdle(new DelayedAction() {
      public void performAction() { 
        mEJSRegion._finalCode();
//        if (mReleaseAfterFinalCode) { // release current threads and garbage collect
//          release();
//          System.gc();
//        }
      }
    });
    if (mSimulationUpdate && mModel._isPaused()) mModel.getSimulation().update();
  }
  
}

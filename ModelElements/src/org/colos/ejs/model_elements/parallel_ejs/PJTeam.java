package org.colos.ejs.model_elements.parallel_ejs;

import edu.rit.pj.ParallelTeam;
import edu.rit.pj.ParallelRegion;

/**
 * Encapsulates access to a Parallel Java ParallelTeam object
 * @author Francisco Esquembre
 * @version 1.0, June 2011
 *
 */
public class PJTeam {
  private String mName;
  private ParallelTeam mTeam;
  private ParallelRegion mRegion;
  private int mNumberOfThreads;
  private long mTimeLasted;

  public PJTeam(ParallelRegion region) {
    this("Parallel Java",region);
  }

  /**
   * Standard constructor to be called by the simulation
   * @param region the ParallelRegion to execute
   */
  public PJTeam(String name, ParallelRegion region) {
    mName = name;
    mRegion = region;
    mNumberOfThreads = ParallelTeam.getDefaultThreadCount();
    mTeam =  new ParallelTeam(name,mNumberOfThreads);
    mTimeLasted = -1;
  }
  
  static public int getAvailableProcessors() {
    return Runtime.getRuntime().availableProcessors();
  }
  
  protected void finalize() throws Throwable {
    super.finalize();
    release();
  }

  public void release() {
    if (mTeam!=null) mTeam.terminateAll();
  }
  
  /**
   * Executes the parallel code using the given number of threads
   * @param nThreads
   * @retuns -1 if there was any error, the execution time in milliseconds it successful
   */
  public long execute(int nThreads) {
    mTimeLasted = -1;
    long initTime = System.currentTimeMillis();
    if (nThreads<=0) nThreads = ParallelTeam.getDefaultThreadCount();
    try {
      //      if (mTeam==null) mTeam = new ParallelTeam(mName,nThreads);
      //      else mTeam.setNumberOfThreads(mName, nThreads);
      if (nThreads!=mNumberOfThreads || mTeam==null) {
        mNumberOfThreads = nThreads;
        release();
        mTeam = new ParallelTeam(mName,mNumberOfThreads);
//        mTeam = (mNumberOfThreads > 0) ? new ParallelTeam(mNumberOfThreads) : new ParallelTeam();
      }
      mTeam.execute(mRegion);
      mTimeLasted = System.currentTimeMillis()-initTime;
    } 
    catch (Exception e) {
      e.printStackTrace();
      mTimeLasted = -1;
    }
    return mTimeLasted;
  }
  
  /**
   * Returns the actual number of threads of the current PJTeam
   * @return
   */
  public int getThreadCount() { return mTeam.getThreadCount(); }
  
  /**
   * Returns the time it took for the last execution to complete
   * @return the time in milliseconds. 0 if there was an exception.
   */
  public long getExecutionTime() { return mTimeLasted; }

  /**
   * Returns the current ParallelTeam. Notice the team changes if the number of threads changes.
   * @return
   */
  public ParallelTeam getTeam () { return mTeam; }
  
  
}

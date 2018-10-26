package org.colos.ejs.model_elements.parallel_ejs;

/**
 * Defines an interface for a Parallel problem that can be measured
 * @author Paco
 *
 */
public interface PJProblem {
  
  /**
   * Returns the current problem size
   * @return
   */
  public int getSize();
  
  /**
   * Solves the problem sequentially
   * @return the time lasted in milliseconds
   */
  public long runSequential();
  
  /**
   * Solves the problem with n threads
   * @param nTheads
   * @return the time lasted in milliseconds
   */
  public long runParallel(int nThreads); 
  
}

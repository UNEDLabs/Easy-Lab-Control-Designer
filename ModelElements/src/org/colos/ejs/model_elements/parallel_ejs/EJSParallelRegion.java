package org.colos.ejs.model_elements.parallel_ejs;

import edu.rit.pj.ParallelRegion;

/**
 * Encapsulates access to a Parallel Java ParallelTeam object
 * @author Francisco Esquembre
 * @version 1.0, June 2011
 *
 */
public abstract class EJSParallelRegion extends ParallelRegion {

  private int _firstIndexOf(int threadIndex, int firstIndex, int lastIndex) {
    int range = lastIndex - firstIndex;
    int n = getThreadCount();
    int remainder = range % n;
    if (threadIndex<=remainder) return firstIndex + threadIndex*((range/n) + 1);
    return firstIndex + threadIndex*(range/n) + remainder;
  }
  
  protected int _firstThreadIndex(int firstIndex, int lastIndex) {
    int threadIndex = getThreadIndex();
    if (threadIndex==0) return firstIndex;
    return _firstIndexOf(threadIndex,firstIndex,lastIndex);
  }
  
  protected int _lastThreadIndex(int firstIndex, int lastIndex) {
    int threadIndex = getThreadIndex();
    if (threadIndex==getThreadCount()-1) return lastIndex;
    return _firstIndexOf(threadIndex+1,firstIndex,lastIndex);
  }
  
  abstract public void _preliminaryCode();

  abstract public void _parallelCode() throws Exception;
  
  abstract public void _finalCode();

  public void run() throws Exception {
    _parallelCode();
  }

}
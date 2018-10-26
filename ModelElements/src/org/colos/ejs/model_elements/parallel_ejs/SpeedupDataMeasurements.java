package org.colos.ejs.model_elements.parallel_ejs;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains data of time measurements for a parallel run.
 * Data consist of a problem size N, the number of processors used in the run, and one or more time measurements.
 * For example, the list of measurements:
 * 20 3 2056 2077 2092
 * can be created as:
 * <pre>
 * SpeedupDataMeasurements measurements = new SpeedupDataMeasurements(20,3);
 * measurements.addMeasurement(2056);
 * measurements.addMeasurement(2077);
 * measurements.addMeasurement(2092);
 * </pre>
 * @author  Francisco Esquembre
 * @version Aug 2011
 */
public class SpeedupDataMeasurements implements Comparable<SpeedupDataMeasurements> {
  private int mProblemSize;
  private int mNumberOfProcessors;
  private List<Long> mTimeMeasurements;

  /**
   * Sorts a possibly unsorted list of SpeedupDataMeasurements
   * @param unsorted
   * @return
   */
  static public List<SpeedupDataMeasurements> sort(List<SpeedupDataMeasurements> unsorted) {
    java.util.Collections.sort(unsorted);
    return unsorted;      
  }

  /**
   * Public constructor
   * @param N problem size
   * @param K the number of processors used in the run (K=0 for a sequential run, K>0 for parallel runs)
   */
  public SpeedupDataMeasurements(int N, int K) {
    mProblemSize = N;
    mNumberOfProcessors = K;
    mTimeMeasurements = new ArrayList<Long>();
  }
  
  /**
   * Returns the problem size
   * @return
   */
  public int getProblemSize() { return mProblemSize; }
  
  /**
   * Returns the number of processors
   * @return
   */
  public int getNumberOfProcessors() { return mNumberOfProcessors; }

  /**
   * Returns the time measurements
   * @return
   */
  public List<Long> getTimeMeasurements() { return mTimeMeasurements; }
  
  public void addTimeMeasurement(long measurement) { mTimeMeasurements.add(measurement); }
  
  /**
   * Clears all time measurements
   */
  public void clear() { mTimeMeasurements.clear(); }
  
  /**
   * Converts the measurements into a human readable String
   */
  public String toString() {
//    StringBuffer buffer = new StringBuffer("N = "+mProblemSize);
//    buffer.append(", K = "+mNumberOfProcessors+", Measurements = ");
    StringBuffer buffer = new StringBuffer(""+mProblemSize);
    buffer.append(" "+mNumberOfProcessors+" ");
    for (long measurement : mTimeMeasurements) buffer.append(measurement+" ");
    return buffer.toString();
  }
  
  /**
   * Used to sort measurements
   */
  public int compareTo(SpeedupDataMeasurements other) {
    if (mProblemSize<other.mProblemSize) return -1;
    if (mProblemSize>other.mProblemSize) return +1;
    if (mNumberOfProcessors<other.mNumberOfProcessors) return -1;
    if (mNumberOfProcessors>other.mNumberOfProcessors) return +1;
    return 0;
  }
  
}
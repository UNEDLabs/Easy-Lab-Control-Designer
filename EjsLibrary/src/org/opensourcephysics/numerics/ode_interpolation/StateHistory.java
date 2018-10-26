/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_interpolation;

import java.util.*;

import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ode_solvers.DelayDifferentialEquation;

/**
 * StateHistory is an object that stores past values of a vector valued real function (i.e. f: R --> Rn) and uses 
 * this information to provide interpolated values at different instants of time.
 * The current implementation uses a linked list of IntervalData, each of which is responsible for a half-open (on the right) interval [left,right).
 * For proper operation, the IntervalData must be ordered from old to new and the left side of each interval 
 * must match the right side of the previous interval. Intervals are closed on the left side, and open on the right: [a,b)
 * However, if a new interval is added that overlaps the last one, this newer interval takes over the responsibility for the intersection of both intervals.
 * Notice that right or left is not actually relevant. If any of the intervals happens to have the left ends greater than the right end, then
 * the collection is consider to run backwards, and everything is taken care of properly.  
 * Finally, there may be an optional 'last resource' IntervalData object that takes care of providing an interpolation for values not covered by the regular
 * intervals. This is useful,for instance, for the pre-initial conditions of DelayDifferentialEquations.
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 * @version 2 June 2012
 */
public class StateHistory {
  /**
   * The List of IntervalData objects that provide the data
   */
  private LinkedList<IntervalData> mIntervalList = new LinkedList<IntervalData>();

  /**
   * Whether the solver is stepping forwards
   */
  private boolean mForwards=true;

  /**
   * Adds an optional IntervalData with pre-initial conditions of a DDE, which can provide data further than the IntervalData objects in the list
   */
  private IntervalData mLastResourceInterval=null;

  private double mMinimumLength=0;
  private double mUserLength=0;
  private double mActualLength=0;

  /**
   * Constructor for a History for states of an ODE
   * @param ode the differential equation
   */
  public StateHistory(ODE ode) {
    if (ode instanceof DelayDifferentialEquation) {
      DelayDifferentialEquation dde = (DelayDifferentialEquation) ode;
      mLastResourceInterval = new InitialConditionData(dde);
      dde.setStateHistory(this);
    }
    else {
      mLastResourceInterval = new ConstantConditionData(ode.getState());
    }
  }

  /**
   * Sets the length of the history requested to the solver.
   * The user will then be able to ask for values of the state as far as the current time minus this length.
   * stepSize is the default for plain ODE, getMaximumDelay() is the minimum used by DDEs.
   * Setting a value of Infinity makes the solver to keep the history for ever (i.e. as much as computer memory permits)
   * @param length
   */
  public void setLength(double length) {
    mUserLength = Math.abs(length);
    mActualLength = Math.max(mMinimumLength, mUserLength);
  }

  /**
   * Sets the minimum length of the history requested to the solver.
   * Not to be used by users
   * @param length
   */
  public void setMinimumLength(double length) {
    mMinimumLength = Math.abs(length);
    mActualLength = Math.max(mMinimumLength, mUserLength);
  }

  //	/**
  //	 * Sets an IntervalData which can provide data for time not covered by the regular intervals
  //	 * @param dde
  //	 */
  //	private void setLastResourceIntervalData(IntervalData interval) { mLastResourceInterval = interval; }

  /**
   * Adds an IntervalData at the end of the memory
   * @param data
   */
  public void addIntervalData(IntervalData data) {
    mForwards = (data.getLeft()<=data.getRight());
    if (!mIntervalList.isEmpty()) {
      IntervalData lastInterval = mIntervalList.getLast(); 
      if (mForwards) { // Clean conflicting intervals
        while (lastInterval!=null && lastInterval.getLeft()>=data.getLeft()) {
          mIntervalList.removeLast();
          if (mIntervalList.isEmpty()) lastInterval = null;
          else lastInterval = mIntervalList.getLast();
        }
        if (lastInterval!=null && data.getLeft()<lastInterval.getRight()) lastInterval.setRight(data.getLeft());
      }
      else {
        while (lastInterval!=null && lastInterval.getLeft()<=data.getLeft()) {
          mIntervalList.removeLast();
          if (mIntervalList.isEmpty()) lastInterval = null;
          else lastInterval = mIntervalList.getLast();
        }
        if (lastInterval!=null && data.getLeft()>lastInterval.getRight()) lastInterval.setRight(data.getLeft());
      }
    }
    mIntervalList.addLast(data);
    //		System.out.println("Added ["+data.getLeft()+" , "+data.getRight()+").\n"+this);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("History has now "+mIntervalList.size()+" intervals");
    for (ListIterator<IntervalData> iterator = mIntervalList.listIterator(); iterator.hasNext(); ) {
      IntervalData interval = iterator.next();
      buffer.append("Interval : ["+interval.getLeft()+" , "+ interval.getRight()+")");
    }
    return buffer.toString();
  }

  //  /**
  //   * Returns the mIntervalList in descending order. Does not include the initial conditions 
  //   */
  //  public Iterator<IntervalData> getDescendingIterator() { return mIntervalList.descendingIterator(); }

  /**
   * Clears all the memory data
   */
  public void clearAll() { 
    mIntervalList.clear(); 
    //    System.out.println("History cleared!\n");
  }

  /**
   * Clears all intervals whose data is older than the given time. 
   * This method can be used to save memory when the data is not needed anymore.
   * The last resource interval is not cleared.
   * @param currentTime
   */
  public void clean(double currentTime) {
    if (Double.isInfinite(mActualLength)) return;
    if (mActualLength==0) { // remember only the new interval
      clearAll();
      return;
    }
    currentTime = mForwards ? currentTime - mActualLength : currentTime + mActualLength;
    synchronized(mIntervalList) {
      Collection<IntervalData> toBeRemoved = new ArrayList<IntervalData>();
      if (mForwards) {
        currentTime -= mActualLength;
        for (ListIterator<IntervalData> iterator = mIntervalList.listIterator(); iterator.hasNext(); ) {
          IntervalData interval = iterator.next();
          if (interval.getRight()>currentTime) break; // This one cannot be removed
          toBeRemoved.add(interval);
          //	        System.out.println("Interval to be removed : ["+interval.getLeft()+" , "+interval.getRight()+").\n");
        }
      }
      else {
        currentTime += mActualLength;
        for (ListIterator<IntervalData> iterator = mIntervalList.listIterator(); iterator.hasNext(); ) {
          IntervalData interval = iterator.next();
          if (interval.getRight()<currentTime) break; // This one cannot be removed
          toBeRemoved.add(interval);
        }
      }
      mIntervalList.removeAll(toBeRemoved);
      //	    System.out.println("Intervals Removed .\n"+this);
    }
  }

  /**
   * Finds the interval responsible for interpolating data at this time, including the last resource interval, if all other intervals fail
   * @param time double the given time for the state
   * @return IntervalData the data responsible for this instant of time
   */
  private IntervalData findInterval(double time) {
    if (mForwards) {
      for (java.util.Iterator<IntervalData> iterator = mIntervalList.descendingIterator(); iterator.hasNext(); ) {
        IntervalData interval = iterator.next();
        if (interval.getLeft()<=time) return interval;
      }
    }
    else { // backwards
      for (java.util.Iterator<IntervalData> iterator = mIntervalList.descendingIterator(); iterator.hasNext(); ) {
        IntervalData interval = iterator.next();
        if (interval.getLeft()>=time) return interval;
      }
    }
    return mLastResourceInterval;
  }

  /**
   * Retrieve the state for the given time. 
   * @param time double the given time for the state
   * @param state double[] a place holder for the returned state   
   * @return double[] the array with the data, same as passed state, 
   * if the value was found in memory, null otherwise (the time extends beyond the memory limit)
   */
  public double[] interpolate(double time, double[] state) {
    //	  System.out.println("Interpolate for t="+time);
    IntervalData interval = findInterval(time);
    return interval.interpolate(time, state);
  }

  /**
   * Retrieve the state for the given time for a subset of indexes
   * @param time double the given time for the state
   * @param state double[] a place holder for the returned state
   * @param beginIndex the first index
   * @param length the number of indexes requested   
   * @return double[] the array with the data, same as passed state, 
   * if the value was found in memory, null otherwise (the time extends beyond the memory limit)
   */
  public double[] interpolate(double time, double[] state, int beginIndex, int length) {
    IntervalData interval = findInterval(time);
    return interval.interpolate(time, state, beginIndex, length);
  }

  /**
   * Retrieve the value of one entry of the state for the given time
   * @param time double the given time for the state
   * @param index the index of the value required
   * @return double the value if found in memory, Double.NaN otherwise (the time extends beyond the memory limit)
   */
  public double interpolate(double time,int index) {
    IntervalData interval = findInterval(time);
    return interval.interpolate(time, index);
  }

}


/* 
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */

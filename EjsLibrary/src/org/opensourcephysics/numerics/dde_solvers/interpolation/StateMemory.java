/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.interpolation;

import java.util.*;

import org.opensourcephysics.numerics.*;

/**
 * StateMemory is a class for objects that store past states of an ODE (in form of IntervalData objects)
 * and uses this information to provide interpolated states at different instants of time.
 * For proper operation, the IntervalData must be ordered from old to new and the left side of each interval 
 * must match the right side of the previous interval.
 * 
 * @author Francisco Esquembre
 * @author Maria Jose Cano
 * @version Feb 2011
 */
public class StateMemory {
  /**
   * The mIntervalList of IntervalData objects that provide the data
   */
	private LinkedList<IntervalData> mIntervalList = new LinkedList<IntervalData>();
	
	/**
	 * Whether the solver is stepping forwards
	 */
	private boolean mForwards=true;
	
	/**
	 * Adds an optional IntervalData with pre initial conditions of a DDE, which can provide data further than the IntervalData objects in the list
	 */
	private IntervalData mInitialConditionsInterval=null;
	
	/**
	 * Instantiates a StateMemory for a given ode
	 * @param ode
	 */
	public StateMemory(ODE ode) {
	  if (ode instanceof DDE) setInitialConditions(new InitialConditionData((DDE) ode));  
	}
	
	/**
	 * Sets a DDE which can provide data further for time beyond the memory limits
	 * @param dde
	 */
	public void setInitialConditions(IntervalData interval) { mInitialConditionsInterval = interval; }
	
	/**
	 * Adds an IntervalData at the end of the memory
	 * @param data
	 */
	public void addIntervalData(IntervalData data) {
		mForwards = (data.getLeft()<=data.getRight());
		if (!mIntervalList.isEmpty()) {
		  IntervalData lastInterval = mIntervalList.getLast(); 
		  if (lastInterval!=null) {
		    if (mForwards) {
		      if (data.getLeft()<lastInterval.getRight()) lastInterval.setEndsAtDiscontinuity(data.mLeft);
		    }
		    else {
		      if (data.getLeft()>lastInterval.getRight()) lastInterval.setEndsAtDiscontinuity(data.mLeft);
		    }
		  }
		}
		mIntervalList.addLast(data);
	}
	 
  /**
   * Returns the mIntervalList in descending order. Does not include the initial conditions 
   */
  public Iterator<IntervalData> getDescendingIterator() { return mIntervalList.descendingIterator(); }
  
	/**
	 * Clears all the memory data
	 */
	public void clearAll() { mIntervalList.clear(); }

	/**
	 * Clears all intervals whose data is older than the given time. This method is used to save memory when the data is not needed anymore.
	 * The initial conditions are not cleared.
	 * @param time
	 */
	public void clearBefore(double time) {
	  synchronized(mIntervalList) {
	    Collection<IntervalData> toBeRemoved = new ArrayList<IntervalData>();
	    if (mForwards) {
	      for (ListIterator<IntervalData> iterator = mIntervalList.listIterator(); iterator.hasNext(); ) {
	        IntervalData interval = iterator.next();
	        if (interval.getRight()>time) break; // This one cannot be removed
	        toBeRemoved.add(interval);
	      }
	    }
	    else {
        for (ListIterator<IntervalData> iterator = mIntervalList.listIterator(); iterator.hasNext(); ) {
          IntervalData interval = iterator.next();
          if (interval.getRight()<time) break; // This one cannot be removed
          toBeRemoved.add(interval);
        }
	    }
	    mIntervalList.removeAll(toBeRemoved);
	  }
	}

	 /**
	  * Finds the interval responsible for interpolating data at this time , including the initial conditions if all other intervals fail
	  * @param time double the given time for the state
	  * @param useLeftInterval boolean If the time coincides with the end of an interval, use the boolean to choose the correct interval
	  * @return IntervalData the data responsible for this instant of time, null if there is no such interval (for instance if the time extends beyond the memory limit)
	  */
	 public IntervalData findInterval(double time, boolean useLeftInterval) {
	   if (mForwards) {
	     if (useLeftInterval) {
	       for (java.util.Iterator<IntervalData> iterator = mIntervalList.descendingIterator(); iterator.hasNext(); ) {
	         IntervalData interval = iterator.next();
	         if (interval.getLeft()<time) return interval;
	       }
	     }
	     else {
	       for (java.util.Iterator<IntervalData> iterator = mIntervalList.descendingIterator(); iterator.hasNext(); ) {
	         IntervalData interval = iterator.next();
	         if (interval.getLeft()<=time) return interval;
	       }
	     }
	   }
	   else { // backwards
	     if (useLeftInterval) {
	       for (java.util.Iterator<IntervalData> iterator = mIntervalList.descendingIterator(); iterator.hasNext(); ) {
	         IntervalData interval = iterator.next();
	         if (interval.getLeft()>time) return interval;
	       }
	     }
	     else {
	       for (java.util.Iterator<IntervalData> iterator = mIntervalList.descendingIterator(); iterator.hasNext(); ) {
	         IntervalData interval = iterator.next();
	         if (interval.getLeft()>=time) return interval;
	       }
	     }
	   }
	   return mInitialConditionsInterval;
	 }

	 /**
	  * Retrieve the state for the given time. 
	  * @param time double the given time for the state
	  * @param useLeftInterval boolean If the time coincides with the end of an interval, use the boolean to choose the correct interval
	  * @param state double[] a place holder for the returned state   
	  * @return double[] the array with the data, same as passed state, if the value was found in memory, null otherwise (the time extends beyond the memory limit)
	  */
	 public double[] interpolate(double time, boolean useLeftInterval, double[] state) {
	   IntervalData interval = findInterval(time,useLeftInterval);
	   if (interval!=null) return interval.interpolate(time, state);
	   return null;
	 }

	 /**
	  * Retrieve the state for the given time for a subset of indexes
	  * @param time double the given time for the state
	  * @param useLeftInterval boolean If the time coincides with the end of an interval, use the boolean to choose the correct interval
	  * @param state double[] a place holder for the returned state
	  * @param beginIndex the first index
	  * @param length the number of indexes requested   
	  * @return double[] the array with the data, same as passed state, if the value was found in memory, null otherwise (the time extends beyond the memory limit)
	  */
	 public double[] interpolate(double time, boolean useLeftInterval, double[] state, int beginIndex, int length) {
	   IntervalData interval = findInterval(time,useLeftInterval);
	   if (interval!=null) return interval.interpolate(time, state, beginIndex, length);
	   return null;
	 }

	/**
	 * Retrieve the value of one entry of the state for the given time
	 * @param time double the given time for the state
	 * @param useLeftInterval boolean If the time coincides with the end of an interval, use the boolean to choose the correct interval
	 * @param index the index of the value required
	 * @return double the value if found in memory, Double.NaN otherwise (the time extends beyond the memory limit)
	 */
	public double interpolate(double time, boolean useLeftInterval, int index) {
	  IntervalData interval = findInterval(time,useLeftInterval);
	  if (interval!=null) return interval.interpolate(time, index);
	  return Double.NaN;
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

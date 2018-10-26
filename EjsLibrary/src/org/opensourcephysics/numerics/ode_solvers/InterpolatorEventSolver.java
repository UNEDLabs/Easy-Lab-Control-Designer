/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.ode_solvers;

import java.util.*;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ODESolverException;
import org.opensourcephysics.numerics.ode_interpolation.EulerIntervalData;
import org.opensourcephysics.numerics.ode_interpolation.StateHistory;

/**
 * InterpolatorEventSolver solves an ODE or DDE with events and discontinuities using any SolverInterpolator as the solver engine.
 * Includes advanced features, such as detection of Zeno-effects
 * @author Francisco Esquembre
 * @version 1.0 February 2008
 * @version 2.0 April 2012 - Add discontinuities
 * @version 3 Aug 2013
 * @version 4 Jan 2014
 */
public class InterpolatorEventSolver {
  static public enum ERROR { // Possible values of the error code
    NO_ERROR,              // Everything went ok
    INTERNAL_SOLVER_ERROR, // The solver produced an internal error, such as when it cannot do an internal step
    EVENT_NOT_FOUND,       // The event was not found after using the maximum number of attempts
    ILLEGAL_EVENT_STATE,   // An event of type STATE_EVENT was left in an illegal (negative) state
    ZENO_EFFECT,           // A Zeno effect was detected without a user action
    TOO_MANY_STEPS_ERROR,  // The solver exceeded the number of internal steps
    DISCONTINUITY_PRODUCED_ERROR,   // Unrecoverable error produced by a discontinuity
    DID_NOT_CONVERGE       // An adaptive method did not converge
    } 
  
  static public enum DISCONTINUITY_CODE { // Possible values of the checkDiscontinuity method
    DISCONTINUITY_PRODUCED_ERROR,   // Unrecoverable error
    NO_DISCONTINUITY_ALONG_STEP,    // There is no discontinuity along the given step
    DISCONTINUITY_ALONG_STEP,       // There is a  discontinuity along the given step, but not exactly at its end
    DISCONTINUITY_JUST_PASSED,      // There is a  discontinuity along the given step, and it is just before the current step end
    DISCONTINUITY_EXACTLY_ON_STEP  // There is a  discontinuity along the given step, and its is exactly at the end of the step
    } 

  static public double EPSILON = 1.0;
  static {
    while (EPSILON+1!=1) EPSILON /=2;
    EPSILON *= 2;
  }
  
  // Configuration variables
  private boolean mEnableExceptions=true; // Whether to throw an exception when an error is produced. 
  protected boolean mUseBestInterpolation=false; // Always use the best interpolation the solvers provide (even if it is more time consuming)
  private double mStepSize = 0.1;         // The step size for reading data from the solver
  private double mAbsoluteTolerance = Double.NaN; // The absolute tolerance for adaptive methods
  private double mRelativeTolerance = Double.NaN; // The relative tolerance for adaptive methods
  private double mMaxEventStep=Double.POSITIVE_INFINITY; // The maximum advance before checking for an event
  private int mZenoMaximumAllowedTimes=500; // The number of times that a separation smaller than the threshold must occur to declare a Zeno effect
  private double mProximityThreshold=2*Double.MIN_VALUE; // The threshold to consider two events as simultaneous or indicate a possible Zeno effect
  private boolean mCoalesceCloseEvents = true; // whether to coalesce close events (even if the action returns true)
  private int mMaxInternalSteps = 10000;
  
  // Implementation variables
  protected SolverEngine mSolverEngine; // The underlying solver engine
  protected ODE mODE;
  
  private boolean mRunsForwards=mStepSize>0;
  private int mDimension; // The dimension of the problem (including time)
  private int mTimeIndex; // The index of the time state, i.e. dimension-1
  private ERROR mErrorCode=ERROR.NO_ERROR; // The error code
  private String mErrorMessage="No error";
  private int mNumberOfAttempts=0;  // Actual number of attempts needed to locate the last event
  private double[] mTest_ode_state; // a place holder for intermediate states
  private double[] mIntermediate_ode_state; // a second place holder for intermediate states
  
  private List<EventData> mEventList = new ArrayList<EventData>(); // The list of events added by the user (except those of type DISCONTINUITY_EVENT, which are handled separately)
  private List<EventData> mHappened  = new ArrayList<EventData>(); // The list of events that take place in a given interval
  private List<EventData> mTemp_list = new ArrayList<EventData>(); // a temporary list of events
  private double mLastEventDataTime=Double.NaN; // Remember the time of the last event
  private ProblemData mLastEventData=null; // Remember the last event
  private int mZenoCounter=0; // Count how many close events have happened
  private List<ZenoEffectListener> mZenoList = new ArrayList<ZenoEffectListener>(); // list of Zeno Listeners

  private ProblemData mCurrentEventData=null; // Placeholder for the current event, used to reset some events properly
  private boolean mHasEventsOrDiscontinuities = false; // version 2.0
  private boolean mHasDiscontinuities = false; // version 2.0
  private DiscontinuityData mDiscontinuityAtEnd = null; // The first DISCONTINUITY_EVENT event founds in one step // version 2.0
  private List<DiscontinuityData> mDiscontinuityList = new ArrayList<DiscontinuityData>(); // The list of DISCONTINUITY_EVENT type of events added by the user // version 2.0

  private DDEDiscontinuity mDDEdiscontinuity=null;
  private int mDDEdiscontinuityMaxIterations = 100;
  private double mDDEdiscontinuityTolerance = 1.0e-8;
  
  // --- Constructor
  
  /**
   * Instantiates an object of this class.
   * @param engine an ODESolverInterpolator object
   * @param ode the ODE to solve
   */
  public InterpolatorEventSolver(SolverEngine engine, ODE ode) {
    mSolverEngine = engine;
    setODE(ode);
  }

  /**
   * Instantiates an object of this class.
   * @param engineClass The Class of the engine to use
   * @param ode the ODE to solve
   */
  public InterpolatorEventSolver(Class<?> engineClass, ODE ode) {
    try {
      mSolverEngine = (SolverEngine) engineClass.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Could not instantiate solver engine, using RK4 instead!");
      mSolverEngine = new org.opensourcephysics.numerics.ode_solvers.rk.RK4();
    }
    setODE(ode);
  }
  
  private void setODE(ODE ode) {
    mODE = ode;
    if (mODE instanceof DelayDifferentialEquation) {
        mDDEdiscontinuity = new DDEDiscontinuity((DelayDifferentialEquation) mODE);
      }
    mSolverEngine.setODE(this, mODE);
  }

  // --------------------------------------------
  // Configuration methods
  // --------------------------------------------
  
  /**
   * Enables runtime exceptions if there is any error condition. 
   * If exceptions are disabled, the solver prints a warning to the standard error output and continues.
   * Exceptions are enabled by default.
   * @param enable boolean
   */
  public void setEnableExceptions(boolean enable) { mEnableExceptions = enable; }

  /**
   * Request the internal solver to always use the best interpolation it can provide,
   * even if it is more time-consuming than the standard one. For examples, some RK
   * solvers will re-step from the initial step every time they are asked for their best
   * interpolation, which is very expensive in terms of function evaluations.
   * Best interpolation is however always used (irrespective of this parameter) to 
   * return the state after a successful step and after an event takes place. 
   * But standard interpolation is used by default for locating the events.
   * Setting this parameter to true forces the solver to use its best interpolation
   * even for event location.
   * @param best
   */
  public void setBestInterpolation(boolean best) { mUseBestInterpolation = best; }

  /**
   * Sets the length of the memory requested to the solver. Must be a positive value.
   * The user will then be able to ask for values of the state as far as the current time minus this length.
   * 0 is the default for plain ODE, getMaximumDelay() is the minimum used by DDEs.
   * Setting a value of Infinity makes the solver to remember for ever (i.e. as much as computer memory permits)
   * @param length
   */
  public void setHistoryLength(double length) { mSolverEngine.getStateHistory().setLength(length); }

  /**
   * Sets the reading step size. That is the step at which solutions are read from 
   * the equation. Most of the times, these solutions are obtained by interpolation.
   */
  public void setStepSize(double stepSize) {
    if (this.mStepSize==stepSize) return;
    this.mStepSize = stepSize;
    mRunsForwards = mStepSize>0;
    setInternalStepSize(mSolverEngine.getStepSize()); // Make sure the interpolator solver runs in the same direction
  }

  /**
   * Returns the reading step
   */
  public double getStepSize() { return mStepSize; }
    
  /**
   * Asks adaptive solvers to estimate the best initial step after reinitialize().
   * If false, the given initial step (as set by setComputationStepSize()) is used.
   * @param estimate
   */
  public void setEstimateFirstStep(boolean estimate) { mSolverEngine.setEstimateFirstStep(estimate); }

  /**
   * Sets the interpolator's internal step size. This is the step at which solutions are computed
   * for fixed step methods, and the initial step size (after reinitialize()) for variable
   * step methods. The value is taken absolutely in the direction of the reading step size.
   */
  public void setInternalStepSize(double stepSize) {
    mSolverEngine.setStepSize(mRunsForwards ? Math.abs(stepSize) : -Math.abs(stepSize));
  }

  /**
   * Sets the interpolator's internal maximum step size. Has no effect on fixed-step solvers and on QSS methods.
   * @param stepSize
   */
  public void setMaximumInternalStepSize(double stepSize) { mSolverEngine.setMaximumStepSize(stepSize); }

  /**
   * Sets the maximum number of internal steps the interpolator can take to reach a 
   * reading step. If the solver takes so large a number (of very small steps) this
   * typically means the solver has reached a singularity and must take too small steps.
   * It can also mean the solver has difficulties to reach the tolerance and the user
   * should consider either increasing this limit, or use another solver. 
   * 
   * @param steps
   */
  public void setMaximumInternalSteps(int steps) { mMaxInternalSteps = steps; }
  
  /**
   * The preferred absolute and relative tolerance desired for the solution if the 
   * underlying solver supports it. If the solver does not support this feature, the 
   * method is ignored. Changing the tolerances may involve a re-computation 
   * of the current step.
   * @param tol
   */
  public void setTolerances(double absTol, double relTol) {
    mSolverEngine.setTolerances(mAbsoluteTolerance = Math.abs(absTol), mRelativeTolerance = Math.abs(relTol));
  }

  /**
   * Equivalent to setTolerances (tol,0)
   * @param tol
   */
  public void setTolerance(double tol) { setTolerances(tol,0); }

  /**
   * Returns the maximum of the absolute and relative tolerances
   */
  public double getTolerance () { return Math.max(mAbsoluteTolerance, mRelativeTolerance); }
  
  /**
   * The tolerance for finding discontinuities created by DDE delays
   * @param tol
   */
  public void setDDETolerance(double tol) { mDDEdiscontinuityTolerance = tol; }

  public double getDDETolerance() { return mDDEdiscontinuityTolerance; }

  /**
   * Number of iterations allowed to find discontinuities created by DDE delays
   * @param iterations
   */
  public void setDDEIterations(int iterations) { mDDEdiscontinuityMaxIterations = iterations; }

  /**
   * Number of iterations needed to find the last discontinuity created by a DDE delay
   */
  public int getDDEIterations() { return mDDEdiscontinuityMaxIterations; }
 
  // -----------------------------------
  // Events and discontinuities
  // -----------------------------------

  public void addEvent(StateEvent event) {
    mEventList.add(new EventData(event,mODE.getState()));
    mHasEventsOrDiscontinuities = true;
  }
  
  public void removeEvent(StateEvent event) {
    EventData foundData = null;
    for (EventData data : mEventList) if (data.event==event) { foundData = data; break; }
    if (foundData!=null) {
      if (mLastEventData==foundData) { mLastEventData = null; mZenoCounter = 0; }
      if (mCurrentEventData==foundData) mCurrentEventData = null;
      mEventList.remove(foundData);
    }
    mHasEventsOrDiscontinuities = mHasDiscontinuities || !mEventList.isEmpty();
  }

  public void addDiscontinuity(Discontinuity discontinuity) {
    mDiscontinuityList.add(new DiscontinuityData(discontinuity,mODE.getState()));
    mHasDiscontinuities = true;
    mHasEventsOrDiscontinuities = true;
  }

  public void removeDiscontinuity(Discontinuity discontinuity) {
    DiscontinuityData foundData = null;
    for (DiscontinuityData data : mDiscontinuityList) if (data.discontinuity==discontinuity) { foundData = data; break; }
    if (foundData!=null) {
      mDiscontinuityList.remove(foundData);
    }
    mHasDiscontinuities = !mDiscontinuityList.isEmpty();
    mHasEventsOrDiscontinuities = mHasDiscontinuities || !mEventList.isEmpty();
  }

  public void removeAllEvents() {
    mEventList.clear();
    mDiscontinuityList.clear();
    mHasDiscontinuities = false;
    mHasEventsOrDiscontinuities = false;
    if (mDDEdiscontinuity!=null) addDiscontinuity(mDDEdiscontinuity);
  }
    
  /**
   * Sets the maximum step allowed before checking for an event.
   * Default is Double.POSITIVE_INFINITY
   * @param step
   */
  public void setMaximumEventStep(double step) { mMaxEventStep = Math.abs(step); }

  /**
   * Returns the maximum step allowed before checking for an event.
   * @return
   */
  public double getMaximumEventStep() { return mMaxEventStep; }

  /**
   * If true, an event closer than the threshold to the previous one 
   * will not return even if the event action returns true. Default is true.
   * @param coalesce
   */
  public void setCoalesceCloseEvents(boolean coalesce) { mCoalesceCloseEvents = coalesce; }
  
  /**
   * Whether an event closer than the threshold to the previous one 
   * will not return even if the event action returns true
   * @return
   */
  public boolean isCoalesceCloseEvents() { return mCoalesceCloseEvents; }
  
  /**
   * Sets the threshold that considers two events as close enough for coalescing
   * or indicating a possible Zeno-like effect. Default is 2*Double.MIN_VALUE
   * @param threshold The small separation that indicates a possible Zeno effect
   */
  public void setEventProximityThreshold (double threshold) { mProximityThreshold = threshold; }

  /**
   * Return the threshold that considers two events as close enough for coalescing
   * or indicating a possible Zeno-like effect
   */
  public double getEventProximityThreshold () { return mProximityThreshold; }

  // -----------------------------------
  // Zeno-like effects 
  // -----------------------------------

  /**
   * If the solver finds more than _times successive events closer than the proximity threshold,
   * it will consider it a Zeno effect and call the registered ZenoEffectListeners. Default is 500.
   * @param times The number of times that a separation smaller than the threshold must occur to declare a Zeno effect. 
   * A zero or negative value disables the detection.
   */
  public void setZenoEffectDetection (int times) { mZenoMaximumAllowedTimes = times; }

  /**
   * Returns the number of successive events closer than the proximity threshold,
   * that will be considered a Zeno effect.
   */
  public int getZenoEffectDetection () { return mZenoMaximumAllowedTimes; }

  /**
   * Adds a ZenoEffectListener that will be called if a Zeno-like effect situation occurs.
   */
  public void addZenoEffectListener (ZenoEffectListener listener) { mZenoList.add(listener); }
  
  /**
   * Removes a previously added ZenoEffectListener 
   */
  public void removeZenoEffectListener (ZenoEffectListener _listener) { mZenoList.remove(_listener); }
  
  public void setZeroZenoCounter() { mZenoCounter = 0; }
  
  // --------------------------------------------
  // Getter methods
  // --------------------------------------------
  
  /**
   * Returns the interpolator solver
   * @return
   */
  public SolverEngine getSolverEngine() { return mSolverEngine; }  

  /**
   * Returns the ODE to solve
   * @return
   */
  public ODE getODE() { return mODE; }  

  /**
   * Provides access to the internal StateHistory responsible for interpolations.
   * @return
   */
  public StateHistory getStateHistory() { return mSolverEngine.getStateHistory(); }

  /**
   * Returns the error code after a step
   */
  public ERROR getErrorCode() { return mErrorCode; }
  
  /**
   * Returns the error message
   * @return
   */
  public String getErrorMessage() { return mErrorMessage; }

  /**
   * Returns the number of function evaluations required by the method to reach the tolerance 
   * @return
   */
  public long getCounter() { return mSolverEngine.getCounter(); }

  /**
   * The number of attempts that were required to locate the last event
   * @return
   */
  public int getNumberOfAttempts() { return mNumberOfAttempts; }

  public double getInternalStepSize() { return mSolverEngine.getInternalStepSize(); }
  
  /**
   * Returns the current value of the independent variable
   * @return
   */
  public double getIndependentVariableValue () { return mODE.getState()[mTimeIndex];}
  
  /**
   * Same as getIndependentVariableValue ()
   * @return
   */
  public double getCurrentTime() { return mODE.getState()[mTimeIndex]; }

  // ----------------------------------------------------
  // Operation
  // ----------------------------------------------------
  
  /**
   * Initializes the solver. The step size is used to set the reading step size AND
   * also passed along to the ODESolverInterpolator for internal initialization.
   * The interpolator's internal step size can be changed with setInternalStepSize(double).
   * Calls to setStepSize() will not affect the internal step size of the interpolator.
   */
  public void initialize(double stepSize) {
    this.mStepSize = stepSize;
    mRunsForwards = mStepSize>0;
    mSolverEngine.initialize(mStepSize);
    double[] state = mODE.getState(); 
    mDimension = state.length;
    mTimeIndex = mDimension - 1;
    mTest_ode_state = new double[mDimension];
    mIntermediate_ode_state = new double[mDimension];
    mErrorCode = ERROR.NO_ERROR;
    mErrorMessage = "No error";
    mZenoCounter = 0;
    mLastEventData = null;
    mCurrentEventData = null;       
    if (mDDEdiscontinuity!=null) {
      mDDEdiscontinuity.initialize(state);
      removeDiscontinuity(mDDEdiscontinuity);
      addDiscontinuity(mDDEdiscontinuity);
    }
    for (EventData data : mEventList) data.reset(state);
    for (DiscontinuityData data : mDiscontinuityList) data.reset(state);
  }
  
  /**
   * Take the next step as indicated by the stepSize
   * If an event is found, it steps up to that event.
   * @return
   */
  public double step() {
    if (mHasEventsOrDiscontinuities) return stepWithEvents();
    return stepWithoutEvents();
  }

  /**
   * Take the maximum possible step.
   * For adaptive-step methods, steps up to the maximum possible adaptive step. 
   * For fixed-step methods, steps up to the next step size.
   * If an event is found, it steps up to that event.
   * @return
   */
  public double maxStep() {
    if (mHasEventsOrDiscontinuities) return maxStepWithEvents();
    return maxStepWithoutEvents();
  }

  
  /**
   * Used to reinitialize the solver when the user changes directly the ODE state,
   * model parameters, or anything THAT MAY AFFECT THE EVENTS (such as the events tolerance, for instance).
   * EjsS calls automatically reinitialize(), which respects the currentDataEvent information when it detects
   * any other minor change.
   */
  public void userReinitialize() {
    mCurrentEventData = null;
    reinitialize();
  }

  /**
   * Does the minimum (soft) initialization of the solver for a given state.
   * Users MUST call reinitialize (or the harder initialize()) whenever they change directly the ODE state,
   * model parameters, or anything that may affect the events (such as the events tolerance, for instance).
   */
  public void reinitialize() {
    double[] state = mODE.getState();
    mSolverEngine.reinitialize(state);
    mErrorCode = ERROR.NO_ERROR;
    mErrorMessage = "No error";
    if (mDDEdiscontinuity!=null) mDDEdiscontinuity.reset(state); // This must be called before before discData.reset() 
    for (EventData eventData : mEventList) eventData.reset(state);
    for (DiscontinuityData discData : mDiscontinuityList) discData.reset(state); // added in version 2.0
    mDiscontinuityAtEnd = null;
  }

  void resetDiscontinuities(double[] state) {
    if (mDDEdiscontinuity!=null) {
      mDDEdiscontinuity.reset(state); // This must be called before before discData.reset() 
      for (DiscontinuityData discData : mDiscontinuityList) discData.reset(state); // added in version 2.0
    }
  }

  /**
   * Checks whether there would be a discontinuity before or at the given state and, in this last case, optionally marks it 
   * @param state The state to check for
   * @param isEndOfInterval Whether the point is the end of the integration interval. In this case, we mark the point as the discontinuity found
   * @return one of DISCONTINUITY_PRODUCED_ERROR, NO_DISCONTINUITY_ALONG_STEP, DISCONTINUITY_ALONG_STEP, or DISCONTINUITY_EXACTLY_ON_STEP
   */
  public DISCONTINUITY_CODE checkDiscontinuity (double[] state, boolean isEndOfInterval) {
//    System.err.println ("Checking disc at "+_state[timeIndex]+ " with flag = "+_isEndOfInterval);
    DiscontinuityData justHappened=null;
    for (DiscontinuityData data : mDiscontinuityList) {
      data.h = data.discontinuity.evaluate(state);
//      System.err.println ("data.h = "+data.h);
      switch (data.currentPosition) {
        default : 
        case EventData.POSITIVE : 
          if (data.h<=0) {
//            if (data.h>-data.discontinuity.getTolerance()) return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED; // h=0 is allowed in this direction
            return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP;
          }
          if (data.h<data.discontinuity.getTolerance()) justHappened = data;
          break;
        case EventData.SMALL_POSITIVE :
          if (data.h<=0 && data.positiveFlag) {
            error(ERROR.ILLEGAL_EVENT_STATE, "The system started from an illegal state at "+state[mTimeIndex]+ " for the discontinuity "+data.discontinuity); //$NON-NLS-1$ //$NON-NLS-2$
            return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR;
          }
          break;
        case EventData.ZERO :
          if (data.h<0 && data.positiveFlag) {
            error(ERROR.ILLEGAL_EVENT_STATE, "The system started from an illegal state at "+state[mTimeIndex]+ " for the discontinuity "+data.discontinuity); //$NON-NLS-1$ //$NON-NLS-2$
            return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR;
          }
          else if (data.h>0 && data.negativeFlag) {
            error(ERROR.ILLEGAL_EVENT_STATE, "The system started from an illegal state at "+state[mTimeIndex]+ " for the discontinuity "+data.discontinuity); //$NON-NLS-1$ //$NON-NLS-2$
            return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR;
          }
          break;
        case EventData.SMALL_NEGATIVE :
          if (data.negativeFlag && data.h>=0) {
            error(ERROR.ILLEGAL_EVENT_STATE, "The system started from an illegal state at "+state[mTimeIndex]+ " for the discontinuity "+data.discontinuity); //$NON-NLS-1$ //$NON-NLS-2$
            return DISCONTINUITY_CODE.DISCONTINUITY_PRODUCED_ERROR;
          }
          break;
        case EventData.NEGATIVE : 
          if (data.h>0) {
//            if (data.h>data.discontinuity.getTolerance()) return DISCONTINUITY_CODE.DISCONTINUITY_JUST_PASSED; // h=0 is allowed in this direction
            return DISCONTINUITY_CODE.DISCONTINUITY_ALONG_STEP; // h=0 is allowed in this direction
          }
          if (data.h>-data.discontinuity.getTolerance()) justHappened = data;
          break; 
      } // end of switch
    } // end of for
    if (justHappened!=null) {
      if (isEndOfInterval) {
          mDiscontinuityAtEnd = justHappened;
          mDiscontinuityAtEnd.time = state[mTimeIndex];
//          System.err.println ("Disc at end is now "+justHappened+ " at time "+discontinuityAtEnd.time);
        }
      return DISCONTINUITY_CODE.DISCONTINUITY_EXACTLY_ON_STEP;
    }
    return DISCONTINUITY_CODE.NO_DISCONTINUITY_ALONG_STEP;  
  }
  
  // -----------------------------------
  // Private methods
  // -----------------------------------

  /**
   * Does the interpolation at the prescribed time.
   */
  final protected void doTheInterpolation (double time, double[] state) {
    if (mUseBestInterpolation) mSolverEngine.bestInterpolate(time, state);
    else mSolverEngine.interpolate(time, state);
  }
  
  public double error (ERROR code, String msg) {
    mErrorCode = code;
    mErrorMessage = msg;
    if (mEnableExceptions) throw new ODESolverException(mSolverEngine + ":\n"+msg); //$NON-NLS-1$
    OSPLog.warning(msg);
//    System.err.println(interpolatorSolver + ": "+msg); //$NON-NLS-1$
    return Double.NaN;
  }
  
  // ----------------------------------------
  // Step without events or discontinuities
  // ----------------------------------------
  
  /**
   * Steps the ODE when there are no events
   * @return
   */
  final private double maxStepWithoutEvents() {
    double[] state = mODE.getState();
    
    // Check for a 0 rate for time
    double[] rate = mSolverEngine.getCurrentRate();
    double tBegin = state[mTimeIndex];
    if (tBegin+rate[mTimeIndex]==tBegin) return 0;
    
    double max_t = mSolverEngine.getMaximumTime(false); // false = no discontinuities
    if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver at "+state[mTimeIndex]); //$NON-NLS-1$
    // Make sure the solver is not already at the maximum step
    if (tBegin==max_t) {
      max_t = mSolverEngine.internalStep(false);
      if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver at max step at "+state[mTimeIndex]); //$NON-NLS-1$
    }
    doTheInterpolation (max_t, state);
    return max_t-tBegin;
  }
  
  /**
   * Steps the ODE when there are no events
   * @return
   */
  private double stepWithoutEvents() {
    double[] state = mODE.getState();
    
    // Check for a 0 rate for time
    double[] rate = mSolverEngine.getCurrentRate();
    double tBegin = state[mTimeIndex];
    if (tBegin+rate[mTimeIndex]==tBegin) return 0;
    
    double tEnd = state[mTimeIndex] + mStepSize;
    double max_t = mSolverEngine.getMaximumTime(false); // false = no discontinuities
    if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver at "+state[mTimeIndex]); //$NON-NLS-1$

    int counter = 0;
    // Make sure the solver can reach the expected time
    if (mRunsForwards) {
      while (max_t<tEnd) { 
        max_t = mSolverEngine.internalStep(false); // false = no discontinuities
        if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver forwards at "+state[mTimeIndex]); //$NON-NLS-1$
        if ((++counter)>mMaxInternalSteps) { 
          double initTime = state[mTimeIndex];
          double currentTime = mSolverEngine.bestInterpolate(tEnd, new double[mDimension])[mTimeIndex];
          return error(ERROR.TOO_MANY_STEPS_ERROR,"The solver exceeded the maximum of "+mMaxInternalSteps+ //$NON-NLS-1$  
              " internal steps\nat "+currentTime+ ", starting from "+initTime +" for an step of "+mStepSize); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
      }
    }
    else {
      while (max_t>tEnd) {
        max_t = mSolverEngine.internalStep(false); // false = no discontinuities
        if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver backwards at "+state[mTimeIndex]); //$NON-NLS-1$
        if ((++counter)>mMaxInternalSteps) return error(ERROR.TOO_MANY_STEPS_ERROR,"The solver exceeded the number of internal steps at "+state[mTimeIndex]); //$NON-NLS-1$
      }
    }
    doTheInterpolation (tEnd, state);
    return mStepSize;
  }

  // ----------------------------------------
  // Step with events and/or discontinuities
  // ----------------------------------------


  /**
   * Steps the ODE with events
   * @return
   */
  final private double maxStepWithEvents() {
    double[] state = mODE.getState();
    if (mZenoMaximumAllowedTimes>0 && mZenoCounter>mZenoMaximumAllowedTimes) {
      if (callZenoAction(state)) return 0;
    }

    double tBegin = state[mTimeIndex];
    // Check for a 0 rate for time
    double[] rate = mSolverEngine.getCurrentRate();
    if (tBegin+rate[mTimeIndex]==tBegin) return 0;

//    discontinuityAtEnd = null; // Do NOT clean the possible discontinuity
    double max_t = mSolverEngine.getMaximumTime(mHasDiscontinuities); // This detects discontinuities, if there are any
    if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver at "+state[mTimeIndex]); //$NON-NLS-1$
    // Make sure the solver is not already at the maximum step
    if (tBegin==max_t) {
      max_t = mSolverEngine.internalStep(mHasDiscontinuities);
      if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver forwards at "+state[mTimeIndex]); //$NON-NLS-1$
    }

    if (mLastEventData!=null) {
      if (!Double.isNaN(mLastEventData.getMaxAdvance())) { // If a short-duration step was found, this helps avoid recurrent events
        max_t = mRunsForwards ? Math.min(max_t, mLastEventData.getMaxAdvance()) : Math.max(max_t, mLastEventData.getMaxAdvance());  
      }    
    }
    
    double tTest = mRunsForwards ? Math.min(tBegin+mMaxEventStep,max_t) : Math.max(tBegin-mMaxEventStep,max_t);

    while (true) {
      ProblemData problem=null;
      mCurrentEventData = null;       
      doTheInterpolation (tTest, mTest_ode_state);
      problem = findFirstEvent(state,tTest,mTest_ode_state); // Find the first event
      if (problem==null && mDiscontinuityAtEnd!=null && mDiscontinuityAtEnd.time<=tTest) problem = mDiscontinuityAtEnd; // If no event, check for discontinuity (but regular events are acted on first)
      if (problem==null) {
        if (tTest==max_t) {
          System.arraycopy(mTest_ode_state, 0, state, 0, mDimension);
          updateEventsAndDiscontinuities(state[mTimeIndex]);
          return max_t-tBegin;
        }
        tTest = mRunsForwards ? Math.min(tTest+mMaxEventStep,max_t) : Math.max(tTest-mMaxEventStep,max_t);
        continue;
      }

      // There was an event
      mCurrentEventData = problem;   
      if (mUseBestInterpolation) mSolverEngine.bestInterpolate(problem.getTime(), state); // So that we reinitialize with the best possible approximation 
      else System.arraycopy(mTest_ode_state, 0, state, 0, mDimension); // -- unnecessary -- interpolatorSolver.interpolate(eventData.time, state);
      double timeBefore = state[mTimeIndex];
      problem.action();
//      if (problem instanceof DiscontinuityData) {
//        if (((DiscontinuityData) problem).discontinuity.shouldReinitialize()) reinitialize(); // For discontinuities, reinitialize only if the result is true
//      }
//      else 
        reinitialize();
      state = mODE.getState(); // who knows if the event changes the ODE array pointer?
      if (timeBefore!=state[mTimeIndex]) { // If the event changes the time, it is meaningless to try to complete a step
        mZenoCounter = 0;
        mLastEventData = null;
        mCurrentEventData = null;
        problem.reset(state); // and that this event just happened has no meaning
        return (problem.getTime()-tBegin);
      }
      if (mLastEventData!=null) {
        if (Math.abs(mLastEventDataTime-problem.getTime())<mProximityThreshold) {
          mZenoCounter++;
        }
        else mZenoCounter = 0;
      }
      mLastEventData = problem;
      mLastEventDataTime = problem.getTime(); // because the internal value of lastEventData will change
      return (problem.getTime()-tBegin);
    }
  }

  /**
   * Steps the ODE with events
   * @return
   */
  private double stepWithEvents() {
    double[] state = mODE.getState();
    if (mZenoMaximumAllowedTimes>0 && mZenoCounter>mZenoMaximumAllowedTimes) {
      if (callZenoAction(state)) return 0;
    }

    double tBegin = state[mTimeIndex];

    // Check for a 0 rate for time
    double[] rate = mSolverEngine.getCurrentRate();
    if (tBegin+rate[mTimeIndex]==tBegin) return 0;

    double tEnd = tBegin + mStepSize;
    
//    discontinuityAtEnd = null; // Do NOT clean the possible discontinuity
    double max_t = mSolverEngine.getMaximumTime(mHasDiscontinuities); // This detects discontinuities, if there are any
    if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver at "+state[mTimeIndex]); //$NON-NLS-1$
    double tTest = mRunsForwards ? Math.min(tBegin+mMaxEventStep,tEnd) : Math.max(tBegin-mMaxEventStep,tEnd);

    int counter = 0;
    while (true) {
      ProblemData problem=null;
      mCurrentEventData = null;
      boolean notYetThere = mRunsForwards ? max_t<tTest : max_t>tTest;
      if (notYetThere) { // This is required because the interpolatorSolver cannot do an interpolation backwards from max_t
        mSolverEngine.bestInterpolate(max_t, mTest_ode_state);
//        System.err.println ("Cehcing for event from "+state[mTimeIndex]+" to "+max_t);

        problem = findFirstEvent(state,max_t,mTest_ode_state); // Find the first event
        if (problem==null) problem = mDiscontinuityAtEnd; // If no event, check for discontinuity (but regular events are acted on first)
        if (problem==null) {
          System.arraycopy(mTest_ode_state,0,state,0,mDimension); // Update the current state
          // faster than interpolatorSolver.interpolate(max_t, state), since test_ode_state doesn't change if no event is found
          updateEventsAndDiscontinuities(state[mTimeIndex]);
          max_t = mSolverEngine.internalStep(mHasDiscontinuities);
//          if (discontinuityAtEnd!=null) {
//            System.err.println ("Max t = "+max_t+" discAtEnd = "+discontinuityAtEnd);
//            System.err.println ("tTest = "+tTest);
//          }
          if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver looking for an event at "+state[mTimeIndex]); //$NON-NLS-1$
          if ((++counter)>mMaxInternalSteps) return error(ERROR.TOO_MANY_STEPS_ERROR,"The solver exceeded the number of internal steps at "+state[mTimeIndex]); //$NON-NLS-1$
          continue;
        }
      }
      else {
//        System.out.println("Do the Interpolate for t="+tTest+" when max_t = "+max_t+ " and tBegin = "+tBegin);
        doTheInterpolation (tTest, mTest_ode_state);
//        System.err.println ("Checking for event from "+state[mTimeIndex]+" to "+tTest);

        problem = findFirstEvent(state,tTest,mTest_ode_state); // Find the first event
//        if (discontinuityAtEnd!=null) {
//          System.err.println ("Disc at end used "+discontinuityAtEnd);
//          System.err.println (" at time "+discontinuityAtEnd.time);
//        }
        if (problem==null && mDiscontinuityAtEnd!=null && mDiscontinuityAtEnd.time<=tTest) problem = mDiscontinuityAtEnd; // If no event, check for discontinuity (but regular events are acted on first)
        if (problem==null) {
          if (tTest==tEnd) {
            System.arraycopy(mTest_ode_state, 0, state, 0, mDimension);
            updateEventsAndDiscontinuities(state[mTimeIndex]);
            return tEnd-tBegin;
          }
          tTest = mRunsForwards ? Math.min(tTest+mMaxEventStep,tEnd) : Math.max(tTest-mMaxEventStep,tEnd);
          continue;
        }
      }
      // There was an event or a discontinuity
      mCurrentEventData = problem;
      if (mUseBestInterpolation) mSolverEngine.bestInterpolate(problem.getTime(), state); // So that we reinitialize with the best possible approximation 
      else System.arraycopy(mTest_ode_state, 0, state, 0, mDimension); // -- unnecessary -- interpolatorSolver.interpolate(eventData.time, state);
      double timeBefore = state[mTimeIndex];
      boolean wantsToQuit = problem.action();
//      if (problem instanceof DiscontinuityData) {
//        if (((DiscontinuityData) problem).discontinuity.shouldReinitialize()) reinitialize(); // For discontinuities, reinitialize only if the result is true
//      }
//      else 
        reinitialize();
      counter = 0;
      state = mODE.getState(); // who knows if the event changes the ODE array pointer?
      if (timeBefore!=state[mTimeIndex]) { // If the event changes the time, it is meaningless to try to complete a step
        mZenoCounter = 0;
        mLastEventData = null;
        mCurrentEventData = null;
        problem.reset(state);
        return (problem.getTime()-tBegin);
      }
      if (mLastEventData!=null) {
        if (Math.abs(mLastEventDataTime-problem.getTime())<mProximityThreshold) {
          mZenoCounter++;
          if (mCoalesceCloseEvents) wantsToQuit = false; // If the events are too close, do not quit (this avoids unnecessary redraw)
        }
        else mZenoCounter = 0;
      }
      mLastEventData = problem;
      mLastEventDataTime = problem.getTime(); // because the internal value of lastEventData will change
      if (wantsToQuit) return (problem.getTime()-tBegin);
      if (mZenoMaximumAllowedTimes>0 && mZenoCounter>mZenoMaximumAllowedTimes) {
        if (callZenoAction(state)) return (problem.getTime()-tBegin);
      }
//      if (notYetThere) { // Respect the user's reading step (Is this really a good idea???)
//        max_t = runsForwards ? Math.min(max_t, interpolatorSolver.getMaximumTime()) : Math.max(max_t, interpolatorSolver.getMaximumTime());
//      }
//      else 
      mDiscontinuityAtEnd = null;
      max_t = mSolverEngine.getMaximumTime(mHasDiscontinuities); // This detects discontinuities, if there are any

      if (!Double.isNaN(problem.getMaxAdvance())) { // If a short-duration step was found, this helps avoid recurrent events
        max_t = mRunsForwards ? Math.min(max_t, problem.getMaxAdvance()) : Math.max(max_t, problem.getMaxAdvance());  
      }
      if (Double.isNaN(max_t)) return error (ERROR.INTERNAL_SOLVER_ERROR,"Error when stepping the solver after an event at "+state[mTimeIndex]); //$NON-NLS-1$
//      lastEventData = eventData;
//      lastEventDataTime = lastEventData.time; // because the internal value of lastEventData will change
    } // end of while
  }

  private void updateEventsAndDiscontinuities (double time) {
    for (EventData data : mEventList)                 data.findPosition (time,data.h); // Update the events
    for (DiscontinuityData data : mDiscontinuityList) data.findPosition (time,data.h); // Update the discontinuities
  }
  
  /**
   * A Zeno effect has been detected. Call the listeners.
   * If there are no listeners registered, then an error is issued, including possibly throwing an exception
   * @return true if the solver should stop the step and return a 0 step size
   */
  private boolean callZenoAction(double[] state) {
    if (mZenoList.isEmpty()) {
      error (ERROR.ZENO_EFFECT,"A Zeno-like effect has been detected.\nLast event was "+ //$NON-NLS-1$
          mLastEventData.getProblem() +" which took place at "+mLastEventDataTime); //$NON-NLS-1$
      return true;
    }
    boolean returnAtZeno = false;
    for (ZenoEffectListener listener : mZenoList) if (listener.zenoEffectAction(mLastEventData.getProblem(), state)) returnAtZeno = true;
    mZenoCounter = 0;
    return returnAtZeno; 
  }

  /**
   * Returns the first event that will take place next in the interval [current_state[timeIndex],tFinal].
   * @param double[] final_state contains the state at tFinal, if an event is found, it will contain the state at the event.
   * If no event is found, the final_state array will remain untouched.
   * The interpolator solver must be able to interpolate correctly in this interval!!!
   * Returns null if no event takes place in that interval. 
   */
  private EventData findFirstEvent(double[] current_state, double tFinal, double[] final_state) {
    mNumberOfAttempts = 0;
    // -- Find which events have happened
    EventData happensAtT1 = happensRightNow(current_state[mTimeIndex],final_state, mHappened,"at t1");
//    if (happensAtT1==null) System.err.println ("Event not found");
    if (happensAtT1!=null) {
//      System.err.println ("Event found before "+tFinal+" at t="+current_state[mTimeIndex]);
      happensAtT1.time = current_state[mTimeIndex]; 
      happensAtT1.maxAdvance=tFinal; // limit the advance of the solver from this event. This helps avoid recurrent events
      System.arraycopy(current_state, 0, final_state, 0, mDimension);
      return happensAtT1;
    }
    // -- Check for no event
    if (mHappened.isEmpty()) return null; // IMPORTANT! final_state remains unchanged

//    if (Math.abs(tFinal-current_state[timeIndex])<Double.MIN_VALUE) {
//      System.err.println ("dt = 0 at "+tFinal);
////      if (true) System.exit(0);
//      return happened.get(0); // Any event that could have happened actually happened
//    }

    // --  Search until found or maximum of attempts is reached
    boolean doItAgain = true;
    for (EventData eventData : mEventList) eventData.hAfter = eventData.h; // evaluated in happensRightNow() (since no event happened at T1) 
    while (doItAgain) {
      mNumberOfAttempts++;
      double tTest = nextPointToCheck (mHappened, current_state[mTimeIndex], tFinal); // The value of t where the event is expected to be
      doTheInterpolation (tTest, mIntermediate_ode_state);
      // Check if we discover an event at t1. This (short duration) event was not detected in [t1,tFinal]
      EventData shortDurationEvent = happensRightNow(current_state[mTimeIndex],mIntermediate_ode_state, mTemp_list, "short");
//      if (shortDurationEvent==null) System.err.println ("Event not found");
      if (shortDurationEvent!=null) { // This was a short-duration event!
        shortDurationEvent.time = current_state[mTimeIndex]; 
        shortDurationEvent.maxAdvance=tTest; // Try to avoid infinite loops
        System.arraycopy(current_state, 0, final_state, 0, mDimension);
        return shortDurationEvent;
      }
      
      if (mTemp_list.isEmpty()) { // No event in [t1,tTest]
//        System.err.println ("List is empty");
        // check for event in tTest. This is a reduced check because all other possibilities were dealt with in happensRightNow()
        EventData happensInTtest=null;
        for (EventData eventData : mHappened) { 
          if (eventData.currentPosition==EventData.POSITIVE) { 
            if (eventData.h<eventData.event.getTolerance()) { happensInTtest = eventData; break; } // This one is the event
          }
          else { // NEGATIVE and crossing event
            if (eventData.h>-eventData.event.getTolerance()) { happensInTtest = eventData; break; } // This one is the event
          }
        }
        if (happensInTtest!=null) { 
          happensInTtest.time = tTest; 
          happensInTtest.maxAdvance=tFinal; //Double.NaN;
          System.arraycopy(mIntermediate_ode_state, 0, final_state, 0, mDimension);
          return happensInTtest;
        }
        // repeat in [tTest,tFinal]
        System.arraycopy(mIntermediate_ode_state, 0, current_state, 0, mDimension);
        for (EventData eventData : mEventList) eventData.findPosition(current_state[mTimeIndex],eventData.h);
        // -- Find which events have happened
        EventData happensNowInTtest = happensRightNow(current_state[mTimeIndex],final_state, mHappened, "at tTest");
//        if (happensNowInTtest==null) System.err.println ("Event not found");
        if (happensNowInTtest!=null) {
          happensNowInTtest.time = current_state[mTimeIndex]; 
          happensNowInTtest.maxAdvance=tFinal; //Double.NaN;
          System.arraycopy(current_state, 0, final_state, 0, mDimension);
          return happensNowInTtest;
        }
      }
      else { // temp_list is not null. I.e. there is at least one event in [t1,tTest] 
        // check for event in tTest. This is a reduced check because all other possibilities were dealt with in happensRightNow()
        // If there is any event previous to this, then this is not the point we are looking for.
        boolean notPreviousFound=true;
        EventData happensInTtest=null;
        for (EventData data : mTemp_list) {
          if (data.currentPosition==EventData.POSITIVE) { 
            if (data.h<=-data.event.getTolerance()) { notPreviousFound = false; break; } // I happened earlier, so this is not the point
            // else if (data.h<data.generalEvent.getTolerance()) no need to check since data.h < 0
            happensInTtest = data; // If there is no previous one, then it is me
          }
          else { // NEGATIVE and crossing event
            if (data.h>=data.event.getTolerance()) { notPreviousFound = false; break; } // I happened earlier, so this is not the point
            // else if (data.h>-data.generalEvent.getTolerance()) // no need to check since data.h>0 
            happensInTtest = data; // If there is no previous one, then it is me
          }
        }
        if (notPreviousFound && happensInTtest!=null) { // This is the event!
          happensInTtest.time = tTest; 
          happensInTtest.maxAdvance=tFinal; //Double.NaN;
          System.arraycopy(mIntermediate_ode_state, 0, final_state, 0, mDimension);
          return happensInTtest; 
        }
        tFinal = tTest; 
        System.arraycopy(mIntermediate_ode_state, 0, final_state, 0, mDimension);
        for (EventData eventData : mEventList) eventData.hAfter = eventData.h;
        mHappened.clear();
        mHappened.addAll(mTemp_list);
      }
//       Check for the iteration limit
//      for (EventData data : happened) {
//        System.err.println ("Happened "+data.generalEvent+"\nCurrent state: ");
//        for (int i=0; i<dimension; i++) System.err.print(current_state[i]+", ");  
//        System.err.println ("\nFinal   state: ");
//        for (int i=0; i<dimension; i++) System.err.print(final_state[i]+", ");  
//        System.err.println ("\n");
//      }
      for (EventData data : mHappened) {
        if (mNumberOfAttempts>data.event.getMaxIterations()) { doItAgain = false; break; }
      }
    }
    
    // If this happens, the event is most likely poorly designed!
    EventData remaining = mHappened.get(0); // The event is any of those which remain in the list of happened
    error (ERROR.EVENT_NOT_FOUND,"Warning : Event not found after "+ mNumberOfAttempts+ " attempts at t=" + current_state[mTimeIndex]  //$NON-NLS-1$ //$NON-NLS-2$
        + " h = "+ remaining.h //$NON-NLS-1$
        + ".\nPlease check the code of your event, decrease the initial step size, the tolerance of the solver," //$NON-NLS-1$
        +	"\nor the event maximum step, or increase the maximum number of attempts." //$NON-NLS-1$
        + "\nFirst event remaining in the queue: "+remaining.event); //$NON-NLS-1$

    remaining.time = (current_state[mTimeIndex]+tFinal)/2;
    remaining.maxAdvance=Double.NaN;
    mSolverEngine.bestInterpolate(remaining.time, final_state);
//    if (true) System.exit(0);
    return remaining;
  }

  /**
   * Fills the list with all events that happen between the current time and the provided final time.
   * Returns an event if it happens at the initial point, null otherwise
   */
  private EventData happensRightNow(double currentTime, double[] final_state, List<EventData> list, String id) {
    list.clear();
    for (EventData eventData : mEventList) {
      eventData.h = eventData.event.evaluate(final_state);
//      interpolatorSolver.getODE().debugTask();
//      System.err.print(id+" Buscando evento: en t = "+currentTime +" t2 = "+final_state[timeIndex]+" h = "+eventData.h + " h before = "+eventData.hBefore); 
//      System.err.println(" Pos : = "+eventData.currentPosition); 

      switch (eventData.currentPosition) {
        default : 
        case EventData.POSITIVE : if (eventData.h<=0) list.add(eventData); break; // This event happens! 
        case EventData.SMALL_POSITIVE :
          if (eventData.h<=0 && (eventData.positiveFlag || eventData.eventType==StateEvent.STATE_EVENT)) return eventData;
          break;
        case EventData.ZERO :
          if (eventData.h<0) {
            if (eventData.positiveFlag || eventData.eventType==StateEvent.STATE_EVENT) return eventData;
          }
          else if (eventData.h>0) {
            if (eventData.negativeFlag && eventData.eventType==StateEvent.CROSSING_EVENT) return eventData;
          }
          break;
        case EventData.SMALL_NEGATIVE :
          if (eventData.eventType==StateEvent.STATE_EVENT) {
            if (eventData.h<=-eventData.event.getTolerance()) return eventData;
          }
          else if (eventData.eventType==StateEvent.CROSSING_EVENT) {
            if (eventData.negativeFlag && eventData.h>=0) return eventData;
          }
          break;
        case EventData.NEGATIVE : 
          if (eventData.eventType==StateEvent.CROSSING_EVENT) { if (eventData.h>=0) list.add(eventData); }
          else if (eventData.eventType==StateEvent.STATE_EVENT) { // This should NOT happen, but I save the problem by saying that this event happens right now!
            error(ERROR.ILLEGAL_EVENT_STATE, "The system started from an illegal state at "+currentTime+ " for the state event "+eventData.event); //$NON-NLS-1$ //$NON-NLS-2$
            return eventData;
          }
          break; 
      } // end of switch
    } // end of for
    return null;
  }
  
  /**
   * The first point where events estimate the crossing takes place
   */
  private double nextPointToCheck (List<EventData> list, double t1, double t2) {
    double tFirst = t2, dt = (t2 - t1), tMiddle = (t1+t2)/2;
    if (mRunsForwards) for (EventData eventData : list) {
      switch (eventData.event.getRootFindingMethod()) {
        default : 
        case StateEvent.BISECTION : tFirst = Math.min(tFirst, tMiddle); break;
        case StateEvent.SECANT : 
//          System.err.println ("hBefore = "+eventData.hBefore);
//          System.err.println ("hAfter = "+eventData.hAfter);
//          System.err.println ("dt = "+dt);
//          System.err.println ("t1 = "+t1);
//          System.err.println ("tFirst = "+tFirst);
          tFirst = Math.min(tFirst, t1 - eventData.hBefore * dt / (eventData.hAfter - eventData.hBefore)); break;
      }
    }
    else for (EventData eventData : list) {
      switch (eventData.event.getRootFindingMethod()) {
        default : 
        case StateEvent.BISECTION : tFirst = Math.max(tFirst, tMiddle); break;
        case StateEvent.SECANT : tFirst = Math.max(tFirst, t1 - eventData.hBefore * dt / (eventData.hAfter - eventData.hBefore)); break;
      }
    }
    return tFirst;
  }



  private interface ProblemData {
    static final public int POSITIVE = 2;
    static final public int SMALL_POSITIVE = 1;
    static final public int ZERO = 0;
    static final public int SMALL_NEGATIVE = -1;
    static final public int NEGATIVE = -2;

    public void reset (double[] state); 
    public void findPosition (double _time, double hValue);
    public double getTime();
    public double getMaxAdvance();
    public boolean action();
    public Object getProblem();
  }
  
  /**
   * A class that handles the information of a StateEvent for given states
   * @author Francisco Esquembre
   *
   */
  private class EventData implements ProblemData {
      
    StateEvent event; // The event itself
    boolean positiveFlag; // Whether the state has ever been positive (since last reset)
    boolean negativeFlag; // Whether the state has ever been negative (since last reset)
    final int eventType; // The type of event (STATE_EVENT, POSITIVE_EVENT, or CROSSING_EVENT)
    int currentPosition; // In which side of the [-Tol,+Tol] is h at the current time (POSITIVE, SMALL_POSITIVE, etc.)
    double hBefore;  // The value of h at the current time
    double hAfter;   // The value of h at the end of a period
    double h;        // The value of time at the test point
    double time;     // the next time at which the event took place
    double maxAdvance; // The maximum the solver must advance after this event happens
    
    EventData (StateEvent _event, double[] state) {
      event = _event;
      eventType = event.getTypeOfEvent();
      reset (state);           
    }
    
    public double getTime() { return this.time; }
    
    public double getMaxAdvance() { return this.maxAdvance; }
    
    public boolean action() { return event.action(); }
    
    public Object getProblem() { return event; }
    
    public void reset (double[] state) {
//      System.out.println ("Resetting event at "+state[timeIndex] + " current h = "+hBefore);
      positiveFlag = negativeFlag = false;
      double h = event.evaluate(state);
      findPosition (state[mTimeIndex],h);
      // At initialization times, a small posi(nega)tive is actually a posi(nega)tive
      if (mCurrentEventData!=this) { // unless this event just happened
//        System.out.println ("Recalculating this event at "+state[timeIndex] + " current h = "+hBefore);
        if (eventType==StateEvent.CROSSING_EVENT) {
          if (h>0) positiveFlag = true;
          else if (h<0) negativeFlag = true;
        }
        else if (eventType==StateEvent.POSITIVE_EVENT) {
          if (h>0) positiveFlag = true;
        }
//        System.out.println ("Positive flag = "+positiveFlag);
//        System.out.println ("Negative flag = "+negativeFlag);
      }
//      System.err.println ("Reiniting event at "+state[timeIndex] + "new h = "+generalEvent.evaluate(state));
    }

    public void findPosition (double _time, double hValue) {
      hBefore = hValue;
      if      (hBefore>= event.getTolerance()) { currentPosition = POSITIVE; positiveFlag = true; }
      else if (hBefore>0) currentPosition = SMALL_POSITIVE; 
      else if (hBefore==0) currentPosition = ZERO;
      else if (hBefore>-event.getTolerance()) currentPosition = SMALL_NEGATIVE;
      else { currentPosition = NEGATIVE; negativeFlag = true; }
      if (eventType==StateEvent.STATE_EVENT && currentPosition==NEGATIVE) {
        String msg = "The state event " + event+" is in an illegal state: "+hBefore+ " at "+_time;//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (mLastEventData==null) msg +="\nThere was no previous event"; //$NON-NLS-1$ 
        else msg +="\nLast previous event was "+mLastEventData.getProblem()+", which took place at "+mLastEventDataTime; //$NON-NLS-1$ //$NON-NLS-2$
        error(ERROR.ILLEGAL_EVENT_STATE, msg);
      }
    }

  }
  
  /**
   * A class that handles the information of a StateEvent for given states
   * @author Francisco Esquembre
   *
   */
  private class DiscontinuityData implements ProblemData {
      
    Discontinuity discontinuity; // The event itself
    boolean positiveFlag; // Whether the state has ever been positive (since last reset)
    boolean negativeFlag; // Whether the state has ever been negative (since last reset)
    int currentPosition; // In which side of the [-Tol,+Tol] is h at the current time (POSITIVE, SMALL_POSITIVE, etc.)
    double hBefore;  // The value of h at the current time
    double h;        // The value of time at the test point
    double time;     // the next time at which the event took place
//    double maxAdvance; // The maximum the solver must advance after this event happens
    
    DiscontinuityData (Discontinuity _disc, double[] state) {
      discontinuity = _disc;
      reset (state);           
    }
    
    public double getTime() { return this.time; }
    
    public double getMaxAdvance() { return Double.NaN; }
    
    public boolean action() { return discontinuity.action(); }
    
    public Object getProblem() { return discontinuity; }

    public void reset (double[] state) {
//      System.out.println ("Resetting event at "+state[timeIndex] + " current h = "+hBefore);
      positiveFlag = negativeFlag = false;
      double h = discontinuity.evaluate(state);
      findPosition (state[mTimeIndex],h);
      // At initialization times, a small posi(nega)tive is actually a posi(nega)tive
      if (mCurrentEventData!=this) { // unless this event just happened
        //        System.out.println ("Recalculating this event at "+state[timeIndex] + " current h = "+hBefore);
        if (h>0) positiveFlag = true;
        else if (h<0) negativeFlag = true;
        //        System.out.println ("Positive flag = "+positiveFlag);
        //        System.out.println ("Negative flag = "+negativeFlag);
      }
//      System.err.println ("Reiniting event at "+state[timeIndex] + "new h = "+generalEvent.evaluate(state));
    }

    public void findPosition (double _time, double hValue) {
      hBefore = hValue;
      if      (hBefore>= discontinuity.getTolerance()) { currentPosition = POSITIVE; positiveFlag = true; }
      else if (hBefore>0) currentPosition = SMALL_POSITIVE; 
      else if (hBefore==0) currentPosition = ZERO;
      else if (hBefore>-discontinuity.getTolerance()) currentPosition = SMALL_NEGATIVE;
      else { currentPosition = NEGATIVE; negativeFlag = true; }
//      System.err.println ("time = "+_time);
//      System.err.println ("HBefore = "+hBefore);
//      System.err.println ("Position = "+currentPosition);
//      System.err.println ("flags = "+positiveFlag+ ", "+negativeFlag+"\n");
    }

  }
  
 /**
   * A Discontinuity that handles the discontinuities of a DDE
   * @author Francisco Esquembre
   *
   */
  private class DDEDiscontinuity implements Discontinuity {
      
    DelayDifferentialEquation mDDE; // The dde
    List<Double> mDiscontinuities = new ArrayList<Double>();
//    int mDoIndex; This is not needed
    int[] mDiIndex;
    double[] mRateForCorrections;

    private DDEDiscontinuity(DelayDifferentialEquation dde) {
      mDDE = dde;
    }
    
    private void initialize(double[] state) {
      if (mRunsForwards) mDiscontinuities.add(Double.NEGATIVE_INFINITY);
      else mDiscontinuities.add(Double.POSITIVE_INFINITY);
      double[] initDisc = mDDE.getInitialConditionDiscontinuities();
      if (initDisc!=null) for (double disc : initDisc) mDiscontinuities.add(disc);
//      System.err.println("Adding disc at (timeIndex=)"+timeIndex+",  " + state[timeIndex]);
      mDiscontinuities.add(state[mTimeIndex]);
      double[] delays = mDDE.getDelays(state);
      mDiIndex = new int[delays.length];
      reset(state);
      mRateForCorrections = new double[mDimension];
    }
    
    void reset(double[] state) {
//      if (mDiIndex==null) initialize(state);
//      mDoIndex = 0;
      for (int i=0; i<mDiIndex.length; i++) mDiIndex[i] = 0;
      update(state);
    }
    
    private void update(double[] state) {
      double[] delays = mDDE.getDelays(state);
      int n = delays.length;
      int discCount = mDiscontinuities.size();
      // Now find the mDi
      if (mRunsForwards) {
        for (int i=0; i<n; i++) {
          double limit = state[mTimeIndex]-delays[i]+getTolerance(); // add the tolerance so that to skip round-off errors
          for (int index=mDiIndex[i]; index<discCount; index++) {
            double disc = mDiscontinuities.get(index);
//            System.err.print("Disc["+index+"] = "+disc+" > limit "+limit+ "... ");
            if (disc>limit) {
//              System.err.println("YES");
              mDiIndex[i] = index;
              break;
            }
//            System.err.println("NO");
          }
        }
      }
      else {
        for (int i=0; i<n; i++) {
          double limit = state[mTimeIndex]-delays[i]-getTolerance(); // add the tolerance so that to skip round-off errors
          for (int index=mDiIndex[i]; index<discCount; index++) {
            double disc = mDiscontinuities.get(index);
            if (disc<limit) {
              mDiIndex[i] = index;
              break;
            }
          }
        }
      }
//      System.err.println("Discont. are : ");
//      for (int i=0; i<discCount; i++) System.err.println("disc["+i+"] = "+mDiscontinuities.get(i));
//      System.err.println("di's are : ");
//      for (int i=0; i<n; i++) System.err.println("mdi["+i+"] = "+mDiIndex[i]);
//      System.err.println("\n");
    }
    
    // implementation of Discontinuity
    
    public double evaluate(double[] state) {
      double t = state[mTimeIndex];
      double[] delays = mDDE.getDelays(state);
      int n = delays.length;
      if (mRunsForwards) {
        double min = Double.POSITIVE_INFINITY;
        for (int i=0; i<n; i++) {
          double di = mDiscontinuities.get(mDiIndex[i]);
//          System.err.println("Discont = "+di);
//          System.err.println("delay = "+delays[i]);
//          System.err.println("time = "+t);
//          System.err.println("min = "+(delays[i]+di-t));
          min = Math.min(min, delays[i]+di-t);
        }
//        System.err.println("Time = "+state[1]+ " Value returned = "+min);
        return min;
      }
      else {
        double max = Double.NEGATIVE_INFINITY;
        for (int i=0; i<n; i++) {
          double di = mDiscontinuities.get(mDiIndex[i]);
          max = Math.max(max, delays[i]+di-t);
        }
        return max;
      }
    }
    
    /**
     * Advances time (and state) past the delay
     * @param state
     * @return
     */
    private double correctTime(double[] state) {
      double step = mDDEdiscontinuityTolerance/20;
      int counter=0;
      if (mRunsForwards) {
        while ((++counter)<mDDEdiscontinuityMaxIterations) {
          mDDE.getRate(state, mRateForCorrections); // advance using Euler
          mSolverEngine.getStateHistory().addIntervalData(new EulerIntervalData(state,mRateForCorrections,state[mTimeIndex]+step));
          for (int i=0; i<mDimension;i++) state[i] += step*mRateForCorrections[i];
//          state[timeIndex] += step;
          double h = evaluate(state);
          if (h<0) {
//        	  System.out.println("Counter -------------- "+counter+ "/"+mDDEdiscontinuityMaxIterations);
        	  return state[mTimeIndex];
          }
        }
      }
      else {
        while ((++counter)<mDDEdiscontinuityMaxIterations) {
          mDDE.getRate(state, mRateForCorrections);
          mSolverEngine.getStateHistory().addIntervalData(new EulerIntervalData(state,mRateForCorrections,state[mTimeIndex]-step));
          for (int i=0; i<mDimension;i++) state[i] -= step*mRateForCorrections[i];
//          state[timeIndex] -= step;
          double h = evaluate(state);
          if (h>0) return state[mTimeIndex];
        }
      }
      return Double.NaN;
    }
    
    public boolean action() {
      double[] state = mDDE.getState();
//      System.err.println("Found delay discontinuity at "+state[1]+ " --------------------------------------");
//      System.err.println ("Correct state should be "+correctTime(state));
      double time = correctTime(state); // Automatically find the moment where it crosses the discontinuity
//      System.out.println("Corrected time ="+time);
//      interpolatorSolver.interpolate(time, state);
//      state[timeIndex] = time;
      mDiscontinuities.add(time);
      EventData eventData = findFirstEvent(state,time,state); // Find a possible new event because of this time increase!
      if (eventData!=null) {
        mCurrentEventData = eventData;   
        // No need to interpolate because the state was changed in the call to findFirstEvent
//        if (mUseBestInterpolation) mSolverEngine.bestInterpolate(problem.getTime(), state); // So that we reinitialize with the best possible approximation 
//        else System.arraycopy(mTest_ode_state, 0, state, 0, mDimension); // -- unnecessary -- interpolatorSolver.interpolate(eventData.time, state);
        eventData.action();
      }
      // update(state); will be done by the reinitialize() method of the solver
      return true; // implies reset
    }
    
    public double getTolerance() { return mDDEdiscontinuityTolerance; }

  }
  
  
} // End of class

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

/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

import java.util.*;

import org.opensourcephysics.controls.OSPLog;

/**
 * ODEInterpolatorEventSolver implements ODEEventSolver using any ODESolverInterpolator as the solver engine.
 * Includes advanced features, such as detection of Zeno-effects
 * @author Francisco Esquembre
 *
 */
public class ODEInterpolatorEventSolver implements ODEEventSolver, ODEAdaptiveSolver {
  /**
   * Value of the error code when the solver produces an internal error, such as when it cannot do an internal step 
   */
  static final public int INTERNAL_SOLVER_ERROR=101;
  /**
   * Value of the error code when the event was not found using the maximum number of attempts
   */
  static final public int EVENT_NOT_FOUND=102;
  /**
   * Value of the error code when an event of type STATE_EVENT was left in an illegal (negative) state
   */
  static final public int ILLEGAL_EVENT_STATE=103;
  /**
   * Value of the error code when a Zeno effect is detected without a user action
   */
  static final public int ZENO_EFFECT=104;
  /**
   * Value of the error code when the solver exceeds the number of internal steps
   */
  static final public int TOO_MANY_STEPS_ERROR=105;
  
  // Configuration variables
  private boolean enableExceptions=true; // Whether to throw an exception when an error is produced. 
  protected boolean useBestInterpolation=false; // Always use the best interpolation the solvers provide (even if it is more time consuming)
  private double stepSize = 0.1;         // The step size for reading data from the solver
  private double absoluteTolerance = Double.NaN; // The absolute tolerance for adaptive methods
  private double relativeTolerance = Double.NaN; // The relative tolerance for adaptive methods
  private double maxEventStep=Double.POSITIVE_INFINITY; // The maximum advance before checking for an event
  private int zenoMaximumAllowedTimes=500; // The number of times that a separation smaller than the threshold must occur to declare a Zeno effect
  private double proximityThreshold=2*Double.MIN_VALUE; // The threshold to consider two events as simultaneous or indicate a possible Zeno effect
  private boolean coalesceCloseEvents = true; // whether to coalesce close events (even if the action returns true)
  private int maxInternalSteps = 10000;
  
  // Implementation variables
  private boolean runsForwards=stepSize>0;
  private int dimension; // The dimension of the problem (including time)
  private int timeIndex; // The index of the time state, i.e. dimension-1
  private int errorCode=ODEAdaptiveSolver.NO_ERROR; // The error code
  private String errorMessage="No error";
  private int numberOfAttempts=0;  // Actual number of attempts needed to locate the last event
  private double[] test_ode_state; // a place holder for intermediate states
  private double[] intermediate_ode_state; // a second place holder for intermediate states
  protected ODESolverInterpolator interpolatorSolver; // The underlying solver engine
  private List<EventData> eventList = new ArrayList<EventData>(); // The list of events added by the user
  private List<EventData> happened  = new ArrayList<EventData>(); // The list of events that take place in a given interval
  private List<EventData> temp_list = new ArrayList<EventData>(); // a temporary list of events
  private double lastEventDataTime=Double.NaN; // Remember the time of the last event
  private EventData lastEventData=null; // Remember the last event
  private EventData currentEventData=null; // Placeholder for the current event, used to reset some events properly
  private int zenoCounter=0; // Count how many close events have happened
  private List<ZenoEffectListener> zenoList = new ArrayList<ZenoEffectListener>(); // list of Zeno Listeners
  
  // --- Constructor
  
  /**
   * Instantiates an object of this class.
   * @param ode an ODE object
   * @param solver an ODESolverInterpolator object
   */
  public ODEInterpolatorEventSolver(ODESolverInterpolator solver) { interpolatorSolver = solver; }

  /**
   * Returns the interpolator solver
   * @return
   */
  public ODESolverInterpolator getSolver() { return interpolatorSolver; }
  
  // --- Configuration methods
  
  /**
   * Enables runtime exceptions if there is any error condition. 
   * If exceptions are disabled, the solver prints a warning to the standard error output and continues.
   * Exceptions are enabled by default.
   * @param enable boolean
   */
  public void setEnableExceptions(boolean enable) { enableExceptions = enable; }

  /**
   * Asks adaptive solvers to estimate the best initial step after reinitialize().
   * If false, the given initial step (as set by setComputationStepSize()) is used.
   * @param _estimate
   */
  public void setEstimateFirstStep(boolean _estimate) { interpolatorSolver.setEstimateFirstStep(_estimate); }

  /**
   * Request the internal solver to always use the best interpolation it can provide,
   * even if it is more time-consuming than the standard one. For examples, some RK
   * solvers will re-step from the initial step every time they are asked fro their best
   * interpolatation, which is very expensive in terms of function evaluations.
   * Best interpolation is however always used (irrespective of this parameter) to 
   * return the state after a successful step and after an event takes place. 
   * But standard interpolation is used by default for locating the events.
   * Setting this parameter to true forces the solver to use its best interpolation
   * even for event location.
   * @param _best
   */
  public void setBestInterpolation(boolean _best) {
    this.useBestInterpolation = _best;
  }

  /**
   * Sets the maximum number of internal steps the interpolator can take to reach a 
   * reading step. If the solver takes so large a number (of very small steps) this
   * typically means the solver has reached a singularity and must take too small steps.
   * It can also mean the solver has difficulties to reach the tolerance and the user
   * should consider either increasing this limit, or use another solver. 
   * 
   * @param _steps
   */
  public void setMaximumInternalSteps(int _steps) { maxInternalSteps = _steps; }
  
  /**
   * Sets the interpolator's internal step size. This is the step at which solutions are computed
   * for fixed step methods, and the initial step size (after reinitialize()) for variable
   * step methods. The value is taken absolutely in the direction of the reading step size.
   */
  public void setInternalStepSize(double _stepSize) {
    interpolatorSolver.setStepSize(runsForwards ? Math.abs(_stepSize) : -Math.abs(_stepSize));
  }

  /**
   * Sets the interpolator's internal maximum step size. Has no effect on fixed-step solvers and on QSS methods.
   * @param _stepSize
   */
  public void setMaximumInternalStepSize(double _stepSize) { interpolatorSolver.setMaximumStepSize(_stepSize); }
  
  /**
   * The preferred absolute and relative tolerance desired for the solution if the 
   * underlying solver supports it. If the solver does not support this feature, the 
   * method is ignored. Changing the tolerances may involve a re-computation 
   * of the current step.
   * @param tol
   */
  public void setTolerances(double absTol, double relTol) {
    interpolatorSolver.setTolerances(absoluteTolerance = absTol, relativeTolerance = relTol);
  }

  /**
   * The number of attempts that were required to locate the last event
   * @return
   */
  public int getNumberOfAttempts() { return numberOfAttempts; }

  /**
   * But this the version called by the user when she wants to reinitialize the system.
   * EJS calls autommatically reinitialize(), which respects the currentDataEvent information
   * Users MUST call userReinitialize (or the harder initialize()) whenever they change directly the ODE state,
   * model parameters, or anything that may affect the events (such as the events tolerance, for instance).
   */
  public void userReinitialize() {
    currentEventData = null;
    reinitialize();
  }

  /**
   * Does the minimum (soft) initialization of the solver for a given state.
   * Users MUST call reinitialize (or the harder initialize()) whenever they change directly the ODE state,
   * model parameters, or anything that may affect the events (such as the events tolerance, for instance).
   */
  public void reinitialize() {
    double[] state = interpolatorSolver.getODE().getState();
//    System.err.println ("Reiniting solver at "+state[timeIndex]);
    //    interpolatorSolver.getODE().debugTask();
    interpolatorSolver.reinitialize(state);
    errorCode = ODEAdaptiveSolver.NO_ERROR;
    errorMessage = "No error";
    // The next sentence can produce an error
    for (EventData eventData : eventList) eventData.reset(state);
  }

  // --- Implementation of ODEAdaptiveSolver

  /**
   * Equivalent to setTolerances (tol,0)
   * @param tol
   */
  public void setTolerance(double tol) { setTolerances(tol,0); }

  /**
   * Returns the maximum of the absolute and relative tolerances
   */
  public double getTolerance () { return Math.max(absoluteTolerance, relativeTolerance); }
  
  /**
   * Returns the error code after a step
   */
  public int getErrorCode() { return errorCode; }
  
  /**
   * Returns the error message
   * @return
   */
  public String getErrorMessage() { return errorMessage; }

  /**
   * Sets the maximum step allowed before checking for an event.
   * Default is Double.POSITIVE_INFINITY
   * @param _step
   */
  public void setMaximumEventStep (double _step) { maxEventStep = Math.abs(_step); }

  /**
   * Returns the maximum step allowed before checking for an event.
   * @return
   */
  public double getMaximumEventStep () { return maxEventStep; }

  /**
   * If true, an event closer than the threshold to the previous one 
   * will not return even if the event action returns true. Default is true.
   * @param _coalesce
   */
  public void setCoalesceCloseEvents (boolean _coalesce) { coalesceCloseEvents = _coalesce; }
  
  /**
   * Whether an event closer than the threshold to the previous one 
   * will not return even if the event action returns true
   * @return
   */
  public boolean isCoalesceCloseEvents() { return coalesceCloseEvents; }
  
  /**
   * Sets the threshold that considers two events as close enough for coalescing
   * or indicating a possible Zeno-like effect. Default is 2*Double.MIN_VALUE
   * @param _threshold The small separation that indicates a possible Zeno effect
   */
  public void setEventProximityThreshold (double _threshold) { proximityThreshold = _threshold; }

  /**
   * Return the threshold that considers two events as close enough for coalescing
   * or indicating a possible Zeno-like effect
   */
  public double getEventProximityThreshold () { return proximityThreshold; }

  // --- Implementation of ODEEventSolver

  public void addEvent(StateEvent event) {
    GeneralStateEvent generalEvent;
    if (event instanceof GeneralStateEvent) generalEvent =  (GeneralStateEvent) event;
    else generalEvent = new GeneralStateEventAdapter(event);
    eventList.add(new EventData(generalEvent,interpolatorSolver.getODE().getState()));
  }

  public void removeEvent(StateEvent event) {
    for (EventData data : eventList) {
      if (data.generalEvent instanceof GeneralStateEventAdapter) {
        if ( ((GeneralStateEventAdapter)data.generalEvent).getEvent()==event) {
          if (lastEventData==data) { lastEventData = null; zenoCounter = 0; }
          if (currentEventData==data) currentEventData = null;       
          eventList.remove(data); 
          return; 
        }
      }
      else if (data.generalEvent==event) { 
        if (lastEventData==data) { lastEventData = null; zenoCounter = 0; }
        if (currentEventData==data) currentEventData = null;       
        eventList.remove(data); 
        return; 
      }
    }
  }

  public void removeAllEvents(){
    eventList.clear();  
  }
  
  // --- Implementation of ODESolver
  
  /**
   * Initializes the solver. The step size is used to set the reading step size AND
   * also passed along to the ODESolverInterpolator for internal initialization.
   * The interpolator's internal step size can be changed with setInternalStepSize(double).
   * Calls to setStepSize() will not affect the internal step size of the interpolator.
   */
  public void initialize(double _stepSize) {
    this.stepSize = _stepSize;
    runsForwards = stepSize>0;
    interpolatorSolver.initialize(stepSize);
    double[] state = interpolatorSolver.getODE().getState(); 
    dimension = state.length;
    timeIndex = dimension - 1;
    test_ode_state = new double[dimension];
    intermediate_ode_state = new double[dimension];
    errorCode = ODEAdaptiveSolver.NO_ERROR;
    errorMessage = "No error";
    zenoCounter = 0;
    lastEventData = null;
    currentEventData = null;       
    // The next sentence can produce an error
    for (EventData data : eventList) data.reset(state);
  }

  /**
   * Sets the reading step size. That is the step at which solutions are read from 
   * the equation. Most of the times, these solutions are obtained by interpolation.
   */
  public void setStepSize(double _stepSize) {
    if (this.stepSize==_stepSize) return;
    this.stepSize = _stepSize;
    runsForwards = stepSize>0;
    setInternalStepSize(interpolatorSolver.getStepSize()); // Make sure the interpolator solver runs in the same direction
  }

  /**
   * Returns the reading step
   */
  public double getStepSize() { return stepSize; }
    
  public double step() {
    if (eventList.isEmpty()) return stepWithoutEvents();
    return stepWithEvents();
  }

  /**
   * Take the maximum possible step.
   * For adaptive-step methods, steps up to the maximum possible adaptive step. 
   * For fixed-step methods, steps up to the next step size.
   * If an event is found, it steps up to that event.
   * @return
   */
  public double maxStep() {
    if (eventList.isEmpty()) return maxStepWithoutEvents();
    return maxStepWithEvents();
  }

  // -----------------------------------
  // Zeno-like effects 
  // -----------------------------------

  /**
   * If the solver finds more than _times successive events closer than the proximity threshold,
   * it will consider it a Zeno effect and call the registered ZenoEffectListeners. Default is 500.
   * @param _times The number of times that a separation smaller than the threshold must occur to declare a Zeno effect. 
   * A zero or negative value disables the detection.
   */
  public void setZenoEffectDetection (int _times) { zenoMaximumAllowedTimes = _times; }

  /**
   * Returns the number of successive events closer than the proximity threshold,
   * that will be considered a Zeno effect.
   */
  public int getZenoEffectDetection () { return zenoMaximumAllowedTimes; }

  /**
   * Adds a ZenoEffectListener that will be called if a Zeno-like effect situation occurs.
   */
  public void addZenoEffectListener (ZenoEffectListener _listener) { zenoList.add(_listener); }
  
  /**
   * Removes a previously added ZenoEffectListener 
   */
  public void removeZenoEffectListener (ZenoEffectListener _listener) { zenoList.remove(_listener); }
  
  
  /**
   * Returns the current value of the independent variable
   * @return
   */
  public double getIndependentVariableValue () {
    return interpolatorSolver.getODE().getState()[timeIndex];
  }
  
  public void setZeroZenoCounter() { zenoCounter = 0; }
  
  // -----------------------------------
  // Private methods
  // -----------------------------------

  /**
   * Does the interpolation at the prescribed time.
   * It is written as a separate method so that subclasses may overwrite it
   */
  final protected void doTheInterpolation (double _time, double[] _state) {
    if (useBestInterpolation) interpolatorSolver.bestInterpolate(_time, _state);
    else interpolatorSolver.interpolate(_time, false, _state);
  }
  
  /**
   * Steps the ODE when there are no events
   * @return
   */
  final private double maxStepWithoutEvents() {
    double[] state = interpolatorSolver.getODE().getState();
    
    // Check for a 0 rate for time
    double[] rate = interpolatorSolver.getCurrentRate();
    double tBegin = state[timeIndex];
    if (tBegin+rate[timeIndex]==tBegin) return 0;
    
    double max_t = interpolatorSolver.getMaximumTime();
    if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver at "+state[timeIndex]); //$NON-NLS-1$
    // Make sure the solver is not already at the maximum step
    if (tBegin==max_t) {
      max_t = interpolatorSolver.internalStep();
      if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver at max step at "+state[timeIndex]); //$NON-NLS-1$
    }
    doTheInterpolation (max_t, state);
    return max_t-tBegin;
  }
  
  /**
   * Steps the ODE when there are no events
   * @return
   */
  private double stepWithoutEvents() {
    double[] state = interpolatorSolver.getODE().getState();
    
    // Check for a 0 rate for time
    double[] rate = interpolatorSolver.getCurrentRate();
    double tBegin = state[timeIndex];
    if (tBegin+rate[timeIndex]==tBegin) return 0;
    
    double tEnd = state[timeIndex] + stepSize;
    double max_t = interpolatorSolver.getMaximumTime();
    if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver at "+state[timeIndex]); //$NON-NLS-1$

    int counter = 0;
    // Make sure the solver can reach the expected time
    if (runsForwards) {
      while (max_t<tEnd) { 
        max_t = interpolatorSolver.internalStep();
        if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver forwards at "+state[timeIndex]); //$NON-NLS-1$
        if ((++counter)>maxInternalSteps) { 
          double initTime = state[timeIndex];
          double currentTime = interpolatorSolver.bestInterpolate(tEnd, new double[dimension])[timeIndex];
          return error(TOO_MANY_STEPS_ERROR,"The solver exceeded the maximum of "+maxInternalSteps+ //$NON-NLS-1$  
              " internal steps\nat "+currentTime+ ", starting from "+initTime +" for an step of "+stepSize); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
      }
    }
    else {
      while (max_t>tEnd) {
        max_t = interpolatorSolver.internalStep();
        if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver backwards at "+state[timeIndex]); //$NON-NLS-1$
        if ((++counter)>maxInternalSteps) return error(TOO_MANY_STEPS_ERROR,"The solver exceeded the number of internal steps at "+state[timeIndex]); //$NON-NLS-1$
      }
    }
    doTheInterpolation (tEnd, state);
    return stepSize;
  }

  
  /**
   * Steps the ODE with events
   * @return
   */
  final private double maxStepWithEvents() {
    double[] state = interpolatorSolver.getODE().getState();
    if (zenoMaximumAllowedTimes>0 && zenoCounter>zenoMaximumAllowedTimes) {
      if (callZenoAction(state)) return 0;
    }

    double tBegin = state[timeIndex];
    // Check for a 0 rate for time
    double[] rate = interpolatorSolver.getCurrentRate();
    if (tBegin+rate[timeIndex]==tBegin) return 0;

    double max_t = interpolatorSolver.getMaximumTime();
    if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver at "+state[timeIndex]); //$NON-NLS-1$
    // Make sure the solver is not already at the maximum step
    if (tBegin==max_t) {
      max_t = interpolatorSolver.internalStep();
      if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver forwards at "+state[timeIndex]); //$NON-NLS-1$
    }

    if (lastEventData!=null) {
      if (!Double.isNaN(lastEventData.maxAdvance)) { // If a short-duration step was found, this helps avoid recurrent events
        max_t = runsForwards ? Math.min(max_t, lastEventData.maxAdvance) : Math.max(max_t, lastEventData.maxAdvance);  
      }    
    }
    
    double tTest = runsForwards ? Math.min(tBegin+maxEventStep,max_t) : Math.max(tBegin-maxEventStep,max_t);

    while (true) {
      EventData eventData=null;
      currentEventData = null;       
      doTheInterpolation (tTest, test_ode_state);
      eventData = findFirstEvent(state,tTest,test_ode_state); // Find the first event
      if (eventData==null) {
        if (tTest==max_t) {
          System.arraycopy(test_ode_state, 0, state, 0, dimension);
          for (EventData evtDat : eventList) evtDat.findPosition (state[timeIndex],evtDat.h); // Update the events
          return max_t-tBegin;
        }
        tTest = runsForwards ? Math.min(tTest+maxEventStep,max_t) : Math.max(tTest-maxEventStep,max_t);
        continue;
      }

      // There was an event
      currentEventData = eventData;       
      if (useBestInterpolation) interpolatorSolver.bestInterpolate(eventData.time, state); // So that we reinitialize with the best possible approximation 
      else System.arraycopy(test_ode_state, 0, state, 0, dimension); // -- unnecessary -- interpolatorSolver.interpolate(eventData.time, state);
      double timeBefore = state[timeIndex];
      eventData.generalEvent.action();
      reinitialize();
      state = interpolatorSolver.getODE().getState(); // who knows if the event changes the ODE array pointer?
      if (timeBefore!=state[timeIndex]) { // If the event changes the time, it is meaningless to try to complete a step
        zenoCounter = 0;
        lastEventData = null;
        currentEventData = null;
        eventData.reset(state); // and that this event just happened has no meaning
        return (eventData.time-tBegin);
      }
      if (lastEventData!=null) {
        if (Math.abs(lastEventDataTime-eventData.time)<proximityThreshold) {
          zenoCounter++;
        }
        else zenoCounter = 0;
      }
      lastEventData = eventData;
      lastEventDataTime = eventData.time; // because the internal value of lastEventData will change
      return (eventData.time-tBegin);
    }
  }

  /**
   * Steps the ODE with events
   * @return
   */
  private double stepWithEvents() {
    double[] state = interpolatorSolver.getODE().getState();
    if (zenoMaximumAllowedTimes>0 && zenoCounter>zenoMaximumAllowedTimes) {
      if (callZenoAction(state)) return 0;
    }

    double tBegin = state[timeIndex];

    // Check for a 0 rate for time
    double[] rate = interpolatorSolver.getCurrentRate();
    if (tBegin+rate[timeIndex]==tBegin) return 0;

    double tEnd = tBegin + stepSize;
    double max_t = interpolatorSolver.getMaximumTime();
    if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver at "+state[timeIndex]); //$NON-NLS-1$
    double tTest = runsForwards ? Math.min(tBegin+maxEventStep,tEnd) : Math.max(tBegin-maxEventStep,tEnd);

    int counter = 0;
    while (true) {
      EventData eventData=null;
      currentEventData = null;
      boolean notYetThere = runsForwards ? max_t<tTest : max_t>tTest;
      if (notYetThere) { // This is required because the interpolatorSolver cannot do an interpolation backwards from max_t
        interpolatorSolver.bestInterpolate(max_t, test_ode_state);
        eventData = findFirstEvent(state,max_t,test_ode_state); // Find the first event
        if (eventData==null) {
          System.arraycopy(test_ode_state,0,state,0,dimension); // Update the current state
          // faster than interpolatorSolver.interpolate(max_t, state), since test_ode_state doesn't change if no event is found
          for (EventData evtDat : eventList) evtDat.findPosition (state[timeIndex],evtDat.h); // Update the events
          max_t = interpolatorSolver.internalStep();
          if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver looking for an event at "+state[timeIndex]); //$NON-NLS-1$
          if ((++counter)>maxInternalSteps) return error(TOO_MANY_STEPS_ERROR,"The solver exceeded the number of internal steps at "+state[timeIndex]); //$NON-NLS-1$
          continue;
        }
      }
      else {
        doTheInterpolation (tTest, test_ode_state);
        eventData = findFirstEvent(state,tTest,test_ode_state); // Find the first event
        if (eventData==null) {
          if (tTest==tEnd) {
            System.arraycopy(test_ode_state, 0, state, 0, dimension);
            for (EventData evtDat : eventList) evtDat.findPosition (state[timeIndex],evtDat.h); // Update the events
            return tEnd-tBegin;
          }
          tTest = runsForwards ? Math.min(tTest+maxEventStep,tEnd) : Math.max(tTest-maxEventStep,tEnd);
          continue;
        }
      }
      // There was an event
      currentEventData = eventData;       
      if (useBestInterpolation) interpolatorSolver.bestInterpolate(eventData.time, state); // So that we reinitialize with the best possible approximation 
      else System.arraycopy(test_ode_state, 0, state, 0, dimension); // -- unnecessary -- interpolatorSolver.interpolate(eventData.time, state);
      double timeBefore = state[timeIndex];
      boolean wantsToQuit = eventData.generalEvent.action();
      reinitialize();
      counter = 0;
      state = interpolatorSolver.getODE().getState(); // who knows if the event changes the ODE array pointer?
      if (timeBefore!=state[timeIndex]) { // If the event changes the time, it is meaningless to try to complete a step
        zenoCounter = 0;
        lastEventData = null;
        currentEventData = null;
        eventData.reset(state);
        return (eventData.time-tBegin);
      }
      if (lastEventData!=null) {
        if (Math.abs(lastEventDataTime-eventData.time)<proximityThreshold) {
          zenoCounter++;
          if (coalesceCloseEvents) wantsToQuit = false; // If the events are too close, do not quit (this avoids unnecessary redraw)
        }
        else zenoCounter = 0;
      }
      lastEventData = eventData;
      lastEventDataTime = eventData.time; // because the internal value of lastEventData will change
      if (wantsToQuit) return (eventData.time-tBegin);
      if (zenoMaximumAllowedTimes>0 && zenoCounter>zenoMaximumAllowedTimes) {
        if (callZenoAction(state)) return (eventData.time-tBegin);
      }
//      if (notYetThere) { // Respect the user's reading step (Is this really a good idea???)
//        max_t = runsForwards ? Math.min(max_t, interpolatorSolver.getMaximumTime()) : Math.max(max_t, interpolatorSolver.getMaximumTime());
//      }
//      else 
      max_t = interpolatorSolver.getMaximumTime();
      if (!Double.isNaN(eventData.maxAdvance)) { // If a short-duration step was found, this helps avoid recurrent events
        max_t = runsForwards ? Math.min(max_t, eventData.maxAdvance) : Math.max(max_t, eventData.maxAdvance);  
      }
      if (Double.isNaN(max_t)) return error (INTERNAL_SOLVER_ERROR,"Error when stepping the solver after an event at "+state[timeIndex]); //$NON-NLS-1$
//      lastEventData = eventData;
//      lastEventDataTime = lastEventData.time; // because the internal value of lastEventData will change
    } // end of while
  }

  /**
   * A Zeno effect has been detected. Call the listeners.
   * If there are no listeners registered, then an error is issued, including possibly throwing an exception
   * @return true if the solver should stop the step and return a 0 step size
   */
  private boolean callZenoAction(double[] _state) {
    if (zenoList.isEmpty()) {
      error (ZENO_EFFECT,"A Zeno-like effect has been detected.\nLast event was "+ //$NON-NLS-1$
          lastEventData.generalEvent+" which took place at "+lastEventDataTime); //$NON-NLS-1$
      return true;
    }
    boolean returnAtZeno = false;
    for (ZenoEffectListener listener : zenoList) if (listener.zenoEffectAction(lastEventData.generalEvent, _state)) returnAtZeno = true;
    zenoCounter = 0;
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
    numberOfAttempts = 0;
    // -- Find which events have happened
    EventData happensAtT1 = happensRightNow(current_state[timeIndex],final_state, happened,"at t1");
//    if (happensAtT1==null) System.err.println ("Event not found");
    if (happensAtT1!=null) {
      happensAtT1.time = current_state[timeIndex]; 
      happensAtT1.maxAdvance=tFinal; // limit the advance of the solver from this event. This helps avoid recurrent events
      System.arraycopy(current_state, 0, final_state, 0, dimension);
      return happensAtT1;
    }
    // -- Check for no event
    if (happened.isEmpty()) return null; // IMPORTANT! final_state remains unchanged

//    if (Math.abs(tFinal-current_state[timeIndex])<Double.MIN_VALUE) {
//      System.err.println ("dt = 0 at "+tFinal);
////      if (true) System.exit(0);
//      return happened.get(0); // Any event that could have happened actually happened
//    }

    // --  Search until found or maximum of attempts is reached
    boolean doItAgain = true;
    for (EventData eventData : eventList) eventData.hAfter = eventData.h; // evaluated in happensRightNow() (since no event happened at T1) 
    while (doItAgain) {
      numberOfAttempts++;
      double tTest = nextPointToCheck (happened, current_state[timeIndex], tFinal); // The value of t where the event is expected to be
      doTheInterpolation (tTest, intermediate_ode_state);
      // Check if we discover an event at t1. This (short duration) event was not detected in [t1,tFinal]
      EventData shortDurationEvent = happensRightNow(current_state[timeIndex],intermediate_ode_state, temp_list, "short");
//      if (shortDurationEvent==null) System.err.println ("Event not found");
      if (shortDurationEvent!=null) { // This was a short-duration event!
        shortDurationEvent.time = current_state[timeIndex]; 
        shortDurationEvent.maxAdvance=tTest; // Try to avoid infinite loops
        System.arraycopy(current_state, 0, final_state, 0, dimension);
        return shortDurationEvent;
      }
      
      if (temp_list.isEmpty()) { // No event in [t1,tTest]
//        System.err.println ("List is empty");
        // check for event in tTest. This is a reduced check because all other possibilities were dealt with in happensRightNow()
        EventData happensInTtest=null;
        for (EventData eventData : happened) { 
          if (eventData.currentPosition==EventData.POSITIVE) { 
            if (eventData.h<eventData.generalEvent.getTolerance()) { happensInTtest = eventData; break; } // This one is the event
          }
          else { // NEGATIVE and crossing event
            if (eventData.h>-eventData.generalEvent.getTolerance()) { happensInTtest = eventData; break; } // This one is the event
          }
        }
        if (happensInTtest!=null) { 
          happensInTtest.time = tTest; 
          happensInTtest.maxAdvance=tFinal; //Double.NaN;
          System.arraycopy(intermediate_ode_state, 0, final_state, 0, dimension);
          return happensInTtest;
        }
        // repeat in [tTest,tFinal]
        System.arraycopy(intermediate_ode_state, 0, current_state, 0, dimension);
        for (EventData eventData : eventList) eventData.findPosition(current_state[timeIndex],eventData.h);
        // -- Find which events have happened
        EventData happensNowInTtest = happensRightNow(current_state[timeIndex],final_state, happened, "at tTest");
//        if (happensNowInTtest==null) System.err.println ("Event not found");
        if (happensNowInTtest!=null) {
          happensNowInTtest.time = current_state[timeIndex]; 
          happensNowInTtest.maxAdvance=tFinal; //Double.NaN;
          System.arraycopy(current_state, 0, final_state, 0, dimension);
          return happensNowInTtest;
        }
      }
      else { // temp_list is not null. I.e. there is at least one event in [t1,tTest] 
        // check for event in tTest. This is a reduced check because all other possibilities were dealt with in happensRightNow()
        // If there is any event previous to this, then this is not the point we are looking for.
        boolean notPreviousFound=true;
        EventData happensInTtest=null;
        for (EventData data : temp_list) {
          if (data.currentPosition==EventData.POSITIVE) { 
            if (data.h<=-data.generalEvent.getTolerance()) { notPreviousFound = false; break; } // I happened earlier, so this is not the point
            // else if (data.h<data.generalEvent.getTolerance()) no need to check since data.h < 0
            happensInTtest = data; // If there is no previous one, then it is me
          }
          else { // NEGATIVE and crossing event
            if (data.h>=data.generalEvent.getTolerance()) { notPreviousFound = false; break; } // I happened earlier, so this is not the point
            // else if (data.h>-data.generalEvent.getTolerance()) // no need to check since data.h>0 
            happensInTtest = data; // If there is no previous one, then it is me
          }
        }
        if (notPreviousFound && happensInTtest!=null) { // This is the event!
          happensInTtest.time = tTest; 
          happensInTtest.maxAdvance=tFinal; //Double.NaN;
          System.arraycopy(intermediate_ode_state, 0, final_state, 0, dimension);
          return happensInTtest; 
        }
        tFinal = tTest; 
        System.arraycopy(intermediate_ode_state, 0, final_state, 0, dimension);
        for (EventData eventData : eventList) eventData.hAfter = eventData.h;
        happened.clear();
        happened.addAll(temp_list);
      }
//       Check for the iteration limit
//      for (EventData data : happened) {
//        System.err.println ("Happened "+data.generalEvent+"\nCurrent state: ");
//        for (int i=0; i<dimension; i++) System.err.print(current_state[i]+", ");  
//        System.err.println ("\nFinal   state: ");
//        for (int i=0; i<dimension; i++) System.err.print(final_state[i]+", ");  
//        System.err.println ("\n");
//      }
      for (EventData data : happened) {
        if (numberOfAttempts>data.generalEvent.getMaxIterations()) { doItAgain = false; break; }
      }
    }
    
    // If this happens, the event is most likely poorly designed!
    EventData remaining = happened.get(0); // The event is any of those which remain in the list of happened
    error (EVENT_NOT_FOUND,"Warning : Event not found after "+ numberOfAttempts+ " attempts at t=" + current_state[timeIndex]  //$NON-NLS-1$ //$NON-NLS-2$
        + " h = "+ remaining.h //$NON-NLS-1$
        + ".\nPlease check the code of your event, decrease the initial step size, the tolerance of the solver," //$NON-NLS-1$
        +	"\nor the event maximum step, or increase the maximum number of attempts." //$NON-NLS-1$
        + "\nFirst event remaining in the queue: "+remaining.generalEvent); //$NON-NLS-1$

    remaining.time = (current_state[timeIndex]+tFinal)/2;
    remaining.maxAdvance=Double.NaN;
    interpolatorSolver.bestInterpolate(remaining.time, final_state);
//    if (true) System.exit(0);
    return remaining;
  }

  /**
   * Fills the list with all events that happen between the current time and the provided final time.
   * Returns an event if it happens at the initial point, null otherwise
   */
  private EventData happensRightNow(double currentTime, double[] final_state, List<EventData> list, String id) {
    list.clear();
    for (EventData eventData : eventList) {
      eventData.h = eventData.generalEvent.evaluate(final_state);
//      interpolatorSolver.getODE().debugTask();
//      System.err.print(id+" Buscando evento: en t = "+currentTime +" t2 = "+final_state[timeIndex]+" h = "+eventData.h + " h before = "+eventData.hBefore); 
//      System.err.println(" Pos : = "+eventData.currentPosition); 

      switch (eventData.currentPosition) {
        default : 
        case EventData.POSITIVE : if (eventData.h<=0) list.add(eventData); break; // This event happens! 
        case EventData.SMALL_POSITIVE :
          if (eventData.h<=0 && (eventData.positiveFlag || eventData.eventType==GeneralStateEvent.STATE_EVENT)) return eventData;
          break;
        case EventData.ZERO :
          if (eventData.h<0) {
            if (eventData.positiveFlag || eventData.eventType==GeneralStateEvent.STATE_EVENT) return eventData;
          }
          else if (eventData.h>0) {
            if (eventData.negativeFlag && eventData.eventType==GeneralStateEvent.CROSSING_EVENT) return eventData;
          }
          break;
        case EventData.SMALL_NEGATIVE :
          if (eventData.eventType==GeneralStateEvent.STATE_EVENT) {
            if (eventData.h<=-eventData.generalEvent.getTolerance()) return eventData;
          }
          else if (eventData.eventType==GeneralStateEvent.CROSSING_EVENT) {
            if (eventData.negativeFlag && eventData.h>=0) return eventData;
          }
          break;
        case EventData.NEGATIVE : 
          if (eventData.eventType==GeneralStateEvent.CROSSING_EVENT) { if (eventData.h>=0) list.add(eventData); }
          else if (eventData.eventType==GeneralStateEvent.STATE_EVENT) { // This should NOT happen, but I save the problem by saying that this event happens right now!
            error(ILLEGAL_EVENT_STATE, "The system started from an illegal state at "+currentTime+ " for the state event "+eventData.generalEvent); //$NON-NLS-1$ //$NON-NLS-2$
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
    if (runsForwards) for (EventData eventData : list) {
      switch (eventData.generalEvent.getRootFindingMethod()) {
        default : 
        case GeneralStateEvent.BISECTION : tFirst = Math.min(tFirst, tMiddle); break;
        case GeneralStateEvent.SECANT : 
//          System.err.println ("hBefore = "+eventData.hBefore);
//          System.err.println ("hAfter = "+eventData.hAfter);
//          System.err.println ("dt = "+dt);
//          System.err.println ("t1 = "+t1);
//          System.err.println ("tFirst = "+tFirst);
          tFirst = Math.min(tFirst, t1 - eventData.hBefore * dt / (eventData.hAfter - eventData.hBefore)); break;
      }
    }
    else for (EventData eventData : list) {
      switch (eventData.generalEvent.getRootFindingMethod()) {
        default : 
        case GeneralStateEvent.BISECTION : tFirst = Math.max(tFirst, tMiddle); break;
        case GeneralStateEvent.SECANT : tFirst = Math.max(tFirst, t1 - eventData.hBefore * dt / (eventData.hAfter - eventData.hBefore)); break;
      }
    }
    return tFirst;
  }

  private double error (int code, String msg) {
    errorCode = code;
    errorMessage = msg;
    if (enableExceptions) throw new ODESolverException(interpolatorSolver + ":\n"+msg); //$NON-NLS-1$
    OSPLog.warning(msg);
//    System.err.println(interpolatorSolver + ": "+msg); //$NON-NLS-1$
    return Double.NaN;
  }

  /**
   * A class that handles the information of a StateEvent for given states
   * @author Francisco Esquembre
   *
   */
  private class EventData {
    static final protected int POSITIVE = 2;
    static final protected int SMALL_POSITIVE = 1;
    static final protected int ZERO = 0;
    static final protected int SMALL_NEGATIVE = -1;
    static final protected int NEGATIVE = -2;
      
    GeneralStateEvent generalEvent; // The event itself
    boolean positiveFlag; // Whether the state has ever been positive (since last reset)
    boolean negativeFlag; // Whether the state has ever been negative (since last reset)
    final int eventType; // The type of event (STATE_EVENT, POSITIVE_EVENT, or CROSSING_EVENT)
    int currentPosition; // In which side of the [-Tol,+Tol] is h at the current time (POSITIVE, SMALL_POSITIVE, etc.)
    double hBefore;  // The value of h at the current time
    double hAfter;   // The value of h at the end of a period
    double h;        // The value of time at the test point
    double time;     // the next time at which the event took place
    double maxAdvance; // The maximum the solver must advance after this event happens
    
    EventData (GeneralStateEvent _event, double[] state) {
      generalEvent = _event;
      eventType = generalEvent.getTypeOfEvent();
      reset (state);           
    }
    
    void reset (double[] state) {
//      System.out.println ("Resetting event at "+state[timeIndex] + " current h = "+hBefore);
      positiveFlag = negativeFlag = false;
      double h = generalEvent.evaluate(state);
      findPosition (state[timeIndex],h);
      // At initialization times, a small posi(nega)tive is actually a posi(nega)tive
      if (currentEventData!=this) { // unless this event just happened
//        System.out.println ("Recalculating this event at "+state[timeIndex] + " current h = "+hBefore);
        if (eventType==GeneralStateEvent.CROSSING_EVENT) {
          if (h>0) positiveFlag = true;
          else if (h<0) negativeFlag = true;
        }
        else if (eventType==GeneralStateEvent.POSITIVE_EVENT) {
          if (h>0) positiveFlag = true;
        }
//        System.out.println ("Positive flag = "+positiveFlag);
//        System.out.println ("Negative flag = "+negativeFlag);
      }
//      System.err.println ("Reiniting event at "+state[timeIndex] + "new h = "+generalEvent.evaluate(state));
    }

    private void findPosition (double _time, double hValue) {
      hBefore = hValue;
      if      (hBefore>= generalEvent.getTolerance()) { currentPosition = POSITIVE; positiveFlag = true; }
      else if (hBefore>0) currentPosition = SMALL_POSITIVE; 
      else if (hBefore==0) currentPosition = ZERO;
      else if (hBefore>-generalEvent.getTolerance()) currentPosition = SMALL_NEGATIVE;
      else { currentPosition = NEGATIVE; negativeFlag = true; }
      if (eventType==GeneralStateEvent.STATE_EVENT && currentPosition==-2) {
        String msg = "The state event " + generalEvent+" is in an illegal state: "+hBefore+ " at "+_time;//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (lastEventData==null) msg +="\nThere was no previous event"; //$NON-NLS-1$ 
        else msg +="\nLast previous event was "+lastEventData.generalEvent+", which took place at "+lastEventDataTime; //$NON-NLS-1$ //$NON-NLS-2$
        error(ILLEGAL_EVENT_STATE, msg);
      }
    }
  }
  
  /**
   * GeneralStateEventAdapter is a wrapper for state events
   * @author Francisco Esquembre. February 2008
   */
  private class GeneralStateEventAdapter implements GeneralStateEvent {
    private StateEvent event;
    
    public GeneralStateEventAdapter(StateEvent _event) { this.event = _event; }
    
    public StateEvent getEvent () { return event; }
    
    public int getTypeOfEvent () { return GeneralStateEvent.STATE_EVENT; }
    
    public int getMaxIterations() { return 100; }
    
    public int getRootFindingMethod() { return GeneralStateEvent.BISECTION; }
    
    public double getTolerance()  { return event.getTolerance(); }
    
    public double evaluate(double[] state) { return event.evaluate(state); }
    
    public boolean action() { return event.action(); }
    
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

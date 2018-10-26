/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

import java.util.*;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.library.control.swing.ControlWindow;
import netscape.javascript.*;

/**
 * A base interface for a simulation
 */

public abstract class Animation implements java.lang.Runnable {
  static private final ThreadGroup sEJSThreadGroup = new ThreadGroup("EJS thread group");
  
  static public int MAXIMUM_FPS=25; // Not final on purpose
  static public final int MINIMUM_FPS=1;
  static public final int MINIMUM_DELAY = 10;

  // Execution of the animation
  private volatile java.lang.Thread animationThread = null; // The animation animationThread
//  private volatile boolean loopEnabled = true;
  private volatile boolean mustUpdateWhenIdle=false;
  private List<DelayedAction> methodList = new ArrayList<DelayedAction>(); //java.util.Collections.synchronizedList(new ArrayList<MethodTypeAndObject>());
  private Set<String> variablesToApply = new HashSet<String>(); //Collections.synchronizedSet(new HashSet<String>());
  volatile private boolean invokingDelayedActions=false; // Whether the model is idle and View actions and variable reads can be done immediately
  
  private boolean autoplay = false; // Whether the animation should start automatically after a reset
  protected volatile boolean isPlaying = false; // Whether the simulation is playing
  protected boolean abortSPDLoop=false; // Whether a possible SPD loop must be aborted 
  protected boolean updateView=true;   // Whether the view must be updated after each step
  private int stepsPerDisplay=1;     // How many steps to run before updating the view
  private long delay = MINIMUM_DELAY; // The delay between successive steps
  
  protected String resetFile = null; // If not null this file is read when resetting the variables
  protected boolean hasEnded=false;

  // Relation with the simulation parts
  protected Model model=null;
  protected View view=null;

  // Variables for clones
  protected Animation master=null;
  protected ArrayList<Model> slaveList = new ArrayList<Model>();

// -----------------------------
// Setters and getters
// -----------------------------

  static public ThreadGroup getThreadGroup() { return sEJSThreadGroup; }
  
  final public Model getModel () { return model; }

  final public void setModel (Model _aModel) {
    model = _aModel; 
    LauncherApplet applet = model._getApplet();
//    System.out.println ("Setting the model : applet = "+applet);
//    System.out.println ("parameter _initialState = "+applet.getParameter("initialState")); 
    if (applet!=null) {
//      System.out.println ("applet is not null "); 
      String initFile = applet.getParameter("initialStateFromURL"); 
      if (initFile!=null) {
        resetFile = initFile.trim();
        if (!resetFile.startsWith("http://")) { // files starting with http:// are considered absolute
          if (resetFile.startsWith("./")) resetFile = resetFile.substring(2);
          resetFile = applet.getCodeBase() + resetFile;
        }
        resetFile = "url:"+ resetFile;
//        System.out.println ("Init file from "+resetFile);
      }
      else {
        initFile = applet.getParameter("initialState"); 
        if (initFile!=null) { 
          resetFile = initFile.trim();
//          System.out.println ("Init file : "+resetFile);
        }
      }
    }
  }

  final public View getView () {
    if (view instanceof MultipleView) return ((MultipleView) view).getFirstView();
    return view; 
  }

  final public void setView (View _aView) { view = _aView; }

  /**
   * Sets the (approximate) number of frames per second for the simulation
   * @param _fps the number of frames per second
   */
  final public void setFPS (int _fps)  {
    if      (_fps<=MINIMUM_FPS) delay = 1000;
    else if (_fps>=MAXIMUM_FPS) delay = MINIMUM_DELAY;
    else delay = Math.max((long) (1000.0/_fps),MINIMUM_DELAY);
  }

  /**
   * Sets the delay between two steps of the simulation
   * @param _aDelay the number of milliseconds for the delay
   */
  final public void setDelay(int _aDelay) {
    delay = Math.max((long) _aDelay,0); // Exception. The user might want to have a delay of 0! MINIMUM_DELAY);
  }

  final public int getDelay () { return (int) delay; }

  /**
   * Sets the number of model steps before refreshing the view
   * @param _steps the number of model steps
   */
  final public void setStepsPerDisplay(int _steps) {
    if (_steps>=1) stepsPerDisplay = _steps;
  }

  /**
   * Sets whether the simulation should update the view in each step
   * Default is true.
   * @param _update Whether to update the view
   */
  final public void setUpdateView (boolean _update) { updateView = _update; }

  /**
   * Sets whether the simulation should be set to play mode when it is reset.
   * Default is false.
   * @param _play Whether it should play
   */
  final public void setAutoplay (boolean _play) { autoplay = _play; }

  /**
   * Returns whether the simulation is running or not
   */
  final public boolean isPlaying () { return isPlaying && animationThread!=null; }

  /**
   * Returns whether the simulation is running or not
   */
  final public boolean isPaused () { return !isPlaying(); }

//  public void disableLoop() {
//    //System.out.println ("Disabling loop ");
//    loopEnabled = false;
//    if (Thread.currentThread()==animationThread) return; // cannot join with own thread so return
//    // another thread has called this method in order to stop the animation thread
//    try { // guard against an exception in applet mode
//      animationThread.interrupt(); // get out of a sleep state
//      animationThread.join(100);  // wait up to 1/10 second for animation thread to stop
//    } 
//    catch(Exception e) {
//      // System.out.println("exception in stop animation"+e);
//    }
//  }
//
//  public void enableLoop() {
//    loopEnabled = true;
//  }

// -----------------------------
// Controlling the execution
// Based on org.opensourcephysics.display.AbstractAnimation
// -----------------------------

  private double initialRealTime = Double.NaN;
  
  /**
   * Returns the real time in milliseconds
   * @return
   */
  public void resetRealTime() {
    initialRealTime = 1000*getModel()._getRealTime() - System.currentTimeMillis();
  }
  
  /**
   * Starts the animation.
   *
   * Use this method to start a timer or a thread.
   */
  public synchronized void play() {
    if (master!=null) { getTopMaster().play(); return; }
     if(animationThread!=null) {
        return; // animation is running
     }
     animationThread = model._isApplet() ? new Thread(this) : new Thread(sEJSThreadGroup,this);
     animationThread.setPriority(Thread.MIN_PRIORITY);
     animationThread.setDaemon(true);
     initialRealTime = 1000*getModel()._getRealTime() - System.currentTimeMillis();
     animationThread.start(); // start the animation
     isPlaying = true;
     for (Model slave : slaveList) slave._getSimulation().isPlaying = true;
  }

  /**
   * Stops the animation.
   *
   * Sets animationThread to null and waits for a join with the animation thread.
   */
  public synchronized void pause() {
    if (master!=null) { getTopMaster().pause(); return; }
     if(animationThread==null) { // animation thread is already dead
        return;
     }
     Thread tempThread = animationThread; // local reference
     animationThread = null; // signal the animation to stop
     isPlaying = false;
     abortSPDLoop = true;
     for (Model slave : slaveList) {
       slave._getSimulation().isPlaying = false;
       slave._getSimulation().abortSPDLoop = true;
     }
     if(Thread.currentThread()==tempThread) return; // cannot join with own thread so return
     // another thread has called this method in order to stop the animation thread
     try {                      // guard against an exception in applet mode
       tempThread.interrupt(); // get out of a sleep state
       tempThread.join(100);  // wait up to 1 second for animation thread to stop
     } catch(Exception e) {
       // System.out.println("excetpion in stop animation"+e);
     }

  }
  
  /**
   * Implementation of Runnable interface.  DO NOT access this method directly.
   */
  public void run() {
     while(animationThread==Thread.currentThread()) {
        long currentTime = System.currentTimeMillis();
//        double currentRealTime = getModel()._getRealTime();
//        if (loopEnabled) 
        {
          step();
          for (Model slave : slaveList) slave._getSimulation().step();
        }
        if (applyVariablesWhenIdle() || checkMethodsInvokedByView()) {
          model._automaticResetSolvers();
          update();
        }
        else if (mustUpdateWhenIdle) update();
        // adjust the sleep time to try and achieve a constant animation rate
        // some VMs will hang if sleep time is less than 10
        long sleepTime;
        if (Double.isNaN(initialRealTime)) sleepTime = delay - (System.currentTimeMillis()-currentTime); // Math.max(10, delay-(System.currentTimeMillis()-currentTime));
        else sleepTime = (long) (1000*getModel()._getRealTime() - System.currentTimeMillis()  - initialRealTime);
//        System.out.println ("Sleep time = "+sleepTime);
        if (sleepTime<10) Thread.yield();
        else {
          try { Thread.sleep(sleepTime); } 
          catch(InterruptedException ie) {}
        }
     }
     // Just in case
     if (applyVariablesWhenIdle() || checkMethodsInvokedByView()) {
       model._automaticResetSolvers();
       update();
     }
     else if (mustUpdateWhenIdle) update();
  }

  /**
   * Stops the simulation and frees memory
   */
  public void onExit() {
    animationThread = null;
    isPlaying = false;
    abortSPDLoop = true;
    checkMethodsInvokedByView();
    for (ControlElement element : view.getElements()) {
      if (element instanceof ControlWindow) ControlWindow.removeFromWindowList((ControlWindow) element);
    }
    if (model._isApplet()) {
      view.onExit();
      model._freeMemory();
    }
    else {
      Thread onExitThread = new Thread(sEJSThreadGroup,new Runnable() {
        public void run() {
          view.onExit();
          model._freeMemory();
        }
      });
      onExitThread.setPriority(Thread.NORM_PRIORITY);
      onExitThread.setDaemon(true);
      onExitThread.start(); // start the animation
      try { Thread.sleep(500); } 
      catch(InterruptedException ie) {}

//      SwingUtilities.invokeLater(new Runnable() {
//        public void run () { 
//          view.onExit();
//          model._freeMemory();
//        }
//      });
//        try { Thread.sleep(500); } 
//        catch(InterruptedException ie) {}
//        view.onExit();
//        model._freeMemory();
    }
  }
//      javax.swing.Timer timer = new javax.swing.Timer(500, new ActionListener(){
//        public void actionPerformed(ActionEvent evt) {
//          view.onExit();
//          model._freeMemory();
//        }
//      });
//      timer.setRepeats(false);
//      timer.setCoalesce(true);
//      timer.start();

// ------------------------------------
// Simulation logic based on the model
// ------------------------------------

  /**
   * Reset to a user-defined default state 
   */
  abstract protected void userDefinedReset();

  /**
   * User-defined update view (such as video capture) 
   */
  abstract protected void userDefinedViewUpdate();

  /**
   * User-defined update view (such as video capture) 
   */
  abstract protected void resetDescriptionPages();

  /**
   * Resets the simulation to a complete initial state
   */
  /*final*/ public synchronized void reset() { //CJB for collaborative (remove final)
    resetDescriptionPages();
    pause();
    if (model!=null) {
      model._resetModel();
//      model._initializeSolvers();
      //if(_init_!=null)_init_.invoke(0, model._getApplet());//FKH 20060417
// It was here before      userDefinedReset(); //      if (resetFile!=null) readVariablesFromFile (resetFile,stateVariablesList);
    }
    if (view!=null) {
     view.setUpdateSimulation(false);
     view.reset();
     view.initialize();
    }
    if (model!=null) {
      model._initializeModel();
      model._updateModel();
      userDefinedReset();
//      model._initializeSolvers();
    }
    //LDL Torre for Moodle Support
    if(model._isMoodleConnected()){
        Object[] args={new Integer(10)};
        try {
            JSObject window = JSObject.getWindow(model._getApplet());
            window.call("loadState", args);
        } catch (Exception e) {}
        try {
            JSObject window = JSObject.getWindow(model._getApplet());
            window.call("loadController", args);
        } catch (Exception e) {}
        try {
            JSObject window = JSObject.getWindow(model._getApplet());
            window.call("personalizeVars", args);
        } catch (Exception e) {}
        try {
            JSObject window = JSObject.getWindow(model._getApplet());
            window.call("loadExperiment", args);
        } catch (Exception e) {}
    }
    //LDL Torre for Moodle Support
    if (view!=null) {
      //view.setUpdateSimulation(false);
      //view.reset();
      //view.initialize();
      if (updateView) {
        view.update();
        view.finalUpdate();
        userDefinedViewUpdate();
      }
      else view.collectData();
      view.setUpdateSimulation(true);
    }
    System.gc();
    synchronized (slaveList) { killAllSimulations(); }
    if (autoplay) {
//      System.out.println ("Trying to play "+this);
      play();
    }
  }

  /**
   * Initialize model using user interface changes
   */
 /*final*/ public void initialize() { //CJB for collaborative (remove final)
    if (view!=null) {
      view.read();
      view.initialize();
    }
    if (model!=null) {
      model._initializeModel();
      model._updateModel();
    }
    if (view!=null) {
//      view.initialize(); //It was here before! 100828
      if (updateView) {
        view.update();
        view.finalUpdate();
        userDefinedViewUpdate();
     }
      else view.collectData();
    }
  }

  /**
   * apply user interface changes
   */
  /*final*/ public void apply() { //CJB for collaborative (remove final)
    view.read();
    update();
  }

  /**
   * apply user interface changes. Yes, exactly the same as apply() (I need it somewhere else :-)
   */
  final public void applyAll() {
    view.read();
    update();
  }

  /**
   * apply a single change in the user interface
   */
  /*fina*/ public void apply (String _variable) { //CJB for collaborative (remove final
    if (invokingDelayedActions || isPaused()) {
      view.read(_variable);
      model._automaticResetSolvers();
    }
    else {
      view.blockVariable(_variable);
      synchronized(variablesToApply) { variablesToApply.add(_variable); }
    }
  }

  /**
   * Applies changes to model variables generated by user interaction when the model is idle
   * @return
   */
  private boolean applyVariablesWhenIdle() {
//    if (__memoryFred) return true;
    Set<String> copyList;
    synchronized (variablesToApply) { // Delayed update of variables
      if (variablesToApply.isEmpty()) return false;
      copyList = new HashSet<String>(variablesToApply);
      variablesToApply.clear();
    }
    invokingDelayedActions = true;
    for (String variable : copyList) view.read(variable);
    invokingDelayedActions = false;
    return true;
  }
  
  private boolean checkMethodsInvokedByView() {
//    if (__memoryFred) return true;
    ArrayList<DelayedAction> copyList;
    synchronized (methodList) { // Delayed invocation of methods
      if (methodList.isEmpty()) return false;
      copyList = new ArrayList<DelayedAction>(methodList);
      methodList.clear();
    }
    invokingDelayedActions = true;
    for (DelayedAction action : copyList) action.performAction();
    invokingDelayedActions = false;
    return true;
  }

  /**
   * Updates the simulation
   */
  /*final*/ public void update() { //CJB for collaborative (remove final)
//    System.err.println("Updating the simulation");
    if (model!=null) model._updateModel();
    if (view!=null) {
       if (updateView) view.update();
       else view.collectData();
    }
    mustUpdateWhenIdle = false;
    if (applyVariablesWhenIdle() || checkMethodsInvokedByView()) { // do it again
      model._automaticResetSolvers(); // But first, reset solvers
      if (model!=null) model._updateModel();
      if (updateView) view.update();
      else view.collectData();
    }
    if (updateView) view.finalUpdate(); // render the drawing
  }
  
  public boolean hasEnded() { return hasEnded; }

  public void setEnded() { hasEnded = true; }
  
  /**
   * Updates the simulation when possible in order not to conflict with the model thread 
   */
  final public void updateWhenIdle() {
//    if (__memoryFred) return;
    if (invokingDelayedActions || isPaused()) update();
    else mustUpdateWhenIdle = true;
  }

//  final public void invokeMethodWhenIdle (MethodWithOneParameter _method, int _type, Object _callingObject) {
//    if (isPlaying()) {
//      MethodTypeAndObject mto = new MethodTypeAndObject(_method,_type,_callingObject);
//      synchronized (methodList) { methodList.add(mto); }
//    }
//    else {
//      _method.invoke(_type, _callingObject);
//      model._automaticResetSolvers();
//    }
//  }

  final public void invokeMethodWhenIdle (DelayedAction _action) {
//    if (__memoryFred) return;
    if (invokingDelayedActions || isPaused()) {
      //      if (model._hasBeenReset()) {
      _action.performAction();
      model._automaticResetSolvers();
      //      }
    }
    else {
      synchronized (methodList) { methodList.add(_action); }
    }
  }

  /**
   * step
   */
  public void step() { // Not final because the generated sim can change the stepsPerDisplay
    // This is used when you want to step the model
    // several times updating the display only once
    abortSPDLoop = false;
    if (stepsPerDisplay>1) {
      for (int i=1; i<stepsPerDisplay; i++) {
        if (abortSPDLoop) { // The user called _pause()
          update();
          if (updateView) userDefinedViewUpdate();
          return;
        }
        model._stepModel();
        model._updateModel();
        view.collectData();
      }
    }
    // Now the final time
    model._stepModel();
    update();
    if (updateView) userDefinedViewUpdate();
  }

// --------------------------------------------------------
// Accessing model methods
// --------------------------------------------------------

  public void updateAfterModelAction() {
//    if (view!=null) view.initialize(); // If initialize sends all the data, this is redundant
    if (master!=null) { getTopMaster().updateAfterModelAction(); return; }
    update();
    for (Model slave : slaveList) slave._getSimulation().update();
//    loopEnabled = true;
  }

  //---------------------------------------------------
  // Utilities for clones
  //---------------------------------------------------
  /**
   * Creates a simulation of the same class as this one.
   * The simulation thus created is synchronized with (slave to) the original one.
   * @return Model The model of the simulation created.
   */
  final public Model runSimulation () { return runSimulation(null); }

  /**
   * Creates a simulation of the given classname.
   * The simulation thus created is synchronized with (slave to) the original one.
   * @param _classname String
   * @return Model The model of the simulation created.
   */
  final public Model runSimulation (String _classname) {
    try {
      Class<?> theClass;
      if (_classname==null) theClass = getModel().getClass();
      else theClass = Class.forName(_classname);
      Model simModel = (Model) theClass.newInstance();
      Animation top = getTopMaster();
      simModel._getSimulation().master = top;
      simModel._getSimulation().isPlaying = isPlaying;
      simModel._getSimulation().update();
      top.slaveList.add(simModel);
      return simModel;
    }
    catch (Exception _exc) {
      _exc.printStackTrace();
      return null;
    }
  }

  /**
   * Makes a slave simulation not synchronized with the original one.
   * @param _simulation Object
   */
  final public void freeSimulation (Model _simulationModel) {
    if (_simulationModel==null) return;
    try {
      Animation top = getTopMaster();
      top.slaveList.remove(_simulationModel);
      _simulationModel._getSimulation().master = null;
      if (_simulationModel._getView() instanceof EjsControl) ((EjsControl) _simulationModel._getView()).undoReparenting();
      if (top.isPlaying) _simulationModel._getSimulation().play();
      else _simulationModel._getSimulation().isPlaying = false;
      _simulationModel._getSimulation().update();
    }
    catch (Exception _exc) {
      _exc.printStackTrace();
    }
  }

  /**
   * Kills a slave simulation created from this simulation.
   * @param _simulation Object
   */
  final public void killSimulation (Model _simulationModel) {
    if (_simulationModel==null) return;
    freeSimulation(_simulationModel);
    _simulationModel._getSimulation().pause();
    if (_simulationModel._getView() instanceof EjsControl) ((EjsControl) _simulationModel._getView()).clear();
  }

  /**
   * Kills all slaves simulations of this one.
   */
  final public synchronized void killAllSimulations () {
    List<Model> list = slaveList;
    slaveList = new ArrayList<Model>();
    for (Iterator<Model> it=list.iterator(); it.hasNext(); ) {
      Model simModel = it.next();
      simModel._getSimulation().pause();
      simModel._getSimulation().master = null;
      if (simModel._getView() instanceof EjsControl) {
        ( (EjsControl) simModel._getView()).undoReparenting();
        ( (EjsControl) simModel._getView()).clear();
      }
    }
  }

  /**
   * Returns the model of the top level simulation from which this one is
   * a slave (or a slave of a slave, or ...)
   * @return Model
   */
  final public Model getTopSimulation () { return getTopMaster().getModel(); }

  /**
   * Returns the top level simulation from which this one is
   * a slave (or a slave of a slave, or ...)
   * @return Simulation
   */
  final private Animation getTopMaster () {
    if (master==null) return this;
    Animation topMaster = master;
    while (topMaster.master!=null) topMaster = topMaster.master;
    return topMaster;
  }

  
} // End of class


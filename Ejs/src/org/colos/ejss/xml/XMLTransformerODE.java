package org.colos.ejss.xml;

import java.util.HashSet;

import javax.swing.JOptionPane;

import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejss.xml.SimulationXML.ODE;
import org.colos.ejss.xml.SimulationXML.ODE_DISCONTINUITY;
import org.colos.ejss.xml.SimulationXML.ODE_EVENT;
import org.w3c.dom.Element;

public class XMLTransformerODE {
  static private ResourceUtil res = new ResourceUtil ("Resources");

  /**
   * Preparatory code for all ODEs
   * @param simulation
   * @param buffer
   */
  static void appendCode(StringBuffer buffer, SimulationXML simulation) {
    if (simulation.isInsideEJS()) { // just define historic functions
      for (Element page : simulation.getModelEvolution()) {
        String type = BasicElement.evaluateNode(page,"type");
        if ("ode".equals(type)) {
          java.util.List<Element> equations = simulation.getODEEquations(page);
          for (int i=0,n=equations.size(); i<n; i++) {
            Element equation = equations.get(i);
            String state = BasicElement.evaluateNode(equation, "state");
            buffer.append("  function _historic_" + state + "(__time) {}\n");
          }
        }
      }
      return;
    }
    
    buffer.append("  function _initializeSolvers() {\n");
    buffer.append("    for (var i=0,n=_privateOdesList.length; i<n; i++) _privateOdesList[i].initializeSolver();\n");
    buffer.append("  }\n\n");
    //    {
    //      int pageCounter = 0;
    //      for (Element mODE : mSimulation.getModelEvolution()) {
    //        String type = BasicElement.evaluateNode(mODE,"type");
    //        if ("ode".equals(type)) {
    //          pageCounter++;
    //          buffer.append("    _ODEi_evolution"+pageCounter+".initializeSolver();\n");
    //        }
    //      }
    //    }
    //    buffer.append("  }\n\n");

    buffer.append("  function _automaticResetSolvers() {\n");
    buffer.append("    for (var i=0,n=_privateOdesList.length; i<n; i++) _privateOdesList[i].automaticResetSolver();\n");
    buffer.append("  }\n\n");

    buffer.append("  _model.resetSolvers = function() {\n");
    buffer.append("    for (var i=0,n=_privateOdesList.length; i<n; i++) _privateOdesList[i].resetSolver();\n");
    buffer.append("  };\n\n");

    //BLOCKLY EXPERIMENTS
    buffer.append("  _getODE = function (_odeName) {\n");
    int pageCounter = 1;
    for (Element page : simulation.getModelEvolution()) {
      String type = BasicElement.evaluateNode(page,"type");
      if ("ode".equals(type)) {
        String name = BasicElement.evaluateNode(page,"name");
        buffer.append("    if (_odeName==\""+name+"\") return _ODEi_evolution"+pageCounter+";\n");
        pageCounter++;
      }
    }
    buffer.append("    return null;\n");
    buffer.append("  }\n\n");

    buffer.append("  function _getEventSolver(_odeName) {\n");
    buffer.append("    var ode = _getODE(_odeName);\n");
    buffer.append("    if (ode===null) return null;\n");
    buffer.append("    return ode.getEventSolver();\n");
    buffer.append("  }\n\n");

    buffer.append("  function _setSolverClass(_odeName, _engine) {\n");
    buffer.append("    var ode = _getODE(_odeName);\n");
    buffer.append("    if (ode===null) return;\n");
    buffer.append("    if (!_engine.setODE) {\n");
    buffer.append("      var classname = _engine.toLowerCase();\n");
    buffer.append("      if      (classname.indexOf(\"boga\")>=0)   _engine = EJSS_ODE_SOLVERS.bogackiShampine23;\n");
    buffer.append("      else if (classname.indexOf(\"cash\")>=0)   _engine = EJSS_ODE_SOLVERS.cashKarp45;\n");
    buffer.append("      else if (classname.indexOf(\"dopri5\")>=0) _engine = EJSS_ODE_SOLVERS.dopri5;\n");
    buffer.append("      else if (classname.indexOf(\"dopri8\")>=0) _engine = EJSS_ODE_SOLVERS.dopri853;\n");
    buffer.append("      else if (classname.indexOf(\"richa\")>=0)  _engine = EJSS_ODE_SOLVERS.eulerRichardson;\n");
    buffer.append("      else if (classname.indexOf(\"euler\")>=0)  _engine = EJSS_ODE_SOLVERS.euler;\n");
    buffer.append("      else if (classname.indexOf(\"fehlberg87\")>=0) _engine = EJSS_ODE_SOLVERS.fehlberg87;\n");
    buffer.append("      else if (classname.indexOf(\"fehlberg8\")>=0)  _engine = EJSS_ODE_SOLVERS.fehlberg8;\n");
    buffer.append("      else if (classname.indexOf(\"radau\")>=0)   _engine = EJSS_ODE_SOLVERS.radau5;\n");
    buffer.append("      else if (classname.indexOf(\"runge\")>=0)  _engine = EJSS_ODE_SOLVERS.rungeKutta4;\n");
    buffer.append("      else if (classname.indexOf(\"rk4\")>=0)    _engine = EJSS_ODE_SOLVERS.rungeKutta4;\n");
    buffer.append("      else if (classname.indexOf(\"verlet\")>=0) _engine = EJSS_ODE_SOLVERS.velocityVerlet;\n");
    buffer.append("    }\n");
    buffer.append("    if (_engine) ode.setSolverClass(_engine);\n");
    buffer.append("  }\n\n");

    pageCounter = 1; 
    for (Element page : simulation.getModelEvolution()) {
      String type = BasicElement.evaluateNode(page,"type");
      if ("ode".equals(type)) {
        XMLTransformerODE odeCode = new XMLTransformerODE(simulation, page, pageCounter);
        odeCode.generateCode(buffer);
        odeCode.createFunctionsCode(buffer);
        pageCounter++;
      }
    }
  }

  // ----------------------------------------------
  // non-static part
  // ----------------------------------------------

  private SimulationXML mSimulation;
  private Element mODE;
  private int mPageIndex;
  private String mIndVar;
  private HashSet<Element> mFollowedByDerivativeSet = new HashSet<Element>();

  XMLTransformerODE(SimulationXML simulation, Element odePage, int pageIndex) {
    mSimulation = simulation;
    mODE = odePage;
    mPageIndex = pageIndex;
    mIndVar = mSimulation.getODEConfiguration(mODE, ODE.INDEPENDENT_VARIABLE);

    // See which states are followed by their derivatives
    java.util.List<Element> equations = simulation.getODEEquations(odePage);
    int nEqn = equations.size();
    if (nEqn>1) {
      String lastRate = BasicElement.evaluateNode(equations.get(0), "rate").trim();
      for (int i=1; i<nEqn; i++) {
        Element equation = equations.get(i);
        String thisState = BasicElement.evaluateNode(equation, "state").trim();
        String thisIndex = BasicElement.evaluateNode(equation, "index");
        if (thisIndex!=null) thisState += "["+thisIndex+"]";
//        System.out.println("Checking state "+thisState+" with previous rate = "+lastRate);
        if (thisState.equals(lastRate)) mFollowedByDerivativeSet.add(equations.get(i-1));
        lastRate = BasicElement.evaluateNode(equation, "rate").trim();
      }
    }
//    for (Element equation : mFollowedByDerivativeSet) {
//      String state = BasicElement.evaluateNode(equation, "state").trim();
//      System.out.println("State "+state+ " IS followed by its derivative");
//    }
    // Check Verlet ordering
    String method = mSimulation.getODEConfiguration(odePage, ODE.SOLVER_METHOD);
    if (method.contains("Verlet")) { 
      StringBuffer buffer = new StringBuffer();
      for (int i=0, num=nEqn-1; i<num; i+=2) {
        Element equation = equations.get(i);
        if (!isFollowedByDerivative(equation)) buffer.append(BasicElement.evaluateNode(equation, "state")+"\n");
      }
      String wrongList = buffer.toString();
      if (wrongList.length()>0) JOptionPane.showMessageDialog(null, 
          res.getString("Experiment.ScheduledEvent.ODEPage")+": "+BasicElement.evaluateNode(odePage,"name")+"\n"+
              res.getString("EquationEditor.VerletOrderingNotRespected")+wrongList,
              res.getString("Warning"), JOptionPane.WARNING_MESSAGE);
    }

  }

  private boolean isFollowedByDerivative(Element equation) {
    return mFollowedByDerivativeSet.contains(equation);
  }

  // ----------------------------------------------
  // private methods
  // ----------------------------------------------

  private void generateCode(StringBuffer buffer) {
    String pageName = BasicElement.evaluateNode(mODE,"name");
    String solverType = mSimulation.getODEConfiguration(mODE, ODE.SOLVER_METHOD);
    String delays = mSimulation.getODEConfiguration(mODE, ODE.DELAY_LIST);
    boolean manualSynchro = "false".equals(mSimulation.getODEConfiguration(mODE, ODE.FORCE_SYNCHRONIZATION));
    boolean useDelays = (delays!=null) && (delays.trim().length()>0) ;
    boolean hasErrorCode = mSimulation.getODEErrorHandlers(mODE).size()>0;

    buffer.append("  function _ODE_evolution"+mPageIndex+"() {\n");
    buffer.append("    var __odeSelf = {};\n");

    buffer.append("    var __eventSolver;\n");
    buffer.append("    var __solverClass = "+solverType+";\n");

    buffer.append("    var __state=[];\n");
    for (int eventCounter=1,n=mSimulation.getODEEvents(mODE).size(); eventCounter<=n; eventCounter++) {
      buffer.append("    var _ODE_evolution"+mPageIndex+"_Event"+eventCounter+";\n");
    }
    for (int discontinuityCounter=1,n=mSimulation.getODEDiscontinuities(mODE).size(); discontinuityCounter<=n; discontinuityCounter++) {
      buffer.append("    var _ODE_evolution"+mPageIndex+"_Discontinuity"+discontinuityCounter+";\n");
    }

    if (!hasErrorCode) buffer.append("    var __ignoreErrors=false;\n");
    buffer.append("    var __mustInitialize=true;\n");
    buffer.append("    var __isEnabled=true;\n");
    buffer.append("    var __mustUserReinitialize=false;\n");
    if (manualSynchro) buffer.append("    var __mustReinitialize=true;\n\n");

    for (Element equation : mSimulation.getODEEquations(mODE)) {
      String index = BasicElement.evaluateNode(equation, "index");
      if (index!=null) {
        String state = BasicElement.evaluateNode(equation, "state");
        buffer.append("    var _"+state+"Length;\n");
      }
    }
    buffer.append("\n");
    
    // BLOCKLY EXPERIMENTS //
    buffer.append("    __odeSelf._getOdeVars = function (){ return"+getVarsNames()+"};\n\n");
    ////

    buffer.append("    __odeSelf.setSolverClass = function(__aSolverClass) {\n");
    buffer.append("      __solverClass = __aSolverClass;\n");
    buffer.append("      __instantiateSolver();\n");
    buffer.append("    };\n\n");

    buffer.append("    function __instantiateSolver() {\n");
    buffer.append("      __state=[];\n");
    for (Element equation : mSimulation.getODEEquations(mODE)) {
      String index = BasicElement.evaluateNode(equation, "index");
      if (index!=null) {
        String state = BasicElement.evaluateNode(equation, "state");
        buffer.append("      _"+state+"Length = "+state+".length;\n");
      }
    }

    buffer.append("      __pushState();\n");
    buffer.append("      __eventSolver = EJSS_ODE_SOLVERS.interpolatorEventSolver(__solverClass(),__odeSelf);\n");
    buffer.append("      __mustInitialize = true;\n");
    buffer.append("    }\n\n");

    buffer.append("    __odeSelf.setEnabled = function(_enabled) { __isEnabled = _enabled; };\n\n");
    buffer.append("    __odeSelf.getIndependentVariableValue = function() { return __eventSolver.getIndependentVariableValue(); };\n\n");
    buffer.append("    __odeSelf.getInternalStepSize = function() { return __eventSolver.getInternalStepSize(); };\n\n");
    buffer.append("    __odeSelf.isAccelerationIndependentOfVelocity = function() { return "+mSimulation.getODEConfiguration(mODE, ODE.ACCELERATION_INDEPENDENT_OF_VELOCITY)+"; };\n\n");

    buffer.append("    __odeSelf.initializeSolver = function() {\n");
    buffer.append("      if (__arraysChanged()) { __instantiateSolver(); __odeSelf.initializeSolver(); return; }\n");
    buffer.append("      __pushState();\n");
    String stepSize = mSimulation.getODEConfiguration(mODE, ODE.INCREMENT).trim();
    String intStepSize = mSimulation.getODEConfiguration(mODE, ODE.INTERNAL_STEP).trim();
    if (intStepSize.length()>0) buffer.append("      __eventSolver.initialize("+intStepSize+");\n");
    else                        buffer.append("      __eventSolver.initialize("+stepSize+");\n");
    buffer.append("      __eventSolver.setBestInterpolation("+mSimulation.getODEConfiguration(mODE, ODE.USE_BEST_INTERPOLATION)+");\n");
    String historyLength = mSimulation.getODEConfiguration(mODE, ODE.HISTORY_LENGTH).trim();
    if (historyLength.length()>0) buffer.append("      __eventSolver.setHistoryLength("+historyLength+");\n");

    String maxStep = mSimulation.getODEConfiguration(mODE,ODE.MAXIMUM_STEP).trim();
    if (maxStep.length()>0) buffer.append("      __eventSolver.setMaximumInternalStepSize("+maxStep+");\n");
    String maxNumberOfSteps = mSimulation.getODEConfiguration(mODE,ODE.MAXIMUM_NUMBER_OF_STEPS).trim();
    if (maxNumberOfSteps.length()>0) buffer.append("      __eventSolver.setMaximumInternalSteps("+maxNumberOfSteps+");\n");

    buffer.append("      __eventSolver.removeAllEvents();\n");
    {
      java.util.List<Element> eventList = mSimulation.getODEEvents(mODE);
      for (int eventCounter=1,n=eventList.size(); eventCounter<=n; eventCounter++) {
        Element page = eventList.get(eventCounter-1);
        String name = BasicElement.evaluateNode(page,"name");
        buffer.append("      if (__pagesEnabled[\""+name+"\"]) __eventSolver.addEvent(_ODE_evolution"+mPageIndex+"_Event"+eventCounter+"());\n");
      }
      java.util.List<Element> discontinuityList = mSimulation.getODEDiscontinuities(mODE);
      for (int discontinuityCounter=1,n=discontinuityList.size(); discontinuityCounter<=n; discontinuityCounter++) {
        Element page = discontinuityList.get(discontinuityCounter-1);
        String name = BasicElement.evaluateNode(page,"name");
        buffer.append("      if (__pagesEnabled[\""+name+"\"]) __eventSolver.addDiscontinuity(_ODE_evolution"+mPageIndex+"_Discontinuity"+discontinuityCounter+"());\n");
      }
    }
    
    // BLOCKLY EXPERIMENTS
    buffer.append("      for(k in userEvents"+mPageIndex+"){__eventSolver.addEvent(userEvents"+mPageIndex+"[k]);}\n");
    ////
    
    
    String zenoAction = mSimulation.getODEConfiguration(mODE,ODE.ZENO_ACTION);
    if (zenoAction!=null && zenoAction.trim().length()>0) buffer.append("      __eventSolver.addZenoEffectListener(__odeSelf);\n");

    buffer.append("      __eventSolver.setEstimateFirstStep("+mSimulation.getODEConfiguration(mODE,ODE.ESTIMATE_FIRST_STEP)+");\n");
    buffer.append("      __eventSolver.setEnableExceptions(false);\n");

    String absTol = mSimulation.getODEConfiguration(mODE,ODE.ABSOLUTE_TOLERANCE).trim();
    String toleranceStr=null;
    if (absTol.length()>0) {
      String relTol = mSimulation.getODEConfiguration(mODE,ODE.RELATIVE_TOLERANCE).trim();
      if (relTol.length()<=0) relTol = absTol;
      toleranceStr = "setTolerances("+absTol+","+relTol+")";
    }
    if (toleranceStr!=null) buffer.append("      __eventSolver."+toleranceStr+";\n");
    if (manualSynchro) buffer.append("      __mustReinitialize = true;\n");
    buffer.append("      __mustInitialize = false;\n"); 
    buffer.append("    };\n\n");

    buffer.append("    function __pushState() {\n");
    updateStateFromVariablesCode(buffer,"__state",manualSynchro);
    buffer.append("    }\n\n");

    buffer.append("    function __arraysChanged () {\n");
    for (Element equation : mSimulation.getODEEquations(mODE)) {
      String index = BasicElement.evaluateNode(equation, "index");
      if (index!=null) {
        String state = BasicElement.evaluateNode(equation, "state");
        buffer.append("      if (_"+state+"Length != "+state+".length) return true;\n");
      }
    }
    buffer.append("      return false;\n");
    buffer.append("    }\n\n");

    buffer.append("    __odeSelf.getEventSolver = function() {\n");
    buffer.append("      return __eventSolver;\n");
    buffer.append("    };\n\n");

    buffer.append("    __odeSelf.resetSolver = function() {\n");
    buffer.append("      __mustUserReinitialize = true;\n");
    buffer.append("    };\n\n");

    buffer.append("    __odeSelf.automaticResetSolver = function() {\n");
    if (manualSynchro) buffer.append("      __mustReinitialize = true;\n");
    buffer.append("    };\n\n");

    
    buffer.append("    function __errorAction () {\n");
    if (hasErrorCode) {
      if (manualSynchro) {
        buffer.append("      // Make sure the solver is reinitialized;\n");
        buffer.append("      __mustReinitialize = true;\n");
      }
      buffer.append("      var _errorCode = __eventSolver.getErrorCode();\n");
      for (Element handler : mSimulation.getODEErrorHandlers(mODE)) {
        String handlerName = BasicElement.evaluateNode(handler, "name");
        String handlerType = BasicElement.evaluateNode(handler, "type");
        String code = BasicElement.evaluateNode(handler, "code");
        String comment = BasicElement.evaluateNode(handler, "comment");
        buffer.append("      if (__pagesEnabled[\""+handlerName+"\"]) {\n");
        if (handlerType.equals(org.colos.ejs.osejs.edition.ode_editor.ErrorEditor.ANY_ERROR)) buffer.append("        { // For any error: "+comment+"\n");
        else buffer.append("        if (__eventSolver.getErrorCode()==EJSS_ODE_SOLVERS.ERROR."+handlerType+") { // "+comment+"\n");
        SimulationXML.splitCode(buffer,code,"ErrorHandler for "+pageName+" : "+handlerName,"          ");
        buffer.append("        }\n");
        buffer.append("      }\n");
      }
    }
    else { // No error code
      buffer.append("      if (__ignoreErrors) return;\n");
      buffer.append("      console.log (__eventSolver.getErrorMessage());\n");
      //          buffer.append("      var __option = javax.swing.JOptionPane.showConfirmDialog(_view.getComponent(_simulation.getMainWindow()),org.colos.ejs.library.Simulation.getEjsString(\"ODEError.Continue\"),\n");
      //          buffer.append("        org.colos.ejs.library.Simulation.getEjsString(\"Error\"), javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);\n");
      //          buffer.append("      if (__option==javax.swing.JOptionPane.YES_OPTION) __ignoreErrors = true;\n");
      //          buffer.append("      else if (__option==javax.swing.JOptionPane.CANCEL_OPTION) _pause();\n");
      buffer.append("      _pause();\n");
      if (manualSynchro) {
        buffer.append("      // Make sure the solver is reinitialized;\n");
        buffer.append("      __mustReinitialize = true;\n");
      }
    }
    buffer.append("    }\n\n");

    buffer.append("    __odeSelf.step = function() { return __privateStep(false); };\n\n");
    buffer.append("    __odeSelf.solverStep = function() { return __privateStep(true); };\n\n");

    buffer.append("    function __privateStep(__takeMaximumStep) {\n");
    buffer.append("      if (!__isEnabled) return 0;\n"); 
    buffer.append("      if ("+stepSize+"===0) return 0;\n"); 
    buffer.append("      if (__mustInitialize) __odeSelf.initializeSolver();\n"); 
    buffer.append("      if (__arraysChanged()) { __instantiateSolver(); __odeSelf.initializeSolver(); }\n");
    buffer.append("      __eventSolver.setStepSize("+stepSize+");\n");
    if (intStepSize.length()>0) buffer.append("      __eventSolver.setInternalStepSize("+intStepSize+");\n");
    else                        buffer.append("      __eventSolver.setInternalStepSize("+stepSize+");\n");
    if (historyLength.length()>0) buffer.append("      __eventSolver.setHistoryLength("+historyLength+");\n");
    if (maxStep.length()>0) buffer.append("      __eventSolver.setMaximumInternalStepSize("+maxStep+");\n");
    if (maxNumberOfSteps.length()>0) buffer.append("      __eventSolver.setMaximumInternalSteps("+maxNumberOfSteps+");\n");

    String eventStep = mSimulation.getODEConfiguration(mODE,ODE.MAXIMUM_EVENT_STEP).trim();
    if (eventStep.length()>0) buffer.append("      __eventSolver.setMaximumEventStep("+eventStep+");\n");
    if (toleranceStr!=null) buffer.append("      __eventSolver."+toleranceStr+";\n");
    buffer.append("      __pushState();\n");

    buffer.append("      if (__mustUserReinitialize) { \n");
    buffer.append("        __eventSolver.userReinitialize();\n");
    buffer.append("        __mustUserReinitialize = false;\n"); 
    if (manualSynchro) buffer.append("        __mustReinitialize = false;\n"); 
    buffer.append("        if (__eventSolver.getErrorCode()!=EJSS_ODE_SOLVERS.ERROR.NO_ERROR) __errorAction();\n");
    buffer.append("      }\n");
    if (manualSynchro) {
      buffer.append("      else if (__mustReinitialize) { \n");
      buffer.append("        __eventSolver.reinitialize();\n");
      buffer.append("        __mustReinitialize = false;\n"); 
      buffer.append("        if (__eventSolver.getErrorCode()!=EJSS_ODE_SOLVERS.ERROR.NO_ERROR) __errorAction();\n");
      buffer.append("      }\n"); 
    }
    else {
      buffer.append("      __eventSolver.reinitialize(); // force synchronization: inefficient!\n");
      buffer.append("      if (__eventSolver.getErrorCode()!=EJSS_ODE_SOLVERS.ERROR.NO_ERROR) __errorAction();\n");
    }
    buffer.append("      var __stepTaken = __takeMaximumStep ? __eventSolver.maxStep() : __eventSolver.step();\n");
    extractVariablesFromStateCode(buffer,"__state",false);
    buffer.append("      // Check for error\n");
    buffer.append("      if (__eventSolver.getErrorCode()!=EJSS_ODE_SOLVERS.ERROR.NO_ERROR) __errorAction();\n");
    buffer.append("      return __stepTaken;\n");
    buffer.append("    }\n\n");

    buffer.append("    __odeSelf.getState = function() { return __state; };\n\n");

    if (useDelays) {
      buffer.append("    __odeSelf.setStateHistory = function(_history) { }; // deliberately left empty\n\n");

      buffer.append("    __odeSelf.getDelays = function(__aState) {\n");
      extractVariablesFromStateCode(buffer,"__aState",true);
      buffer.append("      return [" + delays + " ];\n");
      buffer.append("    };\n\n");

      buffer.append("    __odeSelf.getMaximumDelay = function() {\n");
      String maxDelayStr = mSimulation.getODEConfiguration(mODE, ODE.DELAY_MAXIMUM);
      if (maxDelayStr==null || maxDelayStr.trim().length()<=0) {
        buffer.append("      var _delaysArray = __odeSelf.getDelays(__state);\n" );    
        buffer.append("      var _maximum = Number.POSITIVE_INFINITY;\n" );    
        buffer.append("      for (var _i=0,_n=_delaysArray.length; _i<_n; _i++) _maximum = Math.max(_maximum,Math.abs(_delaysArray[_i]));\n" );    
        buffer.append("      return _maximum;\n" );    
      }
      else buffer.append("      return "+maxDelayStr+";\n" );    
      buffer.append("    };\n\n");
      
      buffer.append("    __odeSelf.getInitialConditionDiscontinuities = function() {\n");
      buffer.append("      return ["+mSimulation.getODEConfiguration(mODE, ODE.DELAY_DISCONTINUITIES)+"];\n" ); 
      buffer.append("    };\n\n");
      
      buffer.append("    function _userDefinedInitialCondition(_time){\n"); // This is because the user returns a smaller array (not including time)
      buffer.append("      // In case it uses the independent variable\n");
      buffer.append("      var "+mIndVar+" = _time;\n");
      String initCond = mSimulation.getODEConfiguration(mODE, ODE.DELAY_INITIAL_CONDITION);
      if (initCond.trim().length()<=0) {
        System.err.println("Simlation definition error: Initial conditions for DDE "+pageName+" not set!");
        initCond = "return []"; 
      }
      SimulationXML.splitCode(buffer,initCond,res.getString("EquationEditor.DelaysInitialConditionDiscontinuities"),"      ");
      buffer.append("    }\n\n");
       
      buffer.append("    __odeSelf.getInitialCondition = function(_time, _state) {\n");
      buffer.append("      var _userDelayInitCond = _userDefinedInitialCondition(_time);\n");
      buffer.append("      if (_userDelayInitCond!==null) {\n");
      buffer.append("        for (var _i=0, _n=_state.length-1; _i<_n; _i++) _state[_i] = _userDelayInitCond[_i];\n");
      buffer.append("        _state[_state.length-1] = _time;\n");
      buffer.append("      }\n");
      buffer.append("    };\n\n");
    }
    
    buffer.append("    __odeSelf.getRate = function(_aState,_aRate) {\n");
    String prelimCode = mSimulation.getODEConfiguration(mODE, ODE.PRELIMINARY_CODE).trim();
    if (prelimCode.length()>0) {
      buffer.append("      _aRate[_aRate.length-1] = 0.0; // In case the prelim code returns\n");
      buffer.append("      var __index=-1; // so that it can be used in preliminary code\n");
      extractVariablesFromStateCode(buffer,"_aState",true);
      buffer.append("      // Preliminary code: "+mSimulation.getODEConfiguration(mODE, ODE.PRELIMINARY_CODE_COMMENT)+"\n");
      SimulationXML.splitCode(buffer,prelimCode,"Preliminary code for ODE."+pageName,"        ");
    }
    else extractVariablesFromStateCode(buffer,"_aState",true);

    computeRateCode (buffer,"Rate for ODE: "+pageName);
    buffer.append("    }; //end of getRate\n\n");

    if (useDelays) {
    }

    if (zenoAction!=null && zenoAction.trim().length()>0) {
      buffer.append("    // Implementation of org.opensourcephysics.numerics.ZenoEffectListener\n");
      buffer.append("    __odeSelf.zenoEffectAction = function(_anEvent, _aState) {\n");
      extractVariablesFromStateCode(buffer, "_aState",false);
      SimulationXML.splitCode(buffer,zenoAction,"Zeno code for ODE."+pageName,"        ");
      updateStateFromVariablesCode(buffer,"_aState",false);
      buffer.append("        return "+mSimulation.getODEConfiguration(mODE,ODE.ZENO_END_STEP)+";\n");
      buffer.append("      };\n\n");
    }
    // Now build the event classes
    // BLOCKLY EXPERIMENTS
    buffer.append("    __odeSelf._addEvent = function(userCondition,userAction,eventType,eventMethod,maxIter,eventTolerance,endAtEvent){\n");
    buffer.append("    var User_Event = function (userCondition,userAction,eventType,eventMethod,maxIter,eventTolerance,endAtEvent) {\n");
    buffer.append("      var _eventSelf = {};\n\n");
    buffer.append("      _eventSelf.getTypeOfEvent = function() { return eventType; };\n\n");
    buffer.append("      _eventSelf.getRootFindingMethod = function() { return eventMethod; };\n\n");
    buffer.append("      _eventSelf.getMaxIterations = function() { return maxIter; };\n\n");
    buffer.append("      _eventSelf.getTolerance = function() { return eventTolerance; };\n\n");

    buffer.append("      _eventSelf.evaluate = function(_aState) { \n");
    extractVariablesFromStateCode(buffer,"_aState",true);
    buffer.append("      return eval(userCondition);\n");
    buffer.append("      };\n\n");

    buffer.append("      _eventSelf.action = function() { \n");
    extractVariablesFromStateCode(buffer, "__state",false);
    buffer.append("        var _returnValue = __userDefinedAction();\n");
    updateStateFromVariablesCode(buffer, "__state",false);
    buffer.append("        return _returnValue;\n");
    buffer.append("      };\n\n");

    buffer.append("      function __userDefinedAction() {\n");
    buffer.append("        if (undefined != functions) eval(functions.toString());\n");
    buffer.append("        eval(userAction);\n");
    buffer.append("        return endAtEvent;\n");
    buffer.append("      }\n\n");

    buffer.append("      return _eventSelf;\n");
    buffer.append("    }; // End of event\n\n");
    buffer.append("   userEvents"+mPageIndex+".push(User_Event(userCondition,userAction,eventType,eventMethod,maxIter,eventTolerance,endAtEvent));\n");
    buffer.append("   }\n\n");
    
    //////
    
    java.util.List<Element> eventList = mSimulation.getODEEvents(mODE); 
    for (int eventCounter=1,n=eventList.size(); eventCounter<=n; eventCounter++) {
      Element event = eventList.get(eventCounter-1);
      buffer.append("    _ODE_evolution"+mPageIndex+"_Event"+eventCounter+" = function() {\n");
      buffer.append("      var _eventSelf = {};\n\n");
      buffer.append("      _eventSelf.getTypeOfEvent = function() { return EJSS_ODE_SOLVERS.EVENT_TYPE."+mSimulation.getEventConfiguration(event, ODE_EVENT.TYPE)+"; };\n\n");
      buffer.append("      _eventSelf.getRootFindingMethod = function() { return EJSS_ODE_SOLVERS.EVENT_METHOD."+mSimulation.getEventConfiguration(event, ODE_EVENT.METHOD)+"; };\n\n");
      buffer.append("      _eventSelf.getMaxIterations = function() { return "+mSimulation.getEventConfiguration(event, ODE_EVENT.ITERATIONS)+"; };\n\n");
      String eventToleranceStr = mSimulation.getEventConfiguration(event, ODE_EVENT.TOLERANCE).trim();
      if (eventToleranceStr.length()<=0) {
        if (absTol.length()>0) eventToleranceStr = absTol;
      }
      buffer.append("      _eventSelf.getTolerance = function() { return "+eventToleranceStr+"; };\n\n");

      buffer.append("      _eventSelf.evaluate = function(_aState) { \n");
      extractVariablesFromStateCode(buffer,"_aState",true);
      SimulationXML.splitCode(buffer,mSimulation.getEventConfiguration(event, ODE_EVENT.ZERO_CONDITION),"Event zero-condition for page "+pageName,"        ");
      buffer.append("      };\n\n");

      buffer.append("      _eventSelf.action = function() { \n");
      extractVariablesFromStateCode(buffer, "__state",false);
      buffer.append("        var _returnValue = __userDefinedAction();\n");
      updateStateFromVariablesCode(buffer, "__state",false);
      buffer.append("        return _returnValue;\n");
      buffer.append("      };\n\n");

      buffer.append("      function __userDefinedAction() {\n");
      SimulationXML.splitCode(buffer,mSimulation.getEventConfiguration(event, ODE_EVENT.ACTION),"Event action for page "+pageName,"        ");
      buffer.append("        return "+mSimulation.getEventConfiguration(event, ODE_EVENT.END_AT_EVENT)+";\n");
      buffer.append("      }\n\n");

      buffer.append("      return _eventSelf;\n");
      buffer.append("    }; // End of event\n\n");
    }
    // Now build the discontinuity classes
    java.util.List<Element> discList = mSimulation.getODEDiscontinuities(mODE); 
    for (int discontinuityCounter=1,n=discList.size(); discontinuityCounter<=n; discontinuityCounter++) {
      Element discontinuity = discList.get(discontinuityCounter-1);
      buffer.append("    _ODE_evolution"+mPageIndex+"_Discontinuity"+discontinuityCounter+" = function() {\n");
      buffer.append("      var _discontinuitySelf = {};\n\n");
      String discontinuityToleranceStr = mSimulation.getDiscontinuityConfiguration(discontinuity, ODE_DISCONTINUITY.TOLERANCE).trim();
      if (discontinuityToleranceStr.length()<=0) {
        if (absTol.length()>0) discontinuityToleranceStr = absTol;
      }
      buffer.append("      _discontinuitySelf.getTolerance = function() { return "+discontinuityToleranceStr+"; };\n\n");

      buffer.append("      _discontinuitySelf.evaluate = function(_aState) { \n");
      extractVariablesFromStateCode(buffer,"_aState",true);
      SimulationXML.splitCode(buffer,mSimulation.getDiscontinuityConfiguration(discontinuity, ODE_DISCONTINUITY.ZERO_CONDITION),"Discontinity zero-condition for page "+pageName,"        ");
      buffer.append("      };\n\n");

      buffer.append("      _discontinuitySelf.action = function() { \n");
      extractVariablesFromStateCode(buffer, "__state",false);
      buffer.append("        var _returnValue = __userDefinedAction();\n");
      updateStateFromVariablesCode(buffer, "__state",false);
      buffer.append("        return _returnValue;\n");
      buffer.append("      };\n\n");

      buffer.append("      function __userDefinedAction() {\n");
      SimulationXML.splitCode(buffer,mSimulation.getDiscontinuityConfiguration(discontinuity, ODE_DISCONTINUITY.ACTION),"Event action for page "+pageName,"        ");
      buffer.append("        return "+mSimulation.getDiscontinuityConfiguration(discontinuity, ODE_DISCONTINUITY.END_AT_DISCONTINUITY)+";\n");
      buffer.append("      }\n\n");

      buffer.append("      return _discontinuitySelf;\n");
      buffer.append("    }; // End of discontinuity\n\n");
    }

    buffer.append("    __instantiateSolver();\n\n");

    buffer.append("    return __odeSelf;\n");
    buffer.append("  }\n\n");
  }

  // ----------------------------------------------
  // utility methods
  // ----------------------------------------------
  
  // BLOCKLY EXPERIMENTS //
  private String getVarsNames(){
    String names = "[";
    java.util.List<Element> equations = mSimulation.getODEEquations(mODE);
    for (int i=0,n=equations.size(); i<n; i++) {
      Element equation = equations.get(i);
      String state = BasicElement.evaluateNode(equation, "state");
      if(i==0) names+="\""+state+"\"";
      else names+=",\""+state+"\"";
    }
    if(equations.size()>0)names+=",\""+mIndVar+"\"]";
    else names+="\""+mIndVar+"\"]";
    return names;
  }
  /////

  private void extractVariablesFromStateCode(StringBuffer buffer, String stateStr, boolean declareTemporary) {
    buffer.append("      // Extract our variables from "+stateStr+"\n");
    buffer.append("        var __i=0;\n");
    buffer.append("        var __cOut=0;\n");
    java.util.List<Element> equations = mSimulation.getODEEquations(mODE);
    for (int i=0,n=equations.size(); i<n; i++) {
      Element equation = equations.get(i);
      String state = BasicElement.evaluateNode(equation, "state");
      String index = BasicElement.evaluateNode(equation, "index");
      if (index!=null) {
        if (declareTemporary) buffer.append("        var "+state+" = [];\n");
        if (isFollowedByDerivative(equation)) {
          Element nextEquation = equations.get(i+1);
          String nextState = BasicElement.evaluateNode(nextEquation, "state");
          if (declareTemporary) buffer.append("        var "+nextState+" = [];\n");
          buffer.append("        for (__i=0; __i<_"+state+"Length; __i++) { // These two alternate in the state\n");
          buffer.append("          "+state    +"[__i] = "+stateStr+"[__cOut++];\n");
          buffer.append("          "+nextState+"[__i] = "+stateStr+"[__cOut++];\n");
          buffer.append("        }\n");
          i++; // step over the next one
        }
        else {
          buffer.append("        for (__i=0;__i<_"+state+"Length; __i++) {\n");
          buffer.append("          "+state+"[__i] = "+stateStr+"[__cOut++];\n");
          buffer.append("        }\n");
        }
      }
      else {
        if (declareTemporary) buffer.append("        var ");
        else buffer.append("        ");
        buffer.append(state+" = "+stateStr+"[__cOut++];\n");
      }
    }
    if (declareTemporary) buffer.append("        var ");
    else buffer.append("        ");
    buffer.append(mIndVar+" = "+stateStr+"[__cOut++];\n");
  }

  private void updateStateFromVariablesCode(StringBuffer buffer, String stateStr, boolean addSynchro) {
    buffer.append("      // Copy our variables to "+stateStr+"[] \n");
    buffer.append("        var __j=0;\n");
    buffer.append("        var __n=0;\n");
    buffer.append("        var __cIn=0;\n");
    java.util.List<Element> equations = mSimulation.getODEEquations(mODE);
    for (int i=0,n=equations.size(); i<n; i++) {
      Element equation = equations.get(i);
      String state = BasicElement.evaluateNode(equation, "state");
      String index = BasicElement.evaluateNode(equation, "index");
      if (index!=null) {
        if (isFollowedByDerivative(equation)) {
          Element nextEquation = equations.get(i+1);
          String nextState = BasicElement.evaluateNode(nextEquation, "state");
          if (addSynchro) {
            buffer.append("      if (!__mustReinitialize)\n");
            buffer.append("        for (__j=0,__n=__cIn; __j<_"+state+"Length; __j++)\n");
            buffer.append("          if ("+stateStr+"[__n++]!="+state+"[__j] || "+stateStr+"[__n++]!="+nextState+"[__j]) { __mustReinitialize = true; break; }\n");
          }
          buffer.append("      for (__j=0; __j<_"+state+"Length; __j++) { // These two alternate in the state\n");
          buffer.append("         "+stateStr+"[__cIn++] = "+state+"[__j];\n");
          buffer.append("         "+stateStr+"[__cIn++] = "+nextState+"[__j];\n");
          buffer.append("      }\n");
          i++; // step over the next one
        }
        else {
          if (addSynchro) {
            buffer.append("      if (!__mustReinitialize)\n");
            buffer.append("        for (__j=0,__n=__cIn; __j<_"+state+"Length; __j++)\n");
            buffer.append("           if ("+stateStr+"[__n++]!="+state+"[__j]) { __mustReinitialize = true; break; }\n");
          }
          buffer.append("        for (__j=0;__j<_"+state+"Length; __j++) {\n");
          buffer.append("          "+stateStr+"[__cIn++] = "+state+"[__j];\n");
          buffer.append("        }\n");
        }
      }
      else {
        if (addSynchro) buffer.append("        if ("+stateStr+"[__cIn]!="+state+") __mustReinitialize = true;\n");
        buffer.append("        "+stateStr+"[__cIn++] = "+state+";\n");
      }
    }
    if (addSynchro) buffer.append("        if ("+stateStr+"[__cIn]!="+mIndVar+") __mustReinitialize = true;\n");
    buffer.append("        "+stateStr+"[__cIn++] = "+mIndVar+";\n");
  }


  /**
   * Write the code to compute the rate
   * @param _singleRate true if the rate is required only for a simgle index (for MultirateODEs)
   * @param _info
   * @param _variableList
   * @return
   */
  private void computeRateCode (StringBuffer buffer, String info) {
    buffer.append("      // Compute the rate\n");
    buffer.append("        var __cRate=0;\n");
    HashSet<String> indexSet = new HashSet<String>();
    indexSet.add("__i");
    java.util.List<Element> equations = mSimulation.getODEEquations(mODE);
    for (int i=0,n=equations.size(); i<n; i++) {
      Element equation = equations.get(i);
      String state = BasicElement.evaluateNode(equation, "state");
      String fullInfo = " // "+info+":"+state+"\n";
      String rate  = BasicElement.evaluateNode(equation, "rate");
      String index = BasicElement.evaluateNode(equation, "index");
      if (index!=null) {
        if (!indexSet.contains(index)) {
          buffer.append("        var "+index+";\n");
          indexSet.add(index);
        }
        if (isFollowedByDerivative(equation)) {
          Element nextEquation = equations.get(i+1);
          String nextState = BasicElement.evaluateNode(nextEquation, "state");
          String nextRate  = BasicElement.evaluateNode(nextEquation, "rate");
          buffer.append("        for ("+index+"=0;"+index+"<_"+state+"Length;"+index+"++) { // These two alternate in the state\n");
          buffer.append("          _aRate[__cRate++] = Array.isArray("+rate+") ? "+ rate+ "["+index+"] : "+rate+";"+fullInfo);
          buffer.append("          _aRate[__cRate++] = Array.isArray("+nextRate+") ? "+ nextRate+ "["+index+"] : "+nextRate+"; // "+info+":"+nextState+"\n");
          buffer.append("        }\n");
          i++;
        }
        else {
          buffer.append("        for ("+index+"=0;"+index+"<_"+state+"Length;"+index+"++) {\n");
          buffer.append("          _aRate[__cRate++] = Array.isArray("+rate+") ? "+ rate+ "["+index+"] : "+ rate +";"+fullInfo);
          buffer.append("        }\n");
        }
      }
      else {
        buffer.append("        _aRate[__cRate++] = "+rate+";"+fullInfo);
      }
    }
    buffer.append("        _aRate[__cRate++] = 1; // independent variable\n");
    buffer.append("        return _aRate;\n");
  }

  /**
   * Creates a method to retrieve past values (from the memory) for each variable in the ODE
   * @param _editor
   * @param _variableList
   * @return
   */
  private void createFunctionsCode(StringBuffer buffer) {
    String odeName = "_ODEi_evolution"+mPageIndex;
    String totalSizeStr = "0";
    java.util.List<Element> equations = mSimulation.getODEEquations(mODE);
    for (int i=0,n=equations.size(); i<n; i++) {
      Element equation = equations.get(i);
      String state = BasicElement.evaluateNode(equation, "state");
      String index = BasicElement.evaluateNode(equation, "index");
      buffer.append("  function _historic_" + state + "(__time) {\n");
      if (index!=null) {
        buffer.append("    var __beginIndex = "+totalSizeStr+";\n");
        buffer.append("    return "+odeName+".getEventSolver().getStateHistory().interpolate(__time,new Array("+state+".length),__beginIndex,"+state+".ength);\n");
        buffer.append("  }\n\n");
        totalSizeStr += " + "+state+".length";
      }    
      else {
        buffer.append("    var __index = "+totalSizeStr+";\n");
        buffer.append("    return "+odeName+".getEventSolver().getStateHistory().interpolate(__time,__index);\n");
        buffer.append("  }\n\n");
        totalSizeStr += " + 1";
      }
    }
  }

}

/**
 * Copyright (c) June 2003 F. Esquembre
 * Last revision: September 2008
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.ode_editor;

import java.util.Vector;

import javax.swing.JOptionPane;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.*;
import org.colos.ejs.osejs.edition.variables.VariablesEditor;
//import org.colos.ejs.osejs.edition.experiments.*;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.utils.StringAndInteger;

public class EquationCode {
  static private ResourceUtil res = new ResourceUtil ("Resources");
  
  /**
   * Fills the vector with the information about the variables in the given ODE editor
   * @param _ejs
   * @param _editor
   * @param _indVar
   * @param varNames
   * @return the total size as a String and the number of variables which are arrays
   */
  static StringAndInteger getEquationVariables(Osejs _ejs, EquationEditor _editor, String _indVar, Vector<EquationVariable> _variableList) {
    Vector<?> table = _editor.getDataVector();
    String totalSizeStr = "1";  // For the independent variable
    int arrayCounter = 0;
    for (int rowNumber=0,rowCount=table.size(); rowNumber<rowCount; rowNumber++) { // construct the list of variables
      Vector<?> row = (Vector<?>) table.get(rowNumber);
      String rateString = row.get(1).toString().trim(); 
      if (rateString.length()>0) { // The rate cell is not empty
        String stateString = row.get(0).toString().trim();
        if (stateString.length()<=0) continue; // The state cell is empty
        boolean isArray = true;
        if (stateString.indexOf("[")<0) { // If is has no [] or [i], it could still be an array.
          isArray = _ejs.getModelEditor().getVariablesEditor().isDoubleArray(stateString); // ask the variables editor
//          if (isArray) stateString = stateString+"[]"; // Allow for specifying arrays as x instead of x[]
        }
        if (isArray) {
          int index = stateString.indexOf ("[");
          String varName = (index<0) ? stateString : stateString.substring (0,index).trim();
          _variableList.add (new EquationVariable(varName,rowNumber,true,stateString,rateString));
          totalSizeStr += "+"+varName+".length";
          arrayCounter++;
        }
        else {
          _variableList.add (new EquationVariable(stateString,rowNumber,false,stateString,rateString));
          totalSizeStr += "+1";
        }
      }
    }
    if (!_variableList.isEmpty()) { // See which variable is followed by its derivative
      EquationVariable currentVariable = _variableList.get(0);
      for (int i=1, n=_variableList.size(); i<n; i++) {
        EquationVariable nextVariable = _variableList.get(i);
//        System.out.println ("Current rate = "+currentVariable.getRateString());
//        System.out.println ("Next state   = "+nextVariable.getStateString());
//        System.out.println ("  is followed = "+ currentVariable.getRateString().equals(nextVariable.getStateString()));
        currentVariable.setFollowedByDerivative(currentVariable.getRateString().equals(nextVariable.getStateString()));
        currentVariable = nextVariable;
      }
    } // end of for
    
    // The independent variable goes last (OSP Convention)
    _variableList.add(new EquationVariable(_indVar,-1,false,_indVar,"1"));
    return new StringAndInteger(totalSizeStr,arrayCounter);
  }

  /**
   * Returns a StringBuffer with the code required to extract our variables from a single
   * unidmensional array, indicated by _fromState. Thsi code is used several times in the 
   * process of solving ODEs
   * @param _fromState
   * @param varNamesList
   * @return
   */
  static private StringBuffer extractVariablesFromStateCode(String _fromState, boolean _declareTemporary, Vector<EquationVariable> _variableList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("      // Extract our variables from "+_fromState+"\n");
    buffer.append("      int __cOut=0;\n");
    for (int i=0,n=_variableList.size(); i<n; i++) {
      EquationVariable eqnVar = _variableList.get(i);
      String varName = eqnVar.getName();
//      System.out.println ("Var "+varName +" is followed by its derivative="+eqnVar.isFollowedByDerivative());
      if (eqnVar.isArray()) {
        if (_declareTemporary) buffer.append("      double[] "+varName+" = _"+varName+";\n");
        if (eqnVar.isFollowedByDerivative()) {
          EquationVariable nextVar = _variableList.get(i+1);
          String nextVarName = nextVar.getName();
          if (_declareTemporary) buffer.append("      double[] "+nextVarName+" = _"+nextVarName+";\n");
          buffer.append("      for (int __i=0; __i<"+varName+".length; __i++) { // These two alternate in the state\n");
          buffer.append("        "+varName+"[__i] = "+_fromState+"[__cOut++];\n");
          buffer.append("        "+nextVarName+"[__i] = "+_fromState+"[__cOut++];\n");
          buffer.append("      }\n");
          i++; // step over the next one
        }
        else buffer.append("      System.arraycopy("+_fromState+",__cOut,"+varName+",0,"+varName+".length); __cOut+="+varName+".length;\n");
      }
      else {
        if (_declareTemporary) buffer.append("      double ");
        else buffer.append("      ");
        buffer.append(varName+" = "+_fromState+"[__cOut++];\n");
      }
    }
    return buffer;
  }
  
  static private StringBuffer updateStateFromVariablesCode(String _toState, boolean _addSynchro, Vector<EquationVariable> _variableList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("      // Copy our variables to "+_toState+"[] \n");
    buffer.append("      int __cIn=0;\n"); 
    for (int i=0,n=_variableList.size(); i<n; i++) {
      EquationVariable eqnVar = _variableList.get(i);
      String varName = eqnVar.getName();
      if (eqnVar.isArray()) {
        if (eqnVar.isFollowedByDerivative()) {
          EquationVariable nextVar = _variableList.get(i+1);
          String nextVarName = nextVar.getName();
          if (_addSynchro) {
            buffer.append("      if (!__mustReinitialize)\n");
            buffer.append("        for (int __i=0,__n=__cIn; __i<"+varName+".length; __i++)\n");
            buffer.append("          if ("+_toState+"[__n++]!="+varName+"[__i] || "+_toState+"[__n++]!="+nextVarName+"[__i]) { __mustReinitialize = true; break; }\n");
          }
          buffer.append("      for (int __i=0; __i<"+varName+".length; __i++) { // These two alternate in the state\n");
          buffer.append("         "+_toState+"[__cIn++] = "+varName+"[__i];\n");
          buffer.append("         "+_toState+"[__cIn++] = "+nextVarName+"[__i];\n");
          buffer.append("      }\n");
          i++; // step over the next one
        }
        else {
          if (_addSynchro) {
            buffer.append("      if (!__mustReinitialize)\n");
            buffer.append("        for (int __i=0,__n=__cIn; __i<"+varName+".length; __i++)\n");
            buffer.append("           if ("+_toState+"[__n++]!="+varName+"[__i]) { __mustReinitialize = true; break; }\n");
          }
          buffer.append("      System.arraycopy("+varName+",0,"+_toState+",__cIn,"+varName+".length); __cIn += "+varName+".length;\n");
        }
      }
      else {
        if (_addSynchro) buffer.append("      if ("+_toState+"[__cIn]!="+varName+") __mustReinitialize = true;\n");
        buffer.append("      "+_toState+"[__cIn++] = "+varName+";\n");
      }
    }
    return buffer;
  }
 
  /**
   * Creates a method to retrieve past values (from the memory) for each variable in the ODE
   * @param _editor
   * @param _variableList
   * @return
   */
  static private StringBuffer createFunctionsCode(EquationEditor _editor, String _odeName, String _indVar, Vector<EquationVariable> _variableList) {
    StringBuffer buffer = new StringBuffer();
    String totalSizeStr = "0";
    for (int i=0,n=_variableList.size()-1; i<n; i++) { // -1 because the last one is time
      EquationVariable eqnVar = _variableList.get(i);
      String varName = eqnVar.getName();
      if (eqnVar.isArray()) { 
        buffer.append("    public double[] " + varName + " (double __time) {\n");
        buffer.append("      int __beginIndex = "+totalSizeStr+";\n");
        buffer.append("      return "+_odeName+".__eventSolver.getStateHistory().interpolate(__time,new double["+varName+".length],__beginIndex,"+varName+".length);\n");
        buffer.append("    }\n\n");
        totalSizeStr += "+"+varName+".length";
      }    
      else {
        buffer.append("    public double " + varName + " (double __time) {\n");
        buffer.append("      int __index = "+totalSizeStr+";\n");
        buffer.append("      return "+_odeName+".__eventSolver.getStateHistory().interpolate(__time,__index);\n");
        buffer.append("    }\n\n");
        totalSizeStr += "+1";
      }
    }
    return buffer;
  }
    
    static private void declareFunctionsCode(EquationEditor _editor, Vector<EquationVariable> _variableList, VariablesEditor _varEditor) throws Exception {
      for (int i=0,n=_variableList.size()-1; i<n; i++) { // -1 because the last one is time
        EquationVariable eqnVar = _variableList.get(i);
        String varName = eqnVar.getName();
        if (eqnVar.isArray())  _varEditor.evaluateExpression("double[] " + varName + " (double __time) { return null; }\n");
        else                   _varEditor.evaluateExpression("double " + varName + " (double __time) { return 0; }\n");
      }
  }
  
  /**
   * Write the code to compute the rate
   * @param _singleRate true if the rate is required only for a simgle index (for MultirateODEs)
   * @param _info
   * @param _variableList
   * @return
   */
  static private StringBuffer computeRateCode (boolean _singleRate, String _info, Vector<EquationVariable> _variableList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("      // Compute the rate\n");
    buffer.append("      int __cRate = 0;\n");
    for (int varNumber=0,numVars=_variableList.size()-1; varNumber<numVars; varNumber++) {
      EquationVariable eqnVar = _variableList.get(varNumber);
      // location information for error display
      String fullInfo = " // "+_info+":"+(eqnVar.getRowNumber()+1)+"\n";
      if (eqnVar.isArray()) {
        String varLengthString = eqnVar.getName()+".length";
        String stateString = eqnVar.getStateString();
        String indexString = OsejsCommon.getPiece(stateString,"[","]",false); // i.e. "[i]" or "[]"
        if (eqnVar.isFollowedByDerivative()) {
          EquationVariable nextVar = _variableList.get(varNumber+1);
          if (indexString==null || indexString.length()<=0) { // The index was not specified
            if (_singleRate) {
              buffer.append("      if (__index<(__cRate+2*"+varLengthString+")) {\n");
              buffer.append("        int __indexDelta = __index-__cRate;\n");
              buffer.append("        int __theIndex = __indexDelta/2;\n");
              buffer.append("        if (__indexDelta % 2 == 0) return "+eqnVar.getRateString() +"[__theIndex];"+fullInfo);
              buffer.append("        else                       return "+nextVar.getRateString()+"[__theIndex]; // "+_info+":"+(nextVar.getRowNumber()+1)+"\n");
              buffer.append("      }\n");
              buffer.append ("      __cRate += 2*"+varLengthString+";\n");
            }
            else {
              buffer.append("      for (int __i=0; __i<"+varLengthString+"; __i++) {\n");
              buffer.append("        __aRate[__cRate++] = "+eqnVar.getRateString() +"[__i];"+fullInfo);
              buffer.append("        __aRate[__cRate++] = "+nextVar.getRateString()+"[__i]; // "+_info+":"+(nextVar.getRowNumber()+1)+"\n");
              buffer.append("      }\n");
            }
          }
          else { // The index WAS specified
            if (_singleRate) {
              buffer.append("      if (__index<(__cRate+2*"+varLengthString+")) {\n");
              buffer.append("        int __indexDelta = __index-__cRate;\n");
              buffer.append("        int "+indexString+" = __indexDelta/2;\n");
              buffer.append("        if (__indexDelta % 2 == 0) return "+eqnVar.getRateString() +";"+fullInfo);
              buffer.append("        else                       return "+nextVar.getRateString()+"; // "+_info+":"+(nextVar.getRowNumber()+1)+"\n");
              buffer.append("      }\n");
              buffer.append ("      __cRate += 2*"+varLengthString+";\n");
            }
            else {
              buffer.append("      for (int "+indexString+"=0; "+indexString+"<"+varLengthString+"; "+indexString+"++) {\n");
              buffer.append("        __aRate[__cRate++] = "+eqnVar.getRateString() +";"+fullInfo);
              buffer.append("        __aRate[__cRate++] = "+nextVar.getRateString()+"; // "+_info+":"+(nextVar.getRowNumber()+1)+"\n");
              buffer.append("      }\n");
            }
          }
          varNumber++;
        }
        else { // Not followed by its derivative
          if (indexString==null || indexString.length()<=0) { // The index was not specified
            if (_singleRate) buffer.append("      if (__index<(__cRate+"+varLengthString+")) return "+eqnVar.getRateString()+"[__index-__cRate];"+fullInfo);
            else buffer.append ("      System.arraycopy("+eqnVar.getRateString()+",0,__aRate,__cRate,_"+varLengthString+");"+fullInfo);
            buffer.append ("      __cRate += "+varLengthString+";\n");
          }
          else { // The index WAS specified
            if (_singleRate) {
              buffer.append("      if (__index<__cRate+"+varLengthString+") { int "+indexString+" = __index-__cRate; return "+eqnVar.getRateString()+"; }"+fullInfo);
              buffer.append("      __cRate += "+varLengthString+";\n");
            }
            else buffer.append("      for (int "+indexString+"=0; "+indexString+"<"+varLengthString+"; "+indexString+"++) __aRate[__cRate++] = "+eqnVar.getRateString()+";"+fullInfo);
          }
        }
      }
      else {  // It's a single variable
        if (_singleRate) {
          buffer.append("      if (__index<=__cRate) return "+eqnVar.getRateString()+";" + fullInfo);
          buffer.append("      __cRate++;\n");
        }
        else buffer.append("      __aRate[__cRate++] = "+eqnVar.getRateString()+";"+fullInfo);
      }
    } // for ends
    if (_singleRate) buffer.append("      return 1.0; // The independent variable\n");
    else buffer.append("      __aRate[__cRate++] = 1.0; // The independent variable \n");
    return buffer;
  }

  /**
   *  Checks whether the ODE has the x - dx ordering required by Verlet methods
   *  Returns an empty String if true. And a non-empty String with the variables that are not followed by their derivatives
   */
  static private String respectsVerletOrdering(Vector<EquationVariable> _variableList) {
    StringBuffer buffer = new StringBuffer();
    for (int i=0, num=_variableList.size()-1; i<num; i+=2) { // Check every second variable, excluding time
      EquationVariable eqnVar = _variableList.get(i);
      if (!eqnVar.isFollowedByDerivative()) buffer.append(eqnVar.getName()+"\n");
    }
    return buffer.toString();
  }

  /**
   * Adds to the interpreter the declaration of a method for each of the state variables of the ODE 
   * @param _interpreter
   */
  static public void addToInterpreter (Osejs _ejs, EquationEditor _editor, String _indVar) {
    boolean useDelays = _editor.getDelays().length()>0;
    String historyLength = _editor.getMemoryLength();
    boolean useDDESolvers = historyLength.length()>0 || useDelays;
    if (useDDESolvers) {
      try {
        Vector<EquationVariable> variableList = new Vector<EquationVariable>();
        getEquationVariables(_ejs,_editor,_indVar,variableList);
        declareFunctionsCode(_editor,variableList,_ejs.getModelEditor().getVariablesEditor());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // Example:
  //  _solverType = "RK4"
  //  _generateName = "evolution1"
  //  _info = "Evolution:My page"
  //  _indVar = "time"
  // _eventEditor is the editor for events for this ODE (if any)
  static public StringBuffer generateCode (Osejs _ejs, EquationEditor _editor,
      String _generateName, String _info, String _indVar) {

    
    // Extract the list of variables from the editor for further use
    Vector<EquationVariable> variableList = new Vector<EquationVariable>();
    StringAndInteger  sai = getEquationVariables(_ejs,_editor,_indVar,variableList);
    final int arrayCounter = sai.getInteger();
    final String totalSizeStr = sai.getString();

    if (_editor.getSolverType().contains("Verlet")) {
      String wrongList = respectsVerletOrdering(variableList);
      if (wrongList.length()>0) JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Experiment.ScheduledEvent.ODEPage")+": "+_editor.getName()+"\n"+
          res.getString("EquationEditor.VerletOrderingNotRespected")+wrongList,
            res.getString("Warning"), JOptionPane.WARNING_MESSAGE);
    }

//    boolean hasScheduleEvents=_ejs.getExperimentEditor().hasScheduleEvents();

    // Now build the class
    String pageName = _editor.getName();
    String classname = "_ODE_"+_generateName;

    String toleranceStr = null;
    String absTol = _editor.getAbsoluteToleranceStr();
    if (absTol.length()>0) toleranceStr = "setTolerances("+absTol+","+_editor.getRelativeToleranceStr()+")";

    String incidenceMatrix = _editor.getIncidenceMatrix();
    boolean isMultirate = incidenceMatrix.length()>0; 
    boolean manualSynchro = !_editor.getForceSynchro();
    boolean useDelays = _editor.getDelays().length()>0;
    String historyLength = _editor.getMemoryLength();
    boolean useDDESolvers = historyLength.length()>0 || useDelays;

    StringBuffer txt = new StringBuffer();
    
    txt.append("  private " +classname+ " _ODEi_"+_generateName+";\n\n");
    if (useDDESolvers) txt.append(createFunctionsCode(_editor, "_ODEi_"+_generateName, _indVar, variableList));

    txt.append("\n  // ----------- private class for ODE in page "+_info+"\n\n");
    txt.append("  private class "+classname + " implements org.opensourcephysics.numerics.ode_solvers.EjsS_ODE, org.opensourcephysics.numerics.ode_solvers.symplectic.VelocityVerletSavvy");
    if (useDelays)   txt.append(", org.opensourcephysics.numerics.ode_solvers.DelayDifferentialEquation");
    if (isMultirate) txt.append(", org.opensourcephysics.numerics.qss.MultirateODE");
    if (_editor.zenoEditor!=null) txt.append(", org.opensourcephysics.numerics.ode_solvers.ZenoEffectListener");
    txt.append(" {\n");

    txt.append("    private org.opensourcephysics.numerics.ode_solvers.SolverEngine __solver=null; // The solver engine\n");
    txt.append("    private org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver __eventSolver=null; // The event solver\n");
    txt.append("    private Class<?> __solverClass=null; // The solver class\n");
    txt.append("    private double[] __state=null; // Our state array\n");

//    txt.append("    private double __currentTime; // The time we called rate\n");
    //    txt.append("    private boolean __initialized=false; // Whether the solver has been initialized\n");
    
    boolean hasErrorCode = _editor.getErrorHandlingCode().toString().trim().length()>0;
    if (!hasErrorCode) txt.append("    private boolean __ignoreErrors=false; // Whether to ignore solver errors\n");
    txt.append("    private boolean __mustInitialize=true; // Be sure to initialize the solver\n");
    txt.append("    private boolean __isEnabled=true; // Whether it is enabled\n");
    txt.append("    private boolean __mustUserReinitialize=false; // Whether the user asked to reset the solver\n");
    if (manualSynchro) txt.append("    private boolean __mustReinitialize=true; // flag to reinitialize the solver\n");
    if (isMultirate) txt.append("    private int[][] __directArray, __inverseArray; // implementation of MultirateODE\n");

    txt.append("\n");

    if (arrayCounter>0) { // create temporary variables for arrays
      txt.append("    // Temporary array variables matching those defined by the user\n") ;
      for (EquationVariable eqnVar : variableList) {
        if (eqnVar.isArray()) txt.append("    private double[] _"+eqnVar.getName()+";\n");
      }
      txt.append("\n");
    }
    
    txt.append("    "+classname + "() { // Class constructor\n");
    txt.append("      __solverClass = org.opensourcephysics.numerics.ode_solvers."+_editor.getSolverType()+".class;\n");
    txt.append("      __instantiateSolver();\n");
    txt.append("      _privateOdesList.put(\""+pageName+"\",this);\n");
    txt.append("    }\n\n");

    txt.append("    public org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver getEventSolver() { return __eventSolver; } \n\n");

    txt.append("    public void setSolverClass (Class<?> __aSolverClass) { // Change the solver in run-time\n");
    txt.append("      this.__solverClass = __aSolverClass;\n"); 
    txt.append("      __instantiateSolver();\n");
    //    txt.append("      __initializeSolver();\n");
    txt.append("    }\n\n"); 

    txt.append("    public String setSolverClass (String _solverClassName) { // Change the solver in run-time\n");
    txt.append("      String _prefix = \"org.opensourcephysics.numerics.ode_solvers.\";\n");  
    txt.append("      _solverClassName = _solverClassName.trim().toLowerCase();\n");
    txt.append("      if (_solverClassName.indexOf(\"euler\")>=0) {\n");
    txt.append("        if (_solverClassName.indexOf(\"rich\")>=0) _solverClassName = _prefix + \"rk.EulerRichardson\";\n");
    txt.append("        else _solverClassName = _prefix + \"rk.Euler\";\n");
    txt.append("      }\n");
    txt.append("      else if (_solverClassName.indexOf(\"verlet\")>=0) _solverClassName = _prefix + \"symplectic.VelocityVerlet\";\n");
    txt.append("      else if (_solverClassName.indexOf(\"runge\")>=0)  _solverClassName = _prefix + \"rk.RK4\";\n");
    txt.append("      else if (_solverClassName.indexOf(\"rk4\")>=0)    _solverClassName = _prefix + \"rk.RK4\";\n");
    txt.append("      else if (_solverClassName.indexOf(\"boga\")>=0)  _solverClassName = _prefix + \"rk.BogackiShampine23\";\n");
    txt.append("      else if (_solverClassName.indexOf(\"cash\")>=0)  _solverClassName = _prefix + \"rk.CashKarp45\";\n");
    txt.append("      else if (_solverClassName.indexOf(\"fehl\")>=0) {\n");
    txt.append("        if (_solverClassName.indexOf(\"7\")>=0) _solverClassName = _prefix + \"rk.Fehlberg78\";\n");
    txt.append("        else _solverClassName = _prefix + \"rk.Fehlberg8\";\n");
    txt.append("      }\n");
    txt.append("      else if (_solverClassName.indexOf(\"dorm\")>=0 || _solverClassName.indexOf(\"dopri\")>=0) {\n");
    txt.append("        if (_solverClassName.indexOf(\"8\")>=0) _solverClassName = _prefix + \"rk.Dopri853\";\n");
    txt.append("        else _solverClassName = _prefix + \"rk.Dopri5\";\n");
    txt.append("      }\n");
    txt.append("      else if (_solverClassName.indexOf(\"radau\")>=0) _solverClassName = _prefix + \"rk.Radau5\";\n");
//    txt.append("      else if (_solverClassName.indexOf(\"qss\")>=0) _solverClassName = _prefix + \"qss.Qss3\";\n");
    txt.append("      else { System.err.println (\"There is no solver with this name \"+_solverClassName); return null; }\n");
    txt.append("      try { setSolverClass(Class.forName(_solverClassName)); }\n");
    txt.append("      catch (Exception exc) { exc.printStackTrace(); }\n");
    txt.append("      return _solverClassName;\n");
    txt.append("    }\n\n");

    //    txt.append("    public void __setEnabled(boolean _enabled) { __enabled = _enabled; } \n\n");

    txt.append("    private void __instantiateSolver () {\n");
    txt.append("      __state = new double["+totalSizeStr+"];\n");
    if (arrayCounter>0) {
      txt.append("      // allocate temporary arrays\n");
      for (EquationVariable eqnVar : variableList) {
        if (eqnVar.isArray()) txt.append("      _"+eqnVar.getName()+" = new double["+eqnVar.getName()+".length];\n");
      }
    }
    txt.append("      __pushState();\n");
    if (isMultirate) {
      txt.append("      __directArray = __recomputeDirectArray();\n");
      txt.append("      __inverseArray = org.opensourcephysics.numerics.qss.MultirateUtils.getReciprocalMatrix(__directArray);\n");
    }
    txt.append("      try { // Create the solver by reflection\n");
    txt.append("        Class<?>[] __c = { };\n");
    txt.append("        Object[] __o = { };\n");
    txt.append("        java.lang.reflect.Constructor<?> __constructor = __solverClass.getDeclaredConstructor(__c);\n");
    txt.append("        __solver = (org.opensourcephysics.numerics.ode_solvers.SolverEngine) __constructor.newInstance(__o);\n");
    txt.append("      } catch (Exception exc) { exc.printStackTrace(); } \n");
    txt.append("      __eventSolver = new org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver(__solver,this);\n");
    txt.append("      __mustInitialize = true;\n"); 
    txt.append("    }\n\n");

    txt.append("    public void setEnabled (boolean __enabled) { __isEnabled = __enabled; }\n\n");
    txt.append("    public double getIndependentVariableValue () { return __eventSolver.getIndependentVariableValue(); }\n\n");

    txt.append("    public double getInternalStepSize () { return __eventSolver.getInternalStepSize(); }\n\n");

    txt.append("    public boolean isAccelerationIndependentOfVelocity() { return "+ _editor.getAccelerationIndependentOfVelocity() +"; }\n\n");
    
    txt.append("    public void initializeSolver () {\n");
    
    if (arrayCounter>0)  {
      txt.append("      if (__arraysChanged()) { __instantiateSolver(); initializeSolver(); return; }\n");
    }
    txt.append("      __pushState();\n");
    //    txt.append("      System.err.println (\"Initing \"+this);\n");
    String intStep = _editor.getInternalStepSize();
    if (intStep.length()<=0) txt.append("      __eventSolver.initialize("+_editor.getReadStepSize()+");\n");
    else txt.append("      __eventSolver.initialize("+intStep+");\n");
    txt.append("      __eventSolver.setBestInterpolation("+_editor.getUseBestInterpolation()+");\n");
    if (historyLength.length()>0) txt.append("      __eventSolver.setHistoryLength("+_editor.getMemoryLength()+");\n");
    
    String maxStep = _editor.getMaximumStepSize();
    if (maxStep.length()>0) txt.append("      __eventSolver.setMaximumInternalStepSize("+maxStep+");\n");
    String maxNumberOfSteps = _editor.getMaximumNumberOfSteps();
    if (maxNumberOfSteps.length()>0) txt.append("      __eventSolver.setMaximumInternalSteps("+maxNumberOfSteps+");\n");
    txt.append("      __eventSolver.removeAllEvents();\n");
    if (_editor.eventEditor!=null) {
      Vector<Editor> eventList = _editor.eventEditor.getPages();
      for (int counter=1,n=eventList.size(); counter<=n; counter++) {
        //        EventEditor anEvent = (EventEditor) eventList.elementAt(counter-1);
//        txt.append("      if (_isEnabled_"+_generateName+"_Event"+counter+" && _isEnabled_" + _generateName + ") __eventSolver.addEvent( new "+ classname +"_Event"+counter+"());\n");
        txt.append("      if (_isEnabled_"+_generateName+"_Event"+counter+") __eventSolver.addEvent( new "+ classname +"_Event"+counter+"());\n");
      }
      if (_editor.zenoEditor!=null) txt.append("      __eventSolver.addZenoEffectListener(this);\n");
    }
    {
      Vector<Editor> discontinuitiesList = _editor.discontinuityEditor.getPages();
      for (int counter=1,n=discontinuitiesList.size(); counter<=n; counter++) {
//        txt.append("      if (_isEnabled_"+_generateName+"_Discontinuity"+counter+" && _isEnabled_" + _generateName + ") __eventSolver.addDiscontinuity( new "+ classname +"_Discontinuity"+counter+"());\n");
        txt.append("      if (_isEnabled_"+_generateName+"_Discontinuity"+counter+") __eventSolver.addDiscontinuity( new "+ classname +"_Discontinuity"+counter+"());\n");
      }
    }
//    if (hasScheduleEvents) {
//      txt.append("      java.util.ArrayList ___list = (java.util.ArrayList) _scheduledEventsList.get(\""+pageName+"\");\n");
//      txt.append("      if (___list!=null) {\n");
//      txt.append("        for (java.util.Iterator ___iterator = ___list.iterator(); ___iterator.hasNext(); ) {\n");
//      txt.append("          int _seCounter = ((Integer) ___iterator.next()).intValue();\n");
//      txt.append("          org.opensourcephysics.numerics.StateEvent _event = _createScheduledEvent(_seCounter);\n");
//      txt.append("          if (_event!=null) __eventSolver.addEvent(_event);\n");
//      txt.append("        } // end of for\n");
//      txt.append("      } // end of if\n");
//    }


    txt.append("      __eventSolver.setEstimateFirstStep("+_editor.getEstimateFirstStep()+");\n");
    txt.append("      __eventSolver.setEnableExceptions(false);\n");
//    txt.append("      __eventSolver.setEnableExceptions("+hasErrorCode+");\n");

    if (toleranceStr!=null) txt.append("      __eventSolver."+toleranceStr+";\n");
    if (manualSynchro) txt.append("      __mustReinitialize = true;\n");
    //    txt.append("      __initialized = true;\n");
    txt.append("      __mustInitialize = false;\n"); 
    
    txt.append("    }\n\n");

    txt.append("    private void __pushState () { // Copy our variables to the state\n");
    txt.append(updateStateFromVariablesCode("__state",manualSynchro, variableList));
    txt.append("    }\n\n");

    if (arrayCounter>0) {
      txt.append("    private boolean __arraysChanged () {\n");
      for (EquationVariable eqnVar : variableList) {
        if (eqnVar.isArray()) txt.append("      if ("+eqnVar.getName()+".length != _"+eqnVar.getName()+".length) return true;\n");
      }
      txt.append("      return false;\n");
      txt.append("    }\n\n");
    }  // end of arrayCounter>0

    txt.append("    public void resetSolver () {\n");
    //    txt.append("      if (!__initialized) initializeSolver();\n");

    if (isMultirate) {
      txt.append("      __directArray = __recomputeDirectArray();\n");
      txt.append("      __inverseArray = org.opensourcephysics.numerics.qss.MultirateUtils.getReciprocalMatrix(__directArray);\n");
    }
//    txt.append("      __eventSolver.userReinitialize();\n");
    txt.append("      __mustUserReinitialize = true;\n");
    //    txt.append("      System.err.println (\"period at reinit = \"+period);\n");
    txt.append("    }\n\n");

    txt.append("    public void automaticResetSolver () {\n");
    if (isMultirate) {
      txt.append("      __directArray = __recomputeDirectArray();\n");
      txt.append("      __inverseArray = org.opensourcephysics.numerics.qss.MultirateUtils.getReciprocalMatrix(__directArray);\n");
    }
    if (manualSynchro) txt.append("      __mustReinitialize = true;\n");
    //    txt.append("      System.err.println (\"period at reinit = \"+period);\n");
    txt.append("    }\n\n");

    //    txt.append("    public void debugTask () { System.err.println (\"period = \"+period); }\n\n");

    txt.append("    private void __errorAction () {\n");
    if (hasErrorCode) {
      if (manualSynchro) {
        txt.append("      // Make sure the solver is reinitialized;\n");
        txt.append("      __mustReinitialize = true;\n");
      }
      txt.append("      org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.ERROR _errorCode = __eventSolver.getErrorCode();\n");
      txt.append(_editor.getErrorHandlingCode());
    }
    else {
      txt.append("      if (__ignoreErrors) return;\n");
      txt.append("      System.err.println (__eventSolver.getErrorMessage());\n");
      txt.append("      int __option = javax.swing.JOptionPane.showConfirmDialog(_view.getComponent(_simulation.getMainWindow()),org.colos.ejs.library.Simulation.getEjsString(\"ODEError.Continue\"),\n");
      txt.append("        org.colos.ejs.library.Simulation.getEjsString(\"Error\"), javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);\n");
      txt.append("      if (__option==javax.swing.JOptionPane.YES_OPTION) __ignoreErrors = true;\n");
      txt.append("      else if (__option==javax.swing.JOptionPane.CANCEL_OPTION) _pause();\n");
      if (manualSynchro) {
        txt.append("      // Make sure the solver is reinitialized;\n");
        txt.append("      __mustReinitialize = true;\n");
      }
    }
    txt.append("    }\n\n");

    txt.append("    public double step() { return __privateStep(false); }\n\n");
    txt.append("    public double solverStep() { return __privateStep(true); }\n\n");

    txt.append("    private double __privateStep(boolean __takeMaximumStep) {\n");
    txt.append("      if (!__isEnabled) return 0;\n"); 
    txt.append("      if ("+_editor.getReadStepSize()+"==0) return 0;\n"); 
    txt.append("      if (__mustInitialize) initializeSolver();\n"); 
    if (arrayCounter>0)   txt.append("      if (__arraysChanged()) { __instantiateSolver(); initializeSolver(); }\n");
    txt.append("      __eventSolver.setStepSize("+_editor.getReadStepSize()+");\n");
    if (intStep.length()>0) txt.append("      __eventSolver.setInternalStepSize("+intStep+");\n");
    else txt.append("      __eventSolver.setInternalStepSize("+_editor.getReadStepSize()+");\n");
    if (historyLength.length()>0) txt.append("      __eventSolver.setHistoryLength("+_editor.getMemoryLength()+");\n");
    if (maxStep.length()>0) txt.append("      __eventSolver.setMaximumInternalStepSize("+maxStep+");\n");
    if (maxNumberOfSteps.length()>0) txt.append("      __eventSolver.setMaximumInternalSteps("+maxNumberOfSteps+");\n");
    String eventStep = _editor.getEventMaximumStep();
    if (eventStep.length()>0) txt.append("      __eventSolver.setMaximumEventStep("+eventStep+");\n");
    if (toleranceStr!=null) txt.append("      __eventSolver."+toleranceStr+";\n");
    txt.append("      __pushState();\n");
    //    txt.append("      synchronized(_model) {\n");
    
    txt.append("      if (__mustUserReinitialize) { \n");
    txt.append("        __eventSolver.userReinitialize();\n");
    txt.append("        __mustUserReinitialize = false;\n"); 
    if (manualSynchro) txt.append("        __mustReinitialize = false;\n"); 
    txt.append("        if (__eventSolver.getErrorCode()!=org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.ERROR.NO_ERROR) __errorAction();\n");
    txt.append("      }\n");
    if (manualSynchro) {
      txt.append("      else if (__mustReinitialize) { \n");
      //      txt.append("        System.err.println (\"period at REINIT = \"+period);\n");
      txt.append("        __eventSolver.reinitialize();\n");
      txt.append("        __mustReinitialize = false;\n"); 
      txt.append("        if (__eventSolver.getErrorCode()!=org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.ERROR.NO_ERROR) __errorAction();\n");
      txt.append("      }\n"); 
    }
    else {
      txt.append("      __eventSolver.reinitialize(); // force synchronization: inefficient!\n");
      txt.append("      if (__eventSolver.getErrorCode()!=org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.ERROR.NO_ERROR) __errorAction();\n");
    }

    txt.append("      double __stepTaken = __takeMaximumStep ? __eventSolver.maxStep() : __eventSolver.step();\n");
    
    txt.append(extractVariablesFromStateCode("__state",false,variableList));
    txt.append("      // Check for error\n");
    txt.append("      if (__eventSolver.getErrorCode()!=org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver.ERROR.NO_ERROR) __errorAction();\n");
    txt.append("      return __stepTaken;\n");
    txt.append("    }\n\n");

    txt.append("    public double[] getState () { return __state; }\n\n");
    
    if (useDelays) {
      txt.append("    public void setStateHistory(org.opensourcephysics.numerics.ode_interpolation.StateHistory _history) { } // deliberately left empty\n\n");

      txt.append("    public double[] getDelays(double[] __aState) {\n");
      txt.append(extractVariablesFromStateCode("__aState",true,variableList));
      txt.append("      return new double[] {" + _editor.getDelays() + " };\n");
      txt.append("    }\n\n");

      txt.append("    public double getMaximumDelay() {\n");
      String maxDelayStr = _editor.getMaximumDelay();
      if (maxDelayStr==null || maxDelayStr.trim().length()<=0) {
        txt.append("      double[] _delaysArray = getDelays(__state);\n" );    
        txt.append("      double maximum = Double.POSITIVE_INFINITY;\n" );    
        txt.append("      for (int i=0; i<_delaysArray.length; i++) maximum = Math.max(maximum,Math.abs(_delaysArray[i]));\n" );    
        txt.append("      return maximum;\n" );    
      }
      else txt.append("      return "+maxDelayStr+";\n" );    
      txt.append("    }\n\n");
      
      txt.append("    public double[] getInitialConditionDiscontinuities() {\n");
      txt.append("      return new double[] {"+_editor.getDelayAddDiscont()+"};\n" ); 
      txt.append("    }\n\n");
      
      txt.append("    private double[] _userDefinedInitialCondition(double _time){\n"); // This is because the user returns a smaller array (not including time)
      txt.append("      // In case it uses the independent variable\n");
      txt.append("      double "+_indVar+" = _time;\n");
      txt.append(CodeEditor.splitCode(res.getString("EquationEditor.DelaysInitialConditionDiscontinuities"),
          _editor.getDelayInitCond(),_info + ":" + _editor.getName(),"      "));
      txt.append("    }\n\n");
       
      txt.append("    public void getInitialCondition(double _time, double _state[]) {\n");
      txt.append("      double[] _userDelayInitCond = _userDefinedInitialCondition(_time);\n");
      txt.append("      if (_userDelayInitCond!=null) {\n");
      txt.append("        System.arraycopy(_userDelayInitCond,0,_state,0,_state.length-1);\n");
      txt.append("        _state[_state.length-1] = _time;\n");
      txt.append("      }\n");
      txt.append("    }\n\n");
    } // end of useDelay
    
    txt.append("    public void getRate (double[] __aState, double[] __aRate) {\n");

    txt.append("      __aRate[__aRate.length-1] = 0.0; // In case the prelim code returns\n");
    txt.append("      int __index=-1; // so that it can be used in preliminary code\n");

    txt.append(extractVariablesFromStateCode("__aState",true,variableList));
//    txt.append("       __currentTime = "+_indVar+";\n");
    txt.append("      // Preliminary code: "+_editor.prelimEditor.getCommentField().getText()+"\n");
    txt.append(_editor.prelimEditor.generateCode(Editor.GENERATE_PLAINCODE,_info));
    
    txt.append(computeRateCode (false, _info, variableList));
    
    txt.append("    }//end of getRate\n\n");

    if (isMultirate) {
      txt.append("    public int[][] __recomputeDirectArray() {\n");
      txt.append(CodeEditor.splitCode(res.getString("EquationEditor.QSSDirectIncidenceMatrix"),
          incidenceMatrix,_info + ":" + _editor.getName(),"      "));
      txt.append("    }\n\n");
      txt.append("    public int [][] getInverseIncidenceMatrix () { return __inverseArray; }\n\n");
      txt.append("    public int [][] getDirectIncidenceMatrix ()  { return __directArray; }\n\n");

      txt.append("    public double getRate(double[] __aState, int __index) {\n");
      
      txt.append(extractVariablesFromStateCode("__aState",true,variableList));

      txt.append("      // Preliminary code: "+_editor.prelimEditor.getCommentField().getText()+"\n");
      txt.append(_editor.prelimEditor.generateCode(Editor.GENERATE_PLAINCODE,_info));

      txt.append("      // Compute a single rate\n");
      txt.append(computeRateCode (true, _info, variableList));
      txt.append("    } // End of single getRate(index)\n\n");
    }

    // The zeno effect listener
    if (_editor.zenoEditor!=null) {
      txt.append("    // Implementation of org.opensourcephysics.numerics.ZenoEffectListener\n");
      txt.append("    public boolean zenoEffectAction(Object __anEvent, double[] __aState) {\n");
      txt.append(extractVariablesFromStateCode("__aState",false,variableList));
      txt.append(_editor.zenoEditor.generateCode(Editor.GENERATE_PLAINCODE,_info));
      txt.append(updateStateFromVariablesCode("__aState",false, variableList));
      txt.append("      return "+_editor.zenoEditor.isSelected()+";\n");
      txt.append("    }\n\n");
    }

    // Now build the event classes
    if (_editor.eventEditor!=null) {
      Vector<Editor> eventList = _editor.eventEditor.getPages();
      for (int counter=1,n=eventList.size(); counter<=n; counter++) {
        EventEditor anEvent = (EventEditor) eventList.elementAt(counter-1);
        //        if (!anEvent.isActive()) continue;
        txt.append("    private class " + classname + "_Event" + counter + " implements org.opensourcephysics.numerics.ode_solvers.StateEvent {\n\n");
        txt.append("      public int getTypeOfEvent() { return "+anEvent.getEventType()+"; }\n\n");
        txt.append("      public int getRootFindingMethod() { return "+anEvent.getMethod()+"; }\n\n");
        txt.append("      public int getMaxIterations() { return "+anEvent.getIterations()+"; }\n\n");

        txt.append("      public String toString () { return \""+anEvent.getName()+"\"; }\n\n");
        String eventToleranceStr = anEvent.getTolerance().trim();
        if (eventToleranceStr.length()<=0) {
          if (absTol.length()>0) eventToleranceStr = absTol;
        }
        txt.append("      public double getTolerance () { return "+eventToleranceStr+"; }\n\n");

        txt.append("      public double evaluate (double[] __aState) { \n");

        txt.append(extractVariablesFromStateCode("__aState",true,variableList));
        txt.append(anEvent.generateCode(EventEditor.ZERO_CONDITION,_info));
        txt.append("      }\n\n");

        txt.append("      public boolean action () { \n");
        txt.append(extractVariablesFromStateCode("__state",false,variableList));
        txt.append("        boolean _returnValue = userDefinedAction();\n");
        txt.append(updateStateFromVariablesCode("__state",false, variableList));
        txt.append("        return _returnValue;\n");
        txt.append("      }\n\n");

        txt.append("      private boolean userDefinedAction() {\n");
        txt.append(         anEvent.generateCode(EventEditor.ACTION,_info));
        txt.append("        return "+anEvent.getStopAtEvent()+";\n");
        txt.append("      }\n\n");

        txt.append("    } // End of event class "+classname+"_Event" + counter +"\n\n");
      }
    }
    // Now build the discontinuity classes
    {
      Vector<Editor> discontinuityList = _editor.discontinuityEditor.getPages();
      for (int counter=1,n=discontinuityList.size(); counter<=n; counter++) {
        DiscontinuityEditor aDiscontinuity = (DiscontinuityEditor) discontinuityList.elementAt(counter-1);
        txt.append("    private class " + classname + "_Discontinuity" + counter + " implements org.opensourcephysics.numerics.ode_solvers.Discontinuity {\n\n");

        txt.append("      public String toString () { return \""+aDiscontinuity.getName()+"\"; }\n\n");
        String eventToleranceStr = aDiscontinuity.getTolerance().trim();
        if (eventToleranceStr.length()<=0) {
          if (absTol.length()>0) eventToleranceStr = absTol;
        }
        txt.append("      public double getTolerance () { return "+eventToleranceStr+"; }\n\n");

        txt.append("      public double evaluate (double[] __aState) { \n");

        txt.append(extractVariablesFromStateCode("__aState",true,variableList));
        txt.append(aDiscontinuity.generateCode(EventEditor.ZERO_CONDITION,_info));
        txt.append("      }\n\n");

        txt.append("      public boolean action () { \n");
        txt.append(extractVariablesFromStateCode("__state",false,variableList));
        txt.append("        boolean _returnValue = userDefinedAction();\n");
        txt.append(updateStateFromVariablesCode("__state",false, variableList));
        txt.append("        return _returnValue;\n");
        txt.append("      }\n\n");

        txt.append("      private boolean userDefinedAction() {\n");
        txt.append(         aDiscontinuity.generateCode(EventEditor.ACTION,_info));
        txt.append("        return "+aDiscontinuity.getStopAtDiscontinuity()+";\n");
        txt.append("      }\n\n");

        txt.append("    } // End of discontinuity class "+classname+"_Discontinuity" + counter +"\n\n");
      }
    }
    // Now build the schedule event classes //Gonzalo 070217
//    if (hasScheduleEvents) {
//      TabbedEditor _scheduleEditor = _ejs.getExperimentEditor().getscheduledEventEditor();
//      if (_scheduleEditor!=null) {
//        txt.append("  // ---- Scheduled Events for "+ classname+ "\n\n");
//        Vector<Editor> eventList = _scheduleEditor.getPages();
//
//        txt.append("    org.opensourcephysics.numerics.StateEvent _createScheduledEvent (int _index) {\n");
//        for (int counter=1,n=eventList.size(); counter<=n; counter++) {
//          ScheduledEventEditor anEvent = (ScheduledEventEditor) eventList.elementAt(counter-1);
//          if (!anEvent.isActive()) continue;
//          if (anEvent.getOdePage().equals(pageName)) txt.append("      if (_index=="+counter+") return new _ScheduledEvent_" + counter+"();\n");
//        }
//        txt.append("      return null;\n");
//        txt.append("    }\n\n");
//
//        for (int counter=1,n=eventList.size(); counter<=n; counter++) {
//          ScheduledEventEditor anEvent = (ScheduledEventEditor) eventList.elementAt(counter-1);
//          if (!anEvent.isActive()) continue;
//          if (anEvent.getOdePage().equals(pageName)){
//            txt.append("    class _ScheduledEvent_" + counter + " implements org.opensourcephysics.numerics.StateEvent {\n");
//            txt.append("\n      public double getTolerance () { return "+anEvent.getTolerance()+"; }\n\n");
//            // the evaluate method
//            txt.append("      public double evaluate (double[] __aState) { \n");
//            txt.append(extractVariablesFromStateCode("__aState",true,variableList));
//            txt.append(anEvent.generateCode(EventEditor.ZERO_CONDITION,_info));
//            txt.append("      }\n\n");
//
//            txt.append("      public boolean action () { \n");
//            txt.append(extractVariablesFromStateCode("__state",false,variableList));
//            txt.append("        boolean _returnValue = userDefinedAction();\n");
//            txt.append(updateStateFromVariablesCode("__state",false, variableList));
//            txt.append("        // Remove this scheduled event\n");
//            txt.append("        __solver.removeEvent(this);\n");
//            txt.append("        java.util.ArrayList ___list = (java.util.ArrayList) _scheduledEventsList.get(\""+pageName+"\");\n");
//            txt.append("        if (___list!=null) ___list.remove(this);\n");
//            txt.append("        return _returnValue;\n");
//            txt.append("      }\n\n");
//
//            txt.append("      private boolean userDefinedAction() {\n");
//            txt.append(         anEvent.generateCode(ScheduledEventEditor.ACTION,_info));
//            txt.append("        return "+anEvent.getStopAtEvent()+";\n");
//            txt.append("      }\n\n");
//
//
//            txt.append("    } // End of event class _ScheduledEvent_" + counter +"\n\n");
//          }
//        }
//        txt.append("  // ---  End of Scheduled Events for "+ classname+ "\n\n");
//      }
//    }
    txt.append("  } // End of class "+classname+"\n\n");

    return txt;
  }


} // End of the class

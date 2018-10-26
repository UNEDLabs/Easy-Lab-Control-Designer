/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) Feb 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.numerics.MathExpParser;
import org.opensourcephysics.numerics.SuryonoParser;
import java.util.*;

/**
 * A text field that is able to evaluate analytic expression
 */
public class ControlFunction extends ControlTextField {
  static protected final int FUNCTION_ADDED = 2;
  static private final int MY_VARIABLE  = ControlTextField.VARIABLE+FUNCTION_ADDED; // This is the function
  static private final int MY_VALUE  = ControlTextField.VALUE+FUNCTION_ADDED; // This is the function

  // Configuration variables
  protected boolean useJavaSyntax;

  // Implementation variables
  protected FunctionTextField functionTextField;
  protected String variables;
  private org.colos.ejs.library.Function theFunction; // For backwards compatibility
//  private boolean verbose = false;

  protected java.awt.Component createVisual () {
    // things similar to its parent
      textfield = functionTextField = new FunctionTextField();
      textfield.setText ("0");
    defaultValue = textfield.getText();
    textfield.addActionListener (new MyActionListener());
    textfield.addKeyListener    (new MyKeyListener());
    defaultValueSet = false;
    internalValue = new StringValue(defaultValue);
    decideColors (textfield.getBackground());

    // new things
    ((FunctionTextField) textfield).setControlFunction(this);
    useJavaSyntax=true;
    variables = "t";
    indVars = new String [] { variables };
    // For backwards compatibility
    theFunction = new org.colos.ejs.library.Function() {
      public double eval (double x) { return evaluate(x); }
    };

    return textfield;
  }

  protected int getVariableIndex () { return ControlFunction.MY_VARIABLE; }
  protected int getValueIndex () { return ControlFunction.MY_VALUE; }

  protected void setInternalValue (String _value) {
    setTheFunction(_value);
    super.setInternalValue(_value);
  }

//  public void setVerbose(boolean _verbose) { this.verbose = _verbose; }
  
// -------------------------------------
// Evaluate the function
// -------------------------------------

  // For backwards compatibility
    public Object getObject (String _name) {
      if ("function".equals(_name)) return theFunction;
      return null;
    }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("javaSyntax");
      infoList.add ("independent");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("javaSyntax"))  return "boolean PREVIOUS";
    if (_property.equals("variable"))    return "String PREVIOUS";
    if (_property.equals("independent")) return "String PREVIOUS";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : useJavaSyntax = _value.getBoolean(); setTheFunction(internalValue.value); break;
      case 1 : if (!_value.getString().equals(variables)) setTheVariables(_value.getString()); break;
      case MY_VARIABLE :
        if (!internalValue.value.equals(_value.getString())) setTheFunction(_value.getString());
//        super.setValue(ControlTextField.VARIABLE,_value);
        break;
      default: super.setValue(_index-FUNCTION_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : useJavaSyntax = true; setTheFunction(internalValue.value); break;
      case 1 : setTheVariables("t"); break;
      case MY_VARIABLE :
        setTheFunction ("0");
//        super.setDefaultValue(ControlTextField.VARIABLE);
        break;
      default: super.setDefaultValue(_index-FUNCTION_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "true";
      case 1 : return "t";
      case MY_VARIABLE : return "0";
      default : return super.getDefaultValueString(_index-FUNCTION_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 :
        return null;
      default: return super.getValue (_index-FUNCTION_ADDED);
    }
  }

// ------------------------------------------
// Private classes and methods
// ------------------------------------------

  protected ParserSuryono parser;
  protected SuryonoParser Bparser;
  protected String[] allVars, indVars;

  /**
   * Sets the independent variables of the function
   * @param _variables String
   * @return boolean true if correct, false if the expression is incorrect
   */
  boolean setTheVariables (String _variables) {
    StringTokenizer tkn = new StringTokenizer(_variables," ,");
    int n = tkn.countTokens();
    indVars = new String[n];
    for (int i=0; i<n; i++) indVars[i] = tkn.nextToken();
    this.variables = _variables;
    return setTheFunction (internalValue.value);
  }

  /**
   * Sets the function to evaluate
   * @param _function String
   * @return boolean true if correct, false if the expression is incorrect
   */
  public boolean setTheFunction (String _function) {
    if (_function==null) return false;
    // extract the list of variables
    Set<String> names = null;
    inputError=false;
    if (myGroup!=null) { // Obtain the list of accepted variables
      names = myGroup.getVariablesSet();
      allVars = new String[indVars.length+names.size()];
      for (int i=0; i<indVars.length; i++) allVars[i] = indVars[i];
      Iterator<String> it = names.iterator();
      int i = indVars.length;
      while (it.hasNext ()) allVars[i++] = it.next();
    }
    else {
      allVars = new String[indVars.length];
      for (int i=0; i<indVars.length; i++) allVars[i] = indVars[i];
    }
    if (useJavaSyntax) {
      if (names==null) allVars = ParserSuryono.getVariableList(_function);
      parser = new ParserSuryono(allVars.length);
      for (int i = 0, n = allVars.length; i < n; i++) parser.defineVariable(i,allVars[i]);
      parser.define(_function);
      parser.parse();
      inputError = parser.getErrorCode()!=ParserSuryono.NO_ERROR;
    }
    else {
      if (names!=null) {
        Bparser = new SuryonoParser(allVars.length);
        for (int i = 0, n = allVars.length; i < n; i++) Bparser.defineVariable(i+1,allVars[i]);
      }
      else {
        try {
          Bparser = new SuryonoParser(0);
          allVars = Bparser.parseUnknown(_function);
        }
        catch (Exception _exc) {
          allVars = ParserSuryono.getVariableList(_function);
          Bparser = new SuryonoParser(allVars.length);
          for (int i = 0, n = allVars.length; i < n; i++) Bparser.defineVariable(i+1, allVars[i]);
        }
      }
      Bparser.define(_function);
      Bparser.parse();
      inputError = Bparser.getErrorCode()!=MathExpParser.NO_ERROR;
    }
    functionTextField.setTheText (internalValue.value = _function);
    if (inputError) setColor (errorColor);
    else setColor (defaultColor);
    return !inputError;
  }

   public double evaluate (double x) {
     if (indVars.length!=1) throw new UnsupportedOperationException();
     if (useJavaSyntax) {
       parser.setVariable(0, x);
       if (myGroup!=null) { // get the necessary values from the ControlGroup
         for (int i = 1, n = allVars.length; i < n; i++) {
           parser.setVariable(i, myGroup.getDouble(allVars[i]));
         }
       }
       return parser.evaluate();
    }
     Bparser.setVariable(1, x);
     if (myGroup!=null) { // get the necessary values from the ControlGroup
       for (int i = 1, n = allVars.length; i < n; i++) {
         Bparser.setVariable(i+1, myGroup.getDouble(allVars[i]));
       }
     }
     return Bparser.evaluate();
   } // end of method evaluate

   public double evaluate (double[] x) {
     if (indVars.length!=x.length) throw new UnsupportedOperationException();
     int n = x.length;
     if (useJavaSyntax) {
       for (int i=0; i<n; i++) parser.setVariable(i, x[i]);
       if (myGroup!=null) { // get the necessary values from the ControlGroup
         for (int i = n, m = allVars.length; i < m; i++) parser.setVariable(i, myGroup.getDouble(allVars[i]));
       }
       return parser.evaluate();
    }
     for (int i=0; i<n; i++) Bparser.setVariable(i+1, x[i]);
     if (myGroup!=null) { // get the necessary values from the ControlGroup
       for (int i = n, m = allVars.length; i < m; i++) Bparser.setVariable(i+1, myGroup.getDouble(allVars[i]));
     }
     return Bparser.evaluate();
   } // end of method eval


} // End of class

/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.variables;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.library.control.PropertyEditor;
import org.colos.ejs.library.control.value.BooleanValue;
import org.colos.ejs.library.control.value.DoubleValue;
import org.colos.ejs.library.control.value.IntegerValue;
import org.colos.ejs.library.control.value.ObjectValue;
import org.colos.ejs.library.control.value.StringValue;
import org.colos.ejs.library.control.value.Value;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.ode_editor.EquationEditor;
import org.colos.ejs.osejs.edition.TabbedEditor;
import org.colos.ejss.html_view.ElementInfo;

/**
 * Created a TabbedEditor with tables of variables
 * @author Paco
 *
 */
public class VariablesEditor extends TabbedEditor implements org.colos.ejs.library.control.VariableEditor {
  private CodeInterpreter codeInterpreter; 

  public VariablesEditor (org.colos.ejs.osejs.Osejs _ejs) {
    super (_ejs, Editor.VARIABLE_EDITOR, "Model.Variables");
    if (ejs.supportsJava()) codeInterpreter = new CodeInterpreterForJava(_ejs);
    else codeInterpreter = new CodeInterpreterForJavascript(_ejs);
  }

  protected Editor createPage (String _type, String _name, String _code) {
    TableOfVariablesEditor page = new TableOfVariablesEditor (ejs);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }

  /**
   * Whether this variable name is already in use
   * @param _name
   * @return
   */
  public boolean nameExists(String _name) {
    for (java.util.Enumeration<Editor> e=pageList.elements(); e.hasMoreElements();) {
      TableOfVariablesEditor table = (TableOfVariablesEditor) e.nextElement();
      if (table.nameExists(_name)) return true;
    }
    if (ejs.getModelEditor().getElementsEditor().nameExists(_name)) return true;
    return false;
  }

  // ---------------------------------
  // Implementation of TableOfVariablesEditor
  // ---------------------------------

  public void updateControlValues (boolean showErrors) {
    updateControlValues (showErrors, false);
  }
  
  /**
   * Checks the syntax, evaluates variables, and passes model variables to the control
   */
  public void updateControlValues (boolean showErrors, boolean forced) {
    if (!forced && ejs.isReading()) return;
//    System.out.println ("Updating control values");
    
    EjsControl control = ejs.getViewEditor().getTree().getControl();
    if (control==null) return; //early call, no real variables in it
    codeInterpreter.reset(); 
    control.clearModelVariables(); // Cleans the control values defined by the model
    control.resetTraces();
    for (Editor page : getPages())  ((TableOfVariablesEditor) page).evaluateVariables ();
    ejs.getModelEditor().getElementsEditor().evaluateVariables(ejs);
    for (Editor page : ejs.getModelEditor().getEvolutionEditor().getPages()) {
      if (page instanceof EquationEditor) ((EquationEditor) page).checkSyntax();
    }
    //control.update();
    ejs.getModelEditor().checkSyntax();
    ejs.getViewEditor().getTree().updateProperties(showErrors);
    control.update();
    control.finalUpdate();
  }

  // Showing table columns
  
  /**
   * Sets the visibility of the column Domain 
   */
  public void showDomainColumn (boolean show) {
    for (Editor page : getPages()) ((TableOfVariablesEditor) page).showDomainColumn(show);
  }
  
  // --- Syntax checking and evaluation of variables

  /**
   * Updates a variable of the model with the given value. 
   * Triggered by interaction of the user with the control.
   */
  public void updateTableValues (PropertyEditor _editor, String _variable, String _value, Value _theValue) {
    if (ejs.getViewEditor().getTree().getControl()==null) return;
    for (Editor page : getPages()) if (((TableOfVariablesEditor) page).updateVariableInTable(_editor,_variable,_value,_theValue)) break;
  }

  /**
   * Returns the initial value for a variable, if it exists. Null otherwise
   */
  public String getInitialValue (String _name) {  
    for (Editor page : getPages()) {
      String value = ((TableOfVariablesEditor) page).getInitialValue(_name);
      if (value!=null) return value;
    }
    return null;
  }

  // ---------------------------------
  // All about parsing expressions
  // ---------------------------------

  /**
   * Counts the number of (non-scaped) quotes in the string
   * @param str
   * @return
   */
  static public int numberOfQuotes (String str) {
    int l = str.length();
    int counter = 0;
    for (int i=0; i<l; i++) {
      char c = str.charAt(i);
      if (c=='\"') ++counter;
      else if (c=='\\') i++;
    }
    return counter;
  }

  /**
   * Whether an expression is a constant for a given element property
   */
  static public boolean isAConstant (ControlElement _element, String _property, String _expression) { //AMAVP (See note in ControlElement)
    if (_expression==null || _expression.length()<=0) return true;
    if (_element!=null) {  // Check for a special constant
      if (_element.parseConstant (_element.propertyType(_property),_expression)!=null) {
        //System.out.println (_expression +" Is a constant for the element");
        return true;
      }
      if (_element.propertyIsTypeOf(_property,"String")) {
        int count = numberOfQuotes(_expression);
        if (count==0) return true;
        if (count==2 && _expression.startsWith("\"") && _expression.endsWith("\"")) return true;
        if (count==1 || count>2) return false;
        return false;
      }
    }
    if (Value.parseConstantOrArray(_expression,true)!=null) return true; // It's a constant
    return false;
  }

  /**
   * Whether an expression is a constant for a given element property
   */
  static public boolean isAConstant (ElementInfo _element, String _property, String _expression) { //AMAVP (See note in ControlElement)
    if (_expression==null || _expression.length()<=0) return true;
    if (_element!=null) {  // Check for a special constant
      if (_element.acceptsConstant(_property,_expression)) {
        //System.out.println (_expression +" Is a constant for the element");
        return true;
      }
      if (_element.propertyIsOfType(_property, "String")) {
        int count = numberOfQuotes(_expression);
        if (count==0) return true;
        if (count==2 && _expression.startsWith("\"") && _expression.endsWith("\"")) return true;
        if (count==1 || count>2) return false;
        return false;
      }
    }
    return isJSConstantOrArray(_expression);
  }

  /**
   * Evaluates an expression 
   */
  public Object evaluateExpression(String _expression) {
    return codeInterpreter.evaluateExpression(_expression); 
  }

  /**
   * Whether a given name corresponds to a variable of the given type.
   */
  public boolean isVariableDefined(String _name, String _type) {
    return codeInterpreter.isVariableDefined(_name, _type);
  }

  /**
   * Whether a given name corresponds to double array.
   */
  public boolean isDoubleArray(String _name) {
    return codeInterpreter.isDoubleArray(_name);
  }

  /**
   * Parses an expression of the given type and returns a Value with is value
   * @return a Value with the correct type and value, null if there was any error
   */
  public Value checkExpression(String _expression, String _type) {
    return codeInterpreter.checkExpression(_expression, _type);
  }

  /**
   * Parsers the given expression and assigns it to a variable of the given name, type, and dimension.
   * Used only by TableOfVariablesEditor
   * @return A Value with the correct type and value, null if there was any error
   */
  public Value checkVariableValue(String _name, String _expression, String _type, String _dimension) {
    return codeInterpreter.checkVariableValue(_name,_expression, _type,_dimension);
  }
  
  static private boolean isJSConstantOrArray (String _input) {
    String inputTrimmed = _input.trim();
    if (inputTrimmed.startsWith("{") && inputTrimmed.endsWith("}") ) {
      _input = inputTrimmed.substring(1,inputTrimmed.length()-1);  
      for (String token :  _input.split(",")) {
        String[] parts = token.split(":");
        if (parts.length!=2) return false;
        if (! (Value.parseConstant(parts[0].trim(),true) instanceof StringValue)) return false;
        if (Value.parseConstant(parts[1].trim(),true)==null) return false;
      }
      return true;
    }
    return parseJSConstantOrArray(_input,true)!=null;
  }
  
  static private Value parseJSConstantOrArray (String _input, boolean _silentMode) {
    String inputTrimmed = _input.trim();
    boolean isArray=false;
    boolean hasDoubles = false, hasInts = false, hasBooleans=false, hasStrings=false;
    if (inputTrimmed.startsWith("new ")) { // check for special case new Array (1,2,3)
      String rest = inputTrimmed.substring(4).trim();
      if (rest.startsWith("Array")) {
        inputTrimmed = rest.substring(5).trim();
        if (inputTrimmed.startsWith("(") && inputTrimmed.endsWith(")")) {
          inputTrimmed = "[" + inputTrimmed.substring(1, inputTrimmed.length()-1)+ "]"; 
        }
        else return Value.parseConstant(_input,_silentMode);
      }
    }
    if (inputTrimmed.startsWith("[") && inputTrimmed.endsWith("]") ) {
      _input = inputTrimmed.substring(1,inputTrimmed.length()-1);
      isArray = true;
    }
    if (inputTrimmed.startsWith("\"") && inputTrimmed.endsWith("\"")) return Value.parseConstant(_input,_silentMode);
    java.util.StringTokenizer tkn = new java.util.StringTokenizer(_input,",");
    int dim = tkn.countTokens();
    if (!isArray && dim<=1) return Value.parseConstant(_input,_silentMode);
    Value[] data = new Value[dim];
    for (int i=0; i<dim; i++) {
      String token = tkn.nextToken();
      //System.out.println ("Parsing <"+token+">");
      data[i] = Value.parseConstant(token,_silentMode);
      if (data[i]==null) return Value.parseConstant(_input,_silentMode);
      if      (data[i] instanceof DoubleValue)  hasDoubles  = true;
      else if (data[i] instanceof IntegerValue) hasInts     = true;
      else if (data[i] instanceof BooleanValue) hasBooleans = true;
      else if (data[i] instanceof StringValue) hasStrings = true;
    }
    if (hasDoubles) {
      double[] doubleArray = new double[dim];
      for (int i=0; i<dim; i++) doubleArray[i] = data[i].getDouble();
      return new ObjectValue(doubleArray);
    }
    else if (hasInts) {
      int[] intArray = new int[dim];
      for (int i=0; i<dim; i++) intArray[i] = data[i].getInteger();
      return new ObjectValue(intArray);
    }
    else if (hasBooleans) {
      boolean[] booleanArray = new boolean[dim];
      for (int i=0; i<dim; i++) booleanArray[i] = data[i].getBoolean();
      return new ObjectValue(booleanArray);
    }
    else if (hasStrings) {
      String[] stringArray = new String[dim];
      for (int i=0; i<dim; i++) stringArray[i] = data[i].getString();
      return new ObjectValue(stringArray);
    }
    return Value.parseConstant(_input,_silentMode);
  }

} // end of class


package org.colos.ejs.osejs.edition.variables;

import org.colos.ejs.library.control.value.Value;

public interface CodeInterpreter {

  public void reset();
  
  /**
   * Evaluates an expression 
   */  
  public Object evaluateExpression(String expression);
  
  /**
   * Whether a given name corresponds to a variable of the given type.
   */
  public boolean isVariableDefined(String name, String type);
  
  /**
   * Whether a given name corresponds to double array.
   */
  public boolean isDoubleArray(String name);

  /**
   * Parses an expression of the given type and returns a Value with is value
   * @return a Value with the correct type and value, null if there was any error
   */
  public Value checkExpression(String expression, String type);
  
  /**
   * Parsers the given expression and assigns it to a variable of the given name, type, and dimension.
   * Used only by TableOfVariablesEditor
   * @return A Value with the correct type and value, null if there was any error
   */
  public Value checkVariableValue(String name, String expression, String type, String dimension);

}

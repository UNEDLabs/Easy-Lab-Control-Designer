package org.colos.ejs.library.utils;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.control.ControlElement;

/**
 * A class of utilities for model elements
 * @author Francisco Esquembre
 * @version 1.0, August 2010
 *
 */
public class ModelElementsUtilities {

  /**
   * Removes a string from the beginning and end of the value (if it is there)
   */
  static public String removeEnclosingString(String _value, String _removeString) {
    int length = _removeString.length();
    if (_value.startsWith(_removeString)) _value = _value.substring(length);
    if (_value.endsWith(_removeString)) _value = _value.substring(0,_value.length()-length);
    return _value;
  }

  /**
   * Whether the value is linked to a model variable
   * @param _value
   * @return
   */
  static public boolean isLinkedToVariable(String _value) {
    return _value.startsWith("%");
  }
  
  /**
   * Gets the pure value, either the name of the model variable or the constant value
   * @param _value
   * @return
   */
  static public String getPureValue(String _value) {
    if (_value.startsWith("%")) return removeEnclosingString(_value,"%"); // It is linked to a model variable
    return removeEnclosingString(_value,"\"");
  }

  /**
   * Gets the value within quotes, either the name of the model variable or the constant value
   * @param _value
   * @return
   */
  static public String getQuotedValue(String _value) {
    if (_value.startsWith("%")) return "\"" + _value + "\""; // It is linked to a model variable
    return "\""+removeEnclosingString(_value,"\"")+"\"";
  }

  /**
   * Returns the value of a constant String (by removing its quotes), 
   * or that of a String variable of the model
   * @return
   */
  static public String getValue(Model _model, String _value) {
    if (_value==null) return null;
    if (_value.startsWith("%")) return _model._getVariable(removeEnclosingString(_value,"%"));
    return removeEnclosingString(_value,"\"");
  }

  /**
   * Returns the integer value of a constant integer (by removing its quotes), 
   * or that of a int variable of the model
   * @return
   */
  static public int getIntegerValue(Model _model, String _value) {
    try {
      if (_value.startsWith("%")) return Integer.parseInt(_model._getVariable(removeEnclosingString(_value,"%")));
      return Integer.parseInt(removeEnclosingString(_value,"\""));
    }
    catch (Exception exc) { return 0; }
  }
  
  /**
   * Returns the double value of a constant double (by removing its quotes), 
   * or that of a double variable of the model
   * @return
   */
  static public double getDoubleValue(Model _model, String _value) {
    try {
      if (_value.startsWith("%")) return Double.parseDouble(_model._getVariable(removeEnclosingString(_value,"%")));
      return Double.parseDouble(removeEnclosingString(_value,"\""));
    }
    catch (Exception exc) { return 0; }
  }
  
  /**
   * Returns the boolean value of a constant boolean (by removing its quotes), 
   * or that of a boolean variable of the model
   * @return
   */
  static public boolean getBooleanValue(Model _model, String _value) {
    try {
      if (_value.startsWith("%")) return Boolean.parseBoolean(_model._getVariable(removeEnclosingString(_value,"%")));
      return Boolean.parseBoolean(removeEnclosingString(_value,"\""));
    }
    catch (Exception exc) { return false; }
  }
  
  /**
   * Returns the view element object identified by its name
   * @return
   */
  static public Object getViewElement(Model _model, String _value) {
    String elementName = getValue(_model, _value) ;
    ControlElement ctrlEl = _model.getView().getElement(elementName);
    if (ctrlEl!=null) return ctrlEl.getObject();
    return null;
  }
  
}

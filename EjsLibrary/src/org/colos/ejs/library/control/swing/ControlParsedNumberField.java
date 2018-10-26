/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

/**
 * A textfield to display double values. When this value changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlParsedNumberField extends ControlNumberField {

// -------------------------------------
// Private methods and inner classes
// -------------------------------------

  @Override
  protected void fixTheFormat (java.text.DecimalFormat _format) {
    _format.setDecimalFormatSymbols(new java.text.DecimalFormatSymbols(new java.util.Locale("en")));
  }

  protected void acceptValue() {
    String valStr = textfield.getText();
    int index = valStr.lastIndexOf('=');
    if (index>=0) valStr = valStr.substring(index+1);
    double value;
    try { value= Double.parseDouble(valStr); } 
    catch (NumberFormatException ex){ value= org.opensourcephysics.numerics.Util.evalMath(valStr); }
    if (Double.isNaN(value)) setColor (errorColor);
    else {
      setColor (defaultColor);
      setInternalValue (value);
      if (isUnderEjs) setFieldListValueWithAlternative(VARIABLE, VALUE,internalValue);
    }
  }

} // End of class

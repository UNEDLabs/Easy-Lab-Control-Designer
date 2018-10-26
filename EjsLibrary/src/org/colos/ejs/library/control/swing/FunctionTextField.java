/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) March 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import javax.swing.JTextField;

/**
 * A text field that implements the Function interface
 */
public class FunctionTextField extends JTextField implements org.opensourcephysics.numerics.Function {
  ControlFunction wrapper = null;

  void setControlFunction (ControlFunction _element) {
    wrapper = _element;
  }

//  public void setVerbose(boolean verbose) { wrapper.setVerbose(verbose); }
 
  public double evaluate (double x) { return wrapper.evaluate(x); }

  public double evaluate (double x, double y) { return wrapper.evaluate (new double[] {x, y}); }

  public double evaluate (double x, double y, double z) { return wrapper.evaluate (new double[] {x, y, z}); }

  public double evaluate (double[] x) { return wrapper.evaluate(x); }

  /**
   * Sets the independent variables of the function
   * @param _variables String
   * @return boolean true if correct, false if the expression is incorrect
   */
  public boolean setVariables (String variables) {
    return wrapper.setTheVariables (variables);
  }

  /**
   * Sets the function to evaluate
   * @param _function String
   * @return boolean true if correct, false if the expression is incorrect
   */
  public boolean setFunction (String function) {
    return wrapper.setTheFunction (function);
  }
  
  public void setText (String txt) {
    if (wrapper!=null) wrapper.setTheFunction(txt);
    else super.setText(txt);
  }
  
  public void setTheText (String txt) {
    super.setText(txt);
  }
  
} // End of class

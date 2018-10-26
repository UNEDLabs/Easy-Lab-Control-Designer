/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import javax.swing.JProgressBar;

import org.colos.ejs.library.control.ConstantParser;

import java.text.DecimalFormat;

/**
 * A slider to display double values. 
 */
public class JProgressBarDouble extends JProgressBar {
  static public final int RESOLUTION=100000;

  private double scale, minimum, maximum=Double.NaN;
  private double doubleValue;
  private java.text.DecimalFormat format=null;

  public JProgressBarDouble(int _orientation) {
    super(_orientation);
    setBorderPainted(true);
    setStringPainted(false);
    setMinimum (0);
    setMaximum (RESOLUTION);
    setValue (0);
    
    minimum = 0.0;
    doubleValue = 0.0;
    setDoubleMaximum(1.0);
  }

  //------------------------------------------------
  // Set and Get the values of the properties
  //------------------------------------------------

  public void setDoubleValue (double value) {
    if (value==doubleValue) return;
    doubleValue = value;
    if (doubleValue<minimum) setValue(0);
    else if (doubleValue>maximum) setValue (RESOLUTION);
    else {
      int intValue = (int) ((doubleValue-minimum)*scale);
      setValue (intValue);
    }
    if (format!=null) setString(format.format(doubleValue));
  }

  public double getDoubleValue() {
    return doubleValue;
  }

  public double getDoubleMinimum () { return minimum; }

  public void setDoubleMinimum (double val) {
    if (val==minimum) return;
    minimum = val;
    scale = 1.0*RESOLUTION/(maximum-minimum);
    setDoubleValue (doubleValue);
  }

  public double getDoubleMaximum () { return maximum; }

  public void setDoubleMaximum (double val) {
    if (val==maximum) return;
    maximum = val;
    scale = 1.0*RESOLUTION/(maximum-minimum);
    setDoubleValue (doubleValue);
  }

  public void setFormat(DecimalFormat _format) {
    if (_format==null) {
      format = null;
      setStringPainted(false);
      return;
    }
    if (_format.equals(format)) return;
    format = _format;
    setString (format.format(doubleValue));
    setStringPainted(true);
    repaint();
  }

  public void setFormat (String _formatStr) {
    setFormat((DecimalFormat) ConstantParser.formatConstant(_formatStr).getObject());
  }
  
} // End of class

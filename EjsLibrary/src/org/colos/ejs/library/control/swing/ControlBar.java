/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ConstantParser;
import org.colos.ejs.library.control.value.*;
import javax.swing.SwingConstants;
import java.text.DecimalFormat;

/**
 * A bar that display double values. The value cannot be changed
 */
public class ControlBar extends ControlSwingElement {

  protected JProgressBarDouble bar;
  private String formatStr=null;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    bar = new JProgressBarDouble(SwingConstants.HORIZONTAL);
    return bar;
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("variable");
      infoList.add ("minimum");
      infoList.add ("maximum");
      infoList.add ("format");
      infoList.add ("orientation");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("variable"))       return "int|double";
    if (_property.equals("minimum"))        return "int|double";
    if (_property.equals("maximum"))        return "int|double";
    if (_property.equals("format"))         return "Format|Object|String  TRANSLATABLE";
    if (_property.equals("orientation"))    return "Orientation|int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : bar.setDoubleValue(_value.getDouble()); break;
      case 1 : bar.setDoubleMinimum(_value.getDouble()); break;
      case 2 : bar.setDoubleMaximum (_value.getDouble()); break;
      case 3 :
        if (_value.getObject() instanceof DecimalFormat) {
          formatStr=null;
          bar.setFormat((DecimalFormat) _value.getObject());
        }
        else {
          String newFormatStr = org.opensourcephysics.display.TeXParser.parseTeX(_value.getString());
          if (newFormatStr.equals(formatStr)) return;
          formatStr = newFormatStr;
          bar.setFormat((DecimalFormat) ConstantParser.formatConstant(formatStr).getObject());
        }
        break;
      case 4 :
        if (bar.getOrientation()!=_value.getInteger()) bar.setOrientation(_value.getInteger());
        break;
      default: super.setValue(_index-5,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : break; // Do nothing
      case 1 : bar.setDoubleMinimum (0.0); break;
      case 2 : bar.setDoubleMaximum (1.0); break;
      case 3 : formatStr=null; bar.setFormat((DecimalFormat)null); break;
      case 4 : bar.setOrientation(SwingConstants.HORIZONTAL); break;
      default: super.setDefaultValue(_index-5); break;
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 :
        return null;
      default: return super.getValue(_index-5);
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "0.0";
      case 2 : return "1.0";
      case 3 : return "<none>";
      case 4 : return "HORIZONTAL";
      default : return super.getDefaultValueString(_index-5);
    }
  }

}

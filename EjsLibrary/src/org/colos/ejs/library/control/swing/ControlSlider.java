/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import java.awt.*;
import java.text.DecimalFormat;

import javax.swing.SwingConstants;

import org.colos.ejs.library.control.ConstantParser;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

/**
 * A slider to display double values. When the value is changing it
 * invokes all VARIABLE_CHANGED actions. When the value is finally set,
 * it invokes all ACTION actions.
 */
public class ControlSlider extends ControlSwingElement {
  static protected final int SLIDER_ADDED = 12;
  static protected final int MY_FONT = FONT+SLIDER_ADDED;
  static protected final int MY_FOREGROUND = FOREGROUND+SLIDER_ADDED;

  static private final int VARIABLE  = 0;
  static private final int VALUE  = 1;

  protected JSliderDouble slider;
  private boolean defaultValueSet;
  private double defaultValue;
  private String formatStr;
  private DoubleValue internalValue;
  private boolean interactiveChange = true;
  // Paco introduced this variable because having the SnapToTick set to true produces
  // spurious changes of the value before the view is completely created. 090817
  private boolean mustSetClosest = false;


// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    slider = new JSliderDouble(new MyChangeListener());
    defaultValue  = 0.0;
    defaultValueSet = false;
    formatStr = null;
    internalValue = new DoubleValue(defaultValue);
    internalValue.value = slider.getDoubleValue();
//    slider.addChangeListener (new MyChangeListener());
    slider.addMouseListener  (new MyMouseListener());
    return slider;
  }

  public void reset() {
    if (mustSetClosest) { slider.setSnapToTicks(true); mustSetClosest = false; }
    if (defaultValueSet) {
      interactiveChange = false;
      slider.setDoubleValue (internalValue.value = defaultValue);
      interactiveChange = true;
      variableChanged (VARIABLE,internalValue);
    }
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("variable");
      infoList.add ("value");
      infoList.add ("minimum");
      infoList.add ("maximum");
      infoList.add ("pressaction");
      infoList.add ("dragaction");
      infoList.add ("action");

      infoList.add ("format");
      infoList.add ("ticks");
      infoList.add ("ticksFormat");
      infoList.add ("closest");
      infoList.add ("orientation");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("action"))      return "releaseAction";
    if (_property.equals("pressaction")) return "pressAction";
    if (_property.equals("dragaction"))  return "dragAction";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("variable"))       return "int|double";
    if (_property.equals("value"))          return "int|double CONSTANT DEPRECATED";
    if (_property.equals("minimum"))        return "int|double";
    if (_property.equals("maximum"))        return "int|double";
    if (_property.equals("pressaction"))    return "Action CONSTANT";
    if (_property.equals("dragaction"))     return "Action CONSTANT";
    if (_property.equals("action"))         return "Action CONSTANT";
    if (_property.equals("format"))         return "Format|Object|String TRANSLATABLE";
    if (_property.equals("ticks"))          return "int";
    if (_property.equals("ticksFormat"))    return "Format|Object|String TRANSLATABLE";
    if (_property.equals("closest"))        return "boolean";
    if (_property.equals("orientation"))    return "Orientation|int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case VARIABLE : 
        if (internalValue.value!=_value.getDouble()) {
          interactiveChange = false;
          slider.setDoubleValue (internalValue.value = _value.getDouble()); 
          interactiveChange = true;
        }
        break;
      case VALUE :
        defaultValueSet = true; 
        defaultValue = _value.getDouble();
        setActive (false); 
        reset (); 
        setActive(true);
        break;
      case 2 : slider.setDoubleMinimum (_value.getDouble()); break;
      case 3 : slider.setDoubleMaximum (_value.getDouble()); break;
      case 4 : // pressaction
        removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressaction"));
        addAction(ControlSwingElement.ACTION_PRESS,_value.getString());
        break;
      case 5 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        break;
      case 6 : // pressaction
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 7 : 
        if (_value.getObject() instanceof DecimalFormat) {
          formatStr = null;
          slider.setTitleFormat((DecimalFormat) _value.getObject());
        }
        else {
          String newFormatStr =org.opensourcephysics.display.TeXParser.parseTeX(_value.getString());
          if (newFormatStr.equals(formatStr)) return;
          slider.setTitleFormat(formatStr = newFormatStr);
//          slider.setTitleFormat((DecimalFormat) ConstantParser.formatConstant(formatStr).getObject());
        }
        break;
      case 8 : slider.setNumberOfTicks(_value.getInteger()); break;
      case 9 :
        if (_value.getObject() instanceof DecimalFormat) slider.setTicksFormat((DecimalFormat) _value.getObject());
        else slider.setTicksFormat((DecimalFormat) ConstantParser.formatConstant(org.opensourcephysics.display.TeXParser.parseTeX(_value.getString())).getObject());
        break;
      case 10 : 
        if (editorIsReading()) mustSetClosest = _value.getBoolean();
        else slider.setSnapToTicks (_value.getBoolean()); 
        break;
      case 11 : if (slider.getOrientation()!=_value.getInteger()) slider.setOrientation(_value.getInteger()); break;
      default: super.setValue(_index-SLIDER_ADDED,_value); break;
      case MY_FONT :
        if (_value.getObject() instanceof Font) slider.setTitleFont((Font) _value.getObject());
        super.setValue(FONT,_value) ;
        break;
      case MY_FOREGROUND :
        if (_value.getObject() instanceof Color) slider.setTitleForeground((Color) _value.getObject());
        super.setValue(FOREGROUND,_value) ;
        break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case VARIABLE : break; // Do nothing
      case VALUE : defaultValueSet = false; break;
      case 2 : slider.setDoubleMinimum (0.0); break;
      case 3 : slider.setDoubleMaximum (1.0); break;
      case 4 : removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressaction")); break;
      case 5 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragaction"));   break;
      case 6 : removeAction (ControlElement.ACTION,getProperty("action"));                 break;
      case 7 : slider.setTitleFormat((DecimalFormat)null); formatStr=null; break;
      case 8 : slider.setNumberOfTicks(0); break;
      case 9 : slider.setPaintLabels(false); break;
      case 10 : slider.setSnapToTicks (false); break;
      case 11 : slider.setOrientation(SwingConstants.HORIZONTAL); break;
      default: super.setDefaultValue(_index-SLIDER_ADDED); break;
      case MY_FONT : slider.setTitleFont(myDefaultFont); super.setDefaultValue(FONT) ; break;
      case MY_FOREGROUND : slider.setTitleForeground(myDefaultFrgd); super.setDefaultValue(FOREGROUND) ; break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : case 1 : return "<none>";
      case 2 : return "0.0";
      case 3 : return "1.0";
      case 4 : case 5 : case 6 : return "<no_action>";
      case 7 : return "<none>";
      case 8 : return "0";
      case 9 : return "<none>";
      case 10 : return "false";
      case 11 : return "HORIZONTAL";
      default : return super.getDefaultValueString(_index-SLIDER_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case VARIABLE : return internalValue;
      case VALUE :  case 2 :  case 3 :  case 4 : case 5 :
      case 6 :  case 7 :  case 8 :  case 9 : case 10 :
      case 11 :
        return null;
      default: return super.getValue(_index-SLIDER_ADDED);
    }
  }


// -------------------------------------
// Inner classes
// -------------------------------------

  private class MyChangeListener implements javax.swing.event.ChangeListener {
    
    public void stateChanged(javax.swing.event.ChangeEvent e) {
//      if (editorIsReading ()) return; This is too much. The mustSetClosest variable fixed it
      if (slider.isInteractiveChange() && interactiveChange) {
        internalValue.value = slider.getDoubleValue();
//        System.out.println ("From control slider is at : "+slider.getValue());
//        System.out.println ("Value is now : "+internalValue.value);
        
        variableChanged (VARIABLE,internalValue);
        if (isUnderEjs) setFieldListValueWithAlternative(VARIABLE, VALUE, internalValue);
      }
    }
    
  }

  private class MyMouseListener extends java.awt.event.MouseAdapter {
    
    public void mousePressed (java.awt.event.MouseEvent evt) {
      if (slider.isEnabled()) invokeActions (ControlSwingElement.ACTION_PRESS);
    }

    public void mouseReleased (java.awt.event.MouseEvent evt) {
      if (slider.isEnabled()) invokeActions (ControlElement.ACTION);
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          interactiveChange = false;
          slider.setDoubleValue (internalValue.value);
          interactiveChange = true;
          slider.repaint();
        }
      });
    }
    
  }

} // End of class

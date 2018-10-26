/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ConstantParser;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import java.awt.Color;
import javax.swing.JTextField;
import java.text.DecimalFormat;

/**
 * A textfield to display double values. When this value changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlNumberField extends ControlSwingElement {
  static private final int NUMBER_FIELD_ADDED = 6;
  static protected final int VARIABLE  = 0;
  static protected final int VALUE  = 1;
  static private final int FIELD_BACKGROUND  = ControlSwingElement.BACKGROUND+NUMBER_FIELD_ADDED;
  static private final int FIELD_FOREGROUND  = ControlSwingElement.FOREGROUND+NUMBER_FIELD_ADDED;

  static protected final java.text.DecimalFormat defaultFormat = new java.text.DecimalFormat ("0.000;0.000");

  protected JTextField textfield;
  protected DoubleValue internalValue;
  protected double defaultValue;
  protected boolean defaultValueSet;
  protected int defaultColumns;
  protected java.text.DecimalFormat format;
  protected String formatStr=null;
  protected boolean foregroundSet;
  protected Color defaultColor, editingColor, errorColor;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  // To be overriden by ParsedField
  protected void fixTheFormat (java.text.DecimalFormat _format) {
  }

  protected java.awt.Component createVisual () {
    textfield = new JTextField();
    fixTheFormat(defaultFormat);
    format = defaultFormat;
    defaultValue  = 0.0;
    defaultValueSet = false;
    defaultColumns = textfield.getColumns();
    internalValue = new DoubleValue(defaultValue);
    /*
    if (javax.swing.SwingUtilities.isEventDispatchThread()) textfield.setText (format.format (internalValue.value));
    else try {
      javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
        public synchronized void run() { 
          textfield.setText (format.format (internalValue.value)); 
        }
      });
    } catch (Exception exc) {}
    */
    textfield.setText (format.format (internalValue.value)); 
    textfield.addActionListener (new MyActionListener());
    textfield.addKeyListener    (new MyKeyListener());
    textfield.setBackground(Color.WHITE);
    textfield.setForeground(Color.BLACK);
    foregroundSet = false;
    decideColors (textfield.getBackground());
    return textfield;
  }

  public void reset() {
    if (defaultValueSet) {
      setTheValue (defaultValue);
      setInternalValue (defaultValue);
    }
  }

  private void setTheValue (double _value) {
    if (_value!=internalValue.value) {
      internalValue.value = _value;
      if (javax.swing.SwingUtilities.isEventDispatchThread()) {
        textfield.setText (format.format (_value));
        getVisual().setBackground (defaultColor);
      }
      else { // Same thing, but delayed
        final String str = format.format (_value);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public synchronized void run() {
            textfield.setText (str);
            getVisual().setBackground (defaultColor);
          }
        });
      }
    }
  }

  protected void setInternalValue (double _value) {
    internalValue.value = _value;
    variableChanged (VARIABLE,internalValue);
    //invokeActions ();
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
      infoList.add ("editable");
      infoList.add ("format");
      infoList.add ("action");
      infoList.add ("columns");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("variable"))       return "int|double";
    if (_property.equals("value"))          return "int|double CONSTANT DEPRECATED";
    if (_property.equals("editable"))       return "boolean";
    if (_property.equals("format"))         return "Format|Object|String TRANSLATABLE";
    if (_property.equals("action"))         return "Action CONSTANT";
    if (_property.equals("columns"))        return "int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    // System.out.println (getComponent().getName()+": NumberField setting value "+_index + " to "+_value.toString());
    switch (_index) {
      case VARIABLE : setTheValue (_value.getDouble()); break;
      case 1 :
        defaultValueSet = true; defaultValue = _value.getDouble();
        setActive (false); reset (); setActive(true);
        break;
      case 2 : 
        if (_value.getBoolean()) {
          textfield.setEditable(true);
          if (!foregroundSet) textfield.setForeground(Color.BLACK);
        }
        else {
          textfield.setEditable(false);
          if (!foregroundSet) textfield.setForeground(Color.GRAY);
        }
//        setColor(defaultColor);
        break;
      case 3 :
        {
          DecimalFormat newFormat=null;
          if (_value.getObject() instanceof DecimalFormat) {
            newFormat = (DecimalFormat) _value.getObject();
            fixTheFormat(newFormat);
//            newFormat.setDecimalFormatSymbols(new java.text.DecimalFormatSymbols(new java.util.Locale("en")));
            formatStr = null;
          }
          else {
            String newFormatStr = org.opensourcephysics.display.TeXParser.parseTeX(_value.getString());
            if (newFormatStr.equals(formatStr)) return;
            formatStr = newFormatStr;
            newFormat = (DecimalFormat) ConstantParser.formatConstant(formatStr).getObject();
            fixTheFormat(newFormat);
//            newFormat.setDecimalFormatSymbols(new java.text.DecimalFormatSymbols(new java.util.Locale("en")));
          }
          if (newFormat.equals(format)) return;
          format = newFormat;
          /*  Is this needed?
          setActive (false);
          try { setInternalValue (format.parse(textfield.getText()).doubleValue()); }
          catch (Exception exc) {}
          setActive (true);
          */
          
          if (javax.swing.SwingUtilities.isEventDispatchThread()) textfield.setText (format.format (internalValue.value)); 
          else {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
              public synchronized void run() { textfield.setText (format.format (internalValue.value)); }
            });
          }
        }
        break;
      case 4 : // action
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("action"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        break;
      case 5 : 
        if (_value.getInteger()!=textfield.getColumns()) {
          textfield.setColumns(_value.getInteger());
          if (textfield.getParent()!=null) textfield.getParent().validate();
        }
        break;
      case FIELD_BACKGROUND :
        super.setValue (ControlSwingElement.BACKGROUND,_value);
        decideColors (getVisual().getBackground());
        setColor(defaultColor);
        break;
      case FIELD_FOREGROUND :
        super.setValue (ControlSwingElement.FOREGROUND,_value);
        foregroundSet = true;
        break;
      default: super.setValue(_index-NUMBER_FIELD_ADDED,_value); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case VARIABLE : 
      case 1 : return "<none>"; 
      case 2 : return "true";
      case 3 : return "0.000;0.000";
      case 4 : return "<no_action>";
      case 5 : return Integer.toString(defaultColumns); 
      default : return super.getDefaultValueString(_index-NUMBER_FIELD_ADDED);
      case FIELD_BACKGROUND : return "WHITE";
      case FIELD_FOREGROUND : return "BLACK/GRAY";
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case VARIABLE : break;  // Do nothing
      case 1 : defaultValueSet = false; break;
      case 2 : 
        textfield.setEditable(true); 
        if (!foregroundSet) textfield.setForeground(Color.BLACK);
//        setColor(defaultColor);
        break;
      case 3 :
        format = defaultFormat;
        formatStr=null;
        Runnable refreshScreen = new Runnable() {
          public synchronized void run() { textfield.setText (format.format (internalValue.value)); }
        };
        if (javax.swing.SwingUtilities.isEventDispatchThread()) refreshScreen.run();
        else javax.swing.SwingUtilities.invokeLater(refreshScreen);
        break;
      case 4 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("action")); break;
      case 5 : 
        textfield.setColumns(defaultColumns); 
        if (textfield.getParent()!=null) textfield.getParent().validate();
        break;
      case FIELD_BACKGROUND :
        super.setDefaultValue (ControlSwingElement.BACKGROUND);
        decideColors (getVisual().getBackground());
        setColor(defaultColor);
        break;
      case FIELD_FOREGROUND :
        super.setDefaultValue (ControlSwingElement.FOREGROUND);
        foregroundSet = false;
        break;
      default: super.setDefaultValue(_index-NUMBER_FIELD_ADDED); break;
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case VARIABLE : return internalValue;
      case 1 : case 2 : case 3 : case 4 :
        return null;
      default: return super.getValue(_index-NUMBER_FIELD_ADDED);
    }
  }
  

// -------------------------------------
// Private methods and inner classes
// -------------------------------------

  protected void setColor (Color aColor) {
    textfield.setBackground (aColor);
//    if (textfield.isEditable()) textfield.setBackground (aColor);
//    else textfield.setBackground(noneditableColor);
  }

  protected void decideColors (Color aColor) {
    if (aColor==null) return;
    defaultColor = aColor;
    if (defaultColor.equals(Color.yellow)) editingColor = Color.orange;
    else editingColor = Color.yellow;
    if (defaultColor.equals(Color.red)) errorColor = Color.magenta;
    else errorColor = Color.red;
//    if (defaultColor.equals(Color.LIGHT_GRAY)) noneditableColor = Color.GRAY;
//    else noneditableColor = Color.LIGHT_GRAY;
  }

  protected void acceptValue() {
    setColor (defaultColor);
    try {
      setInternalValue (format.parse(textfield.getText()).doubleValue());
      if (isUnderEjs) setFieldListValueWithAlternative(VARIABLE, VALUE,internalValue);
    }
    catch (Exception exc) {
      setColor (errorColor);
      // exc.printStackTrace(System.err);
    }
  }

  protected class MyActionListener implements java.awt.event.ActionListener {
    public void actionPerformed (java.awt.event.ActionEvent _e) {
      if (textfield.isEditable()) acceptValue();
    }
  }

  protected class MyKeyListener implements java.awt.event.KeyListener {
    public void keyPressed  (java.awt.event.KeyEvent _e) { processKeyEvent (_e,0); }
    public void keyReleased (java.awt.event.KeyEvent _e) { processKeyEvent (_e,1); }
    public void keyTyped    (java.awt.event.KeyEvent _e) { processKeyEvent (_e,2); }
    private void processKeyEvent (java.awt.event.KeyEvent _e, int _n) {
      if (!textfield.isEditable()) return;
      if (_e.getKeyChar()!='\n') setColor (editingColor);
      if (_e.getKeyCode()==27)   setValue (VARIABLE,internalValue);
    }
  }

} // End of class

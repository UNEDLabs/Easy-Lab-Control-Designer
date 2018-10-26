/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.value.*;

import javax.swing.*;

/**
 * A configurable checkbox. It will trigger an action when the
 * state changes. It has a boolean internal value, which is
 * returned as a Boolean object.
 */
public class ControlRadioButtonMenuItem extends ControlRadioButton implements RadioButtonInterface {
  private static final int ADDED=1;

  //Static initialization
  {
	  JPopupMenu.setDefaultLightWeightPopupEnabled( false );
	  ToolTipManager.sharedInstance().setLightWeightPopupEnabled( false );
  }
// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    radioButton = new JRadioButtonMenuItem ();
    internalValue = new BooleanValue (radioButton.isSelected());
    defaultStateSet = false;
    radioButton.addActionListener (
      new java.awt.event.ActionListener() {
        public void actionPerformed (java.awt.event.ActionEvent _e) {
          if (cantUnselectItself) {
            if (internalValue.value && !radioButton.isSelected()) {
              radioButton.setSelected(true);
              return;
            }
          }
          setInternalValue (radioButton.isSelected());
          if (isUnderEjs) setFieldListValueWithAlternative(VARIABLE, SELECTED,internalValue);
        }
      }
    );
    return radioButton;
  }

  public int getVariableIndex() { return VARIABLE+ADDED; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

      static private java.util.List<String> infoList=null;

      public java.util.List<String> getPropertyList() {
        if (infoList==null) {
          infoList = new java.util.ArrayList<String> ();
          infoList.add ("accelerator");
          infoList.addAll(super.getPropertyList());
        }
        return infoList;
      }

      public String getPropertyInfo(String _property) {
        if (_property.equals("accelerator")) return "String TRANSLATABLE";
        return super.getPropertyInfo(_property);
      }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

      public void setValue (int _index, Value _value) {
        switch (_index) {
          case 0 : ((JRadioButtonMenuItem)radioButton).setAccelerator(KeyStroke.getKeyStroke(_value.getString().charAt(0),java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())); break;
          default: super.setValue(_index-ADDED,_value); break;
        }
      }

      public void setDefaultValue (int _index) {
        switch (_index) {
          case 0 : ((JRadioButtonMenuItem)radioButton).setAccelerator(null); break;
          default: super.setDefaultValue(_index-ADDED); break;
        }
      }
      
      public String getDefaultValueString (int _index) {
        switch (_index) {
          case 0 : return "<none>";
          default : return super.getDefaultValueString(_index-ADDED);
        }
      }
      

      public Value getValue (int _index) {
        switch (_index) {
          case 0 :
            return null;
          default: return super.getValue(_index-ADDED);
        }
      }

} // End of class

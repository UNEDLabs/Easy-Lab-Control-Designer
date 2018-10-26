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
 * state changes. It has a boolean internal value.
 */
public class ControlCheckBoxMenuItem extends ControlCheckBox {
  
  //Static initialization
  {
	  JPopupMenu.setDefaultLightWeightPopupEnabled( false );
	  ToolTipManager.sharedInstance().setLightWeightPopupEnabled( false );
  }
// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    checkbox = new JCheckBoxMenuItem ();
    defaultStateSet = false;
    internalValue = new BooleanValue(defaultState=checkbox.isSelected());
    checkbox.addActionListener (
      new java.awt.event.ActionListener() {
        public void actionPerformed (java.awt.event.ActionEvent _e) {
          setInternalValue (checkbox.isSelected());
          if (isUnderEjs) setFieldListValueWithAlternative(getVariableIndex(), getValueIndex(),internalValue);
        }
      }
    );
    return checkbox;
  }

  protected int getVariableIndex () { return ControlCheckBox.VARIABLE+1; }
  protected int getValueIndex () { return ControlCheckBox.SELECTED+1; }

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
        case 0 : ((JCheckBoxMenuItem)checkbox).setAccelerator(KeyStroke.getKeyStroke(_value.getString().charAt(0),java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())); break;
        default: super.setValue(_index-1,_value); break;
      }
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : ((JCheckBoxMenuItem)checkbox).setAccelerator(null); break;
        default: super.setDefaultValue(_index-1); break;
      }
    }

    public Value getValue (int _index) {
      switch (_index) {
        case 0 :
          return null;
        default: return super.getValue(_index-1);
      }
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "<none>";
        default : return super.getDefaultValueString(_index-1);
      }
    }

} // End of class

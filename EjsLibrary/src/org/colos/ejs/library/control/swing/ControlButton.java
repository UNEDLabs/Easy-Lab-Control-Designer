/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import javax.swing.JButton;

/**
 * A configurable control button. It will trigger an action when clicked.
 * It has no internal value.
 */
public class ControlButton extends ControlSwingElement {
  protected JButton button;
  private String imageFile = null, labelString="";

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    button = new JButton ();
    button.addActionListener (
      new java.awt.event.ActionListener() {
        public void actionPerformed (java.awt.event.ActionEvent _e) { invokeActions (); }
      }
    );
    return button;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("text");
      infoList.add ("image");
      infoList.add ("alignment");
      infoList.add ("action");
      infoList.add ("mnemonic");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("text"))      return "String NotTrimmed TRANSLATABLE";
    if (_property.equals("image"))     return "File|String TRANSLATABLE";
    if (_property.equals("alignment")) return "Alignment|int";
    if (_property.equals("action"))    return "Action CONSTANT";
    if (_property.equals("mnemonic"))    return "String  TRANSLATABLE";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
//    if (_value!=null) System.err.println (getComponent().getName()+": Setting property #"+_index+" to "+_value.toString());
    switch (_index) {
      case 0 :
        if (!labelString.equals(_value.getString())) {
          labelString = _value.getString();
          if (labelString==null) labelString = "";
          button.setText(org.opensourcephysics.display.TeXParser.parseTeX(_value.getString()));
        }
      break;  // text
      case 1 : // image
        if (_value.getString().equals(imageFile)) return; // no need to do it again
        button.setIcon (getIcon(imageFile = _value.getString()));
        break;
      case 2 : button.setHorizontalAlignment(_value.getInteger()); break; // alignment
      case 3 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 4 : button.setMnemonic(_value.getString().charAt(0)); break;
      default: super.setValue(_index-5,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : button.setText(labelString=""); break;
      case 1 : imageFile = null; button.setIcon(null); break;
      case 2 : button.setHorizontalAlignment(javax.swing.SwingConstants.CENTER); break;
      case 3 : removeAction (ControlElement.ACTION,getProperty("action"));       break;
      case 4 : button.setMnemonic(-1); break;
      default: super.setDefaultValue(_index-5); break;
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 :
        return null;
      default: return super.getValue(_index-5);
    }
  }
  
  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "<none>";
      case 2 : return "CENTER";
      case 3 : return "<no_action>";
      case 4 : return "<none>";
      default : return super.getDefaultValueString(_index-5);
    }
  }

} // End of class

/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import javax.swing.*;

/**
 * A configurable control button. It will trigger an action when clicked.
 * It has no internal value.
 */
public class ControlMenuItem extends ControlSwingElement {
  protected static final int MENUITEM_ADDED=6;

  protected JMenuItem menuItem;
  private String imageFile = null, labelString="";
  
  //Static initialization
  {
	  JPopupMenu.setDefaultLightWeightPopupEnabled( false );
	  ToolTipManager.sharedInstance().setLightWeightPopupEnabled( false );
  }

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    menuItem = new JMenuItem ();
    menuItem.addActionListener (
      new java.awt.event.ActionListener() {
        public void actionPerformed (java.awt.event.ActionEvent _e) { invokeActions (); }
      }
    );
    return menuItem;
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
      infoList.add ("accelerator");
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
    if (_property.equals("accelerator")) return "String TRANSLATABLE";
//    if (_property.equals("mnemonic"))    return "int|String|Mnemonic";
    if (_property.equals("mnemonic"))    return "String TRANSLATABLE";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :
        if (!labelString.equals(_value.getString())) {
          labelString = _value.getString();
          if (labelString==null) labelString = "";
          menuItem.setText(org.opensourcephysics.display.TeXParser.parseTeX(_value.getString()));
        }
      break;  // text
      case 1 : // image
        if (_value.getString().equals(imageFile)) return; // no need to do it again
        menuItem.setIcon (getIcon(imageFile = _value.getString()));
        break;
      case 2 : menuItem.setHorizontalAlignment(_value.getInteger()); break; // alignment
      case 3 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 4 : menuItem.setAccelerator(KeyStroke.getKeyStroke(_value.getString().charAt(0),java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())); break;
      case 5 : menuItem.setMnemonic(_value.getString().charAt(0)); break;
      default: super.setValue(_index-MENUITEM_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : menuItem.setText(labelString=""); break;
      case 1 : imageFile = null; menuItem.setIcon(null); break;
      case 2 : menuItem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER); break;
      case 3 : removeAction (ControlElement.ACTION,getProperty("action"));       break;
      case 4 : menuItem.setAccelerator(null); break;
      case 5 : menuItem.setMnemonic(-1); break;
      default: super.setDefaultValue(_index-MENUITEM_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "<none>";
      case 2 : return "CENTER";
      case 3 : return "<no_action>";
      case 4 : case 5 : return "<none>";
      default : return super.getDefaultValueString(_index-MENUITEM_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 :
        return null;
      default: return super.getValue(_index-MENUITEM_ADDED);
    }
  }

} // End of class

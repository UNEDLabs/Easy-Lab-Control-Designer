/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import javax.swing.JLabel;

/**
 * A configurable Label. It has no internal value, nor can trigger
 * any action.
 */
public class ControlLabel extends ControlSwingElement {
  static private final int LABEL_ADDED = 6;
  protected JLabel label;
  private String imageFile = null, labelString="";

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    label = new JLabel ();
    label.setOpaque(true);
    label.addMouseListener (new java.awt.event.MouseAdapter() {
      public void mousePressed (java.awt.event.MouseEvent e) { invokeActions (ControlSwingElement.ACTION_ON); }
      public void mouseClicked (java.awt.event.MouseEvent e) { invokeActions (); }
      public void mouseReleased (java.awt.event.MouseEvent e) { invokeActions (ControlSwingElement.ACTION_OFF); }
    });
    return label;
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
      infoList.add ("pressAction");
      infoList.add ("releaseAction");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("text"))      return "String NotTrimmed TRANSLATABLE";
    if (_property.equals("image"))     return "File|String TRANSLATABLE";
    if (_property.equals("alignment")) return "Alignment|int";
    if (_property.equals("action"))    return "Action CONSTANT";
    if (_property.equals("pressAction"))    return "Action CONSTANT";
    if (_property.equals("releaseAction"))  return "Action CONSTANT";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :
//        System.out.println ("Proposed Label is <"+_value.getString()+">");
        if (!labelString.equals(_value.getString())) {
          labelString = _value.getString();
          if (labelString==null) labelString = "";
//          if (text.startsWith("\"") && text.endsWith("\"")) text = text.substring(1,text.length()-1);
          label.setText(org.opensourcephysics.display.TeXParser.parseTeX(labelString));
        }
        break;  // text
      case 1 : // image
        if (_value.getString().equals(imageFile)) return; // no need to do it again
        label.setIcon (getIcon(imageFile = _value.getString()));
        break;
      case 2 : label.setHorizontalAlignment(_value.getInteger()); break; // alignment
      case 3 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 4 : // pressAction
        removeAction (ControlSwingElement.ACTION_ON,getProperty("pressAction"));
        addAction(ControlSwingElement.ACTION_ON,_value.getString());
        break;
      case 5 : // releaseAction
        removeAction (ControlSwingElement.ACTION_OFF,getProperty("releaseAction"));
        addAction(ControlSwingElement.ACTION_OFF,_value.getString());
        break;
      default: super.setValue(_index-LABEL_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : label.setText(labelString = ""); break;
      case 1 : label.setIcon(null); imageFile = null; break;
      case 2 : label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER ); break;
      case 3 : removeAction (ControlElement.ACTION,getProperty("action"));       break;
      case 4 : removeAction (ControlSwingElement.ACTION_ON,getProperty("pressAction"));    break;
      case 5 : removeAction (ControlSwingElement.ACTION_OFF,getProperty("releaseAction")); break;
      default: super.setDefaultValue(_index-LABEL_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "<none>";
      case 2 : return "CENTER";
      case 3 : case 4 : case 5 : return "<no_action>";
      default : return super.getDefaultValueString(_index-LABEL_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 :
        return null;
      default: return super.getValue(_index-LABEL_ADDED);
    }
  }

} // End of class

/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import javax.swing.JRadioButton;
import javax.swing.AbstractButton;

/**
 * A configurable checkbox. It will trigger an action when the
 * state changes. It has a boolean internal value, which is
 * returned as a Boolean object.
 */
public class ControlRadioButton extends ControlSwingElement implements RadioButtonInterface {
  static final int VARIABLE  = 4;
  static final int SELECTED  = 5;

  protected AbstractButton radioButton;
  protected ControlRadioButton mySelf = this;

  protected BooleanValue internalValue;
  protected boolean defaultState, defaultStateSet, cantUnselectItself;
  protected String imageFile = null, selectedimageFile = null, labelString = "";
  private ControlContainer myControlParent=null;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    radioButton = new JRadioButton ();
    internalValue = new BooleanValue (radioButton.isSelected());
    defaultStateSet = false;
    cantUnselectItself = false;
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

  public void reset() {
    if (defaultStateSet) {
      radioButton.setSelected(defaultState);
      setInternalValue (defaultState);
    }
  }

  protected void setInternalValue (boolean _state) {
    internalValue.value = _state;
    if (myControlParent!=null) myControlParent.informRadioGroup(mySelf,_state);
    variableChanged (VARIABLE,internalValue);
    invokeActions ();
    if (internalValue.value) invokeActions(ControlSwingElement.ACTION_ON);
    else invokeActions(ControlSwingElement.ACTION_OFF);
  }

  public void setControlParent (ControlContainer _aParent) { myControlParent = _aParent; }

  public void reportChanges () { variableChangedDoNotUpdate (VARIABLE,internalValue); }

  public int getVariableIndex() { return VARIABLE; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("text");
      infoList.add ("image");
      infoList.add ("selectedimage");
      infoList.add ("alignment");
      infoList.add ("variable");
      infoList.add ("selected");
      infoList.add ("action");
      infoList.add ("actionon");
      infoList.add ("actionoff");
      infoList.add ("mnemonic");
      infoList.add ("noUnselect");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("text"))           return "String NotTrimmed TRANSLATABLE";
    if (_property.equals("image"))          return "File|String TRANSLATABLE";
    if (_property.equals("selectedimage"))  return "File|String TRANSLATABLE";
    if (_property.equals("alignment"))      return "Alignment|int";
    if (_property.equals("variable"))       return "boolean";
    if (_property.equals("selected"))       return "boolean CONSTANT POSTPROCESS";
    if (_property.equals("action"))         return "Action CONSTANT";
    if (_property.equals("actionon"))       return "Action CONSTANT";
    if (_property.equals("actionoff"))      return "Action CONSTANT";
    if (_property.equals("mnemonic"))       return "String TRANSLATABLE";
    if (_property.equals("noUnselect"))     return "boolean";
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
          radioButton.setText(org.opensourcephysics.display.TeXParser.parseTeX(_value.getString()));
        }
      break;  // text
      case 1 : // image
        if (_value.getString().equals(imageFile)) return; // no need to do it again
        radioButton.setIcon (getIcon(imageFile=_value.getString()));
        break;
      case 2 : // selectedImage
        if (_value.getString().equals(selectedimageFile)) return; // no need to do it again
        radioButton.setSelectedIcon (getIcon(selectedimageFile=_value.getString()));
        break;
      case 3 : radioButton.setHorizontalAlignment(_value.getInteger()); break; // alignment
      case VARIABLE : radioButton.setSelected(internalValue.value = _value.getBoolean()); break;
      case SELECTED :
        defaultStateSet = true; defaultState = _value.getBoolean();
        setActive (false); reset (); setActive(true);
        break;
      case 6 : // action
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 7 : // actionon
        removeAction (ControlSwingElement.ACTION_ON,getProperty("actionon"));
        addAction(ControlSwingElement.ACTION_ON,_value.getString());
        break;
      case 8 : // actionoff
        removeAction (ControlSwingElement.ACTION_OFF,getProperty("actionoff"));
        addAction(ControlSwingElement.ACTION_OFF,_value.getString());
        break;
      case 9 : radioButton.setMnemonic(_value.getString().charAt(0)); break;
      case 10 : cantUnselectItself = _value.getBoolean(); break;
      default: super.setValue(_index-11,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : radioButton.setText(labelString=""); break;
      case 1 : radioButton.setIcon(null); imageFile=null; break;
      case 2 : radioButton.setIcon(null); selectedimageFile=null; break;
      case 3 : radioButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER ); break;
      case VARIABLE : break; // Do nothing
      case SELECTED : defaultStateSet = false; break;
      case 6 : removeAction (ControlElement.ACTION,getProperty("action"));            break;
      case 7 : removeAction (ControlSwingElement.ACTION_ON,getProperty("actionon"));  break;
      case 8 : removeAction (ControlSwingElement.ACTION_OFF,getProperty("actionoff"));break;
      case 9 : radioButton.setMnemonic(-1); break;
      case 10 : cantUnselectItself = false; break;
      default: super.setDefaultValue(_index-11); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : 
      case 1 : 
      case 2 : return "<none>";
      case 3 : return "CENTER";
      case VARIABLE : 
      case SELECTED : return "<none>";
      case 6 : 
      case 7 : 
      case 8 : return "<no_action>";
      case 9 : return "<none>";
      case 10 : return "false";
      default : return super.getDefaultValueString(_index-11);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case VARIABLE : return internalValue;
      case 0 : case 1 : case 2 : case 3 :
      case SELECTED : case 6 : case 7 : case 8 :
      case 9 : case 10 :
        return null;
      default: return super.getValue(_index-11);
    }
  }

} // End of class

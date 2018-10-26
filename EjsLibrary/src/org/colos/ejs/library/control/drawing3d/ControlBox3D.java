/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementBox;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlBox3D extends ControlElement3D {
  static final private int BOX_PROPERTIES_ADDED=3;

  private ElementBox box;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementBox"; }

  protected Element createElement () { return box = new ElementBox(); }

  protected int getPropertiesDisplacement () { return BOX_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("closedTop");
      infoList.add ("closedBottom");
      infoList.add ("sizeZreduction");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("closedTop")) return "boolean";
    if (_property.equals("closedBottom")) return "boolean";
    if (_property.equals("sizeZreduction")) return "double";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : box.setClosedTop(_value.getBoolean());    break;
      case 1 : box.setClosedBottom(_value.getBoolean()); break;
      case 2 : box.setSizeZReduction(_value.getDouble()); break;
      default : super.setValue(_index-BOX_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
    case 0 : box.setClosedTop(true);    break;
    case 1 : box.setClosedBottom(true); break;
    case 2 : box.setSizeZReduction(1); break;
      default: super.setDefaultValue(_index-BOX_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : 
      case 1 : return "true";
      case 2 : return "1.0";
      default : return super.getDefaultValueString(_index-BOX_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : return null;
      default: return super.getValue (_index-BOX_PROPERTIES_ADDED);
    }
  }

} // End of class

/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import java.awt.Font;
import org.opensourcephysics.drawing2d.*;
import org.colos.ejs.library.control.value.*;

/**
 * A 2D image
 */
public class ControlText2D extends ControlElement2D {
  static final private int PROPERTIES_ADDED=4;

  private ElementText text;
  private Font font, defaultFont;

  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.ElementText"; }

  protected org.opensourcephysics.display.Drawable createDrawable () {
    text = new ElementText();
    font = defaultFont = text.getFont();
    return text;
  }

  protected int getPropertiesDisplacement () { return PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("text");
      infoList.add ("font");
      infoList.add ("pixelSize");
      infoList.add ("elementposition");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("text")) return "String TRANSLATABLE";
    if (_property.equals("font")) return "Font|Object";
    if (_property.equals("pixelSize")) return "boolean";
    if (_property.equals("elementposition"))return "ElementPosition|int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getString()!=text.getText()) text.setText(_value.getString()); break;
      case 1 : if (_value.getObject() instanceof Font) {
                 Font newFont = (Font) _value.getObject();
                 if (newFont!=font) text.setFont(font=newFont);
                 }
               break;
      case 2 : if (_value.getBoolean()!=text.isTrueSize()) text.setTrueSize(_value.getBoolean()); break;
      case 3 : if (_value.getInteger()!=text.getStyle().getRelativePosition()) text.getStyle().setRelativePosition(_value.getInteger()); break; 
      default : super.setValue(_index-PROPERTIES_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : text.setText(""); break;
      case 1 : text.setFont(defaultFont); break;
      case 2 : text.setTrueSize(false); break;
      case 3 : text.getStyle().setRelativePosition(Style.CENTERED); break; 
      default: super.setDefaultValue(_index-PROPERTIES_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "<none>";
      case 2 : return "false";
      case 3 : return "CENTERED";
      default : return super.getDefaultValueString(_index-PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : 
        return null; 
      default: return super.getValue (_index-PROPERTIES_ADDED);
    }
  }

} // End of class

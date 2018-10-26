/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

import java.awt.Font;

/**
 * A set of Texts
 */
public class ControlTextSet3D extends ControlSet3D {
  static final private int TEXTSET_PROPERTIES_ADDED=4;

  private Font defaultElementFont;

  protected int getPropertiesAddedToSet () { return TEXTSET_PROPERTIES_ADDED; }

  public ControlTextSet3D () {
    super ();
    defaultElementFont = ((ElementText)elements[0]).getFont();
  }

  protected Element createAnElement() {
    return new ElementText();
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementText) newElement).setText(((ElementText) oldElement).getText());
      ((ElementText) newElement).setFont(((ElementText) oldElement).getFont());
      ((ElementText)newElement).setTrueSize(((ElementText)oldElement).isTrueSize());
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("text");
        infoList.add ("font");
        infoList.add ("pixelSize");
        infoList.add ("rotationAngle");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
        if (_property.equals("text")) return "String|String[] TRANSLATABLE";
        if (_property.equals("font")) return "Font|Object|Object[]";
        if (_property.equals("pixelSize")) return "boolean|boolean[]";
        if (_property.equals("rotationAngle")) return "int|double|int[]|double[]";

        return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
      case 0 :
          if (_value.getObject() instanceof String[]) {
            String[] val = (String[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementText) elements[i]).setText(val[i]);
          }
          else {
            String val = _value.getString();
            for (int i=0; i<numElements; i++) ((ElementText) elements[i]).setText(val);
          }
        break;
      case 1 :
          if (_value.getObject() instanceof Object[]) {
            Object[] val = (Object[]) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementText) elements[i]).setFont((Font)val[i]);
          }
          else if (_value.getObject() instanceof Font) {
            Font newFont = (Font) _value.getObject();
            for (int i=0; i<numElements; i++) ((ElementText) elements[i]).setFont(newFont);
          }
        break;
      case 2 :
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(numElements,val.length); i<n; i++) ((ElementText)elements[i]).setTrueSize(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0; i<numElements; i++) ((ElementText)elements[i]).setTrueSize(val);
        }
        break;
      case 3 :
        if (_value.getObject() instanceof double[]) {
              double[] val = (double[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementText) elements[i]).setRotationAngle(val[i]);
            }
            else if (_value.getObject() instanceof int[]) {
              int[] val = (int[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementText) elements[i]).setRotationAngle(val[i]*ControlDrawingPanel3D.TO_RAD);
            }
            else if (_value instanceof IntegerValue) {
              double angle = _value.getInteger()*ControlDrawingPanel3D.TO_RAD;
              for (int i=0; i<numElements; i++) ((ElementText) elements[i]).setRotationAngle(angle);
            }
            else {
              double angle = _value.getDouble();
              for (int i=0; i<numElements; i++) ((ElementText) elements[i]).setRotationAngle(angle);
            }
            break;
        default : super.setValue(_index-TEXTSET_PROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : for (int i=0; i<numElements; i++) ((ElementText)elements[i]).setText(""); break;
        case 1 : for (int i=0; i<numElements; i++) ((ElementText)elements[i]).setFont(defaultElementFont); break;
        case 2 : for (int i=0; i<numElements; i++) ((ElementText)elements[i]).setTrueSize(false); break; 
        case 3 : for (int i=0; i<numElements; i++) ((ElementText) elements[i]).setRotationAngle(0.0); break;
        default: super.setDefaultValue(_index-TEXTSET_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "<none>";
        case 1 : return "<none>";
        case 2 : return "false";
        case 3 : return "0.0";
        default : return super.getDefaultValueString(_index-TEXTSET_PROPERTIES_ADDED);
      }
    }
    
    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : case 3 : return null;
        default: return super.getValue (_index-TEXTSET_PROPERTIES_ADDED);
      }
    }

} // End of class

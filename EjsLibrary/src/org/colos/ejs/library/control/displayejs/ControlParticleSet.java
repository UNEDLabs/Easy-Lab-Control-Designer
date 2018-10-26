/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import java.awt.*;

import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;

/**
 * An interactive set of particles
 */
public class ControlParticleSet extends ControlElementSet {
  static private final int PARTICLE_SET_ADDED = 1;
  static private final int MY_STYLE=STYLE+PARTICLE_SET_ADDED;
  static private final int MY_PRIMARY_COLOR=PRIMARY_COLOR+PARTICLE_SET_ADDED;
  static private final int MY_SECONDARY_COLOR=SECONDARY_COLOR+PARTICLE_SET_ADDED;

  protected Drawable createDrawable () {
      elementSet = new ElementSet(1, InteractiveParticle.class);
      elementSet.setEnabled(InteractiveElement.TARGET_POSITION,true);  // Backwards compatibility
    return elementSet;
  }

  protected int getPropertiesDisplacement () { return PARTICLE_SET_ADDED; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("pixelSize");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
      if (_property.equals("pixelSize"))         return "boolean|boolean[]";
      return super.getPropertyInfo(_property);
    }


// ------------------------------------------------
// Variable properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case  0 :
        if (_value.getObject() instanceof boolean[]) {
          boolean[] tv = (boolean[]) _value.getObject();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
            ((InteractiveParticle)elementSet.elementAt(i)).setPixelSize(tv[i]);
        }
        else {
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
            ((InteractiveParticle)elementSet.elementAt(i)).setPixelSize(_value.getBoolean());
        }
        break;

      case MY_STYLE :
        if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) ((InteractiveParticle)elementSet.elementAt(i)).setShapeType(val[i]);
          }
          else {
            int val = _value.getInteger();
            for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveParticle)elementSet.elementAt(i)).setShapeType(val);
          }
        break;

      case MY_PRIMARY_COLOR :
        if (_value instanceof IntegerValue) {
          Color col = DisplayColors.getLineColor(_value.getInteger());
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern(col);
        }
        else if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern(DisplayColors.getLineColor(val[i]));
        }
        else if (_value.getObject() instanceof Object[]) {
          Object[] val = (Object[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern((Paint)val[i]);
        }
        else if (_value.getObject() instanceof Color) {
          Paint val = (Paint) _value.getObject();
          if (val==NULL_COLOR) val = null;
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern(val);
        }
        break;

      case MY_SECONDARY_COLOR :
        if (_value instanceof IntegerValue) {
          Color col = DisplayColors.getLineColor(_value.getInteger());
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor(col);
        }
        else if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor(DisplayColors.getLineColor(val[i]));
        }
        else if (_value.getObject() instanceof Object[]) {
          Object[] val = (Object[]) _value.getObject();
          for (int i=0, n=Math.min(elementSet.getNumberOfElements(),val.length); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor((Color)val[i]);
        }
        else if (_value.getObject() instanceof Color) {
          Color val = (Color) _value.getObject();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor(val);
        }
        break;

      default: super.setValue(_index-PARTICLE_SET_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  0 :
        for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
          ((InteractiveParticle)elementSet.elementAt(i)).setPixelSize(false);
        break;

      case MY_STYLE :
        for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) ((InteractiveParticle)elementSet.elementAt(i)).setShapeType(InteractiveParticle.ELLIPSE);
        break;
      case MY_PRIMARY_COLOR   : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setFillPattern(Color.blue); break;
      case MY_SECONDARY_COLOR : for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setEdgeColor(Color.black); break;
      default : super.setDefaultValue (_index-PARTICLE_SET_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  0 : return "false";
      default : return super.getDefaultValueString(_index-PARTICLE_SET_ADDED);

      case MY_STYLE : return "ELLIPSE";
      case MY_PRIMARY_COLOR   : return "BLUE";
      case MY_SECONDARY_COLOR : return "BLACK";
    }
  }

  public Value getValue (int _index) {
  switch (_index) {
    case 0 : return null;
    default: return super.getValue(_index-PARTICLE_SET_ADDED);
  }
}



} // End of class

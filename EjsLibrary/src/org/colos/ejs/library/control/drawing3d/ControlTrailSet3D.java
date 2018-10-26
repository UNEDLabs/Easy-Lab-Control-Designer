/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementTrail;
import org.opensourcephysics.drawing3d.MultiTrail;

/**
 * A set of Trails
 */
public class ControlTrailSet3D extends ControlSet3D implements org.colos.ejs.library.control.swing.NeedsPreUpdate,
                                                               org.colos.ejs.library.control.Resetable,
                                                               org.colos.ejs.library.control.DataCollector {
  static final private int TRAILSET_PROPERTIES_ADDED=12;

  private volatile boolean isSet = false;
  private double[] x, y, z;
  private double[][] xArray=null, yArray=null, zArray=null;

  @Override
  protected int getPropertiesAddedToSet () { return TRAILSET_PROPERTIES_ADDED; }

  @Override
  protected Element createAnElement() {
    Element element = new MultiTrail();
    return element;
  }

  @Override
  public void setNumberOfElements(int newNumber) {
    super.setNumberOfElements(newNumber);
    x = new double[newNumber];
    y = new double[newNumber];
    z = new double[newNumber];
  }
  
  @Override
  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((MultiTrail)newElement).setMaximumPoints(((MultiTrail)oldElement).getMaximumPoints());
      ((MultiTrail)newElement).setConnectionType(((MultiTrail)oldElement).getConnectionType());
      ((MultiTrail)newElement).setSkipPoints(((MultiTrail)oldElement).getSkipPoints());
      ((MultiTrail)newElement).setActive(((MultiTrail)oldElement).isActive());
      ((MultiTrail)newElement).setNoRepeat(((MultiTrail)oldElement).isNoRepeat());
  }

  @Override
  public void reset () { // Overwrites default reset
    for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).clear();
  }

  @Override
  public void initialize () { // Overwrites default initialize
    for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).initialize();
  }

  public void preupdate () {
    if (isSet) {
      for (int i=0; i<numElements; i++) {
        MultiTrail trail = (MultiTrail) elements[i];
        if (!trail.isActive()) continue;
//        trail.addPoint(x[i],y[i],z[i]);
        if (xArray==null) {
          if (yArray==null) {
            if (zArray==null) trail.addPoint(x[i],y[i],z[i]);
            else trail.addPoints(x[i],y[i],zArray[i]);
          }
          else {
            if (zArray==null) trail.addPoints(x[i],yArray[i],z[i]);
            else trail.addPoints(x[i],yArray[i],zArray[i]);
          }
        }
        else {
          if (yArray==null) {
            if (zArray==null) trail.addPoints(xArray[i],y[i],z[i]);
            else trail.addPoints(xArray[i],y[i],zArray[i]);
          }
          else {
            if (zArray==null) trail.addPoints(xArray[i],yArray[i],z[i]);
            else trail.addPoints(xArray[i],yArray[i],zArray[i]);
          }
        }
      }
    }
//    if (isSet) for (int i=0; i<numElements; i++) {
//      MultiTrail trail = (MultiTrail) elements[i];
//      if (trail.isActive()) trail.addPoint(x[i],y[i],z[i]);
//    }
    isSet = false;
  }

  @Override
  public void onExit () { // free memory
    for (int i=0; i<numElements; i++) ((MultiTrail) elements[i]).clear();
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("maximumPoints");
        infoList.add ("connected");
        infoList.add ("inputX");
        infoList.add ("inputY");
        infoList.add ("inputZ");

        infoList.add ("skippoints");
        infoList.add ("active");
        infoList.add ("norepeat");
        infoList.add ("clearAtInput");

        infoList.add ("xLabel");
        infoList.add ("yLabel");
        infoList.add ("zLabel");

        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
        if (_property.equals("maximumPoints")) return "int|int[]";
        if (_property.equals("connected")) return "boolean|boolean[]";
        if (_property.equals("inputX")) return "int|double|double[]|double[][]";
        if (_property.equals("inputY")) return "int|double|double[]|double[][]";
        if (_property.equals("inputZ")) return "int|double|double[]|double[][]";

        if (_property.equals("skippoints"))  return "int";
        if (_property.equals("active"))      return "boolean";
        if (_property.equals("norepeat"))    return "boolean";
        if (_property.equals("clearAtInput"))return "boolean";

        if (_property.equals("xLabel"))    return "String|String[] TRANSLATABLE";
        if (_property.equals("yLabel"))    return "String|String[] TRANSLATABLE";
        if (_property.equals("zLabel"))    return "String|String[] TRANSLATABLE";

      return super.getPropertyInfo(_property);
    }
    
    @Override
    public ControlElement setProperty(String _property, String _value) {
      _property = _property.trim();
      if (_value!=null) {
        boolean useDefaultTitle = _value.startsWith("%_model.") && _value.endsWith("()%");
        if      (_property.equals("inputX")) for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setXLabel(useDefaultTitle ? "Input X" : _value);
        else if (_property.equals("inputY")) for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setYLabel(useDefaultTitle ? "Input Y" : _value);
        else if (_property.equals("inputz")) for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setYLabel(useDefaultTitle ? "Input Z" : _value);
      }
      
      return super.setProperty(_property,_value);
    }


// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case 0 :
          if (_value.getObject() instanceof int[]) {
            int[] val = (int[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++) ((MultiTrail)elements[i]).setMaximumPoints(val[i]);
          }
          else {
            int val = _value.getInteger();
            for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setMaximumPoints(val);
          }
          break;
        case 1 :
          if (_value.getObject() instanceof boolean[]) {
            boolean[] val = (boolean[]) _value.getObject();
            for (int i=0, n=Math.min(numElements,val.length); i<n; i++)  
              ((MultiTrail)elements[i]).setConnectionType(val[i]? ElementTrail.LINE_CONNECTION : ElementTrail.NO_CONNECTION);
          }
          else {
            int val = _value.getBoolean() ? ElementTrail.LINE_CONNECTION : ElementTrail.NO_CONNECTION;
            for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setConnectionType(val);
          }
          break;
        case 2 :
          xArray = null;
          if (_value.getObject() instanceof double[]) {
            double[] val = (double[]) _value.getObject();
            if (!numberOfElements_isSet) checkNumberOfElements (val.length,false);
            System.arraycopy(val,0,x,0,Math.min(numElements,val.length));
          }
          else if (_value.getObject() instanceof double[][]) {
            xArray = (double[][]) _value.getObject();
          }
          else {
            double val = _value.getDouble();
            for (int i=0; i<numElements; i++) x[i] = val;
          }
          isSet = true;
          break;
      case 3 :
        yArray = null;
        if (_value.getObject() instanceof double[]) {
          double[] val = (double[]) _value.getObject();
          if (!numberOfElements_isSet) checkNumberOfElements (val.length,false);
          System.arraycopy(val,0,y,0,Math.min(numElements,val.length));
        }
        else if (_value.getObject() instanceof double[][]) {
          yArray = (double[][]) _value.getObject();
        }
        else {
          double val = _value.getDouble();
          for (int i=0; i<numElements; i++) y[i] = val;
        }
        isSet = true;
        break;
      case 4 :
        zArray = null;
        if (_value.getObject() instanceof double[]) {
          double[] val = (double[]) _value.getObject();
          if (!numberOfElements_isSet) checkNumberOfElements (val.length,false);
          System.arraycopy(val,0,z,0,Math.min(numElements,val.length));
        }
        else if (_value.getObject() instanceof double[][]) {
          zArray = (double[][]) _value.getObject();
        }
        else {
          double val = _value.getDouble();
          for (int i=0; i<numElements; i++) z[i] = val;
        }
        isSet = true;
          break;

      case 5 : for (int i=0; i<numElements; i++) ((MultiTrail) elements[i]).setSkipPoints(_value.getInteger()); break;
      case 6 :
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(numElements,val.length); i<n; i++) ((MultiTrail)elements[i]).setActive(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setActive(val);
        }
        break;
      case 7 :
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(numElements,val.length); i<n; i++) ((MultiTrail)elements[i]).setNoRepeat(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setNoRepeat(val);
        }
        break;
      case 8 :
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(numElements,val.length); i<n; i++) ((MultiTrail)elements[i]).setClearAtInput(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setClearAtInput(val);
        }
        break;

      case 9 : 
        if (_value.getObject() instanceof String[]) {
          String[] val = (String[]) _value.getObject();
          for (int i=0, n=Math.min(numElements,val.length); i<n; i++) ((MultiTrail)elements[i]).setXLabel(val[i]);
        }
        else {
          String label = _value.getString();
          for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setXLabel(label); 
        }
        break;
      case 10 : 
        if (_value.getObject() instanceof String[]) {
          String[] val = (String[]) _value.getObject();
          for (int i=0, n=Math.min(numElements,val.length); i<n; i++) ((MultiTrail)elements[i]).setYLabel(val[i]);
        }
        else {
          String label = _value.getString();
          for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setYLabel(label);
        }
        break;
      case 11 : 
        if (_value.getObject() instanceof String[]) {
          String[] val = (String[]) _value.getObject();
          for (int i=0, n=Math.min(numElements,val.length); i<n; i++) ((MultiTrail)elements[i]).setZLabel(val[i]);
        }
        else {
          String label = _value.getString();
          for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setZLabel(label); 
        }
        break;

      default : super.setValue(_index-TRAILSET_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setMaximumPoints(0); break;
        case 1 : for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setConnectionType(ElementTrail.LINE_CONNECTION); break;
        case 2 : for (int i=0; i<numElements; i++) x[i] = 0.0; break;
        case 3 : for (int i=0; i<numElements; i++) y[i] = 0.0; break;
        case 4 : for (int i=0; i<numElements; i++) z[i] = 0.0; break;
        case 5 : for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setSkipPoints(0); break;
        case 6 : for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setActive(true); break;
        case 7 :  for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setNoRepeat(false); break;
        case 8 :  for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setClearAtInput(false); break;
        case 9 : for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setXLabel(null); break;
        case 10 : for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setYLabel(null); break;
        case 11 : for (int i=0; i<numElements; i++) ((MultiTrail)elements[i]).setZLabel(null); break;
        default: super.setDefaultValue(_index-TRAILSET_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
   }
    
    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "0";
        case 1 : return "true";
        case 2 : return "0.0";
        case 3 : return "0.0";
        case 4 : return "0.0";
        case 5 : return "0";
        case 6 : return "true";
        case 7 : return "false";
        case 8 : return "false";
        case 9 : return "<none>";
        case 10 : return "<none>";
        case 11 : return "<none>";

        default : return super.getDefaultValueString(_index-TRAILSET_PROPERTIES_ADDED);
      }
    }

    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : case 3 : 
        case 4 : case 5 : case 6 : case 7 :
        case 8 : case 9 : case 10 : case 11 :
          return null;
        default: return super.getValue (_index-TRAILSET_PROPERTIES_ADDED);
      }
    }


} // End of class

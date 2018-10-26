/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.drawing2d.*;

/**
 * A set of arrows
 */
public class ControlTrailSet2D extends ControlSet2D implements org.colos.ejs.library.control.swing.NeedsPreUpdate,
                                                               org.colos.ejs.library.control.Resetable,
                                                               org.colos.ejs.library.control.DataCollector {
  static final private int PROPERTIES_ADDED=10;
  
  private volatile boolean isSet = false;
  private double[] x, y;
  private double[][] xArray=null, yArray=null;

  protected int getPropertiesAddedToSet () { return PROPERTIES_ADDED; }

  protected Element createAnElement() {
    Element el = new ElementTrail();
    return el;
  }

  protected int setNumberOfElements(int newNumber) {
    int number = super.setNumberOfElements(newNumber);
    x = new double[number];
    y = new double[number];
    return number;
  }
  
    
  protected void copyAnElement (Element oldElement, Element newElement) {
    super.copyAnElement(oldElement, newElement);
    ((ElementTrail)newElement).setMaximumPoints(((ElementTrail)oldElement).getMaximumPoints());
    ((ElementTrail)newElement).setConnectionType(((ElementTrail)oldElement).getConnectionType());
    ((ElementTrail)newElement).setSkipPoints(((ElementTrail)oldElement).getSkipPoints());
    ((ElementTrail)newElement).setActive(((ElementTrail)oldElement).isActive());
    ((ElementTrail)newElement).setNoRepeat(((ElementTrail)oldElement).isNoRepeat());
  }

  public void reset () { // Overwrites default reset
    for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).clear();
  }

  public void initialize () { // Overwrites default initialize
    for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).initialize();
  }

  public void preupdate () {
    if (isSet) {
      for (int i=0; i<elements.length; i++) {
        ElementTrail trail = (ElementTrail) elements[i];
        if (!trail.isActive()) continue;
        if (xArray==null) {
          if (yArray==null) trail.addPoint(x[i], y[i]);
          else trail.addPoints(x[i],yArray[i]);
        }
        else {
          if (yArray == null) trail.addPoints(xArray[i],y[i]);
          else trail.addPoints(xArray[i],yArray[i]);
        }
      }
    }
    isSet = false;
  }

//  public void preupdate () {
//    if (isSet) for (int i=0; i<elements.length; i++) {
//      ElementTrail trail = (ElementTrail) elements[i];
//      if (trail.isActive()) trail.addPoint(x[i],y[i]);
//    }
//    isSet = false;
//  }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !org.opensourcephysics.tools.ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getParent().getDrawingPanel(),getSet()));
  }

  @Override
  public void onExit () { // free memory
    for (int i=0; i<elements.length; i++) ((ElementTrail) elements[i]).clear();
  }

//------------------------------------------------
//Definition of Properties
//------------------------------------------------

  static java.util.ArrayList<String> infoList=null;

  public java.util.ArrayList<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("maximumPoints");
      infoList.add ("connected");
      infoList.add ("inputX");
      infoList.add ("inputY");

      infoList.add ("skippoints");
      infoList.add ("active");
      infoList.add ("norepeat");
      infoList.add ("clearAtInput");

      infoList.add ("xLabel");
      infoList.add ("yLabel");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("maximumPoints")) return "int|int[]";
    if (_property.equals("connected")) return "boolean|boolean[]";
    if (_property.equals("inputX")) return "int|double|double[]|double[][]";
    if (_property.equals("inputY")) return "int|double|double[]|double[][]";

    if (_property.equals("skippoints"))  return "int|int[]";
    if (_property.equals("active"))      return "boolean|boolean[]";
    if (_property.equals("norepeat"))    return "boolean|boolean[]";
    if (_property.equals("clearAtInput"))return "boolean|boolean[]";

    if (_property.equals("xLabel"))    return "String|String[] TRANSLATABLE";
    if (_property.equals("yLabel"))    return "String|String[] TRANSLATABLE";

    return super.getPropertyInfo(_property);
  }

  @Override
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_value!=null) {
      boolean useDefaultTitle = _value.startsWith("%_model.") && _value.endsWith("()%");
      if      (_property.equals("inputX")) for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setXLabel(useDefaultTitle ? "Input X" : _value);
      else if (_property.equals("inputY")) for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setYLabel(useDefaultTitle ? "Input Y" : _value);
    }
    
    return super.setProperty(_property,_value);
  }

  //------------------------------------------------
  //Set and Get the values of the properties
  //------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : 
        if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementTrail)elements[i]).setMaximumPoints(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setMaximumPoints(val);
        }
        break;
      case 1 : 
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++)  
            ((ElementTrail)elements[i]).setConnectionType(val[i]? ElementTrail.LINE_CONNECTION : ElementTrail.NO_CONNECTION);
        }
        else {
          int val = _value.getBoolean() ? ElementTrail.LINE_CONNECTION : ElementTrail.NO_CONNECTION;
          for (int i=0; i<elements.length; i++) 
            ((ElementTrail)elements[i]).setConnectionType(val);
        }
        break;
      case 2 :
        xArray = null;
        if (_value.getObject() instanceof double[]) {
          double[] val = (double[]) _value.getObject();
          if (!numberOfElements_isSet) checkNumberOfElements (val.length,false);
          System.arraycopy(val,0,x,0,Math.min(elements.length,val.length));
        }
        else if (_value.getObject() instanceof double[][]) {
          xArray = (double[][]) _value.getObject();
        }
        else {
          double val = _value.getDouble();
          for (int i=0; i<elements.length; i++) x[i] = val;
        }
        isSet = true;
        break;
      case 3 : 
        yArray = null;
        if (_value.getObject() instanceof double[]) {
          double[] val = (double[]) _value.getObject();
          if (!numberOfElements_isSet) checkNumberOfElements (val.length,false);
          System.arraycopy(val,0,y,0,Math.min(elements.length,val.length));
        }
        else if (_value.getObject() instanceof double[][]) {
          yArray = (double[][]) _value.getObject();
        }
        else {
          double val = _value.getDouble();
          for (int i=0; i<elements.length; i++) y[i] = val;
        }
        isSet = true;
        break;
      case 4 : 
        if (_value.getObject() instanceof int[]) {
          int[] val = (int[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementTrail)elements[i]).setSkipPoints(val[i]);
        }
        else {
          int val = _value.getInteger();
          for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setSkipPoints(val);
        }
        break;
      case 5 :
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementTrail)elements[i]).setActive(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setActive(val);
        }
        break;
      case 6 :
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementTrail)elements[i]).setNoRepeat(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setNoRepeat(val);
        }
        break;
      case 7 :
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementTrail)elements[i]).setClearAtInput(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setClearAtInput(val);
        }
        break;

      case 8 : 
        if (_value.getObject() instanceof String[]) {
          String[] val = (String[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementTrail)elements[i]).setXLabel(val[i]);
        }
        else {
          String label = _value.getString();
          for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setXLabel(label); 
        }
        break;
      case 9 : 
        if (_value.getObject() instanceof String[]) {
          String[] val = (String[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementTrail)elements[i]).setYLabel(val[i]);
        }
        else {
          String label = _value.getString();
          for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setYLabel(label);
        }
        break;

      default : super.setValue(_index-PROPERTIES_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setMaximumPoints(0); break;
      case 1 : for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setConnectionType(ElementTrail.LINE_CONNECTION); break;
      case 2 : for (int i=0; i<elements.length; i++) x[i] = 0.0; break;
      case 3 : for (int i=0; i<elements.length; i++) y[i] = 0.0; break;
      case 4 : for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setSkipPoints(0); break;
      case 5 : for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setActive(true); break;
      case 6 :  for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setNoRepeat(false); break;
      case 7 :  for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setClearAtInput(false); break;
      case 8 : for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setXLabel(null); break;
      case 9 : for (int i=0; i<elements.length; i++) ((ElementTrail)elements[i]).setYLabel(null); break;
      default: super.setDefaultValue(_index-PROPERTIES_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "0";
      case 1 : return "true";
      case 2 : return "0.0";
      case 3 : return "0.0";
      case 4 : return "0";
      case 5 : return "true";
      case 6 : return "false";
      case 7 : return "false";
      case 8 : return "<none>";
      case 9 : return "<none>";
      default : return super.getDefaultValueString(_index-PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
      case 8 : case 9 :
        return null;
      default: return super.getValue (_index-PROPERTIES_ADDED);
    }
  }

} // End of class

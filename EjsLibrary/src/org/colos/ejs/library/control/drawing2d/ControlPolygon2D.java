/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import org.opensourcephysics.drawing2d.*;
import org.opensourcephysics.drawing2d.interaction.*;
import org.opensourcephysics.tools.ToolForData;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.ControlSwingElement;
import org.colos.ejs.library.control.value.*;

/**
 * A 2D polygon
 */
public class ControlPolygon2D extends ControlElement2D implements org.colos.ejs.library.control.swing.NeedsPreUpdate {
  static final private int POLYGON2D_ADDED=5;
  static final private int INDEX_SELECTED = 2;

  private ElementPolygon polygon;
  protected IntegerValue pointValue = new IntegerValue(-1);
  private double[] xArray=null, yArray=null, xFixed={ 0.0 }, yFixed={ 0.0 };
  private boolean dataSet=false;
  
  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.ElementPolygon"; }

  protected org.opensourcephysics.display.Drawable createDrawable () {
    polygon = new ElementPolygon();
    polygon.setData(new double[][]{ {0.0,0.0}, {0.1,0.0}, {0.1,0.1} });
    return polygon;
  }

  protected int getPropertiesDisplacement () { return POLYGON2D_ADDED; }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getParent().getDrawingPanel(),polygon));
  }
  
  public void preupdate() {
    if (dataSet) return;
    if (xArray==null) {
      if (yArray==null) polygon.setData(xFixed, yFixed); 
      else polygon.setData(xFixed, yArray);
    }
    else {
      if (yArray==null) polygon.setData(xArray, yFixed); 
      else polygon.setData(xArray, yArray);
    }
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.ArrayList<String> infoList=null;

  public java.util.ArrayList<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("closed");
      infoList.add ("data");
      infoList.add ("indexSelected");
      infoList.add ("xData");
      infoList.add ("yData");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("closed"))  return "boolean";
    if (_property.equals("data"))    return "double[][]|Object";
    if (_property.equals("indexSelected")) return "int";
    if (_property.equals("xData"))   return "int|double|double[]|Object";
    if (_property.equals("yData"))   return "int|double|double[]|Object";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : polygon.setClosed(_value.getBoolean()); break;
      case 1 : polygon.setData((double[][])_value.getObject()); dataSet = true; break;
      case INDEX_SELECTED : pointValue.value = _value.getInteger(); break;
      case 3 : 
        if (_value.getObject() instanceof double[]) xArray = (double[]) _value.getObject();
        else {
          xArray = null;
          xFixed[0] = _value.getDouble();
        }
        break;
      case 4 : 
        if (_value.getObject() instanceof double[]) yArray = (double[]) _value.getObject();
        else {
          yArray = null;
          yFixed[0] = _value.getDouble();
        }
        break;
      default : super.setValue(_index-POLYGON2D_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : polygon.setClosed(true); break;
      case 1 : polygon.setData(new double[][]{ {0.0,0.0}, {0.1,0.0}, {0.1,0.1} }); dataSet = false; break;
      case INDEX_SELECTED : pointValue.value = -1; break;
      case 3 : 
        xArray = null;
        xFixed[0] = 0;
        break;
      case 4 : 
        yArray = null;
        yFixed[0] = 0;
        break;
      default: super.setDefaultValue(_index-POLYGON2D_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "true";
      case 1 : return "new double[][]{ {0.0,0.0}, {0.1,0.0}, {0.1,0.1} }";
      case INDEX_SELECTED : return "-1";
      case 3 :
      case 4 : return "<none>";
      default : return super.getDefaultValueString(_index-POLYGON2D_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : return null;
      case 1 : return new ObjectValue(polygon.getData());
      case INDEX_SELECTED : return pointValue;
      case 3 :
      case 4 : return null;
      default: return super.getValue (_index-POLYGON2D_ADDED);
    }
  }

  //-------------------------------------
  //Respond to interaction
  //-------------------------------------

  @Override
  protected void propagateSize (ControlElement2D origin) {
    if (origin!=this) { // It is resizing the group
      super.propagateSize(origin);
      return;
    }
    Value objVal = new ObjectValue(polygon.getData());
    variableChanged (1,objVal);
    if (isUnderEjs) setFieldListValue(1,objVal);
  }

  @Override
  public void interactionPerformed(InteractionEvent _event) {
    switch (_event.getID()) {
      case InteractionEvent.MOUSE_ENTERED  : 
        pointValue.value = ((Integer) ((InteractionTarget)_event.getInfo()).getDataObject()).intValue();
        variableChanged (INDEX_SELECTED,pointValue);
        invokeActions (ControlSwingElement.MOUSE_ENTERED_ACTION); 
        break;
      case InteractionEvent.MOUSE_EXITED   : 
        invokeActions (ControlSwingElement.MOUSE_EXITED_ACTION);  
//        pointValue.value = -1; This caused problem with delayed actions
        variableChanged (INDEX_SELECTED,pointValue);
        break;
      case InteractionEvent.MOUSE_PRESSED  :
        pointValue.value = ((Integer) ((InteractionTarget)_event.getInfo()).getDataObject()).intValue();
        variableChanged (INDEX_SELECTED,pointValue);
        invokeActions (ControlSwingElement.ACTION_PRESS);
//        reportMouseMotion (_event.getInfo()); This calls On drag unneccessarily
        break;
      case InteractionEvent.MOUSE_DRAGGED  :
        reportMouseMotion (_event.getInfo()); 
        //invokeActions (ControlElement.VARIABLE_CHANGED);
        break;
      case InteractionEvent.MOUSE_RELEASED : 
        invokeActions (ControlElement.ACTION); 
//        pointValue.value = -1; This caused problem with delayed actions
        variableChanged (INDEX_SELECTED,pointValue);
        break;
    }
  } // End of interaction method


} // End of class

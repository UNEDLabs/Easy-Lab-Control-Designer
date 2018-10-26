/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementPolygon;
import org.opensourcephysics.tools.ToolForData;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlPolygon3D extends ControlElement3D implements org.colos.ejs.library.control.swing.NeedsPreUpdate {
  static final private int POLYGON_PROPERTIES_ADDED=6;
  static final private int INDEX_SELECTED = 2;

  private ElementPolygon polygon;
  protected IntegerValue pointValue = new IntegerValue(-1);
  private double[] xArray=null, yArray=null,zArray=null, xFixed={ 0.0 }, yFixed={ 0.0 }, zFixed={ 0.0 };
  private boolean dataSet=false;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementPolygon"; }

  protected Element createElement () {
    polygon = new ElementPolygon();
    polygon.setData(new double[][]{ {0.0,0.0,0.0}, {0.1,0.0,0.0}, {0.1,0.1,0.0} });
    return polygon;
  }

  protected int getPropertiesDisplacement () { return POLYGON_PROPERTIES_ADDED; }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getTopWindow(),polygon));
  }

  public void preupdate() {
    if (dataSet) return;
    if (xArray==null) {
      if (yArray==null) {
        if (zArray==null) polygon.setData(xFixed, yFixed, zFixed); 
        else polygon.setData(xFixed, yFixed, zArray);
      }
      else {
        if (zArray==null) polygon.setData(xFixed, yArray, zFixed); 
        else polygon.setData(xFixed, yArray, zArray);
      }
    }
    else {
      if (yArray==null) {
        if (zArray==null) polygon.setData(xArray, yFixed, zFixed); 
        else polygon.setData(xArray, yFixed, zArray);
      }
      else {
        if (zArray==null) polygon.setData(xArray, yArray, zFixed); 
        else polygon.setData(xArray, yArray, zArray);
      }
    }
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("closed");
      infoList.add ("data");
      infoList.add ("indexSelected");
      infoList.add ("xData");
      infoList.add ("yData");
      infoList.add ("zData");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("closed"))  return "boolean";
    if (_property.equals("data"))    return "double[][]";
    if (_property.equals("indexSelected")) return "int";
    if (_property.equals("xData"))   return "int|double|double[]|Object";
    if (_property.equals("yData"))   return "int|double|double[]|Object";
    if (_property.equals("zData"))   return "int|double|double[]|Object";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : polygon.setClosed  (_value.getBoolean()); break;
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
      case 5 : 
        if (_value.getObject() instanceof double[]) zArray = (double[]) _value.getObject();
        else {
          zArray = null;
          zFixed[0] = _value.getDouble();
        }
        break;
      default : super.setValue(_index-POLYGON_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : polygon.setClosed (true); break;
      case 1 : dataSet = false; break; // Do nothing
      case INDEX_SELECTED : pointValue.value = -1; break;
      case 3 : 
        xArray = null;
        xFixed[0] = 0;
        break;
      case 4 : 
        yArray = null;
        yFixed[0] = 0;
        break;
      case 5 : 
        zArray = null;
        zFixed[0] = 0;
        break;
      default: super.setDefaultValue(_index-POLYGON_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "true";
      case 1 : return "<none>";
      case INDEX_SELECTED : return "-1";
      case 3 :
      case 4 : 
      case 5 : return "<none>";
      default : return super.getDefaultValueString(_index-POLYGON_PROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : return null;
      default: return super.getValue (_index-POLYGON_PROPERTIES_ADDED);
    }
  }

  //-------------------------------------
  //Respond to interaction
  //-------------------------------------

//  @Override
//  protected void propagateSize (ControlElement2D origin) {
//    if (origin!=this) { // It is resizing the group
//      super.propagateSize(origin);
//      return;
//    }
//    Value objVal = new ObjectValue(polygon.getData());
//    variableChanged (1,objVal);
//    if (isUnderEjs) setFieldListValue(1,objVal);
//  }
//
//  @Override
//  public void interactionPerformed(InteractionEvent _event) {
//    switch (_event.getID()) {
//      case InteractionEvent.MOUSE_ENTERED  : 
//        pointValue.value = ((Integer) ((InteractionTarget)_event.getInfo()).getDataObject()).intValue();
//        variableChanged (INDEX_SELECTED,pointValue);
//        invokeActions (ControlSwingElement.MOUSE_ENTERED_ACTION); 
//        break;
//      case InteractionEvent.MOUSE_EXITED   : 
//        invokeActions (ControlSwingElement.MOUSE_EXITED_ACTION);  
////        pointValue.value = -1; This caused problem with delayed actions
//        variableChanged (INDEX_SELECTED,pointValue);
//        break;
//      case InteractionEvent.MOUSE_PRESSED  :
//        pointValue.value = ((Integer) ((InteractionTarget)_event.getInfo()).getDataObject()).intValue();
//        variableChanged (INDEX_SELECTED,pointValue);
//        reportMouseMotion (_event.getInfo());
//        invokeActions (ControlSwingElement.ACTION_PRESS);
//        break;
//      case InteractionEvent.MOUSE_DRAGGED  :
//        reportMouseMotion (_event.getInfo()); 
//        //invokeActions (ControlElement.VARIABLE_CHANGED);
//        break;
//      case InteractionEvent.MOUSE_RELEASED : 
//        invokeActions (ControlSwingElement.ACTION); 
////        pointValue.value = -1; This caused problem with delayed actions
//        variableChanged (INDEX_SELECTED,pointValue);
//        break;
//    }
//  } // End of interaction method


} // End of class

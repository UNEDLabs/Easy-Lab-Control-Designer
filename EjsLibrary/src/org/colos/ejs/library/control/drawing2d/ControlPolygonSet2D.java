/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.ControlSwingElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing2d.*;
import org.opensourcephysics.drawing2d.interaction.*;

/**
 * A set of arrows
 */
public class ControlPolygonSet2D extends ControlSet2D implements org.colos.ejs.library.control.swing.NeedsPreUpdate {
  static final private int POLSET2D_ROPERTIES_ADDED=5;
  static final private int INDEX_SELECTED = 2;

  protected IntegerValue pointValue = new IntegerValue(-1);
  private double[][] xArray=null, yArray=null, xFixedArray={{0.0}}, yFixedArray={{0.0}};
  private boolean dataSet=false;

  protected int getPropertiesAddedToSet () { return POLSET2D_ROPERTIES_ADDED; }

  @Override
  protected int setNumberOfElements(int newNumber) {
    int number = super.setNumberOfElements(newNumber);
    xFixedArray = new double[number][1];
    yFixedArray = new double[number][1];
    return number;
  }
  
  protected Element createAnElement() {
    ElementPolygon el = new ElementPolygon();
    el.setData(new double[][]{ {0.0,0.0}, {0.1,0.0}, {0.1,0.1} });
    return el;
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
    super.copyAnElement(oldElement, newElement);
    ((ElementPolygon)newElement).setClosed(((ElementPolygon)oldElement).isClosed());
    ((ElementPolygon)newElement).setData(((ElementPolygon)oldElement).getData());
  }

  @Override
  public void addMenuEntries () {
    if (getMenuNameEntry()==null || !org.opensourcephysics.tools.ToolForData.getTool().isFullTool()) return;
     getSimulation().addElementMenuEntries(getMenuNameEntry(), getDataInformationMenuEntries(getParent().getDrawingPanel(),getSet()));
  }

  public void preupdate() {
    if (dataSet) return;
    if (xArray==null) {
      if (yArray==null) for (int i=0; i<elements.length; i++) ((ElementPolygon)elements[i]).setData(xFixedArray[i], yFixedArray[i]);
      else for (int i=0, n=Math.min(elements.length,yArray.length); i<n; i++) ((ElementPolygon)elements[i]).setData(xFixedArray[i], yArray[i]);
    }
    else {
      if (yArray==null) for (int i=0, n=Math.min(elements.length,xArray.length); i<n; i++) ((ElementPolygon)elements[i]).setData(xArray[i],yFixedArray[i]); 
      else for (int i=0, n=Math.min(elements.length,Math.min(xArray.length,yArray.length)); i<n; i++)
        ((ElementPolygon)elements[i]).setData(xArray[i], yArray[i]);
    }
  }

//------------------------------------------------
//Definition of Properties
//------------------------------------------------

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
    if (_property.equals("closed"))  return "boolean|boolean[]";
    if (_property.equals("data"))    return "double[][]|Object|double[][][]";
    if (_property.equals("indexSelected")) return "int";
    if (_property.equals("xData"))   return "int|double|double[]|double[][]|Object";
    if (_property.equals("yData"))   return "int|double|double[]|double[][]|Object";
    return super.getPropertyInfo(_property);
  }

  //------------------------------------------------
  //Set and Get the values of the properties
  //------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : 
        if (_value.getObject() instanceof boolean[]) {
          boolean[] val = (boolean[]) _value.getObject();
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementPolygon)elements[i]).setClosed(val[i]);
        }
        else {
          boolean val = _value.getBoolean();
          for (int i=0; i<elements.length; i++) ((ElementPolygon)elements[i]).setClosed(val);
        }
        break;
      case 1 : 
        if (_value.getObject() instanceof double[][][]) {
          double[][][] val = (double[][][]) _value.getObject();
          checkNumberOfElements(val.length, true);
          for (int i=0, n=Math.min(elements.length,val.length); i<n; i++) ((ElementPolygon)elements[i]).setData(val[i]);
        }
        else {
          double[][] val = (double[][]) _value.getObject();
          for (int i=0; i<elements.length; i++) ((ElementPolygon)elements[i]).setData(val);
        }
        dataSet = true;
        break;
      case INDEX_SELECTED : pointValue.value = _value.getInteger(); break;
      case 3 : 
        if (_value.getObject() instanceof double[][]) xArray = (double[][]) _value.getObject();
        else if (_value.getObject() instanceof double[]) {
          xArray = null;
          double[] val = (double[]) _value.getObject();
          int n=Math.min(xFixedArray.length,val.length);
          for (int i=0; i<n; i++) xFixedArray[i][0] = val[i];
          for (int i=n; i<xFixedArray.length; i++) xFixedArray[i][0] = 0;
        }
        else {
          xArray = null;
          double val = _value.getDouble();
          for (int i=0; i<xFixedArray.length; i++) xFixedArray[i][0] = val;
        }
        break;
      case 4 : 
        if (_value.getObject() instanceof double[][]) yArray = (double[][]) _value.getObject();
        else if (_value.getObject() instanceof double[]) {
          yArray = null;
          double[] val = (double[]) _value.getObject();
          int n=Math.min(yFixedArray.length,val.length);
          for (int i=0; i<n; i++) yFixedArray[i][0] = val[i];
          for (int i=n; i<yFixedArray.length; i++) yFixedArray[i][0] = 0;
        }
        else {
          yArray = null;
          double val = _value.getDouble();
          for (int i=0; i<yFixedArray.length; i++) yFixedArray[i][0] = val;
        }
        break;
      default : super.setValue(_index-POLSET2D_ROPERTIES_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : for (int i=0; i<elements.length; i++) ((ElementPolygon)elements[i]).setClosed(true); break;
      case 1 : 
        double[][] data = new double[][]{ {0.0,0.0}, {0.1,0.0}, {0.1,0.1} };
        for (int i=0; i<elements.length; i++) ((ElementPolygon)elements[i]).setData(data); 
        dataSet = false;
        break;
      case INDEX_SELECTED : pointValue.value = -1; break;
      case 3 : 
        xArray = null;
        for (int i=0; i<xFixedArray.length; i++) xFixedArray[i][0] = 0;
        break;
      case 4 : 
        yArray = null;
        for (int i=0; i<yFixedArray.length; i++) yFixedArray[i][0] = 0;
        break;
      default: super.setDefaultValue(_index-POLSET2D_ROPERTIES_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "true";
      case 1 : return "new double[][]{ {0.0,0.0}, {0.1,0.0}, {0.1,0.1} }";
      case INDEX_SELECTED : return "-1";
      case 3 :
      case 4 : return "<none>";
      default : return super.getDefaultValueString(_index-POLSET2D_ROPERTIES_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : return null;
      case 1 : 
        double[][][] data = new double[elements.length][][];
        for (int i=0; i<elements.length; i++) data[i] = ((ElementPolygon)elements[i]).getData();
        return new ObjectValue(data);
      case INDEX_SELECTED : return pointValue;
      case 3 :
      case 4 : return null;
      default: return super.getValue (_index-POLSET2D_ROPERTIES_ADDED);
    }
  }

  //-------------------------------------
  //Respond to interaction
  //-------------------------------------

  @Override
  protected void propagateSize (ControlElement2D origin,int _index) {
    if (origin!=this) { // It is resizing the group
      super.propagateSize(origin);
      return;
    }
    double[][][] data = new double[elements.length][][];
    for (int i=0; i<elements.length; i++) data[i] = ((ElementPolygon)elements[i]).getData();
    Value objVal = new ObjectValue(data);
    variableChanged (1,objVal);
    if (isUnderEjs) setFieldListValue(1,objVal);
  }
  
  @Override
  public void interactionPerformed(InteractionEvent _event) {
//    System.out.println("Event ID = "+_event.getID());
    switch (_event.getID()) {
      case InteractionEvent.MOUSE_ENTERED  :
        selectedValue.value = getSet().getInteractedIndex(); //getElementInteracted(_event);
//        selectedValue.value = set.indexInGroup((Element) _event.getSource());
        pointValue.value = ((Integer) ((InteractionTarget)_event.getInfo()).getDataObject()).intValue();
        variableExtraChanged (ELEMENT_SELECTED+getPropertiesAddedToSet(),selectedValue);
        variableChanged (INDEX_SELECTED,pointValue);
        invokeActions (ControlSwingElement.MOUSE_ENTERED_ACTION);
        break;
      case InteractionEvent.MOUSE_EXITED   :
        invokeActions (ControlSwingElement.MOUSE_EXITED_ACTION);
//        selectedValue.value = -1;  This caused problem with delayed actions
//        pointValue.value = -1;  This caused problem with delayed actions
        variableExtraChanged (ELEMENT_SELECTED+getPropertiesAddedToSet(),selectedValue);
        variableChanged (INDEX_SELECTED,pointValue);
        break;
      case InteractionEvent.MOUSE_PRESSED  :
        selectedValue.value = getSet().getInteractedIndex(); //getElementInteracted(_event);
//        selectedValue.value = set.indexInGroup((Element) _event.getSource());
        pointValue.value = ((Integer) ((InteractionTarget)_event.getInfo()).getDataObject()).intValue();
        variableExtraChanged (ELEMENT_SELECTED+getPropertiesAddedToSet(),selectedValue);
        variableChanged (INDEX_SELECTED,pointValue);
        invokeActions (ControlSwingElement.ACTION_PRESS);
//        reportMouseMotion (_event.getInfo(),selectedValue.value); This calls On drag unneccessarily
        break;
      case InteractionEvent.MOUSE_DRAGGED  :
        reportMouseMotion (_event.getInfo(),selectedValue.value);
        //invokeActions (ControlSwingElement.MOUSE_DRAGGED_ACTION);
        break;
      case InteractionEvent.MOUSE_RELEASED :
        invokeActions (ControlElement.ACTION);
//        selectedValue.value = -1;  This caused problem with delayed actions
//        pointValue.value = -1;  This caused problem with delayed actions
        variableExtraChanged (ELEMENT_SELECTED+getPropertiesAddedToSet(),selectedValue);
        variableChanged (INDEX_SELECTED,pointValue);
        break;
    }
  } // End of interaction method

  
  
  
} // End of class

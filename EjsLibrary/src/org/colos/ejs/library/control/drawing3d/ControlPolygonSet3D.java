/*
 * The control.display3D package contains classes that wrap display3d objects
 * Copyright (c) February 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

/**
 * A set of Polygons
 */
public class ControlPolygonSet3D extends ControlSet3D implements org.colos.ejs.library.control.swing.NeedsPreUpdate {
  static final private int POLSET_ROPERTIES_ADDED=6;

  protected IntegerValue pointValue = new IntegerValue(-1);
  private double[][] xArray=null, yArray=null, zArray=null, xFixedArray={{0.0}}, yFixedArray={{0.0}}, zFixedArray={{0.0}};
  private boolean dataSet=false;

  protected int getPropertiesAddedToSet () { return POLSET_ROPERTIES_ADDED; }

  @Override
  public synchronized void setNumberOfElements(int newNumber) {
    super.setNumberOfElements(newNumber);
    xFixedArray = new double[newNumber][1];
    yFixedArray = new double[newNumber][1];
    zFixedArray = new double[newNumber][1];
  }

  protected Element createAnElement() {
    ElementPolygon el = new ElementPolygon();
    el.setData(new double[][]{ {0.0,0.0,0.0}, {0.1,0.0,0.0}, {0.1,0.1,0.0} });
    return el;
  }

  protected void copyAnElement (Element oldElement, Element newElement) {
      super.copyAnElement(oldElement,newElement);
      ((ElementPolygon) newElement).setClosed(((ElementPolygon) oldElement).isClosed());
      ((ElementPolygon) newElement).setData(((ElementPolygon) oldElement).getData());
  }
  
  public void preupdate() {
    if (dataSet) return;
    if (xArray==null) {
      if (yArray==null) {
        if (zArray==null) for (int i=0; i<numElements; i++) ((ElementPolygon)elements[i]).setData(xFixedArray[i], yFixedArray[i], zFixedArray[i]); 
        else {
          int min = Math.min(numElements,zArray.length);
          for (int i=0; i<min; i++) ((ElementPolygon)elements[i]).setData(xFixedArray[i], yFixedArray[i], zArray[i]); 
        }
      }
      else {
        if (zArray==null) {
          int min = Math.min(numElements,yArray.length);
          for (int i=0; i<min; i++) ((ElementPolygon)elements[i]).setData(xFixedArray[i], yArray[i], zFixedArray[i]); 
        }
        else {
          int min = Math.min(numElements,Math.min(yArray.length,zArray.length));
          for (int i=0; i<min; i++) ((ElementPolygon)elements[i]).setData(xFixedArray[i], yArray[i], zArray[i]); 
        }
      }
    }
    else {
      if (yArray==null) {
        if (zArray==null) {
          int min = Math.min(numElements,xArray.length);
          for (int i=0; i<min; i++) ((ElementPolygon)elements[i]).setData(xArray[i], yFixedArray[i], zFixedArray[i]); 
        }
        else {
          int min = Math.min(numElements,Math.min(xArray.length,zArray.length));
          for (int i=0; i<min; i++) ((ElementPolygon)elements[i]).setData(xArray[i], yFixedArray[i], zArray[i]); 
        }
      }
      else {
        if (zArray==null) {
          int min = Math.min(numElements,Math.min(xArray.length,yArray.length));
          for (int i=0; i<min; i++) ((ElementPolygon)elements[i]).setData(xArray[i], yArray[i], zFixedArray[i]); 
        }
        else {
          int min = Math.min(numElements,Math.min(xArray.length,Math.min(yArray.length,zArray.length)));
          for (int i=0; i<min; i++) ((ElementPolygon)elements[i]).setData(xArray[i], yArray[i], zArray[i]); 
        }
      }
    }
  }

  // ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

    static java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
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
        if (_property.equals("closed"))  return "boolean|boolean[]";
        if (_property.equals("data"))    return "double[][]|Object|double[][][]";
        if (_property.equals("indexSelected")) return "int";
        if (_property.equals("xData"))   return "int|double|double[]|double[][]|Object";
        if (_property.equals("yData"))   return "int|double|double[]|double[][]|Object";
        if (_property.equals("zData"))   return "int|double|double[]|double[][]|Object";

      return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case 0 :
            if (_value.getObject() instanceof boolean[]) {
              boolean[] val = (boolean[]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementPolygon) elements[i]).setClosed(val[i]);
            }
            else {
              boolean val = _value.getBoolean();
              for (int i=0; i<numElements; i++) ((ElementPolygon) elements[i]).setClosed(val);
            }
            break;
        case 1 :
            if (_value.getObject() instanceof double[][][]) {
              double[][][] val = (double[][][]) _value.getObject();
              checkNumberOfElements(val.length, true);
              for (int i=0; i<numElements; i++) ((ElementPolygon) elements[i]).setData(val[i]);
            }
            else if (_value.getObject() instanceof double[][]) {
              double[][] val = (double[][]) _value.getObject();
              for (int i=0; i<numElements; i++) ((ElementPolygon) elements[i]).setData(val);
            }
            dataSet = true;
            break;
        case 2 : pointValue.value = _value.getInteger(); break;
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
        case 5 : 
          if (_value.getObject() instanceof double[][]) zArray = (double[][]) _value.getObject();
          else if (_value.getObject() instanceof double[]) {
            zArray = null;
            double[] val = (double[]) _value.getObject();
            int n=Math.min(zFixedArray.length,val.length);
            for (int i=0; i<n; i++) zFixedArray[i][0] = val[i];
            for (int i=n; i<zFixedArray.length; i++) zFixedArray[i][0] = 0;
          }
          else {
            zArray = null;
            double val = _value.getDouble();
            for (int i=0; i<zFixedArray.length; i++) zFixedArray[i][0] = val;
          }
          break;
        default : super.setValue(_index-POLSET_ROPERTIES_ADDED,_value); break;       
      }
      if (isUnderEjs) updatePanel();
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : for (int i = 0; i < numElements; i++) ((ElementPolygon) elements[i]).setClosed(true); break;
        case 1 : 
          double[][] data = new double[][]{ {0.0,0.0,0.0}, {0.1,0.0,0.0}, {0.1,0.1,0.0} };
          for (int i=0; i<numElements; i++) ((ElementPolygon)elements[i]).setData(data); 
          dataSet = false;
          break;
        case 2 : pointValue.value = -1; break;
        case 3 : 
          xArray = null;
          for (int i=0; i<xFixedArray.length; i++) xFixedArray[i][0] = 0;
          break;
        case 4 : 
          yArray = null;
          for (int i=0; i<yFixedArray.length; i++) yFixedArray[i][0] = 0;
          break;
        case 5 : 
          zArray = null;
          for (int i=0; i<zFixedArray.length; i++) zFixedArray[i][0] = 0;
          break;
        default: super.setDefaultValue(_index-POLSET_ROPERTIES_ADDED); break;
      }
      if (isUnderEjs) updatePanel();
    }
    
    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "true";
        case 1 : return "new double[][]{ {0.0,0.0,0.0}, {0.1,0.0,0.0}, {0.1,0.1,0.0} }";
        case 2 : return "-1";
        case 3 :
        case 4 : return "<none>";
        default : return super.getDefaultValueString(_index-POLSET_ROPERTIES_ADDED);
      }
    }

    public Value getValue (int _index) {
      switch (_index) {
        case 0 : return null;
        case 1 : 
          double[][][] data = new double[numElements][][];
          for (int i=0; i<numElements; i++) data[i] = ((ElementPolygon)elements[i]).getData();
          return new ObjectValue(data);
        case 2 : return pointValue;
        case 3 :
        case 4 : return null;
        default: return super.getValue (_index-POLSET_ROPERTIES_ADDED);
      }
    }

} // End of class

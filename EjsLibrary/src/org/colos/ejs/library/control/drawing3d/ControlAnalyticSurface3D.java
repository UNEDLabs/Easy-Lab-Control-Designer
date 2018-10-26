/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) Feb 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementSurface;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 */
public class ControlAnalyticSurface3D extends ControlElement3D implements org.colos.ejs.library.control.swing.NeedsPreUpdate {
  static final private int SURFACE_ROPERTIES_ADDED=14;

  // Configuration variables
  protected String variable1, variable2;
  protected int numPoints1, numPoints2;
  protected double min1 = Double.NaN, max1 = Double.NaN;
  protected double min2 = Double.NaN, max2 = Double.NaN;
  protected String functionX=null, functionY=null, functionZ=null;
  protected boolean useJavaSyntax=true;
//  protected String parameterName=null;
//  protected double[] parameterValues=null;

  // Implementation variables
  protected ElementSurface surface;
  protected boolean changedXfunction, changedYfunction, changedZfunction, updateIndexes;
  protected GeneralParser parserX, parserY, parserZ;
  protected String[] varsX, varsY, varsZ;
  protected int indexX1, indexY1, indexZ1;
  protected int indexX2, indexY2, indexZ2;
  private double minAbcise1=Double.NaN, maxAbcise1=Double.NaN;
  private double minAbcise2=Double.NaN, maxAbcise2=Double.NaN;

  protected Element createElement () { 
    surface = new ElementSurface();
    surface.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _event) { checkExtremes(); }
    });
    surface.setCanBeMeasured (false);

    variable1 = "u";
    variable2 = "v";
    numPoints1 = 20;
    numPoints2 = 20;
    functionX = null; varsX = new String[0]; indexX1 = indexX2 = -1;
    functionY = null; varsY = new String[0]; indexY1 = indexY2 = -1;
    functionZ = null; varsZ = new String[0]; indexZ1 = indexZ2 = -1;
    changedXfunction = changedYfunction = changedZfunction = true;
    return surface; 
  }

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementSurface"; }

  protected int getPropertiesDisplacement () { return SURFACE_ROPERTIES_ADDED; }

  // ------------------------------------------------
  // Definition of Properties
  // ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("variable1");
        infoList.add ("min1");
        infoList.add ("max1");
        infoList.add ("points1");
        infoList.add ("variable2");
        infoList.add ("min2");
        infoList.add ("max2");
        infoList.add ("points2");
        infoList.add ("functionx");
        infoList.add ("functiony");
        infoList.add ("functionz");
        infoList.add ("javaSyntax");
//        infoList.add ("parameterName");
//        infoList.add ("parameterValues");
        infoList.add ("onErrorAction");
        infoList.add ("onSuccessAction");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyInfo(String _property) {
      if (_property.equals("variable1"))  return "String";
      if (_property.equals("variable2"))  return "String";
      if (_property.equals("min1"))       return "int|double";
      if (_property.equals("max1"))       return "int|double";
      if (_property.equals("min2"))       return "int|double";
      if (_property.equals("max2"))       return "int|double";
      if (_property.equals("points1"))    return "int";
      if (_property.equals("points2"))    return "int";
      if (_property.equals("functionx"))  return "String";
      if (_property.equals("functiony"))  return "String";
      if (_property.equals("functionz"))  return "String";
      if (_property.equals("javaSyntax")) return "boolean";

      //      if (_property.equals("parameterName"))   return "String";
      //      if (_property.equals("parameterValues")) return "double[]";
      if (_property.equals("onErrorAction"))   return "Action CONSTANT";
      if (_property.equals("onSuccessAction")) return "Action CONSTANT";


      return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case 0 : if (variable1==null || !variable1.equals(_value.getString()))  { variable1 = _value.getString(); updateIndexes = true; } break;
        case 1 : min1 = _value.getDouble();  checkMeasured(); break;
        case 2 : max1 = _value.getDouble();  checkMeasured(); break;
        case 3 : numPoints1 = _value.getInteger(); break;
        case 4 : if (variable2==null || !variable2.equals(_value.getString())) { variable2 = _value.getString(); updateIndexes = true; } break;
        case 5 : min2 = _value.getDouble(); checkMeasured(); break;
        case 6 : max2 = _value.getDouble(); checkMeasured(); break;
        case 7 : numPoints2 = _value.getInteger(); break;
        case  8 : if (functionX==null || !functionX.equals(_value.getString())) { functionX = _value.getString(); changedXfunction = true; } break;
        case  9 : if (functionY==null || !functionY.equals(_value.getString())) { functionY = _value.getString(); changedYfunction = true; } break;
        case 10 : if (functionZ==null || !functionZ.equals(_value.getString())) { functionZ = _value.getString(); changedZfunction = true; } break;
        case 11 : 
          if (_value.getBoolean()!=useJavaSyntax) {
            useJavaSyntax = _value.getBoolean();
            changedXfunction = changedYfunction = changedZfunction = true;
          }
          break;
//        case 12 : parameterName = _value.getString(); adjustNumberOfPoints(); updateIndexesParam (); break;
//        case 13 :
//          if (_value.getObject() instanceof double[]) {
//            parameterValues = (double[]) _value.getObject();
//            adjustNumberOfPoints();
//          }
//          else parameterValues = null;
//          break;
        case 12 : // onParseError
          removeAction (ACTION_ERROR,getProperty("onErrorAction"));
          addAction(ACTION_ERROR,_value.getString());
          break;
        case 13 : // onParseSuccess
          removeAction (ACTION_SUCCESS,getProperty("onSuccessAction"));
          addAction(ACTION_SUCCESS,_value.getString());
          break;

        default: super.setValue(_index-SURFACE_ROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) {
        preupdate();
        updatePanel();
      }
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : variable1 = "u"; updateIndexes = true; break;
        case 1 : min1 = Double.NaN; surface.setCanBeMeasured (false); break;
        case 2 : max1 = Double.NaN; surface.setCanBeMeasured (false); break;
        case 3 : numPoints1 = 20; break;
        case 4 : variable2 = "v"; updateIndexes = true; break;
        case 5 : min2 = Double.NaN; surface.setCanBeMeasured (false); break;
        case 6 : max2 = Double.NaN; surface.setCanBeMeasured (false); break;
        case 7 : numPoints2 = 20; break;
        case 8 :  functionX = null; varsX = new String[0]; changedXfunction = true; break;
        case 9 :  functionY = null; varsY = new String[0]; changedYfunction = true; break;
        case 10 : functionZ = null; varsZ = new String[0]; changedZfunction = true;  break;
        case 11 : useJavaSyntax = true; changedXfunction = changedYfunction = changedZfunction = true; break;
//        case 12 : parameterName = null; adjustNumberOfPoints(); break;
//        case 13 : parameterValues = null; adjustNumberOfPoints(); break;
        case 12 : removeAction (ACTION_ERROR,getProperty("onErrorAction")); break;
        case 13 : removeAction (ACTION_SUCCESS,getProperty("onSuccessAction")); break;
        default: super.setDefaultValue(_index-SURFACE_ROPERTIES_ADDED); break;
      }
      if (isUnderEjs) {
        preupdate();
        updatePanel();
      }
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "u";
        case 1 :
        case 2 : return "Double.NaN";
        case 3 : return "20";
        case 4 : return "u";
        case 5 :
        case 6 : return "Double.NaN";
        case 7 : return "20";
        case 8 :
        case 9 :
        case 10 : return "<none>"; 
        case 11 :  return "true";
        case 12 :
        case 13 : return "<no_action>";
        default: return super.getDefaultValueString(_index-SURFACE_ROPERTIES_ADDED);
      }
    }
    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : case 3 :
        case 4 : case 5 : case 6 : case 7 :
        case 8 : case 9 : case 10 : case 11 :
        case 12 : case 13 :
          return null;
        default: return super.getValue (_index-SURFACE_ROPERTIES_ADDED);
      }
    }


// -------------------------------------
// Computing the curve
// -------------------------------------

    private boolean isVariableDefined (String varName) {
      if (variable1.equals(varName)) return true;
      if (variable2.equals(varName)) return true;
//      if (parameterName!=null && varName.equals(parameterName)) return true;
      Value value = myGroup.getValue(varName);
      if (value instanceof IntegerValue) return true; // The variable is registered as an integer or double value
      if (value instanceof DoubleValue) return true;
      return false;
    }

    // called by the surface before drawing to make sure the drawing panel has not changed extremes
    private void checkExtremes() {
      if (myParent==null) return;
      DrawingPanel3D panel = myParent.getDrawingPanel3D();
      boolean mustPreupdate = false;
      if (Double.isNaN(min1) || Double.isNaN(max1)) {
        if (Double.isNaN(min1) && (panel.getPreferredMinX()!=minAbcise1)) mustPreupdate = true;
        else if (Double.isNaN(max1) && (panel.getPreferredMaxX()!=maxAbcise1)) mustPreupdate = true;
      }
      if (Double.isNaN(min2) || Double.isNaN(max2)) {
        if (Double.isNaN(min2) && (panel.getPreferredMinY()!=minAbcise2)) mustPreupdate = true;
        else if (Double.isNaN(max2) && (panel.getPreferredMaxY()!=maxAbcise2)) mustPreupdate = true;
      }
      if (mustPreupdate) preupdate();
    }

  private void checkMeasured() {
    surface.setCanBeMeasured(!(Double.isNaN(min1)||Double.isNaN(max1)||Double.isNaN(min2)||Double.isNaN(max2)));
  }

// -------------------------------------
// Update the curve
// -------------------------------------

  public void preupdate() {
    boolean parsedOk=false, errorX=false, errorY=false, errorZ=false;
    if (changedXfunction && functionX!=null) {
      ParserAndVariables pav = new ParserAndVariables(useJavaSyntax,functionX);
      varsX = pav.getVariables();
      parserX = pav.getParser();
      for (int i = 0, n = varsX.length; i < n; i++) {
        if (!isVariableDefined(varsX[i])) { errorX = true; break; } // The variable is not defined in the model
        parserX.defineVariable(i,varsX[i]);
      }
      if (!errorX) {
        parserX.define(functionX);
        parserX.parse();
        if (parserX.hasError()) errorX = true;
        else parsedOk = true;
      }
      changedXfunction = false;
      updateIndexes = true;
    }
    if (changedYfunction && functionY!=null) {
      ParserAndVariables pav = new ParserAndVariables(useJavaSyntax,functionY);
      varsY = pav.getVariables();
      parserY = pav.getParser();
      for (int i = 0, n = varsY.length; i < n; i++) {
        if (!isVariableDefined(varsY[i])) { errorY = true; break; } // The variable is not defined in the model
        parserY.defineVariable(i,varsY[i]);
      }
      if (!errorY) {
        parserY.define(functionY);
        parserY.parse();
        if (parserY.hasError()) errorY = true;
        else parsedOk = true;
      }
      changedYfunction = false;
      updateIndexes = true;
    }
    if (changedZfunction && functionZ!=null) {
      ParserAndVariables pav = new ParserAndVariables(useJavaSyntax,functionZ);
      varsZ = pav.getVariables();
      parserZ = pav.getParser();
      for (int i = 0, n = varsZ.length; i < n; i++) {
        if (!isVariableDefined(varsZ[i])) { errorZ = true; break; } // The variable is not defined in the model
        parserZ.defineVariable(i,varsZ[i]);
      }
      if (!errorZ) {
        parserZ.define(functionZ);
        parserZ.parse();
        if (parserZ.hasError()) errorZ = true;
        else parsedOk = true;
      }
      changedZfunction = false;
      updateIndexes = true;
    }
    if (errorX || errorY || errorZ) invokeActions(ACTION_ERROR);
    else if (parsedOk) invokeActions(ACTION_SUCCESS);
    if (myEjsPropertyEditor!=null) {
      myEjsPropertyEditor.displayErrorOnProperty ("functionx", errorX);
      myEjsPropertyEditor.displayErrorOnProperty ("functiony", errorY);
      myEjsPropertyEditor.displayErrorOnProperty ("functionz", errorZ);
    }

    if (updateIndexes) {
      indexX1 = indexOf (variable1,varsX);
      indexY1 = indexOf (variable1,varsY);
      indexZ1 = indexOf (variable1,varsZ);
      indexX2 = indexOf (variable2,varsX);
      indexY2 = indexOf (variable2,varsY);
      indexZ2 = indexOf (variable2,varsZ);
//      if (parameterName!=null) {
//        indexParameterX = indexOf(parameterName, varsX);
//        indexParameterY = indexOf(parameterName, varsY);
//        indexParameterZ = indexOf(parameterName, varsZ);
//      }
      updateIndexes = false;
    }
//    if (parameterName!=null && parameterValues!=null) {
//      if (numElements!=parameterValues.length) {
//        data = new double[parameterValues.length][numPoints][3];
//        super.setNumberOfElements(parameterValues.length);
//      }
//    }
//    else {
//      if (numElements!=1) {
//        super.setNumberOfElements(1);
//        data = new double[1][numPoints][3];
//      }
//    }
//    if (numPoints!= data[0].length) data = new double[data.length][numPoints][3];
    
    
//    int nPoints = numPoints;
//    if (nPoints<=0) {
//      if (panel!=null) nPoints = Math.max(100,panel.getComponent().getWidth()/2);
//      else nPoints = 100;
//    }
    
    double[][][] data = surface.getData();
    if (data==null || data.length==0 || data.length!=numPoints1 || data[0].length!=numPoints2)  data = new double[numPoints1][numPoints2][3];
    
    minAbcise1 = min1;
    maxAbcise1 = max1;

    minAbcise2 = min2;
    maxAbcise2 = max2;

    if (myParent!=null) {
      DrawingPanel3D panel = myParent.getDrawingPanel3D();
      if (Double.isNaN(minAbcise1)) minAbcise1 = panel.getPreferredMinX();
      if (Double.isNaN(maxAbcise1)) maxAbcise1 = panel.getPreferredMaxX();
      if (Double.isNaN(minAbcise2)) minAbcise2 = panel.getPreferredMinY();
      if (Double.isNaN(maxAbcise2)) maxAbcise2 = panel.getPreferredMaxY();
    }
    
    // Ready to start
    // get the necessary values from the ControlGroup
    if (isUnderEjs) {
      try {
        for (int i = 0, n = varsX.length; i < n; i++) if (i!=indexX1 && i!=indexX2) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
        for (int i = 0, n = varsY.length; i < n; i++) if (i!=indexY1 && i!=indexY2) parserY.setVariable(i, myGroup.getDouble(varsY[i]));
        for (int i = 0, n = varsZ.length; i < n; i++) if (i!=indexZ1 && i!=indexZ2) parserZ.setVariable(i, myGroup.getDouble(varsZ[i]));
      } catch (Exception exc) { // null values can cause a big exception 
        System.err.println ("ControlAnalyticSurface3D: Null String?\n");
        exc.printStackTrace(); 
      }
    }
    else {
      for (int i = 0, n = varsX.length; i < n; i++) if (i!=indexX1 && i!=indexX2) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
      for (int i = 0, n = varsY.length; i < n; i++) if (i!=indexY1 && i!=indexY2) parserY.setVariable(i, myGroup.getDouble(varsY[i]));
      for (int i = 0, n = varsZ.length; i < n; i++) if (i!=indexZ1 && i!=indexZ2) parserZ.setVariable(i, myGroup.getDouble(varsZ[i]));
    }
    for (int i = 0, n = numPoints1 - 1; i <= n; i++) {
      double u = ( (n - i) * minAbcise1 + i * maxAbcise1) / n;
      if (indexX1>=0) parserX.setVariable(indexX1,u);
      if (indexY1>=0) parserY.setVariable(indexY1,u);
      if (indexZ1>=0) parserZ.setVariable(indexZ1,u);
      for (int j=0, m=numPoints2 - 1; j<=m; j++) {
        double v = ((m-j)*minAbcise2 + j*maxAbcise2)/m;
        if (functionX == null || errorX) data[i][j][0] = 0.0;
        else {
          if (indexX2 >= 0) parserX.setVariable(indexX2, v);
          data[i][j][0] = parserX.evaluate();
        }
        if (functionY == null || errorY) data[i][j][1] = 0.0;
        else {
          if (indexY2 >= 0) parserY.setVariable(indexY2, v);
          data[i][j][1] = parserY.evaluate();
        }
        if (functionZ == null || errorZ) data[i][j][2] = 0.0;
        else {
          if (indexZ2 >= 0) parserZ.setVariable(indexZ2, v);
          data[i][j][2] = parserZ.evaluate();
        }
      }
    }
    surface.setData(data);
//    surface.addChange(Element.CHANGE_POSITION);
  }

} // End of class

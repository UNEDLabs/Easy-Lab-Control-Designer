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
import org.opensourcephysics.drawing3d.ElementPolygon;

/**
 * Abstract superclass for display3d Elements (children of ControlElements3DParent)
 * Last revision: Jan 2008
 */
public class ControlAnalyticCurve3D extends ControlElement3D implements org.colos.ejs.library.control.swing.NeedsPreUpdate {
  static final private int CURVE_PROPERTIES_ADDED=10;

  // Configuration variables
  protected String variable;
  protected int numPoints;
  protected double min = Double.NaN, max = Double.NaN;
  protected String functionX, functionY, functionZ;
  protected boolean useJavaSyntax=true;

  // Implementation variables
  protected ElementPolygon polygon;
  protected boolean changedXfunction, changedYfunction, changedZfunction, updateIndexes;
  protected GeneralParser parserX, parserY, parserZ;
  protected String[] varsX, varsY, varsZ;
  protected int indexX, indexY, indexZ;
  private double minAbcise=Double.NaN, maxAbcise=Double.NaN;

  // About paremeters
//    protected String parameterName=null;
//    protected double[] parameterValues=null;
//    protected int indexParameterX, indexParameterY, indexParameterZ;

  protected Element createElement () { 
    polygon = new ElementPolygon();
    polygon.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _event) { checkExtremes(); }
    });
    polygon.setClosed(false);
    polygon.setCanBeMeasured (false);

    variable = "t";
    functionX = null; varsX = new String[0]; indexX = -1;
    functionY = null; varsY = new String[0]; indexY = -1;
    functionZ = null; varsZ = new String[0]; indexZ = -1;
    changedXfunction = changedYfunction = changedZfunction = true;
    return polygon; 
  }

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementPolygon"; }


  protected int getPropertiesDisplacement () { return CURVE_PROPERTIES_ADDED; }

  // ------------------------------------------------
  // Definition of Properties
  // ------------------------------------------------

    static private java.util.List<String> infoList=null;

    public java.util.List<String> getPropertyList() {
      if (infoList==null) {
        infoList = new java.util.ArrayList<String> ();
        infoList.add ("variable");
        infoList.add ("min");
        infoList.add ("max");
        infoList.add ("points");
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
      if (_property.equals("variable"))  return "String";
      if (_property.equals("min"))       return "int|double";
      if (_property.equals("max"))       return "int|double";
      if (_property.equals("points"))    return "int";
      if (_property.equals("functionx")) return "String";
      if (_property.equals("functiony")) return "String";
      if (_property.equals("functionz")) return "String";
      if (_property.equals("javaSyntax"))return "boolean";

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
        case 0 : if (variable==null || !variable.equals(_value.getString()))  { variable = _value.getString(); updateIndexes = true; } break;
        case 1 : min = _value.getDouble(); polygon.setCanBeMeasured (!(Double.isNaN(min)||Double.isNaN(max))); break;
        case 2 : max = _value.getDouble(); polygon.setCanBeMeasured (!(Double.isNaN(min)||Double.isNaN(max))); break;
        case 3 : numPoints = _value.getInteger(); break;
        case 4 : if (functionX==null || !functionX.equals(_value.getString())) { functionX = _value.getString(); changedXfunction = true; } break;
        case 5 : if (functionY==null || !functionY.equals(_value.getString())) { functionY = _value.getString(); changedYfunction = true; } break;
        case 6 : if (functionZ==null || !functionZ.equals(_value.getString())) { functionZ = _value.getString(); changedZfunction = true; } break;
        case 7 : 
          if (_value.getBoolean()!=useJavaSyntax) {
            useJavaSyntax = _value.getBoolean();
            changedXfunction = changedYfunction = changedZfunction = true;
          }
        break;
//        case 8 :
//          if (!_value.getString().equals(parameterName)) {
//            parameterName = _value.getString();
//            changedXfunction = changedYfunction = changedZfunction = updateIndexes = true;
//          }
//          break;
//        case 9 :
//          if (_value.getObject() instanceof double[]) parameterValues = (double[]) _value.getObject();
//          else parameterValues = null;
//          break;
        case 8 : // onParseError
          removeAction (ACTION_ERROR,getProperty("onErrorAction"));
          addAction(ACTION_ERROR,_value.getString());
          break;
        case 9 : // onParseSuccess
          removeAction (ACTION_SUCCESS,getProperty("onSuccessAction"));
          addAction(ACTION_SUCCESS,_value.getString());
          break;
        default: super.setValue(_index-CURVE_PROPERTIES_ADDED,_value); break;
      }
      if (isUnderEjs) {
        preupdate();
        updatePanel();
      }
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : variable = "t"; updateIndexes = true; break;
        case 1 : min = Double.NaN; polygon.setCanBeMeasured (false); break;
        case 2 : max = Double.NaN; polygon.setCanBeMeasured (false); break;
        case 3 : numPoints = 0; break;
        case 4 : functionX = null; varsX = new String[0]; changedXfunction = true; break;
        case 5 : functionY = null; varsY = new String[0]; changedYfunction = true;  break;
        case 6 : functionZ = null; varsZ = new String[0]; changedZfunction = true;  break;
        case 7 : useJavaSyntax = true; changedXfunction = changedYfunction = changedZfunction = true; break;
//        case 8 : parameterName = null; changedXfunction = changedYfunction = changedZfunction = updateIndexes = true; break;
//        case 9 : parameterValues = null; break;
        case 8 : removeAction (ACTION_ERROR,getProperty("onErrorAction")); break;
        case 9 : removeAction (ACTION_SUCCESS,getProperty("onSuccessAction")); break;

        default: super.setDefaultValue(_index-CURVE_PROPERTIES_ADDED); break;
      }
      if (isUnderEjs) {
        preupdate();
        updatePanel();
      }
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "t";
        case 1 :
        case 2 : return "Double.NaN";
        case 3 : return "0";
        case 4 :
        case 5 :
        case 6 : return "<none>"; 
        case 7 :  return "true";
        case 8 :
        case 9 : return "<no_action>";
        default: return super.getDefaultValueString(_index-CURVE_PROPERTIES_ADDED);
      }
    }

    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : case 3 :
        case 4 : case 5 : case 6 : case 7 :
        case 8 : case 9 :
          return null;
        default: return super.getValue (_index-CURVE_PROPERTIES_ADDED);
      }
    }


// -------------------------------------
// Computing the curve
// -------------------------------------

  private boolean isVariableDefined (String varName) {
    if (variable.equals(varName)) return true;
//    if (parameterName!=null && varName.equals(parameterName)) return true;
    Value value = myGroup.getValue(varName);
    if (value instanceof IntegerValue) return true; // The variable is registered as an integer or double value
    if (value instanceof DoubleValue) return true;
    return false;
  }

  // called by the polygon before drawing to make sure the drawing panel has not changed extremes
  private void checkExtremes() {
    if (Double.isNaN(min) || Double.isNaN(max)) {
      if (myParent==null) return;
      DrawingPanel3D panel = myParent.getDrawingPanel3D();
      if (Double.isNaN(min) && (panel.getPreferredMinX()!=minAbcise)) preupdate();
      else if (Double.isNaN(max) && (panel.getPreferredMaxX()!=maxAbcise)) preupdate();
    }
  }

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
      indexX = indexOf (variable,varsX);
      indexY = indexOf (variable,varsY);
      indexZ = indexOf (variable,varsZ);
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
    
    
    int nPoints = numPoints;
    minAbcise = min;
    maxAbcise = max;

    if (myParent!=null) {
      DrawingPanel3D panel = myParent.getDrawingPanel3D();
      if (nPoints<=0) {
        if (panel!=null) nPoints = Math.max(100,panel.getComponent().getWidth()/2);
        else nPoints = 100;
      }
      if (Double.isNaN(minAbcise)) minAbcise = panel.getPreferredMinX();
      if (Double.isNaN(maxAbcise)) maxAbcise = panel.getPreferredMaxX();
    }
    else {
      if (nPoints<=0) nPoints = 100;
    }
    
    if (polygon.getDataArray().length!=nPoints) polygon.setData(new double[nPoints][3]);
    double[][] data = polygon.getDataArray();


    // Ready to start
    // get the necessary values from the ControlGroup
    if (isUnderEjs) {
      try {
        for (int i = 0, n = varsX.length; i < n; i++) if (i != indexX) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
        for (int i = 0, n = varsY.length; i < n; i++) if (i != indexY) parserY.setVariable(i, myGroup.getDouble(varsY[i]));
        for (int i = 0, n = varsZ.length; i < n; i++) if (i != indexZ) parserZ.setVariable(i, myGroup.getDouble(varsZ[i]));
      } catch (Exception exc) { // null values can cause a big exception 
        System.err.println ("ControlAnalyticCurve3D: Null String?\n");
        exc.printStackTrace(); 
      }
    }
    else {
      for (int i = 0, n = varsX.length; i < n; i++) if (i != indexX) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
      for (int i = 0, n = varsY.length; i < n; i++) if (i != indexY) parserY.setVariable(i, myGroup.getDouble(varsY[i]));
      for (int i = 0, n = varsZ.length; i < n; i++) if (i != indexZ) parserZ.setVariable(i, myGroup.getDouble(varsZ[i]));
    }
//    if (parameterName!=null && parameterValues!=null) {
//      for (int curve = 0, nCurves = parameterValues.length; curve < nCurves; curve++) {
//        if (functionX!=null && indexParameterX>=0) parserX.setVariable (indexParameterX,parameterValues[curve]);
//        if (functionY!=null && indexParameterY>=0) parserY.setVariable (indexParameterY,parameterValues[curve]);
//        if (functionZ!=null && indexParameterZ>=0) parserZ.setVariable (indexParameterZ,parameterValues[curve]);
//        for (int i = 0, n = numPoints - 1; i <= n; i++) {
//          double t = ( (n - i) * minAbcise + i * maxAbcise) / n;
//          if (functionX == null || errorX) data[curve][i][0] = 0.0;
//          else {
//            if (indexX >= 0) parserX.setVariable(indexX, t);
//            data[curve][i][0] = parserX.evaluate();
//          }
//          if (functionY == null || errorY) data[curve][i][1] = 0.0;
//          else {
//            if (indexY >= 0) parserY.setVariable(indexY, t);
//            data[curve][i][1] = parserY.evaluate();
//          }
//          if (functionZ == null || errorZ) data[curve][i][2] = 0.0;
//          else {
//            if (indexZ >= 0) parserZ.setVariable(indexZ, t);
//            data[curve][i][2] = parserZ.evaluate();
//          }
//        } // end of for points
//        ((ElementPolygon) elements[curve]).setData(data[curve]); // force polygon to update
//      } // end of for curve
//    }
//    else {
    for (int i = 0, n = nPoints - 1; i <= n; i++) {
      double t = ( (n - i) * minAbcise + i * maxAbcise) / n;
      if (functionX == null || errorX) data[i][0] = 0.0;
      else {
        if (indexX >= 0) parserX.setVariable(indexX, t);
        data[i][0] = parserX.evaluate();
      }
      if (functionY == null || errorY) data[i][1] = 0.0;
      else {
        if (indexY >= 0) parserY.setVariable(indexY, t);
        data[i][1] = parserY.evaluate();
      }
      if (functionZ == null || errorZ) data[i][2] = 0.0;
      else {
        if (indexZ >= 0) parserZ.setVariable(indexZ, t);
        data[i][2] = parserZ.evaluate();
      }
    }

    polygon.addChange(Element.CHANGE_POSITION);
  }

} // End of class

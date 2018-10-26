/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) Feb 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last modified: Dec 2007
 */

package org.colos.ejs.library.control.drawing2d;

import java.awt.event.*;
import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing2d.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;

/**
 * An analytic curve
 */
public class ControlAnalyticCurve2D extends ControlElement2D implements NeedsPreUpdate {
  static final private int ANALCURVE_PROPERTIES_ADDED=9;

  // Configuration variables
  protected String variable;
  protected int numPoints;
  protected double min = Double.NaN, max = Double.NaN;
  protected String functionX, functionY;
  protected boolean useJavaSyntax=true;

  // Implementation variables
  protected ElementPolygon polygon;
  protected boolean changedXfunction, changedYfunction, updateIndexes;
  protected GeneralParser parserX, parserY;
  protected String[] varsX, varsY;
  protected int indexX, indexY;
  private double minAbcise=Double.NaN, maxAbcise=Double.NaN;

  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.ElementPolygon"; }

  protected int getPropertiesDisplacement () { return ANALCURVE_PROPERTIES_ADDED; }

  protected Drawable createDrawable () {
    polygon = new ElementPolygon();
    polygon.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _event) { checkExtremes(); }
    });
    polygon.setClosed(false);
    polygon.setCanBeMeasured (false);
    variable = "t";
    functionX = null; varsX = new String[0]; indexX = -1;
    functionY = null; varsY = new String[0]; indexY = -1;
    changedXfunction = changedYfunction = true;
    return polygon;
  }

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
        infoList.add ("javaSyntax");
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
      if (_property.equals("javaSyntax"))return "boolean";
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
          case 6 :
            if (_value.getBoolean()!=useJavaSyntax) {
              useJavaSyntax = _value.getBoolean();
              changedXfunction = changedYfunction = true;
            }
            break;
          case 7 : // onParseError
            removeAction (ACTION_ERROR,getProperty("onErrorAction"));
            addAction(ACTION_ERROR,_value.getString());
            return;
          case 8 : // onParseSuccess
            removeAction (ACTION_SUCCESS,getProperty("onSuccessAction"));
            addAction(ACTION_SUCCESS,_value.getString());
            return;
          default: super.setValue(_index-ANALCURVE_PROPERTIES_ADDED,_value); break;
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
          case 6 : useJavaSyntax = true; changedXfunction = changedYfunction = true; break;
          case 7 : removeAction (ACTION_ERROR,getProperty("onErrorAction")); return;
          case 8 : removeAction (ACTION_SUCCESS,getProperty("onSuccessAction")); return;
          default: super.setDefaultValue(_index-ANALCURVE_PROPERTIES_ADDED); break;
        }
      }

      public String getDefaultValueString (int _index) {
        switch (_index) {
          case 0 : return "t";
          case 1 :
          case 2 : return "Double.NaN";
          case 3 : return "0";
          case 4 :
          case 5 : return "<none>";
          case 6 : return "true";
          case 7 :
          case 8 : return "<no_action>";
          default: return super.getDefaultValueString(_index-ANALCURVE_PROPERTIES_ADDED);
        }
      }

      public Value getValue (int _index) {
        switch (_index) {
          case 0 : case 1 : case 2 : case 3 :
          case 4 : case 5 : case 6 : case 7 :
          case 8 : 
            return null;
          default: return super.getValue (_index-ANALCURVE_PROPERTIES_ADDED);
        }
      }

// -------------------------------------
// Update the curve
// -------------------------------------

  private boolean isVariableDefined (String varName) {
    if (variable.equals(varName)) return true;
    Value value = myGroup.getValue(varName);
    if (value instanceof IntegerValue) return true; // The variable is registered as an integer or double value
    if (value instanceof DoubleValue) return true;
    return false;
  }

  // called by the polygon before drawing to make sure the drawing panel has nto changed extremes
  private void checkExtremes() {
    if (Double.isNaN(min) || Double.isNaN(max)) {
      if (myParent==null) return;
      DrawingPanel panel = myParent.getDrawingPanel();
      if (Double.isNaN(min) && (panel.getXMin()!=minAbcise)) preupdate();
      else if (Double.isNaN(max) && (panel.getXMax()!=maxAbcise)) preupdate();
    }
  }
  
  public void preupdate() {
    boolean parsedOk=false, errorX=false, errorY=false;
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
    if (errorX || errorY) invokeActions (ACTION_ERROR);
    else if (parsedOk) invokeActions (ACTION_SUCCESS);
    if (isUnderEjs) {
      myEjsPropertyEditor.displayErrorOnProperty ("functionx", errorX);
      myEjsPropertyEditor.displayErrorOnProperty ("functiony", errorY);
    }
    if (updateIndexes) {
      indexX = indexOf (variable,varsX);
      indexY = indexOf (variable,varsY);
      updateIndexes = false;
    }
    DrawingPanel panel = myParent.getDrawingPanel();
    int nPoints = numPoints;
    if (nPoints<=0) {
      if (panel!=null) nPoints = Math.max(100,panel.getWidth()/2);
      else nPoints = 100;
    }
    if (polygon.getDataArray().length!=nPoints) polygon.setData(new double[nPoints][2]);
    double[][] data = polygon.getDataArray();

    minAbcise = min;
    maxAbcise = max;
//    System.out.println ("Min = "+min+", Panel min = "+panel.getXMin());
//    System.out.println ("Max = "+max+", Panel max = "+panel.getXMax());
    if (Double.isNaN(minAbcise)) minAbcise = panel.getXMin();
    if (Double.isNaN(maxAbcise)) maxAbcise = panel.getXMax();
//    System.out.println ("min = "+minAbcise);
//    System.out.println ("max = "+maxAbcise);

    // Ready to start
    // get the necessary values from the ControlGroup
    if (isUnderEjs) {
      try {
        for (int i = 0, n = varsX.length; i < n; i++) if (i != indexX) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
        for (int i = 0, n = varsY.length; i < n; i++) if (i != indexY) parserY.setVariable(i, myGroup.getDouble(varsY[i]));
      } catch (Exception exc) { // null values can cause a big exception 
        System.err.println ("ControlAnalyticCurve2D: Null String?\n");
        exc.printStackTrace(); 
      }
    }
    else {
      for (int i = 0, n = varsX.length; i < n; i++) if (i != indexX) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
      for (int i = 0, n = varsY.length; i < n; i++) if (i != indexY) parserY.setVariable(i, myGroup.getDouble(varsY[i]));
    }
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
    }
    polygon.setElementChanged(); // force polygon to update
  }

} // End of class

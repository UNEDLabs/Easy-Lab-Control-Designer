/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) Feb 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last modified: Dec 2007
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;

/**
 * An analytic curve
 */
public class ControlAnalyticCurve extends ControlInteractiveElement implements NeedsPreUpdate {
  static protected final int CURVE_ADDED = 12;
  static protected final int MY_LINE_COLOR = SECONDARY_COLOR+CURVE_ADDED;

  // Configuration variables
  protected InteractivePoligon poligon;
  protected String variable;
  protected int numPoints;
  protected double min = Double.NaN, max = Double.NaN;
  protected String functionX, functionY, functionZ;
  protected boolean useJavaSyntax=true;
  protected String parameterName=null;
  protected double[] parameterValues=null;

  // Implementation variables
  protected boolean changedXfunction, changedYfunction, changedZfunction, updateIndexes;
  protected GeneralParser parserX, parserY, parserZ;
  protected String[] varsX, varsY, varsZ;
  protected int indexX, indexY, indexZ;
  protected int indexParameterX, indexParameterY, indexParameterZ;

  public ControlAnalyticCurve () { super (); enabledEjsEdit = true; }

  protected void setName (String _name) { poligon.setName(_name); } // To be overwritten

  protected Drawable createDrawable () {
    poligon = new InteractivePoligon();
    poligon.setNumberOfPoints(numPoints=0);
//    polygon.setEnabled(false);
    poligon.setClosed(false);
    poligon.setAllowTable(true);
    variable = "t";
    functionX = null; varsX = new String[0]; indexX = -1;
    functionY = null; varsY = new String[0]; indexY = -1;
    functionZ = null; varsZ = new String[0]; indexZ = -1;
    changedXfunction = changedYfunction = changedZfunction = true;
    poligon.canBeMeasured (false);
    return poligon;
  }

  protected int getPropertiesDisplacement () { return CURVE_ADDED; }

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
        infoList.add ("parameterName");
        infoList.add ("parameterValues");
        infoList.add ("onErrorAction");
        infoList.add ("onSuccessAction");
        infoList.addAll(super.getPropertyList());
      }
      return infoList;
    }

    public String getPropertyCommonName(String _property) {
      if (_property.equals("color")) return "lineColor";
      if (_property.equals("secondaryColor")) return "fillColor";
      return super.getPropertyCommonName(_property);
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
      if (_property.equals("parameterName"))   return "String";
      if (_property.equals("parameterValues")) return "double[]";
      if (_property.equals("onErrorAction"))   return "Action CONSTANT";
      if (_property.equals("onSuccessAction")) return "Action CONSTANT";
      return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

      public void setValue (int _index, Value _value) {
        switch (_index) {
          case 0 : if (!_value.getString().equals(variable))  { variable = _value.getString(); updateIndexes = true; } break;
          case 1 : min = _value.getDouble(); poligon.canBeMeasured (!Double.isNaN(max)); break;
          case 2 : max = _value.getDouble(); poligon.canBeMeasured (!Double.isNaN(min)); break;
          case 3 : numPoints = _value.getInteger(); break;
          case 4 : if (!_value.getString().equals(functionX)) { functionX = _value.getString(); changedXfunction = true; } break;
          case 5 : if (!_value.getString().equals(functionY)) { functionY = _value.getString(); changedYfunction = true; } break;
          case 6 : if (!_value.getString().equals(functionZ)) { functionZ = _value.getString(); changedZfunction = true; } break;
          case 7 :
            if (_value.getBoolean()!=useJavaSyntax) {
              useJavaSyntax = _value.getBoolean();
              changedXfunction = changedYfunction = changedZfunction = true;
            }
            break;
          case 8 :
            if (!_value.getString().equals(parameterName)) {
              parameterName = _value.getString();
              changedXfunction = changedYfunction = changedZfunction = updateIndexes = true;
            }
            break;
          case 9 :
            if (_value.getObject() instanceof double[]) parameterValues = (double[]) _value.getObject();
            else parameterValues = null;
            break;
          case 10 : // onParseError
            removeAction (ACTION_ERROR,getProperty("onErrorAction"));
            addAction(ACTION_ERROR,_value.getString());
            return;
          case 11 : // onParseSuccess
            removeAction (ACTION_SUCCESS,getProperty("onSuccessAction"));
            addAction(ACTION_SUCCESS,_value.getString());
            return;
          default: super.setValue(_index-CURVE_ADDED,_value); break;
          case MY_LINE_COLOR : super.setValue(PRIMARY_COLOR,_value) ; break;
        }
      }

      public void setDefaultValue (int _index) {
        switch (_index) {
          case 0 : variable = "t"; updateIndexes = true; break;
          case 1 : min = Double.NaN; poligon.canBeMeasured (false); break;
          case 2 : max = Double.NaN; poligon.canBeMeasured (false); break;
          case 3 : numPoints = 0; break;
          case 4 : functionX = null; varsX = new String[0]; changedXfunction = true; break;
          case 5 : functionY = null; varsY = new String[0]; changedYfunction = true;  break;
          case 6 : functionZ = null; varsZ = new String[0]; changedZfunction = true;  break;
          case 7 : useJavaSyntax = true; changedXfunction = changedYfunction = changedZfunction = true; break;
          case 8 : parameterName = null; changedXfunction = changedYfunction = changedZfunction = updateIndexes = true; break;
          case 9 : parameterValues = null; break;
          case 10 : removeAction (ACTION_ERROR,getProperty("onErrorAction")); return;
          case 11 : removeAction (ACTION_SUCCESS,getProperty("onSuccessAction")); return;
          default: super.setDefaultValue(_index-CURVE_ADDED); break;
          case MY_LINE_COLOR : super.setDefaultValue(PRIMARY_COLOR) ; break;
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
          case 7 : return "true";
          case 8 :
          case 9 : return "<none>";
          case 10 :
          case 11 : return "<no_action>";
          default: return super.getDefaultValueString(_index-CURVE_ADDED);
          case MY_LINE_COLOR : return super.getDefaultValueString(PRIMARY_COLOR) ;
        }
      }

      public Value getValue (int _index) {
        switch (_index) {
          case 0 : case 1 : case 2 : case 3 :
          case 4 : case 5 : case 6 : case 7 :
          case 8 : case 9 : case 10 : case 11 :
            return null;
          default: return super.getValue (_index-CURVE_ADDED);
        }
      }

// -------------------------------------
// Update the curve
// -------------------------------------

  private boolean isVariableDefined (String varName) {
    if (variable.equals(varName)) return true;
    if (parameterName!=null && varName.equals(parameterName)) return true;
    Value value = myGroup.getValue(varName);
    if (value instanceof IntegerValue) return true; // The variable is registered as an integer or double value
    if (value instanceof DoubleValue) return true;
    return false;
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
    if (errorX || errorY || errorZ) invokeActions (ACTION_ERROR);
    else if (parsedOk) invokeActions (ACTION_SUCCESS);
    if (isUnderEjs) {
      myEjsPropertyEditor.displayErrorOnProperty ("functionx", errorX);
      myEjsPropertyEditor.displayErrorOnProperty ("functiony", errorY);
      myEjsPropertyEditor.displayErrorOnProperty ("functionz", errorZ);
    }

    if (updateIndexes) {
      indexX = indexOf (variable,varsX);
      indexY = indexOf (variable,varsY);
      indexZ = indexOf (variable,varsZ);
      if (parameterName!=null) {
        indexParameterX = indexOf(parameterName, varsX);
        indexParameterY = indexOf(parameterName, varsY);
        indexParameterZ = indexOf(parameterName, varsZ);
      }
      updateIndexes = false;
    }
    DrawingPanel panel = myParent.getDrawingPanel();
    int nPoints = numPoints;
    if (nPoints<=0) {
      if (panel!=null) nPoints = Math.max(100,panel.getWidth()/2);
      else nPoints = 100;
    }
    if (parameterName!=null && parameterValues!=null) {
      int nFinal = nPoints*parameterValues.length;
      if (poligon.getNumberOfPoints()!=nFinal) {
        poligon.setNumberOfPoints(nFinal);
        for (int i=1,n=parameterValues.length; i<n; i++) poligon.setConnected(i*nPoints-1,false);
      }
    }
    else if (poligon.getNumberOfPoints()!=nPoints) poligon.setNumberOfPoints(nPoints);
    double[][] data = poligon.getData();

    double minAbcise = min, maxAbcise = max;
    if (panel!=null) {
      if (Double.isNaN(minAbcise)) minAbcise = panel.getXMin();
      if (Double.isNaN(maxAbcise)) maxAbcise = panel.getXMax();
    }
//    System.out.println ("min = "+min);
//    System.out.println ("min abscise= "+minAbcise);
//    System.out.println ("panel xmin = "+panel.getXMin());
    
    // Ready to start
    // get the necessary values from the ControlGroup
    for (int i = 0, n = varsX.length; i < n; i++) if (i != indexX) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
    for (int i = 0, n = varsY.length; i < n; i++) if (i != indexY) parserY.setVariable(i, myGroup.getDouble(varsY[i]));
    for (int i = 0, n = varsZ.length; i < n; i++) if (i != indexZ) parserZ.setVariable(i, myGroup.getDouble(varsZ[i]));
    if (parameterName!=null && parameterValues!=null) {
      for (int curve = 0, nCurves = parameterValues.length; curve < nCurves; curve++) {
        if (functionX!=null && indexParameterX>=0) parserX.setVariable (indexParameterX,parameterValues[curve]);
        if (functionY!=null && indexParameterY>=0) parserY.setVariable (indexParameterY,parameterValues[curve]);
        if (functionZ!=null && indexParameterZ>=0) parserZ.setVariable (indexParameterZ,parameterValues[curve]);
        for (int i = 0, n = nPoints - 1; i <= n; i++) {
          double t = ( (n - i) * minAbcise + i * maxAbcise) / n;
          int index = i+curve*nPoints;
          if (functionX == null || errorX) data[0][index] = 0.0;
          else {
            if (indexX >= 0) parserX.setVariable(indexX, t);
            data[0][index] = parserX.evaluate();
          }
          if (functionY == null || errorY) data[1][index] = 0.0;
          else {
            if (indexY >= 0) parserY.setVariable(indexY, t);
            data[1][index] = parserY.evaluate();
          }
          if (functionZ == null || errorZ) data[2][index] = 0.0;
          else {
            if (indexZ >= 0) parserZ.setVariable(indexZ, t);
            data[2][index] = parserZ.evaluate();
          }
        } // end of for points
      } // end of for curve
    }
    else {
      for (int i = 0, n = nPoints - 1; i <= n; i++) {
        double t = ( (n - i) * minAbcise + i * maxAbcise) / n;
        if (functionX == null || errorX) data[0][i] = 0.0;
        else {
          if (indexX >= 0) parserX.setVariable(indexX, t);
          data[0][i] = parserX.evaluate();
        }
        if (functionY == null || errorY) data[1][i] = 0.0;
        else {
          if (indexY >= 0) parserY.setVariable(indexY, t);
          data[1][i] = parserY.evaluate();
        }
        if (functionZ == null || errorZ) data[2][i] = 0.0;
        else {
          if (indexZ >= 0) parserZ.setVariable(indexZ, t);
          data[2][i] = parserZ.evaluate();
        }
      }
    }
    poligon.needsToProject(null); // force polygon to update
  }

} // End of class

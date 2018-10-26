/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;

/**
 * A set of arrows that implements a simpler 2D vector field
 */
public class ControlAnalyticScalarField extends ControlScalarField {
  static protected final int ANALYTIC_SCALAR_FIELD_ADDED=8;
  static public final int CARTESIAN=0;
  static public final int POLAR=1;

  static protected final int ANALYTIC_DATA=DATA+ANALYTIC_SCALAR_FIELD_ADDED;

  // Configuration variables
  protected int nx, ny, coordinatesMode;
  protected String variableX = "x", variableY = "y";
  protected boolean useJavaSyntax, changedDatafunction, updateIndexes;
  protected String functionData;
  protected GeneralParser parserData;
  protected String[] varsData;
  protected int indexOfxInDataStr, indexOfyInDataStr;

  @Override
  protected Drawable createDrawable () {
    Drawable drawable = super.createDrawable();
//    minX = maxX = minY = maxY = Double.NaN;
    nx = ny = 0;
    changedDatafunction = updateIndexes = true;
    parserData = null;
    functionData = null; varsData = new String[0]; indexOfxInDataStr = indexOfyInDataStr = -1;
    useJavaSyntax = false;
    coordinatesMode = CARTESIAN;
    return drawable;
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("points1");
      infoList.add ("points2");
      infoList.add ("variable1");
      infoList.add ("variable2");
      infoList.add ("onErrorAction");
      infoList.add ("onSuccessAction");
      infoList.add ("javaSyntax");
      infoList.add ("coordinateSystem");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("minimumX")) return "min1";
    if (_property.equals("maximumX")) return "max1";
    if (_property.equals("minimumY")) return "min2";
    if (_property.equals("maximumY")) return "max2";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("z"))             return "String";

    if (_property.equals("points1"))    return "int";
    if (_property.equals("points2"))    return "int";

    if (_property.equals("variable1"))  return "String";
    if (_property.equals("variable2"))  return "String";

    if (_property.equals("onErrorAction"))   return "Action CONSTANT";
    if (_property.equals("onSuccessAction")) return "Action CONSTANT";
    if (_property.equals("javaSyntax")) return "boolean";
    if (_property.equals("coordinateSystem"))  return "CoordinateSystem|int";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("CoordinateSystem")>=0) {
      _value = _value.trim().toLowerCase();
      if      (_value.equals("polar"))     return new IntegerValue(POLAR);
      else if (_value.equals("cartesian")) return new IntegerValue(CARTESIAN);
    }
    return super.parseConstant(_propertyType, _value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : nx = _value.getInteger(); break;
      case 1 : ny = _value.getInteger(); break;

      case 2 :
        if (!_value.getString().equals(variableX)) { variableX = _value.getString(); updateIndexes = true; }
       break;
      case 3 :
        if (!_value.getString().equals(variableY)) { variableY = _value.getString(); updateIndexes = true; }
       break;

     case 4 : // onParseError
       removeAction (ACTION_ERROR,getProperty("onErrorAction"));
       addAction(ACTION_ERROR,_value.getString());
       return;
     case 5 : // onParseSuccess
       removeAction (ACTION_SUCCESS,getProperty("onSuccessAction"));
       addAction(ACTION_SUCCESS,_value.getString());
       return;
     case 6 :
       if (_value.getBoolean()!=useJavaSyntax) {
         useJavaSyntax = _value.getBoolean();
         changedDatafunction = true;
       }
       break;
     case 7 :
       if (_value.getInteger()!=coordinatesMode) {
         coordinatesMode = _value.getInteger();
         changedDatafunction = true;
       }
       break;

      case ANALYTIC_DATA :
          if (!_value.getString().equals(functionData)) { functionData = _value.getString(); changedDatafunction= true; }
          break;

      default : super.setValue(_index-ANALYTIC_SCALAR_FIELD_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : nx = 0; break;
        case 1 : ny = 0; break;
        case 2 : variableX = "x"; updateIndexes = true; break;
        case 3 : variableY = "y"; updateIndexes = true; break;
        case 4 : removeAction (ACTION_ERROR,getProperty("onErrorAction")); return;
        case 5 : removeAction (ACTION_SUCCESS,getProperty("onSuccessAction")); return;
        case 6 :
          useJavaSyntax = false;
          changedDatafunction = true;
          break;
        case 7 :
          coordinatesMode = CARTESIAN;
          changedDatafunction = true;
          break;

        case ANALYTIC_DATA : functionData = null; changedDatafunction= true; break;

        default: super.setDefaultValue(_index-ANALYTIC_SCALAR_FIELD_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 :
      case 1 : return "0";
      case 2 : return "x";
      case 3 : return "y";
      case 4 :
      case 5 : return "<no_action>";
      case 6 : return "false";
      case 7 : return "CARTESIAN";

      case ANALYTIC_DATA : return "<none>";
      default : return super.getDefaultValueString(_index-ANALYTIC_SCALAR_FIELD_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
      return null;
      default: return super.getValue (_index-ANALYTIC_SCALAR_FIELD_ADDED);
    }
  }

  // ------------------------------------------------
  // Preupdating and convenience methods
  // ------------------------------------------------

  boolean isVariableDefined (String varName) {
    if (variableX.equals(varName)) return true;
    if (variableY.equals(varName)) return true;
    Value value = myGroup.getValue(varName);
    if (value instanceof IntegerValue) return true; // The variable is registered as an integer or double value
    if (value instanceof DoubleValue) return true;
    return false;
  }

  public void preupdate () {
    if (!visibility) return;
    if (myParent==null) return;
    DrawingPanel panel = myParent.getDrawingPanel();
    int nxPoints = nx, nyPoints=ny;
    if (nxPoints<=0) {
      if (panel!=null) nxPoints = Math.max(20,Math.min(60,panel.getWidth()/20));
      else nxPoints = 20;
    }
    if (nyPoints<=0) {
      if (panel!=null) nyPoints = Math.max(20,Math.min(60,panel.getHeight()/20));
      else nyPoints = 20;
    }
    if (nx<=0 || ny<=0) nxPoints = nyPoints = Math.min(nxPoints,nyPoints);

    if (dataArray==null || nxPoints!=dataArray.length || nyPoints!=dataArray[0].length) dataArray = new double[nxPoints][nyPoints];

    boolean parsedOk = false, errorData=false;
    if (changedDatafunction && functionData!=null) {
      ParserAndVariables pav = new ParserAndVariables(useJavaSyntax,functionData);
      varsData = pav.getVariables();
      parserData = pav.getParser();
      for (int i = 0, n = varsData.length; i < n; i++) {
        if (!isVariableDefined(varsData[i])) { errorData = true; break; } // The variable is not defined in the model
        parserData.defineVariable(i,varsData[i]);
      }
      if (!errorData) {
        parserData.define(functionData);
        parserData.parse();
        if (parserData.hasError()) errorData = true;
        else parsedOk = true;
      }
      changedDatafunction = false;
      updateIndexes = true;
    }
    if (errorData) invokeActions (ControlElement.ACTION_ERROR);
    else if (parsedOk) invokeActions (ControlElement.ACTION_SUCCESS);
    if (isUnderEjs) {
      myEjsPropertyEditor.displayErrorOnProperty ("z", errorData);
    }
    if (updateIndexes) {
        indexOfxInDataStr = ControlElement.indexOf (variableX,varsData);
        indexOfyInDataStr = ControlElement.indexOf (variableY,varsData);
    }

    // Analytic elements must be recomputed every time because a parameter in the expression may have
    // changed even if all the rest is unchanged
    super.computeMinMax();
    double dx = (maxAbcise-minAbcise)/(nxPoints-1), dy = (maxOrdinate-minOrdinate)/(nyPoints-1);

    if (functionData==null || errorData) for (int i = 0; i < nxPoints; i++) for (int j = 0; j < nyPoints; j++) dataArray[i][j] = 0.0;
    else {
      // Prepare the parser
      for (int i = 0, n = varsData.length; i < n; i++)
        if (i!=indexOfxInDataStr && i!=indexOfyInDataStr) parserData.setVariable(i, myGroup.getDouble(varsData[i]));
      double x = minAbcise;
      switch (coordinatesMode) {
        case POLAR :
          for (int i=0; i<nxPoints; i++, x+=dx) {
            double y = minOrdinate;
            for (int j = 0; j < nyPoints; j++, y+=dy) {
              double r = Math.sqrt(x*x+y*y);
              double angle = Math.atan2(y,x);
              if (indexOfxInDataStr >= 0) parserData.setVariable(indexOfxInDataStr, r);
              if (indexOfyInDataStr >= 0) parserData.setVariable(indexOfyInDataStr, angle);
              dataArray[i][j] = parserData.evaluate();
            }
          }
          break;
      default :
      case CARTESIAN:
        for (int i=0; i<nxPoints; i++, x+=dx) {
          double y = minOrdinate;
          for (int j = 0; j < nyPoints; j++, y+=dy) {
            if (indexOfxInDataStr >= 0) parserData.setVariable(indexOfxInDataStr, x);
            if (indexOfyInDataStr >= 0) parserData.setVariable(indexOfyInDataStr, y);
            dataArray[i][j] = parserData.evaluate();
          }
        }
        break;
      }
    }

    mustUpdate = true;
    commonPreupdate();
  }

} // End of class

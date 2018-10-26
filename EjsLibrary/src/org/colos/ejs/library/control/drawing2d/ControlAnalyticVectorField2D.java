/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;

/**
 * An analytic vector field in 2D
 */
public class ControlAnalyticVectorField2D extends ControlVectorField2D implements NeedsPreUpdate {
  static protected final int ANALYTIC_VECTORFIELD_ADDED=7;

  static protected final int ANALYTIC_X_COMPONENT=X_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_Y_COMPONENT=Y_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_ANGLE_COMPONENT=ANGLE_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_MAGNITUDE=MAGNITUDE+ANALYTIC_VECTORFIELD_ADDED;

  // Configuration variables
  protected boolean useJavaSyntax;
  protected int nx, ny;
  protected String variableX = "x", variableY = "y";
  protected String functionX, functionY, functionAngle, functionMag;

  // Implementation variables
  protected boolean changedX, changedY, changedAngle, changedMag, updateIndexes;
  protected GeneralParser parserX, parserY, parserAngle, parserMag;
  protected String[] varsX, varsY, varsAngle, varsMag;
  protected int indexOfxInXStr, indexOfyInXStr;
  protected int indexOfxInYStr, indexOfyInYStr;
  protected int indexOfxInAngleStr, indexOfyInAngleStr;
  protected int indexOfxInMagStr, indexOfyInMagStr;
  private double[][] xArray=null, yArray=null, angleArray=null, magArray=null;

  @Override
  protected org.opensourcephysics.display.Drawable createDrawable () {
    nx = ny = 20;
    changedX = changedY = true;
    changedAngle = true;
    changedMag = true;
    updateIndexes = true;
    parserX = parserY = parserAngle = parserMag = null;
    functionX = null; varsX = new String[0]; indexOfxInXStr = indexOfyInXStr = -1;
    functionY = null; varsY = new String[0]; indexOfxInYStr = indexOfyInYStr = -1;
    functionAngle = null; varsAngle = new String[0]; indexOfxInAngleStr = indexOfyInAngleStr = -1;
    functionMag = null; varsMag = new String[0]; indexOfxInMagStr = indexOfyInMagStr = -1;
    useJavaSyntax = false;
    return super.createDrawable();
  }

  @Override
  protected int getPropertiesDisplacement () { return ANALYTIC_VECTORFIELD_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

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
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    
    if (_property.equals("points1"))    return "int";
    if (_property.equals("points2"))    return "int";

    if (_property.equals("variable1"))  return "String";
    if (_property.equals("variable2"))  return "String";

    if (_property.equals("onErrorAction"))   return "Action CONSTANT";
    if (_property.equals("onSuccessAction")) return "Action CONSTANT";
    if (_property.equals("javaSyntax"))return "boolean";

    // Super's changed
    if (_property.equals("xcomponent")) return "String";
    if (_property.equals("ycomponent")) return "String";
    if (_property.equals("angles"))     return "String";
    if (_property.equals("magnitude"))  return "String";
    
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : nx = _value.getInteger(); break;
      case 1 : ny = _value.getInteger(); break;
      case 2 : if (variableX==null || !variableX.equals(_value.getString())) { variableX = _value.getString(); updateIndexes = true; } break;
      case 3 : if (variableY==null || !variableY.equals(_value.getString())) { variableY = _value.getString(); updateIndexes = true; } break;

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
          changedX = changedY = true;
          changedAngle = true;
          changedMag = true;
        }
        break;

      default : super.setValue(_index-ANALYTIC_VECTORFIELD_ADDED,_value); break;

      case ANALYTIC_X_COMPONENT :     if (functionX==null || !functionX.equals(_value.getString())) { functionX = _value.getString(); changedX = true; } break;
      case ANALYTIC_Y_COMPONENT :     if (functionY==null || !functionY.equals(_value.getString())) { functionY = _value.getString(); changedY = true; } break;
      case ANALYTIC_ANGLE_COMPONENT : if (functionAngle==null || !functionAngle.equals(_value.getString())) { functionAngle = _value.getString(); changedAngle = true; } break;
      case ANALYTIC_MAGNITUDE :       if (functionMag==null || !functionMag.equals(_value.getString())) { functionMag = _value.getString(); changedMag = true; } break;

    }
    if (isUnderEjs) preupdate();

  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : nx = 20; break;
      case 1 : ny = 20; break;
      case 2 : variableX = "x"; updateIndexes = true; break;
      case 3 : variableY = "y"; updateIndexes = true; break;
      case 4 : removeAction (ACTION_ERROR,getProperty("onErrorAction")); return;
      case 5 : removeAction (ACTION_SUCCESS,getProperty("onSuccessAction")); return;
      case 6 :
        useJavaSyntax = false;
        changedX = changedY = true;
        changedAngle = true;
        changedMag = true;
        break;

      default: super.setDefaultValue(_index-ANALYTIC_VECTORFIELD_ADDED); break;

      case ANALYTIC_X_COMPONENT : functionX = null; changedX= true; break;
      case ANALYTIC_Y_COMPONENT : functionY = null; changedY= true; break;
      case ANALYTIC_ANGLE_COMPONENT : functionAngle = null; changedAngle= true; break;
      case ANALYTIC_MAGNITUDE : functionMag = null; changedMag=true; break;

    }
    if (isUnderEjs) preupdate();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 :
      case 1 : return "20";
      case 2 : return "x";
      case 3 : return "y";
      case 4 :
      case 5 : return "<no_action>";
      case 6 : return "false";

      default : return super.getDefaultValueString(_index-ANALYTIC_VECTORFIELD_ADDED);

      case ANALYTIC_X_COMPONENT :
      case ANALYTIC_Y_COMPONENT :
      case ANALYTIC_ANGLE_COMPONENT :
      case ANALYTIC_MAGNITUDE : return "<none>";

    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 : case 8 :
      return null;
      default: return super.getValue (_index-ANALYTIC_VECTORFIELD_ADDED);
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
    if (!field.isVisible()) return; // save time
    if (nx<1 || ny<1) return;
    if (myParent==null) return;
    boolean parsedOk = false, errorX=false, errorY=false, errorAngle=false, errorMag=false;
    if (changedX && functionX!=null) {
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
      changedX = false;
      updateIndexes = true;
    }
    if (changedY && functionY!=null) {
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
      changedY = false;
      updateIndexes = true;
    }
    if (changedAngle && functionAngle!=null) {
      ParserAndVariables pav = new ParserAndVariables(useJavaSyntax,functionAngle);
      varsAngle = pav.getVariables();
      parserAngle = pav.getParser();
      for (int i = 0, n = varsAngle.length; i < n; i++) {
        if (!isVariableDefined(varsAngle[i])) { errorAngle = true; break; } // The variable is not defined in the model
        parserAngle.defineVariable(i,varsAngle[i]);
      }
      if (!errorAngle) {
        parserAngle.define(functionAngle);
        parserAngle.parse();
        if (parserAngle.hasError()) errorAngle = true;
        else parsedOk = true;
      }
      changedAngle = false;
      updateIndexes = true;
    }
    if (changedMag && functionMag!=null) {
      ParserAndVariables pav = new ParserAndVariables(useJavaSyntax,functionMag);
      varsMag = pav.getVariables();
      parserMag = pav.getParser();
      for (int i = 0, n = varsMag.length; i < n; i++) {
        if (!isVariableDefined(varsMag[i])) { errorMag = true; break; } // The variable is not defined in the model
        parserMag.defineVariable(i,varsMag[i]);
      }
      if (!errorMag) {
        parserMag.define(functionMag);
        parserMag.parse();
        if (parserMag.hasError()) errorMag = true;
        else parsedOk = true;
      }
      changedMag = false;
      updateIndexes = true;
    }

    if (errorX || errorY || errorAngle || errorMag) invokeActions (ControlElement.ACTION_ERROR);
    else if (parsedOk) invokeActions (ControlElement.ACTION_SUCCESS);
    if (isUnderEjs) {
      myEjsPropertyEditor.displayErrorOnProperty ("xcomponent", errorX);
      myEjsPropertyEditor.displayErrorOnProperty ("ycomponent", errorY);
      myEjsPropertyEditor.displayErrorOnProperty ("angles", errorAngle);
      myEjsPropertyEditor.displayErrorOnProperty ("magnitude", errorMag);
    }
    
    if (updateIndexes) {
        indexOfxInXStr = ControlElement.indexOf (variableX,varsX);
        indexOfyInXStr = ControlElement.indexOf (variableY,varsX);
        indexOfxInYStr = ControlElement.indexOf (variableX,varsY);
        indexOfyInYStr = ControlElement.indexOf (variableY,varsY);
        indexOfxInAngleStr = ControlElement.indexOf (variableX,varsAngle);
        indexOfyInAngleStr = ControlElement.indexOf (variableY,varsAngle);
        indexOfxInMagStr = ControlElement.indexOf (variableX,varsMag);
        indexOfyInMagStr = ControlElement.indexOf (variableY,varsMag);
    }

    // Analytic elements must be recomputed every time because a parameter in the expression may have
    // changed even if all the rest is unchanged

    if (functionX!=null || functionY!=null || functionAngle==null || errorAngle) { // use the (dx,dy,dz) format
      // Prepare the parser
      for (int i = 0, n = varsX.length; i < n; i++)
        if (i!=indexOfxInXStr && i!=indexOfyInXStr) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
      for (int i = 0, n = varsY.length; i < n; i++)
        if (i!=indexOfxInYStr && i!=indexOfyInYStr) parserY.setVariable(i, myGroup.getDouble(varsY[i]));

      boolean useX = (functionX!=null && !errorX); 
      boolean useY = (functionY!=null && !errorY); 
      
      // Check Arrays
      if (useX) {
        if (xArray==null || xArray.length!=nx || xArray[0].length!=ny) xArray = new double[nx][ny];
      }
      if (useY) {
        if (yArray==null || yArray.length!=nx || yArray[0].length!=ny) yArray = new double[nx][ny];
      }
      // Compute values
      for (int i=0; i<nx; i++) {
        double x = field.indexToX(i);
        if (useX && indexOfxInXStr >= 0) parserX.setVariable(indexOfxInXStr, x);
        if (useY && indexOfxInYStr >= 0) parserY.setVariable(indexOfxInYStr, x);
        for (int j = 0; j < ny; j++) {
          double y = field.indexToY(j);
          if (useX) {
//            if (indexOfxInXStr >= 0) parserX.setVariable(indexOfxInXStr, x);
            if (indexOfyInXStr >= 0) parserX.setVariable(indexOfyInXStr, y);
            xArray[i][j] = parserX.evaluate();
          }
          if (useY) {
//            if (indexOfxInYStr >= 0) parserY.setVariable(indexOfxInYStr, x);
            if (indexOfyInYStr >= 0) parserY.setVariable(indexOfyInYStr, y);
            yArray[i][j] = parserY.evaluate();
          }
        } // for y
      } // for x
      if (useX) field.setVectorSizeXData(xArray);
      else field.setVectorSizeX(0.0);
      if (useY) field.setVectorSizeYData(yArray);
      else field.setVectorSizeY(0.0);
    }
    else { // format (length, angle, beta) functionAngle, functionBeta are not null and errorAngle, errorBeta are false
      // Prepare the parser
      for (int i = 0, n = varsAngle.length; i < n; i++)
        if (i!=indexOfxInAngleStr && i!=indexOfyInAngleStr)  parserAngle.setVariable(i, myGroup.getDouble(varsAngle[i]));
      // Check arrays
      if (angleArray==null || angleArray.length!=nx || angleArray[0].length!=ny) angleArray = new double[nx][ny];
      // Compute values
      for (int i=0; i<nx; i++) {
        if (indexOfxInAngleStr >= 0) parserAngle.setVariable(indexOfxInAngleStr, field.indexToX(i));
        for (int j = 0; j < ny; j++) {
          if (indexOfyInAngleStr >= 0) parserAngle.setVariable(indexOfyInAngleStr, field.indexToY(j));
          angleArray[i][j] = parserAngle.evaluate();
        } // for y
      } // for x
      field.setVectorAngleData(angleArray);
    }
    
    // Recompute the magnitude. 
    if (functionMag == null || errorMag) field.setMagnitudeData(null);
    else {
      // Prepare the parser
      for (int i = 0, n = varsMag.length; i < n; i++)
        if (i!=indexOfxInMagStr && i!=indexOfyInMagStr) parserMag.setVariable(i, myGroup.getDouble(varsMag[i]));
      // Check the array
      if (magArray==null || magArray.length!=nx || magArray[0].length!=ny) magArray = new double[nx][ny];
      // Compute the array
      for (int i=0; i<nx; i++) {
        if (indexOfxInMagStr >= 0) parserMag.setVariable(indexOfxInMagStr, field.indexToX(i));
        for (int j = 0; j < ny; j++) {
          if (indexOfyInMagStr >= 0) parserMag.setVariable(indexOfyInMagStr, field.indexToY(j));
          magArray[i][j] = parserMag.evaluate();
        } // for y
      } // for x
      field.setMagnitudeData(magArray);
    } 
  }

} // End of class

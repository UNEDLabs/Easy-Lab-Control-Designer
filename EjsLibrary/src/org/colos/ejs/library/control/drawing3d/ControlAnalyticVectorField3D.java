/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.Element;

/**
 * A set of arrows that implements a simpler 2D vector field
 */
public class ControlAnalyticVectorField3D extends ControlVectorField3D implements NeedsPreUpdate {
  static protected final int ANALYTIC_VECTORFIELD_ADDED=9;

  static protected final int ANALYTIC_X_COMPONENT=X_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_Y_COMPONENT=Y_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_Z_COMPONENT=Z_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_ALPHA_COMPONENT=ALPHA_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_BETA_COMPONENT=BETA_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_MAGNITUDE=MAGNITUDE+ANALYTIC_VECTORFIELD_ADDED;

  // Configuration variables
  protected boolean useJavaSyntax;
  protected int nx, ny, nz;
  protected String variableX = "x", variableY = "y", variableZ = "z";
  protected String functionX, functionY, functionZ, functionAlpha, functionBeta, functionMag;

  // Implementation variables
  protected boolean changedX, changedY, changedZ, changedAlpha, changedBeta, changedMag, updateIndexes;
  protected GeneralParser parserX, parserY, parserZ, parserAlpha, parserBeta, parserMag;
  protected String[] varsX, varsY, varsZ, varsAlpha, varsBeta, varsMag;
  protected int indexOfxInXStr, indexOfyInXStr, indexOfzInXStr;
  protected int indexOfxInYStr, indexOfyInYStr, indexOfzInYStr;
  protected int indexOfxInZStr, indexOfyInZStr, indexOfzInZStr;
  protected int indexOfxInAlphaStr, indexOfyInAlphaStr, indexOfzInAlphaStr;
  protected int indexOfxInBetaStr, indexOfyInBetaStr, indexOfzInBetaStr;
  protected int indexOfxInMagStr, indexOfyInMagStr, indexOfzInMagStr;
  private double[][][] xArray=null, yArray=null, zArray=null, alphaArray=null, betaArray=null, magArray=null;

  @Override
  protected Element createElement () {
    nx = ny = nz = 5;
    changedX = changedY = changedZ = true;
    changedAlpha = changedBeta = true;
    changedMag = true;
    updateIndexes = true;
    parserX = parserY = parserZ = parserAlpha = parserBeta = parserMag = null;
    functionX = null; varsX = new String[0]; indexOfxInXStr = indexOfyInXStr = indexOfzInXStr = -1;
    functionY = null; varsY = new String[0]; indexOfxInYStr = indexOfyInYStr = indexOfzInYStr = -1;
    functionZ = null; varsZ = new String[0]; indexOfxInZStr = indexOfyInZStr = indexOfzInZStr = -1;
    functionAlpha = null; varsAlpha = new String[0]; indexOfxInAlphaStr = indexOfyInAlphaStr = indexOfzInAlphaStr = -1;
    functionBeta = null; varsBeta = new String[0]; indexOfxInBetaStr = indexOfyInBetaStr = indexOfzInBetaStr = -1;
    functionMag = null; varsMag = new String[0]; indexOfxInMagStr = indexOfyInMagStr = indexOfzInMagStr = -1;
    useJavaSyntax = false;
    return super.createElement();
  }

  @Override
  protected int getPropertiesDisplacement () { return ANALYTIC_VECTORFIELD_ADDED + ControlVectorField3D.VECTORFIELD_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("points1");
      infoList.add ("points2");
      infoList.add ("points3");
      infoList.add ("variable1");
      infoList.add ("variable2");
      infoList.add ("variable3");
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
    if (_property.equals("points3"))    return "int";

    if (_property.equals("variable1"))  return "String";
    if (_property.equals("variable2"))  return "String";
    if (_property.equals("variable3"))  return "String";

    if (_property.equals("onErrorAction"))   return "Action CONSTANT";
    if (_property.equals("onSuccessAction")) return "Action CONSTANT";
    if (_property.equals("javaSyntax"))return "boolean";

    // Super's changed
    if (_property.equals("xcomponent")) return "String";
    if (_property.equals("ycomponent")) return "String";
    if (_property.equals("zcomponent")) return "String";
    if (_property.equals("angles"))     return "String";
    if (_property.equals("betas"))     return "String";
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
      case 2 : nz = _value.getInteger(); break;

      case 3 : if (variableX==null || !variableX.equals(_value.getString())) { variableX = _value.getString(); updateIndexes = true; } break;
      case 4 : if (variableY==null || !variableY.equals(_value.getString())) { variableY = _value.getString(); updateIndexes = true; } break;
      case 5 : if (variableZ==null || !variableZ.equals(_value.getString())) { variableZ = _value.getString(); updateIndexes = true; } break;

      case 6 : // onParseError
        removeAction (ACTION_ERROR,getProperty("onErrorAction"));
        addAction(ACTION_ERROR,_value.getString());
        return;
      case 7 : // onParseSuccess
        removeAction (ACTION_SUCCESS,getProperty("onSuccessAction"));
        addAction(ACTION_SUCCESS,_value.getString());
        return;
      case 8 :
        if (_value.getBoolean()!=useJavaSyntax) {
          useJavaSyntax = _value.getBoolean();
          changedX = changedY = changedZ = true;
          changedAlpha = changedBeta = true;
          changedMag = true;
        }
        break;

      default : super.setValue(_index-ANALYTIC_VECTORFIELD_ADDED,_value); break;

      case ANALYTIC_X_COMPONENT :     if (functionX==null || !functionX.equals(_value.getString())) { functionX = _value.getString(); changedX= true; } break;
      case ANALYTIC_Y_COMPONENT :     if (functionY==null || !functionY.equals(_value.getString())) { functionY = _value.getString(); changedY= true; } break;
      case ANALYTIC_Z_COMPONENT :     if (functionZ==null || !functionZ.equals(_value.getString())) { functionZ = _value.getString(); changedZ= true; } break;
      case ANALYTIC_ALPHA_COMPONENT : if (functionAlpha==null || !functionAlpha.equals(_value.getString())) { functionAlpha = _value.getString(); changedAlpha= true; } break;
      case ANALYTIC_BETA_COMPONENT :  if (functionBeta==null || !functionBeta.equals(_value.getString())) { functionBeta = _value.getString(); changedBeta= true; } break;
      case ANALYTIC_MAGNITUDE :       if (functionMag==null || !functionMag.equals(_value.getString())) { functionMag = _value.getString(); changedMag = true; } break;

    }
    if (isUnderEjs) {
      preupdate();
      updatePanel();
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : nx = 5; break;
      case 1 : ny = 5; break;
      case 2 : nz = 5; break;

      case 3 : variableX = "x"; updateIndexes = true; break;
      case 4 : variableY = "y"; updateIndexes = true; break;
      case 5 : variableZ = "z"; updateIndexes = true; break;

      case 6 : removeAction (ACTION_ERROR,getProperty("onErrorAction")); return;
      case 7 : removeAction (ACTION_SUCCESS,getProperty("onSuccessAction")); return;
      case 8 :
        useJavaSyntax = false;
        changedX = changedY = changedZ = true;
        changedAlpha = changedBeta = true;
        changedMag = true;
        break;

      default: super.setDefaultValue(_index-ANALYTIC_VECTORFIELD_ADDED); break;

      case ANALYTIC_X_COMPONENT : functionX = null; changedX= true; break;
      case ANALYTIC_Y_COMPONENT : functionY = null; changedY= true; break;
      case ANALYTIC_Z_COMPONENT : functionZ = null; changedZ= true; break;
      case ANALYTIC_ALPHA_COMPONENT : functionAlpha = null; changedAlpha= true; break;
      case ANALYTIC_BETA_COMPONENT : functionBeta = null; changedBeta= true; break;
      case ANALYTIC_MAGNITUDE : functionMag = null; changedMag=true; break;

    }
    if (isUnderEjs) {
      preupdate();
      updatePanel();
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 :
      case 1 : 
      case 2 : return "5";
      case 3 : return "x";
      case 4 : return "y";
      case 5 : return "z";
      case 6 :
      case 7 : return "<no_action>";
      case 8 : return "false";

      default : return super.getDefaultValueString(_index-ANALYTIC_VECTORFIELD_ADDED);

      case ANALYTIC_X_COMPONENT :
      case ANALYTIC_Y_COMPONENT :
      case ANALYTIC_Z_COMPONENT :
      case ANALYTIC_ALPHA_COMPONENT :
      case ANALYTIC_BETA_COMPONENT :
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
    if (variableZ.equals(varName)) return true;
    Value value = myGroup.getValue(varName);
    if (value instanceof IntegerValue) return true; // The variable is registered as an integer or double value
    if (value instanceof DoubleValue) return true;
    return false;
  }

  public void preupdate () {
    if (!field.isVisible()) return; // save time
    if (nx<1 || ny<1 || nz<1) return;
    if (myParent==null) return;
    boolean parsedOk = false, errorX=false, errorY=false, errorZ=false, errorAlpha=false, errorBeta=false, errorMag=false;
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
    if (changedZ && functionZ!=null) {
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
      changedZ = false;
      updateIndexes = true;
    }
    if (changedAlpha && functionAlpha!=null) {
      ParserAndVariables pav = new ParserAndVariables(useJavaSyntax,functionAlpha);
      varsAlpha = pav.getVariables();
      parserAlpha = pav.getParser();
      for (int i = 0, n = varsAlpha.length; i < n; i++) {
        if (!isVariableDefined(varsAlpha[i])) { errorAlpha = true; break; } // The variable is not defined in the model
        parserAlpha.defineVariable(i,varsAlpha[i]);
      }
      if (!errorAlpha) {
        parserAlpha.define(functionAlpha);
        parserAlpha.parse();
        if (parserAlpha.hasError()) errorAlpha = true;
        else parsedOk = true;
      }
      changedAlpha = false;
      updateIndexes = true;
    }
    if (changedBeta && functionBeta!=null) {
      ParserAndVariables pav = new ParserAndVariables(useJavaSyntax,functionBeta);
      varsBeta = pav.getVariables();
      parserBeta = pav.getParser();
      for (int i = 0, n = varsBeta.length; i < n; i++) {
        if (!isVariableDefined(varsBeta[i])) { errorBeta = true; break; } // The variable is not defined in the model
        parserBeta.defineVariable(i,varsBeta[i]);
      }
      if (!errorBeta) {
        parserBeta.define(functionBeta);
        parserBeta.parse();
        if (parserBeta.hasError()) errorBeta = true;
        else parsedOk = true;
      }
      changedBeta = false;
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

    if (errorX || errorY || errorZ || errorAlpha || errorBeta || errorMag) invokeActions (ControlElement.ACTION_ERROR);
    else if (parsedOk) invokeActions (ControlElement.ACTION_SUCCESS);
    if (isUnderEjs) {
      myEjsPropertyEditor.displayErrorOnProperty ("xcomponent", errorX);
      myEjsPropertyEditor.displayErrorOnProperty ("ycomponent", errorY);
      myEjsPropertyEditor.displayErrorOnProperty ("zcomponent", errorZ);
      myEjsPropertyEditor.displayErrorOnProperty ("angles", errorAlpha);
      myEjsPropertyEditor.displayErrorOnProperty ("betas", errorBeta);
      myEjsPropertyEditor.displayErrorOnProperty ("magnitude", errorMag);
    }
    
    if (updateIndexes) {
        indexOfxInXStr = ControlElement.indexOf (variableX,varsX);
        indexOfyInXStr = ControlElement.indexOf (variableY,varsX);
        indexOfzInXStr = ControlElement.indexOf (variableZ,varsX);
        indexOfxInYStr = ControlElement.indexOf (variableX,varsY);
        indexOfyInYStr = ControlElement.indexOf (variableY,varsY);
        indexOfzInYStr = ControlElement.indexOf (variableZ,varsY);
        indexOfxInZStr = ControlElement.indexOf (variableX,varsZ);
        indexOfyInZStr = ControlElement.indexOf (variableY,varsZ);
        indexOfzInZStr = ControlElement.indexOf (variableZ,varsZ);
        indexOfxInAlphaStr = ControlElement.indexOf (variableX,varsAlpha);
        indexOfyInAlphaStr = ControlElement.indexOf (variableY,varsAlpha);
        indexOfzInAlphaStr = ControlElement.indexOf (variableZ,varsAlpha);
        indexOfxInBetaStr = ControlElement.indexOf (variableX,varsBeta);
        indexOfyInBetaStr = ControlElement.indexOf (variableY,varsBeta);
        indexOfzInBetaStr = ControlElement.indexOf (variableZ,varsBeta);
        indexOfxInMagStr = ControlElement.indexOf (variableX,varsMag);
        indexOfyInMagStr = ControlElement.indexOf (variableY,varsMag);
        indexOfzInMagStr = ControlElement.indexOf (variableZ,varsMag);
    }

    // Analytic elements must be recomputed every time because a parameter in the expression may have
    // changed even if all the rest is unchanged
    
    if (functionX!=null || functionY!=null || functionZ!=null || 
        functionAlpha==null || errorAlpha || functionBeta==null || errorBeta) { // use the (dx,dy,dz) format
      // Prepare the parser
      for (int i = 0, n = varsX.length; i < n; i++)
        if (i!=indexOfxInXStr && i!=indexOfyInXStr && i!=indexOfzInXStr) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
      for (int i = 0, n = varsY.length; i < n; i++)
        if (i!=indexOfxInYStr && i!=indexOfyInYStr && i!=indexOfzInYStr) parserY.setVariable(i, myGroup.getDouble(varsY[i]));
      for (int i = 0, n = varsZ.length; i < n; i++)
        if (i!=indexOfxInZStr && i!=indexOfyInZStr && i!=indexOfzInZStr) parserZ.setVariable(i, myGroup.getDouble(varsZ[i]));

      boolean useX = (functionX!=null && !errorX); 
      boolean useY = (functionY!=null && !errorY); 
      boolean useZ = (functionZ!=null && !errorZ);
      
      // Check Arrays
      if (useX) {
        if (xArray==null || xArray.length!=nx || xArray[0].length!=ny || xArray[0][0].length!=nz) xArray = new double[nx][ny][nz];
      }
      if (useY) {
        if (yArray==null || yArray.length!=nx || yArray[0].length!=ny || yArray[0][0].length!=nz) yArray = new double[nx][ny][nz];
      }
      if (useZ) {
        if (zArray==null || zArray.length!=nx || zArray[0].length!=ny || zArray[0][0].length!=nz) zArray = new double[nx][ny][nz];
      }
      // Compute values
      for (int i=0; i<nx; i++) {
        double x = field.indexToX(i);
        if (useX && indexOfxInXStr >= 0) parserX.setVariable(indexOfxInXStr, x);
        if (useY && indexOfxInYStr >= 0) parserY.setVariable(indexOfxInYStr, x);
        if (useZ && indexOfxInZStr >= 0) parserZ.setVariable(indexOfxInZStr, x);
        for (int j = 0; j < ny; j++) {
          double y = field.indexToY(j);
          if (useX && indexOfyInXStr >= 0) parserX.setVariable(indexOfyInXStr, y);
          if (useY && indexOfyInYStr >= 0) parserY.setVariable(indexOfyInYStr, y);
          if (useZ && indexOfyInZStr >= 0) parserZ.setVariable(indexOfyInZStr, y);
          for (int k = 0; k < nz; k++) {
            double z = field.indexToZ(k);
            if (useX) {
              if (indexOfzInXStr >= 0) parserX.setVariable(indexOfzInXStr, z);
              xArray[i][j][k] = parserX.evaluate();
            }
            if (useY) {
              if (indexOfzInYStr >= 0) parserY.setVariable(indexOfzInYStr, z);
              yArray[i][j][k] = parserY.evaluate();
            }
            if (useZ) {
              if (indexOfzInZStr >= 0) parserZ.setVariable(indexOfzInZStr, z);
              zArray[i][j][k] = parserZ.evaluate();
            }
          } // for z
        } // for y
      } // for x
      if (useX) field.setVectorSizeXData(xArray);
      else field.setVectorSizeX(0.0);
      if (useY) field.setVectorSizeYData(yArray);
      else field.setVectorSizeY(0.0);
      if (useZ) field.setVectorSizeZData(zArray);
      else field.setVectorSizeZ(0.0);
    }
    else { // format (length, alpha, beta) functionAlpha, functionBeta are not null and errorAlpha, errorBeta are false
      // Prepare the parser
      for (int i = 0, n = varsAlpha.length; i < n; i++)
        if (i!=indexOfxInAlphaStr && i!=indexOfyInAlphaStr && i!=indexOfzInAlphaStr)  parserAlpha.setVariable(i, myGroup.getDouble(varsAlpha[i]));
      for (int i = 0, n = varsBeta.length; i < n; i++)
        if (i!=indexOfxInBetaStr && i!=indexOfyInBetaStr && i!=indexOfzInBetaStr)  parserBeta.setVariable(i, myGroup.getDouble(varsBeta[i]));
      // Check arrays
      if (alphaArray==null || alphaArray.length!=nx || alphaArray[0].length!=ny || alphaArray[0][0].length!=nz) alphaArray = new double[nx][ny][nz];
      if (betaArray==null || betaArray.length!=nx || betaArray[0].length!=ny || betaArray[0][0].length!=nz) betaArray = new double[nx][ny][nz];
      // Compute values
      for (int i=0; i<nx; i++) {
        double x = field.indexToX(i);
        if (indexOfxInAlphaStr >= 0) parserAlpha.setVariable(indexOfxInAlphaStr, x);
        if (indexOfxInBetaStr  >= 0) parserBeta.setVariable(indexOfxInBetaStr, x);
        for (int j = 0; j < ny; j++) {
          double y = field.indexToY(j);
          if (indexOfyInAlphaStr >= 0) parserAlpha.setVariable(indexOfyInAlphaStr, y);
          if (indexOfyInBetaStr  >= 0) parserBeta.setVariable(indexOfyInBetaStr, y);
          for (int k = 0; k < nz; k++) {
            double z = field.indexToZ(k);
            if (indexOfzInAlphaStr >= 0) parserAlpha.setVariable(indexOfzInAlphaStr, z);
            if (indexOfzInBetaStr  >= 0) parserBeta.setVariable(indexOfzInBetaStr, z);
            alphaArray[i][j][k] = parserAlpha.evaluate();
            betaArray[i][j][k] = parserBeta.evaluate();
          } // for z
        } // for y
      } // for x
      field.setVectorAlphaAndBetaData(alphaArray,betaArray);
    }
    
    // Recompute the magnitude. 
    if (functionMag == null || errorMag) field.setMagnitudeData(null);
    else {
      // Prepare the parser
      for (int i = 0, n = varsMag.length; i < n; i++)
        if (i!=indexOfxInMagStr && i!=indexOfyInMagStr && i!=indexOfzInMagStr) parserMag.setVariable(i, myGroup.getDouble(varsMag[i]));
      // Check the array
      if (magArray==null || magArray.length!=nx || magArray[0].length!=ny || magArray[0][0].length!=nz) magArray = new double[nx][ny][nz];
      // Compute the array
      for (int i=0; i<nx; i++) {
        if (indexOfxInMagStr >= 0) parserMag.setVariable(indexOfxInMagStr, field.indexToX(i));
        for (int j = 0; j < ny; j++) {
          if (indexOfyInMagStr >= 0) parserMag.setVariable(indexOfyInMagStr, field.indexToY(j));
          for (int k = 0; k < nz; k++) {
            if (indexOfzInMagStr >= 0) parserMag.setVariable(indexOfzInMagStr, field.indexToZ(k));
            magArray[i][j][k] = parserMag.evaluate();
          } // for z
        } // for y
      } // for x
      field.setMagnitudeData(magArray);
    }
  }

} // End of class

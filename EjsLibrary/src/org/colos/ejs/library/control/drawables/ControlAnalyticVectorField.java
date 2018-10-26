/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;

/**
 * A set of arrows that implements a simpler 2D vector field
 */
public class ControlAnalyticVectorField extends ControlVectorField {
  static protected final int ANALYTIC_VECTORFIELD_ADDED=7;

  static protected final int ANALYTIC_X_COMPONENT=X_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_Y_COMPONENT=Y_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_ANGLE_COMPONENT=ANGLE_COMPONENT+ANALYTIC_VECTORFIELD_ADDED;
  static protected final int ANALYTIC_MAGNITUDE=MAGNITUDE+ANALYTIC_VECTORFIELD_ADDED;

  // Configuration variables
  protected int nx, ny;
  protected String variableX, variableY;
  protected boolean useJavaSyntax, changedXfunction, changedYfunction, changedAnglefunction, changedMagfunction, updateIndexes;
  protected String functionX, functionY, functionAngle, functionMag;
  protected GeneralParser parserX, parserY, parserAngle, parserMag;
  protected String[] varsX, varsY, varsAngle, varsMag;
  protected int indexOfxInXStr, indexOfyInXStr;
  protected int indexOfxInYStr, indexOfyInYStr;
  protected int indexOfxInAngleStr, indexOfyInAngleStr;
  protected int indexOfxInMagStr, indexOfyInMagStr;

  protected Drawable createDrawable () {
    Drawable drawable = super.createDrawable();
    nx = ny = 0;
    variableX = "x";
    variableY = "y";
    changedXfunction = changedYfunction = changedAnglefunction = changedMagfunction = updateIndexes = true;
    parserX = parserY = parserAngle = parserMag = null;
    functionX = null; varsX = new String[0]; indexOfxInXStr = indexOfyInXStr = -1;
    functionY = null; varsY = new String[0]; indexOfxInYStr = indexOfyInYStr = -1;
    functionAngle = null; varsAngle = new String[0]; indexOfxInAngleStr = -1; indexOfyInAngleStr = -1;
    functionMag = null; varsMag = new String[0]; indexOfxInMagStr = indexOfyInMagStr = -1;
    useJavaSyntax = false;
    return drawable;
  }

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

  public String getPropertyCommonName(String _property) {
    if (_property.equals("minimumX")) return "min1";
    if (_property.equals("maximumX")) return "max1";
    if (_property.equals("minimumY")) return "min2";
    if (_property.equals("maximumY")) return "max2";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("xcomponent")) return "String";
    if (_property.equals("ycomponent")) return "String";
    if (_property.equals("angles"))     return "String";
    if (_property.equals("magnitude"))  return "String";

    if (_property.equals("points1"))    return "int";
    if (_property.equals("points2"))    return "int";

    if (_property.equals("variable1"))  return "String";
    if (_property.equals("variable2"))  return "String";

    if (_property.equals("onErrorAction"))   return "Action CONSTANT";
    if (_property.equals("onSuccessAction")) return "Action CONSTANT";
    if (_property.equals("javaSyntax"))return "boolean";
    return super.getPropertyInfo(_property);
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
         changedXfunction = changedYfunction = changedAnglefunction = changedMagfunction = true;
       }
       break;

      case ANALYTIC_X_COMPONENT :
          if (!_value.getString().equals(functionX)) { functionX = _value.getString(); changedXfunction= true; }
          break;
      case ANALYTIC_Y_COMPONENT :
          if (!_value.getString().equals(functionY)) { functionY = _value.getString(); changedYfunction= true; }
          break;
      case ANALYTIC_ANGLE_COMPONENT :
          if (!_value.getString().equals(functionAngle)) { functionAngle = _value.getString(); changedAnglefunction= true; }
          break;
      case ANALYTIC_MAGNITUDE :
          if (!_value.getString().equals(functionMag)) { functionMag = _value.getString(); changedMagfunction=magnitudeSet= true; }
          break;

      default : super.setValue(_index-ANALYTIC_VECTORFIELD_ADDED,_value); break;
    }
    if (isUnderEjs) preupdate();
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
          changedXfunction = changedYfunction = changedAnglefunction = changedMagfunction = true;
          break;

        case ANALYTIC_X_COMPONENT : functionX = null; changedXfunction= true; break;
        case ANALYTIC_Y_COMPONENT : functionY = null; changedYfunction= true; break;
        case ANALYTIC_ANGLE_COMPONENT : functionAngle = null; changedAnglefunction= true; break;
        case ANALYTIC_MAGNITUDE : functionMag = null; changedMagfunction=true; magnitudeSet=false; break;

        default: super.setDefaultValue(_index-ANALYTIC_VECTORFIELD_ADDED); break;
    }
      if (isUnderEjs) preupdate();
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

      case ANALYTIC_X_COMPONENT :
      case ANALYTIC_Y_COMPONENT :
      case ANALYTIC_ANGLE_COMPONENT :
      case ANALYTIC_MAGNITUDE : return "<none>";

      default : return super.getDefaultValueString(_index-ANALYTIC_VECTORFIELD_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 :
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

  @Override
  public void preupdate () {
    if (!visible) return;
    if (myParent==null) return;
    DrawingPanel panel = myParent.getDrawingPanel();
    int nxPoints = nx, nyPoints=ny;
    if (nxPoints<=0) {
      if (panel!=null) nxPoints = Math.max(20,panel.getWidth()/20);
      else nxPoints = 20;
    }
    if (nyPoints<=0) {
      if (panel!=null) nyPoints = Math.max(20,panel.getHeight()/20);
      else nyPoints = 20;
    }
    if (vectorLength==null || vectorLength.length!=nxPoints || vectorLength[0].length!=nyPoints) vectorLength = new double[nxPoints][nyPoints];

    if (nxPoints*nyPoints!=elementSet.getNumberOfElements()) {
      elementSet.setNumberOfElements(nxPoints*nyPoints);
      elementSet.setEnabled(InteractiveElement.TARGET_POSITION, false);
      elementSet.setEnabled(InteractiveElement.TARGET_SIZE, false);
      for (int i=0, el=0; i<nxPoints; i++) for (int j = 0; j < nyPoints; j++, el++) {
        InteractiveArrow arrow = (InteractiveArrow) elementSet.elementAt(el);
        arrow.getStyle().setEdgeStroke(stroke);
        arrow.setArrowType(arrowType);
      }
    }

    boolean parsedOk = false, errorX=false, errorY=false, errorAngle=false, errorMag=false;
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
    if (changedAnglefunction && functionAngle!=null) {
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
      changedAnglefunction = false;
      updateIndexes = true;
    }

    if (changedMagfunction && functionMag!=null) {
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
      changedMagfunction = false;
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
    double minAbcise = xmin, maxAbcise = xmax;
    double minOrdinate = ymin, maxOrdinate = ymax;
    boolean anyNaN = false;
    if (Double.isNaN(minAbcise)) { minAbcise = panel.getPreferredXMin(); anyNaN = true; }
    if (Double.isNaN(maxAbcise)) { maxAbcise = panel.getPreferredXMax(); anyNaN = true; }
    if (Double.isNaN(minOrdinate)) { minOrdinate = panel.getPreferredYMin(); anyNaN = true; }
    if (Double.isNaN(maxOrdinate)) { maxOrdinate = panel.getPreferredYMax(); anyNaN = true; }
    elementSet.canBeMeasured (!anyNaN);

    double dx = (maxAbcise-minAbcise)/(nxPoints-1), dy = (maxOrdinate-minOrdinate)/(nyPoints-1);
    double minD = Math.min(dx,dy);
    double lengthFactor;
    if (constantLength>0) lengthFactor = constantLength*minD;
    else lengthFactor = Math.abs(constantLength);

    if (functionX!=null || functionY!=null || functionAngle==null || errorAngle) { // use the (dx,dy) format
      // Prepare the parser
      for (int i = 0, n = varsX.length; i < n; i++)
        if (i!=indexOfxInXStr && i!=indexOfyInXStr) parserX.setVariable(i, myGroup.getDouble(varsX[i]));
      for (int i = 0, n = varsY.length; i < n; i++)
        if (i!=indexOfxInYStr && i!=indexOfyInYStr) parserY.setVariable(i, myGroup.getDouble(varsY[i]));
      double x = minAbcise;
      for (int i=0, el=0; i<nxPoints; i++, x+=dx) {
        double y = minOrdinate;
        for (int j = 0; j < nyPoints; j++, el++, y+=dy) {
          double sizeXComputed, sizeYComputed;
          if (functionX == null || errorX) sizeXComputed = 0.0;
          else {
            if (indexOfxInXStr >= 0) parserX.setVariable(indexOfxInXStr, x);
            if (indexOfyInXStr >= 0) parserX.setVariable(indexOfyInXStr, y);
            sizeXComputed = parserX.evaluate();
          }
          if (functionY == null || errorY) sizeYComputed = 0.0;
          else {
            if (indexOfxInYStr >= 0) parserY.setVariable(indexOfxInYStr, x);
            if (indexOfyInYStr >= 0) parserY.setVariable(indexOfyInYStr, y);
            sizeYComputed = parserY.evaluate();
          }
          vectorLength[i][j] = Math.sqrt(sizeXComputed*sizeXComputed + sizeYComputed*sizeYComputed);
          if (lengthSet) { // Use (dx,dy) for the direction and lengthFactor for the length
            double length = vectorLength[i][j];
            if (length != 0) {
              length = lengthFactor/length;
              sizeXComputed *= length; sizeYComputed *= length;
            }
          }
          InteractiveElement element = elementSet.elementAt(el);
//          System.out.println ("El "+el+" at "+x+","+y+"  size = "+sizeX+","+sizeY);
          if (centered) element.setXY(x-sizeXComputed/2,y-sizeYComputed/2);
          else element.setXY(x,y);
          element.setSizeXY(sizeXComputed,sizeYComputed);
        }
      }
    }
    else { // format (length, angle) functionAngle is not null and errorAngle is false
      // Prepare the parser
      for (int i = 0, n = varsAngle.length; i < n; i++)
        if (i!=indexOfxInAngleStr && i!=indexOfyInAngleStr)  parserAngle.setVariable(i, myGroup.getDouble(varsAngle[i]));
      double x = minAbcise;
      for (int i=0, el=0; i<nxPoints; i++, x+=dx) {
        double y = minOrdinate;
        for (int j = 0; j < nyPoints; j++, el++, y+=dy) {
          vectorLength[i][j] = constantLength;
          if (indexOfxInAngleStr >= 0) parserAngle.setVariable(indexOfxInAngleStr, x);
          if (indexOfyInAngleStr >= 0) parserAngle.setVariable(indexOfyInAngleStr, y);
          double angleComputed = parserAngle.evaluate();
          double sizeXComputed = lengthFactor*Math.cos(angleComputed);
          double sizeYComputed = lengthFactor*Math.sin(angleComputed);
          InteractiveElement element = elementSet.elementAt(el);
          if (centered) element.setXY(x-sizeXComputed/2,y-sizeYComputed/2);
          else element.setXY(x,y);
          element.setSizeXY(sizeXComputed,sizeYComputed);
        }
      }
    } // end

    // Recompute the magnitude. If levels is 0 there is no need to expend the extra time in computing vectorMagnitudeData
    if (levels<=0) magData = null;
    else {
      if (magData==null || magData.length!=nxPoints || magData[0].length!=nyPoints) magData = new double[nxPoints][nyPoints];
      if (functionMag == null || errorMag) for (int i=0; i<nxPoints; i++) for (int j=0; j<nyPoints; j++) magData[i][j] = 0.0;
      else  {
        // Prepare the parser
        for (int i = 0, n = varsMag.length; i < n; i++)
          if (i!=indexOfxInMagStr && i!=indexOfyInMagStr) parserMag.setVariable(i, myGroup.getDouble(varsMag[i]));
        double x = minAbcise;
        for (int i=0; i<nxPoints; i++, x+=dx) {
          double y = minOrdinate;
          for (int j = 0; j < nyPoints; j++, y+=dy) {
            if (indexOfxInMagStr >= 0) parserMag.setVariable(indexOfxInMagStr, x);
            if (indexOfyInMagStr >= 0) parserMag.setVariable(indexOfyInMagStr, y);
            magData[i][j] = parserMag.evaluate();
          }
        }
      }
    }
    processMagnitude(nxPoints,nyPoints);
    if (isUnderEjs) {
      if (javax.swing.SwingUtilities.isEventDispatchThread()||Thread.currentThread().getName().equals("main")) {
        panel.invalidateImage();
        panel.repaint();
      }
      else if (getSimulation()==null || getSimulation().isPaused()) panel.render();
    }
  }

} // End of class

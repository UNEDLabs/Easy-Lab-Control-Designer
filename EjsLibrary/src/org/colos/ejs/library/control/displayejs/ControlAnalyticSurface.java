/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) Feb 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
//import org.colos.ejs.library.control.ControlElement;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;

/**
 * An analytic surface
 */
public class ControlAnalyticSurface extends ControlInteractiveTile implements NeedsPreUpdate {
  static final int ANALYTIC_SURFACE_ADDED=14;
  static final int PROPERTIES_SURFACE=PROPERTIES_ADDED+ANALYTIC_SURFACE_ADDED;
  static final int MY_PRIMARY_COLOR=PRIMARY_COLOR+PROPERTIES_SURFACE;
  static final int MY_SECONDARY_COLOR=SECONDARY_COLOR+PROPERTIES_SURFACE;

  // Configuration variables
  protected InteractiveSurface surface;
  protected String variable1, variable2;
  protected double min1 = -1.0, max1 = 1.0;
  protected double min2 = -1.0, max2 = 1.0;
  protected int points1 = 20, points2 = 20;
  protected String functionX, functionY, functionZ;
  protected double[][][] data=new double[points1][points2][3];
  protected boolean useJavaSyntax=true;

  // Implementation variables
  protected boolean changedXfunction, changedYfunction, changedZfunction, updateIndexes;
  protected GeneralParser parserX, parserY, parserZ;
  protected String[] varsX, varsY, varsZ;
  protected int indexX1, indexX2, indexY1, indexY2, indexZ1, indexZ2;

  protected Drawable createDrawable () {
    surface = new InteractiveSurface();
//    surface.setEnabled(false);
    variable1 = "u"; variable2 = "v";
    functionX = null; varsX = new String[0]; indexX1 = -1; indexX2 = -1;
    functionY = null; varsY = new String[0]; indexY1 = -1; indexY2 = -1;
    functionZ = null; varsZ = new String[0]; indexZ1 = -1; indexZ2 = -1;
    changedXfunction = changedYfunction = changedZfunction = true;
    surface.setOrigin(0,0,0,true);
    sizeValues = new DoubleValue[] { new DoubleValue(1), new DoubleValue(1), new DoubleValue(1)};
    return surface;
  }

  protected int getPropertiesDisplacement () { return PROPERTIES_SURFACE; }

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
      if (_property.equals("onErrorAction"))   return "Action CONSTANT";
      if (_property.equals("onSuccessAction")) return "Action CONSTANT";
      return super.getPropertyInfo(_property);
    }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

    public void setValue (int _index, Value _value) {
      switch (_index) {
        case 0 : if (!_value.getString().equals(variable1))  { variable1 = _value.getString(); updateIndexes = true; } break;
        case 1 : min1 = _value.getDouble();   break;
        case 2 : max1 = _value.getDouble();   break;
        case 3 : points1 = _value.getInteger(); break;
        case 4 : if (!_value.getString().equals(variable2))  { variable2 = _value.getString(); updateIndexes = true; } break;
        case 5 : min2 = _value.getDouble();   break;
        case 6 : max2 = _value.getDouble();   break;
        case 7 : points2 = _value.getInteger(); break;
        case  8 : if (!_value.getString().equals(functionX)) { functionX = _value.getString(); changedXfunction = true; } break;
        case  9 : if (!_value.getString().equals(functionY)) { functionY = _value.getString(); changedYfunction = true; } break;
        case 10 : if (!_value.getString().equals(functionZ)) { functionZ = _value.getString(); changedZfunction = true; } break;
        case 11:
          if (_value.getBoolean()!=useJavaSyntax) {
            useJavaSyntax = _value.getBoolean();
            changedXfunction = changedYfunction = changedZfunction = true;
          }
          break;
        case 12 : // onParseError
          removeAction (ACTION_ERROR,getProperty("onErrorAction"));
          addAction(ACTION_ERROR,_value.getString());
          return;
        case 13 : // onParseSuccess
          removeAction (ACTION_SUCCESS,getProperty("onSuccessAction"));
          addAction(ACTION_SUCCESS,_value.getString());
          return;

        default: super.setValue(_index-ANALYTIC_SURFACE_ADDED,_value); break;

        case MY_PRIMARY_COLOR   :
          if (_value instanceof IntegerValue) myElement.getStyle().setFillPattern(DisplayColors.getLineColor(_value.getInteger()));
          else {
            java.awt.Paint fill = (java.awt.Paint) _value.getObject();
            if (fill==NULL_COLOR) fill = null;
            myElement.getStyle().setFillPattern(fill);
          }
        break;
        case MY_SECONDARY_COLOR : 
          if (_value instanceof IntegerValue) myElement.getStyle().setEdgeColor(DisplayColors.getLineColor(_value.getInteger()));
          else myElement.getStyle().setEdgeColor((java.awt.Color) _value.getObject()); break;
      }
    }

    public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : variable1 = "u"; updateIndexes = true; break;
        case 1 : min1 = -1.0;   break;
        case 2 : max1 = 1.0;   break;
        case 3 : points1 = 20; break;
        case 4 : variable2 = "v"; updateIndexes = true; break;
        case 5 : min2 = -1.0;   break;
        case 6 : max2 = 1.0;   break;
        case 7 : points2 = 20; break;
        case  8 : functionX = null; varsX = new String[0]; changedXfunction = true; break;
        case  9 : functionY = null; varsY = new String[0]; changedYfunction = true;  break;
        case 10 : functionZ = null; varsZ = new String[0]; changedZfunction = true;  break;
        case 11 : useJavaSyntax = true; changedXfunction = changedYfunction = changedZfunction = true; break;
        case 12 : removeAction (ACTION_ERROR,getProperty("onErrorAction")); return;
        case 13 : removeAction (ACTION_SUCCESS,getProperty("onSuccessAction")); return;
        default: super.setDefaultValue(_index-ANALYTIC_SURFACE_ADDED); break;

        case MY_PRIMARY_COLOR : myElement.getStyle().setFillPattern(java.awt.Color.blue); break;
        case MY_SECONDARY_COLOR : myElement.getStyle().setEdgeColor(java.awt.Color.black); break;
      }
    }

    public String getDefaultValueString (int _index) {
      switch (_index) {
        case 0 : return "u";
        case 1 : return "-1.0";
        case 2 : return "1.0";
        case 3 : return "20";
        case 4 : return "v";
        case 5 : return "-1.0";
        case 6 : return "1.0";
        case 7 : return "20";
        case  8 :
        case  9 :
        case 10 : return "<none>";
        case 11 : return "true";
        case 12 :
        case 13 : return "<no_action>";

        case MY_PRIMARY_COLOR : return "BLUE";
        case MY_SECONDARY_COLOR : return "BLACK";
        default : return super.getDefaultValueString(_index-ANALYTIC_SURFACE_ADDED);
      }
    }

    public Value getValue (int _index) {
      switch (_index) {
        case 0 : case 1 : case 2 : case 3 :
        case 4 : case 5 : case 6 : case 7 :
        case 8 : case 9 : case 10 : case 11 :
        case 12 : case 13 :
          return null;
        default: return super.getValue (_index-ANALYTIC_SURFACE_ADDED);
      }
    }

// -------------------------------------
// Update the curve
// -------------------------------------

  private boolean isVariableDefined (String varName) {
    if (variable1.equals(varName)) return true;
    if (variable2.equals(varName)) return true;
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
      indexX1 = ControlElement.indexOf (variable1,varsX);
      indexY1 = ControlElement.indexOf (variable1,varsY);
      indexZ1 = ControlElement.indexOf (variable1,varsZ);
      indexX2 = ControlElement.indexOf (variable2,varsX);
      indexY2 = ControlElement.indexOf (variable2,varsY);
      indexZ2 = ControlElement.indexOf (variable2,varsZ);
      updateIndexes = false;
    }
    // Prepare the parser
    for (int i=0, n=varsX.length; i<n; i++) if (i!=indexX1 && i!=indexX2) parserX.setVariable(i,myGroup.getDouble(varsX[i]));
    for (int i=0, n=varsY.length; i<n; i++) if (i!=indexY1 && i!=indexY2) parserY.setVariable(i,myGroup.getDouble(varsY[i]));
    for (int i=0, n=varsZ.length; i<n; i++) if (i!=indexZ1 && i!=indexZ2) parserZ.setVariable(i,myGroup.getDouble(varsZ[i]));
    if (data.length!=points1 || data[0].length!=points2) data =new double[points1][points2][3];
    for (int i=0, n=points1-1; i<=n; i++) {
      double u = ((n-i)*min1 + i*max1)/n;
      if (indexX1>=0) parserX.setVariable(indexX1,u);
      if (indexY1>=0) parserY.setVariable(indexY1,u);
      if (indexZ1>=0) parserZ.setVariable(indexZ1,u);
      for (int j=0, m=points2-1; j<=m; j++) {
        double v = ((m-j)*min2 + j*max2)/m;
        if (functionX==null) data[i][j][0] = 0.0;
        else {
          if (indexX2>=0) parserX.setVariable(indexX2,v);
          data[i][j][0] = parserX.evaluate();
        }
        if (functionY==null) data[i][j][1] = 0.0;
        else {
          if (indexY2>=0) parserY.setVariable(indexY2,v);
          data[i][j][1] = parserY.evaluate();
        }
        if (functionZ==null) data[i][j][2] = 0.0;
        else {
          if (indexZ2>=0) parserZ.setVariable(indexZ2,v);
          data[i][j][2] = parserZ.evaluate();
        }
      }
    }
    surface.setData(data);
  }

} // End of class

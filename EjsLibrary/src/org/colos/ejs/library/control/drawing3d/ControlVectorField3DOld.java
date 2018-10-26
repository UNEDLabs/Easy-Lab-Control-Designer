/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d;

import java.awt.Color;
import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.*;

/**
 * A set of arrows that implements a 3D vector field
 * This is provided for backwards compatibility only!!!
 */
public class ControlVectorField3DOld extends ControlElement3D implements NeedsPreUpdate {
  static protected final int FIELD_PROPERTIES_ADDED=9;

  // Configuration variables
  protected int levels;
  protected int invisibleLevel = -1;
  protected boolean autoscaleMagnitude = false;
  protected double minMagnitude=0.0, maxMagnitude=1.0;
  protected Color maxColor,minColor;

  // Implementation variables
  protected boolean dataChanged=true, colorChanged=true;
  protected Group group;
  protected ElementArrow arrows[];
  protected double[][][][] data;
  protected double zoom = 1.0, magConstant = 0.0;
  protected double[] magnitude;
  protected Color[] colors;

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.Group"; }

  protected Element createElement () {
    group = new Group();
    arrows = new ElementArrow[0];
    maxColor = Color.RED;
    minColor = Color.BLUE;
    setNumberOfLevels (16);
    setAutoscaleMagnitude(true);
    return group;
  }

  protected int getPropertiesDisplacement () { return FIELD_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("data");
      infoList.add ("autoscale");
      infoList.add ("minimum");
      infoList.add ("maximum");
      infoList.add ("levels");
      infoList.add ("mincolor");
      infoList.add ("maxcolor");
      infoList.add ("zoom");
      infoList.add ("invisibleLevel");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("data"))          return "double[][][][]";
    if (_property.equals("autoscale"))     return "boolean";
    if (_property.equals("minimum"))       return "int|double";
    if (_property.equals("maximum"))       return "int|double";
    if (_property.equals("levels"))        return "int";
    if (_property.equals("mincolor"))      return "Color|Object";
    if (_property.equals("maxcolor"))      return "Color|Object";
    if (_property.equals("zoom"))          return "int|double";
    if (_property.equals("invisibleLevel"))return "int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getObject() instanceof double[][][][]) setDataArray((double[][][][])_value.getObject()); break;
      case 1 : setAutoscaleMagnitude(_value.getBoolean()); break;
      case 2 : setExtrema(_value.getDouble(),maxMagnitude); break;
      case 3 : setExtrema(minMagnitude,_value.getDouble()); break;
      case 4 : setNumberOfLevels(_value.getInteger()); break;
      case 5 : setMinColor((Color)_value.getObject()); break;
      case 6 : setMaxColor((Color)_value.getObject()); break;
      case 7 : setZoom (_value.getDouble()); break;
      case 8 : setInvisibleLevel(_value.getInteger()); break;
      default : super.setValue(_index-FIELD_PROPERTIES_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : setDataArray((double[][][][])null); break;
        case 1 : setAutoscaleMagnitude(true); break;
        case 2 : setExtrema(0.0,maxMagnitude); break;
        case 3 : setExtrema(minMagnitude,1.0); break;
        case 4 : setNumberOfLevels(16); break;
        case 5 : setMinColor(Color.BLUE); break;
        case 6 : setMaxColor(Color.RED); break;
        case 7 : setZoom (1.0); break;
        case 8 : setInvisibleLevel(-1); break;
        default: super.setDefaultValue(_index-FIELD_PROPERTIES_ADDED); break;
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
      case 8 : return null;
      default: return super.getValue (_index-FIELD_PROPERTIES_ADDED);
    }
  }

  // ------------------------------------------------
  // Preupdating and convenience methods
  // ------------------------------------------------

  public void preupdate () {
    if (dataChanged && data!=null) {
      for (int el=0, i=0,m=data.length; i<m; i++) {
        for (int j=0, n=data[0].length; j<n; j++) {
          for (int k = 0, p = data[0][0].length; k < p; k++, el++) {
            Element element = arrows[el];
            element.setX(data[i][j][k][0]);
            element.setY(data[i][j][k][1]);
            element.setZ(data[i][j][k][2]);
            element.setSizeX(data[i][j][k][3] * zoom);
            element.setSizeY(data[i][j][k][4] * zoom);
            element.setSizeZ(data[i][j][k][5] * zoom);
            magnitude[el] = data[i][j][k][6];
          }
        }
      }
      if (autoscaleMagnitude) computeMagnitudeExtrema();
      dataChanged = false;
      colorChanged = true;
    }
    if (colorChanged) {
      if (levels>0) for (int i=0,n=arrows.length; i<n; i++) {
        Color color = magToColor(magnitude[i]);
        if (color==null) { arrows[i].setVisible(false); continue; }
        arrows[i].setVisible(true);
        arrows[i].getStyle().setLineColor(color);
        arrows[i].getStyle().setFillColor(color);
      }
      else for (int i=0,n=arrows.length; i<n; i++) {
        arrows[i].setVisible(true);
        arrows[i].getStyle().setLineColor(minColor);
        arrows[i].getStyle().setFillColor(minColor);
      }
    }
    colorChanged = false;
  }

  public void setNumberOfLevels (int _lev) {
    if (_lev==levels) return;
    colorChanged = true;
    if (_lev<=0) { levels = 0; return; }
    levels = _lev;
    colors = new Color[levels];
    initColors();
    magConstant = levels/(maxMagnitude-minMagnitude);
  }

  public void setMinColor (Color _aColor) {
    if (_aColor.equals(minColor)) return;
    minColor = _aColor;
    initColors();
    colorChanged = true;
  }

  public void setMaxColor (Color _aColor) {
    if (_aColor.equals(maxColor)) return;
    maxColor = _aColor;
    initColors();
    colorChanged = true;
  }

  public void setInvisibleLevel (int _lev) {
    if (invisibleLevel == _lev) return;
    invisibleLevel = _lev;
    colorChanged = true;
  }

  public void setZoom (double _scale) {
    if (zoom==_scale) return;
    zoom = _scale;
    dataChanged = true;
  }

  public void setAutoscaleMagnitude (boolean _auto){
    if (autoscaleMagnitude == _auto) return;
    autoscaleMagnitude = _auto;
    if (autoscaleMagnitude) computeMagnitudeExtrema();
    colorChanged = true;
  }

  public void setExtrema (double min, double max){
    if (autoscaleMagnitude) { autoscaleMagnitude = false; colorChanged=true; }
    if (minMagnitude==min && maxMagnitude==max) return;
    minMagnitude = min;
    maxMagnitude = max;
    if (maxMagnitude==minMagnitude) maxMagnitude = minMagnitude + 1.0;
    magConstant = levels/(maxMagnitude-minMagnitude);
    colorChanged = true;
  }

  public void setDataArray(double[][][][] _data) {
    dataChanged = true;
    data = _data;
    if (data==null) {
      magnitude = null;
      arrows = new ElementArrow[0];
      group.removeAllElements();
      return;
    }
    int num = data.length*data[0].length*data[0][0].length;
    if (arrows.length!=num) {
      magnitude = new double[num];
      arrows = new ElementArrow[num];
      group.removeAllElements();
      for (int i=0; i<num; i++) group.addElement(arrows[i] = new ElementArrow());
    }
  }

    // ------------------ Private or protected methods

    protected void initColors () {
//    System.out.println ("Recreating colors");
      int redStart   = minColor.getRed();
      int greenStart = minColor.getGreen();
      int blueStart  = minColor.getBlue();
      int redEnd     = maxColor.getRed();
      int greenEnd   = maxColor.getGreen();
      int blueEnd    = maxColor.getBlue();
      for (int i = 0; i<levels; i++) {
        int r = (int) (redStart   + ((redEnd-redStart)*i*1.0f)/(levels-1) );
        int g = (int) (greenStart + ((greenEnd-greenStart)*i*1.0f)/(levels-1) );
        int b = (int) (blueStart  + ((blueEnd-blueStart)*i*1.0f)/(levels-1) );
        colors[i] = new Color(r, g, b);
      }
    }

    protected void computeMagnitudeExtrema() {
      double[] temp=magnitude;  // copy array reference in case the data array changes
      if (temp==null) return;
      minMagnitude=temp[0];
      maxMagnitude=temp[0];
      for (int i = 0; i <temp.length; i++) {
        double v=temp[i];
        if (v>maxMagnitude) maxMagnitude=v;
        if (v<minMagnitude) minMagnitude=v;
      }
      magConstant = levels/(maxMagnitude-minMagnitude);
//    System.out.println ("Extrema are "+minMagnitude+" "+maxMagnitude);
    }

    protected Color magToColor (double mag) {
      int index = (int)(magConstant*(mag - minMagnitude));
      if (index<=invisibleLevel) return null;
      if (index <= 0) return colors[0];
      if (index >= levels) return colors[levels-1];
      return  colors[index];
    }

} // End of class

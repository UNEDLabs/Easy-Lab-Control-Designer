/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.swing.*;
import org.colos.ejs.library.control.value.*;

import java.awt.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;

/**
 * A set of arrows that implements a simpler 2D vector field
 */
public class ControlVectorField extends ControlDrawable implements NeedsPreUpdate {
  static protected final int VECTORFIELD_ADDED=20;

  static protected final int X_COMPONENT=5;
  static protected final int Y_COMPONENT=6;
  static protected final int ANGLE_COMPONENT=8;
  static protected final int MAGNITUDE=12;

  // Configuration variables
  protected double xmin, xmax, ymin, ymax;

  protected boolean centered=false, visible=true, magnitudeSet=false;
  protected double constantLength=0.1;
  protected double sizeX=1, sizeY=1, angle = 0, magnitude=0;
  protected double[][] sizeXData, sizeYData, angleData;

  protected int arrowType=InteractiveArrow.ARROW;
  protected double lineWidth=1.0;
  protected Stroke stroke=new BasicStroke((float) lineWidth);

  protected int levels;
  protected int invisibleLevel;
  protected boolean autoscaleMagnitude = false;
  protected double[][] magData;
  protected Color maxColor,minColor;

  // Implementation variables
  private boolean positionChanged=true, sizeChanged = true, magChanged=true, angleSet=false;
  protected boolean lengthSet=false;
  protected double magConstant = 0.0, minMagnitude=0.0, maxMagnitude=1.0;

  protected ElementSet elementSet;
  protected Color[] colors;
  protected double[][] vectorLength; // used as default magnitude

  protected Drawable createDrawable () {
    elementSet = new ElementSet(1, InteractiveArrow.class);
    elementSet.setEnabled(InteractiveElement.TARGET_POSITION, false);
    elementSet.setEnabled(InteractiveElement.TARGET_SIZE, false);
    minColor = Color.BLUE;
    maxColor = Color.RED;
    xmin = xmax = ymin = ymax = Double.NaN;
    positionChanged = sizeChanged = magChanged = true;
    lengthSet = angleSet = false;
    levels = invisibleLevel = -1;
    setNumberOfLevels(16);
    return elementSet;
  }

  public String getObjectClassname () { return "org.opensourcephysics.displayejs.ElementSet"; }

  public void setParent (org.colos.ejs.library.control.swing.ControlParentOfDrawables _dp) {
    super.setParent(_dp);
    if (Double.isNaN(xmin) || Double.isNaN(xmax) || Double.isNaN(ymin) || Double.isNaN(ymax)) {
      positionChanged = true;
      preupdate();
    }
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("minimumX");
      infoList.add ("maximumX");
      infoList.add ("minimumY");
      infoList.add ("maximumY");
      infoList.add ("centered");

      infoList.add ("xcomponent");
      infoList.add ("ycomponent");
      infoList.add ("length");
      infoList.add ("angles");

      infoList.add ("autoscale");
      infoList.add ("minimum");
      infoList.add ("maximum");
      infoList.add ("magnitude");
      infoList.add ("levels");
      infoList.add ("invisibleLevel");
      infoList.add ("mincolor");
      infoList.add ("maxcolor");

      infoList.add ("visible");
      infoList.add ("style");
      infoList.add ("stroke");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("minimumX"))   return "int|double";
    if (_property.equals("maximumX"))   return "int|double";
    if (_property.equals("minimumY"))   return "int|double";
    if (_property.equals("maximumY"))   return "int|double";
    if (_property.equals("centered"))   return "boolean";

    if (_property.equals("xcomponent"))      return "int|double|double[][]";
    if (_property.equals("ycomponent"))      return "int|double|double[][]";
    if (_property.equals("length"))     return "int|double";
    if (_property.equals("angles"))      return "int|double|double[][]";

    if (_property.equals("autoscale"))     return "boolean";
    if (_property.equals("minimum"))       return "int|double";
    if (_property.equals("maximum"))       return "int|double";
    if (_property.equals("magnitude"))     return "int|double|double[][]";
    if (_property.equals("levels"))        return "int";
    if (_property.equals("invisibleLevel"))return "int";
    if (_property.equals("mincolor"))      return "Color|Object";
    if (_property.equals("maxcolor"))      return "Color|Object";

    if (_property.equals("visible"))       return "boolean";
    if (_property.equals("style"))         return "ArrowStyle|int";
    if (_property.equals("stroke"))         return "int|double|Object";

    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("ArrowStyle")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("arrow"))       return new IntegerValue (InteractiveArrow.ARROW);
      if (_value.equals("segment"))     return new IntegerValue (InteractiveArrow.SEGMENT);
      if (_value.equals("box"))         return new IntegerValue (InteractiveArrow.BOX);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (xmin!=_value.getDouble()) { xmin = _value.getDouble(); positionChanged=true; } break;
      case 1 : if (xmax!=_value.getDouble()) { xmax = _value.getDouble(); positionChanged=true; } break;
      case 2 : if (ymin!=_value.getDouble()) { ymin = _value.getDouble(); positionChanged=true; } break;
      case 3 : if (ymax!=_value.getDouble()) { ymax = _value.getDouble(); positionChanged=true; } break;
      case 4 : if (centered!=_value.getBoolean()) { centered = _value.getBoolean(); positionChanged=true; } break;

      case 5 :
        if (_value.getObject() instanceof double[][]) { sizeXData = (double[][]) _value.getObject(); sizeChanged=true; }
        else if (sizeX!=_value.getDouble()) { sizeX = _value.getDouble(); sizeChanged=true; }
        break;
      case 6 :
        if (_value.getObject() instanceof double[][]) { sizeYData = (double[][]) _value.getObject(); sizeChanged=true; }
        else if (sizeY!=_value.getDouble()) { sizeY = _value.getDouble(); sizeChanged=true; }
        break;
      case 7 :
        if (constantLength!=_value.getDouble()) { constantLength = _value.getDouble(); sizeChanged=true; }
        lengthSet=true;
        break;
      case 8 :
        if (_value.getObject() instanceof double[][]) { angleData = (double[][]) _value.getObject(); sizeChanged=true;}
        else {
          if (angle!=_value.getDouble()) { angle = _value.getDouble(); sizeChanged=true; }
          angleSet=true;
        }
        break;

      case 9 : if (autoscaleMagnitude!=_value.getBoolean()) { autoscaleMagnitude = _value.getBoolean(); magChanged=true; } break;
      case 10 : setExtrema(_value.getDouble(), maxMagnitude); break;
      case 11 : setExtrema(minMagnitude,_value.getDouble()); break;
      case 12 :
        if (_value.getObject() instanceof double[][]) { magData = (double[][]) _value.getObject(); magChanged=magnitudeSet=true; }
        else if (magnitude!=_value.getDouble()) { magnitude = _value.getDouble(); magChanged=magnitudeSet=true; }
        break;
      case 13 : setNumberOfLevels(_value.getInteger());  break;
      case 14 : if (invisibleLevel!=_value.getInteger()) { invisibleLevel = _value.getInteger(); magChanged=true; } break;
      case 15 : setMinColor ((Color)_value.getObject()); break;
      case 16 : setMaxColor ((Color)_value.getObject()); break;

      case 17 : elementSet.setVisible(visible=_value.getBoolean()); break;
      case 18 : if (arrowType!=_value.getInteger()) {// ArrowStyle
          arrowType = _value.getInteger();
          int n = elementSet.getNumberOfElements();
          for (int i=0; i<n; i++) ((InteractiveArrow)elementSet.elementAt(i)).setArrowType(arrowType);
        }
        break;
      case 19 :
        if (_value.getObject() instanceof Stroke) {
          Stroke val = (Stroke) _value.getObject();
          if (val.equals(stroke)) return;
          stroke = val;
          int n = elementSet.getNumberOfElements();
          for (int i=0; i<n; i++) elementSet.elementAt(i).getStyle().setEdgeStroke(stroke);
        }
        else if (lineWidth!=_value.getDouble()) {
          lineWidth = _value.getDouble();
          if (lineWidth<0) stroke = new BasicStroke((float) -lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,Style.DASHED_STROKE, 0);
          else stroke = new BasicStroke((float) lineWidth);
          int n = elementSet.getNumberOfElements();
          for (int i=0; i<n; i++) elementSet.elementAt(i).getStyle().setEdgeStroke(stroke);
        }
        break;

      default : super.setValue(_index-VECTORFIELD_ADDED,_value); break;
    }
    if (isUnderEjs) preupdate();
  }

  public void setDefaultValue (int _index) {
      switch (_index) {
        case 0 : xmin = Double.NaN; positionChanged=true; break;
        case 1 : xmax = Double.NaN; positionChanged=true; break;
        case 2 : ymin = Double.NaN; positionChanged=true; break;
        case 3 : ymax = Double.NaN; positionChanged=true; break;
        case 4 : centered = false; positionChanged=true; break;

        case 5 : sizeXData = null; sizeX = 1; sizeChanged=true; break;
        case 6 : sizeYData = null; sizeY = 1; sizeChanged=true; break;
        case 7 : constantLength = 0.1; lengthSet=false; sizeChanged=true; break;
        case 8 : angleData = null; angleSet=false; sizeChanged=true; break;

        case 9 : autoscaleMagnitude = true; magChanged=true; break;
        case 10 : setExtrema(0.0, maxMagnitude); break;
        case 11 : setExtrema(minMagnitude,1.0); break;
        case 12 : magData = null; magnitudeSet=false; magChanged=true; break;
        case 13 : setNumberOfLevels(16);  break;
        case 14 : invisibleLevel = -1; magChanged=true; break;
        case 15 : setMinColor (Color.BLUE); break;
        case 16 : setMaxColor (Color.RED); break;

        case 17 : elementSet.setVisible(true); break;
        case 18 : // ArrowStyle
          {
            arrowType = InteractiveArrow.ARROW;
            int n = elementSet.getNumberOfElements();
            for (int i=0; i<n; i++) ((InteractiveArrow)elementSet.elementAt(i)).setArrowType(InteractiveArrow.ARROW);
          }
          break;
        case 19 :
          {
          stroke = new java.awt.BasicStroke((float) (lineWidth=1.0));
          int n = elementSet.getNumberOfElements();
          for (int i=0; i<n; i++) elementSet.elementAt(i).getStyle().setEdgeStroke(stroke);
        }
        break;

        default: super.setDefaultValue(_index-VECTORFIELD_ADDED); break;
    }
      if (isUnderEjs) preupdate();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : 
      case 1 : 
      case 2 : 
      case 3 : return "<none>";
      case 4 : return "false";

      case 5 :
      case 6 : return "1";
      case 7 : return "0.1";
      case 8 : return "<none>";

      case 9 : return "true";
      case 10 : return "0";
      case 11 : return "1";
      case 12 : return "<none>";
      case 13 : return "16";
      case 14 : return "-1";
      case 15 : return "BLUE";
      case 16 : return "RED";

      case 17 : return "true";
      case 18 : return "ARROW";
      case 19 : return "1";
      default : return super.getDefaultValueString(_index-VECTORFIELD_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
      case 8 : case 9 : case 10 : case 11 :
      case 12 : case 13 : case 14 : case 15 :
      case 16 : case 17 : case 18 : case 19 :
      return null;
      default: return super.getValue (_index-VECTORFIELD_ADDED);
    }
  }

  // ------------------------------------------------
  // Preupdating and convenience methods
  // ------------------------------------------------

  public void preupdate () {
    if (!visible) return;
    if (myParent==null) return;
    int nx=-1, ny=-1;
    if      (sizeXData!=null) { nx = sizeXData.length; ny = sizeXData[0].length; }
    else if (sizeYData!=null) { nx = sizeYData.length; ny = sizeYData[0].length; }
    else if (angleData!=null) { nx = angleData.length; ny = angleData[0].length; }
    else if (magData!=null)   { nx = magData.length;   ny = magData[0].length; }
    if (nx<=0 || ny<=0) return;
    if (vectorLength==null || vectorLength.length!=nx || vectorLength[0].length!=ny) vectorLength = new double[nx][ny];
    if (nx*ny!=elementSet.getNumberOfElements()) {
      elementSet.setNumberOfElements(nx*ny);
      elementSet.setEnabled(InteractiveElement.TARGET_POSITION, false);
      elementSet.setEnabled(InteractiveElement.TARGET_SIZE, false);
      for (int i=0, el=0; i<nx; i++) for (int j = 0; j < ny; j++, el++) {
        InteractiveArrow arrow = (InteractiveArrow) elementSet.elementAt(el);
        arrow.getStyle().setEdgeStroke(stroke);
        arrow.setArrowType(arrowType);
      }
      positionChanged = sizeChanged = magChanged = true;
    }

    if (sizeChanged) {
      if (sizeXData!=null || sizeYData!=null || !angleSet) {
        if (lengthSet) { // Use (dx,dy) for the direction and constantLength for the length
          for (int i=0, el=0; i<nx; i++) for (int j = 0; j < ny; j++, el++) {
            double dx=sizeX,dy=sizeY;
            if (sizeXData!=null) dx = sizeXData[i][j];
            if (sizeYData!=null) dy = sizeYData[i][j];
            double length = vectorLength[i][j] = Math.sqrt(dx*dx + dy*dy);
            if (length == 0) dx = dy = 0;
            else {
              length = constantLength/length;
              dx *= length; dy *= length;
            }
            elementSet.elementAt(el).setSizeXY(dx,dy);
          }
        }
        else { // Use just (dx,dy) for the size
          double dx=sizeX,dy=sizeY;
          for (int i=0, el=0; i<nx; i++) for (int j = 0; j < ny; j++, el++) {
            if (sizeXData!=null) dx = sizeXData[i][j];
            if (sizeYData!=null) dy = sizeYData[i][j];
            vectorLength[i][j] = Math.sqrt(dx*dx+dy*dy);
            elementSet.elementAt(el).setSizeXY(dx,dy);
          }
        }
      }
      else { // angleSet=true : use polar coordinates
        double alpha=angle;
        for (int i=0, el=0; i<nx; i++) for (int j = 0; j < ny; j++, el++) {
          if (angleData!=null) alpha = angleData[i][j];
          vectorLength[i][j] = constantLength;
          elementSet.elementAt(el).setSizeXY(constantLength*Math.cos(alpha),constantLength*Math.sin(alpha));
        }
      }
    } // end sizeChanged

    if (positionChanged) { // update the position
      DrawingPanel panel = myParent.getDrawingPanel();
      double minAbcise = xmin, maxAbcise = xmax;
      double minOrdinate = ymin, maxOrdinate = ymax;
      boolean anyNaN = false;
      if (Double.isNaN(minAbcise)) { minAbcise = panel.getPreferredXMin(); anyNaN = true; }
      if (Double.isNaN(maxAbcise)) { maxAbcise = panel.getPreferredXMax(); anyNaN = true; }
      if (Double.isNaN(minOrdinate)) { minOrdinate = panel.getPreferredYMin(); anyNaN = true; }
      if (Double.isNaN(maxOrdinate)) { maxOrdinate = panel.getPreferredYMax(); anyNaN = true; }
      elementSet.canBeMeasured (!anyNaN);

      double x = minAbcise, dx = (maxAbcise-minAbcise)/(nx-1), dy = (maxOrdinate-minOrdinate)/(ny-1);
      if (centered) for (int i=0, el=0; i<nx; i++, x+=dx) {
        double y = minOrdinate;
        for (int j = 0; j < ny; j++, el++, y+=dy) {
          InteractiveElement element = elementSet.elementAt(el);
          element.setXY(x-element.getSizeX()/2,y-element.getSizeY()/2);
        }
      }
      else for (int i=0, el=0; i<nx; i++, x+=dx) {
        double y = ymin;
        for (int j = 0; j < ny; j++, el++, y+=dy) elementSet.elementAt(el).setXY(x,y);
      }
    } // end position changed

    if (magChanged) processMagnitude(nx,ny);
    positionChanged = sizeChanged = magChanged = false;
  }

  protected void processMagnitude(int nx, int ny) {
    if (levels>0) {
      double[][] useThisOne=magData;
      if (!magnitudeSet) useThisOne = vectorLength; // use vector length as magnitude
      if (useThisOne!=null) { // Color the elements according to the magnitude
          if (autoscaleMagnitude) computeMagnitudeExtrema(useThisOne);
          for (int i=0, el=0; i<nx; i++) for (int j = 0; j < ny; j++, el++) {
            InteractiveElement element = elementSet.elementAt(el);
            Color color = magToColor(useThisOne[i][j]);
            if (color == null) element.setVisible(false);
            else {
              element.setVisible(true);
              element.getStyle().setEdgeColor(color);
              element.getStyle().setFillPattern(color);
            }
          }
          return;
      }
    }
    Color color = magToColor(magnitude);
    for (int i=0, el=0; i<nx; i++) for (int j = 0; j < ny; j++, el++) {
      InteractiveElement element = elementSet.elementAt(el);
      element.setVisible(true);
      element.getStyle().setEdgeColor(color);
      element.getStyle().setFillPattern(color);
    }
  }

  public void setNumberOfLevels (int _lev) {
    if (_lev==levels) return;
    magChanged = true;
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
    magChanged = true;
  }

  public void setMaxColor (Color _aColor) {
    if (_aColor.equals(maxColor)) return;
    maxColor = _aColor;
    initColors();
    magChanged = true;
  }

  public void setExtrema (double min, double max){
    if (autoscaleMagnitude) { autoscaleMagnitude = false; magChanged=true; }
    if (minMagnitude==min && maxMagnitude==max) return;
    minMagnitude = min;
    maxMagnitude = max;
    if (maxMagnitude==minMagnitude) maxMagnitude = minMagnitude + 1.0;
    magConstant = levels/(maxMagnitude-minMagnitude);
    magChanged = true;
    if (isUnderEjs && myParent instanceof ControlDrawablesParent) ((ControlDrawablesParent)myParent).update();
  }

  protected void computeMagnitudeExtrema(double[][] temp) {
    if (temp==null) return;
    minMagnitude = Double.POSITIVE_INFINITY;
    maxMagnitude = Double.NEGATIVE_INFINITY;
    for (int i = 0; i <temp.length; i++) {
      double[] v=temp[i];
      for (int j = 0; j <v.length; j++) {
        minMagnitude = Math.min(minMagnitude,v[j]);
        maxMagnitude = Math.max(minMagnitude,v[j]);
      }
    }
    magConstant = levels/(maxMagnitude-minMagnitude);
//    System.out.println ("Extrema are "+minMagnitude+" "+maxMagnitude);
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

    protected Color magToColor (double mag) {
      if (colors==null || levels==0) return minColor;
      int index = (int)(magConstant*(mag - minMagnitude));
      if (invisibleLevel>=0 && index<=invisibleLevel) return null;
      if (index <= 0) return colors[0];
      if (index >= levels) return colors[levels-1];
      return  colors[index];
    }

/*
    public void addMenuEntries () {
        ArrayList list = new ArrayList ();
        list.add(new AbstractAction(Simulation.getEjsString("InteractiveTrace.ShowDataTable")){
          public void actionPerformed(ActionEvent e) { showDataTable(true); }
        });
        getSimulation().addElementMenuEntries (getMenuNameEntry(), list);
      }

      private GridTableFrame tableFrame=null;

      public synchronized void showDataTable(boolean show) {
         if(show) {
            if(tableFrame==null || !tableFrame.isDisplayable()) {
               if(plot.getGridData()==null) {
                  return;
               }
               tableFrame = new GridTableFrame(plot.getGridData());
               tableFrame.setTitle(this.getProperty("name")+ " Data");
               tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            }
            tableFrame.refreshTable();
            tableFrame.setVisible(true);
         } else {
            tableFrame.setVisible(false);
            tableFrame.dispose();
            tableFrame = null;
         }
       }
      }
*/

} // End of class

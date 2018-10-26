/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display2d.VectorColorMapper;

/**
 * A group of arrows that implements a simpler 2D vector field
 */
public class VectorField extends Group {
  
  // Configuration variables
  protected double minimumX=Double.NaN, maximumX=Double.NaN, minimumY=Double.NaN, maximumY=Double.NaN;
  protected double vectorSizeX=1, vectorSizeY=1, vectorAngle = Double.NaN, vectorMagnitude=Double.NaN;
  protected double[][] vectorSizeXData, vectorSizeYData, vectorAngleData, vectorMagnitudeData;

  protected boolean autoscaleMagnitude = false;
  protected boolean useColorMapper=false;
  protected double constantLength=Double.NaN;
  protected int levels=-1, invisibleLevel=-1;
  protected Color maxColor=Color.RED,minColor=Color.BLUE;

  // Implementation variables
  private boolean positionChanged=true, sizeChanged = true, magChanged=true;
  protected double magConstant = 0.0, minMagnitude=0.0, maxMagnitude=1.0;
  protected double useMinX, useMaxX, useMinY, useMaxY;

  protected Color[] colors;
  protected VectorColorMapper mapper=new VectorColorMapper(16, 1.0);
  protected double[][] vectorLength; // used as default magnitude
  protected int nX = -1, nY = -1;
  private ElementArrow invisibleElement;
  private boolean showLegend;
  private int arrowType=ElementArrow.ARROW;
  
  public VectorField () {
    invisibleElement = new ElementArrow();
    addElement(invisibleElement);
    setNumberOfLevels(16);
  }

// ------------------------------------------------
// Setters and Getters
// ------------------------------------------------

  public void setMinimumX (double min) { 
    if (min!=minimumX) { minimumX = min; positionChanged = true; } 
  }
  
  public void setMaximumX (double max) { 
    if (max!=maximumX) { maximumX = max; positionChanged = true; } 
  }
  
  public void setMinimumY (double min) { 
    if (min!=minimumY) { minimumY = min; positionChanged = true; } 
  }

  public void setMaximumY (double max) { 
    if (max!=maximumY) { maximumY = max; positionChanged = true; } 
  }

  public void setVectorSizeXData(double[][] data) {
    vectorSizeXData = data;
    sizeChanged=true; 
    checkArrays();
  }

  public void setVectorSizeX(double size) { 
    if (vectorSizeX!=size) {
      vectorSizeX = size; 
      sizeChanged=true; 
    }
  }
  
  public void setVectorSizeYData(double[][] data) { 
    vectorSizeYData = data;
    sizeChanged=true; 
    checkArrays();
  }

  public void setVectorSizeY(double size) { 
    if (vectorSizeY!=size) {
      vectorSizeY = size; 
      sizeChanged=true;
    }
  }

  public void setVectorAngleData(double[][] data) { 
    vectorAngleData = data; 
    sizeChanged=true; 
    checkArrays();
  }
  
  public void setVectorAngle(double size) { 
    if (vectorAngle!=size) { vectorAngle = size; sizeChanged=true; }
  }

  public void setConstantLength(double length) { 
    if (constantLength!=length) { constantLength = length; sizeChanged=true; }
  }
  
  public void setAutoscaleMagnitude (boolean scale) { 
    if (autoscaleMagnitude!=scale) {
      autoscaleMagnitude = scale;
      magChanged = true;
    }
  }
  
  public void setMagnitudeExtrema (double min, double max) {
    if (autoscaleMagnitude) { autoscaleMagnitude = false; magChanged=true; }
    if (minMagnitude==min && maxMagnitude==max) return;
    minMagnitude = min;
    maxMagnitude = max;
    if (maxMagnitude==minMagnitude) maxMagnitude = minMagnitude + 1.0;
    magConstant = levels/(maxMagnitude-minMagnitude);
    mapper.setScale(maxMagnitude);
    mapper.updateLegend();
    magChanged = true;
  }

  public double getMagnitudeMinimum() { return minMagnitude; }
  
  public double getMagnitudeMaximum() { return maxMagnitude; }
  
  public void setMagnitudeData(double[][] data) { 
    vectorMagnitudeData = data;  
    magChanged=true; 
    checkArrays();
  }
  
  public void setMagnitude(double magnitude) { 
    if (vectorMagnitude!=magnitude) { vectorMagnitude = magnitude; magChanged=true; }
  }  
  
  public void setNumberOfLevels (int _lev) {
    if (_lev==levels) return;
    magChanged = true;
    if (_lev<=0) { levels = 0; return; }
    levels = _lev;
    colors = new Color[levels];
    initColors();
    magConstant = levels/(maxMagnitude-minMagnitude);
    mapper.setNumberOfColors(levels);
    mapper.updateLegend();
  }

  public void setMinColor (Color _aColor) {
    if (_aColor.equals(minColor)) return;
    minColor = _aColor;
    initColors();
    magChanged = true;
//    mapper.setFloorCeilColor(minColor, maxColor);
//    mapper.updateLegend();
  }

  public void setMaxColor (Color _aColor) {
    if (_aColor.equals(maxColor)) return;
    maxColor = _aColor;
    initColors();
    magChanged = true;
//    mapper.setFloorCeilColor(minColor, maxColor);
//    mapper.updateLegend();
  }

  public void setInvisibleLevel(int level) { 
    if (invisibleLevel!=level) { 
      invisibleLevel = level; 
      magChanged=true; 
    }
  }

  public void setArrowType (int type) {
    arrowType = type;
    for (Element element : getElements()) ((ElementArrow)element).setArrowType(type);
  }
  
  public void setRelativePosition (int position) {
    for (Element element : getElements()) element.getStyle().setRelativePosition(position);
  }
  
  public void setUseColorMapper(boolean _do) {
    useColorMapper = _do;
    setShowLegend(showLegend);
//    mapper.setFloorCeilColor(minColor, maxColor);
//    mapper.setNumberOfColors(levels);
    mapper.updateLegend();
    magChanged = true;
  }
  
  public void setMapperPaletteType(int _type) {
    mapper.setPaletteType(_type);
  }

  public VectorColorMapper getColorMapper() { return this.mapper; }
  
  public void setShowLegend(boolean _visible) {
    showLegend = _visible;
    javax.swing.JFrame legendFrame = mapper.getLegendFrame();
    if (legendFrame==null) legendFrame = mapper.showLegend();
    legendFrame.setVisible(useColorMapper && _visible);
  }
  
  /**
   * Gets the x coordinate for the given index.
   *
   * @param i int
   * @return double the x coordinate
   */
  public double indexToX(int i) {
    if (i<0 || i>=nX) return Double.NaN;
    checkExtrema(); 
    if (nX==1) return (useMinX+useMaxX)/2;
    return useMinX + i*(useMaxX-useMinX)/(nX-1);
  }

  /**
   * Gets the y coordinate for the given index.
   *
   * @param i int
   * @return double the y coordinate
   */
  public double indexToY(int i) {
    if (i<0 || i>=nY) return Double.NaN;
    checkExtrema(); 
    if (nY==1) return (useMinY+useMaxY)/2;
    return useMinY + i*(useMaxY-useMinY)/(nY-1);
  }

  // -------------------------
  // Override super methods
  // -------------------------
  
  @Override
  public boolean isMeasured() {
    if (Double.isNaN(minimumX) || Double.isNaN(maximumX) || Double.isNaN(minimumY) || Double.isNaN(maximumY)) return false; 
    return canBeMeasured;
  }

  @Override
  public double getXMin() { return minimumX; } 

  @Override
  public double getXMax() { return maximumX; } 
  
  @Override
  public double getYMin() { return minimumY; } 
  
  @Override
  public double getYMax() { return maximumY; }
  
  @Override
  public double getXMinLogscale() { return minimumX>0 ? minimumX : Double.NaN; } 

  @Override
  public double getXMaxLogscale() { return maximumX>0 ? maximumX : Double.NaN; } 
  
  @Override
  public double getYMinLogscale() { return minimumY>0 ? minimumY : Double.NaN; } 

  @Override
  public double getYMaxLogscale() { return maximumY>0 ? maximumY : Double.NaN; } 

  @Override
  public void draw (org.opensourcephysics.display.DrawingPanel _panel, Graphics _g) {
    if (prepareField()) super.draw(_panel, _g);
  }

  // ------------------------------
  // private methods
  // ------------------------------

//  public void echo() {
//    if (getPanel()==null) return;
//    org.opensourcephysics.display.DrawingPanel panel = (org.opensourcephysics.display.DrawingPanel) getPanel().getComponent();
//    System.out.println (" panel min ="+panel.getPreferredXMin());
//    System.out.println (" minimuX="+minimumX);
//  }

  /**
   * computes the real extrema that will be used for drawing.
   * Different from the given extrema if any of them is NaN (i.e. it is not set)
   */
  private void checkExtrema() {
//    System.out.println (" minimuX="+minimumX);
    org.opensourcephysics.display.DrawingPanel panel = (org.opensourcephysics.display.DrawingPanel) getPanel().getComponent();
    useMinX = Double.isNaN(minimumX) ? panel.getPreferredXMin() : minimumX;
    useMaxX = Double.isNaN(maximumX) ? panel.getPreferredXMax() : maximumX;
    useMinY = Double.isNaN(minimumY) ? panel.getPreferredYMin() : minimumY;
    useMaxY = Double.isNaN(maximumY) ? panel.getPreferredYMax() : maximumY;
//    System.out.println (" useMinX="+useMinX);
  }

  private void checkArraySize(double[][] data) {
    if (data!=null) { 
      nX = Math.min(nX,data.length); 
      nY = Math.min(nY,data[0].length); 
    }
  }

  private boolean checkArrays() {
    nX = nY = Integer.MAX_VALUE;
    checkArraySize(vectorSizeXData);
    checkArraySize(vectorSizeYData);
    checkArraySize(vectorAngleData);
    checkArraySize(vectorMagnitudeData);
    if (nX==Integer.MAX_VALUE) nX = 0; 
    if (nY==Integer.MAX_VALUE) nY = 0;
    if (nX<=0 || nY<=0) return false;

    if (vectorLength==null || vectorLength.length!=nX || vectorLength[0].length!=nY) vectorLength = new double[nX][nY];
    int total = nX*nY;
    if (total!=(getElements().size()-1)) setNumberOfElements(total);
    return true;
  }
  
  private boolean isAngleSet() {
    if (vectorAngleData==null && Double.isNaN(vectorAngle)) return false;
    return true;
  }
  
  private boolean isMagnitudeSet() {
    if (vectorMagnitudeData==null && Double.isNaN(vectorMagnitude)) return false;
    return true;
  }

  private void setNumberOfElements(int newNumber) {
    // remember the style of one of the previous arrows
    Style oldStyle = invisibleElement.getStyle();

    // rebuild the group
    removeAllElements();
    addElement(invisibleElement);
    for (int i = 1; i <= newNumber; i++) {
      ElementArrow arrow = new ElementArrow();
      oldStyle.copyTo(arrow);
      arrow.setArrowType(arrowType);
      addElementAtIndex(i,arrow);
    }
    positionChanged = sizeChanged = magChanged = true;
  }

  public boolean prepareField() {
    if (!isReallyVisible()) return false;
    if (nX<=0 || nY<=0) return false;
    if (sizeChanged) {
      if (vectorSizeXData!=null || vectorSizeYData!=null || !isAngleSet()) {
        if (Double.isNaN(constantLength)) { // Use just (dx,dy) for the size
          double dx=vectorSizeX, dy=vectorSizeY;
          for (int i=0, el=1; i<nX; i++) for (int j = 0; j < nY; j++, el++) {
            if (vectorSizeXData!=null) dx = vectorSizeXData[i][j];
            if (vectorSizeYData!=null) dy = vectorSizeYData[i][j];
            vectorLength[i][j] = Math.sqrt(dx*dx+dy*dy);
            getElement(el).setSizeXY(dx,dy);
          }
        }
        else { // Use (dx,dy) for the direction and constantLength for the length
          double dx=vectorSizeX,dy=vectorSizeY;
          for (int i=0, el=1; i<nX; i++) for (int j = 0; j < nY; j++, el++) {
            if (vectorSizeXData!=null) dx = vectorSizeXData[i][j];
            if (vectorSizeYData!=null) dy = vectorSizeYData[i][j];
            double length = vectorLength[i][j] = Math.sqrt(dx*dx + dy*dy);
            if (length == 0) dx = dy = 0;
            else {
              length = constantLength/length;
              dx *= length; dy *= length;
            }
            getElement(el).setSizeXY(dx,dy);
          }
        }
      }
      else { // angleSet=true : use polar coordinates
        double alpha=vectorAngle;
        for (int i=0, el=1; i<nX; i++) for (int j = 0; j < nY; j++, el++) {
          if (vectorAngleData!=null) alpha = vectorAngleData[i][j];
          vectorLength[i][j] = constantLength;
          getElement(el).setSizeXY(constantLength*Math.cos(alpha),constantLength*Math.sin(alpha));
        }
      }
      if (!isMagnitudeSet()) magChanged = true;
    } // end sizeChanged

    double oldUseMinX = useMinX, oldUseMaxX = useMaxX, oldUseMinY = useMinY, oldUseMaxY = useMaxY;
    checkExtrema();
    if (positionChanged || oldUseMinX!=useMinX || oldUseMaxX!=useMaxX || oldUseMinY!=useMinY || oldUseMaxY!=useMaxY) { // update the position
      double startX, dx, startY, dy;
      if (nX<=1) {  startX = (useMinX+useMaxX)/2; dx = 0; }
      else { startX = useMinX; dx = (useMaxX-useMinX)/(nX-1);  }
      if (nY<=1) {  startY = (useMinY+useMaxY)/2; dy = 0; }
      else { startY = useMinY; dy = (useMaxY-useMinY)/(nY-1);  }
      for (int i=0, el=1; i<nX; i++) {
        double x = startX + i*dx;
        for (int j = 0; j < nY; j++, el++) getElement(el).setXY(x,startY+j*dy);
      }
    } // end position changed

    if (magChanged) processMagnitude();
    positionChanged = sizeChanged = magChanged = false;
    invisibleElement.setVisible(false); // keep this one hidden
    return true;
  }

  private void processMagnitude() {
    Component panel = getPanel().getComponent();
    if (panel instanceof PlottingPanel) mapper.checkPallet(((PlottingPanel)panel).getAxes().getInteriorBackground());
    else mapper.checkPallet(panel.getBackground());
    if (levels>0) {
      double[][] magArray = isMagnitudeSet() ? vectorMagnitudeData : vectorLength;
      if (magArray!=null) { // Color the elements according to the magnitude
          if (autoscaleMagnitude) computeMagnitudeExtrema(magArray);
          for (int i=0, el=1; i<nX; i++) for (int j = 0; j < nY; j++, el++) {
            Element element = getElement(el);
            double mag = magArray[i][j];
            Color color = magToColor(mag);
            if (color == null) {
              element.setVisible(false);
            }
            else {
              element.setVisible(true);
              element.getStyle().setLineColor(color);
              element.getStyle().setFillColor(color);
              element.getStyle().setExtraColor(magToCompColor(mag));
            }
          }
          return;
      }
    }
    Color color = Double.isNaN(vectorMagnitude) ? minColor : magToColor(vectorMagnitude);
    Color extraColor = Double.isNaN(vectorMagnitude) ? maxColor : magToCompColor(vectorMagnitude);
    for (int i=0, el=1; i<nX; i++) for (int j = 0; j < nY; j++, el++) {
      Element element = getElement(el);
      element.setVisible(true);
      element.getStyle().setLineColor(color);
      element.getStyle().setFillColor(color);
      element.getStyle().setExtraColor(extraColor);
    }
  }


  private void computeMagnitudeExtrema(double[][] temp) {
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
    mapper.setScale(maxMagnitude);
    mapper.updateLegend();
  }

  private void initColors () {
    int redStart   = minColor.getRed();
    int greenStart = minColor.getGreen();
    int blueStart  = minColor.getBlue();
    int alphaStart = minColor.getAlpha();
    int redEnd     = maxColor.getRed();
    int greenEnd   = maxColor.getGreen();
    int blueEnd    = maxColor.getBlue();
    int alphaEnd   = maxColor.getAlpha();
    for (int i = 0; i<levels; i++) {
      int r = (int) (redStart   + ((redEnd-redStart)*i*1.0f)/(levels-1) );
      int g = (int) (greenStart + ((greenEnd-greenStart)*i*1.0f)/(levels-1) );
      int b = (int) (blueStart  + ((blueEnd-blueStart)*i*1.0f)/(levels-1) );
      int a = (int) (alphaStart + ((alphaEnd-alphaStart)*i*1.0f)/(levels-1) );
      colors[i] = new Color(r, g, b, a);
    }
  }

  private Color magToColor (double mag) {
    if (useColorMapper) return mapper.doubleToColor(mag);
    if (colors==null || levels==0) return minColor;
    int index = (int)(magConstant*(mag - minMagnitude));
    if (invisibleLevel>=0 && index<=invisibleLevel) return null;
    if (index <= 0) return colors[0];
    if (index >= levels) return colors[levels-1];
    return  colors[index];
  }
  
  private Color magToCompColor (double mag) {
    if (useColorMapper) return mapper.doubleToCompColor(mag);
    if (colors==null || levels==0) return maxColor;
    int index = (int)(magConstant*(maxMagnitude - mag));
    if (invisibleLevel>=0 && index<=invisibleLevel) return null;
    if (index <= 0) return colors[0];
    if (index >= levels) return colors[levels-1];
    return  colors[index];
  }

} // End of class

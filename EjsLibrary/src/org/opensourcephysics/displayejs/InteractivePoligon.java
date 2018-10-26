/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.*;
import java.awt.geom.*;
import org.opensourcephysics.display.*;

public class InteractivePoligon extends AbstractInteractiveElement implements Data {
  static public final int PLAIN  = 0;
  static public final int CIRCLE = 1;
  static public final int DIAMOND = 2;
  static public final int SQUARE = 3;
  static public final int ARROW  = 4;
  static public final int LINE   = 5;
  static public final int FILLED_CIRCLE  = 6;
  static public final int FILLED_DIAMOND = 7;
  static public final int FILLED_SQUARE  = 8;
  static public final int FILLED_ARROW   = 9;

  // Configuration variables
  protected boolean closed = true;
  protected int numPoints=-1, startType=PLAIN, endType=PLAIN;
  protected double neumaticDash = 0, startSize=Double.NaN, endSize=Double.NaN;
  protected double coordinates[][] = null;
  protected boolean connect[] = null, pointSizeEnabled[] = null;
  protected Color lineColors[] = null;
  protected int[] shapeType=null;
  protected Color[] shapeEdgeColor=null, shapeFillColor=null;
  protected int[] shapeSize=null;

  // Implementation variables
  protected int sides = 0, dashSize=0, theStartSize=0, theEndSize=0;
  protected int aPoints[]=null, bPoints[]=null;
  protected double center[] = new double[3]; // The center of the polygon
  protected double pixel[]  = new double[3], pixelOrigin[] = new double[5]; // Output of panel's projections
  protected double point[]  = new double[3], origin[] = new double[6], size[] = new double[3]; // Auxiliary arrays

  protected Object3D[] lineObjects=null;  // Objects3D for each of the lines
  protected Object3D[] closedObject= new Object3D[] { new Object3D(this,-1) }; // A special object for a closed polygon
  protected InteractionTargetPoligonPoint targetPoint = new InteractionTargetPoligonPoint(this,-1);

  protected Shape[] shape=null;
  private boolean showZ = false, allowTable=false;
  protected String name="polygon";

  public InteractivePoligon () {
    setXYZ(0.0,0.0,0.0);
    setSizeXYZ(1.0,1.0,1.0);
  }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractivePoligon) {
      InteractivePoligon old = (InteractivePoligon) _element;
      setNumberOfPoints(old.getNumberOfPoints());
      setClosed(old.isClosed());
      setData(old.getData());
      for (int i=0; i<numPoints; i++) connect[i] = old.connect[i];
      for (int i=0; i<numPoints; i++) pointSizeEnabled[i] = old.pointSizeEnabled[i];
      for (int i=0; i<numPoints; i++) shapeType[i] = old.shapeType[i];
      for (int i=0; i<numPoints; i++) shapeSize[i] = old.shapeSize[i];
      for (int i=0; i<numPoints; i++) shapeEdgeColor[i] = old.shapeEdgeColor[i];
      for (int i=0; i<numPoints; i++) shapeFillColor[i] = old.shapeFillColor[i];
      for (int i=0; i<numPoints; i++) shape[i] = old.shape[i];
      setName(((InteractivePoligon)_element).getName());
      setAllowTable(((InteractivePoligon)_element).allowTable);
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  public void setNumberOfPoints (int _n) {
    if (_n==numPoints) return;
    if (_n<1) return;
    numPoints = _n;
    sides = numPoints-1;
    coordinates  = new double [3][numPoints];
    connect = new boolean [numPoints];
    pointSizeEnabled = new boolean [numPoints];
    aPoints = new int [numPoints];
    bPoints = new int [numPoints];
    lineObjects = new Object3D[numPoints];
    lineColors = new Color[numPoints];
    shapeType = new int[numPoints];
    shapeSize = new int[numPoints];
    shapeEdgeColor = new Color[numPoints];
    shapeFillColor = new Color[numPoints];
    shape = new Shape[numPoints];
    for (int i=0; i<numPoints; i++) {
      coordinates[0][i] = coordinates[1][i] = coordinates[2][i] = 0.0;
      connect[i] = true;
      lineColors[i] = null; // Not set
      pointSizeEnabled[i] = true;
      // each of the lines, including the closing line
      lineObjects[i] = new Object3D(this,i);
      shapeType[i] = InteractiveParticle.NONE;
      shapeSize[i] = 10;
      shapeEdgeColor[i] = Color.BLACK;
      shapeFillColor[i] = Color.RED;
      shape[i] = null;
    }
    connect[sides] = closed;
    hasChanged = true;
  }
  public int getNumberOfPoints () {  return numPoints; }

  public void setStartType (int _type) {
    if (_type==startType) return;
    startType = _type;
    hasChanged = true;
  }
  public int getStartType () {  return startType; }

  public void setStartSize (double _size) {
    if (_size==startSize) return;
    startSize = _size;
    hasChanged = true;
  }
  public double getStartSize () {  return startSize; }

  public void setEndType (int _type) {
    if (_type==endType) return;
    endType = _type;
    hasChanged = true;
  }
  public int getEndType () {  return endType; }

  public void setEndSize (double _size) {
    if (_size==endSize) return;
    endSize = _size;
    hasChanged = true;
  }
  public double getEndSize () {  return endSize; }

  public void setNeumatic (double _dash) {
    if (neumaticDash==_dash) return;
    neumaticDash = _dash;
    hasChanged = true;
  }

  public void setClosed (boolean _closed) {
    closed = _closed;
    if (sides>0) connect[sides] = closed;
    hasChanged = true;
  }
  public boolean isClosed () {  return closed; }

  public void setAllowTable (boolean _allow) { this.allowTable = _allow; }

  public void setData (double[][] _data) {
    if (numPoints!=_data.length) setNumberOfPoints(_data.length);
    int maxPoints = Math.min(_data.length,numPoints);
    int n = Math.min(_data[0].length,3);
    for (int i=0; i<maxPoints; i++)  for (int k=0; k<n; k++) coordinates[k][i] = _data[i][k];
    hasChanged = true;
  }

  /**
   * Be warned! Data is stored as coordinates[3][numPoints] or coordinates[2][numPoints]
   * @return double[][]
   */
  public double[][] getData () { return coordinates; }

  public double[] getPoint (int _index) {
    double[] thePoint = new double[coordinates.length];
    for (int i = 0; i < coordinates.length; i++) thePoint[i] = coordinates[i][_index];
    return thePoint;
  }

  public void setXs (double[] _data) {
//    if (numPoints!=_data.length) setNumberOfPoints(_data.length);
    for (int i=0, n=Math.min(_data.length,numPoints); i<n; i++)  coordinates[0][i] = _data[i];
    hasChanged = true;
  }
  public void setXs (double _data) {
    for (int i=0; i<numPoints; i++)  coordinates[0][i] = _data;
    hasChanged = true;
  }

  public void setYs (double[] _data) {
//    if (numPoints!=_data.length) setNumberOfPoints(_data.length);
    for (int i=0, n=Math.min(_data.length,numPoints); i<n; i++)  coordinates[1][i] = _data[i];
    hasChanged = true;
  }
  public void setYs (double _data) {
    for (int i=0; i<numPoints; i++)  coordinates[1][i] = _data;
    hasChanged = true;
  }

  public void setZs (double[] _data) {
//    if (numPoints!=_data.length) setNumberOfPoints(_data.length);
    for (int i=0, n=Math.min(_data.length,numPoints); i<n; i++)  coordinates[2][i] = _data[i];
    showZ = true;
    hasChanged = true;
  }
  public void setZs (double _data) {
    for (int i=0; i<numPoints; i++)  coordinates[2][i] = _data;
    showZ = true;
    hasChanged = true;
  }

  public void setConnections (boolean[] _c) {
    if (_c==null) for (int i=0; i<numPoints; i++)  connect[i] = true;
    else for (int i=0; i<numPoints; i++)  connect[i] = _c[i];
    if (connect!=null) connect[sides] = closed;
    hasChanged = true;
  }

  public void setConnected (int _index, boolean _c) {
    if (_index<numPoints)  connect[_index] = _c;
    hasChanged = true;
  }

  public void setPointSizeEnableds (boolean[] _enabled) {
    for (int i=0, n=Math.min(_enabled.length,numPoints); i<n; i++)  pointSizeEnabled[i] = _enabled[i];
  }

  public void setPointSizeEnabled (int _index, boolean _enabled) {
    if (_index>-1 && _index<numPoints)  pointSizeEnabled[_index] = _enabled;
  }

/**
 * Sets the color of each individual line of the polygon
 * Notice that setting the colors to a non-null array masks the style edge color.
 * Setting the colors to a null array makes the element use the style edge color.
 * @param _c Color[]
 */
  public void setColors (Color[] _c) {
    if (_c==null) for (int i=0; i<numPoints; i++)  lineColors[i] = null;
    else for (int i=0, n=Math.min(_c.length,numPoints); i<n; i++)  lineColors[i] = _c[i];
  }

  public void setName (String _name) { this.name = _name; }
  public String getName () { return this.name; }

  // ----------------------------------------------------
  // Implementation of org.opensourcephysics.display.Data
  // ----------------------------------------------------

  /** an integer ID that identifies this object */
  protected int datasetID = hashCode();
//  private Dataset dataset = null;

  /**
   * Sets the ID number of this Data.
   *
   * @param id the ID number
   */
  public void setID(int id) {
    datasetID = id;
  }

  /**
   * Returns a unique identifier for this Data.
   *
   * @return the ID number
   */
  public int getID() {
    return datasetID;
  }

  public double[][] getData2D() {
    double[][] data;
    synchronized(coordinates) {
      if (showZ) {
        data = new double[3][numPoints];
        for (int i=0; i<numPoints; i++) {
          data[0][i] = coordinates[0][i];
          data[1][i] = coordinates[1][i];
          data[2][i] = coordinates[2][i]; 
        }
      }
      else {
        data = new double[2][numPoints];
        for (int i=0; i<numPoints; i++) {
          data[0][i] = coordinates[0][i];
          data[1][i] = coordinates[1][i];
        }
      }
    }
    return data;
  }

  public double[][][] getData3D() { return null; }

  public String[] getColumnNames() { return showZ ? new String[]{"x","y","z"} : new String[]{"x","y"}; }

  public Color[] getLineColors() { 
    return new Color[] { Color.BLACK, getStyle().getEdgeColor()}; 
  }

  public Color[] getFillColors() { 
    return new Color[] { Color.BLACK, new Color(255,128,128,128)};
  }

  public java.util.List<Data> getDataList() { return null; }

  public java.util.ArrayList<Dataset>  getDatasets() { return null; }
//    if (dataset==null) dataset = new Dataset();
//    else dataset.clear();
//    dataset.setName(getName());
//    dataset.setConnected (true);
//    dataset.setLineColor(getLineColor());
//    dataset.setMarkerShape(Dataset.SQUARE);
//    dataset.setMarkerColor(getFillColor(),getLineColor());
//    double[][] data = getData2D();
//    for (int i=0,n=data.length; i<n; i++) dataset.append(data[i][0], data[i][1]);
//    java.util.ArrayList<Dataset> datasetList = new java.util.ArrayList<Dataset>();
//    datasetList.add(dataset);
//    return datasetList;    
//  }

// ------------------------
// Shapes
// ------------------------

  public void setShapesType (int[] _c) {
    if (_c==null) for (int i=0; i<numPoints; i++)  createShape(i,InteractiveParticle.NONE);
    else for (int i=0, n=Math.min(_c.length,numPoints); i<n; i++)  createShape(i,_c[i]);
  }
  public void setShapesType (int _c) {
    for (int i=0; i<numPoints; i++)  createShape(i,_c);
  }

  public void setShapesSize (int[] _c) {
    if (_c==null) for (int i=0; i<numPoints; i++)  shapeSize[i] = 10;
    else for (int i=0, n=Math.min(_c.length,numPoints); i<n; i++)  shapeSize[i] = _c[i];
  }
  public void setShapesSize (int _c) {
    for (int i=0; i<numPoints; i++)  shapeSize[i] = _c;
  }

  public void setShapesEdgeColor (Object[] _c) {
    if (_c==null) for (int i=0; i<numPoints; i++)  shapeEdgeColor[i] = Color.BLACK;
    else for (int i=0, n=Math.min(_c.length,numPoints); i<n; i++)  shapeEdgeColor[i] = (Color) _c[i];
  }
  public void setShapesEdgeColor (Color _c) {
    for (int i=0; i<numPoints; i++)  shapeEdgeColor[i] = _c;
  }

  public void setShapesFillColor (Object[] _c) {
    if (_c==null) for (int i=0; i<numPoints; i++)  shapeFillColor[i] = Color.RED;
    else for (int i=0, n=Math.min(_c.length,numPoints); i<n; i++)  shapeFillColor[i] = (Color) _c[i];
  }
  public void setShapesFillColor (Color _c) {
    for (int i=0; i<numPoints; i++)  shapeFillColor[i] = _c;
  }

  private void createShape (int index, int _type) {
    if (shapeType[index]==_type) return;
    shapeType[index] = _type;
    switch (shapeType[index]) {
      default :
      case InteractiveParticle.NONE            : shape[index] = null; break;
      case InteractiveParticle.ELLIPSE         : shape[index] = new Ellipse2D.Float(); break;
      case InteractiveParticle.RECTANGLE       : shape[index] = new Rectangle2D.Float(); break;
      case InteractiveParticle.ROUND_RECTANGLE : shape[index] = new RoundRectangle2D.Float(); break;
    }
  }

// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------


  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    if (!(numPoints>0 && visible)) return null;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints(_panel);
    if (sizeEnabled) {
      for (int i=0; i<numPoints; i++)
        if (pointSizeEnabled[i] && Math.abs(aPoints[i]-_xpix)<SENSIBILITY
            && Math.abs(bPoints[i]-_ypix)<SENSIBILITY) return new InteractionTargetPoligonPoint(this,i);
    }
    if (positionEnabled) {
      for (int i=0; i<numPoints; i++)
        if (Math.abs(aPoints[i]-_xpix)<SENSIBILITY && Math.abs(bPoints[i]-_ypix)<SENSIBILITY) return new InteractionTargetPoligonMovingPoint(this,i);
      // if (closed && Math.abs(pixelOrigin[0]-_xpix)<SENSIBILITY && Math.abs(pixelOrigin[1]-_ypix)<SENSIBILITY) return new InteractionTargetElementPosition(this);
    }
    if (allowTable) {
      for (int i=0; i<numPoints; i++) {
        if (Math.abs(aPoints[i]-_xpix)<SENSIBILITY && Math.abs(bPoints[i]-_ypix)<SENSIBILITY) return this;
      }
    }
    return null;
   }

  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    if (!(numPoints>0 && visible)) return null;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints(_panel);
    if (closed && style.fillPattern!=null) return closedObject;
    return lineObjects;
  }

  public void draw (DrawingPanel3D _panel, Graphics2D _g2, int _index) {
    if (_index<0) { // Interior ==> closed = true and fillPattern!=null
      java.awt.Paint theFillPattern = style.fillPattern;
      if (theFillPattern instanceof Color) theFillPattern = _panel.projectColor((Color) theFillPattern,closedObject[0].distance);
      _g2.setPaint(theFillPattern);
      _g2.setStroke(style.edgeStroke);
      _g2.fillPolygon(aPoints,bPoints,numPoints);
      if (style.edgeColor==null && lineColors[0]==null) return; // Then draw noting else and just return
      if (connect[0]) {
        if (lineColors[0]!=null) _g2.setColor(_panel.projectColor(lineColors[0],lineObjects[0].distance));
        else _g2.setColor(_panel.projectColor(style.edgeColor,lineObjects[0].distance));
        drawStart(_g2);
      }
      for (int i=1; i<sides; i++) {
        if (connect[i]) {
          if (lineColors[i]!=null) _g2.setColor(_panel.projectColor(lineColors[i],lineObjects[i].distance));
          else _g2.setColor(_panel.projectColor(style.edgeColor,lineObjects[i].distance));
          drawLine(_g2, aPoints[i], bPoints[i], aPoints[i + 1], bPoints[i + 1]);
        }
      }
      if (connect[sides]) {
        if (lineColors[sides]!=null) _g2.setColor(_panel.projectColor(lineColors[sides],lineObjects[sides].distance));
        else _g2.setColor(_panel.projectColor(style.edgeColor,lineObjects[sides].distance));
        drawEnd(_g2, aPoints[sides], bPoints[sides], aPoints[0], bPoints[0]);
      }
      return;
    }  // end of index<0

    if (connect[_index] && (lineColors[_index]!=null || style.edgeColor!=null) ) {
      if (lineColors[_index]!=null) _g2.setColor (_panel.projectColor(lineColors[_index],lineObjects[_index].distance));
      else _g2.setColor (_panel.projectColor(style.edgeColor,lineObjects[_index].distance));
      _g2.setStroke(style.edgeStroke);
      if (_index==0) {
        if (numPoints==2) drawStartAndEnd(_g2);
        else drawStart(_g2);
      }
      else if (_index<sides-1) drawLine(_g2, aPoints[_index], bPoints[_index], aPoints[_index+1], bPoints[_index+1]);
      else if (_index<sides) { // i.e. index==sides-1
        if (closed) drawLine(_g2, aPoints[_index], bPoints[_index], aPoints[sides], bPoints[sides]);
        else        drawEnd (_g2, aPoints[_index], bPoints[_index], aPoints[sides], bPoints[sides]);
      }
      else { // _index==sides, last closing segment
        drawEnd (_g2,aPoints[_index],bPoints[_index],aPoints[0],bPoints[0]);
      }
    }
    // Now draw the shape
    if (shapeType[_index]!=InteractiveParticle.NONE) drawMarker (_g2, aPoints[_index], bPoints[_index], _index);
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (!(numPoints>0 && visible)) return;
    Graphics2D g2 = (Graphics2D) _g;
    // if (hasChanged || _panel!=panelWithValidProjection)
    projectPoints(_panel);
    g2.setStroke(style.edgeStroke);
    // Draw the interior, if closed
    if (closed && style.fillPattern!=null) {
      g2.setPaint(style.fillPattern);
      g2.fillPolygon(aPoints, bPoints, numPoints);
    }
    if (style.edgeColor!=null || lineColors[0]!=null) {
      if (lineColors[0]!=null) g2.setColor(lineColors[0]);
      else g2.setColor(style.edgeColor);
      if (numPoints==2) { // A very special case
        if (connect[0]) drawStartAndEnd(g2);
        drawMarkers(g2);
        return;
      }
      if (connect[0]) drawStart (g2);
      int last = sides, first = 0;
      if (!closed) { last = sides-1; first = sides; }

      for (int i=1; i<last; i++) {
        if (connect[i]) {
          if (lineColors[i]!=null) g2.setColor(lineColors[i]);
          else g2.setColor(style.edgeColor);
          drawLine(g2, aPoints[i], bPoints[i], aPoints[i + 1], bPoints[i + 1]);
        }
      }
      if (connect[last]) {
        if (lineColors[last]!=null) g2.setColor(lineColors[last]);
        else g2.setColor(style.edgeColor);
        drawEnd(g2, aPoints[last], bPoints[last], aPoints[first], bPoints[first]);
      }
    }
    drawMarkers(g2);
  }

  private void drawMarker (Graphics2D _g2, int a1, int b1, int index) {
    RectangularShape theShape = (RectangularShape) shape[index];
    int minus = shapeSize[index]/2;
    theShape.setFrame(a1-minus,b1-minus,shapeSize[index], shapeSize[index]);
    if (shapeFillColor[index]!=null) { // First fill the inside
      _g2.setPaint(shapeFillColor[index]);
      _g2.fill(theShape);
    }
    if (shapeEdgeColor[index]!=null) { // Now the edge
      _g2.setColor (shapeEdgeColor[index]);
      _g2.setStroke(style.edgeStroke);
      _g2.draw(theShape);
    }
  }

  private void drawMarkers (Graphics2D _g2) {
    for (int i = 0; i < numPoints; i++) {
      if (shapeType[i] != InteractiveParticle.NONE) drawMarker(_g2,aPoints[i], bPoints[i], i);
    }
  }

// ----------------------------------------------
// Implementation of Measured3D
// ----------------------------------------------

  public boolean isMeasured () { return canBeMeasured && visible && numPoints>0;  }

  public double getXMin () {
    if (numPoints<=0) return 0.0;
    double min = Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[0][i]<min) min = coordinates[0][i];
    if (group==null) return x+min*sizex;
    return group.x + (x + min*sizex)*group.sizex;
  }
  public double getXMax () {
    if (numPoints<=0) return 0.0;
    double max = -Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[0][i]>max) max = coordinates[0][i];
    if (group==null) return x+max*sizex;
    return group.x + (x + max*sizex)*group.sizex;
  }
  public double getYMin () {
    if (numPoints<=0) return 0.0;
    double min = Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[1][i]<min) min = coordinates[1][i];
    if (group==null) return y+min*sizey;
    return group.y + (y + min*sizey)*group.sizey;
  }
  public double getYMax () {
    if (numPoints<=0) return 0.0;
    double max = -Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[1][i]>max) max = coordinates[1][i];
    if (group==null) return y+max*sizey;
    return group.y + (y + max*sizey)*group.sizey;
  }
  public double getZMin () {
    if (numPoints<=0) return 0.0;
    double min = Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[2][i]<min) min = coordinates[2][i];
    if (group==null) return z+min*sizez;
    return group.z + (z + min*sizez)*group.sizez;
  }
  public double getZMax () {
    if (numPoints<=0) return 0.0;
    double max = -Double.MAX_VALUE;
    for (int i=0; i< numPoints; i++) if (coordinates[2][i]>max) max = coordinates[2][i];
    if (group==null) return z+max*sizez;
    return group.z + (z + max*sizez)*group.sizez;
  }

// -------------------------------------
//  Private methods
// -------------------------------------

  protected void projectPoints (DrawingPanel _panel) {
//    System.out.println("Projecting polygon");
    // Project all points and compute the center of the polygon
    center[0] = 0.0;   center[1] = 0.0;   center[2] = 0.0;
    if (group==null) {
      origin[0] = x;     origin[1] = y;     origin[2] = z;
      size[0]   = sizex; size[1]   = sizey; size[2] = sizez;
    }
    else {
      origin[0] = group.x + x*group.sizex;   origin[1] = group.y + y*group.sizey;   origin[2] = group.z + z*group.sizez;
      size[0]   = sizex*group.sizex;         size[1]   = sizey*group.sizey;         size[2]   = sizez*group.sizez;
    }
    origin[3] = origin[4] = origin[5] = neumaticDash;
    _panel.project(origin,pixelOrigin);
    if (neumaticDash>0) dashSize = (int) (Math.max(pixelOrigin[2],pixelOrigin[3])/2.82);
    else dashSize = (int) (Math.min(pixelOrigin[2],pixelOrigin[3])/2.82);
    if (startType!=PLAIN && !Double.isNaN(startSize)) {
      origin[3] = origin[4] = origin[5] = startSize;
      _panel.project(origin, pixelOrigin);
      theStartSize = (int) (Math.max(pixelOrigin[2], pixelOrigin[3]));
    }
    if (endType!=PLAIN && !Double.isNaN(endSize)) {
      origin[3] = origin[4] = origin[5] = endSize;
      _panel.project(origin, pixelOrigin);
      theEndSize = (int) (Math.max(pixelOrigin[2], pixelOrigin[3]));
    }
    for (int i=0; i<numPoints; i++) {
      for (int k=0; k<3; k++) {
        double delta = coordinates[k][i]*size[k];
        center[k] += delta;
        point[k] = origin[k] + delta;
      }
      _panel.project (point,pixel);
      aPoints[i] = (int) pixel[0];
      bPoints[i] = (int) pixel[1];
      if (connect[i]) lineObjects[i].distance = pixel[2];
      else lineObjects[i].distance = Double.NaN; // Will not be drawn
    }
    for (int k=0; k<3; k++) center[k] = origin[k] + center[k]/numPoints;
    // The interior
    if (closed && style.fillPattern!=null) {
      _panel.project(center,pixel);
      closedObject[0].distance = pixel[2];
    }
    else closedObject[0].distance = Double.NaN; // Will not be drawn
    computeStart();
    computeEnd();
    hasChanged = false;
    panelWithValidProjection = _panel;
  }

// -------------------------------------
//  For arrowHead
// -------------------------------------

static final private double ARROW_CST=0.35;
static final private double ARROW_MAX=25.0;
static final private int MAX=10;

private int startPoints=0, endPoints=0;
private int startA[] = new int [MAX], startB[] = new int [MAX]; // Used to display the decoration at the start point
private int endA[]   = new int [MAX], endB[]   = new int [MAX]; // Used to display the decoration at the end point

  private void computeStart () {
    startPoints = computeExtreme (startA, startB, 1, 0, startType, startSize, theStartSize);
  }

  private void computeEnd () {
    if (closed) endPoints = computeExtreme (endA, endB, numPoints-1,           0, endType, endSize, theEndSize);
    else        endPoints = computeExtreme (endA, endB, numPoints-2, numPoints-1, endType, endSize, theEndSize);
  }

  private int computeExtreme (int[] A, int B[], int from, int to, int type, double _size, int theSize) {
    if (type==PLAIN) {
      A[0] = aPoints[to];    B[0] = bPoints[to];
      A[1] = aPoints[from];  B[1] = bPoints[from];
      return 2;
    }
    double a = aPoints[to] - aPoints[from], b = bPoints[to] - bPoints[from];
    double h = Math.sqrt (a*a+b*b);
    if (h==0.0) {
      A[0] = aPoints[to];    B[0] = bPoints[to];
      A[1] = aPoints[from];  B[1] = bPoints[from];
      return 2;
    }
    double p0,q0;
    if (Double.isNaN(_size)) { // Use standard size
      a *= ARROW_CST/h; b *= ARROW_CST/h;
      if (h>ARROW_MAX) h = ARROW_MAX;
      p0 = aPoints[to] - a*h;
      q0 = bPoints[to] - b*h;
    }
    else {
      h = theSize/h;
      p0 = aPoints[to] - a*h;
      q0 = bPoints[to] - b*h;
    }
    a *= h/2.0; b *= h/2.0;
    switch (type) {
      default :
      case PLAIN : A[0] = aPoints[to];    B[0] = bPoints[to];
                   A[1] = aPoints[from];  B[1] = bPoints[from];
                   return 2;
      case FILLED_CIRCLE :
      case CIRCLE :
        int n = MAX-1;
        double angle1 = Math.atan2(b,a), delta = 2*Math.PI/(n-1), r = Math.sqrt(a*a+b*b);
        for (int i=0; i<n; i++) {
          double angle = angle1 + i * delta;
          A[i] = (int) (aPoints[to] - a - r*Math.cos(angle));
          B[i] = (int) (bPoints[to] - b - r*Math.sin(angle));
        }
        A[n] = aPoints[from]; B[n] = bPoints[from];
        return MAX;
      case FILLED_DIAMOND :
      case DIAMOND :
        A[0] = (int) (p0);                   B[0] = (int) (q0);
        A[1] = (int) (aPoints[to] - a - b);  B[1] = (int) (bPoints[to] - b + a);
        A[2] = (aPoints[to]);                B[2] = (bPoints[to]);
        A[3] = (int) (aPoints[to] - a +  b); B[3] = (int) (bPoints[to] - b - a);
        A[4] = (int) (p0);                   B[4] = (int) (q0);
        A[5] = (aPoints[from]);              B[5] = (bPoints[from]);
        return 6;
      case FILLED_SQUARE :
      case SQUARE :
        A[0] = (int) (p0);              B[0] = (int) (q0);
        A[1] = (int) (p0 - b);          B[1] = (int) (q0 + a);
        A[2] = (int) (aPoints[to] - b); B[2] = (int) (bPoints[to] + a);
        A[3] = (int) (aPoints[to] + b); B[3] = (int) (bPoints[to] - a);
        A[4] = (int) (p0 + b);          B[4] = (int) (q0 - a);
        A[5] = (int) (p0);              B[5] = (int) (q0);
        A[6] = (aPoints[from]);         B[6] = (bPoints[from]);
        return 7;
      case LINE :
        A[0] = (int) (aPoints[to] - b); B[0] = (int) (bPoints[to] + a);
        A[1] = (int) (aPoints[to] + b); B[1] = (int) (bPoints[to] - a);
        A[2] = (aPoints[to]);           B[2] = (bPoints[to]);
        A[3] = (aPoints[from]);         B[3] = (bPoints[from]);
        return 4;
      case FILLED_ARROW :
      case ARROW :
        A[0] = (int) (p0);          B[0] = (int) (q0);
        A[1] = (int) (p0 - b);      B[1] = (int) (q0 + a);
        A[2] = (aPoints[to]);       B[2] = (bPoints[to] );
        A[3] = (int) (p0 + b);      B[3] = (int) (q0 - a);
        A[4] = (int) (p0);          B[4] = (int) (q0);
        A[5] = (aPoints[from]);     B[5] = (bPoints[from]);
        return 6;
    }
  }

private void drawStart (Graphics2D _g2) {
  if (startType>=FILLED_CIRCLE && style.fillPattern!=null) {
    Color color = _g2.getColor();
    _g2.setPaint(style.fillPattern);
    _g2.fillPolygon(startA, startB, startPoints-1);
    _g2.setColor(color);
  }
  _g2.drawPolyline (startA,startB,startPoints);
  drawDash (_g2,aPoints[0],bPoints[0],aPoints[1],bPoints[1]);
}

private void drawEnd (Graphics2D _g2, int a1, int b1, int a2, int b2) {
  if (endType>=FILLED_CIRCLE && style.fillPattern!=null) {
    Color color = _g2.getColor();
    _g2.setPaint(style.fillPattern);
    _g2.fillPolygon(endA, endB, endPoints-1);
    _g2.setColor(color);
  }
  _g2.drawPolyline (endA,endB,endPoints);
  drawDash (_g2,a1,b1,a2,b2);
}

private void drawStartAndEnd (Graphics2D _g2) {
  if (style.fillPattern!=null) {
    Color color = _g2.getColor();
    _g2.setPaint(style.fillPattern); // This seems to modify the color! I call this a bug
    if (startType>=FILLED_CIRCLE) _g2.fillPolygon(startA, startB, startPoints-1);
    if (endType>=FILLED_CIRCLE)   _g2.fillPolygon(endA, endB, endPoints-1);
    _g2.setColor(color);
  }
  _g2.drawPolyline (startA,startB,startPoints-1);
  _g2.drawPolyline (endA,endB,endPoints-1);
  _g2.drawLine (startA[startPoints-2],startB[startPoints-2],endA[endPoints-2],endB[endPoints-2]);
  drawDash (_g2,startA[startPoints-2],startB[startPoints-2],endA[endPoints-2],endB[endPoints-2]);
}

  private void drawLine (Graphics2D _g2, int a1, int b1, int a2, int b2) {
    _g2.drawLine (a1,b1,a2,b2);
    drawDash (_g2,a1,b1,a2,b2);
  }

  private void drawDash (Graphics2D _g2, int a1, int b1, int a2, int b2) {
    if (neumaticDash==0) return;
    if (Math.max(Math.abs(a2-a1),Math.abs(b2-b1))<2.82*dashSize) return;
    int amed = (a1+a2)/2, bmed = (b1+b2)/2;
    if (neumaticDash>0) _g2.drawLine (amed-dashSize,bmed+dashSize,amed+dashSize,bmed-dashSize);
    else _g2.drawLine (amed-dashSize,bmed-dashSize,amed+dashSize,bmed+dashSize);
  }


}

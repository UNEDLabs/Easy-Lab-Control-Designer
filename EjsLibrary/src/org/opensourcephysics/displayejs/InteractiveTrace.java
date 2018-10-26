/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;


import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import org.opensourcephysics.display.*;

public class InteractiveTrace extends AbstractInteractiveElement implements Data, org.opensourcephysics.display.LogMeasurable {
  static public final int SHOW_ALL = 0;
  static public final int ORDER_OF_APPEARANCE = 1;
  static public final int X_COORDINATE = 2;
  static public final int Y_COORDINATE = 3;
  static public final int Z_COORDINATE = 4;
  
  static public final int BAR = InteractiveParticle.WHEEL+10;
  static public final int POST = InteractiveParticle.WHEEL+11;
  static public final int AREA = InteractiveParticle.WHEEL+12;

  static private final int MAX_POINTS = 100000;

  // Configuration variables
  protected boolean connected=true, ignore=false, active=true, clearAtInput=false;
  protected int maxPoints=MAX_POINTS, skip=0, shapeSize, drivenBy=SHOW_ALL, memorySets=1;
  private int shapeType = -1;  // Make sure a shape is created (if requested to)
  protected Color memoryColor=null;
  protected String name="trace";
  protected Dataset dataset = null;

  // Implementation variables
  private int counter=0, pointsAdded=0, pointsNotProjected=0;
  private double point[] = new double[3];
  private String xLabel = "x", yLabel = "y", zLabel = "z";
  protected ArrayList<OnePoint> list, displayList;
  private OnePoint nullPoint=new OnePoint(this,Double.NaN,Double.NaN,Double.NaN,false,InteractiveParticle.NONE,style);
  private OnePoint lastPoint = nullPoint;
  private OnePoint flushPoint = nullPoint;
  private Object3D[] minimalObjects = new Object3D[1];
  private AffineTransform transform = new AffineTransform();
  private ArrayList<ArrayList<OnePoint>> memoryLists=new ArrayList<ArrayList<OnePoint>>();  // One or more ArrayLists that hold previous data

  protected double xmaxLogscale;  // the maximum x value in the dataset when using a log scale
  protected double ymaxLogscale;  // the maximum y value in the dataset when using a log scale
  protected double xminLogscale;  // the minimum x value in the dataset when using a log scale
  protected double yminLogscale;  // the minimum y value in the dataset when using a log scale

//  private TableFrame tableFrame = null;
  private boolean showZ = false, allowTable=false;

  /**
   * Default constructor
   */
  public InteractiveTrace () {
    list = new ArrayList<OnePoint>();
    setXYZ(0,0,0);
    setSizeXYZ(1,1,1);
  }

  public void setName (String _name) { this.name = _name; }
  public String getName () { return this.name; }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveTrace) {
      setMaximumPoints(((InteractiveTrace)_element).getMaximumPoints());
      setConnected(((InteractiveTrace)_element).isConnected());
      setIgnoreEqualPoints(((InteractiveTrace)_element).isIgnoreEqualPoints());
      setActive(((InteractiveTrace)_element).isActive());
      setSkip(((InteractiveTrace)_element).getSkip());
      setMemorySets(((InteractiveTrace)_element).getMemorySets());
      setMemoryColor(((InteractiveTrace)_element).getMemoryColor());
      setName(((InteractiveTrace)_element).getName());
      setAllowTable(((InteractiveTrace)_element).allowTable);
    }
  }

  @Override
  public void initializeMemberOfSet() {
    setAllowTable(true);
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  public void setMaximumPoints (int _n) {
    if (_n < 0 || _n == maxPoints)return;
    if (_n==0) maxPoints = MAX_POINTS;
    else maxPoints = _n;
    counter = 0;
    initialize ();
  }
  public int getMaximumPoints () {  return maxPoints; }

  public void setConnected (boolean connect) { this.connected = connect; }
  public boolean isConnected () { return this.connected; }

  public void setIgnoreEqualPoints (boolean ignoreEqual) { this.ignore = ignoreEqual; }
  public boolean isIgnoreEqualPoints () { return this.ignore; }

  public void setActive (boolean acceptInput) {  this.active = acceptInput; }
  public boolean isActive () {  return this.active; }

  public void setClearAtInput (boolean _clear) { this.clearAtInput = _clear;}
  public boolean isClearAtInput () { return this.clearAtInput; }

  public void setSkip (int howMany) {
    if (howMany==this.skip) return;
    this.skip = howMany;
    counter = 0;
  }
  public int  getSkip () { return this.skip; }

  public void setMemorySets (int howMany) {
    if (howMany==this.memorySets || howMany<0) return;
    memoryLists.clear();
    this.memorySets = howMany;
  }
  public int getMemorySets () { return this.memorySets; }

  public void setMemoryDrivenBy (int driving) { this.drivenBy = driving; }
  public int getMemoryDrivenBy () { return this.drivenBy; }

  public void setMemoryColor (Color _color) { this.memoryColor = _color; }
  public Color getMemoryColor () { return this.memoryColor; }

  public void setAllowTable (boolean _allow) { this.allowTable = _allow; }

  /**
   * Sets the label of the X coordinate when the data is displayed in a table
   * @param _label
   */
  public void setXLabel (String _label) { xLabel = _label; }
  
  /**
   * Sets the label of the Y coordinate when the data is displayed in a table
   * @param _label
   */
  public void setYLabel (String _label) { yLabel = _label; }
  
  /**
   * Sets the label of the Z coordinate when the data is displayed in a table
   * @param _label
   */
  public void setZLabel (String _label) { zLabel = _label; }
  
  // Methods for operation

  public synchronized void clear () {
    synchronized (list) { list.clear(); }
    synchronized (memoryLists) { memoryLists.clear(); }
    pointsAdded = pointsNotProjected = 0;
    lastPoint = nullPoint;
    flushPoint = nullPoint;
    counter = 0;
    showZ = false;
  }

  @SuppressWarnings("fallthrough")
  public synchronized void initialize () {
    if (memorySets==1) return; // Backwards compatibility
    pointsAdded = pointsNotProjected = 0;
    lastPoint = nullPoint;
    flushPoint = nullPoint;
    if (list.size()<=0) return;
    switch (memorySets) {
      default :
        if (memoryLists.size() >= (memorySets - 1)) memoryLists.remove(0);
        // Do NOT break!!!
      case 0 :
        if (memoryColor!=null) { // Use the memory color for the 'old' series of point
          for (OnePoint onePoint : list) onePoint.pointStyle.setEdgeColor(memoryColor);
        }
        memoryLists.add(list);
        break;
    }
    list = new ArrayList<OnePoint>();
  }

  public void addPoint (double xInput, double yInput) { 
    if (clearAtInput) clear();
    addThePoint (xInput,yInput,0.0); 
  }

  public void addPoints (double[] xInput, double[] yInput) {
    if (clearAtInput) clear();
    int n = Math.min(xInput.length,yInput.length);
    for (int i=0; i<n; i++) addThePoint (xInput[i],yInput[i],0.0);
  }

  public void addPoint (double xInput, double yInput, double zInput) {
    if (clearAtInput) clear();
    showZ = true;
    addThePoint (xInput,yInput,zInput);
  }

  public void addPoints (double[] xInput, double[] yInput, double[] zInput) {
    if (clearAtInput) clear();
    int n = Math.min(Math.min(xInput.length,yInput.length),zInput.length);
    for (int i=0; i<n; i++) addThePoint (xInput[i],yInput[i],zInput[i]);
  }

  public void moveToPoint (double xInput, double yInput) {
    if (clearAtInput) clear();
    boolean was_connected = connected;
    connected = false;
    addThePoint (xInput,yInput,0.0);
    connected = was_connected;
  }

  public void moveToPoint (double xInput, double yInput, double zInput) {
    if (clearAtInput) clear();
    showZ = true;
    boolean was_connected = connected;
    connected = false;
    addThePoint (xInput,yInput,zInput);
    connected = was_connected;
  }

  private void addThePoint (double xInput, double yInput, double zInput) {
//    if (!active) return;
    if (Double.isNaN(xInput) || Double.isNaN(yInput) || Double.isNaN(zInput)) return;

    if (ignore && xInput==lastPoint.coordinates[0]
               && yInput==lastPoint.coordinates[1]
               && zInput==lastPoint.coordinates[2]) return;
//    System.out.println ("Adding "+xInput+","+yInput+" connected = "+connected);
    if (skip>0) {
      if (counter>0) {
        counter++;
        if (counter>=skip) counter = 0;
        lastPoint = flushPoint = new OnePoint (this,xInput,yInput,zInput,connected && (pointsAdded!=0),shapeType,style);
        return;
      }
      counter++;
    }
    flushPoint = nullPoint;
    synchronized(list) {
      if (maxPoints>0 && (list.size()>=maxPoints) ) {
        list.remove(0);
        if (!list.isEmpty()) list.get(0).isConnected = false;
      }
      list.add (lastPoint = new OnePoint (this,xInput,yInput,zInput,connected && (pointsAdded!=0),shapeType,style));
    }
    pointsAdded++;
    pointsNotProjected++;
/*
    for (Iterator it=list.iterator(); it.hasNext(); ) {
      OnePoint point = (OnePoint) it.next();
      System.out.println("list has  x = " + point.coordinates[0] + " " + point.coordinates[1]);
    }
*/
  }

  /**
   * Set the type of the marker
   */
  public void setShapeType (int _type) {
    if (shapeType==_type) return;
    shapeType = _type;
    switch (shapeType) {
      default :
      case InteractiveParticle.NONE            : style.displayObject = null; break;
      case InteractiveParticle.ELLIPSE         : style.displayObject = new Ellipse2D.Float(); break;
      case InteractiveTrace.BAR  :
      case InteractiveTrace.POST :
      case InteractiveParticle.RECTANGLE       : style.displayObject = new Rectangle2D.Float(); break;
      case InteractiveParticle.ROUND_RECTANGLE : style.displayObject = new RoundRectangle2D.Float(); break;
      case InteractiveTrace.AREA :               style.displayObject = new Polygon(); break;
    }
    hasChanged = true;
  }

  /**
   * Set the size of the marker
   */
  public void setShapeSize (int _size) { 
    if (shapeSize != _size) { shapeSize = _size; hasChanged = true; }
  }

  // ----------------------------------------------------
  // Implementation of org.opensourcephysics.display.Data
  // ----------------------------------------------------
  /** an integer ID that identifies this object */
  protected int datasetID = hashCode();

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
    synchronized(list) {
      int n = list.size();
      if (showZ) {
        data = (flushPoint==nullPoint) ? new double[3][n] : new double[3][n+1];
        for (int i=0; i<n; i++) {
          double[] coor = list.get(i).coordinates;
          data[0][i] = coor[0];
          data[1][i] = coor[1];
          data[2][i] = coor[2];
        }
        if (flushPoint!=nullPoint) {
          data[0][n] = flushPoint.coordinates[0];
          data[1][n] = flushPoint.coordinates[1];
          data[2][n] = flushPoint.coordinates[2];
        }
      }
      else {
        data = (flushPoint==nullPoint) ? new double[2][n] : new double[2][n+1];
        for (int i=0; i<n; i++) {
          double[] coor = list.get(i).coordinates;
          data[0][i] = coor[0];
          data[1][i] = coor[1];
        }
        if (flushPoint!=nullPoint) {
          data[0][n] = flushPoint.coordinates[0];
          data[1][n] = flushPoint.coordinates[1];
        }
      }
    }
    return data;
  }

  public double[][][] getData3D() { return null; }

  public String[] getColumnNames() { 
    return showZ ? new String[]{xLabel, yLabel, zLabel} : new String[]{xLabel, yLabel};
  }

  public Color[] getLineColors() { 
    return new Color[] { Color.BLACK, getStyle().getEdgeColor()}; 
  }

  public Color[] getFillColors() { 
    Color fillColor;
    if (getStyle().getFillPattern() instanceof Color) fillColor = (Color) getStyle().getFillPattern();
    else  fillColor = new Color (125,125,125);
    return new Color[] { Color.BLACK, fillColor};
  }
  
  public java.util.List<Data> getDataList() { return null; }

  public java.util.ArrayList<Dataset>  getDatasets() { return null; }
  
//    if (dataset==null) dataset = new Dataset();
//    else dataset.clear();
//    dataset.setName(getName());
//    dataset.setConnected (connected);
//    dataset.setLineColor(getStyle().getEdgeColor());
//    int markerShape = Dataset.SQUARE;
//    switch (shapeType) {
//      default :
//      case InteractiveParticle.NONE            : markerShape = Dataset.PIXEL; break;
//      case InteractiveParticle.ELLIPSE         : markerShape = Dataset.CIRCLE; break;
//      case InteractiveTrace.BAR                : markerShape = Dataset.BAR; break;
//      case InteractiveTrace.POST               : markerShape = Dataset.POST; break;
//      case InteractiveTrace.AREA               : markerShape = Dataset.AREA; break;
//      case InteractiveParticle.RECTANGLE       : markerShape = Dataset.SQUARE; break;
//      case InteractiveParticle.ROUND_RECTANGLE : markerShape = Dataset.SQUARE; break;
//    }
//    dataset.setMarkerShape(markerShape);
//    Color fillColor;
//    if (getStyle().getFillPattern() instanceof Color) fillColor = (Color) getStyle().getFillPattern();
//    else  fillColor = new Color (125,125,125);
//    dataset.setMarkerColor(fillColor,getStyle().getEdgeColor());
//    double[][] data = getData2D();
//    for (int i=0,n=data.length; i<n; i++) dataset.append(data[0][i], data[1][i]);
//    java.util.ArrayList<Dataset> datasetList = new java.util.ArrayList<Dataset>();
//    datasetList.add(dataset);
//    return datasetList;    
//  }

// -------------------------------------
// Implementation of Drawable3D
// -------------------------------------

  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    if (!visible)return null;
    if (_panel instanceof DrawingPanel3D) {
      if (hasChanged || _panel != panelWithValidProjection) projectPoints(_panel, true);
      else if (pointsNotProjected > 0) projectPoints(_panel, false);
    }
    else projectPoints(_panel, true);
  //    if (sizeEnabled      && Math.abs(a2-_xpix)<SENSIBILITY               && Math.abs(b2-_ypix)<SENSIBILITY)               return sizeTarget;
    if (positionEnabled) {
      if (Math.abs(lastPoint.pixel[0]-_xpix)<SENSIBILITY && Math.abs(lastPoint.pixel[1]-_ypix)<SENSIBILITY) {
        return new InteractionTargetTracePoint(this, new Point3D (lastPoint.coordinates[0],lastPoint.coordinates[1],lastPoint.coordinates[2]));
      }
      /*
      for (Iterator it=displayList.iterator(); it.hasNext(); ) {
        OnePoint point = (OnePoint) it.next();
        if (Math.abs(point.pixel[0]-_xpix)<SENSIBILITY && Math.abs(point.pixel[1]-_ypix)<SENSIBILITY) {
          return new InteractionTargetTracePoint(this, new Point3D (point.coordinates[0],point.coordinates[1],point.coordinates[2]));
        }
      }
      */
    }
    if (allowTable) {
      for (Iterator<OnePoint> it=displayList.iterator(); it.hasNext(); ) {
        OnePoint onePoint = it.next();
        if (Math.abs(onePoint.pixel[0]-_xpix)<SENSIBILITY && Math.abs(onePoint.pixel[1]-_ypix)<SENSIBILITY) {
          return this;
        }
      }
      
    }
    return null;
  }

  public Object3D[] getObjects3D (DrawingPanel3D _panel) {
    if (list.size()<=0 || !visible) return null;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints(_panel,true);
    else if (pointsNotProjected>0) projectPoints(_panel,false);
    return displayList.toArray(minimalObjects);
  }

  public void draw (DrawingPanel3D _panel, Graphics2D _g, int _index) {
    try {
      OnePoint onePoint = displayList.get(_index);
      Color theColor = _panel.projectColor(onePoint.pointStyle.edgeColor, onePoint.distance);
      if (onePoint.isConnected) {
        OnePoint pointPrev = displayList.get(_index-1);
        if (shapeType==InteractiveTrace.AREA) {
          Paint theFillPattern = onePoint.pointStyle.fillPattern;
          if (theFillPattern instanceof Color) theFillPattern = _panel.projectColor( (Color) theFillPattern, onePoint.distance);
          _g.setPaint(theFillPattern);
          Polygon pol = (Polygon) onePoint.pointStyle.displayObject;
          pol.reset();
          pol.addPoint(pointPrev.zeroA,pointPrev.zeroB);
          pol.addPoint((int)pointPrev.pixel[0],(int)pointPrev.pixel[1]);
          pol.addPoint((int)onePoint.pixel[0],(int)onePoint.pixel[1]);
          pol.addPoint(onePoint.zeroA,onePoint.zeroB);
          pol.addPoint(pointPrev.zeroA,pointPrev.zeroB);
          _g.fill(pol);
        }
        _g.setColor(theColor);
        _g.setStroke(onePoint.pointStyle.edgeStroke);
        _g.drawLine((int)onePoint.pixel[0],(int)onePoint.pixel[1],(int)pointPrev.pixel[0],(int)pointPrev.pixel[1]);
      }
      if (onePoint.pointStyle.displayObject!=null) {
        Paint theFillPattern = onePoint.pointStyle.fillPattern;
        if (theFillPattern instanceof Color) theFillPattern = _panel.projectColor( (Color) theFillPattern, onePoint.distance);
        drawMarker (_g,onePoint,theColor,theFillPattern);
      }
    } catch (Exception _e) { } // Ignore it
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (list.size()<=0 || !visible) return;
//    if (pointsNotProjected>0 || hasChanged || _panel!=panelWithValidProjection)
    projectPoints(_panel,true);
//    int aprev=0, bprev=0;
    Graphics2D g2 = (Graphics2D) _g;
    OnePoint pointPrev=null;
    for (Iterator<OnePoint> it=displayList.iterator(); it.hasNext(); ) { // Draw everything
      OnePoint onePoint = it.next();
      //      System.out.println("x = "+point.coordinates[0]+" " +point.coordinates[1]+ " connected = "+point.connected);
      if (onePoint.isConnected) {
        if (shapeType==InteractiveTrace.AREA) {
          g2.setPaint(onePoint.pointStyle.fillPattern);
          Polygon pol = (Polygon) onePoint.pointStyle.displayObject;
          pol.reset();
          pol.addPoint(pointPrev.zeroA,pointPrev.zeroB);
          pol.addPoint((int)pointPrev.pixel[0],(int)pointPrev.pixel[1]);
          pol.addPoint((int)onePoint.pixel[0],(int)onePoint.pixel[1]);
          pol.addPoint(onePoint.zeroA,onePoint.zeroB);
          pol.addPoint(pointPrev.zeroA,pointPrev.zeroB);
          g2.fill(pol);
        }
        if (onePoint.pointStyle.edgeColor!=null) {
          g2.setColor (onePoint.pointStyle.edgeColor);
          g2.setStroke(onePoint.pointStyle.edgeStroke);
          g2.drawLine ((int)onePoint.pixel[0],(int)onePoint.pixel[1],(int)pointPrev.pixel[0],(int)pointPrev.pixel[1]);
        }
      }
      if (onePoint.pointStyle.displayObject!=null) drawMarker (g2,onePoint,onePoint.pointStyle.edgeColor,onePoint.pointStyle.fillPattern);
      pointPrev = onePoint;
//      aprev = (int)onePoint.pixel[0];
//      bprev = (int)onePoint.pixel[1];
    }
  }

// -------------------------------------
//  Private methods and classes
// -------------------------------------

  @SuppressWarnings("unchecked")
  private synchronized void projectPoints (DrawingPanel _panel, boolean forceProjection) {
    displayList = new ArrayList<OnePoint>();
    if (memorySets!=1) {
      switch (drivenBy) {
        default :
        case SHOW_ALL :
          for (Iterator<ArrayList<OnePoint>> it = memoryLists.iterator(); it.hasNext(); ) displayList.addAll(it.next());
          break;
        case ORDER_OF_APPEARANCE :
          for (Iterator<ArrayList<OnePoint>> it = memoryLists.iterator(); it.hasNext(); ) {
            ArrayList<OnePoint> memory = it.next();
            for (int i = 0, n = Math.min(pointsAdded,memory.size()); i<n; i++) displayList.add (memory.get(i));
          }
          break;
        case X_COORDINATE :
          for (Iterator<ArrayList<OnePoint>> it = memoryLists.iterator(); it.hasNext(); ) {
            ArrayList<OnePoint> memory = it.next();
            for (int i = 0, n = memory.size(); i<n; i++) {
              OnePoint aPoint = memory.get(i);
              if (aPoint.coordinates[0]<=lastPoint.coordinates[0]) displayList.add (aPoint);
              else break;
            }
          }
          break;
        case Y_COORDINATE :
          for (Iterator<ArrayList<OnePoint>> it = memoryLists.iterator(); it.hasNext(); ) {
            ArrayList<OnePoint> memory = it.next();
            for (int i = 0, n = memory.size(); i<n; i++) {
              OnePoint aPoint = memory.get(i);
              if (aPoint.coordinates[1]<=lastPoint.coordinates[1]) displayList.add (aPoint);
              else break;
            }
          }
          break;
        case Z_COORDINATE :
          for (Iterator<ArrayList<OnePoint>> it = memoryLists.iterator(); it.hasNext(); ) {
            ArrayList<OnePoint> memory = it.next();
            for (int i = 0, n = memory.size(); i<n; i++) {
              OnePoint aPoint = memory.get(i);
              if (aPoint.coordinates[2]<=lastPoint.coordinates[2]) displayList.add (aPoint);
              else break;
            }
          }
          break;
      }
    }
    synchronized (list) { displayList.addAll((ArrayList<OnePoint>) list.clone()); } // Or face synchronization problems!
    if (flushPoint!=nullPoint) displayList.add(flushPoint);
    for (int i = 0, n = displayList.size(); i < n; i++) displayList.get(i).project(_panel, i,forceProjection);
    hasChanged = false;
    panelWithValidProjection = _panel;
    pointsNotProjected = 0;
  }

  private void drawMarker (Graphics2D _g2, OnePoint _point, Color _color, Paint _fill) {
    if (shapeType==InteractiveTrace.AREA) return; // Done already
    if (! (_point.pointStyle.displayObject instanceof RectangularShape) ) {
      _g2.setColor (_color);
      _g2.drawOval ((int) _point.pixel[0], (int) _point.pixel[1],1,1);  // draw a point
      return;
    }
//    System.out.println ("draw Pos is "+_point.pixel[0]+ " a1 = "+_point.a1); 

    RectangularShape shape = (RectangularShape) _point.pointStyle.displayObject;
    AffineTransform originalTransform = _g2.getTransform();
    transform.setTransform(originalTransform);
    transform.rotate(-_point.pointStyle.angle,_point.pixel[0],_point.pixel[1]);
    _g2.setTransform(transform);
    if (shapeType==InteractiveTrace.BAR) {
      if (_point.b1<_point.zeroB) shape.setFrame(_point.a1,_point.b1, shapeSize, _point.zeroB-_point.b1);
      else shape.setFrame(_point.a1,_point.zeroB,shapeSize, _point.b1-_point.zeroB); 
    }
    else shape.setFrame(_point.a1,_point.b1,shapeSize, shapeSize); // shape.getWidth(),shape.getHeight());
    if (shapeType==InteractiveTrace.POST) {
      _g2.setColor (_color);
      _g2.drawLine(_point.zeroA,_point.b1, _point.zeroA, _point.zeroB);
    }
    if (_fill!=null) { // First fill the inside
      _g2.setPaint(_fill);
      _g2.fill(shape);
    }
    _g2.setColor (_color);
    _g2.setStroke(_point.pointStyle.edgeStroke);
    _g2.draw(shape); // Second, draw the edge
    _g2.setTransform(originalTransform);
  }


private class OnePoint extends Object3D {
  boolean isConnected, alreadyProjected;
  double[] coordinates = new double[3];
  double[] pixel = new double[3];
  int a1,b1,zeroA=0,zeroB=0;
  int pointShapeType;
  Style pointStyle=null;

  OnePoint (Drawable3D _drawable, double _x,double _y,double _z, boolean _c, int _shapeType, Style _style) {
    super (_drawable,-1);
    coordinates[0] = _x; coordinates[1] = _y;  coordinates[2] = _z;
    isConnected = _c;
    pointShapeType = _shapeType;
    if (_style!=null) pointStyle = new Style(_style);
    // style = _style;
    alreadyProjected=false;
    }

  protected void project (DrawingPanel _panel, int _index, boolean forceProjection) {
    index = _index;
    if ((!forceProjection) && alreadyProjected) return;
    // Project the (x,0) point for bars, posts, and areas
    if (pointShapeType==InteractiveTrace.BAR || pointShapeType==InteractiveTrace.POST || pointShapeType==InteractiveTrace.AREA) {
      if (group==null) {
        point[0] = x + coordinates[0]*sizex;
        point[1] = 0;
        point[2] = 0;
      }
      else {
        point[0] = group.x + (x + coordinates[0]*sizex)*group.sizex;
        point[1] = 0;
        point[2] = 0;
      }
      _panel.project(point,pixel);
      zeroA = (int) pixel[0];
      zeroB = (int) pixel[1];
    }
    // Now project the real point
    if (group==null) {
      point[0] = x + coordinates[0]*sizex;
      point[1] = y + coordinates[1]*sizey;
      point[2] = z + coordinates[2]*sizez;
    }
    else {
      point[0] = group.x + (x + coordinates[0]*sizex)*group.sizex;
      point[1] = group.y + (y + coordinates[1]*sizey)*group.sizey;
      point[2] = group.z + (z + coordinates[2]*sizez)*group.sizez;
    }
    _panel.project(point,pixel);
    alreadyProjected = true;
    distance = pixel[2];
    if (pointStyle.displayObject instanceof RectangularShape) {
//      RectangularShape shape = (RectangularShape) style.displayObject;
      double dx, dy;
      switch (pointStyle.position) {
        default :
        case Style.CENTERED:   dx = shapeSize/2.0; dy = shapeSize/2.0; break;
        case Style.NORTH:      dx = shapeSize/2.0; dy = 0.0;                   break;
        case Style.SOUTH:      dx = shapeSize/2.0; dy = shapeSize;     break;
        case Style.EAST:       dx = shapeSize;     dy = shapeSize/2.0; break;
        case Style.SOUTH_EAST: dx = shapeSize;     dy = shapeSize;     break;
        case Style.NORTH_EAST: dx = shapeSize;     dy = 0.0;                   break;
        case Style.WEST:       dx = 0.0;           dy = shapeSize/2.0; break;
        case Style.SOUTH_WEST: dx = 0.0;           dy = shapeSize;     break;
        case Style.NORTH_WEST: dx = 0.0;           dy = 0.0;                   break;
      }
      if (pointShapeType==InteractiveTrace.BAR) {
        a1 = (int) (pixel[0] - dx);
        b1 = (int) pixel[1];
      }
      else {
        a1 = (int) (pixel[0] - dx);
        b1 = (int) (pixel[1] - dy);
      }
    }
  }

}  // End of class OnePoint

//-------------------------------------
//Implementation of LogMeasurable
//-------------------------------------

public double getXMinLogscale(){
  double min = Double.MAX_VALUE;
  synchronized(list) {
    for (OnePoint onePoint : list) {
      double xp = onePoint.coordinates[0];
      if (xp>0) min = Math.min(min,xp);
    }
  }
  if (flushPoint!=nullPoint) {
    double xp = flushPoint.coordinates[0];
    if (xp>0) min = Math.min(min,xp);
  }
  if (memorySets!=1) {
    for (ArrayList<OnePoint> memory : memoryLists) {
      for (OnePoint onePoint : memory) {
        double xp = onePoint.coordinates[0];
        if (xp>0) min = Math.min(min,xp);
      }
    }
  }
  if (group==null) return x + min*sizex;
  return group.x + (x + min*sizex)*group.sizex;
}

public double getXMaxLogscale(){
  double max = -Double.MAX_VALUE;
  synchronized(list) {
    for (OnePoint onePoint : list) {
      double xp = onePoint.coordinates[0];
      if (xp>0) max = Math.max(max,xp);
    }
  }
  if (flushPoint!=nullPoint) {
    double xp = flushPoint.coordinates[0];
    if (xp>0) max = Math.max(max,xp);
  }
  if (memorySets!=1) {
    for (ArrayList<OnePoint> memory : memoryLists) {
      for (OnePoint onePoint : memory) {
        double xp = onePoint.coordinates[0];
        if (xp>0) max = Math.max(max,xp);
      }
    }
  }
  if (group==null) return x + max*sizex;
  return group.x + (x + max*sizex)*group.sizex;
}

public double getYMinLogscale(){
  double min = Double.MAX_VALUE;
  synchronized(list) {
    for (OnePoint onePoint : list) {
      double yp = onePoint.coordinates[1];
      if (yp>0) min = Math.min(min,yp);
    }
  }
  if (flushPoint!=nullPoint) {
    double yp = flushPoint.coordinates[1];
    if (yp>0) min = Math.min(min,yp);
  }
  if (memorySets!=1) {
    for (ArrayList<OnePoint> memory : memoryLists) {
      for (OnePoint onePoint : memory) {
        double yp = onePoint.coordinates[1];
        if (yp>0) min = Math.min(min,yp);
      }
    }
  }
  if (group==null) return y + min*sizey;
  return group.y + (y + min*sizey)*group.sizey;
}

public double getYMaxLogscale(){
  double max = -Double.MAX_VALUE;
  synchronized(list) {
    for (OnePoint onePoint : list) {
      double yp = onePoint.coordinates[1];
      if (yp>0) max = Math.max(max,yp);
    }
  }
  if (flushPoint!=nullPoint) {
    double yp = flushPoint.coordinates[1];
    if (yp>0) max = Math.max(max,yp);
  }
  if (memorySets!=1) {
    for (ArrayList<OnePoint> memory : memoryLists) {
      for (OnePoint onePoint : memory) {
        double yp = onePoint.coordinates[1];
        if (yp>0) max = Math.max(max,yp);
      }
    }
  }
  if (group==null) return y + max*sizey;
  return group.y + (y + max*sizey)*group.sizey;
}

// -------------------------------------
// Implementation of Measured3D
// -------------------------------------

  public boolean isMeasured () { return canBeMeasured && visible && !list.isEmpty();  }

  public double getXMin () {
    double min = Double.MAX_VALUE;
    synchronized(list) {
      for (OnePoint onePoint : list) min = Math.min(min,onePoint.coordinates[0]);
    }
    if (flushPoint!=nullPoint) min = Math.min(min,flushPoint.coordinates[0]); 
    if (memorySets!=1) {
      for (ArrayList<OnePoint> memory : memoryLists) for (OnePoint onePoint : memory) min = Math.min(min,onePoint.coordinates[0]);
    }
    if (group==null) return x + min*sizex;
    return group.x + (x + min*sizex)*group.sizex;
  }

  public double getXMax () {
    double max = -Double.MAX_VALUE;
    synchronized(list) {
      for (OnePoint onePoint : list) max = Math.max(max,onePoint.coordinates[0]);
    }
    if (flushPoint!=nullPoint) max = Math.max(max,flushPoint.coordinates[0]); 
    if (memorySets!=1) {
      for (ArrayList<OnePoint> memory : memoryLists) for (OnePoint onePoint : memory) max = Math.max(max,onePoint.coordinates[0]);
    }
    if (group==null) return x + max*sizex;
    return group.x + (x + max*sizex)*group.sizex;
  }
  
  public double getYMin () {
    double min = Double.MAX_VALUE;
    synchronized(list) {
      for (OnePoint onePoint : list) min = Math.min(min,onePoint.coordinates[1]);
    }
    if (flushPoint!=nullPoint) min = Math.min(min,flushPoint.coordinates[1]); 
    if (memorySets!=1) {
      for (ArrayList<OnePoint> memory : memoryLists) for (OnePoint onePoint : memory) min = Math.min(min,onePoint.coordinates[1]);
    }
    if (group==null) return y + min*sizey;
    return group.y + (y + min*sizey)*group.sizey;
  }
  
  public double getYMax () {
    double max = -Double.MAX_VALUE;
    synchronized(list) {
      for (OnePoint onePoint : list) max = Math.max(max,onePoint.coordinates[1]);
    }
    if (flushPoint!=nullPoint) max = Math.max(max,flushPoint.coordinates[1]); 
    if (memorySets!=1) {
      for (ArrayList<OnePoint> memory : memoryLists) for (OnePoint onePoint : memory) max = Math.max(max,onePoint.coordinates[1]);
    }
    if (group==null) return y + max*sizey;
    return group.y + (y + max*sizey)*group.sizey;
  }
  
  public double getZMin () {
    double min = Double.MAX_VALUE;
    synchronized(list) {
      for (OnePoint onePoint : list) min = Math.min(min,onePoint.coordinates[2]);
    }
    if (flushPoint!=nullPoint) min = Math.min(min,flushPoint.coordinates[2]); 
    if (memorySets!=1) {
      for (ArrayList<OnePoint> memory : memoryLists) for (OnePoint onePoint : memory) min = Math.min(min,onePoint.coordinates[2]);
    }
    if (group==null) return z + min*sizez;
    return group.z + (z + min*sizez)*group.sizez;
  }
  
  public double getZMax () {
    double max = -Double.MAX_VALUE;
    synchronized(list) {
      for (OnePoint onePoint : list) max = Math.max(max,onePoint.coordinates[2]);
    }
    if (flushPoint!=nullPoint) max = Math.max(max,flushPoint.coordinates[2]); 
    if (memorySets!=1) {
      for (ArrayList<OnePoint> memory : memoryLists) for (OnePoint onePoint : memory) max = Math.max(max,onePoint.coordinates[2]);
    }
    if (group==null) return z + max*sizez;
    return group.z + (z + max*sizez)*group.sizez;
  }


}  // End of main class

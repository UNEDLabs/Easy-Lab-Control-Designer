/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.*;
import java.awt.geom.*;
import org.opensourcephysics.display.*;

/**
 * An InteractiveParticle is an InteractiveElement that displays a RectangularShape
 */
public class InteractiveParticle extends AbstractInteractiveElement {
  public final static int NONE             = 0;
  public final static int ELLIPSE          = 1;
  public final static int RECTANGLE        = 2;
  public final static int ROUND_RECTANGLE  = 3;
  public final static int WHEEL            = 4;

  /* Configuration variables */
  protected int shapeType = -1;  // Make sure an shape is created
  protected boolean pixelSize = false; // Whether size is in pixels

  /* Implementation variables */
  protected double a1=0.0, b1=0.0; // the final coordinates for the Shape's origin
//  protected double a2=0.0, b2=0.0; // the final coordinates for the (other end) Shape's corner
//  protected double dx, dy; // The displacement from the center
  protected double[] coordinates  = new double[6]; // the input for all projections
  protected double[] pixelOrigin  = new double[5]; // The projection of the origin (and size)
  protected Object3D[] objects    = new Object3D[] { new Object3D(this,0) };
  protected AffineTransform transform = new AffineTransform();

  /**
   * Default constructor
   */
  public InteractiveParticle () { this(ELLIPSE); }

  /**
   * Constructor that accepts a given type. Either ELLIPSE, RECTANGLE, ROUND_RECTANGLE or NONE (a dot will be displayed)
   */
  public InteractiveParticle (int _type) {
    setShapeType(_type);
  }


  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveParticle) {
      setShapeType( ( (InteractiveParticle) _element).shapeType);
      setPixelSize ( ( (InteractiveParticle) _element).getPixelSize());
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------
  /**
   * Set the type of the marker
   */
  public void setShapeType (int _type) {
    if (shapeType==_type) return;
    shapeType = _type;
    switch (shapeType) {
      default :
      case NONE            : style.displayObject = null; break;
      case WHEEL           :
      case ELLIPSE         : style.displayObject = new Ellipse2D.Float(); break;
      case RECTANGLE       : style.displayObject = new Rectangle2D.Float(); break;
      case ROUND_RECTANGLE : style.displayObject = new RoundRectangle2D.Float(); break;
    }
  }

  /**
   * If true, the image is drawn as is, without resizing it
   * @param _s boolean
   */
  public void setPixelSize (boolean _s) { pixelSize = _s; }

  public boolean getPixelSize () { return pixelSize; }

// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------

  // method added by W. Christian
  protected double[] getPixelOrigin(){ return pixelOrigin; }

  public void setSensitivity (int _s) { actualSensitivity = _s; }

  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    if (!visible) return null;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints (_panel);
    // In 3D it is not possible to change size interactively, due to the effect of style.position and style.angle
//    if (sizeEnabled      && Math.abs(a2-_xpix)<SENSIBILITY               && Math.abs(b2-_ypix)<SENSIBILITY)               return sizeTarget;
    if (positionEnabled) {
      if (actualSensitivity<=0) { // Any point in the whole picture will do
        double xI = pixelOrigin[0] - _xpix, yI = pixelOrigin[1] - _ypix;
        if (style.angle != 0.0) { // Take care of a possible rotation
          double aux = xI * style.cosAngle - yI * style.sinAngle;
          yI = xI * style.sinAngle + yI * style.cosAngle;
          xI = aux;
        }
        if (shapeType==ELLIPSE || shapeType==WHEEL) {
          if((xI*xI)/(pixelOrigin[2]*pixelOrigin[2])+(yI*yI)/(pixelOrigin[3]*pixelOrigin[3])<0.25) return new InteractionTargetElementPosition(this);
        }
        else {
          if(Math.abs(xI)<pixelOrigin[2]/2 && Math.abs(yI)<pixelOrigin[3]/2) return new InteractionTargetElementPosition(this);
        }
      }
      else { // Only the center of the particle
        if (Math.abs(pixelOrigin[0]-_xpix)<actualSensitivity && Math.abs(pixelOrigin[1]-_ypix)<actualSensitivity) return new InteractionTargetElementPosition(this);
      }
    } // End of positionEnabled
    return null;
   }

  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    if (!visible) return null;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints (_panel);
    return objects;
  }

  // No need to project, projection has already been computed in getObjects3D
  public void draw (DrawingPanel3D _panel, Graphics2D _g2, int _index) {
    // Allow the panel to adjust color according to depth
    Color theColor = _panel.projectColor(style.edgeColor,objects[0].distance);
    Paint theFillPattern = style.fillPattern;
    if (theFillPattern instanceof Color)
      theFillPattern = _panel.projectColor((Color) theFillPattern,objects[0].distance);
    drawIt (_g2,theColor,theFillPattern);
  }

  public void drawQuickly (DrawingPanel3D _panel, Graphics2D _g2) {
    if (!visible) return;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints (_panel);
    drawIt (_g2, style.edgeColor,style.fillPattern);
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (!visible) return;
//    if (hasChanged || _panel!=panelWithValidProjection)
    projectPoints (_panel); // DrawingPanel still doesn't implement the call to needsToProject()
    drawIt ((Graphics2D) _g, style.edgeColor,style.fillPattern);
  }

// -------------------------------------
//  Private methods
// -------------------------------------

  protected void projectPoints (DrawingPanel _panel) {
    // System.out.println("Projecting particle");
    if (group!=null) {
      coordinates[0] = group.x + x*group.sizex;  coordinates[1] = group.y + y*group.sizey; coordinates[2] = group.z + z*group.sizez;
      coordinates[3] = sizex*group.sizex;        coordinates[4] = sizey*group.sizey;       coordinates[5] = sizez*group.sizez;
    }
    else {
      coordinates[0] = x;     coordinates[1] = y;     coordinates[2] = z;
      coordinates[3] = sizex; coordinates[4] = sizey; coordinates[5] = sizez;
    }
    _panel.project(coordinates,pixelOrigin);
    if (pixelSize) { pixelOrigin[2] = sizex; pixelOrigin[3] = sizey; }
    objects[0].distance = pixelOrigin[4];
    double dx, dy;
    switch (style.position) {
      default :
      case Style.CENTERED:   dx =pixelOrigin[2]/2.0; dy =pixelOrigin[3]/2.0; break;
      case Style.NORTH:      dx =pixelOrigin[2]/2.0; dy = 0.0;               break;
      case Style.SOUTH:      dx =pixelOrigin[2]/2.0; dy =pixelOrigin[3];     break;
      case Style.EAST:       dx =pixelOrigin[2];     dy =pixelOrigin[3]/2.0; break;
      case Style.SOUTH_EAST: dx =pixelOrigin[2];     dy =pixelOrigin[3];     break;
      case Style.NORTH_EAST: dx =pixelOrigin[2];     dy = 0.0;               break;
      case Style.WEST:       dx = 0.0;               dy =pixelOrigin[3]/2.0; break;
      case Style.SOUTH_WEST: dx = 0.0;               dy =pixelOrigin[3];     break;
      case Style.NORTH_WEST: dx = 0.0;               dy = 0.0;               break;
    }
    a1 = pixelOrigin[0] - dx;
    b1 = pixelOrigin[1] - dy;
//    a2 = pixelOrigin[0] + dx*style.cosAngle - dy*style.sinAngle;
//    b2 = pixelOrigin[1] - dx*style.sinAngle - dy*style.cosAngle;
    hasChanged = false;
    panelWithValidProjection = _panel;
  }

  private void drawIt (Graphics2D _g2, Color _color, Paint _fill) {
    // System.out.println("Drawing particle ");
    if (! (style.displayObject instanceof RectangularShape) ) {
      _g2.setColor(_color);
      _g2.drawOval( (int) pixelOrigin[0], (int) pixelOrigin[1], 1, 1); // draw a point
      return;
    }
    RectangularShape shape = (RectangularShape) style.displayObject;
    AffineTransform originalTransform = _g2.getTransform();
    transform.setTransform(originalTransform);
    transform.rotate(-style.angle,pixelOrigin[0],pixelOrigin[1]);
    _g2.setTransform(transform);
    shape.setFrame(a1,b1,pixelOrigin[2],pixelOrigin[3]);
    if (_fill!=null) { // First fill the inside
      _g2.setPaint(_fill);
      _g2.fill(shape);
    }
    _g2.setColor (_color);
    _g2.setStroke(style.edgeStroke);
    if (shapeType==WHEEL) {
      int c = (int) (b1 + pixelOrigin[3]/2); // centerY
      _g2.drawLine((int) a1, c, (int) (a1+pixelOrigin[2]), c);
      c = (int) (a1 + pixelOrigin[2]/2);     // centerX
      _g2.drawLine(c, (int) b1, c, (int) (b1+pixelOrigin[3]));
    }
    _g2.draw(shape); // Second, draw the edge
    _g2.setTransform(originalTransform);
  }

// ----------------------------------------------------
// Measurable
// ----------------------------------------------------

  public double getXMin () {
    if (!pixelSize) return super.getXMin();
    if (group==null) return x;
    return group.x + x;
  }
  public double getXMax () {
    if (!pixelSize) return super.getXMax();
    if (group==null) return x;
    return group.x + x;
  }
  public double getYMin () {
    if (!pixelSize) return super.getYMin();
    if (group==null) return y;
    return group.y + y;
  }
  public double getYMax () {
    if (!pixelSize) return super.getYMax();
    if (group==null) return y;
    return group.y + y;
  }
  public double getZMin () {
    if (!pixelSize) return super.getZMin();
    if (group==null) return z;
    return group.z + z;
  }
  public double getZMax () {
    if (!pixelSize) return super.getZMax();
    if (group==null) return z;
    return group.z + z;
  }

}

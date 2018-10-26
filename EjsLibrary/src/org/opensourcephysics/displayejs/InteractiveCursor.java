/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.*;
import org.opensourcephysics.display.*;

/**
 * An InteractiveParticle is an InteractiveElement that displays a RectangularShape
 */
public class InteractiveCursor extends AbstractInteractiveElement {
  static public final int HORIZONTAL = 0;
  static public final int VERTICAL = 1;
  static public final int CROSSHAIR = 2;

  // Configuration variables
  private int type=CROSSHAIR;

  /* Implementation variables */
  private int a1=0, b1=0;
//  private int x1=0, x2=0;
//  private int y1=0, y2=0;
  private double[] coordinates  = new double[3]; // The input for all projections (3 saves computations)
  private double[] pixelOrigin  = new double[3]; // The projection of the origin
  private Object3D[] objects    = new Object3D[] { new Object3D(this,0) };

  /**
   * Default constructor
   */
  public InteractiveCursor () { this(CROSSHAIR); }

  /**
   * Constructor that accepts a given type. Either ELLIPSE, RECTANGLE, ROUND_RECTANGLE or NONE (a dot will be displayed)
   */
  public InteractiveCursor (int _type) {
    setType(_type);
  }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveCursor) {
      setType( ( (InteractiveCursor) _element).type);
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  public void setType (int _aType) { type = _aType; }
  public int getType () { return type; }

// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------

  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    if (!visible) return null;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints (_panel);
    if (positionEnabled) {
//      double xI = pixelOrigin[0] - _xpix, yI = pixelOrigin[1] - _ypix;
      switch (type) {
        case VERTICAL:   
          if (Math.abs(pixelOrigin[0]-_xpix) < actualSensitivity) return new InteractionTargetCursorPosition(this,InteractionTargetCursorPosition.X);
          break;
        case HORIZONTAL: 
          if (Math.abs(pixelOrigin[1]-_ypix) < actualSensitivity) return new InteractionTargetCursorPosition(this,InteractionTargetCursorPosition.Y);
          break;
        default:
          if ((Math.abs(pixelOrigin[0]-_xpix) < actualSensitivity)) {
            if ((Math.abs(pixelOrigin[1]-_ypix) < actualSensitivity) ) return new InteractionTargetCursorPosition(this,InteractionTargetCursorPosition.XY);
            return new InteractionTargetCursorPosition(this,InteractionTargetCursorPosition.X);
          }
          if ((Math.abs(pixelOrigin[1]-_ypix) < actualSensitivity) ) return new InteractionTargetCursorPosition(this,InteractionTargetCursorPosition.Y);
        break;
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
    drawIt (_panel,_g2,theColor);
  }

  public void drawQuickly (DrawingPanel3D _panel, Graphics2D _g2) {
    if (!visible) return;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints (_panel);
    drawIt (_panel,_g2, style.edgeColor);
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (!visible) return;
//    if (hasChanged || _panel!=panelWithValidProjection)
    projectPoints (_panel); // DrawingPanel still doesn't implement the call to needsToProject()
    drawIt (_panel,(Graphics2D) _g, style.edgeColor);
  }

// -------------------------------------
//  Private methods
// -------------------------------------

  protected void projectPoints (DrawingPanel _panel) {
    // System.out.println("Projecting particle");
    if (_panel instanceof DrawingPanel3D) { // Not done yet
    }
    else {
//      coordinates[0] = _panel.getXMin(); coordinates[1] = _panel.getYMin();
//      _panel.project(coordinates,pixelOrigin);
//      x1 = (int) pixelOrigin[0]; y1 = (int) pixelOrigin[1];
//      coordinates[0] = _panel.getXMax(); coordinates[1] = _panel.getYMax();
//      _panel.project(coordinates,pixelOrigin);
//      x2 = (int) pixelOrigin[0]; y2 = (int) pixelOrigin[1];
    }
    if (group!=null) {
      coordinates[0] = group.x + x*group.sizex;  coordinates[1] = group.y + y*group.sizey; coordinates[2] = group.z + z*group.sizez;
    }
    else {
      coordinates[0] = x;     coordinates[1] = y;     coordinates[2] = z;
    }
    _panel.project(coordinates,pixelOrigin);
    objects[0].distance = pixelOrigin[2];
    a1 = (int) pixelOrigin[0];
    b1 = (int) pixelOrigin[1];
    hasChanged = false;
    panelWithValidProjection = _panel;
  }

  private void drawIt (DrawingPanel _panel, Graphics2D _g2, Color _color) {
    _g2.setColor (_color);
    _g2.setStroke(style.edgeStroke);
    int[] gutters=_panel.getGutters();
//    g.drawLine(orginX, gutters[1], orginX, panel.getHeight()-gutters[3]);
//    g.drawLine(gutters[0], originY, panel.getWidth()-gutters[2], originY);

    switch (type) {
//      case HORIZONTAL: _g2.drawLine(x1, b1, x2,b1); break; This does not work right on Log scales!
//      case VERTICAL:   _g2.drawLine(a1,y1, a1, y2); break;
//      default:
//        _g2.drawLine(x1, b1, x2,b1);
//        _g2.drawLine(a1,y1, a1, y2);
//        break;
      case HORIZONTAL: _g2.drawLine(gutters[0], b1, _panel.getWidth()-gutters[2],b1); break;
      case VERTICAL:   _g2.drawLine(a1,gutters[1], a1, _panel.getHeight()-gutters[3]); break;
      default:
        _g2.drawLine(gutters[0], b1, _panel.getWidth()-gutters[2],b1);
        _g2.drawLine(a1,gutters[1], a1, _panel.getHeight()-gutters[3]);
        break;
    }
  }

// -------------------------------------
// Implementation of Measured3D
// -------------------------------------

  public boolean isMeasured() { return false; }
  
  public double getXMin () {
    if (group==null) return x;
    return group.x + x;
  }
  public double getXMax () {
    if (group==null) return x;
    return group.x + x;
  }
  public double getYMin () {
    if (group==null) return y;
    return group.y + y;
  }
  public double getYMax () {
    if (group==null) return y;
    return group.y + y;
  }
  public double getZMin () {
    if (group==null) return z;
    return group.z + z;
  }
  public double getZMax () {
    if (group==null) return z;
    return group.z + z;
  }

}

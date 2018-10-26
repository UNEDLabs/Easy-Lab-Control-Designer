/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;
import java.awt.*;
import java.awt.geom.*;

public class InteractiveText extends AbstractInteractiveElement {

  /* Implementation variables */
  private double a1=0.0, b1=0.0;
  private double[] coordinates  = new double[3]; // The input for all projections (3 saves computations)
  private double[] pixelOrigin  = new double[3]; // The projection of the origin
  private Object3D[] objects    = new Object3D[] { new Object3D(this,0) };
  protected AffineTransform transform = new AffineTransform();
  protected TextLine textLine;
  private Object lastDisplayObject=null;
  /**
   * Default constructor
   */
  public InteractiveText () { this(""); }

  /**
   * Constructor that accepts the string to be displayed
   */
  public InteractiveText (String _text) {
    textLine = new TextLine();
    style.setFont(new Font("Dialog",Font.PLAIN,18));
    style.setDisplayObject(_text);
    sizeEnabled = false;
  }

// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------

  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    if (!visible) return null;
    if (hasChanged || _panel!=panelWithValidProjection) projectPoints (_panel);
//    if (sizeEnabled     && Math.abs(pixelEndpoint[0]-_xpix)<SENSIBILITY && Math.abs(pixelEndpoint[1]-_ypix)<SENSIBILITY) return sizeTarget;
    if (positionEnabled && Math.abs(pixelOrigin[0]  -_xpix)<SENSIBILITY && Math.abs(pixelOrigin[1]  -_ypix)<SENSIBILITY) return new InteractionTargetElementPosition(this);
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
    projectPoints (_panel);
    drawIt ((Graphics2D) _g, style.edgeColor,style.fillPattern);
  }

// -------------------------------------
//  Private methods
// -------------------------------------

  protected void projectPoints (DrawingPanel _panel) {
//    System.out.println ("Projecting text "+style.displayObject.toString());
    if (group!=null) { coordinates[0] = group.x + x*group.sizex;  coordinates[1] = group.y + y*group.sizey; coordinates[2] = group.z + z*group.sizez; }
    else { coordinates[0] = x;     coordinates[1] = y;     coordinates[2] = z; }
    _panel.project(coordinates,pixelOrigin);
    objects[0].distance = pixelOrigin[2];
//    a1 = pixelOrigin[0]; b1 = pixelOrigin[1];
//    if (style.displayObject!=null) adjustPositionOld (_panel,null);
    hasChanged = false;
    panelWithValidProjection = _panel;
  }

  private void drawIt (Graphics2D _g2, Color _color, Paint _fill) {
    if (style.displayObject==null) return;
//    if (style.font!=null) _g2.setFont (style.font);
//    _g2.setColor (_color);
    if (style.font!=null && style.font!=textLine.getFont()) textLine.setFont (style.font);
    if (lastDisplayObject!=style.displayObject) {
      textLine.setText(style.displayObject.toString());
      lastDisplayObject=style.displayObject;
    }
    textLine.setColor(_color);
    adjustPosition (_g2);

    AffineTransform originalTransform = _g2.getTransform();
    transform.setTransform(originalTransform);
    transform.rotate(-style.angle,pixelOrigin[0],pixelOrigin[1]);
    _g2.setTransform(transform);
    textLine.drawText (_g2,(int) a1,(int) b1);
//    _g2.drawString (style.displayObject.toString(),(int) a1,(int) b1);
    _g2.setTransform(originalTransform);
  }

  private void adjustPosition (Graphics _g) {
    a1 = pixelOrigin[0]; b1 = pixelOrigin[1];
    int lenx = textLine.getWidth(_g);
    int leny = textLine.getHeight(_g)/2;
    switch (style.position) {
      default :
      case Style.CENTERED:   a1-=lenx/2.0; b1+=leny/2.0; break;
      case Style.NORTH:      a1-=lenx/2.0; b1+=leny;     break;
      case Style.SOUTH:      a1-=lenx/2.0;               break;
      case Style.EAST:       a1-=lenx;     b1+=leny/2.0; break;
      case Style.NORTH_EAST: a1-=lenx;     b1+=leny;     break;
      case Style.SOUTH_EAST: a1-=lenx;                               break;
      case Style.WEST:                           b1+=leny/2.0;       break;
      case Style.NORTH_WEST:                     b1+=leny;           break;
      case Style.SOUTH_WEST:                              break;
    }

  }

//  private void adjustPositionOld (DrawingPanel _panel) {
//    a1 = pixelOrigin[0]; b1 = pixelOrigin[1];
//    FontMetrics fm;
//    if (style.font!=null) fm = _panel.getFontMetrics(style.font);
//    else fm = _panel.getFontMetrics(_panel.getFont());
//    int lenx = fm.stringWidth(style.displayObject.toString());
//    int leny = fm.getHeight()/2;
//    switch (style.position) {
//      default :
//      case Style.CENTERED:   a1-=lenx/2.0; b1+=leny/2.0; break;
//      case Style.NORTH:      a1-=lenx/2.0; b1+=leny;     break;
//      case Style.SOUTH:      a1-=lenx/2.0;               break;
//      case Style.EAST:       a1-=lenx;     b1+=leny/2.0; break;
//      case Style.NORTH_EAST: a1-=lenx;     b1+=leny;     break;
//      case Style.SOUTH_EAST: a1-=lenx;                               break;
//      case Style.WEST:                           b1+=leny/2.0;       break;
//      case Style.NORTH_WEST:                     b1+=leny;           break;
//      case Style.SOUTH_WEST:                              break;
//    }
//  }

}

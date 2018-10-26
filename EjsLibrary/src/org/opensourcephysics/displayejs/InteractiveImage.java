/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;
//import org.opensourcephysics.displayejs.utils.Utils;
import java.awt.*;
import java.awt.geom.*;

/**
 * An InteractiveImage is an InteractiveElement that displays an Image
 */
public class InteractiveImage extends AbstractInteractiveElement {
//  static Utils utils=null;

  /* Implementation variables */
  private double a1=0.0, b1=0.0;
  private double[] coordinates  = new double[6]; // the input for all projections
  private double[] pixelOrigin  = new double[5]; // The projection of the origin (and size)
  private Object3D[] objects    = new Object3D[] { new Object3D(this,0) };
  private AffineTransform transform = new AffineTransform();
  private boolean trueSize=false;

  private Image currentImage=null;

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveImage) setTrueSize(((InteractiveImage) _element).getTrueSize());
  }

  /**
   * If true, the image is drawn as is, without resizing it
   * @param _s boolean
   */
  public void setTrueSize (boolean _s) { trueSize = _s; }

  public boolean getTrueSize () { return trueSize; }

  public void needsToProject(DrawingPanel _panel)   {
    super.needsToProject(_panel);
    if (style.displayObject!=currentImage && style.displayObject instanceof Image) {
      currentImage = (Image) style.displayObject;
      Toolkit.getDefaultToolkit().prepareImage(currentImage, -1, -1, _panel);
    }
     
//  if (_panel==this.panelWithValidProjection) This is very unlikely to happen, let's save one check
  panelWithValidProjection = null;
}

// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------

  public void setSensitivity (int _s) { actualSensitivity = _s; }

   public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
     if (!visible) return null;
     if (hasChanged || _panel!=panelWithValidProjection) projectPoints (_panel);
     // In 3D it is not possible to change size interactively, due to the effect of style.position and style.angle
//    if (sizeEnabled      && Math.abs(a2-_xpix)<SENSIBILITY               && Math.abs(b2-_ypix)<SENSIBILITY)               return sizeTarget;
     if (positionEnabled) {
       if (actualSensitivity<=0) { // Any point in the whole image will do
         double xI = pixelOrigin[0]-_xpix, yI = pixelOrigin[1]-_ypix;
         if (style.angle!=0.0) { // Take care of a possible rotation
           double aux = xI*style.cosAngle-yI*style.sinAngle;
           yI = xI*style.sinAngle+yI*style.cosAngle;
           xI = aux;
         }
         if (Math.abs(xI)<pixelOrigin[2]/2 && Math.abs(yI)<pixelOrigin[3]/2) return new InteractionTargetElementPosition(this);
       }
       else { // Only the center of the image
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
    drawIt (_panel,_g2); 
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (!visible) return;
//    if (hasChanged || _panel!=panelWithValidProjection)
    projectPoints (_panel);
    drawIt (_panel,(Graphics2D) _g);
  }

// -------------------------------------
//  Private methods
// -------------------------------------

  protected void projectPoints (DrawingPanel _panel) {
//    System.out.println("Projecting image");
    if (group!=null) {
      coordinates[0] = group.x + x*group.sizex;  coordinates[1] = group.y + y*group.sizey; coordinates[2] = group.z + z*group.sizez;
      coordinates[3] = sizex*group.sizex;        coordinates[4] = sizey*group.sizey;       coordinates[5] = sizez*group.sizez;
    }
    else {
      coordinates[0] = x;     coordinates[1] = y;     coordinates[2] = z;
      coordinates[3] = sizex; coordinates[4] = sizey; coordinates[5] = sizez;
    }
    _panel.project(coordinates,pixelOrigin);
    objects[0].distance = pixelOrigin[4];
    a1 = pixelOrigin[0]; b1 = pixelOrigin[1];
    double sx = pixelOrigin[2], sy = pixelOrigin[3];
    if (trueSize && style.displayObject instanceof Image) {
      sx = ( (Image) style.displayObject).getWidth(null);
      sy = ( (Image) style.displayObject).getHeight(null);
    }
    switch (style.position) {
      default :
      case Style.CENTERED:   a1-=sx/2.0; b1-=sy/2.0; break;
      case Style.NORTH:      a1-=sx/2.0;             break;
      case Style.SOUTH:      a1-=sx/2.0; b1-=sy;     break;
      case Style.EAST:       a1-=sx;     b1-=sy/2.0; break;
      case Style.SOUTH_EAST: a1-=sx;     b1-=sy;     break;
      case Style.NORTH_EAST: a1-=sx;                 break;
      case Style.WEST:                   b1-=sy/2.0; break;
      case Style.SOUTH_WEST:             b1-=sy;     break;
      case Style.NORTH_WEST:                         break;
    }
    hasChanged = false;
    panelWithValidProjection = _panel;
  }

  private void drawIt (DrawingPanel _panel, Graphics2D _g2) {
    if (! (style.displayObject instanceof Image)) return;
    AffineTransform originalTransform = _g2.getTransform();
    transform.setTransform(originalTransform);
    transform.rotate(-style.angle,pixelOrigin[0],pixelOrigin[1]);
    _g2.setTransform(transform);
    if (trueSize) _g2.drawImage((Image) style.displayObject,(int)a1,(int)b1,null);
    else _g2.drawImage((Image) style.displayObject,(int)a1,(int)b1,(int)pixelOrigin[2],(int)pixelOrigin[3],null);
    _g2.setTransform(originalTransform);
  }
  
}

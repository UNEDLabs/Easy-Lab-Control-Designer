/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

public class InteractionTargetElementPosition implements InteractionTarget {

  protected InteractiveElement element;

  InteractionTargetElementPosition(InteractiveElement _element) { element = _element; }

  public InteractionSource getSource () { return element; }

  public Point3D getHotspot (DrawingPanel _panel) {
    if (element.getGroup()==null) return new Point3D(element.getX(), element.getY(), element.getZ());
    return new Point3D(element.getGroup().getX() + element.getX() * element.getGroup().getSizeX(),
                            element.getGroup().getY() + element.getY() * element.getGroup().getSizeY(),
                            element.getGroup().getZ() + element.getZ() * element.getGroup().getSizeZ());
  }

  public void updateHotspot (DrawingPanel _panel, Point3D _point) {
    if (element.getGroup()==null) element.setXYZ (_point.x, _point.y, _point.z);
    else {
      if (element.isGroupEnabled(InteractiveElement.TARGET_POSITION))
        element.getGroup().setXYZ(_point.x-element.getX()*element.getGroup().getSizeX(),
                                  _point.y-element.getY()*element.getGroup().getSizeY(),
                                  _point.z-element.getZ()*element.getGroup().getSizeZ());
      else {
        double x = element.getGroup().getSizeX(),
               y = element.getGroup().getSizeY(),
               z = element.getGroup().getSizeZ();
        if (x==0.0) x = element.getX(); else x = (_point.x - element.getGroup().getX())/x;
        if (y==0.0) y = element.getY(); else y = (_point.y - element.getGroup().getY())/y;
        if (z==0.0) z = element.getZ(); else z = (_point.z - element.getGroup().getZ())/z;
        element.setXYZ ( x,y,z);
      }
    }
  }

  /* Implementation of Interactive */
  // Not to be used directly ?

  public Interactive findInteractive(DrawingPanel _panel, int _xpix, int _ypix) { return element; }
  public void setEnabled(boolean _enabled) { element.setEnabled(InteractiveElement.TARGET_POSITION,_enabled);}
  public boolean isEnabled() { return element.isEnabled(InteractiveElement.TARGET_POSITION); }
  public void setXY(double _x, double _y) { element.setXY(_x,_y); }
  public void setX(double _x){ element.setX(_x); }
  public void setY(double _y){ element.setY(_y); }
  public double getX(){ return element.getX(); }
  public double getY(){ return element.getY(); }

  public boolean isMeasured () { return true; }
  public double getXMin(){ return element.getXMin(); }
  public double getXMax(){ return element.getXMax(); }
  public double getYMin(){ return element.getYMin(); }
  public double getYMax(){ return element.getYMax(); }

  public void draw (DrawingPanel _panel, java.awt.Graphics _g) { element.draw(_panel,_g); }

}



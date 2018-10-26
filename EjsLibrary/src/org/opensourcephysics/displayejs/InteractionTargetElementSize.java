/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

public class InteractionTargetElementSize implements InteractionTarget {
  protected InteractiveElement element;

  InteractionTargetElementSize(InteractiveElement _element) { element = _element; }

  public InteractionSource getSource () { return element; }

  public Point3D getHotspot (DrawingPanel _panel) {
    if (element.getGroup()==null) return new Point3D(element.getX() + element.getSizeX(), element.getY() + element.getSizeY(), element.getZ() + element.getSizeZ());
    return new Point3D (element.getGroup().getX() + (element.getX()+element.getSizeX())*element.getGroup().getSizeX(),
                             element.getGroup().getY() + (element.getY()+element.getSizeY())*element.getGroup().getSizeY(),
                             element.getGroup().getZ() + (element.getZ()+element.getSizeZ())*element.getGroup().getSizeZ());
  }

  public void updateHotspot (DrawingPanel _panel, Point3D _point) {
    if (element.getGroup()==null) element.setSizeXYZ (_point.x-element.getX(), _point.y-element.getY(), _point.z-element.getZ());
    else {
      if (element.isGroupEnabled(InteractiveElement.TARGET_SIZE)) {
        double dx = element.getX()+element.getSizeX(),
               dy = element.getY()+element.getSizeY(),
               dz = element.getZ()+element.getSizeZ();
        if (dx==0.0) dx = element.getGroup().getSizeX(); else dx = (_point.x - element.getGroup().getX())/dx;
        if (dy==0.0) dy = element.getGroup().getSizeY(); else dy = (_point.y - element.getGroup().getY())/dy;
        if (dz==0.0) dz = element.getGroup().getSizeZ(); else dz = (_point.z - element.getGroup().getZ())/dz;
        element.getGroup().setSizeXYZ ( dx,dy,dz);
      }
      else {
        double dx = element.getGroup().getSizeX(),
               dy = element.getGroup().getSizeY(),
               dz = element.getGroup().getSizeZ();
        if (dx==0.0) dx = element.getSizeX(); else dx = (_point.x - element.getGroup().getX())/dx - element.getX();
        if (dy==0.0) dy = element.getSizeY(); else dy = (_point.y - element.getGroup().getY())/dy - element.getY();
        if (dz==0.0) dz = element.getSizeZ(); else dz = (_point.z - element.getGroup().getZ())/dz - element.getZ();
        element.setSizeXYZ ( dx,dy,dz);
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



/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

public class InteractionTargetTracePoint extends InteractionTargetElementPosition {
  Point3D point;

  InteractionTargetTracePoint(InteractiveElement _element, Point3D _point) { super(_element); this.point = _point; }

  public Point3D getHotspot (DrawingPanel _panel) {
    if (element.getGroup()==null) return new Point3D(element.getX()+point.x, element.getY()+point.y, element.getZ()+point.z);
    return new Point3D(element.getGroup().getX() + (element.getX()+point.x) * element.getGroup().getSizeX(),
                            element.getGroup().getY() + (element.getY()+point.y) * element.getGroup().getSizeY(),
                            element.getGroup().getZ() + (element.getZ()+point.z) * element.getGroup().getSizeZ());
  }

  public void updateHotspot (DrawingPanel _panel, Point3D _point) {
    if (element.getGroup()==null) element.setXYZ (_point.x-point.x, _point.y-point.y, _point.z-point.z);
    else {
      if (element.isGroupEnabled(InteractiveElement.TARGET_POSITION))
        element.getGroup().setXYZ(_point.x-(element.getX()+point.x)*element.getGroup().getSizeX(),
                                  _point.y-(element.getY()+point.y)*element.getGroup().getSizeY(),
                                  _point.z-(element.getZ()+point.z)*element.getGroup().getSizeZ());
      else {
        double x = element.getGroup().getSizeX(),
               y = element.getGroup().getSizeY(),
               z = element.getGroup().getSizeZ();
        if (x==0.0) x = element.getX() - point.x; else x = (_point.x - element.getGroup().getX())/x - point.x;
        if (y==0.0) y = element.getY() - point.y; else y = (_point.y - element.getGroup().getY())/y - point.y;
        if (z==0.0) z = element.getZ() - point.z; else z = (_point.z - element.getGroup().getZ())/z - point.z;
        element.setXYZ ( x,y,z);
      }
    }
  }

}



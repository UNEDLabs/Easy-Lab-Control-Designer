/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

/**
 * A particular implmentation of a position target for a cursor
 */
public class InteractionTargetCursorPosition extends InteractionTargetElementPosition {
  static public final int X = 0;
  static public final int Y = 1;
  static public final int XY = 2;

  private int motion = -1;

  InteractionTargetCursorPosition(InteractiveElement _element, int _motion) {
    super(_element);
    motion = _motion;
  }

  public Point3D getHotspot (DrawingPanel _panel) {
    if (element.getGroup()==null) return new Point3D(element.getX(), element.getY(), element.getZ());
    return new Point3D(element.getGroup().getX() + element.getX() * element.getGroup().getSizeX(),
                            element.getGroup().getY() + element.getY() * element.getGroup().getSizeY(),
                            element.getGroup().getZ() + element.getZ() * element.getGroup().getSizeZ());
  }

  public void updateHotspot (DrawingPanel _panel, Point3D _point) {
    switch (motion) {
      case X  : element.setX(_point.x); break;
      case Y  : element.setY(_point.y); break;
      default :
      case XY : element.setXY(_point.x,_point.y); break;
    }
  }

}



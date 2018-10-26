/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

public class InteractionTargetPoligonMovingPoint extends InteractionTargetElementPosition {
  protected InteractivePoligon poligon=null;
  protected int index=-1;

  InteractionTargetPoligonMovingPoint(InteractivePoligon _poligon, int _index) {
    super(_poligon);
    poligon = _poligon;
    this.index = _index;
  }

  public int getPointIndex ()  { return index; }

  public Point3D getHotspot (DrawingPanel _panel) {
    if (element.getGroup()==null)
      return new Point3D(poligon.x + poligon.coordinates[0][index]*poligon.sizex,
                         poligon.y + poligon.coordinates[1][index]*poligon.sizey,
                         poligon.z + poligon.coordinates[2][index]*poligon.sizez);
    return new Point3D (element.getGroup().getX() + (poligon.x + poligon.coordinates[0][index]*poligon.sizex)*element.getGroup().getSizeX(),
                             element.getGroup().getY() + (poligon.y + poligon.coordinates[1][index]*poligon.sizey)*element.getGroup().getSizeY(),
                             element.getGroup().getZ() + (poligon.z + poligon.coordinates[2][index]*poligon.sizez)*element.getGroup().getSizeZ());

  }

  public void updateHotspot (DrawingPanel _panel, Point3D _point) {
    if (element.getGroup()==null) {
      poligon.x = _point.x - poligon.coordinates[0][index]*poligon.sizex;
      poligon.y = _point.y - poligon.coordinates[1][index]*poligon.sizey;
      poligon.z = _point.z - poligon.coordinates[2][index]*poligon.sizez;
    }
    else {
      if (element.isGroupEnabled(InteractiveElement.TARGET_POSITION)) {
        element.getGroup().setXYZ (_point.x - (poligon.x + poligon.coordinates[0][index]*poligon.sizex)*element.getGroup().getSizeX(),
                                   _point.y - (poligon.y + poligon.coordinates[1][index]*poligon.sizey)*element.getGroup().getSizeY(),
                                   _point.z - (poligon.z + poligon.coordinates[2][index]*poligon.sizez)*element.getGroup().getSizeZ());
      }
      else {
        double x = element.getGroup().getSizeX(), y = element.getGroup().getSizeY(), z = element.getGroup().getSizeZ();
        if (x!=0.0) poligon.x = (_point.x - element.getGroup().getX())/x - poligon.coordinates[0][index]*poligon.sizex;
        if (y!=0.0) poligon.y = (_point.y - element.getGroup().getY())/y - poligon.coordinates[1][index]*poligon.sizey;
        if (z!=0.0) poligon.z = (_point.z - element.getGroup().getZ())/z - poligon.coordinates[2][index]*poligon.sizez;
      }
    }
    poligon.hasChanged = true;
  }

}



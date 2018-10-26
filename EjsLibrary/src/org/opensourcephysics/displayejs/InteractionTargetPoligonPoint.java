/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

public class InteractionTargetPoligonPoint extends InteractionTargetElementSize {
  protected InteractivePoligon poligon=null;
  protected int index=-1;

  InteractionTargetPoligonPoint(InteractivePoligon _poligon, int _index) {
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
      poligon.coordinates[0][index] = (_point.x - poligon.x)/poligon.sizex;
      poligon.coordinates[1][index] = (_point.y - poligon.y)/poligon.sizey;
      poligon.coordinates[2][index] = (_point.z - poligon.z)/poligon.sizez;
    }
    else {
      if (element.isGroupEnabled(InteractiveElement.TARGET_SIZE)) {
        double dx = poligon.x + poligon.coordinates[0][index]*poligon.sizex,
               dy = poligon.y + poligon.coordinates[1][index]*poligon.sizey,
               dz = poligon.z + poligon.coordinates[2][index]*poligon.sizez;
        if (dx==0.0) dx = element.getGroup().getSizeX(); else dx = (_point.x - element.getGroup().getX())/dx;
        if (dy==0.0) dy = element.getGroup().getSizeY(); else dy = (_point.y - element.getGroup().getY())/dy;
        if (dz==0.0) dz = element.getGroup().getSizeZ(); else dz = (_point.z - element.getGroup().getZ())/dz;
        element.getGroup().setSizeXYZ ( dx,dy,dz);
      }
      else {
        if (element.getGroup().getSizeX()!=0.0 &&  poligon.sizex!=0.0)
          poligon.coordinates[0][index] = ((_point.x - element.getGroup().getX())/element.getGroup().getSizeX() - poligon.x)/poligon.sizex;
        if (element.getGroup().getSizeY()!=0.0 &&  poligon.sizey!=0.0)
          poligon.coordinates[1][index] = ((_point.y - element.getGroup().getY())/element.getGroup().getSizeY() - poligon.y)/poligon.sizey;
        if (element.getGroup().getSizeZ()!=0.0 &&  poligon.sizez!=0.0)
          poligon.coordinates[2][index] = ((_point.z - element.getGroup().getZ())/element.getGroup().getSizeZ() - poligon.z)/poligon.sizez;
      }
    }
    poligon.hasChanged = true;
  }

}



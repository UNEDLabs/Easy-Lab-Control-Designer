/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

public class InteractionTargetSetElement implements InteractionTarget {
  private ElementSet set;
  private int index;
  private InteractionTarget targetHit;

  InteractionTargetSetElement(ElementSet _set, int _index, InteractionTarget _targetHit) {
    set = _set;
    index = _index;
    targetHit = _targetHit;
  }

  public InteractionSource getSource () { return set; }

  public Point3D getHotspot (DrawingPanel _panel) { return targetHit.getHotspot(_panel); }

  public void updateHotspot (DrawingPanel _panel, Point3D _point) { targetHit.updateHotspot(_panel,_point); }

  /* Extra information */

  public int getElementIndex ()  { return index; }

  public InteractionTarget getElementTarget () { return targetHit; }

  /* Dummy implementation of Interactive */

  public Interactive findInteractive(DrawingPanel _panel, int _xpix, int _ypix) { return null; }
  public void setEnabled(boolean _enabled) { }
  public boolean isEnabled() { return true; }
  public void setXY(double _x, double _y) { }
  public void setX(double _x){ }
  public void setY(double _y){ }
  public double getX(){ return Double.NaN; }
  public double getY(){ return Double.NaN; }

  public boolean isMeasured () { return true; }
  public double getXMin(){ return Double.NaN; }
  public double getXMax(){ return Double.NaN; }
  public double getYMin(){ return Double.NaN; }
  public double getYMax(){ return Double.NaN; }

  public void draw (DrawingPanel _panel, java.awt.Graphics _g) { }

}



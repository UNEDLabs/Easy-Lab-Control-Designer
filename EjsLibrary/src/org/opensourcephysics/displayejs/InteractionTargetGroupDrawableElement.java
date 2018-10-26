/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

public class InteractionTargetGroupDrawableElement implements InteractionTarget {
  private GroupDrawable group;
  private InteractiveElement element;
  private InteractionTarget targetHit;

  InteractionTargetGroupDrawableElement(GroupDrawable _group, InteractiveElement _element, InteractionTarget _targetHit) {
    group = _group;
    element = _element;
    targetHit = _targetHit;
  }

  public InteractionSource getSource () { return group; }

  public Point3D getHotspot (DrawingPanel _panel) { return targetHit.getHotspot(_panel); }

  public void updateHotspot (DrawingPanel _panel, Point3D _point) { targetHit.updateHotspot(_panel,_point); }

  /* Extra information */

  public InteractiveElement getElement() { return element; }

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



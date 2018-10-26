/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : August 2003
 */

package org.opensourcephysics.displayejs;

import java.awt.*;
import java.util.*;
import org.opensourcephysics.display.*;

/**
 * An ElementSet is a simple interactive, drawable3D, measurable3D, that helps create and set
 * some of the values of a large number of InteractiveElements using arrays.
 * It is NOT and InteractiveElement itself, though it is an InteractionSource.  When
 * iteracting with it, it returns targets of the InteractionTargetSetElement class.
 * Because of this peculiarity, you should not add listeners to elements of a set directly.
 * This is th eonly restriction that a set imposes on addressing its elements directly.
 */
public class ElementSet extends AbstractInteractionSource implements Interactive, Drawable3D, Measurable3D  {
  // Configuration variables
  protected int numElements=0;
  protected Class<?> classType;
  protected int elementInteracted=-1;
  protected String name="";

// Implementation variables
  protected boolean canBeMeasured = true;
  protected InteractiveElement[] elements = null;
  protected ArrayList<Object3D> list3D=new ArrayList<Object3D> ();
  protected Object3D[] minimalObjects = new Object3D[1];

  public ElementSet (int _n, Class<?> _aClass) {
//    if (!_aClass.isAssignableFrom(InteractiveElement.class)) { // This comparison doesn't work!
//      System.out.println("Error: Set requires a class that extends InteractiveElement!");
//    }
    if (_n<1) {
      System.out.println("ElementSet error: An element set must contain at least one element!");
      _n = 1;
    }
    classType = _aClass;
    setNumberOfElements(_n);
  }

  public void setName (String _name) { this.name = _name; }
  public String getName () { return this.name; }

  public int getNumberOfElements () { return numElements; }

  public synchronized void setNumberOfElements (int _n) {
    if (_n==numElements || _n<1) return;
    // Keep original settings for the new elements
    InteractiveElement[] oldElements = elements;
    elements = new InteractiveElement[_n];
    try {
      for (int i = 0; i < _n; i++) {
        elements[i] = (InteractiveElement) classType.newInstance();
        elements[i].setSet (this,i);
      }
      numElements = _n;
    }
    catch (Exception exc) {
      System.out.println("Error: ElementSet requires a class that extends InteractiveElement!");
      exc.printStackTrace();
      numElements = 0;
      return;
    }
    if (oldElements==null) return;
    // Keep the previous configuration
    for (int i=0, n=Math.min(elements.length,oldElements.length); i<n; i++) elements[i].copyFrom(oldElements[i]);
    if (elements.length>oldElements.length)
      for (int i=oldElements.length, n=elements.length; i<n; i++) elements[i].copyFrom(oldElements[0]);
    oldElements=null; // Make (double) sure the old elements go to the garbage collector
    setName(name); // make sure elements are named properly
  }

  public InteractiveElement elementAt (int i) { return elements[i]; }

  public InteractiveElement getElement (int i) { return elements[i]; }

  /* Position in space of the elements*/
  public void setXs (double[] _x) {
    int n = numElements;
    if (n>_x.length) n = _x.length;
    for (int i=0; i<n; i++) elements[i].setX(_x[i]);
  }

  public double[] getXs () {
    double x[] = new double[numElements];
    for (int i=0; i<numElements; i++) x[i] = elements[i].getX();
    return x;
  }

  public void setYs (double[] _y) {
    int n = numElements;
    if (n>_y.length) n = _y.length;
    for (int i=0; i<n; i++) elements[i].setY(_y[i]);
  }

  public double[] getYs () {
    double y[] = new double[numElements];
    for (int i=0; i<numElements; i++) y[i] = elements[i].getY();
    return y;
  }

  public void setZs (double[] _z) {
    int n = numElements;
    if (n>_z.length) n = _z.length;
    for (int i=0; i<n; i++) elements[i].setZ(_z[i]);
  }

  public double[] getZs () {
    double z[] = new double[numElements];
    for (int i=0; i<numElements; i++) z[i] = elements[i].getZ();
    return z;
  }

  public void setXYZs (double[] _x, double[] _y, double[] _z) {
    int n = numElements;
    if (n>_x.length) n = _x.length;
    if (n>_y.length) n = _y.length;
    if (n>_z.length) n = _z.length;
    for (int i=0; i<n; i++) elements[i].setXYZ(_x[i],_y[i],_z[i]);
  }

  /* Size of the elements */
  public void setSizeXs (double[] _x) {
    int n = numElements;
    if (n>_x.length) n = _x.length;
    for (int i=0; i<n; i++) elements[i].setSizeX(_x[i]);
  }

  public double[] getSizeXs () {
    double sx[] = new double[numElements];
    for (int i=0; i<numElements; i++) sx[i] = elements[i].getSizeX();
    return sx;
  }

  public void setSizeYs (double[] _y) {
    int n = numElements;
    if (n>_y.length) n = _y.length;
    for (int i=0; i<n; i++) elements[i].setSizeY(_y[i]);
  }

  public double[] getSizeYs () {
    double sy[] = new double[numElements];
    for (int i=0; i<numElements; i++) sy[i] = elements[i].getSizeY();
    return sy;
  }

  public void setSizeZs (double[] _z) {
    int n = numElements;
    if (n>_z.length) n = _z.length;
    for (int i=0; i<n; i++) elements[i].setSizeZ(_z[i]);
  }

  public double[] getSizeZs () {
    double sz[] = new double[numElements];
    for (int i=0; i<numElements; i++) sz[i] = elements[i].getSizeZ();
    return sz;
  }

  public void setSizeXYZs (double[] _x, double[] _y, double[] _z) {
    int n = numElements;
    if (n>_x.length) n = _x.length;
    if (n>_y.length) n = _y.length;
    if (n>_z.length) n = _z.length;
    for (int i=0; i<n; i++) elements[i].setSizeXYZ(_x[i],_y[i],_z[i]);
  }

  /* Visibility and interactivity */
  public void setVisible(boolean _visible) { for (int i=0; i<numElements; i++) elements[i].setVisible(_visible); }
  public boolean isVisible() {
    for (int i=0; i<numElements; i++) if (elements[i].isVisible()) return true;
    return false;
  }
  public void setVisibles (boolean[] _vis) {
    int n = numElements;
    if (n>_vis.length) n = _vis.length;
    for (int i=0; i<n; i++) elements[i].setVisible(_vis[i]);
  }

  public void setEnabled(boolean _enabled) { for (int i=0; i<numElements; i++) elements[i].setEnabled(_enabled); }
  public boolean isEnabled() {
    for (int i=0; i<numElements; i++) if (elements[i].isEnabled()) return true;
    return false;
  }
  public void setEnabled(int _target, boolean _enabled) { for (int i=0; i<numElements; i++) elements[i].setEnabled(_target,_enabled); }
  public boolean isEnabled(int _target) {
    for (int i=0; i<numElements; i++) if (elements[i].isEnabled(_target)) return true;
    return false;
  }
  public void setEnableds (boolean[] _enab) {
    int n = numElements;
    if (n>_enab.length) n = _enab.length;
    for (int i=0; i<n; i++) elements[i].setEnabled(_enab[i]);
  }
  public void setEnableds (int _target, boolean[] _enab) {
    int n = numElements;
    if (n>_enab.length) n = _enab.length;
    for (int i=0; i<n; i++) elements[i].setEnabled(_target,_enab[i]);
  }

  /* Input/Output */
  public String toXML () { return this.toString(); }

// -----------------------------------------
//  Interaction
// -----------------------------------------

  public int getElementInteracted () { return elementInteracted; }

  public int getInteractedIndex() { return elementInteracted; }

  public Interactive findInteractive(DrawingPanel _panel, int _xpix, int _ypix){
    for (int i=0; i<numElements; i++) {
      Interactive iad = elements[i].findInteractive (_panel,_xpix,_ypix);
      if (iad!=null) {
        elementInteracted = i;
        if (iad instanceof InteractionTarget) return new InteractionTargetSetElement(this, i, (InteractionTarget) iad);
        return iad;
      }
    }
//    elementInteracted = -1; This caused problem with delayed actions
    return null;
  }

// -----------------------------------------
//  Implementation of interfaces
// -----------------------------------------

  public void needsToProject(DrawingPanel _panel) { for (int i=0; i<numElements; i++) elements[i].needsToProject(_panel); }

  /* Drawable3D */
  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    if (numElements<=0) return null;
    list3D.clear();
    for (int i=0; i<numElements; i++) {
      Object3D[] objects = elements[i].getObjects3D(_panel);
      if (objects!=null) for (int j=0; j<objects.length; j++) list3D.add(objects[j]);
    }
    if (list3D.size()==0) return null;
    return list3D.toArray(minimalObjects);
  }

  public void draw (DrawingPanel3D _panel, Graphics2D _g2, int _index) {
    System.out.println ("ElementSet draw (i): I should not be called!");
  }

  public void drawQuickly (DrawingPanel3D _panel, Graphics2D _g2) {
    // Reverse order so that interaction is more natural
    for (int i=numElements-1; i>=0; i--) elements[i].drawQuickly(_panel, _g2);
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    //System.out.println ("Drawing "+numElements);
    // Reverse order so that interaction is more natural
    for (int i=numElements-1; i>=0; i--) elements[i].draw(_panel, _g);
  }

  /* Measurable3D */

  /**
   * Whether the element should be taken into account for computing the scales.
   * This is used by DrawingPanel3D's decoration.
   */
  public void canBeMeasured (boolean _canBe) { this.canBeMeasured = _canBe; }

  final public boolean isMeasured () { return (numElements > 0) && canBeMeasured; }
  
  public double getXMin () {
    double min = Double.MAX_VALUE, aux;
    for (int i=0; i<numElements; i++) if ((aux=elements[i].getXMin())<min) min = aux;
    return min;
  }
  public double getXMax () {
    double max = -Double.MAX_VALUE, aux;
    for (int i=0; i<numElements; i++) if ((aux=elements[i].getXMax())>max) max = aux;
    return max;
  }
  public double getYMin () {
    double min = Double.MAX_VALUE, aux;
    for (int i=0; i<numElements; i++) if ((aux=elements[i].getYMin())<min) min = aux;
    return min;
  }
  public double getYMax () {
    double max = -Double.MAX_VALUE, aux;
    for (int i=0; i<numElements; i++) if ((aux=elements[i].getYMax())>max) max = aux;
    return max;
  }
  public double getZMin () {
    double min = Double.MAX_VALUE, aux;
    for (int i=0; i<numElements; i++) if ((aux=elements[i].getZMin())<min) min = aux;
    return min;
  }
  public double getZMax () {
    double max = -Double.MAX_VALUE, aux;
    for (int i=0; i<numElements; i++) if ((aux=elements[i].getZMax())>max) max = aux;
    return max;
  }
  /* A dummy interactive  */
   public void setXY(double _x, double _y) { }
   public void setX(double _x){ }
   public void setY(double _y){ }
   public double getX(){ return Double.NaN; }
   public double getY(){ return Double.NaN; }

}

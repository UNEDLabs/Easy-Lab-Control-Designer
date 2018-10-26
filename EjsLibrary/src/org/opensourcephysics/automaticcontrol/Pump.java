/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */


package org.opensourcephysics.automaticcontrol;

import org.opensourcephysics.displayejs.*;

public class Pump extends PoligonsAndTexts  {
  static final public int RIGHT = 0;
  static final public int LEFT  = 1;

  protected double rotorAngle = Double.NaN;
  private boolean showRotor=false;

  public Pump() {
    super();
    poligon2.setData(rotorData());
    poligon2.setConnections(rotorConnected);
    poligon2.setClosed(false);
    setRotorData (false);
    setShowSecondText (false);
    format = new java.text.DecimalFormat("0.000");
    text.getStyle().setPosition(Style.SOUTH);
    setShowText (false); // By default the value is hidden
  }

  public void setRotorAngle (double _val) {
    rotorAngle = _val;
    setRotorData(true);
  }

  public void setFillColor2(java.awt.Paint _fill) {
    if (_fill instanceof java.awt.Color) poligon2.getStyle().setEdgeColor((java.awt.Color)_fill);
  }

  public void setVisible (boolean _visible) {
    super.setVisible(_visible);
    if (_visible) poligon2.setVisible(showRotor);
  }

// --------------------------------
// New methods
// --------------------------------

  static final private double w = 0.08;
  static final private double w2 = 0.05;
  static final private double h = 0.12;
  static final private boolean[] rotorConnected = new boolean[]{ true, false, true, false };


  static private double[][] rotorData() { return new double[][]{ {-w2,0}, {w2,0}, {0,w2}, {0,-w2} }; }

  static private double[][] pumpRight() {
    int nc = 20,nc2=nc/3;
    double[][] cData = new double[nc+nc2+2][];
    double range = 3*Math.PI/2 + Math.PI/6;
    range = 2*Math.PI;
    for (int i = 0; i < nc; i++) {
      double alpha = Math.PI/2 + i * range / (nc - 1);
      cData[i] = new double[] {w * Math.cos(alpha), w * Math.sin(alpha)};
    }
    cData[nc] = new double[] {h,w};
    cData[nc+1] = new double[] {h,cData[nc-nc2][1]};
    for (int i = 0; i < nc2; i++) {
      cData[nc+2+i] = new double[] {cData[nc-nc2+i][0], cData[nc-nc2+i][1]};
    }
    return cData;
  }

  static private double[][] pumpLeft() {
    int nc = 20,nc2=nc/3;
    double[][] cData = new double[nc+nc2+2][];
    double range = 3*Math.PI/2 + Math.PI/6;
    range = 2*Math.PI;
    for (int i = 0; i < nc; i++) {
      double alpha = Math.PI/2 - i * range / (nc - 1);
      cData[i] = new double[] {w * Math.cos(alpha), w * Math.sin(alpha)};
    }
    cData[nc] = new double[] {-h,w};
    cData[nc+1] = new double[] {-h,cData[nc-nc2][1]};
    for (int i = 0; i < nc2; i++) {
      cData[nc+2+i] = new double[] {cData[nc-nc2+i][0], cData[nc-nc2+i][1]};
    }
    return cData;
  }

  protected double getRadius() { return w; }

  protected void setRotorData(boolean _rotate) {
    if (Double.isNaN(rotorAngle)) {
      data2 = null;
      poligon2.setVisible(showRotor=false);
      return;
    }
    data2 = rotorData();
    double theAngle;
    switch (type) {
      default :
      case RIGHT : theAngle = -rotorAngle; break;
      case LEFT  : theAngle =  rotorAngle; break;
    }

    double a = Math.cos(theAngle), b = Math.sin(theAngle);
    for (int i = 0, n = data2.length; i < n; i++) {
      double auxX = a * data2[i][0] - b * data2[i][1];
      data2[i][1] = b * data2[i][0] + a * data2[i][1];
      data2[i][0] = auxX;
    }
    if (_rotate) {
      rotateData();
      poligon2.setVisible(showRotor=true);
      poligon2.setData(data2);
    }
  }

  protected void setGroupData() {
    switch (type) {
      default :
      case RIGHT : data = pumpRight(); break;
      case LEFT  : data = pumpLeft(); break;
    }
    setRotorData (false);
    textX = 0.0;
    textY = 1.1*w;
    rotateData ();

    if (data2==null) poligon2.setVisible(false);
    else { poligon2.setVisible(true); poligon2.setData(data2); }
    poligon.setData(data);
    text.setXY(textX, textY);
    text.getStyle().setAngle(angle);

/*
    int intAngle = (int) (angle * 180 / Math.PI) % 360;
    if (intAngle < 15) text.getStyle().setPosition(Style.SOUTH);
    else if (intAngle < 45) text.getStyle().setPosition(Style.SOUTH_EAST);
    else if (intAngle < 115) text.getStyle().setPosition(Style.EAST);
    else if (intAngle < 165) text.getStyle().setPosition(Style.NORTH_EAST);
    else if (intAngle < 195) text.getStyle().setPosition(Style.NORTH);
    else if (intAngle < 225) text.getStyle().setPosition(Style.NORTH_WEST);
    else if (intAngle < 285) text.getStyle().setPosition(Style.WEST);
    else if (intAngle < 345) text.getStyle().setPosition(Style.SOUTH_WEST);
    else text.getStyle().setPosition(Style.SOUTH);
*/
  }


}

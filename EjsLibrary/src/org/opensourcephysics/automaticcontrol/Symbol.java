/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */


package org.opensourcephysics.automaticcontrol;

import org.opensourcephysics.displayejs.*;

public class Symbol extends PoligonsAndTexts {
  static final public int CIRCLE_1 = 0;
  static final public int CIRCLE_2 = 1;
  static final public int CIRCLE_3 = 2;
  static final public int CIRCLE_4 = 3;
  static final public int CIRCLE_5 = 4;
  static final public int DIAMOND_1 = 10;
  static final public int DIAMOND_2 = 11;
  static final public int DIAMOND_3 = 12;
  static final public int RECTANGLE_1 = 20;
  static final public int RECTANGLE_2 = 21;

  // Configuration variables

  // Implementation variables

  public Symbol() {
    super();
    setShowSecondText (true);
    format = new java.text.DecimalFormat("##0");
    setShowText (false); // By default (type=0) the value is hidden
    increment = 5;
    hasChanged = true;
  }

// --------------------------------
// New methods
// --------------------------------

  static final private double w = 0.15;
  static final private double wUp = w/10;
  static final private double wDown = -w/4;

  static private double[][] box() { return new double[][] {{-w,-w}, {w,-w}, {w,w}, {-w,w}}; }
  static private double[][] boxBar() { return new double[][] {{-w,0}, {-w,w}, {w,w}, {w,-w}, {-w,-w}, {-w,0}, {w,0}}; }
  static private double[][] diamondBox() { return new double[][] {{-w,0}, {0,-w}, {w,0}, {0,w}, {-w,0}, {-w,-w}, {w,-w}, {w,w}, {-w,w}}; }

  static private double[][] diamond() { return new double[][] {{-w,0}, {0,-w}, {w,0}, {0,w}}; }
  static private double[][] diamondBar() { return new double[][] {{-w,0}, {0,-w}, {w,0}, {0,w}, {-w,0}, {w,0}}; }

  static private double[][] circle(int _add) {
    int nc = 20;
    double[][] cData = new double[nc+_add][2];
    for (int i = 0; i < nc; i++) {
      double alpha = i * 2.0*Math.PI / (nc - 1);
      cData[i] = new double[] {w * Math.cos(alpha), w * Math.sin(alpha)};
    }
    if (_add>0) cData[nc] = new double[] {-w, 0};
    return cData;
  }

  static private double[][] circleBox() {
    int nc = 20;
    double[][] cData = new double[nc+6][];
    cData[0] = new double[] {w,0};
    cData[1] = new double[] {w,w};
    cData[2] = new double[] {-w,w};
    cData[3] = new double[] {-w,-w};
    cData[4] = new double[] {w,-w};
    cData[5] = new double[] {w,0};
    for (int i = 6; i < nc+6; i++) {
      double alpha = (i-6) * 2.0*Math.PI / (nc - 1);
      cData[i] = new double[] {w * Math.cos(alpha), w * Math.sin(alpha)};
    }
    return cData;
  }

  protected double getRadius() { return w; }

  protected void setGroupData() {
    switch (type) {
      case CIRCLE_1:
        data = null;
        data2 = circle(0);
        setShowText(false);
        text2.getStyle().setPosition(Style.CENTERED);
        text2X = text2Y = 0;
        break;
      case CIRCLE_2:
        data = null;
        data2 = circle(0);
        setShowText(true);
        textX = 0; textY = wDown;
        text.getStyle().setPosition(Style.NORTH);
        text2X = 0; text2Y = wUp;
        text2.getStyle().setPosition(Style.SOUTH);
        break;
      case CIRCLE_3:
        data = null;
        data2 = circle(1);
        setShowText(true);
        textX = 0; textY = wDown;
        text.getStyle().setPosition(Style.NORTH);
        text2X = 0; text2Y = wUp;
        text2.getStyle().setPosition(Style.SOUTH);
        break;
      case CIRCLE_4:
        data = circleBox();
        data2 = circle(0);
        setShowText(true);
        textX = 0; textY = wDown;
        text.getStyle().setPosition(Style.NORTH);
        text2X = 0; text2Y = wUp;
        text2.getStyle().setPosition(Style.SOUTH);
        break;
      default:
      case CIRCLE_5:
        data = circleBox();
        data2 = circle(1);
        setShowText(true);
        textX = 0; textY = wDown;
        text.getStyle().setPosition(Style.NORTH);
        text2X = 0; text2Y = wUp;
        text2.getStyle().setPosition(Style.SOUTH);
        break;
      case DIAMOND_1:
        data = null;
        data2 = diamond();
        setShowText(false);
        text2.getStyle().setPosition(Style.CENTERED);
        text2X = text2Y = 0;
        break;
      case DIAMOND_2:
        data = null;
        data2 = diamondBar();
        setShowText(true);
        textX = 0; textY = wDown;
        text.getStyle().setPosition(Style.NORTH);
        text2X = 0; text2Y = wUp;
        text2.getStyle().setPosition(Style.SOUTH);
        break;
      case DIAMOND_3:
        data = diamondBox();
        data2 = diamondBar();
        setShowText(true);
        textX = 0; textY = wDown;
        text.getStyle().setPosition(Style.NORTH);
        text2X = 0; text2Y = wUp;
        text2.getStyle().setPosition(Style.SOUTH);
        break;
      case RECTANGLE_1:
        data = null;
        data2 = box();
        setShowText(false);
        text2.getStyle().setPosition(Style.CENTERED);
        text2X = text2Y = 0;
        break;
      case RECTANGLE_2:
        data = null;
        data2 = boxBar();
        setShowText(true);
        textX = 0; textY = wDown;
        text.getStyle().setPosition(Style.NORTH);
        text2X = 0; text2Y = wUp;
        text2.getStyle().setPosition(Style.SOUTH);
        break;
    }
    rotateData ();
    text.getStyle().setAngle(angle);
    text2.getStyle().setAngle(angle);
    text.setXY(textX, textY);
    text2.setXY(text2X, text2Y);
    if (data==null) poligon.setVisible(false);
    else { poligon.setVisible(true); poligon.setData(data); }
    poligon2.setData(data2);
  }

}

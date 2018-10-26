/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */


package org.opensourcephysics.automaticcontrol;

import org.opensourcephysics.displayejs.*;

public class Valve extends PoligonsAndTexts  {
  static final public int CONTINUOUS = 0;
  static final public int DISCRETE = 1;
  static final public int TRIPLE_CONTINUOUS = 10;
  static final public int TRIPLE_DISCRETE = 11;

  public Valve() {
    super();
    setShowSecondText(false);
    format = new java.text.DecimalFormat("##0%");
    text.getStyle().setPosition(Style.SOUTH);
    setShowText (false); // By default the value is hidden
  }

// --------------------------------
// New methods
// --------------------------------

  static final private double w = 0.08;
  static final private double w2 = 0.07;
  static final private double h = 0.07;
  static final private double t = 0.06;

  protected double getRadius() { return w; }

  protected void setGroupData() {
    if (type>=10) data = new double[][] { {0,0}, {-w,-w/2}, {-w,w/2}, {0,0}, {w,-w/2}, {w,w/2}, {0,0}, {-w/2,-w}, {w/2,-w},{0,0}, {0,h}};
    else          data = new double[][] { {0,0}, {-w,-w/2}, {-w,w/2}, {0,0}, {w,-w/2}, {w,w/2}, {0,0}, {0,h}};
    switch (type%10) {
      case DISCRETE: data2 = new double[][] { {w2,h}, {w2,h+t}, {-w2,h+t}, {-w2,h}, {0,h}}; break;
      default:
      case CONTINUOUS:
        int nc = 9;
        data2 = new double[nc+1][];
        double r = Math.sqrt(h * h / 9 + w2 * w2);
        double alpha1 = Math.atan2(h / 3, w2);
        double range = Math.PI - 2 * alpha1;
        for (int i = 0; i < nc; i++) {
          double alpha = alpha1 + i * range / (nc - 1);
          data2[i] = new double[] {r * Math.cos(alpha), 2 * h / 3 + r * Math.sin(alpha)};
        }
        data2[nc] = new double[] {0, h};
        break;
    }
    textX = 0.0;
    textY = h + 1.4 * t;
    rotateData ();
    poligon.setData (data);
    poligon2.setData(data2);
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

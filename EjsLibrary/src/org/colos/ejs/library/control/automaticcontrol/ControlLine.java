/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.automaticcontrol;

import org.colos.ejs.library.control.displayejs.ControlPoligon;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.Drawable;


/**
 * An interactive particle
 */
public class ControlLine extends ControlPoligon {
  static private final double[][] defData = new double[][] { {0,0}, {0,0.1}, {0.1,0.1} };

  protected Drawable createDrawable () {
    super.createDrawable();
    poligon.setNumberOfPoints (3);
    poligon.setData (defData);
    double[][] data = poligon.getData();
    coordinatesValues[0] = new ObjectValue(data[0]);
    coordinatesValues[1] = new ObjectValue(data[1]);
    coordinatesValues[2] = new ObjectValue(data[2]);
    poligon.setClosed (false);
    return poligon;
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 :
        poligon.setNumberOfPoints (3);
        poligon.setData (defData);
        double[][] data = poligon.getData();
        coordinatesValues[0] = new ObjectValue(data[0]);
        coordinatesValues[1] = new ObjectValue(data[1]);
        coordinatesValues[2] = new ObjectValue(data[2]);
        break;
      case 2 : poligon.setClosed (false);      break;
      default: super.setDefaultValue(_index); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "3";
      case 2 : return "false";
      default : return super.getDefaultValueString(_index);
    }
  }

// -------------------------------------
// Respond to interaction
// -------------------------------------

} // End of class

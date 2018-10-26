/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create display2D elements for inclusion in
 * a DrawingPanel
 * Copyright (c) July 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.colos.ejs.library.control.swing.ControlDrawable;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display2d.ColorMapper;

public abstract class ControlDrawable2D extends ControlDrawable {

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("ColorMode")>=0 || _propertyType.indexOf("PlotMode")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("spectrum"))     return new IntegerValue (ColorMapper.SPECTRUM);
      if (_value.equals("grayscale"))    return new IntegerValue (ColorMapper.GRAYSCALE);
      if (_value.equals("dualshade"))    return new IntegerValue (ColorMapper.DUALSHADE);
      if (_value.equals("red"))          return new IntegerValue (ColorMapper.RED);
      if (_value.equals("green"))        return new IntegerValue (ColorMapper.GREEN);
      if (_value.equals("blue"))         return new IntegerValue (ColorMapper.BLUE);
      if (_value.equals("black"))        return new IntegerValue (ColorMapper.BLACK);
      if (_value.equals("binary"))       return new IntegerValue (ColorMapper.BLACK);
      if (_value.equals("wireframe"))    return new IntegerValue (ColorMapper.WIREFRAME);
      if (_value.equals("norender"))     return new IntegerValue (ColorMapper.NORENDER);
      if (_value.equals("redblueshade")) return new IntegerValue (ColorMapper.REDBLUE_SHADE);
    }
    return super.parseConstant (_propertyType,_value);
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("z")) return "zArray";
    return super.getPropertyCommonName(_property);
  }

// ------------------------------------------------
// Variables
// ------------------------------------------------

} // End of interface

/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.swing.ControlDrawable;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;

/**
 *
 */
public abstract class ControlDrawable3D extends ControlDrawable {

  public static final java.awt.Color NULL_COLOR = org.colos.ejs.library.control.ConstantParserUtil.NULL_COLOR;

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("ElementPosition")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("centered"))         return new IntegerValue (Style.CENTERED);
      if (_value.equals("north"))            return new IntegerValue (Style.NORTH);
      if (_value.equals("south"))            return new IntegerValue (Style.SOUTH);
      if (_value.equals("east"))             return new IntegerValue (Style.EAST);
      if (_value.equals("west"))             return new IntegerValue (Style.WEST);
      if (_value.equals("north_east"))       return new IntegerValue (Style.NORTH_EAST);
      if (_value.equals("north_west"))       return new IntegerValue (Style.NORTH_WEST);
      if (_value.equals("south_east"))       return new IntegerValue (Style.SOUTH_EAST);
      if (_value.equals("south_west"))       return new IntegerValue (Style.SOUTH_WEST);
      // Backwards compatibility
      if (_value.equals("hor_centered"))     return new IntegerValue (Style.NORTH);
      if (_value.equals("hor_center_down"))  return new IntegerValue (Style.NORTH);
      if (_value.equals("hor_center_up"))    return new IntegerValue (Style.SOUTH);
      if (_value.equals("ver_centered"))     return new IntegerValue (Style.WEST);
      if (_value.equals("ver_center_right")) return new IntegerValue (Style.WEST);
      if (_value.equals("ver_center_left"))  return new IntegerValue (Style.EAST);
      if (_value.equals("lower_left"))       return new IntegerValue (Style.SOUTH_WEST);
      if (_value.equals("upper_left"))       return new IntegerValue (Style.NORTH_WEST);
    }

    if (_propertyType.indexOf("MarkerShape")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("ellipse"))         return new IntegerValue(InteractiveParticle.ELLIPSE);
      if (_value.equals("rectangle"))       return new IntegerValue(InteractiveParticle.RECTANGLE);
      if (_value.equals("round_rectangle")) return new IntegerValue(InteractiveParticle.ROUND_RECTANGLE);
      if (_value.equals("wheel"))           return new IntegerValue(InteractiveParticle.WHEEL);
      if (_value.equals("bar"))             return new IntegerValue(InteractiveTrace.BAR);
      if (_value.equals("post"))            return new IntegerValue(InteractiveTrace.POST);
      if (_value.equals("area"))            return new IntegerValue(InteractiveTrace.AREA);
      if (_value.equals("none"))            return new IntegerValue(InteractiveParticle.NONE);
      // Backwards compatibility
      if (_value.equals("filled_circle"))   return new IntegerValue (InteractiveParticle.ELLIPSE);
      if (_value.equals("circle"))          return new IntegerValue (InteractiveParticle.ELLIPSE);
      if (_value.equals("filled_square"))   return new IntegerValue (InteractiveParticle.RECTANGLE);
      if (_value.equals("square"))          return new IntegerValue (InteractiveParticle.RECTANGLE);
      if (_value.equals("no_marker"))       return new IntegerValue (InteractiveParticle.NONE);
    }
    if (_propertyType.indexOf("Resolution")>=0) {
      Resolution res = decodeResolution (_value);
      if (res!=null) return new ObjectValue(res);
    }
    return super.parseConstant (_propertyType,_value);
  }

  static public Resolution decodeResolution (String _value) {
    _value = _value.trim().toLowerCase();
    if (_value.indexOf('.')>=0) { // A double value
      try { return Resolution.createDivisions (Double.parseDouble(_value)); }
      catch (Exception exc) {
        System.out.println("Incorrect double value for resolution");
        exc.printStackTrace();
        return null;
      }
    }
    else if (_value.indexOf(',')<0) { // A single integer
      try { new Resolution (Integer.parseInt(_value)); }
      catch (Exception e) { } // Do not complain, could be a variable
      return null;
    }
    else { // A sequence of integers n1,n2 or n1,n2,n3
      try {
        java.util.StringTokenizer t = new java.util.StringTokenizer(_value,"\",");
        int n1 = Integer.parseInt(t.nextToken());
        int n2 = Integer.parseInt(t.nextToken());
        if (t.hasMoreTokens()) return new Resolution (n1,n2,Integer.parseInt(t.nextToken()));
        return new Resolution (n1,n2);
      } catch (Exception exc) {
        System.out.println("Incorrect integer values for resolution");
        exc.printStackTrace();
        return null;
      }
    }
  }

}

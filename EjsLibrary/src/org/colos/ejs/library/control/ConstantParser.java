/*
 * The control package contains utilities to build and control
 * simulations using a central control.
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control;

import java.util.*;
import java.awt.*;

import org.colos.ejs.library.control.value.*;

/**
 * This class provides static methods that parse a string and return
 * a Value with the corresponding type and value, ready to be used by
 * the setValue() method of ControlElements
 */
public class ConstantParser {

// -------------- public variables and methods -----------

  static public Value fontConstant (Font _currentFont, String _value) {
    Font font = ConstantParserUtil.fontConstant(_currentFont, _value);
    if (font==null) return null; // No commas, not a valid constant
    return new ObjectValue(font);
  }

  static public Value booleanConstant (String _value) {
    if (_value.equals("true"))  return new BooleanValue(true);
    if (_value.equals("false")) return new BooleanValue(false);
    return null; // Not a valid constant
  }

  static public Value colorConstant (String _value) {
    Color color = ConstantParserUtil.colorConstant(_value);
    if (color==null) return null;
    return new ObjectValue(color);
  }

  static public Value formatConstant (String _value) {
    if (_value==null || "null".equals(_value)) return new ObjectValue (null);
    if (_value.startsWith("\"") && _value.endsWith("\"")) _value = _value.substring(1,_value.length()-1);
    _value = org.opensourcephysics.display.TeXParser.parseTeX(_value);
    if(_value.indexOf(";")==-1){//FKH 021103
      int id1=_value.indexOf("0"),id2=_value.indexOf("#"),id=-1;
      if(id1>0&&id2>0)id=id1<id2? id1:id2;
      else if(id1>0)id=id1;
      else if(id2>0)id=id2;
      if(id>0)_value=_value+";"+_value.substring(0,id)+"-"+_value.substring(id);
    }//endFKH 021103
    try { return new ObjectValue (new java.text.DecimalFormat(_value)); }
    catch (IllegalArgumentException _exc) { return null; }
  }

  static public Value rectangleConstant (String _value) {
    if (_value.indexOf(',')<0) return null; // No commas, not a valid constant
    try { // x,y,w,h
      StringTokenizer t = new StringTokenizer(_value,",");
      int x = Integer.parseInt(t.nextToken());
      int y = Integer.parseInt(t.nextToken());
      int w=0,h=0;
      if (t.hasMoreTokens()) w = Integer.parseInt(t.nextToken());
      if (t.hasMoreTokens()) h = Integer.parseInt(t.nextToken());
      return new ObjectValue(new Rectangle(x,y,w,h));
    } catch (Exception exc) { exc.printStackTrace(); return null; }
  }

} // end of class

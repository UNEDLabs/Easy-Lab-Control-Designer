/*
 * The control package contains utilities to build and control
 * simulations using a central control.
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control;

import java.util.*;
import java.awt.*;

/**
 * This class provides static methods that parse a string and return
 * a Value with the corresponding type and value, ready to be used by
 * the setValue() method of ControlElements
 */
public class ConstantParserUtil {

// -------------- public variables and methods -----------

  public static final java.awt.Color NULL_COLOR = new java.awt.Color(0,0,0,0);
  static private Font defaultFont = new Font ("Dialog",12,Font.PLAIN);

  static public Font fontConstant (Font _currentFont, String _value) {
    if (_value.indexOf(',')<0) return null; // No commas, not a valid constant
    if (_currentFont==null) _currentFont = defaultFont;
    int style = _currentFont.getStyle();
    int size  = _currentFont.getSize();
    String name=null;
    StringTokenizer t = new StringTokenizer(_value,",",true);
    if (t.hasMoreTokens()) {
      name = t.nextToken();
      if (name.equals(",")) name = _currentFont.getName();
      else if (t.hasMoreTokens()) t.nextToken(); // read out next ','
    }
    else name = _currentFont.getName();
    if (t.hasMoreTokens()) {
      String styleStr = t.nextToken().toLowerCase();
      style = _currentFont.getStyle();
      if (!styleStr.equals(",")) {
        if (styleStr.indexOf("plain")!=-1)  style = java.awt.Font.PLAIN;
        if (styleStr.indexOf("bold")!=-1)   style = java.awt.Font.BOLD;
        if (styleStr.indexOf("italic")!=-1) style = style | java.awt.Font.ITALIC;
        if (t.hasMoreTokens()) t.nextToken(); // read out next ','
      }
    }
    if (t.hasMoreTokens()) try { size = Integer.parseInt(t.nextToken());}
                           catch (Exception exc) { size = _currentFont.getSize(); }
    return new Font (name,style,size);
  }

  static public Color colorConstant (String _value) {
    if (_value.indexOf(',')>=0) { // format is red,green,blue
      try {
        StringTokenizer t = new StringTokenizer(_value,":,");
        int r = Integer.parseInt(t.nextToken());
        int g = Integer.parseInt(t.nextToken());
        int b = Integer.parseInt(t.nextToken());
        int alpha;
        if (t.hasMoreTokens()) alpha = Integer.parseInt(t.nextToken());
        else alpha = 255;
        if (r<0) r = 0; else if (r>255) r = 255;
        if (g<0) g = 0; else if (g>255) g = 255;
        if (b<0) b = 0; else if (b>255) b = 255;
        if (alpha<0) alpha = 0; else if (alpha>255) alpha = 255;
        return new Color (r,g,b,alpha);
      } catch (Exception exc) { exc.printStackTrace(); return null; }
    }
    if (_value.startsWith("Color.")) _value = _value.substring(6);
    _value = _value.toUpperCase();
    if (_value.equals("NULL")      || _value.equals("NONE")) return NULL_COLOR;
    if (_value.equals("BLACK"))     return Color.black;
    if (_value.equals("BLUE"))      return Color.blue;
    if (_value.equals("CYAN"))      return Color.cyan;
    if (_value.equals("DARKGRAY"))  return Color.darkGray;
    if (_value.equals("GRAY"))      return Color.gray;
    if (_value.equals("GREEN"))     return Color.green;
    if (_value.equals("LIGHTGRAY")) return Color.lightGray;
    if (_value.equals("MAGENTA"))   return Color.magenta;
    if (_value.equals("ORANGE"))    return Color.orange;
    if (_value.equals("PINK"))      return Color.pink;
    if (_value.equals("RED"))       return Color.red;
    if (_value.equals("WHITE"))     return Color.white;
    if (_value.equals("YELLOW"))    return Color.yellow;
    return null; // Not a valid constant
  }

} // end of class

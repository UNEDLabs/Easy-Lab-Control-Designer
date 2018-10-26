/*
 * The control package contains utilities to build and control
 * simulations using a central control.
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.value.*;

import java.util.*;
import java.awt.*;

import javax.swing.SwingConstants;

/**
 * This class provides static methods that parse a string and return
 * a Value with the corresponding type and value, ready to be used by
 * the setValue() method of ControlElements
 */
public class ConstantParser extends org.colos.ejs.library.control.ConstantParser {

// toString methods. Only required for values which can be changed interactively
// by any element

  static public String toString (Point p) { return "\""+p.x+","+p.y+"\""; }
  static public String toString (Dimension d) { return "\""+d.width+","+d.height+"\""; }

// -------------- public methods -----------

  static public Value pointConstant (String _value) {
    _value = _value.trim().toLowerCase();
    if (_value.startsWith("\"") && _value.endsWith("\"")) _value = _value.substring(1,_value.length()-1);
    if ("center".equals(_value)) {
      Rectangle bounds = org.colos.ejs.library.control.EjsControl.getDefaultScreenBounds();
      return new ObjectValue (new Point(bounds.x+bounds.width/2,bounds.y+bounds.height/2));
    }
    if (_value.indexOf(',')<0) return null; // No commas, not a valid constant
    try { // x,y
      StringTokenizer t = new StringTokenizer(_value,",");
      int x = Integer.parseInt(t.nextToken());
      int y = Integer.parseInt(t.nextToken());
      return new ObjectValue (new Point (x,y));
    } catch (Exception exc) { exc.printStackTrace(); return null; }
  }


  static public Value dimensionConstant (String _value) {
    _value = _value.trim().toLowerCase();
    if (_value.startsWith("\"") && _value.endsWith("\"")) _value = _value.substring(1,_value.length()-1);
    if ("pack".equals(_value)) return new StringValue("pack");
    if (_value.indexOf(',')<0) return null; // No commas, not a valid constant
    try { // w,h
      StringTokenizer t = new StringTokenizer(_value,",");
      int w = Integer.parseInt(t.nextToken());
      int h = Integer.parseInt(t.nextToken());
      return new ObjectValue(new Dimension (w,h));
    } catch (Exception exc) { exc.printStackTrace(); return null; }
  }

  static public Value placementConstant (String _value) {
    _value = _value.trim().toLowerCase();
    if      (_value.equals("bottom")) return new IntegerValue(javax.swing.SwingConstants.BOTTOM);
    else if (_value.equals("left"))   return new IntegerValue(javax.swing.SwingConstants.LEFT);
    else if (_value.equals("right"))  return new IntegerValue(javax.swing.SwingConstants.RIGHT);
    else if (_value.equals("top"))    return new IntegerValue(javax.swing.SwingConstants.TOP);
    else return null;
  }

  static public Value layoutConstant (Container _container, String _value) {
    _value = _value.trim().toLowerCase();
    StringTokenizer tkn = new StringTokenizer (_value,":, ");
    String type = tkn.nextToken();
    if (type.equals("flow")) {  // java.awt.FlowLayout
      if (tkn.hasMoreTokens()) try {
        int align;
        String alignStr = tkn.nextToken();
        if      (alignStr.equals("left"))   align = FlowLayout.LEFT;
        else if (alignStr.equals("right"))  align = FlowLayout.RIGHT;
        else align = FlowLayout.CENTER;
        if (tkn.hasMoreTokens()) {
          int hgap = Integer.parseInt(tkn.nextToken());
          int vgap = Integer.parseInt(tkn.nextToken());
          return new ObjectValue(new FlowLayout(align,hgap,vgap));
        }
        return new ObjectValue(new FlowLayout(align));
      }
      catch (Exception exc) { exc.printStackTrace(); }
      return new ObjectValue(new FlowLayout());
    }

    if (type.equals("grid")) {  // java.awt.GridLayout
      if (tkn.hasMoreTokens()) try {
        int rows = Integer.parseInt(tkn.nextToken());
        int cols = Integer.parseInt(tkn.nextToken());
        if (tkn.hasMoreTokens()) {
          int hgap = Integer.parseInt(tkn.nextToken());
          int vgap = Integer.parseInt(tkn.nextToken());
          return new ObjectValue(new GridLayout(rows,cols,hgap,vgap));
        }
        return new ObjectValue(new GridLayout(rows,cols));
      }
      catch (Exception exc) { exc.printStackTrace(); }
      return new ObjectValue (new GridLayout());
    }

    if (type.equals("border")) {  // java.awt.BorderLayout
      if (tkn.hasMoreTokens()) try {
        int hgap = Integer.parseInt(tkn.nextToken());
        int vgap = Integer.parseInt(tkn.nextToken());
        return new ObjectValue (new BorderLayout(hgap,vgap));
      }
      catch (Exception exc) { exc.printStackTrace(); }
      return new ObjectValue (new BorderLayout());
    }

    if (type.equals("hbox")) return new ObjectValue (new javax.swing.BoxLayout(_container,javax.swing.BoxLayout.X_AXIS));

    if (type.equals("vbox")) return new ObjectValue (new javax.swing.BoxLayout(_container,javax.swing.BoxLayout.Y_AXIS));

    return null;
  }

  static public Value constraintsConstant (String _value) {
    _value = _value.trim().toLowerCase();
    if (_value.equals("north"))    return new StringValue(BorderLayout.NORTH);
    if (_value.equals("south"))    return new StringValue(BorderLayout.SOUTH);
    if (_value.equals("east"))     return new StringValue(BorderLayout.EAST);
    if (_value.equals("west"))     return new StringValue(BorderLayout.WEST);
    if (_value.equals("center"))   return new StringValue(BorderLayout.CENTER);
    return new StringValue(BorderLayout.CENTER);
  }

  static public Value orientationConstant (String _value) {
    _value = _value.trim().toLowerCase();
    if (_value.equals("vertical")) return new IntegerValue(SwingConstants.VERTICAL);
    return new IntegerValue(SwingConstants.HORIZONTAL);
  }

  static public Value alignmentConstant (String _value) {
    _value = _value.trim().toLowerCase();
    if (_value.indexOf("top")!=-1)      return new IntegerValue(javax.swing.SwingConstants.TOP);
    if (_value.indexOf("center")!=-1)   return new IntegerValue(javax.swing.SwingConstants.CENTER);
    if (_value.indexOf("bottom")!=-1)   return new IntegerValue(javax.swing.SwingConstants.BOTTOM);
    if (_value.indexOf("left")!=-1)     return new IntegerValue(javax.swing.SwingConstants.LEFT);
    if (_value.indexOf("right")!=-1)    return new IntegerValue(javax.swing.SwingConstants.RIGHT);
    if (_value.indexOf("leading")!=-1)  return new IntegerValue(javax.swing.SwingConstants.LEADING);
    if (_value.indexOf("trailing")!=-1) return new IntegerValue(javax.swing.SwingConstants.TRAILING);
    return new IntegerValue(javax.swing.SwingConstants.CENTER);
  }

} // end of class

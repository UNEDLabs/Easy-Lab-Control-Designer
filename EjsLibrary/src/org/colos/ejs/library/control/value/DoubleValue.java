/*
 * The value package contains utilities to work with primitives
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.value;

 /**
  * A <code>DoubleValue</code> is a <code>Value</code> object that
  * holds a double value.
  * <p>
  * @see     Value
  */
public class DoubleValue extends Value {
  public double value;

  public DoubleValue(double _val) { value = _val; }

  public boolean getBoolean() { return (value!=0.0); }

  public int     getInteger() { return (int) Math.round(value); }

  public double  getDouble()  { return value; }

  public String  getString()  { return String.valueOf(value); }

  public Object  getObject()  { return null; }
}
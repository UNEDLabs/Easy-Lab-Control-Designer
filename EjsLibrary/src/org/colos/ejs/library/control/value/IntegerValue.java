/*
 * The value package contains utilities to work with primitives
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.value;

 /**
  * A <code>IntegerValue</code> is a <code>Value</code> object that
  * holds an integer value.
  * <p>
  * @see     Value
  */
public class IntegerValue extends Value {
  public int value;

  public IntegerValue(int _val) { value = _val; }

  public boolean getBoolean() { return (value!=0); }

  public int     getInteger() { return value; }

  public double  getDouble()  { return value; }

  public String  getString()  { return String.valueOf(value); }

  public Object  getObject()  { return null; }

}


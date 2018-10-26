/*
 * The value package contains utilities to work with primitives
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.value;

 /**
  * A <code>BooleanValue</code> is a <code>Value</code> object that
  * holds a boolean value.
  * <p>
  * @see     Value
  */
public class BooleanValue extends Value {
  public boolean value;

  public BooleanValue(boolean _val) { value = _val; }

  public boolean getBoolean() { return value; }

  public int getInteger() { 
    if (value) return 1; 
    return 0; 
  }

  public double getDouble()  { 
    if (value) return 1.0; 
    return 0.0; 
  }

  public String getString()  { 
    if (value) return "true"; 
    return "false"; 
  }

  public Object getObject()  { return null; }

}


/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) May 2005 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

import java.util.*;

/**
 * A base class to store and retrieve data
 */

public class Memory {

  static public String getResource(String key) {
    return org.colos.ejs.library.Simulation.ejsRes.getString(key);
  }

  protected Hashtable<String, Object> hashTable = new Hashtable<String, Object>();

  public void clear() {
    hashTable.clear();
  }

  public void setValue (String variable, boolean value) {
    hashTable.put(variable, new Boolean(value));
  }

  public void setValue (String variable, int value) {
    hashTable.put(variable, new Integer(value));
  }

  public void setValue (String variable, double value) {
    hashTable.put(variable, new Double(value));
  }

  public void setValue (String variable, Object value) {
    hashTable.put(variable, value);
  }

  public boolean getBoolean (String variable){
    try {
      Boolean value = (Boolean) hashTable.get(variable);
      return value.booleanValue();
    }
    catch (Exception exc) {
      System.out.println(getResource("ReadError")+" "+variable);
      exc.printStackTrace();
      return false;
    }
  }

  public int getInt (String variable){
    try {
      Integer value = (Integer) hashTable.get(variable);
      return value.intValue();
    }
    catch (Exception exc) {
      System.out.println(getResource("ReadError")+" "+variable);
      exc.printStackTrace();
      return 0;
    }
  }

  public double getDouble (String variable){
    try {
      Double value = (Double) hashTable.get(variable);
      return value.doubleValue();
    }
    catch (Exception exc) {
      System.out.println(getResource("ReadError")+" "+variable);
      exc.printStackTrace();
      return 0.0;
    }
  }

  public String getString (String variable){
    try {
      String value = (String) hashTable.get(variable);
      return value;
    }
    catch (Exception exc) {
      System.out.println(getResource("ReadError")+" "+variable);
      exc.printStackTrace();
      return "";
    }
  }

  public Object getObject (String variable){
    try {
      return hashTable.get(variable);
    }
    catch (Exception exc) {
      System.out.println(getResource("ReadError")+" "+variable);
      exc.printStackTrace();
      return null;
    }
  }

} // End of class


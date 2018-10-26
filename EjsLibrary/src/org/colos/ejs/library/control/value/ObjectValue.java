/*
 * The value package contains utilities to work with primitives
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.value;

 /**
  * @see     Value
  */

public class ObjectValue extends Value {
  public Object value;

  public ObjectValue (Object _val) { value = _val; }

  public boolean getBoolean() {
    if (value==null) return false;
    return value.toString().equals("true");
  }

  public int getInteger() {
    return (int) Math.round(getDouble());
  }

  public double  getDouble()  {
    try { return Double.valueOf(value.toString()).doubleValue(); }
    catch (Exception exc) { return 0.0; }
  }

  public String  getString()  {
    if (value==null) return null;
    //System.out.println ("Value is "+value);
    if (value instanceof double[]) {
      double[] data = (double[]) value;
      String txt = "new double[]{"; 
      for (int i=0; i<data.length; i++) { 
        if (i>0) txt +=","; 
        txt += data[i]; 
      }
      return txt+"}";
    }
    if (value instanceof int[]) {
      int[] data = (int[]) value;
      String txt = "new int[]{"; 
      for (int i=0; i<data.length; i++) { 
        if (i>0) txt +=","; 
        txt += data[i]; 
      }
      return txt+"}";
    }
    if (value instanceof double[][]) {
      double[][] data = (double[][]) value;
      //System.out.println ("dim = "+data.length+","+data[0].length);
      String txt = "new double[][]{"; 
      for (int i=0; i<data.length; i++) { 
        if (i>0) txt +=",{"; 
        else txt += "{";
        for (int j=0; j<data[i].length; j++) {
          if (j>0) txt +=","; 
          txt += data[i][j];
        }
        txt += "}";
      }
      //System.out.println ("Returning "+txt);
      return txt+"}";
    }
    if (value instanceof int[][]) {
      int[][] data = (int[][]) value;
      String txt = "new int[][]{"; 
      for (int i=0; i<data.length; i++) { 
        if (i>0) txt +=",{"; 
        else txt += "{";
        for (int j=0; j<data[i].length; j++) {
          if (j>0) txt +=","; 
          txt += data[i][j];
        }
        txt += "}";
      }
      return txt+"}";
    }
    if (value instanceof double[][][]) {
      double[][][] data = (double[][][]) value;
      //System.out.println ("dim = "+data.length+","+data[0].length);
      String txt = "new double[][][]{"; 
      for (int i=0; i<data.length; i++) { 
        if (i>0) txt +=",{"; 
        else txt += "{";
        for (int j=0; j<data[i].length; j++) {
          if (j>0) txt +=",{"; 
          else txt += "{";
          for (int k=0; k<data[i][j].length; k++) {
            if (k>0) txt +=","; 
            txt += data[i][j][k];
          }
          txt += "}";
        }
        txt += "}";
      }
      //System.out.println ("Returning "+txt);
      return txt+"}";
    }
    if (value instanceof int[][][]) {
      int[][][] data = (int[][][]) value;
      //System.out.println ("dim = "+data.length+","+data[0].length);
      String txt = "new int[][][]{"; 
      for (int i=0; i<data.length; i++) { 
        if (i>0) txt +=",{"; 
        else txt += "{";
        for (int j=0; j<data[i].length; j++) {
          if (j>0) txt +=",{"; 
          else txt += "{";
          for (int k=0; k<data[i][j].length; k++) {
            if (k>0) txt +=","; 
            txt += data[i][j][k];
          }
          txt += "}";
        }
        txt += "}";
      }
      //System.out.println ("Returning "+txt);
      return txt+"}";
    }    
    return value.toString();
  }

  public Object  getObject()  { return value; }

//  public void copyInto (double[] array) {
//    double[] data = (double[]) value;
//    int n = data.length;
//    if (array.length<n) n = array.length;
//    System.arraycopy(data,0,array,0,n);
//  }
//
//  public void copyInto (double[][] array) {
//    double[][] data = (double[][]) value;
//    int n = data.length;
//    if (array.length<n) n = array.length;
//    for (int i=0; i<n; i++) {
//      int ni = data[i].length;
//      if (array[i].length<ni) ni = array[i].length;
//      System.arraycopy(data[i],0,array[i],0,ni);
//    }
//  }

}


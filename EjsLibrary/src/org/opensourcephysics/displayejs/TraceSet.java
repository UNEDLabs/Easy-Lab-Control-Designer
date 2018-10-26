/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;

import java.awt.Color;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.Dataset;

public class TraceSet extends ElementSet implements Data {

  public TraceSet (int _n) { super (_n,InteractiveTrace.class); }

  public InteractiveTrace traceAt (int i) { return (InteractiveTrace) elements[i]; }

  public void clear() {
    for (int i=0; i<numElements; i++) ((InteractiveTrace) elements[i]).clear();
  }

  public void setName (String _name) {
    super.setName(_name);
    for (int i=0; i<numElements; i++) ((InteractiveTrace) elements[i]).setName(_name+"_"+i);
  }

  public void addPoints (double[] _x, double[] _y, double[] _z) {
    int n = numElements;
    if (n>_x.length) n = _x.length;
    if (n>_y.length) n = _y.length;
    if (_z==null) {
      for (int i=0; i<n; i++) ((InteractiveTrace) elements[i]).addPoint(_x[i],_y[i],0.0);
    }
    else {
      if (n>_z.length) n = _z.length;
      for (int i=0; i<n; i++) ((InteractiveTrace) elements[i]).addPoint(_x[i],_y[i],_z[i]);
    }
  }

  // ----------------------------------------------------
  // Implementation of Data
  // ----------------------------------------------------

  protected int datasetID = hashCode();

  public void setID(int id) { datasetID = id; }

  public int getID() { return datasetID; }

  public double[][] getData2D() { return null; }

  public double[][][] getData3D() { return null; }

  public String[] getColumnNames() { return null; }

  public Color[] getLineColors() { 
    return new Color[] { Color.BLACK, Color.BLUE}; 
  }

  public Color[] getFillColors() { 
    return new Color[] { Color.BLACK, Color.RED};
  }

  public java.util.List<Data> getDataList() {
    java.util.List<Data> list = new java.util.ArrayList<Data>();
    for (int i=0; i<numElements; i++) list.add((InteractiveTrace)elements[i]);
    return list;
  }

  public java.util.ArrayList<Dataset>  getDatasets() { return null; }
  
}

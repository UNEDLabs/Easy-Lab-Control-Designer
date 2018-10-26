/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.util.*;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.Dataset;

/**
 * <p>Title: Set</p>
 * <p>Description: A Set is a Group which all its elements of the same type.
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class Set extends Group {

//  private Dataset dataset=null;
  private String xLabel="x", yLabel="y", zLabel="z";
  
  public void setXLabel(String label) { xLabel = label; }
  
  public void setYLabel(String label) { yLabel = label; }
  
  public void setZLabel(String label) { zLabel = label; }

  // ----------------------------------------------------
  // Implementation of Data
  // ----------------------------------------------------

  @Override
  public java.util.List<Data> getDataList() { return null; }
  
  public double[][] getData2D() {
    List<Element> list = getElements();
    double[][] data = new double[3][list.size()];
    int index = 0;
    for (Element el : list) {
      data[0][index] = el.getX();
      data[1][index] = el.getY();
      data[2][index] = el.getZ();
      index++;
    }
    return data; 
  }

  public String[] getColumnNames() {
    for (Element el : getElements()) if (el instanceof Data) return ((Data) el).getColumnNames();
    return new String[] {xLabel, yLabel, zLabel}; 
  }
  
  public java.util.ArrayList<Dataset>  getDatasets() { return null; } 
  

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */

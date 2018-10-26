/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.utils.mapping;

  /**
   * <p>Title: Mapping</p>
   * A mapping is a class that maps XYZ coordinates to the coordinates of the
   * first, second, and third axes. 
   * @author Francisco Esquembre
   * @version August 2009
   */
public interface Mapping {
  static public final int MAP_XYZ = 0;
  static public final int MAP_XZY = 1;
  static public final int MAP_YXZ = 2;
  static public final int MAP_YZX = 3;
  static public final int MAP_ZXY = 4;
  static public final int MAP_ZYX = 5;

  /**
   * Reorders the array with the coordinates for each axis in the correct order
   * @param point
   * @return the given array
   */
  public double[] map(double[] point);

  /**
   * Inverts the reordering
   * @param point
   * @return the given array
   */
  public double[] inverse(double[] point);
  
  /**
   * Whether the resulting axes are positively oriented
   * @return boolean
   */
  public boolean isPositivelyOriented();
  
  //CJB
  public double[] quatForPrimitives();

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

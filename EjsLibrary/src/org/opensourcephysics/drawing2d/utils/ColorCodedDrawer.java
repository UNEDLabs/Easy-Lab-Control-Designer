/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d.utils;

import java.awt.*;

import javax.swing.JFrame;

import org.opensourcephysics.display2d.ColorMapper;
import org.opensourcephysics.display2d.ZExpansion;

/**
 * <p>Title: ColorCodedDrawer</p>
 * <p>Description: ColorCodedDrawer is a utility class that allows drawing a collection of poligons using a color code.</p>
 * <p>Each point in the polygon has a double value associated to it and the drawer uses a color mapper to find color subpolygons 
 * for each of the levels of the mapper</p>
 */
public class ColorCodedDrawer {

	// Configuration variables
	private boolean mSymmetricZ=false;

	// Implementation variables
	private ColorMapper mColorMapper = new ColorMapper(16, -1, 1, ColorMapper.SPECTRUM);
	private double[] mThresholds = mColorMapper.getColorThresholds();


	// -------------------------------------
	// Public methods for coloring the tiles
	// -------------------------------------

	/**
	 * Shows how values map to colors.
	 */
	public JFrame showLegend() {
		return mColorMapper.showLegend();
	}

	/**
	 * Sets the floor and ceiling values for the colors.
	 * @param floor
	 * @param ceil
	 */
	public void setScale(double floor, double ceil) {
		mColorMapper.setScale(floor, ceil);
		mColorMapper.updateLegend(null);
		mThresholds = mColorMapper.getColorThresholds();
	}

	/**
	 * Gets the floor for scaling the z data.
	 * @return double
	 */
	public double getFloor() {
		return mColorMapper.getFloor();
	}

	/**
	 * Gets the ceiling for scaling the z data.
	 * @return double
	 */
	public double getCeiling() {
		return mColorMapper.getCeil();
	}

	/**
	 * Sets the floor and ceiling colors.
	 *
	 * @param floorColor
	 * @param ceilColor
	 */
	public void setFloorCeilColor(Color floorColor, Color ceilColor) {
		mColorMapper.setFloorCeilColor(floorColor, ceilColor);
	}

	/**
	 * Determines the palette type that will be used.
	 * @param type
	 */
	public void setPaletteType(int type) {
		mColorMapper.setPaletteType(type);
	}

	/**
	 * Sets the colors that will be used between the floor and ceiling values.
	 *
	 * @param colors
	 */
	public void setColorPalette(Color[] colors) {
		mColorMapper.setColorPalette(colors);
	}

	/**
	 * Forces the z-scale to be symmetric about zero.
	 * Forces zmax to be positive and zmin=-zmax when in autoscale mode.
	 *
	 * @param symmetric
	 */
	public void setSymmetricZ(boolean symmetric){
		mSymmetricZ=symmetric;
	}

	/**
	 * Gets the symmetric z flag.  
	 */
	public boolean isSymmetricZ(){
		return mSymmetricZ;
	}

	/**
	 * Expands the z scale so as to enhance values close to zero.
	 *
	 * @param expanded boolean
	 * @param expansionFactor double
	 */
	public void setExpandedZ(boolean expanded, double expansionFactor) {
		if(expanded&&(expansionFactor>0)) {
			ZExpansion zMap = new ZExpansion(expansionFactor);
			mColorMapper.setZMap(zMap);
		} else {
			mColorMapper.setZMap(null);
		}
	}

	// -------------------------------------
	// Utility methods
	// -------------------------------------

	public Color doubleToColor(double value) {
	  int valueIndex = mColorMapper.doubleToIndex(value);
    return mColorMapper.indexToColor(valueIndex);
	}
  
	/**
	 * Sets the scales for the ColorMapper
	 * @param min double
	 * @param max double
	 */
	public void setAutoscale(double min, double max) {
		double ceil = max;
		double floor = min;
		if (mSymmetricZ) {
			ceil=Math.max(Math.abs(min),Math.abs(max));
			floor=-ceil;
		}
		setScale(floor, ceil);
	}

	/**
	 * Draws a polygon with the given values
	 * @param g2
	 * @param a the x coordinates for the points (in pixel)
	 * @param b the y coordinates for the points (in pixel)
	 * @param values The value for each point
	 */
	public void drawColorCoded(Graphics2D g2, int[] a, int b[], double[] values, boolean drawBorder){
		int nVertex = a.length;
		int vertexIndex[] = new int[a.length];

		// Compute vertex indexes and their range
		int minIndex = Integer.MAX_VALUE, maxIndex = Integer.MIN_VALUE; 
		for(int i=0; i<nVertex; i++) {
			int valueIndex = mColorMapper.doubleToIndex(values[i]); // from -1 to numColors
			minIndex = Math.min(valueIndex, minIndex);
			maxIndex = Math.max(valueIndex, maxIndex);
			vertexIndex[i] = valueIndex; 
		}

		// Computes the subpoligon in each region
		int newCornersX[] = new int[2*nVertex];
		int newCornersY[] = new int[2*nVertex];
		for (int levelIndex = minIndex; levelIndex<=maxIndex; levelIndex++) { // for each level affected
			int pointsAdded = 0;
			for (int point = 0; point<nVertex; point++) { // for each point
				int nextPoint = (point+1) % nVertex;

				if (vertexIndex[point]<=levelIndex && vertexIndex[nextPoint]>=levelIndex) { // There is a bottom-up change of level
					if (vertexIndex[point]==levelIndex) { // the point is on the current level
						newCornersX[pointsAdded] = a[point];
						newCornersY[pointsAdded] = b[point];
						pointsAdded++;
					}
					else { // Add the point where the change occurs
						//(x,y,levels[l]) = (x_p, y_p, z_p) + lambda((x_n, y_n, z_n)-(x_p, y_p, z_p))
						//where (x_p, y_p) are point's coord and z_p can be calculated as the projection of (x_p, y_p) over levels vector 
						//and (x_n, y_n) are next's coord and z_n can be calculated as the projection of (x_n, y_n) over levels vector
						double lambda = (mThresholds[levelIndex]-values[point])/(values[nextPoint]-values[point]);
						newCornersX[pointsAdded] = (int) Math.round(a[point]+lambda*(a[nextPoint]-a[point]));
						newCornersY[pointsAdded] = (int) Math.round(b[point]+lambda*(b[nextPoint]-b[point]));
						pointsAdded++;        
					}
					if (vertexIndex[nextPoint]>levelIndex) { // This segment contributes with a second point
						double lambda = (mThresholds[levelIndex+1]-values[point])/(values[nextPoint]-values[point]);
						newCornersX[pointsAdded] = (int) Math.round(a[point]+lambda*(a[nextPoint]-a[point]));
						newCornersY[pointsAdded] = (int) Math.round(b[point]+lambda*(b[nextPoint]-b[point]));
						pointsAdded++;
					}
				} //end bottom-up
				else if (vertexIndex[point]>=levelIndex && vertexIndex[nextPoint]<=levelIndex) { // There is a top-down change of level
					if (vertexIndex[point]==levelIndex) { // the point is on the current level
						newCornersX[pointsAdded] = a[point];
						newCornersY[pointsAdded] = b[point];
						pointsAdded++;
					}
					else { // Add the point where the change occurs
						double lambda = (mThresholds[levelIndex+1]-values[point])/(values[nextPoint]-values[point]);
						newCornersX[pointsAdded] = (int) Math.round(a[point]+lambda*(a[nextPoint]-a[point]));
						newCornersY[pointsAdded] = (int) Math.round(b[point]+lambda*(b[nextPoint]-b[point]));
						pointsAdded++;
					}
					if (vertexIndex[nextPoint]<levelIndex) { // This segment contributes with a second point
						double lambda = (mThresholds[levelIndex]-values[point])/(values[nextPoint]-values[point]);
						newCornersX[pointsAdded] = (int) Math.round(a[point]+lambda*(a[nextPoint]-a[point]));
						newCornersY[pointsAdded] = (int) Math.round(b[point]+lambda*(b[nextPoint]-b[point]));
						pointsAdded++;
					}
				}    
			} // end for each point

			if (pointsAdded>0) { // Draw the subpoligon
				g2.setPaint(mColorMapper.indexToColor(levelIndex));
				g2.fillPolygon(newCornersX, newCornersY, pointsAdded);
				if (drawBorder) g2.drawPolygon(newCornersX, newCornersY, pointsAdded);
			}

		} //end for each level

	}

	static final private double ARROW_CST = 0.35;
	static final private double ARROW_MAX = 25.0;

	public void drawColorCodedArrows(Graphics2D g2, Color color, int[] aPos, int bPos[], double[] sizes, double[] modules, double[][] coordinates) {
		int nVertex = aPos.length;

		int arrowA[] = new int[6];
		int arrowB[] = new int[6]; // Used to display the arrow
		double h = Math.sqrt(sizes[0]*sizes[0]+sizes[1]*sizes[1]);
		double h2 = h/2.0;
		double sizeCST = ARROW_CST/h;
		if (h > ARROW_MAX) sizeCST *= ARROW_MAX/h;

		for (int point = 0; point<nVertex; point++) { // for each point
			double[] arrow = coordinates[point];
			if (arrow[0]==0 && arrow[1]==0) {
//		     if (color!=null) g2.setColor(color);
//		     else g2.setColor(valueColor);
//		     g2.drawLine(xOrigin, yOrigin, xOrigin+1, yOrigin);
			  return;
			}
			int valueIndex = mColorMapper.doubleToIndex(modules[point]);
			Color valueColor = mColorMapper.indexToColor(valueIndex);
			
			int xOrigin = aPos[point];
			int yOrigin = bPos[point];
			double angle = Math.atan2(-arrow[1], arrow[0]);
			double a = sizes[0]*Math.cos(angle);
			double b = sizes[1]*Math.sin(angle);

			double xEnd = xOrigin + a;
			double yEnd = yOrigin + b;
			a *= sizeCST;
			b *= sizeCST;
			double p0 = xEnd - a*sizes[0]*2;
			double q0 = yEnd - b*sizes[1]*2;
			a *= h2;
			b *= h2;
			arrowA[0] = (int) p0;
			arrowB[0] = (int) q0;
			arrowA[1] = (int) (p0-b);
			arrowB[1] = (int) (q0+a);
			arrowA[2] = (int) xEnd;
			arrowB[2] = (int) yEnd;
			arrowA[3] = (int) (p0+b);
			arrowB[3] = (int) (q0-a);
			arrowA[4] = (int) p0;
			arrowB[4] = (int) q0;
			arrowA[5] = (int) xOrigin;
			arrowB[5] = (int) yOrigin;
			g2.setPaint(valueColor);
			g2.fillPolygon(arrowA, arrowB, 5);
			if (color!=null) g2.setColor(color);
			else g2.setColor(valueColor);
			g2.drawPolyline(arrowA, arrowB, 6);
		}
	}

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

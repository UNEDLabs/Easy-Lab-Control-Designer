/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.utils;

import java.awt.*;
import org.opensourcephysics.drawing3d.Element;

public class Style {
  
  static final public int DEFAULT_SENSITIVITY = 5;

  static final public int CHANGED_LINE_COLOR = 0;
  static final public int CHANGED_LINE_WIDTH = 1;
  static final public int CHANGED_FILL_COLOR = 2;
  static final public int CHANGED_DRAWING_FILL = 3;
  static final public int CHANGED_DRAWING_LINES = 4;
  static final public int CHANGED_RELATIVE_POSITION = 5;
  static final public int CHANGED_SENSITIVITY = 6;
  static final public int CHANGED_EXTRA_COLOR = 7;
  static final public int CHANGED_DEPTH_FACTOR = 8;
  static final public int CHANGED_RESOLUTION = 9;
  static final public int CHANGED_TEXTURES = 10;
  
  static final public int CENTERED    =  0;
  static final public int NORTH       =  1;
  static final public int SOUTH       =  2;
  static final public int EAST        =  3;
  static final public int WEST        =  4;
  static final public int NORTH_EAST  =  5;
  static final public int NORTH_WEST  =  6;
  static final public int SOUTH_EAST  =  7;
  static final public int SOUTH_WEST  =  8;

  // Configuration variables
  private boolean drawsFill = true, drawsLines = true;
  private Color lineColor = Color.BLACK;
  private float lineWidth = 1.0f;
  private Paint fillColor = Color.BLUE;
  private Color extraColor = Color.black; // an extra color used by some elements
  private int sensitivity = DEFAULT_SENSITIVITY; // The sensitivity to mouse position
  private Resolution resolution = null;
  private double depthFactor = 1.0;
  /**
   * Indicates if the drawable should displace itself from the drawing point.
   * Standard values are provided as static class members. Default is CENTERED.
   */
  private int position=CENTERED;
  private Object texture1 = null;
  private Object texture2 = null;
  private double transpTexture = Double.NaN;
  private boolean combineTexture = false;
  private double ambientFactor = 0.4;//CJB

  // implementation variables
  /**
   * The owner element. This is needed to report to the element any change.
   */
  private Element element = null;
  private Stroke lineStroke = new BasicStroke(lineWidth);

  /**
   * Package-private constructor
   * User must obtain Style objects from elements, by using the getStyle() method
   * @param _element Element
   */
  public Style(Element _element) {
    this.element = _element;
  }
  
  public Element getElement() { return this.element; }
  
  /**
   * Returns a clone of this style
   */
  public Style clone() {
    Style newStyle = new Style(this.element);
    copyTo(newStyle);
    return newStyle;
  }

  /**
   * Copies this style onto another
   * @param target
   */
  public void copyTo(Style target) {
    target.setDrawingFill(drawsFill);
    target.setDrawingLines(drawsLines);
    target.setLineColor(lineColor);
    target.setLineWidth(lineWidth);
    target.setFillColor(fillColor);
    target.setExtraColor(extraColor);
    target.setSensitivity(sensitivity);
    target.setResolution(resolution);
    target.setDepthFactor(depthFactor);
    target.setRelativePosition(position);
    target.setTexture(texture1, texture2, transpTexture, combineTexture);
  }
  
  private void notifyChange(int change) {
    if (element==null) return;
    element.styleChanged(change);
  }

  /**
   * Sets the element. For the use of ElementLoader only.
   * @param _element Element
   */
  void setElement(Element _element) {
    this.element = _element;
  }

  public void setLineColor(Color _color) {
    if (_color==null || _color.equals(this.lineColor)) return; // Ignore null colors
    this.lineColor = _color;
    notifyChange(CHANGED_LINE_COLOR);
  }

  final public Color getLineColor() {
    return this.lineColor;
  }

  public void setLineWidth(float _width) {
    if (this.lineWidth==_width) return;
    this.lineStroke = new BasicStroke(this.lineWidth = _width);
    notifyChange(CHANGED_LINE_WIDTH);
  }

  final public float getLineWidth() {
    return this.lineWidth;
  }

  /**
   * Gets the Stroke derived from the line width
   * @return Stroke
   * @see java.awt.Stroke
   */
  final public Stroke getLineStroke() {
    return this.lineStroke;
  }

  public void setFillColor(Paint _color) {
    if (_color==null || _color.equals(this.fillColor)) return; // Ignore null colors
    this.fillColor = _color;
    notifyChange(CHANGED_FILL_COLOR);
  }

  final public Paint getFillColor() {
    return this.fillColor;
  }

  final public void setExtraColor(Color _color) {
    if (_color==null || _color.equals(this.extraColor)) return; // Ignore null colors
    this.extraColor = _color;
    notifyChange(CHANGED_EXTRA_COLOR);
  }

  final public Color getExtraColor() {
    return this.extraColor;
  }

  public void setResolution(Resolution _resolution) {
    if (this.resolution!=null && this.resolution.equals(_resolution)) return;
    this.resolution = _resolution; // No need to clone. Resolution is unmutable
    notifyChange(CHANGED_RESOLUTION);
  }
  // No danger. Resolution is unmutable
  final public Resolution getResolution() {
    return this.resolution;
  }

  public boolean isDrawingFill() {
    return drawsFill;
  }

  public void setDrawingFill(boolean _drawsFill) {
    if (this.drawsFill==_drawsFill) return;
    this.drawsFill = _drawsFill;
    notifyChange(CHANGED_DRAWING_FILL);
  }

  public boolean isDrawingLines() {
    return drawsLines;
  }

  public void setDrawingLines(boolean _drawsLines) {
    if (this.drawsLines==_drawsLines) return;
    this.drawsLines = _drawsLines;
    notifyChange(CHANGED_DRAWING_LINES);
  }
  
  public void setDepthFactor (double factor) { 
    if (this.depthFactor==factor) return;
    this.depthFactor = factor; 
    notifyChange(CHANGED_DEPTH_FACTOR);
  }
  
  public double getDepthFactor() { return this.depthFactor; }

  public void setTexture(Object texture1, Object texture2, double transparency, boolean combine){
    this.texture1 = texture1;
    this.texture2 = texture2;
	  this.transpTexture =  transparency;
	  this.combineTexture = combine;
	  notifyChange(CHANGED_TEXTURES);
  }
  
  public Object[] getTextures(){
    return new Object[] { texture1, texture2 };
//	  return new String[] {textureFile1,textureFile2};
  }
  
  public double getTransparency(){
	  return transpTexture;
  }
  
  public boolean getCombine(){
	  return combineTexture;
  }
  
  //CJB
  public void setAmbientFactor(double _factor) {
	    if (this.ambientFactor==_factor) return;
	    this.ambientFactor= _factor; 
	    notifyChange(CHANGED_FILL_COLOR);
  }
  
  public double getAmbientFactor(){
	  return ambientFactor;
  }
  //CJB
  
  final public void setRelativePosition (int _position) {
    if (this.position==_position) return;
    this.position = _position; 
    notifyChange(CHANGED_RELATIVE_POSITION);
  }
  
  final public int getRelativePosition () { return this.position; }

  /**
   * Set the accuracy for the mouse to find a point on the screen
   * @param _s sensitive pixels around the hot spot. 0 for the full area
   */
  final public void setSensitivity (int _s) { 
    if (this.sensitivity==_s) return;
    sensitivity = _s; 
    notifyChange(CHANGED_SENSITIVITY);
  }

  final public int getSensitivity () { return sensitivity; }
  
  

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

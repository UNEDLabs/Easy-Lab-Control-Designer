/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;

import org.opensourcephysics.tools.ResourceLoader;

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
  private Color lineColor = Color.black;
  private float lineWidth = 1.0f;
  private Paint fillColor = Color.blue;
  private Color extraColor = Color.black; // an extra color used by some elements
  private int sensitivity = DEFAULT_SENSITIVITY; // The sensitivity to mouse position
  
  /**
   * Indicates if the drawable should displace itself from the drawing point.
   * Standard values are provided as static class members. Default is CENTERED.
   */
  int position=CENTERED;

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
  Style(Element _element) {
    this.element = _element;
  }

  /**
   * Returns a clone of this style
   */
  public Style clone() {
    Style newStyle = new Style(this.element);
    newStyle.drawsFill = this.drawsFill;
    newStyle.drawsLines = this.drawsLines;
    newStyle.fillColor = this.fillColor;
    newStyle.lineColor = this.lineColor;
    newStyle.lineStroke = this.lineStroke;
    newStyle.lineWidth = this.lineWidth;
    newStyle.position = this.position;
    newStyle.sensitivity = this.sensitivity;
    newStyle.extraColor = this.extraColor;
    return newStyle;
  }
  
  /**
   * Returns a clone of this style
   */
  public void copyTo(Element targetElement) {
    Style targetStyle = targetElement.getStyle();
    targetStyle.setDrawingFill(this.drawsFill);
    targetStyle.setDrawingLines(this.drawsLines);
    targetStyle.setFillColor(this.fillColor);
    targetStyle.setLineColor(this.lineColor);
    targetStyle.setLineWidth(this.lineWidth);
    targetStyle.setRelativePosition(this.position);
    targetStyle.setSensitivity(this.sensitivity);
    targetStyle.setExtraColor (this.extraColor);
  }
  
  
  final public void setLineColor(Color _color) {
    if (_color==null) return; // Ignore null colors
    this.lineColor = _color;
    if(element!=null) {
      element.styleChanged(CHANGED_LINE_COLOR);
    }
  }

  final public Color getLineColor() {
    return this.lineColor;
  }

  final public void setLineStroke(Stroke _stroke) {
    if (this.lineStroke==_stroke) {
      return;
    }
    this.lineStroke = _stroke;
    if(element!=null) {
      element.styleChanged(CHANGED_LINE_WIDTH);
    }
  }
  
  final public void setLineWidth(float _width) {
    if(this.lineWidth==_width) {
      return;
    }
    this.lineStroke = new BasicStroke(this.lineWidth = _width);
    if(element!=null) {
      element.styleChanged(CHANGED_LINE_WIDTH);
    }
  }

  final public float getLineWidth() {
    return this.lineWidth;
  }

  /**
   * Gets the Stroke derived from the line width
   * @return Stroke
   * @see java.awt.Stroke
   */
  final Stroke getLineStroke() {
    return this.lineStroke;
  }

  final public void setFillColor(Paint _fill) {
    if (_fill==null) return; // Ignore null colors
    this.fillColor = _fill;
    if(element!=null) {
      element.styleChanged(CHANGED_FILL_COLOR);
    }
  }

  final public Paint getFillColor() {
    return this.fillColor;
  }

  final public void setExtraColor(Color _color) {
    if (_color==null) return; // Ignore null colors
    this.extraColor = _color;
    if(element!=null) {
      element.styleChanged(CHANGED_EXTRA_COLOR);
    }
  }

  final public Color getExtraColor() {
    return this.extraColor;
  }

  final public boolean isDrawingFill() {
    return drawsFill;
  }

  final public void setDrawingFill(boolean _drawsFill) {
    this.drawsFill = _drawsFill;
    if(element!=null) {
      element.styleChanged(CHANGED_DRAWING_FILL);
    }
  }

  final public boolean isDrawingLines() {
    return drawsLines;
  }

  final public void setDrawingLines(boolean _drawsLines) {
    this.drawsLines = _drawsLines;
    if(element!=null) {
      element.styleChanged(CHANGED_DRAWING_LINES);
    }
  }

  //public void setDepthFactor (double factor) { this.depthFactor = factor; }
  
  //public double getDepthFactor() { return this.depthFactor; }

  final public void setRelativePosition (int _position) {
    this.position = _position; 
    element.styleChanged(CHANGED_RELATIVE_POSITION);
  }
  
  final public int getRelativePosition () { return this.position; }

  /**
   * Set the accuracy for the mouse to find a point on the screen
   * @param _s sensitive pixels around the hot spot. 0 for the full area
   */
  final public void setSensitivity (int _s) { sensitivity = _s; }

  final public int getSensitivity () { return sensitivity; }

  /**
   * Paints the element using an image file 
   * @param filename the source image file
   */
  final public void setPaint(String filename) {
    setPaint(filename,0,0);
  }

  /**
   * Paints the element using an image file
   * @param filename the source image file
   * @param width the desired width of the image
   * @param height the desired height of the image
   */
  final public void setPaint(String filename, double width, double height) {
    if (filename==null) return;
    java.awt.image.BufferedImage image = ResourceLoader.getBufferedImage(filename);
    if (image!=null) {
      DrawingPanel panel = element.getPanel();
      if (panel != null) {
        Toolkit.getDefaultToolkit().prepareImage(image,-1,-1,panel.getComponent());
        if (width<=0) width = image.getWidth();
        if (height<=0) height = image.getHeight();
        fillColor = new TexturePaint(image, new java.awt.geom.Rectangle2D.Double(0,0,width,height)); 
      }
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

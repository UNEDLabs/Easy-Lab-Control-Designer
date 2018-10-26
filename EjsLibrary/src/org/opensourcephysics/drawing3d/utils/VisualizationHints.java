/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.utils;

import org.opensourcephysics.drawing3d.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.text.*;

public class VisualizationHints {
  static public final int HINT_DECORATION_TYPE = 0;
  static public final int HINT_REMOVE_HIDDEN_LINES = 1;
  static public final int HINT_ALLOW_QUICK_REDRAW = 2;
  static public final int HINT_USE_COLOR_DEPTH = 3;
  static public final int HINT_CURSOR_TYPE = 4;
  static public final int HINT_SHOW_COORDINATES = 5;
  static public final int HINT_AXES_LABELS = 6;
  static public final int HINT_BACKGROUND_IMAGE = 7;
  static public final int HINT_BACKGROUND_MOVEABLE = 8;
  static public final int HINT_BACKGROUND_SCALE = 9;
  static public final int HINT_COLORS = 10;
  static public final int HINT_FONT = 11;
  static public final int HINT_DEFAULT_ILLUMINATION = 12;
  
  static public final int HINT_ANY = -1;

  static public final int DECORATION_NONE = 0;
  static public final int DECORATION_AXES = 1;
  static public final int DECORATION_CUBE = 2;
  static public final int DECORATION_CENTERED_AXES = 3;
  static public final int CURSOR_NONE = 0;
  static public final int CURSOR_XYZ = 1;
  static public final int CURSOR_CUBE = 2;
  static public final int CURSOR_CROSSHAIR = 3;
  
  // Configuration variables
  private boolean removeHiddenLines = true, allowQuickRedraw = true, useColorDepth = true;
  private int cursorType = CURSOR_XYZ, showCoordinates = DrawingPanel3D.BOTTOM_LEFT;
  private int decorationType = DECORATION_CUBE;
  private String formatX = "x = 0.00;x = -0.00"; //$NON-NLS-1$
  private String formatY = "y = 0.00;y = -0.00"; //$NON-NLS-1$
  private String formatZ = "z = 0.00;z = -0.00"; //$NON-NLS-1$
  private String[] axesLabels = new String[] { "X", "Y", "Z" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  private String backgroundImageFilename = null;
  private Image backgroundImage = null;
  private Dimension backgroundTile = null;
  private boolean backgroundMoveable = true;
  private double scaleBackground = 0.0;
  private Color backgroundColor=new Color(239, 239, 255);
  private Color foregroundColor=Color.BLACK;
  private Font font = new Font("Dialog", Font.PLAIN, 12);
  private boolean ilumination = true;
  

  // Implementation variables
  private NumberFormat theFormatX = new DecimalFormat(formatX);
  private NumberFormat theFormatY = new DecimalFormat(formatY);
  private NumberFormat theFormatZ = new DecimalFormat(formatZ);

  /**
   * The DrawingPanel3D to which it belongs.
   * This is needed to report to it any change that implies a call to update()
   */
  private DrawingPanel3D panel;

  /**
   * Package-private constructor
   * VisualizationHints objects are obtained from DrawingPanel3Ds using the
   * getVisualizationHints() method.
   * @param _panel DrawingPanel3D
   * @see DrawingPanel3D
   */
  public VisualizationHints(DrawingPanel3D _panel) {
    this.panel = _panel;
  }
  
  public void setBackgroundColor (Color _color) {
    if (_color==null || _color.equals(backgroundColor)) return;
    this.backgroundColor = _color;
    panel.hintChanged(HINT_COLORS);
  }

  public Color getBackgroundColor () {
    return this.backgroundColor;
  }

  public void setForegroundColor (Color _color) {
    if (_color==null || _color.equals(foregroundColor)) return;
    this.foregroundColor = _color;
    panel.hintChanged(HINT_COLORS);
  }

  public Color getForegroundColor () {
    return this.foregroundColor;
  }

  public void setFont (Font _font) {
    if (_font==null || _font.equals(this.font)) return;
    this.font = _font;
    panel.hintChanged(HINT_FONT);
  }

  public Font getFont () {
    return this.font;
  }

  public void setCursorType(int _type) {
    this.cursorType = _type;
    panel.hintChanged(HINT_CURSOR_TYPE);
  }

  final public int getCursorType() {
    return this.cursorType;
  }

  public void setDecorationType(int _value) {
    this.decorationType = _value;
    panel.hintChanged(HINT_DECORATION_TYPE);
  }

  final public int getDecorationType() {
    return this.decorationType;
  }

  final public void setAxesLabels(String[] labels) {
    axesLabels = labels;
    panel.hintChanged(HINT_AXES_LABELS);
  }
  final public String[] getAxesLabels() { return axesLabels; }

  public void setRemoveHiddenLines(boolean _value) {
    this.removeHiddenLines = _value;
    panel.hintChanged(HINT_REMOVE_HIDDEN_LINES);
  }

  final public boolean isRemoveHiddenLines() {
    return this.removeHiddenLines;
  }

  public void setAllowQuickRedraw(boolean _value) {
    this.allowQuickRedraw = _value;
    panel.hintChanged(HINT_ALLOW_QUICK_REDRAW);
  }

  final public boolean isAllowQuickRedraw() {
    return this.allowQuickRedraw;
  }

  public void setUseColorDepth(boolean _value) {
    this.useColorDepth = _value;
    panel.hintChanged(HINT_USE_COLOR_DEPTH);
  }

  final public boolean isUseColorDepth() {
    return this.useColorDepth;
  }

  public void setShowCoordinates(int location) {
    showCoordinates = location;
    panel.hintChanged(HINT_SHOW_COORDINATES);
  }

  public int getShowCoordinates() {
    return showCoordinates;
  }

  public void setXFormat(String format) {
    formatX = format;
    if(formatX!=null) {
      theFormatX = new java.text.DecimalFormat(formatX);
    }
  }

  public String getXFormat() {
    return formatX;
  }

  public void setYFormat(String format) {
    formatY = format;
    if(formatY!=null) {
      theFormatY = new java.text.DecimalFormat(formatY);
    }
  }

  public String getYFormat() {
    return formatY;
  }

  public void setZFormat(String format) {
    formatZ = format;
    if(formatZ!=null) {
      theFormatZ = new java.text.DecimalFormat(formatZ);
    }
  }

  public String getZFormat() {
    return formatZ;
  }

  //CJB
  public void setBackgroundImage(String _imageFile) {
	//if(_imageFile==null || backgroundImageFilename.equals(_imageFile)) return;
    backgroundImageFilename = _imageFile;
    backgroundImage = null;
    panel.hintChanged(HINT_BACKGROUND_IMAGE);
  }

  final public String getBackgroundImageFilename() {
    return backgroundImageFilename;
  }

  public void setBackgroundImage(Image _image) {
//    System.out.println ("Setting bkgd image to "+_image);
  //if(_imageFile==null || backgroundImageFilename.equals(_imageFile)) return;
    backgroundImageFilename = null;
    backgroundImage = _image;
    panel.hintChanged(HINT_BACKGROUND_IMAGE);
  }

  final public Image getBackgroundImage() {
    return backgroundImage;
  }

  public void setBackgroundSize(Dimension _tile){
	if(_tile!=null && (backgroundTile.height==_tile.height && backgroundTile.width==_tile.width)) return;
	backgroundTile = _tile;
	panel.hintChanged(HINT_BACKGROUND_IMAGE);
  }
  
  final public Dimension getBackgroundTile(){
	  return backgroundTile;
  }
  
  public void setBackgroundMoveable(boolean _state){
	 if(backgroundMoveable==_state) return;
	 backgroundMoveable = _state;
	 panel.hintChanged(HINT_BACKGROUND_MOVEABLE);  
  }
  
  final public boolean getBackgroundMoveable(){
    if (backgroundImage!=null) return false;
	  return backgroundMoveable;
  }
  
  public void setScaleBackground(double _factor) {
	  if(scaleBackground==_factor) return;
	  scaleBackground = _factor;
	  panel.hintChanged(HINT_BACKGROUND_SCALE);
  }

  final public double getScaleBackground() {
	  return scaleBackground;
  }
  
  public void setDefaultIllumination(boolean _state){
	  if(ilumination==_state) return;
	  ilumination = _state;
	  panel.hintChanged(HINT_DEFAULT_ILLUMINATION);  
  }
  
  final public boolean getDefaultIllumination(){
	  return ilumination;
  }
  //CJB
  
  public void displayPosition(int projectionMode, double[] point) {
    if(showCoordinates<0) {
      return;
    }
    if(point==null) {
      panel.getImplementingPanel().setMessage(null, showCoordinates);
      return;
    }
    String text = ""; //$NON-NLS-1$
    switch(projectionMode) {
    case Camera.MODE_PLANAR_XY :
      if(formatX!=null) {
        text = theFormatX.format(point[0]);
      }
      if(formatY!=null) {
        text += ", "+theFormatY.format(point[1]); //$NON-NLS-1$
      }
      break;
    case Camera.MODE_PLANAR_XZ :
      if(formatX!=null) {
        text = theFormatX.format(point[0]);
      }
      if(formatZ!=null) {
        text += ", "+theFormatZ.format(point[2]); //$NON-NLS-1$
      }
      break;
    case Camera.MODE_PLANAR_YZ :
      if(formatY!=null) {
        text = theFormatY.format(point[1]);
      }
      if(formatZ!=null) {
        text += ", "+theFormatZ.format(point[2]); //$NON-NLS-1$
      }
      break;
    default :
      if(formatX!=null) {
        text = theFormatX.format(point[0]);
      }
      if(formatY!=null) {
        text += ", "+theFormatY.format(point[1]); //$NON-NLS-1$
      }
      if(formatZ!=null) {
        text += ", "+theFormatZ.format(point[2]); //$NON-NLS-1$
      }
      break;
    }
    if(text.startsWith(", ")) { //$NON-NLS-1$
      text = text.substring(2);
    }
    panel.getImplementingPanel().setMessage(text, showCoordinates);
  }

  public void copyFrom (VisualizationHints hints) {
    decorationType = hints.getDecorationType();
    cursorType = hints.getCursorType();
    axesLabels = hints.getAxesLabels();
    this.removeHiddenLines = hints.isRemoveHiddenLines();
    this.allowQuickRedraw = hints.isAllowQuickRedraw();
    this.useColorDepth = hints.isUseColorDepth();
    this.showCoordinates = hints.getShowCoordinates();
    formatX = hints.getXFormat();
    if(formatX!=null) theFormatX = new java.text.DecimalFormat(formatX);
    formatZ = hints.getYFormat();
    if(formatY!=null) theFormatY = new java.text.DecimalFormat(formatY);
    formatZ = hints.getZFormat();
    if(formatZ!=null) theFormatZ = new java.text.DecimalFormat(formatZ);
    this.backgroundImageFilename = hints.getBackgroundImageFilename();
    this.backgroundImage = hints.getBackgroundImage();
    panel.hintChanged(HINT_ANY);
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

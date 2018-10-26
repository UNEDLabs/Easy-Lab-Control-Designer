/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.*;
import org.opensourcephysics.display.TextLine;
import org.opensourcephysics.drawing3d.utils.ImplementingObject;
import org.opensourcephysics.drawing3d.utils.Style;

/**
 * <p>Title: ElementText</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementText extends Element {
  // Configuration variables
  private String text="";
  private Font font;
  private boolean trueSize = false;
  private double rotationAngle=0.0;
  
  // Implementation variables
  private TextLine textLine = new TextLine();

  {
    setSizeXYZ(0.2,0.2,0.2);
    setFont(new Font("dialog",Font.PLAIN,12));
    getStyle().setRelativePosition(Style.CENTERED);
    getStyle().setFillColor(getStyle().getLineColor());
  }
  
  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementText(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
          return new org.opensourcephysics.drawing3d.java3d.Java3dElementText(this);
    }
  }

  // ------------------------------------
  // New configuration methods
  // -------------------------------------

  /**
   * Sets the text to be displayed
   * @param _text the String
   */
  public void setText(String _text) {
    if (text!=null && text.equals(_text)) return;
    textLine.setText(text = org.opensourcephysics.display.TeXParser.parseTeX(_text));
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Gets the text displayed
   */
  public String getText() { return this.text; }

  /**
   * Sets the font for the text
   * @param _font Font
   */
  public void setFont(Font _font) {
    this.font = _font;
    textLine.setFont(_font);
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Gets the font of the text
   * @return Font
   */
  public Font getFont() { return this.font; }

  /**
   * Sets the rotation angle of the text
   * @param _angle double
   */
  public void setRotationAngle(double _angle) {
    this.rotationAngle = _angle;
  }

  /**
   * Gets the rotationangle of the text
   * @return Font
   */
  public double getRotationAngle() { return this.rotationAngle; }

  /**
   * Set whether the size should be that prescribed by the font
   * @param _s boolean
   */
  public void setTrueSize (boolean _s) { 
    trueSize = _s; 
    addChange(Element.CHANGE_PROJECTION);
    addChange(Element.CHANGE_SHAPE);
  }
  
  /**
   * Get whether the size should be that prescribed by the font
   * @return boolean
   */
  public boolean isTrueSize () { return trueSize; }

  
  public void setSizeX(double _sizeX){
	  if(!this.isTrueSize()) _sizeX=0.4;
	  super.setSizeX(_sizeX);
  }
  
  public void setSizeY(double _sizeY){
	  if(!this.isTrueSize()) _sizeY=0.4;
	  super.setSizeY(_sizeY);
  }
  
  public void setSizeZ(double _sizeZ){
	  if(!this.isTrueSize()) _sizeZ=0.4;
	  super.setSizeZ(_sizeZ);
  }

  
  // ------------------------------------
  // Implementation methods 
  // -------------------------------------

  /**
   * Returns the TextLine object of the element
   * @return TextLine
   */
  public TextLine getTextLine() { return textLine; }
  

  // ------------------------------------
  // Super's methods overriden
  // -------------------------------------

  @Override
  public double getDiagonalSize() { 
    if (trueSize) return 0.0;
    return super.getDiagonalSize(); 
  }

  @Override
  protected void getExtrema(double[] min, double[] max) {
    if (trueSize) {
      System.arraycopy(Element.STD_ORIGIN, 0, min, 0, 3);
      System.arraycopy(Element.STD_ORIGIN, 0, max, 0, 3);
      sizeAndToSpaceFrame(min);
      sizeAndToSpaceFrame(max);
    }
    else super.getExtrema(min, max);
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

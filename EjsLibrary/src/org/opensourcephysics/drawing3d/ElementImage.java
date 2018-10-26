/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import org.opensourcephysics.drawing3d.utils.*;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * <p>Title: ElementImage</p>
 * <p>Description: A 2D image displayed in a 3D scene</p>
 * 
 * @author Francisco Esquembre
 * @author Carlos Jara Bravo (CJB)
 * @version August 2009
 */
public class ElementImage extends Element {

  // Configuration variables
  private String imageFile = null;
  private double angle = 0.0;
  private boolean trueSize = false; // Whether size is in pixels

  // Implementation variables
  private Resource resource = null;
  protected BufferedImage image = null;

  {
    setSizeXYZ(0.1, 0.1, 0.1);
    setImageFile ("/org/opensourcephysics/resources/controls/images/window.gif");
  }

  @Override
  protected ImplementingObject createImplementingObject(int _implementation) {
    switch(_implementation) {
      default :
      case DrawingPanel3D.IMPLEMENTATION_SIMPLE3D : 
        return new org.opensourcephysics.drawing3d.simple3d.SimpleElementImage(this);
      case DrawingPanel3D.IMPLEMENTATION_JAVA3D : 
        return new org.opensourcephysics.drawing3d.java3d.Java3dElementImage(this);
    }
  }

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  
  /**
   * Sets the angle to rotate the image (in 2D) before displaying it
   */
  public void setRotationAngle(double angle) {
    if (this.angle==angle) return;
    this.angle = angle;
    addChange(Element.CHANGE_PROJECTION);
  }

  /**
   * Returns the angle to rotate the image (in 2D) before displaying it
   * @return double
   */
  public double getRotationAngle() {
    return this.angle;
  }

  /**
   * Sets the image file to be displayed
   * @param _filename String The filename of the file
   */
  public void setImageFile(String _filename) {
    if (imageFile!=null && imageFile.equals(_filename)) return;
    this.imageFile = _filename;
    if (_filename!=null) {
      resource = ResourceLoader.getResource(_filename);
      if (resource!=null) {
        this.image = resource.getBufferedImage();
        Component comp=null;
        if (getPanel()!=null) comp = getPanel().getComponent();
        Toolkit.getDefaultToolkit().prepareImage(image,-1,-1,comp);
        addChange(Element.CHANGE_SHAPE);
      }
    }
  }

  /**
   * Gets the image filename displayed
   * @return String
   */
  public String getImageFile() { return this.imageFile; }

  /**
   * Sets the image to be displayed
   * @param _image java.awt.Image
   */
  public void setImage(BufferedImage _image) {
    this.imageFile = null;
    this.image = _image;
    addChange(Element.CHANGE_SHAPE);
  }

  /**
   * Returns the Image object
   * @return BufferedImage
   */
  public BufferedImage getImage() { return this.image; }
  
  /**
   * Set whether the size should be that of the original image file
   * @param _s boolean
   */
  public void setTrueSize (boolean _s) {
    if (this.trueSize==_s) return;
    this.trueSize = _s;
    addChange(Element.CHANGE_SIZE);
  }

  /**
   * Get whether the size should be that of the original image file
   * @return boolean
   */
  public boolean isTrueSize () { return this.trueSize; }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  // -------------------------------------
  // Interaction
  // -------------------------------------
  
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

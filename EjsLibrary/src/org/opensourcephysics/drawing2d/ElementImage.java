/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing2d;

import java.awt.*;
import java.awt.geom.AffineTransform;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * <p>Title: ElementImage</p>
 * <p>Description: An image.
 * @author Francisco Esquembre
 * @version June 2008
 */
public class ElementImage extends Element { 
  // Configuration variables
  private String imageFile = null;
  private boolean trueSize = false;

  // Implementation variables
  private Resource resource = null;
  private Image image = null;
  private double[] coordinates = new double[2];
  private double[] pixel = new double[2];     // The output of position projections
  private double[] pixelSize = new double[2]; // The output of size projections
  private AffineTransform imageTransform = new AffineTransform();
  private AffineTransform trueSizeTransform=new AffineTransform(); // additional transformation for trueSize
  private Shape shape; // The shape used to detect sensitivity=0 clicks
  private double tempSizeX, tempSizeY;
  
  {
    setSize (new double[]{0.1,0.1});
    setImageFile ("/org/opensourcephysics/resources/controls/images/window.gif");
  }

  // -------------------------------------
  // New configuration methods
  // -------------------------------------
  
  private void setTheImage(Image _image, int _relPos) {
    this.image = _image;
    if (image==null) return;
    int width = Math.max(image.getWidth(null),1);
    int height = Math.max(image.getHeight(null), 1);
    int a1,b1;
    switch (_relPos) {
      default :
      case Style.CENTERED   : a1 = -width/2; b1 = -height/2; break;
      case Style.NORTH      : a1 = -width/2; b1 = 0;         break;
      case Style.SOUTH      : a1 = -width/2; b1 = -height;   break;
      case Style.EAST       : a1 = -width;   b1 = -height/2; break;
      case Style.WEST       : a1 =  0;       b1 = -height/2; break;
      case Style.NORTH_EAST : a1 = -width;   b1 = 0;         break;
      case Style.NORTH_WEST : a1 =  0;       b1 = 0;         break;
      case Style.SOUTH_EAST : a1 = -width;   b1 = -height;   break;
      case Style.SOUTH_WEST : a1 =  0;       b1 = -height;   break;
    }
    imageTransform = AffineTransform.getScaleInstance(1.0/width, -1.0/height);
    imageTransform.concatenate(AffineTransform.getTranslateInstance(a1, b1));
    double x1,y1;
    switch (_relPos) {
      default :
      case Style.CENTERED   : x1 = -0.5; y1 = -0.5; break;
      case Style.NORTH      : x1 = -0.5; y1 = -1.0; break;
      case Style.SOUTH      : x1 = -0.5; y1 =  0.0; break;
      case Style.EAST       : x1 = -1.0; y1 = -0.5; break;
      case Style.WEST       : x1 =  0.0; y1 = -0.5; break;
      case Style.NORTH_EAST : x1 = -1.0; y1 = -1.0; break;
      case Style.NORTH_WEST : x1 =  0.0; y1 = -1.0; break;
      case Style.SOUTH_EAST : x1 = -1.0; y1 =  0.0; break;
      case Style.SOUTH_WEST : x1 =  0.0; y1 =  0.0; break;
    }
    shape = new java.awt.geom.Rectangle2D.Double(x1,y1,1.0,1.0);
    setNeedToProject(true);
  }

  /**
   * Sets the image file to be displayed
   * @param text the String
   */
  public void setImageFile(String file) {
    if (imageFile!=null && imageFile.equals(file)) return;
    this.imageFile = file;
    if (file!=null) {
      resource = ResourceLoader.getResource(file);
      if (resource!=null) {
        this.image = resource.getImage();
        Component comp=null;
        if (getPanel()!=null) comp = getPanel().getComponent();
        Toolkit.getDefaultToolkit().prepareImage(image,-1,-1,comp);
        setTheImage(resource.getImage(),getStyle().getRelativePosition());
      }
    }
  }

  /**
   * Gets the image displayed
   */
  public String getImageFile() {
    return this.imageFile;
  }

  /**
   * Sets the image to be displayed
   * @param image java.awt.Image
   */
  public void setImage(java.awt.Image _image) {
    this.imageFile = null;
    if (_image!=this.image) {
      this.image = _image;
      Component comp=null;
      if (getPanel()!=null) comp = getPanel().getComponent();
      Toolkit.getDefaultToolkit().prepareImage(image,-1,-1,comp);
      setTheImage(_image,getStyle().getRelativePosition());
      if (trueSize) setTrueSize(true);
    }
  }

  /**
   * Set whether the size should be that of the original image file
   * @param _s boolean
   */
  public void setTrueSize (boolean _s) { 
    this.trueSize = _s;
    if (trueSize) { 
      tempSizeX = getSizeX();
      tempSizeY = getSizeY();
      if (image!=null) super.setSizeXY(image.getWidth(null),image.getHeight(null));
    }
    else super.setSizeXY(tempSizeX,tempSizeY);
    setNeedToProject(true);
  }

  /**
   * Get whether the size should be that of the original image file
   * @return boolean
   */
  public boolean isTrueSize () { 
    return this.trueSize; 
  }

  public void setSizeX(double _sizeX) {
    if (trueSize) { tempSizeX = _sizeX; setElementChanged(); }
    else super.setSizeX(_sizeX);
  }

  public void setSizeY(double _sizeY) {
    if (trueSize) { tempSizeY = _sizeY; setElementChanged(); }
    else super.setSizeY(_sizeY);
  }

  public void setSizeXY(double _sizeX, double _sizeY) {
    if (trueSize) { tempSizeX = _sizeX; tempSizeY = _sizeY; setElementChanged(); }
    else super.setSizeXY(_sizeX,_sizeY);
  }

  public void setSize(double[] _size) {
    if (trueSize) { tempSizeX = _size[0]; tempSizeY = _size[0]; setElementChanged(); }
    else super.setSize(_size);
  }

  protected void styleChanged(int styleThatChanged) {
    if (styleThatChanged==Style.CHANGED_RELATIVE_POSITION) {
      setTheImage(this.image,getStyle().getRelativePosition());
    }
  }
  
  // -------------------------------------
  // Parent methods overwritten
  // -------------------------------------

  @Override
  protected int getCorners(double[] _corners) {
    if (trueSize) {
      _corners[0] = _corners[1] = 0;
      return 1;
    }
    switch (getStyle().getRelativePosition()) {
      default :
      case Style.CENTERED   : _corners[0] = -0.5; _corners[1] = -0.5; break;
      case Style.NORTH      : _corners[0] = -0.5; _corners[1] = -1.0; break;
      case Style.SOUTH      : _corners[0] = -0.5; _corners[1] =  0.0; break;
      case Style.EAST       : _corners[0] = -1.0; _corners[1] = -0.5; break;
      case Style.WEST       : _corners[0] =  0.0; _corners[1] = -0.5; break;
      case Style.NORTH_EAST : _corners[0] = -1.0; _corners[1] = -1.0; break;
      case Style.NORTH_WEST : _corners[0] =  0.0; _corners[1] = -1.0; break;
      case Style.SOUTH_EAST : _corners[0] = -1.0; _corners[1] =  0.0; break;
      case Style.SOUTH_WEST : _corners[0] =  0.0; _corners[1] =  0.0; break;
    }
    _corners[4] = _corners[2] = _corners[0] + 1.0;
    _corners[6] = _corners[0];
    _corners[3] = _corners[1];
    _corners[5] = _corners[7] = _corners[1] + 1.0;
    return 4;
  }

  public void draw (org.opensourcephysics.display.DrawingPanel _panel, Graphics _g) {
    if (image==null || !isReallyVisible()) return;
    //lastPanel = _panel;
    Graphics2D g2 = (Graphics2D) _g;
    AffineTransform tr;
    if (trueSize) {
      if (hasChanged() || needsToProject()) projectPoints();
      tr = new AffineTransform (trueSizeTransform);
      tr.preConcatenate(getPixelTransform(_panel));
    }
    else tr = getPixelTransform(_panel);
    tr.concatenate(imageTransform);
    g2.drawImage(image,tr,_panel); // Setting the panel as observer is important!
  }

  // ------------------------------------- 
  // Interaction
  // -------------------------------------

  public org.opensourcephysics.display.Interactive findInteractive(org.opensourcephysics.display.DrawingPanel _panel, int _xpix, int _ypix) {
    if (!targetPosition.isEnabled()) return null;
    if (image==null || !isReallyVisible()) return null;
    if (hasChanged() || needsToProject()) projectPoints();
    int sensitivity = getStyle().getSensitivity();
    if (sensitivity<=0) {
      AffineTransform tr;
      if (trueSize) {
        tr = new AffineTransform (trueSizeTransform);
        tr.preConcatenate(getPixelTransform(_panel));
      }
      else tr = getPixelTransform(_panel);
      Shape trShape = tr.createTransformedShape(shape);
      if (trShape.contains(_xpix,_ypix)) return this.targetPosition;
    }
    else {
      if (Math.abs(pixel[0]-_xpix)<sensitivity && Math.abs(pixel[1]-_ypix)<sensitivity) return this.targetPosition;
    }
    return null;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  private void projectPoints() {
    if (image==null) return;
    coordinates[0] = 0.0; 
    coordinates[1] = 0.0;
    getTotalTransform().transform(coordinates,0,coordinates,0,1);
    getPanel().projectPosition(coordinates, pixel);
    if (trueSize) {
      int width = image.getWidth(null);
      int height = image.getHeight(null);
      getPanel().projectSize(coordinates, getSize(), pixelSize);
      trueSizeTransform = AffineTransform.getScaleInstance(pixelSize[0]==0?0:width/pixelSize[0], pixelSize[1]==0?0:height/pixelSize[1]);
    }
    setNeedToProject(false);
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

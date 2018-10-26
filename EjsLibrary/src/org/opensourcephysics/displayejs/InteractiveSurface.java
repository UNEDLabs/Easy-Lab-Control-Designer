/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

/**
 * This class implements a general 3D surface.
 * The surface can be repositioned but cannot be resized.
 */

public class InteractiveSurface extends AbstractInteractiveTile {
  // Configuration variables
  protected double[][][] data;

  // Implementation variables
  protected int nu = -1, nv = -1; // Make sure arrays are allocated
  protected double center[] = {0.0,0.0,0.0}, size[] = {1.0,1.0,1.0};

  /**
   * Default constructor
   */
  public InteractiveSurface () {
    setXYZ(0.0,0.0,0.0);
    setSizeXYZ(1.0,1.0,1.0);
  }


  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveSurface) setData (((InteractiveSurface) _element).data);
  }

// -------------------------------------
// New configuration methods
// -------------------------------------
  /**
   * Sets the data of the surface.
   * @param dataArray the double[nu][nv][3] array of coordinates for the surface.
   */
  public void setData (double[][][] dataArray) { this.data = dataArray; hasChanged = true; }

// ----------------------------------------------
// Implementation of Interactive and Drawable3D
// ----------------------------------------------

  public org.opensourcephysics.display.Interactive findInteractive (DrawingPanel _panel, int _xpix, int _ypix) {
    if (!visible) return null;
    if (hasChanged) { computeCorners(); projectPoints(_panel); }
    else if (_panel!=panelWithValidProjection) projectPoints(_panel);
//    if (sizeEnabled     && Math.abs(pixelEndpoint[0]-_xpix)<SENSIBILITY && Math.abs(pixelEndpoint[1]-_ypix)<SENSIBILITY) return sizeTarget;
    if (positionEnabled && Math.abs(pixelOrigin[0]  -_xpix)<SENSIBILITY && Math.abs(pixelOrigin[1]  -_ypix)<SENSIBILITY) return new InteractionTargetElementPosition(this);
    return null;
   }

// -------------------------------------
//  Private or protected methods
// -------------------------------------

  protected void projectPoints (DrawingPanel _panel) {
    super.projectPoints(_panel);
    if (corners==null) return;
    if (group==null) _panel.project(corners[0][0],pixelOrigin);
    else {
      coordinates[0] = group.x + corners[0][0][0]*group.sizex;
      coordinates[1] = group.y + corners[0][0][1]*group.sizey;
      coordinates[2] = group.z + corners[0][0][2]*group.sizez;
      _panel.project(coordinates,pixelOrigin);
    }
  }

  protected synchronized void computeCorners () {
//    System.out.println("Computing surface");
    if (data==null) return;
    int theNu = data.length-1, theNv = data[0].length-1;
    if (nu==theNu && nv==theNv); // No need to reallocate arrays
    else {
      nu = theNu; nv = theNv;
      setCorners(new double [nu*nv][4][3]); // Reallocate arrays
    }
    int tile = 0;
    center[0] = x;   center[1] = y;   center[2] = z;
    size[0] = sizex; size[1] = sizey; size[2] = sizez;
    {
      for (int v=0; v<nv; v++) {
        for (int u=0; u<nu; u++, tile++) {
          for (int k=0; k<3; k++) {
            corners[tile][0][k] = center[k] + data[u  ][v  ][k]*size[k];
            corners[tile][1][k] = center[k] + data[u+1][v  ][k]*size[k];
            corners[tile][2][k] = center[k] + data[u+1][v+1][k]*size[k];
            corners[tile][3][k] = center[k] + data[u  ][v+1][k]*size[k];
          }
        }
      }
    }
    transformCorners();
    xmin = xmax = ymin = ymax = zmin = zmax = Double.NaN; // To signal out that extrema may be out of date
    hasChanged = false;
  }


}

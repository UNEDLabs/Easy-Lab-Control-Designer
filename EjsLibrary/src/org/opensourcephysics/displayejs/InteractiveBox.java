/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;

public class InteractiveBox extends AbstractInteractiveTile {
  // Configuration variables
  protected boolean closedBottom = true, closedTop = true;

  // Implementation variables
  protected boolean changeNTiles=true;
  protected int nx = -1, ny = -1, nz = -1; // Make sure arrays are allocated

  /**
   * Constructor
   */
//  public InteractiveBox () { setOrigin (0.5,0.5,0.5,true); }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveBox) {
      InteractiveBox old = (InteractiveBox) _element;
      setClosedBottom(old.isClosedBottom());
      setClosedTop(old.isClosedTop());
    }
  }


// -------------------------------------
// Configuration
// -------------------------------------

  /**
   * Whether the element should be closed at its bottom.
   * @param closed the desired value
   */
  public void    setClosedBottom (boolean close) { this.closedBottom = close; hasChanged = true; changeNTiles = true; }
  /**
   * Whether the element is closed at its bottom.
   * @return the value
   */
  public boolean isClosedBottom () { return this.closedBottom; }

  /**
   * Whether the element should be closed at its top.
   * @param closed the desired value
   */
  public void    setClosedTop (boolean close) { this.closedTop = close; hasChanged = true; changeNTiles = true; }
  /**
   * Whether the element is closed at its top.
   * @return the value
   */
  public boolean isClosedTop () { return this.closedTop; }

// -------------------------------------
//  Private or protected methods
// -------------------------------------

  protected synchronized void computeCorners () {
//    System.out.println("Computing box");
    int theNx = 1, theNy = 1, theNz = 1;
    if (resolution!=null) {
      switch (resolution.type) {
        case Resolution.DIVISIONS :
          theNx = Math.max(resolution.n1,1);
          theNy = Math.max(resolution.n2,1);
          theNz = Math.max(resolution.n3,1);
          break;
        case Resolution.MAX_LENGTH :
          theNx = Math.max((int) Math.round(0.49 + Math.abs(sizex)/resolution.maxLength), 1);
          theNy = Math.max((int) Math.round(0.49 + Math.abs(sizey)/resolution.maxLength), 1);
          theNz = Math.max((int) Math.round(0.49 + Math.abs(sizez)/resolution.maxLength), 1);
        break;
      }
    }
    if (nx==theNx && ny==theNy && nz==theNz && changeNTiles==false); // No need to reallocate arrays
    else {
      nx = theNx; ny = theNy; nz = theNz;
      int nTotal = 2*nx*nz + 2*ny*nz;
      if (closedBottom) nTotal += nx*ny;
      if (closedTop) nTotal += nx*ny;
      changeNTiles = false;
      setCorners(new double [nTotal][4][3]); } // Reallocate arrays

    int tile = 0;
    double dx = sizex/nx,  dy = sizey/ny,  dz = sizez/nz;
    double ex = x + sizex, ey = y + sizey, ez = z + sizez;
    for (int i=0; i<nx; i++) { // x-y sides
      double theX = x + i*dx;
      for (int j=0; j<ny; j++) {
        double theY = y + j*dy;
        if (closedBottom) {
          corners[tile][0][0] = theX;    corners[tile][0][1] = theY;    corners[tile][0][2] = z;
          corners[tile][1][0] = theX+dx; corners[tile][1][1] = theY;    corners[tile][1][2] = z;
          corners[tile][2][0] = theX+dx; corners[tile][2][1] = theY+dy; corners[tile][2][2] = z;
          corners[tile][3][0] = theX;    corners[tile][3][1] = theY+dy; corners[tile][3][2] = z;
          tile++;
        }
        if (closedTop) { // The upper side
          corners[tile][0][0] = theX;    corners[tile][0][1] = theY;    corners[tile][0][2] = ez;
          corners[tile][1][0] = theX+dx; corners[tile][1][1] = theY;    corners[tile][1][2] = ez;
          corners[tile][2][0] = theX+dx; corners[tile][2][1] = theY+dy; corners[tile][2][2] = ez;
          corners[tile][3][0] = theX;    corners[tile][3][1] = theY+dy; corners[tile][3][2] = ez;
          tile++;
        }
      }
    }
    for (int i=0; i<nx; i++) { // x-z sides
      double theX = x + i*dx;
      for (int k=0; k<nz; k++) {
        double theZ = z + k*dz;
        corners[tile][0][0] = theX;    corners[tile][0][2] = theZ;    corners[tile][0][1] = y;
        corners[tile][1][0] = theX+dx; corners[tile][1][2] = theZ;    corners[tile][1][1] = y;
        corners[tile][2][0] = theX+dx; corners[tile][2][2] = theZ+dz; corners[tile][2][1] = y;
        corners[tile][3][0] = theX;    corners[tile][3][2] = theZ+dz; corners[tile][3][1] = y;
        tile++; // The upper side
        corners[tile][0][0] = theX;    corners[tile][0][2] = theZ;    corners[tile][0][1] = ey;
        corners[tile][1][0] = theX+dx; corners[tile][1][2] = theZ;    corners[tile][1][1] = ey;
        corners[tile][2][0] = theX+dx; corners[tile][2][2] = theZ+dz; corners[tile][2][1] = ey;
        corners[tile][3][0] = theX;    corners[tile][3][2] = theZ+dz; corners[tile][3][1] = ey;
        tile++;
      }
    }
    for (int k=0; k<nz; k++) { // y-z sides
      double theZ = z + k*dz;
      for (int j=0; j<ny; j++) {
        double theY = y + j*dy;
        corners[tile][0][2] = theZ;    corners[tile][0][1] = theY;    corners[tile][0][0] = x;
        corners[tile][1][2] = theZ+dz; corners[tile][1][1] = theY;    corners[tile][1][0] = x;
        corners[tile][2][2] = theZ+dz; corners[tile][2][1] = theY+dy; corners[tile][2][0] = x;
        corners[tile][3][2] = theZ;    corners[tile][3][1] = theY+dy; corners[tile][3][0] = x;
        tile++; // The upper side
        corners[tile][0][2] = theZ;    corners[tile][0][1] = theY;    corners[tile][0][0] = ex;
        corners[tile][1][2] = theZ+dz; corners[tile][1][1] = theY;    corners[tile][1][0] = ex;
        corners[tile][2][2] = theZ+dz; corners[tile][2][1] = theY+dy; corners[tile][2][0] = ex;
        corners[tile][3][2] = theZ;    corners[tile][3][1] = theY+dy; corners[tile][3][0] = ex;
        tile++;
      }
    }
    transformCorners();
    xmin = xmax = ymin = ymax = zmin = zmax = Double.NaN; // To signal out that extrema may be out of date
    hasChanged = false;
  }

}

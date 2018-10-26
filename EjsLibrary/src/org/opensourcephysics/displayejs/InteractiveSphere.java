/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;

/**
 * This class implements a general 3D sphere. The user can specify the three main axes
 * for the sphere resultig, for instance in non-straight sphere.
 * The sphere can be repositioned but cannot be resized.
 */

public class InteractiveSphere extends InteractiveCylinder {
  // Configuration variables
  protected int minanglev = -90, maxanglev = 90;

  // Implementation variables
  protected double cosv[] = null, sinv[] = null;
  protected double nextCenter[] = {0.0,0.0,0.0};

  public InteractiveSphere () { this(Z_AXIS); }

  public InteractiveSphere (int _direction) {
    super(_direction);
    setSizeXYZ(0.2,0.2,0.2);
    setResolution (new Resolution(3,12,12));
//    setOrigin (0.5,0.5,0.5,true);
  }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveCylinder) {
      InteractiveSphere old = (InteractiveSphere) _element;
      setMinAngleV( old.getMinAngleV());
      setMaxAngleV(old.getMaxAngleV());
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  /**
   * Sets the minimum angle to build the meridians of the sphere.
   * @param angle the minimum angle
   */
  public void   setMinAngleV(int angle)  { this.minanglev = angle; hasChanged = true; changeNTiles = true; }
  /**
   * Gets the minimum angle to build the meridians of the sphere.
   * @return the minimum angle
   */
  public int    getMinAngleV() { return this.minanglev; }

  /**
   * Sets the maximum angle to build the meridians of the sphere.
   * @param angle the maximum angle
   */
  public void   setMaxAngleV(int angle)  { this.maxanglev = angle; hasChanged = true; changeNTiles = true; }
  /**
   * Gets the maximum angle to build the meridians of the sphere.
   * @return the maximum angle
   */
  public int    getMaxAngleV() { return this.maxanglev; }

// -------------------------------------
//  Private or protected methods
// -------------------------------------

  protected void computeAbsoluteDifference (double[] result) {
    double dx = (originx-0.5)*sizex, dy = (originy-0.5)*sizey, dz = (originz-0.5)*sizez;
    result[0] = dx*vectorx[0] + dy*vectory[0] + dz*vectorz[0];
    result[1] = dx*vectorx[1] + dy*vectory[1] + dz*vectorz[1];
    result[2] = dx*vectorx[2] + dy*vectory[2] + dz*vectorz[2];
  }

  protected synchronized void computeCorners () {
//    System.out.println("Computing sphere");
    double dx = sizex/2, dy = sizey/2, dz = sizez/2;
    double angleu1 = minangleu, angleu2 = maxangleu;
    if (Math.abs(angleu2-angleu1)>360) angleu2 = angleu1+360;
    double anglev1 = minanglev, anglev2 = maxanglev;
    if (Math.abs(anglev2-anglev1)>180) anglev2 = anglev1+180;
    int theNr = 1, theNu = 1, theNz = 1;
    if (resolution!=null) {
      switch (resolution.type) {
        case Resolution.DIVISIONS :
          theNr = Math.max(resolution.n1,1);
          theNu = Math.max(resolution.n2,1);
          theNz = Math.max(resolution.n3,1);
          break;
        case Resolution.MAX_LENGTH :
          theNr = Math.max((int) Math.round(0.49 + Math.max(Math.abs(dx),Math.abs(dy))/resolution.maxLength), 1);
          theNu = Math.max((int) Math.round(0.49 + Math.abs(angleu2-angleu1)*TO_RADIANS*(Math.abs(dx)+Math.abs(dy))/resolution.maxLength), 1);
          theNz = Math.max((int) Math.round(0.49 + Math.abs(TO_RADIANS*(anglev2-anglev1)*dz)/resolution.maxLength), 1);
        break;
      }
    }
    if (nr==theNr && nu==theNu && nz==theNz && changeNTiles==false); // No need to reallocate arrays
    else {
      nr = theNr; nu = theNu; nz = theNz;
      cosu = new double[nu+1]; sinu = new double[nu+1];
      cosv = new double[nz+1]; sinv = new double[nz+1];
      int totalN = nu*nz;
      if (Math.abs(anglev2-anglev1)<180) {
        if (closedBottom) totalN += nr*nu;
        if (closedTop)    totalN += nr*nu;
      }
      if (Math.abs(angleu2-angleu1)<360) {
        if (closedLeft)   totalN += nr*nz;
        if (closedRight)  totalN += nr*nz;
      }
      changeNTiles = false;
      setCorners(new double [totalN][4][3]); // Reallocate arrays
    }
    // Compute sines and cosines
    for (int u=0; u<=nu; u++) { // compute sines and cosines
      double angle = ((nu-u)*angleu1 + u*angleu2)*TO_RADIANS/nu;
      cosu[u] = Math.cos(angle)*dx;
      sinu[u] = Math.sin(angle)*dy;
    }
    for (int v=0; v<=nz; v++) { // compute sines and cosines
      double angle = ((nz-v)*anglev1 + v*anglev2)*TO_RADIANS/nz;
      cosv[v] = Math.cos(angle);
      sinv[v] = Math.sin(angle)*dz;
    }
    // Build the tiles
    int tile = 0;
    center[0] = x; center[1] = y; center[2] = z;
    { // Tiles along the z axis
      for (int v=0; v<nz; v++) {
        for (int u=0; u<nu; u++, tile++) { // This ordering is important for the computations below (see ref)
          for (int k=0; k<3; k++) {
            corners[tile][0][k] = center[k] + (cosu[u  ]*vectorx[k] + sinu[u  ]*vectory[k])*cosv[v]   + sinv[v]  *vectorz[k];
            corners[tile][1][k] = center[k] + (cosu[u+1]*vectorx[k] + sinu[u+1]*vectory[k])*cosv[v]   + sinv[v]  *vectorz[k];
            corners[tile][2][k] = center[k] + (cosu[u+1]*vectorx[k] + sinu[u+1]*vectory[k])*cosv[v+1] + sinv[v+1]*vectorz[k];
            corners[tile][3][k] = center[k] + (cosu[u  ]*vectorx[k] + sinu[u  ]*vectory[k])*cosv[v+1] + sinv[v+1]*vectorz[k];
          }
        }
      }
    }
    if (Math.abs(anglev2-anglev1)<180) { // No need to close top or bottom is the sphere is 'round' enough
      if (closedBottom) { // Tiles at bottom
        center[0] = x + sinv[0]*vectorz[0]; center[1] = y + sinv[0]*vectorz[1]; center[2] = z + sinv[0]*vectorz[2];
//        int ref=0; // not used
        for (int u=0; u<nu; u++) {
          for (int i=0; i<nr; i++, tile++) {
            for (int k=0; k<3; k++) {
              corners[tile][0][k] = ((nr-i)  *center[k] +  i   *corners[u][0][k])/nr; // should be ref+u
              corners[tile][1][k] = ((nr-i-1)*center[k] + (i+1)*corners[u][0][k])/nr; // should be ref+u
              corners[tile][2][k] = ((nr-i-1)*center[k] + (i+1)*corners[u][1][k])/nr; // should be ref+u
              corners[tile][3][k] = ((nr-i)  *center[k] +  i   *corners[u][1][k])/nr; // should be ref+u
            }
          }
        }
      }
      if (closedTop) { // Tiles at top
        int ref = nu*(nz-1);
        center[0] = x + sinv[nz]*vectorz[0]; center[1] = y + sinv[nz]*vectorz[1]; center[2] = z + sinv[nz]*vectorz[2];
        for (int u=0; u<nu; u++) {
          for (int i=0; i<nr; i++, tile++) {
            for (int k=0; k<3; k++) {
              corners[tile][0][k] = ((nr-i)  *center[k] +  i   *corners[ref+u][3][k])/nr;
              corners[tile][1][k] = ((nr-i-1)*center[k] + (i+1)*corners[ref+u][3][k])/nr;
              corners[tile][2][k] = ((nr-i-1)*center[k] + (i+1)*corners[ref+u][2][k])/nr;
              corners[tile][3][k] = ((nr-i)  *center[k] +  i   *corners[ref+u][2][k])/nr;
            }
          }
        }
      }
    }
    if (Math.abs(angleu2-angleu1)<360){ // No need to close left or right if the sphere is 'round' enough
//      System.out.println ("Computing lateral tiles");
      if (closedRight) { // Tiles at right
        int ref = 0;
        for (int j=0; j<nz; j++, ref+=nu) {
          center[0]     = x + sinv[j]  *vectorz[0]; center[1]     = y + sinv[j]  *vectorz[1]; center[2]     = z + sinv[j]  *vectorz[2];
          nextCenter[0] = x + sinv[j+1]*vectorz[0]; nextCenter[1] = y + sinv[j+1]*vectorz[1]; nextCenter[2] = z + sinv[j+1]*vectorz[2];
          for (int i=0; i<nr; i++, tile++) {
            for (int k=0; k<3; k++) {
              corners[tile][0][k] = ((nr-i)  *center[k]     +  i   *corners[ref][0][k])/nr;
              corners[tile][1][k] = ((nr-i-1)*center[k]     + (i+1)*corners[ref][0][k])/nr;
              corners[tile][2][k] = ((nr-i-1)*nextCenter[k] + (i+1)*corners[ref][3][k])/nr;
              corners[tile][3][k] = ((nr-i)  *nextCenter[k] +  i   *corners[ref][3][k])/nr;
            }
          }
        }
      }
      if (closedLeft) { // Tiles at left
        int ref = nu-1;
        for (int j=0; j<nz; j++, ref+=nu) {
          center[0]     = x + sinv[j]  *vectorz[0]; center[1]     = y + sinv[j]  *vectorz[1]; center[2]     = z + sinv[j]  *vectorz[2];
          nextCenter[0] = x + sinv[j+1]*vectorz[0]; nextCenter[1] = y + sinv[j+1]*vectorz[1]; nextCenter[2] = z + sinv[j+1]*vectorz[2];
          for (int i=0; i<nr; i++, tile++) {
            for (int k=0; k<3; k++) {
              corners[tile][0][k] = ((nr-i)  *center[k]     +  i   *corners[ref][1][k])/nr;
              corners[tile][1][k] = ((nr-i-1)*center[k]     + (i+1)*corners[ref][1][k])/nr;
              corners[tile][2][k] = ((nr-i-1)*nextCenter[k] + (i+1)*corners[ref][2][k])/nr;
              corners[tile][3][k] = ((nr-i)  *nextCenter[k] +  i   *corners[ref][2][k])/nr;
            }
          }
        }
      }
    }
    transformCorners();
    xmin = xmax = ymin = ymax = zmin = zmax = Double.NaN; // To signal out that extrema may be out of date
    hasChanged = false;
  }

}

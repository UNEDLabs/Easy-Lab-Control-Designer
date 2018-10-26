/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;

/**
 * This class implements a general 3D cone. The user can specify the three main axes
 * for the cone resultig, for instance in non-straight cone.
 * The cone can be repositioned but cannot be resized.
 */

public class InteractiveCone extends InteractiveCylinder {
  // Implementation variables
  protected double nextCenter[] = {0.0,0.0,0.0};

  public InteractiveCone () { this(Z_AXIS); }

  public InteractiveCone (int _direction) {
    setSizeXYZ(0.2,0.2,0.5);
    setResolution (new Resolution(3,12,5));
    setDirection(_direction);
  }


// -------------------------------------
//  Private or protected methods
// -------------------------------------

  protected synchronized void computeCorners () {
//    System.out.println("Computing cone");
    double dx = sizex/2, dy = sizey/2, dz = sizez;
    int theNr = 1, theNu = 1, theNz = 1;
    double angle1 = minangleu, angle2 = maxangleu;
    if (Math.abs(angle2-angle1)>360) angle2 = angle1+360;
    if (resolution!=null) {
      switch (resolution.type) {
        case Resolution.DIVISIONS :
          theNr = Math.max(resolution.n1,1);
          theNu = Math.max(resolution.n2,1);
          theNz = Math.max(resolution.n3,1);
          break;
        case Resolution.MAX_LENGTH :
          theNr = Math.max((int) Math.round(0.49 + Math.max(Math.abs(dx),Math.abs(dy))/resolution.maxLength), 1);
          theNu = Math.max((int) Math.round(0.49 + Math.abs(angle2-angle1)*TO_RADIANS*(Math.abs(dx)+Math.abs(dy))/resolution.maxLength), 1);
          theNz = Math.max((int) Math.round(0.49 + Math.abs(dz)/resolution.maxLength), 1);
        break;
      }
    }
    if (nr==theNr && nu==theNu && nz==theNz && changeNTiles==false); // No need to reallocate arrays
    else {
      nr = theNr; nu = theNu; nz = theNz;
      cosu = new double[nu+1]; sinu = new double[nu+1];
      int totalN = nu*nz;
      if (closedBottom) totalN += nr*nu; // No closedTop
      if (Math.abs(angle2-angle1)<360) {
        if (closedLeft)   totalN += nr*nz;
        if (closedRight)  totalN += nr*nz;
      }
      changeNTiles = false;
      setCorners(new double [totalN][4][3]); // Reallocate arrays
    }
    // Compute sines and cosines
    for (int u=0; u<=nu; u++) { // compute sines and cosines
      double angle = ((nu-u)*angle1 + u*angle2)*TO_RADIANS/nu;
      cosu[u] = Math.cos(angle)*dx;
      sinu[u] = Math.sin(angle)*dy;
    }
    int tile = 0;
    center[0] = x; center[1] = y; center[2] = z;
    { // Tiles along the z axis
      double aux = dz/nz;
      for (int j=0; j<nz; j++) {
        for (int u=0; u<nu; u++, tile++) { // This ordering is important for the computations below (see ref)
          for (int k=0; k<3; k++) {
            corners[tile][0][k] = center[k] + (cosu[u  ]*vectorx[k] + sinu[u  ]*vectory[k])*(nz-j)/nz   + j    *aux*vectorz[k];
            corners[tile][1][k] = center[k] + (cosu[u+1]*vectorx[k] + sinu[u+1]*vectory[k])*(nz-j)/nz   + j    *aux*vectorz[k];
            corners[tile][2][k] = center[k] + (cosu[u+1]*vectorx[k] + sinu[u+1]*vectory[k])*(nz-j-1)/nz + (j+1)*aux*vectorz[k];
            corners[tile][3][k] = center[k] + (cosu[u  ]*vectorx[k] + sinu[u  ]*vectory[k])*(nz-j-1)/nz + (j+1)*aux*vectorz[k];
          }
        }
      }
    }
//          data[i][j][k] = center[k] + cos*size[0]*(1.0-aux)*vectorx[k] + sin*size[1]*(1.0-aux)*vectory[k] + aux*size[2]*vectorz[k];
    if (closedBottom) { // Tiles at bottom
//      int ref=0; // not used
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
    if (Math.abs(angle2-angle1)<360){ // No need to close left or right if the cilinder is 'round' enough
//      System.out.println ("Computing lateral tiles");
      center[0] = x; center[1] = y; center[2] = z;
      if (closedRight) { // Tiles at right
        int ref = 0;
        double aux = dz/nz;
        for (int j=0; j<nz; j++, ref+=nu) {
          center[0]     = x + j    *aux*vectorz[0]; center[1]     = y + j    *aux*vectorz[1]; center[2]     = z + j    *aux*vectorz[2];
          nextCenter[0] = x + (j+1)*aux*vectorz[0]; nextCenter[1] = y + (j+1)*aux*vectorz[1]; nextCenter[2] = z + (j+1)*aux*vectorz[2];
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
        double aux = dz/nz;
        for (int j=0; j<nz; j++, ref+=nu) {
          center[0]     = x + j    *aux*vectorz[0]; center[1]     = y + j    *aux*vectorz[1]; center[2]     = z + j    *aux*vectorz[2];
          nextCenter[0] = x + (j+1)*aux*vectorz[0]; nextCenter[1] = y + (j+1)*aux*vectorz[1]; nextCenter[2] = z + (j+1)*aux*vectorz[2];
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

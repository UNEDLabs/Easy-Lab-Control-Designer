/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.displayejs.utils.VectorAlgebra;


/**
 * This class implements a straight 3D Cylinder.
 * The Cylinder can be repositioned and resized along its axis.
 * (The size of the Cylinder is actually its vector axis.)
 * To be honest, I find InteractiveCylinder more useful.
 * @see InteractiveCylinder for a more general 3D Cylinder.
 */
public class InteractiveCylinderSimple extends AbstractInteractiveTile {
  static final protected double TO_RADIANS=Math.PI/180.0;

  // Configuration variables
  /**
   * The radius of the spring (normal to its direction)
   */
  protected double radius=0.1;
  protected boolean closedBottom = true, closedTop = true;
  protected boolean closedLeft   = true, closedRight = true;
  protected int minangleu = 0, maxangleu = 360;
  protected double[] vectorx = {1.0,0.0,0.0}, vectory = {0.0,1.0,0.0}, vectorz = {0.0,0.0,1.0}; // Standard vertical Cylinder
  // Implementation variables
  protected int nr = -1, nu = -1, nz = -1; // Make sure arrays are allocated
  protected double cos[] = null, sin[] = null, center[] =  {0.0,0.0,0.0};

  public InteractiveCylinderSimple () {
    setSizeXYZ(0.2,0.2,0.5);
    setResolution (new Resolution(3,12,5));
//    setOrigin (0.5,0.5,0,true);

  }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveCylinderSimple) {
      InteractiveCylinderSimple oldCylinder = (InteractiveCylinderSimple) _element;
      setRadius( oldCylinder.getRadius());
      setMinAngleU( oldCylinder.getMinAngleU());
      setMaxAngleU(oldCylinder.getMaxAngleU());
      setClosedBottom(oldCylinder.isClosedBottom());
      setClosedTop(oldCylinder.isClosedTop());
      setClosedLeft(oldCylinder.isClosedLeft());
      setClosedRight(oldCylinder.isClosedRight());
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  /**
   * Set the radius of the Cylinder.
   */
  public void setRadius (double _r) { this.radius = _r; hasChanged = true; }

  /**
   * Get the radius of the Cylinder.
   */
  public double getRadius () { return this.radius; }

  public void   setMinAngleU(int _angle)  { this.minangleu = _angle; hasChanged = true; }
  public int    getMinAngleU() { return this.minangleu; }

  public void   setMaxAngleU(int _angle)  { this.maxangleu = _angle; hasChanged = true; }
  public int    getMaxAngleU() { return this.maxangleu; }

  public void    setClosedBottom (boolean _close) { this.closedBottom = _close; hasChanged = true; }
  public boolean isClosedBottom () { return this.closedBottom; }

  public void    setClosedTop (boolean _close) { this.closedTop = _close; hasChanged = true; }
  public boolean isClosedTop () { return this.closedTop; }

  public void    setClosedLeft (boolean _close) { this.closedLeft = _close; hasChanged = true; }
  public boolean isClosedLeft () { return this.closedLeft; }

  public void    setClosedRight (boolean _close) { this.closedRight = _close; hasChanged = true; }
  public boolean isClosedRight () { return this.closedRight; }

// -------------------------------------
//  Private or protected methods
// -------------------------------------

  protected void computeAbsoluteDifference (double[] result) {
    double dx = (originx-0.5)*sizex, dy = (originy-0.5)*sizey, dz = originz*sizez;
    result[0] = dx*vectorx[0] + dy*vectory[0] + dz*vectorz[0];
    result[1] = dx*vectorx[1] + dy*vectory[1] + dz*vectorz[1];
    result[2] = dx*vectorx[2] + dy*vectory[2] + dz*vectorz[2];
  }

  protected synchronized void computeCorners () {
//    System.out.println("Computing Cylinder");
    double dx = sizex, dy = sizey, dz = sizez;
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
          theNr = Math.max((int) Math.round(0.49 + Math.abs(radius)/resolution.maxLength), 1);
          theNu = Math.max((int) Math.round(0.49 + Math.abs(angle2-angle1)*TO_RADIANS*Math.abs(radius)/resolution.maxLength), 1);
          theNz = Math.max((int) Math.round(0.49 + Math.sqrt(dx*dx+dy*dy+dz*dz)/resolution.maxLength), 1);
        break;
      }
    }
    if (nr==theNr && nu==theNu && nz==theNz); // No need to reallocate arrays
    else {
      nr = theNr; nu = theNu; nz = theNz;
      cos = new double[nu+1]; sin = new double[nu+1];
      int totalN = nu*nz;
      if (closedBottom) totalN += nr*nu;
      if (closedTop)    totalN += nr*nu;
      if (Math.abs(angle2-angle1)<360) {
        if (closedLeft)   totalN += nr*nz;
        if (closedRight)  totalN += nr*nz;
      }
      setCorners(new double [totalN][4][3]); // Reallocate arrays
    }
    center[0] = x; center[1] = y; center[2] = z;
    for (int u=0; u<=nu; u++) { // compute sines and cosines
      double angle = ((nu-u)*angle1 + u*angle2)*TO_RADIANS/nu;
      cos[u] = Math.cos(angle);
      sin[u] = Math.sin(angle);
    }
    Point3D size = new Point3D (dx,dy,dz);
    Point3D u1 = VectorAlgebra.normalTo(size);
    Point3D u2 = VectorAlgebra.normalize (VectorAlgebra.crossProduct(size,u1));
    vectorx = u1.toArray();   for (int k=0; k<3; k++) vectorx[k] *= radius;
    vectory = u2.toArray();   for (int k=0; k<3; k++) vectory[k] *= radius;
    vectorz = size.toArray(); for (int k=0; k<3; k++) vectorz[k] /= nz;

    int tile = 0;
    { // Tiles along the z axis
      for (int j=0; j<nz; j++) {
        for (int u=0; u<nu; u++, tile++) { // This ordering is important for the computations below (see ref)
          for (int k=0; k<3; k++) {
            corners[tile][0][k] = center[k] + cos[u  ]*vectorx[k] + sin[u  ]*vectory[k] + j    *vectorz[k];
            corners[tile][1][k] = center[k] + cos[u+1]*vectorx[k] + sin[u+1]*vectory[k] + j    *vectorz[k];
            corners[tile][2][k] = center[k] + cos[u+1]*vectorx[k] + sin[u+1]*vectory[k] + (j+1)*vectorz[k];
            corners[tile][3][k] = center[k] + cos[u  ]*vectorx[k] + sin[u  ]*vectory[k] + (j+1)*vectorz[k];
          }
        }
      }
    }
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
    if (closedTop) { // Tiles at top
      int ref = nu*(nz-1);
      center[0] = x + dx; center[1] = y + dy; center[2] = z + dz;
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
    if (Math.abs(angle2-angle1)<360) { // No need to close left or right if the Cylinder is 'round' enough
//      System.out.println ("Computing lateral tiles");
      center[0] = x; center[1] = y; center[2] = z;
      if (closedRight) { // Tiles at right
        for (int j=0; j<nz; j++) {
          for (int i=0; i<nr; i++, tile++) {
            for (int k=0; k<3; k++) {
              corners[tile][0][k] = ((nr-i)  *center[k] +  i   *corners[0][0][k])/nr + j    *vectorz[k];
              corners[tile][1][k] = ((nr-i-1)*center[k] + (i+1)*corners[0][0][k])/nr + j    *vectorz[k];
              corners[tile][2][k] = ((nr-i-1)*center[k] + (i+1)*corners[0][0][k])/nr + (j+1)*vectorz[k];
              corners[tile][3][k] = ((nr-i)  *center[k] +  i   *corners[0][0][k])/nr + (j+1)*vectorz[k];
            }
          }
        }
      }
      if (closedLeft) { // Tiles at left
        int ref = nu-1;
        for (int j=0; j<nz; j++) {
          for (int i=0; i<nr; i++, tile++) {
            for (int k=0; k<3; k++) {
              corners[tile][0][k] = ((nr-i)  *center[k] +  i   *corners[ref][1][k])/nr + j    *vectorz[k];
              corners[tile][1][k] = ((nr-i-1)*center[k] + (i+1)*corners[ref][1][k])/nr + j    *vectorz[k];
              corners[tile][2][k] = ((nr-i-1)*center[k] + (i+1)*corners[ref][1][k])/nr + (j+1)*vectorz[k];
              corners[tile][3][k] = ((nr-i)  *center[k] +  i   *corners[ref][1][k])/nr + (j+1)*vectorz[k];
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

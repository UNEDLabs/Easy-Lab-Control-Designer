/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;


public class InteractivePlane extends AbstractInteractiveTile {

  // Configuration variables
  protected double vectorU[] = { 1.0, 0.0, 0.0};
  protected double vectorV[] = { 0.0, 1.0, 0.0};
  // Implementation variables
  protected int nu = -1, nv = -1; // Make sure arrays are allocated
  protected double vectorUSize = 1.0, vectorVSize = 1.0;

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractivePlane) {
      InteractivePlane old = (InteractivePlane) _element;
      setVectorU (old.vectorU[0],old.vectorU[1],old.vectorU[2]);
      setVectorV (old.vectorV[0],old.vectorV[1],old.vectorV[2]);
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  /**
   * Set the first director vector of the plane
   */
  public void setVectorU (double ux, double uy, double uz) {
    vectorU[0] = ux; vectorU[1] = uy; vectorU[2] = uz;
    vectorUSize = Math.sqrt(ux*ux+uy*uy+uz*uz);
    hasChanged = true;
  }
  /**
   * Set the second director vector of the plane
   */
  public void setVectorV (double vx, double vy, double vz) {
    vectorV[0] = vx; vectorV[1] = vy; vectorV[2] = vz;
    vectorVSize = Math.sqrt(vx*vx+vy*vy+vz*vz);
    hasChanged = true;
  }

// -------------------------------------
//  Private or protected methods
// -------------------------------------

  protected void computeAbsoluteDifference (double[] result) {
    double dx = originx*sizex, dy = originy*sizey; //, dz = originz*sizez;
    result[0] = dx*vectorU[0] + dy*vectorV[0]; // + dz*vectorz[0];
    result[1] = dx*vectorU[1] + dy*vectorV[1]; // + dz*vectorz[1];
    result[2] = dx*vectorU[2] + dy*vectorV[2]; // + dz*vectorz[2];
  }


  protected synchronized void computeCorners () {
//    System.out.println("Computing plane");
    int theNu = 1, theNv = 1;
    if (resolution!=null) {
      switch (resolution.type) {
        case Resolution.DIVISIONS :
          theNu = Math.max(resolution.n1,1);
          theNv = Math.max(resolution.n2,1);
          break;
        case Resolution.MAX_LENGTH :
          theNu = Math.max((int) Math.round(0.49 + Math.abs(sizex)*vectorUSize/resolution.maxLength), 1);
          theNv = Math.max((int) Math.round(0.49 + Math.abs(sizey)*vectorVSize/resolution.maxLength), 1);
        break;
      }
    }
    if (nu==theNu && nv==theNv); // No need to reallocate arrays
    else { nu = theNu; nv = theNv; setCorners(new double [nu*nv][4][3]); } // Reallocate arrays

    int tile = 0;
    double du = sizex/nu,  dv = sizey/nv;
    double origin[] = { x, y, z};
    for (int i=0; i<nu; i++) { // x-y sides
      double u = i*du;
      for (int j=0; j<nv; j++) {
        double v = j*dv;
        for (int k=0; k<3; k++) corners[tile][0][k] = origin[k] + u     *vectorU[k] + v     *vectorV[k];
        for (int k=0; k<3; k++) corners[tile][1][k] = origin[k] + (u+du)*vectorU[k] + v     *vectorV[k];
        for (int k=0; k<3; k++) corners[tile][2][k] = origin[k] + (u+du)*vectorU[k] + (v+dv)*vectorV[k];
        for (int k=0; k<3; k++) corners[tile][3][k] = origin[k] + u     *vectorU[k] + (v+dv)*vectorV[k];
        tile++; // The upper side
      }
    }
    transformCorners();
    hasChanged = false;
  }

}

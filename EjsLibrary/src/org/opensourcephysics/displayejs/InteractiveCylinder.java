/*
 * The display package contains drawing classes and drawables
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.opensourcephysics.displayejs;

import org.opensourcephysics.display.*;

/**
 * This class implements a general 3D Cylinder. The user can specify the three main axes
 * for the Cylinder resultig, for instance in non-straight Cylinders.
 * The Cylinder can be repositioned but cannot be resized.
 * @see InteractiveCylinderSimple for a simpler 3D Cylinder.
 */

public class InteractiveCylinder extends AbstractInteractiveTile {
  static final public int X_AXIS=0;
  static final public int Y_AXIS=1;
  static final public int Z_AXIS=2;
  static final public int USER_DEFINED = 3;

  static final protected double TO_RADIANS=Math.PI/180.0;

  // Configuration variables
  protected boolean closedBottom = true, closedTop = true;
  protected boolean closedLeft   = true, closedRight = true;
  protected int direction = Z_AXIS, minangleu = 0, maxangleu = 360;
  protected double[] center =  {0.0,0.0,0.0};
  protected double[] vectorx = {1.0,0.0,0.0}, // Standard vertical Cylinder
                     vectory = {0.0,1.0,0.0},
                     vectorz = {0.0,0.0,1.0};

  // Implementation variables
  protected boolean changeNTiles = true;
  protected int nr = -1, nu = -1, nz = -1; // Make sure arrays are allocated
  protected double cosu[] = null, sinu[] = null;

  /**
   * Default constructor. Equals to InteractiveCylinder (InteractiveCylinder.Z_AXIS);
   */
  public InteractiveCylinder () { this(Z_AXIS); }

  /**
   * Constructor for a given direction.
   * @param direction The direction for the sides of the Cylinder. Must be one of X_AXIS, Y_AXIS, or Z_AXIS.
   */
  public InteractiveCylinder (int direction) {
    setSizeXYZ(0.2,0.2,0.5);
    setResolution (new Resolution(3,12,5));
    setDirection(direction);
//    setOrigin (0.5,0.5,0,true);
  }

  public void copyFrom (InteractiveElement _element) {
    super.copyFrom(_element);
    if (_element instanceof InteractiveCylinder) {
      InteractiveCylinder oldCylinder = (InteractiveCylinder) _element;
      setMinAngleU( oldCylinder.getMinAngleU());
      setMaxAngleU(oldCylinder.getMaxAngleU());
      setClosedBottom(oldCylinder.isClosedBottom());
      setClosedTop(oldCylinder.isClosedTop());
      setClosedLeft(oldCylinder.isClosedLeft());
      setClosedRight(oldCylinder.isClosedRight());
      setDirection(oldCylinder.getDirection());
      for (int i=0; i<3; i++) {
        vectorx[i] = oldCylinder.vectorx[i];
        vectory[i] = oldCylinder.vectory[i];
        vectorz[i] = oldCylinder.vectorz[i];
      }
    }
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  /**
   * Sets the minimum angle to build the top and bottom sides of the element.
   * @param angle the minimum angle
   */
  public void   setMinAngleU(int angle)  { this.minangleu = angle; hasChanged = true; changeNTiles = true; }
  /**
   * Gets the minimum angle used to build the top and bottom sides of the element.
   * @return the minimum angle
   */
  public int    getMinAngleU() { return this.minangleu; }

  /**
   * Sets the maximum angle to build the top and bottom sides of the element.
   * @param angle the maximum angle
   */
  public void   setMaxAngleU(int angle)  { this.maxangleu = angle; hasChanged = true; changeNTiles = true; }
  /**
   * Gets the maximum angle used to build the top and bottom sides of the element.
   * @return the maximum angle
   */
  public int    getMaxAngleU() { return this.maxangleu; }

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

  /**
   * Whether an incomplete element should be closed at its left side.
   * @param closed the desired value
   */
  public void    setClosedLeft (boolean close) { this.closedLeft = close; hasChanged = true; changeNTiles = true; }
  /**
   * Whether the element is closed at its left side.
   * @return the value
   */
  public boolean isClosedLeft () { return this.closedLeft; }

  /**
   * Whether an incomplete element should be closed at its right side.
   * @param closed the desired value
   */
  public void    setClosedRight (boolean close) { this.closedRight = close; hasChanged = true; changeNTiles = true; }
  /**
   * Whether the element is closed at its right side.
   * @return the value
   */
  public boolean isClosedRight () { return this.closedRight; }

  /**
   * Sets the element's main direction.
   * @param direction The direction for the sides of the element. Must be one of X_AXIS, Y_AXIS, or Z_AXIS.
   */
  public void setDirection (int direction)  {
    if (direction==this.direction) return;
    this.direction = direction;
    switch (this.direction) {
      case X_AXIS :
        vectorx[0] = 0.0; vectorx[1] = 1.0; vectorx[2] =0.0;
        vectory[0] = 0.0; vectory[1] = 0.0; vectory[2] =1.0;
        vectorz[0] = 1.0; vectorz[1] = 0.0; vectorz[2] =0.0;
        break;
      case Y_AXIS :
        vectorx[0] = 1.0; vectorx[1] = 0.0; vectorx[2] =0.0;
        vectory[0] = 0.0; vectory[1] = 0.0; vectory[2] =1.0;
        vectorz[0] = 0.0; vectorz[1] = 1.0; vectorz[2] =0.0;
        break;
      default :
      case Z_AXIS :
        vectorx[0] = 1.0; vectorx[1] = 0.0; vectorx[2] =0.0;
        vectory[0] = 0.0; vectory[1] = 1.0; vectory[2] =0.0;
        vectorz[0] = 0.0; vectorz[1] = 0.0; vectorz[2] =1.0;
        break;
      case USER_DEFINED : break;
    }
    hasChanged = true;
  }
  /**
   * Get the element's main direction.
   * @return the direction for the sides of the element. One of X_AXIS, Y_AXIS, or Z_AXIS. If custom axes
   * have been set, then it returns USER_DEFINED.
   */
  public int getDirection() { return this.direction; }

  /**
   * Set custom axes for the element.
   * @param axesData an array of nine doubles, three for each of the axes.
   */
  public void setCustomAxes  (double[] axesData) {
    this.direction=USER_DEFINED;
    vectorx[0] = axesData[0]; vectorx[1] = axesData[1]; vectorx[2] = axesData[2];
    vectory[0] = axesData[3]; vectory[1] = axesData[4]; vectory[2] = axesData[5];
    vectorz[0] = axesData[6]; vectorz[1] = axesData[7]; vectorz[2] = axesData[8];
    hasChanged = true;
  }

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

   protected void computeAbsoluteDifference (double[] result) {
     double dx = (originx-0.5)*sizex, dy = (originy-0.5)*sizey, dz = originz*sizez;
     result[0] = dx*vectorx[0] + dy*vectory[0] + dz*vectorz[0];
     result[1] = dx*vectorx[1] + dy*vectory[1] + dz*vectorz[1];
     result[2] = dx*vectorx[2] + dy*vectory[2] + dz*vectorz[2];
   }

   protected synchronized void computeCorners () {
//    System.out.println("Computing Cylinder");
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
       int totalN = nu*nz; //  + 2*nr*nu + 2*nr*nz;
       if (closedBottom) totalN += nr*nu;
       if (closedTop)    totalN += nr*nu;
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
             corners[tile][0][k] = center[k] + cosu[u  ]*vectorx[k] + sinu[u  ]*vectory[k] + j    *aux*vectorz[k];
             corners[tile][1][k] = center[k] + cosu[u+1]*vectorx[k] + sinu[u+1]*vectory[k] + j    *aux*vectorz[k];
             corners[tile][2][k] = center[k] + cosu[u+1]*vectorx[k] + sinu[u+1]*vectory[k] + (j+1)*aux*vectorz[k];
             corners[tile][3][k] = center[k] + cosu[u  ]*vectorx[k] + sinu[u  ]*vectory[k] + (j+1)*aux*vectorz[k];
           }
         }
       }
     }
     if (closedBottom) { // Tiles at bottom
//       int ref=0; // not used
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
       center[0] = x + dz*vectorz[0]; center[1] = y + dz*vectorz[1]; center[2] = z + dz*vectorz[2];
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
     if (Math.abs(angle2-angle1)<360){ // No need to close left or right if the Cylinder is 'round' enough
//      System.out.println ("Computing lateral tiles");
       center[0] = x; center[1] = y; center[2] = z;
       if (closedRight) { // Tiles at right
         double aux = dz/nz;
         for (int j=0; j<nz; j++) {
           for (int i=0; i<nr; i++, tile++) {
             for (int k=0; k<3; k++) {
               corners[tile][0][k] = ((nr-i)  *center[k] +  i   *corners[0][0][k])/nr + j    *aux*vectorz[k];
               corners[tile][1][k] = ((nr-i-1)*center[k] + (i+1)*corners[0][0][k])/nr + j    *aux*vectorz[k];
               corners[tile][2][k] = ((nr-i-1)*center[k] + (i+1)*corners[0][0][k])/nr + (j+1)*aux*vectorz[k];
               corners[tile][3][k] = ((nr-i)  *center[k] +  i   *corners[0][0][k])/nr + (j+1)*aux*vectorz[k];
             }
           }
         }
       }
       if (closedLeft) { // Tiles at left
         double aux = dz/nz;
         int ref = nu-1;
         for (int j=0; j<nz; j++) {
           for (int i=0; i<nr; i++, tile++) {
             for (int k=0; k<3; k++) {
               corners[tile][0][k] = ((nr-i)  *center[k] +  i   *corners[ref][1][k])/nr + j    *aux*vectorz[k];
               corners[tile][1][k] = ((nr-i-1)*center[k] + (i+1)*corners[ref][1][k])/nr + j    *aux*vectorz[k];
               corners[tile][2][k] = ((nr-i-1)*center[k] + (i+1)*corners[ref][1][k])/nr + (j+1)*aux*vectorz[k];
               corners[tile][3][k] = ((nr-i)  *center[k] +  i   *corners[ref][1][k])/nr + (j+1)*aux*vectorz[k];
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

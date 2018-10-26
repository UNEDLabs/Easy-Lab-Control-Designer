/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.utils;

import org.opensourcephysics.drawing3d.*;
import org.opensourcephysics.drawing3d.utils.mapping.Mapping;
import org.opensourcephysics.drawing3d.utils.mapping.MappingXYZ;
import org.opensourcephysics.drawing3d.utils.mapping.MappingXZY;
import org.opensourcephysics.drawing3d.utils.mapping.MappingYXZ;
import org.opensourcephysics.drawing3d.utils.mapping.MappingYZX;
import org.opensourcephysics.drawing3d.utils.mapping.MappingZXY;
import org.opensourcephysics.drawing3d.utils.mapping.MappingZYX;
import org.opensourcephysics.numerics.*;

/**
 * <p>Title: Camera</p>
 * <p>Description: This class provides access to the position of the camera,
 * its focus point and its distance to the projection screen that are used
 * to view the 3D scenes. The camera can also be rotated around the line
 * of sight (i.e. the line which conects the camera with the focus point).</p>
 * 
 * <p>The camera position can be set using either the desired X,Y,Z coordinates
 * or spherical coordinates around the focus point. This makes it
 * easy to rotate the scene both horizontally and vertically (around the focus).</p>
 * 
 * <p>Panning can be achieved by moving the focus point to one side.</p>
 * 
 * <p>Zooming is done increasing (positive zoom) or decreasing the distance
 * between the camera and the projection screen.</p>
 * 
 * <p> The projection screen is always normal to the line of sight and has
 * its origin at the intersection of this line with the screen itself.</p>
 * 
 * <p>The camera provides fives different modes of projecting points in space
 * to the screen. Two modes are truly three-dimensional. The other three are
 * planar modes.
 *
 * @author Francisco Esquembre
 * @author Carlos Jara (CJB)
 * @version August 2009
 */
public class Camera {
  static public final int MODE_PLANAR_XY = 0;
  static public final int MODE_PLANAR_XZ = 1;
  static public final int MODE_PLANAR_YZ = 2;
  static public final int MODE_PERSPECTIVE_OFF = 3;
  static public final int MODE_PERSPECTIVE_ON = 4;
  static public final int MODE_NO_PERSPECTIVE = 10;
  static public final int MODE_PERSPECTIVE = 11;

  static private final double RATIO_TO_SCREEN = 2.5;
  static private final double RATIO_TO_FOCUS = 2.0;
  static private final double[] VERTICAL_AXIS = {0, 0, 1};

  static public final int CHANGE_ANY      = 0;
  static public final int CHANGE_MODE     = 1;
  static public final int CHANGE_POSITION = 2;
  static public final int CHANGE_FOCUS    = 3;
  static public final int CHANGE_ROTATION = 4;
  static public final int CHANGE_SCREEN   = 5;
  static public final int CHANGE_ANGLES   = 6;
  static public final int CHANGE_MAPPING  = 7;

  // Configuration variables
  private int projectionMode = MODE_PERSPECTIVE_ON;
  private double posX=4, posY=0, posZ=0;
  private double focusX=0, focusY=0, focusZ=0;
  private double distanceToScreen, rotationAngle = 0;
  private double alpha = 0.0, beta = 0.0;
  private int mapType = Mapping.MAP_XYZ;

  // Implementation variables
  private double distanceToFocus, panelMaxSizeConstant, planarRatio;
  double cosAlpha = 1, sinAlpha = 0, cosBeta = 1, sinBeta = 0;
  private double cosRot = 1, sinRot = 0;
  private double[] e1 = {-1, 0, 0}, e2 = {0, 1, 0}, e3 = {0, 0, 1};
  private Projection projection = new Projection();
  private Quaternion rotation = new Quaternion(1, 0, 0, 0);
  private Mapping mapping = new MappingXYZ();

  /**
   * The DrawingPanel3D to which it belongs.
   * This is needed to report to it any change that implies a call to update()
   */
  private DrawingPanel3D panel;

  public Camera(DrawingPanel3D aPanel) {
    this.panel = aPanel;
  }

  // -----------------------------
  // Implementation of Camera
  // ----------------------------
  
  /**
   * Sets one of the projecting modes. Possible values are:
   * <ul>
   *   <li>MODE_PERSPECTIVE_ON: 3D mode in which objects far away look smaller.</li>
   *   <li>MODE_PERSPECTIVE_OFF: 3D mode in which distance doesn't affect the size of the objects</li>
   *   <li>MODE_PLANAR_XY: 2D mode in which only the X and Y coordinates are displayed.</li>
   *   <li>MODE_PLANAR_XZ: 2D mode in which only the X and Z coordinates are displayed.</li>
   *   <li>MODE_PLANAR_YZ: 2D mode in which only the Y and Z coordinates are displayed.</li>
   * </ul>
   * <p>Changing the mode does not reset the camera.
   * @param mode int
   */
  public void setProjectionMode(int mode) {
    projectionMode = mode;
    panelMaxSizeConstant = panel.getMaximum3DSize()*0.01;
    panel.cameraChanged(CHANGE_MODE);
  }

  /**
   * Gets the projecting mode of the camera.
   * @return int
   * #see #setProjectionMode(int)
   */
  final public int getProjectionMode() {
    return projectionMode;
  }

  /**
   * Resets the camera to the default.
   * The camera is placed along the X direction, at a reasonable distance
   * from the center of the panel, which becomes the focus, and is not rotated.
   * The screen is also placed at a reasonable distance so that to view the
   * whole scene.
   */
  public void reset() {
    double[] center = panel.getCenter();
    if (is3dMode()) mapping.map(center);
    focusX = center[0];
    focusY = center[1];
    focusZ = center[2];
    panelMaxSizeConstant = panel.getMaximum3DSize();
    rotationAngle = 0;
    cosRot = 1;
    sinRot = 0;
    distanceToScreen = RATIO_TO_SCREEN*panelMaxSizeConstant;
    distanceToFocus = RATIO_TO_FOCUS*panelMaxSizeConstant;
    planarRatio = distanceToScreen/distanceToFocus;
    posX = center[0]+distanceToFocus;
    posY = center[1];
    posZ = center[2];
    alpha = 0;
    cosAlpha = 1;
    sinAlpha = 0;
    beta = 0;
    cosBeta = 1;
    sinBeta = 0;
    e1 = new double[] {-1, 0, 0};
    e2 = new double[] {0, 1, 0};
    e3 = new double[] {0, 0, 1};
    panelMaxSizeConstant *= 0.01;
    panel.cameraChanged(CHANGE_ANY);
  }

  /**
   * Adjust is a soft reset. It respects the azimuth and altitude, but recomputes the
   * rest so that the scene is not too close to the eye.
   * This method is typically called when the extrema of the scene change
   */
  public void adjust() {
    double[] center = panel.getCenter();
    if (is3dMode()) mapping.map(center);
    focusX = center[0];
    focusY = center[1];
    focusZ = center[2];
    panelMaxSizeConstant = panel.getMaximum3DSize();
    distanceToScreen = RATIO_TO_SCREEN*panelMaxSizeConstant;
    distanceToFocus = RATIO_TO_FOCUS*panelMaxSizeConstant;
    planarRatio = distanceToScreen/distanceToFocus;
    panelMaxSizeConstant *= 0.01;
    updateCamera(CHANGE_ANGLES);
  }

  /**
   * Sets the correspondance between axes and coordinates.
   * @param mappingType An integer specified by the static constants in the Mapping class
   * @see org.opensourcephysics.drawing3d.utils.mapping.Mapping
   */
  public void setMapping(int mappingType) {
    if (this.mapType==mappingType) return; // No need to change
    this.mapType = mappingType; 
    switch(mappingType) {
      default :
      case Mapping.MAP_XYZ : mapping = new MappingXYZ(); break;
      case Mapping.MAP_XZY : mapping = new MappingXZY(); break;
      case Mapping.MAP_YXZ : mapping = new MappingYXZ(); break;
      case Mapping.MAP_YZX : mapping = new MappingYZX(); break;
      case Mapping.MAP_ZXY : mapping = new MappingZXY(); break;
      case Mapping.MAP_ZYX : mapping = new MappingZYX(); break;
    }
    if (panel.getResetCameraOnChanges()) adjust();
    panel.cameraChanged(CHANGE_MAPPING);
  }

  /**
   * Maps coordinates to axes for the given point
   * @param point
   * @return double[]
   */
  public double[] map(double[] point) { return mapping.map(point); }

  /**
   * Reverses the mapping on the given point
   * @param point
   * @return double[]
   */
  public double[] inverseMapping(double[] point) { return mapping.inverse(point); }
  
  //CJB
  public double[] getQuatMapping() { return mapping.quatForPrimitives(); }
  
  /**
   * Sets the position of the camera.
   * @param x double
   * @param y double
   * @param z double
   */
  public void setXYZ(double x, double y, double z) {
    posX = x;
    posY = y;
    posZ = z;
    updateCamera(CHANGE_POSITION);
  }

  /**
   * Sets the position of the camera.
   * @param point double[]
   */
  public void setXYZ(double[] point) {
    setXYZ(point[0], point[1], point[2]);
  }

  /**
   * Returns the camera X coordinate
   * @return double the X coordinate of the camera position
   */
  final public double getX() {
    return posX;
  }

  /**
   * Returns the camera Y coordinate
   * @return double the Y coordinate of the camera position
   */
  final public double getY() {
    return posY;
  }

  /**
   * Returns the camera Z coordinate
   * @return double the Z coordinate of the camera position
   */
  final public double getZ() {
    return posZ;
  }

  /**
   * Sets the focus point of the camera. That it, the point in space
   * at which the camera is pointing.
   * @param x double
   * @param y double
   * @param z double
   */
  public void setFocusXYZ(double x, double y, double z) {
    focusX = x;
    focusY = y;
    focusZ = z;
    updateCamera(CHANGE_FOCUS);
  }

  /**
   * Sets the focus of the camera.
   * @param point double[]
   */
  public void setFocusXYZ(double[] point) {
    setFocusXYZ(point[0], point[1], point[2]);
  }

  /**
   * Returns the focus X coordinate
   * @return double the X coordinate of the focus position
   */
  final public double getFocusX() {
    return focusX;
  }

  /**
   * Returns the focus Y coordinate
   * @return double the Y coordinate of the focus position
   */
  final public double getFocusY() {
    return focusY;
  }

  /**
   * Returns the focus Z coordinate
   * @return double the Z coordinate of the focus position
   */
  final public double getFocusZ() {
    return focusZ;
  }

  final public double getDistanceToFocus() {
    return this.distanceToFocus;
  }
  
  /**
   * Sets the angle that the camera is rotated along the line of sight.
   * Default is 0.
   * @param angle double The angle in radians
   */
  public void setRotation(double angle) {
    rotationAngle = angle;
    cosRot = Math.cos(rotationAngle/2);
    sinRot = Math.sin(rotationAngle/2);
    updateCamera(CHANGE_ROTATION);
  }

  /**
   * Returns the angle that the camera is rotated along the line of sight.
   * @return double
   */
  final public double getRotation() {
    return rotationAngle;
  }

  /**
   * Sets the distance from the camera to the projecting screen.
   * @param distance double
   */
  public void setDistanceToScreen(double distance) {
    distanceToScreen = distance;
    planarRatio = distanceToScreen/distanceToFocus;
    panel.cameraChanged(CHANGE_SCREEN);
  }

  /**
   * Returns the distance from the camera to the projecting screen.
   * @return double
   */
  final public double getDistanceToScreen() {
    return distanceToScreen;
  }

  /**
   * Set the azimuthal (horizontal) angle of the camera position in spherical
   * coordinates with respect to the focus point. A value of 0 places the
   * camera in the XZ plane.
   * @param angle the desired angle in radians
   */
  public void setAzimuth(double angle) {
    alpha = angle;
    cosAlpha = Math.cos(alpha);
    sinAlpha = Math.sin(alpha);
    updateCamera(CHANGE_ANGLES);
  }

  /**
   * Get the horizontal angle of the camera position in spherical coordinates
   * with respect to the focus point. A value of 0 means the camera is in the
   * XZ plane.
   * @return double
   */
  final public double getAzimuth() {
    return alpha;
  }

  /**
   * Set the elevation (VERTICAL_AXIS) angle of the camera position in spherical
   * coordinates with respect to the focus point. A value of 0 places the
   * camera is in the XY plane.
   * @param angle the desired angle in radians in the range [-Math.PI/2,Math.PI/2]
   */
  public void setAltitude(double angle) {
    beta = angle;
    if(beta<-Math.PI/2) beta = -Math.PI/2;
    else if(beta>Math.PI/2) beta = Math.PI/2;
    cosBeta = Math.cos(beta);
    sinBeta = Math.sin(beta);
    updateCamera(CHANGE_ANGLES);
  }

  /**
   * Get the elevation (VERTICAL_AXIS) angle of the camera position in spherical
   * coordinates with respect to the focus point. A value of 0 means the
   * camera is in the XY plane.
   * @return double
   */
  final public double getAltitude() {
    return beta;
  }

  /**
   * Set the angles of the camera position in spherical coordinates
   * with respect to the focus point.
   * @param azimuth the desired azimuthal angle in radians
   * @param altitude the desired altitude angle in radians in the range [-Math.PI/2,Math.PI/2]
   */
  public void setAzimuthAndAltitude(double azimuth, double altitude) {
    alpha = azimuth;
    beta = altitude;
    if(beta<-Math.PI/2) beta = -Math.PI/2;
    else if(beta>Math.PI/2) beta = Math.PI/2;
    cosAlpha = Math.cos(alpha);
    sinAlpha = Math.sin(alpha);
    cosBeta = Math.cos(beta);
    sinBeta = Math.sin(beta);
    updateCamera(CHANGE_ANGLES);
  }

  /**
   * Copies its configuration from another camera
   * @param camera
   */
  public void copyFrom (Camera camera) {
    projectionMode = camera.getProjectionMode();
    panelMaxSizeConstant = panel.getMaximum3DSize()*0.01;
    posX = camera.getX();
    posY = camera.getY();
    posZ = camera.getZ();
    focusX = camera.getFocusX();
    focusY = camera.getFocusY();
    focusZ = camera.getFocusZ();
    rotationAngle = camera.getRotation();
    cosRot = Math.cos(rotationAngle/2);
    sinRot = Math.sin(rotationAngle/2);
    distanceToScreen = camera.getDistanceToScreen();
    planarRatio = distanceToScreen/distanceToFocus;
    updateCamera(CHANGE_ANY);
  }

  final public double getCosAlpha() {
    return this.cosAlpha;
  }
  
  final public double getSinAlpha() {
    return this.sinAlpha;
  }

  final public double getCosBeta() {
    return this.cosBeta;
  }

  final public double getSinBeta() {
    return this.sinBeta;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  private void updateCamera(int change) {
    switch(change) {
      case CHANGE_POSITION :
      case CHANGE_FOCUS :
        distanceToFocus = computeCameraVectors();
        planarRatio = distanceToScreen/distanceToFocus;
        alpha = Math.atan2(-e1[1], -e1[0]);
        beta = Math.atan2(-e1[2], Math.abs(e1[0]));
        cosAlpha = Math.cos(alpha);
        sinAlpha = Math.sin(alpha);
        cosBeta = Math.cos(beta);
        sinBeta = Math.sin(beta);
        break;
      case CHANGE_ROTATION :
        computeCameraVectors(); // e2 and e3 are different (rotated)
        break;
      case CHANGE_ANGLES :
        posX = focusX+distanceToFocus*cosBeta*cosAlpha;
        posY = focusY+distanceToFocus*cosBeta*sinAlpha;
        posZ = focusZ+distanceToFocus*sinBeta;
        computeCameraVectors();
        break;
      case CHANGE_ANY :
        distanceToFocus = computeCameraVectors();
        planarRatio = distanceToScreen/distanceToFocus;
        alpha = Math.atan2(-e1[1], -e1[0]);
        beta = Math.atan2(-e1[2], Math.abs(e1[0]));
        cosAlpha = Math.cos(alpha);
        sinAlpha = Math.sin(alpha);
        cosBeta = Math.cos(beta);
        sinBeta = Math.sin(beta);
        computeCameraVectors(); // e2 and e3 are different (rotated)
        break;
    }
    panel.cameraChanged(change);
  }

  private double computeCameraVectors() {
    e1 = new double[] {focusX-posX, focusY-posY, focusZ-posZ};
    double magnitudeE1 = VectorMath.magnitude(e1);
    for(int i = 0;i<e1.length;i++) {
      e1[i] /= magnitudeE1;
    }
    e2 = VectorMath.cross3D(e1, VERTICAL_AXIS);
    double magnitude = VectorMath.magnitude(e2);
    for(int i = 0;i<e2.length;i++) {
      e2[i] /= magnitude;
    }
    e3 = VectorMath.cross3D(e2, e1);
    magnitude = VectorMath.magnitude(e3);
    for(int i = 0;i<e3.length;i++) {
      e3[i] /= magnitude;
    }
    // Finally apply the rotation
    rotation.setCoordinates(cosRot, e1[0]*sinRot, e1[1]*sinRot, e1[2]*sinRot);
    rotation.direct(e2);
    rotation.direct(e3);
    return magnitudeE1;
  }

  // -------------------------------------
  // Projection methods
  // -------------------------------------

  /**
   * Whether the projection mode is three-dimensional
   * @return boolean
   */
  public boolean is3dMode() {
    switch(projectionMode) {
      case MODE_PLANAR_XY :
      case MODE_PLANAR_XZ :
      case MODE_PLANAR_YZ :
        return false;
      default :
        return true;
    }
  }

  public double[] projectPosition(double[] p) {
    return projection.direct(p);
  }
  
  /**
   * Computes the projection of a size at a given point.
   * For internal use of DrawingPanel3D only
   */
  public double[] projectSize(double[] p, double[] size) {
    switch(projectionMode) {
      case MODE_PLANAR_XY :
        size[0] = size[0]*planarRatio;
        return size;
      case MODE_PLANAR_XZ :
        size[0] = size[0]*planarRatio;
        size[1] = size[2]*planarRatio;
        return size;
      case MODE_PLANAR_YZ :
        size[0] = size[1]*planarRatio;
        size[1] = size[2]*planarRatio;
        return size;
      case MODE_NO_PERSPECTIVE : case MODE_PERSPECTIVE_OFF :
        mapping.map(size);
        size[0] = Math.max(size[0], size[1]);
        size[1] = size[2];
        return size;
      default :
      case MODE_PERSPECTIVE : case MODE_PERSPECTIVE_ON :
        mapping.map(p);
        mapping.map(size);
        double factor = (p[0]-posX)*e1[0]+(p[1]-posY)*e1[1]+(p[2]-posZ)*e1[2];
        if (Math.abs(factor)<panelMaxSizeConstant) {
          factor = panelMaxSizeConstant; // Avoid division by zero
        }
        factor = distanceToScreen/factor;
        size[0] = Math.max(size[0], size[1])*factor;
        size[1] = size[2]*factor;
        return size;
    }
  }

  private class Projection implements org.opensourcephysics.numerics.Transformation {
    public Object clone() {
      try {
        return super.clone();
      } catch(CloneNotSupportedException exc) {
        exc.printStackTrace();
        return null;
      }
    }

    public double[] direct(double[] p) {
      switch(projectionMode) {
        case MODE_PLANAR_XY :
          p[0] = (p[0]-focusX)*planarRatio;
          p[1] = (p[1]-focusY)*planarRatio;
          p[2] = 1.0-(p[2]-focusZ)/distanceToFocus;
          return p;
        case MODE_PLANAR_XZ : {
          double aux = p[1];
          p[0] = (p[0]-focusX)*planarRatio;
          p[1] = (p[2]-focusZ)*planarRatio;
          p[2] = 1.0-(aux-focusY)/distanceToFocus;
          return p;
        }
        case MODE_PLANAR_YZ : {
          double aux = p[0];
          p[0] = (p[1]-focusY)*planarRatio;
          p[1] = (p[2]-focusZ)*planarRatio;
          p[2] = 1.0-(aux-focusX)/distanceToFocus;
          return p;
        }
        case MODE_NO_PERSPECTIVE : case MODE_PERSPECTIVE_OFF : {
          mapping.map(p);
          p[0] -= posX;
          p[1] -= posY;
          p[2] -= posZ;
          double aux1 = VectorMath.dot(p, e1);
          double aux2 = VectorMath.dot(p, e2);
          p[1] = VectorMath.dot(p, e3);
          p[0] = aux2;
          p[2] = aux1/distanceToFocus;
          return p;
        }
        default :
        case MODE_PERSPECTIVE : case MODE_PERSPECTIVE_ON  : {
          mapping.map(p);
          p[0] -= posX;
          p[1] -= posY;
          p[2] -= posZ;
          double factor = VectorMath.dot(p, e1), aux1 = factor;
          if(Math.abs(factor)<panelMaxSizeConstant) {
            factor = panelMaxSizeConstant; // Avoid division by zero
          }
          // if (aux1<0) aux1 = Double.NaN;
          factor = distanceToScreen/factor;
          double aux2 = VectorMath.dot(p, e2)*factor;
          p[1] = VectorMath.dot(p, e3)*factor;
          p[0] = aux2;
          p[2] = aux1/distanceToFocus;
          return p;
        }
      }
    }

    public double[] inverse(double[] point) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
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

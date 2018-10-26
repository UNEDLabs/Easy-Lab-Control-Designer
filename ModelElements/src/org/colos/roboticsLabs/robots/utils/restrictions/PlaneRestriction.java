package org.colos.roboticsLabs.robots.utils.restrictions;

/**
 * @author Almudena Ruiz
 */

abstract public class PlaneRestriction implements Restriction {

  static public enum COORDINATE{X, Y, Z};
  COORDINATE mCoord;
  double mLimit;
  boolean mGreater;

  public PlaneRestriction(COORDINATE coord, double limit, boolean greater) {
    mCoord = coord;
    mLimit = limit;
    mGreater = greater;
    }

  public boolean allowsPoint(double x, double y, double z) {
    switch(mCoord){
      case X: {
        if (mGreater) return (x > mLimit);
        return (x < mLimit);
      }
      case Y:{
        if (mGreater) return (y > mLimit);
        return (y < mLimit);
      }    
      case Z: {
        if (mGreater) return (z > mLimit);
        return (z < mLimit);
      }   
    }
    return true;
    }
  
  abstract public void action(double x, double y, double z);
}



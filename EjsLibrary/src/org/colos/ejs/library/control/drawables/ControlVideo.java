/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables for inclusion in
 * a DrawingPanel
 * Copyright (c) Dec 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import javax.swing.JOptionPane;

import org.colos.ejs.library.Memory;
import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.swing.ControlDrawable;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.drawing2d.Style;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.gif.GifVideo;
import org.opensourcephysics.media.xuggle.XuggleVideo;

public class ControlVideo extends ControlDrawable {
  static final private int VIDEO_PROPERTIES_ADDED=9;

  static final double TO_RAD = Math.PI/180.0;
  static private final String DEFAULT_VIDEO = "/org/opensourcephysics/resources/controls/images/play.gif";

  private Video mVideo;
  private double mX,mY,mWidth,mHeight,mAngle;
  private int mFrame,mIntegerAngle, mRelativePosition;
  private boolean mVisible;
  private String mVideoFile;
  private double mOriginalAspect;

  protected Drawable createDrawable () {
    mX = mY = 0.0;
    mWidth = mHeight = 1.0;
    mAngle = 0.0;
    mFrame = 0;
    mIntegerAngle = 0;
    mRelativePosition=Style.SOUTH_EAST;
    mVisible = true;
    mVideoFile = null;
    setVideo(DEFAULT_VIDEO);
    return mVideo;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("x");
      infoList.add ("y");
      infoList.add ("sizex");
      infoList.add ("sizey");
      infoList.add ("angle");
      infoList.add ("videofile");
      infoList.add ("frame");
      infoList.add ("visible");
      infoList.add ("elementposition");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyCommonName(String _property) {
    if (_property.equals("angle")) return "rotationAngle";
    if (_property.equals("sizex")) return "sizeX";
    if (_property.equals("sizey")) return "sizeY";
    return super.getPropertyCommonName(_property);
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("x"))          return "int|double";
    if (_property.equals("y"))          return "int|double";
    if (_property.equals("sizex"))      return "int|double";
    if (_property.equals("sizey"))      return "int|double";
    if (_property.equals("angle"))      return "int|double";
    if (_property.equals("videofile"))  return "File|String";
    if (_property.equals("frame"))      return "int";
    if (_property.equals("visible"))    return "boolean";
    if (_property.equals("elementposition"))return "ElementPosition|int";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;

    if (_propertyType.indexOf("ElementPosition")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("centered"))         return new IntegerValue (Style.CENTERED);
      if (_value.equals("north"))            return new IntegerValue (Style.NORTH);
      if (_value.equals("south"))            return new IntegerValue (Style.SOUTH);
      if (_value.equals("east"))             return new IntegerValue (Style.EAST);
      if (_value.equals("west"))             return new IntegerValue (Style.WEST);
      if (_value.equals("north_east"))       return new IntegerValue (Style.NORTH_EAST);
      if (_value.equals("north_west"))       return new IntegerValue (Style.NORTH_WEST);
      if (_value.equals("south_east"))       return new IntegerValue (Style.SOUTH_EAST);
      if (_value.equals("south_west"))       return new IntegerValue (Style.SOUTH_WEST);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Variable properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getDouble()!=mX)  { mX =_value.getDouble(); positionVideo(); } break;
      case 1 : if (_value.getDouble()!=mY)  { mY =_value.getDouble(); positionVideo(); } break;
      case 2 : if (_value.getDouble()!=mWidth) { 
          mWidth=_value.getDouble();
          mVideo.setRelativeAspect((mWidth/mHeight)/mOriginalAspect);
          mVideo.setWidth(mWidth); 
          positionVideo();
        } 
        break;
      case 3 : if (_value.getDouble()!=mHeight) { 
          mHeight=_value.getDouble(); 
          mVideo.setRelativeAspect((mWidth/mHeight)/mOriginalAspect);
          mVideo.setHeight(mHeight);
          positionVideo();
        } 
        break;
      case 4 :
        if (_value instanceof IntegerValue) {
          if (_value.getInteger() != mIntegerAngle) {
            mIntegerAngle = _value.getInteger();
            mAngle = mIntegerAngle * TO_RAD;
            mVideo.setAngle(mAngle);
          }
        }
        else {
          if (_value.getDouble() != mAngle) {
            mAngle = _value.getDouble();
            mIntegerAngle = (int) (mAngle / TO_RAD);
            mVideo.setAngle(mAngle);
          }
        }
        break;
      case 5 : if (!_value.getString().equals(mVideoFile)) setVideo(_value.getString()); break;
      case 6 : if (_value.getInteger()!=mFrame) {
          mFrame = _value.getInteger();
          mVideo.setFrameNumber(mFrame);
        }
        break;
      case 7 : mVideo.setVisible(mVisible = _value.getBoolean()); break;
      case 8 : if (mRelativePosition!=_value.getInteger()) {
          mRelativePosition = _value.getInteger();
          positionVideo();
        }
        break;
      default: super.setValue(_index-VIDEO_PROPERTIES_ADDED,_value);
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : mX = 0; positionVideo(); break;
      case 1 : mY = 0; positionVideo(); break;
      case 2 : 
        mWidth = 1.0;
        mVideo.setRelativeAspect((mWidth/mHeight)/mOriginalAspect);
        mVideo.setWidth(mWidth); 
        positionVideo();
        break;
      case 3 : 
        mHeight = 1;
        mVideo.setRelativeAspect((mWidth/mHeight)/mOriginalAspect);
        mVideo.setHeight(mHeight);
        positionVideo();
        break;
      case 4 : mIntegerAngle = 0; mVideo.setAngle(mAngle=0.0); break;
      case 5 : setVideo(DEFAULT_VIDEO); break;
      case 6 : mVideo.setFrameNumber(mFrame=0); break;
      case 7 : mVideo.setVisible(mVisible=true); break;
      case 8 : mRelativePosition = Style.SOUTH_WEST; positionVideo(); break;

      default: super.setDefaultValue(_index-VIDEO_PROPERTIES_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 :
      case 1 : return "0";
      case 2 :
      case 3 : return "1";
      case 4 : return "0";
      case 5 : return "<none>";
      case 6 : return "0";
      case 7 : return "true";
      case 8 : return "SOUTH_EAST";
      default : return super.getDefaultValueString(_index-VIDEO_PROPERTIES_ADDED);
    }
 }

  public Value getValue (int _index) {
    switch(_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 :
      case 5 : case 6 : case 7 : case 8 :
        return null;
      default: return super.getValue(_index-VIDEO_PROPERTIES_ADDED);
    }
  }

// -------------------------------------
// private methods
// -------------------------------------

  private boolean setVideo(String filename) {
    if (filename==null) return false;
    if (filename.equals(mVideoFile)) return true;

    Video newVideo=null;
    try {
      if (filename.toLowerCase().endsWith(".gif")) newVideo = new GifVideo(filename);
      else newVideo = new XuggleVideo(Simulation.getResourceFile(filename).getAbsolutePath());
    }
    catch (Exception ex) { 
      ex.printStackTrace();
    }
    if (newVideo==null) {
      JOptionPane.showMessageDialog(this.getComponent(), Memory.getResource("ControlVideo.CannotPlayVideo"), Memory.getResource("Error"), JOptionPane.ERROR_MESSAGE);
      return false;
    }
    
    mVideoFile = filename;
    mVideo = newVideo;
    
 // get original sizes
    mVideo.setRelativeAspect(1);
    mOriginalAspect = mVideo.getWidth()/mVideo.getHeight();

    mVideo.setRelativeAspect((mWidth/mHeight)/mOriginalAspect);
    mVideo.setWidth(mWidth); 
    mVideo.setHeight(mHeight);

    mVideo.setAngle(mAngle);
    mVideo.setFrameNumber (mFrame);
    mVideo.setVisible(mVisible);
    positionVideo();
    super.replaceDrawable(mVideo);

    return true;
  }

  /**
   * Sets the mVideo in th eright position and with the right size
   */
  private void positionVideo () {
    double x = mX, y = mY;
    switch (mRelativePosition) {
      default :
      case Style.CENTERED   : x -= mWidth/2; y += mHeight/2; break;
      case Style.NORTH      : x -= mWidth/2;                 break;
      case Style.SOUTH      : x -= mWidth/2; y += mHeight;   break;
      case Style.EAST       : x -= mWidth;   y += mHeight/2; break;
      case Style.WEST       :                y += mHeight/2; break;
      case Style.NORTH_EAST : x -= mWidth;                   break;
      case Style.NORTH_WEST :                                break;
      case Style.SOUTH_EAST : x -=  mWidth;  y += mHeight;   break;
      case Style.SOUTH_WEST :                y += mHeight;   break;
    }
    mVideo.setX(x);
    mVideo.setY(y);
  }
  
}

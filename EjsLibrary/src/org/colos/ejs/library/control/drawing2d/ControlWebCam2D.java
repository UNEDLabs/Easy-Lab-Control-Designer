/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import java.awt.Image;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.opensourcephysics.drawing2d.*;
import org.colos.ejs.library.control.value.*;
import org.colos.ejs.library.utils.WebVideo;
import org.colos.ejs.library.utils.WebVideoListener;

/**
 * A 2D Image obtained from a WebCam
 */
public class ControlWebCam2D extends ControlImage2D implements WebVideoListener {
  static final private int WEBCAM2D_ADDED  = 6;
  static public final int WEBCAM2D_TRUE_SIZE = 0; // The URL of the element
  static public final int WEBCAM2D_POSITION  = 1; // The URL of the element
  static public final int WEBCAM2D_URL     = 2; // The URL of the element
  static public final int WEBCAM2D_MJPEG   = 3; // The MJPEG boolean
  static public final int WEBCAM2D_DELAY   = 4; // The delay between images
  static public final int WEBCAM2D_ENABLED = 5; // The enabled boolean

  private WebVideo mWebVideo;

  public String getObjectClassname () { return "org.opensourcephysics.drawing2d.ElementImage"; }

  protected org.opensourcephysics.display.Drawable createDrawable () {
    super.createDrawable();
    mWebVideo =  new WebVideo();
    mWebVideo.setMJPEGFormat(false);
    mWebVideo.addListener(this);
    return mImage;
  }

  protected int getPropertiesDisplacement () { return WEBCAM2D_ADDED; }

  public void onExit() { mWebVideo.stopRunning(); }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("trueSize");
      infoList.add ("elementposition");
      infoList.add ("url");
      infoList.add ("mjpeg");
      infoList.add ("delay");
      infoList.add ("enabled");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("trueSize")) return "boolean";
    if (_property.equals("elementposition"))return "ElementPosition|int";
    if (_property.equals("url"))      return "String";
    if (_property.equals("mjpeg"))    return "boolean";
    if (_property.equals("delay"))    return "int";
    if (_property.equals("enabled"))  return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case WEBCAM2D_TRUE_SIZE : if (_value.getBoolean()!=mImage.isTrueSize()) mImage.setTrueSize(_value.getBoolean()); break;
      case WEBCAM2D_POSITION  : if (_value.getInteger()!=mImage.getStyle().getRelativePosition()) mImage.getStyle().setRelativePosition(_value.getInteger()); break; 
      case WEBCAM2D_URL : mWebVideo.setURL(_value.getString()); break;
      case WEBCAM2D_MJPEG : mWebVideo.setMJPEGFormat(_value.getBoolean()); break;
      case WEBCAM2D_DELAY : mWebVideo.setDelay(_value.getInteger()); break;
      case WEBCAM2D_ENABLED : mWebVideo.setEnabled(_value.getBoolean()); break;
        
      default : super.setValue(_index-WEBCAM2D_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case WEBCAM2D_TRUE_SIZE : mImage.setTrueSize(false); break;
      case WEBCAM2D_POSITION  : mImage.getStyle().setRelativePosition(Style.CENTERED); break; 
      case WEBCAM2D_URL : mWebVideo.setURL(null); break;
      case WEBCAM2D_MJPEG : mWebVideo.setMJPEGFormat(false); break;
      case WEBCAM2D_DELAY : mWebVideo.setDelay(100); break;
      case WEBCAM2D_ENABLED : mWebVideo.setEnabled(false); break;
        
      default: super.setDefaultValue(_index-WEBCAM2D_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case WEBCAM2D_TRUE_SIZE : return "false";
      case WEBCAM2D_POSITION  : return "CENTERED";
      case WEBCAM2D_URL : return "<none>";
      case WEBCAM2D_MJPEG : return "false";
      case WEBCAM2D_DELAY : return "100";
      case WEBCAM2D_ENABLED : return "true";

      default : return super.getDefaultValueString(_index-WEBCAM2D_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case WEBCAM2D_TRUE_SIZE : 
      case WEBCAM2D_POSITION  : 
      case WEBCAM2D_URL : 
      case WEBCAM2D_MJPEG : 
      case WEBCAM2D_DELAY : 
      case WEBCAM2D_ENABLED : 
        return null;
      default: return super.getValue (_index-WEBCAM2D_ADDED);
    }
  }
  
//------------------------------------------------
//Implementation of WebVideoListener
//------------------------------------------------
 
 public void imageChanged(Image image) {
   mImage.setImage(image);
 }

 public void connectionError(String error) {
   ResourceBundle ejsRes = ResourceBundle.getBundle("org.colos.ejs.library.resources.ejs_res", Locale.getDefault());
   JOptionPane.showMessageDialog(getComponent(), error,getProperty("Name")+": "+ejsRes.getString("Error"),JOptionPane.ERROR_MESSAGE);
   mImage.setImage(null);
 }

} // End of class

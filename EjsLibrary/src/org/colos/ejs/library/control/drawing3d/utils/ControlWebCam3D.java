/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;

import java.awt.Image;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.drawing3d.ControlDrawingPanel3D;
import org.colos.ejs.library.control.value.*;
import org.colos.ejs.library.utils.WebVideo;
import org.colos.ejs.library.utils.WebVideoListener;
import org.opensourcephysics.drawing3d.DrawingPanel3D;


/**
 * An WebCam image interactive
 */
public class ControlWebCam3D extends ControlElement implements WebVideoListener, Control3DChild {
//  static protected final int WEBCAM_ADDED = 6; // Number of new properties
  static public final int WEBCAM_NAME    = 0; // The name of the element
  static public final int WEBCAM_PARENT  = 1; // The parent of the element
  static public final int WEBCAM_URL     = 2; // The URL of the element
  static public final int WEBCAM_MJPEG   = 3; // The MJPEG boolean
  static public final int WEBCAM_DELAY   = 4; // The delay between images
  static public final int WEBCAM_ENABLED = 5; // The enabled boolean

  private WebVideo mWebVideo;
  private DrawingPanel3D mPanel3D;

  /**
   * Constructor and utilities
   */
  public ControlWebCam3D() {
    super ();
    mWebVideo =  new WebVideo();
    mWebVideo.addListener(this);
    mPanel3D = null;
  }

  @Override
  public Object getObject() { return mWebVideo; }

  @Override
  public String getObjectClassname () { return "org.colos.ejs.library.utils.WebVideo"; }

  final private void setParent (ControlDrawingPanel3D _dp) {
//    System.out.println ("Setting parent of "+this+" to "+_dp);
    boolean isEnabled = mWebVideo.isEnabled();
    if (mPanel3D!=null) {
      mPanel3D.getVisualizationHints().setBackgroundImage((Image)null);
      mPanel3D = null;
      mWebVideo.setEnabled(false);
    }
    if (_dp!=null) {
      mPanel3D = _dp.getDrawingPanel3D();
      if (isEnabled) mWebVideo.setEnabled(true);
      mPanel3D.update();
    }
  }
    
  public void onExit() { mWebVideo.stopRunning(); }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("name");
      infoList.add ("parent");
      infoList.add ("url");
      infoList.add ("mjpeg");
      infoList.add ("delay");
      infoList.add ("enabled");
      // Does not inherit. 
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("name"))    return "String CONSTANT";
    if (_property.equals("parent"))  return "ControlElement CONSTANT";
    if (_property.equals("url"))      return "String";
    if (_property.equals("mjpeg"))    return "boolean";
    if (_property.equals("delay"))    return "int";
    if (_property.equals("enabled"))  return "boolean";
    return null;
  }

// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case WEBCAM_NAME :
        super.setValue (ControlElement.NAME,_value); 
        break;
      case WEBCAM_PARENT :
        {
          ControlElement parent = myGroup.getElement(getProperty("parent"));
          if (parent!=null) setParent (null);
          parent = myGroup.getElement(_value.toString());
          if (parent==null) System.err.println (getClass().getName()+" : Error! Parent <"+_value+"> not found for "+toString());
          else {
            if (parent instanceof ControlDrawingPanel3D) setParent ((ControlDrawingPanel3D) parent);
            else System.err.println (getClass().getName()+" : Error! Parent <"+_value+"> is not a ControlDrawingPanel3D");
          }
        }
        break;

      case  WEBCAM_URL : mWebVideo.setURL(_value.getString()); break;
      case  WEBCAM_MJPEG : mWebVideo.setMJPEGFormat(_value.getBoolean()); break;
      case  WEBCAM_DELAY : mWebVideo.setDelay(_value.getInteger()); break;
      case  WEBCAM_ENABLED : 
        mWebVideo.setEnabled(_value.getBoolean());
        mPanel3D.getVisualizationHints().setBackgroundImage((Image)null);
        break;
      
      default : break; // Do nothing. No inherited properties
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {

      case WEBCAM_NAME : super.setDefaultValue (ControlElement.NAME); break;
      case WEBCAM_PARENT : if (myGroup.getElement(getProperty("parent"))!=null) setParent (null); break;

      case  WEBCAM_URL : mWebVideo.setURL(null); break;
      case  WEBCAM_MJPEG : mWebVideo.setMJPEGFormat(false); break;
      case  WEBCAM_DELAY : mWebVideo.setDelay(100); break;
      case  WEBCAM_ENABLED :
        mWebVideo.setEnabled(false); 
//        mPanel3D.getVisualizationHints().setBackgroundImage((Image)null); Causes a null pointer exception
        break;

      default : break; // Do nothing. No inherited properties
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case WEBCAM_NAME : 
      case WEBCAM_PARENT :
        
      case WEBCAM_URL : 
        return "<none>";
      case WEBCAM_MJPEG : return "false";
      case WEBCAM_DELAY : return "100";
      case WEBCAM_ENABLED : return "true";
      
      default : return "<none>";
    }
  }
  
  public Value getValue (int _index) {
    return null;
  }

//------------------------------------------------
// Implementation of WebVideoListener
//------------------------------------------------
  
  public void imageChanged(Image image) {
    if (mPanel3D!=null) mPanel3D.getVisualizationHints().setBackgroundImage(image);
  }

  public void connectionError(String error) {
    ResourceBundle ejsRes = ResourceBundle.getBundle("org.colos.ejs.library.resources.ejs_res", Locale.getDefault());
    JOptionPane.showMessageDialog(mPanel3D.getComponent(), error,getProperty("Name")+": "+ejsRes.getString("Error"),JOptionPane.ERROR_MESSAGE);
  }
  


  
} // End of class



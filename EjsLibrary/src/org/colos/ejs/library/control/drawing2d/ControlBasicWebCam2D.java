/*
 * The control.display2d package contains subclasses of
 * control.ControlElement that deal with the display2d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing2d;

import java.awt.Dimension;
import java.awt.Image;
import java.util.Locale;
import java.util.ResourceBundle;
import org.colos.ejs.library.control.value.Value;
import javax.swing.JOptionPane;
import org.opensourcephysics.drawing2d.*;
import org.colos.ejs.library.control.NeedsFinalUpdate;
import org.colos.ejs.library.control.drawing2d.ControlImage2D;
import org.colos.ejs.library.utils.WebCamElement;
import org.colos.ejs.library.utils.WebVideoListener;
import javax.swing.event.ChangeEvent;

/**
 * A 2D Image obtained from a WebCam
 */
public class ControlBasicWebCam2D extends ControlImage2D implements WebVideoListener, NeedsFinalUpdate {
  static final private int WEBCAM2D_ADDED  = 12;
  static public final int WEBCAM2D_TRUE_SIZE = 0; // The URL of the element
  static public final int WEBCAM2D_POSITION  = 1; // The URL of the element
  static public final int WEBCAM2D_URL     = 2; // The URL of the element
  static public final int WEBCAM2D_DELAY   = 3; // The delay between images
  static public final int WEBCAM2D_ENABLED = 4; // The enabled boolean
  static public final int WEBCAM2D_RESOLUTION = 5; // The X resolution
 // static public final int WEBCAM2D_ACTION  = 6; // The action to perform when the image changes
  static public final int WEBCAM2D_FPS     = 6; // Number of frames per second (between 1 and 25)
  static public final int WEBCAM2D_USER    = 7; // The X resolution
  static public final int WEBCAM2D_PASS    = 8; // The X resolution
  static public final int WEBCAM2D_AUTOFPS = 9; // Auto-tune of FPS value
  static public final int WEBCAM2D_MAXAUTO = 10;//Max FPS in auto-tune mode
  static public final int WEBCAM2D_MINAUTO = 11;//Min FPS in auto-tune mode
  
  
  private boolean mEnabled = false;
  private WebCamElement mWebCamElement;
  @Override
  public Object getObject() { return mWebCamElement; }

  @Override
  public String getObjectClassname () { return "org.colos.ejs.library.utils.WebCamElement"; }
  
  protected org.opensourcephysics.display.Drawable createDrawable () {
	    super.createDrawable();
	    mWebCamElement =  new WebCamElement();
	    mWebCamElement.setEnabled(false);
	    mWebCamElement.addListener(this);
	    return mImage;
	  }

  public void stateChanged(ChangeEvent e) {
	    invokeActions();
	  }

  protected int getPropertiesDisplacement () { return WEBCAM2D_ADDED; }

  public void onExit() { mWebCamElement.stop(); }
 
//------------------------------------------------
//Definition of Properties
//------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("trueSize");
      infoList.add ("elementposition");
      infoList.add ("url");
      infoList.add ("delay");
      infoList.add ("enabled");
      infoList.add ("resolution");
      infoList.add ("fps");
      infoList.add ("username");
      infoList.add ("password");
      infoList.add ("autotunefps");
      infoList.add ("maxautofps");
      infoList.add ("minautofps");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }
  
  public String getPropertyInfo(String _property) {
	    if (_property.equals("trueSize")) return "boolean";
	    if (_property.equals("elementposition"))return "ElementPosition|int";
	    if (_property.equals("url"))      return "String";
	    if (_property.equals("delay"))    return "int";
	    if (_property.equals("enabled"))  return "boolean";
	    if (_property.equals("resolution"))  return "Dimension|Object|String";
	    if (_property.equals("fps"))  return "int";
	    if (_property.equals("username"))  return "String";
	    if (_property.equals("password"))  return "String";
	    if (_property.equals("autotunefps"))  return "boolean";
	    if (_property.equals("maxautofps"))  return "int";
	    if (_property.equals("minautofps"))  return "int";
	    return super.getPropertyInfo(_property);
	  }
  
//------------------------------------------------
//Set and Get the values of the properties
//------------------------------------------------

  public void setValue (int _index, Value _value) {
	    switch (_index) {
	      case WEBCAM2D_TRUE_SIZE :{
	    	  if (_value.getBoolean()!=mImage.isTrueSize()) mImage.setTrueSize(_value.getBoolean()); 
	    	  break;
	      }
	      case WEBCAM2D_POSITION  :{
	    	  if (_value.getInteger()!=mImage.getStyle().getRelativePosition()) mImage.getStyle().setRelativePosition(_value.getInteger());
	    	  break; 
	      }
	      case WEBCAM2D_URL : {mWebCamElement.setURL(_value.getString());break;}
	      case WEBCAM2D_DELAY :{mWebCamElement.setDelay(_value.getInteger());break;}
	      case WEBCAM2D_ENABLED : {mEnabled = _value.getBoolean();break;}
	      case WEBCAM2D_RESOLUTION : {
	          if (_value.getString()==null) {
	            System.err.println ("Size invalid for element ");
	            return;
	          }
	          String value = _value.getString();
	          int in = value.indexOf(',');
	          int in2 = value.indexOf('{');
	          int in3 = value.indexOf('}');
	          if (in2 < 0)	in2 = 0;
	          else 	in2 = in2+1;
	          if (in3 <0)	in3 = value.length();
	          int w = Integer.valueOf(value.substring(in2, in));
	          int h = Integer.valueOf(value.substring(in+1, in3));
	          Dimension size = new Dimension(w,h);
	          System.err.println ("Sizes W: " + w + "H : " + h);
	          mWebCamElement.setResolution(size.width,size.height); 
	    	  break;
	        }
	     case WEBCAM2D_FPS: {mWebCamElement.setFPS(_value.getInteger());break;}
	     case WEBCAM2D_USER : mWebCamElement.setUsername(_value.getString()); break;
	     case WEBCAM2D_PASS : mWebCamElement.setPassword(_value.getString()); break;
	     case WEBCAM2D_AUTOFPS: mWebCamElement.setAutoFPS(_value.getBoolean()); break;
	     case WEBCAM2D_MAXAUTO: mWebCamElement.setMaxAutoFPS(_value.getInteger()); break;
	     case WEBCAM2D_MINAUTO: mWebCamElement.setMinAutoFPS(_value.getInteger()); break;
	     default : {
	    	  super.setValue(_index-WEBCAM2D_ADDED,_value);
	    	  break;
	     }
	  }
  }
  
  public void setDefaultValue (int _index) {
	    switch (_index) {
	      case WEBCAM2D_TRUE_SIZE : mImage.setTrueSize(false); break;
	      case WEBCAM2D_POSITION  : mImage.getStyle().setRelativePosition(Style.CENTERED); break; 
	      case WEBCAM2D_URL : mWebCamElement.setURL(null); break;
	      case WEBCAM2D_DELAY : mWebCamElement.setDelay(100); break;
	      case WEBCAM2D_ENABLED :{
	          mWebCamElement.setEnabled(false);
	    	  mEnabled = false; 
	    	  break;
	      }
	      case WEBCAM2D_RESOLUTION :{mWebCamElement.setResolution(640,480);break;} 
	      case WEBCAM2D_FPS: mWebCamElement.setFPS(10); break;
	      case WEBCAM2D_USER : mWebCamElement.setUsername(""); break;
	      case WEBCAM2D_PASS : mWebCamElement.setPassword(""); break;
	      case WEBCAM2D_AUTOFPS: mWebCamElement.setAutoFPS(false); break;
	      case WEBCAM2D_MAXAUTO: mWebCamElement.setMaxAutoFPS(15); break;
		  case WEBCAM2D_MINAUTO: mWebCamElement.setMinAutoFPS(15); break;
	      default: super.setDefaultValue(_index-WEBCAM2D_ADDED); break;
	    }
	  }
  public String getDefaultValueString (int _index) {
	    switch (_index) {
	      case WEBCAM2D_TRUE_SIZE : return "false";
	      case WEBCAM2D_POSITION  : return "CENTERED";
	      case WEBCAM2D_URL : return "<none>";
	      case WEBCAM2D_DELAY : return "100";
	      case WEBCAM2D_ENABLED : return "false";
	      case WEBCAM2D_RESOLUTION: return "\"640,480\"";
	      case WEBCAM2D_FPS: return "10";
	      case WEBCAM2D_USER : return "<none>";
	      case WEBCAM2D_PASS : return "<none>";
	      case WEBCAM2D_AUTOFPS: return "false";
	      case WEBCAM2D_MAXAUTO:  return "15";
		  case WEBCAM2D_MINAUTO: return "15";
	      default : return super.getDefaultValueString(_index-WEBCAM2D_ADDED);
	    }
	  }
  public Value getValue (int _index) {
	    switch (_index) {
	      case WEBCAM2D_TRUE_SIZE : 
	      case WEBCAM2D_POSITION  : 
	      case WEBCAM2D_URL : 
	      case WEBCAM2D_DELAY : 
	      case WEBCAM2D_ENABLED : 
	      case WEBCAM2D_RESOLUTION:
	      case WEBCAM2D_FPS: 
	      case WEBCAM2D_USER :
	      case WEBCAM2D_PASS :
	      case WEBCAM2D_AUTOFPS:
	      case WEBCAM2D_MAXAUTO:
		  case WEBCAM2D_MINAUTO:
	        return null;
	      default: return super.getValue (_index-WEBCAM2D_ADDED);
	    }
	  }
  
//
/// Updating images
//
  
  public synchronized void imageChanged(Image image) {
	 try{
		mImage.setImage(image);
	 }catch(Exception e){
		mImage.setImage(null);
		mWebCamElement.setEnabled(false);
		mWebCamElement.stop();
		System.err.println("Error settings Images: High FPS rate or slow connection");
		System.err.println("...Stopping");
	 }
	 /*
   	 System.err.println(" The Image H : " + image.getHeight(getComponent()) + " " + image.getWidth(getComponent()));
   	 System.err.println("_________________________________________________");
 	*/
  }
 
 
  public void connectionError(String error) {
	   ResourceBundle ejsRes = ResourceBundle.getBundle("org.colos.ejs.library.resources.ejs_res", Locale.getDefault());
	   JOptionPane.showMessageDialog(getComponent(), error,getProperty("Name")+": "+ejsRes.getString("Error"),JOptionPane.ERROR_MESSAGE);
	   mImage.setImage(null);
  }

  @Override
  public void finalUpdate() {
		if (!isUnderEjs)  mWebCamElement.setEnabled(mEnabled);
  }

} // End of BasicWebCamElement class

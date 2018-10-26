/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;

import java.awt.Dimension;
import java.util.StringTokenizer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.NeedsFinalUpdate;
import org.colos.ejs.library.control.drawing3d.ControlDrawingPanel3D;
import org.colos.ejs.library.control.swing.ConstantParser;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.drawing3d.DrawingPanel3D;

import es.uhu.augmented_reality.AbstractElementAR;


/**
 * An WebCam image interactive
 */
public abstract class ControlARSystem3DAbstract extends ControlElement implements ChangeListener, NeedsFinalUpdate, Control3DChild {
  static public final int AR_NAME    = 0; // The name of the element
  static public final int AR_PARENT  = 1; // The parent of the element
  static public final int AR_RESOLUTION = 2; // The X resolution
  static public final int AR_CONFIG  = 3; // The X resolution
  static public final int AR_ENABLED = 4; // The enabled boolean
  static public final int AR_ACTION  = 5; // The action to perform when the image changes
  static public final int AR_MARKERS = 6; // A comma-separated list of markers and sizes (e.g. "EJS:76,UHU:50,myMarker.patt:50")
  static public final int AR_FPS     = 7; // Number of frames per second (between 1 and 25)

  protected AbstractElementAR mARSystem;
  protected boolean mEnabled;
  private String mMarkers=null;

  /**
   * Constructor and utilities
   */
  public ControlARSystem3DAbstract() {
    super ();
    mARSystem =  createElementAR();
    mARSystem.setEnabled(mEnabled=false);
    mARSystem.setChangeListener(this);
  }

  abstract protected AbstractElementAR createElementAR();
  
  public void stateChanged(ChangeEvent e) {
    invokeActions();
  }
  
  final private void setParent (ControlDrawingPanel3D _dp) {
//    System.out.println ("Setting parent of "+this+" to "+_dp);
    mARSystem.setDrawingPanel3D(null);
    if (_dp!=null) {
      DrawingPanel3D panel = _dp.getDrawingPanel3D();
      mARSystem.setDrawingPanel3D(panel);
      panel.update();
    }
  }
    
  public void onExit() { mARSystem.dispose(); }
  

  public void finalUpdate() {
    if (!isUnderEjs) mARSystem.setEnabled(mEnabled); // This must be always last
  }
  
// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("name");
      infoList.add ("parent");
      infoList.add ("resolution");
      infoList.add ("configuration");
      infoList.add ("enabled");
      infoList.add ("action");
      infoList.add ("markers");
      infoList.add ("fps");

      // Does not inherit. 
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("name"))     return "String CONSTANT";
    if (_property.equals("parent"))   return "ControlElement CONSTANT";
    if (_property.equals("resolution")) return "Dimension|Object|String";
    if (_property.equals("configuration"))   return "File|String";
    if (_property.equals("enabled"))  return "boolean";
    if (_property.equals("action"))   return "Action CONSTANT";
    if (_property.equals("markers"))  return "String";
    if (_property.equals("fps"))      return "int";

    return null;
  }

   public Value parseConstant (String _propertyType, String _value) {
     if (_value==null) return null;
     Value constantValue;
     if (_propertyType.indexOf("Dimension")>=0) {
       constantValue = ConstantParser.dimensionConstant(_value);
       if (constantValue!=null) return constantValue;
     }
     return super.parseConstant(_propertyType, _value);
   }
   
  private void setMarkers(String markers) {
    if (markers==null) {
      mARSystem.removeAllMarkers(); 
      mMarkers = null;
    }
    else if (!markers.equals(mMarkers)) {
      mMarkers = markers;
      mARSystem.removeAllMarkers(); 
      StringTokenizer tkn = new StringTokenizer(markers,";,");
      while (tkn.hasMoreTokens()) {
        String token = tkn.nextToken().trim();
        if (token.length()<=0) continue;
        int index = token.indexOf(':');
        if (index<0) {
          System.err.println("ARSystem Error: marker format incorrect (name:width): "+token);
        }
        else {
          String name = token.substring(0,index);
          int width = Integer.parseInt(token.substring(index+1));
          if (name.indexOf('.')<0) name = AbstractElementAR.getPredefinedMarker(name);
          mARSystem.addMarker(name, width);
//          System.out.println("ARSystem: marker added: "+name+":"+width);
        }
      }
    }
  }
  
// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case AR_NAME :
        super.setValue (ControlElement.NAME,_value); 
        break;
      case AR_PARENT :
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

      case AR_RESOLUTION : 
        if (_value.getObject()==null) {
          System.err.println ("Size invalid for element "+getProperty("name"));
          return;
        }
        Dimension size = (Dimension) _value.getObject();
        mARSystem.setResolution(size.width,size.height); 
        break;
      case AR_CONFIG : mARSystem.setConfigurationFile(_value.getString()); break;

      case AR_ENABLED : mEnabled = _value.getBoolean(); break;
      
      case AR_ACTION : 
        removeAction (ControlElement.ACTION,getProperty("action"));
        addAction(ControlElement.ACTION,_value.getString());
        break;

      case AR_MARKERS: setMarkers(_value.getString()); break;
      case AR_FPS: mARSystem.setFPS(_value.getInteger()); break;
        
      default : break; // Do nothing. No inherited properties
    }
  }


  public void setDefaultValue (int _index) {
    switch (_index) {
      case AR_NAME : super.setDefaultValue (ControlElement.NAME); break;
      case AR_PARENT : if (myGroup.getElement(getProperty("parent"))!=null) setParent (null); break;

      case AR_RESOLUTION : mARSystem.setResolution(640,480); break;
      case AR_CONFIG : mARSystem.setConfigurationFile(null); break;

      case AR_ENABLED : mEnabled = false; break;
      case AR_ACTION : removeAction (ControlElement.ACTION,getProperty("action")); break;

      case AR_MARKERS:  setMarkers(null); break;
      case AR_FPS: mARSystem.setFPS(10); break;

      default : break; // Do nothing. No inherited properties
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case AR_NAME : 
      case AR_PARENT :

      case  AR_RESOLUTION : return "\"640,480\"";
      case  AR_CONFIG : 

      case AR_ENABLED : return "false";
      case AR_ACTION : return "<no_action>";

      case AR_MARKERS: return "<none>";
      case AR_FPS: return "10";

      default : return "<none>";
    }
  }
  
  public Value getValue (int _index) {
    return null;
  }

} // End of class

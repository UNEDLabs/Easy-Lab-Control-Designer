/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawing3d.utils;


import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.utils.TransformationWrapper;
import org.opensourcephysics.numerics.*;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.drawing3d.ControlElement3D;
import org.colos.ejs.library.control.value.*;

/**
 * Superclass for 3D transformations of elements (children of ControlElements3D)
 */
abstract public class ControlTransformation3D extends ControlElement implements TransformationWrapper {
  static public final int TR3D_NAME    = 0; // The name of the element
  static public final int TR3D_PARENT  = 1; // The parent of the element
  static public final int TR3D_ENABLED = 2;

  protected boolean enabled;
  protected Transformation transformation;
  protected ControlElement3D myParent;

  /**
   * Constructor and utilities
   */
  protected ControlTransformation3D() {
    super ();
    transformation = createTransformation();
    myParent = null;
    enabled = true;
  }

  /**
   * This method actually creates the Transformation
   * Must be overrriden
   */
  abstract protected Transformation createTransformation ();

  @Override
  public Object getObject() { return transformation; }

  @Override
  public String getObjectClassname () { return "org.opensourcephysics.numerics.Transformation"; }

  final public void setParent (ControlElement3D _dp) {
//    System.out.println ("Setting parent of "+this+" to "+_dp);
    if (myParent!=null) {
      myParent.getElement().removeSecondaryTransformation(this);
      setAffectedElement(null);
    }
    if (_dp!=null) {
      myParent = _dp;
      String indexInParent = getProperty("_ejs_indexInParent_");
      int index = -1;
      if (indexInParent!=null) index = Integer.parseInt(indexInParent);
      setProperty("_ejs_indexInParent_",null);
      if (index>=0) myParent.getElement().addSecondaryTransformation(this,index);
      else myParent.getElement().addSecondaryTransformation(this);
      setAffectedElement(myParent.getElement());
      myParent.updatePanel();
    }
  }
  
  protected void setAffectedElement(Element element) {};
  
  final public ControlElement3D getParent () { return myParent; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() { // This eliminates any previous property
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("name");
      infoList.add ("parent");
      infoList.add ("enabled");
      // Does not inherit. 
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("name"))    return "String CONSTANT";
    if (_property.equals("parent"))  return "ControlElement CONSTANT";
    if (_property.equals("enabled"))  return "boolean";
    return null;
  }

// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case TR3D_NAME :
          super.setValue (ControlElement.NAME,_value); 
          break;
      case TR3D_PARENT :
        {
          ControlElement parent = myGroup.getElement(getProperty("parent"));
          if (parent!=null) setParent (null);
          parent = myGroup.getElement(_value.toString());
          if (parent==null) System.err.println (getClass().getName()+" : Error! Parent <"+_value+"> not found for "+toString());
          else {
            if (parent instanceof ControlElement3D) setParent ((ControlElement3D) parent);
            else System.err.println (getClass().getName()+" : Error! Parent <"+_value+"> is not a ControlElements3DParent");
          }
        }
        break;
      case TR3D_ENABLED : 
        enabled = _value.getBoolean(); 
        if (myParent!=null) myParent.getElement().addChange(Element.CHANGE_TRANSFORMATION); 
        break;
      default : break; // Do nothing. No inherited properties
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case TR3D_NAME : 
        super.setDefaultValue (ControlElement.NAME); 
        break;
      case TR3D_PARENT : if (myGroup.getElement(getProperty("parent"))!=null) setParent (null); break;
      case TR3D_ENABLED : 
        enabled = true; 
        if (myParent!=null) myParent.getElement().addChange(Element.CHANGE_TRANSFORMATION); 
        break;
      default : break; // Do nothing. No inherited properties
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case TR3D_NAME : 
      case TR3D_PARENT : return "<none>";
      case TR3D_ENABLED : return "true";
      default : return "<none>";
    }
  }
  
  public Value getValue (int _index) {
    return null;
  }
  
//------------------------------------------------
// Implementation of TransformationWrapper
//------------------------------------------------

  final public boolean isEnabled() { return enabled; }
  
  final public void setEnabled(boolean _enabled) { this.enabled = _enabled; }

  final public Transformation getTransformation() { return transformation; }

  abstract public Object clone();
  
} // End of interface

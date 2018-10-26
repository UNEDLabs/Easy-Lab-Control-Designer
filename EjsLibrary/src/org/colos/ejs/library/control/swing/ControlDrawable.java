/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) Dec 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.display.Drawable;

/**
 * Abstract superclass for Drawables (children of ControlDrawableParent)
 */
public abstract class ControlDrawable extends ControlElement {
  public static final int DRAWABLE_NAME    = 0; // The name of the element
  public static final int PARENT  = 1; // The parent of the element

  protected ControlParentOfDrawables myParent;
  private Drawable myDrawable = null; // change with care
  private String menuNameEntry=null;

  /**
   * Constructor and utilities
   */
  public ControlDrawable() {
    super ();
    myObject = myDrawable = createDrawable();
  }

  protected abstract Drawable createDrawable ();

  protected void setName (String _name) {}; // To be overwritten

  final public Drawable getDrawable () { return myDrawable; }
  // use with care. Some may need it (like ByteRaster f.i.)

  final public void replaceDrawable (Drawable _dr) {
    if (myParent!=null) {
      myParent.getDrawingPanel().replaceDrawable(myDrawable, _dr);
    }
    myObject = myDrawable = _dr; 
  }

  // This one is not final on purpose
  public void setParent (final ControlParentOfDrawables _dp) {
    if (myParent!=null) {
      myParent.removeDrawable(myDrawable);
      if (this instanceof NeedsPreUpdate) myParent.removeFromPreupdateList((NeedsPreUpdate)this);
    }
    if (_dp!=null) {
      int index = -1;
      String indexInParent = getProperty("_ejs_indexInParent_");
      if (indexInParent!=null) index = Integer.parseInt(indexInParent);
      setProperty("_ejs_indexInParent_",null);
      if (index>=0) _dp.addDrawableAtIndex(index,myDrawable);
      else _dp.addDrawable(myDrawable);
      if (isUnderEjs) _dp.getDrawingPanel().render();
      if (this instanceof NeedsPreUpdate) _dp.addToPreupdateList((NeedsPreUpdate)this);
      myParent = _dp;
    }
  }
  final public ControlParentOfDrawables getParent () { return myParent; }
  
  public void destroy () {
    super.destroy();
    if (myParent instanceof ControlDrawablesParent) ((ControlDrawablesParent) myParent).getDrawingPanel().render();
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
      infoList.add ("menuName");
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("name"))     return "String CONSTANT";
    if (_property.equals("parent"))   return "ControlElement CONSTANT";
    if (_property.equals("menuName")) return "String TRANSLATABLE";
    return null;
  }

  public String getMenuNameEntry() {
    if (menuNameEntry!=null) {
      if (menuNameEntry.equals("null")) return null;
      return menuNameEntry;
    }
    return this.getProperty("name");
  }

// ------------------------------------------------
// Variables
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case DRAWABLE_NAME : setName (_value.getString()); super.setValue (ControlElement.NAME,_value); break;
      case PARENT :
        {
          ControlElement parent = myGroup.getElement(getProperty("parent"));
          if (parent!=null) setParent (null);
          parent = myGroup.getElement(_value.toString());
          if (parent==null) System.err.println (getClass().getName()+" : Error! Parent <"+_value+"> not found for "+toString());
          else {
            if (parent instanceof ControlParentOfDrawables) setParent ((ControlParentOfDrawables) parent);
            else System.err.println (getClass().getName()+" : Error! Parent <"+_value+"> is not a ControlParentOfDrawables");
          }
        }
        break;
      case 2 : setName (menuNameEntry = _value.getString()); break;
      default : // Do nothing. No inherited properties
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case DRAWABLE_NAME : setName (""); super.setDefaultValue (ControlElement.NAME); break;
      case PARENT :
        {
          ControlElement parent = myGroup.getElement(getProperty("parent"));
          if (parent!=null) setParent (null);
        }
        break;
      case 2 : menuNameEntry = null; break;
      default : break;
    }
  }


  public String getDefaultValueString (int _index) {
    switch (_index) {
      case DRAWABLE_NAME : return "";
      case PARENT : return "null";
      case 2 : return this.getProperty("name");
      default : return super.getDefaultValueString(_index-3);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      default: return null;
    }
  }

} // End of class


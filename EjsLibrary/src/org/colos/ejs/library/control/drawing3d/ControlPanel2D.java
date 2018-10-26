/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;


import java.awt.Color;
import java.util.Vector;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.NeedsFinalUpdate;
import org.colos.ejs.library.control.NeedsUpdate;
import org.colos.ejs.library.control.swing.ControlDrawable;
import org.colos.ejs.library.control.swing.ControlParentOfDrawables;
import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.drawing2d.DrawingPanel2D;
import org.opensourcephysics.drawing3d.Element;
import org.opensourcephysics.drawing3d.ElementPanel2D;


public class ControlPanel2D extends ControlElement3D implements NeedsUpdate, NeedsFinalUpdate, ControlParentOfDrawables {
  static final private int PANEL2D_PROPERTIES_ADDED=16;

  private ElementPanel2D panel2D;
  private DrawingPanel2D dPanel;
  protected Vector<NeedsPreUpdate> preupdateList = new Vector<NeedsPreUpdate>();
  protected double minX, maxX,minY, maxY;
  protected boolean autoX, autoY;
  protected boolean xminSet=false, xmaxSet=false, yminSet=false, ymaxSet=false;
  private java.awt.Rectangle myGutters=null;
  protected Color myDefaultBkgd;


  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.ElementPanel2D"; }

  protected Element createElement () {
    panel2D = new ElementPanel2D();
    dPanel = panel2D.getDrawingPanel();
    dPanel.enableInspector (false); // OSP Update
    dPanel.setSquareAspect (false);
    dPanel.setBuffered(true);
    dPanel.removeOptionController();
    minX = Double.NaN; // drawingPanel.getXMin();
    maxX = Double.NaN; // drawingPanel.getXMax();
    minY = Double.NaN; // drawingPanel.getYMin();
    maxY = Double.NaN; // drawingPanel.getYMax();
    autoX = dPanel.isAutoscaleX();
    autoY = dPanel.isAutoscaleY();
    myDefaultBkgd = dPanel.getBackground();

    return panel2D;
  }

  protected int getPropertiesDisplacement () { return PANEL2D_PROPERTIES_ADDED; }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("autoscaleX");
      infoList.add ("autoscaleY");
      infoList.add ("minimumX");
      infoList.add ("maximumX");
      infoList.add ("minimumY");
      infoList.add ("maximumY");
      infoList.add ("square");
      infoList.add ("gutters");
      infoList.add ("xMarginPercentage");
      infoList.add ("yMarginPercentage");
      infoList.add ("TLmessage");
      infoList.add ("TRmessage");
      infoList.add ("BLmessage");
      infoList.add ("BRmessage");
      infoList.add ("aliasing");
      infoList.add ("background");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("autoscaleX"))     return "boolean";
    if (_property.equals("autoscaleY"))     return "boolean";
    if (_property.equals("minimumX"))       return "int|double";
    if (_property.equals("maximumX"))       return "int|double";
    if (_property.equals("minimumY"))       return "int|double";
    if (_property.equals("maximumY"))       return "int|double";
    if (_property.equals("square"))         return "boolean";
    if (_property.equals("gutters"))        return "Gutters|Object";
    if (_property.equals("xMarginPercentage"))       return "int|double";
    if (_property.equals("yMarginPercentage"))       return "int|double";
    if (_property.equals("TLmessage"))   return "String";
    if (_property.equals("TRmessage"))   return "String";
    if (_property.equals("BLmessage"))   return "String";
    if (_property.equals("BRmessage"))   return "String";
    if (_property.equals("aliasing")) return "boolean";
    if (_property.equals("background")) return "int|Color|Object";

    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :  autoX = _value.getBoolean(); updateAutoscale(); break;
      case 1 :  autoY = _value.getBoolean(); updateAutoscale(); break;
      case 2 : if (_value.getDouble()!=minX || !xminSet) { minX=_value.getDouble(); xminSet = true; updateExtrema(); } break;
      case 3 : if (_value.getDouble()!=maxX || !xmaxSet) { maxX=_value.getDouble(); xmaxSet = true; updateExtrema(); } break;
      case 4 : if (_value.getDouble()!=minY || !yminSet) { minY=_value.getDouble(); yminSet = true; updateExtrema(); } break;
      case 5 : if (_value.getDouble()!=maxY || !ymaxSet) { maxY=_value.getDouble(); ymaxSet = true; updateExtrema(); } break;
      case 6 : dPanel.setSquareAspect(_value.getBoolean());    break;
      case 7 :
        if (_value.getObject() instanceof java.awt.Rectangle) {
          java.awt.Rectangle rect = (java.awt.Rectangle) _value.getObject();
          if (rect!=myGutters) {
            dPanel.setGutters(rect.x,rect.y,rect.width,rect.height);
            myGutters = rect;
          }
        }
        break;
      case 8 : dPanel.setXMarginPercentage(_value.getDouble()); break;
      case 9 : dPanel.setYMarginPercentage(_value.getDouble()); break;

      case 10 : dPanel.setMessage(_value.getString(),DrawingPanel.TOP_LEFT); break;
      case 11 : dPanel.setMessage(_value.getString(),DrawingPanel.TOP_RIGHT); break;
      case 12 : dPanel.setMessage(_value.getString(),DrawingPanel.BOTTOM_LEFT); break;
      case 13 : dPanel.setMessage(_value.getString(),DrawingPanel.BOTTOM_RIGHT); break;

      case 14 : 
        boolean on = _value.getBoolean();
        dPanel.setAntialiasTextOn(on);
        dPanel.setAntialiasShapeOn(on);
        break;
        
      case 15 :
        if (_value.getObject() instanceof Color) dPanel.setBackground((Color) _value.getObject()); 
        else dPanel.setBackground(DisplayColors.getLineColor(_value.getInteger()));
        break;

      default: super.setValue(_index-PANEL2D_PROPERTIES_ADDED,_value); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public void setDefaultValue (int _index) {
    switch (_index) {

      case 0 :  autoX = false; updateAutoscale(); break;
      case 1 :  autoY = false; updateAutoscale(); break;
      case 2 : minX=Double.NaN; xminSet = false; updateExtrema(); break;
      case 3 : maxX=Double.NaN; xmaxSet = false; updateExtrema(); break;
      case 4 : minY=Double.NaN; yminSet = false; updateExtrema(); break;
      case 5 : maxY=Double.NaN; ymaxSet = false; updateExtrema(); break;
      case 6 : dPanel.setSquareAspect(false); break;
      case 7 : dPanel.setGutters(0,0,0,0); myGutters = null; break;
      case 8 : dPanel.setXMarginPercentage(0.0); break;
      case 9 : dPanel.setYMarginPercentage(0.0); break;

      case 10 : dPanel.setMessage("",DrawingPanel.TOP_LEFT); break;
      case 11 : dPanel.setMessage("",DrawingPanel.TOP_RIGHT); break;
      case 12 : dPanel.setMessage("",DrawingPanel.BOTTOM_LEFT); break;
      case 13 : dPanel.setMessage("",DrawingPanel.BOTTOM_RIGHT); break;

      case 14 : 
        dPanel.setAntialiasTextOn(false);
        dPanel.setAntialiasShapeOn(false);
        break;
        
      case 15 : dPanel.setBackground(this.myDefaultBkgd); break;
      
      default: super.setDefaultValue(_index-PANEL2D_PROPERTIES_ADDED); break;
    }
    if (isUnderEjs) updatePanel();
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      
      case 0 : 
      case 1 : return "false";
      case 2 : 
      case 3 : 
      case 4 : 
      case 5 : return "<none>";
      case 6 : return "false";
      case 7 : return "0,0,0,0";
      case 8 : 
      case 9 : return "0.0";
      case 10 : 
      case 11 : 
      case 12 : 
      case 13 : return "<none>";
      case 14 : return "false";
      case 15 : return "<none>";

      default : return super.getDefaultValueString(_index-PANEL2D_PROPERTIES_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : case  4 : case  5 :
      case 6 : case 7 : case 8 : case 9 : case 10 : case 11 : 
      case 12 : case 13 : case 14 : case 15 :
        return null;

      default: return super.getValue (_index-PANEL2D_PROPERTIES_ADDED);
    }
  }

  
  // -----------------------------
  // 
  
  public boolean acceptsChild (ControlElement _child) {
     if (_child instanceof ControlDrawable) return true;
	   return false;
  }

  public void addDrawable(Drawable _drawable) { 
//    System.out.println ("Adding drawable "+_drawable);
    dPanel.addDrawable(_drawable);
    panel2D.refresh();
  }

  public void addDrawableAtIndex(int _index, Drawable _drawable) { 
//    System.out.println ("Adding drawable ("+_index+")"+_drawable);
    dPanel.addDrawableAtIndex(_index, _drawable);
    panel2D.refresh();
  }

  public void addToPreupdateList(NeedsPreUpdate _child) { 
//    System.out.println ("Adding preupdate child "+_child);
    preupdateList.add(_child); 
  }

  public org.opensourcephysics.display.DrawingPanel getDrawingPanel() { return dPanel;}

  public void removeDrawable(Drawable _drawable) { 
//    System.out.println ("Removing drawable "+_drawable);
	  dPanel.removeDrawable(_drawable);
	  panel2D.refresh();
  }

  public void removeFromPreupdateList(NeedsPreUpdate _child) { 
//    System.out.println ("Removing preupdate child "+_child);
    preupdateList.remove(_child); 
  }

  
  public void update() { // Ensure it will be updated
    // prepare children that need to do something
    for (NeedsPreUpdate npu : preupdateList) npu.preupdate();
//    System.out.println ("panel update...");
    panel2D.refresh();
  }
  
  public void finalUpdate() {
//    if (myGroup!=null && myGroup.isCollectingData()) return;
//    System.out.println ("panel final udpate...");
    panel2D.refresh();
  }
  
  // ----------------------------
  
  private void updateAutoscale () {
      dPanel.setAutoscaleX(autoX);
      dPanel.setAutoscaleY(autoY);
      updateExtrema(); 
  }
  
  private void updateExtrema () {
    if (dPanel.isAutoscaleX()) {
      if (xminSet || xmaxSet) dPanel.limitAutoscaleX(minX, maxX);
    }
    else {
      dPanel.setPreferredMinMaxX(minX,maxX);
    }
    if (dPanel.isAutoscaleY()) {
      if (yminSet || ymaxSet) dPanel.limitAutoscaleY(minY, maxY);
    }
    else dPanel.setPreferredMinMaxY(minY,maxY);
    if (isUnderEjs) dPanel.render();
}

  
} // End of class

/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.border.*;

/**
 * A configurable panel. It has no internal value, nor can trigger
 * any action.
 */
public class ControlPanel extends ControlContainer {
  static public final int PANEL_ADDED=7;

  static public final int EMPTY_BORDER  = 0;
  static public final int LOWERED_BEVEL_BORDER  = 1;
  static public final int RAISED_BEVEL_BORDER   = 2;
  static public final int LOWERED_ETCHED_BORDER = 3;
  static public final int RAISED_ETCHED_BORDER  = 4;
  static public final int LINE_BORDER           = 5;
  static public final int ROUNDED_LINE_BORDER   = 6;
  static public final int MATTE_BORDER          = 7;
  static public final int TITLED_BORDER         = 8;
  static public final int ROUNDED_TITLED_BORDER = 9;

  private JPanel panel;
  private java.awt.LayoutManager myLayout=null;
  private java.awt.Rectangle     myBorder=new java.awt.Rectangle(0,0,0,0);
  private int borderType = ControlPanel.EMPTY_BORDER;
  private Color borderColor=Color.BLACK;
  private String borderTitle="Title";
  private int borderPosition=TitledBorder.DEFAULT_POSITION;
  private int borderJustification=TitledBorder.DEFAULT_JUSTIFICATION;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    panel = new JPanel();
    panel.addKeyListener (
        new java.awt.event.KeyAdapter() {
          public void keyPressed  (java.awt.event.KeyEvent _e) {
            if (_e.isControlDown() && getSimulation()!=null) {
              if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_M) getPopupMenu(0,0);
              else if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_P) printScreen();
            }
          }
        }
      );
    panel.addMouseListener (
        new MouseAdapter() {
          public void mousePressed  (MouseEvent _evt) {
            if (getSimulation()!=null && org.opensourcephysics.display.OSPRuntime.isPopupTrigger(_evt)) { //) {SwingUtilities.isRightMouseButton(_evt) 
              getPopupMenu(_evt.getX(),_evt.getY());
            }
          }
        }
      );
    return panel;
  }

  private void setBorder () {
    Border border;
    switch (borderType) {
      default :
      case EMPTY_BORDER:          border = new EmptyBorder(myBorder.x,myBorder.y,myBorder.width,myBorder.height); break;
      case LOWERED_BEVEL_BORDER:  border = new BevelBorder(BevelBorder.LOWERED); break;
      case RAISED_BEVEL_BORDER:   border = new BevelBorder(BevelBorder.RAISED); break;
      case LOWERED_ETCHED_BORDER: border = new EtchedBorder(EtchedBorder.LOWERED); break;
      case RAISED_ETCHED_BORDER:  border = new EtchedBorder(EtchedBorder.RAISED); break;
      case ROUNDED_LINE_BORDER:
      case LINE_BORDER:
        int size = Math.max(Math.max(Math.max(Math.max(myBorder.x,myBorder.y),myBorder.width),myBorder.height),1);
        border = new LineBorder(borderColor,size,borderType==ROUNDED_LINE_BORDER);
        break;
      case MATTE_BORDER:          border = new MatteBorder(myBorder.x,myBorder.y,myBorder.width,myBorder.height,borderColor); break;
      case ROUNDED_TITLED_BORDER:
      case TITLED_BORDER:
        int size2 = Math.max(Math.max(Math.max(Math.max(myBorder.x,myBorder.y),myBorder.width),myBorder.height),1);
        border = new TitledBorder(new LineBorder(borderColor,size2,borderType==ROUNDED_TITLED_BORDER),
                                  borderTitle,borderJustification,borderPosition,panel.getFont(),panel.getForeground());
    }
    panel.setBorder(border);
  }

  public ControlElement setProperty (String _property, String _value, boolean _store) {
    ControlElement returnValue = super.setProperty (_property,_value,_store);
    if (_property.equals("font") || _property.equals("foreground")) setBorder();
    return returnValue;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("layout");
      infoList.add ("border");
      infoList.add ("borderType");
      infoList.add ("borderColor");
      infoList.add ("borderTitle");
      infoList.add ("borderPosition");
      infoList.add ("borderJustification");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("layout"))         return "Layout|Object NO_RESET";
    if (_property.equals("border"))         return "Margins|Object";
    if (_property.equals("borderType"))     return "BorderType|int";
    if (_property.equals("borderColor"))    return "Color|Object";
    if (_property.equals("borderTitle"))    return "String TRANSLATABLE";
    if (_property.equals("borderPosition")) return "BorderPosition|int";
    if (_property.equals("borderJustification")) return "Alignment|int";
    return super.getPropertyInfo(_property);
  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("BorderType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("empty"))         return new IntegerValue (ControlPanel.EMPTY_BORDER);
      if (_value.equals("lowered_bevel")) return new IntegerValue (ControlPanel.LOWERED_BEVEL_BORDER);
      if (_value.equals("raised_bevel"))  return new IntegerValue (ControlPanel.RAISED_BEVEL_BORDER);
      if (_value.equals("lowered_etched"))return new IntegerValue (ControlPanel.LOWERED_ETCHED_BORDER);
      if (_value.equals("raised_etched")) return new IntegerValue (ControlPanel.RAISED_ETCHED_BORDER);
      if (_value.equals("line"))          return new IntegerValue (ControlPanel.LINE_BORDER);
      if (_value.equals("rounded_line"))  return new IntegerValue (ControlPanel.ROUNDED_LINE_BORDER);
      if (_value.equals("matte"))         return new IntegerValue (ControlPanel.MATTE_BORDER);
      if (_value.equals("titled"))        return new IntegerValue (ControlPanel.TITLED_BORDER);
      if (_value.equals("rounded_titled"))return new IntegerValue (ControlPanel.ROUNDED_TITLED_BORDER);
    }
    else if (_propertyType.indexOf("BorderPosition")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("top"))          return new IntegerValue (TitledBorder.TOP);
      if (_value.equals("bottom"))       return new IntegerValue (TitledBorder.BOTTOM);
      if (_value.equals("above_top"))    return new IntegerValue (TitledBorder.ABOVE_TOP);
      if (_value.equals("above_bottom")) return new IntegerValue (TitledBorder.ABOVE_BOTTOM);
      if (_value.equals("below_top"))    return new IntegerValue (TitledBorder.BELOW_TOP);
      if (_value.equals("below_bottom")) return new IntegerValue (TitledBorder.BELOW_BOTTOM);
    }
    else if (_propertyType.indexOf("Alignment")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("left"))         return new IntegerValue (TitledBorder.LEFT);
      if (_value.equals("center"))       return new IntegerValue (TitledBorder.CENTER);
      if (_value.equals("right"))        return new IntegerValue (TitledBorder.RIGHT);
    }
    return super.parseConstant (_propertyType,_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : // layout
        if (_value.getObject() instanceof java.awt.LayoutManager) {
          java.awt.LayoutManager layout = (java.awt.LayoutManager) _value.getObject();
          if (layout!=myLayout) {
            getContainer().setLayout(myLayout=layout);
            this.adjustChildren();
            panel.validate();
          }
        }
        break;
      case 1 : // border margins
        if (_value.getObject() instanceof java.awt.Rectangle) {
          java.awt.Rectangle rect = (java.awt.Rectangle) _value.getObject();
          if (rect!=myBorder) { myBorder = rect; setBorder(); }
        }
        break;
      case 2 : // border type
        if (_value.getInteger()!=borderType) { borderType = _value.getInteger(); setBorder(); }
        break;
      case 3 : // border color
        if (_value.getObject() instanceof Color) {
          Color newColor = (Color) _value.getObject();
          if (newColor!=borderColor) { borderColor = newColor; setBorder(); }
        }
        break;
      case 4 : // border title
        if (!borderTitle.equals(_value.getString())) { borderTitle = _value.getString(); setBorder(); }
        break;
      case 5 : // border position
        if (_value.getInteger()!=borderPosition) { borderPosition = _value.getInteger(); setBorder(); }
        break;
      case 6 : // border justification
        if (_value.getInteger()!=borderJustification) { borderJustification = _value.getInteger(); setBorder(); }
        break;

      default: super.setValue(_index-PANEL_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 :
        getContainer().setLayout(myLayout = new java.awt.BorderLayout());
        this.adjustChildren();
        panel.validate();
        break;
      case 1 : myBorder = new java.awt.Rectangle(0,0,0,0); setBorder(); break;
      case 2 : borderType = EMPTY_BORDER; setBorder(); break;
      case 3 : borderColor = Color.BLACK; setBorder(); break;
      case 4 : borderTitle = "Title"; setBorder(); break;
      case 5 : borderPosition = TitledBorder.ABOVE_TOP; setBorder(); break;
      case 6 : borderJustification = TitledBorder.LEFT; setBorder(); break;

      default: super.setDefaultValue(_index-PANEL_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "BORDER";
      case 1 : return "0,0,0,0";
      case 2 : return "EMPTY";
      case 3 : return "BLACK";
      case 4 : return "Title";
      case 5 : return "ABOVE_TOP";
      case 6 : return "LEFT";
      default : return super.getDefaultValueString(_index-PANEL_ADDED);
    }
  }
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 :
        return null;
      default: return super.getValue(_index-PANEL_ADDED);
    }
  }

} // End of class

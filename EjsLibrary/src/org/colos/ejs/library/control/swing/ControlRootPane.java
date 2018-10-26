/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.OSPRuntime;

import javax.swing.JRootPane;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

/**
 * A configurable RootPane. This is as a substitute for JFrame
 * when inside an applet
 */
public class ControlRootPane extends ControlContainer {
  protected JRootPane panel;
  private java.awt.LayoutManager myLayout=null;
  protected JMenuBar menubar=null;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    panel = new JRootPane();
    panel.getContentPane().addKeyListener (
        new java.awt.event.KeyAdapter() {
          public void keyPressed  (java.awt.event.KeyEvent _e) {
            if (_e.isControlDown() && getSimulation()!=null) {
              if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_M) getPopupMenu(0,0);
              else if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_P) printScreen();
            }
          }
        }
    );
    panel.getContentPane().addMouseListener (
        new MouseAdapter() {
          public void mousePressed  (MouseEvent _evt) {
            if (getSimulation()!=null && OSPRuntime.isPopupTrigger(_evt)) { //) {SwingUtilities.isRightMouseButton(_evt) 
              getPopupMenu(_evt.getX(),_evt.getY());
            }
          }
        }
    );
    return panel;
  }

  public java.awt.Component getComponent () { return panel; }

  public java.awt.Container getContainer () { return panel.getContentPane(); }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("layout");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("layout"))         return "Layout|Object NO_RESET";
    return super.getPropertyInfo(_property);
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
      default: super.setValue(_index-1,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 :
        getContainer().setLayout(myLayout = new java.awt.BorderLayout());
        this.adjustChildren();
        panel.validate();
        break;
      default: super.setDefaultValue(_index-1); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "BORDER";
      default : return super.getDefaultValueString(_index-1);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 :
        return null;
      default: return super.getValue(_index-1);
    }
  }

  public void add(final ControlElement _child) {
    if (! (_child.getVisual() instanceof JMenuBar)) { super.add(_child); return; }
    children.add(_child);
    SwingUtilities.invokeLater(new Runnable() {
      public void run () { 
        panel.setJMenuBar(menubar = (JMenuBar) _child.getVisual());
        adjustSize();
      }
    });
    // Now propagate my own font, foreground and background;
    propagateProperty (_child,"font"      ,getPropagatedProperty("font"));
    propagateProperty (_child,"foreground",getPropagatedProperty("foreground"));
    propagateProperty (_child,"background",getPropagatedProperty("background"));
  }

  public void remove(ControlElement _child) {
    if (! (_child.getVisual() instanceof JMenuBar)) { super.remove(_child); return; }
    children.remove(_child);
    panel.setJMenuBar(menubar = null);
    panel.validate();
    panel.repaint();
  }

} // End of class

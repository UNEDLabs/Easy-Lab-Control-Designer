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

import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * A configurable Dialog.
 */
public class ControlDialog extends ControlWindow {
  static private final int DIALOG_ADDED = 4;

  protected JDialog dialog;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () { return createDialog(null); }

  public String getObjectClassname () { return "javax.swing.JDialog"; }

  public Object getObject () { return dialog; }
  
  public JDialog getJDialog() { return dialog; }

  protected javax.swing.JRootPane getRootPane () { return dialog.getRootPane(); }

  public javax.swing.JMenuBar getJMenuBar() { return dialog.getJMenuBar(); }
  
//  protected javax.swing.JLayeredPane getLayeredPane () { return dialog.getLayeredPane(); }

  // This is a very special case
  public void replaceVisual (java.awt.Frame _owner) {
    dialog.dispose();
    myVisual = createDialog (_owner); 
    adjustBorder();
    dialog.repaint();
  }

  // This is a very special case
  private java.awt.Component createDialog (java.awt.Frame _owner) {
    startingup = true;
    dialog = new JDialog (_owner,"",false,org.colos.ejs.library.control.EjsControl.getDefaultGraphicsConfiguration());
//    if (_owner!=null) dialog = new JDialog (_owner);
//    else dialog = new JDialog ();

    dialog.getContentPane().setLayout (new java.awt.BorderLayout());
    dialog.getContentPane().addKeyListener (
        new java.awt.event.KeyAdapter() {
          public void keyPressed  (java.awt.event.KeyEvent _e) {
            if (_e.isControlDown() && getSimulation()!=null) {
              if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_M) getPopupMenu(0,0);
              else if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_P) printScreen();
            }
          }
        }
    );
    dialog.getContentPane().addMouseListener (
        new MouseAdapter() {
          public void mousePressed  (MouseEvent _evt) {
            if (getSimulation()!=null && OSPRuntime.isPopupTrigger(_evt)) { //) {SwingUtilities.isRightMouseButton(_evt) 
              getPopupMenu(_evt.getX(),_evt.getY());
            }
          }
        }
    );

    internalValue = new BooleanValue (true);
    //setProperty ("visible","true");
    setListeners();
    if (menubar!=null) dialog.setJMenuBar(menubar);
    return dialog.getContentPane();
  }

  protected int getVisibleIndex () { return ControlWindow.WINDOW_VISIBLE+DIALOG_ADDED; }
  protected int getLocationIndex () { return ControlWindow.WINDOW_LOCATION+DIALOG_ADDED; }
  protected int getSizeIndex () { return ControlWindow.WINDOW_SIZE+DIALOG_ADDED; }

  public java.awt.Component getComponent () { return dialog; }

  public java.awt.Container getContainer () { return dialog.getContentPane(); }

  public void add(final ControlElement _child) {
    if (! (_child.getVisual() instanceof JMenuBar)) { super.add(_child); return; }
    children.add(_child);
    SwingUtilities.invokeLater(new Runnable() {
      public void run () { 
        dialog.setJMenuBar(menubar = (JMenuBar) _child.getVisual());
        adjustSize();
      }
    });
//    dialog.setJMenuBar(menubar = (JMenuBar) _child.getVisual());
    ((ControlSwingElement) _child).setControlWindow (this);
    // Now propagate my own font, foreground and background;
    propagateProperty (_child,"font"      ,getPropagatedProperty("font"));
    propagateProperty (_child,"foreground",getPropagatedProperty("foreground"));
    propagateProperty (_child,"background",getPropagatedProperty("background"));
  }

  public void remove(ControlElement _child) {
    if (! (_child.getVisual() instanceof JMenuBar)) { super.remove(_child); return; }
    children.remove(_child);
    ((ControlSwingElement) _child).setControlWindow (null);
    dialog.setJMenuBar(menubar=null);
    adjustSize();
    dialog.validate();
    dialog.repaint();
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("title");
      infoList.add ("resizable");
      infoList.add ("closable");
      infoList.add ("modal");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("title"))     return "String TRANSLATABLE";
    if (_property.equals("resizable")) return "boolean";
    if (_property.equals("closable"))  return "boolean";
    if (_property.equals("modal"))     return "boolean";
    return super.getPropertyInfo(_property);
  }

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : // title
        String ejsWindow = getProperty("_ejs_window_");
        if (ejsWindow!=null) dialog.setTitle(org.opensourcephysics.display.TeXParser.parseTeX(_value.getString())+" "+ejsWindow);
        else dialog.setTitle(org.opensourcephysics.display.TeXParser.parseTeX(_value.getString()));
        break;
      case 1 : dialog.setResizable(_value.getBoolean()); break;
      case 2 : if (_value.getBoolean()) dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
               else dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
               break;
      case 3 : if (!isUnderEjs) dialog.setModal(_value.getBoolean()); break;
      default: super.setValue(_index-DIALOG_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : // title
        String ejsWindow = getProperty("_ejs_window_");
        if (ejsWindow!=null) dialog.setTitle(ejsWindow);
        else dialog.setTitle("");
        break;
      case 1 : dialog.setResizable(true); break;
      case 2 : dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE); break;
      case 3 : dialog.setModal(false); break;
      default: super.setDefaultValue(_index-DIALOG_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "true";
      case 2 : return "true";
      case 3 : return "false";
      default : return super.getDefaultValueString(_index-DIALOG_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
        return null;
      default: return super.getValue(_index-DIALOG_ADDED);
    }
  }

} // End of class

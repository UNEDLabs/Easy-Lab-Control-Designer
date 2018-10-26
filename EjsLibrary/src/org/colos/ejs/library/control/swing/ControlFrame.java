/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.library.control.value.*;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.display.OSPRuntime;

/**
 * A configurable Frame. It has no internal value, nor can trigger
 * any action.
 */
public class ControlFrame extends ControlWindow {
  static private final int FRAME_ADDED = 6;
  static private final int FRAME_NAME = ControlWindow.WINDOW_NAME + FRAME_ADDED;
  
//  static private javax.swing.LookAndFeel currentLookAndFeel = javax.swing.UIManager.getLookAndFeel();
//  static private boolean isDecorated = JFrame.isDefaultLookAndFeelDecorated();

  protected JFrame frame;
  protected String iconImageFile=null;

// ------------------------------------------------
// Visual component
// ------------------------------------------------
  
  protected java.awt.Component createVisual () {
    startingup = true;
    frame = new JFrame(EjsControl.getDefaultGraphicsConfiguration());
    
    frame.getContentPane().setLayout (new java.awt.BorderLayout());
    String path = org.colos.ejs.library.Simulation.getPathToLibrary();
//    System.err.println ("Path is "+path);
    if (!path.endsWith("/")) path += "/";
    java.awt.Image image = ResourceLoader.getImage(path+"_ejs_library/images/EjsMainIcon.gif");
    if (image!=null) frame.setIconImage(image);
//    java.awt.Image image = ResourceLoader.getImage("data/icons/EjsFrameIcon.gif");
//    if (image==null) image = ResourceLoader.getImage(org.colos.ejs.library.Simulation.getPathToLibrary()+"_ejs_library/images/EjsMainIcon.gif");
//    if (image!=null) frame.setIconImage(image);
    frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    internalValue = new BooleanValue (true);
    frame.getContentPane().addKeyListener (
        new java.awt.event.KeyAdapter() {
          public void keyPressed  (java.awt.event.KeyEvent _e) {
            if (_e.isControlDown() && getSimulation()!=null) {
              if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_M) getPopupMenu(0,0);
              else if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_P) printScreen();
            }
          }
        }
        
    );
    frame.getContentPane().addMouseListener (
        new MouseAdapter() {
          public void mousePressed  (MouseEvent _evt) {
            if (getSimulation()!=null && OSPRuntime.isPopupTrigger(_evt)) { //) {SwingUtilities.isRightMouseButton(_evt) 
              getPopupMenu(_evt.getX(),_evt.getY());
            }
          }
        }
    );
    return frame.getContentPane();
  }

  public String getObjectClassname () { return "java.awt.Component"; }

  public Object getObject () { return frame; }

  protected javax.swing.JRootPane getRootPane () { return frame.getRootPane(); }

  public javax.swing.JMenuBar getJMenuBar() { return frame.getJMenuBar(); }
  
//  protected javax.swing.JLayeredPane getLayeredPane () { return frame.getLayeredPane(); }

  protected void whenClosing () {
    super.whenClosing();
    if (frame.getDefaultCloseOperation()==JFrame.EXIT_ON_CLOSE || frame.getDefaultCloseOperation()==WindowConstants.DISPOSE_ON_CLOSE) {
      invokeActions();
      Simulation sim = getSimulation(); 
      if (sim!=null) {
    	  sim.pause();
    	  sim.onExit();
      }
    }
  }

  protected int getVisibleIndex () { return ControlWindow.WINDOW_VISIBLE+FRAME_ADDED; }
  protected int getLocationIndex () { return ControlWindow.WINDOW_LOCATION+FRAME_ADDED; }
  protected int getSizeIndex () { return ControlWindow.WINDOW_SIZE+FRAME_ADDED; }

  public java.awt.Component getComponent () { return frame; }

  public JFrame getJFrame () { return frame; }

  public java.awt.Container getContainer () { return frame.getContentPane(); }

  public void add(final ControlElement _child) {
    if (! (_child.getVisual() instanceof JMenuBar)) { 
      super.add(_child);
      return; 
    }
    children.add(_child);
    SwingUtilities.invokeLater(new Runnable() {
      public void run () { 
        frame.setJMenuBar(menubar = (JMenuBar) _child.getVisual());
        adjustSize();
      }
    });
    //frame.setJMenuBar(menubar = (JMenuBar) _child.getVisual());
    ((ControlSwingElement)_child).setControlWindow (this);
    // Now propagate my own font, foreground and background;
    propagateProperty (_child,"font"      ,getPropagatedProperty("font"));
    propagateProperty (_child,"foreground",getPropagatedProperty("foreground"));
    propagateProperty (_child,"background",getPropagatedProperty("background"));
  }

  public void remove(ControlElement _child) {
    if (! (_child.getVisual() instanceof JMenuBar)) { super.remove(_child); return; }
    children.remove(_child);
    ((ControlSwingElement)_child).setControlWindow (null);
    frame.setJMenuBar(menubar = null);
    adjustSize();
    frame.validate();
    frame.repaint();
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
      infoList.add ("exit");
      infoList.add ("onExit");
      infoList.add ("image");
      infoList.add ("glasspane");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("title"))     return "String TRANSLATABLE";
    if (_property.equals("resizable")) return "boolean";
    if (_property.equals("exit"))      return "boolean CONSTANT";
    if (_property.equals("onExit"))    return "Action CONSTANT";
    if (_property.equals("image"))     return "File|String TRANSLATABLE";
    if (_property.equals("glasspane")) return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : // title
        String ejsWindow = getProperty("_ejs_window_");
        if (ejsWindow!=null) frame.setTitle(org.opensourcephysics.display.TeXParser.parseTeX(_value.getString())+" "+ejsWindow);
        else frame.setTitle(org.opensourcephysics.display.TeXParser.parseTeX(_value.getString()));
        break;
      case 1 : frame.setResizable(_value.getBoolean()); break;
      case 2 : // exit
        if (getProperty("_ejs_")==null) {
          if (_value.getBoolean()) frame.setDefaultCloseOperation(OSPRuntime.appletMode ? WindowConstants.DISPOSE_ON_CLOSE : JFrame.EXIT_ON_CLOSE);
          else frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }
        break;
      case 3 : // action
        removeAction (ControlElement.ACTION,getProperty("onExit"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 4 :
        if (_value.getString().equals(iconImageFile)) return; // no need to do it again
        frame.setIconImage(ControlSwingElement.getImage(iconImageFile = _value.getString()));
        break;
      case 5 : frame.getGlassPane().setVisible(_value.getBoolean()); break;
      case FRAME_NAME : // Overrides ControlElement's 'name'
        super.setValue(ControlWindow.WINDOW_NAME,_value);
        if (getGroup()!=null && getGroup().getOwnerFrame()==getComponent()) {
          String replacement = getGroup().getReplaceOwnerName();
          if (replacement!=null && replacement.equals(_value.getString()))
            getGroup().setOwnerFrame(getGroup().getReplaceOwnerFrame());
          else getGroup().setOwnerFrame(frame);
        }
        break;
      default: super.setValue(_index-FRAME_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : // title
        String ejsWindow = getProperty("_ejs_window_");
        if (ejsWindow!=null) frame.setTitle(ejsWindow);
        else frame.setTitle("");
        break;
      case 1 : frame.setResizable(true); break;
      case 2 : // exit
        if (getProperty("_ejs_")==null) {
          frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }
        break;
      case 3 : removeAction (ControlElement.ACTION,getProperty("onExit")); break;
      case 4 : iconImageFile = null; frame.setIconImage(null); break;
      case 5 : frame.getGlassPane().setVisible(false); break;
      case FRAME_NAME : // Overrides ControlElement's 'name'
        super.setDefaultValue (ControlWindow.WINDOW_NAME);
        if (getGroup()!=null && getGroup().getOwnerFrame()==getComponent()) {
          getGroup().setOwnerFrame(frame);
        }
        break;
      default: super.setDefaultValue(_index-FRAME_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "true";
      case 2 : return "<none>";
      case 3 : return "<no_action>";
      case 4 : return "<none>";
      case 5 : return "false";
      default : return super.getDefaultValueString(_index-FRAME_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 : case 5 :
        return null;
      default: return super.getValue(_index-FRAME_ADDED);
    }
  }


} // End of 2class

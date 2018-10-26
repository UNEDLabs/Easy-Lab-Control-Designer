/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.library.control.NeedsUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.OSPRuntime;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * A configurable Window. Base class for Frame and Dialog
 */
public abstract class ControlWindow extends ControlContainer implements NeedsUpdate {
  static public final int WINDOW_ADDED = 6;
  static public final int WINDOW_LOCATION = 1;
  static public final int WINDOW_NAME     = ControlElement.NAME+WINDOW_ADDED;
  static public final int WINDOW_VISIBLE  = ControlSwingElement.VISIBLE+WINDOW_ADDED;
  static public final int WINDOW_SIZE    = ControlSwingElement.SIZE+WINDOW_ADDED;

  static private boolean keepHidden=false;

//  static public final Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

  private Window myWindow;
  private LayoutManager myLayout=null;
  private Dimension     mySize=null;
  protected boolean waitForReset=false, startingup = true, shouldShow=true;
  protected JMenuBar menubar=null;
  private Border originalBorder;

  private boolean notFirstTimeMoved = false, notFirstTimeSized = false;
  private boolean settingDefaultLocation = true;
  private ObjectValue locationValue,sizeValue;
  protected BooleanValue internalValue;

  static ArrayList<ControlWindow> sWindowList = new ArrayList<ControlWindow>();
  
  static public void setKeepHidden (boolean hidden) {
    keepHidden = hidden;
    for (ControlWindow cWindow : sWindowList) cWindow.reset();
  }
  
//  static public void clearWindowList() { sWindowList.clear(); }

  static public void removeFromWindowList(ControlWindow cWindow) { sWindowList.remove(cWindow); }

  public ControlWindow () {
    super ();
    setListeners();
    originalBorder = getRootPane().getBorder();
    adjustBorder();
    sWindowList.add(this);
  }

  abstract protected int getVisibleIndex ();
  abstract protected int getLocationIndex ();
  abstract protected int getSizeIndex ();
  abstract protected javax.swing.JRootPane getRootPane ();
  abstract public javax.swing.JMenuBar getJMenuBar();
//  abstract protected javax.swing.JLayeredPane getLayeredPane ();
  
  public void adjustBorder() {
    int borderWidth = EjsControl.getBorderWidthAroundWindows();
    JRootPane rootPane = getRootPane();
    if (borderWidth==0) rootPane.setBorder(originalBorder);
    else {
      if (borderWidth<0) borderWidth = -1;
      Icon icon = EjsControl.getBorderIconAroundWindows();
      Color color = EjsControl.getBorderColorAroundWindows();
      rootPane.setOpaque(true);
//      rootPane.setBackground(color);
      javax.swing.border.MatteBorder border;
      if (icon==null) border = BorderFactory.createMatteBorder(borderWidth,borderWidth,borderWidth,borderWidth,color);
      else border = BorderFactory.createMatteBorder(borderWidth,borderWidth,borderWidth,borderWidth,icon);
      String title = EjsControl.getBorderTitleAroundWindows();
      if (title==null) rootPane.setBorder(border);
      else {
        javax.swing.border.TitledBorder titledBorder = BorderFactory.createTitledBorder(border," "+title+" ");
        titledBorder.setTitleJustification(javax.swing.border.TitledBorder.CENTER);
        titledBorder.setTitlePosition(javax.swing.border.TitledBorder.TOP);
        titledBorder.setTitleColor(EjsControl.getBorderTitleColorAroundWindows());
        rootPane.setBorder(titledBorder);
      }
    }
    myWindow.validate();
    myWindow.pack();
  }
  
  public boolean acceptsChild (ControlElement _child) {
    if (_child.getVisual() instanceof javax.swing.JMenuBar) return true;
    if (_child.getVisual() instanceof javax.swing.JMenuItem) return false;
    if (_child instanceof ControlSwingElement) return true;
    return false;
  }

  /**
   * Called by the group when there is a change of displacement
   */
  public void updateLocation () {
    Point loc = (Point) locationValue.value;
    setLocation (loc.x,loc.y);
  }

  /**
   * Makes sure the location is relative to the bounds of the default screen
   * @param x
   * @param y
   */
  private void setLocation (int x, int y) {
//    System.out.println ("Setting location of "+this.getProperty("name")+ " to "+x+","+y);
    if (x==0 && y==0) settingDefaultLocation = true;
    if (myGroup!=null) {
      Point disp = myGroup.getDisplacement();
      x += disp.x;
      y += disp.y;
    }
//    System.out.println ("after displacement location is "+this.getProperty("name")+ " to "+x+","+y);
    Rectangle bounds = myWindow.getGraphicsConfiguration().getBounds();
    // Location is always relative to the current screen bounds
    // Make sure the window fits the screen
    x = Math.max(Math.min(x,bounds.width-30),30-myWindow.getWidth());
    y = Math.max(Math.min(y,bounds.height-30),30-myWindow.getHeight());
//    if (x>bounds.width-30)  x = 0;
//    if (y>bounds.height-30) y = 0;
    // Now add the screen bounds
    x += bounds.x;
    y += bounds.y;
    getComponent().setLocation(x,y);
  }

  protected void setListeners () {
    myWindow = (Window) getComponent();
    locationValue=new ObjectValue(getComponent().getLocation());
    sizeValue=new ObjectValue(getContainer().getSize());
    myWindow.addComponentListener (new java.awt.event.ComponentAdapter() {
      public void componentMoved (java.awt.event.ComponentEvent evt) {
        if (isUnderEjs) { // moved under EJS
          if (settingDefaultLocation) { // so that the default remains unspecified
            settingDefaultLocation = false;
            return; 
          }
          if (isCentered()) return;
        }
        // When repositioning the window, reported motion is always relative to the current screen bounds
        Rectangle bounds = myWindow.getGraphicsConfiguration().getBounds();
        Point loc = getComponent().getLocation();
//        System.out.println (getProperty("name")+" Moved to "+loc.x+","+loc.y);
        if (OSPRuntime.isMac()) {
          if (loc.y<22) {
            getComponent().setLocation(loc.x, loc.y = 22);
            return;
          }
        }
        loc.x -= bounds.x;
        loc.y -= bounds.y;
        locationValue.value = loc;
//        System.out.println (getProperty("name")+" Reported as "+loc.x+","+loc.y);
        //variableChanged (getLocationIndex(),locationValue);
        if (isUnderEjs && notFirstTimeMoved) setFieldListValue(getLocationIndex(),locationValue, false); // do not report
        notFirstTimeMoved = true;
      }
      public void componentResized(java.awt.event.ComponentEvent _e) {
        if (startingup || !notFirstTimeSized) { notFirstTimeSized = true; return; }
        sizeValue.value = mySize = getContainer().getSize();
        //variableChanged (getSizeIndex(),sizeValue);
        if (isUnderEjs) setFieldListValue(getSizeIndex(),sizeValue, false); // do not report
        invokeActions(ControlSwingElement.ACTION_PRESS);
      }
      public void componentShown(java.awt.event.ComponentEvent _e) {
        invokeActions(ControlSwingElement.ACTION_ON);
      }
    });
    myWindow.addWindowListener (new java.awt.event.WindowAdapter() {
      public void windowClosing (java.awt.event.WindowEvent evt) {
        whenClosing();
      }
    });

  }

  private boolean isCentered() {
    String loc = getProperty("location");
    if (loc==null) return false;
    loc = loc.toLowerCase();
    if (loc.startsWith("\"") && loc.endsWith("\"")) loc = loc.substring(1,loc.length()-1);
    return "center".equals(loc);
  }
  
  protected void whenClosing () {
//    System.err.println ("Simulation ended = "+getSimulation().hasEnded());
    if ((getSimulation()!=null) && getSimulation().hasEnded()) return;
    internalValue.value = false;
    variableChanged (getVisibleIndex(),internalValue);
    invokeActions(ControlSwingElement.ACTION_OFF);
  }

  public void disposeWindow() {
    if (getComponent().isDisplayable()) SwingUtilities.invokeLater(new Runnable() {
      public void run() { 
        setProperty("visible", "false");
        ((Window) getComponent()).setVisible(false);
        ((Window) getComponent()).dispose();
        sWindowList.remove(this);
      }
    });
  }

  public boolean isVisible() { return ((Window) getComponent()).isVisible(); }
  
  public void show() {
//    if (getProperty("name").startsWith("teacher")) System.err.println (getProperty("name")+" show(): startingup = "+startingup);
    shouldShow = true;
    if (keepHidden) return;
    if (startingup) {
      if (waitForReset) return;
    }
    final Window w = (Window) getComponent();
    if (!w.isVisible()) {
//      System.err.println (getProperty("name")+" was not visible");
//      updateLocation();
      w.setVisible(true); 
      w.repaint(); 
    }
  }

  public void hide() {
    shouldShow = false;
    if (startingup) {
      if (waitForReset) return;
    }
    Window w = (Window) getComponent();
    if (w.isVisible()) {
//      System.err.println ("Hiding "+getProperty("name"));
      w.setVisible(false);
    }
  }

//  public void destroy() {
//    System.err.println("ControlWIndow destroy "+this);
//    dispose(); 
//    super.destroy(); 
//    }

  public void setWaitForReset(boolean _option) {
    waitForReset = _option;
    if (waitForReset) ((Window) getComponent()).setVisible(false);
  }

  public void reset () {
    startingup = false;
    if (shouldShow) show();
    else            hide();
    Window w = (Window) getComponent();
    w.repaint();
    super.reset();
  }

  public void update () { // Ensure it will be updated
    startingup = false;
//    super.update();
  }

  public void adjustSize() { // overrides its super
//    String size = getProperty("size");
    getContainer().setPreferredSize(mySize);
    myWindow.validate();
    myWindow.pack();
//    if (size!=null && size.trim().toLowerCase().equals("pack")) myWindow.pack();
//    else {
//      getContainer().validate();
//      myWindow.repaint();
//      resizeContainer (myWindow);
//    }
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("layout");
      infoList.add ("location");
      infoList.add ("waitForReset");
      infoList.add ("onClosing");
      infoList.add ("resizeAction");
      infoList.add ("onDisplay");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("location"))       return "Point|Object|String NO_RESET";
    if (_property.equals("layout"))         return "Layout|Object NO_RESET";
    if (_property.equals("waitForReset"))   return "boolean";
    if (_property.equals("onClosing"))      return "Action CONSTANT";
    if (_property.equals("resizeAction"))      return "Action CONSTANT";
    if (_property.equals("onDisplay"))      return "Action CONSTANT";

    if (_property.equals("size"))       return "Dimension|Object|String TRANSLATABLE NO_RESET";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : // layout
        if (_value.getObject() instanceof LayoutManager) {
          LayoutManager layout = (LayoutManager) _value.getObject();
          if (layout!=myLayout) {
            getContainer().setLayout(myLayout = layout);
            this.adjustChildren();
          }
          myWindow.validate();
        }
        break;
      case WINDOW_LOCATION : // location
        {
          Point pos=null;
          if (_value.getObject() instanceof Point) pos = (Point) _value.getObject();
          else {
            String valStr = _value.toString();
            Value val = ConstantParser.pointConstant (valStr);
            if (val==null) return;
            pos = (Point) val.getObject();
          }
          locationValue.value = pos;
          settingDefaultLocation = false;
          if (! (isUnderEjs && isCentered())) setLocation (pos.x,pos.y);
        }
        break;
      case 2 : setWaitForReset (_value.getBoolean()); break;
      case 3 : // onClosing action
        removeAction (ControlSwingElement.ACTION_OFF,getProperty("onClosing"));
        addAction(ControlSwingElement.ACTION_OFF,_value.getString());
        break;
      case 4 : // or resize action
        removeAction (ControlSwingElement.ACTION_PRESS,getProperty("resizeAction"));
        addAction(ControlSwingElement.ACTION_PRESS,_value.getString());
        break;
      case 5 : // onDisplay action
        removeAction (ControlSwingElement.ACTION_ON,getProperty("onDisplay"));
        addAction(ControlSwingElement.ACTION_ON,_value.getString());
        break;

      case WINDOW_VISIBLE : // Overrides its super 'visible'
        internalValue.value = _value.getBoolean();
        if (internalValue.value) show();
        else hide();
        break;
      case WINDOW_SIZE : // // Overrides its super 'size'
        Dimension size=null;
        Rectangle bounds = org.colos.ejs.library.control.EjsControl.getDefaultScreenBounds();
        if (_value instanceof StringValue) {
          if ("pack".equals(_value.getString())) {
            myWindow.pack();
            mySize = size = getContainer().getSize();
            sizeValue.value = size;
          }
          else {
            Value val = ConstantParser.dimensionConstant (_value.getString());
            if (val==null) return;
            size = (Dimension) val.getObject();
            if (size.width>bounds.width) size.width = bounds.width;
            if (size.height>bounds.height) size.height = bounds.height;
            sizeValue.value = size;
            if (size.equals(mySize)) return;
//            System.out.println ("Setting size to "+size.width+","+size.height);
            getContainer().setPreferredSize(mySize=size);
            myWindow.validate();
            myWindow.pack();
          }
        }
        else if (_value.getObject() instanceof Dimension) {
          size = (Dimension) _value.getObject();
          if (size.width>bounds.width) size.width = bounds.width;
          if (size.height>bounds.height) size.height = bounds.height;
          sizeValue.value = size;
          if (size.equals(mySize)) return;
//          System.out.println ("Setting the size to "+size.width+","+size.height);
          getContainer().setPreferredSize(mySize=size);
          myWindow.validate();
          myWindow.pack();
        }
        else return;
        if (isCentered()) setLocation((bounds.width-size.width)/2,(bounds.height-size.height)/2);
        break;
      default: super.setValue(_index-WINDOW_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 :
        getContainer().setLayout(myLayout = new BorderLayout());
        this.adjustChildren();
        myWindow.validate();
        break;
      case WINDOW_LOCATION :
        locationValue.value = new Point(0,0);
        setLocation(0,0);
        break;
      case 2 : setWaitForReset (false); break;
      case 3 : removeAction (ControlSwingElement.ACTION_OFF,getProperty("onClosing")); break;
      case 4 : removeAction (ControlSwingElement.ACTION_PRESS,getProperty("resizeAction")); break;
      case 5 : removeAction (ControlSwingElement.ACTION_ON,getProperty("onDisplay")); break;

      case WINDOW_VISIBLE : // Overrides its super 'visible'
        internalValue.value = true;
        show();
        break;
      case WINDOW_SIZE : // // Overrides its super 'size'
        ((Window) getComponent()).pack();
        Dimension size = getContainer().getSize();
//        if (menubar!=null) {
//          Dimension mbs = menubar.getSize();
//          size.height += mbs.height;
//        }
        sizeValue.value = size;
        if (isCentered()) {
          Rectangle bounds = org.colos.ejs.library.control.EjsControl.getDefaultScreenBounds();
          setLocation((bounds.width-size.width)/2,(bounds.height-size.height)/2);
        }
        break;
      default: super.setDefaultValue(_index-WINDOW_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "BORDER";
      case 1 : return "0,0";
      case 2 : return "false";
      case 3 : case 4 : case 5 : return "<no_action>";
      default : return super.getDefaultValueString(_index-WINDOW_ADDED);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 2 : case 3 : case 4 :
      case 5 :
        return null;
      case WINDOW_LOCATION : return locationValue;
      case WINDOW_VISIBLE : // Overrides its super 'visible'
        return internalValue;
      case WINDOW_SIZE : // // Overrides its super 'size'
        return sizeValue;
      default: return super.getValue(_index-WINDOW_ADDED);
    }
  }

} // End of class

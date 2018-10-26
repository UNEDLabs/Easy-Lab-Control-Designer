/*
 * The control.display3d package contains subclasses of
 * control.ControlElement that deal with the display3d package
 * Copyright (c) July 2006 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */


package org.colos.ejs.library.control.drawing3d;


import java.util.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import java.awt.event.ActionListener;

import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.*;
import org.colos.ejs.library.control.swing.ControlSwingElement;
import org.colos.ejs.library.control.swing.NeedsPreUpdate;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.drawing3d.*;
import org.opensourcephysics.drawing3d.interaction.*;
import org.opensourcephysics.drawing3d.utils.*;
import org.opensourcephysics.drawing3d.utils.mapping.Mapping;

/**
 * A configurable 3D panel from the displayejs package
 */
public class ControlDrawingPanel3D  extends ControlSwingElement implements NeedsUpdate, NeedsFinalUpdate, ControlParentOfElement3D,
        InteractionListener, SpecialRender, HasEditor, ImplementationChangeListener {

  static private final int DP3D_ADDED = 49;
  static private final int PANEL_ENABLED = ControlSwingElement.ENABLED+DP3D_ADDED;
  static private final int PANEL_BACKGROUND = ControlSwingElement.BACKGROUND+DP3D_ADDED;
  static private final int PANEL_FOREGROUND = ControlSwingElement.FOREGROUND+DP3D_ADDED;
  static private final int PANEL_FONT = ControlSwingElement.FONT+DP3D_ADDED;
  
  static final public double TO_RAD = Math.PI/180.0;

  static protected final int PROJECTION = 7;
  static protected final int [] POSITION_INDEXES = {24,25,26};
  static protected final int [] CAMERA_INDEXES         = {8,9,10, 11, 12,13, 36,37,38, 39};
  static protected final int [] CAMERA_PARTIAL_INDEXES = {8,9,10, 11, 12,13, 36,37,38, 39};
//  static protected final int [] CAMERA_FOCUS_INDEXES = {8,9,10};
//  static protected final int CAMERA_DISTANCE_TO_SCREEN_INDEX=11;
//  static protected final int [] CAMERA_ANGLE_INDEXES = {12,13};
//  static protected final int [] CAMERA_ORIGIN_INDEXES = {37,38,39};
//  static protected final int CAMERA_ROTATION_INDEX=40;

  static private final int KEY_INDEX = 35;

  // Drawing Panel settings
  DrawingPanel3D drawingPanel3D;
  private VisualizationHints visHints;
  private double minX, maxX, minY, maxY, minZ, maxZ;
  private double axisXSize, axisYSize, axisZSize;
  private Camera camera;
  private javax.swing.JFrame cameraInspectorFrame=null;
  private CameraInspector cameraInspector=null;
  private Color defBackground, defForeground;
  private Font defFont;

  // Interaction
  private boolean reportXYZMotion=false, reportCameraMotion=false, reportKey=false, cameraLocationSet=false;
  private IntegerValue keyPressedValue = new IntegerValue(-1);
  private DoubleValue[] posValues = { new DoubleValue(0.0), new DoubleValue(0.0), new DoubleValue(0.0)};
  private DoubleValue[] cameraValues;
  private KeyListener keyListener;
  private MouseListener mouseListener;

  public Object getObject() { return drawingPanel3D; }

  public String getObjectClassname () { return "org.opensourcephysics.drawing3d.DrawingPanel3D"; }

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    drawingPanel3D = new DrawingPanel3D();
    // Drawing Panel
    minX = drawingPanel3D.getPreferredMinX();
    maxX = drawingPanel3D.getPreferredMaxX();
    minY = drawingPanel3D.getPreferredMinY();
    maxY = drawingPanel3D.getPreferredMaxY();
    minZ = drawingPanel3D.getPreferredMinZ();
    maxZ = drawingPanel3D.getPreferredMaxZ();
    axisXSize = drawingPanel3D.getAxisXSize();
    axisYSize = drawingPanel3D.getAxisYSize();
    axisZSize = drawingPanel3D.getAxisZSize();
    visHints = drawingPanel3D.getVisualizationHints();
    defBackground = visHints.getBackgroundColor();
    defForeground = visHints.getForegroundColor();
    defFont = visHints.getFont();
    // Camera
    camera = drawingPanel3D.getCamera();
    cameraValues = new DoubleValue[]  { new DoubleValue(camera.getFocusX()),
                                        new DoubleValue(camera.getFocusY()),
                                        new DoubleValue(camera.getFocusZ()),
                                        new DoubleValue(camera.getDistanceToScreen()),
                                        new DoubleValue(camera.getAzimuth()),
                                        new DoubleValue(camera.getAltitude()),
                                        new DoubleValue(camera.getX()),
                                        new DoubleValue(camera.getY()),
                                        new DoubleValue(camera.getZ()),
                                        new DoubleValue(camera.getRotation()) };
    drawingPanel3D.addInteractionListener (this);
    drawingPanel3D.addImplementationChangeListener(this);
    drawingPanel3D.render();

    keyListener = new java.awt.event.KeyAdapter() {
      public void keyPressed  (java.awt.event.KeyEvent _e) {
        if (_e.isControlDown() && getSimulation()!=null) {
          if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_M) {
            getPopupMenu(0,0);
          }
          else if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_P) printScreen();
        }
        keyPressedValue.value = _e.getKeyCode();
        if (reportKey) {
          variableChanged (KEY_INDEX,keyPressedValue);
          invokeActions (ControlSwingElement.KEY_ACTION);
        }
      }
      public void keyReleased (java.awt.event.KeyEvent _e) {
        keyPressedValue.value = -1;
        if (reportKey) variableChanged (KEY_INDEX,keyPressedValue);
      }

    };

    mouseListener = new MouseAdapter() {
      public void mousePressed  (MouseEvent _e) {
        if ((_e.isPopupTrigger() || _e.getModifiers() == InputEvent.BUTTON3_MASK) && getSimulation()!=null) {
          getPopupMenu(_e.getX(), _e.getY());
        }
      }
    };

    setListeners();
    return drawingPanel3D.getComponent();
  }
  
  public void implementationChanged(int toImplementation) {
    setListeners();
    super.changeVisual(drawingPanel3D.getComponent());
  }
   
  private void setListeners() {
    java.awt.Component comp = drawingPanel3D.getComponent();
    comp.setFocusable(true);
    comp.removeKeyListener(keyListener); // In case it was already added
    comp.addKeyListener(keyListener);
    comp.removeMouseListener (mouseListener); // In case it was already added
    comp.addMouseListener (mouseListener);
  }

  public void addMenuEntries() {
    JMenuItem cameraMenuItem = new JMenuItem ("CameraInspector.Camera");
    cameraMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) { showCameraInspector(); }
    });
    cameraMenuItem.setActionCommand("CameraInspector.Camera");

    JRadioButtonMenuItem simple3DItem = new JRadioButtonMenuItem("Simple 3D", drawingPanel3D.getImplementation()==DrawingPanel3D.IMPLEMENTATION_SIMPLE3D);
    simple3DItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        if (evt.getStateChange()==ItemEvent.SELECTED) drawingPanel3D.setImplementation(DrawingPanel3D.IMPLEMENTATION_SIMPLE3D);
      }
    });
    JRadioButtonMenuItem java3DItem = new JRadioButtonMenuItem("Java 3D", drawingPanel3D.getImplementation()==DrawingPanel3D.IMPLEMENTATION_JAVA3D);
    java3DItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        if (evt.getStateChange()==ItemEvent.SELECTED) drawingPanel3D.setImplementation(DrawingPanel3D.IMPLEMENTATION_JAVA3D);
      }
    });
    
    ButtonGroup group = new ButtonGroup();
    group.add(simple3DItem);
    group.add(java3DItem);
    
    JMenu implementationMenu = new JMenu ("Implementation");
    implementationMenu.setActionCommand("ejs_res:DrawingPanel3D.Implementation");

    implementationMenu.add(simple3DItem);
    implementationMenu.add(java3DItem);
    
//    JMenuItem lightMenuItem = new JMenuItem ("LightInspector.FrameTitle");
//    lightMenuItem.addActionListener(new ActionListener() {
//      public void actionPerformed (ActionEvent _evt) { showLightInspector(); }
//    });
//    lightMenuItem.setActionCommand("LightInspector.FrameTitle");

    java.util.List<Object> menuEntries = new java.util.ArrayList<Object>();
    menuEntries.add(cameraMenuItem);
    menuEntries.add(implementationMenu);
//    menuEntries.add(lightMenuItem);
    getSimulation().addElementMenuEntries (getMenuNameEntry(), menuEntries);
  }

  public boolean acceptsChild (ControlElement _child) {
    if (_child instanceof org.colos.ejs.library.control.drawing3d.utils.Control3DChild) return true;
    return false;
  }
  
  public java.awt.image.BufferedImage render(java.awt.image.BufferedImage image) {
      return drawingPanel3D.render(image);
  }

  public void showEditor (String editor) {
    if ("camera".equalsIgnoreCase(editor)) showCameraInspector();
//      camera.reset();
//      reportCameraMotion();
//    }
  }

  public void showCameraInspector() {
    if(cameraInspectorFrame==null) createCameraInspector();
    cameraInspectorFrame.setVisible(true);
  }

  private void createCameraInspector() {
      cameraInspector = new CameraInspector(drawingPanel3D);
      cameraInspector.addActionListener(new ActionListener() {
          public void actionPerformed (ActionEvent _evt) {
              if (isUnderEjs) updateCameraFields();
          }
      });
      cameraInspectorFrame = CameraInspector.createFrame(cameraInspector);
      //cameraInspectorFrame = CameraInspector.createFrame(drawingPanel3D);
      cameraInspectorFrame.setLocationRelativeTo(drawingPanel3D.getComponent());
  }

// ------------------------------------------
// Implementation of ControlElements3DParent
// ------------------------------------------

  // List of children that need to do something before repainting the panel
  private Vector<NeedsPreUpdate> preupdateList = new Vector<NeedsPreUpdate>();

  public void update () { // Ensure it will be updated
    // First prepare children that need to do something
    for (Enumeration<NeedsPreUpdate> e=preupdateList.elements(); e.hasMoreElements(); ) e.nextElement().preupdate();
  }
  
//  public void finalUpdate() {
////    if (myGroup!=null && myGroup.isCollectingData()) return;
//    // Now render
//    drawingPanel3D.update(); // It was render() before!
//  }
//  
  public void finalUpdate() {
    if (javax.swing.SwingUtilities.isEventDispatchThread()||Thread.currentThread().getName().equals("main")) {
      Simulation sim = getSimulation();
      if (sim==null || sim.isPaused()) {
        drawingPanel3D.update();
      }
    }
    else {
      if (drawingPanel3D.canRender()) drawingPanel3D.render();
    }
  }

  public void addToPreupdateList (NeedsPreUpdate _child) { preupdateList.add(_child); }

  public void removeFromPreupdateList (NeedsPreUpdate _child) {
    preupdateList.remove(_child);
  }

  public void addElement (Element _element) { drawingPanel3D.addElement(_element); }

  public void removeElement (Element _element) { drawingPanel3D.removeElement(_element); }

  public DrawingPanel3D getDrawingPanel3D() { return drawingPanel3D; }

// ------------------------------------------
// Printing when pressing ctrl-p
// ------------------------------------------

  protected void getPopupMenu (int _x, int _y) {
    if(cameraInspectorFrame==null) createCameraInspector();
    super.getPopupMenu(_x, _y);
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      // The panel itself
      infoList.add ("minimumX");
      infoList.add ("maximumX");
      infoList.add ("minimumY");
      infoList.add ("maximumY");
      infoList.add ("minimumZ");
      infoList.add ("maximumZ");
      infoList.add ("squareAspect");
      // Camera
      infoList.add ("projectionMode");  // The "camera" prefix is reserved. See SetProperty() below.
      infoList.add ("cameraFocusX");
      infoList.add ("cameraFocusY");
      infoList.add ("cameraFocusZ");
      infoList.add ("cameraDistanceToScreen");
      infoList.add ("cameraAzimuth");
      infoList.add ("cameraAltitude");
      // Visualization Hints
      infoList.add ("implementation"); // 14
      infoList.add ("decorationType");
      infoList.add ("cursorType");
      infoList.add ("removeHiddenLines");
      infoList.add ("allowQuickRedraw");
      infoList.add ("useColorDepth");
      infoList.add ("showCoordinates");
      infoList.add ("xFormat");
      infoList.add ("yFormat");
      infoList.add ("zFormat");
      // Interaction
      infoList.add ("x");
      infoList.add ("y");
      infoList.add ("z");
      infoList.add ("movedAction");
      infoList.add ("pressAction");
      infoList.add ("releaseAction");
      infoList.add ("dragAction");
      infoList.add ("enteredAction");
      infoList.add ("exitedAction");
      infoList.add ("keyAction");
      infoList.add ("keyPressed");

      infoList.add ("axesLabels");

      infoList.add ("cameraX");
      infoList.add ("cameraY");
      infoList.add ("cameraZ");
      infoList.add ("cameraRotation");

      infoList.add ("image");
      
      infoList.add ("axesMapping");
      infoList.add ("sizeX");
      infoList.add ("sizeY");
      infoList.add ("sizeZ");
      
      infoList.add ("illumination");
      infoList.add ("moveable");

      infoList.add ("enabledPosition");
      infoList.add ("autoAdjust");

      infoList.addAll(super.getPropertyList()); // Inherit from ControlSwingElement
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("minimumX"))       return "int|double";
    if (_property.equals("maximumX"))       return "int|double";
    if (_property.equals("minimumY"))       return "int|double";
    if (_property.equals("maximumY"))       return "int|double";
    if (_property.equals("minimumZ"))       return "int|double";
    if (_property.equals("maximumZ"))       return "int|double";
    if (_property.equals("squareAspect"))   return "boolean";

    if (_property.equals("projectionMode"))         return "int|3DCameraMode";
    if (_property.equals("cameraFocusX"))           return "int|double";
    if (_property.equals("cameraFocusY"))           return "int|double";
    if (_property.equals("cameraFocusZ"))           return "int|double";
    if (_property.equals("cameraDistanceToScreen")) return "int|double";
    if (_property.equals("cameraAzimuth"))          return "int|double";
    if (_property.equals("cameraAltitude"))         return "int|double";

    if (_property.equals("implementation"))    return "int|3DImplementation";
    if (_property.equals("decorationType"))    return "int|3DDecorationType";
    if (_property.equals("cursorType"))        return "int|3DCursorType";
    if (_property.equals("removeHiddenLines")) return "boolean";
    if (_property.equals("allowQuickRedraw"))  return "boolean";
    if (_property.equals("useColorDepth"))     return "boolean";
    if (_property.equals("showCoordinates"))   return "int|3DShowCoordinates";
    if (_property.equals("xFormat"))           return "Object|String TRANSLATABLE";
    if (_property.equals("yFormat"))           return "Object|String TRANSLATABLE";
    if (_property.equals("zFormat"))           return "Object|String TRANSLATABLE";

    if (_property.equals("x"))              return "int|double";
    if (_property.equals("y"))              return "int|double";
    if (_property.equals("z"))              return "int|double";
    if (_property.equals("movedAction"))    return "Action CONSTANT";
    if (_property.equals("releaseAction"))  return "Action CONSTANT";
    if (_property.equals("pressAction"))    return "Action CONSTANT";
    if (_property.equals("dragAction"))     return "Action CONSTANT";
    if (_property.equals("enteredAction"))  return "Action CONSTANT";
    if (_property.equals("exitedAction"))   return "Action CONSTANT";
    if (_property.equals("keyAction"))      return "Action CONSTANT";
    if (_property.equals("keyPressed"))     return "int";

    if (_property.equals("axesLabels"))     return "String TRANSLATABLE";

    if (_property.equals("cameraX"))           return "int|double";
    if (_property.equals("cameraY"))           return "int|double";
    if (_property.equals("cameraZ"))           return "int|double";
    if (_property.equals("cameraRotation"))    return "int|double";

    if (_property.equals("image"))    return "File|String TRANSLATABLE";
    
    if (_property.equals("axesMapping"))  return "int|3DAxesMapping";
    if (_property.equals("sizeX"))   return "int|double";
    if (_property.equals("sizeY"))   return "int|double";
    if (_property.equals("sizeZ"))   return "int|double";
    
    if (_property.equals("illumination"))   return "boolean";
    if (_property.equals("moveable"))   return "boolean";

    if (_property.equals("enabledPosition"))   return "int|3DDraggable";
    if (_property.equals("autoAdjust"))   return "boolean";

    return super.getPropertyInfo(_property);
  }

  @Override
  public ControlElement setProperty(String _property, String _value) {
    _property = _property.trim();
    if (_property.equals("x") || _property.equals("y") || _property.equals("z") ) {
      if (_value!=null) {
        Value constant = Value.parseConstant(_value, true);
        if (!reportXYZMotion) reportXYZMotion = (constant == null);
      }
    }
    else if (_property.startsWith("camera")) { // All camera properties, except projectionMode which can't be changed interactively
      if (_property.equals("cameraX") || _property.equals("cameraY") || _property.equals("cameraZ") ) {
        cameraLocationSet = true;  // Use Camera location instead of angles
      }
      if (_value!=null) {
        Value constant = Value.parseConstant(_value, true);
        if (!reportCameraMotion) reportCameraMotion = (constant == null);
//        System.err.println ("Report camera = "+reportCameraMotion+ " value = "+_value);
      }
    }
    else if (_property.equals("keyAction") || _property.equals("keyPressed")) { // All key properties set the reportKey boolean
      if (_value!=null) {
        Value constant = Value.parseConstant(_value, true);
        if (!reportKey) reportKey = (constant == null);
      }
    }
    return super.setProperty(_property,_value);
  }


  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("3DCameraMode")>=0) {
      _value = _value.trim().toLowerCase();
//      while (_value.startsWith("\"")) _value = _value.substring(1);
//      while (_value.endsWith("\""))   _value = _value.substring(0,_value.length()-1);
      if (_value.equals("no_perspective"))  return new IntegerValue (Camera.MODE_NO_PERSPECTIVE);
      if (_value.equals("perspective_off")) return new IntegerValue (Camera.MODE_PERSPECTIVE_OFF);
      if (_value.equals("perspective_on"))  return new IntegerValue (Camera.MODE_PERSPECTIVE_ON);
      if (_value.equals("perspective"))     return new IntegerValue (Camera.MODE_PERSPECTIVE);
      if (_value.equals("planar_xy"))       return new IntegerValue (Camera.MODE_PLANAR_XY);
      if (_value.equals("planar_xz"))       return new IntegerValue (Camera.MODE_PLANAR_XZ);
      if (_value.equals("planar_yz"))       return new IntegerValue (Camera.MODE_PLANAR_YZ);
    }
    if (_propertyType.indexOf("3DDecorationType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("none")) return new IntegerValue (VisualizationHints.DECORATION_NONE);
      if (_value.equals("axes")) return new IntegerValue (VisualizationHints.DECORATION_AXES);
      if (_value.equals("centered_axes")) return new IntegerValue (VisualizationHints.DECORATION_CENTERED_AXES);
      if (_value.equals("cube")) return new IntegerValue (VisualizationHints.DECORATION_CUBE);
    }
    if (_propertyType.indexOf("3DCursorType")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("none")) return new IntegerValue (VisualizationHints.CURSOR_NONE);
      if (_value.equals("cube")) return new IntegerValue (VisualizationHints.CURSOR_CUBE);
      if (_value.equals("crosshair")) return new IntegerValue (VisualizationHints.CURSOR_CROSSHAIR);
      if (_value.equals("xyz")) return new IntegerValue (VisualizationHints.CURSOR_XYZ);
    }
    if (_propertyType.indexOf("3DShowCoordinates")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("none"))         return new IntegerValue (-1);
      if (_value.equals("bottom_left"))  return new IntegerValue (DrawingPanel3D.BOTTOM_LEFT);
      if (_value.equals("bottom_right")) return new IntegerValue (DrawingPanel3D.BOTTOM_RIGHT);
      if (_value.equals("top_left"))     return new IntegerValue (DrawingPanel3D.TOP_LEFT);
      if (_value.equals("top_right"))    return new IntegerValue (DrawingPanel3D.TOP_RIGHT);
    }
    if (_propertyType.indexOf("3DImplementation")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("simple3d")) return new IntegerValue (DrawingPanel3D.IMPLEMENTATION_SIMPLE3D);
      if (_value.equals("java3d"))   return new IntegerValue (DrawingPanel3D.IMPLEMENTATION_JAVA3D);
    }
    if (_propertyType.indexOf("3DAxesMapping")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("xyz")) return new IntegerValue (Mapping.MAP_XYZ);
      if (_value.equals("xzy")) return new IntegerValue (Mapping.MAP_XZY);
      if (_value.equals("yxz")) return new IntegerValue (Mapping.MAP_YXZ);
      if (_value.equals("yzx")) return new IntegerValue (Mapping.MAP_YZX);
      if (_value.equals("zxy")) return new IntegerValue (Mapping.MAP_ZXY);
      if (_value.equals("zyx")) return new IntegerValue (Mapping.MAP_ZYX);
    }
    if (_propertyType.indexOf("3DDraggable")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("none"))     return new IntegerValue (DrawingPanel3D.DRAG_NONE);
      if (_value.equals("any"))      return new IntegerValue (DrawingPanel3D.DRAG_ANY);
      if (_value.equals("azimuth"))  return new IntegerValue (DrawingPanel3D.DRAG_AZIMUTH);
      if (_value.equals("altitude")) return new IntegerValue (DrawingPanel3D.DRAG_ALTITUDE);
    }
    return super.parseConstant (_propertyType,_value);
  }

  private void setAxesLabels (String _labelsStr) {
    String[] labels = new String[]{"X","Y","Z"};
    if (_labelsStr!=null) {
      java.util.StringTokenizer tkn = new java.util.StringTokenizer(_labelsStr,",;");
      if (tkn.hasMoreTokens()) labels[0] = tkn.nextToken();
      if (tkn.hasMoreTokens()) labels[1] = tkn.nextToken();
      if (tkn.hasMoreTokens()) labels[2] = tkn.nextToken();
    }
    drawingPanel3D.getVisualizationHints().setAxesLabels (labels);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
//    if (_index==7) 
//      System.err.println ("Setting "+_index+" to "+_value);
    double angle;
    switch (_index) {
      case  0 : if (_value.getDouble()!=minX) { minX=_value.getDouble(); resetExtrema(); } break;
      case  1 : if (_value.getDouble()!=maxX) { maxX=_value.getDouble(); resetExtrema(); } break;
      case  2 : if (_value.getDouble()!=minY) { minY=_value.getDouble(); resetExtrema(); } break;
      case  3 : if (_value.getDouble()!=maxY) { maxY=_value.getDouble(); resetExtrema(); } break;
      case  4 : if (_value.getDouble()!=minZ) { minZ=_value.getDouble(); resetExtrema(); } break;
      case  5 : if (_value.getDouble()!=maxZ) { maxZ=_value.getDouble(); resetExtrema(); } break;
      case  6 : if (_value.getBoolean()!=drawingPanel3D.isSquareAspect()) drawingPanel3D.setSquareAspect  (_value.getBoolean()); break;

      case PROJECTION : if (_value.getInteger()!=camera.getProjectionMode()) camera.setProjectionMode(_value.getInteger()); break;
      case  8 : if (_value.getDouble()!=cameraValues[0].value) camera.setFocusXYZ(cameraValues[0].value=_value.getDouble(),
                                                                                  cameraValues[1].value,
                                                                                  cameraValues[2].value); break;
      case  9 : if (_value.getDouble()!=cameraValues[1].value) camera.setFocusXYZ(cameraValues[0].value,
                                                                                  cameraValues[1].value=_value.getDouble(),
                                                                                  cameraValues[2].value); break;
      case 10 : if (_value.getDouble()!=cameraValues[2].value) camera.setFocusXYZ(cameraValues[0].value,
                                                                                  cameraValues[1].value,
                                                                                  cameraValues[2].value=_value.getDouble()); break;

      case 11 : if (_value.getDouble()!=cameraValues[3].value) camera.setDistanceToScreen(cameraValues[3].value=_value.getDouble()); break;
      case 12 : if (_value instanceof IntegerValue) angle = _value.getInteger()*TO_RAD;
                 else angle = _value.getDouble();
                 if (angle!=cameraValues[4].value)  camera.setAzimuth(cameraValues[4].value=_value.getDouble());
                 break;
      case 13 : if (_value instanceof IntegerValue) angle = _value.getInteger()*TO_RAD;
                else angle = _value.getDouble();
                if (angle!=cameraValues[5].value) camera.setAltitude(cameraValues[5].value=_value.getDouble());
                break;

      case 14 : if (_value.getInteger()!=drawingPanel3D.getImplementation()) drawingPanel3D.setImplementation(_value.getInteger()); break;
      case 15 : if (_value.getInteger()!=visHints.getDecorationType())   visHints.setDecorationType(_value.getInteger()); break;
      case 16 : if (_value.getInteger()!=visHints.getCursorType())       visHints.setCursorType(_value.getInteger()); break;
      case 17 : if (_value.getBoolean()!=visHints.isRemoveHiddenLines()) visHints.setRemoveHiddenLines(_value.getBoolean()); break;
      case 18 : if (_value.getBoolean()!=visHints.isAllowQuickRedraw())  visHints.setAllowQuickRedraw(_value.getBoolean()); break;
      case 19 : if (_value.getBoolean()!=visHints.isUseColorDepth())     visHints.setUseColorDepth(_value.getBoolean()); break;
      case 20 : if (_value.getInteger()!=visHints.getShowCoordinates())  visHints.setShowCoordinates(_value.getInteger()); break;
      case 21 :
        String formatX = _value.getString();
        if (formatX==null) visHints.setXFormat(null);
        else if (!formatX.equals(visHints.getXFormat())) visHints.setXFormat(formatX);
        break;
      case 22 :
          String formatY = _value.getString();
          if (formatY==null) visHints.setYFormat(null);
          else if (!formatY.equals(visHints.getYFormat())) visHints.setYFormat(formatY);
          break;
      case 23 :
          String formatZ = _value.getString();
          if (formatZ==null) visHints.setZFormat(null);
          else if (!formatZ.equals(visHints.getZFormat())) visHints.setZFormat(formatZ);
          break;

      case 24 : posValues[0].value = _value.getDouble(); break;
      case 25 : posValues[1].value = _value.getDouble(); break;
      case 26 : posValues[2].value = _value.getDouble(); break;

      case 27 : // movedaction
        removeAction (ControlSwingElement.MOUSE_MOVED_ACTION,getProperty("movedAction"));
        addAction(ControlSwingElement.MOUSE_MOVED_ACTION,_value.getString());
        break;
      case 28 : // pressaction
        removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressAction"));
        addAction(ControlSwingElement.ACTION_PRESS,_value.getString());
        break;
      case 29 : // releaseaction
        removeAction (ControlElement.ACTION,getProperty("releaseAction"));
        addAction(ControlElement.ACTION,_value.getString());
        break;
      case 30 : // dragaction
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragAction"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        break;
      case 31 : // mouse entered action
        removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction"));
        addAction(ControlSwingElement.MOUSE_ENTERED_ACTION,_value.getString());
        break;
      case 32 : // mouse exited action
        removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction"));
        addAction(ControlSwingElement.MOUSE_EXITED_ACTION,_value.getString());
        break;
      case 33 : // keyaction
        removeAction (ControlSwingElement.KEY_ACTION,getProperty("keyAction"));
        addAction(ControlSwingElement.KEY_ACTION,_value.getString());
        break;
      case 34 : keyPressedValue.value = _value.getInteger(); break;
      case 35 : setAxesLabels (_value.toString()); break;

      case 36 : if (_value.getDouble()!=cameraValues[6].value) camera.setXYZ(cameraValues[6].value=_value.getDouble(),
                                                                             cameraValues[7].value,
                                                                             cameraValues[8].value); break;
      case 37 : if (_value.getDouble()!=cameraValues[7].value) camera.setXYZ(cameraValues[6].value,
                                                                             cameraValues[7].value=_value.getDouble(),
                                                                             cameraValues[8].value); break;
      case 38 : if (_value.getDouble()!=cameraValues[8].value) camera.setXYZ(cameraValues[6].value,
                                                                             cameraValues[7].value,
                                                                             cameraValues[8].value=_value.getDouble()); break;

      case 39 : {
          double rotAngle;
          if (_value instanceof IntegerValue) rotAngle = _value.getInteger()*ControlDrawingPanel3D.TO_RAD;
          else rotAngle = _value.getDouble();
          if (rotAngle!=cameraValues[9].value) camera.setRotation(cameraValues[9].value=rotAngle); break;
      }
      
      case 40 : visHints.setBackgroundImage(_value.getString()); break;

      case 41 : camera.setMapping(_value.getInteger()); break;
      case 42 : drawingPanel3D.setAxesSize(axisXSize = _value.getDouble(), axisYSize, axisZSize); break;
      case 43 : drawingPanel3D.setAxesSize(axisXSize, axisYSize = _value.getDouble(), axisZSize); break;
      case 44 : drawingPanel3D.setAxesSize(axisXSize, axisYSize, axisZSize = _value.getDouble()); break;

      case 45 : visHints.setDefaultIllumination(_value.getBoolean()); break; 
      case 46 : visHints.setBackgroundMoveable(_value.getBoolean()); break; 
      
      case 47 : 
        if (_value instanceof BooleanValue) drawingPanel3D.setDraggable(_value.getBoolean()); 
        else drawingPanel3D.setDraggable(_value.getInteger());
        break;
        
      case 48 : drawingPanel3D.setResetCameraOnChanges(_value.getBoolean()); break; 

      default: super.setValue(_index-DP3D_ADDED,_value); break;

      case PANEL_ENABLED : drawingPanel3D.getInteractionTarget(0).setEnabled(_value.getBoolean()); break;
      case PANEL_BACKGROUND : 
        if (_value.getObject() instanceof Color) visHints.setBackgroundColor((Color) _value.getObject()); 
        else visHints.setBackgroundColor(DisplayColors.getLineColor(_value.getInteger()));
        break;
      case PANEL_FOREGROUND : 
        if (_value.getObject() instanceof Color) visHints.setForegroundColor((Color) _value.getObject()); 
        else visHints.setForegroundColor(DisplayColors.getLineColor(_value.getInteger()));
        break;
      case PANEL_FONT : if (_value.getObject() instanceof Font) visHints.setFont((Font) _value.getObject()); break;
    }
    if (isUnderEjs) {
      if (cameraInspector!=null) {
        if (_index==PROJECTION) cameraInspector.updateFields();
        else for (int i=0; i<CAMERA_INDEXES.length; i++) if (CAMERA_INDEXES[i]==_index) { cameraInspector.updateFields(); break; }
      }
      drawingPanel3D.getImplementingPanel().update();
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  0 : minX=-1.0; resetExtrema(); break;
      case  1 : maxX= 1.0; resetExtrema(); break;
      case  2 : minY=-1.0; resetExtrema(); break;
      case  3 : maxY= 1.0; resetExtrema(); break;
      case  4 : minZ=-1.0; resetExtrema(); break;
      case  5 : maxZ= 1.0; resetExtrema(); break;
      case  6 : drawingPanel3D.setSquareAspect (true); break;

      case  PROJECTION : camera.setProjectionMode(Camera.MODE_PERSPECTIVE_ON); break;
      case  8 : // Any default resets the camera NOT ANY MORE
      case  9 :
      case 10 :
      case 11 :
      case 12 :
      case 13 :
      case 39 :
          //camera.reset();
          //reportCameraMotion();
          break;

      case 36 :
      case 37 :
      case 38 :
        break;
      case 14 : 
        drawingPanel3D.setImplementation(DrawingPanel3D.IMPLEMENTATION_SIMPLE3D); 
        setListeners();
        super.changeVisual(drawingPanel3D.getComponent());
        break;
      case 15 : visHints.setDecorationType(VisualizationHints.DECORATION_CUBE); break;
      case 16 : visHints.setCursorType(VisualizationHints.CURSOR_XYZ); break;
      case 17 : visHints.setRemoveHiddenLines(true); break;
      case 18 : visHints.setAllowQuickRedraw(true); break;
      case 19 : visHints.setUseColorDepth(true); break;
      case 20 : visHints.setShowCoordinates(DrawingPanel3D.BOTTOM_LEFT); break;
      case 21 : visHints.setXFormat("x = 0.00;x = -0.00"); break;
      case 22 : visHints.setYFormat("y = 0.00;y = -0.00"); break;
      case 23 : visHints.setZFormat("z = 0.00;z = -0.00"); break;

      case 24 : posValues[0].value = (minX+maxX)/2.0; break;
      case 25 : posValues[1].value = (minY+maxY)/2.0; break;
      case 26 : posValues[2].value = (minZ+maxZ)/2.0; break;
      case 27 : removeAction (ControlSwingElement.MOUSE_MOVED_ACTION,getProperty("movedAction")); break;
      case 28 : removeAction (ControlSwingElement.ACTION_PRESS,getProperty("pressAction")); break;
      case 29 : removeAction (ControlElement.ACTION,getProperty("releaseAction"));                break;
      case 30 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("dragAction"));   break;
      case 31 : removeAction (ControlSwingElement.MOUSE_ENTERED_ACTION,getProperty("enteredAction")); break;
      case 32 : removeAction (ControlSwingElement.MOUSE_EXITED_ACTION,getProperty("exitedAction")); break;
      case 33 : removeAction (ControlSwingElement.KEY_ACTION,getProperty("keyAction")); break;
      case 34 : keyPressedValue.value = -1; break;
      case 35 : setAxesLabels (null); break;

      case 40 : visHints.setBackgroundImage((String)null); break;

      case 41 : camera.setMapping(Mapping.MAP_XYZ); break;
      case 42 : drawingPanel3D.setAxesSize(Double.NaN, axisYSize, axisZSize); break;
      case 43 : drawingPanel3D.setAxesSize(axisXSize, Double.NaN, axisZSize); break;
      case 44 : drawingPanel3D.setAxesSize(axisXSize, axisYSize, Double.NaN); break;
      
      case 45 : visHints.setDefaultIllumination(true); break;
      case 46 : visHints.setBackgroundMoveable(true); break;

      case 47 : drawingPanel3D.setDraggable(DrawingPanel3D.DRAG_ANY); break; 

      case 48 : drawingPanel3D.setResetCameraOnChanges(true); break; 

      default: super.setDefaultValue(_index-DP3D_ADDED); break;
      case PANEL_ENABLED : drawingPanel3D.getInteractionTarget(0).setEnabled(false); break;
      case PANEL_BACKGROUND : visHints.setBackgroundColor(defBackground); break; 
      case PANEL_FOREGROUND : visHints.setForegroundColor(defForeground); break; 
      case PANEL_FONT : visHints.setFont(defFont); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  0 : return "-1.0";
      case  1 : return "1.0";
      case  2 : return "-1.0";
      case  3 : return "1.0";
      case  4 : return "-1.0";
      case  5 : return "1.0";
      case  6 : return "true";

      case  PROJECTION : return "PERSPECTIVE_ON";
      case  8 : // Any default resets the camera NOT ANY MORE
      case  9 :
      case 10 :
      case 11 :
      case 12 :
      case 13 :
      case 39 : 
      case 36 :
      case 37 :
      case 38 : return "<none>";
      case 14 : return "SIMPLE3D"; 
      case 15 : return "CUBE";
      case 16 : return "XYZ";
      case 17 : 
      case 18 : 
      case 19 : return "true";
      case 20 : return "BOTTOM_LEFT";
      case 21 : return "x = 0.00;x = -0.00";
      case 22 : return "y = 0.00;y = -0.00";
      case 23 : return "z = 0.00;z = -0.00";

      case 24 : 
      case 25 : 
      case 26 : return "<none>";
      case 27 : 
      case 28 : 
      case 29 : 
      case 30 : 
      case 31 : 
      case 32 : 
      case 33 : return "<no_action>";
      case 34 : return "-1";
      case 35 : return "<none>";

      case 40 : return "<none>";

      case 41 : return "xyz";
      case 42 : 
      case 43 : 
      case 44 : return "<none>";
      
      case 45: return "true";
      case 46: return "true";
      
      case 47: return "ANY";
      case 48: return "true";

      default: return super.getDefaultValueString(_index-DP3D_ADDED); 
      case PANEL_ENABLED : return "false";
      case PANEL_BACKGROUND : return defBackground.toString(); 
      case PANEL_FOREGROUND : return defForeground.toString(); 
      case PANEL_FONT : return defFont.toString();

    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case  0 : case  1 : case  2 : case  3 :
      case  4 : case  5 : case  6 : case  PROJECTION :
        return null;

      case  8 : return cameraValues[0];
      case  9 : return cameraValues[1];
      case 10 : return cameraValues[2];
      case 11 : return cameraValues[3];
      case 12 : return cameraValues[4];
      case 13 : return cameraValues[5];
      case 36 : return cameraValues[6];
      case 37 : return cameraValues[7];
      case 38 : return cameraValues[8];
      case 39 : return cameraValues[9];

      case 14 : case 15 : case 16 : case 17 :
      case 18 : case 19 : case 20 : case 21 :
      case 22 : case 23 : return null;

      case 24 : return posValues[0];
      case 25 : return posValues[1];
      case 26 : return posValues[2];

      case 27 : case 28 : case 29 : case 30 :
      case 31 : case 32 : case 33 : 
        return null;
      case 34 : return keyPressedValue;
      case 35 : return null;
      case 40 : case 41 : case 42 : case 43 : case 44 : 
      case 45 : case 46 : case 47 : case 48 : 
        return null;
      default: return super.getValue(_index-DP3D_ADDED);
      case PANEL_ENABLED : return null;
    }
  }

// -------------------------------------
// Respond to interaction
// -------------------------------------

  private boolean settingExtrema = false;

  private void resetExtrema () {
    settingExtrema = true;
    drawingPanel3D.setPreferredMinMax(minX,maxX,minY,maxY,minZ,maxZ);
    reportCameraMotion();
  }

  private void reportCameraMotion () {
    if (settingExtrema) {
      camera.setDistanceToScreen(cameraValues[3].value);
      if (cameraLocationSet) camera.setXYZ(cameraValues[6].value,cameraValues[7].value,cameraValues[8].value);
      else camera.setAzimuthAndAltitude(cameraValues[4].value,cameraValues[5].value);
      camera.setRotation(cameraValues[9].value);
      camera.setFocusXYZ(cameraValues[0].value,cameraValues[1].value,cameraValues[2].value);
    }
    else {
      cameraValues[0].value = camera.getFocusX();
      cameraValues[1].value = camera.getFocusY();
      cameraValues[2].value = camera.getFocusZ();
      cameraValues[3].value = camera.getDistanceToScreen();
      cameraValues[4].value = camera.getAzimuth();
      cameraValues[5].value = camera.getAltitude();
      cameraValues[6].value = camera.getX();
      cameraValues[7].value = camera.getY();
      cameraValues[8].value = camera.getZ();
      cameraValues[9].value = camera.getRotation();
    }
//    System.err.println ("Must Report camera = "+reportCameraMotion+ " set extr = "+settingExtrema);

    if (reportCameraMotion) { // Report only is someone is interested
      variablesChanged(CAMERA_INDEXES, cameraValues);
    }
    if (isUnderEjs) {
      if (!editorIsReading ()) {
        if (cameraLocationSet) {
          CAMERA_PARTIAL_INDEXES[4] = -1; // This index will be ignored
          CAMERA_PARTIAL_INDEXES[5] = -1; // This index will be ignored
          CAMERA_PARTIAL_INDEXES[6] = 36;
          CAMERA_PARTIAL_INDEXES[7] = 37;
          CAMERA_PARTIAL_INDEXES[8] = 38;
        }
        else {
          CAMERA_PARTIAL_INDEXES[4] = 12;
          CAMERA_PARTIAL_INDEXES[5] = 13;
          CAMERA_PARTIAL_INDEXES[6] = -1; // This index will be ignored
          CAMERA_PARTIAL_INDEXES[7] = -1; // This index will be ignored
          CAMERA_PARTIAL_INDEXES[8] = -1; // This index will be ignored
        }
        setFieldListValues(CAMERA_PARTIAL_INDEXES, cameraValues);
      }
      drawingPanel3D.render();
    }
    settingExtrema = false;
  }


  private void updateCameraFields() {
    reportCameraMotion();
    switch (camera.getProjectionMode()) {
      case Camera.MODE_PLANAR_XY:      setFieldListValue (PROJECTION,new StringValue("PLANAR_XY")); break;
      case Camera.MODE_PLANAR_XZ:      setFieldListValue (PROJECTION,new StringValue("PLANAR_XZ")); break;
      case Camera.MODE_PLANAR_YZ:      setFieldListValue (PROJECTION,new StringValue("PLANAR_YZ")); break;
      case Camera.MODE_NO_PERSPECTIVE: case Camera.MODE_PERSPECTIVE_OFF : 
        setFieldListValue (PROJECTION,new StringValue("PERSPECTIVE_OFF")); break;
      default:
      case Camera.MODE_PERSPECTIVE: case Camera.MODE_PERSPECTIVE_ON :    
        setFieldListValue (PROJECTION,new StringValue("PERSPECTIVE_ON")); break;
    }
  }


  private void reportMouseMotion (Object _info) {
    if (_info==null || !(_info instanceof double[]) ) return;
    double[] point = (double []) _info;
    posValues[0].value = point[0];
    posValues[1].value = point[1];
    posValues[2].value = point[2];
    if (reportXYZMotion) { // Report only is someone is interested
      variablesChanged(POSITION_INDEXES, posValues);
    }
    if (isUnderEjs) setFieldListValues(POSITION_INDEXES, posValues);
  }

  public void interactionPerformed(InteractionEvent _event) {
//     System.out.println("Event ID "+_event.getID());
    switch (_event.getID()) {
      case InteractionEvent.MOUSE_ENTERED  : invokeActions (ControlSwingElement.MOUSE_ENTERED_ACTION); break;
      case InteractionEvent.MOUSE_EXITED   : invokeActions (ControlSwingElement.MOUSE_EXITED_ACTION);  break;
      case InteractionEvent.MOUSE_MOVED    :
        reportMouseMotion (_event.getInfo());
        invokeActions (ControlSwingElement.MOUSE_MOVED_ACTION);
        break;
      case InteractionEvent.MOUSE_PRESSED  :
          drawingPanel3D.getComponent().requestFocus();
          reportMouseMotion (_event.getInfo());
          invokeActions (ControlSwingElement.ACTION_PRESS);
          break;
      case InteractionEvent.MOUSE_DRAGGED  :
          if (_event.getInfo()==null) reportCameraMotion();
          else reportMouseMotion (_event.getInfo());
          //invokeActions (ControlSwingElement.MOUSE_DRAGGED_ACTION);
          break;
      case InteractionEvent.MOUSE_RELEASED : invokeActions (ControlSwingElement.ACTION);               break;
    }
  } // End of interaction method


} // End of class

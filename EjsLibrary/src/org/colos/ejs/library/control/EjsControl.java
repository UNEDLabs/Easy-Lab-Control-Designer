/*
 * The control package contains utilities to build and control
 * simulations using a central control.
 * Copyright (c) July 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control;

import java.util.*;
import java.awt.*;
import java.io.File;

import javax.swing.*;
import javax.swing.event.*;

import java.net.URL;
import org.colos.ejs.library.*;
import org.colos.ejs.library.control.swing.*;
import org.colos.ejs.library.control.value.*;
import org.colos.ejs.library.utils.LocaleItem;
import org.opensourcephysics.tools.*;
import org.opensourcephysics.desktop.OSPDesktop;

/**
 * A base class to group several ControlElements, connect them
 * to one or more target objects in a unified form, and build a
 * graphic interface with all of them.
 */
// Note for myself: This class is still very much dependent on the actual
// implementation of some of the particular subclasses of ControlElement,
// like ControlContainer, ControlFrame and ControlDialog.

public class EjsControl {
  static private String _RETURN_;
  static private GraphicsConfiguration defaultScreenGraphicsConfiguration = getGraphicsConfiguration(-1);
  static private int borderWidthAroundWindows=0;
  static private Icon borderIconAroundWindows=null;
  static private Color borderColorAroundWindows=Color.RED;
  static private Color borderTitleColorAroundWindows=Color.BLACK;
  static private String borderTitleAroundWindows=null;

  private String replaceOwnerName=null;
  private   Frame ownerFrame=null;
  private Frame replaceOwnerFrame=null;
  private Component mainRootPane = null; // The JRootPane of the main window
  
  private JTextArea messageArea=null;

  private boolean mustUpdateSimulation = true;
  private boolean justCollectingData=false;
  private Simulation mySimulation  = null;
  private Hashtable<String,Object>  targetTable   = new Hashtable<String,Object>();
  private Hashtable<String,ControlElement>  elementTable  = new Hashtable<String,ControlElement>();
  Hashtable<String,GroupVariable>  variableTable   = new Hashtable<String,GroupVariable>();
  private Vector<ControlElement> elementList       = new Vector<ControlElement>();
  private Vector<NeedsUpdate> updateList           = new Vector<NeedsUpdate>();
  private Vector<NeedsFinalUpdate> finalUpdateList = new Vector<NeedsFinalUpdate>();
  private URL usercodebase = null;
  private Point displacement = new Point(0,0);
  private boolean reportingChange = false;

  GroupVariable methodTriggerVariable = null; // AMAVP (See Note in ControlElement)

  static {
    org.opensourcephysics.tools.ResourceLoader.setCacheEnabled(true);
    try { _RETURN_ = System.getProperty("line.separator"); }
	  catch (Exception e) { _RETURN_ = "\n"; }
  }

  /**
   * Used by EJS to signal the control is under EJS
   */
  static public void setBorderWidthAroundWindows(int _width) { borderWidthAroundWindows=_width; }
  /**
   * Used by element init methods to know if the control is under EJS
   * @return
   */
  static public int getBorderWidthAroundWindows() { return borderWidthAroundWindows; }

  /**
   * Used by EJS to signal the control is under EJS
   */
  static public void setBorderIconAroundWindows(Icon _icon) { borderIconAroundWindows=_icon; }
  /**
   * Used by element init methods to know if the control is under EJS
   * @return
   */
  static public Icon getBorderIconAroundWindows() { return borderIconAroundWindows; }

  /**
   * Used by EJS to signal the control is under EJS
   */
  static public void setBorderColorAroundWindows(Color _color) { borderColorAroundWindows=_color; }
  /**
   * Used by element init methods to know if the control is under EJS
   * @return
   */
  static public java.awt.Color getBorderColorAroundWindows() { return borderColorAroundWindows; }

  /**
   * Used by EJS to signal the control is under EJS
   */
  static public void setBorderTitleColorAroundWindows(Color _color) { borderTitleColorAroundWindows=_color; }
  /**
   * Used by element init methods to know if the control is under EJS
   * @return
   */
  static public java.awt.Color getBorderTitleColorAroundWindows() { return borderTitleColorAroundWindows; }

  /**
   * Used by EJS to signal the control is under EJS
   */
  static public void setBorderTitleAroundWindows(String _title) { borderTitleAroundWindows=_title; }
  /**
   * Used by element init methods to know if the control is under EJS
   * @return
   */
  static public String getBorderTitleAroundWindows() { return borderTitleAroundWindows; }

  /**
   * Whether Java3D is installed in this computer
   * @return
   */
  static public boolean hasJava3D() { return org.opensourcephysics.display.OSPRuntime.hasJava3D(); }
  

 /**
  * The default constructor.
  */
  public EjsControl () {
    setValue (ControlElement.METHOD_TRIGGER,new BooleanValue(false)); // AMAVP
    methodTriggerVariable = variableTable.get(ControlElement.METHOD_TRIGGER); // AMAVP
  }

 /**
  * The constructor.
  * @param     Object _target  The object that will receive the
  *   actions from the ControlElements in this group.
  */
  public EjsControl (Object _target) {
    this();
    addTarget("_default_",_target);
    if (_target instanceof Simulation) setSimulation ((Simulation)_target);
  }

 /**
  * A specialized constructor for Ejs use.
  * This adds elements to it in the usual way, but replaces a Frame element
  * of a given name by the prescribed frame.
  * @param     Object _simulation  The object that will receive the
  *   actions from the ControlElements in this group.
  * @param     String _replaceName  The name of the Frame that will be replaced
  * @param     java.awt.Frame _replaceOwnerFrame  The Frame that will replace the frame named above
  */
  public EjsControl (Object _simulation, String _replaceName, Frame _replaceOwnerFrame) {
    this(_simulation);
    replaceOwnerFrame (_replaceName,_replaceOwnerFrame);
  }

// ------------------------------------------------
// Preliminary things
// ------------------------------------------------

  /**
   * Returns the bounds of a given screen number
   * @param _screen
   * @return
   */
  static public GraphicsConfiguration getGraphicsConfiguration (int _screen) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gds = ge.getScreenDevices();
    if (_screen>=gds.length || _screen<0) return ge.getDefaultScreenDevice().getDefaultConfiguration();
    return gds[_screen].getDefaultConfiguration();
  }

  /**
   * Sets the default screen number
   */
  static public void setDefaultScreen(int _screen) { defaultScreenGraphicsConfiguration = getGraphicsConfiguration(_screen); }
  
  /**
   * Returns the graphics configuration of the current default screen
   * @return
   */
  static public GraphicsConfiguration getDefaultGraphicsConfiguration() { return defaultScreenGraphicsConfiguration; }
  
  /**
   * Returns the bounds of the current default screen
   * @return
   */
  static public Rectangle getDefaultScreenBounds() {
    Rectangle bounds = defaultScreenGraphicsConfiguration.getBounds();
//    System.out.println ("Bounds are:"+bounds.x+","+bounds.y+" with size: "+bounds.width+","+bounds.height);
    return bounds; 
  }
  
 /**
  * Sets the owner frame for all subsequent Dialogs
  * @param     Frame _frame  The frame that should own next Dialogs
  *   (if there are Dialogs in this group)
  */
  public void setOwnerFrame (Frame _frame) { ownerFrame = _frame; }

 /**
  * Returns the owner frame for all subsequent Dialogs
  */
  public Frame getOwnerFrame () { return ownerFrame; }

  public void replaceOwnerFrame (String _replaceName, Frame _replaceOwnerFrame) {
    replaceOwnerName  = _replaceName;
    replaceOwnerFrame = _replaceOwnerFrame;
  }

 /**
  * Returns the name of the replacement for the owner frame for all subsequent Dialogs
  */
  public String getReplaceOwnerName () { return replaceOwnerName; }

 /**
  * Returns the replacement for the owner frame for all subsequent Dialogs
  */
  public Frame getReplaceOwnerFrame () { return replaceOwnerFrame; }

 /**
  * Sets the simulation under which the control is running
  * This is used to up date the simulation whenever an Element changes a
  * variable (See variableChanged in ControlElement)
  * @param     Simulation _sim  The simulation
  */
  public void setSimulation (Simulation _sim) { mySimulation = _sim; }

 /**
  * Returns the simulation under which the control is running
  * This is used to up date the simulation whenever an Element changes a
  * variable (See variableChanged in ControlElement
  */
  public Simulation getSimulation () { return mySimulation; }

  /**
   * Returns the locale of the simulation
   * @return
   */
  public Locale getLocale() { return mySimulation.getLocale(); }

  /**
   * Returns the locale of the simulation
   * @return
   */
  public LocaleItem getLocaleItem() { return mySimulation.getLocaleItem(); }

  /**
   * Returns the locale language of the simulation ('es', 'en', 'fr', etc.)
   * @return
   */
  public String getLocaleLanguage() { return mySimulation.getLocale().getLanguage(); }

  /**
   * Returns the locale of the simulation
   * @return
   */
  public void setLocale(String _language) { mySimulation.setLocale(_language); }

  /**
   * Whether the simulation must call update when variables change.
   * Also, view elements actions are disabled if this value is false.
   * This should be set to false at start up of Ejs
   * @param     boolean _value The value desired
   */
   public void setUpdateSimulation (boolean _value) { mustUpdateSimulation = _value; }
      
   boolean isUpdatingSimulation() { return mustUpdateSimulation; }
   
   /**
    * Updates the simulation (if it should)
    */
//   public void updateSimulation() { if (mustUpdateSimulation && mySimulation!=null) mySimulation.update(); }

   public void updateSimulationWhenIdle() { if (mustUpdateSimulation && mySimulation!=null) mySimulation.updateWhenIdle(); }

//  public boolean isCollectingData () { return this.justCollectingData; }

  public void addElementsMenuEntries() {
    for (ControlElement element : elementList) element.addMenuEntries();
  }

  /**
   * Whether the element is in the middle of a variableChanged report
   * @return
   */
  final public boolean isReportingChange() { return reportingChange; }

  /**
   * Whether the element is in the middle of a variableChanged report
   * @return
   */
  final public void setReportingChange(boolean reporting) { reportingChange = reporting; }
  
// -----------------------------------------
// Clone related methods
// -----------------------------------------

  public void setDisplacement (int x, int y) { setDisplacement(x,y,null); }

  public void setDisplacement (int x, int y, EjsControl reference) {
    if (reference!=null) {
      displacement.x = x+reference.displacement.x;
      displacement.y = y+reference.displacement.y;
    }
    else {
      displacement.x = x;
      displacement.y = y;
    }
    for (Enumeration<ControlElement> e = elementList.elements() ; e.hasMoreElements() ;) {
      ControlElement element = e.nextElement();
      if (element instanceof ControlWindow) ((ControlWindow) element).updateLocation();
    }

  }

  public Point getDisplacement () { return displacement; }

  private Hashtable<ControlDrawable,ControlParentOfDrawables> drawablesReparented = new Hashtable<ControlDrawable,ControlParentOfDrawables> ();

  /**
   * Reparents the ControlElement of the given name to the parentElement
   * @param _childName String
   * @param _parentElement ControlElement
   * @return true if successfull, false otherwise
   */
  public boolean reparentDrawable (String _childName, ControlElement _parentElement) {
    ControlElement child = getElement(_childName);
    if (child instanceof ControlDrawable && _parentElement instanceof ControlParentOfDrawables) {
      ControlDrawable drawable = (ControlDrawable) child;
      drawablesReparented.put (drawable,drawable.getParent());
      drawable.setParent((ControlParentOfDrawables) _parentElement);
      return true;
    }
    javax.swing.JOptionPane.showMessageDialog(null, "Can't reparent drawable.\nOne of the elements is not of the right type",
                                                    "Error", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    return false;
  }

  public void undoReparenting () {
    for (Enumeration<ControlDrawable> el=drawablesReparented.keys(); el.hasMoreElements(); ){
      ControlDrawable element = el.nextElement();
      element.setParent(drawablesReparented.get(element));
    }
    drawablesReparented.clear();
  }

  /**
   * Disposes all windows in the view
   *
   public void dispose() {
     System.err.println("Calling EjsControl dispose");
     for (ControlElement element : elementList) {
       if (element instanceof ControlWindow) {
         System.err.println("Window in list: "+element);
       }
      }
     for (ControlElement element : elementList) {
      if (element instanceof ControlWindow) {
        ((ControlWindow) element).dispose();
      }
     }
   }*/

   public Vector<ControlElement> getElements() { return this.elementList; }
   
// ------------------------------------------------
// Targets
// ------------------------------------------------

 /**
  * Returns one of the registered target objects
  * @param   String _name  The name given to the target when it was added
  */
  public Object getTarget(String _name) { return targetTable.get(_name); }

 /**
  * Adds an object to be controlled. Actions can then refer to methods of the
  * form 'name.method:parameter'
  * @param   String _name  A name to refer to the target
  * @param   Object _target  A target object
  */
  public void addTarget(String _name, Object _target) { targetTable.put(_name,_target); }

 /**
  * Removes a target object
  */
  public void removeTarget(String _name) { targetTable.remove(_name); }

// --------------------------------------------------------
// Dealing with group variables
// --------------------------------------------------------

  public void setValue (String _name, Value _value) {
    setValue (_name,_value,false);
  }

  /**
   * Sets the group value for a variable. This includes the value in all
   * the elements of this group that are registered to this variable name.
   * @param   String _name  The variable name
   * @param   Value _value  The value as a <code>Value</code> object
   */
  public void setValue (String _name, Value _value, boolean _isModelVariable) {
    GroupVariable variable = variableTable.get(_name);
    if (variable==null) {
      variable = new GroupVariable(_name,_value);
      variable.setValueObsolete(false);
      variable.setDefinedInModel(_isModelVariable);
      variableTable.put(_name,variable);
    }
    else {
      variable.setValue(_value);
      variable.setValueObsolete(false);
      if (_isModelVariable) variable.setDefinedInModel(true);
      else; // leave it unchanged
      variable.propagateValue(null,this.justCollectingData);
    }
  }

 /**
  * Returns the group value of a variable.
  * @return the <code>Value</code> object of the variable. If the
  *         variable has never been set, it returns <b>null</b>.
  * @param  String _name  The variable name
  */
  public Value getValue (String _name) {
    GroupVariable variable = variableTable.get(_name);
    if (variable==null) return null;
    return variable.getValue();
  }

  /**
   * Returns the GroupVariable with this name, if registered.
   * @param _name String
   * @return GroupVariable
   */
  public GroupVariable getVariable (String _name) {
    return variableTable.get(_name);
  }

 /**
  * Associates an element internal value with a variable name. Later on,
  * when the user sets the value for this variable, either
  * programmatically or by interaction with a given element,
  * all registered elements will be informed of the change.
  * Invoked by ControlElements when processing the 'variable'
  * property. Not to be used directly by users.
  * @param     String _name  The name of the variable
  * @param     ControlElement _element  The element to be registered
  * @param     int _index  An indentifier for the element internal value
  * @param     Value _value The initial value if the variable doesn't already exist
  */
  public GroupVariable registerVariable (String _name, ControlElement _element,
                                         int _index, Value _value) {
    if (_name==null) return null;
//    System.out.println ("Registering variable "+_name);
    GroupVariable variable = variableTable.get(_name);
    if (variable==null) {
      variable = new GroupVariable(_name,_value);
      variableTable.put(_name,variable);
    }
//    else     System.out.println ("Already registered variable "+_name);

//    else variable.setValue(_value); // Commented means that the element takes
                                    // the actual value, whatever it is
    variable.addElementListener(_element,_index);
    variable.propagateValue(null,this.justCollectingData); //   null implies that the element takes the actual value
    return variable;
  }

 /**
  * Tells whether a variable is associated to any element.
  * Invoked by EjsControl's method 'setValue/getValue'.
  * Not to be used directly by users.
  * @param     ControlElement _element  The element to be included
  * @param     String _variable  The variable name
  */
  public boolean isVariableRegistered (String _name) {
    if (_name==null) return false;
    return (variableTable.get(_name) != null);
  }

 /**
  * Invoked by ControlElements when their internal variables change.
  * Not be used directly by users.
  */
  public void variableChanged(GroupVariable _variable, ControlElement _element, Value _value) {
    if (_variable==null) return;
    _variable.setValue(_value);
    _variable.propagateValue(_element,this.justCollectingData);
    _variable.invokeListeners(_element); // Call any registered listener
  }

  public void addListener (String _name) { addListener (_name, "apply(\""+_name+"\")", null); }

//  public void addListener (String _name, String _method) { addListener (_name, _method, null); }

 /**
  * Instructs the group to invoke a method (with an optional parameter) when a
  * variable changes.
  * @param  String _name   The name of the variable that may change
  * @param  String _method  The method that should be called in the controlled
  * @param Object _anObject the object to pass in the special case the method is method(#CONTROL#)
  * object.
  */
  public void addListener (String _name, String _method, Object _anObject) {
    if (_name==null) return;
    String [] parts = MethodWithOneParameter.splitMethodName(_method);
    if (parts==null) {
      System.err.println (getClass().getName()+" : Error! Listener <"+_method+"> not assigned");
      return;
    }
    if (parts[0]==null) parts[0] = "_default_";
    Object target = getTarget(parts[0]);
    if (target==null) {
      System.err.println (getClass().getName()+" : Error! Target <"+parts[0]+"> not assigned");
      return;
    }
    GroupVariable variable = variableTable.get(_name);
    if (variable==null) {
      variable = new GroupVariable(_name,doubleValue);
      variableTable.put(_name,variable);
    }
    if (parts[2]==null) variable.addListener(target,parts[1]+"()",_anObject);
    else variable.addListener(target,parts[1]+"("+parts[2]+")",_anObject);
  }

// --------------------------------------------------------
// Adding and removing control elements
// --------------------------------------------------------

 /**
  * Renaming a ControlElement
  * @param     String _name   The new name for the element.
  */
  public void rename (ControlElement _element, String _name) {
    String oldName = _element.getProperty("name");
    if (oldName!=null)   elementTable.remove(oldName);
    if (_name!=null) elementTable.put(_name,_element);
  }

 /**
  * Creates a new ControlElement with a given name
  * This is a special feature that is used by LauncherApplet, so that
  * if the name coincides with a given one, a Frame becomes a Panel,
  * so that it can be captured!
  * @param     String _type   The class name of the new element.
  * @param     String _name   The name of the new element.
  final public ControlElement addNamed (String _type, String _name) {
    String propertyList = "name="+_name;
    if (replaceOwnerName==null || !replaceOwnerName.equals(_name)) return addObject(null,_type,propertyList);
    if (_type.endsWith("ControlFrame") || _type.endsWith("ControlDrawingFrame") || _type.endsWith("ControlDrawingFrame3D")) {
      setOwnerFrame(replaceOwnerFrame);
//      return addObject(null,"org.colos.ejs.library.control.swing.ControlPanel",propertyList);
      return addObject(null,"org.colos.ejs.library.control.swing.ControlRootPane",propertyList);
    }
    return addObject(null,_type,propertyList);
  }
*/
  
  /**
   * Returns the main component of the view. I.e. the rootpane of the main window
   * @return
   */
  public Component getMainComponent() { return mainRootPane; }
    
 /**
  * Creates a new ControlElement
  * @param  String name   The name of the new element
  * @param ControlElement element The new element to add
  * @returns The element actually added. As a special feature used by LauncherApplet, 
  * if the name coincides with a given one, a ControlFrame may become a ControlRootPane,
  */
  final public ControlElement addElement (ControlElement element, String name) {
  	if (replaceOwnerName!=null && replaceOwnerName.equals(name)) { // Manage replaceOwnerFrame
	    if (element.getObject() instanceof Frame) {
	      setOwnerFrame (replaceOwnerFrame);
	      ControlWindow.removeFromWindowList((ControlWindow) element);
	    }
	    element = new org.colos.ejs.library.control.swing.ControlRootPane();
	    mainRootPane = element.getComponent();
  	}
	  Object object = element.getObject();
    if      (object instanceof Frame) setOwnerFrame((Frame) object); // Frames become automatically ownerFrames for subsequent Dialogs
    else if (object instanceof Dialog && ownerFrame!=null) element.replaceVisual(ownerFrame); // Use ownerFrame for Dialogs, if there is any
    else if (object instanceof JTextArea)  messageArea = (JTextArea) object;
    element.setGroup(this);
    element.setProperty("name", name);
    elementList.add(element);
    if (element instanceof NeedsUpdate) updateList.add((NeedsUpdate)element);
    if (element instanceof NeedsFinalUpdate) finalUpdateList.add((NeedsFinalUpdate)element);
    if (usercodebase!=null) element.setProperty("_ejs_codebase",usercodebase.toString());
    if (object instanceof Window) {  // Make windows visible by default
      if (element.getProperty("visible")==null) element.setProperty("visible","true");
    }
    return element;
  }

 /**
  * Returns a control element by name
  * @return    the ControlElement if found, null otherwise.
  * @param     String _name  The name of the control element
  */
  public ControlElement getElement (String _name) {
    if (_name==null) return null;
    return elementTable.get(_name);
  }

  // For backwards compatibility
  public ControlElement getControl (String _name) { return getElement (_name); }

  public ConfigurableElement getConfigurableElement (String _name) { return (ConfigurableElement) getElement (_name); }

 /**
  * Returns the visual of a control element by name
  * @return    the java.awt.Component visual of the element if found, null otherwise.
  * @param     String _name  The name of the control element
  */
  public java.awt.Component getVisual (String _name) {
    ControlElement element = getElement(_name);
    if (element==null) return null;
    return element.getVisual();
  }

 /**
  * Returns the component of a control element by name
  * @return    the java.awt.Component component of the element if found, null otherwise.
  * @param     String _name  The name of the control element
  */
  public java.awt.Component getComponent (String _name) {
    ControlElement element = getElement(_name);
    if (element==null) return null;
    return element.getComponent();
  }

 /**
  * Returns the container of a control element by name
  * @return    the java.awt.Container visual of the element if found, and the
  * element is a container, null otherwise.
  * @param     String _name  The name of the control element
  */
  public java.awt.Container getContainer (String _name) {
    ControlElement element = getElement(_name);
    if (element instanceof ControlContainer) return ((ControlContainer) element).getContainer();
    return null;
  }

  /**
   * Returns an object included in an element
   * @return    the Object defined by the element with this name, if found, null otherwise.
   * @param     String _name  The name of the control element
   */
   public Object getObject (String _elementName, String _objectName) {
     ControlElement element = getElement(_elementName);
     if (element==null) return null;
     return element.getObject(_objectName);
   }

   /**
    * Returns a Function object included in an element
    * @return    the Function object defined by the element with this name, if found, null otherwise.
    * @param     String _name  The name of the control element
    */
   public Function getFunction (String _elementName, String _functionName) {
     Object f = getObject (_elementName,_functionName);
     if (f instanceof Function) return (Function) f;
     return null;
   }


 /**
  * Completely destroy a ControlElement by name
  * @param     String _name  The name of the ControlElement to be destroyed
  */
  public void destroy(String _name) { destroy(getElement(_name),true); }

 /**
  * Completely destroy a ControlElement
  * @param     ControlElement _element The ControlElement to be destroyed
  */
  public void destroy(ControlElement _element) { destroy(_element,true); }

 /**
  * Reset all elements
  */
  public void reset() {
	clearMessages();  
    undoReparenting();
    for (ControlElement element : elementList) element.reset();
  }

 /**
  * Initialize all elements
  */
  public void initialize() {
    for (ControlElement element : elementList) element.initialize();
  }

  public void propagateValues () { } // To be overwritten

  private void doTheUpdate () {
    methodTriggerVariable.propagateValue(null,this.justCollectingData); // AMAVP (See Note in ControlElement)
    for (NeedsUpdate nu : updateList) nu.update();
  }

 /**
  * Refresh all elements
  */
  public void update() {
//    System.out.println ("Calling update");
    if (javax.swing.SwingUtilities.isEventDispatchThread()) propagateValues();
    else try {
      javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
        public synchronized void run() { propagateValues(); }
      });
    } 
    catch (InterruptedException exc) {}
    catch (java.lang.reflect.InvocationTargetException exc2) {}
    doTheUpdate();
  }

  /**
   * Update elements that collect data, but do no graphic work at all
   */
   public void collectData() {
     justCollectingData = true;
     if(javax.swing.SwingUtilities.isEventDispatchThread()) propagateValues();
     else try {
       javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
         public synchronized void run() { propagateValues(); }
       });
     } 
     catch (InterruptedException exc) {}
     catch (java.lang.reflect.InvocationTargetException exc2) {}
     doTheUpdate();
     justCollectingData = false;
   }


  /** 
   * Does the final update once all updates have been done.
   * Typically needed for drawing panels to redraw only once
   */
  public void finalUpdate() {
    for (NeedsFinalUpdate nfu : finalUpdateList) nfu.finalUpdate();
  }
  
  public void updateVariables() {
    propagateValues();
    methodTriggerVariable.propagateValue(null,this.justCollectingData);
  }
  
  /**
   * Same as clearData()
   */
   public void resetTraces() { clearData(); }
   /**
    * Resets all Resetable elements
    */
   public void clearData() { 
     for (ControlElement element : elementList) {
       if (element instanceof Resetable) element.reset();
     }
   }
   
   /**
    * Sets a common URL as codebase for all elements.
    * This is used by graphical elements to retrieve the images., for instance.
    */
    public void setUserCodebase(URL _url) {
      usercodebase = _url;
      for (ControlElement element : elementList) element.setProperty("_ejs_codebase",_url.toString());
    }

    /**
     * Returns the user codebase
     * @return URL
     */
    public URL getUserCodebase () { return usercodebase; }

   /**
    * Deprecated. Does nothing 
    */
    @Deprecated
    public void flush() {
      for (ControlElement element : elementList) element.flush();
    }

 /**
  * Set the active state of all elements
  */
  public void setActive(boolean _active) {
    for (ControlElement element : elementList) element.setActive(_active);
  }

  /**
   * perform any clean-up on exit
   */
  public void onExit() {
    for (ControlElement element : elementList) element.onExit();
   }

  /**
   * Clears all variables
   */
   public void clearVariables() { variableTable.clear(); }

   /**
    * Clears all variables defined in the model
    */
    public void clearModelVariables() {
      Hashtable<String,GroupVariable> newTable = new Hashtable<String,GroupVariable>();
      for (Enumeration<GroupVariable> e = variableTable.elements(); e.hasMoreElements();) {
        GroupVariable var = e.nextElement();
        if (!var.isDefinedInModel()) {
          //System.out.println ("Var not in model "+var.getName());
          newTable.put(var.getName(), var);
        }
        else if (var.hasElementsRegistered()) {
          //System.out.println ("Deleted var in model "+var.getName());
          var.setValueObsolete(true);
          newTable.put(var.getName(), var);
        }
      }
      variableTable = newTable;
    }

   /**
    * Returns a Set with all the variable names
    */
    public Set<String> getVariablesSet() {
      return variableTable.keySet();
    }

    public Hashtable<String,GroupVariable> getVariablesTable() { return this.variableTable; }
    
 /**
  * Destroy all elements
  */
  public void clear() {
    variableTable.clear();
    ArrayList<ControlElement> dialogsList = new ArrayList<ControlElement>(), framesList = new ArrayList<ControlElement>();
    for (ControlElement element : elementList) {
      String parent = element.getProperty("parent");
      if (parent==null) {
        if (element instanceof ControlDialog) dialogsList.add(element);
        else framesList.add(element);
      }
    }
    // It seems that, in 1.6, Dialogs need to be destroyed BEFORE their owner frames!
    for (Iterator<ControlElement> it=dialogsList.iterator(); it.hasNext(); ) {
      ControlElement element = it.next();
//      System.err.println("  Element to destroy "+element.toString()+"(class is "+element.getClass().getName()+")");
      destroy (element,false);
    }
    for (Iterator<ControlElement> it=framesList.iterator(); it.hasNext(); ) {
      ControlElement element = it.next();
//      System.err.println("  Element to destroy "+element.toString()+"(class is "+element.getClass().getName()+")");
      destroy (element,false);
    }
    setOwnerFrame(null);
  }

  private void destroy(final ControlElement _element, boolean _informMyParent) {
//    System.err.println("Calling EjsControl destroy for "+_element);

    if (_element==null) return;
    if (_informMyParent) {
      ControlElement parent = getElement(_element.getProperty("parent"));
      if (parent!=null) {
        if (parent instanceof ControlContainer) ((ControlContainer) parent).remove(_element);
      }
      else { // It may have been added to a container programmatically
        java.awt.Container cont = _element.getComponent().getParent();
        if (cont!=null) {
          cont.remove(_element.getComponent());
          cont.validate();
          cont.repaint();
        }
      }
    }
    _element.variablePropertiesClear();
    String name = _element.getProperty("name");
    if (name!=null) elementTable.remove(name);
    elementList.remove(_element);
    if (_element instanceof NeedsUpdate) updateList.remove(_element);
    if (_element instanceof NeedsFinalUpdate) finalUpdateList.remove(_element);
    if (_element instanceof ControlContainer) {
      for (Enumeration<ControlElement> e=((ControlContainer)_element).getChildren().elements(); e.hasMoreElements(); ) {
        ControlElement child = e.nextElement();
        destroy (child,false);
      }
    }
    if (_element instanceof ControlWindow) ((ControlWindow)_element).disposeWindow();
  }

 /**
  * Returns the top-level ancestor of an element (either the
  * containing Window or Applet), or null if the element has not
  * been added to any container. If no element name is provided, the
  * first control element whose component is a Window is returned.
  * @return    the Container if found, null otherwise.
  * @param     String _name  The name of the control element
  */
  public java.awt.Container getTopLevelAncestor (String _name) {
    if (_name!=null) {
      ControlElement element = getElement(_name);
      java.awt.Component comp = element.getComponent();
      if (comp instanceof javax.swing.JComponent) return ((javax.swing.JComponent) comp).getTopLevelAncestor();
    }
    else {
      for (ControlElement element : elementList) {
        java.awt.Component comp = element.getComponent();
        if (comp instanceof java.awt.Window) return (java.awt.Window) comp;
      }
    }
    return null;
  }

// -------------------------------------------------------
// Convenience methods
// Half way to org.opensourcephysics.controls.Control
// -------------------------------------------------------

  // For the custom methods
  private BooleanValue booleanValue = new BooleanValue(false);
  private IntegerValue integerValue = new IntegerValue(0);
  private DoubleValue  doubleValue  = new DoubleValue(0.0);
  private StringValue  stringValue  = new StringValue("");
  private ObjectValue  objectValue  = new ObjectValue(null);

  // --------- Setting different types of values ------

 /**
  * A convenience method to set a value to a boolean
  * @param _name
  * @param _value
  */
  public void setValue (String _name, boolean _value) {
    booleanValue.value = _value;
    setValue(_name,booleanValue);
  }

 /**
  * A convenience method to set a value to an int
  * @param _name
  * @param _value
  */
  public void setValue (String _name, int _value) {
    integerValue.value = _value;
    setValue(_name,integerValue);
  }

 /**
  * A convenience method to set a value to a double
  * @param _name
  * @param _value
  */
  public void setValue (String _name, double _value) {
    doubleValue.value = _value;
    setValue(_name,doubleValue);
  }

 /**
  * A convenience method to set a value to a String
  * @param _name
  * @param _value
  */
  public void setValue (String _name, String _value) {
    stringValue.value = _value;
    setValue(_name,stringValue);
  }

 /**
  * A convenience method to set a value to any Object
  * @param _name
  * @param _value
  */
  public void setValue (String _name, Object _value) {
    if (_value instanceof String) setValue (_name, (String) _value);
    else {
      objectValue.value = _value;
      setValue(_name, objectValue);
    }
  }

  // --------- Getting different types of values ------

 /**
  * A convenience method to get a value as a boolean
  * @param _name
  */
  public boolean getBoolean (String _name) {
    Value value = getValue(_name);
    if (value==null) return false;
    return value.getBoolean();
  }

 /**
  * A convenience method to get a value as an int
  * @param _name
  */
  public int getInt (String _name) {
    Value value = getValue(_name);
    if (value==null) return 0;
    return value.getInteger();
  }

 /**
  * A convenience method to get a value as a double
  * @param _name
  */
  public double getDouble (String _name) {
    Value value = getValue(_name);
    if (value==null) return 0.0;
    return value.getDouble();
  }

 /**
  * A convenience method to get a value as a String
  * @param _name
  */
  public String getString (String _name) {
    Value value = getValue(_name);
    if (value==null) return "";
    return value.getString();
  }

 /**
  * A convenience method to get a value as an Object
  * @param _name
  */
  public Object getObject (String _name) {
    Value value = getValue(_name);
    if (value==null) return null;
    return value.getObject();
  }

//---------------------------------------------------
//Public Utilities
//---------------------------------------------------

  static private Color[] colorTable = null;

  static public Color[] getPhaseColorTable() {
    if (colorTable==null) {
      colorTable = new Color[256];
      for (int i = 0; i<256; i++){
         double val = Math.abs(Math.sin(Math.PI*i/255));
         int b = (int) (255*val*val);
         val = Math.abs(Math.sin(Math.PI*i/255+Math.PI/3));
         int g = (int) (255*val*val*Math.sqrt(val));
         val = Math.abs(Math.sin(Math.PI*i/255+2*Math.PI/3));
         int r = (int) (255*val*val);
         colorTable[i]=new Color(r,g,b);
      }
    }
    return colorTable;
  }

  /**
   * Converts a phase angle in the range [-Pi,Pi] to hue, saturation, and brightness.
   *
   * @param phi phase angle
   * @return the HSB color
   */
  static public Color phaseToColor(double phi){
    int index = (int) (127.5*(1+phi/Math.PI));
    index = index % 255;
    return getPhaseColorTable()[index];
  }

  /**
   * Shows a message dialog
   * @param _panel
   * @param _title
   * @param _message
   */
  public void alert(String _panel, String _title, String _message) {
    alert(getComponent(_panel), _title, _message);
  }

  public void alert(Component _parent, String _title, String _message) {
    JOptionPane.showMessageDialog(_parent, _message, _title, JOptionPane.INFORMATION_MESSAGE);
  }

  public void setParentComponent (String _parent) { getSimulation().setParentComponent (_parent); }

  public void setUpdateView(boolean _update) { getSimulation().setUpdateView(_update); }

  
  /**
   * Resets all view elements to their initial state
   */
  public void resetElements() {
      reset();
      initialize();
  }

  /**
   * Clear all view elements from previous dat
   */
  public void clearElements() { initialize(); }

  /**
   * Formats a double according to the given pattern
   * @param _value
   * @param _pattern
   * @return
   */
  public String format(double _value, String _pattern) {
    return new java.text.DecimalFormat (_pattern).format(_value);
  }

  /**
   * Creates an ArrayFrame with the given data.
   * @param _parent The component to use as the parent of this frame
   * @param _data the data to display
   * @return the ArrayFrame created
   */
  public Object showTable (java.awt.Component _parent, org.opensourcephysics.display.Data... _data) {
    return org.opensourcephysics.tools.ToolForData.getTool().showTable(_parent,_data);
  }

  /**
   * Same as showTable(null,_data);
   */
  public Object showTable (org.opensourcephysics.display.Data... _data) {
    return showTable(null,_data);
  }

  /**
   * Displays a DataTool (if available) with the given data.
   * @param _parent The component to use as the parent of this frame
   * @param _data the data to display. null will just make the DataTool visible
   * @return the DataTool used 
   */
  public Object showDataTool (java.awt.Component _parent, org.opensourcephysics.display.Data... _data) {
    return org.opensourcephysics.tools.ToolForData.getTool().showDataTool(_parent, _data);
  }

  /**
   * Same as showDataTool(null,_data);
   */
  public Object showDataTool (org.opensourcephysics.display.Data... _data) {
    return showDataTool(null,_data);
  }

//  /**
//   * Displays a FourierTool (if available) with the given data.
//   * @param _parent The component to use as the parent of this frame
//   * @param _data the data to display. null will just make the DataTool visible
//   * @return the FourierTool used 
//   */
//  public Object showFourierTool (java.awt.Component _parent, org.opensourcephysics.display.Data... _data) {
//    return org.opensourcephysics.tools.ToolForData.getTool().showFourierTool(_parent, _data);
//  }

//  /**
//   * Same as showFourierTool(null,_data);
//   */
//  public Object showFourierTool (org.opensourcephysics.display.Data... _data) {
//    return showFourierTool(null,_data);
//  }

  // ----------------------------------------
  // Creation of HTML pages from the description and from files in the JAR file
  // ----------------------------------------
  
  /**
   * Whether the description of the simulation should show at start up. Default is true.
   */
  public void showDescriptionAtStartUp(boolean _show) { getSimulation().showDescriptionAtStartUp(_show); }
  
  /**
   * Actually show/hide the description pages
   * @param _show
   */
  public void showDescription(boolean _show) { getSimulation().showDescription(_show); }

  /**
   * Get the URL of a given description page, which is packaged inside the JAR file 
   * @param tabName the title on the page tab
   * @return
   */
  public URL getDescriptionPageURL (String _tabName) { return getSimulation().getDescriptionPageURL (_tabName); }

  /**
   * Creates a JDialog with a single description page. The dialog is not visible by default.
   * @param _owner
   * @param _tabName
   * @return
   */
  public JDialog createDescriptionDialog (Component _owner, String _tabName) {
    return createHTMLDialog(_owner,getSimulation().getDescriptionPageURL(_tabName));
  }

  /**
   * Creates a JDialog with the given HTML page. This file is typically included in the source JAR file.
   * Use this method instead of showDocument() when the HTML page includes references to other files
   * (such as GIF files) also inside the JAR. The system browser (which is called by showDocument()) 
   * cannot access files inside the JAR.
   * @param _owner
   * @param _page
   * @return the newly created JDialog
   */
  public JDialog createHTMLDialog (Component _owner, String _page) {
    return createDialog(_owner,createHTMLPage(_page));
  }

  /**
   * Creates a JDialog with the HTML page at the given URL.
   * @param _owner
   * @param _url
   * @return the newly created JDialog
   */
  public JDialog createHTMLDialog (Component _owner, URL _url) {
    return createDialog(_owner,createHTMLPage(_url));
  }

  /**
   * Creates a JDialog with the given owner and child component
   * @param _owner
   * @param _child
   * @return
   */
  public JDialog createDialog (Component _owner, Component _child) {
    if (_child==null) return null;
    JDialog dialog;
    if (_owner instanceof Frame) dialog = new JDialog((Frame) _owner);
    else if (_owner instanceof Dialog) dialog = new JDialog((Dialog) _owner);
    else dialog = new JDialog();
    // This code is 1.6 dependent
//    Window windowOwner=null;
//    if (_owner instanceof Window) windowOwner = (Window) _owner;
//    JDialog dialog = new JDialog(windowOwner);
    dialog.getContentPane().add(_child,java.awt.BorderLayout.CENTER);
    dialog.setSize(600, 400);
    return dialog;
  }
  
  /**
   * Creates a JScrollPane with a JEditorPane in it with the HTML from a page.
   * The page is searched for in all possible locations, including the source JAR file.
   * @param _page
   * @return the created JScrollPane
   */
  public JScrollPane createHTMLPage (String _page) {
    Resource resource = ResourceLoader.getResource(_page);
    if (resource==null) {
      System.out.println ("createHTMLPage: resource not found: "+_page);
      return null;
    }
    URL url = resource.getURL(); 
    if (url==null) {
      System.out.println ("createHTMLPage: resource has a null URL: "+_page);
      return null;
    }
    return createHTMLPage(url);
  }

  /**
   * Creates a JScrollPane with a JEditorPane in it with the HTML from the given URL
   * @param _url
   * @return the created JScrollPane
   */
  public JScrollPane createHTMLPage (URL _url) {
    JEditorPane editorPane = new JEditorPane();
    editorPane.setEditable(false);
    editorPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
          getSimulation().openURL(e.getSource(),e.getURL(),getSimulation().getView().getComponent(getSimulation().getMainWindow()), 
              getSimulation().getModel()._getApplet()!=null);
        }
      }
    });
    try { 
      editorPane.setPage(_url); 
      return new JScrollPane(editorPane);
    }
    catch (Exception exc) {
      exc.printStackTrace();
      return null;
    }
  }

  /**
   * Opens one or more documents using the native application. If the document is inside the JAR, it is extracted to a temporary directory
   * @param documents A ; or , separated list of documents, each of which can be a url, pdf, txt file, etc
   * @return
   */
  public boolean showDocument (String _documents) {
    boolean result=true;
    StringTokenizer tkn = new StringTokenizer(_documents,";,");
    while (tkn.hasMoreTokens()) {
      String filename = tkn.nextToken();
      if (Simulation.isDisplayable(filename)) { 
        java.io.File file = Simulation.getResourceFile(filename);
        if (file==null || OSPDesktop.open(file)==false) result = false;
      }
      else if (OSPDesktop.displayURL(filename)==false) result = false;
    }
    return result;
  }
  
  /**
   * Opens all description pages in the system browser 
   * @return true is successful, false otherwise 
   */
  public boolean openDescriptionPagesInBrowser() { return getSimulation().openDescriptionPagesInBrowser(); }

  /**
   * Opens the required page in the system browser
   * @return true is successful, false otherwise 
   */
  public boolean openDescriptionPageInBrowser(String _name) { return getSimulation().openDescriptionPageInBrowser(_name); }

  /**
   * Sets the visibility of a given description page
   */
  public void setDescriptionPageVisible(String _name, boolean _visible) { getSimulation().setDescriptionPageVisible(_name, _visible); }
  
  /**
   * Brings in the capture video tool for the given view element
   * @param element
   */
  public void captureVideo(String _element) {
    getSimulation().captureVideo(_element);  
  }
  
  //------------------------------------------
  // println 
  //------------------------------------------

  public void clearMessages () { 
	if (messageArea!=null) {
	  messageArea.setText("");
	  messageArea.setCaretPosition (messageArea.getText().length());
	}
  }
  
  public void println (String s) { print (s+_RETURN_); }

  public void println () { println (""); }

  public void print (String s) {
    if (messageArea!=null) {
	  messageArea.append(s);
	  messageArea.setCaretPosition (messageArea.getText().length());
    }
    else System.out.print (s);
  }


} // End of class



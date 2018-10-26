/*
 * The control package contains utilities to build and control
 * simulations using a central control.
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import java.util.*;
import java.awt.*;

import javax.swing.*;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;

 /**
  * <code>ControlSwingElement</code> is a base class for an object that
  * displays a visual java.awt.Component.
  * <p>
  * @see     java.awt.Component
  * @see     org.colos.ejs.library.control.ControlElement
  */
public abstract class ControlSwingElement extends ControlElement {
  // Important: if you change the order of the properties you must up date
  // these constants accordingly!!!
  // These constants are her for use of any subclass that overrides the
  // setValue() method for any of these properties
  static public final int SWING_ELEMENT_NAME         = 0; // The name of the element
  static public final int POSITION     = 1; // The position in its parent
  static public final int PARENT       = 2; // Its parent
  static public final int ENABLED      = 3; // Whether it is responsive or not
  static public final int VISIBLE      = 4; // The visibility
  static public final int SIZE         = 5; // The size
  static public final int FOREGROUND   = 6; // The foreground color
  static public final int BACKGROUND   = 7; // The background color
  static public final int FONT         = 8; // The font
  static public final int TOOLTIP      = 9; // The tooltip
  static public final int MENU_NAME    = 10; // The entry in the simulation menu
  static public final int PRINT_TARGET = 11; // The element to print
  static public final int ACTION_IMMEDIATE = 12; // The action must be invoked immediately, not when the model is idle 
  static public final int MENU_ENABLED = 13;

  // Particular types of actions
  static public final int ACTION_PRESS   = 10;
  static public final int ACTION_ON      = 20;
  static public final int ACTION_OFF     = 21;
  static public final int KEY_ACTION     = 30;
  static public final int MOUSE_ENTERED_ACTION   = 31;
  static public final int MOUSE_EXITED_ACTION    = 32;
  static public final int MOUSE_MOVED_ACTION     = 33;
  static public final int AXIS_DRAGGED_ACTION = 40; // Use ControlElement.VARIABLE_CHANGED instead

  static private ArrayList<String> myInfoList=null; // The list of registered properties

  protected Component myVisual; // The visual element to display
  protected Color     myDefaultBkgd, myDefaultFrgd;
  protected Font      myDefaultFont;
  private   Dimension mySize, myDefaultSize;
  private String menuNameEntry=null;
  private ControlWindow  myControlWindow=null;
  private String printTarget=null;
  private String tooltipText=null;
  private boolean menuEnabled=true;

// ------------------------------------------------
// Static constants and constructor
// ------------------------------------------------

//  public ControlSwingElement() { this(null); }

 /**
  * Instantiates an object that wraps a Swing JComponent of this type.
  */
  public ControlSwingElement() {
    super();
    myObject = myVisual = createVisual();
    myDefaultFrgd = myVisual.getForeground();
    myDefaultBkgd = myVisual.getBackground();
    myDefaultFont = myVisual.getFont();
    mySize = myDefaultSize = myVisual.getPreferredSize();
  }

// ------------------------------------------------
// Visual components
// ------------------------------------------------

 /**
  * Creates the visual component of this <code>ControlElement</code>,
  * the one you can configure graphically.
  */
  abstract protected Component createVisual();

 /**
  * Returns the visual component of this <code>ControlElement</code>,
  * the one you can configure graphically.
  */
  final public Component getVisual() { return myVisual; }

 /**
  * Returns the component of this <code>ControlElement</code>,
  * the one that is added to a container.
  */
  public Component getComponent() { return myVisual; }
  // This one is not final because, although this is the usual behavior,
  // there are exceptions. F. i., when embedding the visual into a JScrollPane

  
  public Window getTopWindow() {
    java.awt.Container parent = myVisual.getParent();
    while (parent!=null) {
      if (parent instanceof Window) return (Window) parent;
      parent = parent.getParent();
    }
    return null;
  }
  
  /**
   * Changes the visual component of this <code>ControlElement</code>.
   * Use only for SwingElements with a direct visual
   */
   final public void changeVisual(Component newVisual) {
     if (newVisual.equals(myVisual)) return;
     newVisual.setEnabled(myVisual.isEnabled());
     newVisual.setVisible(myVisual.isVisible());
     newVisual.setPreferredSize(mySize);
     if (newVisual instanceof Container) ((Container) newVisual).validate();
     ControlElement parentElement = myGroup.getElement(getProperty("parent"));
     if (parentElement!=null) ((ControlContainer) parentElement).adjustSize();
     newVisual.setForeground(myVisual.getForeground());
     newVisual.setBackground(myVisual.getBackground());
     newVisual.setFont(myVisual.getFont());
     if (newVisual instanceof JComponent) ((JComponent)newVisual).setToolTipText(tooltipText);
     final ControlElement parent = myGroup.getElement(getProperty("parent"));
     if (parent!=null) {
       if (javax.swing.SwingUtilities.isEventDispatchThread()) ((ControlContainer) parent).remove(ControlSwingElement.this);
       else try { javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
         public synchronized void run() { ((ControlContainer) parent).remove(ControlSwingElement.this); }
         });
       }
       catch(Exception exc) {}
     }
     // Now, do the change
     myObject = myVisual = newVisual;
     myDefaultFrgd = myVisual.getForeground();
     myDefaultBkgd = myVisual.getBackground();
     myDefaultFont = myVisual.getFont();
     mySize = myVisual.getPreferredSize();
     if (parent!=null) {
       if (javax.swing.SwingUtilities.isEventDispatchThread()) {
         ((ControlContainer) parent).add(ControlSwingElement.this);
         ((ControlContainer) parent).adjustSize();
       }
       else try { javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
         public synchronized void run() { 
           ((ControlContainer) parent).add(ControlSwingElement.this);
           ((ControlContainer) parent).adjustSize();
         }});
       }
       catch(Exception exc) {}
     }
   }

   private String getPrintTarget () {
     if (printTarget==null) return getProperty("name");
     return printTarget; 
   }

   protected void getPopupMenu (int x, int y) { 
     if (menuEnabled) getSimulation().getPopupMenu(getVisual(),x,y,getPrintTarget()); 
   }

   protected void printScreen() { getSimulation().saveImage(getPrintTarget()); }


   /**
    * Sets the ControlWindow element in which it lives
    * @param _window
    */
   final public void setControlWindow (ControlWindow _window) {
     myControlWindow = _window;
   }
   
   /**
    * Returns the window in which it lives
    * @return
    */
   final public ControlWindow getControlWindow () { return myControlWindow; }

//   /**
//    * Whether the popup menu should be enabled
//    * @return
//    */
//   final protected boolean isMenuEnabled () { return this.menuEnabled; }
   
// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

  public String getMenuNameEntry() {
    if (menuNameEntry!=null) {
      if (menuNameEntry.equals("null")) return null;
      return menuNameEntry;
    }
    return this.getProperty("name");
  }

 /**
  * Returns the list of all properties that can be set for this
  * ControlElement.
  * Subclasses that add properties should extend this table.
  * Order is crucial here: Both for the presentation in an editor (f.i. ViewElement)
  * and for the setValue() method.
  */
  // Important: Order is crucial!!! if you change the order of the properties
  // you must up date the constants at the beginning of this file accordingly!!!
  public java.util.List<String> getPropertyList() {
    if (myInfoList==null) {
      myInfoList = new ArrayList<String>();
      myInfoList.add("name");
      myInfoList.add("position");
      myInfoList.add("parent");

      myInfoList.add("enabled");
      myInfoList.add("visible");

      myInfoList.add("size");
      myInfoList.add("foreground");
      myInfoList.add("background");
      myInfoList.add("font");
      myInfoList.add("tooltip");
      myInfoList.add ("menuName");
      myInfoList.add ("printTarget");

      myInfoList.add ("immediateAction");
      myInfoList.add ("menu");

    }
    return myInfoList;
  }

 /**
  * Returns information about a given property.
  * Subclasses that add properties should extend this table.
  * <ll>
  *   <li> The first keyword is ALWAYS the type.
  *   <li> The keyword <b>CONSTANT</b> applies to properties that can not be
  *     changed using the setValue() methods
  *  </ll>
  */
  // Order in the implementation is irrelevant.
  public String getPropertyInfo(String _property) {
    if (_property.equals("name"))       return "String         CONSTANT";
    if (_property.equals("position"))   return "Position       CONSTANT PREVIOUS";
    if (_property.equals("parent"))     return "ControlElement CONSTANT";
    if (_property.equals("enabled"))    return "boolean";
    if (_property.equals("visible"))    return "boolean";
    if (_property.equals("size"))       return "Dimension|Object|String TRANSLATABLE";
    if (_property.equals("foreground")) return "int|Color|Object";
    if (_property.equals("background")) return "int|Color|Object";
    if (_property.equals("font"))       return "Font|Object";
    if (_property.equals("tooltip"))    return "String TRANSLATABLE";
    if (_property.equals("menuName"))   return "String TRANSLATABLE";
    if (_property.equals("printTarget"))     return "String";
    if (_property.equals("immediateAction")) return "boolean";
    if (_property.equals("menu"))       return "boolean";

    return null;
  }

 /**
  * Checks if a value can be considered a valid constant value for a property
  * If not, it returns null, meaning the value can be considered to be
  * a GroupVariable or a primitive constant.
  * This method implements more cases than really needed for the base class.
  * This is in order to save repetitions in swing subclasses.
  * @param     String _property The property name
  * @param     String _value The proposed value for the property
  */
  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    Value constantValue;
    if (_propertyType.indexOf("Alignment")>=0) {
      constantValue = ConstantParser.alignmentConstant(_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("Dimension")>=0) {
      constantValue = ConstantParser.dimensionConstant(_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("Layout")>=0) {
      constantValue = ConstantParser.layoutConstant(((ControlContainer) this).getContainer(),_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("Orientation")>=0) {
      constantValue = ConstantParser.orientationConstant(_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("Placement")>=0) {
      constantValue = ConstantParser.placementConstant(_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("Point")>=0) {
      constantValue = ConstantParser.pointConstant(_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("Mnemonic")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.length()>0) return new IntegerValue(_value.charAt(0));
    }
    return super.parseConstant(_propertyType, _value);
  }

  /**
   * This is the opposite of the method above.
   * It formats a value in a form suitable for the parser
   * @param _value Value
   * @return String
   */
  public String toStringValue (Value _value) {
    if (_value instanceof ObjectValue) {
      ObjectValue obj = (ObjectValue) _value;
      if (obj.value instanceof Point) return ConstantParser.toString((Point) obj.value);
      if (obj.value instanceof Dimension) return ConstantParser.toString((Dimension) obj.value);
    }
    return super.toStringValue(_value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

 /**
  * Sets the value of the registered variables.
  * Subclasses with internal values should extend this
  * @param int _index   A keyword index that distinguishes among variables
  * @param Value _value The object holding the value for the variable.
  */
  public void setValue (int _index, final Value _value) {
//    System.out.println (getComponent().getName()+": Setting property #"+_index+" to "+_value.toString());
    switch (_index) {
      case SWING_ELEMENT_NAME :
        super.setValue (ControlElement.NAME,_value);
        getComponent().setName(_value.toString());
        break;
      case POSITION :
        {
          Runnable doIt = new Runnable() {
            public synchronized void run() {
              ControlElement parent = myGroup.getElement(getProperty("parent"));
              if ((parent!=null) && (parent instanceof ControlContainer) ) ((ControlContainer) parent).remove(ControlSwingElement.this);
              myPropertiesTable.put("position",_value.toString());
              if ((parent!=null) && (parent instanceof ControlContainer) ) ((ControlContainer) parent).add(ControlSwingElement.this);
            }
          };
          if (javax.swing.SwingUtilities.isEventDispatchThread()) doIt.run();
          else try { javax.swing.SwingUtilities.invokeAndWait(doIt); }
          catch(Exception exc) { doIt.run(); }
        }
        break;
      case PARENT :
        {
          Runnable doIt = new Runnable() {
            public synchronized void run() {
              ControlElement parent = myGroup.getElement(getProperty("parent"));
              if ((parent!=null) && (parent instanceof ControlContainer) ) ((ControlContainer) parent).remove(ControlSwingElement.this);
              parent = myGroup.getElement(_value.toString());
              if (parent==null) {
                if (!(ControlSwingElement.this instanceof ControlWindow)) {
                  System.err.println(getClass().getName() + " : Error! Parent <" +
                      _value + "> not found for " + toString());

                }
              }
              else {
                if (parent instanceof ControlContainer) ((ControlContainer) parent).add(ControlSwingElement.this);
                else System.err.println (getClass().getName()+" : Error! Parent <"+_value+"> is not a ControlContainer");
              }
            }
          };
          if (javax.swing.SwingUtilities.isEventDispatchThread()) doIt.run();
          else try { javax.swing.SwingUtilities.invokeAndWait(doIt); }
          catch(Exception exc) { doIt.run(); }
        }
        break;
      case ENABLED : getVisual().setEnabled (_value.getBoolean()); break; // enabled
      case VISIBLE : getVisual().setVisible (_value.getBoolean()); break; // visible
      case SIZE : // Size (myVisual is necessarily a JComponent)
        {
          if (_value.getObject()==null) {
            System.err.println ("Size invalid for element "+getProperty("name"));
            return;
          }
          Runnable doIt = new Runnable() {
            public synchronized void run() {
              Dimension size = (Dimension) _value.getObject();
              if (size.width==mySize.width && size.height==mySize.height) return; // Do not waste time
              getComponent().setPreferredSize(mySize = size);
              if (ControlSwingElement.this instanceof ControlContainer) ((ControlContainer) ControlSwingElement.this).getContainer().validate();
              ControlElement parentElement = myGroup.getElement(getProperty("parent"));
              if (parentElement!=null) ((ControlContainer) parentElement).adjustSize();
            }
          };
          if (javax.swing.SwingUtilities.isEventDispatchThread()) doIt.run();
          else try { javax.swing.SwingUtilities.invokeAndWait(doIt); }
          catch(Exception exc) { doIt.run(); }
        }
        break;
      case FOREGROUND : // Foreground (not much time is wasted if the color is the same)
        if (_value.getObject() instanceof Color) getVisual().setForeground((Color) _value.getObject()); 
        else getVisual().setForeground(DisplayColors.getLineColor(_value.getInteger()));
        break;
      case BACKGROUND : // Background
        if (_value.getObject() instanceof Color) getVisual().setBackground((Color) _value.getObject()); 
        else getVisual().setBackground(DisplayColors.getLineColor(_value.getInteger()));
        break;
      case FONT : // Font
        if (_value.getObject() instanceof Font) getVisual().setFont((Font) _value.getObject());
        break;
      case TOOLTIP : // Tooltip
        tooltipText = org.opensourcephysics.display.TeXParser.parseTeX(_value.getString());
        if (getVisual() instanceof JComponent) ((JComponent) getVisual()).setToolTipText(tooltipText);
        break;
      case MENU_NAME : menuNameEntry = _value.getString(); break;
      case PRINT_TARGET :printTarget = _value.getString(); break;
      case ACTION_IMMEDIATE : setImmediateActions(_value.getBoolean()); break;
      case MENU_ENABLED : menuEnabled = _value.getBoolean(); break;
      default : // Do nothing. No inherited properties
    }
  }

  public void setDefaultValue (int _index) {
//    System.out.println ("Setting default value for property #"+_index);
    switch (_index) {
      case SWING_ELEMENT_NAME :
        super.setDefaultValue (ControlElement.NAME);
        getComponent().setName("");
        break;
      case POSITION :
        {
          ControlElement parent = myGroup.getElement(getProperty("parent"));
          if ((parent!=null) && (parent instanceof ControlContainer) ) ((ControlContainer) parent).remove(this);
          myPropertiesTable.remove("position");
          if ((parent!=null) && (parent instanceof ControlContainer) ) ((ControlContainer) parent).add(this);
        }
        break;
      case PARENT :
        {
          ControlElement parent = myGroup.getElement(getProperty("parent"));
          if ((parent!=null) && (parent instanceof ControlContainer) ) ((ControlContainer) parent).remove(this);
        }
        break;
      case ENABLED : getVisual().setEnabled (true); break;
      case VISIBLE : getVisual().setVisible (true); break;
      case SIZE : // Size (getComponent() is necessarily a JComponent)
        Runnable doIt = new Runnable() {
          public synchronized void run() {
            getComponent().setPreferredSize(mySize = myDefaultSize);
            if (ControlSwingElement.this instanceof ControlContainer) ((ControlContainer) ControlSwingElement.this).getContainer().validate();
            ControlElement parentElement = myGroup.getElement(getProperty("parent"));
            if (parentElement!=null) ((ControlContainer) parentElement).adjustSize();
          }
        };
        if (javax.swing.SwingUtilities.isEventDispatchThread()) doIt.run();
        else try { javax.swing.SwingUtilities.invokeAndWait(doIt); }
        catch(Exception exc) { doIt.run(); }
        break;
      case FOREGROUND : getVisual().setForeground(myDefaultFrgd); break;
      case BACKGROUND : getVisual().setBackground(myDefaultBkgd); break;
      case FONT :       getVisual().setFont(myDefaultFont);       break;
      case TOOLTIP :
        tooltipText = null;
        if (getComponent() instanceof JComponent) ((JComponent) getVisual()).setToolTipText(tooltipText); break;
      case MENU_NAME : menuNameEntry = null; break;
      case PRINT_TARGET : printTarget = null; break;
      case ACTION_IMMEDIATE : setImmediateActions(false); break;
      case MENU_ENABLED : menuEnabled = true; break;
      default : break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case ENABLED : return "true";
      case VISIBLE : return "true";
      case ACTION_IMMEDIATE : return "false";
      case MENU_ENABLED : return "true";

      default : return "<none>";
    }
  }
  
 /**
  * Gets the value of any internal variable.
  * Subclasses with internal values should extend this
  * @param int _index   A keyword index that distinguishes among variables
  * @return Value _value The object holding the value for the variable.
  */
  public Value getValue (int _index) {
    return null; // None of these properties can be modified by the element
  }

// ------------------------------------------------
// A utility for subclasses that require an icon
// ------------------------------------------------

  static protected javax.swing.Icon getIcon (String _iconFile) {
    if (_iconFile.startsWith("\"") && _iconFile.endsWith("\"")) _iconFile = _iconFile.substring(1,_iconFile.length()-1);
    return org.opensourcephysics.tools.ResourceLoader.getIcon(_iconFile);
  }

  static protected java.awt.Image getImage (String _iconFile) {
    if (_iconFile.startsWith("\"") && _iconFile.endsWith("\"")) _iconFile = _iconFile.substring(1,_iconFile.length()-1);
    return org.opensourcephysics.tools.ResourceLoader.getImage(_iconFile);
  }

} // End of Class

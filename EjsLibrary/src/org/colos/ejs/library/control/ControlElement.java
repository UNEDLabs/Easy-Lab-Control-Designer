/*
 * The control package contains utilities to build an interface
 * using a central control.
 * Copyright (c) July 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

 // Note 1 : comment "AMAVP" stands for "Accept methods as variable properties"
 //          stands for the BIG change when I first introduced
 //          expressions as possible values for the properties

package org.colos.ejs.library.control;

import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.value.*;

import java.util.*;
import javax.swing.SwingUtilities;

 /**
  * <code>ControlElement</code> is a base class for an object that
  * can be managed using a series of configurable properties, hold actions
  * that when invoked graphically call other objects' methods, and be
  * responsible for the display and change of one or more internal variables.
  * <p>
  * <code>ControlElement</code>s can be included into a EjsControl,
  * thus acting in a coordinated way.
  * <p>
  * In fact, the best way to use a <code>ControlElement</code>, is to include
  * it into a EjsControl and then configure it using the
  * <code>setProperty()</code> method.
  * <p>
  * After this, the value common to several of these ControlElements can be
  * set and retrieved using a single setValue() or getValue() call from the
  * ControlGroup.
  * <p>
  * You can also add any action you want to a ControlElement, but it is the
  * implementing class' responsibility to trigger an action in response
  * to a user's gesture (with the mouse or keyboard)
  * <p>
  * @see     EjsControl
  */
public abstract class ControlElement implements org.colos.ejs.library.ConfigurableElement {

  protected EjsControl  myGroup = null; // The group of ControlElements with which I share variables
  protected Hashtable<String,String> myPropertiesTable = new Hashtable<String,String>(); // A place to hold any property

  protected Object        myObject = null; // The wrapped object
  private boolean         myActiveState = true; // Whether I am active or not
  private Vector<MethodWithOneParameter> myActionsList = new Vector<MethodWithOneParameter>(); // My list of actions
  private GroupVariable[] myProperties = null;      // The variables for the registered properties
  private String[]        myPropertiesNames = null; // The names of the registered properties
  protected boolean isUnderEjs = false;
  private boolean actionsWhenIdle = true;

  MethodWithOneParameter[] myMethodsForProperties = null; // AMAVP
  Value[] myExpressionsForProperties = null; // AMAVP
  protected PropertyEditor myEjsPropertyEditor=null; // For Ejs internal use
  VariableEditor myEjsVariableEditor=null; // For Ejs internal use

// ------------------------------------------------
// Static constants and constructor
// ------------------------------------------------
  public static final int NAME = 0; // The name of the element

  public static final int ACTION            = 0;
  public static final int VARIABLE_CHANGED  = 1;
  public static final int METHOD_FOR_VARIABLE  = 2; // AMAVP

  static public final int ACTION_ERROR   = 1001;
  static public final int ACTION_SUCCESS = 1002;

  static public final String METHOD_TRIGGER  = "_expr_"; // AMAVP

  /**
   * Used by Analytic parser elements
   */
  static public int indexOf (String _var, String[] _vars) {
    for (int i=0, n=_vars.length; i<n; i++) if (_var.equals(_vars[i])) return i;
    return -1;
  }

  /**
   * creates a list of menu entries for DataInformation objects
   * @return
   */
  static protected java.util.List<Object> 
    getDataInformationMenuEntries(final java.awt.Component _parent, final org.opensourcephysics.display.Data _data) { 
    
    java.util.ArrayList<Object> list = new java.util.ArrayList<Object> ();
    list.add(new javax.swing.AbstractAction(org.colos.ejs.library.Simulation.getEjsString("InteractiveTrace.ShowDataTable")){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(java.awt.event.ActionEvent e) { 
        org.opensourcephysics.tools.ToolForData.getTool().showTable(_parent,_data);
      }
    });
    list.add(new javax.swing.AbstractAction(org.colos.ejs.library.Simulation.getEjsString("InteractiveTrace.ShowDatasetTool")){
      private static final long serialVersionUID = 1L;
      public void actionPerformed(java.awt.event.ActionEvent e) { 
        org.opensourcephysics.tools.ToolForData.getTool().showDataTool(_parent, _data);
      }
    });
//    list.add(new javax.swing.AbstractAction(org.colos.ejs.library.Simulation.getEjsString("DataInformation.ShowFourierTool")){
//      private static final long serialVersionUID = 1L;
//      public void actionPerformed(java.awt.event.ActionEvent e) { 
//        org.opensourcephysics.tools.ToolForData.getTool().showFourierTool(_parent,_data);
//      }
//    });
    return list;
  }


  public ControlElement() {
    // Create the list of registered properties
    List<String> info = getPropertyList();
    myPropertiesNames = new String[info.size()];
    myProperties      = new GroupVariable[info.size()];
    myMethodsForProperties = new MethodWithOneParameter[info.size()]; // AMAVP
    myExpressionsForProperties = new Value[info.size()]; // AMAVP
    for (int i=0; i<info.size(); i++) {
      String property = info.get(i);
      myPropertiesNames[i] = property;
      myProperties     [i] = null;
      myMethodsForProperties[i] = null; // AMAVP
      myExpressionsForProperties[i] = null;
    }
  }

  /**
   * Returns the basic object that the ControlElement wrapps.
   * Usually a graphic component
   * @return Object
   */
  public Object getObject () { return myObject; }

  /**
   * Returns the expected class of the object that the ControlElement wrapps.
   * Not final because of special needs (see display3d.DrawingPanel3D, f.i.)
   * @return String
   */
  public String getObjectClassname () { return myObject.getClass().getName(); }
  
  /**
   * Returns the expected class of the object when in server mode
   * Not final because of special needs (see ElementTrail, f.i.)
   * @return String
   */

  public String getServerClassname () { return "org.colos.ejs.library.server.SocketViewElement"; }
  
  /**
   * Finds an object defined by the element that is associated to a given name
   * @param _name String the name of the element
   * @return Object the object defined withthis name, if any.
   */
  public Object getObject (String _name) {
    if (_name==null || _name.trim().length()<=0) return getObject();
    return null;
  }

  protected EjsControl getEjsControl() { return myGroup; }

  /**
   * This is a place holder for elements to add menu entries to a simulation
   */
  public void addMenuEntries () {}

  /**
   * To be overwritten by ControlDialog only
   */
  public void replaceVisual (java.awt.Frame _owner) {} 
  

// ------------------------------------------------
// For Ejs internal use
// ------------------------------------------------

  /**
   * Denotes the editor for the properties
   * For Ejs internal use only
   * @param _editor PropertyEditor
   */
  public void setPropertyEditor (PropertyEditor _editor) {
    myEjsPropertyEditor = _editor;
//    isUnderEjs = true;
  }

  /**
   * Whether the property editor is reading a set of properties.
   * Do not try to use it or it will probably cause a non-fatal
   * (but difficult to track) mistake
   * @return boolean
   */
  public final boolean editorIsReading () { return myEjsPropertyEditor==null ? false : myEjsPropertyEditor.isReading(); }

  /**
   * Denotes the editor for the variables
   * For Ejs internal use only
   * @param _editor VariableEditor
   */
  public void setVariableEditor (VariableEditor _editor) { myEjsVariableEditor = _editor; }

  /**
   * Stores the value of a property in the PropertyEditor
   * For Ejs internal use only
   * @param _property String
   * @param _value String
   */
  final public void setFieldListValue (int _index, Value _value) {
    setFieldListValue (_index, _value, true);
  }

  /**
   * Stores the value of a property in the PropertyEditor
   * For Ejs internal use only
   * @param _property String
   * @param _value String
   */
  final protected void setFieldListValue (int _index, Value _value, boolean andReport) {
    if (myEjsPropertyEditor==null || myEjsVariableEditor==null) return;
    if (myMethodsForProperties[_index]!=null); // System.out.println ("Method at "+index); do nothing
    else if (myExpressionsForProperties[_index]!=null); // System.out.println ("Expression at "+myPropertiesNames[index]);
    else if (myProperties[_index]!=null) {
      myEjsVariableEditor.updateTableValues(this.myEjsPropertyEditor,myProperties[_index].getName(),toStringValue(_value),_value);
    }
    else {  // Update the editor for properties
      String name = myPropertiesNames[_index];
      for (javax.swing.text.JTextComponent field : myEjsPropertyEditor.getFieldList()) {
        if (field.getName().equals(name)) {
          field.setText(toStringValue(_value));
          field.setCaretPosition(0);
          field.setBackground(java.awt.Color.white);
          setProperty (name,toStringValue(_value));
          reset();
          break;
        }
      }
    }
    if (andReport) myEjsVariableEditor.updateControlValues(false);
  }

  /**
   * This one is used when interacting with the element can affect one of two
   * variables. If the first is connected to a variable, then it affects the
   * second. If not, only the first one.
   * @param _index int
   * @param _index2 int
   * @param _value Value
   */
  final protected void setFieldListValueWithAlternative (int _index, int _index2, Value _value) { //see f.i. ControlCheckBox
    if (myEjsPropertyEditor==null || myEjsVariableEditor==null) return;
    if (myMethodsForProperties[_index]!=null); // System.out.println ("Method at "+index); do nothing
    else if (myExpressionsForProperties[_index]!=null); // System.out.println ("Expression at "+myPropertiesNames[index]);
    else if (myProperties[_index]!=null) {
      //System.out.println ("Element = "+getProperty("name"));
      //System.out.println ("Property = "+myPropertiesNames[_index]);
      myEjsVariableEditor.updateTableValues(this.myEjsPropertyEditor,myProperties[_index].getName(),toStringValue(_value),_value);
      setFieldListValue (_index2,_value);
      return;
    }
    else {  // Update the editor for properties
      String name = myPropertiesNames[_index];
      for (javax.swing.text.JTextComponent field : myEjsPropertyEditor.getFieldList()) {
        if (field.getName().equals(name)) {
          //System.out.println ("Setting text of "+_index+" to "+toStringValue(_value));
          field.setText(toStringValue(_value));
          field.setCaretPosition(0);
          field.setBackground(java.awt.Color.white);
          setProperty (name,toStringValue(_value));
          reset();
        }
      }
    }
    myEjsVariableEditor.updateControlValues(false);
  }

  final public void setFieldListValues (int[] _variableIndex, Value[] _values) {
    if (myEjsPropertyEditor==null || myEjsVariableEditor==null) return;
    boolean changeInControl = false;
    for (int i=0, n=_variableIndex.length; i<n; i++) {
      int index = _variableIndex[i];
      if (index<0) continue; // Negative indexes are ignored. Do not remove. This is used somewhere!!!
      if (myMethodsForProperties[index]!=null); //{ System.out.println ("Expression at "+index); do nothing
      else if (myExpressionsForProperties[index]!=null); // System.out.println ("Expression at "+myPropertiesNames[index]);
      else if (myProperties[index]!=null) {
        changeInControl = true;
        myEjsVariableEditor.updateTableValues(this.myEjsPropertyEditor,myProperties[index].getName(),_values[i].getString(),_values[i]);
      }
      else {  // Update the editor for properties, unless it is a deprecated property and these are not shown
        String name = myPropertiesNames[index];
//        System.out.println("Checking if "+name+ " is DEPRECATED");
//        if (propertyIsTypeOf(name,"DEPRECATED") && myGroup!=null && !myGroup.getBoolean("_Ejs_ShowDeprecated_")) continue;
//        System.out.println(name+ " is not DEPRECATED");
        for (javax.swing.text.JTextComponent field : myEjsPropertyEditor.getFieldList()) {
          if (field.getName().equals(name)) {
            field.setText(_values[i].getString());
            field.setCaretPosition(0);
            field.setBackground(java.awt.Color.white);
            setProperty (name,_values[i].getString());
            reset();
          }
        }
      }
    }
    if (changeInControl) 
      SwingUtilities.invokeLater(new Runnable() {
        public void run() { myEjsVariableEditor.updateControlValues(false); }
      });
  }

// ------------------------------------------------
// Definition of Properties
// ------------------------------------------------

 /**
  * Returns the list of all properties that can be set for this
  * ControlElement.
  * Subclasses that add properties should implement this.
  * Order is crucial here: Both for the presentation in an editor
  * (f.i. ViewElement) and for the setValue() method.
  */
  public abstract List<String> getPropertyList();

 /**
  * Returns information about a given property.
  * Subclasses that add properties should implement this.
  * Order in the implementation is irrelevant.
  * <ll>
  *   <li> The first keyword is ALWAYS the type. If more than one type is
  *     accepted, they are separated by | (do NOT use spaces!)
  *   <li> The keyword <b>CONSTANT</b> applies to properties that can not be
  *     changed using the setValue() methods
  *   <li> The keyword <b>VARIABLE_EXPECTED</b> is used when a String could be
  *     accepted, but a variable has priority. In this case, a String requires
  *     using inverted commas or quotes
  *   <li> The keyword <b>NotTrimmed</b> specifies that leading or trailing
  *     spaces must be respected when present. This is useful for labels or
  *     titles, for instance
  *   <li> The keywords <b>PREVIOUS</b> and <b>POSTPROCESS</b> indicate that,
  *     when setting several properties at once (using setProperties()) the
  *     property must be process before, resp. after, the others
  *  </ll>
  */
  public abstract String getPropertyInfo(String _property);

  /**
   * This is used by Ejs to obtain an alias for the property
   * name in order to search for this alias in a common
   * file of easily translatable entries used in the tables
   * of properties for an element.
   * @param _property String
   * @return String
   */
  public String getPropertyCommonName(String _property) {
    return _property;
  }

 /**
  * Checks if a value can be considered a valid constant value for a property
  * If not, it returns null, meaning the value can be considered to be
  * a GroupVariable
  * @param     String _property The property name
  * @param     String _value The proposed value for the property
  */
  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    Value constantValue;
    if (_propertyType.indexOf("boolean")>=0) {
      constantValue = ConstantParser.booleanConstant(_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("Color")>=0) {
      constantValue = ConstantParser.colorConstant(_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("File")>=0) {
      String noQuotesValue;
      if (_value.startsWith("\"") && _value.endsWith("\"")) noQuotesValue = _value.substring(1,_value.length()-1);
      else noQuotesValue = _value;
      if (org.opensourcephysics.tools.ResourceLoader.getResource(noQuotesValue)!=null) return new StringValue(_value);
    }
    if (_propertyType.indexOf("Font")>=0) {
      java.awt.Font currentFont = null;
      if (getVisual()!=null) currentFont = getVisual().getFont();
      constantValue = ConstantParser.fontConstant(currentFont,_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("Format")>=0) {
      constantValue = ConstantParser.formatConstant(_value);
      if (constantValue!=null) return constantValue;
    }
    if (_propertyType.indexOf("Gutters")>=0 || _propertyType.indexOf("Margins")>=0 ||_propertyType.indexOf("Rectangle")>=0) {
      constantValue = ConstantParser.rectangleConstant(_value);
      if (constantValue!=null) return constantValue;
    }
    return null;
  }

  public String toStringValue (Value _value) {
    if (_value instanceof StringValue) return "\""+_value.getString()+"\"";
    return _value.getString();
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

 /**
  * Sets the value of the registered variables.
  * Subclasses with internal values should extend this
  * Order is crucial here: it must match exactly that of the getPropertyList()
  * method.
  * @param int _index   A keyword index that distinguishes among variables
  * @param Value _value The object holding the value for the variable.
  */
  public void setValue (int _index, Value _value) {
    switch (_index) {
      case NAME :
        if (myGroup!=null) myGroup.rename(this,_value.toString());
        break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case NAME :
        if (myGroup!=null) myGroup.rename(this,null);
        break;
    }
  }

  public String getDefaultValueString (int _index) {
    return "???";
  }

 /**
  * Gets the value of any internal variable.
  * Subclasses with internal values should extend this
  * @param int _index   A keyword index that distinguishes among variables
  * @return Value _value The object holding the value for the variable.
  */
  public Value getValue (int _index) { return null; }

  /**
   * Gets the value of any internal variable.
   * Subclasses with internal values should extend this
   * @param int _index   A keyword index that distinguishes among variables
   * @return Value _value The object holding the value for the variable.
   */
   final public Value getValue (String _property) { 
     return getValue(propertyIndex(_property));
   }
  
  
// -------------------------------------------
// Methods that deal with properties
// -------------------------------------------

 /**
  * Sets a property for this <code>ControlElement</code>. Implementing
  * classes are responsible of deciding (by declaring them in the
  * getPropertyList() method) what properties turn into visual
  * changes, or different behaviour, of the ControlElement.
  * <p>
  * However, every propery is accepted, even if it is not meaningful for a
  * particular implementation of this interface. This can serve as a
  * repository of information for future use.
  * <p>
  * Implementing classes should make sure that the following
  * requirements are met:
  * <ll>
  *   <li> Properties can be set in any order. The final result
  *        should not depend on the order. Exceptions must be
  *        explicitly documented.
  *   <li> Any property can be modified. If so, the old value,
  *        and whatever meaning it had, is superseded by the
  *        new one. If the new one is null, the old one is simply removed
  *        and setDefaultValue(index) is called in case a precise default
  *        value should be used.
  *   <li> When the element is part of a EjsControl, final users should
  *        not use this setProperty method directly, but go through the
  *        corresponding method of the group.
  * </ll>
  * @return    This same element. This is useful to nest more
  *            than one call to <code>setProperty</code>
  * @param     String _property The property name
  * @param     String _value    The value desired for the property
  * @see       EjsControl
  */
  // This one is not final because a few of the subclasses
  // (f. i. ControlContainer and ControlTrace) need to overwrite it
  public ControlElement setProperty(String _property, String _value) {
    return setProperty(_property, _value, true);
  }

  /**
   * Same as setProperty (String, String) but only stores the value if _storeIt is true.
   * This is to be used by propagateProperty() of ControlContainer only.
   * @param _property String
   * @param _value String
   * @param _storeIt boolean
   * @return ControlElement
   */
  public ControlElement setProperty(String _property, String _value, boolean _storeIt) {
    if (propertyIsTypeOf(_property, "DEPRECATED")) {
      if (_value!=null)  System.err.println (getProperty("name")+ ": Trying to set deprecated property "+_property+ " to "+_value);
      return this;
    }
//     System.err.println ("Setting property "+_property+ " to "+_value);
    _property = _property.trim();
    if (_property.equals("_ejs_")) isUnderEjs = true;
    // See if the proposed property is registered as a real property
    int index = propertyIndex(_property);
    if (index<0) {
      // It is not a registered property. Store the value but do not call setValue()
      if (_value==null) myPropertiesTable.remove(_property);
      else if (_storeIt) myPropertiesTable.put(_property,_value);
      return this;
    }
    // The property is registered. Unregister and call setValue
    myMethodsForProperties[index] = null; // AMAVP
    myExpressionsForProperties[index] = null; // AMAVP
    if (myProperties[index]!=null) { // remove from the list of listeners for this GroupVariable
      myProperties[index].removeElementListener(this,index);
      myProperties[index] = null;
    }
    
    // Treat the null case separately, so that to avoid a lot of 'if (null)' checks
    if (_value==null) { 
      if (myProperties[index]!=null) { // remove from the list of listeners for this GroupVariable
        myProperties[index].removeElementListener(this,index);
        myProperties[index] = null;
      }
      setDefaultValue (index); // use a default value
      myPropertiesTable.remove(_property); // remove the property
      return this;
    }
    
    // From now on, the value is, necessarily, not null

    // Some properties should not be trimmed ('text', for instance)
    if (!propertyIsTypeOf(_property,"NotTrimmed")) _value = _value.trim();
    String originalValue = _value;
    // Because of backwards compatibility with version 3.01 or earlier
    // There might be confusion with constant strings versus variable names
    // This is the reason for most of the following block
    // From this version on, it is recommended that constant strings should be
    // delimited by either ' or "
    Value constantValue = null;
    if (_value.startsWith("%") && _value.endsWith("%") && _value.length()>2) 
      _value = _value.substring(1,_value.length()-1); // Force a variable or method
    else if (_value.startsWith("@") && _value.endsWith("@") && _value.length()>2); // Do nothing for parsed expressions
    else if (_value.startsWith("#") && _value.endsWith("#") && _value.length()>2); // Do nothing for variables such as f()
    else {
      if (_value.startsWith("\"") || _value.startsWith("'") ); // It is a constant String, don't try anything else
      else {
        // First look for a CONSTANT property that can not be associated to GroupVariables
        if (propertyIsTypeOf(_property,"CONSTANT")) constantValue = new StringValue(_value);
        // Check for String properties
        if (constantValue==null) {
          String propType = propertyType(_property);
          if (propType.equals("String") && !propertyIsTypeOf(_property,"VARIABLE_EXPECTED")) // See TextField f.i.
            constantValue = new StringValue(_value);
          if (propType.equals("String|String[]") && !propertyIsTypeOf(_property,"VARIABLE_EXPECTED")) {// See DataTable f.i.
            if (!_value.endsWith("}")) constantValue = new StringValue(_value); // It could be a String[]
          }
        }
      }
      // End of the compatibility block

      // Now try the particular parser
      // The particular parser comes first because it can discriminate between
      // a real String and a File, f.i.
      if (constantValue==null) constantValue = parseConstant (propertyType(_property),_value);
      // Finally the standard parser
      if (constantValue==null) constantValue = Value.parseConstantOrArray(_value,true); // silentMode
    }
    if (constantValue!=null) { // Just set the value for this property
      setValue (index,constantValue);
      if (_storeIt) myPropertiesTable.put(_property,originalValue);
      return this;
    }
    
    if (myGroup==null) {
      if (_storeIt) myPropertiesTable.put(_property,originalValue);
      return this;
    }

    // Associate the property with a GroupVariable or Method for later use
    boolean isNormalVariable = true, isExpression = false;
    if (_value.startsWith("#") && _value.endsWith("#") && _value.length()>2) {
      _value = _value.substring(1, _value.length() - 1);
      isNormalVariable = true;
    }
    else if (_value.startsWith("@") && _value.endsWith("@") && _value.length()>2) {
      _value = _value.substring(1, _value.length() - 1);
      originalValue = _value;
      isNormalVariable = false;
      isExpression = true;
    }
    else if (_value.startsWith("new ")) { // cases such as new double[][] {{...}} for a Polygon2D
      originalValue = _value;
      isNormalVariable = false;
      isExpression = false;
    }
    else if (_value.indexOf('(')>=0) isNormalVariable = false; // It must be a method
    // Begin --- AMAVP
    if (isNormalVariable) {  // Connect a variable property with a normal variable name
      // This is what would normally happen under Ejs with expressions
      Value newValue=null;
      // get the actual value and use it when you register
      // to the group. This is arguable...
      // if (getProperty("_ejs_")==null) // Why not?
      newValue = getValue(index);
      if (newValue==null) {
//      if      (propertyIsTypeOf(_property,"[]"))      newValue = new ObjectValue(null);
//      else
        if       (propertyIsTypeOf(_property,"double"))  newValue = new DoubleValue(0.0);
        else if (propertyIsTypeOf(_property,"boolean")) newValue = new BooleanValue(false);
        else if (propertyIsTypeOf(_property,"int"))     newValue = new IntegerValue(0);
        else if (propertyIsTypeOf(_property,"String"))  newValue = new StringValue(_value);
        else newValue = new ObjectValue(null);
      }
      myProperties[index] = myGroup.registerVariable (_value,this,index,newValue);
    }
    else if (isExpression) {  // Connect a variable property to an expression
      //System.out.println ("Connecting property "+_property+" to expression: "+originalValue);
      String returnType=null;
      if      (propertyIsTypeOf(_property,"double"))  returnType = "double";
      else if (propertyIsTypeOf(_property,"boolean")) returnType = "boolean";
      else if (propertyIsTypeOf(_property,"int"))     returnType = "int";
      else if (propertyIsTypeOf(_property,"String"))  returnType = "String";
      else if (propertyIsTypeOf(_property,"Action"))  returnType = "Action";
      else {
        //System.out.println ("Error for expression property "+_property+" of the element "+this.toString()
        //                    +". Cannot be set to : "+originalValue);
        myPropertiesTable.put(_property,originalValue);
        return this;
      }
      if (!returnType.equals("Action")) {
        myExpressionsForProperties[index] = new InterpretedValue(_value,myEjsPropertyEditor);
        myGroup.methodTriggerVariable.addElementListener(this,index);
        myProperties[index] = myGroup.methodTriggerVariable;
      }
    }
    else if (getProperty("_ejs_")==null) {  // Connect a variable property to a method (only if not under Ejs)
      String returnType=null;
      if      (propertyIsTypeOf(_property,"String"))  returnType = "String";
      else if (propertyIsTypeOf(_property,"Color"))   returnType = "Object";
      else if (propertyIsTypeOf(_property,"double"))  returnType = "double";
      else if (propertyIsTypeOf(_property,"boolean")) returnType = "boolean";
      else if (propertyIsTypeOf(_property,"int"))     returnType = "int";
      else if (propertyIsTypeOf(_property,"double[]"))  returnType = "Object";
      else if (propertyIsTypeOf(_property,"int[]"))     returnType = "Object";
      else if (propertyIsTypeOf(_property,"Object"))  returnType = "Object";
      else {
        System.out.println ("Error for property "+_property+" of the element "+this.toString()+". Cannot be set to : "+originalValue);
        myPropertiesTable.put(_property,originalValue);
        return this;
      }
      // Resolve for non-default target
      String [] parts = MethodWithOneParameter.splitMethodName(_value);
      if (parts==null) {
        System.err.println (getClass().getName()+" : Error! method <"+originalValue+"> not found");
        myPropertiesTable.put(_property,originalValue);
        return this;
      }
      if (parts[0]==null) parts[0] = "_default_";
      Object target = myGroup.getTarget(parts[0]);
      if (target==null) {
        System.err.println (getClass().getName()+" : Error! Target <"+parts[0]+"> not assigned");
        System.err.println ("when setting property "+_property+" to "+_value);
        myPropertiesTable.put(_property,originalValue);
        return this;
      }
      if (parts[2]==null) _value = parts[1]+"()";
      else _value = parts[1]+"("+parts[2]+")";
      myMethodsForProperties[index] = new MethodWithOneParameter (METHOD_FOR_VARIABLE,target,_value,
          returnType,null,this); // Pass the element itself Jan 31st 2004 Paco
      // Register the property of this element to a standard boolean (why not?) variable
      // myGroup.update() will take care of triggering the method
      myGroup.methodTriggerVariable.addElementListener(this,index);
      myProperties[index] = myGroup.methodTriggerVariable;
//    myProperties[index] = myGroup.registerVariable (METHOD_TRIGGER,this,index,new BooleanValue(false));
      myGroup.update(); // trigger the method right now
      myGroup.finalUpdate(); // trigger the method right now
    } // End --- AMAVP

    if (_storeIt) myPropertiesTable.put(_property,originalValue);
    return this;
  }

 /**
  * Sets more than one property at once. The pairs
  * <code>property=value</code> must be separated by ';'.
  * If any value has a ';' in it, then it must be set
  * in a separate <code>setProperty</code> call.
  * @return    This same element. This is useful to nest more
  *            than one call to <code>setProperties</code>
  * @param     String _propertyList The list of properties and Values
  *            to be set
  */
  final public ControlElement setProperties(String _propertyList) {
    Hashtable<String,String> propTable = new Hashtable<String,String>();
    StringTokenizer tkn = new StringTokenizer(_propertyList,";");
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (token.trim().length()<=0) continue;
      int index = token.indexOf("=");
      if (index<0) System.err.println (getClass().getName()
                  +" : Error! Token <"+token+"> invalid for "+toString());
      else propTable.put (token.substring(0,index).trim(),token.substring(index+1));
    }
    return setProperties(propTable);
  }

  // This is neccesary just to make sure that some properties are processed
  // first and some others (such as 'value') last
  private void preprocess (String _property, Hashtable<String,String> _propertyTable) {
    String value = _propertyTable.get(_property);
    if (value!=null) {
      setProperty(_property,value);
      _propertyTable.remove(_property);
    }
  }

  private ControlElement setProperties(Hashtable<String,String> _propertyTable) {
    // _ejs_ is used by Ejs to signal that the element is working under it.
    // This has some consequences in the behaviour of some properties
    // (f.i. 'exit' on a Frame will not exit the application)
    preprocess("_ejs_",   _propertyTable);

    Hashtable<String,String> postTable = new Hashtable<String,String>();
    for (Enumeration<String> e=_propertyTable.keys(); e.hasMoreElements();) {
      String key = e.nextElement();
      // Some need to be processed before the others
      if (propertyIsTypeOf(key,"PREVIOUS")) preprocess(key,_propertyTable);
      // And some need to be the last ones
      else if (propertyIsTypeOf(key,"POSTPROCESS")) {
        String value = _propertyTable.get(key);
        _propertyTable.remove(key);
        postTable.put(key,value);
      }
    }
    // Process the normal ones
    for (Enumeration<String> e=_propertyTable.keys(); e.hasMoreElements();) {
      String key = e.nextElement();
      setProperty(key, _propertyTable.get(key));
    }
    // Finally proccess those which need to be the last ones
    for (Enumeration<String> e=postTable.keys(); e.hasMoreElements();) {
      String key = e.nextElement();
      setProperty(key,postTable.get(key));
    }
    return this;
  }

 /**
  * Returns the value of a property.
  * @param     String _property The property name
  */
  final public String getProperty (String _property) {
    return myPropertiesTable.get(_property);
  }


 /**
  * Returns wether a property information contains a given keyword in its preamble
  * @param     String _property The property name
  * @param     String _keyword The keyword to look for
  */
  final public boolean propertyIsTypeOf(String _property, String _keyword) {
    String info = getPropertyInfo(_property);
    if (info==null) return false;
    _keyword = _keyword.toLowerCase();
    StringTokenizer tkn = new StringTokenizer(info," |");
    while (tkn.hasMoreTokens()) if (tkn.nextToken().toLowerCase().equals(_keyword)) return true;
    return false;
  }
  
 /**
  * Returns the type of the property
  * @param     String _property The property name
  * @return    String The type of the property
  */
  final public String propertyType(String _property) {
    String info = getPropertyInfo(_property);
    if (info==null) return "double";
    StringTokenizer tkn = new StringTokenizer(info," ");
    if (tkn.countTokens()>=1) return tkn.nextToken();
    return "double";
  }

  public java.awt.Component getComponent() { return null; }

 /**
  * Provided for backwards compatibiliy only
  */
  public java.awt.Component getVisual() { return null; }

  /**
   * Whether this element can have children of the given type
   * @param _child ControlElement
   * @return boolean
   */
  public boolean acceptsChild (ControlElement _child) {
    return false;
  }

  /**
  * resets the element
  */
  public void reset() { }

 /**
  * initializes the element. A kind of soft reset()
  */
  public void initialize() { }

  /**
   * perform any clean-up on exit
   */
   public void onExit() { }

   /**
    * Flush last data (only used by InteractiveTraces, for the moment)
    */
   @Deprecated
    final public void flush() { }

   
 /**
  * refresh the element
  */
//  final public void  update() { } Moved to interface NeedsUpdate

 /**
  * Returns the integer index of a given variable property
  */
  public int propertyIndex(String _property) {
    if (myPropertiesNames!=null)
      for (int i=0; i<myPropertiesNames.length; i++)
        if (myPropertiesNames[i].equals(_property)) return i;
    return -1;
  }

  /**
   * Whether the element implements a given property
   * @param _property the property
   */
   public boolean implementsProperty (String _property) {
     return (propertyIndex(_property)>=0);
   }

 /**
  * Clear all registered internal variable properties
  */
  final public void variablePropertiesClear() {
    if (myPropertiesNames!=null)
      for (int i=0; i<myPropertiesNames.length; i++)
        setProperty(myPropertiesNames[i],null);
  }

/**
  * Reports its  name, if it has been set. If not, returns
  * a standard value.
  */
  public String toString() {
    String name = myPropertiesTable.get("name");
    if (name!=null) return name;
    String text = this.getClass().getName();
    int index = text.lastIndexOf(".");
    if (index>=0) text = text.substring (index+1);
    return "Unnamed element of type "+text;
  }

 /**
  * Clears any trace of myself (specially in the group)
  */
  public void destroy () {
    setProperty("parent",null);
    if (myProperties!=null)
      for (int i=0; i<myProperties.length; i++) {
        if (myProperties[i]!=null) myProperties[i].removeElementListener(this,i);
      }
  }

// ------------------------------------------------
// Actions
// ------------------------------------------------

 /**
  * Defines a generic action that can be invoked from this
  * <code>ControlElement</code>. It is the responsability of implementing
  * classes to decide what actions types can be invoked and how.
  * <p>
  * If the method field is not a valid method for this target object
  * it will ignore the command (and perhaps print an error message).
  * <p>
  * @return    This same element. This is useful to nest it with
  *    other calls to <code>setProperty</code> or <code>adAction</code>.
  * @param     int _type      The action type
  * @param     Object _target The object whose method will be invoked
  * @param     String _method The method to call in the target object.
  * The method can accept a single CONSTANT parameter, either boolean, int,
  * double or String. See MethodWithOneParameter for more details.
  */
  final public ControlElement addAction (int _type, Object _target, String _method) {
    myActionsList.addElement (new MethodWithOneParameter(_type, _target, _method, null, null,this));  // null = void, null = no2nd action
    return this;
  }

 /**
  * This is an advanced form of addAction that allows for nested actions
  */
  final public ControlElement addAction (int _type, Object _target, String _method,
                                         MethodWithOneParameter _secondAction) {
    myActionsList.addElement (new MethodWithOneParameter(_type, _target, _method, null, _secondAction,this)); // null = void
    return this;
  }


 /**
  * Similar to the other addAction but extracts the target from the method,
  * which must be of the form 'target.method:optional parameter', where
  * target has been previously added to the list of targets of the group.
  */
  final public ControlElement addAction (int _type, String _method) {
    // A special entry point for Ejs
    if (getProperty("_ejs_")!=null) _method = "_ejs_.execute(\""+_method+"\")";

    Object target = null;
    MethodWithOneParameter secondAction=null;
    String parts[] = MethodWithOneParameter.splitMethodName(_method);
    if (parts==null) {
      System.err.println (getClass().getName()+" : Error! Method <"+_method+"> not assigned");
      return this;
    }
    if (parts[0]==null) parts[0] = "_default_";
    if (myGroup!=null) {
      target = myGroup.getTarget(parts[0]);
      // Only ACTIONs can have a second ACTION
//      if (_type==ACTION  // BUG!!! this caused other actions such as PRESS_ACTION NOT to call the second action
//                         // actually, only VARIABLE_CHANGED does call update() by default.
      if (_type!=VARIABLE_CHANGED
          && getProperty("_ejs_SecondAction_")!=null && myGroup.getTarget("_default_")!=null) {
        secondAction =  new MethodWithOneParameter(_type, myGroup.getTarget("_default_"),
          getProperty("_ejs_SecondAction_"),null,null,this); // null = void , null= no 2nd action
      }
    }
    if (target==null) {
      System.err.println (getClass().getName()+" : Error! Target <"+parts[0]+"> not assigned");
      System.err.println ("when adding action property "+_type+ " to "+_method);
      return this;
    }
    if (parts[2]==null) return addAction (_type, target, parts[1]+"()", secondAction);
    return addAction (_type, target, parts[1]+"("+parts[2]+")", secondAction);
  }

 /**
  * Removes an action. If the action does not exists, it does nothing.
  * <p>
  * @param     int _type      The action type
  * @param     Object _target The object whose method will be invoked
  * @param     String _method The method to call in the target object.
  * @see addAction(int,Object,String)
  */
  final public void removeAction (int _type, Object _target, String _method) {
    if (_method==null) return;
    for (Enumeration<MethodWithOneParameter> e = myActionsList.elements() ; e.hasMoreElements() ;) {
      MethodWithOneParameter meth = e.nextElement();
      if (meth.equals(_type, _target, _method)) {
        if (!myActionsList.removeElement(meth))
          System.err.println (getClass().getName()+": Error! Action "+_method+" not removed");
        return;
      }
    }
  }

 /**
  * Similar to removeAction but extracts the target from the method
  */
  final public void removeAction (int _type, String _method) {
    if (_method==null) return;

    // A special entry point for Ejs
    if (getProperty("_ejs_")!=null) _method = "_ejs_.execute(\""+_method+"\")";

    String parts[] = MethodWithOneParameter.splitMethodName(_method);
    if (parts==null) {
      System.err.println (getClass().getName()+" : Error! Method <"+_method+"> not removed");
      return;
    }
    if (parts[0]==null) parts[0] = "_default_";
    Object target = null;
    if (myGroup!=null) target = myGroup.getTarget(parts[0]);
    if (target==null) {
      System.err.println (getClass().getName()+" : Error! Target <"+parts[0]+"> not assigned");
      System.err.println ("when removing action property "+_type+ " : "+_method);
      return;
    }
    removeAction (_type, target, parts[1]+"("+parts[2]+")");
  }

 /**
  * Invokes all actions of type ACTION
  */
  final public void invokeActions () { invokeActions (ControlElement.ACTION); }

  final private boolean isControlUpdatingSimulation() {
    if (myGroup!=null) return myGroup.isUpdatingSimulation();
    return true;
  }
  
  /**
   * Whether actions must be invoked right now or only when the simulation is idle (default)
   * @param _rightNow
   */
  final protected void setImmediateActions(boolean _rightNow) { actionsWhenIdle = !_rightNow; }

  /**
   * Whether actions must be invoked right now or only when the simulation is idle (default)
   */
  final protected boolean hasDelayedActions() { return actionsWhenIdle; }

 /**
  * Invokes all actions of this BasicControl of a given type
  * @param     int _type  The action type
  */
  final public void invokeActions (int _type) {
    if (myActiveState && isControlUpdatingSimulation()) {
      Simulation sim = getSimulation();
      if (sim!=null && actionsWhenIdle) { // delay actions until idle
        for (MethodWithOneParameter method : myActionsList) sim.invokeMethodWhenIdle(new MethodDelayedAction(method, _type, this));
      }
      else for (MethodWithOneParameter method : myActionsList) method.invoke (_type,this);
    }
    // Next line (for _model actions only!) would make unnecesary the trick of the secondActions!
    // I still use this choice because otherwise a button that calls simulation.step() would update twice
//    if (myGroup!=null) myGroup.updateSimulation();
  }

 /**
  * Reports changes of internal variables but simulation doesn't update
  * Needed by RadioButtons
  * @param     int _variableIndex the index of the internal variable that changed
  * @param     Value _value the new value for the variable
  */
  final public void variableChangedDoNotUpdate (int _variableIndex, Value _value) {
    // Changing the order of next two sentences is important!!!!
    if (myGroup!=null && myProperties!=null) {
      myGroup.setReportingChange(true);
      myGroup.variableChanged(myProperties[_variableIndex],this,_value);
      myGroup.setReportingChange(false);
    }
    if (myActiveState && isControlUpdatingSimulation()) {
      Simulation sim = getSimulation();
      if (sim!=null && actionsWhenIdle) for (MethodWithOneParameter method : myActionsList) sim.invokeMethodWhenIdle(new MethodDelayedAction(method, ControlElement.VARIABLE_CHANGED, this));
      else for (MethodWithOneParameter method : myActionsList) method.invoke (ControlElement.VARIABLE_CHANGED,this);
    }

//    if (myActiveState) for (Enumeration<MethodWithOneParameter> e = myActionsList.elements() ; e.hasMoreElements() ;) {
//      MethodWithOneParameter method = e.nextElement();
//      method.invoke (ControlElement.VARIABLE_CHANGED,this);
//    }
 }

 /**
  * Reports changes of internal variables
  * @param     int _variableIndex the index of the internal variable that changed
  * @param     Value _value the new value for the variable
  */
  final public void variableChanged (int _variableIndex, Value _value) {
    if (myMethodsForProperties[_variableIndex]!=null) { // AMAVP
//      System.out.println ("Do not update because of method "+myMethodsForProperties[_variableIndex].toString());
      return;
    }
    variableChangedDoNotUpdate (_variableIndex, _value);
    // Next line should apply only to model actions, but these are the only ones expected.
    // Assigning a slider a simulation.pause() would trigger a lot of update()s!
    if (myGroup!=null) myGroup.updateSimulationWhenIdle();
  }

  /**
   * A special variant of variableChanged. Required by element which can change
   * a secondary value
   */
   final public void variableExtraChanged (int _variableIndex, Value _value) {
     if (myMethodsForProperties[_variableIndex]!=null) { // AMAVP
//      System.out.println ("Do not update because of method "+myMethodsForProperties[_variableIndex].toString());
       return;
     }
     // Changing the order of next two sentences is important!!!!
     if (myGroup!=null && myProperties!=null)
       myGroup.variableChanged(myProperties[_variableIndex],this,_value);
     // Next line should apply only to model actions, but these are the only ones expected.
     // Assigning a slider a simulation.pause() would trigger a lot of update()s!
     if (myGroup!=null) myGroup.updateSimulationWhenIdle();
   }


 /**
  * Reports changes of more than one internal variables
  * @param     int[] _variableIndexes the indexes of the internal variables that changed
  * @param     Value[] _value the new values for the variables
  */
  final public void variablesChanged (int[] _variableIndex, Value[] _value) {
    boolean doMore = false;
    if (myGroup!=null && myProperties!=null) {
      for (int i=0; i<_variableIndex.length; i++) {
        if (myMethodsForProperties[_variableIndex[i]]==null) { // AMAVP
//          System.out.println ("Do not update this one because of method "+myMethodsForProperties[_variableIndex[i]].toString());
          myGroup.variableChanged(myProperties[_variableIndex[i]],this,_value[i]);
          doMore = true;
        }
      }
    }
    if (!doMore) return; // AMAVP Nothing has changed
    if (myActiveState) {
      Simulation sim = getSimulation();
      if (sim!=null && actionsWhenIdle) for (MethodWithOneParameter method : myActionsList) sim.invokeMethodWhenIdle(new MethodDelayedAction(method, ControlElement.VARIABLE_CHANGED, this));
      else for (MethodWithOneParameter method : myActionsList) method.invoke (ControlElement.VARIABLE_CHANGED,this);
    }
//    if (myActiveState) for (MethodWithOneParameter method : myActionsList) method.invoke (ControlElement.VARIABLE_CHANGED,this);
    if (myGroup!=null) myGroup.updateSimulationWhenIdle();
  }

 /**
  * Sets whether a <code>ControlElement</code> actually invokes actions.
  * The default is true.
  * @param   boolean _active Whether it is active
  */
  final public void setActive (boolean _act) { myActiveState = _act; }

 /**
  * Returns the active status of the <code>ControlElement</code>.
  */
  final public boolean isActive () { return myActiveState; }
  
// ------------------------------------------------
// Group behavior
// ------------------------------------------------

 /**
  * Sets the EjsControl in which to operate
  * @param EjsControl _group   The EjsControl
  */
  final public void setGroup (EjsControl _group) { myGroup = _group; }

 /**
  * Gets the EjsControl in which it operates
  * @return the EjsControl
  */
  final public EjsControl getGroup () { return myGroup; }

 /**
  * Gets the Simulation in which it runs
  * @return the Simulation
  */
  final public Simulation getSimulation () {
    if (myGroup==null) return null;
    return myGroup.getSimulation();
  }

  /**
   * Gets the Utils of the group in which it runs
   * @return the Utils instance of the group or a new one
   *
   final public Utils getUtils () {
     if (myGroup==null) return new Utils();
     return myGroup.getUtils();
   }
*/
  
  /**
   * Used to delay invoking methods
   */
  static private class MethodDelayedAction implements org.colos.ejs.library.DelayedAction {
    MethodWithOneParameter method;
    int type;
    Object object;
    
    private MethodDelayedAction (MethodWithOneParameter _method, int _type, Object _object) {
      this.method = _method;
      this.type = _type;
      this.object = _object;
    }
    
//    private Value invoke() { return method.invoke(type, object); }

    public void performAction() { method.invoke(type, object); }
    
  }
 

} // End of Class




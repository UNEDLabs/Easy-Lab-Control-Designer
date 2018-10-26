/*
 * The control package contains utilities to build and control
 * simulations using a central control.
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control;

import org.colos.ejs.library.control.value.*;

import java.util.*;

/**
 * A utility class that holds information about a value that can be shared
 * by different ControlElement internal variables and that can also trigger
 * methods of a class
 */
public class GroupVariable {
  private String name;
  private Value value;
  private boolean definedInModel=false, obsolete=false;
  private Vector<Item> elementList;
  private Vector<MethodWithOneParameter> methodList;

  // A GroupVariable should be created with a non-null value
  // that matches the type it is going to be used.
  public GroupVariable (String _aName, Value _aValue) {
    name = _aName;
    elementList = new Vector<Item>();
    methodList  = new Vector<MethodWithOneParameter> ();
//    value = _aValue.cloneValue();
    if (_aValue!=null) value = _aValue.cloneValue();
    else value  = new DoubleValue(0.0);
//    else value = null; // This is rather dangerous if one doesn't follow the instructions above
  }

  public String getName () { return name; }

  public String toString() { return name; }

  public void setValue (Value _aValue) {
    // This can be optimized by removing the check
    // Again, this forces the instantiation to hold a non-null Value
    // which must hold the right subclass of Value
    // Unfortunately Ejs' users tend to modify the variable class
    if (value.getClass()!=_aValue.getClass()) value = _aValue.cloneValue();
    else
      value.copyValue(_aValue);
  }

  public Value getValue () { return value; }

  public void setDefinedInModel (boolean _defined) { definedInModel = _defined; }

  public boolean isDefinedInModel () { return definedInModel; }

  public void setValueObsolete (boolean _obsolete) { obsolete = _obsolete; }

  public boolean isValueObsolete () { return obsolete; }

// --------------------------------------------------------
// Adding and removing control elements
// --------------------------------------------------------

  public void addElementListener (ControlElement _element, int _index) {
    elementList.add (new Item(_element, _index));
  }

  public boolean hasElementsRegistered () {
    return !elementList.isEmpty();
  }

  public void removeElementListener (ControlElement _element, int _index) {
    for (Item item : elementList) {
      if (item.element==_element && item.index==_index) {
        elementList.removeElement(item);
        return;
      }
    }
  }

  public void propagateValue (ControlElement _element, boolean _collectingData) {
    if (_collectingData) {
      for (Enumeration<Item> e = elementList.elements() ; e.hasMoreElements() ;) {
        Item item = e.nextElement();
        if (item.element instanceof DataCollector) {
          item.element.setActive(false);
          if (item.element.myMethodsForProperties[item.index]!=null) { // AMAVP (See note in ControlElement)
            item.element.setValue(item.index,item.element.myMethodsForProperties[item.index]
                .invoke(ControlElement.METHOD_FOR_VARIABLE,null)); // null = no calling object
          }
          else if (item.element.myExpressionsForProperties[item.index]!=null) { // AMAVP (See note in ControlElement)
            item.element.setValue(item.index,item.element.myExpressionsForProperties[item.index]);
          }
          else item.element.setValue(item.index,value);
          item.element.setActive(true);
        }
      }
    }
    else {
      for (Enumeration<Item> e = elementList.elements() ; e.hasMoreElements() ;) {
        Item item = e.nextElement();
        if (item.element!=_element) {
//        if (!item.element.isActive()) continue; // This would avoid infinite loops, but may have side-effects
          item.element.setActive(false);
          if (item.element.myMethodsForProperties[item.index]!=null) { // AMAVP (See note in ControlElement)
//          System.out.println ("I  call the method "+item.element.myMethodsForProperties[item.index].toString()+ "first!");
            item.element.setValue(item.index,item.element.myMethodsForProperties[item.index]
                .invoke(ControlElement.METHOD_FOR_VARIABLE,null)); // null = no calling object
          }
          else if (item.element.myExpressionsForProperties[item.index]!=null) { // AMAVP (See note in ControlElement)
            //System.out.println ("I  call the expression "+((InterpretedValue)item.element.myExpressionsForProperties[item.index]).getExpression()+ "first!");
            item.element.setValue(item.index,item.element.myExpressionsForProperties[item.index]);
          }
          else item.element.setValue(item.index,value);
          item.element.setActive(true);
        }
      }
    }
  }

// --------------------------------------------------------
// Adding and removing method elements
// --------------------------------------------------------

  public void addListener (Object _target, String _method) { addListener (_target,_method,null); }

  public void addListener (Object _target, String _method, Object _anObject) {
    methodList.add(new MethodWithOneParameter (ControlElement.VARIABLE_CHANGED,_target,_method,null,null,_anObject));
  }

  public void removeListener (Object _target, String _method) {
    for (MethodWithOneParameter method : methodList) {
      if (method.equals(ControlElement.VARIABLE_CHANGED,_target, _method)) {
        methodList.removeElement(method);
        return;
      }
    }
  }

  public void invokeListeners (ControlElement _element) {
    for (MethodWithOneParameter method : methodList) method.invoke(ControlElement.VARIABLE_CHANGED,_element);
  }

// --------------------------------------------------------
// Internal classes
// --------------------------------------------------------

  private class Item {
    public ControlElement element;
    public int index;
    Item (ControlElement _anElement, int _anIndex) {
      element = _anElement;
      index   = _anIndex;
    }
  }

}

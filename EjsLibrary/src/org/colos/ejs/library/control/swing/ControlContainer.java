/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

/**
 * A configurable Container
 */
public abstract class ControlContainer extends ControlSwingElement {
  static private final BooleanValue falseValue = new BooleanValue(false);
  protected java.util.Vector<ControlSwingElement> radioButtons = new java.util.Vector<ControlSwingElement>();
  protected java.util.Vector<ControlElement> children = new java.util.Vector<ControlElement>();

  // This is not final since windows may change the default case (this one)
  public java.awt.Container getContainer () { return (java.awt.Container) getVisual(); }

// ------------------------------------------------
// Own methods
// ------------------------------------------------

  public boolean acceptsChild (ControlElement _child) {
    if (_child.getVisual() instanceof javax.swing.JMenuBar) return false;
    if (_child.getVisual() instanceof javax.swing.JMenuItem) return false;
    if (_child instanceof ControlSwingElement) return true;
    return false;
  }

 /**
  * adds a child control
  * @param _child the child control
  */
  public void add(ControlElement _child) {
    children.add(_child);
    java.awt.Container container = getContainer();
    java.awt.LayoutManager layout = container.getLayout();
    // This is set by Ejs to allow changing the natural order of childhood
    String indexInParent = _child.getProperty("_ejs_indexInParent_");
    int index = -1;
    if (indexInParent!=null) index = Integer.parseInt(indexInParent);
    _child.setProperty("_ejs_indexInParent_",null);
    if (layout instanceof java.awt.BorderLayout) {
      String pos = _child.getProperty("position");
      if (pos!=null) container.add(_child.getComponent(),ConstantParser.constraintsConstant(pos).getString(),index);
      else container.add(_child.getComponent(),java.awt.BorderLayout.CENTER,index);
    }
    else container.add(_child.getComponent(),index);
    adjustSize();
    if (_child  instanceof RadioButtonInterface) {
      radioButtons.add((ControlSwingElement)_child);
      ((RadioButtonInterface)_child).setControlParent(this);
    }
    // Now propagate my own font, foreground and background;
    propagateProperty (_child,"font"      ,getPropagatedProperty("font"));
    propagateProperty (_child,"foreground",getPropagatedProperty("foreground"));
    propagateProperty (_child,"background",getPropagatedProperty("background"));
  }

  protected void adjustChildren() {
    java.awt.Container container = getContainer();
    if (container.getLayout() instanceof java.awt.BorderLayout) {
      container.removeAll();
      java.util.List<String> freeSlots = new java.util.ArrayList<String>();
      freeSlots.add("center"); freeSlots.add("north"); freeSlots.add("south");
      freeSlots.add("east"); freeSlots.add("west");
      for (ControlElement child : children) {
        String pos = child.getProperty("position");
        if (pos==null) {
          if (!freeSlots.isEmpty()) pos = freeSlots.get(0);
        }
        if (pos==null) pos = "center"; // Not enough room
        else freeSlots.remove(pos);
        container.add(child.getComponent(),ConstantParser.constraintsConstant(pos).getString(),-1);
      }
    }
  }

  public void adjustSize() {
    getContainer().validate();
    getContainer().repaint();
    resizeContainer (getContainer());
    resizeContainer (getComponent().getParent());
  }

  static private void resizeContainer(java.awt.Container _container) {
    if (_container==null) return;
    java.awt.Rectangle b = _container.getBounds();
    _container.setBounds (b.x,b.y,b.width+1,b.height+1);
    _container.setBounds (b.x,b.y,b.width,b.height);
    _container.validate();
    _container.repaint();
  }

 /**
  * Returns the vector of children
  */
  public java.util.Vector<ControlElement> getChildren() { return children; }

 /**
  * removes a child control
  * @param _child the child control
  */
  public void remove(ControlElement _child) {
    children.remove(_child);
    java.awt.Container container = getContainer();
    container.remove(_child.getComponent());
    if (_child instanceof RadioButtonInterface) {
      radioButtons.remove(_child);
      ((RadioButtonInterface)_child).setControlParent((ControlContainer)null);
    }
    adjustSize();
  }

  public void informRadioGroup(RadioButtonInterface _source, boolean _state) {
    if (_state==false) return;
    for (ControlSwingElement rb : radioButtons) {
      if (rb!=_source) {
        boolean wasActive = rb.isActive();
        rb.setActive(false);
        rb.setValue (_source.getVariableIndex(),falseValue);
        ((RadioButtonInterface)rb).reportChanges ();
        rb.setActive(wasActive);
      }
    }
  }

// ------------------------------------------------
// Private methods
// ------------------------------------------------

  protected void propagateProperty (ControlElement _child, String _property, String _value) {
    if (_child.getProperty(_property)==null) _child.setProperty(_property,_value,false); // propagates but doesn't store it
  }

  protected void propagateProperty (String _property, String _value) {
//    System.out.println(this+" propagating property "+_property);
    for (int i=0; i<children.size(); i++) propagateProperty (children.elementAt(i),_property,_value);
  }

  /**
   * Returns either the value of my own property or that of any of my parents, if they have it set.
   * Only used to propagate properties.
   * @param _property String
   * @return String
   */
  protected String getPropagatedProperty (String _property) {
    String prop = getProperty(_property);
    if (prop!=null) return prop;
    ControlElement parent = myGroup.getElement(getProperty("parent"));
    if (parent!=null && parent instanceof ControlContainer) return ((ControlContainer)parent).getPropagatedProperty(_property);
    return null;
  }


// ------------------------------------------------
// Properties
// ------------------------------------------------

  // This is neccesary because otherwise setting a container background sets the
  // background of all children in Ejs
  public ControlElement setProperty (String _property, String _value, boolean _store) {
    ControlElement returnValue = super.setProperty (_property,_value,_store);
    if (_property.equals("font") || _property.equals("foreground") || _property.equals("background"))
      propagateProperty (_property,_value);
    return returnValue;
  }

/*
  public ControlElement setProperty (String _property, String _value) {
    ControlElement returnValue = super.setProperty (_property,_value);
    if (_property.equals("font") || _property.equals("foreground") || _property.equals("background"))
      propagateProperty (_property,_value);
    return returnValue;
  }
*/

} // End of class

/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import java.util.StringTokenizer;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A configurabel TabbedPanel
 */
public class ControlTabbedPanel extends ControlContainer {
  static private final int TABBEDPANEL_PROPERTIES = 3;
  static private final int TABBEDPANEL_SELECTED_INDEX = 2;
  static private final int TABBEDPANEL_TOOLTIP = ControlSwingElement.TOOLTIP + TABBEDPANEL_PROPERTIES;
  
  protected JTabbedPane tabbedpanel;
  private String[] tabTitles=null, tooltipsArray=null;
  protected IntegerValue internalValue;
  private boolean addingChild = false;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    tabbedpanel=new JTabbedPane (SwingConstants.TOP);
    internalValue = new IntegerValue(-1);

    tabbedpanel.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent arg0) {
        internalValue.value = tabbedpanel.getSelectedIndex();
        variableChanged (TABBEDPANEL_SELECTED_INDEX,internalValue);
//        invokeActions ();
        if (isUnderEjs && !addingChild) setFieldListValue(TABBEDPANEL_SELECTED_INDEX,internalValue);
      }
    });
    return tabbedpanel;
  }

  public void add(ControlElement _child) {
    String header = _child.getProperty("name");
    if (tabTitles!=null) {
      int index = tabbedpanel.getTabCount();
      if (index<tabTitles.length) header = tabTitles[index];
    }
    addingChild = true;
    if (header!=null) tabbedpanel.add(_child.getComponent(),header);
    else tabbedpanel.add(_child.getComponent(),"   ");
    addingChild = false;
    if (_child instanceof RadioButtonInterface) {
      radioButtons.add((ControlSwingElement)_child);
      ((RadioButtonInterface)_child).setControlParent(this);
    }
  }

  private void refreshTitles() {
    if (tabTitles==null)  {
      int n=tabbedpanel.getTabCount();
      for (int i=0; i<n; i++) {
        String header = tabbedpanel.getComponentAt(i).getName();
        if (header!=null) tabbedpanel.setTitleAt(i, header);
      }
      return;
    }
    int n=Math.min(tabbedpanel.getTabCount(),tabTitles.length);
    for (int i=0; i<n; i++) tabbedpanel.setTitleAt(i, tabTitles[i]);
  }
  
  private void refreshToolTips() {
    if (tooltipsArray==null) {
      int n=tabbedpanel.getTabCount();
      for (int i=0; i<n; i++) {
        String tooltip = tabbedpanel.getComponentAt(i).getName();
        if (tooltip!=null) tabbedpanel.setToolTipTextAt(i, tooltip);
      }
      return;
    }
    int n=Math.min(tabbedpanel.getTabCount(),tooltipsArray.length);
    for (int i=0; i<n; i++) tabbedpanel.setToolTipTextAt(i, tooltipsArray[i]);
  }
  
// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("placement");
      infoList.add ("tabTitles");
      infoList.add ("selected");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("placement")) return "Placement|int";
    if (_property.equals("tabTitles")) return "String|String[] TRANSLATABLE";
    if (_property.equals("tooltip"))   return "String|String[] TRANSLATABLE";
    if (_property.equals("selected"))  return "int";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 :
        if (tabbedpanel.getTabPlacement()!=_value.getInteger()) tabbedpanel.setTabPlacement(_value.getInteger());
        break;
      case 1 :
        if (_value.getObject() instanceof String[]) tabTitles = (String[]) _value.getObject();
        else {
          String text = _value.getString();
          if (text==null) tabTitles = null;
          else {
            StringTokenizer tkn = new StringTokenizer(text,",");
            tabTitles = new String[tkn.countTokens()];
            int c = 0;
            while (tkn.hasMoreTokens()) { 
              tabTitles[c] = org.opensourcephysics.display.TeXParser.parseTeX(tkn.nextToken()); 
              c++; 
            }
          }
        }
        refreshTitles();
        break;
      case TABBEDPANEL_SELECTED_INDEX : 
        int tab = _value.getInteger();
        if (tab>=0 && tab<tabbedpanel.getTabCount()) tabbedpanel.setSelectedIndex(tab); 
        break;
      default: super.setValue(_index-TABBEDPANEL_PROPERTIES,_value); break;
      
      case TABBEDPANEL_TOOLTIP : 
        if (_value.getObject() instanceof String[]) tooltipsArray = (String[]) _value.getObject();
        else {
          String text = _value.getString();
          if (text==null) tooltipsArray = null;
          else {
            StringTokenizer tkn = new StringTokenizer(text,",");
            tooltipsArray = new String[tkn.countTokens()];
            int c = 0;
            while (tkn.hasMoreTokens()) { 
              tooltipsArray[c] = org.opensourcephysics.display.TeXParser.parseTeX(tkn.nextToken()); 
              c++; 
            }
          }
        }
        refreshToolTips();
        break;
        
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : tabbedpanel.setTabPlacement(javax.swing.SwingConstants.TOP); break;
      case 1 : tabTitles = null; refreshTitles(); break;
      case TABBEDPANEL_SELECTED_INDEX : break;

      default: super.setDefaultValue(_index-TABBEDPANEL_PROPERTIES); break;
      
      case TABBEDPANEL_TOOLTIP : tooltipsArray = null; refreshToolTips(); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "TOP";
      case 1 : return "<none>";
      case 2 : return "<none>";
      default : return super.getDefaultValueString(_index-TABBEDPANEL_PROPERTIES);
    }
  }
  
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 :
        return null;
      default: return super.getValue(_index-TABBEDPANEL_PROPERTIES);
    }
  }


} // End of class




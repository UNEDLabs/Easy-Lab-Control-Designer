/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) March 2007 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.*;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.OSPRuntime;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.JTable;


/**
 * A configurable control button. It will trigger an action when clicked.
 * It has no internal value.
 */
public class ControlDataTable extends ControlSwingElement implements NeedsUpdate, NeedsFinalUpdate, DataCollector, Resetable {
  static private final int TABLE_ADDED=9;
//  static private final int MAX_INPUT=5;

  private boolean noRepeat=true, isSet=false, active=true;
  private double[] input, oldInput;
  private String[] columnNames=new String[0], columnFormats=new String[] {"0.000;-0.000"};
  private org.opensourcephysics.display.DataPanel dataPanel;
  private String numericFormat = "0.000";
//  private javax.swing.JScrollPane scrollPanel;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    dataPanel = new org.opensourcephysics.display.DataPanel();
    dataPanel.setRowNumberVisible(false);
    dataPanel.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); 
    for (int i=0,n=dataPanel.getColumnCount(); i<n; i++) dataPanel.setColumnFormat(i,"0.000;-0.000");
    input = oldInput = null;
    isSet = false;
    dataPanel.getVisual().addMouseListener (new MouseAdapter() {
      public void mousePressed  (MouseEvent _evt) {
        if (getSimulation()==null || !OSPRuntime.isPopupTrigger(_evt)) return;  
        getPopupMenu(_evt.getX(), _evt.getY());
      }
    });
//    scrollPanel = new javax.swing.JScrollPane(dataPanel);
    return dataPanel;
  }

  public String getObjectClassname () { return "org.opensourcephysics.display.DataPanel"; }

  public Object getObject () { return dataPanel; }

  public java.awt.Component getComponent () { return dataPanel; } // scrollPanel; }

  public void initialize () { // Overwrites default initialize
    reset();
  }

  public void reset () { // Overwrites default reset
      dataPanel.clearData();
      input = oldInput = null;
      isSet = false;
  }

  public void onExit () { // free memory
    reset();
  }

  public void update() {
//    System.out.println ("Adding ");
    if (active && isSet && input!=null) {
      if (noRepeat) {
        if (oldInput==null || input.length!=oldInput.length) oldInput = new double[input.length];
        else {
          boolean equal = true;
          for (int i=0; i<input.length; i++) if (input[i]!=oldInput[i]) { equal = false; break; }
          if (equal) return;
        }
        for (int i=0; i<input.length; i++) oldInput[i] = input[i];
      }
      int count = dataPanel.getColumnCount();
      dataPanel.appendRow(input);
      if (input.length!=count) {
        for (int i=0,n=Math.min(columnNames.length,  input.length); i<n; i++) dataPanel.setColumnNames(i,columnNames[i]);
        if (columnFormats==null) {
//        	System.out.println ("Setting numeric format to "+numericFormat);
        	dataPanel.setNumericFormat(numericFormat);
        }
        else if (columnFormats.length>=input.length) {
          for (int i=0,n=input.length; i<n; i++) dataPanel.setColumnFormat(i,columnFormats[i]);
        }
        else {
          for (int i=0,n=columnFormats.length; i<n; i++) dataPanel.setColumnFormat(i,columnFormats[i]);
          for (int i=columnFormats.length,n=input.length; i<n; i++) dataPanel.setColumnFormat(i,columnFormats[columnFormats.length-1]);
        }
      }
    }
  }
  
  public void finalUpdate() {
//    if (myGroup!=null && myGroup.isCollectingData()) return;
    dataPanel.refreshTable();
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("input");
      infoList.add ("maxPoints");
      infoList.add ("stride");
      infoList.add ("active");
      infoList.add ("norepeat");
      infoList.add ("showRowNumber");
      infoList.add ("columnNames");
      infoList.add ("columnFormat");
      infoList.add ("autoResizeMode");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("input"))     return "int|double|double[]";
    if (_property.equals("maxPoints")) return "int";
    if (_property.equals("stride"))    return "int";
    if (_property.equals("active"))    return "boolean";
    if (_property.equals("norepeat"))  return "boolean";
    if (_property.equals("showRowNumber")) return "boolean";
    if (_property.equals("columnNames"))   return "String|String[] TRANSLATABLE";
    if (_property.equals("columnFormat"))  return "String|String[] TRANSLATABLE";
    if (_property.equals("autoResizeMode")) return "AutoResizeMode|int";
    return super.getPropertyInfo(_property);

  }

  public Value parseConstant (String _propertyType, String _value) {
    if (_value==null) return null;
    if (_propertyType.indexOf("AutoResizeMode")>=0) {
      _value = _value.trim().toLowerCase();
      if (_value.equals("off"))        return new IntegerValue (JTable.AUTO_RESIZE_OFF);
      if (_value.equals("next"))       return new IntegerValue (JTable.AUTO_RESIZE_NEXT_COLUMN);
      if (_value.equals("subsequent")) return new IntegerValue (JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
      if (_value.equals("last"))       return new IntegerValue (JTable.AUTO_RESIZE_LAST_COLUMN);
      if (_value.equals("all"))        return new IntegerValue (JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    return super.parseConstant(_propertyType, _value);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
//    System.out.println ("Setting property "+_index+" to "+_value);
    switch (_index) {
      case 0 :
        if (_value.getObject() instanceof double[]) input = (double[]) _value.getObject();
        else input = new double[] { _value.getDouble() };
        isSet = true;
        break;
      case 1 : dataPanel.setMaxPoints(_value.getInteger()); break;
      case 2 : dataPanel.setStride(_value.getInteger()); break;
      case 3 : active = _value.getBoolean(); break;
      case 4 : noRepeat = _value.getBoolean(); break;
      case 5 : dataPanel.setRowNumberVisible(_value.getBoolean()); break;
      case 6 :
        if (_value.getObject() instanceof String[]) columnNames = (String[]) _value.getObject();
        else {
          String names = _value.getString();
          StringTokenizer tkn = new StringTokenizer(names,",");
          columnNames = new String[tkn.countTokens()];
          int c = 0;
          while (tkn.hasMoreTokens()) {
            columnNames[c] = org.opensourcephysics.display.TeXParser.parseTeX(tkn.nextToken()); 
            c++; 
          }
        }
        for (int i=0,n=columnNames.length; i<n; i++) dataPanel.setColumnNames(i,columnNames[i]);
        break;
      case 7 :
        if (_value.getObject() instanceof String[]) columnFormats = (String[]) _value.getObject();
        else {
          String formats = _value.getString();
          StringTokenizer tkn = new StringTokenizer(formats,",");
          int nTokens = tkn.countTokens();
          if (nTokens<=1) {
        	  columnFormats = null;
//              System.out.println ("Setting numeric format to "+numericFormat);
        	  dataPanel.clearFormats();
        	  dataPanel.setNumericFormat(numericFormat=formats);
        	  return;
          }
          columnFormats = new String[nTokens];
          int c = 0;
          while (tkn.hasMoreTokens()) { 
            columnFormats[c] = org.opensourcephysics.display.TeXParser.parseTeX(tkn.nextToken()); 
            c++; 
          }
        }
        for (int i=0,n=columnFormats.length; i<n; i++) dataPanel.setColumnFormat(i,columnFormats[i]);
        break;
      case 8 : dataPanel.setAutoResizeMode(_value.getInteger()); break;
      default: super.setValue(_index-TABLE_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : input = oldInput = null; isSet = false; break;
      case 1 : dataPanel.setMaxPoints(-1); break;
      case 2 : dataPanel.setStride(1); break;
      case 3 : active = true; break;
      case 4 : noRepeat = true; break;
      case 5 : dataPanel.setRowNumberVisible(false); break;
      case 6 : break;
      case 7 : 
    	  columnFormats = null;
          numericFormat = "0.000;-0.000";
          dataPanel.setNumericFormat(numericFormat);
          break;
      case 8 : dataPanel.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); break;
      default: super.setDefaultValue(_index-TABLE_ADDED); break;
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 : case 4 :
      case 5 : case 6 : case 7 : case 8 :
        return null;
      default: return super.getValue(_index-TABLE_ADDED);
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "-1";
      case 2 : return "1";
      case 3 : return "true";
      case 4 : return "true";
      case 5 : return "false";
      case 6 : return "<none>";
      case 7 : return "0.000;-0.000";
      case 8 : return "ALL";
      default : return super.getDefaultValueString(_index-TABLE_ADDED);
    }
  }
} // End of class

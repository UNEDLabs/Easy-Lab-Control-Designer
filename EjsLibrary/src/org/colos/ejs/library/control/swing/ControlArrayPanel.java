/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.StringTokenizer;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.display.DisplayColors;
import org.opensourcephysics.display.EjsArrayPanel;

/**
 * A configurable Label. It has no internal value, nor can trigger
 * any action.
 */
public class ControlArrayPanel extends ControlSwingElement {
  static private final int ARRAY_PANEL_ADDED = 16;
  
  protected EjsArrayPanel panel;
  private boolean isStatic;
  protected ObjectValue dataValue;

// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
    panel = new EjsArrayPanel();
    double[][] emptyData = new double[][]{{0.0,0.0},{0.0,0.0}};
    panel.setArray(emptyData);
    dataValue = new ObjectValue(emptyData);
    panel.setNumericFormat("0.00;-0.00");
    panel.setRowNumberVisible(true);
    panel.setEditable(true);
    panel.setFirstRowIndex(0);
    panel.setFirstColIndex(0);
    panel.setDataForeground(Color.BLACK);
    panel.setDataBackground(Color.WHITE);
    panel.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    isStatic = false;
    panel.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) { 
        if (pce.getPropertyName().equals("cell")) {
          panel.saveLastEdit((TableModelEvent) pce.getNewValue());
          variableChanged (0,dataValue);
          if (isUnderEjs) setFieldListValue(0,dataValue);
        }
      }
    });
    return panel;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("data");
      infoList.add ("action");
      infoList.add ("format");
      infoList.add ("showRowNumber");
      infoList.add ("columnNames");
      infoList.add ("firstRowIndex");
      infoList.add ("firstColIndex");
      infoList.add ("editable");
      infoList.add ("columnsLocked");
      infoList.add ("static");
      infoList.add ("transposed");
      infoList.add ("alignment");
      infoList.add ("columnWidth");
      infoList.add ("dataForeground");
      infoList.add ("dataBackground");
      infoList.add ("autoResizeMode");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("data"))   return "int[]|int[][]|int[][][]|double[]|double[][]|double[][][]|boolean[]|boolean[][]|boolean[][][]|String[]|String[][]|String[][][]|Object";
    if (_property.equals("action")) return "Action CONSTANT";
    if (_property.equals("format")) return "String|String[] TRANSLATABLE";
    if (_property.equals("showRowNumber")) return "boolean";
    if (_property.equals("columnNames")) return "String[]|Object TRANSLATABLE";
    if (_property.equals("firstRowIndex")) return "int";
    if (_property.equals("firstColIndex")) return "int";
    if (_property.equals("editable")) return "boolean";
    if (_property.equals("columnsLocked")) return "boolean[]";
    if (_property.equals("static")) return "boolean";
    if (_property.equals("transposed")) return "boolean";
    if (_property.equals("alignment")) return "Alignment|int|int[]";
    if (_property.equals("columnWidth")) return "int|int[]";
    if (_property.equals("dataForeground")) return "int|Color|Object";
    if (_property.equals("dataBackground")) return "int|Color|Object";
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
    //System.out.println ("Setting "+_index+" to "+_value);
    switch (_index) {
      case 0 : 
        Object arrayObj = _value.getObject();
        if (arrayObj!=null && arrayObj!=panel.getArray()) { //dataValue.value!=panel.getArray()) {
//          System.out.println ("Setting array to "+arrayObj);
//          System.out.println ("panel array = "+panel.getArray());
//          System.out.println ("datavalue array = "+dataValue.value);
          panel.setArray(arrayObj);
          dataValue.value = arrayObj;
          panel.refreshTable();
        }
        else {
          panel.setFirstRowIndex(panel.getFirstRowIndex());
          if (!isStatic) panel.refreshTable();
        }
        break;
      case 1 : // action
        removeAction (ControlElement.VARIABLE_CHANGED,getProperty("action"));
        addAction(ControlElement.VARIABLE_CHANGED,_value.getString());
        break;
      case 2 : 
        if (_value.getObject() instanceof String[]) panel.setNumericFormat((String[]) _value.getObject());
        else {
          String formats = _value.getString();
          if (formats.indexOf(',')<0) panel.setNumericFormat(formats);
          else {
            StringTokenizer tkn = new StringTokenizer(formats,",");
            String[] columnFormats = new String[tkn.countTokens()];
            int c = 0;
            while (tkn.hasMoreTokens()) { 
              columnFormats[c] = org.opensourcephysics.display.TeXParser.parseTeX(tkn.nextToken()); 
              c++; 
            }
            panel.setNumericFormat(columnFormats);
          }
        }
        break;
      case 3 : panel.setRowNumberVisible(_value.getBoolean()); break;
      case 4 : panel.setColumnNames((String[])_value.getObject()); break;
      case 5 : panel.setFirstRowIndex(_value.getInteger()); break;
      case 6 : panel.setFirstColIndex(_value.getInteger()); break;
      case 7 : panel.setEditable(_value.getBoolean()); break;
      case 8 : if (_value.getObject() instanceof boolean[]) {
          boolean[] array = (boolean []) _value.getObject();
          for (int i=0, n=array.length; i<n; i++) panel.setColumnLock(i, array[i]);
        } 
        break;
      case 9 : isStatic = _value.getBoolean(); break;
      case 10: panel.setTransposed(_value.getBoolean()); break;

      case 11 : 
        if (_value.getObject() instanceof int[]) {
          int[] array = (int []) _value.getObject();
          for (int i=0, n=array.length; i<n; i++) panel.setColumnAlignment(i, array[i]);
        } 
        else panel.setColumnAlignment(_value.getInteger());
        if (this.isUnderEjs) panel.repaint();
        break;
      case 12 : 
        if (_value.getObject() instanceof int[]) {
          int[] array = (int []) _value.getObject();
          for (int i=0, n=array.length; i<n; i++) panel.setPreferredColumnWidth(i, array[i]);
        } 
        else panel.setPreferredColumnWidth(_value.getInteger());
        if (this.isUnderEjs) panel.repaint();
        break;

      case 13 :
        if (_value.getObject() instanceof Color) panel.setDataForeground((Color) _value.getObject()); 
        else panel.setDataForeground(DisplayColors.getLineColor(_value.getInteger()));
        break;
      case 14 :
        if (_value.getObject() instanceof Color) panel.setDataBackground((Color) _value.getObject()); 
        else panel.setDataBackground(DisplayColors.getLineColor(_value.getInteger()));
        break;
      case 15 : panel.setAutoResizeMode(_value.getInteger()); break;
      
      default: super.setValue(_index-ARRAY_PANEL_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : panel.setArray(new double[][]{{0.0,0.0},{0.0,0.0}}); panel.refreshTable(); break;
      case 1 : removeAction (ControlElement.VARIABLE_CHANGED,getProperty("action")); break;
      case 2 : panel.setNumericFormat("0.00;-0.00"); break;
      case 3 : panel.setRowNumberVisible(true); break;
      case 4 : panel.setColumnNames((String[])null); break;
      case 5 : panel.setFirstRowIndex(0); break;
      case 6 : panel.setFirstColIndex(0); break;
      case 7 : panel.setEditable(true); break;
      case 8 : for (int i=0, n=panel.getNumColumns(); i<n; i++) panel.setColumnLock(i, true); break;
      case 9 : isStatic = false; break;
      case 10: panel.setTransposed(false); break;
      case 11 : panel.setColumnAlignment(javax.swing.SwingConstants.RIGHT); break;
      case 12 : break;
      case 13 : panel.setDataForeground(Color.BLACK); break;
      case 14 : panel.setDataBackground(Color.WHITE); break;
      case 15 : panel.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); break;
      default: super.setDefaultValue(_index-ARRAY_PANEL_ADDED); break;
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : return dataValue;
      
      case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
      case 8 : case 9 : case 10 : 
      case 11 : case 12 : case 13 : case 14 :
      case 15 :
        return null;
      default: return super.getValue(_index-ARRAY_PANEL_ADDED);
    }
  }
  
  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "{{0.0,0.0},{0.0,0.0}}";
      case 1 : return "<no_action>";
      case 2 : return "0.00;-0.00";
      case 3 : return "true";
      case 4 : return "<none>";
      case 5 : return "0";
      case 6 : return "0";
      case 7 : return "true";
      case 8 : return "true";
      case 9 : return "false";
      case 10 : return "false";

      case 11 : return "RIGHT";
      case 12 : return "<none>";
      case 13 : return "BLACK";
      case 14 : return "WHITE";

      case 15 : return "ALL";
      default : return super.getDefaultValueString(_index-ARRAY_PANEL_ADDED);
    }
  }


} // End of class

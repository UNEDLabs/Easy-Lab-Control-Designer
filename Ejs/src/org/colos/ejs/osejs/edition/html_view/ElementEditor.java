/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.html_view;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.colos.ejs.control.editors.EditorForFile;
import org.colos.ejs.control.editors.EditorForString;
import org.colos.ejs.library.control.value.Value;
import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.osejs.*;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.edition.TabbedEditor;
import org.colos.ejs.osejs.edition.variables.VariablesEditor;
import org.colos.ejs.osejs.edition.translation.TranslatableProperty;
import org.colos.ejs.osejs.edition.translation.TranslationEditor;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.view.TreeOfElements;
import org.colos.ejss.html_view.ElementInfo;
import org.colos.ejss.xml.SimulationXML;
import org.colos.ejss.xml.SimulationXML.PROPERTY;
import org.opensourcephysics.display.*;
import org.w3c.dom.Element;

public class ElementEditor {
  static private final ResourceUtil res    = new ResourceUtil ("Resources");
  static private final ResourceUtil elRes  = new ResourceUtil ("HtmlViewResources");
  static private final ResourceUtil sTipsRes = new ResourceUtil("ElementTips");
  static private final ResourceUtil propertyRes = new ResourceUtil("PropertyResources");

  static private final int LEFT = 3;
  static private final int RIGHT = 5;
  static private final int fieldWidth = res.getInteger("View.Properties.FieldWidth");
  static private Font font = InterfaceUtils.font(null,res.getString("Editor.TitleFont"));
  static private final Insets NULL_INSETS = new Insets(0,0,0,0);
//  static private final Border editorBorder = new EmptyBorder (3,1,1,0);
  static private final Dimension dialogSize = res.getDimension("View.Properties.PreferredSize");
  static private final Color labelColor = InterfaceUtils.color(res.getString("View.Properties.LabelColor"));
  static private final Color moreLinesColor = InterfaceUtils.color(res.getString("View.Properties.MoreLinesColor"));
  static private final Color modelColor = InterfaceUtils.color(res.getString("Model.Color"));
  static private final Icon helpIcon = org.opensourcephysics.tools.ResourceLoader.getIcon("data/icons/help.gif");

  static private final String sHTML_ACTION_PREFIX = "_htmlAction_for_";
  static private final String sHTML_EXPRESSION_PREFIX = "_htmlExpr_";

  private Vector<JComponent> fontList = new Vector<JComponent>();

  private boolean mIsRoot=false;
  private String mName="", mParent;
  private String mElementClassname=null, mJavascriptClassname="";
  private boolean changed=false, editable=true; //, firstTimeEdited=true;
  private ElementInfo mElementInfo=null;
  private Osejs mEjs;
  private JDialog mEditionDialog=null;
  private ElementsTree mTree;
  private java.util.List<JTextComponent> mFieldList = null;
  private java.awt.Font theFont=null;
  private JLabel firstLabel=null;
  private ArrayList<JTextField> fieldsOnly=new ArrayList<JTextField>();
  private EditorForCode editorForCode=null;
  private Map<String, String> mProperties = new HashMap<String, String>();
  private Map<LocaleItem, Map<String, String>> translations = new HashMap<LocaleItem, Map<String, String>>();

//  static private java.lang.reflect.Method sFileEditor=null;
//
//  static {
//    try {
//      sFileEditor = EditorForFile.class.getMethod("edit",Osejs.class, Component.class, String.class);
//      System.err.println("FileEditorMethod = "+sFileEditor);
//    } catch (Exception exc) {
//      exc.printStackTrace();
//    }
//  }
  
  ElementEditor (Osejs ejs, String classname, String name, ElementsTree tree) {
//    System.err.println ("Adding HtmlView element of class "+classname+" called "+name);
    mEjs = ejs;
    mTree = tree;
    mElementClassname = classname; // Ex. Elements.Panel
    if (classname.equals("Tree.Main")) { 
      mName = name; 
      mParent = null; 
      mIsRoot = true;
      mElementInfo = ElementInfo.getInfo("SIMULATION_ROOT");
      if (mElementInfo==null) {
        System.err.println ("Error creating element "+name+" of class "+classname);
      }
    }
    else { // Create the element information
      mJavascriptClassname = elRes.getString(classname); // Ex. org.colos.ejss.html_view.interface_elements.Panel
      mElementInfo = ElementInfo.getInfo(mJavascriptClassname);
      if (mElementInfo==null) {
        System.err.println ("Error creating element "+name+" of class "+mJavascriptClassname);
      }
      setName(name);
    }
  }

  ElementEditor (String classname) {
    mElementClassname = classname; // Ex. Elements.Panel
    if (classname.equals("Tree.Main")) { 
      mParent = null; 
      mIsRoot = true;
    }
    else { // Create the element information
      mJavascriptClassname = elRes.getString(classname); // Ex. org.colos.ejss.html_view.interface_elements.Panel
      mElementInfo = ElementInfo.getInfo(mJavascriptClassname);
      if (mElementInfo==null) {
        System.err.println ("Error creating DUMMY element of class "+mJavascriptClassname);
      }
    }
  }

  // --------------------------------------
  // Translation
  //--------------------------------------
    
  public java.util.List<TranslatableProperty> getTranslatableProperties() {
    java.util.List<TranslatableProperty> list = new ArrayList<TranslatableProperty>();

    if (mEditionDialog==null) createEditionDialog();
    for (JTextComponent field : mFieldList) {
      String propertyName = field.getName(), propValue = field.getText().trim();
      if (propValue.length()>0 && mElementInfo.propertyIsOfType(propertyName,"TRANSLATABLE"))  list.add(new HtmlViewTranslatableProperty(this,propertyName));
    }
    return list;
  }

  String getTranslatedProperty(LocaleItem item, String property) {
//    System.out.println ("Get "+_property+" = in "+ (_locale!=null ? _locale.getDisplayLanguage(): "Default locale"));
    if (item.isDefaultItem()) return TranslationEditor.removeQuotes(mProperties.get(property));
    Map<String,String> translation = translations.get(item);
    if (translation!=null) {
      String translatedProperty = translation.get(property);
      if (translatedProperty!=null) return translatedProperty;
    }
    return TranslationEditor.removeQuotes(mProperties.get(property));
  }

  void setTranslatedProperty(LocaleItem _item, String _property, String _value) {
    if (_item.isDefaultItem()) return; // default values cannot be changed
    Map<String,String> translationMap = translations.get(_item);
    if (translationMap==null) {
      if (_value==null) return; // No need to create it for nothing
      translationMap = new HashMap<String,String>();
      translations.put(_item,translationMap);
    }
    if (_value==null) translationMap.remove(_property);
    else translationMap.put(_property,_value);
  }
  
  // --------------------------------------
  // Search 
  // --------------------------------------

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    if (mEditionDialog==null) createEditionDialog();
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    boolean toLower = (_mode & SearchResult.CASE_INSENSITIVE) !=0;
    if (toLower) _searchString = _searchString.toLowerCase();
    _info = res.getString("View.Properties.Title")+" ";
    for (JTextComponent field : mFieldList) {
      if (field.getText().trim().length()>0)
        list.addAll(searchInComponent(_info,_searchString,toLower,field));
    }
    return list;
  }

  private java.util.List<SearchResult> searchInComponent(String _info, String _searchString, boolean toLower, JTextComponent textComponent)  {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    int lineCounter=1,caretPosition=0;
    StringTokenizer t = new StringTokenizer(textComponent.getText(), "\n");
    while (t.hasMoreTokens()) {
      String line = t.nextToken();
      int index;
      if (toLower) index = line.toLowerCase().indexOf(_searchString);
      else index = line.indexOf(_searchString);
      if (index>=0) list.add(new PropertySearchResult(_info+getName()+"."+textComponent.getName(),//getResourceString(textComponent.getName(),false),
                             line.trim(),textComponent,lineCounter,caretPosition+index));
      caretPosition += line.length()+1;
      lineCounter++;
    }
    return list;
  }

  public void setEditable (boolean _edit) {
    editable = _edit;
    if (mFieldList!=null) for (JTextComponent field : mFieldList) field.setEditable(editable);
  }

  public String toString () { return mName; }

  public String getName () { return mName; }

  public void setName (String name) {
    mName = OsejsCommon.getValidIdentifier(name);
    if (mEditionDialog!=null) {
      String localizedClassname = sTipsRes.getOptionalString(mElementClassname.substring(mElementClassname.indexOf('.')+1)+".Name");
      if (localizedClassname!=null)
        mEditionDialog.setTitle(res.getString("View.Properties.Title") + " "+mName + " ("+localizedClassname+")");
      else mEditionDialog.setTitle(res.getString("View.Properties.Title") + " "+mName);
    }
  }

  public void setTextProperty() {
    String textProperty = mElementInfo.getTextProperty();
    if (textProperty!=null) mProperties.put(textProperty, "\""+mName+"\"");
  }

  public void setCreationDefaults() {
    Map<String,String> defaultsMap = mElementInfo.getCreationDefaults();
    for (String key : defaultsMap.keySet()) {
//      System.out.println(getName()+" : Setting "+key+" to "+defaultsMap.get(key));
      mProperties.put(key, defaultsMap.get(key));
    }
  }
  
  public String getClassname () { return mElementClassname; }
  
  public String getPlainClassname () {      
    return mElementClassname.substring(mElementClassname.indexOf('.')+1);
  }


  public void setParent(ElementEditor parent) {
//    if (!parent.mIsRoot && parent.mElementInfo!=null) mParent = parent.getName();
    if (!parent.mIsRoot) mParent = parent.getName();
    else mParent = null;
  }
  
  public void setFont (java.awt.Font _font)  {
    theFont = _font;
    int height = 20;
    if (firstLabel!=null) {
      FontMetrics fmLabel = firstLabel.getFontMetrics(firstLabel.getFont());
      height = fmLabel.getHeight();
    }
    for (JComponent comp : fontList) {
      comp.setFont(_font);
      FontMetrics fm = comp.getFontMetrics(comp.getFont());
      int finalHeight = Math.min(height,fm.getHeight());
      comp.setPreferredSize(new Dimension(comp.getSize().width,finalHeight));
    }
    if (mEditionDialog!=null) mEditionDialog.pack(); 
  }

  public void clear () {
    if (mEditionDialog!=null) {
      mEditionDialog.dispose();
      mEjs.removeDependentWindows(mEditionDialog);
    }
    mProperties.clear();
  }

  public boolean hasChanged () { return changed; }

  public void setChanged (boolean _changed) { changed = _changed; }

// ------------------ public methods

  public boolean acceptsChild (ElementEditor _child) {
//    if (mElementInfo==null) {
    if (mIsRoot) {
//      System.out.println ("Is a container = "+_child.mElementInfo.instanceOf("EJSS_INTERFACE.container"));
      return _child.mElementInfo.instanceOf("EJSS_INTERFACE.container"); // Root node only accepts container elements
    }
    return mElementInfo.acceptsChild (_child.mElementInfo);
  }

  public void edit () {
    if (mElementInfo==null) return; // root node cannot be edited
    if (mEditionDialog==null) createEditionDialog();
    mEditionDialog.setLocationRelativeTo (mTree.getComponent());
    mEditionDialog.setVisible(true);
  }
  
  private void addIfFileExists(Set<String> _set, String _value) {
//    System.out.println ("Checking file "+_value);
    if (_value.length()<=0) return;
    String noQuotesValue;
    if (_value.startsWith("\"") && _value.endsWith("\"") && VariablesEditor.numberOfQuotes(_value)==2) noQuotesValue = _value.substring(1,_value.length()-1);
    else noQuotesValue = _value;
    String lowercase = noQuotesValue.trim().toLowerCase();
    if (lowercase.startsWith("http:") || lowercase.startsWith("https:")) return;
    if (org.opensourcephysics.tools.ResourceLoader.getResource(noQuotesValue)!=null) {
     _set.add(noQuotesValue);
      return;
    }
//    if (_value.startsWith("%") && _value.endsWith("%")) _value = _value.substring(1,_value.length()-1);
//    String realValue = group.getString(_value);
//    if (org.opensourcephysics.tools.ResourceLoader.getResource(realValue)!=null) {
//      _set.add(realValue);
//      return;
//    }
  }

  public void addResourcesNeeded (Set<String> _set, String _keyword) {
    for (String key : mProperties.keySet()) {
      if (mElementInfo.propertyIsOfType(key,_keyword)) addIfFileExists(_set,mProperties.get(key).trim()); // Add this file
    }
    java.util.List<LocaleItem> desiredTranslations = mEjs.getTranslationEditor().getDesiredTranslations();
    for (Map.Entry<LocaleItem,Map<String,String>> translation : translations.entrySet()) {
      if (!desiredTranslations.contains(translation.getKey())) continue; // save only desired translations
      for (Map.Entry<String,String> entry : translation.getValue().entrySet()) {
        if (mElementInfo.propertyIsOfType(entry.getKey(),_keyword)) addIfFileExists(_set,entry.getValue().trim()); // Add this file
      }
    }   
  }
  
  public StringBuffer saveStringBuffer (String _prefix) {
    StringBuffer txt = new StringBuffer();
    if (_prefix!=null) txt.append(_prefix);
    if (!mIsRoot) {
      txt.append("<Type>"+mElementClassname+"</Type>\n");
      txt.append("<Name><![CDATA["+mName+"]]></Name>\n");
      if (mParent!=null) txt.append("<Parent><![CDATA["+mParent+"]]></Parent>\n");
    }
    for (String key : mProperties.keySet()) {
      String value = mProperties.get(key);
      if (value!=null) txt.append("<Property name=\""+key+"\"><![CDATA["+value+"]]></Property>\n");
    }
    java.util.List<LocaleItem> desiredTranslations = mEjs.getTranslationEditor().getDesiredTranslations();
    for (Map.Entry<LocaleItem,Map<String,String>> translation : translations.entrySet()) {
      if (!desiredTranslations.contains(translation.getKey())) continue; // save only desired translations
      txt.append("<Translation name=\""+translation.getKey().getKeyword()+"\">\n");
      for (Map.Entry<String,String> entry : translation.getValue().entrySet()) {
        txt.append("<PropertyTranslated name=\""+entry.getKey()+"\">");
        txt.append("<![CDATA["+entry.getValue()+"]]>");
        txt.append("</PropertyTranslated>\n");
      }
      txt.append("</Translation>\n");
    }   
    return txt;
  }

  public void readString (String _input) {
    String propertyLine = OsejsCommon.getPiece(_input,"<Property name=","</Property>",true);
    while (propertyLine!=null) {
//      System.err.println ("reading property line : "+propertyLine);
      String property = OsejsCommon.getPiece(propertyLine,"<Property name=\"","\">",false);
      if (!("Name".equals(property) || "Parent".equals(property)) ) {
        String value;
        if (propertyLine.indexOf("![CDATA[")>=0) value = OsejsCommon.getPiece(propertyLine,"<![CDATA[","]]></Property>",false);
        else value = OsejsCommon.getPiece(propertyLine,"\">","</Property>",false);
        //      System.err.println ("Setting property "+property+" to "+value+" for element "+name);
        mProperties.put(property, value);
      }
      _input = _input.substring(_input.indexOf("</Property>")+11);
      propertyLine = OsejsCommon.getPiece(_input,"<Property name=","</Property>",true);
    }

    // Translations
    String translationBlock = OsejsCommon.getPiece(_input,"<Translation name=","</Translation>",true);
    while (translationBlock!=null) {
      // System.out.println (getName()+" reading translation block : "+translationBlock);
      readTranslationBlock(translationBlock);
      _input = _input.substring(_input.indexOf("</Translation>")+14);
      translationBlock = OsejsCommon.getPiece(_input,"<Translation name=","</Translation>",true);
    }
  }

  private void readTranslationBlock(String _block) {
    String language = OsejsCommon.getPiece(_block,"<Translation name=\"","\">",false);
    LocaleItem item = LocaleItem.getLocaleItem(language);
    if (item==null) {
      mEjs.print("Warning! Ignoring unrecognized locale name : "+language+" for view element "+getName()+"\n");
      return;
    }
    String propertyLine = OsejsCommon.getPiece(_block,"<PropertyTranslated name=","</PropertyTranslated>",true);
    while (propertyLine!=null) {
      String property = OsejsCommon.getPiece(propertyLine,"<PropertyTranslated name=\"","\">",false);
      String value = OsejsCommon.getPiece(propertyLine,"<![CDATA[","]]></PropertyTranslated>",false);
      setTranslatedProperty(item, property, value);
      _block = _block.substring(_block.indexOf("</PropertyTranslated>")+21);
      propertyLine = OsejsCommon.getPiece(_block,"<PropertyTranslated name=","</PropertyTranslated>",true);
    }
  }
  
  public void updateProperties (boolean makeVisible) {
//    System.out.println ("Updating properties of "+getName());
    if (mEditionDialog==null) createEditionDialog();
    boolean showErrors = makeVisible || mTree.getEjs().getOptions().showPropertyErrors();
    for (JTextField field : fieldsOnly) {
      String value = field.getText().trim();
      if (value.length()<=0) continue;
      String property = field.getName();
//      System.out.println("Setting property "+property +" to "+value);
      //field.setBackground(Color.WHITE);
      if (setTheProperty (property,value,field)==false) {
        if (showErrors && !mEditionDialog.isVisible()) mEditionDialog.setVisible(true);
      }
      else field.setBackground(Color.WHITE);
    }
  }

  public void generateCode (StringBuffer buffer, int type) {
    if (mIsRoot) return;
    for (String property : mProperties.keySet()) { // first for PREVIOUS properties
      if (mElementInfo.propertyIsOfType(property,"PREVIOUS")) {
        String value = mProperties.get(property);
        if (value!=null) generateOneProperty(buffer, type, property, value.trim());
      }
    } // end of for property
    for (String property : mProperties.keySet()) { // All but the PREVIOUS ones
      if (mElementInfo.propertyIsOfType(property,"PREVIOUS")) continue;
      String value = mProperties.get(property);
      if (value!=null) generateOneProperty(buffer, type, property, value.trim());
    } // end of for property
  }
    
  private void fillOneProperty (SimulationXML simulation, Element view, Element element, String property, String value) {
    value = value.trim();
    if (value.length()<=0) return;
    boolean isViewOnly = simulation.isViewOnly();
    if (mElementInfo.propertyIsOfType(property,"Action")) {
      if (value.startsWith("%") && value.endsWith("%")) {
        String actionName = value.substring(1, value.length()-1);
        if (isViewOnly) {
          simulation.registerViewAction(view, actionName);
          simulation.addViewElementProperty(element, PROPERTY.ACTION, property, "\""+actionName+"\"");
        }
        else simulation.addViewElementProperty(element, PROPERTY.ACTION, property, actionName);
      }
      else {
        if (isViewOnly) {
          simulation.registerViewAction(view, sHTML_ACTION_PREFIX+mName+"_"+property);
          simulation.addViewElementProperty(element, PROPERTY.ACTION, property, "\""+sHTML_ACTION_PREFIX+mName+"_"+property+"\"");
        }
        else {
          String trimmedValue = value.trim();
          String code;
          if (trimmedValue.endsWith(";") || trimmedValue.endsWith("}")) code = SimulationXML.splitCode(value, null, "  "); //"property "+property+" of "+mName, "      ");
          else code = SimulationXML.splitCode(value+";", null, "  ");
          simulation.addViewElementProperty(element, PROPERTY.ACTION, property, "function(_data,_info) {\n"+code+"\n}");
        }
      }
    }
    else if (isVariable(value)) {
//      System.err.println("Is variable: "+value);
      if (value.startsWith("%") && value.endsWith("%")) {
        String varName = value.substring(1, value.length()-1);
        simulation.addViewElementProperty(element, PROPERTY.VARIABLE, property, varName); 
        if (isViewOnly) simulation.registerViewVariable(view, varName, getVariableString(property,varName),false);
      }
      else {
        simulation.addViewElementProperty(element, PROPERTY.VARIABLE, property, value);
        if (isViewOnly) simulation.registerViewVariable(view, value, getVariableString(property,value),false);
      }
    }
    else {
      if (mElementInfo.propertyIsOfType(property,"MULTILINE")) { // put all lines in one
        StringBuffer multilineBuffer = new StringBuffer();
        StringTokenizer tkn = new StringTokenizer(value, "\n");
        while (tkn.hasMoreTokens()) {
          String line = tkn.nextToken();
          multilineBuffer.append(line);
          multilineBuffer.append(" ");
        }
        value = multilineBuffer.toString().trim();
        if (mElementInfo.propertyIsOfType(property,"String")) {
          if (value.startsWith("\"")) value = value.substring(1);
          if (value.endsWith("\"")) value = value.substring(value.length()-1);
          value = FileUtils.replaceString(value, "\\", "\\\\");            
          value = "\""+ FileUtils.replaceString(value, "\"", "\\\"") + "\"";            
        }
      }
      if (VariablesEditor.isAConstant(mElementInfo,property,value)) {
//        System.err.println("Is constant: "+value);
        //if (mElementInfo.propertyIsOfType(property,"BASE64_IMAGE")) value = "_getBase64Image("+value+")"; // Some images bust be converted to Base64
        simulation.addViewElementProperty(element, PROPERTY.CONSTANT, property, value);
      }
      else {
//        System.err.println("Is expression: "+value);
        if (isViewOnly) {
          simulation.registerViewVariable(view, sHTML_EXPRESSION_PREFIX+mName+"_"+property,getVariableString(property,value),true);
          simulation.addViewElementProperty(element, PROPERTY.EXPRESSION, property, "\""+sHTML_EXPRESSION_PREFIX+mName+"_"+property+"\""); // Must be an expression
        }
        else simulation.addViewElementProperty(element, PROPERTY.EXPRESSION, property, value); // Must be an expression
      }
    }
  }

//===========    JSV for Parse and rewrite    ================================================
  public String parseViewCall(String propValue){
    StringBuffer txt = new StringBuffer();
    //txt.append("  public void _method_for_"+name+"_"+propertyName+" () {\n");
    MethodParser translator = new MethodParser();
    //System.out.println(System.getProperty("java.class.path"));
    String out = "";
    try {
      //System.out.println(System.getProperty("java.class.path"));
      out = translator.parseTxt(propValue);
    } catch (IOException e) {
      out = "System.err.println(\"ERROR in parseViewCall\");";
      e.printStackTrace();
    }
    txt.append(out);
    //txt.append("  }\n");
    return txt.toString();
  }
//____________________________________________________________________
  
  public void generateOneProperty (StringBuffer buffer, int type, String property, String value) {
    value = value.trim();
    if (value.length()<=0) return;
    if (type==Editor.GENERATE_VIEW_EXPRESSIONS) {
//      System.err.print("Considering expression "+value+ "...");
      if (mElementInfo.propertyIsOfType(property,"Action")) { 
        // DO nothing, it is an action  
//        System.err.println("is Action");
      } 
      else if (isVariable(value)) { 
        // Do nothing, it is a global variable
//        System.err.println("is variable");
      } 
      else {
        if (VariablesEditor.isAConstant(mElementInfo,property,value)) { 
          // Do nothing, it is a constant value
//          System.err.println("is constant");
        } 
        else {
//          System.err.println("is Expression");
          buffer.append("   dataMap.put(\""+sHTML_EXPRESSION_PREFIX+mName+"_"+property+"\", "+value+");\n");
        }
      }
    }
    else if (type==Editor.GENERATE_VIEW_LISTENERS) {
      if (mElementInfo.propertyIsOfType(property,"Action")) { // Only generate action expressions
        if (value.startsWith("%") && value.endsWith("%")) { } // Do nothing, it is a global action
        else {
          buffer.append("  public void "+sHTML_ACTION_PREFIX+mName+"_"+property+"(Object _argument) {\n");
          if (!value.endsWith(";")) value += ";";
          //JSV parsing _view Calls
          if (value.contains("_view.")) value = parseViewCall(value);
          StringTokenizer t = new StringTokenizer(value, "\n");
          while (t.hasMoreTokens()) buffer.append("    "+t.nextToken()+"\n");
          buffer.append("  }\n\n");
        }
      }
    }
  }
  
  private String getVariableString (String property, String expression) {
    Object propObject = mEjs.getModelEditor().getVariablesEditor().evaluateExpression(expression);
//    System.err.println ("Checking expression : "+expression);
    if (propObject==null) return null;
//    System.err.println (propObject+ " : Type of Object "+expression+" is "+propObject.getClass());
    String returnType=null;// find type of property
//    Class<?> aClass = propObject.getClass();
//    if (!aClass.isArray()) {
//      returnType = aClass.getName();
//      if (returnType.startsWith("java.lang.")) returnType = null; // There may be conflicts between Integer and Double
//    }
//    if (returnType==null) { // Do it 'by hand'  Order is important: double must go before int, and boolean before int
    if      (mElementInfo.propertyIsOfType(property,"double"))  returnType = "double";
    //  else if (mElementInfo.propertyIsOfType(property,"double[]"))  returnType = "double"; // for double[] arrays only - such as Position[]
    else if (mElementInfo.propertyIsOfType(property,"String"))  returnType = "String";
    else if (mElementInfo.propertyIsOfType(property,"Color"))   returnType = "Color";
    else if (mElementInfo.propertyIsOfType(property,"boolean")) returnType = "boolean";
    else if (mElementInfo.propertyIsOfType(property,"int"))     returnType = "int";
    else returnType = "Object";
//    }
    // Try to see if it is a double array
    String doubleArrayType = returnType+"[][]";
    if (mElementInfo.propertyIsOfType(property,doubleArrayType)) {
      if (returnType.equals("int") && (propObject instanceof int[][])) {
        int[][] value = (int[][]) propObject;
        if (value.length<0 || value.length*value[0].length>1000) return null; // Do not pass too large arrays
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i=0; i<value.length; i++) {
          if (i>0) buffer.append("],");
          buffer.append("[");
          int[] iValue = value[i];
          for (int j=0; j<iValue.length; j++) {
            if (j>0) buffer.append(",");
            buffer.append(iValue[j]);
          }
          buffer.append("]");
        }
        buffer.append("]");
        return buffer.toString();
      }
      if (returnType.equals("double") && (propObject instanceof double[][])) {
        double[][] value = (double[][]) propObject;
        if (value.length<0 || value.length*value[0].length>1000) return null; // Do not pass too large arrays
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i=0; i<value.length; i++) {
          if (i>0) buffer.append(",");
          buffer.append("[");
          double[] iValue = value[i];
          for (int j=0; j<iValue.length; j++) {
            if (j>0) buffer.append(",");
            buffer.append(iValue[j]);
          }
          buffer.append("]");
        }
        buffer.append("]");
        return buffer.toString();
      }
      return null;
    }
    // Try to see if it is an array
    String arrayType = returnType+"[]";
//    System.err.println (propObject+ " : return type "+returnType);

    if (mElementInfo.propertyIsOfType(property,arrayType)) {
//      System.err.println (propObject+ " : arrayType "+arrayType);
//      System.err.println (propObject+ " : is double[]"+(propObject instanceof double[]));
      if (returnType.equals("int") && (propObject instanceof int[])) {
        int [] value = (int[]) propObject;
        if (value.length>1000) return null; // Do not pass too large arrays
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i=0; i<value.length; i++) {
          if (i>0) buffer.append(",");
          buffer.append(toJavascriptValue(value[i],returnType));
        }
        buffer.append("]");
        return buffer.toString();
      }
      if (returnType.equals("double") && (propObject instanceof double[])) {
//        System.err.println (propObject+ " : is double array");

        double [] value = (double[]) propObject;
        if (value.length>1000) return null; // Do not pass too large arrays
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (int i=0; i<value.length; i++) {
          if (i>0) buffer.append(",");
          buffer.append(toJavascriptValue(value[i],returnType));
        }
        buffer.append("]");
        return buffer.toString();
      }
      return null;
    }
    return toJavascriptValue (propObject, returnType);
  }

  private String toJavascriptValue(Object value, String type) {
//    System.err.println (value+ " : to javascript of type "+type);

    if (type.equals("String")) return "\""+value.toString()+"\"";
    if (type.equals("Color")) {
      if (value instanceof Color) {
        Color color = (Color) value;
        return "\"rgba("+color.getRed()+","+color.getGreen()+","+color.getBlue()+","+color.getAlpha()+")\"";
      }
      return null;
    }
    return value.toString();
  }

  
  void fillSimulationXML(SimulationXML simulation, Element view) {
    Element element;
    if (mIsRoot) element = simulation.addRootViewElement(view);
    else element = simulation.addViewElement(view,mElementInfo.getConstructorName(), mName, (mParent==null) ? "_topFrame" : mParent);
    for (String property : mProperties.keySet()) { // first for PREVIOUS properties
      if (mElementInfo.propertyIsOfType(property,"PREVIOUS")) {
        String value = mProperties.get(property);
        if (value!=null) fillOneProperty(simulation, view, element, property, value.trim());
      }
    } // end of for property
    for (String property : mProperties.keySet()) { // All but the PREVIOUS ones
      if (mElementInfo.propertyIsOfType(property,"PREVIOUS")) continue;
      String value = mProperties.get(property);
      if (value!=null) this.fillOneProperty(simulation, view, element, property, value.trim());
    } // end of for property
    
    java.util.List<LocaleItem> desiredTranslations = mEjs.getTranslationEditor().getDesiredTranslations();
    for (Map.Entry<LocaleItem,Map<String,String>> translation : translations.entrySet()) {
      if (!desiredTranslations.contains(translation.getKey())) continue; // save only desired translations
      String locale = translation.getKey().getKeyword();
//      System.err.println ("ElementEditor.fillSimulationXML : Adding locale "+locale+ "for element "+mName);
      for (Map.Entry<String,String> entry : translation.getValue().entrySet()) {
        Element propertyElement = simulation.getViewElementProperty(element,entry.getKey());
//        System.err.println ("Localization of "+BasicElement.evaluateNode(element,"name")+ " for property <"+entry.getKey()+"> = "+propertyElement+" to : "+entry.getValue());
        String localizedValue = entry.getValue();
        if (!localizedValue.startsWith("[")) { // It is not an array
          if (!localizedValue.startsWith("\"")) localizedValue = "\"" + localizedValue;
          if (!localizedValue.endsWith("\""))   localizedValue += "\"";
        }
        simulation.addViewElementLocalizedProperty(propertyElement, locale, localizedValue);
      }
    } // end of for translation
  }
  
  /**
   * Whether a given property is linked to a variable
   */
  private boolean isVariable (String value) {
    if (value.startsWith("%") && value.endsWith("%")) return true; // It should be a variable
    return mEjs.getModelEditor().isVariableDefinedOfType(value, null); // any type
  }

  /**
   * Sets the value of the element property and checks for correct syntax
   * @return true if the value was correct, false if there was an error
   */
  private boolean setTheProperty (String property, String value, JTextComponent field) {
    //System.err.println ("Element "+mName+" setting "+property+ " to "+value);
    if (value==null) {
      mProperties.remove(property);
      return true;
    }
    if (mElementInfo.propertyIsOfType(property,"Action")) { // No need to check the syntax for actions (field==null) or to reset to default
      mProperties.put(property, value);
      return true; 
    }
    // A property can be of several types - such an an angle, which can be either int or double, with several meaning (degrees or radians)
    ArrayList<String> possibleTypes = new ArrayList<String>();
    String info = mElementInfo.getPropertyInfo(property); 
    boolean tryObjectToo=false; // Allowing Objects before any other type will always return an ObjectValue
    if (info!=null && info.indexOf('[')>=0) tryObjectToo=true; // possibleTypes.add("Object");
    if (mElementInfo.propertyIsOfType(property,"int"))     possibleTypes.add("int"); // Order is important since an integer is also accepted as a double
    if (mElementInfo.propertyIsOfType(property,"double"))  possibleTypes.add("double");
    if (mElementInfo.propertyIsOfType(property,"boolean")) possibleTypes.add("boolean");
    if (mElementInfo.propertyIsOfType(property,"String"))  possibleTypes.add("String");
    if (mElementInfo.propertyIsOfType(property,"Object"))  tryObjectToo=true; // possibleTypes.add("Object");
    if (possibleTypes.size()<=0) tryObjectToo = true; // possibleTypes.add("Object");

    // Check for a variable of the form %varName%
    String trimmedValue = value.trim();
    if (trimmedValue.startsWith("%") && trimmedValue.endsWith("%")) {
      mProperties.put(property, trimmedValue);
      String variable = trimmedValue.substring(1,trimmedValue.length()-1);
      for (String type : possibleTypes) if (mEjs.getModelEditor().getVariablesEditor().isVariableDefined(variable,type)) return true;
      if (tryObjectToo && mEjs.getModelEditor().getVariablesEditor().isVariableDefined(variable,"Object")) return true; 
//      System.err.println ("Error 1");
      field.setBackground(TabbedEditor.ERROR_COLOR);
      return false;
    }
    
    // Check for a special value of the property. This is before normal variables to match the sequence in ControlElement
    String trValue = value;
    if (value.startsWith("\"") && value.endsWith("\"") && VariablesEditor.numberOfQuotes(value)==2) trValue = value.substring(1,value.length()-1);
    if (mElementInfo.acceptsConstant(property,trValue)) {
      mProperties.put(property, value);
      return true;
    }
    
    // Check for a variable already defined
    for (Iterator<String> it=possibleTypes.iterator(); it.hasNext(); ) { // an iterator keeps the order
      if (mEjs.getModelEditor().getVariablesEditor().isVariableDefined(trimmedValue,it.next())) {
        mProperties.put(property, trimmedValue);
        return true; 
      }
    }
    if (tryObjectToo && mEjs.getModelEditor().getVariablesEditor().isVariableDefined(trimmedValue,"Object")) {
      mProperties.put(property, trimmedValue);
      return true;      
    }
  
    // Check for an expression
    Value valueObj=null;
    for (Iterator<String> it=possibleTypes.iterator(); it.hasNext(); ) { // an iterator keeps the order
      valueObj = mEjs.getModelEditor().getVariablesEditor().checkExpression(value, it.next());
      if (valueObj!=null) break;
    }
    if (valueObj==null && tryObjectToo) valueObj = mEjs.getModelEditor().getVariablesEditor().checkExpression(value, "Object");
    
    mProperties.put(property, value);
    if (valueObj==null) { // Now check for a String
      if (mElementInfo.propertyIsOfType(property,"String")) {
        if (VariablesEditor.numberOfQuotes(value) % 2 !=0) {
//          System.err.println ("Error 2");
          field.setBackground(TabbedEditor.ERROR_COLOR);
          return false;
        }
        return true;
      }
    }
    if (valueObj!=null) {
      return true;
    }
//    System.err.println ("Error 3");
//    System.err.println ("Field = "+field);
    field.setBackground(TabbedEditor.ERROR_COLOR);
    return false;
  }

  // ---------------------------------------

  private PropertyLine createOnePropertyLine (String property) {
    PropertyLine line = new PropertyLine();
    String txt = property; //getResourceString(property,false);
    line.label = new JLabel (txt,SwingConstants.RIGHT);
//    if (width>0) line.label.setPreferredSize(new Dimension(width,line.label.getHeight()));
    line.label.setFont(font);
    line.label.setBorder(new javax.swing.border.EmptyBorder(0,LEFT,0,RIGHT));
    MouseListener listener = new PropertyLabelMouseListener (txt);
    line.label.addMouseListener(listener);
    txt = getToolTip(property); // getResourceString(property,true);
    line.label.setToolTipText(txt.trim());

    if (firstLabel==null) firstLabel = line.label;
    // fontList.add (line.label); // For future customization

    java.awt.event.MouseListener mouseListener=null;

    if (mElementInfo.propertyIsOfType(property,"Action") || mElementInfo.propertyIsOfType(property,"MULTILINE")) {
      if (mElementInfo.propertyIsOfType(property,"Action")) line.label.setForeground(modelColor);
      JTextArea lineArea = new JTextArea ();
      lineArea.setRows(1);
      if (fieldWidth>0) lineArea.setColumns (fieldWidth);
      else lineArea.setColumns (10);
      mouseListener = new MyMouseListener(lineArea);
      lineArea.getDocument().addDocumentListener (new MyDocumentListener(property,lineArea));
      JScrollPane scrollPane = new JScrollPane(lineArea);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      line.field = lineArea;
      line.superComponent = scrollPane;
    }
    else {
      JTextField lineField = new JTextField ();
      if (fieldWidth>0) lineField.setColumns (fieldWidth);
      else lineField.setColumns (10);
      lineField.setActionCommand (property);
      mouseListener = new MyMouseListener(lineField);
      lineField.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent _evt) {
          String property=_evt.getActionCommand();
//          System.out.println ("Calling field event for "+property+" at "+name);
          JTextField field = (JTextField) _evt.getSource();
          doTheChange(field,checkString (field, property),property);
        }
      });
      lineField.addKeyListener (new MyKeyListener(property));
      lineField.addFocusListener (new MyFocusListener(property));
      line.field = lineField;
      fieldsOnly.add(lineField);
    }
    line.field.addMouseListener(listener);
    line.field.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed (java.awt.event.KeyEvent _e) {
        if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_F1) {
          //mTree.getEjs().openWikiPage("HtmlView"+mElementClassname);
          mTree.showLocalHelp(mElementClassname);
        }
      }
    });
    txt = mProperties.get(property);
//    System.out.println ("Getting property "+_property +" = "+txt);
    if (txt!=null) {
      line.field.setText (txt);
      line.field.setCaretPosition(0);
    }
//      else line.field.setText ("<default>");
    line.field.setEditable(editable);
    line.field.setMargin (NULL_INSETS);
    line.field.setName (property);
    mFieldList.add(line.field);
    line.field.setFont(theFont);
    fontList.add (line.field); // For future customization

    line.buttonConstant = new JButton (TreeOfElements.editIcon);
    line.buttonConstant.setMargin(NULL_INSETS);
    //line.buttonConstant.setBorder(BUTTONS_BORDER);
    line.buttonConstant.setName("CONSTANT:"+property);
    line.buttonConstant.addMouseListener (mouseListener);
    line.buttonConstant.setFocusable(false);

    if (mElementInfo.propertyIsOfType(property,"CONSTANT")) {
      line.buttonVariable = new JButton(TreeOfElements.unlinkIcon);
      line.buttonVariable.setEnabled(false);
    }
    else {
      line.buttonVariable = new JButton(TreeOfElements.linkIcon);
      line.buttonVariable.addMouseListener (mouseListener);
    }
    line.buttonVariable.setMargin(NULL_INSETS);
    //line.buttonVariable.setBorder(BUTTONS_BORDER);
    line.buttonVariable.setName("VARIABLE:"+property);
    line.buttonVariable.setFocusable(false);

    if (mElementInfo.propertyIsOfType(property,"Action") || mElementInfo.propertyIsOfType(property,"MULTILINE")) {
      line.buttonVariable.setIcon(TreeOfElements.actionIcon);
      line.buttonConstant.setName ("ACTION:"+property);
    }
    else if (getEditionInfo(property)==null) {
      line.buttonConstant.setIcon(TreeOfElements.writeIcon);
    }
    if (!editable) {
      line.buttonConstant.setEnabled(false);
      line.buttonVariable.setEnabled(false);
    }
    line.panel = new JPanel (new BorderLayout());
    line.panel.add (line.label ,BorderLayout.WEST);
    if (line.superComponent!=null) line.panel.add (line.superComponent ,BorderLayout.CENTER);
    else line.panel.add (line.field ,BorderLayout.CENTER);
    JPanel buttonsPanel = new JPanel (new GridLayout(1,2));
    buttonsPanel.add (line.buttonConstant);
    buttonsPanel.add (line.buttonVariable);
    line.panel.add (buttonsPanel,BorderLayout.EAST);

    return line;
  }

  private void createEditionDialog () {
    mFieldList = new ArrayList<JTextComponent>();
    JTabbedPane tabbedPanel = new JTabbedPane();
    Set<JComponent> labelSet = new java.util.HashSet<JComponent>();
    
    int maxColumns=0;
    JPanel finalPanel = new JPanel (new GridLayout(1,0));
    String title = res.getString("HtmlView.MainProperties");
    boolean isFirstColumn = true, isNewColumn = false;

    String orderedProperties = mElementInfo.getEditionInfo();
    if (orderedProperties==null) {
      System.err.println("Can't find resources for element of class "+this.mElementClassname);
    }
    else {
      orderedProperties = orderedProperties.trim();
      if (!orderedProperties.startsWith("<COLUMN>;")) orderedProperties = "<COLUMN>;" + orderedProperties; // Make sure a new column is created
      if (!orderedProperties.endsWith(";")) orderedProperties = orderedProperties + ";"; // Make sure it ends with a semicolon
      Box columnPanel=null;
      int numColumns = 0;
      StringTokenizer tkn = new StringTokenizer(orderedProperties,";");
      while (tkn.hasMoreTokens()) {
        String property = tkn.nextToken().trim();
        if (property.equals("<TAB>")) {
          // fill this tab
          InterfaceUtils.makeSameDimension(labelSet);
          tabbedPanel.add(title,new JScrollPane(finalPanel)); // use current title
          // prepare next tab
          labelSet.clear();
          numColumns = 0;
          finalPanel = new JPanel (new GridLayout(1,0));
          title = propertyRes.getString(tkn.nextToken().trim()); // for next time
          isFirstColumn = true;
          isNewColumn = false;
        }
        else if (property.equals("<COLUMN>")) {
          InterfaceUtils.makeSameDimension(labelSet);
          labelSet.clear();
          numColumns++;
          maxColumns = Math.max(maxColumns, numColumns);
          columnPanel = Box.createVerticalBox();
          JPanel bigPanel = new JPanel (new BorderLayout());
          bigPanel.add (columnPanel,BorderLayout.NORTH);
          finalPanel.add(bigPanel);
          isNewColumn = true;
        }
        else if (property.startsWith("<LABEL")) {
          String label = propertyRes.getString(property.substring(1,property.length()-1)); //getResourceString(property.substring(1,property.length()-1),false);
          JLabel jlabel = new JLabel(label, SwingConstants.CENTER);
          jlabel.setFont(jlabel.getFont().deriveFont(Font.BOLD));
          jlabel.setForeground(labelColor);
          JPanel labelPanel = new JPanel(new BorderLayout());
          labelPanel.add(jlabel, BorderLayout.CENTER);
          if (isFirstColumn) {
            JLabel helpLabel = new JLabel(helpIcon);
            helpLabel.setToolTipText(res.getString("Help.F1ForHelp"));
            helpLabel.addMouseListener(new java.awt.event.MouseAdapter() {
              public void mousePressed(java.awt.event.MouseEvent evt) { 
//                mTree.getEjs().openWikiPage("HtmlView"+mElementClassname); 
                mTree.showLocalHelp(mElementClassname);
              }
            });
            labelPanel.add(helpLabel,BorderLayout.WEST);
            isFirstColumn = false;
          }
          else if (isNewColumn) {
            jlabel.setPreferredSize(new Dimension(10,helpIcon.getIconHeight()));
          }
          isNewColumn = false;
          if (columnPanel==null) System.err.println ("null column for property="+property);
          else {
            columnPanel.add(labelPanel);
            columnPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
          }
        }
        else if (property.startsWith("<SEP>")) {
          if (columnPanel==null) System.err.println ("null column for property="+property);
          else columnPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        }
//        else if (property.startsWith("<EDITOR")) {
//          if (mHtmlViewElement instanceof HasEditor) {
//            final String editor = property.substring(7,property.length()-1);
//            String label = propertyRes.getString("LABEL"+editor); //getResourceString("LABEL"+editor,false);
//            JButton button = new JButton (label);
//            button.setToolTipText(propertyRes.getString("LABEL"+editor+".ToolTip")); //getResourceString("LABEL"+editor+".ToolTip",false)); // Do NOT use true
//            button.setFont(button.getFont().deriveFont(Font.BOLD));
//            button.setForeground(labelColor);
//            button.setMargin(NULL_INSETS);
//            button.addActionListener(new ActionListener(){
//              public void actionPerformed(ActionEvent e) { ((HasEditor) mHtmlViewElement).showEditor(editor); }
//            });
//            JPanel buttonPanel = new JPanel(new BorderLayout());
//            buttonPanel.setBorder(editorBorder);
//            buttonPanel.add(button, BorderLayout.CENTER);
//            if (columnPanel==null) System.err.println ("null column for property="+property);
//            else columnPanel.add(buttonPanel);
//          }
//        }
        else if (mElementInfo.hasProperty(property)) {
          PropertyLine line = createOnePropertyLine (property);
          if (line!=null) { // Add one line with all the components
            if (columnPanel==null) System.err.println ("null column for property="+property);
            else columnPanel.add(line.panel);
            labelSet.add(line.label);
          }
        }
      }
    }

    // fill this tab
    InterfaceUtils.makeSameDimension(labelSet);
    tabbedPanel.add(title,new JScrollPane(finalPanel)); // use current title
    
    mEditionDialog = new JDialog (mEjs.getMainFrame());

    String localizedClassname = sTipsRes.getOptionalString(mElementClassname.substring(mElementClassname.indexOf('.')+1)+".Name");
    if (localizedClassname!=null)
      mEditionDialog.setTitle(res.getString("View.Properties.Title") + " "+mName + " ("+localizedClassname+")");
    else mEditionDialog.setTitle(res.getString("View.Properties.Title") + " "+mName);
    if (dialogSize.width>0) mEditionDialog.setSize ((maxColumns*dialogSize.width)/2,dialogSize.height);

    mEditionDialog.getContentPane().setLayout (new BorderLayout());
    mEditionDialog.getContentPane().add (tabbedPanel,BorderLayout.CENTER);

    mEditionDialog.validate();
    mEditionDialog.pack();
    mEditionDialog.setLocationRelativeTo (mTree.getComponent());
    mEditionDialog.setVisible(false);
//    mEditionDialog.setModal  (false);
    mEjs.addDependentWindows(mEditionDialog);
//    mEjs.setMenuBar(mEditionDialog);
  }
  
  private String editLink(String _property, JTextComponent _field){
    if (mElementInfo.propertyIsOfType(_property,"Action")) {
      String action = EditorForVariables.edit (mEjs.getModelEditor(), "Actions", null, _field, _field.getText().trim(),null);
      if (action!=null) {
        int index = action.indexOf('(');
        if (index>=0) action = "%"+action.substring(0, index)+"%";
      }
      return action;
    }
    String txt = EditorForVariables.edit (mEjs.getModelEditor(), "VariablesAndCustomMethods", mElementInfo.getPropertyInfo(_property), _field, _field.getText().trim(),null);
    if (mElementInfo.propertyIsOfType(_property,"String")) {
      if (txt==null); // why I ever added this? -> txt = "";
      else txt = "%"+txt+"%";
    }
    return txt;
  }

  private String editCode(String _property, JTextComponent _field, int caret){
//    String txt = getResourceString(_property,false);
    if (editorForCode==null) {
      editorForCode = new EditorForCode(mEjs);
      editorForCode.setFont(theFont);
    }
    return editorForCode.edit (mName,_property, _field, caret);
  }

  String getToolTip(String property) {
//    System.err.println ("Tooltip for property "+property +" of element "+mName);
    String type = mElementInfo.getPropertyInfo(property);
    StringTokenizer tkn = new StringTokenizer(type," |");
    String tip="";
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (!token.toUpperCase().equals(token)) tip += ", "+token;
    }
    if (tip.startsWith(", ")) tip = tip.substring(2);
    return tip;
  }


  static private class EditionInfo {
    Class<?> mEditorClass;
    java.lang.reflect.Method mEditionMethod;
    EditionInfo (Class<?> editorClass, java.lang.reflect.Method editionMethod) {
      mEditorClass = editorClass;
      mEditionMethod = editionMethod;
    }
  }
  
  private EditionInfo getEditionInfo(String property) {
    String type = mElementInfo.getPropertyInfo(property);
    //  System.out.println ("Type of "+_property +" is "+type);
    if (type==null) type = "double";
    else {
      StringTokenizer tkn = new StringTokenizer(type," "); // Remove other attributes, like TRANSLATABLE, f.i.
      if (tkn.countTokens()>=1) type = tkn.nextToken();
    }
    StringTokenizer tkn = new StringTokenizer(type,"|");
    while (tkn.hasMoreTokens()) {
      String editorName = "Editors."+tkn.nextToken();
      String ownEditor = elRes.getOptionalString(editorName);
      if (ownEditor!=null) { 
        if (ownEditor.equals("org.colos.ejs.control.editors.EditorForFile")) return new EditionInfo(EditorForFile.class,null);
        try {
          if (ownEditor.indexOf('.')<0) ownEditor = "org.colos.ejs.control.editors."+ownEditor;
//          System.err.println ("Trying editor for "+property +"("+type+") = "+ownEditor);
          Class<?> editorClass = Class.forName(ownEditor);
          java.lang.reflect.Method editMethod;
          Class<?>[] classList = new Class[2];
          classList[0] = java.util.List.class;
          classList[1] = JTextField.class;
          editMethod = editorClass.getMethod("edit",classList);
          return new EditionInfo(editorClass,editMethod);
        }
        catch (Exception exc) {
          System.err.println ("Error finding editor for "+property +"("+type+") = "+ownEditor);
          exc.printStackTrace();
          return null; 
        }
      }
    }
    return null;
  }

  private String constantsEdition(String property, JTextField field) {
    EditionInfo editionInfo = getEditionInfo(property);
    if (editionInfo==null) {
      // Use a simple editor to input a String
      return EditorForString.edit(property,field); //this.getResourceString(_property,false),_field);
    }
    if (editionInfo.mEditorClass==EditorForFile.class) return EditorForFile.edit(mEjs,field,property);
    try {
      java.util.List<TwoStrings> list = mElementInfo.getPropertyAcceptedConstants(property);
      return (String) editionInfo.mEditionMethod.invoke (editionInfo.mEditorClass,list,field);
    }
    catch (Exception _e) {
      System.err.println ("Element "+mElementInfo.getConstructorName()+" can't really edit property "+property);
      _e.printStackTrace();
      return null;
    }
  }

// -------------------------------------
// Inner classes
// -------------------------------------

  class MyDocumentListener implements DocumentListener {
    private String property;
    private JTextArea area;

    MyDocumentListener (String _property, JTextArea _area) { property = _property; area = _area; }

    public void changedUpdate (DocumentEvent evt)  { updateIt (evt); }
    public void insertUpdate  (DocumentEvent evt)  { updateIt (evt); }
    public void removeUpdate  (DocumentEvent evt)  { updateIt (evt); }

    private void updateIt (DocumentEvent evt) {
      String value = area.getText();
      changed = true;
      if (area.getLineCount()>1) area.setBackground(moreLinesColor);
      else area.setBackground(Color.white);
//      System.out.println ("Setting value for property "+property+" to "+value);
      if (value.trim().length()<=0) value=null; // Set the default value
      setTheProperty (property,value,area);
      //element.reset(); // This is commented out because it caused a lengthy delay under Windows
      //group.update();  // This is commented out because it caused a lengthy delay under Windows
    }
  }

    class MyMouseListener extends java.awt.event.MouseAdapter {
      private JTextField field;
      private JTextArea area=null;

      MyMouseListener (JTextField _field) { field = _field; }
      MyMouseListener (JTextArea _area) { area = _area; }

      public void mouseClicked (java.awt.event.MouseEvent _evt) {
        Component button = _evt.getComponent();
        String property=button.getName();
        String value = null;
//        System.out.println ("Calling field event for property '"+property+"' of "+mName);
        if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
          if (property.startsWith("CONSTANT:")) {
            property = property.substring(9);
            value = org.colos.ejs.control.editors.EditorForString.edit(property,field); //getResourceString(property,false),field);
            if (value==null) return; // Edition was canceled
            if ("<default>".equals(value)) value = "";
            field.setText(value);
            value = checkString (field, property);
          }
          else return;
        }
        else { // Normal click
//          System.out.println ("Edit "+property+" for "+mName);
          if (property.startsWith("CONSTANT:")) {
            property = property.substring(9);
            value = constantsEdition(property,field);
            if (value==null) return; // Edition was canceled
            if ("<default>".equals(value)) value = "";
//            else {
//              if (!value.startsWith("\"")) value = "\""+ value;
//              if (!value.endsWith("\""))   value += "\"";
//            }
            field.setText(value);
            value = checkString (field, property);
          }
          else if (property.startsWith("VARIABLE:")) {
            property = property.substring(9);
            if (area!=null) value = editLink (property, area);
            else value = editLink (property, field);
          }
          else if (property.startsWith("ACTION:")) {
            property = property.substring(7);
            if (area!=null) value = editCode (property, area,-1);
            else value = editCode (property, field,-1);
          }
          else value = constantsEdition(property,field);
          if (value==null) return; // Edition was canceled
          if ("<default>".equals(value)) value = "";
          if (area!=null) { area.setText(value); area.setCaretPosition(0); }
          else { field.setText(value); field.setCaretPosition(0); }
        }
        doTheChange(field!=null ? field : area,value,property);
      }
    }

    private void doTheChange (JTextComponent _field, String _value, String _property) { // Now do the change
    changed = true;
//    System.out.println ("Setting value for property "+_property+" to "+_value);
    if (_field!=null) _field.setBackground(Color.white);
    if (_value.trim().length()<=0) _value=null; // Set the default value
    if (setTheProperty (_property,_value,_field)) mTree.refreshEmulator();
  }

  private class MyKeyListener implements java.awt.event.KeyListener {
    private String property;
    MyKeyListener (String _property) { property = _property; }
    public void keyPressed  (java.awt.event.KeyEvent _e) {
//      System.out.println ("Key pressed "+_e.getKeyChar());
      if (_e.getKeyCode()==27)   {
        String value = mProperties.get(property);
        if (value==null) value="";
        ((JTextField)_e.getComponent()).setText(value);
        _e.getComponent().setBackground (Color.white);
      }
      else {
        _e.getComponent().setBackground (Color.yellow);
      }
      
    }
    public void keyReleased (java.awt.event.KeyEvent _e) { }
    public void keyTyped    (java.awt.event.KeyEvent _e) {
      if (_e.getKeyChar()!='\n') _e.getComponent().setBackground (Color.yellow);
      else if (_e.getKeyCode()==27) _e.getComponent().setBackground (Color.white);
    }
  }

  private String checkString (JTextField _field, String _property) {
    String valueStr = _field.getText();
    if (mElementInfo.propertyIsOfType(_property,"String")) {
      if (valueStr.trim().length()<=0 || (valueStr.startsWith("%") && valueStr.endsWith("%"))) return valueStr;  // Do nothing
      // See if a double is acceptable and it is a double
      if (mElementInfo.propertyIsOfType(_property,"double")) {
        if (mEjs.getModelEditor().getVariablesEditor().checkExpression(valueStr,"double")!=null) return valueStr;
      }
      if (mElementInfo.propertyIsOfType(_property,"int")) {
        if (mEjs.getModelEditor().getVariablesEditor().checkExpression(valueStr,"int")!=null) return valueStr;
      }
      // See if it can be an array of Strings      
      if (mElementInfo.propertyIsOfType(_property,"String[]")) {
        if (mEjs.getModelEditor().getVariablesEditor().checkExpression(valueStr,"String[]")!=null) return valueStr; 
      }
      // See if the string needs quotes
      if (mEjs.getModelEditor().getVariablesEditor().checkExpression(valueStr,"String")==null) {
        int n = VariablesEditor.numberOfQuotes(valueStr);
        if (n==0) valueStr = "\"" + valueStr + "\"";
//        if (!valueStr.startsWith("\"")) valueStr = "\""+ valueStr;
//        if (!valueStr.endsWith("\"") || valueStr.equals("\"")) valueStr = valueStr + "\"";
        _field.setText(valueStr);
      }
    }
    return valueStr;
  }
  
  private class MyFocusListener extends java.awt.event.FocusAdapter {
    private String property;
    MyFocusListener (String _property) { property = _property; }
     public void focusLost(java.awt.event.FocusEvent _e) {
       JTextField field = (JTextField) _e.getSource();
       if (!field.getBackground().equals(Color.white)) doTheChange(field, checkString (field, property), property);
     }
  }

  private class PropertyLine {
    JLabel label;
    JPanel panel;
    JTextComponent field;
    JButton buttonConstant, buttonVariable;
    JComponent superComponent=null;
  }

  class PropertySearchResult extends SearchResult {
    public PropertySearchResult (String anInformation, String aText, JTextComponent textComponent, int aLineNumber, int aCaretPosition) {
      super (anInformation,aText,textComponent,aLineNumber,aCaretPosition);
    }

    public String toString () {
      if (containerTextComponent instanceof JTextField) return information+": "+textFound;
      return information+"("+lineNumber+"): "+textFound;
    }

    public void show () {
      mEditionDialog.setVisible(true);
      containerTextComponent.requestFocusInWindow();
      if (containerTextComponent instanceof JTextArea) {
        String value = editCode (containerTextComponent.getName(), containerTextComponent,caretPosition);
        if (value==null) return; // Edition was cancelled
        if ("<default>".equals(value)) value = "";
        containerTextComponent.setText(value);
        containerTextComponent.setCaretPosition(0);
      }
      else containerTextComponent.setCaretPosition(caretPosition);
    }
  }

  class PropertyLabelMouseListener extends MouseAdapter {
    private String propertyName;
    PropertyLabelMouseListener (String name) {
      propertyName = name;
    }

    public void mousePressed(MouseEvent _evt) {
      if ((_evt.getModifiers() & InputEvent.BUTTON3_MASK)!=0) {
        JPopupMenu defaultPopup = new JPopupMenu();
        String value = mElementInfo.getPropertyDefault(propertyName);
        if (value.startsWith("<") && value.endsWith(">")) value = res.getString("View.Elements."+value);
        defaultPopup.add(new JMenuItem (res.getString("View.Elements.DefaultValue")+" "+propertyName +": "+value));
        //if (_evt.getComponent() instanceof JLabel) defaultPopup.show(_evt.getComponent(),_evt.getComponent().getWidth(),0);
        defaultPopup.show(_evt.getComponent(),0,0);
      }
    }
  }

}

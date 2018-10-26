/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.view;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.text.*;

import org.colos.ejs.library.control.*;
import org.colos.ejs.library.control.swing.*;
import org.colos.ejs.library.control.value.*;
import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.osejs.*;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.edition.TabbedEditor;
import org.colos.ejs.osejs.edition.variables.VariablesEditor;
import org.colos.ejs.osejs.edition.translation.TranslatableProperty;
import org.colos.ejs.osejs.edition.translation.TranslationEditor;
import org.colos.ejs.osejs.utils.*;
import org.opensourcephysics.display.*;
import org.colos.ejs.osejs.utils.MethodParser;

import javax.swing.tree.DefaultMutableTreeNode;

public class ViewElement implements PropertyEditor {
  static private final ResourceUtil res    = new ResourceUtil ("Resources");
  static private final ResourceUtil elRes  = new ResourceUtil ("ElementResources");
  static private final ResourceUtil combinedElRes = new ResourceUtil("ElementTips");
  static private final ResourceUtil propertyRes = new ResourceUtil("PropertyResources");

  static private final int LEFT = 3;
  static private final int RIGHT = 5;
  static private final int fieldWidth = res.getInteger("View.Properties.FieldWidth");
  static private final Insets NULL_INSETS = new Insets(0,0,0,0);
  static private final Font font = InterfaceUtils.font(null,res.getString("Editor.TitleFont"));
  //static private final Border BUTTONS_BORDER = new EmptyBorder (1,2,1,2);
  static private final Border editorBorder = new EmptyBorder (3,1,1,0);
  static private final Dimension dialogSize = res.getDimension("View.Properties.PreferredSize");
  static private final Color labelColor = InterfaceUtils.color(res.getString("View.Properties.LabelColor"));
  static private final Color moreLinesColor = InterfaceUtils.color(res.getString("View.Properties.MoreLinesColor"));
  static private final Color modelColor = InterfaceUtils.color(res.getString("Model.Color"));
  static private final Icon helpIcon = org.opensourcephysics.tools.ResourceLoader.getIcon("data/icons/help.gif");

  private Vector<JComponent> fontList = new Vector<JComponent>();
  private ResourceUtil resources = null;

  private String name="",classname=null, realClassname="",elementClass="";
  private boolean changed=false, editable=true, reading=false; //, firstTimeEdited=true;
  private ControlElement element=null;
  private Osejs ejs;
  private EjsControl group;
  private JDialog editionDialog=null;
  private TreeOfElements tree;
  private java.util.List<JTextComponent> fieldList = null;
  private java.awt.Font theFont=null;
  private JLabel firstLabel=null;
//  private ArrayList<JComponent> deprecatedFields=null;
  private ArrayList<JTextField> fieldsOnly=new ArrayList<JTextField>();
  private EditorForCode editorForCode=null;

  ViewElement (Osejs _ejs, EjsControl _group, String _classname, String _name, TreeOfElements _tree, boolean _reading) {
    ejs = _ejs;
    tree = _tree;
    group = _group;
    classname = _classname;
    if (_classname.equals("Tree.Main")) { name = _name; return; }
    //    System.err.println (this.getClass().getName()+": Creating element "+_name + " of class "+_classname);
    //    System.out.println ("  reading = "+_reading);
    int index = _classname.lastIndexOf('.');
    if (index>=0) elementClass = _classname.substring(index+1);
    else elementClass = _classname;
    String propertyList="_ejs_=true;";
    realClassname = elRes.getString(_classname);

    // Init the resource file and look for default properties
    String resourceFile = elRes.getOptionalString(_classname+".properties");
    if (resourceFile==null) resourceFile = realClassname; // If there is none, assume it is the same
    resources = new ResourceUtil (resourceFile,false);
    if (resources.isNotAccesible()) resources = null;
    else if (!_reading) {
      String defaults = resources.getOptionalString("Defaults");
      if (defaults!=null) propertyList += defaults;
    }

    // Now, create the element
    // System.out.println ("Adding element of class "+realClassname+" called "+_name);
    try {
      Class<?> aClass = Class.forName(realClassname);
      Class<?> [] c = { };
      Object[] o = { };
      java.lang.reflect.Constructor<?> constructor = aClass.getDeclaredConstructor(c);
      ControlElement controlElement = (ControlElement) constructor.newInstance(o);
      element = group.addElement(controlElement, name);
      element.setProperties(propertyList);
    } catch (Exception exc) {
      System.err.println ("Error creating element "+_name+" of class "+realClassname);
      exc.printStackTrace();
    }
    setName(_name);
//    if (element.propertyIsTypeOf("name","DEPRECATED")) tree.setShowDeprecatedFields();
    //    element.setProperty("_ejs_codebase",_tree.getUserCodebase().toString());
    element.addAction(ControlElement.VARIABLE_CHANGED,"_ejsUpdate");
    element.addAction(ControlElement.ACTION,"_ejsUpdate");
    if (element instanceof ControlWindow) {
      element.setProperty("_ejs_window_",res.getString("View.EjsWindow"));
      ((ControlWindow)element).getComponent().addComponentListener(new MyCL()); //(ControlWindow)element));
    }
    else if (element.getComponent() instanceof JButton) {
      final JButton button = (JButton) element.getComponent();
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) { 
          JOptionPane.showMessageDialog(button, res.getString("View.ThisIsNotTheSimulation"));
        }

      });
    }
    element.setPropertyEditor(this);
    element.setVariableEditor(ejs.getModelEditor().getVariablesEditor());
    element.reset();
    //System.err.println (this.getClass().getName()+": OK Creating element "+_name + " of class "+_classname);
  }

  ViewElement (String _classname) {
    classname = _classname;
    int index = _classname.lastIndexOf('.');
    if (index>=0) elementClass = _classname.substring(index+1);
    else elementClass = _classname;
    realClassname = elRes.getString(_classname);

    // Now, create the element
    // System.out.println ("Adding element of class "+realClassname+" called "+_name);
    try {
      Class<?> aClass = Class.forName(realClassname);
      Class<?> [] c = { };
      Object[] o = { };
      java.lang.reflect.Constructor<?> constructor = aClass.getDeclaredConstructor(c);
      element = (ControlElement) constructor.newInstance(o);
    } catch (Exception exc) {
      System.err.println ("Error creating DUMMY element of class "+realClassname);
      exc.printStackTrace();
    }
  }

  // --------------------------------------
  // Translation
  //--------------------------------------

  private Map<LocaleItem, Map<String, String>> translations = new HashMap<LocaleItem, Map<String, String>>();

  public java.util.List<TranslatableProperty> getTranslatableProperties() {
    java.util.List<TranslatableProperty> list = new ArrayList<TranslatableProperty>();

    if (editionDialog==null) createEditionDialog();
    for (JTextComponent field : fieldList) {
      String propertyName = field.getName(), propValue = field.getText().trim();
      if (propValue.length()>0 && element.propertyIsTypeOf(propertyName,"TRANSLATABLE"))  list.add(new ViewTranslatableProperty(this,propertyName));
    }
    return list;
  }

  String getTranslatedProperty(LocaleItem _item, String _property) {
    //    System.out.println ("Get "+_property+" = in "+ (_locale!=null ? _locale.getDisplayLanguage(): "Default locale"));
    if (_item.isDefaultItem()) return TranslationEditor.removeQuotes(element.getProperty(_property));
    Map<String,String> translation = translations.get(_item);
    if (translation!=null) {
      String translatedProperty = translation.get(_property);
      if (translatedProperty!=null) return translatedProperty;
    }
    return TranslationEditor.removeQuotes(element.getProperty(_property));
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
  //--------------------------------------


  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    if (editionDialog==null) createEditionDialog();
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    boolean toLower = (_mode & SearchResult.CASE_INSENSITIVE) !=0;
    if (toLower) _searchString = _searchString.toLowerCase();
    _info = res.getString("View.Properties.Title")+" ";
    for (JTextComponent field : fieldList) {
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
      if (index>=0) list.add(new PropertySearchResult(_info+getName()+"."+getResourceString(textComponent.getName(),false),
          line.trim(),textComponent,lineCounter,caretPosition+index));
      caretPosition += line.length()+1;
      lineCounter++;
    }
    return list;
  }


  public void setEditable (boolean _edit) {
    editable = _edit;
    if (fieldList!=null) for (JTextComponent field : fieldList) field.setEditable(editable);
  }

  public String toString () { return name; }

  public String getName () { return name; }

  public void setName (String _name) {
    //    name = _name;
    name = _name.replace(' ','_');
    element.setProperty("name",name);
    if (editionDialog!=null) {
      String localizedClassname = combinedElRes.getOptionalString(classname.substring(classname.indexOf('.')+1)+".Name");
      if (localizedClassname!=null)
        editionDialog.setTitle(res.getString("View.Properties.Title") + " "+name + " ("+localizedClassname+")");
      else editionDialog.setTitle(res.getString("View.Properties.Title") + " "+name);
    }
  }

  public ControlElement getElement () { return element; }

  public String getClassname () { return classname; }

  public void setFont (java.awt.Font _font)  {
    theFont = _font;
    int height = 20;
    if (firstLabel!=null) {
      FontMetrics fmLabel = firstLabel.getFontMetrics(firstLabel.getFont());
      height = fmLabel.getHeight();
    }
    //    int width = 10;
    //    if (fieldWidth>0) width = fieldWidth;
    for (JComponent comp : fontList) {
      comp.setFont(_font);
      FontMetrics fm = comp.getFontMetrics(comp.getFont());
      int finalHeight = Math.min(height,fm.getHeight());
      //if (org.opensourcephysics.display.OSPRuntime.isMac()) finalHeight += 6;
      comp.setPreferredSize(new Dimension(comp.getSize().width,finalHeight));
      //      if (comp instanceof JTextArea) ((JTextArea) comp).setColumns(width);
      //      else if (comp instanceof JTextField) ((JTextField) comp).setColumns(width);
    }
    if (editionDialog!=null) editionDialog.pack(); 
  }

  public void clear () {
    if (editionDialog!=null) {
      editionDialog.dispose();
      ejs.removeDependentWindows(editionDialog);
    }
    if (getElement() instanceof ControlWindow) ((ControlWindow) getElement()).disposeWindow();
    if (getElement() instanceof ControlSound) ((ControlSound) getElement()).destroy();
    if (element!=null) element.destroy();
  }


  public void remove (ViewElement _child) {
    //    System.out.println (this+": Removing element "+_child);
    _child.element.destroy();
    if (_child.editionDialog!=null) _child.editionDialog.dispose();
  }

  public boolean hasChanged () { return changed; }

  public void setChanged (boolean _changed) { changed = _changed; }

  public java.util.List<javax.swing.text.JTextComponent> getFieldList () {
    if (editionDialog==null) createEditionDialog();
    changed = true;
    return fieldList;
  }

  //  public Color getErrorColor () { return TabbedEditor.ERROR_COLOR; }

  public void displayErrorOnProperty (String property, boolean error) {
    if (editionDialog==null) return;
    for (JTextComponent field : fieldList) {
      if (field.getName().equals(property)) {
        field.setBackground(error ? TabbedEditor.ERROR_COLOR : Color.WHITE);
        return;
      }
    }
  }

  /**
   * This returns whether it is reading a set of properties.
   * This disables the updating of properties when the wrapped element
   * modifies a property.
   * @return boolean
   */
  public boolean isReading () {
    return reading || ejs.isReading();
  }

  public Object evaluateExpression (String _expression) {
    return ejs.getModelEditor().getVariablesEditor().evaluateExpression(_expression);
  }

  // ------------------ public methods

  public boolean acceptsChild (ViewElement _child) {
    if (element==null) return _child.isWindow(); // Root node only accepts windows
    return element.acceptsChild (_child.element);
  }

  public boolean isContainer () {
    if (element==null) return true;
    return (element instanceof ControlContainer);
  }

  public boolean isWindow () {
    if (element==null) return true;
    return (element instanceof ControlWindow);
  }

  public boolean isFrame () {
    if (element==null) return false;
    return (element instanceof ControlFrame);
  }

  public boolean hasBorderLayout () {
    if (element==null) return false;
    String layout = element.getProperty("layout");
    if (layout==null) return false;
    return layout.toLowerCase().startsWith("border");
  }

  public void setCursor (Cursor _cursor) {
    if (element!=null &&  element.getComponent() instanceof JFrame) {
      ((JFrame) element.getComponent()).setCursor(_cursor);
    }
    else if (element instanceof ControlDrawablesParent) {
      ((DrawingPanel) element.getVisual()).setMouseCursor(_cursor);
    }
    //    if (element instanceof ControlContainer) {
    //      ((ControlContainer) element).getVisual().setCursor(_cursor);
    //    }
    else if (element.getVisual()!=null) element.getVisual().setCursor(_cursor);
  }

  public void edit (boolean _overme) {
    if (element==null) return;
    if (editionDialog==null) createEditionDialog();
    if (_overme && element.getVisual()!=null) editionDialog.setLocationRelativeTo (element.getVisual());
    else editionDialog.setLocationRelativeTo (tree.getComponent());
    editionDialog.setVisible(true);
  }

  public void edit (java.awt.Component _overme) {
    if (element==null) return;
    if (editionDialog==null) createEditionDialog();
    if (_overme!=null) editionDialog.setLocationRelativeTo (_overme);
    else editionDialog.setLocationRelativeTo (tree.getComponent());
    editionDialog.setVisible(true);
  }

  public String getPosition () {
    if (element==null) return null;
    String pos = element.getProperty("position");
    if (pos!=null) return pos;
    return res.getString("Tree.NoPosition");
  }

//  public void showDeprecated () {
//    if (deprecatedFields==null) return;
//    //    System.out.println ("Showing deprecated fields for "+name);
//    for (JComponent component : deprecatedFields) component.setVisible(true);
//    editionDialog.validate();
//    editionDialog.pack();
//  }

  public void add (ViewElement _child, int _position) {
    //    System.out.println (this+": Adding element "+_child + " at position "+_position);
    _child.element.setProperty("_ejs_indexInParent_",""+_position);
    _child.element.setProperty("parent",name);
    //    if (element instanceof ControlContainer) { TO DO : does not work
    //      Container parent = ((ControlContainer) element).getContainer();
    //      java.awt.Rectangle b = parent.getBounds();
    //      parent.setBounds (b.x,b.y,b.width+1,b.height+1);
    //      parent.setBounds (b.x,b.y,b.width,b.height);
    //      parent.validate();
    //      parent.repaint();
    //    }
  }

  public void unparent (ViewElement _child) {
    //    System.out.println (this+": Removing element "+_child);
    _child.element.setProperty ("parent",null);
  }

  public StringBuffer saveStringBuffer (String _prefix) {
    StringBuffer txt = new StringBuffer();
    if (_prefix!=null) txt.append(_prefix);
    txt.append("<Type>"+classname+"</Type>\n"+ "<Property name=\"name\">"+name+"</Property>\n");

    String value = element.getProperty("parent");
    if (value!=null) txt.append("<Property name=\"parent\">"+value+"</Property>\n");

    value = element.getProperty("position");
    if (value!=null) txt.append("<Property name=\"position\">"+value+"</Property>\n");

    if (element.getProperty("_ejs_mainWindow")!=null) txt.append("<Property name=\"_ejs_mainWindow\">true</Property>\n");

    if (editionDialog==null) createEditionDialog();
    for (JTextComponent field : fieldList) {
      if (field.getText().trim().length()>0) {
        txt.append("<Property name=\""+field.getName()+"\">");
        txt.append("<![CDATA["+field.getText()+"]]>");
        //        if (element.propertyIsTypeOf(field.getName(),"Action")) txt.append("<![CDATA[\n"+field.getText()+"\n]]>");
        //        else  txt.append(field.getText());
        txt.append("</Property>\n");
      }
      else {
        value = element.getProperty(field.getName());
        if (value!=null) {
          //          System.out.println("Warning View Element : Not saving property : "+field.getName()+" = "+value);
          txt.append("<Property name=\"" + field.getName() + "\">");
          txt.append("<![CDATA["+value+"]]>");
          txt.append("</Property>\n");
        }
      }
    }
    java.util.List<LocaleItem> desiredTranslations = ejs.getTranslationEditor().getDesiredTranslations();
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

  public void readString (String _input, StringBuffer deprecatedBuffer) {
    reading = true;
    String propertyLine = OsejsCommon.getPiece(_input,"<Property name=","</Property>",true);
    while (propertyLine!=null) {
      //      System.err.println ("reading property line : "+propertyLine);
      String property = OsejsCommon.getPiece(propertyLine,"<Property name=\"","\">",false);
      String value;
      if (propertyLine.indexOf("![CDATA[")>=0) value = OsejsCommon.getPiece(propertyLine,"<![CDATA[","]]></Property>",false);
      else value = OsejsCommon.getPiece(propertyLine,"\">","</Property>",false);
      if (property.equals("parent") || property.equals("position") || property.equals("name")) ; // Already done by Tree
      else if (element.propertyIsTypeOf(property, "DEPRECATED")) {
        System.err.println ("Warning: Deprecated property=\""+property+"\" for element "+getName()+". Ignored value=\"" + value+"\"");
        deprecatedBuffer.append("  "+ getName()+" : "+getResourceString(property,false)+" = "+value+"\n");
      }
      else {
        try { setTheProperty(property,value,null); } 
        catch (Exception exc) {
          JOptionPane.showMessageDialog(ejs.getMainPanel(),res.getString("ViewElement.ErrorReadingLine")+"\n"+propertyLine,
              res.getString("Osejs.File.ReadingError"),JOptionPane.ERROR_MESSAGE);
          exc.printStackTrace();
        }
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
    
    reading = false;
  }

  private void readTranslationBlock(String _block) {
    String language = OsejsCommon.getPiece(_block,"<Translation name=\"","\">",false);
    LocaleItem item = LocaleItem.getLocaleItem(language);
    if (item==null) {
      ejs.print("Warning! Ignoring unrecognized locale name : "+language+" for view element "+getName()+"\n");
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

  public Dimension getMainWindowDimension() {
    if (element.getProperty("_ejs_mainWindow")==null || element.getComponent()==null) return null;
    if (element.getComponent() instanceof RootPaneContainer) return ((RootPaneContainer)element.getComponent()).getGlassPane().getSize();
    return element.getComponent().getSize();
  }

//===========    JSV for Parse and rewrite    ================================================
  public String parseViewCall(String propValue,String propertyName){
    StringBuffer txt = new StringBuffer();
    txt.append("  public void _method_for_"+name+"_"+propertyName+" () {\n");
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
    txt.append("  }\n");
    return txt.toString();
  }
//____________________________________________________________________
 

  public StringBuffer generateCode (int _type) {
    StringBuffer txt = new StringBuffer();
    String type = elRes.getString(classname);
    if (editionDialog==null) createEditionDialog();

    if (_type==Editor.GENERATE_CAPTURE_WINDOW) {
      if (element.getProperty("_ejs_mainWindow")!=null && element.getComponent()!=null) {
        Dimension size;
        if (element.getComponent() instanceof RootPaneContainer) size = ((RootPaneContainer)element.getComponent()).getGlassPane().getSize();
        else size = element.getComponent().getSize();
        txt.append("        width=\"" + size.width + "\" height=\""+ size.height + "\"");
      }
    }
    else if (_type==Editor.GENERATE_MAIN_WINDOW) {
      if (element.getProperty("_ejs_mainWindow")!=null && element.getComponent()!=null) {
        txt.append("\""+name+"\"");
      }
    }
    else if (_type==Editor.GENERATE_WINDOW_LIST) {
      if (element.getComponent() instanceof Window) {
        txt.append("    windowList.add(\""+name+"\");\n");
      }
    }
    //    else if (_type==Editor.GENERATE_OWNER_FRAME) {
    //      if (element.getProperty("_ejs_mainWindow")!=null && element.getComponent()!=null) {
    //        if (element instanceof ControlFrame)
    //          txt += "    setOwnerFrame ((Frame) "+ name +".getComponent())\n";
    //      }
    //    }
    else if (_type==Editor.GENERATE_DECLARATION) {
      String objectClass = element.getObjectClassname();
      txt.append("  public " + objectClass+ " " + name +";\n");
    }
    else if (_type==Editor.GENERATE_SERVER_DECLARATION) {
      String objectClass = element.getServerClassname();
      // txt.append("  private " + type+ " _dummy_" + name +";\n");
      txt.append("  public "+objectClass+ " " + name +";\n");
    }
    else if (_type==Editor.GENERATE_SERVER_CODE) { // Code required to instantiate the element in server mode
      String objectClass = element.getServerClassname();
      txt.append("    " + name + " = ("+objectClass+ ")\n  ");
      txt.append("    addElement(new "+ objectClass+"(),\""+name+"\");\n");
    }

    else if (_type==Editor.GENERATE_SERVER_DUMMY_DECLARATION) {
      txt.append("  public org.colos.ejs.library.server.DummyViewElement " + name +";\n");
    }
    else if (_type==Editor.GENERATE_SERVER_DUMMY_CODE) {
      txt.append("    " + name + " = (org.colos.ejs.library.server.DummyViewElement)\n  ");
      txt.append("    addElement(new org.colos.ejs.library.server.DummyViewElement(),\""+name+"\");\n");
    }

    else if (_type==Editor.GENERATE_RESOURCES_NEEDED_BY_PACKAGE) {
      if (resources!=null) {
        String resource = resources.getOptionalString("Resources");
        if (resource!=null) txt = new StringBuffer(resource+";");
      }
    }
    /*
    else if (_type==Editor.GENERATE_JARS_NEEDED) {
      if (resources!=null) {
        String jars = resources.getOptionalString("JarFile");
        if (jars!=null) txt = new StringBuffer(jars+";");
      }
    }
     */
    else if (_type==Editor.GENERATE_RESOURCES_NEEDED) {  // AMAVP (See note in ControlElement)
      for (JTextComponent field : fieldList) {
        String propValue = field.getText().trim();
        if (propValue.length()>0 && element.propertyIsTypeOf(field.getName(),"File")) {
          if (propValue.startsWith("\"") && propValue.endsWith("\"") && VariablesEditor.numberOfQuotes(propValue)==2) 
            propValue = propValue.substring(1,propValue.length()-1);
          //          if (propValue.startsWith("\"")) propValue = propValue.substring(1);
          //          if (propValue.endsWith("\""))   propValue = propValue.substring(0,propValue.length()-1);
          if (element.parseConstant("File",propValue)!=null) txt.append(propValue + ";"); // Set to a constant file
          else { // It is linked to a variable
            if (propValue.startsWith("%")) propValue = propValue.substring(1);
            if (propValue.endsWith("%"))   propValue = propValue.substring(0,propValue.length()-1);
            String realValue = group.getString(propValue);
            if (element.parseConstant("File",realValue)!=null) txt.append(realValue + ";");
          }
        }
      }  // end of for
    } // end of this type

    else if (_type==Editor.GENERATE_CODE) { // Code required to instantiate the element
      // This is required by some elements prior to creation. See f.i. Display3dPanel
      if (element instanceof RequiresInitializationUnderEjs) txt.append(((RequiresInitializationUnderEjs)element).initializationString());
      String objectClass = element.getObjectClassname();
      txt.append("    " + name + " = ("+objectClass+ ")\n  ");
      txt.append("    addElement(new "+ type+"(),\""+name+"\")");
      txt.append("\n      .setProperty(\"_ejs_SecondAction_\",\"updateAfterModelAction()\")");
      { // Special properties
        String value = element.getProperty("exit");     
        if (value!=null) txt.append("\n      .setProperty(\"exit\",\""+value+"\")");

        if (element instanceof ControlSwingElement) { // There is a different position property for Drawables!
          value = element.getProperty("position");        
          if (value!=null) txt.append("\n      .setProperty(\"position\",\""+value+"\")");
        }

        value = element.getProperty("parent");          
        if (value!=null) txt.append("\n      .setProperty(\"parent\",\""+value+"\")");

        //        value = element.getProperty("_ejs_mainWindow"); 
        //        if (value!=null) txt.append("\n      .setProperty(\"onExit\",\"_model._onExit()\")");
      }
      if (element instanceof ControlWindow) txt.append("\n      .setProperty(\"waitForReset\",\"true\")");
      for (JTextComponent field : fieldList) {  // Now set the properties
        String propertyName = field.getName(), propValue = field.getText().trim();
        if (propValue.length()>0) {
          if (element.propertyIsTypeOf(propertyName,"Action")) txt.append("\n      .setProperty(\""+propertyName+"\",\"_model._method_for_"+name+"_"+propertyName+"()\" )");
          else if (requiresMethod(propertyName,propValue))     txt.append("\n      .setProperty(\""+propertyName+"\",\"%_model._method_for_"+name+"_"+propertyName+"()%\" )");
          // AMAVP (See note in ControlElement)
          //else if (TabbedVariablesEditor.isAnExpression(element,field.getName(),propValue) ||
          //         tree.modelEditor.variablesEditor.checkField(element,field.getName(),propValue)!=null)
          else txt.append("\n      .setProperty(\""+propertyName+"\","+commentInvCommas (propertyName,propValue,"_simulation.")+")");
        }
        else { // Maybe the element has some hidden property?
          String value = element.getProperty(propertyName);
          if (value!=null) txt.append("\n      .setProperty(\""+propertyName+"\","+commentInvCommas (propertyName,value.trim(),"_simulation.")+")");
        }
      }
      txt.append("\n      .getObject()");
      txt.append(";\n");
    } // end of GENERATE_CODE

    else if (_type==Editor.GENERATE_VIEW_RESET) { // Code required to reset the element
      txt.append("    getElement(\"" + name + "\")");
      for (JTextComponent field : fieldList) {
        String propertyName = field.getName();
        if (element.propertyIsTypeOf(propertyName,"NO_RESET")) continue;
        String propValue = field.getText().trim();
        if (propValue.length()>0) {
          if (element.propertyIsTypeOf(propertyName,"Action")); // do nothing
          else if (requiresMethod(propertyName,propValue)); // do nothing
          //else if (TabbedVariablesEditor.isAnExpression(element,field.getName(),propValue)); // do nothing
          //else if (tree.modelEditor.variablesEditor.checkField(element,field.getName(),propValue)!=null); // it's a Java constant
          else if (!isVariable(propValue)) // reset the property except if it is a variable
            txt.append("\n      .setProperty(\""+propertyName+"\","+commentInvCommas (propertyName,propValue,"_simulation.")+")");
        }
        else {
          String value = element.getProperty(field.getName());
          if (value!=null) txt.append("\n      .setProperty(\""+field.getName()+"\","+commentInvCommas (field.getName(),value.trim(),"_simulation.")+")");
        }
      }
      txt.append(";\n");
    } // end of GENERATE_VIEW_RESET


    else if (_type==Editor.GENERATE_VIEW_EXPRESSIONS) {  // Code for methods for properties linked to expressions // AMAVP (See note in ControlElement)
      for (JTextComponent field : fieldList) {
        String propValue = field.getText().trim();
        if (propValue.length()<=0) continue;
        String propertyName = field.getName();
        //JSV changes for parse
        if (element.propertyIsTypeOf(propertyName,"Action")) { // Add a void method for action field 
          if (!propValue.endsWith(";")) propValue += ";";
          if (ejs.supportsHtml()) { 
          String parsedS = parseViewCall(propValue,propertyName);
          //while(parsedS.contains("_view."))   parsedS = parseViewCall(propValue,propertyName);
          txt.append(parsedS);
          }
          else {
            txt.append("  public void _method_for_"+name+"_"+propertyName+" () {\n");
            StringTokenizer t = new StringTokenizer(propValue, "\n");
            while (t.hasMoreTokens()) txt.append("    "+t.nextToken()+"\n");
            txt.append("  }\n");
          }
        }
        /*if (element.propertyIsTypeOf(propertyName,"Action")) { // Add a void method for it
          if (!propValue.endsWith(";")) propValue += ";";
          txt.append("  public void _method_for_"+name+"_"+propertyName+" () {\n");
          //          txt.append("    _simulation.disableLoop(); // Make the simulation thread not to step the model\n");
          StringTokenizer t = new StringTokenizer(propValue, "\n");
          while (t.hasMoreTokens()) txt.append("    "+t.nextToken()+"\n");
          //          txt.append("    _simulation.enableLoop(); // Make the simulation thread not to step the model\n");
          txt.append("  }\n");
        }*/
        //JSv end
        else if (requiresMethod(propertyName,propValue)) { // Add a method of the right return type
          Object propObject = evaluateExpression(propValue);
          //          System.out.println (propValue+ " : Type of Object it "+propObject.getClass());
          String returnType=null;// find type of property
          if (propObject!=null) {
            Class<?> aClass = propObject.getClass();
            if (!aClass.isArray()) {
              returnType = aClass.getName();
              if (returnType.startsWith("java.lang.")) returnType = null; // There may be conflicts between Integer and Double
            }
          }
          if (returnType==null) { // Do it 'by hand'  Order is important: double must go before int, and boolean before int
            if      (element.propertyIsTypeOf(propertyName,"double"))  returnType = "double";
            //            else if (element.propertyIsTypeOf(propertyName,"double[]"))  returnType = "double"; // for double[] arrays only - such as Position[]
            else if (element.propertyIsTypeOf(propertyName,"String"))  returnType = "String";
            else if (element.propertyIsTypeOf(propertyName,"Color"))   returnType = "Color";
            else if (element.propertyIsTypeOf(propertyName,"boolean")) returnType = "boolean";
            else if (element.propertyIsTypeOf(propertyName,"int"))     returnType = "int";
            else returnType = "Object";
            // Try to see if it is an array
            String arrayType = returnType+"[]";
            if (element.propertyIsTypeOf(propertyName,arrayType)) {
              if (propValue.indexOf("[]")>=0) returnType = arrayType; // type casting
              else if ((" "+propValue).indexOf(" new ")>=0) returnType = arrayType;
              else if (propValue.startsWith("{")) {
                returnType = arrayType;
                propValue = "new " + returnType +  propValue;
              }
              else if (propValue.indexOf(',')>=0) {
                returnType = arrayType;
                propValue = "new " + returnType+ " { "+propValue+" }";
              }
            }
          }
          //          txt.append("  public "+returnType+" _method_for_"+name+"_"+propertyName+" () { return "+propValue+"; }\n");
          txt.append("  public "+returnType+" _method_for_"+name+"_"+propertyName+" () {\n");
          if (element.propertyIsTypeOf(propertyName,"TRANSLATABLE")) {// Only for translatable properties
            for (Map.Entry<LocaleItem,Map<String,String>> translation : translations.entrySet()) {
              String value = translation.getValue().get(propertyName);
              if (value==null) continue;
              if (!value.endsWith(";")) value += ";";
              if (! (value.startsWith("return ") || value.indexOf(" return ")>=0) ) value = "return "+value;
              txt.append("    if (_view.getLocaleItem().getKeyword().equals(\""+translation.getKey().getKeyword()+"\")) "+value+"\n");
            }
          }
          if (!propValue.endsWith(";")) propValue += ";";
          if (! (propValue.startsWith("return ") || propValue.indexOf(" return ")>=0) ) propValue = "    return "+propValue;          
          txt.append(propValue+"\n  }\n\n"); // Default locale
        }
        /*
          else {
            Value value = tree.modelEditor.variablesEditor.checkField(element,field.getName(),propValue);
            if (value!=null) { // It is a field
              String returnType = null; // find type of property
              if      (value instanceof BooleanValue) returnType = "boolean";
              else if (value instanceof IntegerValue) returnType = "int";
              else if (value instanceof DoubleValue) returnType = "double";
              else if (value instanceof StringValue) returnType = "String";
              else returnType = "Object";
              txt.append("  public "+returnType+" _method_for_"+name+"_"+field.getName()+" () { return "+propValue+"; }\n");
            }
          }
         */
      }  // end of for
    } // end of this type

    else if (_type==Editor.GENERATE_CHANGE_LOCALE) {
      txt.append("    mMainView.getConfigurableElement(\""+name+"\")");
      for (JTextComponent field : fieldList) {
        String propertyName = field.getName();
        if (!element.propertyIsTypeOf(propertyName,"TRANSLATABLE")) continue; // Only for translatable properties
        String propValue = field.getText().trim();
        if (propValue.length()>0) {
          if (element.propertyIsTypeOf(propertyName,"Action")); // do nothing
          else if (requiresMethod(propertyName,propValue));
          //else if (TabbedVariablesEditor.isAnExpression(element,field.getName(),propValue)); // do nothing
          //else if (tree.modelEditor.variablesEditor.checkField(element,field.getName(),propValue)!=null); // it's a Java constant
          else if (!isVariable(propValue)) // reset the property except if it is a variable
            txt.append("\n      .setProperty(\""+propertyName+"\","+commentInvCommas (propertyName,propValue,"")+")");
        }
        else {
          String value = element.getProperty(propertyName);
          if (value!=null) txt.append("\n      .setProperty(\""+propertyName+"\","+commentInvCommas (propertyName,value.trim(),"")+")");
        }
      } // end of for
      txt.append(";\n");
    } // end of this type

    return txt;
  }

  public void updateProperties (boolean makeVisible) {
    //    System.out.println ("Updating properties of "+getName());
    if (editionDialog==null) createEditionDialog();
    boolean showErrors = makeVisible || tree.getEjs().getOptions().showPropertyErrors();
    for (JTextField field : fieldsOnly) {
      String value = field.getText().trim();
      if (value.length()<=0) continue;
      String property = field.getName();
//            System.out.println("Setting property "+property +" to "+value);
      //field.setBackground(Color.WHITE);
      if (setTheProperty (property,value,field)==false) {
        if (showErrors && !editionDialog.isVisible()) editionDialog.setVisible(true);
      }
      else field.setBackground(Color.WHITE);
    }
  }

  /**
   * This method is used to see if a property value is an expression.
   * The control element will then use the interpreter to find the value.
   */
  private boolean isExpression (String _property, String str) { //AMAVP (See note in ControlElement)
    //System.out.println ("Is an expression? "+str);
    if (str==null) return false;
    str = str.trim();
    if (VariablesEditor.isAConstant(element,_property,str)) return false;
    if (str.indexOf('.')>=0) return true; // This is for things like Color.BLUE or Math.PI
    if (str.indexOf('+')>=0) return true;
    if (str.indexOf('-')>=0) return true;
    if (str.indexOf('*')>=0) return true;
    if (str.indexOf('/')>=0) return true;
    if (str.indexOf('%')>=0) return true;
    if (str.indexOf('>')>=0) return true;
    if (str.indexOf('<')>=0) return true;
    if (str.indexOf("==")>=0) return true;
    if (str.indexOf('!')>=0) return true;
    if (str.indexOf('&')>=0) return true;
    if (str.indexOf('|')>=0) return true;
    if (str.indexOf('(')>=0) return true;
    if (str.indexOf(')')>=0) return true;
    if (str.indexOf('[')>=0) return true;
    if (str.indexOf(']')>=0) return true;
    if (str.indexOf(',')>=0) return true;
    if (str.indexOf('{')>=0) return true;
    if (str.indexOf('}')>=0) return true;
    if (str.indexOf('?')>=0) return true;
    return false;
  }

  /**
   * Whether a given property requires a method to be set
   */
  private boolean requiresMethod (String _property, String _value) { //AMAVP (See note in ControlElement)
    if (_value==null) return false;
    //    System.out.println ("Requires method? "+_value);

    if (isVariable(_value)) {
      //      System.out.println ("No, it is a variable "+_value);
      return false;
    }
    if (VariablesEditor.isAConstant(element,_property,_value.trim())) {
      //      System.out.println ("NO, it is a constant "+_value);
      return false;
    }
    //    System.out.println ("YES!");

    return true;
  }

  /**
   * Whether a given property is linked to a variable
   */
  private boolean isVariable (String _value) {
    if (_value==null) return false;
    String trimmedValue = _value.trim();
    if (trimmedValue.startsWith("%") && trimmedValue.endsWith("%")) return true; // It should be a variable
    if (group.getVariable(trimmedValue)!=null) {
      //System.out.println ("Found group variable "+trimmedValue);
      return true; // It is a variable
    }
    return false;
  }

  /**
   * Sets the value of the element property and checks for correct syntax
   * @return true if the value was correct, false if there was an error
   */
  private boolean setTheProperty (String _property, String _value, JTextField _field) {
    //    System.err.println ("Element "+name+" setting "+_property+ " to "+_value);
    if (_value==null || element.propertyIsTypeOf(_property,"Action")) { // No need to check the syntax for actions (field==null) or to reset to default
      element.setProperty(_property, _value);
      return true; 
    }
    // A property can be of several types - such an an angle, which can be either int or double, with several meaning (degrees or radians)
    ArrayList<String> possibleTypes = new ArrayList<String>();
    String info = element.getPropertyInfo(_property); 
    boolean tryObjectToo=false; // Allowing Objects before any other type will always return an ObjectValue
    if (info!=null && info.indexOf('[')>=0) tryObjectToo=true; // possibleTypes.add("Object");
    if (element.propertyIsTypeOf(_property,"int"))     possibleTypes.add("int"); // Order is important since an integer is also accepted as a double
    if (element.propertyIsTypeOf(_property,"double"))  possibleTypes.add("double");
    if (element.propertyIsTypeOf(_property,"boolean")) possibleTypes.add("boolean");
    if (element.propertyIsTypeOf(_property,"String"))  possibleTypes.add("String");
    if (element.propertyIsTypeOf(_property,"Object"))  tryObjectToo=true; // possibleTypes.add("Object");
    if (possibleTypes.size()<=0) tryObjectToo = true; // possibleTypes.add("Object");

    // Check for a variable of the form %varName%
    String trimmedValue = _value.trim();
    if (trimmedValue.startsWith("%") && trimmedValue.endsWith("%")) {
      element.setProperty(_property, trimmedValue);
      String variable = trimmedValue.substring(1,trimmedValue.length()-1);
      for (String type : possibleTypes) if (ejs.getModelEditor().getVariablesEditor().isVariableDefined(variable,type)) return true;
      if (tryObjectToo && ejs.getModelEditor().getVariablesEditor().isVariableDefined(variable,"Object")) return true; 
      if (_field!=null) _field.setBackground(TabbedEditor.ERROR_COLOR);
      return false;
    }

    // Check for a special value of the property. This is before normal variables to match the sequence in ControlElement

    String trValue = _value;
    if (_value.startsWith("\"") && _value.endsWith("\"") && VariablesEditor.numberOfQuotes(_value)==2) trValue = _value.substring(1,_value.length()-1);
    Value valueObj = element.parseConstant(element.propertyType(_property),trValue);
    if (valueObj!=null) {
      element.setProperty(_property, _value);
      element.setValue(element.propertyIndex(_property),valueObj);
      return true;
    }

    // Check for a variable already defined
    for (Iterator<String> it=possibleTypes.iterator(); it.hasNext(); ) { // an iterator keeps the order
      if (ejs.getModelEditor().getVariablesEditor().isVariableDefined(trimmedValue,it.next())) {
        element.setProperty(_property, trimmedValue);
        return true; 
      }
    }
    if (tryObjectToo && ejs.getModelEditor().getVariablesEditor().isVariableDefined(trimmedValue,"Object")) {
      element.setProperty(_property, trimmedValue);
      return true;      
    }

    // Check for an expression
    for (Iterator<String> it=possibleTypes.iterator(); it.hasNext(); ) { // an iterator keeps the order
      valueObj = ejs.getModelEditor().getVariablesEditor().checkExpression(_value, it.next());
      if (valueObj!=null) break;
    }
    if (valueObj==null && tryObjectToo) valueObj = ejs.getModelEditor().getVariablesEditor().checkExpression(_value, "Object");

    if (valueObj==null) { // Now check for a String
      if (element.propertyIsTypeOf(_property,"String")) {
        element.setProperty(_property, _value);
        element.setValue(element.propertyIndex(_property),new StringValue(trValue));
        if (VariablesEditor.numberOfQuotes(_value) % 2 !=0) {
          _field.setBackground(TabbedEditor.ERROR_COLOR);
          return false;
        }
        return true;
      }
    }

    if (isExpression(_property, _value)) element.setProperty(_property, "@"+_value+"@");
    else element.setProperty(_property, _value);

    if (_field==null) return true; // No need to check
    if (valueObj!=null) {
      element.setValue(element.propertyIndex(_property),valueObj);
      return true;
    }
    _field.setBackground(TabbedEditor.ERROR_COLOR);
    return false;
  }

  private String commentInvCommas (String _property, String str, String simulationPrefix) {
    boolean hadQuotes = false;
    if (str.charAt(0)=='"') {
      hadQuotes = true;
      str = str.substring(1);
    }
    int l = str.length()-1;
    if (str.charAt(l)=='"') {
      hadQuotes = true;
      str = str.substring(0,l);
    }
    l = str.length();
    String txt = "";
    for (int i=0; i<l; i++) {
      char c = str.charAt(i);
      if (c=='\"') txt += "\\\"";
      else if (c=='\\') {
        if (i+1<l) {
          char nextChar = str.charAt(i+1);
          if (nextChar=='\\') { txt += "\\\\"; i++; }
          else if (nextChar=='\"') { txt += "\\\""; i++; }
          else if (nextChar=='u') { txt += "\\u"; i++; } // respect unicode characters
          else txt += "\\\\";
        }
        else txt += "\\\\"; 
      }
      else txt += c;
    }
    if (ejs.getSimInfoEditor().addTranslatorTool() && element.propertyIsTypeOf(_property,"TRANSLATABLE")) {
      if (hadQuotes) return simulationPrefix+"translateString(\"View."+name+"."+_property+"\",\"\\\""+txt+"\\\"\")";
      return simulationPrefix+"translateString(\"View."+name+"."+_property+"\",\""+txt+"\")";
    }
    return "\""+txt+"\"";
  }

  // ---------------------------------------

  private PropertyLine createOnePropertyLine (String _property, int _width) {
    PropertyLine line = new PropertyLine();

    String txt = getResourceString(_property,false);
    line.label = new JLabel (txt,SwingConstants.RIGHT);
    if (_width>0) line.label.setPreferredSize(new Dimension(_width,line.label.getHeight()));
    line.label.setFont(font);
    line.label.setBorder(new javax.swing.border.EmptyBorder(0,LEFT,0,RIGHT));
    MouseListener listener = new PropertyLabelMouseListener (element.propertyIndex(_property),txt);
    line.label.addMouseListener(listener);
    txt = getResourceString(_property,true);
    line.label.setToolTipText(txt.trim());

    if (firstLabel==null) firstLabel = line.label;
    // fontList.add (line.label); // For future customization

    txt = element.propertyType(_property);
    java.awt.event.MouseListener mouseListener=null;

    if (element.propertyIsTypeOf(_property,"Action")) {
      line.label.setForeground(modelColor);
      JTextArea lineArea = new JTextArea ();
      lineArea.setRows(1);
      if (fieldWidth>0) lineArea.setColumns (fieldWidth);
      else lineArea.setColumns (10);
      mouseListener = new MyMouseListener(lineArea);
      lineArea.getDocument().addDocumentListener (new MyDocumentListener(_property,lineArea));
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
      lineField.setActionCommand (_property);
      mouseListener = new MyMouseListener(lineField);
      lineField.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent _evt) {
          String property=_evt.getActionCommand();
          //          System.out.println ("Calling field event for "+property+" at "+name);
          JTextField field = (JTextField) _evt.getSource();
          doTheChange(field,checkString (field, property),property);
        }
      });
      lineField.addKeyListener (new MyKeyListener(_property));
      lineField.addFocusListener (new MyFocusListener(_property));
      line.field = lineField;
      fieldsOnly.add(lineField);
    }
    line.field.addMouseListener(listener);
    line.field.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed (java.awt.event.KeyEvent _e) {
        if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_F1) tree.getEjs().openWikiPage(classname);
      }
    });
    txt = element.getProperty(_property);
    //    System.out.println ("Getting property "+_property +" = "+txt);
    if (txt!=null) {
      line.field.setText (txt);
      line.field.setCaretPosition(0);
    }
    //      else line.field.setText ("<default>");
    line.field.setEditable(editable);
    line.field.setMargin (NULL_INSETS);
    line.field.setName (_property);
    fieldList.add(line.field);
    line.field.setFont(theFont);
    fontList.add (line.field); // For future customization

    line.buttonConstant = new JButton (TreeOfElements.editIcon);
    line.buttonConstant.setMargin(NULL_INSETS);
    //line.buttonConstant.setBorder(BUTTONS_BORDER);
    line.buttonConstant.setName("CONSTANT:"+_property);
    line.buttonConstant.addMouseListener (mouseListener);
    line.buttonConstant.setFocusable(false);

    line.buttonVariable = new JButton(TreeOfElements.linkIcon);
    line.buttonVariable.setMargin(NULL_INSETS);
    //line.buttonVariable.setBorder(BUTTONS_BORDER);
    line.buttonVariable.setName("VARIABLE:"+_property);
//    line.buttonVariable.setEnabled(true); 
    line.buttonVariable.addMouseListener (mouseListener);
    line.buttonVariable.setFocusable(false);

    txt = element.propertyType(_property);
    if (element.propertyIsTypeOf(_property,"Action")) {
      line.buttonVariable.setIcon(TreeOfElements.actionIcon);
      line.buttonConstant.setName ("ACTION:"+_property);
      //      line.buttonConstant.setEnabled(false);
    }
    else if (element.propertyIsTypeOf(_property,"CONSTANT")) {
      line.buttonVariable.setEnabled(false);
    }
    else if (!hasSpecialEditor(_property) ) {
      //      if (txt.equals("String") || txt.equals("String[]") || txt.equals("String|String[]") ||
      //             txt.equals("double") || txt.equals("double[]") || txt.equals("double[][]") || txt.equals("double|double[]") || 
      //             txt.equals("int") || txt.equals("int|double") || txt.equals("int|int[]") ||  
      //             txt.equals("int|double|double[]") ) 
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
    fieldList = new ArrayList<JTextComponent>();
    int numColumns=0;
    JPanel finalPanel = new JPanel (new GridLayout(1,0));
    boolean isFirstColumn = true, isNewColumn = false;

    String orderedProperties = resources.getOptionalString("DisplayOrder");
    if (orderedProperties==null) {
      System.err.println("Can't find resources for element of class "+this.classname);
    }
    else {
      if (orderedProperties.indexOf(';')<0 && orderedProperties.indexOf('+')>=0) {
        StringTokenizer tkn = new StringTokenizer(orderedProperties,"+");
        String fullList = "";
        while (tkn.hasMoreTokens()) {
          String newline = resources.getString(tkn.nextToken().trim());
          if (!newline.endsWith(";")) newline = newline + ";"; // Make sure it ends with a semicolon
          fullList = fullList + newline;
        }
        orderedProperties = fullList;
      }
      Box columnPanel=null;
      numColumns = 0;
      java.util.List<JLabel> labelList = new ArrayList<JLabel>();
      orderedProperties = orderedProperties.trim();
      if (!orderedProperties.startsWith("<COLUMN>;")) orderedProperties = "<COLUMN>;" + orderedProperties; // Make sure a new column is created
      if (!orderedProperties.endsWith(";")) orderedProperties = orderedProperties + ";"; // Make sure it ends with a semicolon
      // Compute longer label
      int maxWidth = 0;
      StringTokenizer tkn = new StringTokenizer(orderedProperties,";");
      JLabel aLabel = new JLabel ("dummy");
      FontMetrics fm = aLabel.getFontMetrics(aLabel.getFont());
      while (tkn.hasMoreTokens()) {
        String property = tkn.nextToken().trim();
        if (property.length()<=0) continue;
        if (property.startsWith("<")) continue;
        if (!element.implementsProperty(property)) {
          System.err.println ("Warning: View Element : unsupported property <"+property+"> for element : "+element);
          continue;
        }
        if (element.propertyIsTypeOf(property, "DEPRECATED")) {
          continue;
        }
        if (element.propertyIsTypeOf(property, "UNNECESSARY")) {
          continue;
        }
        String txt = this.getResourceString(property,false);
        //        if (resources!=null) txt = resources.getString(property);
        //        else txt = property;
        int lenx = fm.stringWidth(txt)+LEFT+RIGHT;
        if (lenx>maxWidth) maxWidth = lenx;
      }
      maxWidth += 4;
      tkn = new StringTokenizer(orderedProperties,";");
      while (tkn.hasMoreTokens()) {
        String property = tkn.nextToken().trim();
        if (element.propertyIsTypeOf(property, "DEPRECATED")) {
          continue;
        }
        if (element.propertyIsTypeOf(property, "UNNECESSARY")) {
          continue;
        }
        if (property.equals("<COLUMN>")) {
          numColumns++;
          //          columnPanel = new JPanel (new GridLayout(0,1));
          columnPanel = Box.createVerticalBox();
          JPanel bigPanel = new JPanel (new BorderLayout());
          bigPanel.add (columnPanel,BorderLayout.NORTH);
          finalPanel.add(bigPanel);
          isNewColumn = true;
        }
        else if (property.startsWith("<LABEL")) {
          String label = getResourceString(property.substring(1,property.length()-1),false);
          JLabel jlabel = new JLabel(label, SwingConstants.CENTER);
          jlabel.setFont(jlabel.getFont().deriveFont(Font.BOLD));
          jlabel.setForeground(labelColor);
          JPanel labelPanel = new JPanel(new BorderLayout());
          labelPanel.add(jlabel, BorderLayout.CENTER);
          if (isFirstColumn) {
            JLabel helpLabel = new JLabel(helpIcon);
            helpLabel.setToolTipText(res.getString("Help.F1ForHelp"));
            helpLabel.addMouseListener(new java.awt.event.MouseAdapter() {
              public void mousePressed(java.awt.event.MouseEvent evt) { tree.getEjs().openWikiPage(classname); }
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
        else if (property.startsWith("<EDITOR")) {
          if (element instanceof HasEditor) {
            final String editor = property.substring(7,property.length()-1);
            String label = getResourceString("LABEL"+editor,false);
            JButton button = new JButton (label);
            button.setToolTipText(getResourceString("LABEL"+editor+".ToolTip",false)); // Do NOT use true
            button.setFont(button.getFont().deriveFont(Font.BOLD));
            button.setForeground(labelColor);
            button.setMargin(NULL_INSETS);
            button.addActionListener(new ActionListener(){
              public void actionPerformed(ActionEvent e) { ((HasEditor) element).showEditor(editor); }
            });
            JPanel buttonPanel = new JPanel(new BorderLayout());
            buttonPanel.setBorder(editorBorder);
            buttonPanel.add(button, BorderLayout.CENTER);
            if (columnPanel==null) System.err.println ("null column for property="+property);
            else columnPanel.add(buttonPanel);
            //columnPanel.add(new JSeparator(JSeparator.HORIZONTAL));
          }
        }
        else if (element.implementsProperty(property)) {
          PropertyLine line = createOnePropertyLine (property,maxWidth);
          if (line!=null) { // Add one line with all the components
            if (columnPanel==null) System.err.println ("null column for property="+property);
            else columnPanel.add(line.panel);
            labelList.add(line.label);
          }
        }
      }
    }

    JScrollPane scrollPanel = new JScrollPane(finalPanel);

    editionDialog = new JDialog (ejs.getMainFrame());

    String localizedClassname = combinedElRes.getOptionalString(classname.substring(classname.indexOf('.')+1)+".Name");
    if (localizedClassname!=null)
      editionDialog.setTitle(res.getString("View.Properties.Title") + " "+name + " ("+localizedClassname+")");
    else editionDialog.setTitle(res.getString("View.Properties.Title") + " "+name);
    if (dialogSize.width>0) editionDialog.setSize ((numColumns*dialogSize.width)/2,dialogSize.height);

    editionDialog.getContentPane().setLayout (new BorderLayout());
    editionDialog.getContentPane().add (scrollPanel,BorderLayout.CENTER);

    editionDialog.validate();
    editionDialog.pack();
    editionDialog.setLocationRelativeTo (tree.getComponent());
    editionDialog.setVisible(false);
//    editionDialog.setModal  (false);
    ejs.addDependentWindows(editionDialog);
//    ejs.setMenuBar(editionDialog);
  }

  private String editLink(String _property, JTextComponent _field){
    String type = element.propertyType(_property);
    //    System.out.println ("Type for link of "+_property +" is "+type);
    if (type==null) return "<default>";
    if (type.equals("Action")) return EditorForVariables.edit (ejs.getModelEditor(), "Actions", null, _field, _field.getText().trim(),null);
    String txt = EditorForVariables.edit (ejs.getModelEditor(), "VariablesAndCustomMethods", type, _field, _field.getText().trim(),null);
    if (element.propertyIsTypeOf(_property,"String")) {
      if (txt==null); // why I ever added this? -> txt = "";
      else txt = "%"+txt+"%";
    }
    return txt;
  }

  private String editCode(String _property, JTextComponent _field, int caret){
    String txt = getResourceString(_property,false);
    if (editorForCode==null) {
      editorForCode = new EditorForCode(ejs);
      editorForCode.setFont(theFont);
    }
    return editorForCode.edit (name,txt, _field, caret);
  }

  String getResourceString(String searchStr, boolean isTooltip){
    String alias = element.getPropertyCommonName(searchStr);
    String property=null;
    if (isTooltip) {
      property = searchStr;
      searchStr += ".ToolTip";
      alias     += ".ToolTip";
    }
    String txt=null;
    if (resources!=null) txt = resources.getOptionalString(searchStr);
    if (txt==null) txt = propertyRes.getOptionalString(elementClass+"."+searchStr); // Search in common repository for a special entry
    if (txt==null) txt = propertyRes.getOptionalString(alias); // Search in common repository
    if (txt==null) {
      System.err.println("Warning: View element: resource not found: "+searchStr+ " for element : "+element);
      txt = searchStr; // use the plain property name
    }
    else if (isTooltip) {
      String type = element.getPropertyInfo(property);
      StringTokenizer tkn = new StringTokenizer(type," |");
      String suffix="";
      while (tkn.hasMoreTokens()) {
        String token = tkn.nextToken();
        if (!token.toUpperCase().equals(token)) suffix += ", "+token;
      }
      if (suffix.startsWith(", ")) suffix = suffix.substring(2);
      txt += ": ("+suffix+")";
    }
    return txt;
  }

  private boolean hasSpecialEditor(String _property) { //,JTextField _field){
    String type = element.propertyType(_property);
    //    System.out.println ("Type of "+_property +" is "+type);
    if (type==null) type = "double";
    StringTokenizer tkn = new StringTokenizer(type,"|");
    while (tkn.hasMoreTokens()) {
      String ownEditor = elRes.getOptionalString("Editors."+tkn.nextToken());
      if (ownEditor!=null) { 
        if (ownEditor.equals("org.colos.ejs.control.editors.EditorForFile")) return true;
        try {
          if (ownEditor.indexOf('.')<0) ownEditor = "org.colos.ejs.control.editors."+ownEditor;
          Class<?> c = Class.forName(ownEditor);
          java.lang.reflect.Method editMethod;
          Class<?>[] classList = new Class[4];
          classList[0] = realClassname.getClass();
          classList[1] = _property.getClass();
          classList[2] = type.getClass();
          classList[3] = JTextField.class; // _field.getClass();
          editMethod = c.getMethod("edit",classList);
          return editMethod!=null;
        }
        catch (Exception _e) { return false; }
      }
    }
    return false;
  }

  private String specialEdition(String _property, JTextField _field){
    String type = element.propertyType(_property);
    //    System.out.println ("Type of "+_property +" is "+type);
    if (type==null) type = "double";
    StringTokenizer tkn = new StringTokenizer(type,"|");
    while (tkn.hasMoreTokens()) {
      String ownEditor = elRes.getOptionalString("Editors."+tkn.nextToken());
      if (ownEditor!=null) { 
        if (ownEditor.equals("org.colos.ejs.control.editors.EditorForFile")) 
          return org.colos.ejs.control.editors.EditorForFile.edit(ejs,_field,_property);
        try {
          if (ownEditor.indexOf('.')<0) ownEditor = "org.colos.ejs.control.editors."+ownEditor;
          Class<?> c = Class.forName(ownEditor);
          java.lang.reflect.Method editMethod;
          Class<?>[] classList = new Class[4];
          classList[0] = realClassname.getClass();
          classList[1] = _property.getClass();
          classList[2] = type.getClass();
          classList[3] = _field.getClass();
          Object[] objList   = new Object[4];
          objList[0] = realClassname;
          objList[2] = type;
          objList[1] = _property;
          objList[3] = _field;
          editMethod = c.getMethod("edit",classList);
          return (String) editMethod.invoke (c,objList);
        }
        catch (Exception _e) {
          System.err.println ("Element "+element.getClass().getName()+" can't really edit property of type "+type);
          _e.printStackTrace();
        }
      }
    }
    // Use a simple editor to input a String
    return org.colos.ejs.control.editors.EditorForString.edit(this.getResourceString(_property,false),_field);
    //return null;
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
      setTheProperty (property,value,null);
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
      String valueStr = null;
      //        System.out.println ("Calling field event for "+property+" at "+name);
      if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
        if (property.startsWith("CONSTANT:")) {
          property = property.substring(9);
          valueStr = org.colos.ejs.control.editors.EditorForString.edit(getResourceString(property,false),field);
          if (valueStr==null) return; // Edition was canceled
          if ("<default>".equals(valueStr)) valueStr = "";
          field.setText(valueStr);
          valueStr = checkString (field, property);
        }
        else return;
      }
      else { // Normal click
        //          System.out.println ("Edit "+property+" for "+name);
        if (property.startsWith("CONSTANT:")) {
          property = property.substring(9);
          valueStr = specialEdition(property,field);
          if (valueStr==null) return; // Edition was canceled
          if ("<default>".equals(valueStr)) valueStr = "";
          field.setText(valueStr);
          valueStr = checkString (field, property);
        }
        else if (property.startsWith("VARIABLE:")) {
          property = property.substring(9);
          if (area!=null) valueStr = editLink (property, area);
          else valueStr = editLink (property, field);
        }
        else if (property.startsWith("ACTION:")) {
          property = property.substring(7);
          if (area!=null) valueStr = editCode (property, area,-1);
          else valueStr = editCode (property, field,-1);
        }
        else valueStr = specialEdition(property,field);
        if (valueStr==null) return; // Edition was canceled
        if ("<default>".equals(valueStr)) valueStr = "";
        if (area!=null) { area.setText(valueStr); area.setCaretPosition(0); }
        else { field.setText(valueStr); field.setCaretPosition(0); }
      }
      doTheChange(field,valueStr,property);
    }
  }

  private void doTheChange (JTextField _field, String _value, String _property) { // Now do the change
    changed = true;
    //System.out.println ("Setting value for property "+_property+" to "+_value);
    if (_field!=null) _field.setBackground(Color.white);
    if (_value.trim().length()<=0) _value=null; // Set the default value
    setTheProperty (_property,_value,_field);
    element.reset();
    group.update();
    group.finalUpdate();
  }

  private class MyKeyListener implements java.awt.event.KeyListener {
    private String property;
    MyKeyListener (String _property) { property = _property; }
    public void keyPressed  (java.awt.event.KeyEvent _e) {
      if (_e.getKeyCode()==27)   {
        String value = element.getProperty(property);
        if (value==null) value="";
        ((JTextField)_e.getComponent()).setText(value);
        _e.getComponent().setBackground (Color.white);
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
    if (element.propertyIsTypeOf(_property,"String")) {
      if (valueStr.trim().length()<=0 || (valueStr.startsWith("%") && valueStr.endsWith("%"))) return valueStr;  // Do nothing
      // See if a double is acceptable and it is a double
      if (element.propertyIsTypeOf(_property,"double")) {
        if (ejs.getModelEditor().getVariablesEditor().checkExpression(valueStr,"double")!=null) return valueStr;
      }
      // See if it can be an array of Strings      
      if (element.propertyIsTypeOf(_property,"String[]")) {
        if (ejs.getModelEditor().getVariablesEditor().checkExpression(valueStr,"String[]")!=null) return valueStr; 
      }
      // See if the string needs quotes
      if (ejs.getModelEditor().getVariablesEditor().checkExpression(valueStr,"String")==null) {
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

  private class MyCL extends java.awt.event.ComponentAdapter {
    public void componentHidden(java.awt.event.ComponentEvent _e) {
      if (tree.keepPreviewHidden()) return; // Disable collapsing the node
      DefaultMutableTreeNode node = tree.findNode(name);
      if (node!=null) {
        javax.swing.tree.TreePath path = new javax.swing.tree.TreePath (node.getPath());
        tree.tree.collapsePath(path);
      }
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
      editionDialog.setVisible(true);
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
    private int propertyIndex;
    private String propertyName;
    PropertyLabelMouseListener (int index, String name) {
      propertyIndex = index;
      propertyName = name;
    }

    public void mousePressed(MouseEvent _evt) {
      if ((_evt.getModifiers() & InputEvent.BUTTON3_MASK)!=0) {
        JPopupMenu defaultPopup = new JPopupMenu();
        String value = element.getDefaultValueString(propertyIndex);
        if (value.startsWith("<") && value.endsWith(">")) value = res.getString("View.Elements."+value);
        defaultPopup.add(new JMenuItem (res.getString("View.Elements.DefaultValue")+" "+propertyName +": "+value));
        //if (_evt.getComponent() instanceof JLabel) defaultPopup.show(_evt.getComponent(),_evt.getComponent().getWidth(),0);
        defaultPopup.show(_evt.getComponent(),0,0);
      }
    }
  }

}

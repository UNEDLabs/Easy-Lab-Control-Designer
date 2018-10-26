/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Modified : March 2006
 */

package org.colos.ejs.osejs.edition.variables;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.EJSAware;
import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejss.xml.SimulationXML;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ResourceLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Creates a panel with groups (and subgroups) of buttons, each of which can create a model element.
 */
public class ElementsEditor implements Editor {
  static public final Icon DIR_UP_ICON = ResourceLoader.getIcon("data/icons/back.png");
  static public final Icon DIR_ICON    = ResourceLoader.getIcon("data/icons/folder.png");
  static public final Icon HTML_ICON   = ResourceLoader.getIcon("data/icons/info32.png");
  static public final Icon USER_DEFINED_ICON = ResourceLoader.getIcon("data/icons/UserDefined.png");

  static public final javax.swing.border.Border EMPTY_BORDER  = BorderFactory.createEmptyBorder(1,1,1,1); 
  static public final javax.swing.border.Border ACTIVE_BORDER = BorderFactory.createLineBorder(new Color (128,64,255),1);
  static public final javax.swing.border.Border BLACK_BORDER  = BorderFactory.createLineBorder(Color.BLACK,1);
  
  private Osejs ejs;
  private JPanel fullPanel;
  private TitledBorder leftTitleBorder, rightTitleBorder;
  private boolean firstTime=true; // used to set the divider location to 0.5 at start-up
  private boolean advanced = false;
  private ModelElementsList modelElementsList;
  private String name;
  private ModelElementsPalette elementsPanel;
  
  public ElementsEditor (Osejs _ejs) {
    this.ejs = _ejs;
    fullPanel = new JPanel(new BorderLayout()); // This must be created first, because elementsPanel reads its background.

    // The panel with the list of elements to offer
    elementsPanel = new ModelElementsPalette(_ejs,fullPanel.getBackground());
    elementsPanel.setPreferredSize(new Dimension(38,38)); // The size is arbitrary (albeit small). This is required or, else, the panel will be too large!

    rightTitleBorder = new TitledBorder (new EmptyBorder(10,0,0,0),Osejs.getResources().getString("Model.Elements.ToolTip"));
    rightTitleBorder.setTitleJustification (TitledBorder.LEFT);
    rightTitleBorder.setTitleFont (InterfaceUtils.font(null,Osejs.getResources().getString("Editor.TitleFont")));
    
    JPanel rightPanel = new JPanel (new BorderLayout());
    rightPanel.setBorder (rightTitleBorder);
    rightPanel.add(elementsPanel,BorderLayout.CENTER);
    
    // Create the panel of elements added
    modelElementsList = new ModelElementsList(_ejs);
    modelElementsList.setPreferredSize(new Dimension(38,38)); // The size is arbitrary (albeit small). This is required or, else, the panel will be too large!

    leftTitleBorder = new TitledBorder (new EmptyBorder(10,0,0,0),Osejs.getResources().getString("Model.ElementsAdded"));
    leftTitleBorder.setTitleJustification (TitledBorder.LEFT);
    leftTitleBorder.setTitleFont (InterfaceUtils.font(null,Osejs.getResources().getString("Editor.TitleFont")));
    
    JPanel leftPanel = new JPanel (new BorderLayout());
    leftPanel.setBorder (leftTitleBorder);
    leftPanel.add(modelElementsList,BorderLayout.CENTER);
    
    // Create the main panel
    final JSplitPane mainPanel = new JSplitPane();
    mainPanel.setOneTouchExpandable(true);
    mainPanel.setLeftComponent(leftPanel);
    mainPanel.setRightComponent(rightPanel);
    mainPanel.setFocusable(true);
    mainPanel.setResizeWeight(1.0);
    
    fullPanel.add(mainPanel,BorderLayout.CENTER);
    fullPanel.addComponentListener(new ComponentAdapter(){
      public void componentResized(ComponentEvent e) {
        if (firstTime) {
          mainPanel.setDividerLocation(0.5);
          firstTime = false;
        }
      }
    });
    setZoomLevel(FontSizer.getLevel());
  }
  
  public void setZoomLevel (int level) {
    FontSizer.setFonts(fullPanel, level);
  }

  public ModelElementsPalette getPalette() { return this.elementsPanel; }
  
  // -----------------------------------
  // Implementation of editor
  // -----------------------------------

  public Component getComponent () { return fullPanel; }

  public void setName (String _name) { name = _name; }

  public String getName() { return name; }
  
  public void setFont (Font _font) { 
    for (ModelElementInformation elementInfo : modelElementsList.getElements()) elementInfo.getElement().setFont(_font);
  }

  public void setColor (Color _color) {
    leftTitleBorder.setTitleColor(_color);
    rightTitleBorder.setTitleColor(_color);
  }

  public boolean isChanged() { return modelElementsList.isChanged(); }
  
  public void setChanged(boolean changed) { modelElementsList.setChanged(changed); }
  
  public boolean isActive() { // always active
    return true;
  }

  public void setActive(boolean active) { }

  public boolean isInternal() {
    return advanced;
  }

  public void setInternal(boolean _advanced) {
    advanced = _advanced;
  }
  
  public void clear () {
    elementsPanel.clear();
    modelElementsList.clear(); 
  }

  public void readCompleted() {
    for (ModelElementInformation elementInfo : modelElementsList.getElements()) {
      ModelElement element = elementInfo.getElement();
      if (element instanceof EJSAware) ((EJSAware) element).readCompleted();
    }
  }
  
  public StringBuffer generateCode(int _type, String _info) {
    StringBuffer buffer = new StringBuffer();
    if (_info==null || _info.length()<=0) _info = "";
    else _info=Osejs.getResources().getString(_info);

    for (ModelElementInformation elementInfo : modelElementsList.getElements()) {
      ModelElement element = elementInfo.getElement();
      switch(_type) {
        case Editor.GENERATE_RESOURCES_NEEDED :
          String required = element.getResourcesRequired();
          if (required!=null && required.trim().length()>0) buffer.append(required.trim()+";");
          break;
        case Editor.GENERATE_IMPORT_STATEMENTS :
          String importList = element.getImportStatements();
          if (importList!=null && importList.trim().length()>0) buffer.append(importList.trim()+";");
          break;
        case Editor.GENERATE_RESOURCES_NEEDED_BY_PACKAGE :
          String packageList = element.getPackageList();
          if (packageList!=null && packageList.trim().length()>0) buffer.append(packageList.trim()+";");
          break;
        case Editor.GENERATE_DECLARATION :
          buffer.append("  public " + element.getConstructorName() +" "+elementInfo.getName()+";\n");
          break;
        case Editor.GENERATE_CODE :
          String initCode = element.getInitializationCode(elementInfo.getName());
          if (initCode!=null && initCode.trim().length()>0) {
            StringTokenizer tkn = new StringTokenizer(initCode,"\n"); // Split it into lines
            while (tkn.hasMoreTokens())  buffer.append("    " + tkn.nextToken()+"\n");
          }
          break;
        case Editor.GENERATE_VIEW_READ :
          if (element instanceof AbstractModelElement) {
            String viewReadCode = ((AbstractModelElement) element).getReadFromView(elementInfo.getName());
            if (viewReadCode!=null && viewReadCode.trim().length()>0) {
            StringTokenizer tkn = new StringTokenizer(viewReadCode,"\n"); // Split it into lines
            while (tkn.hasMoreTokens())  buffer.append("    " + tkn.nextToken()+"\n");
            }
          }
          break;
        case Editor.GENERATE_DESTRUCTION :
          String destCode = element.getDestructionCode(elementInfo.getName());
          if (destCode!=null && destCode.trim().length()>0) {
            StringTokenizer tkn = new StringTokenizer(destCode,"\n"); // Split it into lines
            while (tkn.hasMoreTokens())  buffer.append("    " + tkn.nextToken()+"\n");
          }
          break;
//        case Editor.GENERATE_SOURCECODE:
//          if (element instanceof AbstractModelElement) {
//            String sourceCode = ((AbstractModelElement) element).getSourceCode(elementInfo.getName());
//            if (sourceCode!=null && sourceCode.trim().length()>0) {
//              StringTokenizer tkn = new StringTokenizer(sourceCode,"\n"); // Split it into lines
//              while (tkn.hasMoreTokens())  buffer.append("  " + tkn.nextToken()+"\n");
//            }
//            
//          }
//          break;
      }
    }
    return buffer;
  }

  public void readString (String _input) {
    String prefix = name+".Element";
    int prefixLength = prefix.length()+3;
    int begin = _input.indexOf("<"+prefix+">\n");
    while (begin>=0) {
      int end = _input.indexOf("</"+prefix+">\n");
      String piece = _input.substring(begin+prefixLength,end);
      String elementClassname = OsejsCommon.getPiece(piece,"<"+prefix+".Classname>","</"+prefix+".Classname>\n",false);
      String elementName = OsejsCommon.getPiece(piece,"<"+prefix+".Name>","</"+prefix+".Name>\n",false);
      String jarPath = OsejsCommon.getPiece(piece,"<"+prefix+".JarPath>","</"+prefix+".JarPath>\n",false);
      File jarFile = null;
      if (jarPath!=null) jarFile = ejs.getRelativeFile(jarPath);
      ModelElementInformation elementInfo = elementsPanel.instantiateModelElement(elementClassname,jarFile,false);
      if (elementInfo!=null) {
        elementInfo.setName(elementName);
        modelElementsList.addElement(elementInfo, -1);
        String elementXML = OsejsCommon.getPiece(piece,"<"+prefix+".Configuration>\n","</"+prefix+".Configuration>\n",false);
        if (elementXML!=null) elementInfo.getElement().readfromXML(elementXML);
      }
      else {
        System.err.println ("ElementsEditor error! Could not instantiate class "+elementClassname + " from JAR "+jarPath);
      }
      _input = _input.substring(end+prefixLength+1);
      begin = _input.indexOf("<"+prefix+">\n");
    }
    setChanged(false);
  }

  public void fillSimulationXML(SimulationXML _simXML) { // Do nothing, for the moment
    for (ModelElementInformation elementInfo : modelElementsList.getElements()) {
      ModelElement element = elementInfo.getElement();
      String initCode = element.getInitializationCode(elementInfo.getName());
      String importFile = element.getImportStatements();
      if (element instanceof AbstractModelElement) {
        AbstractModelElement fullElement = (AbstractModelElement) element;
        String fullCode = fullElement.getSourceCode(elementInfo.getName());
        _simXML.addModelElement(importFile,initCode,fullCode);
      }
      else {
        _simXML.addModelElement(importFile,"", initCode);
      }
    }    
  }

  public StringBuffer saveStringBuffer () {
    StringBuffer save = new StringBuffer();
    String prefix = name+".Element";
    for (ModelElementInformation elementInfo : modelElementsList.getElements()) {
      ModelElement element = elementInfo.getElement();
      save.append("<"+prefix+">\n");
      save.append("<"+prefix+".Classname>"+element.getClass().getName()+"</"+prefix+".Classname>\n");
      save.append("<"+prefix+".Name>"+elementInfo.getName()+"</"+prefix+".Name>\n");
      if (elementInfo.getJarFile()!=null) {
        String jarPath = ejs.getRelativePath(elementInfo.getJarFile());
        save.append("<"+prefix+".JarPath>"+jarPath+"</"+prefix+".JarPath>\n");
      }
      String configuration = element.savetoXML();
      if (configuration!=null) {
        save.append("<"+prefix+".Configuration>\n");
        save.append(configuration);
        save.append("\n</"+prefix+".Configuration>\n");
      }
      save.append("</"+prefix+">\n");
    }
    return save;
  }

  public List<SearchResult> search(String info, String searchString, int mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    String myInfo = Osejs.getResources().getString("Model.Elements")+": ";
    for (ModelElementInformation oneMI : modelElementsList.getElements()) {
      java.util.List<ModelElementSearch> result = oneMI.getElement().search(myInfo+oneMI.getName(), searchString, mode, oneMI.getName(),modelElementsList); 
      if (result!=null) list.addAll(result);
    }
    return list;
  }

  // -----------------------------------
  // Utility methods
  // -----------------------------------

  /**
   * Checks if the given jar files containg ModelElements. If they do, the user is asked to add them to the palette for this simulation.
   * @param _jarsFound
   */
  public void checkForUserElements(String _jarPath) {
    elementsPanel.addUserDefinedElements(_jarPath);
  }
  
  public void evaluateVariables(Osejs _ejs) { 
    for (ModelElementInformation elementInfo : modelElementsList.getElements()) {
      ModelElement element = elementInfo.getElement();
      _ejs.getModelEditor().getVariablesEditor().checkVariableValue(elementInfo.getName(),"new "+element.getConstructorName()+"()",element.getConstructorName(),"");
    }
  }
  
  /**
   * Selects a given model element
   * @param element
   */
  public String selectElement(ModelElement _element) {
    ejs.showPanel("Model.Elements");
    return modelElementsList.selectElement(_element);
  }
  
  /**
   * Whether this variable name is already in use
   * @param _name
   * @return
   */
    public boolean nameExists(String _name) { return modelElementsList.nameExists(_name); }

}


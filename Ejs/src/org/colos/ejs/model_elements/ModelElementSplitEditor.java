/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last modified: December 2007
 */

package org.colos.ejs.model_elements;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.util.*;

import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.*;

/**
 * An editor with two areas in a split panel
 */
public class ModelElementSplitEditor implements ModelElementMultipageEditor {
  static private final ResourceUtil RES = new ResourceUtil ("Resources");
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));

  // Configuration variables
  private String mName;
  private String mResourcesPrefix;
  private ModelElementMultipageEditor mParentEditor;

  // Implementation variables
  private JSplitPane mSplitPanel;  
  private JPanel mMainPanel; 
  private final ModelElementEditor[] mPageList = new ModelElementEditor[2];
  private ModelElementsCollection mCollection;

  // ----------- Constructor ----------

  /**
   * Creates a split editor with an editor for each name
   */
  public ModelElementSplitEditor (ModelElement element, ModelElementMultipageEditor parentEditor, String resourcesPrefix, String name, String topEditorName, String bottomEditorName) {
    mName = name;
    mResourcesPrefix = resourcesPrefix+".";
    mParentEditor = parentEditor;
    mPageList[0] = new ModelElementEditor(element,this,false);
    mPageList[0].setName(topEditorName);
    mPageList[1] = new ModelElementEditor(element,this,true);
    mPageList[1].setName(bottomEditorName);
  }
  
  public JComponent getComponent (final ModelElementsCollection _collection, Component topComponent) {
    if (mMainPanel!=null) return mMainPanel;

    // First time creation
    mCollection = _collection;
    mSplitPanel = new JSplitPane (JSplitPane.VERTICAL_SPLIT);
    mSplitPanel.setForeground(COLOR);
    mSplitPanel.setOneTouchExpandable(true);
    mSplitPanel.setResizeWeight(0.5);

    Font font = InterfaceUtils.font(null, RES.getString("Editor.TitleFont"));
    
    JComponent topComp = mPageList[0].getComponent(mCollection);
    TitledBorder topBorder = new TitledBorder(RES.getString(mResourcesPrefix+mPageList[0].getName()));
    topBorder.setTitleFont(font);
    topBorder.setTitleColor(COLOR);
    topComp.setBorder(topBorder);
    mSplitPanel.setTopComponent(topComp);

    JComponent botComp = mPageList[1].getComponent(mCollection);
    TitledBorder botBorder = new TitledBorder(RES.getString(mResourcesPrefix+mPageList[1].getName()));
    botBorder.setTitleFont(font);
    botBorder.setTitleColor(COLOR);
    botComp.setBorder(botBorder);
    mSplitPanel.setBottomComponent(botComp);

    mMainPanel = new JPanel (new BorderLayout());
    mMainPanel.add(mSplitPanel, BorderLayout.CENTER);
    if (topComponent!=null) mMainPanel.add(topComponent, BorderLayout.NORTH);
    
    mSplitPanel.validate();

    return mMainPanel;
  }

  // ----------- Utility methods ----------

  public ModelElementEditor getTopEditor() { return mPageList[0]; }

  public ModelElementEditor getBottomEditor() { return mPageList[1]; }

  public java.util.List<ModelElementSearch> search (String _info, String _searchString, int _mode, String name, ModelElementsCollection _collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    for (ModelElementEditor editor : mPageList) list.addAll (editor.search(mName,_searchString, _mode, name, _collection));
    return list;
  }

  public void setFont (Font font) {
    for (ModelElementEditor editor : mPageList) editor.setFont (font);
  }

  public void setEditable (boolean editable) {
    for (ModelElementEditor editor : mPageList) editor.setEditable (editable);
  }
  
  public StringBuffer saveStringBuffer () {
    StringBuffer buffer = new StringBuffer();
    for (ModelElementEditor editor : mPageList) {
      buffer.append("<"+mName+".Page>\n");
      buffer.append("<Name>"+editor.getName()+"</Name>\n");
      buffer.append("<Active>"+editor.isEditable()+"</Active>\n");
      buffer.append("<Content>\n");
      buffer.append(editor.saveStringBuffer());
      buffer.append("</Content>\n");
      buffer.append("</"+mName+".Page>\n");
    }
    return buffer;
  }

  public void readPlainCode(String topCode, String bottomCode) {
    getTopEditor().readPlainCode(topCode);  
    getBottomEditor().readPlainCode(bottomCode);  
  }
  
  public void readXmlString(String _xmlCode) {
    if (_xmlCode==null) return;
    int begin = _xmlCode.indexOf("<"+mName+".Page>\n");
    while (begin>=0) {
      int end = _xmlCode.indexOf("</"+mName+".Page>\n");
      String piece = _xmlCode.substring(begin+mName.length()+8,end);
      String name = OsejsCommon.getPiece(piece,"<Name>","</Name>\n",false);
      for (ModelElementEditor editor : mPageList) {
        if (editor.getName().equals(name)) { // read the rest
          boolean editable=true;
          if (OsejsCommon.getPiece(piece,"<Active>","</Active>\n",false).toLowerCase().equals("false")) editable = false;
          editor.setEditable(editable);
          editor.readXmlString(OsejsCommon.getPiece(piece,"<Content>\n","</Content>\n",false));
        }
      }
      // Next one
      _xmlCode = _xmlCode.substring(end+mName.length()+9);
      begin = _xmlCode.indexOf("<"+mName+".Page>\n");
    }
  }
  
  public void showPanel(ModelElementsCollection collection, String elementName, String keyword) {
    if (mParentEditor!=null) mParentEditor.showPanel(collection,elementName,mName);
    if (mMainPanel==null) getComponent(collection,null); // Make sure the editor is created before requesting display
    //mSplitPanel.setDividerLocation(0.5);
  }

  public void addPragma(String pragma, String prefix, String suffix) {
    TwoStrings pragmaTexts = new TwoStrings(prefix,suffix);
    for (ModelElementEditor editor : mPageList) editor.addPragma(pragma, pragmaTexts);
  }

} // end of class


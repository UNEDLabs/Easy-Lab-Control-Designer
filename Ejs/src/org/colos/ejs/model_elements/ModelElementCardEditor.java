/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last modified: December 2007
 */

package org.colos.ejs.model_elements;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import java.util.*;

import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.*;

/**
 * An editor with several editors in a CardLayout
 */
public class ModelElementCardEditor implements ModelElementMultipageEditor {
  static private final ResourceUtil RES = new ResourceUtil ("Resources");
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));

  // Configuration variables
  private String mName;
  private String mResourcesPrefix;
  private ModelElementMultipageEditor mParentEditor;

  // Implementation variables
  private JRadioButton[] mButtons;
  private CardLayout mCardLayout;
  private JPanel mCardPanel;
  private JPanel mMainPanel;
  private final ModelElementEditor[] mPageList;

  // ----------- Constructor ----------

  /**
   * Creates a split editor with an editor for each name
   */
  public ModelElementCardEditor (ModelElement element, ModelElementMultipageEditor parentEditor, String resourcesPrefix, String name, String[] pageNames) {
    mName = name;
    mResourcesPrefix = resourcesPrefix+".";
    mParentEditor = parentEditor;
    mPageList = new ModelElementEditor[pageNames.length];
    for (int i=0; i<pageNames.length; i++) {
      mPageList[i] = new ModelElementEditor(element,this);
      mPageList[i].setName(pageNames[i]);
    }
  }
  
  public JComponent getComponent (final ModelElementsCollection collection, Component[] topComponents) {
    if (mMainPanel!=null) return mMainPanel;

    // First time creation
    mCardLayout = new CardLayout ();
    mCardPanel = new JPanel(mCardLayout);

    ActionListener al = new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        mCardLayout.show (mCardPanel,evt.getActionCommand());
      }
    };
    Border buttonsBorder = BorderFactory.createEmptyBorder(0,6,0,6);
    Box toolbar = Box.createHorizontalBox();
    Insets inset = new java.awt.Insets(1,3,0,3);

    Font font = InterfaceUtils.font(null,Osejs.getResources().getString("Model.TitleFont"));

    String[] keywords = new String[mPageList.length];
    for (int i=0; i<mPageList.length; i++) keywords[i] = mPageList[i].getName();
    
    mButtons = MenuUtils.createRadioGroup (keywords,mResourcesPrefix,al,false);
    for (int i=0; i<mButtons.length; i++) {
      mButtons[i].setBorder(buttonsBorder);
      mButtons[i].setFont(font);
      mButtons[i].setForeground(COLOR);
      mButtons[i].setMargin(inset);
      toolbar.add (mButtons[i]);
      
      JPanel cardPanel = new JPanel (new BorderLayout());
      cardPanel.add(mPageList[i].getComponent(collection), BorderLayout.CENTER);
      if (topComponents!=null && topComponents[i]!=null) cardPanel.add(topComponents[i], BorderLayout.NORTH);
      
      mCardPanel.add (cardPanel,keywords[i]);
    }

    mCardLayout.show (mCardPanel,keywords[0]);
    mButtons[0].setSelected(true);

    mMainPanel = new JPanel(new BorderLayout());
    mMainPanel.setPreferredSize(new Dimension(600,500));
    mMainPanel.add(toolbar,BorderLayout.NORTH);
    mMainPanel.add(mCardPanel,BorderLayout.CENTER);
    
    mMainPanel.setBorder(new LineBorder(Color.BLACK));

    return mMainPanel;
  }

  // ----------- Utility methods ----------

  public ModelElementEditor getEditor(String name) { 
    for (ModelElementEditor editor : mPageList) if (editor.getName().equals(name)) return editor;
    return null;
  }

  public ModelElementEditor getEditor(int index) { 
    return mPageList[index];
  }

  public void showEditor(int index) { 
    mCardLayout.show (mCardPanel,mPageList[index].getName());
    mButtons[index].setSelected(true);
  }
  
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

  public void readPlainCode(String[] code) {
    for (int i=0,n=Math.min(code.length, mPageList.length); i<n; i++) {
      mPageList[i].readPlainCode(code[i]);
    }
  }
  
  public void readXmlString(String _xmlCode) {
    if (_xmlCode==null) return;
    int begin = _xmlCode.indexOf("<"+mName+".Page>\n");
    while (begin>=0) {
      int end = _xmlCode.indexOf("</"+mName+".Page>\n");
      String piece = _xmlCode.substring(begin+mName.length()+8,end);
      String name = OsejsCommon.getPiece(piece,"<Name>","</Name>\n",false);
      ModelElementEditor editor = getEditor(name);
      if (editor!=null) { // read the rest
        boolean editable=true;
        if (OsejsCommon.getPiece(piece,"<Active>","</Active>\n",false).toLowerCase().equals("false")) editable = false;
        editor.setEditable(editable);
        editor.readXmlString(OsejsCommon.getPiece(piece,"<Content>\n","</Content>\n",false));
      }
      // Next one
      _xmlCode = _xmlCode.substring(end+mName.length()+9);
      begin = _xmlCode.indexOf("<"+mName+".Page>\n");
    }
  }
  
  public void showPanel(ModelElementsCollection collection, String elementName, String keyword) {
    if (mParentEditor!=null) mParentEditor.showPanel(collection,elementName,mName);
    if (mMainPanel==null) getComponent(collection,null); // Make sure the editor is created before requesting display
    for (int i=0; i<mPageList.length; i++) {
      if (mPageList[i].getName().equals(keyword)) {
        mCardLayout.show (mCardPanel,keyword);
        mButtons[i].setSelected(true);
        return;
      }
    }
  }

  public void addPragma(String pragma, String prefix, String suffix) {
    TwoStrings pragmaTexts = new TwoStrings(prefix,suffix);
    for (ModelElementEditor editor : mPageList) editor.addPragma(pragma, pragmaTexts);
  }

} // end of class


/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last modified: December 2007
 */

package org.colos.ejs.model_elements;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;
import java.util.Map.Entry;

import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.*;
import org.opensourcephysics.display.OSPRuntime;

/**
 * An editor with multiple pages in tabs
 */
public class ModelElementTabbedEditor implements ModelElementMultipageEditor {
  static private final ResourceUtil RES = new ResourceUtil ("Resources");
  static private final ResourceUtil SYSRES = new ResourceUtil ("SystemResources");
  static private final Color COLOR = InterfaceUtils.color(RES.getString("Model.Color"));
  static private final String TOOLTIP = RES.getString("TabbedEditor.MenuTooltip");
  static private final float BUTTON_FONT_SIZE = 22.0f;

  // Configuration variables
  private String mName;
  private Font mFont = InterfaceUtils.font(null,RES.getString("Osejs.DefaultFont"));
  private ModelElement mModelElement;
  private ModelElementMultipageEditor mParentEditor;
  private Hashtable<String,TwoStrings> mPragmas = new Hashtable<String,TwoStrings>(); 

  // Implementation variables
  private JTabbedPane mTabbedPanel;  
  private JPanel mMainPanel;
  private CardLayout mCardLayout;
  private ArrayList<ModelElementEditor> mPageList= new ArrayList<ModelElementEditor>();
  private ModelElementsCollection mCollection;

  // ----------- Constructor ----------

  public ModelElementTabbedEditor (ModelElement _element, ModelElementMultipageEditor _parentEditor, String _name) {
    mModelElement = _element;
    mParentEditor = _parentEditor;
    mName = _name;
  }
  
  /**
   * Adds an editor to the list at the given index
   * @param name
   * @param xmlCode
   * @param editable
   * @param index
   * @return
   */
  protected ModelElementEditor addEditor (String name, String xmlCode, boolean editable, int index) {
    ModelElementEditor editor = new ModelElementEditor (mModelElement, this);
    for (Entry<String,TwoStrings> pragmaEntry : mPragmas.entrySet()) editor.addPragma(pragmaEntry.getKey(), pragmaEntry.getValue());
    editor.setName(name);
    editor.setEditable(editable);
    editor.readXmlString(xmlCode);
    if (index==-1) mPageList.add (editor);
    else mPageList.add (index+1,editor);
    return editor;
  }
  

  /**
   * Creates the panel that appears when there are no pages
   * @return JPanel
   */
  protected JPanel createFirstPanel () {
    String label = RES.getOptionalString("TabbedEditor.ClickHere."+mName);
    if (label==null) label = RES.getString("TabbedEditor.ClickHere");
    JLabel firstPageLabel = new JLabel (label);
    firstPageLabel.setFont (firstPageLabel.getFont().deriveFont(ModelElementTabbedEditor.BUTTON_FONT_SIZE));
    firstPageLabel.setForeground (COLOR);

    JPanel firstButtonPanel = new JPanel (new GridBagLayout());
    firstButtonPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    firstButtonPanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked (MouseEvent _evt) {
        if (OSPRuntime.isPopupTrigger(_evt)) return;
        Object obj = JOptionPane.showInputDialog(mMainPanel,RES.getString("TabbedEditor.NewName"),RES.getString("TabbedEditor.Rename"),
          JOptionPane.QUESTION_MESSAGE,null,null,getUniqueName (mName));
        if (obj==null) return;
        String name = obj.toString().trim();
        if (name.length()<=0) name = mName;   
        String xmlCode = getDefaultCode();
        addPage (addEditor (name, xmlCode, true, -1));
      }
    });
    firstButtonPanel.add(firstPageLabel);

    JPanel firstPanel = new JPanel (new GridLayout(0,1));
    firstPanel.add (firstButtonPanel);
    return firstPanel;
  }
  
  protected String getDefaultCode() {
    return SYSRES.getOptionalString("TabbedEditor.DefaultXMLCode."+mName);
  }

  public JComponent getComponent (final ModelElementsCollection _collection) {
    if (mMainPanel!=null) return mMainPanel;

    // First time creation
    mCollection = _collection;

    ActionListener listener = new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        String aCmd=_evt.getActionCommand();
        if (aCmd.equals("addPage")) {
          Object obj = JOptionPane.showInputDialog(mMainPanel,RES.getString("TabbedEditor.NewName"),RES.getString("TabbedEditor.Rename"),
            JOptionPane.QUESTION_MESSAGE,null,null,getUniqueName (mName));
          if (obj==null) return;
          String name = obj.toString().trim();
          if (name.length()<=0) name = mName;
          String xmlCode = getDefaultCode();
          addPage (addEditor (getUniqueName (name), xmlCode, true, mTabbedPanel.getSelectedIndex()));
        }
        else if (aCmd.equals("upPage"))     moveUpAndDownPage (true);
        else if (aCmd.equals("dnPage"))     moveUpAndDownPage (false);
        else if (aCmd.equals("copyPage"))   copyPage ();
        else if (aCmd.equals("renamePage")) {
          Object obj = JOptionPane.showInputDialog(mMainPanel, RES.getString("TabbedEditor.NewName"),RES.getString("TabbedEditor.Rename"),
            JOptionPane.QUESTION_MESSAGE,null,null,getCurrentPageName());
          if (obj==null) return;
          String txt = obj.toString().trim();
          if (txt.length()>0) renameCurrentPage (txt);
        }
        else if (aCmd.equals("togglePage")) toggleCurrentPage ();
        else if (aCmd.equals("removePage")) removeCurrentPage ();
      }
    };

    Font font = InterfaceUtils.font(null,RES.getString("Editor.TitleFont"));

    final PopupMenu popupMenu = new PopupMenu ();
    createMenuItem (popupMenu,"addPage",listener, font);
    createMenuItem (popupMenu,"copyPage",listener, font);
    createMenuItem (popupMenu,"upPage",listener, font);
    createMenuItem (popupMenu,"dnPage",listener, font);
    createMenuItem (popupMenu,"renamePage",listener, font);
    popupMenu.addSeparator();
    createMenuItem (popupMenu,"togglePage",listener, font);
    createMenuItem (popupMenu,"removePage",listener, font);

    mTabbedPanel = new JTabbedPane ();
    mTabbedPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    mTabbedPanel.add (popupMenu);
    mTabbedPanel.setForeground(COLOR);
    //mTabbedPanel.setPreferredSize (RES.getDimension("TabbedEditor.PreferredSize"));
    mTabbedPanel.addMouseListener (new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (OSPRuntime.isPopupTrigger(evt)) //SwingUtilities.isRightMouseButton(evt)) 
          popupMenu.show (evt.getComponent(),evt.getX(),evt.getY());
      }
    });
    for (ModelElementEditor editor : mPageList) mTabbedPanel.addTab(editor.getName(), editor.getComponent(mCollection));
    
    mCardLayout = new CardLayout ();
    mMainPanel = new JPanel (mCardLayout);
    mMainPanel.add (mTabbedPanel,"TabbedPanel");
    mMainPanel.add (createFirstPanel(),"FirstPanel");
//    setFont (mFinalPanel.getFont());

    if (mTabbedPanel.getTabCount()<=0) mCardLayout.show (mMainPanel,"FirstPanel");
    else {
      mCardLayout.show (mMainPanel,"TabbedPanel");
      mTabbedPanel.setSelectedIndex (0);
    }
    
    return mMainPanel;
  }

  // ----------- Utility methods ----------

  /**
   * Returns the number of active pages of this TabbedEditor
   */
  public int getActivePageCount() {
    int counter=0;
    for (ModelElementEditor editor : mPageList) if (editor.isEditable()) counter++;
    return counter;
  }

  public java.util.List<ModelElementEditor> getEditorList() { return mPageList; }
  
  public java.util.List<ModelElementSearch> search (String _info, String _searchString, int _mode, String name, ModelElementsCollection _collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    for (ModelElementEditor editor : mPageList) list.addAll (editor.search(mName,_searchString, _mode, name, _collection));
    return list;
  }

  public void setFont (Font _font) {
    mFont = _font;
    for (ModelElementEditor editor : mPageList) editor.setFont (_font);
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

  public void readXmlString(String _xmlCode) {
    mPageList.clear();
    if (mMainPanel!=null) mMainPanel.removeAll();
    if (_xmlCode==null) return;
    int begin = _xmlCode.indexOf("<"+mName+".Page>\n");
    while (begin>=0) {
      int end = _xmlCode.indexOf("</"+mName+".Page>\n");
      String piece = _xmlCode.substring(begin+mName.length()+8,end);
      boolean editable=true;
      if (OsejsCommon.getPiece(piece,"<Active>","</Active>\n",false).toLowerCase().equals("false")) editable = false;
      ModelElementEditor editor = addEditor(OsejsCommon.getPiece(piece,"<Name>","</Name>\n",false), OsejsCommon.getPiece(piece,"<Content>\n","</Content>\n",false),editable,-1);
      if (mMainPanel!=null) addPage(editor);
      // Next one
      _xmlCode = _xmlCode.substring(end+mName.length()+9);
      begin = _xmlCode.indexOf("<"+mName+".Page>\n");
    }
    if (mMainPanel!=null) mMainPanel.repaint();
  }
  
  public void showPanel(ModelElementsCollection collection, String elementName, String keyword) {
    if (mMainPanel==null) getComponent(collection); // Make sure the editor is created before requesting display
    System.out.println ("Searching for page "+keyword);
    for (int i=0, n= mTabbedPanel.getComponentCount(); i<n; i++) {
      if (mTabbedPanel.getTitleAt(i).equals(keyword)) {
       System.out.println ("Found editor "+mPageList.get(i).getName()+ " at i="+i);
        mCardLayout.show (mMainPanel,"TabbedPanel");
        mTabbedPanel.setSelectedIndex (i);
        if (mParentEditor!=null) mParentEditor.showPanel(collection,elementName,mName);
        return;
      }
    }
  }

  /**
   * Pragmas are used to embed code into special constructions.
   * The editor will replace code in between pragmas:
   * % begin pragma
   * ...code here...
   * % end
   * with the constructions:
   * prefix
   * "  " ...code here...
   * suffix
   * Pragmas are case insensitive, as are the keywords 'begin' and 'end'.
   * Pragmas cannot be annidated.
   * @param pragma
   * @param prefix
   * @param suffix
   */
 public void addPragma(String pragma, String prefix, String suffix) {
    addPragma(pragma, new TwoStrings(prefix,suffix));
 }

 /**
  * Shorter form to add a pragma
  * @param pragma
  * @param texts
  */
 public void addPragma(String pragma, TwoStrings texts) {
    mPragmas.put(pragma.toLowerCase(), texts);
    for (ModelElementEditor editor : mPageList) editor.addPragma(pragma, texts);
  }
  
// ------ private methods

  protected final MenuItem createMenuItem (PopupMenu _popupMenu, String _key, ActionListener _al, Font _font) {
    MenuItem mi = new MenuItem (RES.getString("TabbedEditor."+_key));
    mi.setActionCommand (_key);
    mi.addActionListener (_al);
    _popupMenu.add (mi);
    mi.setFont (_font);
    return mi;
  }

  private void moveUpAndDownPage (boolean _up) {
    int index = mTabbedPanel.getSelectedIndex();
    if (index<0) return;
    ModelElementEditor page = mPageList.get(index);
    if (page==null) return;
    if (_up) {
      if (index==0) return;
      mPageList.remove (index);
      mPageList.add(index-1,page);
      mTabbedPanel.removeTabAt(index);
      mTabbedPanel.insertTab (page.isEditable() ? page.getName() : page.getName()+" (D)",null,page.getComponent(mCollection),TOOLTIP,index-1);
    }
    else {
      if (index==(mPageList.size()-1)) return;
      mPageList.remove (index);
      mPageList.add (index+1,page);
      mTabbedPanel.removeTabAt(index);
      mTabbedPanel.insertTab (page.isEditable() ? page.getName() : page.getName()+" (D)",null,page.getComponent(mCollection),TOOLTIP,index+1);
    }
    mTabbedPanel.setSelectedComponent (page.getComponent(mCollection));
    mCollection.reportChange(mModelElement);
    }

  private void copyPage () {
    int index = mTabbedPanel.getSelectedIndex();
    if (index<0) return;
    ModelElementEditor page = mPageList.get(index);
    if (page==null) return;
    addPage(addEditor(getUniqueName(page.getName()), page.saveStringBuffer().toString(), page.isEditable(), index+1));
  }

  private void toggleCurrentPage () {
    int index = mTabbedPanel.getSelectedIndex();
    if (index<0) return;
    ModelElementEditor page = mPageList.get (index);
    if (page.isEditable()) {
      page.setEditable (false);
      mTabbedPanel.setTitleAt (index, page.getName()+" (D)");
    }
    else {
      page.setEditable (true);
      mTabbedPanel.setTitleAt (index, page.getName());
    }
    mCollection.reportChange(mModelElement);
//    mCollection.getEJS().getModelEditor().getVariablesEditor().updateControlValues (false);
  }

  protected void removeCurrentPage () {
    int index = mTabbedPanel.getSelectedIndex();
    if (index<0) return;
    mTabbedPanel.removeTabAt (index);
    mPageList.remove (index);
    mCollection.reportChange(mModelElement);
    if (mTabbedPanel.getTabCount()<=0) mCardLayout.show (mMainPanel,"FirstPanel");
//  mCollection.getEJS().getModelEditor().getVariablesEditor().updateControlValues (false);
  }

  private String getCurrentPageName () {
    int index = mTabbedPanel.getSelectedIndex();
    if (index<0) return "";
    return mPageList.get(index).getName();
  }

  private void renameCurrentPage (String _name) {
    int index = mTabbedPanel.getSelectedIndex();
    if (index<0) return;
    _name = getUniqueName (_name); //Gonzalo 070128
    mTabbedPanel.setTitleAt (index,_name);
    ModelElementEditor page = mPageList.get(index);
    page.setName(_name);
    if (!page.isEditable()) mTabbedPanel.setTitleAt (index, page.getName()+" (D)");
    mCollection.reportChange(mModelElement);
  }

  private boolean nameExists (String _name) {
    for (ModelElementEditor editor : mPageList) if (_name.equals (editor.getName())) return true; 
    return false;
  }

  protected final String getUniqueName (String _name) {
    String newname = new String (_name.trim());
    int i=1;
    while (nameExists (newname)) newname = _name + " " + (++i);
    return newname;
  }

  private void addPage (ModelElementEditor editor) {
    mCardLayout.show (mMainPanel,"TabbedPanel");
    Component component = editor.getComponent(mCollection); // This creates the component
    editor.setFont  (mFont);
    int index = mPageList.indexOf(editor);
    mTabbedPanel.insertTab (editor.isEditable() ? editor.getName() : editor.getName()+" (D)",null,component,TOOLTIP,index);
    mTabbedPanel.setSelectedIndex(index);
    mTabbedPanel.repaint();
    mCollection.reportChange(mModelElement);
  }

} // end of class


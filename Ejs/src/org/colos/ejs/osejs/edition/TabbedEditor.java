/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last modified: December 2007
 */

package org.colos.ejs.osejs.edition;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;

import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.ode_editor.EquationEditor;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejss.xml.SimulationXML;
//import org.colos.ejs.osejs.edition.ode_editor.*;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;

/**
 * An Editor that shows its pages in a tabbed panel
 */
public class TabbedEditor implements Editor {
  static protected final ResourceUtil res = new ResourceUtil ("Resources");
  static private final String tooltip = res.getString("TabbedEditor.MenuTooltip");
  static protected final float BUTTON_FONT_SIZE = 22.0f;
  static public final Color ERROR_COLOR = InterfaceUtils.color(res.getString("EditorForVariables.ErrorColor"));

  protected org.colos.ejs.osejs.Osejs ejs;
  protected String defaultType, defaultString, defaultHeader;
  protected JPanel firstButtonPanel;
  protected JPanel finalPanel;
  protected JTabbedPane tabbedPanel;
  protected Vector<Editor> pageList= new Vector<Editor>();
  protected MenuItem addPageMI;
  protected Font myFont;
  protected Color myColor;

  private boolean changed=false, activeEditor=true;
  private String name="Unnamed", contentDelim="Content";
  private CardLayout cardLayout;
  private JLabel firstButton;
  private MenuItem copyPage, removePage, renamePage, upPage, dnPage, togglePage;
  private PopupMenu popupMenu;
  private JTextField pageCounterField = null;
  private JComponent pageCounterButton = null;
  private Color pageCounterFieldDefaultColor, pageCounterFieldColor = Color.RED, pageCounterButtonDefaultForeground;
  protected Window parentWindow;

  /**
   * Creates a new page of the given type with the given code
   */
  protected Editor createPage (String _type, String _name, String _code) {
    Editor page = new CodeEditor (ejs, this);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }
  
  public JTabbedPane getTabbedPane() { return this.tabbedPanel; }

  /**
   * Return the current size of the edition area
   * @return
   */
  public Dimension getCurrentPanelSize () { return this.firstButtonPanel.getSize(); }
  
  /**
   * Returns the type of a page. Used for copying
   */
  protected String typeOfPage (Editor page) { return defaultType; }

  /**
   * Creates custom menu items that go at the top of the pages popup menu
   * @param actionListener ActionListener
   */
  protected void customMenuItems (ActionListener actionListener) {
    addPageMI = createMenuItem ("addPage",defaultHeader,actionListener);
  }

  /**
   * Creates the panel that appears when there are no pages
   * @return JPanel
   */
  protected JPanel createFirstPanel () {
    firstButtonPanel = new JPanel (new GridBagLayout());
    firstButtonPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    firstButtonPanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked (MouseEvent _evt) {
        if (OSPRuntime.isPopupTrigger(_evt)) return; //SwingUtilities.isRightMouseButton(_evt)) return;
        if (!firstButtonPanel.isEnabled()) return;
        Object obj = JOptionPane.showInputDialog(getComponent(),
          res.getString("TabbedEditor.NewName"),res.getString("TabbedEditor.Rename"),
          JOptionPane.QUESTION_MESSAGE,null,null,getUniqueName (defaultString));
        if (obj==null) return;
        String txt = obj.toString().trim();
        if (txt.length()>0) addPage (defaultType,txt,null,true,false);
        else addPage (defaultType,defaultString, null,true,false);
      }
    });
    String label = res.getOptionalString("TabbedEditor.ClickHere."+defaultHeader);
    if (label==null) label = res.getString("TabbedEditor.ClickHere");
    firstButton = new JLabel (label);
    firstButton.setFont (firstButton.getFont().deriveFont(TabbedEditor.BUTTON_FONT_SIZE));
    firstButtonPanel.add(firstButton);

    JPanel firstPanel = new JPanel (new GridLayout(0,1));
    firstPanel.add (firstButtonPanel);
    return firstPanel;
  }

  /**
   * Shows the popup menu
   */
  protected void showMenu (Component comp, int x, int y) { 
    if (activeEditor) popupMenu.show (comp,x,y); 
  }

 /**
  * Placeholder for extra menu entries
  */
 protected void addExtraMenuEntries() {}
 
  // ----------- Constructor ----------

  public TabbedEditor (org.colos.ejs.osejs.Osejs _ejs, String _type, String _header) {
    this(_ejs, _type, _header, true);
  }

  public TabbedEditor (org.colos.ejs.osejs.Osejs _ejs, String _type, String _header, boolean canDisablePages) {
    ejs = _ejs;
    defaultType = _type;
    defaultHeader = _header;
    defaultString = new String(res.getString(defaultHeader+".Page"));

    MyActionListener al = new MyActionListener();

    popupMenu = new PopupMenu ();
    customMenuItems (al); // Creates the top menu items
    // common menu items
    copyPage    = createMenuItem ("copyPage",defaultHeader,al);
    upPage      = createMenuItem ("upPage",defaultHeader,al);
    dnPage      = createMenuItem ("dnPage",defaultHeader,al);
    renamePage  = createMenuItem ("renamePage",defaultHeader,al);
    popupMenu.addSeparator();
    addExtraMenuEntries();
    if (canDisablePages) togglePage  = createMenuItem ("togglePage",defaultHeader,al);
    removePage  = createMenuItem ("removePage",defaultHeader,al);

    JPanel firstPanel = createFirstPanel();

    tabbedPanel = new JTabbedPane ();
    tabbedPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPanel.add (popupMenu);
    //tabbedPanel.setPreferredSize (res.getDimension("TabbedEditor.PreferredSize"));
    tabbedPanel.addMouseListener (new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (OSPRuntime.isPopupTrigger(evt)) //SwingUtilities.isRightMouseButton(evt)) 
          showMenu (evt.getComponent(),evt.getX(),evt.getY());
      }
    });
    cardLayout = new CardLayout ();
    finalPanel = new JPanel (cardLayout);
    finalPanel.add (tabbedPanel,"TabbedPanel");
    finalPanel.add (firstPanel ,"FirstPanel");
    setFont (finalPanel.getFont());

    Font font = InterfaceUtils.font(null,res.getString("Editor.TitleFont"));
    addPageMI.setFont (font);
    copyPage.setFont (font);
    upPage.setFont (font);
    dnPage.setFont (font);
    if (togglePage!=null) togglePage.setFont (font);
    removePage.setFont (font);
    renamePage.setFont (font);
    myFont = font.deriveFont(Font.PLAIN);
    showFirstPage();
  }
  
  public void setZoomLevel (int level) {
    FontSizer.setFonts(finalPanel, level);
    FontSizer.setFonts(popupMenu, level);
  }



  // ----------- Constructor ----------

  /**
   * Returns the pages of this TabbedEditor
   */
    public java.util.Vector<Editor> getPages() { return pageList; }

   /**
    * Returns the pages of this TabbedEditor
    */
    public java.util.Enumeration<Editor> getPageEnumeration() { return pageList.elements(); }

   /**
    * Returns the number of active pages of this TabbedEditor
    */
    public int getActivePageCount() {
      int counter=0;
      for (java.util.Enumeration<Editor> e=pageList.elements(); e.hasMoreElements();)
        if (e.nextElement().isActive()) counter++;
      return counter;
    }

    public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
      String passName = getName();
      if (passName.startsWith("Osejs.")) passName = passName.substring(6);
      java.util.List<SearchResult> list = new ArrayList<SearchResult>();
      for (java.util.Enumeration<Editor> e=pageList.elements(); e.hasMoreElements();)
        list.addAll (e.nextElement().search(passName, _searchString,_mode));
      return list;
    }

    /**
     * Sets a text field where the number of pages will be displayed
     */
    public void setPageCounterField (JTextField _field, Color _color, JComponent _button) {
      pageCounterField = _field;
      pageCounterFieldDefaultColor = _field.getBackground();
      pageCounterFieldColor = _color;
      this.pageCounterButton = _button;
      this.pageCounterButtonDefaultForeground = _button.getForeground();
    }

    public void updatePageCounterField (int n) {
      if (pageCounterField==null) return;
      if (n==0) {
        pageCounterField.setBackground(pageCounterFieldDefaultColor);
        pageCounterField.setForeground(Color.BLACK);
        pageCounterButton.setForeground(pageCounterButtonDefaultForeground);
      }
      else {
        pageCounterField.setBackground(pageCounterFieldColor);
        pageCounterField.setForeground(Color.WHITE);
        pageCounterButton.setForeground(pageCounterFieldColor);
      }
      pageCounterField.setText(""+n);
    }

// ------------- Implementation of interface Editor

  public void setName (String _name) { name = _name; }

  public String getName() { return name; }

  public void clear() {
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements();) e.nextElement().clear ();
    pageList.clear();
    tabbedPanel.removeAll();
    showFirstPage();
    updatePageCounterField(pageList.size());
    changed = false;
  }

  public Component getComponent () { return finalPanel; }

  public void setColor (Color _color) {
    myColor = _color;
    tabbedPanel.setForeground (_color);
    firstButton.setForeground (_color);
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements();) e.nextElement().setColor (_color);
  }

  public void setFont (Font _font) {
    myFont = _font;
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements();) e.nextElement().setFont (_font);
  }

  public boolean isChanged () {
    if (changed) return true;
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements();) if (e.nextElement().isChanged()) return true;
    return false;
  }

  public void setChanged (boolean _ch) {
    changed = _ch;
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements();) e.nextElement().setChanged (_ch);
  }

  public boolean isActive () { return activeEditor;}

  public void setActive (boolean _active) { 
    activeEditor = _active;
    changed = true;
    firstButtonPanel.setEnabled(_active);
    firstButton.setEnabled(_active);
  }

  public boolean isInternal() {
    return false;
  }

  public void setInternal(boolean _advanced) {  }
  
  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    String genName,passName;
    int index = name.lastIndexOf('.');
    if (index>=0) genName = name.substring (index+1).toLowerCase();
    else genName = name.toLowerCase();
    if (_info!=null && _info.trim().length()>0) passName = _info;
    else {
      passName = getName();
      if (passName.startsWith("Osejs.")) passName = passName.substring(6);
    }
    index=0;
    for (Editor editor : pageList) {
      if (editor instanceof CodeEditor) {
        index++;
        ((CodeEditor) editor).setCodeName(genName+index);
      }
      else if (editor instanceof EquationEditor) {
        index++;
        ((EquationEditor) editor).setCodeName(genName+index);
      }
      //if (editor.isActive()) 
        code.append(editor.generateCode (_type,passName));
    }
    return code;
  }

  // Content within content requires this method
  public void setContentDelim (String txt) { contentDelim = txt; }

  public void fillSimulationXML(SimulationXML _simXML) {
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements();) {
      Editor page = e.nextElement();
//      if (page.isActive() && !page.isInternal()) page.fillSimulationXML(_simXML);
      if (!page.isInternal()) page.fillSimulationXML(_simXML);
    }
  }

  public StringBuffer saveStringBuffer () {
    StringBuffer save = new StringBuffer();
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements();) {
      Editor page = e.nextElement();
      save.append("<"+name+".Page>\n"+
              "<Type>"+typeOfPage(page)+"</Type>\n"+
              "<Name>"+page.getName()+"</Name>\n"+
              "<Active>"+page.isActive()+"</Active>\n"+
              "<Internal>"+page.isInternal()+"</Internal>\n"+
              "<"+contentDelim+">\n");
      save.append(page.saveStringBuffer());
      save.append("</"+contentDelim+">\n"+
                  "</"+name+".Page>\n");
    }
    return save;
  }

  public void readString (String _input) {
    int begin = _input.indexOf("<"+name+".Page>\n");
    while (begin>=0) {
      int end = _input.indexOf("</"+name+".Page>\n");
      String piece = _input.substring(begin+name.length()+8,end);
      boolean act=true;
      if (OsejsCommon.getPiece(piece,"<Active>","</Active>\n",false).toLowerCase().equals("false")) act = false;
      boolean internal = false;
      String advancedStr = OsejsCommon.getPiece(piece,"<Internal>","</Internal>\n",false);
      if (advancedStr!=null && advancedStr.toLowerCase().equals("true")) internal = true;
      addPage (OsejsCommon.getPiece(piece,"<Type>","</Type>\n",false),
               OsejsCommon.getPiece(piece,"<Name>","</Name>\n",false),
               OsejsCommon.getPiece(piece,"<"+contentDelim+">\n","</"+contentDelim+">\n",false),
               act,internal);
      _input = _input.substring(end+name.length()+9);
      begin = _input.indexOf("<"+name+".Page>\n");
    }
    showFirstPage();
    setChanged(false);
  }

// ------ End of Implementation of Editor Interface

  public void showPage (Editor anEditor) {
    for (int i=0, n= tabbedPanel.getComponentCount(); i<n; i++) {
      if (tabbedPanel.getComponent(i)==anEditor.getComponent()) {
//        System.out.println ("Found editor "+anEditor.getName()+ " at i="+i);
        ejs.showPanel(defaultHeader);
        cardLayout.show (finalPanel,anEditor.getName());
        tabbedPanel.setSelectedComponent (anEditor.getComponent());
        return;
      }
    }
  }

// ------ private methods

  protected final MenuItem createMenuItem (String _key, String _header, ActionListener _al) {
    MenuItem mi = new MenuItem (res.getString("TabbedEditor."+_key));
    mi.setActionCommand (_key);
    mi.addActionListener (_al);
    popupMenu.add (mi);
    return mi;
  }

  protected void showFirstPage () {
    if (tabbedPanel.getTabCount()<=0) cardLayout.show (finalPanel,"FirstPanel");
    else {
      cardLayout.show (finalPanel,"TabbedPanel");
      tabbedPanel.setSelectedIndex (0);
    }
  }

  private void moveUpAndDownPage (boolean _up) {
    int index = tabbedPanel.getSelectedIndex();
    if (index<0) return;
    Editor page = pageList.get(index);
    if (page==null) return;
    if (_up) {
      if (index==0) return;
      pageList.removeElementAt (index);
      pageList.insertElementAt (page,index-1);
      tabbedPanel.removeTabAt(index);
      tabbedPanel.insertTab (page.isActive() ? page.getName() : page.getName()+" (D)",null,page.getComponent(),tooltip,index-1);
    }
    else {
      if (index==(pageList.size()-1)) return;
      pageList.removeElementAt (index);
      pageList.insertElementAt (page,index+1);
      tabbedPanel.removeTabAt(index);
      tabbedPanel.insertTab (page.isActive() ? page.getName() : page.getName()+" (D)",null,page.getComponent(),tooltip,index+1);
    }
    tabbedPanel.setSelectedComponent (page.getComponent());
    changed = true;
    }

  private void copyPage () {
    int index = tabbedPanel.getSelectedIndex();
    if (index<0) return;
    Editor page = pageList.get(index);
    if (page==null) return;
    addPage(typeOfPage(page),this.getUniqueName(page.getName()),page.saveStringBuffer().toString(),true,false);
    changed = true;
  }

  private void toggleCurrentPage () {
    int index = tabbedPanel.getSelectedIndex();
    if (index<0) return;
    Editor page = pageList.get (index);
    if (page.isActive()) page.setActive (false);
    else page.setActive (true);
    setTitleForPage();
    changed = true;
//    System.out.println(this.getClass()+" toggle calling updateControlValues");
    ejs.getModelEditor().getVariablesEditor().updateControlValues (false);
  }

  protected void toggleInternalCurrentPage () {
    int index = tabbedPanel.getSelectedIndex();
    if (index<0) return;
    Editor page = pageList.get (index);
    if (page.isInternal()) page.setInternal (false);
    else page.setInternal (true);
    setTitleForPage();
    changed = true;
  }
  
  private void setTitleForPage () {
    int index = tabbedPanel.getSelectedIndex();
    if (index<0) return;
    Editor page = pageList.get (index);
    String title = page.getName();
    if (page.isInternal()) title += " (I)";
    if (!page.isActive()) title += " (D)";
    tabbedPanel.setTitleAt (index, title);
  }

  
  protected void removeCurrentPage () {
    int index = tabbedPanel.getSelectedIndex();
    if (index<0) return;
    tabbedPanel.removeTabAt (index);
    pageList.removeElementAt (index);
    updatePageCounterField(pageList.size());
    changed = true;
    if (tabbedPanel.getTabCount()<=0) cardLayout.show (finalPanel,"FirstPanel");
//    System.out.println(this.getClass()+" calling updateControlValues");
    ejs.getModelEditor().getVariablesEditor().updateControlValues (false);
  }

  public Editor getCurrentPage() {
    int index = tabbedPanel.getSelectedIndex();
    if (index<0) return null;
    return pageList.get(index);
  }

  private String getCurrentPageName () {
    int index = tabbedPanel.getSelectedIndex();
    if (index<0) return "";
    return pageList.get(index).getName();
  }

  private void renameCurrentPage (String _name) {
    int index = tabbedPanel.getSelectedIndex();
    if (index<0) return;
    _name = getUniqueName (_name); //Gonzalo 070128
    tabbedPanel.setTitleAt (index,_name);
    Editor page = pageList.get(index);
    page.setName(_name);
    setTitleForPage();
//    if (!page.isActive()) tabbedPanel.setTitleAt (index, page.getName()+" (D)");
    changed = true;
  }

  private boolean nameExists (String _name) {
    for (Enumeration<Editor> e = pageList.elements(); e.hasMoreElements(); ) if (_name.equals (e.nextElement().getName())) return true; 
    return false;
  }

  protected final String getUniqueName (String _name) {
    String newname = new String (_name.trim());
    int i=1;
    while (nameExists (newname)) newname = _name + " " + (++i);
    return newname;
  }

  protected void addPage (String _typeOfPage, String _name, String _code, boolean _enabled, boolean _internal) {
    cardLayout.show (finalPanel,"TabbedPanel");
    _name = getUniqueName (_name);
    Editor page = createPage (_typeOfPage, _name, _code);
    page.setFont  (myFont);
    page.setColor (myColor);
    int index = tabbedPanel.getSelectedIndex();
    if (index==-1) {
      pageList.addElement (page);
      if (tabbedPanel.getTabCount()==0) {
        tabbedPanel.addTab (page.getName(),null,page.getComponent(),tooltip); // This causes an exception sometimes
      }
      else { 
        tabbedPanel.insertTab (page.getName(),null,page.getComponent(),tooltip,tabbedPanel.getTabCount());
      }
      index = 0;
    }
    else {
      index++;
      pageList.insertElementAt (page,index);
      tabbedPanel.insertTab (page.getName(),null,page.getComponent(),tooltip,index);
    }
    tabbedPanel.setSelectedComponent (page.getComponent());
    page.setActive (_enabled);
    page.setInternal (_internal);
    setTitleForPage(); // tabbedPanel.setTitleAt (index, page.getName()+" (D)");
    updatePageCounterField(pageList.size());
    // tabbedPanel.validate(); This hangs the computer when reading a file at start-up !!!???
    tabbedPanel.repaint();
    changed = true;
  }

  // Respond to user actions

  private class MyActionListener implements ActionListener {
    public void actionPerformed (ActionEvent _evt) {
      String aCmd=_evt.getActionCommand();
      if (aCmd.equals("addPage")) {
        Object obj = JOptionPane.showInputDialog(getComponent(),
          res.getString("TabbedEditor.NewName"),res.getString("TabbedEditor.Rename"),
          JOptionPane.QUESTION_MESSAGE,null,null,getUniqueName (defaultString));
        if (obj==null) return;
        String txt = obj.toString().trim();
        if (txt.length()>0) addPage (defaultType,txt,null,true,false);
        else addPage (defaultType,defaultString, null,true,false);
      }
      else if (aCmd.equals("upPage"))     moveUpAndDownPage (true);
      else if (aCmd.equals("dnPage"))     moveUpAndDownPage (false);
      else if (aCmd.equals("copyPage"))   copyPage ();
      else if (aCmd.equals("renamePage")) {
        Object obj = JOptionPane.showInputDialog(getComponent(),
          res.getString("TabbedEditor.NewName"),res.getString("TabbedEditor.Rename"),
          JOptionPane.QUESTION_MESSAGE,null,null,getCurrentPageName());
        if (obj==null) return;
        String txt = obj.toString().trim();
        if (txt.length()>0) renameCurrentPage (txt);
      }
      else if (aCmd.equals("togglePage")) toggleCurrentPage ();
      else if (aCmd.equals("removePage")) removeCurrentPage ();
      // Update ODE list in Experiments
//      if (defaultType.equals(Editor.EVOLUTION_EDITOR)) ejs.getExperimentEditor().updateAllOdeList(aCmd);
    }
  }

} // end of class


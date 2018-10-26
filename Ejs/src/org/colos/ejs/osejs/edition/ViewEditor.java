/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Modified: March 2006
 */

package org.colos.ejs.osejs.edition;

import org.colos.ejs.osejs.view.*;
import org.colos.ejss.xml.SimulationXML;
import org.opensourcephysics.tools.FontSizer;

import java.awt.*;
import javax.swing.*;
//import org.colos.ejs.osejs.utils.ResourceUtil;

public class ViewEditor implements Editor {
//  static private ResourceUtil res = new ResourceUtil ("Resources");

  private boolean visible=true;
  private String name="View";
  private JComponent mainPanel;
  private TreeOfElements tree;
  private CreationPanelRow creationPanel;
  private org.colos.ejs.osejs.Osejs mEjs;

  public ViewEditor (org.colos.ejs.osejs.Osejs _ejs) {
    mEjs = _ejs;
    creationPanel = new CreationPanelRow(_ejs);
    tree = new TreeOfElements(_ejs, creationPanel);
    creationPanel.setTree(tree);


    JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPanel.setOneTouchExpandable(true);
    splitPanel.setRightComponent(creationPanel.getComponent());
    splitPanel.setLeftComponent(tree.getComponent());
    splitPanel.setResizeWeight(1.0);
    
    mainPanel = new JPanel(new BorderLayout());
    mainPanel.add (splitPanel,BorderLayout.CENTER);

//    mainPanel.add(creationPanel.getComponent(),BorderLayout.EAST);
//    mainPanel.add(tree.getComponent(),BorderLayout.CENTER);

    // mainPanel.validate(); // No need for this if the panel hasn't been displayed yet
    mainPanel.setBorder(new javax.swing.border.LineBorder(Color.black));
    clear();
    
    setZoomLevel(FontSizer.getLevel());
  }

  public void setZoomLevel (int level) {
//    FontSizer.setFonts(mainPanel, level);
  }
  

  public TreeOfElements getTree() { return tree; }

  public boolean isEmpty() { return tree.isEmpty(); }

  public CreationPanelRow getCreationPanel() { return creationPanel; }

  // Implementation of Editor

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    return tree.search(_info, _searchString, _mode);
  }

  public void setName (String _name) { name = _name+".Creation"; }  // This is for backwards compatibility of XML files

  public String getName() { return name; }

  public void clear () {
    tree.clear ();
    creationPanel.clear();
//    splitPanel.setDividerLocation(0.4);
    //splitPanel.repaint();
  }

  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) {
    tree.setColor (_color);
    creationPanel.setColor (_color);
  }

  public void setFont (Font _font) {
    tree.setFont (_font);
    creationPanel.setFont (_font);
  }

  public void showWindows (boolean _show) { tree.showWindows(_show); }

  public void setEditable (boolean _editable) { tree.setEditable(_editable); }

  public void refresh (boolean _hiddensToo) {
//    if (visible || _hiddensToo) getComponent().setVisible(true); These two lines caused an error
//    else getComponent().setVisible(false);                       clicking Experiments would show nothing!
  }

  public void setVisible (boolean _visible) { visible = _visible; }

  public boolean isVisible () { return visible; }

  public boolean isChanged () { return tree.isChanged(); }

  public void setChanged (boolean _ch) { tree.setChanged(_ch); }

  public void setActive (boolean _active) { }

  public boolean isActive () { return true; }

  public boolean isInternal() {
    return false;
  }

  public void setInternal(boolean _advanced) {  }
  
  public StringBuffer generateCode (int _type, String _info) { return tree.generateCode(_type); }

  public void fillSimulationXML(SimulationXML _simXML) { // Do nothing, for the moment
  }
  
  public StringBuffer saveStringBuffer () {
    StringBuffer save = new StringBuffer();
    save.append("<"+name+">\n"+tree.saveStringBuffer(getName())+"</"+name+">\n");
    return save;
  }

  public void readString (String _input) {
    int begin = _input.indexOf("<"+name+">\n");
    if (begin>=0) {
      int end = _input.indexOf("</"+name+">\n");
      tree.readString(_input.substring(begin+name.length()+3,end),name,-1);
      tree.setKeepPreviewHidden(mEjs.getOptions().forceKeepPreviewHidden());
      setChanged(false);
    }
  }

// ------ End of implementation of Editor

} // end of class

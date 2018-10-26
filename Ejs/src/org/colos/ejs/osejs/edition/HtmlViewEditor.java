/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Modified: March 2006
 */

package org.colos.ejs.osejs.edition;

import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.edition.html_view.Palette;
import org.colos.ejs.osejs.edition.translation.TranslatableProperty;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.*;

public class HtmlViewEditor extends TabbedEditor {

  private JComponent mMainPanel;
  private Palette mPalette;
  private boolean mReading = false;
  
  public HtmlViewEditor (org.colos.ejs.osejs.Osejs _ejs) {
    super (_ejs, Editor.HTML_VIEW_EDITOR, "HtmlView", false);
    mPalette = new Palette(_ejs);
//    mPalette.setTree(tree);

    JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPanel.setOneTouchExpandable(true);
    splitPanel.setLeftComponent(super.getComponent());
    splitPanel.setRightComponent(mPalette.getComponent());
    splitPanel.setResizeWeight(1.0);
    
    mMainPanel = new JPanel(new BorderLayout());
    mMainPanel.add (splitPanel,BorderLayout.CENTER);

    mMainPanel.validate(); // No need for this if the panel hasn't been displayed yet
    mMainPanel.setBorder(new javax.swing.border.LineBorder(Color.black));
    clear();
    getTabbedPane().addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent e) {
        Editor page = getCurrentPage();
        if (page instanceof OneView) ((OneView) page).showWindows(true);
//          System.out.println("Tab: " + tabbedPane.getSelectedIndex());
      }
  });
  }

  public StringBuffer getResourcesNeeded() {
    Set<String> set = new java.util.HashSet<String>();
    for (Editor page : getPages()) set.addAll(((OneView) page).getTree().getResourcesNeeded("File"));
    StringBuffer buffer = new StringBuffer();
    for (String entry : set) {
//      System.out.println ("Adding file "+entry);
      buffer.append(entry+";");
    }
    return buffer;
  }

  public Set<String> getBase64Images() {
    Set<String> set = new java.util.HashSet<String>();
    for (Editor page : getPages()) set.addAll(((OneView) page).getTree().getResourcesNeeded("BASE64_IMAGE"));
    return set;
  }

  public boolean isEmpty() { 
    for (Editor page : getPages()) {
      if (page.isActive()) return false;
    }
    return true;
  }

  protected Editor createPage (String _type, String _name, String _code) {
    mReading = _code!=null;
    OneView page = new OneView (ejs,mPalette);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    return page;
  }

  protected void removeCurrentPage () {
    Editor page = getCurrentPage();
    if (page instanceof OneView) ((OneView) page).clear();
    super.removeCurrentPage();
  }
  
  public void clear () {
    super.clear ();
    mPalette.clear();
  }

  public Component getComponent () { return mMainPanel; }

  public void setColor (Color _color) {
    super.setColor (_color);
    mPalette.setColor (_color);
  }

  public void setFont (Font _font) {
    super.setFont (_font);
    if (mPalette!=null) mPalette.setFont (_font);
  }

  public java.util.List<TranslatableProperty> getTranslatableProperties() {
    java.util.List<TranslatableProperty> list = new ArrayList<TranslatableProperty>();
    for (Editor page : getPages()) {
      list.addAll( ((OneView) page).getTree().getTranslatableProperties());
    }
    return list;
  }
  
  public void showWindows (boolean _show) { 
    for (Editor page : getPages()) ((OneView) page).showWindows(_show);
  }

  public void refreshEmulator () { 
    for (Editor page : getPages()) ((OneView) page).refreshEmulator();
  }
  
  public void checkForShowingFirstPage () {
    if (!mReading) return;
    java.util.Vector<Editor> pages = getPages();
    if (pages.size()>0) {
      Editor page = pages.get(0);
      if (page instanceof OneView) ((OneView) page).showWindows(true);
    }
  }
  

} // end of class

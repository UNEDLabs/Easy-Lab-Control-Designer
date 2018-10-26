/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.html;


import java.awt.Font;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.TabbedEditor;
import org.colos.ejs.osejs.utils.InterfaceUtils;
import org.opensourcephysics.display.OSPRuntime;

public class DescriptionEditor extends TabbedEditor {
  private JFileChooser chooser=null;

  public DescriptionEditor (org.colos.ejs.osejs.Osejs _ejs) {
    super (_ejs, Editor.DESCRIPTION_EDITOR, "Description");
  }

  protected void addExtraMenuEntries() {
    MenuItem advancedPage = createMenuItem ("toggleInternalPage",defaultHeader,new ActionListener() {
      public void actionPerformed(ActionEvent e) { toggleInternalCurrentPage(); }
    });
    Font font = InterfaceUtils.font(null,res.getString("Editor.TitleFont"));
    advancedPage.setFont(font);
  }

  protected Editor createPage (String _type, String _name, String _code) {
    Editor page;
    if (_type.equals(Editor.HTML_EXTERNAL_EDITOR))  { // backwards compatibility
      page = new HtmlEditor(ejs, false, this);
      if (_code!=null && !_code.startsWith("<![CDATA[")) _code = "<![CDATA[\n"+_code+"\n]]>";
//      String head = OsejsCommon.getPiece(_code,"<head>","</head>",false);
    }
    else page = new HtmlEditor(ejs, true,this);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }
  
  public JFileChooser getChooser () {
    if (chooser==null) { // It is a static chooser
      chooser=OSPRuntime.createChooser("HTML,XHTML",new String[]{"html","htm","xhtml"},ejs.getSourceDirectory().getParentFile());
      org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
      chooser.setCurrentDirectory(ejs.getCurrentDirectory());
    }
    return chooser;
  }
  
  /**
   * Returns the size of any of its pages (the editor part)
   * @return
   */
  public java.awt.Dimension getEditorSize() {
    java.util.Vector<Editor> pages = getPages();
    if (pages.isEmpty()) return new java.awt.Dimension(0,0);
    return ((HtmlEditor) pages.get(0)).getDefaultPage().getComponent().getSize();
  }


  public void refreshCss() {
    for (Editor page : getPages()) {
      ((HtmlEditor) page).refreshCss();
    }
  }
}

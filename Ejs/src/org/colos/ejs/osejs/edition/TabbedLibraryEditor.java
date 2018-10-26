/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

import org.colos.ejs.osejs.edition.experiments.ScheduledConditionEditor;
import org.colos.ejs.osejs.utils.FileChooserUtil;
import org.opensourcephysics.display.OSPRuntime;

public class TabbedLibraryEditor extends TabbedEditor {
  private JFileChooser chooser=null;
  protected MenuItem addPage2;
  private JLabel secondButton;
  protected PopupMenu initialMenu;

  public TabbedLibraryEditor (org.colos.ejs.osejs.Osejs _ejs) {
    super (_ejs, Editor.LIBRARY_EDITOR,"Model.Library");
    addPage2.setFont (addPageMI.getFont());
    //firstButton.setText(res.getString("TabbedEditor.ClickOrRightClickHere"));
    java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
    secondButton = new JLabel (res.getString("TabbedEditor.RightClickForOptions"));
    secondButton.setFont (secondButton.getFont().deriveFont(14.0f));
    gbc.gridy = 1;
    firstButtonPanel.add(secondButton,gbc);
    firstButtonPanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked (MouseEvent _evt) {
        if (OSPRuntime.isPopupTrigger(_evt)) //SwingUtilities.isRightMouseButton(_evt)) 
          initialMenu.show(_evt.getComponent(),_evt.getX(),_evt.getY());
      }
    });
    firstButtonPanel.setFont(firstButtonPanel.getFont().deriveFont(Font.BOLD));
    firstButtonPanel.add(initialMenu);
  }

  protected Editor createPage (String _type, String _name, String _code) {
    Editor page;
    if (_type.equals(Editor.LIBRARY_EXTERNAL_EDITOR))  {
      page = new LibraryExternalEditor(ejs, this);
      if (_code!=null && !_code.startsWith("<![CDATA[")) _code = "<![CDATA[\n"+_code+"\n]]>";
    }
    else page = new LibraryEditor(ejs,this);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }

  public void adjust () {
    for (int i=0,n=pageList.size(); i<n; i++) ((ScheduledConditionEditor) pageList.get(i)).adjust();
  }

  // ------------------------
  
  public void setColor (Color _color) {
    super.setColor(_color);
    secondButton.setForeground(_color);
  }

  public JFileChooser getChooser () {
    if (chooser==null) { // It is a static chooser
      if (ejs.supportsJava()) chooser=OSPRuntime.createChooser("java",new String[]{"java"},ejs.getSourceDirectory().getParentFile());
      else chooser=OSPRuntime.createChooser("javascript",new String[]{"js"},ejs.getSourceDirectory().getParentFile());
      org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
      chooser.setCurrentDirectory(ejs.getCurrentDirectory());
    }
    return chooser;
  }
  
  protected void customMenuItems (java.awt.event.ActionListener actionListener) {
    addPageMI  = createMenuItem ("addPage",defaultHeader,actionListener);
    
    ActionListener al = new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        File javaFile = ejs.useNativeFileChooser() ? 
            ejs.chooseFileUnderSource (FileChooserUtil.getLibraryFileChooser(ejs), finalPanel, true) : 
            ejs.chooseFileUnderSource(getChooser(), finalPanel, true); // true to open
        if (javaFile==null) return; // The user canceled it
        if (!javaFile.exists()) JOptionPane.showMessageDialog(getComponent(), res.getString("Osejs.File.ReadError"),
                                                              res.getString("Osejs.File.ReadingError"), JOptionPane.INFORMATION_MESSAGE);
        else addPage(LIBRARY_EXTERNAL_EDITOR, org.colos.ejs.library.utils.FileUtils.getPlainName(javaFile), ejs.getRelativePath(javaFile), true,false);
      }
    };
    addPage2 = createMenuItem("importExternalLibraryPage", defaultHeader, al);

    // The next is an almost exact copy of the previous one
    MenuItem externalJavami = new MenuItem (res.getString("TabbedEditor.importExternalLibraryPage"));
    externalJavami.addActionListener (al);
    
    initialMenu=new PopupMenu();
    initialMenu.add(externalJavami);
  }

  protected String typeOfPage (Editor page) { 
    if (page instanceof LibraryExternalEditor) return Editor.LIBRARY_EXTERNAL_EDITOR;
    return defaultType; 
  }


}

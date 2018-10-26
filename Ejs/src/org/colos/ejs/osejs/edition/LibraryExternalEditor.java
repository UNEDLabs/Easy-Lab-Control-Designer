/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;

import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.*;

public class LibraryExternalEditor extends CodeEditor {
//  static private ResourceUtil res = new ResourceUtil ("Resources");
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");

  private JPanel fullPanel;
//  private JLabel fileLabel;
  private JTextField fileField;
  private JButton refreshButton,fileButton;

  public LibraryExternalEditor (org.colos.ejs.osejs.Osejs _ejs, final TabbedLibraryEditor aParentEditor) {
    super (_ejs, aParentEditor);
    textComponent.setEditable(false);
    super.hideLowerPanel();

    JLabel fileLabel = new JLabel (res.getString ("LibraryExternalEditor.FileLabel"),SwingConstants.CENTER);
    fileLabel.setBorder(new EmptyBorder(0,5,0,5));

    fileField = new JTextField();
    fileField.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) { readJava (); }
    });

    fileButton = new JButton (org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    fileButton.setToolTipText(res.getString("LibraryExternalEditor.SetJavaFile"));
    fileButton.setRequestFocusEnabled(false);
    fileButton.setMargin(new Insets (0,0,0,0));
    fileButton.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        File javaFile = ejs.chooseFileUnderSource(aParentEditor.getChooser(), fullPanel,true);
        if (javaFile==null) return; // The user canceled it
        fileField.setText(ejs.getRelativePath(javaFile));
        readJava();
      }
    });

    refreshButton = new JButton (org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("HTMLEditor.Refresh.Icon")));
    refreshButton.setToolTipText(res.getString("HTMLEditor.Refresh"));
    refreshButton.setRequestFocusEnabled(false);
    refreshButton.setMargin(new Insets (0,0,0,0));
    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) { readJava (); }
    });

    JPanel buttonsPanel = new JPanel(new GridLayout(1,0));
    buttonsPanel.add(fileButton);
    buttonsPanel.add(refreshButton);

    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(fileLabel,BorderLayout.WEST);
    bottomPanel.add(fileField,BorderLayout.CENTER);
    bottomPanel.add(buttonsPanel,BorderLayout.EAST);

    fullPanel = new JPanel(new BorderLayout());
    fullPanel.add(super.getComponent(),BorderLayout.CENTER);
    fullPanel.add(bottomPanel,BorderLayout.SOUTH);

    fullPanel.validate();
  }

  public Component getComponent () { return fullPanel; }

  public void clear () {
    textComponent.setText("");
    commentField.setText("");
    fileField.setText("");
  }

//  public void setColor (Color _color) {
//    super.setColor(_color);
//    fileLabel.setForeground(_color);
//  }

//  public void setFont (Font _font) {
//    super.setFont(_font);
//  }

  public void setEditable (boolean _editable) {
    super.setEditable(_editable);
    fileField.setEditable(_editable);
    fileButton.setEnabled(_editable);
    refreshButton.setEnabled(_editable);
  }

  public void setActive (boolean _active) {
    super.setActive(_active);
    fileField.setEditable(_active);
    fileButton.setEnabled(_active);
    refreshButton.setEnabled(_active);
  }

  public StringBuffer generateCode (int _type, String _info) {
    if (!isActive()) return new StringBuffer();
    if (_type==Editor.GENERATE_RESOURCES_NEEDED) {
      StringBuffer code = new StringBuffer();
      code.append(fileField.getText()+";");
      return code;
    }
    if (_type==Editor.GENERATE_PLAINCODE) {
      StringBuffer code = new StringBuffer();
      code.append(splitCode(getName(),textComponent.getText(),_info,""));
      return code;
    }
    return new StringBuffer();
  }

//  public String getLink() {
//    String filename = fileField.getText();
//    if (filename.startsWith("./")) filename = FileUtils.getRelativePath(ejs.getCurrentXMLFile().getParentFile(),
//        ejs.getSourceDirectory(),false) + filename.substring(2);
//    return filename;
//  }

  public StringBuffer saveStringBuffer () {
    return new StringBuffer("<![CDATA["+ fileField.getText()+ "]]>\n");
  }

  public void readString (String _input) {
    String piece = OsejsCommon.getPiece(_input,"<![CDATA[","]]>",false);
    fileField.setText(piece);
    readJava();
  }

  private void readJava () {
    File file = org.colos.ejs.osejs.edition.html.HtmlEditor.convertToAbsoluteFile(ejs,fileField.getText().trim());
    if (!file.exists()) {
      JOptionPane.showMessageDialog(getComponent(), res.getString("Osejs.File.ReadError")+" "+fileField.getText(),
        res.getString("Osejs.File.ReadingError"), JOptionPane.INFORMATION_MESSAGE);
      textComponent.setText("");
    }
    else textComponent.setText(FileUtils.readTextFile(file,null));
    changed = true;
  }

} // end of class

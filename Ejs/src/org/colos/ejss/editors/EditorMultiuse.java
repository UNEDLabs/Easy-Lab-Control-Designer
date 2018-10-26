/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejss.editors;

import java.awt.*;

import javax.swing.*;

import org.colos.ejs.osejs.view.CreationPanelRow;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.utils.TwoStrings;

public abstract class EditorMultiuse {
  static private ResourceUtil res = new ResourceUtil("Resources");
  static private final String ICONS_DIR = "data/icons/Editors/";
  
  static private final String sOPTION_NAME = "OPTION";
  static private final String sOK_NAME     = "OK";
  static private final String sCANCEL_NAME = "CANCEL";

  protected static java.util.List<TwoStrings> sOptionsMap;
  static protected String[] sOptions;
  static protected String sIconPrefix;
  static protected JPanel sOptionsPanel=null;

  static private String sReturnValue=null;
  static private JDialog sDialog;
  static private JLabel[] sButtons;

  static private final Color sSelectedColor = new Color (128,64,255);
  static private Color sNormalColor;
  static private JComponent sSelectedButton = null;

  static private java.awt.event.MouseAdapter mouseListener;

  static {
    mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        JComponent button = (JComponent) evt.getComponent();
        if (!button.isEnabled()) return;
        String name = button.getName();
        if (name.equals(sOPTION_NAME)) {
          if (sSelectedButton!=null) {
            sSelectedButton.setBorder(CreationPanelRow.zeroBorder);
            sSelectedButton.setBackground (sNormalColor);
          }
          sSelectedButton = button;
          sSelectedButton.setBorder(CreationPanelRow.selectedBorder);
          sSelectedButton.setBackground (sSelectedColor);
        }
        else if (name.equals(sOK_NAME)) { // find selected option
          sReturnValue = null;
          for (int i=0; i<sButtons.length; i++) if (sButtons[i]==sSelectedButton) {
            sReturnValue = sOptions[i];
            break;
          }
          sDialog.setVisible (false);
        }
        else if (name.equals(sCANCEL_NAME)) {
          sReturnValue = null;
          sDialog.setVisible (false);
        }
      }
    };

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.setName (sOK_NAME);
    okButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setName (sCANCEL_NAME);
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
    buttonPanel.add (cancelButton);

    sOptionsPanel = new JPanel (new java.awt.GridLayout(1,0,0,0));
    sOptionsPanel.setBorder (new javax.swing.border.EmptyBorder (5,5,5,5));
    // Will be filled with options later...

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);
    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (sep1,java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    sDialog = new JDialog();
    sDialog.getContentPane().setLayout (new java.awt.BorderLayout(0,0));
    sDialog.getContentPane().add (sOptionsPanel,java.awt.BorderLayout.NORTH);
    sDialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    sDialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { sReturnValue = null; }
      }
    );

    sDialog.setTitle (res.getString("EditorFor.ChooseOne"));
    sDialog.validate();
    sDialog.pack();
    sDialog.setModal(true);
  }

  static protected void resetButtons() {
    sOptionsPanel.removeAll();
    sButtons = new JLabel[sOptions.length];
    for (int i=0, m = sOptions.length; i<m; i++) {
      sButtons[i] = new JLabel ();
      sButtons[i].setRequestFocusEnabled(false);
      sButtons[i].setName (sOPTION_NAME);
      sButtons[i].addMouseListener(mouseListener);
      sButtons[i].setHorizontalAlignment(SwingConstants.CENTER);
      if (sIconPrefix!=null) {
        String text = sOptions[i].toUpperCase();
        Icon icon = org.opensourcephysics.tools.ResourceLoader.getIcon(ICONS_DIR+sIconPrefix+"_"+text+".gif"); // Do not complain
        if (icon!=null) sButtons[i].setIcon(icon);
      }
      sButtons[i].setText(sOptions[i]);
      String realOption = sOptionsMap.get(i).getSecondString();
      if (realOption!=null) sButtons[i].setToolTipText(realOption);
      sButtons[i].setBorder(CreationPanelRow.zeroBorder);
      sOptionsPanel.add (sButtons[i]);
    }
    sNormalColor = sButtons[0].getBackground();
  }

  static protected String edit (JTextField returnField) {
    sDialog.validate();
    sDialog.pack();
    String value = returnField.getText().trim().toLowerCase();
    if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length()-1);
    if (sSelectedButton!=null) {
      sSelectedButton.setBackground(sNormalColor);
      sSelectedButton.setBorder(CreationPanelRow.zeroBorder);
    }
    for (int i=0, m = sButtons.length; i<m; i++) {
      if (value.equals(sOptionsMap.get(i).getSecondString().toLowerCase())) {
        sSelectedButton = sButtons[i];
        sSelectedButton.setBorder(CreationPanelRow.selectedBorder);
        sSelectedButton.setBackground(sSelectedColor);
        break;
      }
    }
    sDialog.setLocationRelativeTo (returnField);
    sDialog.setVisible (true);
    if (sReturnValue!=null) {
      for (TwoStrings entry : sOptionsMap) {
        if (sReturnValue.equals(entry.getFirstString())) {
          sReturnValue = entry.getSecondString();
          break;
        }
      }
      if (sReturnValue!=null) return "\""+sReturnValue+"\"";  
    }
    return null;
  }

  public static String edit (java.util.List<TwoStrings> optionsList, javax.swing.JTextField returnField) {
    sOptionsMap = optionsList;
    int n = optionsList.size();
    sOptions = new String[n];
    for (int i =0; i<n; i++) {
      TwoStrings entry = optionsList.get(i);
      sOptions[i] = entry.getFirstString();      
    }
    resetButtons();
    return edit (returnField);
  }

}

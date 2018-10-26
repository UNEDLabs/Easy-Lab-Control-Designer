/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import java.awt.*;

import javax.swing.*;

import org.colos.ejs.osejs.view.CreationPanelRow;
import org.colos.ejs.osejs.utils.ResourceUtil;

public abstract class EditorMultiuse {
  static private ResourceUtil res = new ResourceUtil("Resources");
  static private final String ICONS_DIR = "data/icons/Editors/";

  static protected String[] options;
  static protected String prefix;

  static private String returnValue=null;
  static protected JDialog dialog;
  static protected JLabel[] buttons;
  static protected JPanel optionsPanel=null;

  static private final Color selectedColor = new Color (128,64,255);
  static private Color normalColor;
  static private JComponent selectedButton = null;

  static private java.awt.event.MouseAdapter mouseListener;

  static {
    mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {

        JComponent button = (JComponent) evt.getComponent();
        if (!button.isEnabled()) return;
        String aCmd = button.getName();
        if (aCmd.equals("selected")) {
          if (selectedButton!=null) {
            selectedButton.setBorder(CreationPanelRow.zeroBorder);
            selectedButton.setBackground (normalColor);
          }
          selectedButton = button;
          selectedButton.setBorder(CreationPanelRow.selectedBorder);
          selectedButton.setBackground (selectedColor);

        }
        else if (aCmd.equals("ok")) {
          returnValue = returnOption();
          dialog.setVisible (false);
        }
        else if (aCmd.equals("default"))  {
          returnValue = "<default>";
          dialog.setVisible (false);
        }
        else if (aCmd.equals("cancel")) {
          returnValue = null;
          dialog.setVisible (false);
        }
      }
    };

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.setName ("ok");
    okButton.addMouseListener (mouseListener);
/*
    JButton defaultButton = new JButton (res.getString("EditorFor.Default"));
    defaultButton.setName ("default");
    defaultButton.addMouseListener (mouseListener);
*/
    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setName ("cancel");
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
//    buttonPanel.add (defaultButton);
    buttonPanel.add (cancelButton);

    optionsPanel = new JPanel (new java.awt.GridLayout(1,0,0,0));
    optionsPanel.setBorder (new javax.swing.border.EmptyBorder (5,5,5,5));
    // Will be filled with options later...

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);
    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (sep1,java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(0,0));
    dialog.getContentPane().add (optionsPanel,java.awt.BorderLayout.NORTH);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { returnValue = null; }
      }
    );

    dialog.setTitle (res.getString("EditorFor.ChooseOne"));
    dialog.validate();
    dialog.pack();
    dialog.setModal(true);
  }

  protected static void resetButtons() {
    optionsPanel.removeAll();
    buttons = new JLabel[options.length];
    for (int i=0, m = options.length; i<m; i++) {
      buttons[i] = new JLabel ();
      buttons[i].setRequestFocusEnabled(false);
      buttons[i].setName ("selected");
      buttons[i].addMouseListener(mouseListener);
      buttons[i].setHorizontalAlignment(SwingConstants.CENTER);
      String text = options[i].toUpperCase();
      Icon icon = org.opensourcephysics.tools.ResourceLoader.getIcon(ICONS_DIR+prefix+"_"+text+".gif"); // Do not complain
      if (icon==null) buttons[i].setText(text);
      else buttons[i].setIcon(icon);
      String txt = res.getOptionalString("EditorFor"+prefix+"."+options[i]);
      if (txt!=null) buttons[i].setText(txt);
      buttons[i].setToolTipText(options[i]);
      //buttons[i].setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
      buttons[i].setBorder(CreationPanelRow.zeroBorder);
      optionsPanel.add (buttons[i]);
    }
//    buttons[0].setSelected(true);
    normalColor = buttons[0].getBackground();
//    selectedButton = buttons[0];
  }

  private static String returnOption () {
    for (int i=0; i<buttons.length; i++) if (buttons[i]==selectedButton) return options[i];
    return null;
  }

  protected static String edit (String _classname, String _property, String _type, JTextField returnField) {
    return edit(returnField);
  }

  protected static String edit (JTextField returnField) {
    dialog.validate();
    dialog.pack();
    String value = returnField.getText().trim().toLowerCase();
    if (selectedButton!=null) {
      selectedButton.setBackground(normalColor);
      selectedButton.setBorder(CreationPanelRow.zeroBorder);
      }
    for (int i=0, m = buttons.length; i<m; i++)
      if (value.equals(options[i].toLowerCase())) {
        selectedButton = buttons[i];
        if ("selected".equals(buttons[i].getName())) {
          selectedButton.setBorder(CreationPanelRow.selectedBorder);
          selectedButton.setBackground(selectedColor);
        }
        break;
      }
    dialog.setLocationRelativeTo (returnField);
    dialog.setVisible (true);
    return returnValue;
  }

}

/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import java.awt.*;
import javax.swing.*;

import org.colos.ejs.osejs.utils.ResourceUtil;

public class EditorForString {
  static private JDialog dialog;
  static private String returnValue=null;
  static private JLabel label;
  static private JTextField field;
  static private JPanel panel;

  static private ResourceUtil res = new ResourceUtil("Resources");

  static {
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
          returnValue = field.getText();
          dialog.setVisible (false);
        }
        else if (aCmd.equals("cancel")) {
          returnValue = null;
          dialog.setVisible (false);
        }
      }
    };

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.setActionCommand ("ok");
    okButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setActionCommand ("cancel");
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton); //,BorderLayout.WEST);
    buttonPanel.add (cancelButton); //,BorderLayout.EAST);

    // ----------- Input area ----------------------
    
    label = new JLabel();
    label.setBorder(new javax.swing.border.EmptyBorder(0,3,0,3));
    field = new JTextField(20);
    
    panel = new JPanel (new BorderLayout());
    panel.add (label,BorderLayout.WEST);
    panel.add (field,BorderLayout.CENTER);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (new JSeparator(SwingConstants.HORIZONTAL),java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.CENTER);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(0,0));
    dialog.getContentPane().add (panel,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) {
          returnValue = null;
        }
      }
    );

    dialog.setTitle(res.getString("EditorForString.Title"));
    dialog.setModal(true);
    dialog.validate();
    dialog.pack();
  }
  
  public static String edit (String _property, javax.swing.text.JTextComponent returnField) {
    field = new JTextField(20);
    panel.removeAll();
    panel.add (label,BorderLayout.WEST);
    panel.add (field,BorderLayout.CENTER);

    field.setText(returnField.getText());
    label.setText(_property);
    dialog.pack();
    dialog.setLocationRelativeTo (returnField);
    dialog.setVisible (true);
    return returnValue;
  }

  public static String edit (String _label, javax.swing.JComponent _parent, String _previousValue) {
    field = new JTextField(20);
    panel.removeAll();
    panel.add (label,BorderLayout.WEST);
    panel.add (field,BorderLayout.CENTER);

    field.setText(_previousValue);
    label.setText(_label);
    dialog.pack();
    dialog.setLocationRelativeTo (_parent);
    dialog.setVisible (true);
    return returnValue;
  }
  
//  public static String inputPassword (String _label, JComponent _component, javax.swing.JComponent _parent) { Use PasswordDialog instead
//    field = new JPasswordField(20);
//    field.addActionListener(new java.awt.event.ActionListener() {
//      public void actionPerformed(java.awt.event.ActionEvent evt) {
//        returnValue = field.getText();
//        dialog.setVisible (false);
//      }
//    });
//    panel.removeAll();
//    if (_component!=null) panel.add (_component,BorderLayout.NORTH);
//    panel.add (label,BorderLayout.WEST);
//    panel.add (field,BorderLayout.CENTER);
//
//    field.setText("");
//    label.setText(_label);
//    dialog.pack();
//    dialog.setLocationRelativeTo (_parent);
//    dialog.setVisible (true);
//    return returnValue;
//  }
  
  
}

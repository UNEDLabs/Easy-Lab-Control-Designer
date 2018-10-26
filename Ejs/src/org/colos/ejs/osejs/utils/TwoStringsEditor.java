/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.colos.ejs.osejs.utils.ResourceUtil;

public class TwoStringsEditor {
  static private ResourceUtil res = new ResourceUtil("Resources");
  static private JDialog dialog;
  static private JLabel xLabel,yLabel;
  static private JTextField xField;
  static private JPasswordField yField;

  static private TwoStrings returnValue=null;

  static {
    java.awt.event.ActionListener actionListener =  new java.awt.event.ActionListener () {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        String aCmd = evt.getActionCommand();
        if (aCmd.equals("ok")) {
          returnValue = new TwoStrings(xField.getText(),new String(yField.getPassword()));
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
    okButton.addActionListener (actionListener);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setActionCommand ("cancel");
    cancelButton.addActionListener (actionListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
    buttonPanel.add (cancelButton);

    xLabel = new JLabel ("First String:");
    xLabel.setBorder(new EmptyBorder(0,5,0,5));
    yLabel = new JLabel ("Second String:");
    yLabel.setBorder(new EmptyBorder(0,5,0,5));

    xField = new JTextField (20);
    xField.setText("");

    yField = new JPasswordField (20);
    yField.setText("");
    yField.addActionListener (actionListener);

    JPanel labelsPanel = new JPanel (new GridLayout (0,1));
    labelsPanel.add (xLabel);
    labelsPanel.add (yLabel);

    JPanel fieldsPanel = new JPanel (new GridLayout (0,1));
    fieldsPanel.add (xField);
    fieldsPanel.add (yField);

    JPanel centerPanel = new JPanel (new BorderLayout());
    centerPanel.setBorder (new javax.swing.border.EmptyBorder (5,0,5,0));
    centerPanel.add(labelsPanel,BorderLayout.WEST);
    centerPanel.add(fieldsPanel,BorderLayout.CENTER);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel bottomPanel = new JPanel (new BorderLayout());
    bottomPanel.add (sep1,BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(5,0));
    dialog.getContentPane().add (centerPanel,java.awt.BorderLayout.NORTH);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { returnValue = null; }
      }
    );

//    dialog.setSize (res.getDimension("EditorForTwoStrings.Size"));
    dialog.pack();
//    dialog.validate();
    dialog.setModal(true);
  }

  public static TwoStrings edit (Component _parent,String _title, String _firstLabel, String _secondLabel, TwoStrings _value, int _columns, boolean _secret) {
    if (_value==null) return edit (_parent,_title, _firstLabel, _secondLabel, "","", _columns, _secret);
    return edit (_parent,_title, _firstLabel, _secondLabel, _value.getFirstString(),_value.getSecondString(),_columns, _secret);
  }

  public static TwoStrings edit (Component _parent,String _title, String _firstLabel, String _secondLabel, String _firstValue, String _secondValue, int _columns, boolean _secret) {
    FontMetrics fm = xLabel.getFontMetrics(xLabel.getFont());
    Dimension dim = new Dimension(Math.max(fm.stringWidth(_firstLabel),fm.stringWidth(_secondLabel))+14,xLabel.getHeight());
    xLabel.setText(_firstLabel);
    yLabel.setText(_secondLabel);
    xLabel.setPreferredSize(dim);
    yLabel.setPreferredSize(dim);
    xField.setText (_firstValue);
    // Secret or not
    if (_secret) {
      yField.setText ("");
      yField.setEchoChar('*');
      yField.setActionCommand("ok");
      yField.requestFocus();
    }
    else {
      yField.setText (_secondValue);
      yField.setEchoChar((char)0);
      yField.setActionCommand("");
    }
    if (_columns>0) {
      xField.setColumns(_columns);
      yField.setColumns(_columns);
      dialog.pack();
    }
    dialog.setLocationRelativeTo (_parent);
    dialog.setTitle (_title);
    dialog.setVisible (true);
    return returnValue;
  }

}

/**
 * The utils package contains generic utilities
 * Copyright (c) January 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.*;
import javax.swing.*;

public class ConfirmListDialog {
  static public final int YES = 0;
  static public final int NO = 1;
  static public final int CANCEL = 2;
  static public final int DONT_ASK = 3;

  static private org.colos.ejs.osejs.utils.ResourceUtil res = new org.colos.ejs.osejs.utils.ResourceUtil("Resources");

  public static int edit (Component _target, String _message, String _title, String _label, java.util.List<Object> _listContents) {
    final ReturnValue returnValue=new ReturnValue();
    final JDialog dialog=new JDialog();

    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if      (aCmd.equals("yes"))    returnValue.value = YES;
        else if (aCmd.equals("no"))     returnValue.value = NO;
        else if (aCmd.equals("cancel")) returnValue.value = CANCEL;
        else if (aCmd.equals("dontAsk")) returnValue.value = DONT_ASK;
        dialog.setVisible (false);
      }
    };

    JButton yesButton = new JButton (res.getString("EditorFor.Yes"));
    yesButton.setActionCommand ("yes");
    yesButton.addMouseListener (mouseListener);

    JButton noButton = new JButton (res.getString("EditorFor.No"));
    noButton.setActionCommand ("no");
    noButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setActionCommand ("cancel");
    cancelButton.addMouseListener (mouseListener);

    JButton dontAskButton = new JButton (res.getString("EditorFor.DontAskAnymore"));
    dontAskButton.setActionCommand ("dontAsk");
    dontAskButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (yesButton);
    buttonPanel.add (noButton);
    buttonPanel.add (cancelButton);
    buttonPanel.add (dontAskButton);

    JPanel topPanel = new JPanel (new BorderLayout ());

    JTextArea textArea   = new JTextArea (_message);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
    textArea.setBackground(topPanel.getBackground());
    textArea.setBorder(new javax.swing.border.EmptyBorder(5,5,10,5));

//    destLabel = new JLabel();
    JTextArea destLabel   = new JTextArea (_label);
    destLabel.setWrapStyleWord(true);
    destLabel.setLineWrap(true);
    destLabel.setEditable(false);
    destLabel.setFont(textArea.getFont().deriveFont(Font.BOLD));
    destLabel.setBackground(topPanel.getBackground());
    destLabel.setBorder(new javax.swing.border.EmptyBorder(5,5,10,5));


    DefaultListModel<Object> listModel = new DefaultListModel<Object>();
    for (int i=0,n=_listContents.size(); i<n; i++) listModel.addElement(_listContents.get(i));

    JList<Object> list = new JList<Object>(listModel);
//    list.setEnabled(false);
    JScrollPane scrollPane = new JScrollPane(list);
//    scrollPane.setBorder(new javax.swing.border.EmptyBorder(5,10,5,10));

    topPanel.setBorder(new javax.swing.border.EmptyBorder(5,10,5,10));
    topPanel.add (textArea,BorderLayout.NORTH);
    topPanel.add (scrollPane,BorderLayout.CENTER);
    topPanel.add (destLabel,BorderLayout.SOUTH);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel southPanel = new JPanel (new java.awt.BorderLayout());
    southPanel.add (sep1,java.awt.BorderLayout.NORTH);
    southPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog.getContentPane().setLayout (new java.awt.BorderLayout(5,0));
    dialog.getContentPane().add (topPanel,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (southPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { returnValue.value = CANCEL; }
      }
    );

    dialog.setSize (res.getDimension("EditorForConfirmList.Size"));
    dialog.validate();
    dialog.setModal(true);
    dialog.setTitle (_title);
    dialog.setLocationRelativeTo (_target);
    dialog.setVisible (true);
    return returnValue.value;
  }

  static private class ReturnValue {
    int value = CANCEL;
  }

}

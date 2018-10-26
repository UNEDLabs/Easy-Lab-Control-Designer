/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import org.colos.ejs.osejs.utils.ResourceUtil;

public class EditorForLayout {
  static private JDialog dialog;
  static private String returnValue=null;
  static private JRadioButton borderLayoutButton,flowLayoutButton,gridLayoutButton,hboxButton,vboxButton;
  static private JRadioButton leftButton,centerButton,rightButton;
  static private JTextField rowsField, columnsField, horField, verField;
  static private int hGap=0, vGap=0, rows=0, columns=1;

  static private ResourceUtil res = new ResourceUtil("Resources");

  static {
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
          returnValue = returnLayout();
          dialog.setVisible (false);
        }
        else if (aCmd.equals("cancel")) {
          returnValue = null;
          dialog.setVisible (false);
        }
        else if (aCmd.equals("border") || aCmd.equals("hbox") || aCmd.equals("vbox")) {
          if (aCmd.equals("hbox") || aCmd.equals("vbox")) {
            horField.setEnabled(false);
            verField.setEnabled(false);
          }
          else {
            horField.setEnabled(true);
            verField.setEnabled(true);
          }
          rowsField.setEnabled (false);
          columnsField.setEnabled (false);
          leftButton.setEnabled (false);
          centerButton.setEnabled (false);
          rightButton.setEnabled (false);
        }
        else if (aCmd.equals("grid")) {
          horField.setEnabled(true);
          verField.setEnabled(true);
          rowsField.setEnabled (true);
          columnsField.setEnabled (true);
          leftButton.setEnabled (false);
          centerButton.setEnabled (false);
          rightButton.setEnabled (false);
        }
        else if (aCmd.equals("flow")) {
          horField.setEnabled(true);
          verField.setEnabled(true);
          rowsField.setEnabled (false);
          columnsField.setEnabled (false);
          leftButton.setEnabled (true);
          centerButton.setEnabled (true);
          rightButton.setEnabled (true);
        }
      }
    };

    java.awt.event.KeyAdapter ka = new java.awt.event.KeyAdapter() {
      public void keyReleased (java.awt.event.KeyEvent evt) {
        if (evt.getSource()==rowsField) {
          try {
            rows = Integer.parseInt (rowsField.getText());
            rowsField.setText(""+rows);
          } catch (Exception exc) {
            rows = 0;
            rowsField.setText ("0");
          }
        }
        if (evt.getSource()==columnsField) {
          try {
            columns = Integer.parseInt (columnsField.getText());
            columnsField.setText(""+columns);
          } catch (Exception exc) {
            columns = 0;
            columnsField.setText ("0");
          }
        }
        if (evt.getSource()==horField) {
          try {
            hGap = Integer.parseInt (horField.getText());
            horField.setText(""+hGap);
          } catch (Exception exc) {
            hGap = 0;
            horField.setText ("0");
          }
        }
        if (evt.getSource()==verField) {
          try {
            vGap = Integer.parseInt (verField.getText());
            verField.setText(""+vGap);
          } catch (Exception exc) {
            vGap = 0;
            verField.setText ("0");
          }
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

    borderLayoutButton = new JRadioButton (res.getString("EditorForLayout.Border"));
    borderLayoutButton.setActionCommand("border");
    borderLayoutButton.addMouseListener (mouseListener);
    borderLayoutButton.setRequestFocusEnabled(false);
    borderLayoutButton.setSelected(true);

    flowLayoutButton = new JRadioButton (res.getString("EditorForLayout.Flow"));
    flowLayoutButton.setActionCommand("flow");
    flowLayoutButton.addMouseListener (mouseListener);
    flowLayoutButton.setRequestFocusEnabled(false);

    gridLayoutButton = new JRadioButton (res.getString("EditorForLayout.Grid"));
    gridLayoutButton.setActionCommand("grid");
    gridLayoutButton.addMouseListener (mouseListener);
    gridLayoutButton.setRequestFocusEnabled(false);

    hboxButton = new JRadioButton (res.getString("EditorForLayout.HBox"));
    hboxButton.setActionCommand("hbox");
    hboxButton.addMouseListener (mouseListener);
    hboxButton.setRequestFocusEnabled(false);

    vboxButton = new JRadioButton (res.getString("EditorForLayout.VBox"));
    vboxButton.setActionCommand("vbox");
    vboxButton.addMouseListener (mouseListener);
    vboxButton.setRequestFocusEnabled(false);

    ButtonGroup group = new ButtonGroup();
    group.add(borderLayoutButton);
    group.add(flowLayoutButton);
    group.add(gridLayoutButton);
    group.add(hboxButton);
    group.add(vboxButton);

    // ----------- BorderLayout area ----------------------
    JPanel borderPanel = new JPanel (new java.awt.GridLayout(0,1));
    borderPanel.add (borderLayoutButton);
    borderPanel.add (hboxButton);
    borderPanel.add (vboxButton);

    // ----------- GridLayout area ----------------------
    JLabel rowsLabel        = new JLabel (res.getString("EditorForLayout.Rows"));
    rowsField    = new JTextField (3);
    rowsField.setText ("0");
    rowsField.setActionCommand("rows");
    rowsField.addKeyListener (ka);

    JLabel columnsLabel     = new JLabel (res.getString("EditorForLayout.Columns"));
    columnsField = new JTextField (3);
    columnsField.setText ("1");
    columnsField.setActionCommand("columns");
    columnsField.addKeyListener (ka);

    JPanel rcFullPanel = new JPanel (new java.awt.GridLayout(2,2,0,0));
    rcFullPanel.add (rowsLabel);
    rcFullPanel.add (rowsField);
    rcFullPanel.add (columnsLabel);
    rcFullPanel.add (columnsField);

    JPanel rowscolumnsPanel = new JPanel (new java.awt.BorderLayout());
    rowscolumnsPanel.setBorder (new javax.swing.border.EmptyBorder (5,30,5,10));
    rowscolumnsPanel.add (rcFullPanel,java.awt.BorderLayout.WEST);

    JPanel gridPanel = new JPanel (new java.awt.BorderLayout());
    gridPanel.add (gridLayoutButton,BorderLayout.NORTH);
    gridPanel.add (rowscolumnsPanel,BorderLayout.CENTER);

    // ----------- FlowLayout area ----------------------
    leftButton = new JRadioButton (res.getString("EditorForLayout.Left"));
    leftButton.setRequestFocusEnabled(false);
    leftButton.setSelected(true);

    centerButton = new JRadioButton (res.getString("EditorForLayout.Center"));
    centerButton.setRequestFocusEnabled(false);

    rightButton = new JRadioButton (res.getString("EditorForLayout.Right"));
    rightButton.setRequestFocusEnabled(false);

    ButtonGroup group2 = new ButtonGroup();
    group2.add(leftButton);
    group2.add(centerButton);
    group2.add(rightButton);

    JPanel alignPanel = new JPanel (new java.awt.GridLayout(0,1,0,0));
    alignPanel.setBorder (new javax.swing.border.EmptyBorder (5,20,5,10));
    alignPanel.add (leftButton);
    alignPanel.add (centerButton);
    alignPanel.add (rightButton);

    JPanel flowPanel = new JPanel (new java.awt.BorderLayout());
    flowPanel.add (flowLayoutButton,java.awt.BorderLayout.NORTH);
    flowPanel.add (alignPanel,java.awt.BorderLayout.CENTER);


    // ----------- Gaps area ----------------------
    JLabel horLabel     = new JLabel (res.getString("EditorForLayout.HGap"));
    horField = new JTextField (3);
    horField.setText ("0");
    horField.setActionCommand("hGap");
    horField.addKeyListener (ka);

    JLabel verLabel     = new JLabel (res.getString("EditorForLayout.VGap"));
    verField = new JTextField (3);
    verField.setText ("0");
    verField.setActionCommand("vGap");
    verField.addKeyListener (ka);

    JPanel gapsPanel = new JPanel (new java.awt.GridLayout(2,2,0,0));
    gapsPanel.setBorder (new javax.swing.border.EmptyBorder (5,10,5,10));
    gapsPanel.add (horLabel);
    gapsPanel.add (horField);
    gapsPanel.add (verLabel);
    gapsPanel.add (verField);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);
    JSeparator sep2 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel separationPanel = new JPanel (new java.awt.BorderLayout());
    separationPanel.add (sep1,java.awt.BorderLayout.NORTH);
    separationPanel.add (gapsPanel,java.awt.BorderLayout.CENTER);
    separationPanel.add (sep2,java.awt.BorderLayout.SOUTH);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (separationPanel,java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    JPanel topPanel = new JPanel (new java.awt.BorderLayout());
    topPanel.setBorder (new javax.swing.border.EmptyBorder (5,10,5,10));
    topPanel.add (borderPanel,java.awt.BorderLayout.NORTH);
    topPanel.add (gridPanel,java.awt.BorderLayout.CENTER);
    topPanel.add (flowPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(0,0));
    dialog.getContentPane().add (topPanel,java.awt.BorderLayout.NORTH);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) {
          returnValue = null;
        }
      }
    );

    dialog.setSize (res.getDimension("EditorForLayout.Size"));
    dialog.validate();
    dialog.setModal(true);

    rowsField.setEnabled (false);
    columnsField.setEnabled (false);
    leftButton.setEnabled (false);
    centerButton.setEnabled (false);
    rightButton.setEnabled (false);
  }

  private static String returnLayout () {
    if (borderLayoutButton.isSelected()) return "BORDER:"+hGap+","+vGap;
    if (hboxButton.isSelected()) return "HBOX";
    if (vboxButton.isSelected()) return "VBOX";
    if (flowLayoutButton.isSelected()) {
      if (leftButton.isSelected())   return "FLOW:left,"  +hGap+","+vGap;
      if (centerButton.isSelected()) return "FLOW:center,"+hGap+","+vGap;
      if (rightButton.isSelected())  return "FLOW:right," +hGap+","+vGap;
    }
    if (gridLayoutButton.isSelected()) return "GRID:"+rows+","+columns+","+hGap+","+vGap;
    return null;
  }

  public static String edit (String _classname, String _property, String _type, JTextField returnField) {
    String value = returnField.getText().toLowerCase();
    StringTokenizer tkn = new StringTokenizer (value,":, ");
    try {
      value = tkn.nextToken();
      if (value.startsWith("flow")) {
        String alignment = tkn.nextToken();
        if      (alignment.equals("center")) centerButton.setSelected (true);
        else if (alignment.equals("right"))  rightButton.setSelected (true);
        else leftButton.setSelected (true);
      }
      if (tkn.hasMoreTokens()) hGap = Integer.parseInt(tkn.nextToken());
      if (tkn.hasMoreTokens()) vGap = Integer.parseInt(tkn.nextToken());
      if (value.startsWith("grid") && tkn.hasMoreTokens()) {
        rows = hGap; columns = vGap;
        if (tkn.hasMoreTokens()) hGap = Integer.parseInt(tkn.nextToken());
        if (tkn.hasMoreTokens()) vGap = Integer.parseInt(tkn.nextToken());
      }
      if (value.startsWith("flow")) {
        flowLayoutButton.setSelected (true);
        rowsField.setEnabled (false);
        columnsField.setEnabled (false);
        leftButton.setEnabled (true);
        centerButton.setEnabled (true);
        rightButton.setEnabled (true);
      }
      else if (value.startsWith("grid"))  {
        gridLayoutButton.setSelected (true);
        rowsField.setEnabled (true);
        columnsField.setEnabled (true);
        leftButton.setEnabled (false);
        centerButton.setEnabled (false);
        rightButton.setEnabled (false);
      }
      else {
        if (value.startsWith("hbox")) {
          hboxButton.setSelected (true);
          horField.setEnabled(false);
          verField.setEnabled(false);
        }
        else if (value.startsWith("vbox")) {
          vboxButton.setSelected (true);
          horField.setEnabled(false);
          verField.setEnabled(false);
        }
        else {
          borderLayoutButton.setSelected (true);
          horField.setEnabled(true);
          verField.setEnabled(true);
        }
        rowsField.setEnabled (false);
        columnsField.setEnabled (false);
        leftButton.setEnabled (false);
        centerButton.setEnabled (false);
        rightButton.setEnabled (false);
      }

    } catch (Exception exc) { }
    rowsField.setText (""+rows);
    columnsField.setText (""+columns);
    horField.setText (""+hGap);
    verField.setText (""+vGap);
    returnValue = returnLayout();
    dialog.setLocationRelativeTo (returnField);
    dialog.setTitle (res.getString("EditorFor.ChooseOne"));
    dialog.setVisible (true);
    return returnValue;
  }

//  public static String value () { return returnValue; }

}

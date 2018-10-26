/**
 * The utils package contains generic utilities
 * Copyright (c) January 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import org.colos.ejs.osejs.utils.ResourceUtil;

public class EditorForRectangle {
  static private JDialog dialog;
  static private String returnValue=null;
  static private JLabel leftLabel,rightLabel,topLabel, bottomLabel;
  static private JTextField leftField,rightField, topField, bottomField;
  static private int leftValue=0, rightValue=0, topValue, bottomValue;

  static private ResourceUtil res = new ResourceUtil("Resources");

  static {
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
          returnValue = topValue+","+leftValue+","+bottomValue+","+rightValue;
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
    okButton.setActionCommand ("ok");
    okButton.addMouseListener (mouseListener);

    JButton defaultButton = new JButton (res.getString("EditorFor.Default"));
    defaultButton.setActionCommand ("default");
    defaultButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setActionCommand ("cancel");
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
//    buttonPanel.add (defaultButton);
    buttonPanel.add (cancelButton);

    java.awt.event.KeyAdapter ka = new java.awt.event.KeyAdapter() {
      public void keyReleased (java.awt.event.KeyEvent evt) {
        if (evt.getSource()==leftField) {
          try {
            leftValue = Integer.parseInt (leftField.getText());
            leftField.setText(""+leftValue);
          } catch (Exception exc) {
            leftValue = 0;
            leftField.setText ("0");
          }
        }
        else if (evt.getSource()==rightField) {
          try {
            rightValue = Integer.parseInt (rightField.getText());
            rightField.setText(""+rightValue);
          } catch (Exception exc) {
            rightValue = 0;
            rightField.setText ("0");
          }
        }
        else if (evt.getSource()==bottomField) {
          try {
            bottomValue = Integer.parseInt (bottomField.getText());
            bottomField.setText(""+bottomValue);
          } catch (Exception exc) {
            bottomValue = 0;
            bottomField.setText ("0");
          }
        }
        else if (evt.getSource()==topField) {
          try {
            topValue = Integer.parseInt (topField.getText());
            topField.setText(""+topValue);
          } catch (Exception exc) {
            topValue = 0;
            topField.setText ("0");
          }
        }
      }
    };

    leftLabel   = new JLabel (res.getString("EditorForRectangle.Left"));
    rightLabel  = new JLabel (res.getString("EditorForRectangle.Right"));
    topLabel    = new JLabel (res.getString("EditorForRectangle.Top"));
    bottomLabel = new JLabel (res.getString("EditorForRectangle.Bottom"));
    leftLabel.setBorder(new javax.swing.border.EmptyBorder(0,3,0,3));
    rightLabel.setBorder(new javax.swing.border.EmptyBorder(0,3,0,3));
    topLabel.setBorder(new javax.swing.border.EmptyBorder(0,3,0,3));
    bottomLabel.setBorder(new javax.swing.border.EmptyBorder(0,3,0,3));
    int width = 0, height = 0;
    width  = Math.max(width,leftLabel.getPreferredSize().width);
    height = Math.max(height,leftLabel.getPreferredSize().height);
    width  = Math.max(width,rightLabel.getPreferredSize().width);
    height = Math.max(height,rightLabel.getPreferredSize().height);
    width  = Math.max(width,topLabel.getPreferredSize().width);
    height = Math.max(height,topLabel.getPreferredSize().height);
    width  = Math.max(width,bottomLabel.getPreferredSize().width);
    height = Math.max(height,bottomLabel.getPreferredSize().height);
    Dimension commonSize = new Dimension (width,height);
    leftLabel.setPreferredSize(commonSize);
    rightLabel.setPreferredSize(commonSize);
    topLabel.setPreferredSize(commonSize);
    bottomLabel.setPreferredSize(commonSize);

    leftField = new JTextField (10);
    leftField.setText("0");
    leftField.addKeyListener (ka);

    topField = new JTextField (10);
    topField.setText("0");
    topField.addKeyListener (ka);

    bottomField = new JTextField (10);
    bottomField.setText("0");
    bottomField.addKeyListener (ka);

    rightField = new JTextField (10);
    rightField.setText("0");
    rightField.addKeyListener (ka);

    JPanel leftPanel = new JPanel (new BorderLayout ());
    leftPanel.add (leftLabel,BorderLayout.WEST);
    leftPanel.add (leftField,BorderLayout.CENTER);

    JPanel rightPanel = new JPanel (new BorderLayout ());
    rightPanel.add (rightLabel,BorderLayout.WEST);
    rightPanel.add (rightField,BorderLayout.CENTER);

    JPanel topPanel = new JPanel (new BorderLayout ());
    topPanel.add (topLabel,BorderLayout.WEST);
    topPanel.add (topField,BorderLayout.CENTER);

    JPanel bottomPanel = new JPanel (new BorderLayout ());
    bottomPanel.add (bottomLabel,BorderLayout.WEST);
    bottomPanel.add (bottomField,BorderLayout.CENTER);

    JPanel centerPanel = new JPanel (new GridLayout (2,2));
    centerPanel.setBorder (new javax.swing.border.EmptyBorder (5,0,5,0));
    centerPanel.add(topPanel);
    centerPanel.add(bottomPanel);
    centerPanel.add(leftPanel);
    centerPanel.add(rightPanel);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel southPanel = new JPanel (new java.awt.BorderLayout());
    southPanel.add (sep1,java.awt.BorderLayout.NORTH);
    southPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(5,0));
    dialog.getContentPane().add (centerPanel,java.awt.BorderLayout.NORTH);
    dialog.getContentPane().add (southPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { returnValue = null; }
      }
    );

    dialog.setSize (res.getDimension("EditorForRectangle.Size"));
    dialog.validate();
    dialog.setModal(true);
  }

  public static String edit (String _classname, String _property, String _type, JTextField returnField) {
//    return edit (_property, returnField);
//  }
//
//  public static String edit (String varName, JTextField returnField, String aType) {
//  System.out.println("Type is "+_type);
    if (_type.indexOf("Margins")>=0) {
      topLabel.setText(res.getString("EditorForRectangle.Top"));
      leftLabel.setText(res.getString("EditorForRectangle.Left"));
      bottomLabel.setText(res.getString("EditorForRectangle.Bottom"));
      rightLabel.setText(res.getString("EditorForRectangle.Right"));
    }
    else if (_type.indexOf("Gutters")>=0) {
      topLabel.setText(res.getString("EditorForRectangle.Left"));
      leftLabel.setText(res.getString("EditorForRectangle.Top"));
      bottomLabel.setText(res.getString("EditorForRectangle.Right"));
      rightLabel.setText(res.getString("EditorForRectangle.Bottom"));
    }
    else {
      topLabel.setText(res.getString("EditorForRectangle.X"));
      leftLabel.setText(res.getString("EditorForRectangle.Y"));
      bottomLabel.setText(res.getString("EditorForRectangle.Width"));
      rightLabel.setText(res.getString("EditorForRectangle.Height"));
    }
    try {
      StringTokenizer l = new StringTokenizer (returnField.getText(),",");
      if (l.hasMoreTokens()) topValue = Integer.parseInt(l.nextToken());
      if (l.hasMoreTokens()) leftValue = Integer.parseInt(l.nextToken());
      if (l.hasMoreTokens()) bottomValue = Integer.parseInt(l.nextToken());
      if (l.hasMoreTokens()) rightValue = Integer.parseInt(l.nextToken());
    } catch (Exception exc) { }
    leftField.setText (""+leftValue);
    rightField.setText (""+rightValue);
    topField.setText (""+topValue);
    bottomField.setText (""+bottomValue);
    dialog.setLocationRelativeTo (returnField);
    dialog.setTitle (res.getString("EditorFor.ChooseOne"));
    dialog.setVisible (true);
    return returnValue;
  }

}

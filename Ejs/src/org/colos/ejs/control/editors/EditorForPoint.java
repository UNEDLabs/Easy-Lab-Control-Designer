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

public class EditorForPoint {
  static private JDialog dialog;
  static private String returnValue=null;
  static private JLabel xLabel,yLabel;
  static private JTextField xField,yField;
  static private int xValue=0, yValue=0;

  static private ResourceUtil res = new ResourceUtil("Resources");

  static {
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
          returnValue = xValue+","+yValue;
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
        if (evt.getSource()==xField) {
          try {
            xValue = Integer.parseInt (xField.getText());
            xField.setText(""+xValue);
          } catch (Exception exc) {
            xValue = 0;
            xField.setText ("0");
          }
        }
        else if (evt.getSource()==yField) {
          try {
            yValue = Integer.parseInt (yField.getText());
            yField.setText(""+yValue);
          } catch (Exception exc) {
            yValue = 0;
            yField.setText ("0");
          }
        }
      }
    };

    xLabel = new JLabel (res.getString("EditorForPoint.X"));
    yLabel = new JLabel (res.getString("EditorForPoint.Y"));

    xField = new JTextField (10);
    xField.setText("0");
    xField.addKeyListener (ka);

    yField = new JTextField (10);
    yField.setText("0");
    yField.addKeyListener (ka);

    JPanel xPanel = new JPanel (new BorderLayout ());
    xPanel.add (xLabel,BorderLayout.WEST);
    xPanel.add (xField,BorderLayout.CENTER);

    JPanel yPanel = new JPanel (new BorderLayout ());
    yPanel.add (yLabel,BorderLayout.WEST);
    yPanel.add (yField,BorderLayout.CENTER);

    JPanel centerPanel = new JPanel (new GridLayout (1,2));
    centerPanel.setBorder (new javax.swing.border.EmptyBorder (5,0,5,0));
    centerPanel.add(xPanel);
    centerPanel.add(yPanel);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (sep1,java.awt.BorderLayout.NORTH);
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

    dialog.setSize (res.getDimension("EditorForPoint.Size"));
    dialog.validate();
    dialog.setModal(true);
  }

  public static String edit (String _classname, String _property, String _type, JTextField returnField) {
//    return edit (_property, returnField);
//  }
//
//
//  public static String edit (String varName, JTextField returnField, String aType) {
    if (_type.indexOf("Dimension")>=0) {
      xLabel.setText(res.getString("EditorForPoint.Width"));
      yLabel.setText(res.getString("EditorForPoint.Height"));
    }
    else {
      xLabel.setText(res.getString("EditorForPoint.X"));
      yLabel.setText(res.getString("EditorForPoint.Y"));
    }
    try {
      StringTokenizer l = new StringTokenizer (returnField.getText(),",");
      if (l.hasMoreTokens()) xValue = Integer.parseInt(l.nextToken());
      if (l.hasMoreTokens()) yValue = Integer.parseInt(l.nextToken());
    } catch (Exception exc) { }
    xField.setText (""+xValue);
    yField.setText (""+yValue);
    returnValue = xValue+","+yValue;
    dialog.setLocationRelativeTo (returnField);
    dialog.setTitle (res.getString("EditorFor.ChooseOne"));
    dialog.setVisible (true);
    return returnValue;
  }

//  public static String value () { return returnValue; }

}
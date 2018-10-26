/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.osejs.utils.ResourceUtil;

public class EditorFor2DAffineTransform {
  static private JDialog dialog;
  static private String returnValue=null;
  static private JTextField roField;
  static private JTextField scxField, scyField;
  static private JTextField shxField, shyField;
  static private JTextField trxField, tryField;
  static private JTextField m00Field, m10Field, m01Field, m11Field, m02Field, m12Field; // A sequence of doubles m00, m10, m01, m11, m02, m12
  static private JRadioButton roDegrees, roRadians;

  static private JTextField textArea;

  static private ResourceUtil res = new ResourceUtil("Resources");

  static {
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
          returnValue = textArea.getText();
          dialog.setVisible (false);
        }
        else if (aCmd.equals("default"))  {
          textArea.setText("");
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
    buttonPanel.add (defaultButton);
    buttonPanel.add (cancelButton);

    Border border = new EmptyBorder(1,5,1,5);
    border = BorderFactory.createEtchedBorder();

    // Rotation

    JComponent roButton = new JButton (res.getString("EditorFor2DAffineTransform.AddRotation"));
    roButton.setBorder(border);
    roButton.addMouseListener(new MouseAdapter(){
      public void mousePressed (MouseEvent evt) {
        String angleStr = roField.getText().trim(); 
        boolean degrees = roDegrees.isSelected(); 
        if (angleStr.length()<=0) return;
        String rotStr = "ro:"+angleStr;
        if (degrees) rotStr += "d";
        addToCurrentText(rotStr);
      }
    });

    roField = new JTextField (5);
    roDegrees = new JRadioButton(res.getString("EditorFor3DTransformation.Degrees"),true);
    roDegrees.setBorder(border);
    roRadians = new JRadioButton(res.getString("EditorFor3DTransformation.Radians"));
    roRadians.setBorder(border);
    ButtonGroup roGroup = new ButtonGroup();
    roGroup.add(roDegrees);
    roGroup.add(roRadians);
    JPanel roGroupPanel = new JPanel (new GridLayout(1,2));
    roGroupPanel.add(roDegrees);
    roGroupPanel.add(roRadians);
    JPanel roPanel = new JPanel (new java.awt.BorderLayout());
    roPanel.add (roField,java.awt.BorderLayout.CENTER);
    roPanel.add (roGroupPanel,java.awt.BorderLayout.EAST);

    // Scale 

    JComponent scButton = new JButton (res.getString("EditorFor2DAffineTransform.AddScale"));
    scButton.setBorder(border);
    scButton.addMouseListener(new MouseAdapter(){
      public void mousePressed (MouseEvent evt) {
        boolean empty = true;
        String scxStr = scxField.getText().trim(); if (scxStr.length()<=0) scxStr="1"; else empty = false;
        String scyStr = scyField.getText().trim(); if (scyStr.length()<=0) scyStr="1"; else empty = false;
        if (empty) return;
        addToCurrentText("sc:"+ scxStr+","+scyStr);
      }
    });
    scxField = new JTextField (5);
    scyField = new JTextField (5);

    JPanel scPanel = new JPanel (new GridLayout(1,2));
    scPanel.add(scxField);
    scPanel.add(scyField);

    // Translation 

    JComponent trButton = new JButton (res.getString("EditorFor2DAffineTransform.AddTranslation"));
    trButton.setBorder(border);
    trButton.addMouseListener(new MouseAdapter(){
      public void mousePressed (MouseEvent evt) {
        boolean empty = true;
        String trxStr = trxField.getText().trim(); if (trxStr.length()<=0) trxStr="1"; else empty = false;
        String tryStr = tryField.getText().trim(); if (tryStr.length()<=0) tryStr="1"; else empty = false;
        if (empty) return;
        addToCurrentText("tr:"+ trxStr+","+tryStr);
      }
    });
    trxField = new JTextField (5);
    tryField = new JTextField (5);

    JPanel trPanel = new JPanel (new GridLayout(1,2));
    trPanel.add(trxField);
    trPanel.add(tryField);

    // Shear 

    JComponent shButton = new JButton (res.getString("EditorFor2DAffineTransform.AddShear"));
    shButton.setBorder(border);
    shButton.addMouseListener(new MouseAdapter(){
      public void mousePressed (MouseEvent evt) {
        boolean empty = true;
        String shxStr = shxField.getText().trim(); if (shxStr.length()<=0) shxStr="1"; else empty = false;
        String shyStr = shyField.getText().trim(); if (shyStr.length()<=0) shyStr="1"; else empty = false;
        if (empty) return;
        addToCurrentText("sh:"+ shxStr+","+shyStr);
      }
    });
    shxField = new JTextField (5);
    shyField = new JTextField (5);

    JPanel shPanel = new JPanel (new GridLayout(1,2));
    shPanel.add(shxField);
    shPanel.add(shyField);


    // Matrix // A sequence of doubles m00, m10, m01, m11, m02, m12
    
    JComponent mButton = new JButton (res.getString("EditorFor2DAffineTransform.AddMatrix"));
    mButton.setBorder(border);
    mButton.addMouseListener(new MouseAdapter(){
      public void mousePressed (MouseEvent evt) {
        boolean empty = true;
        String m00Str = m00Field.getText().trim(); if (m00Str.length()<=0) m00Str="0"; else empty = false;
        String m10Str = m10Field.getText().trim(); if (m10Str.length()<=0) m10Str="0"; else empty = false;
        String m01Str = m01Field.getText().trim(); if (m01Str.length()<=0) m01Str="0"; else empty = false;
        String m11Str = m11Field.getText().trim(); if (m11Str.length()<=0) m11Str="0"; else empty = false;
        String m02Str = m02Field.getText().trim(); if (m02Str.length()<=0) m02Str="0"; else empty = false;
        String m12Str = m12Field.getText().trim(); if (m12Str.length()<=0) m12Str="0"; else empty = false;
        if (empty) return;
        addToCurrentText(m00Str+","+m10Str+","+m01Str+","+m11Str+","+m02Str+","+m12Str);
      }
    });
    m00Field = new JTextField ();
    m10Field = new JTextField ();
    m01Field = new JTextField ();
    m11Field = new JTextField ();
    m02Field = new JTextField ();
    m12Field = new JTextField ();
    JPanel mPanel = new JPanel (new GridLayout(2,3));
    mPanel.add(m00Field);
    mPanel.add(m10Field);
    mPanel.add(m01Field);
    mPanel.add(m11Field);
    mPanel.add(m02Field);
    mPanel.add(m12Field);

    // final result
    JPanel labelsPanel = new JPanel (new GridLayout (0,1));
    labelsPanel.add(roButton);
    labelsPanel.add(trButton);
    labelsPanel.add(scButton);
    labelsPanel.add(shButton);
    labelsPanel.add(mButton);
    labelsPanel.add(new JLabel());
    
    Box valuesPanel = Box.createVerticalBox(); //new JPanel (new GridLayout (0,1));
    valuesPanel.add(roPanel);
    valuesPanel.add(trPanel);
    valuesPanel.add(scPanel);
    valuesPanel.add(shPanel);
    valuesPanel.add(mPanel);


    textArea = new JTextField();
    textArea.setEditable(true);

    JPanel centerPanel = new JPanel (new BorderLayout ());
    centerPanel.setBorder (new javax.swing.border.EmptyBorder (5,0,5,0));
    centerPanel.add(labelsPanel, BorderLayout.WEST);
    centerPanel.add(valuesPanel, BorderLayout.CENTER);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (sep1,java.awt.BorderLayout.NORTH);
    bottomPanel.add (textArea,java.awt.BorderLayout.CENTER);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.setTitle (res.getString("EditorFor3DTransformation.AddTransformations"));
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(5,0));
    dialog.getContentPane().add (centerPanel,java.awt.BorderLayout.NORTH);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { returnValue = null; }
      }
    );

    //dialog.setSize (res.getDimension("EditorFor3DTransformation.Size"));
    dialog.pack();
    dialog.validate();
    dialog.setModal(true);
  }

  public static String edit (String _classname, String _property, String _type, JTextField returnField) {
    String orig = returnField.getText().trim();
    if (orig.indexOf('{')>=0 || orig.indexOf('}')>=0) textArea.setText(""); // coincides with ControlEditorFor3DTransformation
    else if (orig.indexOf(':')<0 && orig.indexOf(',')<0) textArea.setText(""); // coincides with decodeTransformation in ControlElement3D
    else textArea.setText(FileUtils.removeQuotes(orig).trim());
    textArea.requestFocus();
    dialog.setLocationRelativeTo (returnField);
    dialog.setVisible (true);
    return returnValue;
  }

  static void addToCurrentText(String text) {
    String current = textArea.getText().trim();
    if (current.length()>0) textArea.setText(current+" & "+text);
    else textArea.setText(text);
  }

}

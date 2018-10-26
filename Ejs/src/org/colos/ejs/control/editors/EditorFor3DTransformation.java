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

public class EditorFor3DTransformation {
  static private JDialog dialog;
  static private String returnValue=null;
  static private JTextField xField, yField, zField;
  static private JTextField q1Field, q2Field, q3Field, q4Field;
  static private JTextField v1Field, v2Field, v3Field, vaField;
  static private JTextField a1Field, a2Field, a3Field, a4Field, a5Field, a6Field;
  static private JRadioButton xDegrees, xRadians, yDegrees, yRadians, zDegrees, zRadians, vDegrees, vRadians;

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
    // Rotations about axes

    //String htmlText = "<html><u>"+res.getString("EditorFor3DTransformation.AddRotationAroundXby")+"</u></html>";

    JComponent xButton = new JButton (res.getString("EditorFor3DTransformation.AddRotationAroundXby"));
    xButton.setBorder(border);
/*
    java.util.Map map = xButton.getFont().getAttributes();
    map.put(java.awt.font.TextAttribute.UNDERLINE, java.awt.font.TextAttribute.UNDERLINE_ON);
    map.put(java.awt.font.TextAttribute.FOREGROUND, java.awt.Color.BLUE);
    Font newFont = new Font(map);
*/
    //xButton.setFont(newFont);
    xButton.addMouseListener(new RotationMouseListener(1));
    xField = new JTextField (5);
    xDegrees = new JRadioButton(res.getString("EditorFor3DTransformation.Degrees"),true);
    xDegrees.setBorder(border);
    xRadians = new JRadioButton(res.getString("EditorFor3DTransformation.Radians"));
    xRadians.setBorder(border);
    ButtonGroup xGroup = new ButtonGroup();
    xGroup.add(xDegrees);
    xGroup.add(xRadians);
    JPanel xGroupPanel = new JPanel (new GridLayout(1,2));
    xGroupPanel.add(xDegrees);
    xGroupPanel.add(xRadians);
    JPanel xPanel = new JPanel (new java.awt.BorderLayout());
    xPanel.add (xField,java.awt.BorderLayout.CENTER);
    xPanel.add (xGroupPanel,java.awt.BorderLayout.EAST);

    JComponent yButton = new JButton (res.getString("EditorFor3DTransformation.AddRotationAroundYby"));
    yButton.setBorder(border);
    yButton.addMouseListener(new RotationMouseListener(2));
    yField = new JTextField ();
    yDegrees = new JRadioButton(res.getString("EditorFor3DTransformation.Degrees"),true);
    yDegrees.setBorder(border);
    yRadians = new JRadioButton(res.getString("EditorFor3DTransformation.Radians"));
    yRadians.setBorder(border);
    ButtonGroup yGroup = new ButtonGroup();
    yGroup.add(yDegrees);
    yGroup.add(yRadians);
    JPanel yGroupPanel = new JPanel (new GridLayout(1,2));
    yGroupPanel.add(yDegrees);
    yGroupPanel.add(yRadians);
    JPanel yPanel = new JPanel (new java.awt.BorderLayout());
    yPanel.add (yField,java.awt.BorderLayout.CENTER);
    yPanel.add (yGroupPanel,java.awt.BorderLayout.EAST);

    JComponent zButton = new JButton (res.getString("EditorFor3DTransformation.AddRotationAroundZby"));
    zButton.setBorder(border);
    zButton.addMouseListener(new RotationMouseListener(3));
    zField = new JTextField ();
    zDegrees = new JRadioButton(res.getString("EditorFor3DTransformation.Degrees"),true);
    zDegrees.setBorder(border);
    zRadians = new JRadioButton(res.getString("EditorFor3DTransformation.Radians"));
    zRadians.setBorder(border);
    ButtonGroup zGroup = new ButtonGroup();
    zGroup.add(zDegrees);
    zGroup.add(zRadians);
    JPanel zGroupPanel = new JPanel (new GridLayout(1,2));
    zGroupPanel.add(zDegrees);
    zGroupPanel.add(zRadians);
    JPanel zPanel = new JPanel (new java.awt.BorderLayout());
    zPanel.add (zField,java.awt.BorderLayout.CENTER);
    zPanel.add (zGroupPanel,java.awt.BorderLayout.EAST);

    // Vector rotation

    JComponent vButton = new JButton (res.getString("EditorFor3DTransformation.AddVectorRotation"));
    vButton.setBorder(border);
    vButton.addMouseListener(new MouseAdapter(){
      public void mousePressed (MouseEvent evt) {
        String vaStr = vaField.getText().trim(); if (vaStr.length()<=0) return;
        if (vDegrees.isSelected()) vaStr += "d"; //"º";
        boolean empty = true;
        String v1Str = v1Field.getText().trim(); if (v1Str.length()<=0) v1Str="0"; else empty = false;
        String v2Str = v2Field.getText().trim(); if (v2Str.length()<=0) v2Str="0"; else empty = false;
        String v3Str = v3Field.getText().trim(); if (v3Str.length()<=0) v3Str="0"; else empty = false;
        if (empty) return;
        addToCurrentText(vaStr+","+v1Str+","+v2Str+","+v3Str);
      }
    });
    JLabel vLabel = new JLabel (res.getString("EditorFor3DTransformation.By"),SwingConstants.RIGHT);
    vLabel.setBorder(border);
    v1Field = new JTextField (5);
    v2Field = new JTextField (5);
    v3Field = new JTextField (5);
    vaField = new JTextField ();
    vDegrees = new JRadioButton(res.getString("EditorFor3DTransformation.Degrees"),true);
    vDegrees.setBorder(border);
    vRadians = new JRadioButton(res.getString("EditorFor3DTransformation.Radians"));
    vRadians.setBorder(border);
    ButtonGroup vGroup = new ButtonGroup();
    vGroup.add(vDegrees);
    vGroup.add(vRadians);
    JPanel vGroupPanel = new JPanel (new GridLayout(1,2));
    vGroupPanel.add(vDegrees);
    vGroupPanel.add(vRadians);

    JPanel vComponentsPanel = new JPanel (new GridLayout(1,3));
    vComponentsPanel.add(v1Field);
    vComponentsPanel.add(v2Field);
    vComponentsPanel.add(v3Field);
    JPanel vBottomPanel = new JPanel (new BorderLayout ());
    vBottomPanel.add(vaField, BorderLayout.CENTER);
    vBottomPanel.add(vGroupPanel, BorderLayout.EAST);

    // Alignment rotation

    JComponent aButton = new JButton (res.getString("EditorFor3DTransformation.AddAlignmentRotation"));
    aButton.setBorder(border);
    aButton.addMouseListener(new MouseAdapter(){
      public void mousePressed (MouseEvent evt) {
        boolean empty1 = true, empty2 = true;
        String a1Str = a1Field.getText().trim(); if (a1Str.length()<=0) a1Str="0"; else empty1 = false;
        String a2Str = a2Field.getText().trim(); if (a2Str.length()<=0) a2Str="0"; else empty1 = false;
        String a3Str = a3Field.getText().trim(); if (a3Str.length()<=0) a3Str="0"; else empty1 = false;
        String a4Str = a4Field.getText().trim(); if (a4Str.length()<=0) a1Str="0"; else empty2 = false;
        String a5Str = a5Field.getText().trim(); if (a5Str.length()<=0) a2Str="0"; else empty2 = false;
        String a6Str = a6Field.getText().trim(); if (a6Str.length()<=0) a3Str="0"; else empty2 = false;
        if (empty1 && empty2) return;
        addToCurrentText(a1Str+","+a2Str+","+a3Str+ "," + a4Str+","+a5Str+","+a6Str);
      }
    });
    JLabel aLabel = new JLabel (res.getString("EditorFor3DTransformation.With"),SwingConstants.RIGHT);
    aLabel.setBorder(border);
    a1Field = new JTextField (5);
    a2Field = new JTextField (5);
    a3Field = new JTextField (5);
    a4Field = new JTextField (5);
    a5Field = new JTextField (5);
    a6Field = new JTextField (5);

    JPanel aComponentsPanel = new JPanel (new GridLayout(1,3));
    aComponentsPanel.add(a1Field);
    aComponentsPanel.add(a2Field);
    aComponentsPanel.add(a3Field);
    JPanel aComponents2Panel = new JPanel (new GridLayout(1,3));
    aComponents2Panel.add(a4Field);
    aComponents2Panel.add(a5Field);
    aComponents2Panel.add(a6Field);

    // Quaternion rotation
    JComponent qButton = new JButton (res.getString("EditorFor3DTransformation.AddQuaternionRotation"));
    qButton.setBorder(border);
    qButton.addMouseListener(new MouseAdapter(){
      public void mousePressed (MouseEvent evt) {
        boolean empty = true;
        String q1Str = q1Field.getText().trim(); if (q1Str.length()<=0) q1Str="0"; else empty = false;
        String q2Str = q2Field.getText().trim(); if (q2Str.length()<=0) q2Str="0"; else empty = false;
        String q3Str = q3Field.getText().trim(); if (q3Str.length()<=0) q3Str="0"; else empty = false;
        String q4Str = q4Field.getText().trim(); if (q4Str.length()<=0) q4Str="0"; else empty = false;
        if (empty) return;
        addToCurrentText("q:"+q1Str+","+q2Str+","+q3Str+","+q4Str);
      }
    });
    q1Field = new JTextField ();
    q2Field = new JTextField ();
    q3Field = new JTextField ();
    q4Field = new JTextField ();
    JPanel qPanel = new JPanel (new GridLayout(1,4));
    qPanel.add(q1Field);
    qPanel.add(q2Field);
    qPanel.add(q3Field);
    qPanel.add(q4Field);

    // final result
    JPanel labelsPanel = new JPanel (new GridLayout (0,1));
    labelsPanel.add(xButton);
    labelsPanel.add(yButton);
    labelsPanel.add(zButton);
    labelsPanel.add(vButton);
    labelsPanel.add(vLabel);
    labelsPanel.add(aButton);
    labelsPanel.add(aLabel);
    labelsPanel.add(qButton);

    Box valuesPanel = Box.createVerticalBox(); //new JPanel (new GridLayout (0,1));
    valuesPanel.add(xPanel);
    valuesPanel.add(yPanel);
    valuesPanel.add(zPanel);
    valuesPanel.add(vComponentsPanel);
    valuesPanel.add(vBottomPanel);
    valuesPanel.add(aComponentsPanel);
    valuesPanel.add(aComponents2Panel);
    valuesPanel.add(qPanel);


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


  static class RotationMouseListener extends MouseAdapter {
    private int axis;
    RotationMouseListener (int _axis) { axis = _axis; }

    public void mousePressed (MouseEvent evt) {
      String angleStr,prefix;
      boolean degrees;
      switch (axis) {
        default :
        case 1 : prefix = "x"; angleStr = xField.getText().trim(); degrees = xDegrees.isSelected(); break;
        case 2 : prefix = "y"; angleStr = yField.getText().trim(); degrees = yDegrees.isSelected(); break;
        case 3 : prefix = "z"; angleStr = zField.getText().trim(); degrees = zDegrees.isSelected(); break;
      }
      if (angleStr.length()<=0) return;
      String rotStr = prefix + ":"+angleStr;
      if (degrees) rotStr += "d"; //"º";
      addToCurrentText(rotStr);
    }
  }

}

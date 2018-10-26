/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejss.editors;

import java.awt.*;
import java.util.HashSet;

import javax.swing.*;

import org.colos.ejs.osejs.utils.InterfaceUtils;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.utils.TwoStrings;

public class EditorForColor {
  static private JDialog dialog, dialog2;
  static private JComponent showColor;
  static private JPanel  recentPanel;
  static private JTextField rField, gField, bField,alphaField;
  static private String returnValue=null;
  static private Color lastColor = null;
  static private ResourceUtil res = new ResourceUtil("Resources");
  static private boolean sShowAlpha = true;
  static private JPanel sAlphaPanel;
  
  static {
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent _evt) {
        JComponent button = (JComponent) _evt.getComponent();
        String cmd = button.getName();
        if (cmd.equals("ok")) {
          if (returnValue==null || returnValue.length()==0) returnValue = null;
          if (returnValue!=null && lastColor!=null) addColorToRecent (lastColor,returnValue);
          dialog.setVisible (false);
          dialog2.setVisible (false);
        }
        else if (cmd.equals("cancel")) {
          returnValue = null;
          lastColor = null;
          dialog.setVisible (false);
          dialog2.setVisible (false);
        }
        else if (cmd.equals("default"))  {
          showReturnColor (recentPanel.getBackground());
          lastColor = null;
          returnValue = "<default>";
          dialog.setVisible (false);
          dialog2.setVisible (false);
        }
        else if (cmd.equals("more"))  dialog2.setVisible (true);
        else {
          returnValue = cmd;
          lastColor = button.getBackground();
          showReturnColor (lastColor);
          if (_evt.getClickCount()>1) {
            if (returnValue.length()==0) returnValue = null;
            if (returnValue!=null && lastColor!=null) addColorToRecent (lastColor,returnValue);
            dialog.setVisible (false);
            dialog2.setVisible (false);
          }
        }
      }
    };

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.setName ("ok");
    okButton.addMouseListener (mouseListener);

    JButton defaultButton = new JButton (res.getString("EditorFor.Default"));
    defaultButton.setName ("default");
    defaultButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setName ("cancel");
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
//    buttonPanel.add (defaultButton);
    buttonPanel.add (cancelButton);

    JLabel colorLabel = new JLabel(res.getString("EditorForColor.BasicColors"),SwingConstants.CENTER);
    JPanel colorCenterPanel = new JPanel (new GridLayout(0,5));
    addColor (colorCenterPanel, "BLACK",     Color.black, mouseListener);
    addColor (colorCenterPanel, "DARKGRAY",  Color.darkGray, mouseListener);
    addColor (colorCenterPanel, "GRAY",      Color.gray, mouseListener);
    addColor (colorCenterPanel, "LIGHTGRAY", Color.lightGray, mouseListener);
    addColor (colorCenterPanel, "WHITE",     Color.white, mouseListener);
    addColor (colorCenterPanel, "BLUE",      Color.blue, mouseListener);
    addColor (colorCenterPanel, "CYAN",      Color.cyan, mouseListener);
    addColor (colorCenterPanel, "GREEN",     Color.green, mouseListener);
    addColor (colorCenterPanel, "MAGENTA",   Color.magenta, mouseListener);
    addColor (colorCenterPanel, "RED",       Color.red, mouseListener);
    addColor (colorCenterPanel, "PINK",      Color.pink, mouseListener);
    addColor (colorCenterPanel, "ORANGE",    Color.orange, mouseListener);
    addColor (colorCenterPanel, "YELLOW",    Color.yellow, mouseListener);
    addColor (colorCenterPanel, "200,220,208,1",    new Color(200,220,208), mouseListener);// FKH 021103

    JButton moreButton = new JButton (res.getString("EditorForColor.More"));
    moreButton.setName ("more");
    moreButton.addMouseListener (mouseListener);

    JPanel colorPanel = new JPanel (new BorderLayout());
    colorPanel.add (colorLabel,BorderLayout.NORTH);
    colorPanel.add (colorCenterPanel,BorderLayout.CENTER);
    colorPanel.add (moreButton,BorderLayout.SOUTH);

    class AL implements java.awt.event.ActionListener {
      public void actionPerformed (java.awt.event.ActionEvent evt) { readReturnColor(); }
    }
    AL act = new AL();

    JLabel rLabel = new JLabel(res.getString("EditorForColor.Red"));
    rField = new JTextField (10);
    rField.addActionListener (act);
    JLabel gLabel = new JLabel(res.getString("EditorForColor.Green"));
    gField = new JTextField (10);
    gField.addActionListener (act);
    JLabel bLabel = new JLabel(res.getString("EditorForColor.Blue"));
    bField = new JTextField (10);
    bField.addActionListener (act);
    JLabel alphaLabel = new JLabel(res.getString("EditorForColor.Alpha"));
    alphaField = new JTextField (10);
    alphaField.addActionListener (act);

    HashSet<JComponent> set = new HashSet<JComponent>();
    set.add(rLabel);
    set.add(gLabel);
    set.add(bLabel);
    set.add(alphaLabel);
    InterfaceUtils.makeSameDimension(set);
    
    JPanel rPanel = new JPanel(new BorderLayout());
    rPanel.add(rLabel,BorderLayout.WEST);
    rPanel.add(rField,BorderLayout.CENTER);

    JPanel gPanel = new JPanel(new BorderLayout());
    gPanel.add(gLabel,BorderLayout.WEST);
    gPanel.add(gField,BorderLayout.CENTER);

    JPanel bPanel = new JPanel(new BorderLayout());
    bPanel.add(bLabel,BorderLayout.WEST);
    bPanel.add(bField,BorderLayout.CENTER);

    sAlphaPanel = new JPanel(new BorderLayout());
    sAlphaPanel.add(alphaLabel,BorderLayout.WEST);
    sAlphaPanel.add(alphaField,BorderLayout.CENTER);

    JPanel rgbPanel = new JPanel (new GridLayout(4,1));
    rgbPanel.add(rPanel);
    rgbPanel.add(gPanel);
    rgbPanel.add(bPanel);
    rgbPanel.add(sAlphaPanel);
    

    //    JPanel rgbLeftPanel = new JPanel (new GridLayout(4,1));
//    rgbLeftPanel.add (rLabel);
//    rgbLeftPanel.add (gLabel);
//    rgbLeftPanel.add (bLabel);
//    rgbLeftPanel.add (alphaLabel);
//
//    JPanel rgbCenterPanel = new JPanel (new GridLayout(4,1));
//    rgbCenterPanel.add (rField);
//    rgbCenterPanel.add (gField);
//    rgbCenterPanel.add (bField);
//    rgbCenterPanel.add (alphaField);
//
//    JPanel rgbPanel = new JPanel (new BorderLayout());
//    rgbPanel.add (rgbLeftPanel,BorderLayout.WEST);
//    rgbPanel.add (rgbCenterPanel,BorderLayout.CENTER);

    showColor = new JLabel();
    showReturnColor (Color.black);
    returnValue = "black";

    JLabel selectionLabel = new JLabel(res.getString("EditorForColor.Selection"),SwingConstants.CENTER);
//    selectionLabel.setFont(label.getFont().deriveFont (Font.BOLD));
    JPanel valuePanel = new JPanel (new BorderLayout(0,0));
    valuePanel.add (selectionLabel,BorderLayout.NORTH);
    valuePanel.add (rgbPanel,BorderLayout.WEST);
    valuePanel.add (showColor,BorderLayout.CENTER);


    JLabel recentLabel = new JLabel(res.getString("EditorForColor.Recent"),SwingConstants.CENTER);
    recentPanel = new JPanel (new GridLayout(2,5));
    for (int i=0; i<10; i++)
      addColor (recentPanel, "<default>", recentPanel.getBackground(), mouseListener);
    JPanel recentFullPanel = new JPanel (new BorderLayout());
    recentFullPanel.add (recentLabel,BorderLayout.NORTH);
    recentFullPanel.add (recentPanel,BorderLayout.CENTER);

    JPanel northPanel = new JPanel (new BorderLayout(0,0));
    northPanel.add (colorPanel,BorderLayout.NORTH);
    northPanel.add (recentFullPanel,BorderLayout.SOUTH);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (sep1,java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new BorderLayout(0,0));
    dialog.getContentPane().add (northPanel,BorderLayout.NORTH);
    dialog.getContentPane().add (valuePanel,BorderLayout.CENTER);
    dialog.getContentPane().add (bottomPanel,BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) {
          returnValue = null;
          lastColor = null;
          dialog2.setVisible (false);
        }
      }
    );

    dialog.setSize (res.getDimension("EditorForColor.Size"));
    dialog.validate();
    dialog.setModal(true);

    JPanel moreColorsPanel = new JPanel (new GridLayout(0,5));
    for (int r=0; r<=256; r+=64) {
      if (r>255) r = 255;
      for (int g=0; g<=256; g+=64) {
        if (g>255) g = 255;
        for (int b=0; b<=256; b+=64) {
          if (b>255) b = 255;
          addColor (moreColorsPanel, r+","+g+","+b+",1", new Color (r,g,b), mouseListener);
        }
      }
    }
    dialog2 = new JDialog();
    dialog2.getContentPane().setLayout (new BorderLayout(0,0));
    dialog2.getContentPane().add (moreColorsPanel,BorderLayout.CENTER);
    dialog2.setSize (res.getDimension("EditorForColor.Size"));
    dialog2.setTitle(res.getString("EditorForColor.MoreColors"));
    dialog2.validate();
    dialog2.setModal(true);
  }

  private static void addColorToRecent (Color aColor, String aColorName) {
    int max = recentPanel.getComponentCount();
    JComponent comp = (JComponent) recentPanel.getComponent (max-1);
    for (int i=max-1; i>0; i--) {
      JComponent nextComp = (JComponent) (recentPanel.getComponent (i-1));
      comp.setBackground (nextComp.getBackground());
      comp.setBorder(BorderFactory.createLineBorder(nextComp.getBackground(), 10));
      comp.setName (nextComp.getName());
      comp = nextComp;
    }
    comp.setBackground (aColor);
    comp.setBorder(BorderFactory.createLineBorder(aColor, 10));
    comp.setName (aColorName);
  }

  private static void addColor (JPanel aPanel, String aColorName, Color aColor,
    java.awt.event.MouseListener aListener) {
    JComponent button = new JLabel ("");
    button.setSize(20,20);
    button.setBackground (aColor);
    button.setBorder(BorderFactory.createLineBorder(aColor, 10));
    button.setName (aColorName);
    button.addMouseListener (aListener);
    aPanel.add (button);
  }

  private static void showReturnColor (Color aColor) {
    showColor.setBackground (aColor);
    showColor.setBorder(BorderFactory.createLineBorder(aColor, 10));
    rField.setText (""+aColor.getRed());
    gField.setText (""+aColor.getGreen());
    bField.setText (""+aColor.getBlue());
    float alpha = aColor.getAlpha()/255.0f;
    alphaField.setText (""+(Math.round(alpha * 100)/100.0));
  }

  private static void readReturnColor () {
    int r=0,g=0,b=0;
    try {
      r = Integer.parseInt(rField.getText());
      if (r<0 || r>255) r = 0;
    } catch (Exception exc) { r = 0; }
    try {
      g = Integer.parseInt(gField.getText());
      if (g<0 || g>255) g = 0;
    } catch (Exception exc) { g = 0; }
    try {
      b = Integer.parseInt(bField.getText());
      if (b<0 || b>255) b = 0;
    } catch (Exception exc) { b = 0; }
    if (sShowAlpha) {
      float alpha=1.0f;
      try {
        alpha = Float.parseFloat(alphaField.getText());
        if (alpha<0 || alpha>1) alpha = 1;
      } catch (Exception exc) { alpha = 1; }
      lastColor = new Color (r,g,b,Math.round(alpha*255));
      returnValue = r+","+g+","+b+","+alpha;
    }
    else {
      lastColor = new Color (r,g,b);
      returnValue = r+","+g+","+b;
    }
//    System.err.println ("Return color is "+returnValue);
    showReturnColor (lastColor);
  }

  public static String edit (String _classname, String _property, String _type, JTextField returnField) {
    sAlphaPanel.setVisible(sShowAlpha);
    dialog.setLocationRelativeTo (returnField);
    dialog2.setLocationRelativeTo (returnField);
    dialog.setTitle (res.getString("EditorFor.ChooseOne"));
//    dialog.setTitle (res.getString(_property));
    dialog.setVisible (true);
    return returnValue;
  }

  public static Color editColor (JComponent _parentComponent) {
    dialog.setLocationRelativeTo (_parentComponent);
    dialog2.setLocationRelativeTo (_parentComponent);
    dialog.setTitle (res.getString("EditorFor.ChooseOne"));
    //  dialog.setTitle (res.getString(_property));
    dialog.setVisible (true);
    return lastColor;
  }

  public static String editWithoutAlpha (java.util.List<TwoStrings> optionsList, javax.swing.JTextField returnField) {
    sShowAlpha = false;
    String value = edit (null, null, null, returnField);
    if (value==null) return null;
    for (TwoStrings entry : optionsList) if (entry.getFirstString().equals(value)) return entry.getSecondString();
    return "rgb("+value+")";
  }

  public static String edit (java.util.List<TwoStrings> optionsList, javax.swing.JTextField returnField) {
    sShowAlpha = true;
    String value = edit (null, null, null, returnField);
    if (value==null) return null;
    for (TwoStrings entry : optionsList) if (entry.getFirstString().equals(value)) return entry.getSecondString();
    return "rgba("+value+")";
  }
  
}

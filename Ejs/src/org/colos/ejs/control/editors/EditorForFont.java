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

public class EditorForFont {
  static private JDialog dialog;
  static private JLabel sizeLabel;
  static private JTextArea showFont;
  static private JComboBox<String> nameBox, styleBox;
  static private JScrollBar sizeBar;
  static private String returnValue=null;
  static private Font returnFont=null;

  static private ResourceUtil res = new ResourceUtil("Resources");

  static {
    java.awt.event.ActionListener aListener = new java.awt.event.ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        if (evt.getSource() instanceof JButton) {
          String cmd = ((JButton) (evt.getSource())).getActionCommand();
          if (cmd.equals("ok")) dialog.setVisible (false);
          else if (cmd.equals("default"))  {
            returnValue = "<default>";
            returnFont = null;
            dialog.setVisible (false);
          }
          else if (cmd.equals("cancel")) {
            returnValue = null;
            returnFont = null;
            dialog.setVisible (false);
          }
        }
        else showReturnValue ();
      }
    };

    java.awt.event.AdjustmentListener adjListener = new java.awt.event.AdjustmentListener() {
      public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
        sizeLabel.setText(res.getString("EditorForFont.FontSize")+" ("+sizeBar.getValue()+")");
        showReturnValue ();
      }
    };

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.setActionCommand ("ok");
    okButton.addActionListener (aListener);

    JButton defaultButton = new JButton (res.getString("EditorFor.Default"));
    defaultButton.setActionCommand ("default");
    defaultButton.addActionListener (aListener);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setActionCommand ("cancel");
    cancelButton.addActionListener (aListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
//    buttonPanel.add (defaultButton);
    buttonPanel.add (cancelButton);


    JLabel nameLabel = new JLabel (res.getString("EditorForFont.FontName"));
    nameBox = new JComboBox<String>();
    String[] fontList = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for (int i=0; i<fontList.length; i++) nameBox.addItem (fontList[i]);
    nameBox.setSelectedItem("Monospaced"); // Dialog
    nameBox.addActionListener (aListener);

    JLabel styleLabel = new JLabel (res.getString("EditorForFont.FontStyle"));
    styleBox = new JComboBox<String>();
    styleBox.addItem (res.getString("EditorForFont.Plain"));
    styleBox.addItem (res.getString("EditorForFont.Bold"));
    styleBox.addItem (res.getString("EditorForFont.Italic"));
    styleBox.addItem (res.getString("EditorForFont.BoldItalic"));
    styleBox.setSelectedItem(res.getString("EditorForFont.Plain"));
    styleBox.addActionListener (aListener);

    sizeBar = new JScrollBar(SwingConstants.HORIZONTAL,10,1,5,50);
    sizeBar.addAdjustmentListener (adjListener);
    sizeLabel = new JLabel ();
    sizeLabel.setText(res.getString("EditorForFont.FontSize")+" ("+sizeBar.getValue()+")");
    JPanel familyLeftPanel = new JPanel (new GridLayout (3,1));
    familyLeftPanel.add (nameLabel);
    familyLeftPanel.add (styleLabel);
    familyLeftPanel.add (sizeLabel);
    JPanel familyCenterPanel = new JPanel (new GridLayout (3,1));
    familyCenterPanel.add (nameBox);
    familyCenterPanel.add (styleBox);
    familyCenterPanel.add (sizeBar);
    JPanel familyPanel = new JPanel (new BorderLayout (5,0));
    familyPanel.add (familyLeftPanel,BorderLayout.WEST);
    familyPanel.add (familyCenterPanel,BorderLayout.CENTER);

    showFont = new JTextArea ("ABCDEFGHIJKLMNOPQRSTUVWXYZ\n"+
      "abcdefghijklmnopqrstuvwxyz\n"+
      "0123456789-=\\`!@#$%^&*()_+|~,./<>?");
    showFont.setEditable (false);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel northPanel = new JPanel (new BorderLayout(0,0));
    northPanel.add (familyPanel,BorderLayout.NORTH);
    northPanel.add (showFont   ,BorderLayout.CENTER);
    northPanel.add (sep1       ,BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new BorderLayout(0,0));
    dialog.getContentPane().add (northPanel,BorderLayout.CENTER);
    dialog.getContentPane().add (buttonPanel,BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) {
          returnValue = null;
          returnFont = null;
        }
      }
    );
    dialog.setSize (res.getDimension("EditorForFont.Size"));
    dialog.validate();
    dialog.setModal(true);
    showReturnValue();
  }

  private static void showReturnValue () {
    String name  = (String) (nameBox.getSelectedItem());
    String style = (String) (styleBox.getSelectedItem());
    int iStyle = Font.PLAIN;
    if (style.equals(res.getString("EditorForFont.Bold")))  {
      iStyle = Font.BOLD;
      style  = "BOLD";
    }
    else if (style.equals(res.getString("EditorForFont.Italic"))) {
      iStyle = Font.ITALIC;
      style  = "ITALIC";
    }
    else if (style.equals(res.getString("EditorForFont.BoldItalic"))) {
      iStyle = Font.ITALIC|Font.BOLD;
      style  = "ITALIC|BOLD";
    }
    else style = "PLAIN";
    int size = sizeBar.getValue();
    showFont.setFont (returnFont = new Font(name,iStyle,size));
    returnValue = name+","+style+","+size;
  }

  public static String edit (String _classname, String _property, String _type, JTextField returnField) {
    return edit (_property, returnField);
  }

  public static String edit (String varName, JTextField returnField) {
    String value = returnField.getText();
    if (value.indexOf("Font")!=-1) {
      String numbers = value.substring (value.indexOf('(')+1,value.length()-1);
      StringTokenizer l = new StringTokenizer (numbers,",");
      try {
        String name = l.nextToken();
        nameBox.setSelectedItem (name.substring(1,name.length()-1));
        String style = l.nextToken();
        if (style.indexOf("Font.ITALIC")>=0 && style.indexOf("Font.BOLD")>=0)
          styleBox.setSelectedItem(res.getString("EditorForFont.BoldItalic"));
        else if (style.indexOf("Font.BOLD")>=0)   styleBox.setSelectedItem(res.getString("EditorForFont.Bold"));
        else if (style.indexOf("Font.ITALIC")>=0) styleBox.setSelectedItem(res.getString("EditorForFont.Italic"));
        else styleBox.setSelectedItem(res.getString("EditorForFont.Plain"));
        int size = Integer.parseInt(l.nextToken());
        sizeBar.setValue(size);
      } catch (Exception exc) { }
    }
    showReturnValue ();
    dialog.setLocationRelativeTo (returnField);
    dialog.setTitle (res.getString("EditorForFont.ChooseAFont"));
//    dialog.setTitle (res.getString(varName));
    dialog.setVisible (true);
    return returnValue;
  }

  public static void edit (Component callingComp) {
    dialog.setLocationRelativeTo (callingComp);
    dialog.setTitle (res.getString("EditorFor.ChooseOne"));
    dialog.setVisible (true);
  }


  public static String getFontName () { return returnValue; }

  public static Font getFont () { return returnFont; }

}

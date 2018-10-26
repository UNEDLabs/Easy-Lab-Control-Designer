/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejss.editors;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.utils.TwoStrings;

public class EditorForFont {
  static private JDialog dialog;
  static private JLabel sizeLabel;
  static private JComboBox<String> nameBox, styleBox;
  static private JScrollBar sizeBar;
  static private String returnValue=null;

  static private ResourceUtil res = new ResourceUtil("Resources");

  static {
    java.awt.event.ActionListener aListener = new java.awt.event.ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        if (evt.getSource() instanceof JButton) {
          String cmd = ((JButton) (evt.getSource())).getActionCommand();
          if (cmd.equals("ok")) dialog.setVisible (false);
          else if (cmd.equals("default"))  {
            returnValue = "<default>";
            dialog.setVisible (false);
          }
          else if (cmd.equals("cancel")) {
            returnValue = null;
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

    EmptyBorder border = new EmptyBorder(0,5,0,0);
    
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
    nameLabel.setBorder(border);
    nameBox = new JComboBox<String>();
    nameBox.addItem ("Default");
    nameBox.addItem ("Georgia, serif");
    nameBox.addItem ("\"Palatino Linotype\", \"Book Antiqua\", Palatino, serif");
    nameBox.addItem ("\"Times New Roman\", Times, serif");
    nameBox.addItem ("Arial, Helvetica, sans-serif");
    nameBox.addItem ("\"Arial Black\", Gadget, sans-serif");
    nameBox.addItem ("\"Comic Sans MS\", cursive, sans-serif");
    nameBox.addItem ("Impact, Charcoal, sans-serif");
    nameBox.addItem ("\"Lucida Sans Unicode\", \"Lucida Grande\", sans-serif");
    nameBox.addItem ("Tahoma, Geneva, sans-serif");
    nameBox.addItem ("\"Trebuchet MS\", Helvetica, sans-serif");
    nameBox.addItem ("Verdana, Geneva, sans-serif");
    nameBox.addItem ("\"Courier New\", Courier, monospace");
    nameBox.addItem ("\"Lucida Console\", Monaco, monospace");
    nameBox.setSelectedIndex(0);
    nameBox.addActionListener (aListener);

    JLabel styleLabel = new JLabel (res.getString("EditorForFont.FontStyle"));
    styleLabel.setBorder(border);
    styleBox = new JComboBox<String>();
    styleBox.addItem (res.getString("EditorForFont.Plain"));
    styleBox.addItem (res.getString("EditorForFont.Bold"));
    styleBox.addItem (res.getString("EditorForFont.Italic"));
    styleBox.addItem (res.getString("EditorForFont.BoldItalic"));
    styleBox.setSelectedIndex(0);
    styleBox.addActionListener (aListener);

    sizeBar = new JScrollBar(SwingConstants.HORIZONTAL,10,1,5,50);
    sizeBar.addAdjustmentListener (adjListener);
    sizeLabel = new JLabel (res.getString("EditorForFont.FontSize")+" ("+sizeBar.getValue()+")");
    sizeLabel.setBorder(border);
    
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

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel northPanel = new JPanel (new BorderLayout(0,0));
    northPanel.add (familyPanel,BorderLayout.NORTH);
    northPanel.add (sep1       ,BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new BorderLayout(0,0));
    dialog.getContentPane().add (northPanel,BorderLayout.CENTER);
    dialog.getContentPane().add (buttonPanel,BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) {
          returnValue = null;
        }
      }
    );
    dialog.validate();
    dialog.pack();
    dialog.setModal(true);
    showReturnValue();
  }

  private static void showReturnValue () {
    String name  = (String) (nameBox.getSelectedItem());
    if (name.equals("Default")) name = "";
    String style;
    switch (styleBox.getSelectedIndex()) {
      default:
      case 0 : style = "normal normal"; break;
      case 1 : style = "normal bold";   break;
      case 2 : style = "italic normal"; break;
      case 3 : style = "italic bold";   break;
    }
    int size = sizeBar.getValue();
    returnValue = style + " " + size + "px " + FileUtils.replaceString(name, "\"", "\\\"");
  }

  public static String edit (java.util.List<TwoStrings> list, JTextField returnField) {
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

}

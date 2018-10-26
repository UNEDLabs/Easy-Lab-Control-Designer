/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.wizards;

import org.colos.ejs.osejs.utils.ResourceUtil;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.*;

public class CodeWizard {

  static private final ResourceUtil res = new ResourceUtil("Resources");
  static private final ResourceUtil sysRes = new ResourceUtil ("SystemResources");
  static private JDialog dialog;
  static private String returnValue=null;
  static private JTextComponent textArea=null;
  static private JComboBox<String> wizardCombo=null;
  static private JTextField wizardDescriptionField=null;
  static private String[] wizardNames=null, wizardDesc=null;
  static private Wizard[] wizardClass=null;

  static {
    // Top panel
    wizardNames = ResourceUtil.tokenizeString(sysRes.getString("CodeWizards.Wizards"));
    String[] wizardRes = new String[wizardNames.length];
    wizardDesc = new String[wizardNames.length];
    wizardClass = new Wizard[wizardNames.length];
    for (int j=0; j<wizardNames.length; j++) {
      wizardRes[j] = res.getString("CodeWizard."+wizardNames[j]);
      wizardDesc[j] = res.getString("CodeWizardDescription."+wizardNames[j]);
    }

    wizardDescriptionField = new JTextField();
    wizardDescriptionField.setEditable(false);

    ActionListener cal = new ActionListener() {
      public void actionPerformed(ActionEvent _e) {
        int sel = wizardCombo.getSelectedIndex();
        if (wizardClass[sel]==null) {
          try {
            Class<?> aClass =  Class.forName("org.colos.ejs.osejs.edition.wizards."+wizardNames[sel]+"Wizard");
            wizardClass[sel] = (Wizard) aClass.newInstance();
          }
          catch (Exception exc) { exc.printStackTrace(); }
        }
        if (wizardClass[sel]!=null) textArea.setText(wizardClass[sel].getCode());
        wizardDescriptionField.setText(wizardDesc[sel]);
      }
    };
    wizardCombo = new JComboBox<String>();
    for (int i=0; i<wizardNames.length; i++) {
      wizardCombo.addItem(wizardRes[i]);
    }
    wizardCombo.addActionListener(cal);

    JPanel wizardTopPanel = new JPanel (new java.awt.BorderLayout());
    wizardTopPanel.add (new JLabel(" "+res.getString("CodeWizard.ConstructionLabel")+" "),java.awt.BorderLayout.WEST);
    wizardTopPanel.add (wizardCombo,java.awt.BorderLayout.CENTER);

    JPanel wizardBottomPanel = new JPanel (new java.awt.BorderLayout());
    wizardBottomPanel.add (new JLabel(" "+res.getString("CodeWizard.DescriptionLabel")+" "),java.awt.BorderLayout.WEST);
    wizardBottomPanel.add (wizardDescriptionField,java.awt.BorderLayout.CENTER);

/*
    JPanel wizardPanel = new JPanel (new java.awt.BorderLayout());
    wizardPanel.add (wizardTopPanel,java.awt.BorderLayout.NORTH);
    wizardPanel.add (wizardBottomPanel,java.awt.BorderLayout.CENTER);
*/
    JPanel topPanel = new JPanel (new java.awt.BorderLayout());
    topPanel.add (wizardTopPanel,java.awt.BorderLayout.NORTH);
    topPanel.add (wizardBottomPanel,java.awt.BorderLayout.CENTER);
    topPanel.add (new JSeparator (SwingConstants.HORIZONTAL),java.awt.BorderLayout.SOUTH);

    // Central panel
    textArea = new JTextArea();
    JScrollPane scrollPanel = new JScrollPane(textArea);

    // Bottom panel
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
          returnValue = finalValue();
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

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setActionCommand ("cancel");
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
    buttonPanel.add (cancelButton);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (new JSeparator (SwingConstants.HORIZONTAL),java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    // Now, put everything together
    dialog = new JDialog();
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(0,0));
    dialog.getContentPane().add (topPanel,java.awt.BorderLayout.NORTH);
    dialog.getContentPane().add (scrollPanel,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { returnValue = null; }
      }
    );

    wizardCombo.setSelectedIndex(0);
    wizardDescriptionField.setText(wizardDesc[0]);
    try {
      Class<?> aClass =  Class.forName("org.colos.ejs.osejs.edition.wizards."+wizardNames[0]+"Wizard");
      wizardClass[0] = (Wizard) aClass.newInstance();
      textArea.setText(wizardClass[0].getCode());
    } catch (Exception exc) { exc.printStackTrace(); }

    dialog.setSize (res.getDimension("CodeWizard.Size"));
    dialog.setTitle (res.getString("CodeWizard.Title"));
    dialog.validate();
    dialog.setModal(true);

  }

 /**
  * Edits code for a TextComponent. Null implies Cancel
  */
  static public String edit (javax.swing.text.JTextComponent _target) {
    textArea.setFont(_target.getFont());
    //textArea.setText("");
    dialog.setLocationRelativeTo (_target);
    dialog.setVisible (true);
    return returnValue;
  }

  /**
   * This is what the wizard returns on OK
   */
  public static String finalValue () {
    return textArea.getText();
  }

} // end of class


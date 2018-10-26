/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) May 2005 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

import java.awt.*;
import javax.swing.*;

/**
 * A base class to store and retrieve data
 */

public class Input extends Memory {
  static public final int CONFIRM = 0;
  static public final int YES_NO  = 1;
  static public final int YES_NO_CANCEL = 2;

  static public final int YES = JOptionPane.YES_OPTION;
  static public final int NO  = JOptionPane.NO_OPTION;
  static public final int CANCEL  = JOptionPane.CANCEL_OPTION;
  static public final int CLOSED  = JOptionPane.CLOSED_OPTION;
  static public final int OK  = JOptionPane.OK_OPTION;

  private boolean ok=true;

  public boolean inputVariables (String message, String variables){
    String[] varListArray = variables.split(",");
    int varNumber = varListArray.length;
    String[] _headerTitle={ getResource("Experiment.InputDialog.Variable"),
                            getResource("Experiment.InputDialog.Value") };
    Object[][] _dataTable=new Object[varNumber][2];
    for(int i=0;i<varNumber;i++){
     _dataTable[i][0]=varListArray[i];
     _dataTable[i][1]=hashTable.get(varListArray[i]);
    }
    JTable inputTable = new JTable(_dataTable,_headerTitle);
    inputTable.setPreferredScrollableViewportSize(new Dimension(400, 16*varNumber));
    inputTable.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    JScrollPane scrollPane = new JScrollPane(inputTable);

    final JDialog inputDialog = new JDialog ((JFrame)null,message,true);

    JButton okButton = new JButton(getResource("Ok"));
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        ok = true;
        inputDialog.setVisible(false);
      }
    });

    JButton cancelButton = new JButton(getResource("Cancel"));
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        ok = false;
        inputDialog.setVisible(false);
      }
    });

    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonsPanel.add(okButton);
    buttonsPanel.add(cancelButton);

    inputDialog.getContentPane().add(scrollPane,BorderLayout.NORTH);
    inputDialog.getContentPane().add(buttonsPanel,BorderLayout.CENTER);
    inputDialog.pack();

    ok = false;
    inputDialog.setVisible(true);
    if (ok) {
      for (int i = 0; i < inputTable.getRowCount(); i++) {
        Object value = inputTable.getValueAt(i, 1);
        if (value!=null) readInput ((String) inputTable.getValueAt(i, 0),value.toString());
      }
    }
    inputDialog.dispose();
    return ok;
  }

  public void readInput (String variable, String value) {
    if (value.equals("true"))  { setValue (variable,true); return; }
    if (value.equals("false")) { setValue (variable,false); return; }
    try {
      Integer test = new Integer(value);
      setValue(variable,test.intValue());
      return;
    } catch (Exception exc) {}; // Do nothing
    try {
      Double test = new Double(value);
      setValue(variable,test.doubleValue());
      return;
    } catch (Exception exc) {}; // Do nothing
    setValue (variable,value);
    return;
  }

     /**
      * Prompts the user to confirm a message
      * @param message String
      * @param type int
      * @return int
      */
     public int confirmMessage(String message, int type) {
       switch (type) {
         case YES_NO  : return JOptionPane.showConfirmDialog(null,message,getResource("Experiment.Title"),JOptionPane.YES_NO_OPTION);
         case YES_NO_CANCEL : return JOptionPane.showConfirmDialog(null,message,getResource("Experiment.Title"),JOptionPane.YES_NO_CANCEL_OPTION);
         default : JOptionPane.showMessageDialog(null,message,getResource("Experiment.Title"),JOptionPane.INFORMATION_MESSAGE); return OK;
       }
     }

     public int selectOption(String message,String options){
      String[] optionListArray;
      optionListArray=options.split(",");
      String selectedValue = (String) JOptionPane.showInputDialog(null,message,getResource("Experiment.Title"),JOptionPane.INFORMATION_MESSAGE, null,optionListArray, optionListArray[0]);
      int i=0;
      for (i=0; i<optionListArray.length; i++){
       if (optionListArray[i].equals(selectedValue)) return i;
      }
      return -1;
     }


} // End of class


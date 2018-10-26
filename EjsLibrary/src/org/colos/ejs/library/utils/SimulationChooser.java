/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.utils;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.opensourcephysics.tools.ResourceLoader;

/**
 * Displays a dialog to choose among simulations in a JAR file created with EJS
 */

public class SimulationChooser  {
  static public ResourceBundle ejsRes = ResourceBundle.getBundle("org.colos.ejs.library.resources.ejs_res", Locale.getDefault());

  static public void main(String[] args) {
    String text = ResourceLoader.getString("EJSSimulationList.xml");
    if (text==null) {
      System.err.println ("Could not find file EJSSimulationList.xml. Aborted.");
      System.exit(1);
    }
    String selection = selectOne(text);
    if (selection==null) {
      System.exit(0);
    }
//    System.out.println ("Class selected = "+selection);
    try {
      Class<?> theModel = Class.forName(selection);
      Class<?>[] c = { String[].class };
      java.lang.reflect.Method mainMethod = theModel.getMethod("main", c);
      Object[] o = { null };
      mainMethod.invoke(null, o);
    } 
    catch (Exception exc) {
      exc.printStackTrace();
    }
  }


  public static String selectOne(String _xmlText) {
    class ReturnValue {
      boolean value = false;
    }
    final ReturnValue returnValue = new ReturnValue();

    DefaultListModel listModel = new DefaultListModel();
    int begin = _xmlText.indexOf("<simulation");
    while (begin>=0) {
      int end = _xmlText.indexOf("</simulation>", begin);
      if (end<0) {
        System.err.println ("Incorrect syntax in file EJSSimulationList.xml. Aborted.");
        System.exit(2);
      }
      // Add the simulation to the list

      String simPiece = _xmlText.substring(begin,end+13);
      String name = getPiece(simPiece,"<title>","</title>");
      if (name==null || name.trim().length()<=0) name = getPiece(simPiece,"name=\"","\">");
      String classname = getPiece(simPiece,"<class>","</class>");
      listModel.addElement(new TwoStrings(name,classname));
      // Next entry
      _xmlText = _xmlText.substring(end+14);
      begin = _xmlText.indexOf("<simulation");
    }

    final JDialog dialog = new JDialog();
    final JList list = new JList(listModel);
    list.setEnabled(true);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addMouseListener(new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (evt.getClickCount()>1) {
          returnValue.value = true;
          dialog.setVisible(false);
          dialog.dispose();
        }
      }
    });
    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setPreferredSize(new Dimension (400,300));
    java.awt.event.MouseAdapter mouseListener = new java.awt.event.MouseAdapter() {
      public void mousePressed(java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if(aCmd.equals("ok")) {                //$NON-NLS-1$ 
          returnValue.value = true;
        } 
        else if(aCmd.equals("cancel")) {     //$NON-NLS-1$ 
          returnValue.value = false;
        }
        dialog.setVisible(false);
        dialog.dispose();
      }
    };
    JButton okButton = new JButton(ejsRes.getString("Ok")); //$NON-NLS-1$
    okButton.setActionCommand("ok"); //$NON-NLS-1$
    okButton.addMouseListener(mouseListener);

    JButton cancelButton = new JButton(ejsRes.getString("Cancel")); //$NON-NLS-1$
    cancelButton.setActionCommand("cancel"); //$NON-NLS-1$
    cancelButton.addMouseListener(mouseListener);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    JTextArea textArea = new JTextArea(ejsRes.getString("SimulationChooser.ChooseOne"));

    JPanel topPanel = new JPanel(new BorderLayout());
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
    textArea.setBackground(topPanel.getBackground());
    textArea.setBorder(new javax.swing.border.EmptyBorder(5, 5, 10, 5));
    topPanel.setBorder(new javax.swing.border.EmptyBorder(5, 10, 5, 10));
    topPanel.add(textArea, BorderLayout.NORTH);
    topPanel.add(scrollPane, BorderLayout.CENTER);

    JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
    JPanel southPanel = new JPanel(new java.awt.BorderLayout());
    southPanel.add(sep1, java.awt.BorderLayout.NORTH);
    southPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
    dialog.getContentPane().setLayout(new java.awt.BorderLayout(5, 0));
    dialog.getContentPane().add(topPanel, java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);
    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent event) {
        returnValue.value = false;
      }
    });
    //    dialog.setSize (_size);
    dialog.validate();
    dialog.pack();
    dialog.setTitle(ejsRes.getString("SimulationChooser.Title"));
    dialog.setModal(true);
    Rectangle bounds = org.colos.ejs.library.control.EjsControl.getDefaultScreenBounds();
    dialog.setLocation(bounds.x+(bounds.width - dialog.getSize().width)/2, bounds.y+(bounds.height - dialog.getSize().height)/2);
    dialog.setVisible(true);
    if (!returnValue.value) return null;
    Object selection = list.getSelectedValue();
    if (selection==null) return null;
    return ((TwoStrings)selection).secondString;
  }

  /**
   * A static utility that helps split code
   */
  static public String getPiece (String _text, String _begtag, String _endtag) {
    int begin = _text.indexOf(_begtag);
    if (begin<0) return null;
    int end = _text.indexOf(_endtag,begin);
    if (end<begin) return null;
    return _text.substring(begin+_begtag.length(),end);
  }

  static private class TwoStrings {
    String firstString, secondString;
    private TwoStrings(String first, String second) {
      this.firstString = first;
      this.secondString = second;
    }
    public String toString() { return firstString; }
  }

} // End of class



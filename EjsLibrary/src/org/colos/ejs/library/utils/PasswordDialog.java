/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.utils;

import java.io.IOException;
import java.awt.*;

import javax.swing.*;
import org.colos.ejs.library.Simulation;
import org.opensourcephysics.controls.Cryptic;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

public class PasswordDialog {
  static private JDialog dialog;
  static private String returnValue=null;
  static private JTextField field;

  static private void recreateDialog () {
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
          returnValue = field.getText();
          dialog.setVisible (false);
        }
        else if (aCmd.equals("cancel")) {
          returnValue = null;
          dialog.setVisible (false);
        }
      }
    };

    JButton okButton = new JButton (Simulation.getEjsString("Ok"));
    okButton.setActionCommand ("ok");
    okButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (Simulation.getEjsString("Cancel"));
    cancelButton.setActionCommand ("cancel");
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton); //,BorderLayout.WEST);
    buttonPanel.add (cancelButton); //,BorderLayout.EAST);

    // ----------- Input area ----------------------
    
    JLabel label = new JLabel(Simulation.ejsRes.getString("Password.Password"));
    label.setBorder(new javax.swing.border.EmptyBorder(0,3,0,3));
    field = new JPasswordField(20);
    field.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        returnValue = field.getText();
        dialog.setVisible (false);
      }
    });
    JTextArea message = new JTextArea ();
    message.setWrapStyleWord(true);
    message.setLineWrap(true);
    message.setEditable(false);
//    message.setEnabled(false);
    message.setText(Simulation.ejsRes.getString("Password.FileProtectedByPassword"));
    message.setBorder(BorderFactory.createEmptyBorder(5,3,5,3));

    JPanel panel = new JPanel (new BorderLayout());
    panel.add (label,BorderLayout.WEST);
    panel.add (field,BorderLayout.CENTER);
    panel.add (message,BorderLayout.NORTH);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (new JSeparator(SwingConstants.HORIZONTAL),java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.CENTER);

    dialog = new JDialog();
    dialog.setTitle(Simulation.getEjsString("Password.Password"));
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(0,0));
    dialog.getContentPane().add (panel,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) {
          returnValue = null;
        }
      }
    );

    dialog.setModal(true);
    dialog.validate();
    dialog.pack();
  }

  /*
  static public boolean verifyPassword(String _codedPassword,String _passedPassword) {
//    System.out.println ("Coded password = "+_codedPassword);
//    System.out.println ("Passed password = "+_passedPassword);
    if (_codedPassword==null || _codedPassword.length()<=0) return true;
    if (_passedPassword==null || _passedPassword.length()<=0) return false; 
    Cryptic cryptic = new Cryptic("");
    System.out.println ("Coded password after recoding it = <"+cryptic.encrypt(_codedPassword)+">");
    return cryptic.encrypt(_codedPassword).equals(_passedPassword);
  }    
*/
  
  /**
   * Checks for a password protection and asks for it
   * @param input
   * @return the decrypted code, null if the password was incorrect
   */
  static public String checkPassword(String _codedPassword,javax.swing.JComponent _parent, Window _messageFrame) {
    if (_codedPassword==null || _codedPassword.length()<=0) return "ok";
    if (_messageFrame!=null) _messageFrame.setVisible(true);
    Cryptic cryptic = new Cryptic("");
    recreateDialog(); // The dialog needs to be recreated each time or, otherwise, an applet will hang the browser if reloaded
    String label2 = Simulation.ejsRes.getString("Password.AttemptsLeft");
    dialog.setLocationRelativeTo(_parent);
    for (int i=3; i>0; i--) {
      field.setText("");
      field.requestFocusInWindow();
      dialog.setVisible (true);
      String password = returnValue;
      if (password==null) {
        if (_messageFrame!=null) _messageFrame.setVisible(false);
        return null;
      }
      if (cryptic.encrypt(password).equals(_codedPassword)) {
        if (_messageFrame!=null) _messageFrame.setVisible(false);
        return password;
      }
      if (i>1) JOptionPane.showMessageDialog(_parent, Simulation.ejsRes.getString("Password.IncorrectPassword")+"\n"+(i-1)+" "+label2, 
          Simulation.ejsRes.getString("Error"),JOptionPane.ERROR_MESSAGE);
    }
    if (_messageFrame!=null) _messageFrame.setVisible(false);
    return null;
  }

  static public Window showInformationPage (String _modelName, String _title, String _htmlPage, int width, int height) {
    String title = Simulation.ejsRes.getString("DescriptionFor")+" "+_modelName;
    JDialog descriptionDialog = new JDialog (); //(Window)null,title); This is 1.6
    descriptionDialog.setTitle(title);
    JTabbedPane descriptionPanel = new JTabbedPane();
    if (width<=0) width = 600;
    if (height<=0) height = 400;
    descriptionDialog.getContentPane().add(descriptionPanel,java.awt.BorderLayout.CENTER);
    Rectangle bounds = org.colos.ejs.library.control.EjsControl.getDefaultScreenBounds();
    width = Math.min(width, bounds.width-10);
    height = Math.min(height, bounds.height-10);
    descriptionDialog.setLocation(bounds.x+(bounds.width - width)/2,bounds.y+(bounds.height - height)/2);
    JEditorPane editorPane = new JEditorPane(){
//      public void paintComponent(java.awt.Graphics g) {
//        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
//        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,java.awt.RenderingHints.VALUE_RENDER_QUALITY);
//        super.paintComponent(g2);
//      }
    };
//    editorPane.putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,Boolean.TRUE );
    editorPane.setEditable(false);
//    editorPane.addHyperlinkListener(new HyperlinkListener() {
//      public void hyperlinkUpdate(HyperlinkEvent e) {
//        if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
//          openURL(e.getSource(),e.getURL(),getView().getComponent(getMainWindow()), model._getApplet()!=null);
//        }
//      }
//    });

    Resource htmlRes = ResourceLoader.getResource(_htmlPage);
    //    System.out.println ("Loading html page = "+_htmlPage);
    if (htmlRes==null) {
      System.err.println("Couldn't find description file: "+_htmlPage);
      return null;
    }
    try {
      editorPane.setPage(htmlRes.getURL());
      JScrollPane editorScrollPane = new JScrollPane(editorPane);
      editorScrollPane.setPreferredSize(new Dimension(width,height));
      descriptionPanel.add(_title,editorScrollPane);
      descriptionDialog.pack();
    } 
    catch (IOException e) {
      System.err.println("Attempted to read a bad URL: " + htmlRes.getURL());
      return null;
    }
    return descriptionDialog;
  }

  
}

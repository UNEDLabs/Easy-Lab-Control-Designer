/**
 * The utils package contains generic utilities
 * Copyright (c) November 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import org.colos.ejs.osejs.Osejs;
import com.cdsc.eje.gui.*;

public class EditorForCode {
  static private JDialog dialog;
  static private JTextComponent textComponent;
  static private String returnValue=null;
  static private Font font;

  static private final ResourceUtil res = new ResourceUtil("Resources");
//  static private final ResourceUtil sysRes = new ResourceUtil("SystemResources");

  public EditorForCode (Osejs _ejs) {
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

    //textComponent = new JTextArea();
    textComponent = new EJEArea(_ejs);// editor.getTextArea();

    textComponent.setFont(font=InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    JScrollPane scrollPanel = new JScrollPane(textComponent);
    new Undo2(textComponent,_ejs.getModelEditor());

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (sep1,java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(0,0));
    dialog.getContentPane().add (scrollPanel,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { returnValue = null; }
      }
    );

    dialog.setSize (res.getDimension("EditorForVariables.Size"));
    dialog.validate();
    dialog.setModal(true);

  }

//  public static void setEditable (boolean _edit) { textEditor.getTextPane().setEditable(_edit); }

  public void setFont (Font _font) {
    if (_font!=font) textComponent.setFont(font=_font);
//    if (_font!=font) textEditor.getTextPane().setFont(font=_font);
  }

  public String finalValue () {
//    return textEditor.getTextPane().getText();
    return textComponent.getText();
  }

  public String edit (String _element, String _property, javax.swing.text.JTextComponent _returnField) {
    return edit (_element, _property, _returnField,-1); // backwards compatibility
  }

  public String edit (String _element, String _property, javax.swing.text.JTextComponent _returnField, int caret) {
//    textEditor.getTextPane().setText(_returnField.getText());
    textComponent.setText(_returnField.getText());
    if (caret>=0) {
      textComponent.setCaretPosition(caret);
      textComponent.requestFocusInWindow();
    }
    dialog.setLocationRelativeTo (_returnField);
    dialog.setTitle (res.getString("CodeEditor.CodeFor")+" " + _element + " " +_property);
    dialog.setVisible (true);
    return returnValue;
  }

}

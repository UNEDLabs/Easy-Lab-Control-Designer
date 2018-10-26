/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs;

import org.colos.ejs.osejs.utils.*;

// --- Graphic packages

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

//--------------------

public class OutputArea extends java.io.OutputStream implements org.colos.ejss.xml.ErrorOutput {
  private JPanel panel;
  private JTextArea textArea;
  static private ResourceUtil res = new ResourceUtil("Resources");

  public OutputArea () {
    textArea = new JTextArea ();
    // PrintStream ps = new PrintStream(this);
    // System.setOut(ps);
    // System.setErr(ps);

    JButton clearButton = new JButton (res.getString ("EjsConsole.ClearArea"));
//    clearButton.setFont (InterfaceUtils.font(null,res.getString("Output.TitleFont")));
    clearButton.setMargin(new Insets(0,5,0,5));
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent _evt) { textArea.setText(""); }
    });

    JLabel label = new JLabel (res.getString ("Output.Title"));
    label.setBorder(new EmptyBorder(2,0,2,0));
    label.setFont (InterfaceUtils.font(null,res.getString("Output.TitleFont")));

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(label,BorderLayout.WEST);
    topPanel.add(clearButton,BorderLayout.EAST);

    panel = new JPanel(new BorderLayout());
    panel.setBorder (new EmptyBorder(0,4,4,4));
    panel.add (topPanel,BorderLayout.NORTH);
    panel.add (new JScrollPane (textArea),BorderLayout.CENTER);
//    if (org.opensourcephysics.display.OSPRuntime.isMac()) panel.setPreferredSize(res.getDimension("MAC.Output.Size"));
//    else 
    panel.setPreferredSize(res.getDimension("Output.Size"));
  }

  public Component getComponent () { return panel; }

  public JTextArea textArea() { return textArea; }

  public void setFont (Font _font) { textArea.setFont (_font); }

  public void clear () { textArea.setText (""); textArea.repaint(); }

  public void message (String _prefix, String _text)   { println (res.getString(_prefix)+" "+_text); }

  public void println (String _text)   { 
    textArea.append (_text+"\n");
    textArea.repaint();
    textArea.setCaretPosition (textArea.getText().length());
  }

  //FKH 20021020 show Chinese error message if error occured when compile
//private int max=256,count=0;
//private byte[] buf=new byte[max];

  public void write(int i) throws java.io.IOException {
    textArea.append(new String(new char[]{(char)i}));
    if (i=='\n') {
      textArea.setCaretPosition (textArea.getText().length());
      textArea.repaint();
    }
/*
    if(count<max){
      buf[count]=(byte)i;
      count++;
      if(i==13 || count>=max){
        clean();
      }
    }
    */
  }
  
/*FKH
  public void clean(){
    if(count>0){
      textArea.append(new String(buf,0,count-1));
      textArea.setCaretPosition (textArea.getText().length());
      count=0;
    }
  }
  */
} // end of class outputArea





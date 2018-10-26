/**
 * The html package contains generic tools to view and edit HTML pages
 * Copyright (c) August 2010 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * @version August 2010
 */

package org.colos.ejs.osejs.edition.html;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;

import org.colos.ejs.osejs.utils.*;

/**
 * This class uses a JEditorPane to display an HTML document from file
 * @author Paco
 *
 */
class BasicViewer implements HtmlComponent {
  static protected ResourceUtil res = new ResourceUtil ("Resources");
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");

  private HtmlEditor htmlEditor;
  private JEditorPane htmlArea;
  private JScrollPane htmlScroll;
  private JTextField fileField;
  private JComponent mainPanel;

  BasicViewer (HtmlEditor _editor) {
    htmlEditor = _editor;
    // Create and configure the HTML viewer
    htmlArea = new JEditorPane ();
    HTMLEditorKit kit = new HTMLEditorKit();
    htmlArea.setEditorKit(kit);
    htmlArea.setDocument(kit.createDefaultDocument());
//    htmlArea.setContentType ("text/html");
    htmlArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    htmlArea.setEditable(false);
    if (htmlEditor.getFont()!=null) htmlArea.setFont(htmlEditor.getFont());
    // Set the doc base so that it can correctly load the images
    try {
      String prefix = "file:///"+ FileUtils.getPath(htmlEditor.getEjs().getSourceDirectory());
      if (!prefix.endsWith("/")) prefix += "/";
      javax.swing.text.html.HTMLDocument doc = (javax.swing.text.html.HTMLDocument) htmlArea.getDocument();
      doc.setBase(new java.net.URL(prefix));
    }
    catch (Exception exc) { exc.printStackTrace(); }
//    File cssFile = HtmlEditor.getCssFile(htmlEditor.getEjs());
//    try { kit.getStyleSheet().loadRules(new FileReader(cssFile),null); }
//    catch (Exception exc) {
//      htmlEditor.getEjs().print("Warning! Error loading CSS file from "+FileUtils.getPath(cssFile));
//      exc.printStackTrace();
//    }
    
    // Make hyperlinks work
    htmlArea.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
          org.opensourcephysics.desktop.OSPDesktop.displayURL(e.getURL().toString());
      }
    });
    // Report changes if resized. This is necessary because description pages need to have the (last) size of this panel
    htmlArea.addComponentListener(new java.awt.event.ComponentAdapter(){
      public void componentResized(java.awt.event.ComponentEvent _evt) { htmlEditor.setChanged(true); }
    });
    // Make it scrollable
    htmlScroll = new JScrollPane(htmlArea);

    // The bottom panel to select the external HTML file
    // The label
    JLabel fileLabel = new JLabel (res.getString ("HTMLEditor.FileLabel"),SwingConstants.CENTER);
    fileLabel.setBorder(new EmptyBorder(0,5,0,5));
    // The text field
    fileField = new JTextField();
    fileField.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        String filename = fileField.getText().trim();
        if (filename.length()<=0) return;
        readHtml (); 
      }
    });

//    JButton fileButton = new JButton (org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
//    fileButton.setToolTipText(res.getString("HTMLEditor.SetHtmlFile"));
//    fileButton.setRequestFocusEnabled(false);
//    fileButton.setMargin(new Insets (0,0,0,0));
//    fileButton.addActionListener(new ActionListener() {
//      public void actionPerformed (ActionEvent _evt) {
//        File htmlFile = htmlEditor.chooseHtmlFile();
//        if (htmlFile==null) return; // The user canceled it
//        fileField.setText(htmlEditor.getEjs().getRelativePath(htmlFile));
//        readHtml();
//      }
//    });

    JButton refreshButton = new JButton (org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("HTMLEditor.Refresh.Icon")));
    refreshButton.setToolTipText(res.getString("HTMLEditor.Refresh"));
    refreshButton.setRequestFocusEnabled(false);
    refreshButton.setMargin(new Insets (0,0,0,0));
    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) { readHtml (); }
    });

    JPanel buttonsPanel = new JPanel(new GridLayout(1,0));
//    buttonsPanel.add(fileButton);
    buttonsPanel.add(refreshButton);

    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(fileLabel,BorderLayout.WEST);
    bottomPanel.add(fileField,BorderLayout.CENTER);
    bottomPanel.add(buttonsPanel,BorderLayout.EAST);

    // Put everything together
    mainPanel = new JPanel (new BorderLayout());
    mainPanel.add(bottomPanel,BorderLayout.SOUTH);
    mainPanel.add (htmlScroll,BorderLayout.CENTER);     
    mainPanel.setBorder (new EmptyBorder(5,2,0,2));
  }

  // ------------------------------
  // Basic operation
  // ------------------------------
  
  public Component getComponent () { return mainPanel; }

  public JTextComponent getTextComponent () { return htmlArea; }

  public String getHtmlFile() { return fileField.getText().trim(); }

  public void setHtmlFile(String _filename) {
    _filename = _filename.trim();
    fileField.setText(_filename);
    readHtml();
  }

  public void setFont (Font _font) { htmlArea.setFont (_font); }

  public void clear () { htmlArea.setText (""); }

//  public void refresh (boolean _hiddensToo) {
//    htmlScroll.setVisible (htmlEditor.isVisible() || _hiddensToo);
//  }
  
  public boolean isEmpty () { return fileField.getText().trim().length()<=0; }
  
  private void readHtml () {
    String filename = fileField.getText().trim();
    if (filename.length()<=0) return;
    File file = HtmlEditor.convertToAbsoluteFile(htmlEditor.getEjs(),filename);
    if (!file.exists()) {
      JOptionPane.showMessageDialog(getComponent(), res.getString("Osejs.File.ReadError")+" "+filename,
        res.getString("Osejs.File.ReadingError"), JOptionPane.INFORMATION_MESSAGE);
      htmlArea.setText ("");
      return;
    }
    javax.swing.text.html.HTMLDocument doc = (javax.swing.text.html.HTMLDocument) htmlArea.getDocument();
    try { doc.setBase(new URL(HtmlEditor.convertToAbsolutePath(htmlEditor.getEjs(),filename))); } 
    catch (Exception exc) { exc.printStackTrace(System.err); }
    htmlArea.setText(OneHtmlPage.removeMetaTags(FileUtils.readTextFile(file,null)));
    htmlArea.setCaretPosition(0);
    htmlEditor.setChanged(true);
  }

  
  

} // end of class

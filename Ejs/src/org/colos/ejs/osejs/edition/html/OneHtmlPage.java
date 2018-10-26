package org.colos.ejs.osejs.edition.html;

import java.awt.CardLayout;
import java.awt.Font;
import javax.swing.JPanel;

import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.FileUtils;
import org.opensourcephysics.controls.OSPLog;

/**
 * An object with two html viewers: one is editable and the other is not because it is meant to display external HTML files.
 * The isExternal() method helps distinguish which of the viewers has precedence.
 */
public class OneHtmlPage {
  private HtmlEditor editor;
  private boolean external;
  private String title="";
  private BasicEditor editablePage; // created only if needed
  private BasicViewer externalPage; // created only if needed
  private CardLayout layout = new CardLayout();
  private JPanel panel = new JPanel(layout);
  
  OneHtmlPage(HtmlEditor _editor, boolean _external, String _code) {
    editor = _editor;
    setExternal(_external);
    setCode (_code);
  }
  
  // --------------------------
  // Setters and getters
  //--------------------------
  
  public javax.swing.JComponent getComponent() { return panel; }
  
  public void setExternal (boolean _external) {
    external = _external;
    if (external) { // make sure the corresponding viewer editor has been instantiated
      if (externalPage==null) {
        externalPage = new BasicViewer(editor);
        panel.add(externalPage.getComponent(),"external");
      }
    }
    else {
      if (editablePage==null) {
        editablePage = new BasicEditor(editor);
        panel.add(editablePage.getComponent(),"editable");
      }
    }
    if (external) layout.show(panel, "external");
    else layout.show(panel, "editable");
  }
  
  public boolean isExternal() { return external; }
  
  public void setTitle(String _title) { this.title = _title; }

  public String getTitle() { return title; }
  
  /**
   * Returns the code ready to be saved to file
   * @return
   */
  public String getCode () {
    if (external) return externalPage.getHtmlFile();
    return editor.convertToRelative(editablePage.getHtml());
  }    

  public String getLink() {
    if (!external) return null;
    String filename = externalPage.getHtmlFile();
    if (filename.startsWith("./")) filename = FileUtils.getRelativePath(editor.getEjs().getCurrentXMLFile().getParentFile(),editor.getEjs().getSourceDirectory(),false) + filename.substring(2);
    return filename;
  }

  /**
   * Returns the plain code
   * @return
   */
  public String getPlainCode () {
    if (external) return externalPage.getHtmlFile();
    return editablePage.getHtml();
  }    

  /**
   * Sets the code as read from file
   * @param _code
   */
  public void setCode (String _code) {
    if (external) externalPage.setHtmlFile(_code);
    else editablePage.setHtml(editor.convertToAbsolute(removeMetaTags(_code)));
  }
  
  static public String removeMetaTags(String _code) {
    if (_code.startsWith("<?xml ")) {
      int xmlEnd = _code.indexOf("?>");
      if (xmlEnd>0) _code = _code.substring(xmlEnd+2);
    }
    String headPiece = OsejsCommon.getPiece(_code,"<head>","</head>",true);
    if (headPiece!=null) {
      boolean mustReplaceHead=false;
      String newHead = headPiece;
      String metaTag = OsejsCommon.getPiece(headPiece,"<meta ",">",true);
      while (metaTag!=null) {
        if (metaTag.toLowerCase().indexOf("charset=")>=0) {
          OSPLog.fine("Description page : Removed head meta tag "+metaTag);
          newHead = FileUtils.replaceString(newHead, metaTag, "");
          mustReplaceHead = true;
        }
        headPiece = headPiece.substring(headPiece.indexOf(metaTag)+metaTag.length());
        metaTag = OsejsCommon.getPiece(headPiece,"<meta ",">",true);
      }
      if (mustReplaceHead) {
        headPiece = OsejsCommon.getPiece(_code,"<head>","</head>",true);
        _code = FileUtils.replaceString(_code, headPiece, newHead);
      }
    }
    return _code;
  }
  
  /**
   * Sets the editable condition of the page (if possible)
   * @param _editable
   */
  public void setEditable(boolean _editable) {
    if (editablePage!=null) editablePage.setEditable(_editable);
  }

  /**
   * Switches the editable condition
   */
  public void switchEditable() {
    if (editablePage!=null) editablePage.switchEditable();
  }

  public void setFont (Font _font) {
//    if (externalPage!=null) externalPage.setFont(_font);
//    else editablePage.setFont(_font);
  }
  
  public void show(int _position) {
    if (editablePage!=null) editablePage.show(_position);
  }
  
  public javax.swing.text.JTextComponent getTextComponent() {
    if (external) {
      if (editablePage!=null) return editablePage.getTextComponent();
    }
    else {
      if (externalPage!=null) return externalPage.getTextComponent();
    }
    return null;
  }
  
  public boolean isEmpty() {
    if (external) return externalPage.getHtmlFile().trim().length()<=0;
    String code = editablePage.getHtml().toLowerCase();
    // Extract the <body> tag
    int index = code.indexOf("<body>");
    if (index>0) {
      int index2 = code.lastIndexOf("</body>");
      if (index2<0) code = code.substring(index+6);
      else code = code.substring(index+6,index2);
    }
    return code.trim().length()<=0;
  }
  
  public StringBuffer getHtmlCode (String _info) { // The code for the generated HTML file
    StringBuffer code = new StringBuffer();
    if (external) return code;
    String ret = System.getProperty("line.separator");
   
    if (_info!=null) code.append("<!--- " + _info + "." + editor.getName() + " : " + title + " --->" + ret);

    String htmlCode = editablePage.getHtml();
    // Extract the <body> tag
    String codeStr = htmlCode.toLowerCase();
    int index = codeStr.indexOf("<body>");
    if (index>0) {
      int index2 = codeStr.lastIndexOf("</body>");
      if (index2<0) htmlCode = htmlCode.substring(index+6);
      else htmlCode = htmlCode.substring(index+6,index2);
    }
    // change the SRC in IMG tags to absolute
    if (editor.getEjs().supportsHtml()) {
      htmlCode = editor.convertToRelative(htmlCode);
    }
    else htmlCode = HtmlEditor.convertSRCtags(editor.getEjs(),htmlCode,HtmlEditor.TO_REQUIRED_BY_HTML);
    code.append(htmlCode);
    code.append(ret);
  
    if (_info!=null) code.append("<br /><hr width=\"100%\" size=\"2\" /><br />" + ret);
    return code;
  }
  
  public void refreshCss () {
    if (editablePage!=null) editablePage.refreshCss();
  }

}

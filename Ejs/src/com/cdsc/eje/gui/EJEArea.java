package com.cdsc.eje.gui;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.view.*;


/*
 * EJE 2005 - version 2.5 - "Everyone's Java Editor"
 *
 * Copyright (C) 2003 Claudio De Sio Cesari
 *
 * Require JDK 1.4
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *
 * Info, Questions, Suggestions & Bugs Report to eje@claudiodesio.com
 *
 */

public class EJEArea extends JEditorPane {
  private static final long serialVersionUID = 1L;

  public static final String TAB_SPACES = "  ";
  public static final String KEYWORD_COLOR = "eje_area.keyword_color";
  public static final String COMMON_WORD_COLOR = "eje_area.common_word_color";
  public static final String MULTI_LINE_COMMENT_COLOR = "eje_area.multi_line_color";
  public static final String SINGLE_LINE_COMMENT_COLOR = "eje_area.single_line_color";
  public static final String STRING_LITERAL_COLOR = "eje_area.string_literal_color";
  public static final String CHAR_LITERAL_COLOR = "eje_area.char_literal_color";
  public static final String NUMERIC_LITERAL_COLOR = "eje_area.numeric_literal_color";
  public static final String OPERATOR_COLOR = "eje_area.operator_color";
  public static final String INDENT_ON_INSERT_BREAK = "indentOnInsertBreak";

  static ResourceUtil res = new ResourceUtil("Resources");

  public static final Color BACKGROUND_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.BackgroundColor"));
  public static final Color FOREGROUND_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.ForegroundColor"));
  public static final Color KEYWORD_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.KeywordColor"));
  public static final Color COMMONWORD_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.CommonWordColor"));
  public static final Color CHARLITERAL_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.CharLiteralColor"));
  public static final Color NUMERICLITERAL_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.NumericLiteralColor"));
  public static final Color STRINGLITERAL_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.StringLiteralColor"));
  public static final Color MULTILINECOMMENT_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.MultiLineCommentColor"));
  public static final Color SINGLELINECOMMENT_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.SingleLineCommentColor"));
  public static final Color OPERATOR_JAVACOLOR = InterfaceUtils.color(res.getString("CodeEditor.OperatorColor"));

  private ClassWizard classWizard;
  protected PopupListener popupListener;
  private Osejs ejs;

  private DocumentListener documentListener = new DocumentListener() {
    public void changedUpdate(DocumentEvent e) { }
    public void removeUpdate(DocumentEvent e) { fireDocumentEvent(getCaretPosition()); }
    public void insertUpdate(DocumentEvent e) { fireDocumentEvent(getCaretPosition() + 1); }
    private void fireDocumentEvent(int caretPosition) {
      if (!classWizard.isVisible()) return;
      Element row = getRowAt(caretPosition);
      int firstColumnInRow = row.getStartOffset();
      int end = row.getEndOffset();
      try {
        String text = getText(firstColumnInRow, (end - firstColumnInRow));
        String memberName = popupListener.getLastTokenForSelect(text);
        if (memberName==null) classWizard.setVisible(false);
        else classWizard.setSelectedMember(memberName);
      }
      catch (BadLocationException exc) {
        //System.out.println("In Insert update");
        //exc.printStackTrace();
      }
    }
  }; // end of in-line class

  public EJEArea(Osejs _ejs) {
    ejs = _ejs;
    try {
      setupKeymap();
      setOpaque(true);
      setBackground(BACKGROUND_JAVACOLOR); // Color.white);
      setForeground(FOREGROUND_JAVACOLOR); // Color.darkGray);
      setEditable(true);
      setIndentOnBreak(true);
      setKeywordColor(KEYWORD_JAVACOLOR); // Color.blue.darker());
      setCommonWordColor(COMMONWORD_JAVACOLOR); // Color.black/* new Color(9,20,115) */);
      setCharLiteralColor(CHARLITERAL_JAVACOLOR); // new Color(0, 102, 0));
      setMultiLineCommentColor(MULTILINECOMMENT_JAVACOLOR); // Color.red);
      setNumericLiteralColor(NUMERICLITERAL_JAVACOLOR); // new Color(0, 102, 0));
      setSingleLineCommentColor(SINGLELINECOMMENT_JAVACOLOR); // Color.red);
      setStringLiteralColor(STRINGLITERAL_JAVACOLOR); // new Color(0, 102, 0));
      setOperatorColor(OPERATOR_JAVACOLOR); // Color.lightGray.darker().darker());
      setDragEnabled(true);
      //refsMap = new EJEMap();
    }
    catch (Exception exc) { exc.printStackTrace(); }
    classWizard = new ClassWizard(this,new ArrayInterface()); //Fake class for init...
    this.setOpaque(true);
    popupListener = new PopupListener();
    EJEArea.this.addKeyListener(popupListener);
    this.getDocument().addDocumentListener(documentListener);
  }

  /*
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    RenderingHints rh = g2.getRenderingHints ();
    rh.put (RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
    //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    super.paint(g);
  }
  */

  private void setupKeymap() {
    Keymap map = JTextComponent.getKeymap("EJEKeymap");
    if (map == null) {
      Keymap parent = getKeymap();
      map = JTextComponent.addKeymap("EJEKeymap", parent);
      KeyStroke insertBreakKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
      map.addActionForKeyStroke(insertBreakKeyStroke,new JavaEditorKit.InsertBreakAction());
    }
    setKeymap(map);
  }

  protected EditorKit createDefaultEditorKit() {
    return new JavaEditorKit();
  }

  public void setIndentOnBreak(boolean b) {
    putClientProperty(INDENT_ON_INSERT_BREAK, new Boolean(b));
  }

  public boolean getIndentOnBreak() {
    Boolean b = (Boolean) getClientProperty(INDENT_ON_INSERT_BREAK);
    if (b == null) return false;
    return b.booleanValue();
  }

  public void setKeywordColor(Color c) {
    putClientProperty(KEYWORD_COLOR, c);
  }

  public Color getKeywordColor() {
    return (Color) getClientProperty(KEYWORD_COLOR);
  }

  public void setCommonWordColor(Color c) {
    putClientProperty(COMMON_WORD_COLOR, c);
  }

  public Color getCommonWordColor() {
    return (Color) getClientProperty(COMMON_WORD_COLOR);
  }

  public void setCharLiteralColor(Color c) {
    putClientProperty(CHAR_LITERAL_COLOR, c);
  }

  public Color getCharLiteralColor() {
    return (Color) getClientProperty(CHAR_LITERAL_COLOR);
  }

  public void setMultiLineCommentColor(Color c) {
    putClientProperty(MULTI_LINE_COMMENT_COLOR, c);
  }

  public Color getMultiLineCommentColor() {
    return (Color) getClientProperty(MULTI_LINE_COMMENT_COLOR);
  }

  public void setNumericLiteralColor(Color c) {
    putClientProperty(NUMERIC_LITERAL_COLOR, c);
  }

  public Color getNumericLiteralColor() {
    return (Color) getClientProperty(NUMERIC_LITERAL_COLOR);
  }

  public void setSingleLineCommentColor(Color c) {
    putClientProperty(SINGLE_LINE_COMMENT_COLOR, c);
  }

  public Color getSingleLineCommentColor() {
    return (Color) getClientProperty(SINGLE_LINE_COMMENT_COLOR);
  }

  public void setStringLiteralColor(Color c) {
    putClientProperty(STRING_LITERAL_COLOR, c);
  }

  public Color getStringLiteralColor() {
    return (Color) getClientProperty(STRING_LITERAL_COLOR);
  }

  public void setOperatorColor(Color c) {
    putClientProperty(OPERATOR_COLOR, c);
  }

  public Color getOperatorColor() {
    return (Color) getClientProperty(OPERATOR_COLOR);
  }


  /**
   * Finds a given class or returns null, catching the exception
   */
  static private Class<?> findOneClass (String classname) {
    try { return Class.forName(classname); }
    catch (Exception exc) { return null; }
  }

  /**
   * Finds a given field or returns null, catching the exception
   */
  static private Field findOneField (String classname, String fieldname) {
    try {
      Class<?> c = Class.forName(classname);
      return c.getField(fieldname);
    }
    catch (Exception exc) { return null; }
  }

  /**
   * Finds a given class within the import list of the model,
   * or null if not found
   */
  static private Class<?> findClass (String classname, Osejs ejs) {
    if (classname.indexOf('.')>=0) return findOneClass(classname);
    // Try import statements
    String[] importStatements = ejs.getSimInfoEditor().getImportStatements();
    for (int i=0,n=importStatements.length; i<n; i++) {
      String importSt = importStatements[i];
      int idx = importSt.lastIndexOf('.');
      if (idx<0) continue;
      String importClass = importSt.substring(idx+1);
      if (importClass.equals(classname) || importClass.equals("*")) {
        String importPackage = importSt.substring(0,idx);
        Class<?> aClass = findOneClass(importPackage+"."+classname);
        if (aClass!=null) return aClass;
      }
    }
    return null;
  }

  /**
   * Finds a given field using the import list of the model.
   * It creates and shows the corresponding classWizard
   * @return true if found, false otherwise
   */
  private boolean createFieldWizard (String keyword, Osejs _ejs) {
    int index = keyword.lastIndexOf('.');
    if (index<0) return false;
    String classname = keyword.substring(0,index);
    String fieldname = keyword.substring(index + 1);
    Field field = findOneField(classname, fieldname);
    if (field==null) { // not found
      if (classname.indexOf('.')>=0) return false; // It is a qualified class
      String[] importStatements = _ejs.getSimInfoEditor().getImportStatements();
      for (int i=0,n=importStatements.length; i<n; i++) {
        String importSt = importStatements[i];
        int idx = importSt.lastIndexOf('.');
        if (idx<0) continue;
        String importClass = importSt.substring(idx+1);
        if (importClass.equals(classname) || importClass.equals("*")) {
          String importPackage = importSt.substring(0,idx);
          field = findOneField(importPackage+"."+classname, fieldname);
          if (field!=null) break;
        }
      } // end of for import statements
    }
    if (field==null) return false;
    if (field.getType().isArray()) classWizard = new ClassWizard(this,new ArrayInterface());
    else {
      try { classWizard = new ClassWizard(this,new ObjectInterface(field.get(null).getClass())); }
      catch (Exception exc) { return false; }
    }
    return true; // No need to search for anything else.
  }

  /**
   * Creates and sets classWizard to a wizard for a given keyword using, if required,
   * the import list of the model editor
   * @return true if successful, null otherwise.
   */
  private boolean createClassWizard (String keyword, Osejs _ejs)  {
//     System.out.println("Studying keyword: "+keyword);
    if (keyword.equals("_view")) {
      classWizard = new ClassWizard(this,new ViewInterface(ejs.getModelEditor(),ejs.getViewEditor()));
      return true;
    }
    if (keyword.startsWith("_view.")) {
      String name = keyword.substring(6); // remove "_view."
      if (name.indexOf('.')>=0) return false;
      TreeOfElements tree = _ejs.getViewEditor().getTree();
      ViewElement element = tree.viewOf(tree.findNode(name));
      // System.out.println("Element found = "+element.getElement().toString() +" = "+ element.getElement().getObject());
      try { classWizard = new ClassWizard(this,new ObjectInterface(element.getElement().getObject().getClass())); } // Class.forName(element.getElement().getObjectClassname()))); }
      catch (Exception exc) { exc.printStackTrace(); return false; }
      return true;
    }
    if (keyword.equals("_tools")) {
      try { classWizard = new ClassWizard(this,new ObjectInterface(Class.forName("org.opensourcephysics.tools.ToolForData"))); }
      catch (Exception exc) { exc.printStackTrace(); return false; }
      return true;
    }
    if (keyword.equals("this") || keyword.equals("_model")) {
      classWizard = new ClassWizard(this,new PredefinedInterface(_ejs.getModelEditor()));
      return true;
    }
    if (keyword.equals("super")) {
      classWizard = new ClassWizard(this,new PredefinedInterface(null));
      return true;
    }
    if (keyword.equals("_EjsConstants")) {
      try { classWizard = new ClassWizard(this,new ClassInterface(Class.forName("org.colos.ejs.library._EjsConstants"))); }
      catch (Exception exc) { exc.printStackTrace(); return false; }
      return true;
    }
    if (keyword.equals("_external")) {
      try { classWizard = new ClassWizard(this,new ObjectInterface(Class.forName("org.colos.ejs.library.external.ExternalAppsHandler"))); }//Gonzalo 090611
      catch (Exception exc) { exc.printStackTrace(); return false; }
      return true;
    }
    // First, try if it is a Field
    if (createFieldWizard(keyword,_ejs)) return true;
    // Now try a Class
    Class<?> aClass = findClass (keyword,_ejs);
    if (aClass!=null) {
      classWizard = new ClassWizard(this,new ClassInterface(aClass));
      return true;
    }
    // Now try a package
    JavaInterface javaInterface = new PackageInterface(keyword);
    if (javaInterface.size()>0) { classWizard = new ClassWizard(this,javaInterface); return true; }
    return false;
  }

  private Element getRowAt(int offset) {
    Element element = getDocument().getDefaultRootElement();
    int rowNumber = element.getElementIndex(offset);
    return element.getElement(rowNumber);
  }

// -----------------------------------------
// Private class
// -----------------------------------------

  class PopupListener extends KeyAdapter {
    private int caretPosition;

    @SuppressWarnings("fallthrough")
    public void keyPressed(KeyEvent e) {
      char keyChar = e.getKeyChar();
      switch (keyChar) {
//        case '_': fireUnderscorePressed(e); break;
        case '.': fireDotPressed(e); break;
        case '\n': fireReturnPressed(e); break;
        case '\t': e.consume(); fireTabPressed();
        case '(':
        case ';':
          if (classWizard.isVisible()) classWizard.setVisible(false);
          break;
        case 8:
          try {
            String lastChar = EJEArea.this.getText(EJEArea.this.getCaretPosition() - 1, 1);
            if (classWizard.isVisible()&& lastChar.equals(".")) classWizard.setVisible(false);
          }
          catch (BadLocationException exc) {} // exc.printStackTrace();
          break;
        default: if (classWizard.isVisible()) keyPressedWithVisibleClassWizard(e);
      }
    }

    /*
    private void fireUnderscorePressed(KeyEvent e) {
      if (classWizard.isVisible()) classWizard.setVisible(false);
      try {
        String invoker = getInvoker(); //retrieve the previous word
        System.out.println("Invoker is = <" + invoker + ">");
        if (invoker!=null) return; // Not the beginning of a word
        classWizard = new ClassWizard("Predefined",ClassWizard.PREDEFINED_METHOD);
        showClassWizard(e);
      }
      catch (Exception exc) {exc.printStackTrace();}
    }
    */

   private void fireDotPressed(KeyEvent e) {
     if (classWizard.isVisible()) classWizard.setVisible(false);
     String invoker = getInvoker(); //retrieve the previous word
//     System.out.println("Invoker = <" + invoker + ">");
     if (invoker == null) return;
//     if (invoker.endsWith("]")) classWizard = new ClassWizard(new ArrayInterface());
     try {
       int index = invoker.indexOf('(');
       if (index >= 0) invoker = invoker.substring(index+1);
//       System.out.println("Invoker is now = <" + invoker + ">");
       boolean found = createClassWizard(invoker,ejs);
       if (found) showClassWizard(e);
     }
     catch (Exception exc) {}
   }

    private void keyPressedWithVisibleClassWizard(KeyEvent e) {
      int keyCode = e.getKeyCode();
      if (keyCode == KeyEvent.VK_UP) {
        classWizard.selectPreviousIndex();
      }
      else if (keyCode == KeyEvent.VK_DOWN) {
        classWizard.selectNextIndex();
      }
      else if (keyCode == KeyEvent.VK_PAGE_UP) {
        classWizard.selectPreviousPageIndex();
      }
      else if (keyCode == KeyEvent.VK_PAGE_DOWN) {
        classWizard.selectNextPageIndex();
      }
      else if (keyCode == KeyEvent.VK_HOME) {
        classWizard.selectHomeIndex();
      }
      else if (keyCode == KeyEvent.VK_END) {
        classWizard.selectEndIndex();
      }
      else if (keyCode == KeyEvent.VK_ESCAPE
               || keyCode == KeyEvent.VK_SPACE
               || keyCode == KeyEvent.VK_TAB) {
        classWizard.setVisible(false);
      }
      e.consume();
    }

    private void fireReturnPressed(KeyEvent e) {
      if (classWizard.isVisible()) {
        e.consume();
        fireReturnPressedWithClassWizardVisible();
      }
    }

    public void fireReturnPressedWithClassWizardVisible() {
      String selectedValue = classWizard.getSelectedValue();
      if (selectedValue.equals("<<no member>>")) {
        classWizard.setVisible(false);
        return;
      }
      try {
        Document doc = EJEArea.this.getDocument();
        int length = 0;
        int thisCaretPosition = EJEArea.this.getCaretPosition();
        String lastChar = EJEArea.this.getText(thisCaretPosition - 1, 1);
        classWizard.setVisible(false);
        if (!lastChar.equals(".")) {
          Element row = getRowAt(thisCaretPosition);
          int firstColumnInRow = row.getStartOffset();
          String rowContent = EJEArea.this.getText(firstColumnInRow,thisCaretPosition - firstColumnInRow);
          String memberString = getLastToken(rowContent);
          length = memberString.length();
          doc.remove(thisCaretPosition - length, length);
        }
        selectedValue = classWizard.getSelectedValue();
        int index = selectedValue.indexOf('(') + 1; // Paco changed from lastIndexOf to indexOf
        if (index == 0) {
          index = selectedValue.indexOf(' ');
        }
        selectedValue = (selectedValue.indexOf(')') == index ? // Paco changed from lastIndexOf to indexOf
                         selectedValue
                         .substring(0, index + 1)
                         : selectedValue.substring(0, index));
        doc.insertString(thisCaretPosition - length, selectedValue, null);
      }
      catch (BadLocationException exc) {
        // System.out.println(exc.offsetRequested());
        // exc.printStackTrace();
      }
    }

    private void fireTabPressed() {
      try {
        Document doc = EJEArea.this.getDocument();
        int thisCaretPosition = EJEArea.this.getCaretPosition();
        doc.insertString(thisCaretPosition, TAB_SPACES, //EditorOptionsPanel.getTab(),
                         null);
      }
      catch (BadLocationException exc) {
        // System.out.println(exc.offsetRequested());
        // exc.printStackTrace();
      }
    }

    private void showClassWizard(KeyEvent e) {
      Point p = EJEArea.this.getCaret().getMagicCaretPosition();
      int x=0, y=0;
      if (p!=null) {
        x = (int) p.getX();
        y = (int) p.getY();
      }
      int classWizardWidth = classWizard.getPreferredSize().width;
      int classWizardHeight = classWizard.getPreferredSize().height;
      int fontHeight = EJEArea.this.getFont().getSize();
      int widthLimit = EJEArea.this.getSize().width - classWizardWidth;
      int heightLimit = EJEArea.this.getSize().height
          - (classWizardHeight + 2);
      if (x >= widthLimit) {
        x = x - classWizardWidth;
      }
      if (y >= heightLimit) {
        y -= (classWizardHeight + fontHeight);
      }
      classWizard.highlightIndex(0,0);
      classWizard.show(e.getComponent(), x, (y + fontHeight + 2));
      EJEArea.this.requestFocus();
    }

    private String getInvoker() {
      try {
        caretPosition = EJEArea.this.getCaretPosition();
        String invoker = getInvokerOrClassName(caretPosition);
        return invoker;
      }
      catch (Exception exc) { return null; }
    }

    private String getInvoker(int thisCaretPosition) throws BadLocationException {
      // TODO torna alla precedente parentesi di apertura RELATIVA e prendi il lasttoken
//      String text = EJEArea.this.getText();
      int nestedBrace = 0;
      int index = 0;
      for (index = thisCaretPosition - 2; index > 0; index--) {
        String character = EJEArea.this.getText(index, 1);
        if (character != null && character.equals(")")) {
          nestedBrace++;
        }
        else if (character != null && character.equals("(")) {
          nestedBrace--;
          if (nestedBrace == -1) {
            break;
          }
        }
      }
      String args = EJEArea.this.getText(index, thisCaretPosition - index);
      return getInvokerOrClassName(index) + args;
    }

//    private String getClassName(int caretPosition) throws BadLocationException {
//      return getInvokerOrClassName(caretPosition);
//    }

    private String getInvokerOrClassName(int thisCaretPosition) throws BadLocationException {
      this.caretPosition = thisCaretPosition;
      Element row = getRowAt(thisCaretPosition);
      int firstColumnInRow = row.getStartOffset();
      String rowSegment = EJEArea.this.getText(firstColumnInRow,
                                               thisCaretPosition - firstColumnInRow);
      return getLastTokenNoDot(rowSegment);
    }

    private String getLastTokenNoDot(String rowSegment) throws BadLocationException {
      int indexOf = -1;
      //This is done to partially (without arguments) understand generics
      if ( (indexOf = rowSegment.indexOf('<')) != -1
          && rowSegment.indexOf('>') != -1) {
        // System.out.println("Recursive call to getLastTokenNoDot: "+ rowSegment.substring(0, indexOf));
        return getLastTokenNoDot(rowSegment.substring(0, indexOf));
      }
      StringTokenizer st = new StringTokenizer(rowSegment,
                                               ";\n\"[]$|!,+-/=?^:<>\f\'\t\r%&~ ", false); // <<<<<
      String lastToken = null;
      while (st.hasMoreTokens()) {
        lastToken = st.nextToken();
        if (lastToken.equals(")")) { //////////////////////////////////////////////////////
          lastToken = getInvoker(caretPosition);
        }
      }
      //System.out.println("last token=" + lastToken);
      try {
        @SuppressWarnings("unused")
        int index = lastToken.indexOf(".");
      }
      catch (RuntimeException e) {
        // TODO Auto-generated catch block
        //e.printStackTrace();
      }
      return lastToken;
    }

    private String getLastToken(String rowSegment) {
      StringTokenizer st = new StringTokenizer(rowSegment,
                                               ";. \n\"()[]$|!,+-/=?^:<>\f\'\t\r%&~ ", false);
      String lastToken = null;
      while (st.hasMoreTokens()) {
        lastToken = st.nextToken();
      }
      return lastToken;
    }

    private String getLastTokenForSelect(String rowSegment) {
      StringTokenizer st = new StringTokenizer(rowSegment,
                                               ";. \n\"()[]$|!,+-/=?^:<>\f\'\t\r%&~ ", true);
      String lastToken = null;
      while (st.hasMoreTokens()) {
        String tmpLastToken = st.nextToken();
        if (!tmpLastToken.equals("\n")) {
          lastToken = tmpLastToken;
        }
      }
      return lastToken;
    }

  } // End of private class PopupListener

} // End of main class

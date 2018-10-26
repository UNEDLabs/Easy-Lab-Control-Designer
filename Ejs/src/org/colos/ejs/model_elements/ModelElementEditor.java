/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last modified: December 2007
 */

package org.colos.ejs.model_elements;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.utils.*;
import com.cdsc.eje.gui.EJEArea;

/**
 * A single page editor of code.
 */
public class ModelElementEditor {
  static private final ResourceUtil res = new ResourceUtil ("Resources");

  // Configuration variables
  private Font mFont = InterfaceUtils.font(null,res.getString("Osejs.DefaultFont"));
  private String mName="Unnamed"; // Name of this editor. It is typically used by multipage editor to distinguish it
  private String mXMLCode=null;
  private boolean mEditable=true;
  private boolean mHasCommentField=true;
  private ModelElement mModelElement;
  private ModelElementMultipageEditor mParentEditor; // non null if the editor is part of a multipage editor
  private String mBeginCodeHeader, mEndCodeHeader;
  private String mBeginCommentHeader, mEndCommentHeader;
  private Hashtable<String,TwoStrings> mPragmas = new Hashtable<String,TwoStrings>(); 
  private Component mTopPanel=null;
  
  // Implementation variables
  private JTextComponent mTextComponent;
  private JTextField mCommentField;
  private JPanel mMainPanel;

  public ModelElementEditor (ModelElement element, ModelElementMultipageEditor parentEditor) {
    this (element, parentEditor, true);
  }
    
  public ModelElementEditor (ModelElement element, ModelElementMultipageEditor parentEditor, boolean hasCommentField) {
    this (element, parentEditor, hasCommentField, "Code", "Comment");
  }

  public ModelElementEditor (ModelElement element, ModelElementMultipageEditor parentEditor, boolean hasCommentField, String codeHeader, String commentHeader) {
    mModelElement = element;
    mParentEditor = parentEditor;
    if (codeHeader==null    || codeHeader.trim().length()<=0)    codeHeader    = "ModelElementEditorCode";
    mBeginCodeHeader = "<"+codeHeader+"><![CDATA["; 
    mEndCodeHeader = "]]></"+codeHeader+">\n"; 
    if (mHasCommentField = hasCommentField) {
      if (commentHeader==null || commentHeader.trim().length()<=0) commentHeader = "ModelElementEditorComment";
      mBeginCommentHeader = "<"+commentHeader+"><![CDATA["; 
      mEndCommentHeader = "]]></"+commentHeader+">\n";
    }
  }
  
//  public String getCodeBeginHeader() { return mBeginCodeHeader; }
//
//  public String getCodeEndHeader() { return mEndCodeHeader; }

  public void setTopPanel(Component topPanel) {
    mTopPanel = topPanel;
    if (mMainPanel!=null) mMainPanel.add (mTopPanel,BorderLayout.NORTH);
  }
  
  /**
   * Creates (if not yet done) the editor component and retuns it
   * @param _collection
   * @return
   */
  public JComponent getComponent (final ModelElementsCollection collection) {
    if (mMainPanel!=null) return mMainPanel;
    
    DocumentListener docListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(mModelElement); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(mModelElement); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(mModelElement); }
    };

    mTextComponent = new EJEArea(collection.getEJS());
    mTextComponent.setFont(mFont);
    mTextComponent.getDocument().addDocumentListener(docListener);
    mTextComponent.setEditable(mEditable);
    JScrollPane scrollPanel = new JScrollPane(mTextComponent);
    scrollPanel.setPreferredSize(new Dimension(600,400));

    mMainPanel = new JPanel (new BorderLayout ());
    mMainPanel.setBorder (new EmptyBorder(0,2,0,2));
    mMainPanel.add (scrollPanel,BorderLayout.CENTER);
    if (mTopPanel!=null) mMainPanel.add (mTopPanel,BorderLayout.NORTH);

    if (mHasCommentField) {
      mCommentField = new JTextField();
      mCommentField.setEditable (mEditable);
      mCommentField.setFont(mFont);
      mCommentField.getDocument().addDocumentListener(docListener);

      JLabel commentLabel = new JLabel (res.getString ("Editor.Comment"));
      commentLabel.setFont (InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
      commentLabel.setBorder(new EmptyBorder(0,0,0,3));

      JPanel commentPanel = new JPanel (new BorderLayout());
      commentPanel.add (commentLabel,BorderLayout.WEST);
      commentPanel.add (mCommentField,BorderLayout.CENTER);
      
      mMainPanel.add (commentPanel,BorderLayout.SOUTH);
    }


    new Undo2(mTextComponent,collection.getEJS().getModelEditor());
    
    if (mXMLCode==null) readPlainCode("// "+res.getString("CodeWizard.WriteCodeHere")+"...\n\n");
    else readXmlString(mXMLCode);
    
    return mMainPanel;
  }
  
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, 
      String name, ModelElementsCollection collection) {
    boolean toLower = (mode & SearchResult.CASE_INSENSITIVE) !=0;
    if (toLower) searchString = searchString.toLowerCase();
    if (info==null) info = "";
    int lineCounter=1,caretPosition=0;

    ArrayList<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    StringTokenizer t = new StringTokenizer(getCode(), "\n",true);
    while (t.hasMoreTokens()) {
      String line = t.nextToken();
      int index;
      if (toLower) index = line.toLowerCase().indexOf(searchString);
      else index = line.indexOf(searchString);
      if (index>=0) list.add(new CodeSearchResult(collection, mModelElement, name, info, line.trim(), mTextComponent, lineCounter, caretPosition+index));
      caretPosition += line.length();
      lineCounter++;
    }
    return list;
  }

  /**
   * Sets the font for the editor
   * @param font
   */
  public void setFont (Font font) {
    mFont = font;
    if (mTextComponent!=null) mTextComponent.setFont (font); 
  }

  public void setName (String name) { this.mName = name; }
  
  public String getName() { return this.mName; }

  /**
   * Allows hidning/showing
   * @param visible
   */
  public void setVisible (boolean visible) {
    if (mMainPanel!=null) mMainPanel.setVisible(visible);
  }

  /**
   * Allows edition.
   * @param _editable
   */
  public void setEditable (boolean _editable) {
    mEditable = _editable;
    if (mTextComponent!=null) {
      mTextComponent.setEditable(_editable);
      if (mHasCommentField) mCommentField.setEditable(_editable);
    }
  }

  public boolean isEditable() { 
    return mEditable;
  }

  /**
   * Generates the final code from the contents
   * @param prefix
   * @param suffix
   * @param lineInfo
   * @param tab
   * @return
   */
  public StringBuffer generateCode (String lineInfo, String tab) {
    StringBuffer buffer = new StringBuffer();
    if (lineInfo==null) lineInfo = "";
    buffer.append(splitCode(getCode(),tab,"  // > " + lineInfo + "." + mName + ":"));
    return buffer;
  }

  /**
   * Provides an XML description of the contents, ready to be saved
   * @return
   */
  public StringBuffer saveStringBuffer () {
    StringBuffer buffer = new StringBuffer();
    if (mTextComponent!=null) {
      buffer.append(mBeginCodeHeader+mTextComponent.getText()+mEndCodeHeader);
      if (mHasCommentField) buffer.append(mBeginCommentHeader+mCommentField.getText()+mEndCommentHeader);
    }
    else buffer.append(mXMLCode);
    return buffer;
  }

  public void readXmlString(String _xmlCode) {
    mXMLCode = _xmlCode;
    if (mTextComponent!=null) {
      if (mXMLCode==null || mXMLCode.trim().length()<=0) {
        mTextComponent.setText("");
        if (mHasCommentField) mCommentField.setText("");
      }
      else {
        String code = OsejsCommon.getPiece(mXMLCode,mBeginCodeHeader,mEndCodeHeader,false);
        if (code==null) mTextComponent.setText("");
        else mTextComponent.setText(code);
        if (mHasCommentField) {
          String comment = OsejsCommon.getPiece(mXMLCode,mBeginCommentHeader,mEndCommentHeader,false);
          if (comment==null) mCommentField.setText("");
          else mCommentField.setText(comment);
        }
      }
      mTextComponent.setCaretPosition(0);
    }
  }

  public void readPlainCode (String code) {
    if (code!=null) readXmlString(mBeginCodeHeader + code + mEndCodeHeader);
    else readXmlString(null);
  }

  public void readPlainCode (String code, String comment) {
    StringBuffer buffer = new StringBuffer();
    if (code!=null) {
      buffer.append(mBeginCodeHeader + code + mEndCodeHeader);
    }
    if (comment!=null) {
      buffer.append(mBeginCommentHeader + comment + mEndCommentHeader);
    }
    String xmlCode = buffer.toString();
    readXmlString(xmlCode);
  }

  /**
   * Pragmas are used to embed code into special constructions.
   * The editor will replace code in between pragmas:
   * % begin pragma
   * ...code here...
   * % end
   * with the constructions:
   * prefix
   * "  " ...code here...
   * suffix
   * Pragmas are case insensitive, as are the keywords 'begin' and 'end'.
   * Pragmas cannot be annidated.
   * @param pragma
   * @param prefix
   * @param suffix
   */
  public void addPragma(String pragma, String prefix, String suffix) {
    mPragmas.put(pragma.toLowerCase(), new TwoStrings(prefix,suffix));
  }

  /**
   * Shorter form to add a pragma
   * @param pragma
   * @param texts
   */
  public void addPragma(String pragma, TwoStrings texts) {
    mPragmas.put(pragma.toLowerCase(), texts);
  }

  public StringBuffer splitCode (String codeStr, String tabs, String suffix) {
    StringBuffer code = new StringBuffer ();
    TwoStrings pragmaCode=null;
    
    int lineNumber = 0;
    StringTokenizer tkn = new StringTokenizer(codeStr, "\n");
    while (tkn.hasMoreTokens()) {
      String line = tkn.nextToken();
      lineNumber++;
      String lowercased = line.trim().toLowerCase();
      if (lowercased.startsWith("% begin ")) {
        String pragma = lowercased.substring(8).trim();
        pragmaCode = mPragmas.get(pragma);
//        System.out.println("pragma = "+pragma+ ". Code = "+pragmaCode.getFirstString());
        if (pragmaCode!=null) code.append(tabs + pragmaCode.getFirstString());
      }
      else if (lowercased.startsWith("% end")) {
//        System.out.println("End Code = "+pragmaCode.getSecondString());
        if (pragmaCode!=null) code.append(tabs + pragmaCode.getSecondString());
      }
      else {
        code.append(tabs + line);
        code.append(suffix + lineNumber + "\n");
      }
    }
    return code;
  }
  
  public String getCode() {
    if (mTextComponent!=null) return mTextComponent.getText();
    String code = OsejsCommon.getPiece(mXMLCode,mBeginCodeHeader,mEndCodeHeader,false);
    return (code==null) ? "" : code;
  }
  
  // --- private methods and classes

  private class CodeSearchResult extends ModelElementSearch {

    public CodeSearchResult (ModelElementsCollection collection, ModelElement element, String name, String anInformation, String aText, JTextComponent aComponent, int aLineNumber, int aCaretPosition) {
      super (collection, element, name, anInformation,aText,aComponent,aLineNumber,aCaretPosition);
    }

    public void show () {
      if (mTextComponent==null) { // Make sure the editor is created before requesting display
        getComponent(super.getCollection());
        containerTextComponent = mTextComponent;
      }
      if (mParentEditor!=null) mParentEditor.showPanel(super.getCollection(), super.getElementName(), mName);
      super.show();
    }
  }

} // end of class



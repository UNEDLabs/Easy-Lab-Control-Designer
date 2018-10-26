/**
 * The html package contains generic tools to view and edit HTML pages
 * Copyright (c) August 2010 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * @version August 2010
 */

package org.colos.ejs.osejs.edition.html;

import java.util.Map;
import java.util.Map.Entry;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.osejs.*;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejss.xml.SimulationXML;
import org.opensourcephysics.tools.FontSizer;
import org.w3c.dom.Element;

/**
 * A class that displays one set of HTML pages in different languages.
 * An HTML page is displayed in read-only form if it comes from an external file,
 * or in editable form.
 * @author Paco
 *
 */
public class HtmlEditor implements Editor, org.colos.ejs.library.utils.LocaleListener {
  static private ResourceUtil res = new ResourceUtil ("Resources");
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");

  static private final Icon LINK_ICON   = org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("HTMLEditor.External.Icon"));
  static private final Icon UNLINK_ICON = org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("HTMLEditor.Unlink.Icon"));
  static private final Icon EDIT_ICON   = org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("HTMLEditor.Edit.Icon"));

  protected boolean activeEditor=true, internal=false;
  protected boolean changed=false;
  protected String name="";
  protected Osejs ejs=null;
  protected DescriptionEditor parentEditor;

  private Font font = null;
  private JLabel titleLabel;
  protected JTextField titleField;
  protected org.colos.ejs.library.utils.LocaleSelector localeSelector;
  protected JComponent cardPanel, mainPanel;
  protected JPopupMenu popup;
  
  private CardLayout cardLayout;
  private OneHtmlPage defaultPage, currentPage;
  private Map<LocaleItem, OneHtmlPage> translations = new java.util.HashMap<LocaleItem, OneHtmlPage>();

  static public java.util.List<File> getCssFiles(Osejs _ejs) { 
    String cssFilename = _ejs.getSimInfoEditor().getCSSFile();
    java.util.List<File> cssFiles = new java.util.ArrayList<File>();
    File cssFile;
    if (cssFilename.length()>0) cssFile = new File(_ejs.getCurrentDirectory(),cssFilename);
    else cssFile = new File (_ejs.getConfigDirectory(),"_ejs_library/css/ejss.css");
    if (!cssFile.exists()) cssFile = new File (_ejs.getBinDirectory(),"config/_ejs_library/css/ejss.css");
    cssFiles.add(cssFile);
    
    for (String filename : _ejs.getSimInfoEditor().getMoreCSSFiles()) {
      cssFile = new File (_ejs.getCurrentDirectory(),filename);
      if (cssFile.exists()) cssFiles.add(cssFile);
    }
    
    return cssFiles; 
  }
  
  public HtmlEditor (Osejs _ejs, boolean _editable, DescriptionEditor aParentEditor) {
    ejs = _ejs;
    parentEditor = aParentEditor;
    
    // First Page
    currentPage = defaultPage = new OneHtmlPage(this,!_editable,"");
    translations.put(LocaleItem.getDefaultItem(),defaultPage);
    
    cardLayout = new CardLayout();
    cardPanel = new JPanel(cardLayout);
    cardPanel.add(defaultPage.getComponent(),LocaleItem.getDefaultItem().getKeyword());
    cardLayout.show(cardPanel,LocaleItem.getDefaultItem().getKeyword());
    cardPanel.validate();

    // Title panel 
    titleLabel = new JLabel (res.getString("HtmlEditor.Header"));
    titleField = new JTextField();
    titleField.setEditable(false);
    titleField.getDocument().addDocumentListener (new DocumentListener(){
      public void changedUpdate(DocumentEvent e) { reflectChange(); }
      public void insertUpdate(DocumentEvent e)  { reflectChange(); }
      public void removeUpdate(DocumentEvent e)  { reflectChange(); }
      private void reflectChange() {
        currentPage.setTitle(titleField.getText());
        changed = true;
      }
    });
    
    JPanel titlePanel = new JPanel(new BorderLayout());
    titlePanel.add(titleLabel,BorderLayout.WEST);
    titlePanel.add(titleField,BorderLayout.CENTER);

    JCheckBox editButton =  new JCheckBox(EDIT_ICON);
    editButton.setToolTipText(res.getString("HtmlEditor.EditButton"));
    editButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { 
        if (currentPage.isExternal()) {
          File htmlFile = ejs.useNativeFileChooser() ? 
              ejs.chooseFileUnderSource (FileChooserUtil.getHtmlFileChooser(ejs), mainPanel, true) : 
              ejs.chooseFileUnderSource(parentEditor.getChooser(), mainPanel,true);
          if (htmlFile==null) return; // The user canceled it
          currentPage.setCode(ejs.getRelativePath(htmlFile));
        }
        else currentPage.switchEditable(); 
      }
    });

    JCheckBox linkedButton =  new JCheckBox(UNLINK_ICON, !_editable);
    linkedButton.setSelectedIcon(LINK_ICON);
    linkedButton.setToolTipText(res.getString("HtmlEditor.LinkButton"));

    final JPanel buttonsPanel = new JPanel(new GridLayout(1,0,0,0));
    buttonsPanel.add(editButton);
    buttonsPanel.add(linkedButton);

    linkedButton.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) { 
        currentPage.setExternal(!currentPage.isExternal());
//        FontSizer.setFonts(buttonsPanel, FontSizer.getLevel());
      }
    });

    // Locale selector
    localeSelector = new org.colos.ejs.library.utils.LocaleSelector (this);
    localeSelector.setMasterSelector(ejs.getTranslationEditor().getLocaleSelector());
    localeSelector.setEnabled(ejs.getSimInfoEditor().addTranslatorTool());

    // Top panel
    JPanel topRightPanel = new JPanel(new BorderLayout());
    topRightPanel.add(titlePanel,BorderLayout.CENTER);
    topRightPanel.add(buttonsPanel,BorderLayout.EAST);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(topRightPanel,BorderLayout.CENTER);
    topPanel.add(localeSelector.getComponent(),BorderLayout.EAST);

    // All together
    mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(cardPanel,BorderLayout.CENTER);
    mainPanel.validate();
    
    changed = false;
    setZoomLevel(FontSizer.getLevel());
  }

  public void setZoomLevel (int level) {
    FontSizer.setFonts(mainPanel, level);
  }
  
  /**
   * Used by the basic viewers
   * @return
   */
  public Osejs getEjs() { return ejs; }

//  public DescriptionEditor getParentEditor() { return parentEditor; }

  public OneHtmlPage getDefaultPage() { return defaultPage; }
  
  public OneHtmlPage getHtmlPage(LocaleItem _item) { return translations.get(_item); }

  public void setAllowChangeLocale(boolean _allow) {
    if (!_allow) {
//      cardLayout.show(cardPanel, LocaleItem.getDefaultItem().getKeyword());
      localeSelector.setLocaleItem(LocaleItem.getDefaultItem());
    }
    localeSelector.setEnabled(_allow);
  }
  
  public void setLocaleItem(LocaleItem _item) {
    OneHtmlPage translation = translations.get(_item);
    if (translation==null) {
      translation = new OneHtmlPage(this,currentPage.isExternal(),"");
      translation.setTitle(defaultPage.getTitle()+" ("+_item.getKeyword()+")");
      translations.put(_item, translation);
      cardPanel.add(translation.getComponent(), _item.getKeyword());
    }
    cardLayout.show(cardPanel, _item.getKeyword());
    currentPage = translation;
    if (currentPage.isEmpty()) {
      currentPage.setEditable(true);
    }
    titleField.setText(currentPage.getTitle());
    titleField.setEditable(currentPage!=defaultPage);
  }

  // ------------------------------
  // Implementation of Editor
  // ------------------------------
  
  public void setName (String _newName) {
    name = _newName;
    defaultPage.setTitle(name);
    if (currentPage==defaultPage) titleField.setText(name);
    changed = true;
  }

  public String getName() { return name; }

  public void clear () {
    translations.clear();
    translations.put(LocaleItem.getDefaultItem(),defaultPage);
    cardPanel.removeAll();
    cardPanel.add(defaultPage.getComponent(),LocaleItem.getDefaultItem().getKeyword());
    cardLayout.show(cardPanel, LocaleItem.getDefaultItem().getKeyword());
    cardPanel.validate();
    currentPage = defaultPage;
    titleField.setEditable(false);
  }

  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) {
    mainPanel.setForeground (_color);
    titleLabel.setForeground (_color.darker());
    localeSelector.setColor (_color.darker());
  }

  public void setFont (Font _font) {
    // titleField.setFont(_font);
//    font = _font;
//    for (OneHtmlPage ohp : translations.values()) ohp.setFont(_font);
  }

  public Font getFont() { return font; } 
  
  public void setEditable (boolean _editable) { }

//  public void setVisible (boolean _visible) { visible = _visible; }
//
//  public boolean isVisible () { return visible; }

  public void refreshCss () {
    for (OneHtmlPage ohp : translations.values()) ohp.refreshCss();
  }

  public boolean isChanged () { return changed; }

  public void setChanged (boolean _ch) { changed = _ch; }

  public void setActive (boolean _active) {
    changed = true;
    activeEditor=_active;
  }
  
  public boolean isActive () { return activeEditor; }

  public boolean isInternal() {
    return internal;
  }

  public void setInternal(boolean _internal) {
    internal = _internal;
  }
  // ------------------------
  // input and output
  // ------------------------

  public void fillSimulationXML(SimulationXML _simXML) {
    if (!isActive()) return;
    if (isInternal()) return;
    java.util.List<LocaleItem> desiredTranslations = ejs.getTranslationEditor().getDesiredTranslations();
    Element pageElement = _simXML.addDescriptionPage(getName());
    for (Entry<LocaleItem,OneHtmlPage> translation : translations.entrySet()) {
      LocaleItem item = translation.getKey();
      if (!desiredTranslations.contains(item)) continue; // save only desired translations
      OneHtmlPage page = translation.getValue();
      if (!item.isDefaultItem() && page.isEmpty()) continue; // Empty translations are NOT saved (except the default one)
      if (page.isExternal()) _simXML.addDescriptionLocalizedExternalPage(pageElement,item.getKeyword(), page.getTitle(),page.getCode());
      else                   _simXML.addDescriptionLocalizedPage(pageElement,item.getKeyword(), page.getTitle(),page.getCode());
    }
  }
  
  public StringBuffer saveStringBuffer () {
    StringBuffer buffer = new StringBuffer();
    java.util.List<LocaleItem> desiredTranslations = ejs.getTranslationEditor().getDesiredTranslations();
    for (Entry<LocaleItem,OneHtmlPage> translation : translations.entrySet()) {
      LocaleItem item = translation.getKey();
      if (!desiredTranslations.contains(item)) continue; // save only desired translations
      OneHtmlPage page = translation.getValue();
      if (!item.isDefaultItem() && page.isEmpty()) continue; // Empty translations are NOT saved (except the default one)
      buffer.append("<OneHTMLPage>\n");
      buffer.append("  <Locale>"+item.getKeyword()+"</Locale>\n");
      buffer.append("  <Title>"+page.getTitle()+"</Title>\n");
      buffer.append("  <External>"+page.isExternal()+"</External>\n");  
      buffer.append("  <![CDATA[\n"+ page.getCode()+ "\n]]>\n");
      buffer.append("</OneHTMLPage>\n");
    }
    return buffer;
  }

  public void readString (String _input) {
    String onePage = OsejsCommon.getPiece(_input,"<OneHTMLPage>","</OneHTMLPage>",false);
    if (onePage==null) { // backwards compatibility
      String code = OsejsCommon.getPiece(_input,"<![CDATA[\n","\n]]>",false);
      defaultPage.setCode(code);
      defaultPage.setEditable(false);
      return;
    }
    while (onePage!=null) {
      String language  = OsejsCommon.getPiece(onePage,"<Locale>","</Locale>",false);
      String title     = OsejsCommon.getPiece(onePage,"<Title>","</Title>",false);
      String code      = OsejsCommon.getPiece(onePage,"<![CDATA[\n","]]>\n",false);
      boolean external = "true".equals(OsejsCommon.getPiece(onePage,"<External>","</External>",false));
      LocaleItem item = LocaleItem.getLocaleItem(language);
      if (item==null) {
        ejs.print("Warning! Html editor is ignoring unrecognized locale name : "+language+"\n");
        return;
      }
      OneHtmlPage page;
      if (item.isDefaultItem()) {
        page = defaultPage;
        page.setExternal(external);
        page.setCode(code);
      }
      else page = new OneHtmlPage(this,external,code); 
      page.setEditable(false);
      page.setTitle(title);

      translations.put(item,page);
      if (!item.isDefaultItem()) {
        cardPanel.add(page.getComponent(), item.getKeyword());
//        localeDropDown.addLocaleItem(item);
      }
      _input = _input.substring(_input.indexOf("</OneHTMLPage>")+14);
      onePage = OsejsCommon.getPiece(_input,"<OneHTMLPage>","</OneHTMLPage>",false);
    }
    localeSelector.setLocaleItem(LocaleItem.getDefaultItem());
  }

  public StringBuffer generateCode (int _type, String _info) {
    if (!isActive()) return new StringBuffer();
    StringBuffer code = new StringBuffer();
    if (_type==Editor.GENERATE_CODE) { // The code for the generated HTML file
      System.out.println ("Error! GenerateCode of HtmlEditor should not be called.");
//      String ret = System.getProperty("line.separator");
//      for (Entry<LocaleItem,OneHtmlPage> entry : translations.entrySet()) {
//        OneHtmlPage page = entry.getValue();
//        if (page.isExternal()) continue; // External pages cannot appear here
//        String htmlCode = page.getCode();
//        // Extract the <body> tag
//        String theText = null, codeStr = htmlCode.toLowerCase();
//        int index = codeStr.indexOf("<body>");
//        if (index<0) theText = htmlCode;
//        else {
//          int index2 = codeStr.lastIndexOf("</body>");
//          if (index2<0) htmlCode.substring(index+6);
//          else theText = htmlCode.substring(index+6,index2);
//        }
//        // change the SRC in IMG tags to absolute
//        theText = convertSRCtags(ejs,theText,TO_REQUIRED_BY_HTML);
//        // Generate the code      
//        if (_info!=null) code.append("<!--- " + _info + "." + getName() + (entry.getKey().isDefaultItem() ? "" : entry.getKey().toString()) + " --->" + ret);
//        code.append(theText);
//        code.append(ret);
//        if (_info!=null) code.append("<br><hr width=\"100%\" size=\"2\"><br>" + ret);
//      } // end of for Entry
    }

    else if (_type==Editor.GENERATE_RESOURCES_NEEDED) {  // AMAVP (See note in ControlElement)
      for (Entry<LocaleItem,OneHtmlPage> entry : translations.entrySet()) {
        OneHtmlPage page = entry.getValue();
        String htmlCode;
        String relativePath=null;
        if (page.isExternal()) {
          code.append(page.getCode()+";");
          File htmlFile = new File(ejs.getCurrentDirectory(),page.getCode());
          htmlCode = FileUtils.readTextFile(htmlFile, null);
          relativePath = ejs.getRelativeParentPath(htmlFile);
        }
        else htmlCode = page.getCode();
        if (htmlCode==null) {
          JOptionPane.showMessageDialog(null,"Page has no HTML:"+getName()+"\nProcess will continue.","EJS Error",JOptionPane.ERROR_MESSAGE);
          return code;
        }
        // Try to find images in this HTML or XHTML code
        String codeStr = htmlCode.toLowerCase();
        int index = codeStr.indexOf("<img");
        while (index>=0) {
          int index2 = codeStr.indexOf('>',index);
          if (index2<0) break; // This would be a syntax error, actually
          String tag = codeStr.substring(index,index2);
          // Process the tag
//          StringTokenizer tkn = new StringTokenizer(tag,"= \t");
//          while (tkn.hasMoreTokens()) {
//            String token = tkn.nextToken();
//            if (token.toLowerCase().equals("src")) {
//              if (tkn.hasMoreTokens()) image = tkn.nextToken(delim)
//            }
//          }
          int srcBegin = tag.indexOf("src");
          if (srcBegin>=0) {
            srcBegin = tag.indexOf('\"',srcBegin+3);
            int srcEnd = index+tag.indexOf('\"',srcBegin+1);
            srcBegin = index + srcBegin+1;
            String imageFilename = htmlCode.substring(srcBegin,srcEnd);
            // make the resource relative
            if (relativePath!=null) {
              if (imageFilename.startsWith("./")) imageFilename = relativePath+imageFilename.substring(2); 
//              else if (imageFilename.startsWith("../")) imageFilename = relativePath+imageFilename; 
              else imageFilename = relativePath+imageFilename;
              code.append(imageFilename+ ";");
            }
            else code.append(convertToRelativePath(ejs,imageFilename+";"));
          }
          // Search next tag
          codeStr = codeStr.substring(index2);
          htmlCode = htmlCode.substring(index2);
          index = codeStr.indexOf("<img");
        }
      } // end of for Entry
    } // end if
    
    return code;
  }
  // ------------------------------
  // Conversion to relative path
  // ------------------------------
  static public final int TO_RELATIVE_TO_XML_FILE = 0;
  static public final int TO_REQUIRED_BY_HTML = 1;
  static public final int TO_ABSOLUTE_URL = 2;

  /**
   * Converts a relative path to absolute URL in the file system.
   * Matches closely convertToAbsoluteFile
   */
  static public String convertToAbsolutePath (Osejs _ejs, String path) {
    org.opensourcephysics.tools.Resource resource = org.opensourcephysics.tools.ResourceLoader.getResource(path);
    if (resource!=null) {
      File file = resource.getFile();
      if (file!=null) return "file:///"+FileUtils.correctUrlString(FileUtils.getPath(file));
    }
    System.err.println ("convertToAbsolutePath : ResourceLoader could not help for path = <"+path+">");
    if (path.startsWith("./")) path = FileUtils.getPath(_ejs.getCurrentXMLFile().getParentFile()) + path.substring(2);
    else if (path.equals(".")) path = FileUtils.getPath(_ejs.getCurrentXMLFile().getParentFile());
    else {
      File file2 = new File (_ejs.getSourceDirectory(),path);
      if (file2.exists()) path = FileUtils.getPath(file2);
      else; // Do nothing, the file is absolute
    }
    System.err.println ("Found in "+path);
    return "file:///"+FileUtils.correctUrlString(path);
  }

  /**
   * Converts a relative path to absolute URL in the file system.
   * Matches convertToAbsolutePath
   */
  static public File convertToAbsoluteFile (Osejs _ejs, String path) {
//    System.err.println ("Trying to find <"+path+">");
    org.opensourcephysics.tools.Resource resource = org.opensourcephysics.tools.ResourceLoader.getResource(path);
//    System.err.println ("resource = "+resource);
    if (resource!=null &&  resource.getFile()!=null) return resource.getFile();
    System.err.println ("convertToAbsoluteFile : ResourceLoader could not help for <"+path+">");
    if (path.startsWith("./")) return new File (_ejs.getCurrentXMLFile().getParentFile(), path.substring(2));
    if (path.equals(".")) return _ejs.getCurrentXMLFile().getParentFile();
    File file = new File (_ejs.getSourceDirectory(),path);
    if (file.exists()) return file;
    return new File (path);
  }

  /**
   * Convert an absolute URL location to a relative path.
   * It first tries to make it relative to the XML file.
   * Then to the EJS user directory.
   * Finally, it leaves it absolute.
   */
  static private String convertToRelativePath (Osejs _ejs, String path) {
    if (path.toLowerCase().startsWith("file:///")) path = path.substring(8);
    path = FileUtils.uncorrectUrlString(path);
    return _ejs.getRelativePath(path); // relative to the XML file
  }
  
  /**
   * Converts an absolute URL to the path required by the generated HTML file.
   */
  static private String convertToRequiredByHTML (Osejs _ejs, String _path, String _relativeParent, String _pathToLib) {
    _path = convertToRelativePath (_ejs,_path);
    if (_path.startsWith("./")) _path = _relativeParent + _path.substring(2);
    else {
      File file = new File (_ejs.getSourceDirectory(),_path);
      if (!file.exists()) _path = "file:///" + _path; // Not under EJS's user directory
    }
    return _pathToLib+FileUtils.correctUrlString(_path);
  }

  /**
   * Changes the SRC reference in IMG tags to absolute URL or relative paths.
   * If _type == TO_RELATIVE_TO_XML_FILE the references are made relative to the XML file
   * If _type == TO_RELATIVE_TO_USER_DIR the references are made relative to the EJS user directory
   * If _type == TO_ABSOLUTE_URL the references are made absolute to the file system. 
   */
  static public String convertSRCtags (Osejs _ejs, String _text, int _type) {
    String textLowercase = _text.toLowerCase();
    StringBuffer textChanged = new StringBuffer();
    // Compute relativePath
    String relativePath = "", pathToLib=""; // Levels up to get to the _ejs_library
    if (_type==TO_REQUIRED_BY_HTML) {
      relativePath = FileUtils.getRelativePath(_ejs.getCurrentXMLFile().getParentFile(), _ejs.getSourceDirectory(),false);
      char[] pathChars = relativePath.toCharArray();
      for (int i=0; i<pathChars.length; i++) if (pathChars[i]=='/') pathToLib += "../";

    }
    int index = textLowercase.indexOf("<img");
    while (index>=0) {
      boolean hasSlash = true;
      int index2 = textLowercase.indexOf("/>",index);
      if (index2<0) {
        hasSlash = false;
        index2 = textLowercase.indexOf('>',index);
      }
      if (index2<0) break; // This is a syntax error , actually
      String tag = textLowercase.substring(index,index2);
      // Process the tag
      int srcBegin = tag.indexOf("src=\"")+5;
      int srcEnd = index+tag.indexOf("\"",srcBegin);
      srcBegin = index + srcBegin;
      String filename = _text.substring(srcBegin,srcEnd);
      switch (_type) {
        case TO_RELATIVE_TO_XML_FILE : filename = convertToRelativePath(_ejs,filename); break;
        case TO_REQUIRED_BY_HTML : filename = convertToRequiredByHTML(_ejs,filename,relativePath,pathToLib); break;
        default :
        case TO_ABSOLUTE_URL : filename = convertToAbsolutePath(_ejs,filename); break;
      }
      textChanged.append(_text.substring(0,srcBegin)+filename);
      textChanged.append(_text.substring(srcEnd,index2));
      if (!hasSlash) textChanged.append("/");
      // Search next tag
      textLowercase = textLowercase.substring(index2);
      _text = _text.substring(index2);
      index = textLowercase.indexOf("<img");
    }
    textChanged.append(_text);
    return textChanged.toString();
  }

  public String convertToRelative (String _htmlCode) { return convertSRCtags(ejs,_htmlCode,TO_RELATIVE_TO_XML_FILE); }
  
  public String convertToAbsolute(String _htmlCode) { return convertSRCtags(ejs,_htmlCode,TO_ABSOLUTE_URL); }

  // ------------------------------
  // Search
  // ------------------------------

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new java.util.ArrayList<SearchResult>();
    _info=res.getString("Osejs.Main.Description");
    for (OneHtmlPage ohp : translations.values()) {
      if (ohp.isExternal()) continue; // External files are not searched for
      
      int caretPosition=0;
      String textToProcess = ohp.getCode();
      boolean toLower = (_mode & SearchResult.CASE_INSENSITIVE) !=0;
      if (toLower) {
        _searchString = _searchString.toLowerCase();
        textToProcess = textToProcess.toLowerCase();
      }
      java.util.StringTokenizer t = new java.util.StringTokenizer(textToProcess, "\n");
      while (t.hasMoreTokens()) {
        String line = t.nextToken();
        int index;
        if (toLower) index = line.toLowerCase().indexOf(_searchString);
        else index = line.indexOf(_searchString);
        if (index>=0) list.add(new HTMLSearchResult(ohp,_info,line.trim(),0,caretPosition+index));
        caretPosition += line.length()+1;
      }
    }
    return list;
  }

  private class HTMLSearchResult extends SearchResult {
    OneHtmlPage page;
    
    public HTMLSearchResult (OneHtmlPage _page, String anInformation, String aText, int aLineNumber, int aCaretPosition) {
      super (anInformation+"."+getName(),aText,_page.getTextComponent(),aLineNumber,aCaretPosition);
      page = _page;
    }

    public String toString () {
      return information+": "+textFound;
    }

    public void show () {
      parentEditor.showPage(HtmlEditor.this);
      // The caretPosition in the HTML code is not the same as in the JTextPane!!! and may produce an exception
      page.show(0);
      containerTextComponent.requestFocusInWindow();
    }

  }


} // end of class

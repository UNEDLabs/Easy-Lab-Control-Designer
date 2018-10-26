/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.AWTEvent;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.net.URL;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;

import org.colos.ejs.library.control.*;
import org.colos.ejs.library.utils.*;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.dialogs.DialogsRes;
import org.opensourcephysics.tools.*;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.desktop.OSPDesktop;

/**
 * A base interface for a simulation
 */

public abstract class Simulation extends Animation implements LocaleListener {
  static public ResourceBundle ejsRes = ResourceBundle.getBundle("org.colos.ejs.library.resources.ejs_res", Locale.getDefault());

  static private final String DEFAULT_STATE_FILENAME = "DefaultState.out";
  
  static private String pathToLibrary="";
  static private AWTEventListener focusListener=null;

  static private String homeDir=null, userDir=null;

  private boolean isUnderEjs = false;
  private Component parentComponent=null;
  private String parentComponentName=null;
  private String captureElement=null; // The element that will be captured as GIF or in video
  private URL codebase=null;
  private LocaleItem currentLocaleItem = LocaleItem.getDefaultLocaleItem();

  private List<String> stateVariablesList=null;

  private JLabel increaseFontButton, decreaseFontButton, openPageButton;
  protected VideoUtil videoUtil;
  
// ---------------------------------------------------
//  Utilities to extract resources
// ---------------------------------------------------
  
  static private Hashtable<String,File> resourcesExtracted = new Hashtable<String,File>();

//  static private final String EJS_TMP_DIR = ".Ejs.tmp";
//  static private final String EJS_TMP_SIM_DIR = EJS_TMP_DIR+"/Simulations/";
  static public String getEjsString(String key) { 
    try { return ejsRes.getString(key); }
    catch (Exception exc) {
      exc.printStackTrace();
      return key;
    }
  }

  static public String getPathToLibrary() { return pathToLibrary; }

  static public void setPathToLibrary(String _path) {
    try {
      File libDir = new File (_path);
//      System.err.println ("Path set to "+_path);
      if (libDir.exists() && libDir.isDirectory()) pathToLibrary = _path;
    }
    catch (Exception exc) { }; //do nothing and don't complain
  }

  static public String getUserDir() {
    if (userDir==null) {
      try {
        userDir = System.getProperty("user.dir").replace('\\','/');
        if (!userDir.endsWith("/")) userDir = userDir + "/";
      }
      catch (Exception exc) { return ""; }
    }
    return userDir;
  }

  static public String getHomeDir() {
    if (homeDir==null) {
      try {
        homeDir = System.getProperty("user.home").replace('\\','/');
        if (!homeDir.endsWith("/")) homeDir = homeDir + "/";
      }
      catch (Exception exc) { return ""; }
    }
    return homeDir;
  }

  static public String getTemporaryDir() {
    try { return getHomeDir()+".Ejs.tmp/"; }
    catch (Exception exc) { return ""; }
  }

  static public File getResourceFile(String _filename) {
	  return getResourceFile(_filename,true);
  }
  
  static public File getResourceFile(String _filename, boolean _deleteOnExit) {
    Resource res = ResourceLoader.getResource(_filename);
    if (res==null) return null;
    
    File resourceFile = res.getFile();
    // See if the file exists
    if (resourceFile!=null && resourceFile.exists()) return resourceFile;
    
    // See if it has already been extracted
    resourceFile = resourcesExtracted.get(_filename);
    if (resourceFile!=null) return resourceFile;

    // Extract it from its location to a temp file
    int index = _filename.lastIndexOf('.');
    String suffix = index>=0 ? _filename.substring(index) : ".tmp";
    try {
      resourceFile = File.createTempFile("ejs_tmp_", suffix);
      if (_deleteOnExit) resourceFile.deleteOnExit();
      InputStream inputStream = res.openInputStream();
      FileOutputStream fout = new FileOutputStream(resourceFile);
      BufferedInputStream in = new BufferedInputStream(inputStream);
      int c;
      while ((c = in.read()) != -1) fout.write(c);
      in.close();
      fout.close();
      resourcesExtracted.put(_filename,resourceFile);
      System.out.println("Ejs simulation: resource "+_filename+ " extracted into " + resourceFile.getAbsolutePath());
      return resourceFile;
    }
    catch(Exception exc) {
      System.out.println("Ejs simulation: resource "+_filename+ " could NOT be extracted");
      exc.printStackTrace();
      return null;
    }
  }  

  /*
   * Extracts a file into the given directory
   */
  static public File extractToDirectory(String _resourceString, File _targetDirectory, boolean _verbose) {
	  return extractToDirectory(_resourceString, _targetDirectory, _verbose, true);
  }

  /*
   * Extracts a file into the given directory
   */
  static public File extractToDirectory(String _resourceString, File _targetDirectory, boolean _verbose, boolean _deleteOnExit) {
    try {
      File destFile = new File(_targetDirectory,_resourceString);
      if (_deleteOnExit) destFile.deleteOnExit();
      destFile.getParentFile().mkdirs();
      InputStream inputStream = ResourceLoader.openInputStream(_resourceString);
      FileOutputStream fout = new FileOutputStream(destFile);
      BufferedInputStream in = new BufferedInputStream(inputStream);
      int c;
      while ((c = in.read()) != -1) fout.write(c);
      in.close();
      fout.close();
      if (_verbose) System.out.println("Ejs simulation: resource "+_resourceString+ " extracted into " + destFile.getAbsolutePath());
      return destFile;
    }
    catch(Exception exc) {
      System.out.println("Ejs simulation: resource "+_resourceString+ " could NOT be extracted");
      exc.printStackTrace();
      return null;
    }
  }  

  static public File extractAs(String _resourceString, String filename, boolean _verbose) {
	  return extractAs(_resourceString, filename, _verbose, true);
  }

  static public File extractAs(String _resourceString, String filename, boolean _verbose, boolean _deleteOnExit) {
    try {
      File destFile = new File(filename);
      if (_deleteOnExit) destFile.deleteOnExit();
      if (destFile.getParentFile()!=null) destFile.getParentFile().mkdirs();
      InputStream inputStream = ResourceLoader.openInputStream(_resourceString);
      FileOutputStream fout = new FileOutputStream(destFile);
      BufferedInputStream in = new BufferedInputStream(inputStream);
      int c;
      while ((c = in.read()) != -1) fout.write(c);
      in.close();
      fout.close();
      if (_verbose) System.out.println("Ejs simulation: resource "+_resourceString+ " extracted into " + destFile.getAbsolutePath());
      return destFile;
    }
    catch(Exception exc) {
      System.err.println("Ejs simulation: resource "+_resourceString+ " could NOT be extracted");
      exc.printStackTrace();
      return null;
    }
  }  

//  /**
//   * Makes sure a resource is present as a File. If it is not,
//   * then it extracts it to a temporary directory.
//   * @param _resource String
//   * @return File returns either the original file or the extracted one
//   */
//  static public File requiresResourceFile (String _resource) {
//    return requiresResourceFile(_resource, _resource);
//  }

//  /**
//   * Makes sure a resource is present as a File. If it is not,
//   * then it extracts it to a temporary directory with a different name.
//   * @param _resource String
//   * @param _destination String the destination file desired
//   * @return File returns either the original file or the extracted one
//   */
//  static public File requiresResourceFile (String _resource, String _destination) {
//    File file = null;
//    Resource res = ResourceLoader.getResource(_resource);
//    if (res!=null) file = res.getFile();
//    if (file!=null && file.exists()) return file;
//    if (_destination.startsWith("/")) _destination = _destination.substring(1);
//    return extractResource(_resource, getTemporaryDir() + _destination);
//  }
  
//  /**
//   * Extracts a file using the ResourceLoader to find it
//   * @param _source String The file to extract
//   * @param _destination String The destination of the file in the user's local file system
//   * @return boolean true if successful
//   */
//  static public File extractResource (String _source, String _destination) {
//    File destFile = new File(_destination);
////    if (destFile.exists()) return destFile;
//    try {
//      InputStream inputStream = ResourceLoader.openInputStream(_source);
//      destFile.getParentFile().mkdirs();
//      FileOutputStream fout = new FileOutputStream(destFile);
//      BufferedInputStream in = new BufferedInputStream(inputStream);
//      int c;
//      while ((c = in.read()) != -1) fout.write(c);
//      in.close();
//      fout.close();
//      resourcesExtracted.add(destFile);
//      System.out.println("Ejs simulation: resource "+_source+ " extracted into " + _destination);
//      return destFile;
//    }
//    catch(Exception exc) {
//      System.out.println("Ejs simulation: resource "+_source+ " could NOT be extracted into " + _destination);
//      exc.printStackTrace();
//      return null;
//    }
//  }
//
//  /**
//   * Cleans any extracted resource
//   */
//  static private void cleanExtractedResources () {
//    if (resourcesExtracted.size()<=0) return;
//    System.out.println("Ejs simulation: Cleaning extracted files.");
//    for (File file : resourcesExtracted) {
//      System.out.println("Ejs simulation: wants to delete "+file.getPath());
//      if (file.exists()) {
//        System.out.println("Ejs simulation: Removing extracted resource "+file.getPath());
//        file.delete();
//      }
//    }
//    resourcesExtracted.clear();
//    removeEmptyDirs (new File(getTemporaryDir()),true);
//  }


//---------------------------------------------------
// methods defined by the abstract superclass
//---------------------------------------------------

  public boolean hasDefaultState() {
    if (isMoodleConnected()) return false; // Otherwise, loading the applet takes ages!
    Resource res = ResourceLoader.getResource(DEFAULT_STATE_FILENAME);
    return res!=null;
  }

  public boolean readDefaultState() {
    if (isMoodleConnected()) return false; // Otherwise, loading the applet takes ages!
    else if (hasDefaultState()) return readVariables(DEFAULT_STATE_FILENAME,(List<String>) null);
    else return false;
  }

  /**
   * Returns the reset filename, if any
   * @return
   */
  public String getResetFilename () { return resetFile; }
  
  /**
   * Reset to a user-defined default state 
   */
  protected void userDefinedReset() {
    if (resetFile!=null && !isMoodleConnected()) {
//      System.out.println ("Must read state "+resetFile);
      if (resetFile.equals(DEFAULT_STATE_FILENAME)) readVariables(DEFAULT_STATE_FILENAME,(List<String>) null);
      else readState (resetFile);
      if (view!=null) view.reset();
    }
  }

  /**
   * User-defined update view (such as video capture) 
   */
  protected void userDefinedViewUpdate() {
    videoUtil.captureVideoImage();
    getModel()._readFromViewAfterUpdate();
  }

  /**
   * Brings in the capture video tool for the given view Element
   * @param element
   */
  public void captureVideo(String element) {
    videoUtil.startVideoTool(getView(),element);
  }
  
// ---------------------------------------------------
//  Utilities to extract resources
// ---------------------------------------------------

  private String getClassname () {
    String classname = this.getClass().getName();
    int index = classname.lastIndexOf('.');
    if (index>=0) classname = classname.substring(index+1);
    return classname;
  }

  private String getModelClassname () {
    String classname = model.getClass().getName();
    int index = classname.lastIndexOf('.');
    if (index>=0) classname = classname.substring(index+1);
    return classname;
  }

  private void errorMessage (String _text) { System.err.println(getClassname()+": "+_text); }

  private void errorMessage (Exception _exc) {
    System.err.println(getClassname()+": Exception caught! Text follows:");
    _exc.printStackTrace(System.err);
  }

  /**
   * Sets the parent component of any subsequent message window such as
   * a JOptionPane.
   * @param _component java.awt.Component
   */
  public void setParentComponent (Component _component) { parentComponent = _component; }

  /**
   * Sets the parent component of any subsequent message window such as
   * a JOptionPane using a name
   * @param _componentName String
   */
  public void setParentComponent (String _componentName) {
    parentComponentName = _componentName;
    parentComponent = null; // Delay resolving the actual component until it is really needed
  }

  /**
   * Gets the parent component for subsequent message windows such as
   * a JOptionPane.
   * @return java.awt.Component
   */
  public Component getParentComponent () {
    if (parentComponent==null) {
      if (view != null) parentComponent = view.getComponent(parentComponentName);
    }
    return parentComponent;
  }

// -----------------------------
// Setters and getters 
// -----------------------------

   /**
   * Sets the codebase
   */
  public void setCodebase (URL _codebase) { codebase = _codebase; }

 /**
   * Returns the codebase
   */
  public URL getCodebase () { return codebase; }

  // -----------------------------
  // Translations
  // -----------------------------

  private Set<LocaleItem> availableLocales = new HashSet<LocaleItem>();
  
  public void addAvailableLocale(String _language) {
    LocaleItem localeItem = LocaleItem.getLocaleItem(_language);
    if (localeItem!=null) availableLocales.add(localeItem);
    else System.out.println("Warning! Html editor is ignoring unrecognized locale name : "+_language+"\n");
  }
  
  public java.util.Set<LocaleItem> getAvailableLocales() { return availableLocales; }
  
  public String translateString (String _property, String _default) {
    return Model._getTranslatorUtil().translateString(_property,_default);
  }
  
  public String translateString (String _property) { 
    return Model._getTranslatorUtil().translateString(_property); 
  }

  /**
   * Sets the locale item for the simulation
   */
  public void setLocaleItem (LocaleItem _item) {
    setLocaleItem(_item, true);
  }

  /**
   * Sets the locale item for the simulation but lets you not reset the model
   */
  protected void setLocaleItem (LocaleItem _item, boolean _resetModel) {
    currentLocaleItem = _item;
    Locale locale = _item.getLocale();
    if (locale==null) locale = Locale.getDefault();
    ToolsRes.setLocale(locale);
    DisplayRes.setLocale(locale);
    DialogsRes.setLocale(locale);
    ejsRes = ResourceBundle.getBundle("org.colos.ejs.library.resources.ejs_res", locale);
    Model._getTranslatorUtil().setLocaleItem(currentLocaleItem);
    setViewLocale();
    popupMenu = null;
    resetDescriptionPages();
    if (_resetModel) model._reset();
  }

  public LocaleItem getLocaleItem() {
    return currentLocaleItem;
  }

  public void setLocale (String _language) {
//    System.out.println("Setting to locale "+_language);
    LocaleItem item = LocaleItem.getLocaleItem(_language);
    if (item!=null) setLocaleItem(item);
    else {
      System.out.println("Warning! Html editor is ignoring unrecognized locale name : "+_language+"\n");
      setLocaleItem(LocaleItem.getDefaultItem());
    }
  }

  public Locale getLocale() {
    Locale locale = currentLocaleItem.getLocale();
    if (locale==null) locale = Locale.getDefault();
    return locale;
  }
  
  protected void setViewLocale() {
    elementsMenu = null;
    popupMenuExtraEntries = null;
    if (view instanceof EjsControl) ((EjsControl) view).addElementsMenuEntries();
    view.update();
    view.finalUpdate();
  };

// -----------------------------
// Controlling the execution
// -----------------------------

//  /**
//   * Stops the simulation and frees memory
//   */
//  public void onExit() {
//    cleanExtractedResources();
//    super.onExit();
//  }

// ---------------------------------------------------
//  Displaying the description
// ---------------------------------------------------

  private boolean showDescriptionOnStart = true;
  private JFrame descriptionDialog = null;
  private JTabbedPane descriptionPanel = null;
  private List<EditorAndScroll> descriptionPagesList=null;

  private void createDescriptionDialog(int _width, int _height) {
    descriptionPanel = new JTabbedPane();
    final javax.swing.border.Border border = BorderFactory.createEmptyBorder(1,1,1,1);
    final javax.swing.border.Border clickedBorder = BorderFactory.createLineBorder(new Color (128,64,255),1);
    Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    increaseFontButton = new JLabel (ResourceLoader.getIcon("/org/colos/ejs/library/resources/fontUp.gif"));
    increaseFontButton.setBorder(border);
    increaseFontButton.setCursor(handCursor);
    increaseFontButton.setToolTipText(DisplayRes.getString("DrawingFrame.IncreaseFontSize_menu_item")); //Memory.getResource("Increase font"));
    increaseFontButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(1000,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
              if (descriptionPagesList==null) return;
              for (EditorAndScroll eas : descriptionPagesList) {
                Font oldFont = eas.editorPane.getFont();
                float newSize = oldFont.getSize2D()*1.1f;
                eas.editorPane.setFont(oldFont.deriveFont(newSize));
              }
            }
          });
        }
      }
    });
    decreaseFontButton = new JLabel (ResourceLoader.getIcon("/org/colos/ejs/library/resources/fontDown.gif"));
    decreaseFontButton.setBorder(border);
    decreaseFontButton.setCursor(handCursor);
    decreaseFontButton.setToolTipText(DisplayRes.getString("DrawingFrame.DecreaseFontSize_menu_item"));
    decreaseFontButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(1000,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
              if (descriptionPagesList==null) return;
              for (EditorAndScroll eas : descriptionPagesList) {
                Font oldFont = eas.editorPane.getFont();
                float newSize = Math.max(10.0f,oldFont.getSize2D()/1.1f);
                eas.editorPane.setFont(oldFont.deriveFont(newSize));
              }
            }
          });
        }
      }
    });
    openPageButton = new JLabel (ResourceLoader.getIcon("/org/colos/ejs/library/resources/fullScreen.gif"));
    openPageButton.setBorder(border);
    openPageButton.setCursor(handCursor);
    openPageButton.setToolTipText(Memory.getResource("DescriptionPages.OpenExternalBrowser"));
    openPageButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(1000,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
              if (descriptionPagesList==null) return;
              Component selectedComponent = descriptionPanel.getSelectedComponent();
              for (EditorAndScroll pane : descriptionPagesList) {
                if (selectedComponent==pane.scrollPane) { // This one is
                  if (!openDescriptionPageInBrowser(pane.name))
                    JOptionPane.showMessageDialog(openPageButton, Memory.getResource("DescriptionPages.ErrorOpeningFiles"), Memory.getResource("Error"), JOptionPane.ERROR_MESSAGE);
                  return;
                }
              }
              System.err.println ("DescriptionPages : Could not find the selected page.");
            }
          });
        }
      }
    });
    JPanel buttonsPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
//    buttonsPanel.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
    buttonsPanel.add(increaseFontButton);
    buttonsPanel.add(decreaseFontButton);
    buttonsPanel.add(openPageButton);
    
    JPanel bottomPanel = new JPanel (new BorderLayout());
    bottomPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    JComponent localeComponent = Model._getTranslatorUtil().getSelectorComponent(this);
    if (localeComponent!=null) {
      localeComponent.setBorder(new javax.swing.border.EmptyBorder(0,0,0,2));
      bottomPanel.add(buttonsPanel,BorderLayout.WEST);
      bottomPanel.add(localeComponent,BorderLayout.EAST);
    }
    else bottomPanel.add(buttonsPanel,BorderLayout.CENTER);
    
    descriptionDialog = new JFrame (Memory.getResource("DescriptionFor")+" "+getModelClassname()); //JDialog(ownerFrame,title);
    descriptionDialog.getContentPane().add(descriptionPanel,java.awt.BorderLayout.CENTER);
    descriptionDialog.getContentPane().add(bottomPanel,java.awt.BorderLayout.SOUTH);
    if (_width<=0) _width = 600;
    if (_height<=0) _height = 400;
    java.awt.Rectangle bounds = EjsControl.getDefaultScreenBounds();
//    Dimension bounds = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    _width = Math.min(_width, bounds.width-10);
    _height = Math.min(_height, bounds.height-10);
    descriptionDialog.setLocation(bounds.x + (bounds.width - _width)/2, bounds.y + (bounds.height - _height)/2);
  }
  
  /**
   * Called by subclasses to add a description page at start-up
   * @param _htmlPage
   * @param _width
   * @param _height
   */
  public void addDescriptionPage (String _htmlPage, int _width, int _height, boolean _visible) {
    HtmlPageInfo pageInfo = model._getHtmlPageInfo(_htmlPage, currentLocaleItem);
    if (pageInfo==null) {
      JOptionPane.showMessageDialog(popupTriggeredBy,"Html file not found: "+_htmlPage,"Description Error",JOptionPane.ERROR_MESSAGE);
      return;
    }
//    if (model._isApplet()) { // So that resource loader minimizes traffic network
//      if (!_htmlPage.startsWith("/")) _htmlPage = "/" + _htmlPage;
//    }
    
//    System.out.println ("Locale = "+this.currentLocaleItem.getKeyword());
//    System.out.println ("Loading html page = "+pageInfo.getLink());

    Resource htmlRes = ResourceLoader.getResource(pageInfo.getLink());
    if (htmlRes == null) {
      System.err.println("Add description page error : Couldn't find description file: "+_htmlPage);
      System.err.println("Link = "+pageInfo.getLink());
      return;
    }
    
    if (descriptionDialog==null) createDescriptionDialog(_width,_height);

    try {
      JEditorPane editorPane = new JEditorPane(){
        //        public void paintComponent(java.awt.Graphics g) {
        //          java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        //          g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //          g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        //          super.paintComponent(g2);
        //        }
      };
      HTMLEditorKit kit = new HTMLEditorKit();
      editorPane.setEditorKit(kit);
      editorPane.setDocument(kit.createDefaultDocument());
      //      editorPane.setContentType ("text/html");
      //      editorPane.putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,Boolean.TRUE );
      editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
      editorPane.setEditable(false);
      //        try { kit.getStyleSheet().loadRules(ResourceLoader.openReader("_ejs_library/css/ejsPage.css"),null); }
      //        catch (Exception exc) { exc.printStackTrace(); }
      editorPane.addHyperlinkListener(new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
            openURL(e.getSource(),e.getURL(),getView().getComponent(getMainWindow()), model._getApplet()!=null);
          }
        }
      });

      editorPane.setPage(htmlRes.getURL());
      JScrollPane editorScrollPane = new JScrollPane(editorPane);
      editorScrollPane.setPreferredSize(new Dimension(_width,_height));
      //        descriptionPanel.add(pageInfo.getTitle(),editorScrollPane);
      //        descriptionDialog.pack();
      if (descriptionPagesList==null) descriptionPagesList = new ArrayList<EditorAndScroll>();
      EditorAndScroll pane = new EditorAndScroll(_htmlPage,editorPane,editorScrollPane);
      pane.setVisible(_visible);
      descriptionPagesList.add(pane);
    } 
    catch (IOException ioExc) {
      System.err.println("Attempted to read a bad URL: " + htmlRes.getURL());
    }
  } 

//  static private void setOneHTMLPage (JEditorPane editorPane, Resource htmlRes) throws Exception {
//    String content = htmlRes.getString();
//    if (content.startsWith("<?xml ")) { // Remove anything up to the first <html
//      int begin = content.indexOf("<html");
//      if (begin>0) {
//        content = content.substring(begin);
//        editorPane.setText(content);
//      }
//      else editorPane.setText(content);
//      System.out.println("\n\n---------------\n\n"+content);
//    } 
//    else editorPane.setPage(htmlRes.getURL());
//  }
  
  /**
   * Old form for backwards compatibility
   * @param _title
   * @param _htmlPage
   * @param width
   * @param height
   */
  public void addDescriptionPage (String _title, String _htmlPage, int width, int height) { addDescriptionPage (_htmlPage, width, height,true); }

  public void setDescriptionPageVisible(String _name, boolean _visible) {
    if (descriptionPagesList==null) return;
    for (EditorAndScroll pane : descriptionPagesList) {
      if (pane.name.equals(_name)) {
        if (pane.isVisible()!=_visible) {
          pane.setVisible(_visible);
          recreateDescriptionPanel();
        }
        break;
      }
    }  
  }

  /**
   * Recreates the description panel 
   */
  protected void recreateDescriptionPanel() {
    if (descriptionPanel==null) return;
    descriptionPanel.removeAll();
    for (EditorAndScroll pane : descriptionPagesList) {
      if (!pane.isVisible()) continue;
      HtmlPageInfo pageInfo = model._getHtmlPageInfo(pane.name, currentLocaleItem);
      if (pageInfo!=null) descriptionPanel.add(pageInfo.getTitle(),pane.scrollPane);
    }
    descriptionDialog.pack();
    if (descriptionPanel.getTabCount()<=0) {
      descriptionDialog.setVisible(false);
      showDescriptionOnStart = false;
    }
  }
  
  /**
   * Resets the description pages after a change of locale, mainly
   */
  protected void resetDescriptionPages() {
    if (descriptionPagesList==null) return;
    try {
      for (EditorAndScroll pane : descriptionPagesList) {
        HtmlPageInfo pageInfo = model._getHtmlPageInfo(pane.name, currentLocaleItem);
        if (pageInfo!=null) {
          Resource htmlRes = ResourceLoader.getResource(pageInfo.getLink());
          URL url = htmlRes.getURL();
          if (pane.editorPane.getPage()!=url) {
            pane.editorPane.setPage(url);
            descriptionPanel.setTitleAt(descriptionPanel.indexOfComponent(pane.scrollPane), pageInfo.getTitle());
          }
        }
      }
    } catch (Exception exc) { exc.printStackTrace(); }
    if (increaseFontButton!=null) {
      descriptionDialog.setTitle(Memory.getResource("DescriptionFor")+" "+getModelClassname());
      increaseFontButton.setToolTipText(DisplayRes.getString("DrawingFrame.IncreaseFontSize_menu_item"));
      decreaseFontButton.setToolTipText(DisplayRes.getString("DrawingFrame.DecreaseFontSize_menu_item"));
      openPageButton.setToolTipText(Memory.getResource("DescriptionPages.OpenExternalBrowser"));
    }
  }
  
  /**
   * Whether the description dialog should show at start-up (true by default)
   * @param _show
   */
  public void showDescriptionAtStartUp(boolean _show) { showDescriptionOnStart = _show; }
  
  /**
   * Shows the description dialog
   */
  public void showDescription () { showDescription (true); }

  /**
   * Shows/Hides the description dialog
   * @param _show
   */
  public void showDescription (boolean _show) {
    if (descriptionDialog!=null) descriptionDialog.setVisible(_show);
  }

  /**
   * Returns the URL of a given Description page
   * @param _htmlPage
   * @return
   */
  public URL getDescriptionPageURL (String _htmlPage) {
    HtmlPageInfo pageInfo = model._getHtmlPageInfo(_htmlPage, currentLocaleItem);
    if (pageInfo==null) return null;
    return ResourceLoader.getResource(pageInfo.getLink()).getURL();
  }

  /**
   * Extracts the description pages (if not yet done) and opens them in the system browser 
   */
  public boolean openDescriptionPagesInBrowser() {
    File tempDir = extractResources();
    if (tempDir==null) return false;
    boolean failed = false;
    for (EditorAndScroll pane : descriptionPagesList) {
      HtmlPageInfo pageInfo = model._getHtmlPageInfo(pane.name, currentLocaleItem);
      if (pageInfo==null) {
        System.err.println ("DescriptionPages : Could not find the page: "+pane.name);
        failed = true;
      }
      if (!openExternalBrowser(tempDir,pageInfo)) failed = true;
    }
    return failed;
  }

  /**
   * Extracts the description pages and opens the required page in the system browser 
   */
  public boolean openDescriptionPageInBrowser(String _name) {
    File tempDir = extractResources();
    if (tempDir==null) {
      return false;
    }
    HtmlPageInfo pageInfo = model._getHtmlPageInfo(_name, currentLocaleItem);
    if (pageInfo==null) {
      System.err.println ("DescriptionPages : Could not find the page: "+_name);
      return false;
    }
    return openExternalBrowser(tempDir,pageInfo);
  }

  /**
   * Extracts the simulation resources
   * @return File the temporary directory where files where extracted, null if there was any error
   */
  private File extractResources() {
    if (org.opensourcephysics.display.OSPRuntime.appletMode) return null;
    File descTempDirectory = new File(getTemporaryDir()+"DescriptionPages/");
    if (!descTempDirectory.exists()) {
      if (!descTempDirectory.mkdirs()) {
        System.err.println ("DescriptionPages : Could not create directory : "+descTempDirectory.getAbsolutePath());
        JOptionPane.showMessageDialog(openPageButton, Memory.getResource("DescriptionPages.CannotExtractFiles"), Memory.getResource("Error"), JOptionPane.ERROR_MESSAGE);
        return null;
      }
    }
    //System.err.println ("Extracting description pages to folder : "+descTempDirectory.getAbsolutePath());
    for (String filename : model._getClassEjsResources()) {
      if (extractToDirectory(filename,descTempDirectory,false)==null) return null;
    }
    extractToDirectory("/_ejs_library/css/ejss.css",descTempDirectory,false); // New in %.3Failure to do this does not invalidate the process
    return descTempDirectory;
  }


  /**
   * Opens the required HTML file in the system browser
   * @return false if failed 
   */
  private boolean openExternalBrowser(File _tmpDir, HtmlPageInfo _pageInfo) {
    String localPage = _pageInfo.getLink();
    if (localPage.startsWith("./")) localPage = model._getClassModelDirectory()+localPage.substring(2);
    if (extractToDirectory(localPage,_tmpDir,false)==null) return false;
    localPage = "file:///"+FileUtils.correctUrlString(FileUtils.getPath(_tmpDir)+localPage);
    return org.opensourcephysics.desktop.OSPDesktop.displayURL(localPage);
  }
  
  static private class EditorAndScroll {
    String name;
    JEditorPane editorPane;
    JScrollPane scrollPane;
    boolean visible=true;
    
    EditorAndScroll(String _name, JEditorPane _editor, JScrollPane _scroll) {
      name = _name;
      editorPane = _editor;
      scrollPane = _scroll;
    }
    public void setVisible(boolean _visible) { visible = _visible; }
    
    public boolean isVisible() { return visible; }
  }

  public void openURL (Object _source, URL _url, Component _parentComponent, boolean _isApplet) {
    try {
      String path = _url.toString();
      int index = path.indexOf("jar!");
//      System.out.println ("Path = "+path);
      if (index<0) { // It is an external link
        tellTheUserToWait();
        OSPDesktop.displayURL(_url.toString()); 
        return;
      }
      path = path.substring(index+4);
      if (path.startsWith("/")) path = path.substring(1);
      
      if (_isApplet) {
        tellTheUserToWait();
        OSPDesktop.displayURL(_url.toString());
      }
      else {
        String lowerCase =path.toLowerCase();
        boolean mustExtract = (lowerCase.indexOf(".html")<0) && (lowerCase.indexOf(".htm")<0);  
        if (mustExtract) {
          File resource = Simulation.getResourceFile(path);
          if (resource==null) JOptionPane.showMessageDialog(_parentComponent, ejsRes.getString("Simulation.ErrorWhenOpening")+":\n  "+_url.getFile());
          else {
            tellTheUserToWait();
            OSPDesktop.displayURL(resource.toURI().toString());
          }
        }
        else {
          int index2 = path.indexOf('#');
          String name = (index2>=0) ? path.substring(0,index2) : path;
          for (EditorAndScroll pane : descriptionPagesList) {
            if (name.equals(pane.name)) {
              descriptionPanel.setSelectedComponent(pane.scrollPane);
              if (pane.editorPane.getPage()!=_url) pane.editorPane.setPage(_url);
              if (_url.getRef()!=null) pane.editorPane.scrollToReference(_url.getRef());
              return;
            }
          }
          ((JEditorPane) _source).setPage(_url);
        }
      }
    } 
    catch(Exception exc1) {
      JOptionPane.showMessageDialog(_parentComponent, ejsRes.getString("Simulation.ErrorWhenOpening")+":\n  "+_url.getFile());
      exc1.printStackTrace(); 
    }
  }

  private void tellTheUserToWait() { // Tell the user that we are processing the command
    final JDialog warningDialog = new JDialog(descriptionDialog,ejsRes.getString("Simulation.Opening"));
    final JLabel label = new JLabel(ejsRes.getString("Simulation.Opening"));
    label.setBorder(new javax.swing.border.EmptyBorder(5,5,5,5));
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { 
        warningDialog.getContentPane().setLayout(new java.awt.BorderLayout());
        warningDialog.getContentPane().add(label,java.awt.BorderLayout.CENTER);
        warningDialog.validate();
        warningDialog.pack();
        warningDialog.setLocationRelativeTo (descriptionDialog);
        warningDialog.setModal  (false);
        warningDialog.setVisible(true); 
      }
    });
    javax.swing.Timer timer = new javax.swing.Timer(3000,new ActionListener(){
      public void actionPerformed(ActionEvent _actionEvent) { 
        warningDialog.setVisible(false);
        warningDialog.dispose();
      }
    });
    timer.setRepeats(false);
    timer.start();
  }
  
// --------------------------------------------------------
// Accesing model variables
// --------------------------------------------------------

  static private final String dummy="";
  static private final Class<?> strClass=dummy.getClass();

/**
   * This method returns a String with the value of a public variable of the
   * model. If the variable is an array, individual element values are
   * separated by a comma. Only public variables of primitive or String
   * type can be accessed.
   * @param _name The name of a public variable of the model
   * @return The value of the variable as a String.
   */

  public String getVariable (String _name) {
    return getVariable (_name,",");
  }

  /**
   * This method returns a String with the value of a public variable of the
   * model. If the variable is an array, individual element values are
   * separated by the specified separator string. Only public variables
   * of primitive or String type can be accessed.
   * @param _name The name of a public variable of the model
   * @param _sep A separator string to use for array variables
   * @return The value of the variable
   */
  public String getVariable (String _name, String _sep) {
    if (model==null) return null;
    try {
      Field field = model.getClass().getField(_name);
      if (field.getType().isArray()) {
        String txt ="";
        Object array = field.get(model);
        int l = Array.getLength(array);
        for (int i=0; i<l; i++) {
          if (i>0) txt += _sep+Array.get(array,i).toString();
          else txt += Array.get(array,i).toString();
        }
        return txt;
      }
      return field.get(model).toString();
    }
    catch (Exception exc) { errorMessage (exc); return null; }
  }

  /**
   * This method sets the value of a public variable of the model. If the
   * variable is an array, individual element values must separated by a
   * comma.
   * In this case, if the number of values specifies differs with the
   * length of the array (a warning may be issued and) either the extra values
   * are ignored (if there are more) or the last elements of the array will be
   * left unmodified (if there are less).
   * If the values provided cannot be parsed to the variable type (an error
   * message may be issued and) the method returns false.
   * Only public variables of primitive or String type can be accessed.
   * @param _name the name of a public variable of the model
   * @param _value the value to be given to the variable
   * @return true if the process was completed sucessfully, false otherwise
   */
  public boolean setVariable (String _name, String _value) {
    return setVariable (_name,_value,",");
  }

  /**
   * This method sets the value of a public variable of the model.
   * If the variable is an array, individual element values must separated by
   * the specified separator string.
   * In this case, if the number of values specifies differs with the
   * length of the array (a warning may be issued and) either the extra values
   * are ignored (if there are more) or the last elements of the array will be
   * left unmodified (if there are less).
   * Only public variables of primitive or String type can be accessed.
   * If the values provided cannot be parsed to the variable type (an error
   * message may be issued and) the method returns false.
   * @param _variable the name of a public variable of the model
   * @param _value the value to be given to the variable
   * @param _sep the separator string for arrays
   * @return true if the process was completed sucessfully, false otherwise
   */
  public boolean setVariable (String _variable, String _value, String _sep) {
    if (model==null) return false;
    try {
      Field field = model.getClass().getField(_variable);
      if (field.getType().isArray()) {
        boolean result=true;
        Object array = field.get(model);
        int i=0, l = Array.getLength(array);
        Class<?> type = field.getType().getComponentType();
        StringTokenizer line = new StringTokenizer(_value,_sep);
        if (l<line.countTokens()) errorMessage("Warning: there are less elements in the array than values provided!");
        else if (l>line.countTokens()) errorMessage("Warning: there are more elements in the array than values provided!");
        while (line.hasMoreTokens() && i<l) {
          String token = line.nextToken();
          if      (type.equals(Double.TYPE))  Array.setDouble(array,i,Double.parseDouble(token));
          else if (type.equals(Float.TYPE))   Array.setFloat(array,i,Float.parseFloat(token));
          else if (type.equals(Byte.TYPE))    Array.setByte(array,i,Byte.parseByte(token));
          else if (type.equals(Short.TYPE))   Array.setShort(array,i,Short.parseShort(token));
          else if (type.equals(Integer.TYPE)) Array.setInt(array,i,Integer.parseInt(token));
          else if (type.equals(Long.TYPE))    Array.setLong(array,i,Long.parseLong(token));
          else if (type.equals(Boolean.TYPE)) {
            if (token.trim().toLowerCase().equals("true")) Array.setBoolean(array,i,true);
            else Array.setBoolean(array,i,false);
          }
          else if (type.equals(Character.TYPE))Array.setChar(array,i,token.charAt(0));
          else if (type.equals(strClass))      Array.set(array,i,token);
          else result=false;
          i++;
        }
        return result;
      }
      Class<?> type = field.getType();
      if      (type.equals(Double.TYPE))  field.setDouble(model,Double.parseDouble(_value));
      else if (type.equals(Float.TYPE))   field.setFloat(model,Float.parseFloat(_value));
      else if (type.equals(Byte.TYPE))    field.setByte(model,Byte.parseByte(_value));
      else if (type.equals(Short.TYPE))   field.setShort(model,Short.parseShort(_value));
      else if (type.equals(Integer.TYPE)) field.setInt(model,Integer.parseInt(_value));
      else if (type.equals(Long.TYPE))    field.setLong(model,Long.parseLong(_value));
      else if (type.equals(Boolean.TYPE)) {
        if (_value.trim().toLowerCase().equals("true")) field.setBoolean(model,true);
        else field.setBoolean(model,false);
      }
      else if (type.equals(Character.TYPE))field.setChar(model,_value.charAt(0));
      else if (type.equals(strClass))      field.set(model,_value);
      else return false;
      return true;
    }
    catch (Exception exc) { errorMessage (exc); return false; }
  }

  /**
   * This method is used to set more than one variables of the model
   * at once. Pairs of the type 'variable=value' must be separated
   * by semicolons. Then they will be tokenized and sent to setVariable().
   * @param _valueList the string containing the pairs 'variable=value'
   * @return true if all the variables are correctly set by setVariable()
   * @see setVariable(String,String);
   *
   */
  public boolean setVariables (String _valueList) {
    return setVariables (_valueList, ";", ",");
  }

  /**
   * This method is used to set more than one variables of the model
   * at once. Pairs of the type 'variable=value' must be separated
   * by the separator string _sep. Then they will be tokenized and
   * sent to setVariable(), using _arraySep as separator string for
   * values of array variables.
   * @param _valueList the string containing the pairs 'variable=value'
   * @param _sep the separator string between pairs
   * @param _arraySep the separator string for values of array variables
   * @return true if all the variables are correctly set by setVariable()
   * @see setVariable(String,String)
   *
   */
  public boolean setVariables (String _valueList, String _sep, String _arraySep) {
    boolean result = true;
    String name="", value="";
    StringTokenizer line = new StringTokenizer(_valueList,_sep);
    while (line.hasMoreTokens()) {
      String token = line.nextToken();
      int index = token.indexOf('=');
      if (index<0) { result = false; continue; }
      name  = token.substring (0,index).trim();
      value = token.substring (index+1).trim();
      boolean partial = setVariable(name,value,_arraySep);
      if (partial==false) result = false;
    }
    update(); // Should this be called by the user explicitly?
    return result;
  }

// --------------------------------------------------------
// Input /Output
// --------------------------------------------------------

  static private Hashtable<String, Object> memory = new Hashtable<String, Object>();

  protected MoodleLink moodle=null;
  private MethodWithOneParameter _init_=null;//FKH20060417

  /**
   * Initializes the applet
   * @return true if teh applet is non-null
   */
  public LauncherApplet initMoodle () {
    LauncherApplet applet = model._getApplet();
    if (applet==null) return null;
    try {// FKH 20060417
        String s = applet.getParameter("init");
        if(s != null)
        {
            _init_ = new MethodWithOneParameter(0, applet._model, s, null, null, applet);
            _init_.invoke(0, applet);
        }
    } catch(Exception exception){
        exception.printStackTrace();
    }//END FKH 20060417
    return applet;
  }

  public boolean isMoodleConnected () {
    if (moodle==null) return false;
    return moodle.isConnected();
  }

  
  // -------------------------------- FKH 20060415 for javascript and java connection
  // Search also for FKH in Model.java
  
  private LauncherApplet javascriptControledApplet=null;
  private boolean javascriptControlMode() { return false; }

  /*
  public void ejsPopup(String url){// works for IE, but it is not working for netscape
        String command = "window.open('"+url+"', 'ejspopup', 'menubar=0,location=0,scrollbars,resizable,width=100,height=100');";
        ejsEval(command);
  }
  protected netscape.javascript.JSObject htmlWindow=null;//FKH 20060731
  public void ejsEval(String command){// call html window to exec command
    if(javascriptControlMode(false)){// valid even for signed applet
      if(htmlWindow==null)htmlWindow = netscape.javascript.JSObject.getWindow(javascriptControledApplet);
      htmlWindow.eval(command);
    }
  }
  public void ejsCommand(String Args[]){// call Javascript function from ejs generated simulations
    ejsCommand("ejsCommand",Args);
  }
  public void ejsCommand(String JScriptFunctionName,String Args[]){
    if(javascriptControlMode(false)){
      if(htmlWindow==null)htmlWindow = netscape.javascript.JSObject.getWindow(javascriptControledApplet);
      htmlWindow.call(JScriptFunctionName,Args);
    }
  }
  private boolean javascriptControlMode(){
    return javascriptControlMode(true);
  }
  private boolean javascriptControlMode(boolean localMode){
    if(javascriptControledApplet==null)javascriptControledApplet = model._getApplet();
    if(javascriptControledApplet==null || isMoodleConnected())return false;
    if(localMode){
    boolean canUseLocalFile=true;// signed applet
    try {
     System.getProperty("user.dir");
      }
      catch (Exception exc) {
        canUseLocalFile = false;
      }
    return !canUseLocalFile;
  }
    return true;
  }

  */
  // -------------------------------------------------- END FKH
  
  static public boolean isImageFormatSupported (String _format) {// for applet mode saveImage
    try {
      String[] names = javax.imageio.ImageIO.getWriterFormatNames();
      for(int i=0; i < names.length; i++) {
//        System.out.println("Image format supported = "+names[i]);
        if (names[i].equalsIgnoreCase(_format)) return true;
      }
    }
    catch(Exception ex) { }
    return false;
  }
  /**
   * Creates an animated gif for a view element.
   * @param _filename the name of the gif file to create
   * @param _element the name of the view element to get the image from
   * @return AnimatedGif
   */
/*
  public AnimatedGif createAnimatedGif (String _filename, String _element) {
    if (view==null) return null;
    java.awt.Component comp = view.getComponent(_element);
    if (comp==null) {
      System.err.println("Component not found: "+_element);
      return null;
    }
    return new AnimatedGif(comp,_filename);
  }
*/


// --------------------------------------------------------
// Tools
// --------------------------------------------------------

  protected JPopupMenu popupMenu=null; //CJB for collaborative (change private for protected)
  private JMenu elementsMenu=null;
  private AbstractList<Object> popupMenuExtraEntries=null;
  private Component popupTriggeredBy=null;
  protected Experiment currentExperiment=null; //CJB for collaborative (change private for protected) 

  protected void setUnderEjs (boolean value) { isUnderEjs = value; }
  
  public boolean isUnderEjs() { return isUnderEjs; }
  
  public List<Experiment> getExperiments () { return null; }

  public Experiment createExperiment (String _experimentName) { return null; }

  public void runExperiment (Experiment _experiment) {
    if (_experiment==null) return;
    if (currentExperiment!=null) currentExperiment._abortExperiment();
    currentExperiment = _experiment;
    _experiment._runExperiment();
  }

  public void runExperiment (String _experimentClassname) {
    runExperiment(createExperiment(_experimentClassname));
  }

  public void killExperiment () {
    if (currentExperiment!=null) currentExperiment._abortExperiment();
    currentExperiment = null;
  }

  public void addMenuEntries (List<Object> _entries) {
    if (popupMenuExtraEntries==null) popupMenuExtraEntries = new ArrayList<Object>();
    popupMenuExtraEntries.addAll(_entries);
    popupMenu = null; // So that to re-generate it
  }

  public void addElementMenuEntries (String elementName, List<Object> _entries) {
    if (elementsMenu==null) {
      elementsMenu = new JMenu(getMenuText("ejs_res:MenuItem.Elements"));
      List<Object> list = new ArrayList<Object>();
      list.add(elementsMenu);
      addMenuEntries (list);
    }
    JMenu menu = new JMenu(elementName);
    for (Object entry : _entries) addMenuItem (menu,entry);
    addMenuItem (elementsMenu,menu);
  }

  public Component getTopLevelComponent(Component _component) {
    Component c = _component;
    while (c.getParent()!=null) c = c.getParent();
    return c;
  }


  public void getPopupMenu (Component _component, String _element) {
    getPopupMenu(_component, -1, -1, _element);
  }

  JMenuItem snapshotMenuItem;
  
  @SuppressWarnings("serial")
  public void getPopupMenu (final Component _component, int _x, int _y, String _element) {
    captureElement = _element;
    if (popupMenu==null) {
      boolean canAccessDisk = true;
      try { System.getProperty("user.home"); }
      catch (Exception exc) { canAccessDisk = false; }
      popupMenu = new JPopupMenu();

      List<Experiment> customActions = getExperiments();
      if (customActions!=null) {
        JMenu experimentsMenu = new JMenu(getMenuText("tools_res:MenuItem.Experiments"));
        for (Iterator<Experiment> it = customActions.iterator(); it.hasNext(); ) {
          final Experiment experiment = it.next();
          String name = translateString(experiment._getName());
          String desc = translateString(experiment._getDescription());
          AbstractAction action = new AbstractAction(name) {
              public void actionPerformed (ActionEvent evt) {  runExperiment(experiment); }
          };
          action.putValue(Action.SHORT_DESCRIPTION,desc);
          experimentsMenu.add(action);
        }
        popupMenu.add(experimentsMenu);
        JMenuItem killExperimentMenuItem = new JMenuItem(getMenuText("tools_res:MenuItem.KillExperiment"));
        killExperimentMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed (ActionEvent evt) { killExperiment (); }
        });
        popupMenu.add(killExperimentMenuItem);
        popupMenu.addSeparator();
      }

      if (descriptionDialog!=null) {
        popupMenu.add(new AbstractAction(Memory.getResource("ShowDescription")){
          public void actionPerformed(ActionEvent e) { descriptionDialog.setVisible(true); }
        });
      }
      
      if (popupMenuExtraEntries!=null && popupMenuExtraEntries.size()>0) {
        for (Object entry : popupMenuExtraEntries) addMenuItem (popupMenu,entry);
        popupMenu.addSeparator();
      }

      //CJB for collaborative
      extraAction2();
      //CJB for collaborative
      
      //LDLTorre for Moodle support
      //A�adida la condici�n de conexi�n a Moodle (No es necesario que el applet
      //est� firmado cuando manda ficheros a Moodle porque no se hace acceso al disco).
      if (canAccessDisk || isMoodleConnected()) { //if (canAccessDisk) {
      //LDLTorre for Moodle support
        
        JMenu snapshotMenu = new JMenu(getMenuText("ejs_res:MenuItem.SnapshotTools"));
        snapshotMenu.add(new AbstractAction(getMenuText("tools_res:MenuItem.Snapshot")){
          public void actionPerformed(ActionEvent e) {
            boolean wasRunning = isPlaying();
            if (wasRunning) pause();
            saveImageAs(captureElement);
            if (wasRunning) play();
          }
        });
        //popupMenu.addSeparator();
        popupMenu.add(snapshotMenu);
        
        snapshotMenuItem = new JMenuItem (getMenuText("ejs_res:MenuItem.SnapshotTool"));
        snapshotMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed (ActionEvent _evt) {
            ((DrawingPanel) view.getElement(captureElement).getVisual()).snapshot(); 
          }
        });
        snapshotMenu.add(snapshotMenuItem);
      
        if (videoUtil.isFullClass()) {
          snapshotMenu.add(new AbstractAction(getMenuText("ejs_res:MenuItem.TakeEPSSnapshot")){
            public void actionPerformed(java.awt.event.ActionEvent e) {
              videoUtil.takeSnapshot(getTopLevelComponent(_component));
            }
          });
          snapshotMenu.add(new AbstractAction(getMenuText("ejs_res:MenuItem.TakeEPSSnapshotWindow")){
            public void actionPerformed(java.awt.event.ActionEvent e) {
              focusListener = new AWTEventListener () {
                public void eventDispatched(AWTEvent _event) {
                  WindowEvent windowEvent = (WindowEvent) _event;
                  if (windowEvent.getID()==WindowEvent.WINDOW_GAINED_FOCUS ) {
                    java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(focusListener);
                    videoUtil.takeSnapshot(windowEvent.getWindow());
                  }
                }
              };
              System.out.println ("Put the focus on the window you want to capture");
              java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(focusListener,AWTEvent.WINDOW_EVENT_MASK);
            }
          });
          snapshotMenu.add(new AbstractAction(getMenuText("tools_res:MenuItem.Video")){
            public void actionPerformed(ActionEvent e) { videoUtil.startVideoTool(getView(),captureElement); }
          });
        }
        JMenu ioStateMenu = new JMenu(getMenuText("ejs_res:MenuItem.StateIO"));
        ioStateMenu.add(new AbstractAction(getMenuText("tools_res:MenuItem.SaveState")){
          public void actionPerformed(ActionEvent e) { saveState(null); }
        });
        boolean isLauncherMode;
        try { isLauncherMode = OSPRuntime.isLauncherMode(); }
        catch (Exception exc) { isLauncherMode = false; }
        if ( ! (isUnderEjs || isLauncherMode || isMoodleConnected())) ioStateMenu.add(new AbstractAction(getMenuText("ejs_res:MenuItem.SaveDefaultState")){
          public void actionPerformed(ActionEvent e) {
            File jarFile=null; 
            try {
              URL url = Simulation.class.getProtectionDomain().getCodeSource().getLocation();
              jarFile = new File(url.toURI());
            }
            catch (Exception exc) {
              JFileChooser chooser=OSPRuntime.createChooser("JAR",new String[]{"jar"});
              String filename = OSPRuntime.chooseFilename(chooser,getParentComponent(),false); // true = to save
              if (filename==null) return;
              jarFile = new File(filename);
            }
            saveDefaultStateToJar(jarFile,null); 
          }
        });
        ioStateMenu.add(new AbstractAction(getMenuText("tools_res:MenuItem.ReadState")){
          public void actionPerformed(ActionEvent e) {
            updateView = false; // So that the update() in readState does not take time
            readState(null);
            updateView = true; // The second update does refreshes the screen
            updateAfterModelAction();
          }
        });
        ioStateMenu.add(new AbstractAction(getMenuText("ejs_res:MenuItem.DefaultResetState")){
          public void actionPerformed(ActionEvent e) {
            resetFile = null;
            reset();
          }
        });
        popupMenu.add(ioStateMenu);
      }

      JMenu guiMenu = new JMenu(ejsRes.getString("MenuItem.GUI"));

      Model._getTranslatorUtil().addToMenu(guiMenu, this);

      JMenu fontMenu = new JMenu(DisplayRes.getString("DrawingFrame.Font_menu_title"));
      fontMenu.add(new AbstractAction(DisplayRes.getString("DrawingFrame.IncreaseFontSize_menu_item")){
        public void actionPerformed(ActionEvent e) {
          FontSizer.levelUp();
          for (String windowName : getWindowsList()) FontSizer.setFonts(getView().getComponent(windowName),FontSizer.getLevel());
        }
      });
      fontMenu.add(new AbstractAction(DisplayRes.getString("DrawingFrame.DecreaseFontSize_menu_item")){
        public void actionPerformed(ActionEvent e) {
          FontSizer.levelDown();
          for (String windowName : getWindowsList()) FontSizer.setFonts(getView().getComponent(windowName),FontSizer.getLevel());
        }
      });
      guiMenu.add(fontMenu);
      popupMenu.add(guiMenu);

      popupMenu.addSeparator();

      popupMenu.add(new AbstractAction(Memory.getResource("Simulation.AboutThisSimulation")){
        public void actionPerformed(ActionEvent e) { aboutThisSimulation(_component); }
      });

      if (canAccessDisk && EjsTool.hasEjsModel(getModel().getClass())) {
        popupMenu.add(new AbstractAction(ejsRes.getString("Simulation.OpenEjsModel")){
          public void actionPerformed(ActionEvent e) { 
            String systemPassword;
            try {  systemPassword = System.getProperty("launcher.password"); } 
            catch (Exception _exc) { systemPassword = null; } // do nothing
//            System.err.println("System password is "+systemPassword);
            boolean quit = EjsTool.runEjs(getModel().getClass(),systemPassword);
            if (!(model._isApplet() || OSPRuntime.isLauncherMode()) && quit) {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e1) {
                e1.printStackTrace();
              }
              System.exit(0);
            }
          }
        });
      }
      if (!isUnderEjs && canAccessDisk && !model._isApplet()) popupMenu.add(new AbstractAction(Memory.getResource("CreateHTMLPage")){
        public void actionPerformed(ActionEvent e) { createHTMLpage(getJarName()); }
      });

      if (canAccessDisk) { // Diagnostics submenu
        JMenu diagnosticsMenu = new JMenu(Memory.getResource("Diagnostics.Menu"));

        diagnosticsMenu.add(new AbstractAction(getMenuText("tools_res:Diagnostics.OS.About.Title")){
          public void actionPerformed(ActionEvent e) { Diagnostics.aboutOS(); }
        });      
        diagnosticsMenu.add(new AbstractAction(getMenuText("tools_res:Diagnostics.Java.About.Title")){
          public void actionPerformed(ActionEvent e) { Diagnostics.aboutJava(); }
        });
        diagnosticsMenu.add(new AbstractAction(getMenuText("ejs_res:Diagnostics.Properties")){
          public void actionPerformed(ActionEvent e) {
            DiagnosticsForSystem.aboutSystem(null);
//            Enumeration<?> propEnum = System.getProperties().propertyNames();
//            while(propEnum.hasMoreElements()) {
//              String next = (String) propEnum.nextElement();
//              String val = System.getProperty(next);
//              view.println (next+":  "+val); //$NON-NLS-1$
//            }
          }
        });
        diagnosticsMenu.add(new AbstractAction(getMenuText("ejs_res:Diagnostics.Threads")){
          public void actionPerformed(ActionEvent e) { DiagnosticsForThreads.aboutThreads(); }
        });

        diagnosticsMenu.add(new AbstractAction(getMenuText("tools_res:Diagnostics.Java3D.About.Title")){
          public void actionPerformed(ActionEvent e) { Diagnostics.aboutJava3D(); }
        });

        diagnosticsMenu.add(new AbstractAction(getMenuText("xuggle_res:Xuggle.Dialog.AboutXuggle.Title")){
          public void actionPerformed(ActionEvent e) { DiagnosticsForXuggle.aboutXuggle(); }
        });

//        diagnosticsMenu.add(new AbstractAction(getMenuText("tools_res:Diagnostics.QTJava.About.Title")){
//          public void actionPerformed(ActionEvent e) { Diagnostics.aboutQTJava(); }
//        });

        popupMenu.add(diagnosticsMenu);
      }
      
      popupMenu.add(new AbstractAction("OSP "+ToolsRes.getString("MenuItem.Log")){
        public void actionPerformed(ActionEvent e) {
          OSPLog log = OSPLog.getOSPLog();
          if (log!=null) log.setLocationRelativeTo(parentComponent);
          OSPLog.showLog();
        }
      });


    } // End of popupmenu creation

    // Show only for drawing panels
    if (snapshotMenuItem!=null) snapshotMenuItem.setVisible(_component instanceof DrawingPanel);

    Model._getTranslatorUtil().refreshMenu();

    popupTriggeredBy = _component;
    if (_x>=0 && _y>=0) popupMenu.show(_component,_x,_y);
    else popupMenu.show(_component,(_component.getWidth()-popupMenu.getWidth())/2,(_component.getHeight()-popupMenu.getHeight())/2);
  }

  public JPanel makeLabel (String _label, Set<JComponent> _labelSet) {
    JLabel label = new JLabel(ejsRes.getString(_label), SwingConstants.RIGHT);
    //label.setPreferredSize(LABEL_SIZE);
    if (_labelSet!=null) _labelSet.add(label);
    label.setBorder(new javax.swing.border.EmptyBorder(4,2,0,2));
    JPanel toppanel = new JPanel(new BorderLayout());
    toppanel.add(label, BorderLayout.NORTH);
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));
    panel.add(toppanel, BorderLayout.WEST);
    return panel;
  }
  
  static public JTextComponent makeField (Container _parent, int _lines, int _cols) {
    if (_lines<2) {
      JTextField field = new JTextField(_cols);
      _parent.add(field, BorderLayout.CENTER);
      field.setEditable(false);
      return field;
    }
    JTextArea area = new JTextArea(_lines, _cols);
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    JScrollPane scroll = new JScrollPane (area);
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));
    panel.add(scroll, BorderLayout.CENTER);
    //scroll.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    _parent.add(panel, BorderLayout.CENTER);
    return area;
  }
  
  static private void getTag(String input, String delim, JTextComponent textComponent) {
    String beginTag = "<"+delim+"><![CDATA[";
    String endTag = "]]></"+delim+">";
    int beginIndex = input.indexOf(beginTag);
    if (beginIndex<0) return;
    int endIndex = input.indexOf(endTag);
    if (endIndex<beginIndex) return;
    textComponent.setText(input.substring(beginIndex+beginTag.length(),endIndex));
  }
  
  private JDialog aboutThisDialog=null;
  
  public void aboutThisSimulation(Component _component) {
    if (aboutThisDialog==null) {
      String filename = getModel()._getClassEjsModel();
      int index = filename.indexOf('.');
      if (index>0) filename = filename.substring(0,index);
      filename += ".metadata";

      String metadata = ResourceLoader.getString(filename);
      if (metadata==null) {
        System.err.println ("Metadata file not found for "+filename);
        return;
      }
      int COLUMNS = 35;

      Set<JComponent> labelSet = new HashSet<JComponent>();
      JPanel titlePanel = makeLabel ("Simulation.Title",labelSet);
      JTextComponent titleField = makeField (titlePanel,1,COLUMNS);
      getTag(metadata,"Title",titleField);

      JPanel authorPanel = makeLabel ("Simulation.Author",labelSet);
      JTextComponent authorField = makeField (authorPanel,1,COLUMNS);
      getTag(metadata,"Author",authorField);

      JPanel keyPanel = makeLabel ("Simulation.Keywords",labelSet);
      JTextComponent keyField = makeField (keyPanel,1,COLUMNS);
      getTag(metadata,"Keywords",keyField);

      JPanel levelPanel = makeLabel ("Simulation.Level",labelSet);
      JTextComponent levelField = makeField (levelPanel,1,COLUMNS);
      getTag(metadata,"Level",levelField);

      JPanel languagePanel = makeLabel ("Simulation.Language",labelSet);
      JTextComponent languageField = makeField (languagePanel,1,COLUMNS);
      getTag(metadata,"Language",languageField);

      JPanel abstractPanel = makeLabel ("Simulation.Abstract",labelSet);
      JTextComponent abstractField = makeField (abstractPanel,7,COLUMNS);
      getTag(metadata,"Abstract",abstractField);

      // Make all labels in the set the same dimension
      int maxWidth = 0, maxHeight=0;
      for (JComponent label : labelSet) {
        maxWidth  = Math.max(maxWidth,  label.getPreferredSize().width);
        maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
      }
      Dimension dim = new Dimension (maxWidth,maxHeight);
      for (JComponent label : labelSet) label.setPreferredSize(dim);

      Box infoPanel = Box.createVerticalBox();
      infoPanel.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
      infoPanel.add(titlePanel);
      infoPanel.add(authorPanel);
      infoPanel.add(keyPanel);
      infoPanel.add(levelPanel);
      infoPanel.add(languagePanel);

      //    infoPanel.add(abstractPanel);
      
      JPanel metadataPanel = new JPanel (new BorderLayout());
      metadataPanel.add(infoPanel,BorderLayout.NORTH);
      metadataPanel.add(abstractPanel,BorderLayout.CENTER);
      metadataPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));


      aboutThisDialog = new JDialog();
      aboutThisDialog.setTitle(Memory.getResource("Simulation.AboutThisSimulation"));
      aboutThisDialog.getContentPane().setLayout(new BorderLayout());
      aboutThisDialog.getContentPane().add(metadataPanel,BorderLayout.CENTER);
      aboutThisDialog.setModal(false);
      aboutThisDialog.setResizable(true);
      aboutThisDialog.pack();
    }
    //editorDialog.setSize(editorDialog.getPreferredSize()); //res.getDimension("Osejs.CodeDialog.Size"));
    aboutThisDialog.setLocationRelativeTo(_component);
    aboutThisDialog.setVisible(true);

  }
  
  
  private String getMenuText (String name) {
    String text;
    if (name.startsWith("ejs_res:")) text = ejsRes.getString(name.substring(name.indexOf(':')+1));
    else if (name.startsWith("tools_res:")) text = ToolsRes.getString(name.substring(name.indexOf(':')+1));
    else if (name.startsWith("display_res:")) text = DisplayRes.getString(name.substring(name.indexOf(':')+1));
    else if (name.startsWith("xuggle_res:")) text = XuggleRes.getString(name.substring(name.indexOf(':')+1));
    else text = ToolsRes.getString(name);
    if (text.startsWith("!") && text.endsWith("!")) text = DisplayRes.getString(name);
    if (text.startsWith("!") && text.endsWith("!")) text = name;
    return text;
  }

  private void processMenuTexts (Object object) {
    if (object instanceof JMenu) {
      JMenu menu = (JMenu) object;
      menu.setText(getMenuText(menu.getActionCommand()));
      for (int i=0, n=menu.getItemCount(); i<n; i++) processMenuTexts (menu.getItem(i));
    }
    else if (object instanceof JMenuItem) {
      JMenuItem menuItem = (JMenuItem) object;
      menuItem.setText(getMenuText(menuItem.getActionCommand()));
    }
  }

  private void addMenuItem (JComponent targetMenu, Object object) {
    if (object instanceof Action) {
      final Action item = (Action) object;
      String name = (String) item.getValue(Action.NAME);
      String text = getMenuText(name);
      AbstractAction action = new AbstractAction(text) {
        private static final long serialVersionUID = 1L;
        public void actionPerformed (ActionEvent evt) { item.actionPerformed(evt); }
      };
      action.putValue(Action.SHORT_DESCRIPTION,text);
      if (targetMenu instanceof JMenu) ((JMenu) targetMenu).add(action);
      else if (targetMenu instanceof JPopupMenu) ((JPopupMenu) targetMenu).add(action);
    }
    else if (object instanceof JMenu) {
      JMenu menu = (JMenu) object;
      processMenuTexts(menu);
      if (targetMenu instanceof JMenu) ((JMenu) targetMenu).add(menu);
      else if (targetMenu instanceof JPopupMenu) ((JPopupMenu) targetMenu).add(menu);
    }
    else if (object instanceof JMenuItem) {
      JMenuItem menuItem = (JMenuItem) object;
      processMenuTexts(menuItem);
      if (targetMenu instanceof JMenu) ((JMenu) targetMenu).add(menuItem);
      else if (targetMenu instanceof JPopupMenu) ((JPopupMenu) targetMenu).add(menuItem);
    }
  }

  /**
   * Returns the list of top level containers of the simulation's view.
   * Subclasses should overwrite this method.
   * @return ArrayList
   */
  public List<String> getWindowsList() { return new ArrayList<String>(); }

  /**
   * Returns the main frame of the simulation's view.
   * Subclasses should overwrite this method.
   * @return ArrayList
   */
  public String getMainWindow() { return null; }


  /**
   * Utility that returns the name of the JAR file which contains the simulation
   * @return
   */
  public String getJarName() {
    URL clsUrl = getClass().getResource(getClass().getSimpleName() + ".class");
    if (clsUrl != null) {
      try {
        java.net.URLConnection conn = clsUrl.openConnection();
        if (conn instanceof java.net.JarURLConnection) {
          java.net.JarURLConnection connection = (java.net.JarURLConnection) conn;
          String path = connection.getJarFileURL().getPath();
          //System.out.println ("Path = "+path);
          int index = path.lastIndexOf('/');
          if (index>=0) path = path.substring(index+1);
          return path;
        }
      }
      catch (IOException e) { e.printStackTrace(); }
    }
    return null;
  } 
  /**
   * Creates an HTML page
   */
  public void createHTMLpage(String jarFilename) {
    JFileChooser chooser=OSPRuntime.createChooser("HTML",new String[]{"html"});
    chooser.setSelectedFile(new File(getModelClassname()+".html"));
    String filename = OSPRuntime.chooseFilename(chooser,popupTriggeredBy,true); // true = to save
    if (filename==null) return;
    if (filename.lastIndexOf('.')<0) filename += ".html";

    Component mainWindow = getView().getComponent(getMainWindow());
    Dimension size = new Dimension(100,100);
    if (mainWindow!=null) size = mainWindow.getSize();
    
    String name = getClass().getName();
    //System.out.println ("Classname = "+name);
    if (name.endsWith("Simulation")) name = name.substring(0,name.length()-10);
    name += "Applet.class";
    String programName = getModelClassname();
    StringBuffer buffer = new StringBuffer();
    buffer.append("<html>\n");
    buffer.append("  <head>\n");
    buffer.append("    <title>"+programName+" HTML</title>\n");
    buffer.append("  </head>\n");
    buffer.append("  <body >\n");
    buffer.append("    <applet code=\""+name+"\"\n");
    buffer.append("            codebase=\".\" archive=\""+jarFilename+"\"\n");
    buffer.append("            name=\""+programName+"\"  id=\""+programName+"\"\n");
    buffer.append("            width=\""+size.width+"\" height=\""+size.height+"\">\n");
    buffer.append("      <param name=\"permissions\" value=\"sandbox\">\n");
    buffer.append("  </applet>\n");
    buffer.append("  </body>\n");
    buffer.append("</html>\n");
    
    try {
      FileWriter fout = new FileWriter(filename);
      fout.write(buffer.toString());
      fout.close();
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(popupTriggeredBy,"Error saving file: "+filename,
          "File Error",JOptionPane.ERROR_MESSAGE);
    }

  }
  
  /**
   * Saves the image produced by a component. The name is created
   * from the name of the simulation class plus a counter
   * @param _element the name of the view element to get the image from
   * @return true if the file was correctly saved
   */
  public boolean saveImage (String _element) {
    GregorianCalendar cal = new GregorianCalendar();
    DecimalFormat format = new DecimalFormat("00");
    String date = cal.get(Calendar.YEAR)+""
                + format.format(cal.get(Calendar.MONTH))+""
                + format.format(cal.get(Calendar.DATE))+"_"
                + format.format(cal.get(Calendar.HOUR_OF_DAY))+""
                + format.format(cal.get(Calendar.MINUTE))+""
                + format.format(cal.get(Calendar.SECOND));
    return saveImage (getClassname()+"_"+date+".jpg",_element);
  }

  /**
   * Saves the image produced by a component. The user will be given the chance to choose a name.
   * @param _element the name of the view element to get the image from
   * @return true if the file was correctly saved
   */
  public boolean saveImageAs (String _element) {
    return saveImage (null,_element);
  }

  /**
   * Saves the image produced by a component to disk.
   * @param _filename the name of a file (either in disk or in memory). Null if the user must select a name.
   * @param _element the name of the view element to get the image from
   * @return true if the file was correctly saved
   */
  public boolean saveImage (String _filename, String _element) {
    if (view==null) return false;
    ControlElement ctrlEl = view.getElement(_element);
    if (ctrlEl==null) {
      System.err.println("Component not found: "+_element);
      return false;
    }
    Component comp = ctrlEl.getComponent();
    // Special case : Moodle
    if (isMoodleConnected () || javascriptControlMode() ) {
      if      (comp instanceof javax.swing.JFrame)  comp = ((javax.swing.JFrame)  comp).getContentPane();
      else if (comp instanceof javax.swing.JDialog) comp = ((javax.swing.JDialog) comp).getContentPane();
      // Generate the image
      BufferedImage bi = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      if (ctrlEl instanceof SpecialRender) ( (SpecialRender) ctrlEl).render(bi);
      else {
        java.awt.Graphics g = bi.getGraphics();
        comp.paint(g);
        g.dispose();
      }
      //LDLTorre for consistency
      //Simplemente he cambiado el nombre por defecto del fichero de la imagen para ajustarlo al que
      //ya se pon�a como nombre por defecto para grabar un fichero de texto.
      if (_filename==null) _filename = getModelClassname()+".gif"; //_filename = "default.gif"
      if(isMoodleConnected ())return moodle.saveImage(_filename,"GIF image", bi)!=null;
      //LDLTorre for consistency
      //applet mode starts here 20061201, use previously code
      try { // Save it to memory or disk
        String format = "jpg";
        int index = _filename.lastIndexOf('.');
        if (index>=0) format = _filename.substring(index+1).toLowerCase();
        else _filename = _filename + "." + format;
        boolean supported = isImageFormatSupported (format);
        if ( ! (supported || (videoUtil.isFullClass() && "gif".equalsIgnoreCase(format)) ) ) {
          System.err.println("Format not supported : "+format);
          return false;
        }
        java.io.OutputStream out = null;

        // FKH 200600415 add javascriptControlMode() for javascript control applet mode
        if (javascriptControlMode() || _filename.startsWith("ejs:")) out = new java.io.ByteArrayOutputStream();
        else out = new java.io.FileOutputStream(_filename);
        boolean result=true;
        if (supported) result = javax.imageio.ImageIO.write(bi, format, out);
        else result = videoUtil.writeGIF(out,bi); // use GIFEncoder
        out.close();
        if (!result) return false;
        if (_filename.startsWith("ejs:")) memory.put(_filename,((java.io.ByteArrayOutputStream) out).toByteArray());
        else if(javascriptControlMode()){
          javascriptControledApplet.setImageByteArray( ( (java.io.ByteArrayOutputStream) out).toByteArray());
        }
      }
      catch (Exception _exc) { _exc.printStackTrace(); return false; }
      return true;

    }
      return SnapshotTool.getTool().saveImage (_filename,comp,null);
  }

// --------------------------------------------------------
// Saving or reading the state
// --------------------------------------------------------

  public void processArguments (String[] _args) {
    LauncherApplet applet = model._getApplet();
    boolean stateRead = false;
    if (applet!=null) {
      try {
        // Try to read a default file with an initial state
        String arg0 = applet.getParameter("initialState");
        if (arg0 != null) stateRead = readState(arg0,applet.getCodeBase());
      } 
      catch (Exception exception){ exception.printStackTrace(); }
    }
    else {
      if (_args != null && _args.length > 0) {
        for (int i = 0; i < _args.length; i++) {
          if (_args[i].toLowerCase().endsWith(".xml")) {
            stateRead = readState(resetFile = _args[i]);
          }
          else if (_args[i].equals("-_initialState")) {
            stateRead = readState(resetFile = _args[++i]);
          }
          else if (_args[i].equals("-_noDescription")) {
            showDescriptionOnStart = false;
          }
        }
      }
    }
    if (!stateRead) {
      if ((!isMoodleConnected()) && ResourceLoader.getResource(DEFAULT_STATE_FILENAME)!=null) {
        OSPLog.fine("Reading default state from jar "+DEFAULT_STATE_FILENAME);
        readVariables(DEFAULT_STATE_FILENAME, (List<String>) null);
        resetFile = DEFAULT_STATE_FILENAME;
        if (view!=null) view.reset();
      }
//      else System.out.println ("No default state to read");
    }
    if (applet==null) { // decide whether or not to show the description
      boolean showIt = !isUnderEjs;
      // Check if under Launcher
      if (org.opensourcephysics.display.OSPRuntime.isLauncherMode()) showIt = false;
      else try {
        if ("true".equals(System.getProperty("osp_launcher"))) showIt = false;
      } catch (Exception exc) {}; // Do not complain
      if (showIt && showDescriptionOnStart) showDescription();
    }

  }

  static public List<String> toArrayList (String _list) {
    if (_list==null) return null;
    StringTokenizer tkn = new StringTokenizer (_list," ;,");
    List<String> arrayList = new ArrayList<String> ();
    while (tkn.hasMoreTokens()) arrayList.add(tkn.nextToken());
    return arrayList;
  }

  /**
   * Gets the default list used when saving or reading the state.
   * If null, the whole state is saved using reflection.
   */
  public List<String> getStateVariablesList () {
    return stateVariablesList;
  }

  /**
   * Sets the default list to use when saving or reading the state.
   * If not set, the whole state is saved using reflection.
   * @param _list ArrayList the list of variables that define the state
   * to save or read
   */
  public void setStateVariablesList (List<String> _list) {
    stateVariablesList = _list;
  }

  /**
   * Sets the default list to use when saving or reading the state
   * If not set, the whole state is saved using reflection.
   * @param _list String a comma or semicolon list of variables that define the
   * state to save or read
   */
  public void setStateVariablesList (String _varList) {
    stateVariablesList = toArrayList(_varList);
  }

  /**
   * Saves the complete state of the model (as defined by setStateVariablesList())
   * either to a file on the disk or to memory.
   * @param _filename the name of a file, either in disk or in memory (if given
   * the prefix "ejs:")
   * @return true if the file was correctly saved
   */
  public boolean saveState (String _filename) {
    return saveVariables (_filename,stateVariablesList);
  }

  /**
   * Reads the complete state of the model (as defined by setStateVariablesList())
   * either from a file on the disk, from memory, or from a url location.
   * @param _filename the name of a file, either in disk , in memory (if given
   * the prefix "ejs:") or from a url (if given the prefix "url:")
   * If running as an applet, the codebase of the applet is used for a non-qualified file
   * (one that does not starts explictly with "http:").
   * If file is null the user will be prompted to select a file from disk. 
   * @return true if the file was correctly read
   */
  public boolean readState (String _filename) {
    return readVariables (_filename,stateVariablesList);
//    if (_filename==null) return readVariables (_filename,null,stateVariablesList);
//    java.net.URL theCodebase = null;
//    if (model._getApplet()!=null) { // running as an applet: get the codebase
//      if (_filename.startsWith("url:") || _filename.toLowerCase().startsWith("http:")); // do nothing
//      else theCodebase = model._getApplet().getCodeBase();
//    }
//    return readVariables (_filename,theCodebase,stateVariablesList);
  }

  /**
   * Reads the complete state of the model (as defined by setStateVariablesList())
   * from a file on the disk, from memory or from a url location.
   * @param _filename the name of a file, either in disk , in memory (if given
   * the prefix "ejs:") or from a url (if given the prefix "url:")
   * @param _codebase the codebase when the simulation runs as an applet
   * @return true if the file was correctly read
   */
  public boolean readState (String _filename, java.net.URL _codebase) {
//    return readVariables (_filename, _codebase, stateVariablesList);
    return readVariables (_filename, stateVariablesList);
  }

// --------------------------------------------------------
// Saving or reading variables
// --------------------------------------------------------

  /**
   * Saves the state of the variables of the model specified in the given string,
   * either to a file on the disk or to memory.
   * @param _filename the name of a file (either in disk or in memory)
   * @param _varList a String with the list of variables separated by semicolons ';'
   * @return true if the file was correctly saved
   * @see #saveVariables(String,ArrayList)
   */
  public boolean saveVariables (String _filename, String _varList) {
    return saveVariables (_filename,toArrayList(_varList));
  }

  /**
   * Reads the state of the specified variables of the model either from a file
   * on the disk, from memory or from a url location.
   * @param _filename the name of a file (either in disk , in memory or a url)
   * @param _codebase the codebase when the simulation runs as an applet
   * @param _varList a String with the list of variables separated by semicolons ';'
   * @return true if the file was correctly read
   * @see #readVariables (String, java.net.URL, ArrayList)
   */
  public boolean readVariables (String _filename, java.net.URL _codebase, String _varList) {
    return readVariables (_filename,toArrayList(_varList));
//    return readVariables (_filename,_codebase,toArrayList(_varList));
  }

  /**
   * Reads the state of the specified variables of the model either from a file
   * on the disk, from memory or from a url location.
   * <p>If the name of the file starts with the prefix "ejs:", then the
   * state of the model will be read from a memory file that must have been
   * created previously with the corresponding call to saveVariables().</p>
   * <p>If the name of the file starts with "url:" it will be considered
   * a url location and the method will attempt to read the file from this
   * url (either locally or through the network).</p>
   * <p>If the codebase is non-null the file is considered to be a network file 
   * on that codebase.</p>
   * <p>In all other cases, the file will be considered to be on a disk. If the
   * filename is null, the user will be prompted to choose a file to read from.</p>
   * <p>If the name of the file ends in ".xml" the data is expected to be
   * in XML form. Otherwise it is considered to be binary.</p>
   * <p>Security considerations apply when running the simulation as
   * an applet.</p>
   * <p>The state of the model is read by reading from disk all its public
   * fields which implement the java.io.Serializable interface. This
   * includes primitives and arrays.</p>
   * @param _filename the name of a file (either in disk, in memory, or a url)
   * @param _codebase the codebase when the simulation runs as an applet
   * @param _varList the list of variables to read
   * @return true if the file was correctly read
   */
  public boolean readVariables (String _filename, java.net.URL _codebase, List<String> _varList) {
    return readVariables(_filename,_varList);
  }
  
  public boolean readVariables (String _filename, List<String> _varList) {
    if (model==null) return false;
    //LDLTorre for Moodle support
    //A�adida una nueva posibilidad si estamos en Moodle. Si _filename es null, cargamos una 
    //interfaz que muestra una lista de aquellos ficheros contenidos en Moodle y generados por 
    //ese applet y ese usuario.
    /*if (_filename==null) { // Choose a filename, preferably an ".xml" file
      JFileChooser chooser=OSPRuntime.createChooser("XML",new String[]{"xml"});
      chooser.setSelectedFile(new File(this.getModelClassname()+"_Variables.xml"));
      _filename = OSPRuntime.chooseFilename(chooser,popupTriggeredBy,false); // false = to read
      if (_filename==null) return false;
    }*/
    if (isMoodleConnected()){
      //When working in Moodle, it checks if filename is null. In that case, it connects to Moodle 
      //php and obtains the list of xml files, created with this EJS lab, as a string. 
      _filename = moodle.readXML(_filename);
      if (_filename.equals("url:")) return false;
    }
    else if (_filename==null) { // Choose a filename, preferably an ".xml" file
      JFileChooser chooser=OSPRuntime.createChooser("XML",new String[]{"xml"});
      chooser.setSelectedFile(new File(this.getModelClassname()+"_Variables.xml"));
      _filename = OSPRuntime.chooseFilename(chooser,popupTriggeredBy,false); // false = to read
      if (_filename==null) return false;
    }
    //LDLTorre for Moodle support
    boolean success = justReadVariables (_filename, _varList);
    if (success) {
      if (view!=null) {
        view.initialize();
        view.update();
      }
      updateAfterModelAction();
    }
    return success;
  }
  
  private boolean justReadVariables (String _filename, List<String> _varList) {
    //System.out.println ("Reading vars from file "+_filename);
    //if (_codebase==null) System.out.println ("codebase = null");
    //else System.out.println (", codebase = "+_codebase.toString());
    try {
      // Check if the file exists
      boolean exists=true;
      java.net.URLConnection urlConnection = null;
      Reader reader = null;
      if (_filename.startsWith("ejs:")) {
        exists = (memory.get(_filename)!=null);
      }
      else if (_filename.startsWith("url:")) {
        String urlStr = _filename.substring(4);
//        System.err.println ("Trying to read from Reso Load URL "+urlStr);
        try { urlConnection = new java.net.URL(urlStr).openConnection(); }
        catch (Exception _exc) {
          _exc.printStackTrace();
          exists = false; 
        } // Do complain
//        reader = ResourceLoader.openReader(urlStr);
//        exists = reader!=null;
      }
      else {
        //System.err.println ("Trying to read "+_filename);
        reader = ResourceLoader.openReader(_filename);
        exists = reader!=null;
      }
      if (!exists) {
        errorMessage ("File does not exist "+_filename);
        return false; // But do it silently
      }
      //errorMessage ("File does exist "+_filename);
      // Previous to changing the variables of the model
      //errorMessage ("File does exist "+_filename);
      // Previous to changing the variables of the model
      XMLControlElement control = null;     // Used to read XML
      java.io.ObjectInputStream din = null; // Used to read binary
      if (_filename.toLowerCase().endsWith(".xml")) {  // Read XML
        String xmlString = null;
        java.io.Reader in=null;
        if (urlConnection!=null) {
          in = new InputStreamReader((java.io.InputStream)urlConnection.getContent());
        }
        else { // Either memory or a file
          if (_filename.startsWith("ejs:")) in = new java.io.CharArrayReader( (char[])memory.get(_filename));
          else if (isMoodleConnected()) xmlString = moodle.readXML(_filename);
          else in = reader;
        }
        if (in!=null) {
          java.io.LineNumberReader l = new java.io.LineNumberReader(in);
          StringBuffer txt = new StringBuffer();
          String sl = l.readLine();
          while (sl != null) { txt.append(sl + "\n"); sl = l.readLine(); }
          in.close();
          xmlString = txt.toString();
        }

//        if (!xmlContainsData (xmlString)) return true; // Why not? I read it, but data was irrelevant.

        control = new XMLControlElement(this.getClass());
        if (!control.readXMLForClass(xmlString,this.getClass())) return true; // Why not? I read it, but data was irrelevant.
        //control.readXML(xmlString);
//        return true; // Why not? I read it, but data was irrelevant.
      }
      else { // Prepare to read binary
        java.io.InputStream in;
        if (urlConnection!=null) in = urlConnection.getInputStream();
        else if (_filename.startsWith("ejs:")) in = new java.io.ByteArrayInputStream((byte[]) memory.get(_filename));
        else in = ResourceLoader.openInputStream(_filename);
        if (in==null) return false;// FKH20060413 no data in state byte array
        din = new java.io.ObjectInputStream (new java.io.BufferedInputStream (in));
      }
      // Now modify the model variables
      java.lang.reflect.Field[] fields = model.getClass().getFields();
      if (_varList==null) { // read all variables in the order they appear
        for (int i=0; i<fields.length; i++) {
          Object objectToRead = fields[i].get(model);
//          System.err.print ("Reading variable "+fields[i].getName());
          if (objectToRead!=null && ! (objectToRead instanceof java.io.Serializable)) {
//            System.err.println (" Ignoring it! "+fields[i].getName());
            continue; // Ignore these
          }
          if (control!=null) { // read xml
            Object object = control.getObject(fields[i].getName());
            if (object != null) fields[i].set(model, object);
          }
          else if (din!=null) {
            Object objectRead = din.readObject();
//            System.err.println (" Value read = "+objectRead+ " (type = "+fields[i].getType()+")");
            fields[i].set(model, objectRead); // read binary
          }
        }
      } // End of reading all variables
      else { // read only variables in the list in the same order
        for (int j=0,n=_varList.size(); j<n; j++) {
          String varName = _varList.get(j).trim();
          for (int i=0; i<fields.length; i++) {
            Object objectToRead = fields[i].get(model);

            if (objectToRead!=null && ! (objectToRead instanceof java.io.Serializable))continue; // Ignore these            
            if (fields[i].getName().equals(varName)) { // Found
              if (control!=null) {
                Object object = control.getObject(varName);
                if (object!=null) fields[i].set(model,object);
              }
              else if (din!=null) fields[i].set(model,din.readObject());
              break;
            }
          }
        }
      }  // End of reading for each variable in the list
      // Finalize
      if (din!=null) din.close();
      //errorMessage ("File read OK "+_filename);
      return true;
    }
    catch (java.lang.Exception ioe) {
      errorMessage ("Error when trying to read "+_filename);
      ioe.printStackTrace(System.err);
      javax.swing.JOptionPane.showMessageDialog(getParentComponent(),ioe.getLocalizedMessage());
      return false;
    }
  }


  /**
   * Saves the state of the variables of the model specified in the given list,
   * either to a file on the disk or to memory. If the list is null, it saves
   * all the variables of the model.
   * <p>If the name of the file starts with the prefix "ejs:", then the
   * state of the model will be saved to memory, otherwise it will be
   * saved to disk. </p>
   * <p>If the name of the file ends with ".xml", then the
   * state of the model will be saved in XML form.</p>
   * <p> Security considerations apply when running the simulation as
   * an applet.</p>
   * <p>The state of the model is saved by writing to disk all its public
   * fields which implement the java.io.Serializable interface. This
   * includes primitives and arrays.
   * </p>
   * @param _filename the name of a file (either in disk or in memory). If null,
   * the user will be prompted to choose a file name.
   * @param _varList the list of variables to save. If null, all variables are saved
   * @return true if the file was correctly saved
   */
  public boolean saveVariables (String _filename, List<String> _varList) {
    if (model==null) return false;
    boolean saveAsXML = true;
    if (_filename==null) { // Choose a filename, preferably an ".xml" file
      //LDLTorre for Moodle support and consistency.
      //Introducido un if para hacer la verificaci�n de conexi�n a Moodle a fin de evitar en este 
      //caso la aparici�n del panel de navegaci�n por el disco duro. Tambi�n se introduce un nombre
      //por defecto para cuando no se est� conectado a Moodle.
      if (isMoodleConnected()) _filename = getModelClassname()+"_Variables.xml";
      else {
        JFileChooser chooser=OSPRuntime.createChooser("XML",new String[]{"xml"});
        chooser.setSelectedFile(new File(getModelClassname()+"_Variables.xml"));
        final MyXMLAccessory accesory = new MyXMLAccessory(chooser);
        chooser.setAccessory(accesory);
        chooser.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser theFileChooser = (JFileChooser) actionEvent.getSource();
            String command = actionEvent.getActionCommand();
            if (command.equals(JFileChooser.APPROVE_SELECTION)) {
              File selectedFile = theFileChooser.getSelectedFile();
              String selectedFilename = selectedFile.getName();
              boolean nameChanged=false;
              if (accesory.saveAsXML()) { // must end with ".xml"
                if (!selectedFilename.toLowerCase().endsWith(".xml")) { 
                  theFileChooser.setSelectedFile(new File(selectedFile.getParent(),selectedFilename + ".xml")); 
                  nameChanged = true; 
                }
              }
              else { // anything but ".xml"
                if (selectedFilename.toLowerCase().endsWith(".xml"))  { 
                  theFileChooser.setSelectedFile(new File(selectedFile.getParent(),selectedFilename + ".bin")); 
                  nameChanged = true; 
                }
              }
              if (nameChanged) {
                JOptionPane.showMessageDialog(popupTriggeredBy, ejsRes.getString("Simulation.XMLFileRenamed")+":\n  "+theFileChooser.getSelectedFile().getName());
              }
            }
          }
        });
        _filename = OSPRuntime.chooseFilename(chooser,popupTriggeredBy,true); // true = to save
        if (_filename==null) return false;
        saveAsXML = accesory.saveAsXML();
      }
    }
    else saveAsXML = _filename.toLowerCase().endsWith(".xml");
    try {
      XMLControlElement control = null;       // Used to save XML
      java.io.OutputStream out = null;        // Used to save binary
      java.io.ObjectOutputStream dout = null; // Used to save binary
      if (saveAsXML) control = new XMLControlElement(this.getClass()); // save as XML
      else { // Save in binary form
        // FKH 20060415 add javascriptcontrolMode()
        if (javascriptControlMode() || _filename.startsWith("ejs:") || isMoodleConnected ()) out = new ByteArrayOutputStream ();
        else out = new java.io.FileOutputStream (_filename);
        java.io.BufferedOutputStream bout = new java.io.BufferedOutputStream (out);
        dout = new java.io.ObjectOutputStream (bout);
      }
      java.lang.reflect.Field[] fields = model.getClass().getFields();
      if (_varList==null) { // save all variables in the order they appear
        for (int i=0; i<fields.length; i++) {
          Object objectToSave = fields[i].get(model);
          if (objectToSave!=null && !(objectToSave instanceof java.io.Serializable)) {
//            System.err.println (" Ignoring variable "+fields[i].getName());
            continue; // Ignore these ones
          }
          if (control!=null) control.setValue(fields[i].getName(), objectToSave);
          else if (dout!=null) {
//            System.err.println ("Saving variable "+fields[i].getName()+" to "+objectToSave+ " (type = "+fields[i].getType()+")");
            dout.writeObject(objectToSave);
          }
        }
      } // End of saving all variables
      else { // save only variables in the list in the same order
        for (int j=0,n=_varList.size(); j<n; j++) {
          String varName = _varList.get(j).trim();
          for (int i=0; i<fields.length; i++) {
            Object objectToSave = fields[i].get(model);
            if (objectToSave!=null && !(objectToSave instanceof java.io.Serializable)) continue; // Ignore these ones
            if (fields[i].getName().equals(varName)) {
              if (control!=null) control.setValue(fields[i].getName(), fields[i].get(model));
              else if (dout!=null) dout.writeObject(fields[i].get(model));
              break;
            }
          }
        }
      }  // End of saving for each variable in the list
      if (control!=null) {  // Write the XML
        java.io.Writer writer;
        //LDLTorre for Moodle support
        //Cambio en el orden de verificaci�n de las condiciones
        /*
         if (_filename.startsWith("ejs:")) writer = new java.io.CharArrayWriter ();
        else if (isMoodleConnected()) return moodle.saveXML(_filename, "XML file", control.toXML()) != null;
        else writer = new java.io.FileWriter (_filename);
        control.write(writer);
        if (_filename.startsWith("ejs:")) memory.put(_filename,((java.io.CharArrayWriter) writer).toCharArray());
        */
        if (isMoodleConnected()) return moodle.saveXML(_filename, "XML file", control.toXML()) != null;
        if (_filename.startsWith("ejs:")) writer = new java.io.CharArrayWriter ();
        else writer = new java.io.FileWriter (_filename);
        control.write(writer);
        if (_filename.startsWith("ejs:")) memory.put(_filename,((java.io.CharArrayWriter) writer).toCharArray());
        //LDLTorre for Moodle support
      }
      else {  // Save the binary
        if (dout!=null) dout.close();
        if (out!=null) {
          //LDLTorre for Moodle support
          /*
          if (_filename.startsWith("ejs:")) memory.put(_filename,((ByteArrayOutputStream) out).toByteArray());
          else if (isMoodleConnected ()) return moodle.saveBinary(_filename,"Binary data",( (ByteArrayOutputStream) out).toByteArray())!=null;
          else if(javascriptControlMode()) javascriptControledApplet.setStateByteArray(((java.io.ByteArrayOutputStream) out).toByteArray());
          */
          if (isMoodleConnected()) return moodle.saveBinary(_filename,"Binary data",( (ByteArrayOutputStream) out).toByteArray())!=null;
          if (_filename.startsWith("ejs:")) memory.put(_filename,((ByteArrayOutputStream) out).toByteArray());
          else if(javascriptControlMode()) javascriptControledApplet.setStateByteArray(((java.io.ByteArrayOutputStream) out).toByteArray());
          //LDLTorre for Moodle support
        }
      }
      return true;
    }
    catch (java.lang.Exception ioe) {
      errorMessage ("Error when trying to save "+_filename);
      ioe.printStackTrace(System.err);
      return false;
    }
  }
  
  
  private void addFileToJar(JarOutputStream _jarOut, String _name, File _file) throws Exception{
    byte[] buf = new byte[1024];
    InputStream in = new FileInputStream(_file);
    _jarOut.putNextEntry(new JarEntry(_name));
    int len;
    while ((len = in.read(buf)) > 0) { _jarOut.write(buf, 0, len); }
    in.close();
    _jarOut.closeEntry();  
  }
  
  static public boolean isDisplayable(String filename) {
    if (filename.toLowerCase().startsWith("http://")) return false; // ignore web files
    return true;
  }
  
  /**
   * Saves a default file with default state into the simulation JAR file
   * @return
   */
  public boolean saveDefaultStateToJar (File _jarFile, String _filenames) {
    if (OSPRuntime.isLauncherMode()) {
      OSPLog.warning("Simulation cannot save state to JAR when in Launcher mode. Ignored!");
      return false;
    }
    try {
      File tempFile = File.createTempFile(DEFAULT_STATE_FILENAME, null);
      tempFile.delete();
      boolean savedOk = saveVariables (tempFile.getAbsolutePath(), (List<String>) null); 
      if (!savedOk) {
        JOptionPane.showMessageDialog(getParentComponent(),"Cannot save data file","Could not create temp file",JOptionPane.ERROR_MESSAGE);
        return false;
      }
//      System.out.println ("Saved OK "+tempFile.getAbsolutePath());

      java.util.Set<File> extraFilesSet=new java.util.HashSet<File>();
      if (_filenames!=null) {
        StringTokenizer tkn = new StringTokenizer(_filenames,";,");
        while (tkn.hasMoreTokens()) {
          String extraFileName = tkn.nextToken();
          if (!isDisplayable(extraFileName)) continue;
//          System.err.println ("Trying "+extraFileName);
          Resource res = ResourceLoader.getResource(extraFileName);          
          File extraFile = null;
          if (res!=null) extraFile = res.getFile();
//          System.err.println ("res = "+res+" , FIle = "+extraFile);
          // See if the file exists
          if (extraFile==null || !extraFile.exists()) {
            if (res==null || res.openReader()==null) JOptionPane.showMessageDialog(getParentComponent(),ejsRes.getString("Simulation.CantFindExtraFile")+" "+extraFileName,ejsRes.getString("Simulation.IgnoringExtraFile"),JOptionPane.WARNING_MESSAGE);
            else JOptionPane.showMessageDialog(getParentComponent(),ejsRes.getString("Simulation.ExtraFileAlreadyIn")+" "+extraFileName+"\n"+ejsRes.getString("Simulation.RenameDocument"),ejsRes.getString("Simulation.IgnoringExtraFile"),JOptionPane.WARNING_MESSAGE);
          }
          else extraFilesSet.add(extraFile);
        }        
      }
      
      JPanel accesoryPanel = new JPanel(new BorderLayout());
      JCheckBox htmlCheckBox = new JCheckBox(Memory.getResource("CreateHTMLPage"),false);
      accesoryPanel.add(htmlCheckBox,BorderLayout.NORTH);

      String proposedName = FileUtils.getPlainName(_jarFile) + "_StateAdded.jar";
      File proposedFile = new File(proposedName);
      int counter = 2;
      while (proposedFile.exists()) {
        proposedName = FileUtils.getPlainName(_jarFile) + "_StateAdded_"+counter+".jar";
        proposedFile = new File(proposedName);
        counter++;
      }
      File newJarFile = new File (_jarFile.getParentFile(),proposedName);
      JFileChooser chooser=OSPRuntime.createChooser("JAR",new String[]{"jar"});
      chooser.setSelectedFile(newJarFile);
      chooser.setAccessory(accesoryPanel);
      String filename = OSPRuntime.chooseFilename(chooser,getParentComponent(),true); // true = to save      
      if (filename==null) return false;
      if (filename.lastIndexOf('.')<0) filename += ".jar";

      newJarFile = new File(filename);

      JarInputStream  jarIn = new JarInputStream(new FileInputStream(_jarFile));
      JarFile tmpJarFile = new JarFile(_jarFile);
      JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(newJarFile),tmpJarFile.getManifest());

      java.util.Set<File> extraFilesRemoveSet=new java.util.HashSet<File>();
      // Copy the old jar
      JarEntry entry = jarIn.getNextJarEntry();
      while (entry != null) {
        String name = entry.getName();
        if (name.equals(DEFAULT_STATE_FILENAME)) {
          OSPLog.fine("Saving state to JAR : removing previous state file "+name);
          entry = jarIn.getNextJarEntry();
          continue; // Do not copy this one
        }
        String lowercaseName = name.toLowerCase();
        if (lowercaseName.startsWith("meta-inf/") && (lowercaseName.endsWith(".sf") || lowercaseName.endsWith(".rsa") || lowercaseName.endsWith(".dsa")) ) {
//        if ( lowercaseName.endsWith(".sf") || lowercaseName.endsWith(".rsa") || lowercaseName.endsWith(".dsa")) {
          OSPLog.fine("Saving state to JAR : Ignoring signature file : "+name);
          entry = jarIn.getNextJarEntry();
          continue; // Do not copy this one
        }
//        System.out.println ("Copying file : "+name);
        boolean overwrite = false;
        for (File extraFile : extraFilesSet) {
          if (extraFile.getName().equals(name)) {
            int option = JOptionPane.showConfirmDialog(getParentComponent(),
                "File already exists in JAR:"+name+"\nDo you want to overwrite it?", "Conflict with existing file", JOptionPane.YES_NO_OPTION);
            if (option==JOptionPane.NO_OPTION) extraFilesRemoveSet.add(extraFile);
            else overwrite = true;
            break;
          }
        }
        if (overwrite) {
          entry = jarIn.getNextJarEntry();
          continue;
        }
        byte[] buf = new byte[1024];
        jarOut.putNextEntry(new JarEntry(name));
        int len;
        while ((len = jarIn.read(buf)) > 0) { jarOut.write(buf, 0, len); }
        entry = jarIn.getNextJarEntry();
      }
      jarIn.close();
      
      // Exclude those already in JAR
      extraFilesSet.removeAll(extraFilesRemoveSet);

//      XMLControlElement control = null;       // Used to save XML
//      java.io.OutputStream out = null;        // Used to save binary
//      java.io.ObjectOutputStream dout = null; // Used to save binary
//      control = new XMLControlElement(this.getClass());
      
      // Add the state
      addFileToJar(jarOut, DEFAULT_STATE_FILENAME, tempFile);
      tempFile.delete();
      // Add the extra files
      for (File extraFile : extraFilesSet) addFileToJar(jarOut, extraFile.getName(), extraFile);

//      jarOut.putNextEntry(new JarEntry(DEFAULT_STATE_FILENAME));
//      java.io.BufferedOutputStream bout = new java.io.BufferedOutputStream (jarOut);
//      java.io.ObjectOutputStream dout = new java.io.ObjectOutputStream (bout);
//      java.lang.reflect.Field[] fields = model.getClass().getFields();
//      for (int i=0; i<fields.length; i++) {
//        if (!(fields[i].get(model) instanceof java.io.Serializable)) continue; // Ignore these ones
//        dout.writeObject(fields[i].get(model));
//      }
//      jarOut.closeEntry();

      jarOut.close();
      tmpJarFile.close();
      if (htmlCheckBox.isSelected()) createHTMLpage(newJarFile.getName());
      return true;
    }
    catch (java.lang.Exception ioe) {
      errorMessage ("Error when trying to save state to "+_jarFile.getName());
      ioe.printStackTrace(System.err);
      return false;
    }
  }


  static private class MyXMLAccessory extends JPanel { //implements java.beans.PropertyChangeListener {
    private JRadioButton xmlButton;
    
    public MyXMLAccessory(JFileChooser _chooser) {
      xmlButton = new JRadioButton("XML",true);
//      xmlButton.addActionListener (new ActionListener() { // make it non-unselectable
//        public void actionPerformed (ActionEvent _e) {
//          if (xmlButton.isSelected()) setXMLExtension(true);
//        }
//      });
      JRadioButton binaryButton = new JRadioButton("BIN",false);
//      binaryButton.addActionListener (new ActionListener() { // Make it non-unselectable
//        public void actionPerformed (ActionEvent _e) {
//          if (binaryButton.isSelected()) setXMLExtension(false);
//        }
//      });
      
      ButtonGroup group = new ButtonGroup();
      group.add(xmlButton);
      group.add(binaryButton);

      setLayout(new javax.swing.BoxLayout(this,javax.swing.BoxLayout.Y_AXIS));
      add(new JLabel(ejsRes.getString("Simulation.StateFormat")));
      add(xmlButton);
      add(binaryButton);
      
//      fileChooser.addPropertyChangeListener(this); // Listen for changes to the selected file
    }

//    /**
//     * Force/prevent the XML extension
//     * @param _xml true if the extension must be ".xml" false if the extension cannot be ".xml"
//     */
//    private void setXMLExtension(boolean _xml) {
//      System.err.println ("Checking XML extension "+_xml);
//      File currentFile = fileChooser.getSelectedFile();
//      String filename = currentFile.getName();
//      if (_xml) { // must end with ".xml"
//        if (!filename.toLowerCase().endsWith(".xml")) fileChooser.setSelectedFile(new File(currentFile.getParentFile(),filename+".xml"));
//      }
//      else { // anything but ".xml"
//        if (filename.toLowerCase().endsWith(".xml"))  fileChooser.setSelectedFile(new File(currentFile.getParentFile(),filename.substring(0,filename.length()-4)));
//      }
//    }
    
    /**
     * Whether the user wants an XML file
     * @return
     */
    public boolean saveAsXML() { return xmlButton.isSelected(); }
    
    // This listener listens for changes to the selected file
//    public void propertyChange(java.beans.PropertyChangeEvent evt) {
//      System.err.println ("Property changed ="+evt.getPropertyName());
//        if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
////            JFileChooser chooser = (JFileChooser)evt.getSource();
//            File newFile = (File)evt.getNewValue();
//            String filename = newFile.getName(); 
//            System.err.println ("Filename is now "+filename);
//            if (filename.toLowerCase().endsWith(".xml")) xmlButton.setSelected(true);
//            else binaryButton.setSelected(true);
////            repaint();
//        }
//    }

}

  /**
   * Returns the state of the variables of the model specified in the given list a a text with XML format.
   * @param _varList a String with the list of variables separated by commas ',' or semicolons ';'
   * @return String the text in XML format
   * @see #getVariablesXML(ArrayList)
   */
  public String getVariablesXML (String _varList) {
    return getVariablesXML (toArrayList(_varList));
  }
  
  /**
   * Returns the state of the variables of the model specified in the given list a a text with XML format.
   * If the list is null, it saves all the variables of the model.
   * <p>The state of the model is saved by writing to disk all its public
   * fields which implement the java.io.Serializable interface. This
   * includes primitives and arrays.
   * </p>
   * @param _varList the list of variables to save. If null, all variables are saved
   * @return String the text in XML format
   */
  public String getVariablesXML (List<String> _varList) {
    if (model==null) return null;
    try {
      XMLControlElement control = new XMLControlElement(this.getClass());
      java.lang.reflect.Field[] fields = model.getClass().getFields();
      if (_varList==null) { // save all variables in the order they appear
        for (int i=0; i<fields.length; i++) {
          Object objectToRead = fields[i].get(model);
          if (objectToRead!=null && !(objectToRead instanceof java.io.Serializable)) continue; // Ignore these ones
          control.setValue(fields[i].getName(), fields[i].get(model));
        }
      } // End of saving all variables
      else { // save only variables in the list in the same order
        for (int j=0,n=_varList.size(); j<n; j++) {
          String varName = _varList.get(j).trim();
          for (int i=0; i<fields.length; i++) {
            Object objectToRead = fields[i].get(model);
            if (objectToRead!=null && !(objectToRead  instanceof java.io.Serializable)) continue; // Ignore these ones
            if (fields[i].getName().equals(varName)) {
              control.setValue(fields[i].getName(), fields[i].get(model));
              break;
            }
          }
        }
      }  // End of saving for each variable in the list
      java.io.CharArrayWriter writer = new java.io.CharArrayWriter ();
      control.write(writer);
      return writer.toString();
    }
    catch (java.lang.Exception ioe) {
      errorMessage ("Error when trying to get XML of variables!");
      ioe.printStackTrace(System.err);
      return null;
    }
  }

// --------------------------------------------------------
// Saving or reading text
// --------------------------------------------------------

  /**
   * Saves a String with user-defined info either to a file on the disk or to memory.
   * <p>If the name of the file starts with the prefix "ejs:", then the
   * state of the model will be saved to memory, otherwise it will be
   * dumped to disk.
   * </p>
   * <p>
   * Security considerations apply when running the simulation as
   * an applet.
   * <p>
   * @param _filename the name of a file (either in disk or in memory)
   * @param _text the user-defined info string
   * @param _type the type of file string
   * @return true if the file was correctly saved
   */
  public boolean saveText (String _filename, String _type, String _text) {
    try {
      java.io.Writer out;
      if (_filename.startsWith("ejs:")) out = new java.io.CharArrayWriter ();
      // LDLTorre for Moodle support
      else if (isMoodleConnected()) {
        if (_filename.toLowerCase().endsWith(".xml")) return moodle.saveXML(_filename, "XML file", _text) != null;
        return moodle.saveText(_filename, _type, _text) != null;
      }
      // LDLTorre for Moodle support
      else out = new java.io.FileWriter (_filename);
      java.io.BufferedWriter bout = new java.io.BufferedWriter (out);
      bout.write(_text);
      bout.close();
      if (_filename.startsWith("ejs:")) memory.put(_filename,((java.io.CharArrayWriter) out).toCharArray());
      return true;
    }
    catch (java.lang.Exception ioe) {
      System.err.println("Error when trying to save"+_filename);
      ioe.printStackTrace(System.err);
      return false;
    }
  }

  /**
   * Saves user generated info to a file. Equivalent to
   * saveText (_filename,_info.toString());
   * @param _filename String
   * @param _info StringBuffer
   * @return boolean
   */
  public boolean saveText (String _filename, StringBuffer _info) {
    return saveText (_filename,_info.toString());
  }

  /**
   * Saves user generated info to a file. Equivalent to
   * saveText (_filename,_text,"Text file");
   * @param _filename String
   * @param _text the user-defined info string
   * @return boolean
   */
  public boolean saveText (String _filename, String _text) {
    return saveText (_filename,"Text file",_text);
  }


  /**
   * <p>
   * Reads a String with user-defined info either from a file on the disk, from
   * memory or from a url location.
   * </p>
   * @param _filename the name of a file (either in disk, in memory or a url)
   * @return String The string read, or null if failed
   */
  public String readText (String _filename) {
    return readText (_filename, "text", (java.net.URL) null);
  }

  /**
   * <p>
   * Reads a String with user-defined info either from a file on the disk, from
   * memory or from a url location.
   * </p>
   * @param _filename the name of a file (either in disk, in memory or a url)
   * @param _type the type of files you want to search for when embedded in Moodle
   * @return String The string read, or null if failed
   */
  public String readText (String _filename, String _type) {
    return readText (_filename, _type, (java.net.URL) null);
  }

  /**
   * <p>
   * Reads a String with user-defined info either from a file on the disk, from
   * memory or from a url location.
   * </p>
   * @param _filename the name of a file (either in disk, in memory or a url)
   * @param _codebase the codebase when the simulation runs as an applet
   * @return String The string read, or null if failed
   */
  public String readText (String _filename, java.net.URL _codebase) {
    return readText (_filename, "text", _codebase);
  }

  /**
   * <p>
   * Reads a String with user-defined info either from a file on the disk, from
   * memory or from a url location.
   * </p>
   * <p>
   * If the name of the file starts with the prefix "ejs:", then the
   * info string will be read from a memory file.
   * </p>
   * <p>
   * If the name of the file starts with "url:" it will be considered
   * a url location and the method will attempt to read the file from this
   * url (either locally or through the network).
   * </p>
   * <p>
   * If the name of the file does not start with any of those prefixes,
   * then it will be considered to be a file.
   * Security considerations apply when running the simulation as
   * an applet.
   * </p>
   * <p>
   * @param _filename the name of a file (either in disk, in memory or a url)
   * @param _type the type of files you want to search for when embedded in Moodle
   * @param _codebase the codebase when the simulation runs as an applet
   * @return String The string read, or null if failed
   */
  public String readText (String _filename, String _type, java.net.URL _codebase) {
    // LDLTorre for Moodle support
    if (isMoodleConnected () || _filename.startsWith("url:")) {
      String url = "";
      if (isMoodleConnected ()) {
        if (_filename.toLowerCase().endsWith(".xml")) url = moodle.readXML(_filename);
        else url = moodle.readText(_filename, _type);
        if (url.equals("url:")) return null;
      } else if (_filename.startsWith("url:")) {
        url = _filename.substring(4);
        if (_codebase==null || url.startsWith("http:")); // Do nothing
        else url = _codebase+url;
      }
      try {
        java.net.URL urlConn = new java.net.URL(url);
        InputStream is = (InputStream) urlConn.getContent();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        StringBuffer sb = new StringBuffer();
        while((line = br.readLine()) != null){
          sb.append(line+"\n");
        }
        return sb.toString();
      }
      catch (java.lang.Exception ioe) {
        System.err.println ("Error when trying to read "+_filename);
        ioe.printStackTrace(System.err);
        return null;
      }
    }
    // LDLTorre for Moodle support
    try { // Either memory or a file
      java.io.Reader in;
      if (_filename.startsWith("ejs:")) in = new java.io.CharArrayReader((char[]) memory.get(_filename));
      else in = new java.io.FileReader (_filename);
      java.io.LineNumberReader l = new java.io.LineNumberReader(in);
      StringBuffer txt = new StringBuffer();
      String sl = l.readLine();
      while (sl != null) {
        txt.append(sl+"\n");
        sl = l.readLine();
      }
      in.close();
      return txt.toString();
    }
    catch (java.lang.Exception ioe) {
      System.err.println ("Error when trying to read "+_filename);
      ioe.printStackTrace(System.err);
      return null;
    }
  }
  
  //CJB for collaborative
  protected void extraAction2(){}
  protected void extraAction3(){}
  //CJB for collaborative

} // End of class



/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Revised February 2006 F. Esquembre
 */

package org.colos.ejs.osejs;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.Charset;

import javax.swing.*;

import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.edition.*;
//import org.colos.ejs.osejs.edition.experiments.ExperimentEditor;
import org.colos.ejs.osejs.edition.html.DescriptionEditor;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.edition.translation.TranslationEditor;
import org.colos.ejs._EjsSConstants;
import org.colos.ejs.library.Animation;
import org.colos.ejs.library.Memory;
import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.library.control.swing.ControlWindow;
import org.colos.ejs.library.utils.HardcopyWriter;
import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.library.utils.PasswordDialog;
import org.colos.ejs.library.utils.TemporaryFilesManager;
import org.colos.ejss.xml.SimulationXML;
import org.colos.ejss.xml.SimulationXML.INFORMATION;
import org.colos.ejss.xml.XMLTransformerJava;
import org.opensourcephysics.controls.Cryptic;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.*;
import org.opensourcephysics.tools.minijar.PathAndFile;


public class Osejs {
  static private final boolean USE_AWT_FILEDIALOG = true; 
  static private boolean isMacOSX;

  static private final ResourceUtil sysRes = new ResourceUtil("SystemResources");
  static private final String[] mainOptions  = ResourceUtil.tokenizeString(sysRes.getString("Osejs.MainToolBar"));
  static public final Icon RUNNING_ICON     = ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Running"));
  static public final Icon NOT_RUNNING_ICON = ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Run"));
  static private ResourceUtil res = null; // to be initialized after the Locale is set
  static private Font defaultFont = null; // to be initialized after the Locale is set
  static private Font titleFont = null;   // to be initialized after the Locale is set
  static public final int DELAY_FOR_LABEL_BORDERS=1000;
  static public enum PROGRAMMING_LANGUAGE { JAVA, JAVASCRIPT, JAVA_PLUS_HTML };
//  static public final String sEJSS_ZIP_EXTENSION = "ejz";

  private boolean isPureMac;
  private PROGRAMMING_LANGUAGE mProgrammingLanguage = PROGRAMMING_LANGUAGE.JAVA;
  private Editor[] editors=null;
  private SimInfoEditor simInfoEditor=null;
  private TranslationEditor translationEditor=null;
  private org.colos.ejs.osejs.edition.html.DescriptionEditor descriptionEditor=null;
  private ModelEditor modelEditor = null;
  private ViewEditor viewEditor = null;
  //  private ExperimentEditor experimentEditor=null;
  private HtmlViewEditor htmlViewEditor=null;
  private OutputArea outputArea=null;
  private EjsOptions myEjsOptions=null;
  private ProcessListDialog processDialog = null;
  private Object lastProcess = null;
  private SearchDialog searchDialog=null;
  private JFileChooser fileDialog = null;
  private javax.swing.filechooser.FileFilter fileFilter=null;
  //  private JFileChooser importDirectoryChooser = null; // Used to choose import directories

  private boolean externalAppsOn=false;
  private boolean verbose=false;
  private boolean firstTimeNetworkError=true; // Used when trying to access the help site on the Internet
  private boolean justToCompile=false; // Whether EJS was started just to compile a simulation
  private boolean isReading = false;
  private String lookAndFeel=UIManager.getSystemLookAndFeelClassName(); //getCrossPlatformLookAndFeelClassName();
  private boolean closedNormally=false; // whether EJS was closed normally

  private File binDirectory=null;  // The EJS bin directory
  private File docDirectory=null;  // The documentation directory
  private File configDirectory=null; // The user/config directory for the configuration files
  private File exportDirectory=null; // The user/export directory for the exported (JAR and HTML) files
  private File outputDirectory=null; // The user/output directory for the generated files
  private File sourceDirectory=null; // The user/source directory for the XML files
  private File tmpDirectory=TemporaryFilesManager.createTemporaryDirectory("_zip_", new File(Simulation.getTemporaryDir())); // A temporary directory to expand ZIP files

  private Font currentFont=null;
  private CardLayout cardLayout=null;
  private JFrame mainFrame=null;
  private JPanel topPanel=null, mainPanel=null;
  private JSplitPane splitPanel=null;
  private JLabel runButton=null,simInfoButton=null,netButton=null;
  private JRadioButton[] mainButtons=null;
  //  private JRadioButton experimentButton=null;
  private JPopupMenu packagePopup=null, runPopup=null, infoPopup=null, searchPopup=null; //, savePopup=null;
  private JMenu diagnosticsMenu=null;
  private Set<Window> windowsList = new HashSet<Window>(); // The set of windows that will be minimized if EJS is minimized
  private Set<Window> dependentWindowsList = new HashSet<Window>(); // The set of other windows that must be minimized if EJS is minimized
  private JMenu htmlViewsMenu=null;

  // Variables that change according to user actions
  private File unnamedXMLFile = null; // Default file when nothing has been written
  private File currentXMLFile = null; // The current XML file that was loaded 
  private File currentMetadataFile=null; // The current meta data file that was generated
  private String addedResourcePath=null; // Used to add paths to ResourceLoader
  //private String executionClassname=null; // The complete class to be executed
  //private String executionPath=null; // The execution path required by the current class to run
  private java.util.List<String> previousOpenedFilePaths = new ArrayList<String>(); // The list of file paths previously opened

  private String openString = null;

  static {
    try {
      isMacOSX = false;
      if (System.getProperty("os.name", "").toLowerCase().startsWith("mac")) {
        System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", sysRes.getString("Osejs.Name"));
        isMacOSX = true;
      }
    } catch(Exception exc) { } // Do nothing
  }

  public static ResourceUtil getResources() { return res; }
  public static ResourceUtil getSystemResources() { return sysRes; }

  // --- Setter and getter methods

  public boolean isOfferingExternalPages()  { return this.externalAppsOn; }
  public boolean isJustCompiling()          { return this.justToCompile; }
  public boolean isVerbose()                { return this.verbose; }
  public Object  getLastProcess()           { return this.lastProcess; }
  public String  getLookAndFeel()           { return this.lookAndFeel; }
  public boolean isNimbusLookAndFeel()      { return this.lookAndFeel.toLowerCase().indexOf("nimbus")>=0; }
//  public boolean useNativeFileChooser()     { return lookAndFeel.equals(UIManager.getSystemLookAndFeelClassName()) && USE_AWT_FILEDIALOG; }
  public boolean useNativeFileChooser()     { return isPureMac && USE_AWT_FILEDIALOG; }
  //  public boolean isSystemLookAndFeel()      { return this.lookAndFeel.toLowerCase().indexOf("system")>=0; }
  //  public boolean isSystemLookAndFeel()      { return lookAndFeel.equals(UIManager.getSystemLookAndFeelClassName()); }

  public String  getCurrentXMLFilename()    { 
    if (currentXMLFile!=null) return org.colos.ejs.library.utils.FileUtils.getPlainName(currentXMLFile);
    return "Unnamed";
  }

  public File    getCurrentXMLFile()        { return this.currentXMLFile; }
  public File    getUnnamedXMLFile()        { return this.unnamedXMLFile; }
  public Font    getCurrentFont ()          { return this.currentFont; }
  public File    getBinDirectory()          { return this.binDirectory; }
  public File    getDocDirectory()          { return this.docDirectory; }
  public File    getExportDirectory()       { return this.exportDirectory; }
  public File    getConfigDirectory()       { return this.configDirectory; }
  public File    getOutputDirectory()       { return this.outputDirectory; }
  public File    getSourceDirectory()       { return this.sourceDirectory; }
  public File    getTemporaryDirectory()    { return this.tmpDirectory; }

  public JFrame    getMainFrame()           { return this.mainFrame; }
  public JPanel    getMainPanel()           { return this.mainPanel; }

  public org.colos.ejs.osejs.edition.html.DescriptionEditor getDescriptionEditor() { return this.descriptionEditor; }
  public ModelEditor       getModelEditor()       { return this.modelEditor; }
  public ViewEditor        getViewEditor()        { return this.viewEditor; }
  public HtmlViewEditor    getHtmlViewEditor()    { return this.htmlViewEditor; }
  //  public ExperimentEditor  getExperimentEditor()  { return this.experimentEditor; }
  public SimInfoEditor     getSimInfoEditor()     { return this.simInfoEditor; }
  public OutputArea        getOutputArea()        { return outputArea; }
  public EjsOptions        getOptions()           { return myEjsOptions; }
  public TranslationEditor getTranslationEditor() { return translationEditor; }

  public PROGRAMMING_LANGUAGE getProgrammingLanguage() { return mProgrammingLanguage; }
  public boolean supportsJava() { return mProgrammingLanguage==PROGRAMMING_LANGUAGE.JAVA || mProgrammingLanguage==PROGRAMMING_LANGUAGE.JAVA_PLUS_HTML; }
  public boolean supportsHtml() { return mProgrammingLanguage==PROGRAMMING_LANGUAGE.JAVASCRIPT || mProgrammingLanguage==PROGRAMMING_LANGUAGE.JAVA_PLUS_HTML; }
  public boolean supportsJavascript() { return mProgrammingLanguage==PROGRAMMING_LANGUAGE.JAVASCRIPT; }

  public Object getViewElement(String _elementName) {
    ControlElement element = viewEditor.getTree().getControl().getElement(_elementName);
    if (element!=null) return element.getObject();
    return null;
  }

  //  static {
  //    try {
  //      if (System.getProperty("os.name", "").toLowerCase().startsWith("mac")) {
  //        System.setProperty("apple.laf.useScreenMenuBar", "true");
  //        System.setProperty("com.apple.mrj.application.apple.menu.about.name", sysRes.getString("Osejs.Name"));
  //      }
  //    } catch(Exception exc) {} // Do nothing
  //  }

  /**
   * Returns the open resource string. I did that because sometimes EJS would hang when opening a file dialog
   * and we suspected that calling so many times the resource loader might be to blame. 080912
   * @return
   */
  public String getOpenString() {
    if (openString==null) openString = res.getString("Osejs.File.Open");
    return openString;
  }

  /**
   * The current directory (parent of the current XML file)
   */
  public File getCurrentDirectory() {
    if (currentXMLFile!=null) return currentXMLFile.getParentFile();
    return sourceDirectory;
  }

  /**
   * The last class (i.e. ".class") file that has been generated 
   */
  public File getCurrentMetadataFile () { return currentMetadataFile; }

  /**
   * The list of recently opened files
   * @return
   */
  public java.util.List<String> getOpenedFilePathList() { return previousOpenedFilePaths; }

  /**
   * Set the parameters needed for a later run of the current simulation 
   *
  void setExecutionParameters (String classname, String path) {
    executionClassname = classname;
    //executionPath = path; 
  }

  /**
   * The qualified name of the class to run
   *
  public String getExecutionClassname () { return executionClassname; }
   *

  /*
   * The class path required to run the class
   *
  public String getExecutionPath () { return executionPath; }
   */

  public JFileChooser getFileDialog(File _firstDirectory) {
    return getFileDialog(_firstDirectory, true); 
  }

  // The file dialog to read and save files
  public JFileChooser getFileDialog(File _firstDirectory, boolean forFiles) {
    if (fileDialog==null) {
      fileDialog = OSPRuntime.createChooser(res.getString("Osejs.File.Description"), sysRes.getString("Osejs.File.Extension").split(","), getSourceDirectory().getParentFile());
      if (forFiles) fileDialog.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
      else fileDialog.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
      fileDialog.setMultiSelectionEnabled(false);
      if (_firstDirectory!=null) fileDialog.setCurrentDirectory(_firstDirectory);
      else fileDialog.setCurrentDirectory(sourceDirectory);
      fileFilter = fileDialog.getFileFilter(); 
    }
    else if (_firstDirectory!=null) {
      fileDialog.setCurrentDirectory(_firstDirectory);
    }
    else {
      File currentDir = fileDialog.getCurrentDirectory();
      if (!FileUtils.isRelative(currentDir, sourceDirectory)) {
        fileDialog.setCurrentDirectory(sourceDirectory);
      }
    }
    if (forFiles) fileDialog.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
    else fileDialog.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(fileDialog);
    fileDialog.setFileFilter(fileFilter);
    fileDialog.rescanCurrentDirectory();
    FontSizer.setFonts(fileDialog, FontSizer.getLevel());
    return fileDialog;
  }

  // -- The dialog that shows the processes currently running
  public ProcessListDialog getProcessDialog() {
    if (processDialog==null) processDialog = new ProcessListDialog(this);
    return processDialog;
  }

  /**
   * Adds a dependent window (such as a property edition dialog) that will be minimized when EJS's window is minimized
   * @param window
   */
  public void addDependentWindows (Window window) { 
    dependentWindowsList.add(window); 
  }

  /**
   * Removes a dependent window
   * @param window
   */
  public void removeDependentWindows (Window window) { 
    dependentWindowsList.remove(window); 
  }

  // ----------- main method  ------------

  static public void main (java.lang.String[] args) {
    System.setProperty("_ejs_running_", "true");
    // A very early scan to set the local language
    for (int i=0; i<args.length; i++) {
      if (args[i].equals("-locale")) {
        String language = args[++i], country = args[++i];
        Locale locale;
        if (country.equals("XX")) locale = new Locale(language);
        else locale = new Locale(language,country);
        //        System.out.println("Locale language= "+language+", country = "+country);
        //        System.out.println("Locale is "+locale.getDisplayName());
        ResourceUtil.setLocale(locale);
        Locale.setDefault(locale);
        JComponent.setDefaultLocale(locale);
      }
      else if (args[i].equals("-zoom_level")) {
        try {
          int level = Integer.parseInt(args[++i]);
          FontSizer.setLevel(level);
        }
        catch (Exception exc) { exc.printStackTrace(); }  
      }
    }

    // We can now initialize the static, but language-dependent objects
    res = new ResourceUtil("Resources");
    defaultFont = InterfaceUtils.font(null,res.getString("Osejs.DefaultFont"));
    titleFont = InterfaceUtils.font(null,res.getString("Osejs.TitleFont"));
    UIManager.put("FileChooser.homeFolderToolTipText", res.getString("Ejs.WorkspaceDirectory"));

    // Create EJS
    final Osejs ejs = new Osejs();

    // Scan the options
    String outputPath = OsejsCommon.OUTPUT_DIR_PATH;
    boolean progressFlag=true, compressFile=false, zipFile=false, decorateWindows=false;
    String exportPrefix="ejs_"; // The prefix for exported files for a -jar command
    String zipPrefix="ejs_"; // The prefix for ZIP exported files for a -zip command
    int screen=0; // The screen in which EJS should appear

    for (int i=0; i<args.length; i++) {
      //System.out.println ("Arg = "+args[i]);
      if      (args[i].equals("-compile"))      progressFlag = false;
      else if (args[i].equals("-maximumfps"))   Animation.MAXIMUM_FPS=Integer.parseInt(args[++i]) ;//fkh20050214
      else if (args[i].equals("-externalApps")) ejs.externalAppsOn = true;
      else if (args[i].equals("-outputDir"))    { outputPath = args[++i];  System.out.println ("Outputdir is now "+outputPath); }
      else if (args[i].equals("-lookAndFeel"))  ejs.lookAndFeel = args[++i];
      else if (args[i].equals("-decorateWindows"))  decorateWindows = true;
      else if (args[i].equals("-verbose"))      ejs.verbose = true;
      else if (args[i].equals("-jar"))          compressFile = true;
      else if (args[i].equals("-prefix"))       exportPrefix = args[++i]; //System.out.println ("Export prefix is now "+exportPrefix); }
      else if (args[i].equals("-zip"))          zipFile = true;
      else if (args[i].equals("-zip_prefix"))   zipPrefix = args[++i]; //System.out.println ("Export prefix is now "+exportPrefix); }
      else if (args[i].equals("-screen"))       screen = Integer.parseInt(args[++i]);
      else if (args[i].equals("-programming_language"))  {
        String progLan = args[++i].toUpperCase();
        if      (progLan.equals(OsejsCommon.PROGRAMMING_JAVASCRIPT))     ejs.mProgrammingLanguage = PROGRAMMING_LANGUAGE.JAVASCRIPT;
        else if (progLan.equals(OsejsCommon.PROGRAMMING_JAVA_PLUS_HTML)) ejs.mProgrammingLanguage = PROGRAMMING_LANGUAGE.JAVA_PLUS_HTML;
        else ejs.mProgrammingLanguage = PROGRAMMING_LANGUAGE.JAVA;
      }
    }

    if (!ejs.supportsJava()) {
      ControlWindow.setKeepHidden(true);
      OSPRuntime.disableAllDrawing = true;
    }

    //    System.out.println("Programming language = "+ejs.mProgrammingLanguage);

    // Set the default look and feel
    org.opensourcephysics.display.OSPRuntime.setLookAndFeel(decorateWindows, ejs.lookAndFeel);
    ejs.isPureMac = isMacOSX && ejs.lookAndFeel.equals(UIManager.getSystemLookAndFeelClassName());

    // Initialize EJS
    ejs.initializeDirectories(outputPath);
    Generate.copyEJSLibrary(ejs); // put EJS library in place 
    JFrame frame = ejs.mainFrame = new JFrame(sysRes.getString("Osejs.Title"));
    org.colos.ejs.library.control.value.BooleanValue mustAskAuthorInfo = new org.colos.ejs.library.control.value.BooleanValue(false);
    final ProgressDialog pD = ejs.initializeInterface(progressFlag,frame,screen,mustAskAuthorInfo);

    // Put EJS' interface inside a JFrame. 
    // This code is here -and not in initializeInterface()- for historical reasons (when EJS could be run as an applet)
    //JFrame frame = ejs.mainFrame = new JFrame(sysRes.getString("Osejs.Title"));
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.setIconImage(ResourceLoader.getImage(sysRes.getString("Osejs.Icon.EjsIcon")));
    frame.addComponentListener(new ComponentAdapter() {
      public void componentMoved (ComponentEvent e) {
        EjsControl.setDefaultScreen(OsejsCommon.getScreenNumber(ejs.getMainFrame()));
      }
    });
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing (WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable () {
          public void run() {
            ejs.quit();
            //            if (!ejs.checkChangesAndContinue(true)) return; // True = onExit
            //            //            System.err.println("Closing");
            //            ejs.getOptions().save();
            //            //            org.colos.ejs.external.BrowserForSimulink.exitMatlab();
            //            ejs.getProcessDialog().killAllProcesses();
            //            ejs.closedNormally = true;
            //            TemporaryFilesManager.clear();
            //            System.exit(0);
          }
        });
      }
      public void windowIconified (WindowEvent e) {
        ejs.windowsList.clear();
        for (Frame childFrame : Frame.getFrames()) {
          if (childFrame!=e.getWindow() && childFrame.isVisible()) { 
            childFrame.setVisible(false); 
            ejs.windowsList.add(childFrame); 
          }
        }
        for (Window window : ejs.dependentWindowsList) {
          if (window!=e.getWindow() && window.isVisible()) { 
            window.setVisible(false); 
            ejs.windowsList.add(window); 
          }
        }

      }
      public void windowDeiconified (WindowEvent e) {
        for (Window window : ejs.windowsList) window.setVisible(true);
        ejs.windowsList.clear();
      }
    });
    frame.getContentPane().setLayout(new java.awt.BorderLayout());
    frame.getContentPane().add (ejs.topPanel,BorderLayout.CENTER);
    frame.pack();
    //    Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    //    frame.setLocation((d.width - frame.getSize().width)/2, (d.height - frame.getSize().height)/2);
    Rectangle bounds = org.colos.ejs.library.control.EjsControl.getGraphicsConfiguration(screen).getBounds(); 
    frame.setLocation(bounds.x + (bounds.width - frame.getSize().width)/2, bounds.y + (bounds.height - frame.getSize().height)/2);

    String fileToRead = null;
    boolean readLastFile=false;
    // Final scan of run-time options
    for (int i=0; i<args.length; i++) {
      if      (args[i].equals("-file")) fileToRead = args[i+1];
      else if (args[i].equals("-lastFile")) readLastFile = true;
      else if (args[i].equals("-compile")) { // java -jar EjsConsole.jar -outputDir _apps2 -compile _users/murcia/fem/Demo/EarthAndMoon.xml
        final String fileToCompile = args[++i];
        final boolean andCompressIt = compressFile; 
        final String outputPrefix = exportPrefix;
        final boolean andZipIt = zipFile; 
        final String zipOutputPrefix = zipPrefix;
        try {
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() { 
              org.colos.ejs.library.control.swing.ControlWindow.setKeepHidden(true);
              ejs.justToCompile = true;
              ejs.readFilename (fileToCompile);
              if (ejs.mProgrammingLanguage==Osejs.PROGRAMMING_LANGUAGE.JAVA_PLUS_HTML) {
                ejs.currentMetadataFile = Generate.generate(ejs,false,getFreePort());
                if (andCompressIt) {
                  Generate.packageCurrentSimulation(ejs, new File(ejs.getExportDirectory(),outputPrefix+ejs.getCurrentXMLFilename()+".jar")); 
                  GenerateJS.packageXMLSimulation(ejs, outputPrefix+ejs.getCurrentXMLFilename()+".zip"); 
                }
              }
              else if (ejs.mProgrammingLanguage==Osejs.PROGRAMMING_LANGUAGE.JAVASCRIPT) {
                // No need to compile
                if (andCompressIt) GenerateJS.packageXMLSimulation(ejs, outputPrefix+ejs.getCurrentXMLFilename()+".zip"); 
              }
              else {
                ejs.currentMetadataFile = Generate.generate(ejs,false,-1);
                if (andCompressIt) Generate.packageCurrentSimulation(ejs, new File(ejs.getExportDirectory(),outputPrefix+ejs.getCurrentXMLFilename()+".jar")); 
              }
              if (andZipIt) Generate.zipCurrentSimulation(ejs, new File(ejs.getExportDirectory(),zipOutputPrefix+ejs.getCurrentXMLFilename()+".zip")); 
              TemporaryFilesManager.clear();
              System.exit(0);
            }
          });
        } catch (InterruptedException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        } catch (InvocationTargetException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      }
    }
    frame.setVisible(true);
    EjsControl.setDefaultScreen(OsejsCommon.getScreenNumber(frame));
    if (pD!=null) pD.dispose();
    ejs.getOptions().sizeFrame(frame);
    ejs.getOptions().placeFrame(frame);
    ejs.splitPanel.setResizeWeight(0.65);
    ejs.splitPanel.validate();
    ejs.outputArea.getComponent().requestFocus();
    if (mustAskAuthorInfo.value) ejs.getOptions().askAuthorInfo(frame);
    // This needs to be here because otherwise it affects the size of the main EJS panel
    if (fileToRead!=null) {
      final String theFileToRead = fileToRead;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() { 
          File file = new File (ejs.getSourceDirectory(),theFileToRead); // Try it relative to EJS's source directory first
          if (!file.exists()) file = new File(theFileToRead); 
          ejs.readFile(file,false);
          ejs.getFileDialog(file.getParentFile()); // Just to set the chooser's current directory
          //          ejs.readFilename (theFileToRead); 
        }
      });
    }
    else if (readLastFile){
      final String lastFilename = ejs.myEjsOptions.lastXMLFilePath();
      if (lastFilename!=null) {
        final File file = new File (lastFilename);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { 
            if (file.exists()) {
              if (pD!=null) pD.reportProgress(res.getString("Osejs.Init.ReadingFile")+" "+ejs.getPathRelativeToSourceDirectory(lastFilename));
              ejs.readFile(file,false);
              ejs.getFileDialog(file.getParentFile()); // Just to set the chooser's current directory
            }
          }
        });
      }
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        //        System.err.println("Quitting");
        if (ejs.closedNormally) return;
        //        System.err.println("Actions on quit");
        //        if (ejs.isChanged()) { // This hangs the program
        //          Object[] options = new Object[] {res.getString("Osejs.File.SaveChanges"),res.getString("Osejs.File.IgnoreChanges")};
        //          String message = ejs.getPathRelativeToSourceDirectory(FileUtils.getPath(ejs.currentXMLFile))+"\n"+res.getString("Osejs.WantToSaveBeforeExit");
        //          int option = JOptionPane.showOptionDialog(ejs.mainPanel, message, res.getString("Osejs.File.SimulationChanged"), 
        //              JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        //          if (option==0) ejs.saveFile(ejs.currentXMLFile);
        //        }
        ejs.getOptions().save();
        //        org.colos.ejs.external.BrowserForSimulink.exitMatlab();
        ejs.getProcessDialog().killAllProcesses();
      }
    });

    if (isMacOSX) {
      double javaVersion = OsejsCommon.getJavaVersion();
      try {
        Class<?> handlerClass = (javaVersion>=9.0) ? Class.forName("org.colos.ejs.osejs.macos.MacOSXHandlerForEjsJava9") : Class.forName("org.colos.ejs.osejs.macos.MacOSXHandlerForEjs");
        Class<?>[] __c = { Osejs.class };
        Object[] __o = { ejs };
        java.lang.reflect.Constructor<?> constructor = handlerClass.getDeclaredConstructor(__c);
        constructor.newInstance(__o);
        //        JOptionPane.showMessageDialog(null,"MacOSX handlers added","EJS message",JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,"Can't add MacOSX handlers:\n"+e.getMessage(),"EJS Console error",JOptionPane.ERROR_MESSAGE);
      }     
    }
    ejs.setMenuBar(frame);
    // And finally adjust the font level
    ejs.setZoomLevel(FontSizer.getLevel());

  }

  public void setMenuBar(JFrame frame) {
    if (isPureMac) { frame.setJMenuBar(createMenuBar()); }
  }

  private JMenuBar createMenuBar() {
    JMenuItem openMenuItem = new JMenuItem(res.getString("Osejs.File.Open"));
    openMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) { 
        openFile(false);
      }
    });
    openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    JMenuItem networkMenuItem = new JMenuItem(res.getString("Osejs.File.DigitalLibrary"));
    networkMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) { 
        DigitalLibraryUtils.showDialog(Osejs.this);
      }
    });
    networkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    JMenuItem saveMenuItem = new JMenuItem(res.getString("Osejs.File.Save"));
    saveMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) { saveFile(currentXMLFile); }
    });
    saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    JMenuItem runMenuItem = new JMenuItem(res.getString("RunMenu.RunAgain"));
    //    fileMenu.setText("File");
    //    runMenuItem.setText("Run");
    runMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) { 
        if (myEjsOptions.saveWhenRunning() && isChanged()) saveFile(currentXMLFile);
        runSimulation(true);
      }
    });
    runMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    JMenuItem stopMenuItem = new JMenuItem(res.getString("RunMenu.KillCurrentSimulation"));
    stopMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) { 
        getProcessDialog().killLastProcess();
        lastProcess = null;
      }
    });
    stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    // (3) some time later

    JMenu fileMenu = new JMenu(res.getString("Osejs.File"));
    fileMenu.add(openMenuItem);
    fileMenu.add(networkMenuItem);
    fileMenu.add(new JSeparator());
    fileMenu.add(saveMenuItem);

    JMenu projectMenu = new JMenu(res.getString("Osejs.Project"));
    projectMenu.add(runMenuItem);
    projectMenu.add(stopMenuItem);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(projectMenu);

    return menuBar;
  }


  public boolean quit() {
    if (!checkChangesAndContinue(true)) return true; // True = onExit
    //            System.err.println("Closing");
    getOptions().save();
    //            org.colos.ejs.external.BrowserForSimulink.exitMatlab();
    getProcessDialog().killAllProcesses();
    closedNormally = true;
    TemporaryFilesManager.clear();
    System.exit(0);
    return false;
  }

  public void editPreferences() {
    myEjsOptions.edit(mainPanel);
  }

  // -----------------------------------------------
  // start-up methods
  // -----------------------------------------------

  /**
   * Set EJS' bin and user directories
   */
  private void initializeDirectories (String outputPath) {
    if (verbose) {
      System.out.println ("User home  "+System.getProperty("user.home"));
      System.out.println ("User dir   "+System.getProperty("user.dir"));
    }

    String home = System.getProperty("home");
    if (home==null) home = OsejsCommon.USER_DIR_PATH;
    home = home.replace('\\','/');

    // user directories
    File userDirectory = new File(home);
    configDirectory = new File (userDirectory,OsejsCommon.CONFIG_DIR_PATH);
    exportDirectory = new File (userDirectory,OsejsCommon.EXPORT_DIR_PATH);
    outputDirectory = new File (userDirectory,outputPath);
    sourceDirectory = new File (userDirectory,OsejsCommon.SOURCE_DIR_PATH);
    // Create the workspace directories
    configDirectory.mkdirs();
    exportDirectory.mkdirs();
    outputDirectory.mkdirs();
    sourceDirectory.mkdirs();

    // binary (EJS installation) directory
    String binPath = System.getProperty("user.dir");
    binDirectory = new File (binPath,OsejsCommon.BIN_DIR_PATH);
    docDirectory = new File (binPath,OsejsCommon.DOC_DIR_PATH);
    //    File defaultConfigDir = new File (binDirectory,OsejsCommon.CONFIG_DIR_PATH);
    org.colos.ejs.library.Simulation.setPathToLibrary(FileUtils.getAbsolutePath(OsejsCommon.CONFIG_DIR_PATH,binDirectory));

    // Additional routines    
    unnamedXMLFile = this.getCorrectExtension(new File (sourceDirectory,"Unnamed.ejs")); // Default unnamed XML file
    ResourceLoader.addSearchPath(FileUtils.getPath(sourceDirectory));
    //String optionsFilePath = System.getProperty("ejs.console_options");
    //if (optionsFilePath==null) optionsFilePath = FileUtils.getPath(new File(binDirectory,"ConsoleOptions.txt"));

    // Store the information about last time EJS was run
    EjsTool.saveInformation(FileUtils.getPath(binDirectory.getParentFile()), 
        FileUtils.getPath(sourceDirectory),_EjsSConstants.VERSION);

  }

  /**
   * Initializes EJS' interface. This creates all the panels but does not open any window (if _showProgress is false).
   * @param _elementsFilename String The name of the file with the elements of the view to display. Default is null.
   * @param _iconsAt String Where to place the icons bar. One of "center", "north", "south", "east", or "west". Default is "east".
   * @param _showProgress boolean Whether to display a progress bar while loading.
   */
  private ProgressDialog initializeInterface (boolean _showProgress, JFrame _frame, int _screen, org.colos.ejs.library.control.value.BooleanValue _mustReadAuthorInfo) {
    Rectangle bounds = org.colos.ejs.library.control.EjsControl.getGraphicsConfiguration(_screen).getBounds();
    ProgressDialog pD = null;
    if (_showProgress) pD = new ProgressDialog(3,res.getString("Osejs.StartDialogTitle"),res.getDimension("Osejs.StartDialogSize"),bounds);

    // --- Create the icons bar
    if (pD!=null) pD.reportProgress(res.getString("Osejs.Init.Toolbars"));
    JToolBar iconbar = new JToolBar();
    iconbar.setRollover (false);
    iconbar.setBorderPainted(false);
    iconbar.setFloatable(false);
    iconbar.setBorder (new javax.swing.border.EmptyBorder(1,2,0,0));
    iconbar.setOrientation(SwingConstants.VERTICAL);
    //JButton[] iconButtons = MenuUtils.createIconGroup (ResourceUtil.tokenizeString(sysRes.getString("Osejs.IconToolBar")), "Osejs.Icon.", al);

    // --- Create the task buttons
    final javax.swing.border.Border border = BorderFactory.createEmptyBorder(1,1,1,1);// createEtchedBorder(javax.swing.border.EtchedBorder.RAISED); 
    Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    final javax.swing.border.Border clickedBorder = BorderFactory.createLineBorder(new Color (128,64,255),1);
    // New: Create an empty simulation
    JComponent newButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.New")));   
    newButton.setBorder(border);
    newButton.setToolTipText(res.getString("Osejs.Icon.New"));
    newButton.setCursor(handCursor);
    newButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
              if (checkChangesAndContinue(false)) clear(false); 
              if (useNativeFileChooser()) FileChooserUtil.getEjsSFileChooser(Osejs.this).clear();
              else {
                if (fileDialog!=null) fileDialog.setSelectedFile(getCorrectExtension(new File(fileDialog.getCurrentDirectory(),"Unnamed.ejs")));
              }
            } 
          });
        }        
      }
    });

    // Open: Read an existing simulation
    JComponent openButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Open")));   
    openButton.setBorder(border);
    openButton.setToolTipText(res.getString("Osejs.Icon.Open"));
    openButton.setCursor(handCursor);
    openButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
          JPopupMenu openPopup = new JPopupMenu();
          openPopup.add(new JLabel (" "+res.getString("OpenMenu.PreviousFiles")));
          openPopup.add(new JSeparator());
          for (int i=previousOpenedFilePaths.size()-1; i>=0; i--) {
            String filename = previousOpenedFilePaths.get(i);
            final File file = new File (Osejs.this.getSourceDirectory(),filename);
            if (file.exists()) openPopup.add(new AbstractAction(filename){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() { 
                    if (checkChangesAndContinue(false) == false) return;
                    readFile(file,false);
                    getFileDialog(file.getParentFile()); // Just to set (correctly) the chooser's current directory
                  }
                });
              }
            });
          }
          openPopup.add(new JSeparator());
          openPopup.add(new AbstractAction(res.getString("OpenMenu.CleanList")){
            public void actionPerformed(java.awt.event.ActionEvent e) { previousOpenedFilePaths.clear(); }
          });
          openPopup.add(new AbstractAction(res.getString("Osejs.File.Merge")){
            public void actionPerformed(java.awt.event.ActionEvent e) { openFile(true); } // merging
          });
          openPopup.show(_evt.getComponent(),0,0); // openButton
        }
        else if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() 
          {
            public void run() { openFile(false); } // false = not merging;
          });
        }

      }
    });

    // save: Save the current simulation
    JComponent saveButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Save")));   
    saveButton.setBorder(border);
    saveButton.setToolTipText(res.getString("Osejs.Icon.Save"));
    saveButton.setCursor(handCursor);
    saveButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { saveFile(currentXMLFile); }
          });
        }
      }
    });

    // saveas: Save the current simulation with a different name
    JComponent saveasButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.SaveAs")));   
    saveasButton.setBorder(border);
    saveasButton.setToolTipText(res.getString("Osejs.Icon.SaveAs"));
    saveasButton.setCursor(handCursor);
    saveasButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { saveFile(unnamedXMLFile); }
          });
        }
      }
    });

    // search: Search dialog
    JComponent searchButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Search")));   
    searchButton.setBorder(border);
    searchButton.setToolTipText(res.getString("Osejs.Icon.Search"));
    searchButton.setCursor(handCursor);
    searchButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
          if (searchPopup==null) {
            searchPopup = new JPopupMenu();
            searchPopup.add(new AbstractAction(res.getString("SearchMenu.SearchString")){
              public void actionPerformed(java.awt.event.ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { 
                    if (searchDialog==null) searchDialog = new SearchDialog(Osejs.this,mainPanel);
                    searchDialog.show();
                  }
                });
              }
            });
            searchPopup.add(new AbstractAction(res.getString("SearchMenu.ShowInspectorsWithErrors")){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { viewEditor.getTree().updateProperties(true); }
                });
              }
            });
            JCheckBoxMenuItem smi = new JCheckBoxMenuItem(res.getString("EjsOptions.ShowPropertyErrors"));
            smi.setSelected(myEjsOptions.showPropertyErrors());
            smi.addActionListener( new ActionListener () {
              public void actionPerformed(java.awt.event.ActionEvent e) {
                myEjsOptions.setShowPropertyErrors(((JCheckBoxMenuItem)e.getSource()).isSelected());
              }
            });
            searchPopup.add(smi);
          }
          searchPopup.show(_evt.getComponent(),0,0);
        }
        else if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
              if (searchDialog==null) searchDialog = new SearchDialog(Osejs.this,mainPanel);
              searchDialog.show();
            }
          });
        }


      }
    });

    // run: Run the simulation
    runButton = new JLabel (NOT_RUNNING_ICON);   
    runButton.setBorder(border);
    runButton.setToolTipText(res.getString("Osejs.Icon.Run"));
    runButton.setCursor(handCursor);
    runButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
          if (runPopup==null) {
            runPopup = new JPopupMenu();
            if (!Osejs.this.supportsJava()) {
              runPopup.add(htmlViewsMenu = new JMenu(res.getString("RunMenu.RunModes")));
            }
            runPopup.add(new AbstractAction(res.getString("RunMenu.KillCurrentSimulation")){
              public void actionPerformed(java.awt.event.ActionEvent e) {
                getProcessDialog().killLastProcess();
                lastProcess = null;
              }
            });
            runPopup.add(new AbstractAction(res.getString("RunMenu.KillAllSimulations")){
              public void actionPerformed(java.awt.event.ActionEvent e) {
                getProcessDialog().killAllProcesses();
                lastProcess = null;
              }
            });
            runPopup.add(new AbstractAction(res.getString("RunMenu.RunAgain")){
              public void actionPerformed(java.awt.event.ActionEvent e) { runSimulation(false); }
            });
            runPopup.add(new AbstractAction(res.getString("RunMenu.KillSimulations")){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                getProcessDialog().show(runButton); 
              }
            });
          }
          if (htmlViewsMenu!=null) fillHtmlViewsMenu();
          runPopup.show(_evt.getComponent(),0,0);
        }
        else if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if (runButton.getIcon()==RUNNING_ICON) {
                getProcessDialog().killLastProcess();
                lastProcess = null;
              }
              else {
                if (myEjsOptions.saveWhenRunning() && isChanged()) saveFile(currentXMLFile);
                runSimulation(true);
              }
            }
          });
        }

      }
    });
    // info: Display info
    JComponent infoButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Info")));   
    infoButton.setBorder(border);
    infoButton.setToolTipText(res.getString("Osejs.Icon.Info"));
    infoButton.setCursor(handCursor);
    infoButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
          if (infoPopup==null) {
            infoPopup = new JPopupMenu();
            infoPopup.add(new AbstractAction(res.getString("InfoMenu.ShowWiki")){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { openWikiPage(""); }
                });
              }
            });
            infoPopup.add(getDiagnosticsMenu());
            infoPopup.add(new AbstractAction(res.getString("Print.PrintCode")){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { printCode(); }
                });
              }
            });
            infoPopup.add(new AbstractAction(res.getString("InfoMenu.TakeSnapshot")){
              public void actionPerformed(java.awt.event.ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { 
                    try { org.opensourcephysics.display.PrintUtils.saveComponentAsEPS(mainFrame); }
                    catch (Exception exc) { exc.printStackTrace(); }
                  }
                });
              }
            });
            infoPopup.add(new AbstractAction(res.getString("InfoMenu.TakeSnapshotWindow")){
              AWTEventListener focusListener;
              public void actionPerformed(java.awt.event.ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { 
                    focusListener = new AWTEventListener () {
                      public void eventDispatched(AWTEvent _event) {
                        WindowEvent windowEvent = (WindowEvent) _event;
                        if (windowEvent.getID()==WindowEvent.WINDOW_GAINED_FOCUS ) {
                          java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(focusListener);
                          try { org.opensourcephysics.display.PrintUtils.saveComponentAsEPS(windowEvent.getWindow()); }
                          catch (Exception exc) { exc.printStackTrace(); }
                        }
                      }
                    };
                    outputArea.println("InfoMenu.SelectWindow");
                    java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(focusListener,AWTEvent.WINDOW_EVENT_MASK);
                  }
                });
              }
            });
          }
          infoPopup.show(_evt.getComponent(),0,0);
        }
        else if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { openWikiPage(""); }
          });
        }

      }
    });

    // package: Compress and export
    JComponent packageButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Package")));   
    packageButton.setBorder(border);
    packageButton.setToolTipText(res.getString("Osejs.Icon.Package"));
    packageButton.setCursor(handCursor);
    packageButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
          if (packagePopup==null) {
            packagePopup = new JPopupMenu();
            packagePopup.add(new AbstractAction(res.getString("Package.PackageCurrentSimulation")){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { 
                    switch (mProgrammingLanguage) {
                      default : 
                      case JAVA :           packageSimulation(); break;
                      case JAVASCRIPT :     // Do not break
                      case JAVA_PLUS_HTML : GenerateJS.packageXMLSimulation(Osejs.this,null); break;
                    }
                  }
                });
              }
            });
            packagePopup.add(new AbstractAction(res.getString("Package.ZIPCurrentSimulation")){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { zipSimulation(); }
                });
              }
            });
            if (!supportsJava()) {
              packagePopup.add(new AbstractAction(res.getString("Package.PackageScorm")){
                public void actionPerformed(java.awt.event.ActionEvent e) { 
                  SwingUtilities.invokeLater(new Runnable () {
                    public void run() { GenerateJS.packageXMLSimulation(Osejs.this,null,true); } 
                  });
                }
              });
              packagePopup.add(new AbstractAction(res.getString("Package.PackageForSingleApp")){
                public void actionPerformed(java.awt.event.ActionEvent e) { 
                  SwingUtilities.invokeLater(new Runnable () {
                    public void run() { SingleApp.convertToSingleApp(Osejs.this); } 
                  });
                }
              });
              packagePopup.add(new AbstractAction(res.getString("Package.PackageForWidget")){
                public void actionPerformed(java.awt.event.ActionEvent e) { 
                  SwingUtilities.invokeLater(new Runnable () {
                    public void run() { WidgetExport.convertToWidgetXMLSimulation(Osejs.this); } 
                  });
                }
              });
//              packagePopup.add(new AbstractAction(res.getString("Package.SaveJSON")){
//                public void actionPerformed(java.awt.event.ActionEvent e) { 
//                  SwingUtilities.invokeLater(new Runnable () {
//                    public void run() { GenerateJS.saveJavaScriptSimulation(Osejs.this,null,false); } 
//                  });
//                }
//              });
//              packagePopup.add(new AbstractAction(res.getString("Package.SaveXML")){
//                public void actionPerformed(java.awt.event.ActionEvent e) { 
//                  SwingUtilities.invokeLater(new Runnable () {
//                    public void run() { GenerateJS.saveJavaScriptSimulation(Osejs.this,null,true); } 
//                  });
//                }
//              });
            }
            packagePopup.addSeparator();

            packagePopup.add(new AbstractAction(res.getString("Package.PackageSeveralSimulations")){
              public void actionPerformed(java.awt.event.ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { 
                    switch (mProgrammingLanguage) {
                      default : 
                      case JAVA :           Generate.packageSeveralSimulations(Osejs.this); break;
                      case JAVASCRIPT :     // Do not break
                      case JAVA_PLUS_HTML : Generate.packageSeveralXMLSimulations(Osejs.this,CreateListDialog.LIST_TYPE.OTHER); break;
                    }
                  }
                });
              }
            });
            packagePopup.add(new AbstractAction(res.getString("Package.ZIPSeveralSimulations")){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { 
                    if (checkChangesAndContinue(false) == false) return; // The user canceled the action
                    Generate.zipSeveralSimulations(Osejs.this);
                  }
                });
              }
            });

            if (supportsJava()) {
              packagePopup.add(new AbstractAction(res.getString("Package.PackageAllSimulations")){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                  SwingUtilities.invokeLater(new Runnable () {
                    public void run() { Generate.packageAllSimulations(Osejs.this); }
                  });
                }
              });
              
//              packagePopup.add(new AbstractAction(res.getString("Package.CreateGroupHTML")){
//                public void actionPerformed(java.awt.event.ActionEvent e) { 
//                  SwingUtilities.invokeLater(new Runnable () {
//                    public void run() { Generate.createGroupHTML(Osejs.this); }
//                  });
//                }
//              });
            }
            else { // does not support Java, only Javascript 
              packagePopup.add(new AbstractAction(res.getString("Package.PackageForBookApp")){
                public void actionPerformed(java.awt.event.ActionEvent e) { 
                  SwingUtilities.invokeLater(new Runnable () {
                    public void run() { Generate.packageSeveralXMLSimulations(Osejs.this,CreateListDialog.LIST_TYPE.BOOK_APP); }
                  });
                }
              });
              packagePopup.add(new AbstractAction(res.getString("Package.PackageForEPUB")){
                public void actionPerformed(java.awt.event.ActionEvent e) { 
                  SwingUtilities.invokeLater(new Runnable () {
                    public void run() { Generate.packageSeveralXMLSimulations(Osejs.this,CreateListDialog.LIST_TYPE.EPUB); }
                  });
                }
              });
            }

            packagePopup.addSeparator();
            
            packagePopup.add(new AbstractAction(res.getString("Package.RemoveCurrentSimulation")){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { removeSimulation(); }
                });
              }
            });
            if (supportsJava()) packagePopup.add(new AbstractAction(res.getString("Package.CleanSimulations")){
              public void actionPerformed(java.awt.event.ActionEvent e) { 
                SwingUtilities.invokeLater(new Runnable () {
                  public void run() { Generate.cleanSimulations(Osejs.this); }
                });;
              }
            });
          }
          packagePopup.show(_evt.getComponent(),0,0);
        }
        else if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
              switch (mProgrammingLanguage) {
                default : 
                case JAVA :           packageSimulation(); break;
                case JAVASCRIPT :     // do not break
                case JAVA_PLUS_HTML : GenerateJS.packageXMLSimulation(Osejs.this,null);; break;
              }
            }
          });
        }

      }
    });

    // options: display EJS options
    JComponent optionsButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Options")));   
    optionsButton.setBorder(border);
    optionsButton.setToolTipText(res.getString("Osejs.Icon.Options"));
    optionsButton.setCursor(handCursor);
    optionsButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { myEjsOptions.edit(mainPanel); }
          });
        }
      }
    });

    netButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.ReadFromServer")));   
    netButton.setBorder(border);
    netButton.setToolTipText(res.getString("Osejs.Icon.ReadFromServer"));
    netButton.setCursor(handCursor);
    netButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        if (!_evt.getComponent().isEnabled()) return;
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { DigitalLibraryUtils.showDialog(Osejs.this); }
          });
        }
      }
    });

    // options: display EJS options
    JComponent translateButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Translate")));   
    translateButton.setBorder(border);
    translateButton.setToolTipText(res.getString("Osejs.Icon.Translate"));
    translateButton.setCursor(handCursor);
    translateButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        if (!getSimInfoEditor().addTranslatorTool()) {
          JOptionPane.showMessageDialog(mainPanel,res.getString("TranslationEditor.EnableTranslation"), res.getString("Information"),JOptionPane.INFORMATION_MESSAGE);
          getSimInfoEditor().showRunningTab();
          return;
        }
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { translationEditor.setVisible(true); }
          });
        }
      }
    });

    iconbar.add(newButton);
    iconbar.add(openButton);
    iconbar.add(netButton);
    iconbar.add(saveButton);
    iconbar.add(saveasButton);
    iconbar.add(searchButton);
    iconbar.add(runButton);
    iconbar.add(translateButton);
    iconbar.add(packageButton);
    iconbar.add(optionsButton);
    iconbar.add(infoButton);
    //    iconbar.add(Box.createVerticalStrut(20));

    iconbar.add(Box.createVerticalGlue());

    // --- Create the main bar
    Box mainbar = Box.createHorizontalBox();
    //JPanel mainbarPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
    //mainbarPanelLeft.setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));

    mainButtons = MenuUtils.createRadioGroup(mainOptions,"Osejs.Main.", new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent _evt) { 
        cardLayout.show (mainPanel,_evt.getActionCommand()); 
      }
    }, false);
    Insets inset = new java.awt.Insets(0,3,0,10);

    for (int i=0; i<mainButtons.length; i++) {
      if (mainButtons[i]==null) continue;
      mainButtons[i].setFont(titleFont);
      mainButtons[i].setForeground(InterfaceUtils.color(res.getString(mainOptions[i]+".Color")));
      mainButtons[i].setToolTipText(res.getString("Osejs.Main."+mainOptions[i]+".ToolTip"));
      //mainButtons[i].setBorder(BorderFactory.createEmptyBorder(2,8,2,8));
      mainButtons[i].setMargin(inset);
      mainbar.add (mainButtons[i]);
      //      if (mainOptions[i].startsWith("Experiment")) experimentButton = mainButtons[i];
    }

    // simInfo: Save the current simulation with a different name
    simInfoButton = new JLabel (ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.SimInfo")));   
    simInfoButton.setBorder(border);
    simInfoButton.setToolTipText(res.getString("Osejs.Icon.SimInfo.Tooltip"));
    simInfoButton.setCursor(handCursor);
    simInfoButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { simInfoEditor.makeVisible(); }
          });
        }
      }
    });

    //mainbar.add(mainbarPanelLeft);
    mainbar.add(Box.createHorizontalGlue());
    mainbar.add(simInfoButton);

    // ---------- Creation of the main editors
    if (pD!=null) pD.reportProgress(res.getString("Osejs.Init.Editors"));


    myEjsOptions = new EjsOptions(this,_frame);

    simInfoEditor = new SimInfoEditor (this);
    translationEditor = new TranslationEditor(this);
    cardLayout = new CardLayout();
    mainPanel = new JPanel(cardLayout);
    editors = new Editor[4]; //mainButtons.length];
    editors[0] = descriptionEditor = new DescriptionEditor(this); descriptionEditor.setColor(InterfaceUtils.color(res.getString("Description.Color")));
    editors[1] = modelEditor       = new ModelEditor(this);       modelEditor.setColor(InterfaceUtils.color(res.getString("Model.Color")));
    editors[2] = viewEditor        = new ViewEditor(this);        viewEditor.setColor(InterfaceUtils.color(res.getString("View.Color")));
    editors[3] = htmlViewEditor    = new HtmlViewEditor(this);    htmlViewEditor.setColor(InterfaceUtils.color(res.getString("HtmlView.Color")));
    //    editors[4] = experimentEditor  = new ExperimentEditor(this);  experimentEditor.setColor(InterfaceUtils.color(res.getString("Experiment.Color")));

    mainPanel.add(descriptionEditor.getComponent(),"Description");
    mainPanel.add(modelEditor.getComponent(),"Model");
    if (supportsJava())       mainPanel.add(viewEditor.getComponent(),"View");
    else mainButtons[2].setVisible(false);
    if (supportsHtml()) mainPanel.add(htmlViewEditor.getComponent(),"HtmlView");
    else mainButtons[3].setVisible(false);
    //    mainPanel.add(experimentEditor.getComponent(),"Experiment");

    //    for (int i=0; i<mainButtons.length; i++) {
    //      String classname = sysRes.getString("Osejs.Main."+mainOptions[i]);
    //      if      (classname.endsWith("ViewEditor"))        editors[i] = viewEditor  = new ViewEditor(this);
    //      else if (classname.endsWith("ModelEditor"))       editors[i] = modelEditor = new ModelEditor(this);
    //      else if (classname.endsWith("DescriptionEditor")) editors[i] = descriptionEditor = new DescriptionEditor(this);
    //      else if (classname.endsWith("Html5Editor"))       editors[i] = htmlViewEditor = new HtmlViewEditor(this);
    //      else if (classname.endsWith("ExperimentEditor"))  editors[i] = experimentEditor = new ExperimentEditor(this);
    //      if (editors[i]!=null) {
    //        editors[i].setColor(InterfaceUtils.color(res.getString(mainOptions[i]+".Color")));
    //        mainPanel.add(editors[i].getComponent(),mainOptions[i]);
    //      }
    //    }

    // Select the first (Description) panel
    mainButtons[0].setSelected (true);
    cardLayout.show(mainPanel,mainOptions[0]);

    // ----- Create the output area
    outputArea = new OutputArea();
    outputArea.setFont(defaultFont);

    // ---------- update labels and titles
    updateCurrentFile(unnamedXMLFile); // Despite default changes in editors, no program has been loaded

    // ---------- Put everything together
    if (pD!=null) pD.reportProgress(res.getString("Osejs.Init.FinalStartUp"));

    JPanel superMainPanel = new JPanel(new BorderLayout());
    superMainPanel.add(BorderLayout.CENTER,mainPanel);
    superMainPanel.add(BorderLayout.EAST, iconbar);

    splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPanel.setTopComponent(superMainPanel);
    splitPanel.setBottomComponent(outputArea.getComponent());
    splitPanel.setResizeWeight(1.0);
    splitPanel.validate();
    splitPanel.setOneTouchExpandable(true);

    topPanel = new JPanel(new BorderLayout());
    topPanel.add(BorderLayout.NORTH, mainbar);
    topPanel.add(BorderLayout.CENTER, splitPanel);

    for (int i=0; i<editors.length; i++) editors[i].setName(OsejsCommon.OSEJS_NAME+"."+mainOptions[i]);
    setFont(defaultFont);

    //    if (org.opensourcephysics.display.OSPRuntime.isMac()) topPanel.setPreferredSize(res.getDimension("MAC.Osejs.Size"));
    //    else 
    //    if (org.opensourcephysics.display.OSPRuntime.isLinux()) topPanel.setPreferredSize(res.getDimension("LINUX.Osejs.Size"));
    //    else if (isNimbusLookAndFeel()) topPanel.setPreferredSize(res.getDimension("NIMBUS.Osejs.Size"));
    //    else topPanel.setPreferredSize(res.getDimension("Osejs.Size"));
    topPanel.validate();
    _mustReadAuthorInfo.value = myEjsOptions.read();
    outputArea.getComponent().requestFocusInWindow();
    return pD;
  }

  // ---------------------------------
  // Utilities for files
  // ---------------------------------


  /**
   * Choose a folder under the workspace source
   * @return
   */
  public File chooseFolderUnderSource () {
    File destDir = useNativeFileChooser() ? 
        chooseFileUnderSource (FileChooserUtil.getDirectoryFileChooser(Osejs.this), mainPanel,true) : 
//        chooseFileUnderSource (getImportDirectoryChooser(), mainPanel,false);
        chooseFileUnderSource (getFileDialog(null,false), mainPanel,false);        
    if (destDir==null) return null; // The user canceled it
    while (! (destDir.exists() && destDir.isDirectory()) ) destDir = destDir.getParentFile();
    return destDir;
  }
  
  /**
   * Forces to choose a file under the source directory.
   * Returns null if the user cancels the process
   */
  public File chooseFileUnderSource (JFileChooser _fileChooser, JComponent _parentComponent, boolean _toOpen) {
    File target=null;
    if (_toOpen) _fileChooser.setApproveButtonText(getOpenString());
    else _fileChooser.setApproveButtonText(res.getString("Osejs.File.Save"));
    while (target==null) {
      int ok;
      if (_toOpen) ok = _fileChooser.showOpenDialog(_parentComponent);
      else ok = _fileChooser.showSaveDialog(_parentComponent);
      if (ok!=JFileChooser.APPROVE_OPTION) return null; 
      target = _fileChooser.getSelectedFile();
      if (target==null || !FileUtils.isRelative(target,sourceDirectory)) {
        JOptionPane.showMessageDialog(_parentComponent,res.getString("Osejs.Read.MustBeUnderSource"),
            res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
        target=null;
      }
    }
    return target;
  }

  /**
   * Forces to choose a file under the source directory.
   * Returns null if the user cancels the process
   */
  public File chooseFileUnderSource (FileChooserUtil _fileChooser, Component _parentComponent, boolean _toOpen) {
    File target=null;
    while (target==null) {
      if (_toOpen) target = _fileChooser.showOpenDialog(_parentComponent);
      else target = _fileChooser.showSaveDialog(_parentComponent);
      if (target==null) return null;
      if (!FileUtils.isRelative(target,sourceDirectory)) {
        JOptionPane.showMessageDialog(_parentComponent,res.getString("Osejs.Read.MustBeUnderSource"),
            res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
        target=null;
      }
    }
    return target;
  }

  /**
   * Converts a relative filename to a File
   */
  public File getRelativeFile (String filename) {
    if (filename.startsWith("./")) return new File (getCurrentDirectory(),filename.substring(2));
    return new File (getSourceDirectory(),filename);
  }

  /**
   * Return the relative path of a file 
   */
  public String getRelativeParentPath (File _file) {
    String path = getRelativePath(FileUtils.getPath(_file));
    int index = path.lastIndexOf('/');
    if (index>=0) path = path.substring(0, index+1);
    return path;
  }

  /**
   * Return the relative path of a file 
   */
  public String getRelativePath (File _file) {
    return getRelativePath(FileUtils.getPath(_file));
  }

  /**
   * Return the relative path of a file 
   */
  public String getRelativePath (String path) {
    String relPath = FileUtils.getRelativePath (path,getCurrentDirectory(),true);
    if (relPath.startsWith("./")) return relPath;
    return getPathRelativeToSourceDirectory(path);
  }

  /**
   * Return the path of a user file relative to EJS' user directory
   */
  public String getPathRelativeToSourceDirectory (String path) { 
    return FileUtils.getRelativePath(path,sourceDirectory,false); 
  }

  /**
   * Return the path of a user file relative to EJS' user directory
   */
  public String getPathRelativeToSourceDirectory (File _file) { 
    return FileUtils.getRelativePath(FileUtils.getPath(_file),sourceDirectory,false); 
  }


  //------------------------------------
  // Utilities to control the interface
  //------------------------------------

  /**
   * Clears all the panels and labels of EJS
   */
  private void clear (boolean beforeReading) {
    boolean showErrors = myEjsOptions.showPropertyErrors();

    myEjsOptions.setShowPropertyErrors(false);
    simInfoEditor.clear(beforeReading);
    translationEditor.clear();
    for (int i=0; i<editors.length; i++) editors[i].clear();
    outputArea.clear();
    currentXMLFile = null;
    updateCurrentFile(unnamedXMLFile);
    lastProcess = null;
    forceCompilation();
    dependentWindowsList.clear();
    if (searchDialog!=null) searchDialog.clear();
    myEjsOptions.setShowPropertyErrors(showErrors);
    //System.err.println("Clear done");
    //    mainButtons[0].setSelected (true);
    //    cardLayout.show(mainPanel,mainOptions[0]);
  }

  //  /**
  //   * Show/Hide the experiments panel
  //   * @param show boolean
  //   */
  //  public void showExperimentsPanel (boolean show) {
  //    if (experimentButton==null) return;
  //    experimentButton.setVisible(show);
  //    if (experimentButton.isSelected()) {
  //      mainButtons[0].setSelected(true);
  //      cardLayout.show(mainPanel, mainOptions[0]);
  //    }
  //  }

  public void showDLButton(boolean visible) {
    netButton.setEnabled(visible); 
  }

  /**
   * Make sure a given panel is visible
   * @param header
   */
  public void showPanel (String header) {
    String panel, subpanel=null;
    int index = header.indexOf('.');
    if (index>0) {
      panel = header.substring(0,index);
      subpanel = header.substring(index+1);
    }
    else panel = header;
    if (mainFrame!=null) mainFrame.requestFocus();
    for (int i=0; i<mainOptions.length; i++) {
      if (mainOptions[i].equals(panel)) {
        cardLayout.show(mainPanel, mainOptions[i]);
        mainButtons[i].setSelected(true);
        if ("Model".equals(mainOptions[i])) modelEditor.showPanel(subpanel);
        return;
      }
    }
  }

  /**
   * Set the running condition
   * @param process Process
   * @param running boolean
   */
  public void setRunning (String name, Object process, boolean running) {
    //    System.out.println("Set running : process = "+process);
    //    System.out.println("Set running : Last process = "+lastProcess);
    //    System.out.println("Set running : are equal? = "+(process==lastProcess));
    ProcessListDialog dialog = getProcessDialog();
    if (running) {
      dialog.addProcess(process, name);
      lastProcess = process;
    }
    else {
      dialog.removeProcess(process);
      if (process==lastProcess) {
        viewEditor.showWindows(true);
        htmlViewEditor.showWindows(true);
      }
      lastProcess = null;
    }
    if (runButton!=null) {
      if (dialog.processesRunning()>0) {
        runButton.setIcon(RUNNING_ICON);
        runButton.setToolTipText(res.getString("RunMenu.KillCurrentSimulation"));
      }
      else {
        runButton.setIcon(NOT_RUNNING_ICON);
        runButton.setToolTipText(res.getString("Osejs.Icon.Run"));
      }
    }
  }

  /**
   * Change the font of (most of) the panels of EJS.
   * @param _font Font
   */
  public void setFont  (Font _font) {
    currentFont = _font;
    simInfoEditor.setFont(_font);
    translationEditor.setFont(_font);
    for (int i=0; i<editors.length; i++) editors[i].setFont(_font);
    outputArea.setFont(_font);
//    DigitalLibraryUtils.setFont(_font);
    if (searchDialog!=null) searchDialog.setFont(_font);
    // menuBar.setFont (smallFont.deriveFont (Font.BOLD));
  }
  
  public void setZoomLevel(int level) {    
    FontSizer.setFonts(getMainFrame(), level);
//    this.getOptions().setFontLevel(level);
//    simInfoEditor.setFontLevel(level);
//    translationEditor.setFontLevel(level);
    for (int i=0; i<editors.length; i++) editors[i].setZoomLevel(level);
//    outputArea.setFontLevel(level);
//    DigitalLibraryUtils.setFontLevel(level);
    //if (searchDialog!=null) searchDialog.setFontLevel(level);
  }

  /**
   * Used to mark whenever anything in the code changes
   * @param _ch boolean
   */
  private void setUnchanged () {
    //System.out.println ("Setting changed to false");
    simInfoEditor.setChanged(false);
    translationEditor.setChanged(false);
    for (int i=0; i<editors.length; i++) editors[i].setChanged(false);
    forceCompilation();
    SwingUtilities.invokeLater(new Runnable() {
      public void run () { 
        //System.out.println ("Setting changed to false (deferred)");
        simInfoEditor.setChanged(false);
        translationEditor.setChanged(false);
        for (int i=0; i<editors.length; i++) editors[i].setChanged(false);
        forceCompilation();
      }
    });
  }

  /**
   * Whether anything in the code has changed
   * @return boolean
   */
  public boolean isChanged () {
    //for (int i=0; i<editors.length; i++) System.out.println ("Editor "+editors[i].getName()+ "changed = "+editors[i].isChanged());
    if (simInfoEditor.isChanged()) return true;
    if (translationEditor.isChanged()) return true;
    for (int i=0; i<editors.length; i++) if (editors[i].isChanged()) return true;
    return false;
  }

  //------------------------------------
  // Searching and displaying help
  //------------------------------------

  /**
   * Search for a given String
   * @param _searchString
   * @param _mode
   * @param _searchIn
   * @return
   */
  public java.util.List<SearchResult> search (String _searchString, int _mode, int _searchIn) {
    java.util.List<SearchResult> list = new java.util.ArrayList<SearchResult>();
    if ((_searchIn & SearchResult.SEARCH_DESCRIPTION)!=0) list.addAll(descriptionEditor.search("",_searchString,_mode));
    if ((_searchIn & SearchResult.SEARCH_MODEL      )!=0) list.addAll(modelEditor.search      ("",_searchString,_mode));
    if ((_searchIn & SearchResult.SEARCH_VIEW       )!=0) list.addAll(viewEditor.search    ("",_searchString,_mode));
    if ((_searchIn & SearchResult.SEARCH_HTML_VIEW  )!=0) list.addAll(htmlViewEditor.search("",_searchString,_mode));
    //    if ((_searchIn & SearchResult.SEARCH_VIEW  )!=0) list.addAll(htmlViewEditor.search    ("",_searchString,_mode));
    //    if ((_searchIn & SearchResult.SEARCH_EXPERIMENTS)!=0) list.addAll(experimentEditor.search ("",_searchString,_mode));
    return list;
  }

  public JMenu getDiagnosticsMenu() {
    if (diagnosticsMenu==null) {
      diagnosticsMenu = new JMenu(Memory.getResource("Diagnostics.Menu"));

      diagnosticsMenu.add(new AbstractAction(ToolsRes.getString("Diagnostics.OS.About.Title")){
        public void actionPerformed(ActionEvent e) { Diagnostics.aboutOS(); }
      });      
      diagnosticsMenu.add(new AbstractAction(ToolsRes.getString("Diagnostics.Java.About.Title")){
        public void actionPerformed(ActionEvent e) { Diagnostics.aboutJava(); }
      });
      diagnosticsMenu.add(new AbstractAction(Memory.getResource("Diagnostics.Properties")){
        public void actionPerformed(ActionEvent e) {
          DiagnosticsForSystem.aboutSystem(Osejs.this.mainFrame);
          //      Enumeration<?> propEnum = System.getProperties().propertyNames();
          //      while(propEnum.hasMoreElements()) {
          //        String next = (String) propEnum.nextElement();
          //        String val = System.getProperty(next);
          //        view.println (next+":  "+val); //$NON-NLS-1$
          //      }
        }
      });
      diagnosticsMenu.add(new AbstractAction(Memory.getResource("Diagnostics.Threads")){
        public void actionPerformed(ActionEvent e) { DiagnosticsForThreads.aboutThreads(); }
      });

      diagnosticsMenu.add(new AbstractAction(ToolsRes.getString("Diagnostics.Java3D.About.Title")){
        public void actionPerformed(ActionEvent e) { Diagnostics.aboutJava3D(); }
      });

      diagnosticsMenu.add(new AbstractAction(XuggleRes.getString("Xuggle.Dialog.AboutXuggle.Title")){
        public void actionPerformed(ActionEvent e) { DiagnosticsForXuggle.aboutXuggle(); }
      });

      diagnosticsMenu.add(new AbstractAction("OSP "+ToolsRes.getString("MenuItem.Log")){
        public void actionPerformed(ActionEvent e) {
          OSPLog log = OSPLog.getOSPLog();
          if (log!=null) log.setLocationRelativeTo(Osejs.this.mainFrame);
          OSPLog.showLog();
        }
      });

    }
    return diagnosticsMenu;
  }
  /**
   * Open a given help page, either in the Web server or in its local copy
   * @param page
   */
  public void openWikiPage (String page) {
    //    Locale locale = Locale.getDefault();
    //    System.out.println ("Locale = "+locale);
    //    System.out.println ("Locale language = "+locale.getLanguage());
    //    System.out.println ("Locale country = "+locale.getCountry());
    //    System.out.println ("Requesting page "+page);
    if (page.endsWith("Elements.Poligon")) page = page.substring(0,page.length()-4)+"ygon"; // Fix the error from Poligon to Polygon
    else if (page.startsWith("Elements.UserDefined.")) page = "ElementsUserDefined";
    //System.out.println ("Changed to page "+page);
    // remove '.' characters
    String modifiedPage=""; 
    StringTokenizer tkn = new StringTokenizer(page,"."); 
    while (tkn.hasMoreTokens()) modifiedPage += tkn.nextToken(); 
    //System.out.println ("Final page "+modifiedPage);
    final String thePage = modifiedPage;
    SwingUtilities.invokeLater(new Runnable () {
      //    new Thread (new Runnable(){
      public void run() {
        boolean done=false;
        try {
          String WebServer = "http://"+OsejsCommon.EJS_SERVER;
          URL url = new URL(WebServer);
          url.openStream(); // This is here to throw an exception if the URL is not accessible
//          String webPage;
//          String locale = Locale.getDefault().getLanguage();
//          if ("es".equals(locale)) webPage = WebServer+"/Es/"+thePage;
//          else webPage = WebServer+"/Main/"+thePage;
          String webPage = WebServer+"/Main/"+thePage;
          done = org.opensourcephysics.desktop.OSPDesktop.displayURL(webPage);
        }
        catch (Exception exc) { } // Try local file
        if (!done) {
          if (firstTimeNetworkError) {
            outputArea.println(res.getString("InfoMenu.LocalWikiError"));
            firstTimeNetworkError = false;
          }
          // Set the locale
          String localPage = OsejsCommon.EJS_SERVER;
//          String locale = Locale.getDefault().getLanguage();
//          if ("es".equals(locale)) localPage += "/Es/";
//          else localPage += "/Main/";
          localPage += "/Main/";
          
          // Add the page
          if (thePage.trim().length()==0) localPage += "HomePage.html";
          else {
            localPage += thePage;
            if (!localPage.endsWith(".html")) localPage += ".html";
          }
          // Open the page or issue a warning
          if (!new File(docDirectory,localPage).exists()) {
            JOptionPane.showMessageDialog(getMainPanel(),res.getString("Osejs.Help.HelpNotFound")+localPage,
                res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
          }
          else {
            localPage = "file:///"+FileUtils.correctUrlString(FileUtils.getPath(docDirectory)+localPage);
            org.opensourcephysics.desktop.OSPDesktop.displayURL(localPage);
          }
        }
      }
    }); //.start();
  }

  // --------------------------------------
  // File operation methods
  // --------------------------------------

  /**
   * Set the ResourceLoader path so that files relative to the XML file can be found by it
   */
  private void setResourceLoaderPath(File _xmlFile) {
    // Remove the previous one ...
    if (addedResourcePath!=null) ResourceLoader.removeSearchPath(addedResourcePath);
    // ... and add the new one
    addedResourcePath=FileUtils.getPath(_xmlFile.getParentFile());
    if (verbose) System.out.println ("Directory added to ResourceLoader is "+addedResourcePath);
    ResourceLoader.addSearchPath(addedResourcePath);
  }

  /**
   * Updates the name of the different files to use which depend on the file loaded
   * @param _filename String
   */
  private void updateCurrentFile (File _xmlFile) {
    if (_xmlFile!=currentXMLFile) forceCompilation();
    currentXMLFile = _xmlFile;
    lastProcess = null;
    setUnchanged();
    String title = sysRes.getString("Osejs.Title") + " " + _EjsSConstants.VERSION;
    if (currentXMLFile!=unnamedXMLFile) {
      String plainName = getTitleName();
      String relativeName = getPathRelativeToSourceDirectory(FileUtils.getPath(currentXMLFile));
      simInfoEditor.setTitle(plainName);
      simInfoButton.setToolTipText(res.getString("SimInfoEditor.InfoFor")+" "+relativeName);
      translationEditor.setName(plainName);
      title += " - " + plainName;
    }
    else {
      simInfoEditor.setTitle(null);
      translationEditor.setName(unnamedXMLFile.getName());
      simInfoButton.setToolTipText(res.getString("Osejs.Icon.SimInfo.Tooltip"));
    }
    if (mainFrame!=null) mainFrame.setTitle(title);
  }

  private String getTitleName() {
    String plainName = FileUtils.getRelativePath(currentXMLFile,this.getSourceDirectory(),false);
    if (plainName.length()>70) plainName = "... "+currentXMLFile.getName();
    return plainName;
  }

  /**
   * Checks that the model has been saved to disk.
   * This is recommended before refering to auxiliary files
   * (which are refered to relatively)
   */
  public void saveXMLFileFirst(Component parent) {
    if (currentXMLFile!=unnamedXMLFile) return;
    JOptionPane.showMessageDialog(parent, res.getString("Osejs.ShowBeforeFiles"));
    saveFile (unnamedXMLFile);
  }


  /**
   * Check whether the file to be written exists and, if so, asks for confirmation
   * @param _filename
   * @return
   */ 
  private boolean checkExistence (File _file, JComponent _parentComponent) {
    if (!_file.exists()) return true;
    Object[] options =  { res.getString("Osejs.File.DoSave"), res.getString("Osejs.File.CancelSave")};
    String plainName = getPathRelativeToSourceDirectory(FileUtils.getPath(_file));
    if (plainName.length()>70) plainName = "... "+_file.getName();
    int option = JOptionPane.showOptionDialog(_parentComponent, plainName+"\n"+ res.getString("Osejs.File.Overwrite"), 
        res.getString("Osejs.File.FileExists"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
    return option==0;
  }

  public boolean checkChangesAndContinue (boolean onExit) {
    if (isChanged()) {
      Object[] options = new Object[] {res.getString("Osejs.File.SaveChanges"),
          res.getString("Osejs.File.IgnoreChanges"),
          res.getString("Osejs.File.CancelSave")};
      String message = onExit? res.getString("Osejs.WantToSaveBeforeExit") : res.getString("Osejs.File.WantToSave");
      message = getPathRelativeToSourceDirectory(FileUtils.getPath(currentXMLFile))+"\n"+message;
      int option = JOptionPane.showOptionDialog(mainPanel, message, res.getString("Osejs.File.SimulationChanged"), 
          JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
      if (option==2) return  false;
      if (option==0) saveFile(currentXMLFile);
      setUnchanged();
    }
    else if (onExit && myEjsOptions.checkOnExit()) {
      Object[] options = new Object[] { res.getString("Osejs.File.SaveChanges"),res.getString("Osejs.File.CancelSave")};
      int option = JOptionPane.showOptionDialog(mainPanel, res.getString("Osejs.WantToExit"),
          sysRes.getString("Osejs.Name"), JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE, null, options, options[0]);
      if (option!= 0) return false;
    }
    return  true;
  }

  // --------------------------------------
  // Input
  // --------------------------------------

  private void openFile (boolean _merging) {
    if (getProcessDialog().processesRunning()>0) {
      JOptionPane.showMessageDialog(getMainPanel(), res.getString("Osejs.File.SimulationRunning"),
          res.getString("Warning"), JOptionPane.WARNING_MESSAGE);
    }
    if (!_merging) {
      if (checkChangesAndContinue(false) == false) return; // The user canceled the action
    }
    File file;
    if (useNativeFileChooser()) {
      file = FileChooserUtil.getEjsSFileChooser(this).showOpenDialog(getMainPanel());
      if (file==null) return;
    }
    else {
      JFileChooser dialog = getFileDialog(null);
      dialog.setDialogTitle(getOpenString());
      dialog.setApproveButtonText(getOpenString());
      int returnVal = dialog.showDialog(getMainPanel(), getOpenString());
      if (returnVal!=JFileChooser.APPROVE_OPTION) return;
      file = dialog.getSelectedFile();
    }
    String filenameToLower = file.getName().toLowerCase(); 
    if (!OsejsCommon.isEJSfilename(filenameToLower)) {
      for (String suffix : OsejsCommon.sAllowedEJSExtensions) {
        File testFile = new File (file.getParentFile(),file.getName()+ suffix);
        if (testFile.exists()) file = testFile; // The user forgot to type the .ejs extension
        break;
      }
    }
    if (OsejsCommon.isEJSfile(file)) {
      readFile (file,_merging);
      return;
    }
    if (file.getName().toLowerCase().endsWith(".zip")) { // unzip and find EJS models inside
      final File fileToRead = file;
      SwingUtilities.invokeLater(new Runnable() {
        public void run () { 
          try {
            UnzipUtility.unzipAndChoose (Osejs.this,getMainFrame(),fileToRead.getName(),new FileInputStream(fileToRead));
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          }
        }
      });
      return;
    }
    // try to open the file using the default application
    if (OSPDesktop.edit(file)) return;
    if (OSPDesktop.open(file)) return;
    JOptionPane.showMessageDialog(mainPanel, // openButton 
        res.getString("Osejs.File.NotReadOK")+" "+getPathRelativeToSourceDirectory(FileUtils.getPath(file)),
        res.getString("Osejs.File.ErrorReadingFile"), JOptionPane.ERROR_MESSAGE);

  }

  private void addToPreviouslyOpened (File _xmlFile) {
    // Update the list of previously opened files
    String relative = getPathRelativeToSourceDirectory(FileUtils.getPath(_xmlFile));
    if (previousOpenedFilePaths.contains(relative)) previousOpenedFilePaths.remove(relative);
    previousOpenedFilePaths.add(relative);
    if (previousOpenedFilePaths.size()>20) previousOpenedFilePaths.remove(0);
  }

  /**
   * Copy relative auxiliary files to the destination directory.
   * Non relative files are copied to the EJS source directory
   */
  private void copyAuxiliaryFiles (Set<PathAndFile> _list, File _destDir) {
    boolean checkAll = true;
    ResourceUtil jarRes = null;
    Object[] ynOptions = null;

    for (PathAndFile paf : _list) {
      File target;
      //      System.out.println ("Copying "+paf.getPath());
      if (paf.getPath().startsWith("./")) target = new File (_destDir,paf.getPath().substring(2)); // Relative files are copied to the destination directory.
      else target = new File (getSourceDirectory(),paf.getPath()); // Non relative files are copied to the EJS source directory
      boolean doIt = true;
      if (checkAll && target.exists()) {
        if (jarRes==null) {
          jarRes = new ResourceUtil("org.opensourcephysics.resources.tools.tools");
          ynOptions = new Object[]{ jarRes.getString("JarTool.Yes"), jarRes.getString("JarTool.YesToAll"), jarRes.getString("JarTool.No")};
        }
        int option = JOptionPane.showOptionDialog(mainPanel, // openButton  
            getPathRelativeToSourceDirectory(FileUtils.getPath(target)) + " : "+ res.getString("Osejs.File.Overwrite"), 
            res.getString("Osejs.File.FileExists"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ynOptions, ynOptions[0]);
        switch (option) {
          case 0 : break; // Do nothing
          case 1 : checkAll = false; break; 
          case 2 : doIt = false; break; // Do not copy it
        }
      }
      if (doIt) {
        if (FileUtils.copy (paf.getInputStream(), target)) 
          outputArea.message("Osejs.File.SavedOK",getPathRelativeToSourceDirectory(FileUtils.getPath(target)));
        else 
          JOptionPane.showMessageDialog(mainPanel, // openButton 
              res.getString("Osejs.File.Error")+" "+getPathRelativeToSourceDirectory(FileUtils.getPath(target)),
              res.getString("Osejs.File.SavingError"), JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }

  /**
   * Read a file by its name
   * @param filename the name of the file relative to the source directory, or absolutely if that one doesn't exists
   */
  private void readFilename (String filename) {
    File file = new File (sourceDirectory,filename); // Try it relative to EJS's source directory first
    if (file.exists()) readFile (file,false); 
    else readFile (new File (filename),false); // Try absolutely
  }

  /*
  public JFileChooser getImportDirectoryChooser() {
    JFileChooser chooser = getFileDialog(null,false);
//    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    return chooser;
  }
*/

  /**
   * Checks for a correct version of the file
   * @param input
   * @return true if correct
   */
  private boolean checkVersion(String _header) {
    int begin = _header.indexOf("version=\"");
    if (begin<0) return false;
    int end = _header.indexOf("\"",begin+9);
    if (end<0) return false;
    String version = _header.substring(begin+9,end);
    int index = version.indexOf(' ');
    if (index>0) version = version.substring(0, index);
    String currentVersion = _EjsSConstants.VERSION;
    index = currentVersion.indexOf(' ');
    if (index>0) currentVersion = currentVersion.substring(0, index);
    //    System.err.println ("File version is <"+version+">");
    //    System.err.println ("Current version is <"+currentVersion+">");
    try {
      if (version.compareTo(currentVersion)>0) {
        String message = res.getString("Osejs.File.FileVersion")+" "+version+" "+
            res.getString("Osejs.File.CurrentVersion")+" "+_EjsSConstants.VERSION+".\n"+
            res.getString("Osejs.File.InvalidVersion")+"\n" +
            res.getString("Osejs.File.WantToOpenIt")+"\n";
        int confirm = JOptionPane.showConfirmDialog(mainPanel,message,
            res.getString("Warning"), JOptionPane.YES_NO_OPTION);
        return confirm==JOptionPane.YES_OPTION;
      }
    } catch (Exception exc) { System.err.println ("Wrong file version number: "+version); }
    return true;
  }


  /**
   * Checks for a password protection and asks for it
   * @param input
   * @return the decrypted code, null if the password was incorrect
   */
  private String checkPassword(String _header, String _content) {
    int begin = _header.indexOf("password=\"");
    if (begin<0) return _content;
    int end = _header.indexOf('"',begin+10);
    if (end<0) return _content;
    String codedPassword = _header.substring(begin+10,end);
    //    System.err.println ("coded password is "+codedPassword);
    if (codedPassword.length()<=0) return _content;
    Cryptic cryptic = new Cryptic("");
    try { // See if Launcher passed a password
      String systemPassword = System.getProperty("launcher.password");
      //      System.err.println ("EJS received a password = "+systemPassword);
      if (systemPassword!=null && systemPassword.length()>0) {
        System.setProperty("launcher.password", ""); // Only read it once
        systemPassword = systemPassword.substring(1,systemPassword.length()-1); // Remove comments
        //        System.err.println ("Password is = "+systemPassword);
        if (systemPassword.equals(codedPassword)) {
          cryptic.setCryptic(systemPassword);
          String realPassword = cryptic.decrypt();
          //          System.err.println ("Real password is "+realPassword);
          cryptic.setCryptic(_content);
          return cryptic.decrypt(realPassword);
        }
      }
    } catch (Exception _exc) { _exc.printStackTrace(); } // do nothing
    // Ask the user for a password
    String password = PasswordDialog.checkPassword(codedPassword, mainPanel, null);
    if (password==null) return null;
    cryptic.setCryptic(_content);
    return cryptic.decrypt(password);
  }
  
  /**
   * Read the given file
   * @param _xmlFile
   */
  public void readFile (File _xmlFile, boolean _merging) {
    if (!_xmlFile.exists()) {
      outputArea.message("Osejs.File.NotReadOK",FileUtils.getPath(_xmlFile));
      return;
    }

    if (checkLanguageSupport(_xmlFile)==false) return; // Aborted

    //boolean merging = false;
    // OLD: names which start with '_' are for merging files
    // if (_file.getName().startsWith("_")) merging = true;
    if (_merging) { // merging
      if (verbose) System.out.println ("Merging from "+FileUtils.getPath(_xmlFile));
      // Check if it is under the current directory
      if (!FileUtils.isRelative(_xmlFile, getCurrentDirectory())) { // Must be imported
        // copy its auxiliary files
        Set<PathAndFile> list = new HashSet<PathAndFile>();
        list.addAll(OsejsCommon.getAuxiliaryFiles(_xmlFile,getSourceDirectory(),mainPanel));
        copyAuxiliaryFiles (list,getCurrentDirectory());
        modelEditor.setMerging(true);
      }
    }
    else { // reading
      outputArea.clear();
      simInfoEditor.hideEditor();
      translationEditor.clear();
      if (verbose) System.out.println ("Reading from "+FileUtils.getPath(_xmlFile));

      // Check if it is an imported file and actually import it   
      if (!FileUtils.isRelative(_xmlFile, sourceDirectory)) { // Must be imported
        // Choose the destination directory
        File destDir = chooseFolderUnderSource();
        if (destDir==null) return;

        // Do the import
        Set<PathAndFile> list = new HashSet<PathAndFile>();
        list.add(new PathAndFile("./"+_xmlFile.getName(),_xmlFile));
        list.addAll(OsejsCommon.getAuxiliaryFiles(_xmlFile,_xmlFile.getParentFile(),mainPanel));
        copyAuxiliaryFiles (list,destDir);
        _xmlFile = new File (destDir,_xmlFile.getName());
        //        fileDialog.setCurrentDirectory(destDir.getParentFile());
      }
      modelEditor.setMerging(false);
      setResourceLoaderPath(_xmlFile);
    }

    // Read a file from under the source directory
    boolean ok = false;
    String input = FileUtils.readTextFile(_xmlFile,OsejsCommon.charsetOfFile(_xmlFile));
    if (input==null) JOptionPane.showMessageDialog(mainPanel, res.getString("Osejs.File.ReadError"),res.getString("Osejs.File.ReadingError"), JOptionPane.ERROR_MESSAGE);
    else {
      int begin = input.indexOf("<" + OsejsCommon.OSEJS_NAME);
      int end = input.indexOf("</" + OsejsCommon.OSEJS_NAME + ">");
      if (begin<0 || begin>=end) JOptionPane.showMessageDialog(mainPanel, res.getString("Osejs.File.InvalidFile"),
          res.getString("Osejs.File.ReadingError"), JOptionPane.INFORMATION_MESSAGE);
      else {
        int headerEnd = input.indexOf('>',begin);
        String header = input.substring(begin,headerEnd);
        String content = checkPassword(header,input.substring(headerEnd+1,end));
        if (content==null) {
          JOptionPane.showMessageDialog(mainPanel, res.getString("Osejs.File.ReadError"),res.getString("Osejs.File.ReadingError"), JOptionPane.INFORMATION_MESSAGE);
          return;
        }
        if (checkVersion(header)) ok = readString (_xmlFile,content,_merging);
      }
    } 
    if (ok) {
      if (_merging) {
        outputArea.message("Osejs.File.MergedOK",_xmlFile.getName());
        translationEditor.setName(this.currentXMLFile.getName());
      }
      else {
        updateCurrentFile(_xmlFile);
        simInfoEditor.detectAuxiliaryFiles(false); // Update the list of auxiliary files
        outputArea.message("Osejs.File.ReadOK",_xmlFile.getName());
        addToPreviouslyOpened (_xmlFile);
      }
    }
    else {
      if (_merging) outputArea.message("Osejs.File.NotMergedOK",FileUtils.getPath(_xmlFile));
      else outputArea.message("Osejs.File.NotReadOK",FileUtils.getPath(_xmlFile));
      currentXMLFile = null;
      updateCurrentFile(unnamedXMLFile);
    }
    this.setZoomLevel(FontSizer.getLevel());
  }

  public boolean isReading() { return isReading; }

  /**
   * Read an XML string with the code
   * @param _input String
   */
  public boolean readString (File _file, String _input, boolean _merging) {
    try {
      isReading = true;
      if (!_merging) {
        clear(true);
        if (_file!=null) currentXMLFile = _file; // Needed for relative resources to get the right path
      }
      {
        // Read the information about this simulation
        String infoText = null;
        int begin = _input.indexOf("<"+OsejsCommon.OSEJS_NAME+".Information>\n");
        if (begin>=0) infoText = _input.substring(begin+OsejsCommon.OSEJS_NAME.length()+15,_input.indexOf("</"+OsejsCommon.OSEJS_NAME+".Information>\n"));
        if (infoText!=null) simInfoEditor.readString(infoText,_merging);
      }
      // Read the code in each of the main editors
      for (int i=0; i<editors.length; i++) {
        int begin = _input.indexOf("<"+OsejsCommon.OSEJS_NAME+"."+mainOptions[i]+">\n");
        if (begin<0) continue;
        int end = _input.indexOf("</"+OsejsCommon.OSEJS_NAME+"."+mainOptions[i]+">\n");
        editors[i].readString(_input.substring(begin+mainOptions[i].length()+OsejsCommon.OSEJS_NAME.length()+4,end));
      }
      modelEditor.getVariablesEditor().updateControlValues(false,true); // Update the view with the value of the variables
      //     setChanged(false);

      // The translation editor must be read last, because it passes the loaded items to the description pages
      String trText = null;
      int beginTrText = _input.indexOf("<"+OsejsCommon.OSEJS_NAME+".TranslationEditor>\n");
      if (beginTrText>=0) trText = _input.substring(beginTrText+OsejsCommon.OSEJS_NAME.length()+15,_input.indexOf("</"+OsejsCommon.OSEJS_NAME+".TranslationEditor>\n"));
      if (trText!=null) translationEditor.readString(trText);

      modelEditor.getElementsEditor().readCompleted();

      isReading = false;
      return true;
    }
    catch (Exception exc) {
      JOptionPane.showMessageDialog(mainPanel, res.getString("Osejs.File.ReadError"),res.getString("Osejs.File.ReadingError"), JOptionPane.ERROR_MESSAGE);
      exc.printStackTrace();
      isReading = false;
      return false;
    }
  }

  // --------------------------------------
  // Output
  // --------------------------------------

  /**
   * Create a String with all the code
   * @return String
   */
  private String saveString (Editor _skipEditor) {
    String password = getSimInfoEditor().getPassword();
    String codedPassword="";
    if (password.length()>0) codedPassword = new Cryptic(password).getCryptic();
    StringBuffer code = new StringBuffer();
    code.append ("<" + OsejsCommon.OSEJS_NAME + " version=\""+_EjsSConstants.VERSION+"\" password=\""+codedPassword+"\">\n");

    StringBuffer content = new StringBuffer();
    content.append("<" + OsejsCommon.OSEJS_NAME + ".Information>\n");
    content.append(simInfoEditor.saveString());
    content.append("</" + OsejsCommon.OSEJS_NAME + ".Information>\n");

    String trStr = translationEditor.saveString();
    if (trStr.length()>0) {
      content.append("<" + OsejsCommon.OSEJS_NAME + ".TranslationEditor>\n");
      content.append(translationEditor.saveString());
      content.append("</" + OsejsCommon.OSEJS_NAME + ".TranslationEditor>\n");
    }

    //    content.append("<" + OsejsCommon.OSEJS_NAME + ".Properties>\n");
    //    content.append(translationEditor.saveStringBuffer());
    //    content.append("</" + OsejsCommon.OSEJS_NAME + ".Properties>\n");


    for (int i=0; i<editors.length; i++) {
      if (editors[i]==_skipEditor) {
        if (_skipEditor==viewEditor && !viewEditor.isEmpty()) {
          outputArea.println("Skipping Java view from this JS file...");
          viewEditor.clear();
        }
        if (_skipEditor==htmlViewEditor && !htmlViewEditor.isEmpty()) {
          outputArea.println("Skipping HTML view from this Java file...");
          htmlViewEditor.clear();
        }
        continue;
      }
      content.append("<" + OsejsCommon.OSEJS_NAME + "." + mainOptions[i] + ">\n");
      content.append(editors[i].saveStringBuffer());
      content.append("</" + OsejsCommon.OSEJS_NAME + "." + mainOptions[i] + ">\n");
    }
    // encrypt the content if there is a password
    if (password.length()>0) code.append(new Cryptic(content.toString(),password).getCryptic());
    else code.append(content);

    code.append("</" + OsejsCommon.OSEJS_NAME + ">\n");
    return code.toString();
  }

  private File getCorrectExtension(File file) {
    String nameToLower = file.getName().toLowerCase();
    switch (this.mProgrammingLanguage) {
      default : 
      case JAVA : 
        if (nameToLower.endsWith(".xml")) return file; // allow old xml files
        if (!nameToLower.endsWith(".ejs")) return new File (file.getParentFile(),org.colos.ejs.library.utils.FileUtils.getPlainName(file)+".ejs");
        break;
      case JAVASCRIPT : 
        if (!nameToLower.endsWith(".ejss")) return new File (file.getParentFile(),org.colos.ejs.library.utils.FileUtils.getPlainName(file)+".ejss");
        break;
      case JAVA_PLUS_HTML : 
        if (!nameToLower.endsWith(".ejsh")) return new File (file.getParentFile(),org.colos.ejs.library.utils.FileUtils.getPlainName(file)+".ejsh");
        break;
    }
    return file;
  }

  private File suggestChangeToCorrectExtension(File file) {
    File suggestedFile = getCorrectExtension(file);
    if (suggestedFile==file) return file;

    int option = JOptionPane.NO_OPTION; // No problem
    boolean hasHTMLView = !getHtmlViewEditor().isEmpty();
    boolean hasJavaView = !getViewEditor().isEmpty();
    switch(mProgrammingLanguage) {
      default : 
      case JAVA :
        if (hasHTMLView) return file;
        option = JOptionPane.showConfirmDialog(getMainPanel(),res.getString("Osejs.ShouldSaveInJava"),res.getString("Warning"), JOptionPane.YES_NO_CANCEL_OPTION);
        break;
      case JAVASCRIPT : 
        if (hasJavaView) return file;
        option = JOptionPane.showConfirmDialog(getMainPanel(),res.getString("Osejs.ShouldSaveInJavascript"),res.getString("Warning"), JOptionPane.YES_NO_CANCEL_OPTION);
        break;
      case JAVA_PLUS_HTML : 
        if (! (hasHTMLView && hasJavaView)) return file;
        option = JOptionPane.showConfirmDialog(getMainPanel(),res.getString("Osejs.ShouldSaveInJavaPlusHtml"),res.getString("Warning"), JOptionPane.YES_NO_CANCEL_OPTION);
        break;
    }
    switch (option) {
      default : 
      case JOptionPane.NO_OPTION :     return file; 
      case JOptionPane.CANCEL_OPTION : return null; 
      case JOptionPane.YES_OPTION :    return suggestedFile;
    }
  }

  /**
   * Saves the simulation to an XML file
   * @param _xmlFile
   */
  private void saveFile (File _xmlFile) {
    JFileChooser dialog=null;
    if (_xmlFile==unnamedXMLFile) { // Save under a new name
      if (useNativeFileChooser()) {
        FileChooserUtil.getEjsSFileChooser(Osejs.this).setCurrentFile(currentXMLFile);
      }
      else {
        dialog = getFileDialog(null);
        dialog.setDialogTitle(res.getString("Osejs.File.Save"));
        dialog.setSelectedFile(currentXMLFile);
      }
      if (useNativeFileChooser()) {
        do {
          _xmlFile = chooseFileUnderSource(FileChooserUtil.getEjsSFileChooser(Osejs.this), mainPanel,false); 
          if (_xmlFile==null) return; // The user canceled it
          File correctFile = getCorrectExtension(_xmlFile);
          if (correctFile.equals(_xmlFile)) break;
          _xmlFile = correctFile;
        } while (!checkExistence(_xmlFile,mainPanel)); // openButton);
      }
      else {
        do {
          _xmlFile = chooseFileUnderSource(dialog, mainPanel,false);
          if (_xmlFile==null) return; // The user canceled it
          _xmlFile = getCorrectExtension(_xmlFile);
        } while (!checkExistence(_xmlFile,mainPanel)); // openButton);
      }
      if (!simInfoEditor.hasAuthor()) simInfoEditor.fillAuthor(getOptions());
    }
    // Suggest correct extension for existing files
    _xmlFile = suggestChangeToCorrectExtension(_xmlFile);
    if (_xmlFile==null) return; // The user cancelled it

    // Check for .ejs extension if it has translations
    if (getTranslationEditor().getDesiredTranslations().size()>1) {
      if (OsejsCommon.charsetOfFile(_xmlFile)==null) {
        int option = JOptionPane.showConfirmDialog(mainPanel,res.getString("Osejs.ChangeToEJSExtension"),res.getString("Warning"), JOptionPane.YES_NO_CANCEL_OPTION);
        if (option==JOptionPane.CANCEL_OPTION) return; 
        if (option==JOptionPane.YES_OPTION) {
          _xmlFile = new File (_xmlFile.getParentFile(),org.colos.ejs.library.utils.FileUtils.getPlainName(_xmlFile)+ (supportsJava() ? ".ejs" : ".ejss"));
          if (!checkExistence(_xmlFile,mainPanel)) return;
        }
      }
    }
    // do it
    String filename = getPathRelativeToSourceDirectory(FileUtils.getPath(_xmlFile));
    if (verbose) System.out.println("You chose to save this file: " + filename);
    try {
      Charset charset = OsejsCommon.charsetOfFile(_xmlFile); //getUTF16(); // 
      StringBuffer content = new StringBuffer();
      if (charset!=null) content.append("<?xml version=\"1.0\" encoding=\""+charset.displayName()+"\"?>\n");
      content.append ("<!-- This XML file has been created by "+sysRes.getString("Osejs.Name")+" ("+sysRes.getString("Osejs.Title")+"). Visit http://www.um.es/fem/Ejs. -->\n");
      content.append ("<!-- Please, save the file to your hard disk on your Ejs' user directory and open it with Ejs. -->\n");
      //      if (charset!=null)  header.append ("<!-- This file was saved with the character set "+ charset.displayName() +" -->\n");

      Editor skipEditor = null;
      if (filename.toLowerCase().endsWith(".ejsh")) { } // Saving Java+HTML file. Do nothing, everything is saved
      else if (filename.toLowerCase().endsWith(".ejss")) { // Saving JS file
        if (!viewEditor.isEmpty()) {
          int option = JOptionPane.showConfirmDialog(getMainPanel(),res.getString("Osejs.ShouldSaveJavaView"),res.getString("Warning"), JOptionPane.YES_NO_CANCEL_OPTION);
          if (option==JOptionPane.CANCEL_OPTION) return;
          if (option==JOptionPane.NO_OPTION) skipEditor = viewEditor;
        }
      }
      else { // Saving Java file
        if (!htmlViewEditor.isEmpty()) {
          int option = JOptionPane.showConfirmDialog(getMainPanel(),res.getString("Osejs.ShouldSaveHtmlView"),res.getString("Warning"), JOptionPane.YES_NO_CANCEL_OPTION);
          if (option==JOptionPane.CANCEL_OPTION) return;
          if (option==JOptionPane.NO_OPTION) skipEditor = htmlViewEditor;
        }
      }
      content.append(saveString(skipEditor));
      FileUtils.saveToFile(_xmlFile, charset, content.toString());

      // Copy auxiliary files together with any new file
      if (!currentXMLFile.getParentFile().equals(_xmlFile.getParentFile())) {
        Set<PathAndFile> list = new HashSet<PathAndFile>();
        for (PathAndFile paf : simInfoEditor.getAuxiliaryPathAndFiles(false)) {
          if (paf.getPath().startsWith("./")) {
            if (paf.getFile().exists()) {
              if (paf.getFile().isDirectory()) {
                String dirpath = FileUtils.getPath(getCurrentDirectory());
                for (File subfile : JarTool.getContents(paf.getFile())) list.add(new PathAndFile(FileUtils.getRelativePath(subfile,dirpath,true),subfile));
              }
              else list.add(paf);
            }
            else outputArea.message("Osejs.File.NotReadOK", paf.getPath());
          }
        }
        copyAuxiliaryFiles (list,_xmlFile.getParentFile());
      }
      outputArea.message("Osejs.File.SavedOK",filename);

      // Adjust the EJS environment
      addToPreviouslyOpened (_xmlFile);
      updateCurrentFile(_xmlFile);
      setResourceLoaderPath(_xmlFile);
      if (dialog!=null) {
//        System.err.println("Dialog = "+dialog);
        dialog.rescanCurrentDirectory();
//        System.err.println("Rescanning ok");
      }
    } 
    catch (IOException ex) {
      ex.printStackTrace();
      outputArea.message("Warning",ex.getMessage());
      JOptionPane.showMessageDialog(mainPanel, res.getString("Osejs.File.Error"),res.getString("Osejs.File.SavingError"), JOptionPane.INFORMATION_MESSAGE);
    }
  }

  // --------------------------------------------------
  // Printing
  // --------------------------------------------------

  public void printCode () { print (saveString(null)); }

  public void print (String output) {
    HardcopyWriter hw;
    try { hw = new HardcopyWriter(this.mainFrame,this.currentXMLFile.getName(), 8, .5, .5, .5, .5); }
    catch (HardcopyWriter.PrintCanceledException e) { return; }
    PrintWriter out = new PrintWriter(hw);
    out.println (output);
    out.close();
  }

  //-------------------------------------
  // Compiling and running
  //-------------------------------------

  /**
   * Clears the class file path so that trying to run forces a compilation
   */
  public void forceCompilation()  { 
    currentMetadataFile = null; 
    //executionPath = null; 
  }

  protected boolean firstCompile (int _port) {
    if (isChanged()) forceCompilation();
    if (currentMetadataFile==null) currentMetadataFile = Generate.generate(this,supportsHtml(),_port); // server version
    return currentMetadataFile!=null;
  }

  public LocaleItem getLocaleItem () {
    OneView oneViewPage = ((OneView) htmlViewEditor.getCurrentPage());
    if (oneViewPage==null) return null;
    return oneViewPage.getLocale();
  }

  private void runSimulation (boolean _checkPrevious) {
    if (_checkPrevious && getProcessDialog().processesRunning()>0) {
      JOptionPane.showMessageDialog(runButton, res.getString("Generate.IsRunningAlready"),
          res.getString("Warning"), JOptionPane.WARNING_MESSAGE);
      return;
    }
    boolean runHtmlView = supportsHtml();
    if (runHtmlView) {
      if (htmlViewEditor.getPages().isEmpty()) {
        JOptionPane.showMessageDialog(getMainPanel(),res.getString("Osejs.HTMLempty"),
            res.getString("Warning"),JOptionPane.INFORMATION_MESSAGE);
        runHtmlView = false;
      }
      forceCompilation();
    }
    int port = -1;
    if (mProgrammingLanguage==PROGRAMMING_LANGUAGE.JAVA_PLUS_HTML) {
      port = getFreePort();
      if (port==-1) {
        JOptionPane.showMessageDialog(getMainPanel(),res.getString("Osejs.NoFreePorts"),
            res.getString("Error"),JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    if (supportsJava()) {
      if (firstCompile(port)) {
        viewEditor.showWindows(false);
        Generate.run(Osejs.this);
      }
      else return; // generating the Java program failed
    }

    if (runHtmlView) { // Javascript
      OneView oneViewPage = ((OneView) htmlViewEditor.getCurrentPage());
      if (oneViewPage==null) return;
      LocaleItem locale = oneViewPage.getLocale();
      if (simInfoEditor.runInBrowserFirst() || getOptions().runInBrowserFirst() || supportsJava()) {
        String localeString = locale.isDefaultItem() ? SimulationXML.sDEFAULT_LOCALE : locale.getKeyword();
        runSimulationInBrowser(oneViewPage.getName(), localeString, port);
        htmlViewEditor.showWindows(false);
      }
      else runUsingEmulator(locale);
    }
  }

  private void runSimulationInBrowser (String viewDesired, String locale, int port) { // Javascript only
    String name = FileUtils.getPlainNameAndExtension(currentXMLFile).getFirstString();
    SimulationXML simulation = getSimulationXML(name);
    if (simulation.getInformation(INFORMATION.TITLE)==null) simulation.setInformation(INFORMATION.TITLE,name);

    File cssFile;
    {
    String cssFilename = getSimInfoEditor().getCSSFile();
    if (cssFilename.length()>0) cssFile = new File(getCurrentDirectory(),cssFilename);
    else cssFile = new File(getOutputDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css/ejss.css");
    }

    File javascriptDir = new File(getBinDirectory(),"javascript/lib");
    String libPath = FileUtils.getPath(javascriptDir);
    String htmlPath = FileUtils.getPath(currentXMLFile.getParentFile());
    String relPath = FileUtils.getRelativePath(htmlPath, getSourceDirectory(), false);
//    String extension = simInfoEditor.generateHtmlFiles() ? ".html" : ".xhtml";
    String extension = ".xhtml";
    String outputFilepath = (port>=0) ? relPath+simulation.getName()+"_"+port+extension : relPath+simulation.getName()+extension;
    final File htmlFile = new File (getOutputDirectory(),outputFilepath);
    boolean ok = XMLTransformerJava.saveHTMLFile(Osejs.this,libPath,htmlFile, // output info 
        //        false, false, getOutputArea(), // EJS info 
        simulation, viewDesired, locale, FileUtils.getPath(cssFile.getAbsoluteFile()), libPath, htmlPath,getOptions().separateJSfile(),getOptions().useFullLibrary()); // generation info
    if (!ok) {
      getOutputArea().println(res.getString("Generate.JarFileNotCreated")+ " : "+outputFilepath);
      return;
    }
    // save model elements files associated to simulation
    String resources = getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED_BY_PACKAGE,"").toString();
    if (resources != null) {
      StringTokenizer tkn = new StringTokenizer(resources,";");    
      File modelsDir = new File(getBinDirectory(), "javascript/model_elements");
      while(tkn.hasMoreTokens()) {
        File resFolder = new File(modelsDir, tkn.nextToken().trim());
        for (File file : JarTool.getContents(resFolder)) { 
          String destName = FileUtils.getRelativePath(file, modelsDir, false);
          JarTool.copy(file, new File(getOutputDirectory(),destName));
        }
      }
    }     
    getOutputArea().println(res.getString("Generate.JarFileCreated")+ " : "+outputFilepath);
    GeneratedUtil.openBrowser(this, htmlFile);
//    org.opensourcephysics.desktop.OSPDesktop.displayURL(localPage);
  }

  private void runUsingEmulator(LocaleItem locale) {
    String name = FileUtils.getPlainNameAndExtension(currentXMLFile).getFirstString();
    SimulationXML simulation = getSimulationXML(name);
    if (simulation.getInformation(INFORMATION.TITLE)==null) simulation.setInformation(INFORMATION.TITLE,name);
    
    File cssFile;
    {
    String cssFilename = getSimInfoEditor().getCSSFile();
    if (cssFilename.length()>0) cssFile = new File(getCurrentDirectory(),cssFilename);
    else cssFile = new File(getOutputDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css/ejss.css");
    }

    File javascriptDir = new File(getBinDirectory(),"javascript/lib");
    String libPath = FileUtils.getPath(javascriptDir);
    String htmlPath = FileUtils.getPath(currentXMLFile.getParentFile());
    EmulatorRun emulator = new EmulatorRun(this,simulation,FileUtils.getPath(cssFile.getAbsoluteFile()),libPath,htmlPath);
    emulator.setLocaleItem(locale);
    emulator.setLocaleInMenu(locale);
    setRunning(name, emulator, true);
    htmlViewEditor.showWindows(false);
  }

  private void fillHtmlViewsMenu () { // Javascript only
    htmlViewsMenu.removeAll();
    boolean hasAny = false;
    if (simInfoEditor.runInBrowserFirst() || getOptions().runInBrowserFirst()) { // add the Emulator
      htmlViewsMenu.add(new AbstractAction(res.getString("RunMenu.RunWithEmulator")) {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          OneView oneViewPage = ((OneView) htmlViewEditor.getCurrentPage());
          if (oneViewPage==null) return;
          LocaleItem locale = oneViewPage.getLocale();
          runUsingEmulator(locale);
        }
      });
    }
    for (Editor page : htmlViewEditor.getPages()) {
      final OneView oneViewPage = ((OneView) page);
      hasAny = true;
      LocaleItem locale = oneViewPage.getLocale();
      final String localeString = locale.isDefaultItem() ? SimulationXML.sDEFAULT_LOCALE : locale.getKeyword();
      htmlViewsMenu.add(new AbstractAction(res.getString("RunMenu.Browser")+": "+oneViewPage.getName()) {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          runSimulationInBrowser(oneViewPage.getName(),localeString,-1);
        }
      });
    }
    htmlViewsMenu.setEnabled(hasAny);
  }


  static public int getFreePort() {
    int minport = 8800;
    int maxport = 8900;
    int port = minport;
    while (port<maxport) {
      try {
        ServerSocket commSocket = new ServerSocket();
        commSocket.bind(new InetSocketAddress(port));
        commSocket.close();
        return port;
      } catch (IOException e) { }
      port++;
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) { }
    }
    return -1;
  }

  public SimulationXML getSimulationXML(String name) {
    SimulationXML simulation = new SimulationXML(name);
    if (supportsJava()) simulation.setViewOnly(""+getFreePort());
    simInfoEditor.fillXMLSimulation(simulation);
    StringBuffer localeStr = new StringBuffer("_default_");
    for (LocaleItem item : getTranslationEditor().getDesiredTranslations()) {
      if (item.isDefaultItem()) continue; // This is obvious
      localeStr.append(";");
      localeStr.append(item.getKeyword());
    }
    simulation.setInformation(INFORMATION.LOCALES_SUPPORTED,localeStr.toString());
    descriptionEditor.fillSimulationXML(simulation);
    if (!simulation.isViewOnly()) modelEditor.fillSimulationXML(simulation);
    htmlViewEditor.fillSimulationXML(simulation);
    return simulation;
  } 

  private void packageSimulation () {
    getExportDirectory().mkdirs(); // In case it doesn't exist
    String plainName = getCurrentXMLFilename();

    // Select the target
    String prefix;
    switch (this.mProgrammingLanguage) {
      default : 
      case JAVA :           prefix = "ejs"; break;
      case JAVASCRIPT :     prefix = "ejss"; break;
      case JAVA_PLUS_HTML : prefix = "ejsh"; break; 
    }

    String targetName = FileChooserUtil.chooseFilename(this, new File(getExportDirectory(),prefix+"_model_"+plainName+".jar"), 
        "JAR", new String[]{"jar"}, true);
    if (targetName==null) {
      getOutputArea().println(res.getString("Package.JarFileNotCreated"));
      return;
    }
    boolean warnBeforeOverwritting = true;
    if (! targetName.toLowerCase().endsWith(".jar")) targetName = targetName + ".jar";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
    File targetFile = new File(targetName);
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(mainPanel,DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) {
        getOutputArea().println(res.getString("Package.JarFileNotCreated"));
        return;
      }
    }
    if (firstCompile(-1)) {
      Generate.packageCurrentSimulation(Osejs.this, targetFile);
    }
    else JOptionPane.showMessageDialog(mainPanel, res.getString("Package.CantCompileError"),
        res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
  }

  private void removeSimulation() {
    File xmlFile = getCurrentXMLFile();
    if (!xmlFile.exists()) {
      JOptionPane.showMessageDialog(getMainPanel(), res.getString("Package.NoSimulations"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    java.util.List<PathAndFile> list = new ArrayList<PathAndFile>();
    String sourcePath = FileUtils.getPath(sourceDirectory);
    list.add(new PathAndFile(FileUtils.getRelativePath(xmlFile,sourcePath,false),xmlFile));

    for (PathAndFile paf : getSimInfoEditor().getAuxiliaryPathAndFiles(false)) {
      if (!paf.getFile().exists()) continue;
      list.add(new PathAndFile(FileUtils.getRelativePath(paf.getFile(),sourcePath,false),paf.getFile()));
      if (paf.getFile().isDirectory()) { // Add also its contents is a complete directory
        for (File file : JarTool.getContents(paf.getFile())) 
          list.add(new PathAndFile(FileUtils.getRelativePath(file,sourcePath,false),file));
      }
    }

    // Ask the user for confirmation
    java.util.List<Object> confirmedList = EjsTool.ejsConfirmList(this.getMainPanel(),
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.RemoveSimulationsMessage"),
        res.getString("Package.RemoveCurrentSimulation"),list);
    if (confirmedList==null) return;
    clear(false);
    String deleteString = res.getString("Package.FileDeleted")+" ";
    for (Iterator<?> it=confirmedList.iterator(); it.hasNext(); ) {
      PathAndFile fileNamed = (PathAndFile) it.next();
      File file = fileNamed.getFile();
      if (!file.isDirectory()) {
        outputArea.println(deleteString+fileNamed.getPath());
        file.delete();
      }
    }
    // Now, remove possible empty directories
    for (Object object : confirmedList) {
      PathAndFile fileNamed = (PathAndFile) object;
      File file = fileNamed.getFile();
      if (file.isDirectory() && JarTool.getContents(file).isEmpty()) {
        outputArea.println(deleteString+fileNamed.getPath());
        file.delete();
      }
    }
  }

  private void zipSimulation () {
    getExportDirectory().mkdirs(); // In case it doesn't exist

    if (checkChangesAndContinue(false) == false) return; // The user canceled the action

    // Select the target
    String prefix;
    switch (this.mProgrammingLanguage) {
      default : 
      case JAVA :           prefix = "ejs"; break;
      case JAVASCRIPT :     prefix = "ejss"; break;
      case JAVA_PLUS_HTML : prefix = "ejsh"; break;
    }
    String targetName = FileChooserUtil.chooseFilename(this, new File(getExportDirectory(),prefix+"_src_"+getCurrentXMLFilename()+".zip"), 
        "ZIP", new String[]{"zip"}, true);
    if (targetName==null) {
      getOutputArea().println(res.getString("Package.JarFileNotCreated"));
      return;
    }
    boolean warnBeforeOverwritting = true;
    if (! targetName.toLowerCase().endsWith(".zip")) targetName = targetName + ".zip";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
    File targetFile = new File(targetName);
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) {
        getOutputArea().println(res.getString("Package.JarFileNotCreated"));
        return;
      }
    }
    Generate.zipCurrentSimulation(Osejs.this, targetFile);
  }

  //  private void packageXMLSimulationForReader () {
  //    getExportDirectory().mkdirs(); // In case it doesn't exist
  //    if (checkChangesAndContinue(false) == false) return; // The user canceled the action
  //    // Select the target
  //    File targetFile = new File(getExportDirectory(),getCurrentXMLFilename()+"."+ sEJSS_ZIP_EXTENSION);
  //    JFileChooser chooser = OSPRuntime.createChooser("ZIP",new String[]{sEJSS_ZIP_EXTENSION},getSourceDirectory().getParentFile());
  //    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
  //    chooser.setCurrentDirectory(targetFile.getParentFile());
  //    chooser.setSelectedFile(targetFile);
  //    String targetName = OSPRuntime.chooseFilename(chooser,getMainPanel(), true);
  //    if (targetName==null) {
  //      getOutputArea().println(res.getString("Package.JarFileNotCreated"));
  //      return;
  //    }
  //    boolean warnBeforeOverwritting = true;
  //    if (! (targetName.toLowerCase().endsWith("."+sEJSS_ZIP_EXTENSION) || targetName.toLowerCase().endsWith(".zip")) ) targetName = targetName + "."+sEJSS_ZIP_EXTENSION;
  //    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
  //    targetFile = new File(targetName);
  //    if (warnBeforeOverwritting && targetFile.exists()) {
  //      int selected = JOptionPane.showConfirmDialog(getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
  //          targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
  //          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
  //      if (selected != JOptionPane.YES_OPTION) {
  //        getOutputArea().println(res.getString("Package.JarFileNotCreated"));
  //        return;
  //      }
  //    }
  //    Generate.xmlPackageCurrentSimulation(Osejs.this, targetFile);
  //  }

  // --------------------------------------------------
  // Correct programming language support
  // --------------------------------------------------

  /**
   * Checks for the correct programming language support.
   * If correct, it just returns true
   * If incorrect, it offers the possibility to launch EJS with the correct support
   * @param ejs
   * @param file
   * @return true if the language support is correct or if the user wants to proceed with the current setting
   */
  public boolean checkLanguageSupport(File file) {
    TwoStrings nameAndExtension = FileUtils.getPlainNameAndExtension(file);
    String extension = nameAndExtension.getSecondString().toLowerCase();
    int option = JOptionPane.NO_OPTION; // No problem
    String progLang = null;
    if ("ejss".equals(extension)) {
      if (mProgrammingLanguage!=PROGRAMMING_LANGUAGE.JAVASCRIPT) {
        option = JOptionPane.showConfirmDialog(getMainPanel(),res.getString("Osejs.ShouldOpenInJavascript"),res.getString("Warning"), JOptionPane.YES_NO_CANCEL_OPTION);
        progLang = OsejsCommon.PROGRAMMING_JAVASCRIPT;
      }
    }
    else if ("ejsh".equals(extension)) {
      if (mProgrammingLanguage!=PROGRAMMING_LANGUAGE.JAVA_PLUS_HTML) {
        option = JOptionPane.showConfirmDialog(getMainPanel(),res.getString("Osejs.ShouldOpenInJavaPlusHtml"),res.getString("Warning"), JOptionPane.YES_NO_CANCEL_OPTION);
        progLang = OsejsCommon.PROGRAMMING_JAVA_PLUS_HTML;
      }
    }
    else if ("ejs".equals(extension) || "xml".equals(extension)) {
      if (mProgrammingLanguage!=PROGRAMMING_LANGUAGE.JAVA) {
        option = JOptionPane.showConfirmDialog(getMainPanel(),res.getString("Osejs.ShouldOpenInJava"),res.getString("Warning"), JOptionPane.YES_NO_CANCEL_OPTION);
        progLang = OsejsCommon.PROGRAMMING_JAVA;
      }
    }
    switch (option) {
      default : 
      case JOptionPane.NO_OPTION : return true; 
      case JOptionPane.CANCEL_OPTION : return false; 
      case JOptionPane.YES_OPTION :
        runEjs(binDirectory.getParentFile(),getPathRelativeToSourceDirectory(file),progLang);
        return false;
    }
  }


  /**
   * Runs EJS and loads a given file
   * @return boolean
   */
  static public boolean runEjs(final File ejsRootDirectory, final String model, final String programmingLanguage) {
    // See if EjsConsole.jar is there
    if(!new File(ejsRootDirectory, "EjsConsole.jar").exists()) {    //$NON-NLS-1$
      return false;
    }
    // Now run the EJS console
    //final String theOptions = console_options;
    Runnable runner = new Runnable() {
      public void run() {
        try {
          final Vector<String> cmd = new Vector<String>();
          String javaHome = System.getProperty("java.home");                                      //$NON-NLS-1$
          if(javaHome!=null) {
            cmd.add(javaHome+java.io.File.separator+"bin"+java.io.File.separator+"java");         //$NON-NLS-1$ //$NON-NLS-2$
          } else {
            cmd.add("java");                                                                      //$NON-NLS-1$
          }
          //if (theOptions!=null) cmd.add("-Dejs.console_options="+theOptions);
          cmd.add("-jar");                                                                        //$NON-NLS-1$
          cmd.add("EjsConsole.jar");                                                              //$NON-NLS-1$
          cmd.add("-programmingLanguage");                                                                       //$NON-NLS-1$
          cmd.add(programmingLanguage);
          cmd.add("-file");                                                                       //$NON-NLS-1$
          cmd.add(model);
          String[] cmdarray = cmd.toArray(new String[0]);
          //          for (int i=0; i<cmdarray.length; i++) System.out.println ("Trying to run ["+i+"] = "+cmdarray[i]);
          Process proc = Runtime.getRuntime().exec(cmdarray, null, ejsRootDirectory);
          proc.waitFor();
        } catch(Exception exc) {
          exc.printStackTrace();
        }
      }

    };
    java.lang.Thread thread = new Thread(runner);
    thread.setPriority(Thread.NORM_PRIORITY);
    thread.start();
    return true;
  }


} // End of class

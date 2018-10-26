/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.opensourcephysics.display.*;
import org.opensourcephysics.tools.*;
import org.opensourcephysics.tools.minijar.PathAndFile;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import org.colos.ejs._EjsSConstants;
import org.colos.ejs.osejs.utils.*;

public class EjsConsole implements ConsoleServer {
  static private final int REGISTRY_PORT = 2008;
  static private final String ALWAYS = "ALWAYS";
  static private final String NEVER  = "NEVER";
  static private final String WEEK   = "WEEK";
  static private final String MONTH  = "MONTH";
  
  static private ResourceUtil res;
  static private ResourceUtil sysRes = new ResourceUtil("SystemResources");
  static private File optionsFile = new File (System.getProperty("user.home")+"/.EjsConsole.txt");
  static private long lastUpdateCheck = 0;
  static private String ignoreUpdateVersion="";
  static private boolean isMacOSX;
  
  static private boolean sRegistryDone = false;
  static private boolean sAlreadyRegistered = false;
  static private int sMAX_REGISTRY_ATTEMPTS = 4;
  static private boolean sVerbose = true;
  

  private JFrame frame;
  private JTabbedPane tabbedPanel;
  private JPanel outputPanel;
  private JCheckBox startMinimizedCB, externalCB, lastFileCB;
  private JComboBox<String> languageCombo, zoomCombo;
  private JComboBox<TwoStrings> programmingLanguageCombo;
  private JComboBox<NamedLookAndFeel> lookandfeelCombo;
  private JComboBox<TwoStrings> updateCombo;
  private JTextField javaRootField, userDirField, externalField, paramsField, argsField;
  private JTextArea outputArea;
  private final File initDir = new File(System.getProperty("user.dir")); // The initial directory
  private File userDir; // The desired user directory
  private String tmpDirPath=OsejsCommon.OUTPUT_DIR_PATH;
  
  private ProcessListDialog processDialog = null;
  private boolean justCompiling = false, rebuilding=false, compressAfterCompilation=false, zipAfterCompilation=false;
  private String compressPrefix = "model_", zipPrefix="src_";
  private java.util.List<String> filesToProcess=new ArrayList<String>();
  private java.util.List<TwoStrings> recompiledWhenRebuilding=null; // Model and class name of apps to recompile when rebuilding a jar
  private java.util.List<TwoStrings> toBeRemovedWhenRebuilding=null; // Model and class name of simulations in jar to remove when rebuilding
  private java.util.List<PathAndFile> toBeAddedWhenRebuilding=null; // File list of already compiled simulations to add when rebuilding a jar
  private File rebuildTmpDir=null, rebuildTarget=null;
  private ProgressDialog reportWindow = null;
  private Registry registry=null;

  
  static {
    try {
      isMacOSX = false;
      if (System.getProperty("os.name", "").toLowerCase().startsWith("mac")) {
        System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "EjsS Console");
        isMacOSX = true;
      }
    } catch(Exception exc) { } // Do nothing
  }

  static void registerMime() {
//    javax.jnlp.IntegrationService is = null;
//    try {
//        is = (javax.jnlp.IntegrationService) javax.jnlp.ServiceManager.lookup("javax.jnlp.IntegrationService");
//    } catch(Exception use){
//        use.printStackTrace();
//        return;
//    }
//
//    // associates the application with the specified mime-type and file extensions
//    String mime = "x-application/ejss";
//    String [] exts = {"ejs", "ejss"};
//    
//    boolean result = is.hasAssociation(mime, exts);
//    System.err.println ("IS associated before = "+result);
//
//    result = is.requestAssociation(mime, exts);
//
//    System.err.println ("Association after = "+result);        
  }
  
  static public void main(final String args[]) {
//    System.setProperty("Xdock:", "name=EJS Console");
    //System.getProperties().list(System.out);
   registerMime();
   
    final Dictionary<String,String> options = readOptions(optionsFile);
    
    String language = options.get("Language");
    System.err.println ("Locale = "+language);

    if (language.equals("Locale")) ; // do nothing
    else {
      Locale locale;
      if (language.endsWith("  ")) locale = new Locale(language.substring(0,2));
      else locale = new Locale(language.substring(0,2),language.substring(3,5));
      ResourceUtil.setLocale(locale);
      Locale.setDefault(locale);
      JComponent.setDefaultLocale(locale);
    }
    res = new ResourceUtil("Resources");

    NamedLookAndFeel laf = NamedLookAndFeel.getLookAndFeel(options.get("LookAndFeel"));
    org.opensourcephysics.display.OSPRuntime.setLookAndFeel(laf.isDecorateWindows(), laf.getClassname());

    try { lastUpdateCheck = Long.parseLong(options.get("LastUpdate")); }
    catch (Exception exc) { lastUpdateCheck = 0; }
    ignoreUpdateVersion = options.get("IgnoreUpdateVersion");


    // Only one EjsConsole should be running at a time
    boolean alreadyRegistered = false;
    boolean internetConnection = true;
//    TimedRMIClientSocketFactory csf=null;
//    try { // See if it is already running
//      csf = new TimedRMIClientSocketFactory(2000);
//      csf.createSocket("localhost", REGISTRY_PORT);
//      Registry registry = LocateRegistry.getRegistry("localhost", REGISTRY_PORT, csf);
//      
////      Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
//      ConsoleServer stub = (ConsoleServer) registry.lookup("EjsConsoleServer");
//      if (args.length>0) { // Pass the arguments along and quit
//        stub.processArgs(args);
//        System.exit(0);
//      }
//      int confirm = JOptionPane.showConfirmDialog((JFrame)null,res.getString("EjsConsole.AlreadyRunning"),
//          res.getString("Warning"),JOptionPane.YES_NO_OPTION);
//      if (confirm!=JOptionPane.YES_OPTION) System.exit(0);
//      alreadyRegistered = true;
//    }
//    catch (Exception exc) { 
//      exc.printStackTrace();
//      internetConnection = false;
//    } // Do nothing
    
    sRegistryDone = false;
    sAlreadyRegistered = false;
//    final String updateOption = options.get("Update");

//    Thread updateThread = new Thread() {
//      public void run() {
//        visitEjsSCounter(false); 
//        checkForUpdates(options.get("Update"),null);
//      }
//    };
//    updateThread.setPriority(Thread.MIN_PRIORITY);
//    updateThread.start();

//    SwingUtilities.invokeLater(new Runnable () {
//      public void run() { visitEjsSCounter(false); }
//    });
    Thread registryThread = new Thread() {
      public void run() {
        
        if (sVerbose) System.err.println ("Ok PreRegistry");

        try { // See if it is already running
          Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
          ConsoleServer stub = (ConsoleServer) registry.lookup("EjsConsoleServer");
          if (args.length>0) { // Pass the arguments along and quit
            stub.processArgs(args);
            System.exit(0);
          }
          sAlreadyRegistered = true;
          int confirm = JOptionPane.showConfirmDialog((JFrame)null,res.getString("EjsConsole.AlreadyRunning"),
              res.getString("Warning"),JOptionPane.YES_NO_OPTION);
          if (confirm!=JOptionPane.YES_OPTION) System.exit(0);
        }
        catch (Exception exc) { 
          if (sVerbose) exc.printStackTrace();
//          internetConnection = false;
        } // Do nothing
        sRegistryDone = true;
      }
      
    };
    
    registryThread.setPriority(Thread.NORM_PRIORITY);
    registryThread.start();

    int attempts = 1;
    while (attempts<=sMAX_REGISTRY_ATTEMPTS || sAlreadyRegistered) {
      try { Thread.sleep(1000); }
      catch(Exception exc) {}
      if (sVerbose) System.err.println("Waiting for connection "+attempts);
      if (sRegistryDone) {
        if (sVerbose) System.err.println("Registry done!");
        break;
      }
      attempts++;
    }
    if (!sRegistryDone) {
      if (sVerbose) System.err.println ("Killing registry thread!");
      registryThread.interrupt();
      internetConnection = false;
    }
    
    alreadyRegistered = sAlreadyRegistered;

    
    if (sVerbose) System.err.println ("Ok Registry");


//    EjsConsole ejsInstance=null;
//    if (ejsClass==null) ejsInstance = new EjsConsole(args,options);
//    else {
//      java.lang.reflect.Constructor<?>[] constructorList = ejsClass.getDeclaredConstructors();
//      for (int i=0; i<constructorList.length; i++) {
//        java.lang.reflect.Constructor<?> aCtror = constructorList[i];
//        System.err.println ("Ctror ["+i+"] = "+aCtror.getName());
//        Class<?>[] params = aCtror.getParameterTypes();
//        for (int j=0; j<params.length; j++) {
//          System.err.println ("Ctror ["+i+"] param["+j+"]= "+params[j].getName());
//        }
//      }
//      
//      try {
//        Class<?>[] __c = { String[].class, Dictionary.class };
//        Object[] __o = { args, options };
//        java.lang.reflect.Constructor<?> constructor = ejsClass.getDeclaredConstructor(__c);
//        System.err.println ("Constriuctor to use = "+constructor.getName());
//
//        ejsInstance = (EjsConsole) constructor.newInstance(__o);
//      } catch (Exception e) {
//        e.printStackTrace();
//        JOptionPane.showMessageDialog(null,"Unable to instantiate class "+ejsClass.getName(),"EJS Console error",JOptionPane.ERROR_MESSAGE);
//        System.exit(-1);
//      }
//      JOptionPane.showMessageDialog(null,"Able to instantiate class "+ejsClass.getName(),"EJS Console error",JOptionPane.ERROR_MESSAGE);
//    }

    final EjsConsole console = new EjsConsole(args,options);
    
    Thread updateThread = new Thread() {
      public void run() {
        visitEjsSCounter(true); 
        checkForUpdates(options.get("Update"),true, console.tabbedPanel);
      }
    };
    updateThread.setPriority(Thread.MIN_PRIORITY);
    updateThread.start();
    
    if (internetConnection && !alreadyRegistered) {
      try { // Start the registry
        console.registry = java.rmi.registry.LocateRegistry.createRegistry(REGISTRY_PORT);
        console.println(res.getString("EjsConsole.RmiIsStarted")+" "+REGISTRY_PORT);
      }
      catch (Exception e) {
        console.println("Warning: Can't start RMI registry at port: "+REGISTRY_PORT);
        e.printStackTrace();
      }
      try { // Register the console as the EjsConsoleServer
        ConsoleServer stub = (ConsoleServer) UnicastRemoteObject.exportObject(console, 0);
        console.registry.bind("EjsConsoleServer", stub);
        console.println(res.getString("EjsConsole.EjsIslistening")+" "+REGISTRY_PORT);
      } 
      catch (Exception e) {
        console.println("Warning: EjsConsole server can't listen at port: "+REGISTRY_PORT);
        e.printStackTrace();
      }
      if (sVerbose) System.err.println ("Ok  Own Registry");
    }
    else if (sVerbose) System.err.println ("Ok  But NO Own Registry");

    if (isMacOSX) {
      double javaVersion = OsejsCommon.getJavaVersion();
      try {
        Class<?> handlerClass = (javaVersion>=9.0) ? Class.forName("org.colos.ejs.osejs.macos.MacOSXHandlerJava9") : Class.forName("org.colos.ejs.osejs.macos.MacOSXHandler");
        Class<?>[] __c = { EjsConsole.class };
        Object[] __o = { console };
        java.lang.reflect.Constructor<?> constructor = handlerClass.getDeclaredConstructor(__c);
        constructor.newInstance(__o);
//        JOptionPane.showMessageDialog(null,"MacOSX handlers added","EJS message",JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,"Can't add MacOSX handlers:\n"+e.getMessage(),"EjsS Console error",JOptionPane.ERROR_MESSAGE);
      }
    }
    
  }
  
  private void saveOptions () {
    try {
      FileWriter fout = new FileWriter(optionsFile);
      fout.write("JavaRoot="+javaRootField.getText()+"\n");
      if (languageCombo.getSelectedIndex()==0) fout.write("Language=Locale\n");
      else fout.write("Language="+languageCombo.getSelectedItem()+"\n");
      fout.write("Update="+ ((TwoStrings) updateCombo.getSelectedItem()).getSecondString() +"\n");
      fout.write("LastUpdate="+ lastUpdateCheck +"\n");
      fout.write("IgnoreUpdateVersion="+ ignoreUpdateVersion +"\n");
      
      fout.write("Minimized="+startMinimizedCB.isSelected()+"\n");
      fout.write("ZoomLevel="+zoomCombo.getSelectedItem()+"\n");

      fout.write("UserDir="+userDirField.getText()+"\n");
      fout.write("ExternalApps="+externalCB.isSelected()+"\n");
      fout.write("LoadLastFile="+lastFileCB.isSelected()+"\n");
      fout.write("ProgrammingLanguage="+((TwoStrings) programmingLanguageCombo.getSelectedItem()).getSecondString()+"\n");
      fout.write("MatlabDir="+externalField.getText()+"\n");
      fout.write("VMparams="+paramsField.getText()+"\n");
      fout.write("Arguments="+argsField.getText()+"\n");
      fout.write("LookAndFeel="+((NamedLookAndFeel)lookandfeelCombo.getSelectedItem()).getName()+"\n");
      fout.write("Screen="+OsejsCommon.getScreenNumber(frame)+"\n");
      Dimension size = frame.getSize();
      fout.write("Width="+size.width+"\n");
      fout.write("Height="+size.height+"\n");
      fout.close();
    }
    catch (IOException ex) {
       ex.printStackTrace();
       JOptionPane.showMessageDialog(frame,FileUtils.getPath(optionsFile)+": "+res.getString("EjsConsole.UnreachableOptionsFile"),
                                     res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
       System.exit(2);
    }
  }
  
  static protected Dictionary<String,String> readOptions (File _file) {
    Dictionary<String,String> options = new Hashtable<String,String> ();
    options.put("JavaRoot", "");
    options.put("Language", "Locale");
    options.put("Update", EjsConsole.ALWAYS);
    options.put("LastUpdate", "0");
    options.put("IgnoreUpdateVersion", "");

    options.put("Minimized", "false");
    options.put("ZoomLevel", "0");
    
    options.put("UserDir", "");
    options.put("ExternalApps", "false");
    options.put("LoadLastFile", "false");
    options.put("ProgrammingLanguage", "JAVA");
    options.put("MatlabDir", "");
    options.put("VMparams", "-Xmx256m");
    options.put("Arguments", "");

    options.put("LookAndFeel", OSPRuntime.SYSTEM_LF); //CROSS_PLATFORM_LF);
    options.put("Screen", "0");
    options.put("Width", "");
    options.put("Height", "");
    
//    if (!_file.exists()) {
//      _file = new File(OsejsCommon.BIN_DIR_PATH+"/config/EjsConsole.txt");
//      //System.out.println ("Will read file: "+FileUtils.getPath(_file));
//    }

    if (_file.exists()) {
      try {
        Reader reader = new FileReader(_file);
        LineNumberReader l = new LineNumberReader(reader);
        String sl = l.readLine();
        while (sl != null) {
          readOptionLine (sl,options);
          sl = l.readLine();
        }
        reader.close();
      } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null,FileUtils.getPath(_file)+": "+res.getString("EjsConsole.UnreachableOptionsFile"),
            res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
      }
    }
    return options;
  }

  static private void readOptionLine (String _line, Dictionary<String,String> options) {
    if      (_line.startsWith("JavaRoot=")) options.put("JavaRoot", _line.substring(9));
    else if (_line.startsWith("Language=")) options.put("Language", _line.substring(9));
    else if (_line.startsWith("Update=")) options.put("Update", _line.substring(7));
    else if (_line.startsWith("LastUpdate=")) options.put("LastUpdate", _line.substring(11));
    else if (_line.startsWith("IgnoreUpdateVersion=")) options.put("IgnoreUpdateVersion", _line.substring(20));
    else if (_line.startsWith("Minimized=")) {
      if (_line.indexOf("true")>=0) options.put("Minimized","true");
    }
    else if (_line.startsWith("ZoomLevel=")) options.put("ZoomLevel", _line.substring(10));
    else if (_line.startsWith("UserDir="))  options.put("UserDir", _line.substring(8));
    else if (_line.startsWith("ProgrammingLanguage=")) options.put("ProgrammingLanguage", _line.substring(20));
    else if (_line.startsWith("ExternalApps=")){
      if (_line.indexOf("true")>=0) options.put("ExternalApps","true");
    }
    else if (_line.startsWith("LoadLastFile=")){
      if (_line.indexOf("true")>=0) options.put("LoadLastFile","true"); // It is false by default
    }
    else if (_line.startsWith("LookAndFeel=")) options.put("LookAndFeel", _line.substring(12));
    else if (_line.startsWith("MatlabDir="))   options.put("MatlabDir", _line.substring(10));
    else if (_line.startsWith("VMparams="))    options.put("VMparams", _line.substring(9));
    else if (_line.startsWith("Arguments="))   options.put("Arguments", _line.substring(10));
    else if (_line.startsWith("Screen="))      options.put("Screen", _line.substring(7));
    else if (_line.startsWith("Width="))       options.put("Width", _line.substring(6));
    else if (_line.startsWith("Height="))      options.put("Height", _line.substring(7));
  }

  // --------------------------------------------
  // Main constructor
  // --------------------------------------------

  public EjsConsole (String[] _args, Dictionary<String,String> _options) {
    // Borders and insets
    Border border1010 = new EmptyBorder(1,0,1,0);
    Border border0303 = new EmptyBorder(0,3,0,3);
    Insets insets0000 = new Insets(0,0,0,0);
    Insets insets0303 = new Insets(0,3,0,3);

    // The listener for actions
    MouseListener mouseListener = new MyMouseListener();
    
    // The dialog that shows the processes currently running
    processDialog = new ProcessListDialog(null);
    processDialog.setZoomLevel(FontSizer.getLevel());

    /*
    // Display the Operating System
    JLabel osLabel = new JLabel(res.getString("EjsConsole.OperatingSystem"),JLabel.LEFT);
    osLabel.setBorder(border0303);

    JTextField osField = new JTextField(System.getProperty("os.name", ""));
    osField.setEditable(false);

    JPanel osPanel = new JPanel (new BorderLayout());
    osPanel.add(osLabel,BorderLayout.WEST);
    osPanel.add(osField,BorderLayout.CENTER);

*/
    // Display the Java VM that started the console
    JLabel vmLabel = new JLabel(res.getString("EjsConsole.VirtualMachine"),SwingConstants.LEFT);
    vmLabel.setBorder(border0303);

    JTextField vmField = new JTextField(System.getProperty("java.home"),res.getInteger("EjsConsole.FieldWidth"));
    vmField.setEditable(false);

    JPanel vmPanel = new JPanel (new BorderLayout());
    vmPanel.add(vmLabel,BorderLayout.WEST);
    vmPanel.add(vmField,BorderLayout.CENTER);

    // User directory
    
    JLabel userDirLabel = new JLabel (res.getString("EjsConsole.WorkingDirectory"),SwingConstants.LEFT);
    userDirLabel.setBorder(border0303);
    
    userDirField = new JTextField(res.getInteger("EjsConsole.FieldWidth"));
    userDirField.setEditable(false);
    
    JButton userDirButton = new JButton(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    userDirButton.setMargin(insets0000);
    userDirButton.setActionCommand("userDir");
    userDirButton.addMouseListener (mouseListener);

    JPanel userDirPanel = new JPanel (new BorderLayout());
    userDirPanel.add(userDirLabel,BorderLayout.WEST);
    userDirPanel.add(userDirField,BorderLayout.CENTER);
    userDirPanel.add(userDirButton,BorderLayout.EAST);
    userDirPanel.setBorder(border1010);
    
    // Logo 
    JLabel logoLabel = new JLabel(ResourceLoader.getIcon(sysRes.getString("EjsConsole.EjsLogo")));
    logoLabel.setBorder(new EmptyBorder(7,10,0,10));
    logoLabel.setVerticalAlignment(SwingConstants.TOP);
    JLabel versionLabel = new JLabel("EjsS "+_EjsSConstants.VERSION,SwingConstants.CENTER);
    versionLabel.setVerticalAlignment(SwingConstants.TOP);
    versionLabel.setFont(versionLabel.getFont().deriveFont(12f));
    JLabel buildLabel = new JLabel("Build "+_EjsSConstants.VERSION_DATE,SwingConstants.CENTER);
    buildLabel.setVerticalAlignment(SwingConstants.TOP);
    buildLabel.setFont(versionLabel.getFont().deriveFont(10f));

    JPanel logoPanel = new JPanel(new BorderLayout());
    logoPanel.add(logoLabel,BorderLayout.NORTH);
    logoPanel.add(versionLabel,BorderLayout.CENTER);
    logoPanel.add(buildLabel,BorderLayout.SOUTH);

    // Load last file

    lastFileCB = new JCheckBox(res.getString("EjsConsole.LoadLastFile"),false);
    lastFileCB.setMargin(insets0303);

    // Default look and Feel
    JLabel lookandfeelLabel = new JLabel(res.getString("EjsConsole.LookAndFeel"),SwingConstants.LEFT);
    lookandfeelLabel.setBorder(border0303);

    lookandfeelCombo = new JComboBox<NamedLookAndFeel>();
    for (NamedLookAndFeel laf : NamedLookAndFeel.getInstalledLookAndFeels()) lookandfeelCombo.addItem(laf);
    lookandfeelCombo.setEditable(false);
    lookandfeelCombo.setSelectedIndex(lookandfeelCombo.getComponentCount()-1); // DEFAULT is the default

    JPanel lookandfeelPanel2 = new JPanel (new BorderLayout());
    lookandfeelPanel2.add(lookandfeelLabel,BorderLayout.WEST);
    lookandfeelPanel2.add(lookandfeelCombo,BorderLayout.CENTER);

    JPanel lookandfeelPanel = new JPanel (new BorderLayout());
    lookandfeelPanel.add(lookandfeelPanel2,BorderLayout.WEST);

    // Choosing the language

    JLabel languageLabel = new JLabel(res.getString("EjsConsole.Language"),SwingConstants.LEFT);
    languageLabel.setBorder(border0303);

    languageCombo = new JComboBox<String>();
    languageCombo.addItem(res.getString("EjsConsole.DefaultLanguage"));
    languageCombo.addItem("en   ");
    languageCombo.addItem("es   ");
//    languageCombo.addItem("ar   ");
    languageCombo.addItem("ca   ");
    languageCombo.addItem("da   ");
    languageCombo.addItem("de   ");
//    languageCombo.addItem("el   ");
    languageCombo.addItem("fr   ");
    languageCombo.addItem("id   ");
    languageCombo.addItem("nl   ");
    languageCombo.addItem("pl   ");
    languageCombo.addItem("ru   ");
    languageCombo.addItem("sl   ");
    languageCombo.addItem("zh CN");
    languageCombo.addItem("zh TW");
    languageCombo.setEditable(false);
    languageCombo.setSelectedIndex(0);

    JPanel languagePanel2 = new JPanel (new BorderLayout());
    languagePanel2.add(languageLabel,BorderLayout.WEST);
    languagePanel2.add(languageCombo,BorderLayout.CENTER);

    JPanel languagePanel = new JPanel (new BorderLayout());
    languagePanel.add(languagePanel2,BorderLayout.WEST);
    
    // Zoom level

    JLabel zoomLabel = new JLabel(res.getString("EjsConsole.ZoomLevel"),SwingConstants.LEFT);
    zoomLabel.setBorder(border0303);

    zoomCombo = new JComboBox<String>();
    zoomCombo.addItem("0");
    zoomCombo.addItem("+1");
    zoomCombo.addItem("+2");
    zoomCombo.addItem("+3");
    zoomCombo.addItem("+4");
    zoomCombo.addItem("+5");
    zoomCombo.addItem("+6");
    zoomCombo.addItem("+7");
    zoomCombo.addItem("+8");

    zoomCombo.setEditable(false);
    zoomCombo.setSelectedIndex(0);
    zoomCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object selected = zoomCombo.getSelectedItem();
        if (selected!=null) {
          int level = Integer.parseInt(selected.toString());
          FontSizer.setLevel(level);
        }
      }
    });
    FontSizer.addPropertyChangeListener("level", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          int level = ((Integer) e.getNewValue()).intValue();
          setZoomLevel(level);
      }
    });

    JPanel zoomPanel2 = new JPanel (new BorderLayout());
    zoomPanel2.add(zoomLabel,BorderLayout.WEST);
    zoomPanel2.add(zoomCombo,BorderLayout.CENTER);

    JPanel zoomPanel = new JPanel (new BorderLayout());
    zoomPanel.add(zoomPanel2,BorderLayout.WEST);
    
    // Minimize console
    
    startMinimizedCB = new JCheckBox(res.getString("Osejs.Minimized"),false);
    startMinimizedCB.setMargin(insets0303);

    // programming language 
    
    JLabel programmingLanguageLabel = new JLabel(res.getString("EjsConsole.ProgrammingLanguage"),SwingConstants.LEFT);
    programmingLanguageLabel.setBorder(border0303);

    programmingLanguageCombo = new JComboBox<TwoStrings>();
    programmingLanguageCombo.addItem(new TwoStrings("Java",       OsejsCommon.PROGRAMMING_JAVA));
    programmingLanguageCombo.addItem(new TwoStrings("Javascript", OsejsCommon.PROGRAMMING_JAVASCRIPT));
  //  programmingLanguageCombo.addItem(new TwoStrings("Java + Html",OsejsCommon.PROGRAMMING_JAVA_PLUS_HTML));
    programmingLanguageCombo.setSelectedIndex(0); // Java by default
    
    JPanel programmingLanguagePanel2 = new JPanel (new BorderLayout());
    programmingLanguagePanel2.add(programmingLanguageLabel,BorderLayout.WEST);
    programmingLanguagePanel2.add(programmingLanguageCombo,BorderLayout.CENTER);

    JPanel programmingLanguagePanel = new JPanel (new BorderLayout());
    programmingLanguagePanel.add(programmingLanguagePanel2,BorderLayout.WEST);

    // Check for updates
    
    JButton updateButton = new JButton(res.getString("EjsConsole.CheckUpdateNow"));
    updateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) { checkForUpdates(EjsConsole.ALWAYS, false, tabbedPanel); }
    });
    
    updateCombo = new JComboBox<TwoStrings>();
    // Important!!! If you ad entries or change the order, modify applyOptions accordingly! 
    updateCombo.addItem(new TwoStrings(res.getString("EjsConsole.CheckUpdateStartUp"),EjsConsole.ALWAYS));
    updateCombo.addItem(new TwoStrings(res.getString("EjsConsole.CheckUpdateNever"),EjsConsole.NEVER));
    updateCombo.addItem(new TwoStrings(res.getString("EjsConsole.CheckUpdateEveryWeek"),EjsConsole.WEEK));
    updateCombo.addItem(new TwoStrings(res.getString("EjsConsole.CheckUpdateEveryMonth"),EjsConsole.MONTH));
    updateCombo.setEditable(false);
    updateCombo.setSelectedIndex(0);
        
    JPanel updatePanel2 = new JPanel (new BorderLayout());
    updatePanel2.add(updateButton,BorderLayout.WEST);
    updatePanel2.add(updateCombo,BorderLayout.CENTER);

    JPanel updatePanel = new JPanel (new BorderLayout());
    updatePanel.add(updatePanel2,BorderLayout.WEST);
    
    // Put together this group of options
    
    JPanel optionsGridLeftPanel = new JPanel (new GridLayout(0,1));
    optionsGridLeftPanel.add(lastFileCB);
    optionsGridLeftPanel.add(startMinimizedCB);
    optionsGridLeftPanel.add(new JLabel());

    JPanel optionsGridRightPanel = new JPanel (new GridLayout(0,1));
    optionsGridRightPanel.add(zoomPanel);
    optionsGridRightPanel.add(languagePanel);
    optionsGridRightPanel.add(lookandfeelPanel);

//    optionsGridRightPanel.add(programmingLanguagePanel);
//    optionsGridRightPanel.add(updateCombo);
    optionsGridRightPanel.setBorder(border0303);

    JPanel optionsGridBothPanel = new JPanel (new BorderLayout());
    optionsGridBothPanel.add(optionsGridRightPanel,BorderLayout.WEST);
    optionsGridBothPanel.add(optionsGridLeftPanel,BorderLayout.CENTER);
    
    programmingLanguagePanel.setBorder(new EmptyBorder(0,3,0,3));
    optionsGridBothPanel.add(programmingLanguagePanel,BorderLayout.SOUTH);

    JPanel optionsGridPanel = new JPanel (new BorderLayout());
    optionsGridPanel.add(optionsGridBothPanel,BorderLayout.WEST);
    
    JPanel optionsPanel = new JPanel (new BorderLayout());
    optionsPanel.add(optionsGridPanel,BorderLayout.NORTH);
    optionsPanel.setBorder(new EmptyBorder(3,3,0,3));

    // Launch Ejs

    JButton doitButton = new JButton (res.getString("EjsConsole.DoIt"));
    doitButton.setActionCommand("doit");
    doitButton.addMouseListener (mouseListener);

//    JButton launchJavaButton = new JButton (res.getString("EjsConsole.LaunchJava"));
//    launchJavaButton.setActionCommand("launchJava");
//    launchJavaButton.addMouseListener (mouseListener);
//
//    JButton launchJavascriptButton = new JButton (res.getString("EjsConsole.LaunchJavascript"));
//    launchJavascriptButton.setActionCommand("launchJavascript");
//    launchJavascriptButton.addMouseListener (mouseListener);

    JPanel doitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    doitPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
    doitPanel.add(doitButton);
//    doitPanel.add(launchJavaButton);
//    doitPanel.add(launchJavascriptButton);

    // All together now!

    Box setBox = Box.createVerticalBox();
    setBox.add(optionsPanel,BorderLayout.CENTER);

    JPanel setPanel = new JPanel(new BorderLayout());
    setPanel.add(logoPanel,BorderLayout.WEST);
    setPanel.add(setBox,BorderLayout.CENTER);

    Box basicPanel = Box.createVerticalBox();
    //basicPanel.add(osPanel);
    basicPanel.add(vmPanel);
    basicPanel.add(userDirPanel);
    basicPanel.add(setPanel);

    JPanel firstPanel = new JPanel(new BorderLayout());
    firstPanel.add(basicPanel,BorderLayout.NORTH);
    firstPanel.add(doitPanel,BorderLayout.SOUTH);

    // ---------------- Advanced ------------------

    // External applications
    externalCB = new JCheckBox(res.getString("EjsConsole.ExternalApplications"),false);
    externalCB.setMargin(insets0303);

    JPanel externalPanel = new JPanel (new BorderLayout());
    externalPanel.add(externalCB,BorderLayout.WEST);

    // VM parameters
    
    JLabel paramsLabel = new JLabel(res.getString("EjsConsole.VMparameters"),SwingConstants.CENTER);
    paramsLabel.setBorder(border0303);
    paramsField = new JTextField("-Xmx256m",res.getInteger("EjsConsole.FieldWidth"));

    JPanel paramsPanel = new JPanel (new BorderLayout());
    paramsPanel.add(paramsLabel,BorderLayout.WEST);
    paramsPanel.add(paramsField,BorderLayout.CENTER);
    paramsPanel.setBorder(border1010);

    // Java virtual machine
    
    JLabel javaRootLabel = new JLabel (res.getString("EjsConsole.JavaRoot"),SwingConstants.CENTER);
    javaRootLabel.setBorder(border0303);
    javaRootField = new JTextField(res.getInteger("EjsConsole.FieldWidth"));
    javaRootField.setActionCommand("javarootfield");
    javaRootField.addActionListener (new ActionListener() {
      public void actionPerformed(ActionEvent evt) { isJavaRootCorrect(); }
    });
    JButton javaRootButton = new JButton(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    javaRootButton.setMargin(insets0303);
    javaRootButton.setActionCommand("javaroot");
    javaRootButton.addMouseListener (mouseListener);

    JPanel javaRootPanel = new JPanel (new BorderLayout());
    javaRootPanel.add(javaRootLabel,BorderLayout.WEST);
    javaRootPanel.add(javaRootField,BorderLayout.CENTER);
    javaRootPanel.add(javaRootButton,BorderLayout.EAST);

    // Additional arguments for Ejs
    JLabel argsLabel = new JLabel(res.getString("EjsConsole.Arguments"),SwingConstants.CENTER);
    argsLabel.setBorder(border0303);
    argsField = new JTextField("",res.getInteger("EjsConsole.FieldWidth"));

    JPanel argsPanel = new JPanel (new BorderLayout());
    argsPanel.add(argsLabel,BorderLayout.WEST);
    argsPanel.add(argsField,BorderLayout.CENTER);
    argsPanel.setBorder(border1010);


    // Matlab directory
    
    JLabel externalLabel = new JLabel(res.getString("EjsConsole.MatlabDirectory"),SwingConstants.CENTER);
    externalLabel.setBorder(border0303);
    externalField = new JTextField("",res.getInteger("EjsConsole.FieldWidth"));
    JButton externalButton = new JButton(ResourceLoader.getIcon(sysRes.getString("EjsOptions.Edit.Icon")));
    externalButton.setMargin(insets0303);
    externalButton.setActionCommand("external");
    externalButton.addMouseListener (mouseListener);

    JPanel externalDirPanel = new JPanel (new BorderLayout());
    externalDirPanel.add(externalLabel,BorderLayout.WEST);
    externalDirPanel.add(externalField,BorderLayout.CENTER);
    externalDirPanel.add(externalButton,BorderLayout.EAST);

    // Do it! again
    JButton doitButton3 = new JButton (res.getString("Osejs.Run"));
    doitButton3.setActionCommand("doit");
    doitButton3.addMouseListener (mouseListener);

    // Launcher
    JButton launcherButton = new JButton (res.getString("EjsConsole.LaunchBuilder"));
    launcherButton.setActionCommand("launchBuilder");
    launcherButton.addMouseListener (mouseListener);

    // Rebuild Launcher package
    JButton rebuildButton = new JButton (res.getString("EjsConsole.RebuildPackage"));
    rebuildButton.setActionCommand("rebuildPackage");
    rebuildButton.addMouseListener (mouseListener);

    // Compile a complete directory
    JButton compileDirButton = new JButton (res.getString("EjsConsole.CompileDirectory"));
    compileDirButton.setActionCommand("compileDirectory");
    compileDirButton.addMouseListener (mouseListener);

    
    // All together now
    
    Box advancedPanel = Box.createVerticalBox();
    //advancedPanel.add(userDirPanel);
    advancedPanel.add(javaRootPanel);
    advancedPanel.add(paramsPanel);
    advancedPanel.add(argsPanel);
    advancedPanel.add(updatePanel);

//    advancedPanel.add(programmingLanguagePanel);
//    advancedPanel.add(externalPanel);
//    advancedPanel.add(externalDirPanel);

    JPanel advancedButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    advancedButtonsPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
    advancedButtonsPanel.add(doitButton3);
    advancedButtonsPanel.add(compileDirButton);
    advancedButtonsPanel.add(launcherButton);
    advancedButtonsPanel.add(rebuildButton);

    JPanel secondPanel = new JPanel(new BorderLayout());
    secondPanel.add(advancedPanel,BorderLayout.NORTH);
    secondPanel.add(advancedButtonsPanel,BorderLayout.SOUTH);

    // Output area
    outputArea = new JTextArea();
    outputArea.setLineWrap(false);
    outputArea.setWrapStyleWord(true);
    JScrollPane outputScroll = new JScrollPane(outputArea);
    outputScroll.setPreferredSize (res.getDimension("EjsConsole.OutputAreaSize"));

    JButton outputButton = new JButton (res.getString("EjsConsole.ClearArea"));
    outputButton.setActionCommand ("clear");
    outputButton.addMouseListener (mouseListener);

    // Do it! again
    JButton doitButton2 = new JButton (res.getString("Osejs.Run"));
    doitButton2.setActionCommand("doit");
    doitButton2.addMouseListener (mouseListener);

//    JButton launchJavaButton2 = new JButton (res.getString("EjsConsole.LaunchJava"));
//    launchJavaButton2.setActionCommand("launchJava");
//    launchJavaButton2.addMouseListener (mouseListener);
//
//    JButton launchJavascriptButton2 = new JButton (res.getString("EjsConsole.LaunchJavascript"));
//    launchJavascriptButton2.setActionCommand("launchJavascript");
//    launchJavascriptButton2.addMouseListener (mouseListener);

    // Show Processes
    
    JButton processesButton = new JButton (res.getString("EjsConsole.ShowProcesses"));
    processesButton.setActionCommand("showProcesses");
    processesButton.addMouseListener (mouseListener);

    JPanel outputButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    outputButtonsPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
    outputButtonsPanel.add(doitButton2);
//    outputButtonsPanel.add(launchJavaButton2);
//    outputButtonsPanel.add(launchJavascriptButton2);
    outputButtonsPanel.add(outputButton);
    outputButtonsPanel.add(processesButton);

    outputPanel = new JPanel (new BorderLayout());
    outputPanel.add(outputScroll,BorderLayout.CENTER);
    outputPanel.add(outputButtonsPanel,BorderLayout.SOUTH);

    tabbedPanel = new JTabbedPane(SwingConstants.TOP);
    tabbedPanel.add(res.getString("EjsConsole.BasicOptions"),firstPanel);
    tabbedPanel.add(res.getString("EjsConsole.AdvancedOptions"),secondPanel);
    tabbedPanel.add(res.getString("EjsConsole.OutputArea"),outputPanel);

    frame = new JFrame(res.getString("EjsConsole.Title")+" "+_EjsSConstants.VERSION);
    frame.setIconImage(ResourceLoader.getImage(sysRes.getString("Osejs.Icon.ConsoleIcon")));
    frame.addWindowListener (new WindowAdapter() {
      public void windowClosing (WindowEvent evt) {
        quit();
//        checkExit();
//        JOptionPane.showMessageDialog(frame, res.getString("EjsConsole.CantCloseIt"),res.getString("Warning"),
//            JOptionPane.WARNING_MESSAGE,ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.ConsoleIcon")));
      }
    });
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(tabbedPanel);
    frame.pack();
    applyOptions(_options);
    if (startMinimizedCB.isSelected()) frame.setExtendedState(Frame.ICONIFIED);

    int defaultScreen = 0;
    try { defaultScreen = Integer.parseInt(_options.get("Screen")); }
    catch (Exception exc) { defaultScreen = 0; }
    Rectangle bounds = OsejsCommon.getScreenBounds(defaultScreen);
//    Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    int h = bounds.y + bounds.height - frame.getSize().height;
//    System.out.println ("IS windows="+OSPRuntime.isWindows());
//    System.out.println ("bounds x = "+bounds.x);
    if (OSPRuntime.isWindows() && bounds.x==0) h -= 30;
    frame.setLocation(bounds.x, h);
    frame.setVisible(true);

    try {
      SwingUtilities.invokeAndWait(new Runnable(){
        public void run () {
          userDir = new File (userDirField.getText());
          if (userDir.exists() && userDir.isDirectory() && FileUtils.isWritable(userDir)) return;
          else userDir = selectWorkspace();
          if (userDir==null) {
            JOptionPane.showMessageDialog(frame,res.getString("EjsConsole.NeedUserDir"),
                res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
            System.exit(1);
          }
        }
      });
    }
    catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(tabbedPanel,res.getString("EjsConsole.NeedUserDir"),
          res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
      System.exit(4);
    }
    
    if (isJavaRootCorrect()) {
      if (_args.length>0) {
        try { processArgs(_args); }
        catch (Exception exc) {
          System.out.println ("Error processing arguments!");
          exc.printStackTrace();
        }
      }
      else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { runEjs(null); } // null = do not compile
        });
      }
    }
    tabbedPanel.setSelectedComponent(outputPanel);
    
  }
  
  private void setZoomLevel(int level) {
    FontSizer.setFonts(frame, level);
    zoomCombo.validate();
    frame.pack();
    tabbedPanel.setSize(tabbedPanel.getSize());
    if (processDialog!=null) processDialog.setZoomLevel(level);    
    if (reportWindow!=null) reportWindow.setZoomLevel(FontSizer.getLevel());
  }
  
  /*
  private void processCombos() {
    {
      int n = languageCombo.getSelectedIndex();
      String[] items = new String[languageCombo.getItemCount()];
      for (int i=0; i<items.length; i++) items[i] = languageCombo.getItemAt(i);
      DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(items);
      languageCombo.setModel(model);
      languageCombo.setSelectedIndex(n);
    }
    {
      int n = zoomCombo.getSelectedIndex();
      String[] items = new String[zoomCombo.getItemCount()];
      for (int i=0; i<items.length; i++) items[i] = zoomCombo.getItemAt(i);
      DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(items);
      zoomCombo.setModel(model);
      zoomCombo.setSelectedIndex(n);
    }

  }
  
  */
  public void quit() {
    checkExit();
    JOptionPane.showMessageDialog(frame, res.getString("EjsConsole.CantCloseIt"),res.getString("Warning"),
        JOptionPane.WARNING_MESSAGE,ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.ConsoleIcon")));
  }
  
  // --------------------------------------------
  // Reading and writing options
  // --------------------------------------------
  
  /**
   * Select the current workspace.
   */
  private File selectWorkspace () {

    JFileChooser chooser=OSPRuntime.createChooser("",new String[0]);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle(res.getString("EjsConsole.ChooseWorkingDirectory"));
    
    {
      File wsDir = new File(initDir,OsejsCommon.USER_DIR_PATH);
      if (userDir!=null && userDir.exists()) chooser.setSelectedFile(userDir);
      else chooser.setSelectedFile(wsDir);
    }
    
    JTextArea textArea = new JTextArea();
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setText(res.getString("EjsConsole.ExplainWorkspaceConcept"));
    textArea.setPreferredSize(res.getDimension("EjsConsole.WorkspaceDescription"));
    textArea.setFont(InterfaceUtils.font(null,res.getString("EjsConsole.WorkspaceDescriptionFont")));
    textArea.setBorder(new EmptyBorder(3,3,3,3));
    
    JCheckBox checkBox = new JCheckBox(res.getString("EjsConsole.CopyStandardWorkspace"),true);
    JLabel label = new JLabel(res.getString("EjsConsole.OnlyNewWorkspaces"),SwingConstants.CENTER);
    
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(checkBox,BorderLayout.CENTER);
    bottomPanel.add(label,BorderLayout.SOUTH);
   
    JPanel accPanel = new JPanel(new BorderLayout());
    accPanel.add(textArea,BorderLayout.CENTER);
    accPanel.add(bottomPanel,BorderLayout.SOUTH);
    
    chooser.setAccessory(accPanel);
   
    while (true) {
      int option = chooser.showOpenDialog(null);
      if (option != JFileChooser.APPROVE_OPTION) return null; // nothing selected
      
      File dirChosen = chooser.getSelectedFile();
      if (! (dirChosen.exists() && dirChosen.isDirectory() && FileUtils.isWritable(dirChosen) ) ) {
        JOptionPane.showMessageDialog(frame,res.getString("EjsConsole.NeedWritableDir"),
            res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE); 
      }
      else { // a reasonable folder was selected
        boolean acceptable = true;
        // Check for the common mistake to select a source folder of an existing workspace
        if ((new File(dirChosen.getParent(),OsejsCommon.CONFIG_DIR_PATH)).exists() && 
            (new File(dirChosen.getParent(),OsejsCommon.EXPORT_DIR_PATH)).exists() && 
            (new File(dirChosen.getParent(),OsejsCommon.OUTPUT_DIR_PATH)).exists() && 
            (new File(dirChosen.getParent(),OsejsCommon.SOURCE_DIR_PATH)).exists()) {
          int confirmation = JOptionPane.showConfirmDialog(frame,res.getString("EjsConsole.ParentSeemsWorkspace"),
              res.getString("Warning"),JOptionPane.YES_NO_OPTION);
          acceptable =  (confirmation==JOptionPane.YES_OPTION);
        }
        if (acceptable) {
          userDirField.setText(FileUtils.getPath(dirChosen));
          File checkFolder = new File(dirChosen,OsejsCommon.CONFIG_DIR_PATH);
          if (!checkFolder.exists()) { 
            (new File(dirChosen,OsejsCommon.OUTPUT_DIR_PATH)).mkdirs();
            (new File(dirChosen,OsejsCommon.SOURCE_DIR_PATH)).mkdirs();
            (new File(dirChosen,OsejsCommon.CONFIG_DIR_PATH)).mkdirs();
            (new File(dirChosen,OsejsCommon.EXPORT_DIR_PATH)).mkdirs();
            if (checkBox.isSelected()) copyDistributionWorkspace (dirChosen); // Copy the distribution workspace to the new workspace
          }
          return dirChosen;
        }
      }
    }
  }

  /**
   * Copy all files in the source directory to the destination directory
   */
  private void copyDistributionWorkspace (File _destDir) {
    File wsDir = new File(initDir,OsejsCommon.USER_DIR_PATH);

    File configFolder = new File(wsDir,OsejsCommon.CONFIG_DIR_PATH);
    Collection<File> filesToCopy = JarTool.getContents(configFolder);

    {
      File javaExamplesFolder = new File(wsDir,OsejsCommon.SOURCE_DIR_PATH+"/JavaExamples");
      if (javaExamplesFolder.exists()) filesToCopy.addAll(JarTool.getContents(javaExamplesFolder));
      File javascriptExamplesFolder = new File(wsDir,OsejsCommon.SOURCE_DIR_PATH+"/JavascriptExamples");
      if (javascriptExamplesFolder.exists()) filesToCopy.addAll(JarTool.getContents(javascriptExamplesFolder));
    }

    boolean noToAll=false, yesToAll=false;
    ResourceUtil jarRes = null;
    Object[] ynOptions = null;
    String basePath = FileUtils.getPath(wsDir);
    
    for (File source : filesToCopy) {
      if (isMacOSX && source.getName().equals(".DS_Store")) continue;
      String filename = FileUtils.getRelativePath(source, basePath, false); // false = no resource
      System.out.println("Copying file "+filename);
      File target = new File (_destDir,filename);
      boolean doIt = true;
      if (target.exists()) {
        if (noToAll) doIt = false;
        else if (yesToAll) doIt = true;
        else { // ask
          if (jarRes==null) {
            jarRes = new ResourceUtil("org.opensourcephysics.resources.tools.tools");
            ynOptions = new Object[]{ jarRes.getString("JarTool.Yes"), jarRes.getString("JarTool.YesToAll"), 
                                      jarRes.getString("JarTool.No"),  jarRes.getString("JarTool.NoToAll")  };
          }
          int option = JOptionPane.showOptionDialog(frame,  
              filename + " : "+ res.getString("Osejs.File.Overwrite"), 
              res.getString("Osejs.File.FileExists"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ynOptions, ynOptions[0]);
          switch (option) {
            case 0 : doIt = true; break; 
            case 1 : doIt = true; yesToAll = true; break; 
            case 2 : doIt = false; break; // Do not copy it
            case 3 : doIt = false; noToAll = true; break;
          }
        }
      }
      if (doIt) {
        if (!JarTool.copy (source, target)) JOptionPane.showMessageDialog(frame, 
              res.getString("Osejs.File.Error")+" "+filename, res.getString("Osejs.File.SavingError"), JOptionPane.INFORMATION_MESSAGE);
      }
    }
    //pd.dispose();
  }

  private void applyOptions (Dictionary<String,String> options) {
    javaRootField.setText(options.get("JavaRoot"));
    String locale = options.get("Language");
    if (locale.equals("Locale")) languageCombo.setSelectedIndex(0);
    else languageCombo.setSelectedItem(locale);

    {
      String zoomLevel = options.get("ZoomLevel");
      if (zoomLevel!=null) {
        zoomCombo.setSelectedItem(zoomLevel);
        try {
          int level = Integer.parseInt(zoomLevel);
          FontSizer.setLevel(level);
        }
        catch (Exception exc) {
          exc.printStackTrace();
        }
      }
    }
    
    String programmingLanguage = options.get("ProgrammingLanguage");
    if      (OsejsCommon.PROGRAMMING_JAVASCRIPT.equals(programmingLanguage))     programmingLanguageCombo.setSelectedIndex(1);
    else if (OsejsCommon.PROGRAMMING_JAVA_PLUS_HTML.equals(programmingLanguage)) programmingLanguageCombo.setSelectedIndex(2);
    else programmingLanguageCombo.setSelectedIndex(0); // JAVA

    String updateOption = options.get("Update");
    if      (updateOption.equals(EjsConsole.NEVER)) updateCombo.setSelectedIndex(1);
    else if (updateOption.equals(EjsConsole.WEEK))   updateCombo.setSelectedIndex(2);
    else if (updateOption.equals(EjsConsole.MONTH))  updateCombo.setSelectedIndex(3);
    else updateCombo.setSelectedIndex(0); // ALWAYS

    if (options.get("Minimized").equals("true")) startMinimizedCB.setSelected(true);
    String path = options.get("UserDir");
    if (path.length()>0) userDirField.setText(path);
    if (options.get("ExternalApps").equals("true")) externalCB.setSelected(true);
    if (options.get("LoadLastFile").equals("true")) lastFileCB.setSelected(true);
    lookandfeelCombo.setSelectedItem(NamedLookAndFeel.getLookAndFeel(options.get("LookAndFeel")));
    externalField.setText(options.get("MatlabDir"));
    paramsField.setText(options.get("VMparams"));
    argsField.setText(options.get("Arguments"));
    try {
      int width = Integer.parseInt(options.get("Width"));
      int height = Integer.parseInt(options.get("Height"));
      int screen;
      try { screen = Integer.parseInt(options.get("Screen")); }
      catch (Exception exc) { screen = 0; }
      Rectangle bounds = OsejsCommon.getScreenBounds(screen);
      frame.setSize(Math.min(bounds.width-10, width),Math.min(bounds.height-10, height));
    }
    catch (Exception exc) {} // do not complain
  }


  // --------------------------------------------
  // Run EJS
  // --------------------------------------------

  /**
   * Run EJS without a password
   * @param _filename
   */
  public void runEjs (String _filename) { runEjs(_filename,null); }

  private void runEjs (String _filename, String _password) {
    runEjs(_filename,_password,null);
  }
  
  private void runEjs (String _filename, String _password, String _programmingLanguage) {
    //if (_filename!=null) System.out.println("EJS opening file: "+_filename);
    if (!isJavaRootCorrect()) return;
    String javaRoot = javaRootField.getText().trim();
    String ejsDir;
    try { ejsDir = userDir.getCanonicalPath(); }
    catch (Exception exc) { ejsDir = userDir.getAbsolutePath(); }
    
    String extraDefines="", JavaLib = "", EjsLib = "", javaHome = "";

    if (javaRoot.length()>0) javaHome = javaRoot; // Use a particular JAVA
    else javaHome = System.getProperty("java.home"); // Use the default Java
//    System.err.println ("Java home = "+javaHome);
//    System.err.println ("Java root = "+javaRoot);
    // Find whether it is a SDK or a RTE
    File toolsFile = new File(javaHome + "/lib/tools.jar");
    if (toolsFile.exists()) { // It is a SDK
      JavaLib += javaHome + "/lib/tools.jar" + File.pathSeparator +
                 javaHome + "/jre/lib/rt.jar";
    }
    else { // It is a JRE
      switch (OsejsCommon.getJavaVersion(javaRoot)) {
        case 5  : EjsLib += OsejsCommon.BIN_DIR_PATH+"/comSun5.jar" + File.pathSeparator; break; // Use version 5 compiler
        case 6  : EjsLib += OsejsCommon.BIN_DIR_PATH+"/comSun6.jar" + File.pathSeparator; break; // Use version 5 compiler
        case 7  : EjsLib += OsejsCommon.BIN_DIR_PATH+"/comSun7.jar" + File.pathSeparator; break; // Use version 7 compiler
        case 8  : EjsLib += OsejsCommon.BIN_DIR_PATH+"/comSun8.jar" + File.pathSeparator; break; // Use version 8 compiler
        default : EjsLib += OsejsCommon.BIN_DIR_PATH+"/comSun8.jar" + File.pathSeparator; break; // Use version 8 compiler
      }
    }
    // Common stuff
    EjsLib += OsejsCommon.BIN_DIR_PATH+"/ejs.jar"  + File.pathSeparator
              + javaHome + "/lib/ext/jfxrt.jar" + File.pathSeparator
              + javaHome + "/lib/jfxrt.jar";
//              OsejsCommon.BIN_DIR_PATH + "/jfxrt1.7.jar";
    
    if (true) { // NOT ONLY Under Mac OSX
      double javaVersion = OsejsCommon.getJavaVersion();
      EjsLib += File.pathSeparator + OsejsCommon.BIN_DIR_PATH + ( (javaVersion>=9.0) ? "/ejs_extras_java9.jar" : "/ejs_extras_java8.jar");
    }
    
    File javascriptDir = new File(OsejsCommon.BIN_DIR_PATH+"/javascript/");
    for (File file : JarTool.getContents(javascriptDir)) { // All JARs in bin/javascript
      String filename = file.getName().toLowerCase();
      if (filename.endsWith(".jar")) {
        String relPath = FileUtils.getRelativePath(file, javascriptDir, false);
        EjsLib += File.pathSeparator + OsejsCommon.BIN_DIR_PATH+"/javascript/" + relPath;
      }
    }
    EjsLib += File.pathSeparator + OsejsCommon.BIN_DIR_PATH+"/bcel.jar";
//    EjsLib += File.pathSeparator +"jce.jar";
      
    // Options
    
    // Add the library files in bin/extension
    String extPrefix = OsejsCommon.BIN_DIR_PATH+"/"+OsejsCommon.EXTENSIONS_DIR_PATH;
    for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (initDir,extPrefix))) EjsLib += File.pathSeparator + extPrefix+"/"+paf.getPath();
    
    String EjsOptions = getLanguageLocale();
    if (externalCB.isSelected()) EjsOptions += " -externalApps";

    if (_programmingLanguage==null) {
      if (_filename==null) _programmingLanguage = ((TwoStrings) programmingLanguageCombo.getSelectedItem()).getSecondString();
      else if (_filename.endsWith(".ejss")) _programmingLanguage = OsejsCommon.PROGRAMMING_JAVASCRIPT;
      else if (_filename.endsWith(".ejsh")) _programmingLanguage = OsejsCommon.PROGRAMMING_JAVA_PLUS_HTML;
      else _programmingLanguage = OsejsCommon.PROGRAMMING_JAVA;
    }

//    if (programmingLanguage!=null) EjsOptions += " -programming_language " + programmingLanguage;
//    else EjsOptions += " -programming_language " + ((TwoStrings) programmingLanguageCombo.getSelectedItem()).getSecondString();

    EjsOptions += " -programming_language " + _programmingLanguage;
    EjsOptions += " -zoom_level " + zoomCombo.getSelectedItem();
    
    if (isMacOSX) { // Under Mac OSX
      JavaLib = "";
      String externalText = externalField.getText().trim();
      if (externalText.length()>0) {
        extraDefines=" -DEjs.MatlabCmd=\"open -a X11;"+externalText+"/bin/matlab -display :0.0\" ";
        extraDefines+=" -Djava.library.path=\""+ejsDir+"/_library/external:"+externalText+"/bin/mac:"+externalText+"/sys/os/mac\" ";
      }
//      extraDefines += " -Djava.library.path=/System/Library/Frameworks/JavaVM.framework/Libraries"; //  DYLD_LIBRARY_PATH=
    }
    else if (OSPRuntime.isLinux()) { // Under Linux
      String externalText = externalField.getText().trim();
      if (externalText.length()>0) {
        extraDefines=" -DEjs.MatlabCmd=\"open -a X11;"+externalText+"/bin/matlab -display :0.0\" ";
        extraDefines+=" -Djava.library.path=\""+ejsDir+"/_library/external:"+externalText+"/bin/mac:"+externalText+"/sys/os/mac\" ";
      }
    }
    StringBuffer EjsLibraries = new StringBuffer();
    if (JavaLib.length()>0) EjsLibraries.append(JavaLib + File.pathSeparator);
    EjsLibraries.append(EjsLib + File.pathSeparator + getLocaleClasspath());
    
//    String J3DPath = "/Users/Paco/j3d";
//    EjsLibraries.append(File.pathSeparator + J3DPath + "/lib/ext/j3dcore.jar");
//    EjsLibraries.append(File.pathSeparator + J3DPath + "/lib/ext/j3dutils.jar");
//    EjsLibraries.append(File.pathSeparator + J3DPath + "/lib/ext/vecmath.jar");
//    EjsLibraries.append(File.pathSeparator + J3DPath + "/jogl/jogl.jar");
//    EjsLibraries.append(File.pathSeparator + J3DPath + "/jogl/gluegen-rt.jar");
    

    Vector<String> command = new Vector<String>();
    if (javaHome != null) command.add(javaHome + "/bin/java");
    else command.add("java");
    String parameters = paramsField.getText();
    if (parameters.length()>0) {
      StringTokenizer tkn = new StringTokenizer(parameters," ");
      while (tkn.hasMoreTokens()) command.add(tkn.nextToken());
    }
    // % setenv DYLD_LIBRARY_PATH /System/Library/Frameworks/JavaVM.framework/Libraries 
//    command.add("-Dapple.awt.graphics.UseQuartz=true");
    command.add("-classpath");
    command.add(EjsLibraries.toString());
    command.add("-Dcodebase=.");
    command.add("-Dhome=" + ejsDir);
    if (isMacOSX) {
      command.add("-Xdock:name=Easy Java Simulations");
      command.add("-Xdock:icon=bin/icons/EjsIcon.gif");
    }
    if (_password!=null) {
      command.add("-Dlauncher.password="+_password);
    }
    //command.add("-Dejs.console_options="+FileUtils.getPath(optionsFile));
    if (extraDefines.length()>0) {
      StringTokenizer tkn = new StringTokenizer(extraDefines," ");
      while (tkn.hasMoreTokens()) command.add(tkn.nextToken());
    }
    command.add("org.colos.ejs.osejs.Osejs");
    if (EjsOptions.length()>0) {
      StringTokenizer tkn = new StringTokenizer(EjsOptions," ");
      while (tkn.hasMoreTokens()) command.add(tkn.nextToken());
    }
    if (!tmpDirPath.equals(OsejsCommon.OUTPUT_DIR_PATH)) { command.add("-outputDir"); command.add(tmpDirPath); }
    if (_filename!=null) {
      if (justCompiling) {
        if (compressAfterCompilation) {
          command.add("-prefix"); 
          if (OsejsCommon.PROGRAMMING_JAVA.equals(_programmingLanguage)) command.add("ejs_"+compressPrefix); 
          else if (OsejsCommon.PROGRAMMING_JAVASCRIPT.equals(_programmingLanguage)) command.add("ejss_"+compressPrefix); 
          else if (OsejsCommon.PROGRAMMING_JAVA_PLUS_HTML.equals(_programmingLanguage)) command.add("ejsh_"+compressPrefix); 
          command.add("-jar");
        }
        if (zipAfterCompilation) {
          command.add("-zip_prefix");
          if (OsejsCommon.PROGRAMMING_JAVA.equals(_programmingLanguage)) command.add("ejs_"+zipPrefix); 
          else if (OsejsCommon.PROGRAMMING_JAVASCRIPT.equals(_programmingLanguage)) command.add("ejss_"+zipPrefix); 
          else if (OsejsCommon.PROGRAMMING_JAVA_PLUS_HTML.equals(_programmingLanguage)) command.add("ejsh_"+zipPrefix); 
          command.add("-zip");
        }
        command.add("-compile");
      }
      else if (rebuilding) command.add("-compile");
      else command.add("-file");
      command.add(_filename);
    }
    else if (lastFileCB.isSelected()) command.add("-lastFile");
    NamedLookAndFeel laf = (NamedLookAndFeel) lookandfeelCombo.getSelectedItem();
    command.add("-lookAndFeel");
    command.add(laf.getClassname());
    if (laf.isDecorateWindows()) command.add("-decorateWindows");
    command.add("-screen");
    command.add(Integer.toString(OsejsCommon.getScreenNumber(frame)));
    
    String arguments = argsField.getText();
    if (arguments.length()>0) {
      StringTokenizer tkn = new StringTokenizer(arguments," ");
      while (tkn.hasMoreTokens()) command.add(tkn.nextToken());
    }
    EjsConsole.GeneratedUtil runnable = new EjsConsole.GeneratedUtil(javaHome,command.toArray(new String[0]),this);
    java.lang.Thread thread = new Thread(runnable);
    thread.setPriority(Thread.NORM_PRIORITY);
    thread.start();
  }

  // --------------------------------------------
  // Compiling several simulations at once
  // --------------------------------------------
  
  /**
   * Implementation of ConsoleServer
   */
  public void processArgs (String[] args) throws java.rmi.RemoteException {
    // Check first for a Simulation inspecting its XML code
    String password=null;
    for (int i=0; i<args.length;i++) { // See if a password was passed along by Launcher 
      if (args[i].equalsIgnoreCase("-launcher.password")) {
        password = args[i+1];
        break;
      }
    }
    File sourceDirectory = new File (userDir,OsejsCommon.SOURCE_DIR_PATH);
    String sourceDirPath = FileUtils.getPath(sourceDirectory);
    @SuppressWarnings("unused")
    String programmingLanguage = null;
    for (int i=0; i<args.length;i++) {
      if (args[i].startsWith("-")) {
        if (args[i].equalsIgnoreCase("-programmingLanguage")) {
          programmingLanguage = args[++i];
        }
        if (args[i].equalsIgnoreCase("-file")) {
          runEjs(args[++i],password);
          return;
        }
      }
      else {
        File file = new File(sourceDirectory,args[i]);
        if (!file.isDirectory()) { // Load a given file
          runEjs(args[++i],password);
          return;
        }
      }
    }
    // Now check for a batch compilation
    for (int i=0; i<args.length;i++) {
      if (args[i].equalsIgnoreCase("-compile")) justCompiling = true;
      else if (args[i].equalsIgnoreCase("-outputDir")) tmpDirPath = args[++i];
      else {
        File file = new File(sourceDirectory,args[i]);
        if (file.isDirectory()) { // Include all files in this directory
          for (File subFile : JarTool.getContents(file)) {
            if (OsejsCommon.isEJSfile(subFile)) 
//            if (subFile.getName().toLowerCase().endsWith(".xml")) 
              filesToProcess.add(FileUtils.getRelativePath(FileUtils.getPath(subFile),sourceDirPath,false));
          }
        }
        else filesToProcess.add(args[i]);
      }
    }
    for (String filepath : filesToProcess) System.out.println (res.getString("EjsConsole.WillProcess")+" "+filepath);
    if (filesToProcess.size()>0) runEjs(filesToProcess.get(0),null);
  }

  /**
   * If there is any file left to compile, it will ask for abortion.
   * @return boolean true if the calling process should continue. False if it should not continue.
   */
  private boolean abortBatchCompilation () {
    if (filesToProcess.size()>0) {
      int selected = JOptionPane.showConfirmDialog(frame, res.getString("EjsConsole.AbortBatchCompilation"),
          res.getString("Warning"),JOptionPane.YES_NO_OPTION);
      if(selected!=JOptionPane.YES_OPTION) return false;
      synchronized(filesToProcess) { filesToProcess.clear(); }  // This stops the process
    }
    return true;
  }

  private void createReportWindow () {
    if (reportWindow!=null) reportWindow.dispose();
    reportWindow = new ProgressDialog(filesToProcess.size(),res.getString("EjsConsole.ProcessTitle"),
                                      res.getDimension("Osejs.StartDialogSize"),OsejsCommon.getScreenBounds(frame));
    reportWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    reportWindow.setIconImage(ResourceLoader.getImage(sysRes.getString("Osejs.Icon.ConsoleIcon")));
    reportWindow.addWindowListener(new WindowAdapter() {
      public void windowClosing (WindowEvent e) { abortBatchCompilation(); }
    });
    reportWindow.setZoomLevel(FontSizer.getLevel());
  }

  /**
   * Compiles all files in the list of directories and files
   * @param _list AbstractList a list of File objects
   */
  private void compileFiles (java.util.List<File> _list) {
    // Get the source directory and path
    File sourceDirectory = new File (userDir,OsejsCommon.SOURCE_DIR_PATH);
    String sourceDirectoryPath = FileUtils.getPath(sourceDirectory);
    
    // Create the list of XML files either in the list or in the indicated directories
    java.util.List<Object> primaryList = new ArrayList<Object>();
    for (File file : _list) {
      if (file.isDirectory()) {
        for (File subFile : JarTool.getContents(file)) {
          if (OsejsCommon.isEJSfile(subFile)) {
            String relPath = FileUtils.getRelativePath(FileUtils.getPath(subFile), sourceDirectoryPath, false);
            primaryList.add(new PathAndFile(relPath,subFile));
          }
        }
      }
      else {
        if (OsejsCommon.isEJSfile(file)) {
          String relPath = FileUtils.getRelativePath(FileUtils.getPath(file), sourceDirectoryPath, false);
          primaryList.add(new PathAndFile(relPath,file));
        }
      }
    }

    // Ask for JAR export after compilation
    JLabel prefixLabel = new JLabel (res.getString("EjsConsole.PrefixForExport"));
    prefixLabel.setBorder(new EmptyBorder(0,3,0,3));
    final JTextField prefixField = new JTextField(compressPrefix,10);
    prefixField.setEditable(true);
    JPanel prefixPanel = new JPanel (new BorderLayout());
    prefixPanel.add(prefixLabel,BorderLayout.WEST);
    prefixPanel.add(prefixField,BorderLayout.CENTER);

    JCheckBox compressFiles = new JCheckBox(res.getString("EjsConsole.ExportAfterCompilation"),true);
    compressFiles.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        prefixField.setEditable(_evt.getStateChange()==ItemEvent.SELECTED);
      }
    });
    
    JPanel jarcompanionPanel = new JPanel (new BorderLayout());
    jarcompanionPanel.add(compressFiles,BorderLayout.WEST);
    jarcompanionPanel.add(prefixPanel,BorderLayout.CENTER);

    // Ask for ZIP export after compilation
    JLabel zipprefixLabel = new JLabel (res.getString("EjsConsole.PrefixForZIPExport"));
    zipprefixLabel.setBorder(new EmptyBorder(0,3,0,3));
    final JTextField zipprefixField = new JTextField(zipPrefix,10);
    zipprefixField.setEditable(true);
    JPanel zipprefixPanel = new JPanel (new BorderLayout());
    zipprefixPanel.add(zipprefixLabel,BorderLayout.WEST);
    zipprefixPanel.add(zipprefixField,BorderLayout.CENTER);

    JCheckBox zipFiles = new JCheckBox(res.getString("EjsConsole.ZIPExportAfterCompilation"),true);
    zipFiles.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        zipprefixField.setEditable(_evt.getStateChange()==ItemEvent.SELECTED);
      }
    });
    
    JPanel zipcompanionPanel = new JPanel (new BorderLayout());
    zipcompanionPanel.add(zipFiles,BorderLayout.WEST);
    zipcompanionPanel.add(zipprefixPanel,BorderLayout.CENTER);

    JPanel companionPanel = new JPanel (new GridLayout(0,1));
    companionPanel.add(jarcompanionPanel,BorderLayout.WEST);
    companionPanel.add(zipcompanionPanel,BorderLayout.CENTER);

    // Now confirm the list
    java.util.List<?> confirmedList = EjsTool.ejsConfirmList(frame,res.getDimension("Package.ConfirmList.Size"),
      res.getString("EjsConsole.ProcessList"),res.getString("EjsConsole.ProcessTitle"),primaryList,companionPanel);
    if (confirmedList==null) return;
    tabbedPanel.setSelectedComponent(outputPanel);
    filesToProcess.clear();
    for (Iterator<?> it=confirmedList.iterator(); it.hasNext(); ) {
      PathAndFile paf = (PathAndFile) it.next();
      filesToProcess.add(paf.getPath());
    }
    for (String filename : filesToProcess) println (res.getString("EjsConsole.WillProcess")+" "+filename);
    justCompiling = true;
    compressAfterCompilation = compressFiles.isSelected();
    compressPrefix = prefixField.getText().trim();
    zipAfterCompilation = zipFiles.isSelected();
    zipPrefix = zipprefixField.getText().trim();
    
    if (filesToProcess.size()>0) {
      createReportWindow();
      reportWindow.reportProgress(filesToProcess.get(0));
      runEjs(filesToProcess.get(0),null);
    }
  }

  private void checkExit () {
    if (filesToProcess.size()>0) {
      filesToProcess.remove(0);
      if (filesToProcess.size()>0) {
        if (reportWindow!=null) reportWindow.reportProgress(filesToProcess.get(0));
        runEjs(filesToProcess.get(0),null);
        return;
      }
      println (res.getString("EjsConsole.BatchCompilationFinished"));
      compressAfterCompilation = false;
      zipAfterCompilation = false;
    }
    if (reportWindow!=null) {
      reportWindow.setVisible(false);
      reportWindow=null;
    }
    if (rebuilding) { // End of rebuilding
      rebuilding = false;
      continueRebuilding();
      return;
    }
    if (processDialog.processesRunning()<=0) {
      saveOptions();
      if (registry!=null) { // Close the registry
        //try { LocateRegistry.getRegistry().unbind ("EjsConsoleServer"); }
        //catch (Exception exc) { System.out.println (exc.getMessage()); }
        try { UnicastRemoteObject.unexportObject(registry,true); }
        catch (Exception exc) { System.out.println (exc.getMessage()); }
      }
     System.exit(0);
    }
  }

  private void runLaunchBuilder() {
    File outputDir = new File(userDir,OsejsCommon.OUTPUT_DIR_PATH);
    if (!outputDir.exists()) {
      JOptionPane.showMessageDialog(frame, res.getString("Package.NoSimulations"),res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    java.util.List<PathAndFile> list = OsejsCommon.getSimulationsMetadataFiles (frame,outputDir,
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.PackageAllSimulationsMessage"),
        res.getString("Package.PackageAllSimulations"),true,false);
    if (list==null || list.size()<=0) return;
    File target = new File (userDir,OsejsCommon.EXPORT_DIR_PATH+"/ejs_launcher.jar");
    if (!target.getParentFile().exists()) target.mkdirs();
    tabbedPanel.setSelectedComponent(outputPanel);
    PackagerBuilder.create(list, initDir, userDir, target, processDialog, outputArea,tabbedPanel,frame);
  }

  @SuppressWarnings("unchecked")
  private void rebuildLauncherPackage() {
    // --- Choose the JAR to rebuild
    JFileChooser chooser=OSPRuntime.createChooser("JAR",new String[]{"jar"});
    chooser.setCurrentDirectory(new File (userDir,OsejsCommon.EXPORT_DIR_PATH));
    String sourceName = OSPRuntime.chooseFilename(chooser,frame,false); // false = to read

    if (sourceName==null) return;
    if (!sourceName.toLowerCase().endsWith(".jar")) sourceName = sourceName + ".jar";
    File source = new File(sourceName);
    if (!PackagerBuilder.canBeRebuilt(source)){
      JOptionPane.showMessageDialog(frame,res.getString("Package.FileNotExistingError") + ": " +
          source.getName(),DisplayRes.getString("Package.Error"),JOptionPane.ERROR_MESSAGE);
      return;
    }

    // --- Uncompress the JAR 
    tabbedPanel.setSelectedComponent(outputPanel);
    println(res.getString("EjsConsole.UncompressingJAR")+" "+source.getName());
    rebuildTmpDir = PackagerBuilder.uncompressToTemp(source);
    if (rebuildTmpDir==null){
      String[] message=new String[]{res.getString("Package.JarFileNotCreated"),res.getString("Package.NotTempDirError")};
      JOptionPane.showMessageDialog(frame,message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
      return;
    }

    // --- Get the list of simulations in the JAR
    String rebuildTmpDirPath = FileUtils.getPath(rebuildTmpDir);
    java.util.List<Class<?>> classes = getSimulationsInJar (rebuildTmpDir);
    if (classes.size()<=0) {
      JOptionPane.showMessageDialog(frame,res.getString("Package.FileNotExistingError") + ": " +
          source.getName(),DisplayRes.getString("Package.Error"),JOptionPane.ERROR_MESSAGE);
      JarTool.remove(rebuildTmpDir);
      return; 
    }
    java.util.List<TwoStrings> modelsInJarList = new ArrayList<TwoStrings>();
    Set<String> resourcesInJarList = new HashSet<String>();
    for (Class<?> theClass : classes) {
      try {
        Class<?> [] c = {};
        java.lang.reflect.Method getModelMethod = theClass.getMethod(EjsTool.GET_MODEL_METHOD,c);
        Object[] o = {};
        String model = (String) getModelMethod.invoke(null,o);
        modelsInJarList.add(new TwoStrings(model,theClass.getName()));
        resourcesInJarList.add(model);
        java.lang.reflect.Method getResourcesMethod = theClass.getMethod(EjsTool.GET_RESOURCES_METHOD, c);
        if (getResourcesMethod!=null) resourcesInJarList.addAll((Set<String>) getResourcesMethod.invoke(null, o));
        // Locate possible HTML files generated by the simulation
        File modelFile = new File(rebuildTmpDir,model);
        String prefix = OsejsCommon.getHTMLFilenamePrefix(modelFile);
        for (File file : JarTool.getContents(modelFile.getParentFile())) {
          TwoStrings ts = FileUtils.getPlainNameAndExtension(file);
          if ((ts.getSecondString().equals("html") || ts.getSecondString().equals("html")) && ts.getFirstString().startsWith(prefix)) {
            String path = FileUtils.getRelativePath(file,rebuildTmpDirPath, false);
//            System.out.println ("Added "+path);
            resourcesInJarList.add(path);
          }
        }
      }
      catch (Exception _exc) { _exc.printStackTrace(); }
    }
    Collections.sort(modelsInJarList);
    
    // --- Confirm the list of models desired in the rebuilt jar
    JCheckBox extractCheckBox = new JCheckBox(res.getString("Package.ExtractModelsFromPackage"),false);
    java.util.List<Object> modelsDesiredList = EjsTool.ejsConfirmList(frame,res.getDimension("Package.ConfirmList.Size"),
      res.getString("EjsConsole.ProcessList"),res.getString("EjsConsole.ProcessTitle"),modelsInJarList,extractCheckBox);
    if (modelsDesiredList==null) {
      println (res.getString("ProcessCanceled"));
      JarTool.remove(rebuildTmpDir);
      return;
    }

    // --- Get the list of already compiled simulations
    java.util.List<PathAndFile> allCompiledList = 
      OsejsCommon.getUnconfirmedSimulationsMetadataFiles (frame,new File (userDir,OsejsCommon.OUTPUT_DIR_PATH), false,false);
    if (allCompiledList==null) {
      println (res.getString("ProcessCanceled"));
      JarTool.remove(rebuildTmpDir);
      return;
    }
    
    // remove models already in the jar from this list
    ArrayList<PathAndFile> notInJarList = new ArrayList<PathAndFile>();
    for (PathAndFile paf : allCompiledList) {
      if (!isModelInJar(paf,modelsInJarList)) notInJarList.add(paf);
    }
    // Create the final list of models to add to the rebuilt jar
    toBeAddedWhenRebuilding = new ArrayList<PathAndFile>();
    // Ask the user for confirmation
    if (!notInJarList.isEmpty()) {
      java.util.List<Object> newModelsDesiredList = EjsTool.ejsConfirmList(frame,res.getDimension("Package.ConfirmList.Size"), 
          res.getString("EjsConsole.AvailableSimulations"), res.getString("Package.PackageExtraSimulationsMessage"),
          notInJarList);
      if (newModelsDesiredList==null) {
        println (res.getString("ProcessCanceled"));
        JarTool.remove(rebuildTmpDir);
        return;
      }
      for (Object object : newModelsDesiredList) toBeAddedWhenRebuilding.add((PathAndFile) object);
    }
    
    // --- Select the target JAR 
    String name = source.getName();
    if (name.toLowerCase().endsWith(".jar")) name = name.substring(0,name.length()-4);
    chooser.setSelectedFile(new File(source.getParentFile(),name+"_rebuilt.jar"));
    String targetName = OSPRuntime.chooseFilename(chooser,frame,true);
    if (targetName==null) return;
    if (!targetName.toLowerCase().endsWith(".jar")) targetName = targetName + ".jar";
    rebuildTarget = new File(targetName);

    // --- The automatic process starts
    
    // Check for simulations in the JAR that are to be removed...
    toBeRemovedWhenRebuilding = new ArrayList<TwoStrings>();
//    File outputDir = new File (userDir,OsejsCommon.OUTPUT_DIR_PATH);
    for (TwoStrings ts : modelsInJarList) {
      if (!modelsDesiredList.contains(ts)) toBeRemovedWhenRebuilding.add(ts);
    }

    // Extract all models and resources, if requested to do so
    if (extractCheckBox.isSelected()) {
      Set<PathAndFile> copyList = new HashSet<PathAndFile>();
      for (Object object : modelsDesiredList) {
        TwoStrings ts = (TwoStrings) object;
        String model = ts.getFirstString(); // This is the name of the model
        File modelFile = new File(rebuildTmpDir,model);
        Set<PathAndFile> auxFiles = OsejsCommon.getAuxiliaryFiles (modelFile, rebuildTmpDir, outputPanel);
        copyList.add(new PathAndFile(model, modelFile));
        copyList.addAll(auxFiles);
      }
      copyFiles(copyList,rebuildTmpDir);
    }

    // Prepare to start up
    filesToProcess.clear();

    recompiledWhenRebuilding = new ArrayList<TwoStrings>(); // List of pairs metadata file,class name
    File sourceDir = new File(userDir,OsejsCommon.SOURCE_DIR_PATH);
    StringBuffer filesNotFound = new StringBuffer();
    for (Object object : modelsDesiredList) {
      TwoStrings ts = (TwoStrings) object;
      String model = ts.getFirstString(); // This is the name of the model
      File file = new File(sourceDir,model);
      if (file.exists()) {
        filesToProcess.add(model);
        int index = model.indexOf('.');
        if (index>0) model = model.substring(0,index); // trim the ".xml" or ".ejs"
        ts.setFirstString(model+Metadata.EXTENSION); // This is the metadata file
        recompiledWhenRebuilding.add(ts);
      }
      else filesNotFound.append("  "+model+"\n");
    }
    String modelsMissing = filesNotFound.toString();
    if (modelsMissing.length()>0) {
      JOptionPane.showMessageDialog(frame,res.getString("SimInfoEditor.RequiredFileNotFound") + "\n" +modelsMissing,
          DisplayRes.getString("Error"),JOptionPane.INFORMATION_MESSAGE);
      println (res.getString("ProcessCanceled"));
      JarTool.remove(rebuildTmpDir);
      return;
    }

    // Clean old files in temporary directory
    JarTool.remove(new File(rebuildTmpDir,"org/opensourcephysics"));
    JarTool.remove(new File(rebuildTmpDir,"org/colos/ejs"));
    JarTool.remove(new File(rebuildTmpDir,"ch/epfl/cockpit"));
    JarTool.remove(new File(rebuildTmpDir,"com/calerga/sysquake"));
    JarTool.remove(new File(rebuildTmpDir,"org/jibble/epsgraphics"));
    ArrayList<TwoStrings> cleanList = new ArrayList<TwoStrings>();
    cleanList.addAll(recompiledWhenRebuilding);
    cleanList.addAll(toBeRemovedWhenRebuilding);
    for (TwoStrings ts : cleanList) { // Clean the old files of these
      String classname = ts.getSecondString();
      classname = classname.substring(0,classname.lastIndexOf('.')); // Remove the class name
      classname = classname.replace('.','/'); // This is now the class directory 
      File classDir = new File(rebuildTmpDir,classname);
      JarTool.remove(classDir);
    }
    // Remove discarded resources and HTML files
    for (String resource : resourcesInJarList) (new File(rebuildTmpDir,resource)).delete();
    // Clean empty directories
    for (File file : JarTool.getContents(rebuildTmpDir)) {
      if (file.isDirectory() && JarTool.getContents(file).isEmpty()) file.delete();
    }

    // --- Compile all simulation files extracted from the jar file
    if (filesToProcess.size()>0) {
      rebuilding = true;
      createReportWindow();
      reportWindow.reportProgress(filesToProcess.get(0));
      runEjs(filesToProcess.get(0),null);
    }
    else continueRebuilding();
  }

  
  /**
   * Whether the given model is in the list of models in the JAR
   * @param _paf
   * @param _list
   * @return
   */
  static private boolean isModelInJar(PathAndFile _paf, java.util.List<TwoStrings> _list) {
    String path = Metadata.getClassname(_paf.getFile());
    for (TwoStrings ts : _list) {
      if (ts.getSecondString().equals(path)) return true;
    }
    return false;
  }

  /**
   * Copy all required files from the temp dir to the EJS source directory
   */
  public void copyFiles (Set<PathAndFile> _list, File _baseDir) {
    boolean noToAll=false, yesToAll=false;
    ResourceUtil jarRes = null;
    Object[] ynOptions = null;
    File destDir = new File (userDir,OsejsCommon.SOURCE_DIR_PATH);
    String baseDirStr = FileUtils.getPath(_baseDir);

    for (PathAndFile paf : _list) {
      String filename = FileUtils.getRelativePath(paf.getFile(), baseDirStr, false);
      File target = new File (destDir,filename);
//      System.out.println ("Copying "+filename + " to " + FileUtils.getPath(target));
      boolean doIt = true;
      if (target.exists()) {
        if (noToAll) doIt = false;
        else if (yesToAll) doIt = true;
        else { // ask
          if (jarRes==null) {
            jarRes = new ResourceUtil("org.opensourcephysics.resources.tools.tools");
            ynOptions = new Object[]{ jarRes.getString("JarTool.Yes"), jarRes.getString("JarTool.YesToAll"), 
                                      jarRes.getString("JarTool.No"),  jarRes.getString("JarTool.NoToAll")  };
          }
          int option = JOptionPane.showOptionDialog(frame,  
              FileUtils.getRelativePath(target,destDir,false) + " : "+ res.getString("Osejs.File.Overwrite"), 
              res.getString("Osejs.File.FileExists"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ynOptions, ynOptions[0]);
          switch (option) {
            case 0 : doIt = true; break; 
            case 1 : doIt = true; yesToAll = true; break; 
            case 2 : doIt = false; break; // Do not copy it
            case 3 : doIt = false; noToAll = true; break;
          }
        }
      }
      if (doIt) {
        if (FileUtils.copy (paf.getInputStream(), target)) {
          println(res.getString("Osejs.File.SavedOK")+" "+FileUtils.getRelativePath(target,destDir,false));
        }
        else 
          JOptionPane.showMessageDialog(frame, // openButton 
              res.getString("Osejs.File.Error")+" "+FileUtils.getRelativePath(target,destDir,false),
              res.getString("Osejs.File.SavingError"), JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }

  /**
   * Once all files have been recompiled, rebuild the jar file
   */
  private void continueRebuilding() {
    PackagerBuilder.rebuild (recompiledWhenRebuilding, toBeAddedWhenRebuilding, toBeRemovedWhenRebuilding,
        initDir,userDir,rebuildTmpDir, rebuildTarget, 
        processDialog, outputArea, tabbedPanel,frame);
  }

  static private java.util.List<Class<?>> getSimulationsInJar(File _source) {
    java.util.List<Class<?>> list = new ArrayList<Class<?>>();
    ClassLoader cl = new MyClassLoader (_source);
    for (String classname : PackagerBuilder.readPackageInfo(_source)) { // for each of the applications in the package
      try {
//        System.err.println("Processing file "+classname);
        Class<?> aClass = Class.forName(classname,true,cl);
//        System.err.println("class is "+aClass);
//        System.err.println("has model = "+EjsTool.hasEjsModel(aClass));
        if (aClass != null && EjsTool.hasEjsModel(aClass)) list.add(aClass);
      } catch (Exception exc2) {exc2.printStackTrace(); };
    }
    return list;
  }

  /*
  static private Set getEJSModels(LaunchPanel tab) {
      Set ejs = new HashSet();
      Enumeration e = tab.getRootNode().breadthFirstEnumeration();
      while(e.hasMoreElements()) {
        LaunchNode next = (LaunchNode) e.nextElement();
        Class type = next.getLaunchClass();
        if (type != null && EjsTool.hasEjsModel(type)) ejs.add(type);
      }
      return ejs;
  }
*/
  // --------------------------------------------
  // Miscellaneous utilities
  // --------------------------------------------

  private boolean isJavaRootCorrect() {
    //  System.out.println ("Checking the javaroot "+javaRootField.getText().trim());
      if (isMacOSX) return true;
      // Under Windows or Linux a JavaRoot directory is optional
      // but this java root must include a bin/java file
      String javaRoot = javaRootField.getText().trim();
      if (javaRoot.length()<=0) return true; // errorMessage ("NoJavaRoot"); // optional JAVA SDK
      File javaRootFile = new File(javaRoot);
      if (!(javaRootFile.exists() && javaRootFile.isDirectory())) return errorMessage ("Not_A_Directory");
      File rtFile = new File(javaRoot+"/lib/rt.jar");
      File jrertFile = new File(javaRoot+"/jre/lib/rt.jar");
      if (!(rtFile.exists() || jrertFile.exists())) return errorMessage ("Not_A_JDK");
      return true;
    }

  private void println (String message) {
    outputArea.append(message+"\n");
    outputArea.setCaretPosition (outputArea.getText().length());
  }

  private boolean errorMessage (String message) {
    frame.setVisible(true);
    JOptionPane.showMessageDialog(frame,res.getString("EjsConsole.Error."+message),
                                        res.getString("Warning"),JOptionPane.ERROR_MESSAGE);
    javaRootField.setText("");
    return false;
  }

  
  private String getLanguageLocale () {
    if (languageCombo.getSelectedIndex()==0) return "";
    String locale = (String) languageCombo.getSelectedItem();
    if (locale.endsWith("  ")) locale = locale.substring(0,2)+" XX";
    return "-locale "+ locale;
  }

  private String getLocaleClasspath () {
   String locale = (String) languageCombo.getSelectedItem();
   if (locale.equals("en   ")) return "";
   return OsejsCommon.BIN_DIR_PATH+"/locales.jar";
  }
  
  // ----------------------------------
  // Check for updates
  // ----------------------------------
  
  static private WebView mWebView;
  /**
   * Visits a dummy page in EjsWiki for the single purpose of counting visits for Google Analytics
   * @param _parent is the parent component for dialogs, only non-null when the user clicks the UPDATE button of the console
   */
  static private void visitEjsSCounter (boolean _verbose) {
    // This method is invoked on the EDT thread
    final JFXPanel mFxPanel = new JFXPanel();
    Platform.runLater(new Runnable() {
      public void run() {
        mWebView = new WebView();
        mFxPanel.setScene(new Scene(mWebView));
      }
    });
    mFxPanel.setPreferredSize(new java.awt.Dimension(500,500));

//    JFrame mDialog = new JFrame("Visitors count");
//    mDialog.getContentPane().setLayout(new BorderLayout());
//    mDialog.getContentPane().add(mFxPanel, BorderLayout.CENTER);
//    mDialog.pack();
//    mDialog.setVisible(true);

    //    System.err.println ("Trying to load http://"+OsejsCommon.EJS_SERVER+"/Site/EjsSUserCounter");
    Platform.runLater(new Runnable() {
      public void run() {
        System.err.println ("loading http://"+OsejsCommon.EJS_SERVER+"/Site/EjsSUserCounter");
        mWebView.getEngine().load("http://"+OsejsCommon.EJS_SERVER+"/Site/EjsSUserCounter");
      }
    });
//    try { // Now, do it
//      String currentVersionPage = "http://"+OsejsCommon.EJS_SERVER+"/Site/EjsSUserCounter";
//      URL url = new URL(currentVersionPage);
//
//      HttpURLConnection con = (HttpURLConnection) url.openConnection();
//      con.setRequestMethod("GET");
////      int responseCode = con.getResponseCode();
////      System.out.println("\nSending 'GET' request to URL : " + url);
////      System.out.println("Response Code : " + responseCode);
//
//      BufferedReader in = new BufferedReader(
//          new InputStreamReader(con.getInputStream()));
//      String inputLine;
//      StringBuffer response = new StringBuffer();
//
//      while ((inputLine = in.readLine()) != null) {
//        response.append(inputLine+"\n");
//      }
//      in.close();
//
//      //print result
////      System.out.println(response.toString());
//    }
//    catch (Exception exc) { 
//      System.err.println("Warning: Could not connect to EjsS Wiki (timeout?).");
//    }
  }  

  /**
   * Checks for a newer version of the current EJS in the EJS Wiki server
   * @param _whenToDoIt indicates when to do this check (ALWAYS, NEVER, WEEK, MONTH)
   * @param _parent is the parent component for dialogs, only non-null when the user clicks the UPDATE button of the console
   * @return String the url of the newer version, empty string "" if the release is up-to-date, null if there is a connection problem
   */
  static private void checkForUpdates (String _whenToDoIt, boolean _startUpCall, Component _parent) {
    boolean doIt=true; // decide whether or not to update
    if      (_whenToDoIt.equals(EjsConsole.NEVER))  doIt = false;
    else if (_whenToDoIt.equals(EjsConsole.ALWAYS) || lastUpdateCheck<=0) doIt = true;
    else { // Depends on the time elapsed from last time
      Calendar checkTime = Calendar.getInstance();
      checkTime.setTimeInMillis(lastUpdateCheck);
      if      (_whenToDoIt.equals(EjsConsole.WEEK))  checkTime.add(Calendar.WEEK_OF_YEAR, 1);
      else if (_whenToDoIt.equals(EjsConsole.MONTH)) checkTime.add(Calendar.MONTH, 1);
      Calendar rightNow = Calendar.getInstance();
      rightNow.add(Calendar.DAY_OF_YEAR, 45);
      doIt = checkTime.compareTo(rightNow)<0;
    }
    if (!doIt) return;
 
    StringBuffer buffer = new StringBuffer();
    try { // Now, do it
//      String path = "http://"+OsejsCommon.EJS_SERVER+"/uploads/Download/counter/counter.php?page=test";
//      String result = ResourceLoader.getString(path);
//      System.out.println("Counter result = "+result);
      String currentVersionPage = "http://"+OsejsCommon.EJS_SERVER+"/Site/EjsCurrentVersion?action=source";
      URL url = new URL(currentVersionPage);
      Reader reader = new InputStreamReader(url.openStream());
      LineNumberReader l = new LineNumberReader(reader);
      String versionTxt = l.readLine();
      reader.close();
      //System.out.println ("Last available version is "+versionTxt);
      if (versionTxt.equals(ignoreUpdateVersion) && _startUpCall) return; 
      lastUpdateCheck = Calendar.getInstance().getTimeInMillis();
      
      StringTokenizer tkn = new StringTokenizer(versionTxt,"_");
      String ejsTxt = tkn.nextToken().toLowerCase();
      if (ejsTxt.equals("ejss") || ejsTxt.equals("ejs")) {
        int comparison = -1; // < 0 means this is a newer release, 0 same release, >0 older release than that in the server
        String versionMajor = tkn.nextToken();
        String versionMinor = tkn.nextToken();
        buffer.append(res.getString("EjsConsole.InstalledRelease")+" "+_EjsSConstants.VERSION +", build "+_EjsSConstants.VERSION_DATE+".\n");
        buffer.append(res.getString("EjsConsole.CurrentRelease")  +" "+versionMajor+", build "+versionMinor+".\n\n");

        int resultMajor = versionMajor.compareTo(_EjsSConstants.VERSION); 
        if  (resultMajor<0) {
          comparison = -1; 
          buffer.append(res.getString("EjsConsole.NewerRelease"));
        }
        else if (resultMajor>0) {
          comparison =  1;
          buffer.append(res.getString("EjsConsole.NeedsUpdate"));
        }
        else { // Same major release, check minor release
          int resultMinor = versionMinor.compareTo(_EjsSConstants.VERSION_DATE);
          if (resultMinor<0) {
            comparison = -1; // You are using a private and newer release of the same version
            buffer.append(res.getString("EjsConsole.NewerRelease"));
          }
          else if (resultMinor>0) {
            comparison =  1;
            buffer.append(res.getString("EjsConsole.NeedsUpdate"));
          }
          else buffer.append(res.getString("EjsConsole.Uptodate"));
        }
        if (comparison>0) {
          Object[] options = { res.getString("EjsConsole.GotoEJSWeb"), res.getString("EjsConsole.UseCurrentVersion"), res.getString("EjsConsole.IgnoreThisUpdate") };
          int option = JOptionPane.showOptionDialog(_parent,buffer.toString(),"Easy Java Simulations",
              JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.ConsoleIcon")), options, options[0]);
          switch (option) {
            case 0 : 
              org.opensourcephysics.desktop.OSPDesktop.displayURL("http://"+OsejsCommon.EJS_SERVER+"/Main/Download"); 
              //processDialog.killAllProcesses(); 
              // System.exit(0); 
              break;
            case 2 : ignoreUpdateVersion = versionTxt; break;
            default : break; // do nothing
          }

//          return "http://"+OsejsCommon.EJS_SERVER+"/uploads/Download/"+versionTxt+".zip";
        }
        else if (!_startUpCall) { // Only if the update button was pressed explicitly
          JOptionPane.showMessageDialog(_parent, buffer.toString(), "Easy Java Simulations", 
              JOptionPane.INFORMATION_MESSAGE,ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.ConsoleIcon")));
        }
      }
    }
    catch (Exception exc) { 
      if (_parent!=null) JOptionPane.showMessageDialog(_parent,res.getString("EjsConsole.UpdateUnavailable"),res.getString("Osejs.File.Error"),
          JOptionPane.ERROR_MESSAGE,ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.ConsoleIcon")));
    }
  }  
  
  // ----------------------------------
  // Inner classes needed to run Ejs
  // ----------------------------------

  class MyMouseListener extends MouseAdapter {
    public void mousePressed(MouseEvent evt) {
      AbstractButton button = (AbstractButton) (evt.getSource());
      String aCmd = button.getActionCommand();
      if (aCmd.equals("javarootfield")) isJavaRootCorrect();
      else if (aCmd.equals("javaroot")) {
        JFileChooser fileChooser = OSPRuntime.createChooser("",new String[0]);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        int returnVal = fileChooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          javaRootField.setText(FileUtils.getPath(fileChooser.getSelectedFile()));
          isJavaRootCorrect();
        }
      }
      else if (aCmd.equals("external")) {
        JFileChooser fileChooser = OSPRuntime.createChooser("",new String[0]);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        if( fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) 
          externalField.setText(FileUtils.getPath(fileChooser.getSelectedFile()));
      }
      else if (aCmd.equals("userDir")) {
        File dirChosen = selectWorkspace();
        if (dirChosen==null || dirChosen.equals(userDir)) return; // no need to change anything
        if (FileUtils.isWritable(dirChosen)) {
          userDir = dirChosen;
          userDirField.setText(FileUtils.getPath(userDir));
          JOptionPane.showMessageDialog(null,res.getString("EjsConsole.WorkspaceChanged"),
              res.getString("Warning"),JOptionPane.WARNING_MESSAGE);
        }
        else JOptionPane.showMessageDialog(null,res.getString("EjsConsole.NeedWritableDir"),
            res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
        
        /*
        JFileChooser fileChooser = OSPRuntime.createChooser("",new String[0]);
        fileChooser.setDialogTitle(res.getString("EjsConsole.ChooseWorkingDirectory"));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        if (userDir.getParentFile()!=null) fileChooser.setCurrentDirectory(userDir.getParentFile());
        fileChooser.setSelectedFile(userDir);

        JTextArea textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setText(res.getString("EjsConsole.ExplainWorkspaceConcept"));
        textArea.setPreferredSize(res.getDimension("EjsConsole.WorkspaceDescription"));
        textArea.setFont(InterfaceUtils.font(null,res.getString("EjsConsole.WorkspaceDescriptionFont")));
        textArea.setBorder(new EmptyBorder(3,3,3,3));
        fileChooser.setAccessory(textArea);

        //fileChooser.setCurrentDirectory(userDir.getParentFile());
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
          File dirChosen = fileChooser.getSelectedFile();
          if (dirChosen.equals(userDir)) return; // no need to change anything
          if (FileUtils.isWritable(dirChosen)) {
            userDir = dirChosen;
            userDirField.setText(FileUtils.getPath(userDir));
            JOptionPane.showMessageDialog(frame,res.getString("EjsConsole.WorkspaceChanged"),
                res.getString("Warning"),JOptionPane.WARNING_MESSAGE);
          }
          else JOptionPane.showMessageDialog(frame,res.getString("EjsConsole.NeedWritableDir"),
              res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
        }
      */
      }
      else if (aCmd.equals("doit")) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { runEjs(null); } // null = do not compile
        });
      }
      else if (aCmd.equals("launchJava")) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { runEjs(null,null,OsejsCommon.PROGRAMMING_JAVA); } // null = do not compile
        });
      }
      else if (aCmd.equals("launchJavascript")) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { runEjs(null,null,OsejsCommon.PROGRAMMING_JAVASCRIPT); } // null = do not compile
        });
      }
      else if (aCmd.equals("showProcesses")) processDialog.show(frame);
      else if (aCmd.equals("launchBuilder")) runLaunchBuilder();
      else if (aCmd.equals("rebuildPackage")) {
        if (abortBatchCompilation()) rebuildLauncherPackage();
      }
      else if (aCmd.equals("clear")) outputArea.setText("");
      else if (aCmd.equals("compileDirectory")) {
        if (!abortBatchCompilation()) return;
        
        JFileChooser chooser=OSPRuntime.createChooser(res.getString("View.FileDescription.xmlfile"), sysRes.getString("Osejs.File.Extension").split(","));
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        chooser.setCurrentDirectory(new File(userDir,OsejsCommon.SOURCE_DIR_PATH));
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        
        File[] dirs = chooser.getSelectedFiles();
        if (dirs==null || dirs.length<=0) {
          System.out.println (res.getString("ProcessCanceled"));
          return;
        }
        java.util.List<File> list = new ArrayList<File>();
        for (int i=0,n=dirs.length; i<n; i++) list.add(dirs[i]);
        compileFiles (list);
      } // end of "compileDirectory"
    }
  } // End of private class


  private static class GeneratedUtil extends java.io.OutputStream implements Runnable {
    private String[] command=null;
    private EjsConsole launcher=null;
    @SuppressWarnings("unused")
    private String javaHome=null;
//    private int max=256; //,count=0;
//    private byte[] buf=new byte[max];

    public GeneratedUtil (String _javaHome, String[] _command, EjsConsole _launcher) {
      javaHome = _javaHome;
      command = _command;
      launcher = _launcher;
    }

    // Implementation of OutputStream
    public void write(int i) throws java.io.IOException {
      launcher.outputArea.append(new String(new char[]{(char)i}));
      if (i=='\n') {
        launcher.outputArea.setCaretPosition (launcher.outputArea.getText().length());
        launcher.outputArea.repaint();
      }
/*
      if(count<max){
        buf[count]=(byte)i;
        count++;
        if(i==13 || count>=max) {
          launcher.outputArea.append(new String(buf,0,count-1));
          if (i==13) launcher.outputArea.setCaretPosition (launcher.outputArea.getText().length());
          count=0;
        }
      }
*/
    }

    public void run () {
      GeneratedOutput generatedOutput = null, generatedError = null;
      try {
       // for (int i=0; i<command.length; i++) launcher.outputArea.append ("Executing ["+i+"] = "+command[i]+"\n");
//        launcher.outputArea.append (" in dir = "+launcher.initDir.getAbsolutePath());
//        String envp[] = new String[] {"DYLD_LIBRARY_PATH=/System/Library/Frameworks/JavaVM.framework/Libraries"};
//        String envp[] = new String[] {"DYLD_LIBRARY_PATH="+libPath};
//        System.err.println ("Env[0]="+envp[0]);
//        String J3DPath = "/Users/Paco/j3d";
//        String envp[] = new String[] {"DYLD_LIBRARY_PATH="+J3DPath+"/jogl"};
        
        Process process = Runtime.getRuntime().exec(command, null, launcher.initDir);
        generatedOutput = new GeneratedOutput(this,process, false);
        Thread thread = new Thread(generatedOutput);
        thread.setPriority(java.lang.Thread.MIN_PRIORITY);
        thread.start();
        generatedError = new GeneratedOutput(this,process, true);
        Thread thread2 = new Thread(generatedError);
        thread2.setPriority(java.lang.Thread.MIN_PRIORITY);
        thread2.start();
        if (launcher.justCompiling || launcher.rebuilding) {
          // launcher.outputArea.append("\n"+filename+": "+res.getString("Generate.Compiling")+"\n");
        }
        else {
          String commandStr = "";
          for (int i=0; i<command.length; i++) {
            if (!command[i].startsWith("-Dlauncher.password")) commandStr += " " + command[i];
          }
          launcher.println(res.getString("EjsConsole.Error.EjsRunning")+"\n"+commandStr);
        }
        launcher.processDialog.addProcess(process, "Easy Java Simulations");
        int error = process.waitFor();
        generatedOutput.goOn = false;
        generatedError.goOn = false;
        launcher.processDialog.removeProcess(process);
        if (error == 0) {
          if (launcher.justCompiling || launcher.rebuilding); // launcher.outputArea.append("\n"+filename+": "+res.getString("Generate.GenerationOk")+"\n");
          else launcher.outputArea.append("\n"+res.getString("EjsConsole.Error.EjsRunOK")+"\n");
        }
        else {
          if (launcher.justCompiling || launcher.rebuilding); // launcher.outputArea.append("\n"+filename+": "+res.getString("Generate.GenerationError")+"\n");
          launcher.println(res.getString("EjsConsole.Error.EjsErrorCode")+error+"\n");
          launcher.frame.setVisible(true);
        }
        launcher.checkExit();
      }
      catch (Exception exc) {
        exc.printStackTrace();
        launcher.println(res.getString("EjsConsole.Error.EjsNotRunning"));
        launcher.frame.setVisible(true);
        if (generatedOutput!=null) generatedOutput.goOn = false;
        if (generatedError!=null) generatedError.goOn = false;
      }
    }

  } // End of private class

} // end of class


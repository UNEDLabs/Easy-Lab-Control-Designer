/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.Dimension;
import java.io.*;

import javax.swing.*;

import java.util.*;
import java.util.zip.*;

import org.opensourcephysics.tools.*;
import org.opensourcephysics.tools.minijar.MiniJar;
import org.opensourcephysics.tools.minijar.PathAndFile;
import org.opensourcephysics.controls.*;
import org.colos.ejs._EjsSConstants;
import org.colos.ejs.library.control.value.BooleanValue;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.Metadata;
import org.opensourcephysics.display.*;

/**
 * Creates a Launcher JAR file out of the list of simulation directories.
 * It runs as a separate process which calls LaunchBuilder to let the user
 * configure the Launcher file.
 *
 * @param sources AbstractList The list of app directories
 * @param target File The target compressed file.
 */
public class PackagerBuilder implements Runnable {
  static private final ResourceUtil res = new ResourceUtil("Resources");

  private ProcessListDialog processListDialog;
  private List<PathAndFile> simulationMetadataFiles;
  private File binDir, outputDir, sourceDir, configDir, tmpDir, target;
  private JTextArea output;
  private JComponent parentComponent;
  private java.awt.Window window;
  // Only for rebuilding purposes
  private boolean rebuilding; // Whether we are rebuilding an existing JAR
  private List<PathAndFile> addedList;     // list of already compiled models to be added to the jar 
  private List<TwoStrings> recompiledList; // list of pairs (Metadata file,oldClassName) of models that have been recompiled
//  private List<TwoStrings> toBeRemoved;    // list of pairs (Metadata file,oldClassName) of models to be removed from teh jar

  /**
   * Private constructor
   */
  private PackagerBuilder () {}

  /**
   * Whether the package builder will be able to process the given JAR file
   * @param _sourceJar File
   * @return boolean
   */
  static public boolean canBeRebuilt(File _sourceJar) {
    if (_sourceJar.exists()==false) return false;
    try {
      ZipInputStream input = new ZipInputStream(new FileInputStream(_sourceJar));
      ZipEntry zipEntry = null;
      while ( (zipEntry =input.getNextEntry()) != null) {
        if (zipEntry.isDirectory()) continue; // don't check directories
        if (zipEntry.getName().equals("EjsPackageInfo.xml")) { input.close(); return true; }
//        if (zipEntry.getName().equals("launcher_default.xset")) { input.close(); return true; }
      }
      input.close();
    } catch (Exception exc) {};
    return false;
  }

  /**
   * Reads the list of applications in the package info file.
   * If the package is not an EJS package, the list will be empty
   * @param _source File
   * @return List<String>
   */
  static public List<String> readPackageInfo(File _source) {
    List<String> list = new ArrayList<String>();
    File file = new File (_source,"EjsPackageInfo.xml");
    if (!file.exists()) return list; // information not available: maybe it is not an EJS package
    try {
      Reader reader = new FileReader(file);
      LineNumberReader l = new LineNumberReader(reader);
      String sl = l.readLine();
      while (sl != null) {
        int begin = sl.indexOf("<class>");
        if (begin>=0) list.add(sl.substring(begin+7,sl.indexOf("</class>")));
        sl = l.readLine();
      }
      reader.close();
    } catch (Exception ex) { ex.printStackTrace(); }
    return list;
  }
  
  /**
   * Returns the real XSET file for the package as specified in the launcher_default.xset file
   * @param tmpDir
   * @return
   */
  static private String getXsetName(File tmpDir) {
    File file = new File(tmpDir,"launcher_default.xset");
    if (!file.exists()) return null;
    String input = FileUtils.readTextFile(file,null);
    String xsetName = OsejsCommon.getPiece(input,"<property name=\"item\" type=\"string\">" ,".xset</property>",false);
    return xsetName;
  }

  /**
   * Prints a message on the given JTextArea
   * @param _textArea
   * @param _text
   */
  static private void println (JTextArea _textArea, String _text) {
    _textArea.append (_text+"\n");
    _textArea.repaint();
    _textArea.setCaretPosition (_textArea.getText().length());
  }


  /**
   * Uncompresses an existing Launcher package to a temporary directory
   * @param _source File
   * @return File The directory created (in the same directory as the source)
   */
  static public File uncompressToTemp(File _source) {
    // Create a temporary directory
    File tempDir = null;
    try {
      tempDir = File.createTempFile("EjsLauncher", ".tmp", _source.getParentFile()); // Get a unique name for our temporary directory
      tempDir.delete();        // remove the created file
    } catch (Exception exc) { tempDir = null; }
    if (tempDir==null || !tempDir.mkdirs()) return null;
    if (JarTool.unzip(_source,tempDir)) return tempDir;
    return null;
  }


  /**
   * Creates a Launcher JAR file.
   * @param _simulationMetadata List<File> The list of simulation metafiles to pack
   * @param _initDir File EJS directory
   * @param _userDir File User workspace directory
   * @param _targetFile File The desired target file
   * @param _processListDialog ProcessListDialog a dialog to show processes 
   * @param _output JTextArea A text area where to print messages
   * @param _parent JComponent The parent component for message dialogs
   * @param _window Window A window for reference
   */
  static public void create (List<PathAndFile> _simulationMetadataFiles, 
      File _initDir, 
      File _userDir, 
      File _targetFile, 
      ProcessListDialog _processListDialog, 
      JTextArea _output, 
      JComponent _parent, 
      java.awt.Window _window) {

    // --- Choose the target JAR file
    boolean warnBeforeOverwritting = true;
    final JFileChooser chooser=OSPRuntime.createChooser("JAR",new String[]{"jar"});
    chooser.setCurrentDirectory(_targetFile.getParentFile());
    chooser.setSelectedFile(_targetFile);
    String targetName = OSPRuntime.chooseFilename(chooser,_parent,true);
    // In case the OSPRuntime changed this...
    if (targetName==null) return;
    if (!targetName.toLowerCase().endsWith(".jar")) targetName = targetName + ".jar";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
    _targetFile = new File(targetName);
    if (warnBeforeOverwritting && _targetFile.exists()){
      int selected = JOptionPane.showConfirmDialog(_parent,DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          _targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"),
          JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) return;
    }

    // --- Create a temporary directory
    File tempDir = null;
    try {
      tempDir = File.createTempFile("EjsLauncher", ".tmp", _targetFile.getParentFile()); // Get a unique name for our temporary directory
      tempDir.delete();        // remove the created file
    } 
    catch (Exception exc) { exc.printStackTrace(); tempDir = null; }
    if (tempDir==null || !tempDir.mkdirs()) {
      String[] message=new String[]{res.getString("Package.JarFileNotCreated") ,res.getString("Package.NotTempDirError")};
      JOptionPane.showMessageDialog(_parent,message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    // Ready to start
    if (_output!=null) println(_output,res.getString("Package.PreparingFiles"));
    
    // --- Prepare the packager
    PackagerBuilder builder = new PackagerBuilder();
    builder.binDir = new File(_initDir,OsejsCommon.BIN_DIR_PATH);
    builder.outputDir = new File (_userDir,OsejsCommon.OUTPUT_DIR_PATH);
    builder.sourceDir = new File (_userDir,OsejsCommon.SOURCE_DIR_PATH);
    builder.configDir = new File (_userDir,OsejsCommon.CONFIG_DIR_PATH);;
    builder.target = _targetFile;
//    builder.processListDialog = _processListDialog;
    builder.output = _output;
    builder.parentComponent = _parent;
    builder.window = _window;

    builder.rebuilding = false;
    builder.tmpDir = tempDir;
    builder.simulationMetadataFiles = _simulationMetadataFiles;

    // Start the process as a new thread
    java.lang.Thread thread = new Thread(builder);
    thread.setPriority(Thread.NORM_PRIORITY);
    thread.start();

  }

  /**
   * Rebuilds a Launcher JAR file from an existing temporary directory plus a list of regenerated simulationMetadataFiles.
   * @param _recompiledWhenRebuilding List<TwoStrings> simulations already in the jar that were recompiled
   * @param _toBeAddedWhenRebuilding List<PathAndFile> Simulations to be added to the JAR
   * @param toBeRemovedWhenRebuilding List<TwoStrings> simulations already in the jar that must be removed
   * @param _initDir File EJS directory
   * @param _userDir File User workspace directory
   * @param _tmpDir  File The temporary directory where the jar has been uncompressed to
   * @param _targetFile File The desired target file
   * @param _processListDialog ProcessListDialog a dialog to show processes 
   * @param _output JTextArea A text area where to print messages
   * @param _parent JComponent The parent component for message dialogs
   * @param _window Window A window for reference
   */
  static public void rebuild (List<TwoStrings> _recompiledWhenRebuilding,
      List<PathAndFile> _toBeAddedWhenRebuilding, 
      List<TwoStrings> _toBeRemovedWhenRebuilding,
      File _initDir, 
      File _userDir, 
      File _tmpDir, 
      File _targetFile, 
      ProcessListDialog _processListDialog, 
      JTextArea _output, 
      JComponent _parent, 
      java.awt.Window _window) {

    // --- Prepare the packager
    PackagerBuilder builder = new PackagerBuilder();
    builder.binDir = new File(_initDir,OsejsCommon.BIN_DIR_PATH);
    builder.outputDir = new File (_userDir,OsejsCommon.OUTPUT_DIR_PATH);
    builder.sourceDir = new File (_userDir,OsejsCommon.SOURCE_DIR_PATH);
    builder.configDir = new File (_userDir,OsejsCommon.CONFIG_DIR_PATH);;
    builder.tmpDir = _tmpDir;
    builder.target = _targetFile;
//    builder.processListDialog = _processListDialog;
    builder.output = _output;
    builder.parentComponent = _parent;
    builder.window = _window;

    // List of all simulations finally in the JAR 
    builder.simulationMetadataFiles = new ArrayList<PathAndFile>(); 
    for (TwoStrings item : _recompiledWhenRebuilding) {
      File metadataFile = new File(builder.outputDir, item.getFirstString());
      if (metadataFile.exists()) builder.simulationMetadataFiles.add(new PathAndFile(item.getFirstString(),metadataFile)); // Check if the metadata file exists. If not, there was some compilation error
    }
    builder.simulationMetadataFiles.addAll(_toBeAddedWhenRebuilding); // Add already compiled simulation metadata files

    // rebuilding
    builder.rebuilding = true;
    builder.recompiledList = _recompiledWhenRebuilding;
//    builder.toBeRemoved = _toBeRemovedWhenRebuilding;
    builder.addedList = _toBeAddedWhenRebuilding;
    
    // Start the process as a new thread
    java.lang.Thread thread = new Thread(builder);
    thread.setPriority(Thread.NORM_PRIORITY);
    thread.start();
  }

  /**
   * For internal purposes only. Implementation of the Runnable interface. Not to be used by end-users
   * This is where the work is done!
   */
  public void run () {
    String rootName   = org.colos.ejs.library.utils.FileUtils.getPlainName(target);
    String binDirPath = FileUtils.getPath(binDir);
    String outputDirPath = FileUtils.getPath(outputDir);

    // Prepare the root launch node
    LaunchNode mainLaunchNode = new LaunchNode(rebuilding ? res.getString("EjsConsole.Added") : rootName);
    mainLaunchNode.setDisplayTab(0, res.getString("Generate.HtmlEjsGenerated"),"_ejs_library/html/EjsLauncher.html",null);
    String label = res.getString("Generate.HtmlEjsGenerated")+" Easy Java/Javascript Simulations "+_EjsSConstants.VERSION;
    mainLaunchNode.setTooltip(label);
    
//    if (rebuilding) { // remove old libraries and simulation class files
//      JarTool.remove(new File(tmpDir,"org/opensourcephysics"));
//      JarTool.remove(new File(tmpDir,"org/colos/ejs"));
//      JarTool.remove(new File(tmpDir,"ch/epfl/cockpit"));
//      JarTool.remove(new File(tmpDir,"com/calerga/sysquake"));
//      JarTool.remove(new File(tmpDir,"org/jibble/epsgraphics"));
//      ArrayList<TwoStrings> cleanList = new ArrayList<TwoStrings>();
//      cleanList.addAll(recompiledList);
//      cleanList.addAll(toBeRemoved);
//      for (TwoStrings ts : cleanList) { // Clean the old files of these
//        String classname = ts.getSecondString();
//        classname = classname.substring(0,classname.lastIndexOf('.')); // Remove the class name
//        classname = classname.replace('.','/'); // This is now the class directory 
//        File classDir = new File(tmpDir,classname);
//        JarTool.remove(classDir);
//      }
//    }

    // Create an instance of MiniJar and prepare it. MiniJar will NOT compress the target file
    MiniJar minijar= new MiniJar();
//    minijar.setVerbose(false);
    minijar.addExclude ("++Thumbs.db");
    minijar.addExclude ("++.DS_Store");
    minijar.addDesired("org/opensourcephysics/resources/++");
    minijar.addDesired("org/opensourcephysics/numerics/++.class");
    minijar.addDesired("org/opensourcephysics/ode/++.class");
    minijar.addDesired("org/opensourcephysics/tools/Launcher.class");
    minijar.addDesired("org/colos/ejs/library/resources/++");
    minijar.addSourcePath(FileUtils.getPath(new File(binDir,"osp.jar")));
    minijar.addSourcePath(FileUtils.getPath(new File(binDir,"ejs_lib.jar")));
    for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (binDir,OsejsCommon.EXTENSIONS_DIR_PATH))) { // Add extension files
      minijar.addSourcePath(FileUtils.getPath(paf.getFile()));
    }

    // Prepare the dictionary and the error buffer
    Dictionary<File,String> dictionary = new Hashtable<File,String>(); // This is a dictionary that translates from app directory to class
    StringBuffer errorMessage = new StringBuffer();
    // Prepare the progress dialog
    int steps = simulationMetadataFiles.size(), interval=1, counter=0;
    if (steps>10) {
      interval = Math.round(steps/10.0f);
      steps = 10;
    }
    ProgressDialog pD = new ProgressDialog(steps+1,res.getString("Package.PackageAllSimulations"), new Dimension(350,150),OsejsCommon.getScreenBounds(window));
    String pdMessage = res.getString("Package.PreparingFiles");
    
    // Process all simulation meta data files adding parameters to the MiniJar, fill the dictionary and possible missing auxiliary files
    Set<String> missingAuxFiles = new HashSet<String>();
    for (PathAndFile metadataPathAndFile : simulationMetadataFiles) {
      if (counter % interval==0) pD.reportProgress(pdMessage);
      counter++;
      String relPath = FileUtils.getRelativePath(metadataPathAndFile.getFile(), outputDirPath, false);
      if (relPath.endsWith(Metadata.EXTENSION)) relPath = relPath.substring(0,relPath.length()-Metadata.EXTENSION.length());
      if (!metadataPathAndFile.getFile().exists()) {
        errorMessage.append(res.getString("Package.AppDirNotExistingError")+" "+relPath);
        continue;
      }
      println (output,res.getString("Package.ProcessingSimulation") + " "+org.colos.ejs.library.utils.FileUtils.getPlainName(metadataPathAndFile.getFile()));

      errorMessage.append(processSimulationFiles(minijar,binDirPath,outputDirPath,
            sourceDir,tmpDir,metadataPathAndFile,label,
            addedList==null || addedList.contains(metadataPathAndFile) ? mainLaunchNode : null, 
            dictionary,missingAuxFiles,output));
    }
    
    // --- Now compile the MiniJar list of dependent files
    Set<PathAndFile> matches = minijar.getMatches();

    for (PathAndFile paf : matches) { // add classes which seem to use reflection
      String path = paf.getPath(); 
//      if (path.startsWith("com/sun/j3d/loaders/vrml97/")) System.err.println (path);
      if (path.endsWith("/ElementObjectVRML.class")) { // Add loader libraries
//        minijar.addForced("com/sun/j3d/loaders/vrml97/++");
        minijar.addForced("loaders/vrml/++");
      }
      else if (path.endsWith("/ElementObject3DS.class")) { // Add loader libraries
        minijar.addForced("loaders/3ds3/++");
      }
    }
    matches = minijar.getMatches(); // a second round is required for loaders which use reflection

    // Print missing files
//    Set<String> missingList = minijar.getMissingFilesList();
//    for (String missingFilename : missingList ) System.out.println ("Missing file: "+missingFilename); 

    // Copy files in the matches list to the temporary directory
    for (PathAndFile paf : matches) {
      if (!FileUtils.copy(paf.getInputStream(), new File(tmpDir,paf.getPath()))) 
        errorMessage.append(res.getString("Generate.JarFileResourceNotFound")+" "+paf.getPath()+"\n");
    }

    // Check for errors
    String error = errorMessage.toString().trim();
    if (error.length()>0) { // There was an error!
      String[] message=new String[]{res.getString("Package.JarFileNotCreated") ,error};
      JOptionPane.showMessageDialog(parentComponent,message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
      JarTool.remove(tmpDir);
      pD.dispose();
      return;
    }

    OsejsCommon.warnAboutFiles(parentComponent,missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");

    // Copy all files in the distribution library directory 
    File binConfigDir = new File (binDir,OsejsCommon.CONFIG_DIR_PATH);
    String binConfigDirPath = FileUtils.getPath(binConfigDir);
    for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH)))
      JarTool.copy(file,new File(tmpDir,FileUtils.getRelativePath(file, binConfigDirPath, false)));
    
    // But overwrite them with user defined files (if any)
    String configDirPath = FileUtils.getPath(configDir);
    for (File file : JarTool.getContents(new File(configDir,OsejsCommon.EJS_LIBRARY_DIR_PATH)))
      JarTool.copy(file,new File(tmpDir,FileUtils.getRelativePath(file, configDirPath, false)));
    
    { // --- Write or update the package information file
      List<String> appsList = new ArrayList<String>();
      for (PathAndFile appPathAndFile : simulationMetadataFiles) { // Build the list of applications
        String mainClass = dictionary.get(appPathAndFile.getFile());
        if (!appsList.contains(mainClass)) appsList.add(mainClass);
      }
      Collections.sort(appsList);

      // Save the list of classes for next time
      StringBuffer pckgBuffer = new StringBuffer();
      pckgBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      pckgBuffer.append("<package name=\""+rootName+"\">\n");
      for (String app : appsList) {
        int index = app.lastIndexOf('.');
        String name = index>=0 ? app.substring(index+1) : app;
        pckgBuffer.append("  <simulation name=\""+name+"\">\n");
        pckgBuffer.append("    <class>"+app+"</class>\n");
        pckgBuffer.append("    <applet>"+app+"Applet</applet>\n");
        pckgBuffer.append("  </simulation>\n");
      }
      pckgBuffer.append("</package>\n");
      try {  FileUtils.saveToFile(new File (tmpDir,"EjsPackageInfo.xml"), null, pckgBuffer.toString()); }
      catch (Exception exc) {
        exc.printStackTrace();
        println(output,res.getString("Osejs.File.CantCreateFile")+" EjsPackageInfo.xml");
      }
    }

    // --- Write the Launcher description file
    Launcher launcher = new Launcher(false);
//      Launcher.setJarsOnly(false);
    launcher.setEditorEnabled(true);

    String launcherFilename = null;
    if (rebuilding) {
      //launcher.setHasToShow(false);
      String xsetName = getXsetName(tmpDir);
      if (xsetName==null) xsetName = rootName;
      //println (output,"xset = "+xsetName);
      XMLControlElement old_control = new XMLControlElement();
      old_control.read(tmpDir.getAbsolutePath() + "/" + xsetName+".xset");
      launcher.open(old_control.toXML()); // This line requires launcher.setHasToShow(false);
      List<LaunchNode> allNodes = recursiveGetLaunchChildren(new ArrayList<LaunchNode>(), launcher.getRootNode());
      // Convert possible old formats for the packages:
      for (LaunchNode oneNode : allNodes) {
        String launchClassname = oneNode.getLaunchClassName();
        if (launchClassname==null) continue; // Not a class node
        for (TwoStrings ts : recompiledList) { // Convert possible old formats of the class name for the recompiled packages
          if (ts.getSecondString().equals(launchClassname)) {
            String newClassname = Metadata.getClassname(new File(outputDir,ts.getFirstString()));
            oneNode.setLaunchClass(newClassname);
          }
        }
//        for (TwoStrings ts : toBeRemoved) { Wolfgang wants to keep these // Remove undesired entries
//          if (ts.getSecondString().equals(launchClassname)) launcher.getRootNode().remove(oneNode);
//        }
      }
      // Add the new entries
      if (mainLaunchNode.getChildCount()>0) {
        XMLControl addednodeControl = new XMLControlElement(mainLaunchNode);
        launcher.open(addednodeControl.toXML());
      }
      Launcher.LaunchSet set = launcher.new LaunchSet();
      XMLControl control = new XMLControlElement(set);
      launcherFilename = tmpDir.getAbsolutePath()+"/"+xsetName+".xset";
      control.write(launcherFilename);
    }
    else { // Not rebuilding
      // Add the new entries
      XMLControl rootnodeControl = new XMLControlElement(mainLaunchNode);
      //    rootnodeControl.setValue("editor_enabled", true);
      launcher.open(rootnodeControl.toXML());
      Launcher.LaunchSet set = launcher.new LaunchSet();
      XMLControl control = new XMLControlElement(set);
      control.write(tmpDir.getAbsolutePath()+"/"+rootName+".xset");
      
      // --- Write the default xset file
      StringBuffer xsetBuffer = new StringBuffer();
      xsetBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      xsetBuffer.append("<object class=\"org.opensourcephysics.tools.Launcher$LaunchSet\">\n");
      xsetBuffer.append("    <property name=\"launch_nodes\" type=\"collection\" class=\"java.util.ArrayList\">\n");
      xsetBuffer.append("        <property name=\"item\" type=\"string\">"+rootName+".xset</property>\n");
      xsetBuffer.append("    </property>\n");
      xsetBuffer.append("</object>\n");
      launcherFilename = tmpDir.getAbsolutePath()+"/launcher_default.xset";

      try { FileUtils.saveToFile(new File (launcherFilename), null, xsetBuffer.toString()); }
      catch (Exception exc) {
        exc.printStackTrace();
        println(output,res.getString("Package.JarFileNotCreated"));
        pD.dispose();
        return;
      }
    }

    // Preparatory work done
    pD.dispose();

    // --- Now call LaunchBuilder and wait for it to finish
    if (output!=null) println(output,res.getString("Package.WaitingLaunchBuilder"));

//    LaunchClassChooser.setBasePath(tmpDir.getAbsolutePath());
//    LaunchBuilder builder = new LaunchBuilder(launcherFilename,false); // File workingDirectory, boolean splash
//    Launcher.setJarsOnly(false);
//    builder.setCanExit(false); // Do not exit when the window is closed
//    builder.setVisible(true);
//
//    int counterRunner=0;
//    while (builder.isVisible()) {
//      System.err.println ("Waiting "+ (++counterRunner)); 
//      try { Thread.sleep(1000); }
//      catch (Exception exc) { exc.printStackTrace(); }
//    }
//    boolean launcherSucceeded = true;

    Runner runner = new Runner(processListDialog,binDir,tmpDir);
    final BooleanValue launchBuilderPending = new BooleanValue(true);
    final java.lang.Thread thread = new Thread(runner);
    thread.setPriority(Thread.NORM_PRIORITY);

    SwingUtilities.invokeLater(new Runnable(){
      public void run() {
        thread.start();
        launchBuilderPending.value = false;
      }
    });

    try { Thread.sleep(1000); }
    catch (Exception exc) { exc.printStackTrace(); }
    Thread.yield();

    while (thread.isAlive() || launchBuilderPending.value) {
      try { Thread.sleep(1000); }
      catch (Exception exc) { exc.printStackTrace(); }
    }
    boolean launcherSucceeded = runner.succeeded();
    
    // --- Compress the resulting package and remove the temporary file
    if (output!=null) println(output,res.getString("Package.PackagingJarFile")+" "+target.getName());
    boolean success = false;
    if (launcherSucceeded) { 
      // pack everything into a single compressed file
      if (JarTool.compress(tmpDir, target, JarTool.createManifest(null,"org.opensourcephysics.tools.Launcher"))) success = true;
      else {
        String[] message=new String[]{res.getString("Package.JarFileNotCreated"),
                                      res.getString("Package.CompressError")+target.getAbsolutePath()};
        JOptionPane.showMessageDialog(parentComponent,message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
      }
    }

    // Wait a bit so that no process blocks the removing process
    try { Thread.sleep(500); } // Thread.currentThread().sleep(500); }
    catch (Exception exc) {}
    // Remove the tab so that the HTML file is released and can be deleted
    mainLaunchNode.removeDisplayTab(0);
    JarTool.remove(tmpDir); // remove the temporary directory

    // --- And that's it!
    if (output!=null) {
      if (success) println(output,res.getString("Package.JarFileCreated")+" "+target.getName());
      else println(output,res.getString("Package.JarFileNotCreated"));
    }
  }

  /**
   * Gets all the XMLControls under a given one
   * @param _list
   * @param _control
   * @return
   */
  static private List<LaunchNode> recursiveGetLaunchChildren(List<LaunchNode> _list, LaunchNode _node) {
    _list.add(_node);
    for (Enumeration<?> en = _node.children(); en.hasMoreElements(); ) 
      recursiveGetLaunchChildren(_list,(LaunchNode) en.nextElement());
    return _list;
  }
  
  /**
   * Process a simulation directory:
   * a) Copy all files in it
   * b) find references to the library jar files
   * c) fill the dictionary
   * d) add a Launch node to the root node
   */
  static private StringBuffer processSimulationFiles (MiniJar _minijar, String _binDirPath, String _outputDirPath, File _sourceDir,
      File _tempDir, PathAndFile _metadataPathAndFile, String _label, LaunchNode _rootnode,
      Dictionary<File,String> _dictionary, Set<String> missingAuxFiles, JTextArea _output) {
    
    String plainName = org.colos.ejs.library.utils.FileUtils.getPlainName(_metadataPathAndFile.getFile());
    File generatedDir = _metadataPathAndFile.getFile().getParentFile();
    String relPath = FileUtils.getRelativePath(generatedDir, _outputDirPath,false);
    // Read the meta data file
    Metadata metadata = Metadata.readFile(_metadataPathAndFile.getFile(), relPath);

    // Process the main class
    if (metadata.getClassname()==null) 
      return new StringBuffer(res.getString("Package.IncorrectMetadata")+" "+FileUtils.getPath(_metadataPathAndFile.getFile())+".\n");
    _dictionary.put(_metadataPathAndFile.getFile(),metadata.getClassname()); // This will be used to create EjsPackageInfo.xml
    String classname = metadata.getClassname().replace('.', '/');
    _minijar.addSourcePath(FileUtils.getPath(new File(generatedDir,plainName+".jar")));
    _minijar.addDesired(classname+".class"); // add the main class file to the minijar search
    _minijar.addDesired(classname+"Applet.class"); // add also the applet, for completeness

    // Add the XML file
//    File xmlFile = new File (_metadataPathAndFile.getFile().getParentFile(), plainName+".ejs");
//    if (!xmlFile.exists()) xmlFile = new File (_metadataPathAndFile.getFile().getParentFile(), plainName+".xml"); // Try older format

    for (String filename : metadata.getFilesCreated())
      if (filename.endsWith(".xml") || filename.endsWith(".ejs") || filename.endsWith(".metadata")) {
        File xmlFile = new File (_metadataPathAndFile.getFile().getParentFile(), filename);
        if (xmlFile.exists() && !JarTool.copy(xmlFile, new File(_tempDir,relPath+xmlFile.getName())))
          return new StringBuffer(res.getString("Package.CopyError") + " " + FileUtils.getPath(xmlFile) + "\n");
      }
    
    // Add the _Intro HTML files
    String prefix = plainName+"_Intro ";
    for (String filename : metadata.getFilesCreated()) 
      if (filename.startsWith(prefix) && filename.endsWith(".html")) 
        if (!JarTool.copy(new File(generatedDir,filename), new File(_tempDir,relPath+filename))) 
          return new StringBuffer(res.getString("Package.CopyError") + " " + relPath+filename + "\n");

    // Copy auxiliary files
    for (String auxPath : metadata.getAuxiliaryFiles()) {
      File auxFile = new File(_sourceDir,auxPath);
      if (!auxFile.exists()) {
        missingAuxFiles.add(auxPath);
        println(_output,res.getString("Generate.JarFileResourceNotFound")+": "+auxPath);
      }
      else if (auxFile.isDirectory()) { // It is a complete directory
        for (File file : JarTool.getContents(auxFile)) {
          if (!JarTool.copy(file, new File(_tempDir,FileUtils.getRelativePath(file,_sourceDir,false))))
            return new StringBuffer(res.getString("Package.CopyError") + " " + FileUtils.getPath(auxFile) + "\n");
        }
      }
      else if (!JarTool.copy(auxFile, new File(_tempDir,auxPath))) 
        return new StringBuffer(res.getString("Package.CopyError") + " " + auxPath + "\n");
    }

    // Add user jars files. These won't get as jars inside the target jar, but their contents will
    // We need them as proper jar files in order to extract them later on
    for (String jarPath : metadata.getJarFiles()) {
      File jarFile = new File(_sourceDir,jarPath);
      if (!jarFile.exists()) {
        missingAuxFiles.add(jarPath);
        println(_output,res.getString("Generate.JarFileResourceNotFound")+": "+jarPath);
      }
      else {
        if (!JarTool.copy(jarFile, new File(_tempDir,jarPath))) 
          return new StringBuffer(res.getString("Package.CopyError") + " " + jarPath + "\n");
        _minijar.addSourcePath(FileUtils.getPath(jarFile));
      }
    }
    
    for (String filename : metadata.getPackageList()) {
      _minijar.addDesired(filename);
    }

    // Add a Launcher node
    if (_rootnode!=null) { // will be null for simulations compiled and in the original JAR (rebuilding)
      LaunchNode node = new LaunchNode(plainName);
      node.setSingleVM(true); // This is very important for external applications!
      node.setLaunchClass(metadata.getClassname());
      node.setDescription(_label);
      metadata.fillLaunchNode(node);

      // Process the contents HTML file to obtain the list of description HTML pages
      File htmlContentsFile = new File (generatedDir,plainName+"_Contents.html");
      if (htmlContentsFile.exists()) { // add user-defined HTML pages to the node
        try {
          int counter=1;
          Reader reader = new FileReader(htmlContentsFile);
          LineNumberReader l = new LineNumberReader(reader);
          String sl = l.readLine();
          while (sl != null) {
            //          code.append("      <div class=\"intro\"><a href=\""+ link+"\" target=\"central\">" + editor.getName()+"</a></div>"+ret);
            int index = sl.indexOf("class=\"intro\"");
            if (index>=0) { // Found an entry
              String link = plainName + "_Intro "+counter+".html"; // default link
              int linkBegin = sl.indexOf("<a href=\"",index);  // find the actual link
              if (linkBegin>0) {
                String linkPiece = sl.substring(linkBegin+9);
                link = linkPiece.substring(0,linkPiece.indexOf("\" target"));
              }
              int beg = sl.indexOf("\"central\">",index);
              if (beg>0) {
                String piece = sl.substring(beg+10);
                String title = piece.substring(0,piece.indexOf('<'));
                //              System.out.println("Add <"+title+">="+link);
                if (link.startsWith("../")) { // This is for external HTML files
                  while (link.startsWith("../")) link = link.substring(3);
                  node.setDisplayTab(counter-1,title,link,null);
                }
                else node.setDisplayTab(counter-1,title,relPath+link,null);
                counter++;
              }
            }
            sl = l.readLine();
          }
          reader.close();
        } catch (Exception ex) { ex.printStackTrace(); }
      }
      _rootnode.add(node);
    }

    return new StringBuffer();
  }

  static private class Runner implements Runnable {
    File tempDir, binDir;
    boolean success = true;
    ProcessListDialog listDialog;

    Runner(ProcessListDialog _listDialog, File _binDir, File _tempDir) {
      listDialog = _listDialog;
      tempDir = _tempDir; 
      binDir = _binDir;
    }

    public void run () {
      Process proc=null;
      GeneratedOutput generatedOutput=null, generatedError=null;
      try {
        final Vector<String> cmd = new Vector<String>();
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) cmd.add(javaHome + java.io.File.separator + "bin" + java.io.File.separator + "java");
        else cmd.add("java");
        cmd.add("-classpath");
        cmd.add("."+File.pathSeparator+FileUtils.getAbsolutePath("osp.jar",binDir)+File.pathSeparator+FileUtils.getAbsolutePath("ejs_lib.jar",binDir));
        cmd.add("org.opensourcephysics.tools.LaunchBuilder");
//        System.err.println ("Running in "+tempDir.getAbsolutePath());
//        for (int i=0,n=cmd.size(); i<n; i++) System.err.println("Cmd["+i+"] = "+cmd.get(i));

        proc = Runtime.getRuntime().exec(cmd.toArray(new String[0]), null, tempDir);
        if (listDialog!=null) listDialog.addProcess(proc, "LaunchBuilder");
        
        generatedOutput = new GeneratedOutput(proc, false);
        Thread thread = new Thread(generatedOutput);
        thread.setPriority(java.lang.Thread.MIN_PRIORITY);
        thread.start();
        generatedError = new GeneratedOutput(proc, true);
        Thread thread2 = new Thread(generatedError);
        thread2.setPriority(java.lang.Thread.MIN_PRIORITY);
        thread2.start();
        int error = proc.waitFor();
        generatedOutput.stop();
        generatedError.stop();
        success = (error==0);
      }
      catch (Exception exc) { 
        exc.printStackTrace(); 
        success = false; 
        if (generatedOutput!=null) generatedOutput.stop();
        if (generatedError!=null) generatedError.stop();
      }
      if (listDialog!=null && proc!=null) listDialog.removeProcess(proc);
    }

    public boolean succeeded() { return success; }

  } // End of inner class


}


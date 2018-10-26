/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs;

import org.colos.ejs._EjsSConstants;
//import org.colos.ejs.library.server.SocketView.Message;
//import org.colos.ejs.library.server.utils.MetadataAPI;
import org.colos.ejs.library.server.utils.MetadataBuilder;
import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.TabbedEvolutionEditor;
import org.colos.ejs.osejs.edition.html.HtmlEditor;
import org.colos.ejs.osejs.edition.html.OneHtmlPage;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejss.xml.JSObfuscator;
import org.opensourcephysics.tools.minijar.MiniJar;
import org.opensourcephysics.tools.minijar.PathAndFile;
import org.opensourcephysics.controls.Cryptic;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
//import java.util.Map;
//import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.*;

import javax.json.Json;
//import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.*;


//--------------------

public class Generate {
  static private final ResourceUtil res    = new ResourceUtil("Resources");
  static public final String sVersionInfo = "generated-with: Easy Java/Javascript Simulations";
 
  /**
   * Generates a simulation and all its auxiliary files
   * @param _ejs Osejs
   * @param _filename String The Java file for the simulation
   * @param _relativePath String The full path of the XML file
   * @return File the main class file generated
   */
  static public File generate (Osejs _ejs, boolean _serverMode, int _port) {

    String binDirPath = FileUtils.getPath(_ejs.getBinDirectory());
    String srcDirPath = FileUtils.getPath(_ejs.getSourceDirectory());

    // Get the filename and relative path information
    final File xmlFile = _ejs.getCurrentXMLFile();
    final TwoStrings fileNameAndAxtension = FileUtils.getPlainNameAndExtension(xmlFile);
    String filename = _ejs.getSimInfoEditor().getSimulationName();
    if (filename==null) filename = fileNameAndAxtension.getFirstString(); // the plain filename
    
    final String relativePath = FileUtils.getRelativePath(xmlFile,srcDirPath,false); // The relative path of the XML file
    String parentPath; // The relative path of the parent directory
    String pathToLib = ""; // Levels up to get to the _ejs_library

    int index = relativePath.lastIndexOf('/');
    if (index>=0) parentPath = relativePath.substring(0,index+1); // including the '/'
    else parentPath = "";
    char[] pathChars = relativePath.toCharArray();
    for (int i=0; i<pathChars.length; i++) if (pathChars[i]=='/') pathToLib += "../";
    if (_ejs.isVerbose()) {
      System.out.println("Generating:\n");
      System.out.println("  name "+filename);
      System.out.println("  relative path "+relativePath);
      System.out.println("  parent path "+parentPath);
      System.out.println("  path to lib "+pathToLib);
    }
    if (_ejs.isJustCompiling()) System.out.println(relativePath+": "+res.getString("Generate.Compiling"));
    // Prepare to start
    _ejs.getOutputArea().println(res.getString("Generate.Compiling")+ " "+ filename + "...");
    File generationDirectory = new File(_ejs.getOutputDirectory(),parentPath);
    generationDirectory.mkdirs();

    String classname = OsejsCommon.getValidIdentifier(filename);  // a legal name for the Java class to create. Must match PackageBuilder !
    switch (classname.length()) { // make sure the name is at least three characters long. CreateTempFile requires this
      case 1 : classname += "__"; break;
      case 2 : classname += "_"; break;
      default: break; // do nothing
    }
    String packageName = getPackageName (xmlFile,classname,parentPath); // a legal name for the package of the class to create
    Set<PathAndFile> jarList = getPathAndFile(_ejs, xmlFile.getParentFile(), parentPath, _ejs.getSimInfoEditor().getUserJars()); // user JAR files
    Set<PathAndFile> resList = getPathAndFile(_ejs, xmlFile.getParentFile(), parentPath, _ejs.getSimInfoEditor().getAuxiliaryFilenames(false)); // auxiliary files (includes the translation files)

    File jarFile = new File (generationDirectory,filename+".jar");

    StringBuffer buffer = new StringBuffer(); // hold the execution path // getExecPath(jarFile,binDirPath,srcDirPath,outDirPath,jarList,false);
    buffer.append(binDirPath+"osp.jar"+File.pathSeparator);
    buffer.append(binDirPath+"ejs_lib.jar"+File.pathSeparator);
    // Add extension files
    String extDirPath = binDirPath + OsejsCommon.EXTENSIONS_DIR_PATH + "/";
    for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (_ejs.getBinDirectory(),OsejsCommon.EXTENSIONS_DIR_PATH))) {
      buffer.append(extDirPath+paf.getPath()+File.pathSeparator);
    }
    buffer.append(FileUtils.getPath(jarFile)+File.pathSeparator);
    for (PathAndFile paf : jarList) buffer.append(srcDirPath+paf.getPath()+File.pathSeparator);
    //_ejs.setExecutionParameters (metadata.getClassname(),buffer.toString());    

    String mainFrame = _ejs.getViewEditor().generateCode(Editor.GENERATE_MAIN_WINDOW, "").toString().trim();
    if (mainFrame.length()<=0) mainFrame = "\"EmptyFrame\"";

    Metadata metadata = new Metadata(_ejs.getSimInfoEditor().saveString(), packageName +"."+classname, buffer.toString(), jarList,
        _ejs.getViewEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED_BY_PACKAGE,"").toString(),
        _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED_BY_PACKAGE,"").toString()+_ejs.getSimInfoEditor().getRequiredClasses().toString(),
        _ejs.getViewEditor().getTree().getMainWindowDimension(),mainFrame,_ejs.getSimInfoEditor().addAppletColSupport(),_ejs.getSimInfoEditor().getManifestEntries());

    // Here we go
    File classesDir = null;
    try {
      // Create and save the Java files
      java.util.List<File> generatedFiles = new ArrayList<File>();
      String qualifiedClassname;
      int indexOfDot = packageName.lastIndexOf('.');
      if (indexOfDot>=0) qualifiedClassname = packageName.substring(index+1) +"/"+classname;
      else qualifiedClassname = packageName + "/" + classname;
      generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + ".java"), null, // the model file
          generateModel (_ejs,classname,packageName,relativePath,parentPath,generationDirectory,resList,mainFrame,_serverMode,_port)));
      generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "Simulation.java"), null, // the simulation file
          generateSimulation(_ejs,classname,packageName, filename, mainFrame,_serverMode)));
      if (_serverMode) {
        generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "ServerView.java"), null, // the view file
            generateServerView(_ejs,classname,packageName)));
        //        generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "DummyView.java"), null, // the view file
        //            generateDummyView(_ejs,classname,packageName)));
        generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "View.java"), null, // the view file
            generateSwingView(_ejs,classname,packageName,_serverMode)));
      } 
      else  {
        if (_ejs.getViewEditor().isEmpty()) { 
          generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "DummyView.java"), null, // the view file
              generateEmptyView(_ejs,classname,packageName)));
        }
        else {
          generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "View.java"), null, // the view file
              generateSwingView(_ejs,classname,packageName,_serverMode)));
        }
      }
      //CJB for collaborative
        generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "Applet.java"), null, // the Applet file
            generateApplet(_ejs,classname, packageName, parentPath, mainFrame)));
      if(_ejs.getSimInfoEditor().addAppletColSupport()){
        generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "AppletStudent.java"), null, // the AppletStudent file
            generateAppletStudent(_ejs,classname, packageName, parentPath, mainFrame)));
      }
      //CJB for collaborative

      // Compilation
      classesDir = File.createTempFile(classname, ".tmp", _ejs.getOutputDirectory()); // Get a unique temporary file
      classesDir.delete(); // remove the created file, we were only interested in getting a unique filename
      if (!classesDir.mkdirs()) {
        _ejs.getOutputArea().println (res.getString("Package.NotTempDirError")+" "+FileUtils.getPath(classesDir));
        if (_ejs.isJustCompiling()) {
          String message = res.getString("Package.NotTempDirError")+" "+FileUtils.getPath(classesDir);
          System.out.println(message);
          FileUtils.saveToFile(new File (generationDirectory,"error.txt"),null, message+"\n");
        }
        return null;
      }

      // Add language resources, if needed
      //      Set<File> languageResourcesFiles = new HashSet<File>(); // Keep the list of them so that they will be added to packages
      if (_ejs.getSimInfoEditor().addTranslatorTool() && (_serverMode==false)) {
        File resourcesDir = new File(classesDir,packageName.replace('.', '/'));
        for (LocaleItem item : _ejs.getTranslationEditor().getDesiredTranslations()) {
          String lrFilename = item.isDefaultItem() ? classname + ".properties" : classname + "_"+item.getKeyword()+".properties";
          FileUtils.saveToFile(new File(resourcesDir,lrFilename), LocaleItem.getCharset(), _ejs.getTranslationEditor().getResources(item));
        }
      }

      String genDirPath = FileUtils.getPath(generationDirectory);
      java.util.List<File> compileFiles = new ArrayList<File>(generatedFiles);
      // Add the external libraries
      String externalFiles = _ejs.getModelEditor().getLibraryEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED, "").toString();
      StringTokenizer tkn = new StringTokenizer(externalFiles,";");
      while (tkn.hasMoreTokens()) {
        String externalFilename =  tkn.nextToken();
//        System.err.println("Filename = "+externalFilename);
        File externalFile;
        if (externalFilename.startsWith("./")) externalFile = new File(xmlFile.getParentFile(),externalFilename.substring(2));
        else externalFile = new File(_ejs.getSourceDirectory(),externalFilename);
        if (externalFile.exists()) compileFiles.add(externalFile);
        else {
          _ejs.getOutputArea().println (res.getString("Generate.JarFileResourceNotFound")+" "+externalFilename);
          if (_ejs.isJustCompiling()) System.out.println(relativePath+": "+res.getString("Generate.JarFileResourceNotFound")+" "+externalFilename);
        }
      }

      boolean ok = compile(_ejs,FileUtils.getPath(classesDir),compileFiles,getClasspath(_ejs.getBinDirectory(),binDirPath,srcDirPath,jarList));

      // Remove Java files, if configured to do so
      if (_ejs.getOptions().removeJavaFile()) for (File file : generatedFiles) file.delete();
      else for (File file : generatedFiles) metadata.addFileCreated(file,genDirPath);

      if (!ok) { // Compilation failed
        String message = res.getString("Generate.CompilationError");
        _ejs.getOutputArea().println (message);
        if (_ejs.isJustCompiling()) {
          System.out.println(relativePath+": "+message);
          FileUtils.saveToFile(new File (generationDirectory,"error.txt"),null, message+FileUtils.getPath(xmlFile)+"\n");
        }
        JarTool.remove(classesDir);
        return null;
      }

      // Compress the classes directory
      if (!JarTool.compress(classesDir, jarFile, null)) {
        _ejs.getOutputArea().println (res.getString("Package.JarFileNotCreated")+" "+FileUtils.getPath(jarFile));
        if (_ejs.isJustCompiling()) {
          String message = res.getString("Package.JarFileNotCreated")+" "+FileUtils.getPath(jarFile);
          System.out.println(message);
          FileUtils.saveToFile(new File (generationDirectory,"error.txt"),null, message+"\n");
        }
        JarTool.remove(classesDir);
        return null;
      }
      metadata.addFileCreated(jarFile,genDirPath);
      JarTool.remove(classesDir);
      _ejs.getOutputArea().println (res.getString("Generate.CompilationSuccessful"));

      // Generate HTML files
      buffer = new StringBuffer (); // holds the archive tag string
      buffer.append("common.jar,"+parentPath+filename+".jar"+",");
      for (PathAndFile paf : jarList) buffer.append(paf.getPath()+",");
      String archiveTag = buffer.toString();
      if (archiveTag.endsWith(",")) archiveTag = archiveTag.substring(0,archiveTag.length()-1);

      if (_ejs.getOptions().generateHtml()!=EjsOptions.GENERATE_NONE) {
        Hashtable<String,StringBuffer> htmlTable = new Hashtable<String,StringBuffer>();
        switch (_ejs.getOptions().generateHtml()) {
          case EjsOptions.GENERATE_ONE_PAGE : 
            addNoFramesHtml(htmlTable, _ejs,filename,classname,packageName,pathToLib,archiveTag); 
            break;
          case EjsOptions.GENERATE_LEFT_FRAME : 
          case EjsOptions.GENERATE_TOP_FRAME : 
            addFramesHtml(htmlTable, _ejs,filename,classname,packageName,pathToLib,archiveTag,true); // true = hasView
            addDescriptionHtml(htmlTable, _ejs,filename,pathToLib);
            break;
        }
        // Now, save them all
        for (String key : htmlTable.keySet()) {
          String htmlCode = htmlTable.get(key).toString();
          String postFix;
          if (htmlCode.startsWith("<?xml ")) postFix = key + ".xhtml";
          else postFix = key + ".html";
          FileUtils.saveToFile (metadata.addFileCreated(new File (generationDirectory,classname+postFix),genDirPath), null, htmlCode);
        }
      }

      // Include the XML model
      if (_ejs.getOptions().includeModel()) {
        String modelName = filename+"."+fileNameAndAxtension.getSecondString(); // Use same extension as the original
        JarTool.copy(xmlFile, metadata.addFileCreated(new File (generationDirectory,modelName),genDirPath));
      }

      // Copy auxiliary files
      Set<String> copyFiles = new HashSet<String>();
      Set<String> missingAuxFiles = new HashSet<String>();

      // Get a unique list of files to copy (so that not to repeat a file)
      for (String auxPath : metadata.getAuxiliaryFiles()) {
        File auxFile;
        if (auxPath.startsWith("./")) auxFile = new File(xmlFile.getParentFile(),auxPath.substring(2));
        else auxFile = new File(_ejs.getSourceDirectory(),auxPath);
        if (!auxFile.exists()) missingAuxFiles.add(auxPath);
        else if (auxFile.isDirectory()) { // It is a complete directory
          if (!auxPath.endsWith("/")) auxPath += "/";
          for (File file : JarTool.getContents(auxFile)) copyFiles.add(auxPath+"/"+FileUtils.getRelativePath(file, auxFile, false));
        }
        else copyFiles.add(auxPath);
      }
      // Now do the copying
      for (String auxPath : copyFiles) {
        File sourceFile, targetFile;
        if (auxPath.startsWith("./")) {
          String path = auxPath.substring(2);
          sourceFile = new File(xmlFile.getParentFile(),path);
          targetFile = new File(generationDirectory,path);
        }
        else {
          sourceFile = new File(_ejs.getSourceDirectory(),auxPath);
          targetFile = new File(_ejs.getOutputDirectory(),auxPath);
        }
        if (!sourceFile.exists()) missingAuxFiles.add(auxPath);
        else JarTool.copy(sourceFile, targetFile);
      }

      OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");

      copyEJSLibrary(_ejs);

      // Save the meta data
      File metadataFile = new File (generationDirectory,filename+Metadata.EXTENSION);
      metadata.saveToFile(metadata.addFileCreated(metadataFile,genDirPath));

      // Report generation done correctly
      _ejs.getOutputArea().println(res.getString("Generate.GenerationOk"));
      if (_ejs.isJustCompiling()) System.out.println(relativePath+": "+res.getString("Generate.GenerationOk"));

      return metadataFile;
    }
    catch (IOException ex) {
      _ejs.getOutputArea().println(res.getString("Generate.GenerationError"));
      _ejs.getOutputArea().println("System says :\n  " + ex.getMessage());
      if (_ejs.isJustCompiling()) {
        String message = res.getString("Generate.GenerationError");
        System.out.println(relativePath+": "+message);
        try {  FileUtils.saveToFile(new File (generationDirectory,"error.txt"), null, message+FileUtils.getPath(xmlFile)+"\n"); }
        catch (Exception exc2) { exc2.printStackTrace(); }
      }
      if (classesDir!=null) JarTool.remove(classesDir);
      return null;
    }
  }

  static public void copyEJSLibrary(Osejs _ejs) {
    // Add all the files in the library directory 
    File binConfigDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
    String binConfigDirPath = FileUtils.getPath(binConfigDir);
    for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
      String destName = FileUtils.getRelativePath(file, binConfigDirPath, false);
      //      System.err.println("Copying "+file +" to "+new File (_ejs.getOutputDirectory(),destName));
      JarTool.copy(file, new File (_ejs.getOutputDirectory(),destName));
    }

    // Overwrite files in the library directory with user defined files (if any)
    String configDirPath = FileUtils.getPath(_ejs.getConfigDirectory());
    for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css"))) {
      String destName = FileUtils.getRelativePath(file, configDirPath, false);
      //      System.err.println("Copying 2 "+file +" to "+new File (_ejs.getOutputDirectory(),destName));
      JarTool.copy(file, new File (_ejs.getOutputDirectory(),destName));
    }
  }

  /**
   * Compiles the generated Java files
   * @param _ejs Osejs The calling EJS
   * @param _javaFiles File[] The Java files for the simulation's code
   * @param _classpath String The class path to use
   * @return boolean true if successful, false otherwise.
   */
  static private boolean compile (Osejs _ejs, String _classesDirPath, java.util.List<File> _javaFiles, String _classpath) {
    int offset = _javaFiles.size();

    //CJB for collaborative
    for(int j=0;j<_javaFiles.size();j++)
      if(_javaFiles.get(j).getPath().equals(""))
        offset--;
    //CJB for collaborative

    String targetVM = _ejs.getOptions().targetVM();
    boolean isJava9 = OsejsCommon.getJavaVersion()>=9.0;
//    boolean addBootClasspath = OsejsCommon.getJavaVersion(null)>=7 && OsejsCommon.getJavaVersion(null)<=8;
    boolean addBootClasspath = targetVM.equals("1.7") || targetVM.equals("1.8");
    
    int argsLength;
    if (isJava9) argsLength = 7 + offset;
    else {
      argsLength = 9 + offset;
      if (addBootClasspath) argsLength += 2;
    }
    int maxFileLength = 0;
    boolean isWindows = OSPRuntime.isWindows();

    String args[] = new String[argsLength];
    for (int i=0; i<offset; i++) {
      args[i] = _javaFiles.get(i).getPath();
      if (isWindows) maxFileLength = Math.max(maxFileLength, args[i].length()); // compute the longest path
    }
    args[offset] = "-classpath";
    args[++offset] = _classpath;
    args[++offset] = "-d";
    args[++offset] = _classesDirPath;
    if (isJava9) {
      args[++offset] = "--release";
      int index = targetVM.indexOf('.');
      if (index<0) args[++offset] = targetVM;
      else args[++offset] = targetVM.substring(index+1); // convert 1.8 to 8
    }
    else {
      args[++offset] = "-source";
      args[++offset] = targetVM;
      args[++offset] = "-target";
      args[++offset] = targetVM;
      if (addBootClasspath) {
        args[++offset] = "-bootclasspath";
        args[++offset] = System.getProperty("java.home") + "/lib/rt.jar";
      }
    }
    args[++offset] = "-Xlint:unchecked";
//        for (int i=0; i<args.length; i++) System.out.println ("Args["+i+"] = "+args[i]);
        
    boolean ok = com.sun.tools.javac.Main.compile(args,new PrintWriter(_ejs.getOutputArea()))==0;
    if (!ok) {
      if (isWindows && maxFileLength>=256) {
        JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Generate.FileTooLong"), 
            res.getString("Generate.CompilationError"), JOptionPane.INFORMATION_MESSAGE);
      }
    }
    // if (!ok) JarTool.remove(_classesDir);
    return ok;
  }




  /**
   * Run a generated simulation locally.
   * @param _ejs Osejs The calling EJS
   * @param _filename String The name of the file to run
   * @return boolean true if successful, false otherwise.
   */
  static public boolean run (Osejs _ejs) {
    GeneratedUtil runable = new GeneratedUtil(_ejs);
    if (SwingUtilities.isEventDispatchThread()) {
      java.lang.Thread thread = new Thread(runable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
    else SwingUtilities.invokeLater(runable);
    return true;
  }

  // ----------------------------
  // Options of the package button
  // ----------------------------

  static private void addResources (Osejs _ejs, MiniJar _minijar) {
    _minijar.addDesired("org/opensourcephysics/resources/++");
    _minijar.addExclude("org/opensourcephysics/resources/tools/launcher++");
    _minijar.addDesired("org/colos/ejs/library/resources/++");
    if (!_ejs.getSimInfoEditor().addToolsForData()) {
      _minijar.addExclude("org/opensourcephysics/resources/tools/html/data_builder_help.html"); 
      _minijar.addExclude("org/opensourcephysics/resources/tools/html/data_tool_help.html"); 
      _minijar.addExclude("org/opensourcephysics/resources/tools/html/fit_builder_help.html"); 
      _minijar.addExclude("org/opensourcephysics/resources/tools/images/++"); 
    }
    _minijar.addExclude("org/opensourcephysics/resources/tools/html/translator_tool_help.html");
    //    if (_ejs.getSimInfoEditor().addTranslatorTool()) {
    //      _minijar.addForced("org/opensourcephysics/resources/tools/images/save.gif");
    //    }
    //    else {
    //      _minijar.addExclude("org/opensourcephysics/resources/tools/html/translator_tool_help.html");
    //    }
  }

  static public void packageCurrentSimulation(Osejs _ejs, File _targetFile) {
    ArrayList<PathAndFile> list = new ArrayList<PathAndFile>();
    File currentFile = _ejs.getCurrentMetadataFile();
    list.add(new PathAndFile(_ejs.getPathRelativeToSourceDirectory(currentFile),currentFile));
    packageSeveralSimulations(_ejs,list,_targetFile,true);
  }

  /**
   * Compresses a list of simulations in a single JAR file, without Launcher capabilities
   * @param _ejs
   * @param _listOfMetadataFiles 
   * @param _targetFile
   */
  static private void packageSeveralSimulations(Osejs _ejs, java.util.List<PathAndFile> _listOfMetadataFiles, File _targetFile, boolean _abortOnError) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    //System.out.println ("packaging "+_targetFile.getAbsolutePath());
    _ejs.getOutputArea().message("Package.PackagingJarFile",_targetFile.getName());

    // Create an instance of MiniJar and prepare it
    MiniJar minijar= new MiniJar();
    minijar.setOutputFile(_targetFile);
    minijar.addExclude ("++Thumbs.db");
    minijar.addExclude ("++.DS_Store");
    addResources(_ejs,minijar);
    boolean manifestNotSet = true;
    if (_listOfMetadataFiles.size()>1) {
      minijar.addDesired("org/colos/ejs/library/utils/SimulationChooser.class");
      //      Set<String> extraInfo = new HashSet<String>();
      //      extraInfo.add("Permissions: sandbox"); //$NON-NLS-1$
      //      extraInfo.add("Codebase: *\n"); //$NON-NLS-1$
      minijar.setManifestFile(MiniJar.createManifest(".","org.colos.ejs.library.utils.SimulationChooser")); // ,extraInfo);
      manifestNotSet = false;
    }

    Set<PathAndFile> extraFiles = new HashSet<PathAndFile>();
    Set<String> missingAuxFiles = new HashSet<String>();

    StringBuffer pckgBuffer = new StringBuffer();
    pckgBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    pckgBuffer.append("<package name=\""+_targetFile.getName()+"\">\n");

    for (PathAndFile paf : _listOfMetadataFiles) {
      File metadataFile = paf.getFile();
      //      String plainName = FileUtils.getPlainName(metadataFile);
      File generationDir = metadataFile.getParentFile();
      String relativePath = FileUtils.getRelativePath(generationDir, _ejs.getOutputDirectory(), false); 

      // Read the meta data
      Metadata metadata = Metadata.readFile(metadataFile, relativePath);
      if (metadata==null) {
        if (_abortOnError) {
          String[] message=new String[]{res.getString("Package.JarFileNotCreated"),res.getString("Package.IncorrectMetadata")};
          JOptionPane.showMessageDialog(_ejs.getMainPanel(),message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
          return;
        }
        String[] message=new String[]{res.getString("Package.IgnoringSimulation"),res.getString("Package.IncorrectMetadata")};
        JOptionPane.showMessageDialog(_ejs.getMainPanel(),message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
        continue;
      }

      if (manifestNotSet) {
        Set<String> extraLines = new HashSet<String>();
        String title = metadata.getTitle();
        if (title==null || title.trim().length()<=0) title = FileUtils.getPlainNameAndExtension(metadataFile).getFirstString();
        extraLines.add("Application-Name: "+ title);
        extraLines.add("Main-Frame: "+metadata.getMainFrame());
        extraLines.add("Is-Collaborative: "+metadata.isCollaborative());
        extraLines.add("Applet-Width: "+metadata.getPreferredWidth());
        extraLines.add("Applet-Height: "+metadata.getPreferredHeight());
        StringTokenizer linesTkn = new StringTokenizer(metadata.getManifestUserLines(),";"); 
        while (linesTkn.hasMoreTokens()) extraLines.add(linesTkn.nextToken());

        //        extraLines.add("Permissions: sandbox"); //$NON-NLS-1$
        //        extraLines.add("Codebase: *\n"); //$NON-NLS-1$
        minijar.setManifestFile(MiniJar.createManifest(".",metadata.getClassname(),extraLines));
        manifestNotSet = false;
      }

      pckgBuffer.append("  <simulation name=\""+org.colos.ejs.library.utils.FileUtils.getPlainName(metadataFile)+"\">\n");
      pckgBuffer.append("    <title>"+metadata.getTitle()+"</title>\n");
      pckgBuffer.append("    <class>"+metadata.getClassname()+"</class>\n");
      pckgBuffer.append("    <applet>"+metadata.getClassname()+"Applet</applet>\n");
      pckgBuffer.append("  </simulation>\n");

      // Add resources needed by the view elements when packaging. For example, CamImage adds  "com/charliemouse/++.gif"
      for (String resource : metadata.getResourcePatterns()) minijar.addDesired(resource);

      // Add source paths according to the execution path
      StringTokenizer tkn = new StringTokenizer (metadata.getExecpath(),File.pathSeparator);
      while (tkn.hasMoreTokens()) minijar.addSourcePath(tkn.nextToken());

      // Now add the main class and the Applet class as well
      String classname = metadata.getClassname().replace('.', '/');
      minijar.addDesired(classname+".class");
      minijar.addDesired(classname+"Applet.class");
      if(_ejs.getSimInfoEditor().addAppletColSupport()) minijar.addDesired(classname+"AppletStudent.class");

      //      if (_ejs.getSimInfoEditor().addTranslatorTool()) 
      minijar.addDesired(classname+"+.properties");


      // Add the XML file
      //      File xmlFile = new File (generationDir, plainName+".ejs");
      //      if (!xmlFile.exists()) xmlFile = new File (generationDir, plainName+".xml"); // Try older format
      //      if (xmlFile.exists()) extraFiles.add(new PathAndFile (relativePath+xmlFile.getName(),xmlFile));
      for (String filename : metadata.getFilesCreated())
        if (filename.endsWith(".xml") || filename.endsWith(".ejs") || filename.endsWith(".metadata")) {
          File xmlFile = new File (generationDir, filename);
          if (xmlFile.exists()) extraFiles.add(new PathAndFile(relativePath+filename,xmlFile));
        }

      // Add the _Intro HTML files
      String prefix = OsejsCommon.getHTMLFilenamePrefix(metadataFile);
      for (String filename : metadata.getFilesCreated())
        if (filename.startsWith(prefix) && (filename.endsWith(".html") || filename.endsWith(".xhtml")))
          extraFiles.add(new PathAndFile(relativePath+filename, new File (generationDir, filename)));

      // Add auxiliary files
      for (String auxPath : metadata.getAuxiliaryFiles()) {
        File auxFile = new File(_ejs.getSourceDirectory(),auxPath);
        if (!auxFile.exists()) missingAuxFiles.add(auxPath);
        else if (auxFile.isDirectory()) { // It is a complete directory
          for (File file : JarTool.getContents(auxFile)) {
            extraFiles.add(new PathAndFile(FileUtils.getRelativePath(file,_ejs.getSourceDirectory(),false),file));
          }
        }
        else extraFiles.add(new PathAndFile(auxPath,auxFile));
      }

      // Add user jars files. These won't get as jars inside the target jar, but their contents will
      // We need them as proper jar files in order to extract them later on
      for (String jarPath : metadata.getJarFiles()) {
        File jarFile = new File(_ejs.getSourceDirectory(),jarPath);
        if (jarFile.exists()) extraFiles.add(new PathAndFile(jarPath,jarFile));
        else missingAuxFiles.add(jarPath);
      }

      // Add files the user marked as non-discoverable files in JAR files which need to go into the jar. 
      // A good example are GIF or DLLs in the model element JARS
      for (String filename : metadata.getPackageList()) {
        //        System.out.println ("Adding package list file : "+filename);
        minijar.addDesired(filename);
      }

    }

    // Get matches
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
      //      else if (path.endsWith("/CoachLabIIPlus.class")) { // Add loader libraries
      //        minijar.addForced("gnu/io/++");
      //      }
    }
    matches = minijar.getMatches(); // a second round is required for loaders which use reflection

    matches.addAll(extraFiles);

    pckgBuffer.append("</package>\n");
    try {  
      File tmpFile = File.createTempFile("ejs_", ".xml");
      FileUtils.saveToFile(tmpFile, null, pckgBuffer.toString());
      matches.add(new PathAndFile("EJSSimulationList.xml",tmpFile));
    }
    catch (Exception exc) {
      exc.printStackTrace();
      _ejs.getOutputArea().println(res.getString("Osejs.File.CantCreateFile")+" EJSSimulationList.xml");
    }


    // Overwrite files in the library directory with user defined files (if any)
    String configDirPath = FileUtils.getPath(_ejs.getConfigDirectory());
    for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH)))
      matches.add(new PathAndFile(FileUtils.getRelativePath(file, configDirPath, false),file));

    // Add all the files in the library directory 
    File binConfigDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
    String binConfigDirPath = FileUtils.getPath(binConfigDir);
    for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH)))
      matches.add(new PathAndFile(FileUtils.getRelativePath(file, binConfigDirPath, false),file));

    if (_listOfMetadataFiles.size()<=1) { 
      // And overwrite CSS files with the current simulation CSS files, if any
      String cssFilename = _ejs.getSimInfoEditor().getCSSFile();
      if (cssFilename.length()>0) {
        File cssFile = new File(_ejs.getCurrentDirectory(),cssFilename);
        removeFromPafList(matches,"_ejs_library/css/ejss.css");
        matches.add(new PathAndFile("_ejs_library/css/ejss.css",cssFile));
      }
    }
    
    if (!_ejs.getSimInfoEditor().addTranslatorTool()) { // Add translation of core properties ONLY for the current locale
      Set<PathAndFile> trimmedSet = new HashSet<PathAndFile>();
      String ownLocale = java.util.Locale.getDefault().getLanguage();
      //      System.out.println ("Language is "+ownLocale);
      for (PathAndFile paf : matches) {
        String path = paf.getPath(); 
        if (!path.endsWith(".properties")) trimmedSet.add(paf);
        else {
          int resIndex = path.indexOf("_res"); // ejs_res and others
          int _index;
          if (resIndex<0) _index = path.indexOf('_');
          else _index = path.indexOf('_', resIndex+1);
          if (_index<0) trimmedSet.add(paf);
          else {
            String pathLocale = path.substring(_index+1,_index+3);
            if (pathLocale.equals(ownLocale)) {
              //              System.out.println ("Adding "+path);
              trimmedSet.add(paf);
            }
            //            else System.out.println ("Excluding "+path);
          }
        }
      }
      matches = trimmedSet;
    }

    //    for (PathAndFile paf : matches) {
    //      System.out.println ("Resource: "+paf);
    //    }

    // Create the jar file
    Set<String> missing = minijar.compress(matches);

    // Print missing files
    if (_ejs.isVerbose()) {
      for (Iterator<String> it=missing.iterator(); it.hasNext(); ) System.out.println ("Missing file: "+it.next()); 
    }

    _ejs.getOutputArea().message("Package.JarFileCreated",_targetFile.getName());
    //    JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.JarFileCreated"+" "+_targetFile.getName()), 
    //        res.getString("Package.PackagingJarFile"), JOptionPane.INFORMATION_MESSAGE);
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
  }

  /**
   * Compresses a list of simulations in a single ZIP file
   * @param _ejs
   * @param _listInfo 
   * @param _targetFile
   */
  static private void packageSeveralXMLSimulations(Osejs _ejs, CreateListDialog.ListInformation _listInfo, File _targetFile) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    //System.out.println ("packaging "+_targetFile.getAbsolutePath());
    _ejs.getOutputArea().message("Package.PackagingJarFile",_targetFile.getName());

    // Create a working temporary directory
    File zipFolder=null;
    try {
      zipFolder = File.createTempFile("EjsPackage", ".tmp", _ejs.getExportDirectory()); // Get a unique name for our temporary directory
      zipFolder.delete();        // remove the created file
      zipFolder.mkdirs();
    } catch (Exception exc) { 
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(),res.getString("Package.JarFileNotCreated"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      JarTool.remove(zipFolder);
      return;
    }

    String ret = System.getProperty("line.separator");
    StringBuffer metadataBuffer = new StringBuffer();
    metadataBuffer.append("package:true"+ret);
    metadataBuffer.append(sVersionInfo+ ", version "+_EjsSConstants.VERSION+", build "+_EjsSConstants.VERSION_DATE+". Visit "+_EjsSConstants.WEB_SITE+ret);
    String title = "";
    java.util.List<PathAndFile> listOfZipFiles = _listInfo.getList();
    boolean isFolder = _listInfo.getName()!=null;

    if (isFolder) { // It is a directory
      String name = _listInfo.getName().trim();
      if (name.length()<=0) name = "Unnamed";
      metadataBuffer.append("folder:"+name+ret);
      metadataBuffer.append("read-only:"+_listInfo.isReadOnly()+ret);
      String subtitle = _listInfo.getSubtitle();
      if (subtitle.length()>0) metadataBuffer.append("author:"+subtitle+ret);
      if (_listInfo.getImagefile()!=null) {
        File iconFile = _listInfo.getImagefile();
        try {
          if (FileUtils.copy(new FileInputStream(iconFile), new File(zipFolder,"_folderIcon.png"))) {
            metadataBuffer.append("logo-image: _folderIcon.png"+ret);
          }
          else { 
            JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
                res.getString("Osejs.File.SavingError")+"\n"+iconFile, 
                res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
          }
        } catch (Exception e) {
          JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
              res.getString("Osejs.File.SavingError")+"\n"+iconFile, 
              res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
      }
      if (_listInfo.getInfoFile()!=null) {
        File infoFile = _listInfo.getInfoFile();
        try {
          if (FileUtils.copy(new FileInputStream(infoFile), new File(zipFolder,"_infoIcon.png"))) {
            metadataBuffer.append("author-image: _infoIcon.png"+ret);
          }
          else { 
            JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
                res.getString("Osejs.File.SavingError")+"\n"+infoFile, 
                res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
          }
        } catch (Exception e) {
          JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
              res.getString("Osejs.File.SavingError")+"\n"+infoFile, 
              res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }
      }
    }
    else { // Not a folder. Process first model to obtain metadata
      PathAndFile firstPAF = listOfZipFiles.get(0);

      File firstMetadataFile = JarTool.extract(firstPAF.getFile(), "_metadata.txt", new File(zipFolder,"_metadata.txt"));
      if (firstMetadataFile==null) {
        _ejs.getOutputArea().println("Error: First ZIP file in list: "+firstPAF.getPath()+" does not contain an EjsS Javascript model. Interrupted!");
        JOptionPane.showMessageDialog(_ejs.getMainPanel(),res.getString("Package.JarFileNotCreated"),
            res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
        JarTool.remove(zipFolder);
        return;
      }

      String metadata = FileUtils.readTextFile(firstMetadataFile, null);
      StringTokenizer tkn = new StringTokenizer(metadata,"\n");
      while (tkn.hasMoreTokens()) {
        String line = tkn.nextToken();
        if (line.startsWith("title:")) {
          title = line.substring(6).trim();
          metadataBuffer.append("title:"       +title+ret);
        }
        else if (line.startsWith("logo-image:")) {
          String logoStr = line.substring(11).trim();
          metadataBuffer.append("logo-image:model1/"+ logoStr + ret);
        }
        else if (line.startsWith("author:")) {
          metadataBuffer.append("author:" +line.substring(7).trim()+ret);
        }
        else if (line.startsWith("copyright:")) {
          metadataBuffer.append("copyright:"   +line.substring(10).trim()+ret);
        }
        else if (line.startsWith("author-image:")) {
          String imgStr = line.substring(13).trim();
          metadataBuffer.append("author-image:");
          StringTokenizer imgTkn = new StringTokenizer(imgStr,";",true);
          while (imgTkn.hasMoreTokens()) {
            String oneImgStr = imgTkn.nextToken();
            if (oneImgStr.equals(";")) metadataBuffer.append(";");
            else if (oneImgStr.startsWith("./")) metadataBuffer.append("model1/"+oneImgStr.substring(2));
            else metadataBuffer.append("model1/"+oneImgStr);
          }
          metadataBuffer.append(ret);
        }
      }
      firstMetadataFile.delete();
    }

    StringBuffer htmlBuffer = new StringBuffer();
    if (!isFolder) { 
      // --- BEGIN OF the HTML page for the table of contents
      if (JSObfuscator.isGenerateXHTML()) {
        htmlBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
        htmlBuffer.append("<!DOCTYPE html>"+ret);
        htmlBuffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">"+ret);
      }
      else htmlBuffer.append("<html>"+ret);
      htmlBuffer.append("  <head>"+ret);
      htmlBuffer.append("    <title>Contents</title>"+ret);
      htmlBuffer.append("    <base target=\"_self\" />" + ret);
      htmlBuffer.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"model1/_ejs_library/css/ejsContentsLeft.css\"></link>" + ret);
      htmlBuffer.append("  </head>"+ret);
      String bodyOptions = _ejs.getOptions().getHtmlBody();
      if (bodyOptions.length()>0) htmlBuffer.append("  <body "+bodyOptions+"> "+ret);
      else htmlBuffer.append("  <body> "+ret);
      htmlBuffer.append("    <h1>"+title+"</h1>"+ret);
      htmlBuffer.append("    <h2>" + res.getString("Generate.HtmlContents") + "</h2>"+ret);
      htmlBuffer.append("    <div class=\"contents\">"+ret);
    }

    // Uncompress all zip files into the zip directory
    int counter = 1;
    String firstHtmlPage = null;
    for (int i=0,n=listOfZipFiles.size(); i<n; i++) {
      PathAndFile paf = listOfZipFiles.get(i);
      String targetFolderName = "model"+counter;
      File targetFolder = new File(zipFolder,targetFolderName);
      targetFolder.mkdirs();
      JarTool.unzip(paf.getFile(), targetFolder);
      File metadataFile = new File(targetFolder,"_metadata.txt");
      if (!metadataFile.exists()) { // This is not an ejss_model file
        _ejs.getOutputArea().println("Warning: ZIP file "+paf.getPath()+" does not contain an EJS Javascript model. Ignored!");
        JarTool.remove(targetFolder);
      }
      else { // process metadata file
        String lastPageTitle = "";
        String modelTitle = null;
        String metadata = FileUtils.readTextFile(metadataFile, null);
        StringTokenizer tkn = new StringTokenizer(metadata,"\n");
        while (tkn.hasMoreTokens()) {
          String line = tkn.nextToken();
          if (line.startsWith("title:")) {
            modelTitle = line.substring(6).trim();
          }
          if (line.startsWith("page-title:")) {
            lastPageTitle = line.substring(11).trim();
          }
          else if (line.startsWith("page-index:")) {
            if (!isFolder) {
              String pageIndex = line.substring(11).trim();
              if (firstHtmlPage==null) firstHtmlPage = targetFolderName+"/"+ pageIndex;
              if (pageIndex.endsWith("_Simulation.html") || pageIndex.endsWith("_Simulation.xhtml")) {
                htmlBuffer.append("      <div class=\"simulation\"><a href=\""+ targetFolderName+"/"+ pageIndex + "\" target=\"central\">"+ lastPageTitle +"</a></div>"+ret);
              }
              else { 
                htmlBuffer.append("      <div class=\"intro\"><a href=\""+ targetFolderName+"/"+ pageIndex +"\" target=\"central\">" + lastPageTitle +"</a></div>"+ret);
              }
            }
          }
        }
        if (modelTitle!=null) metadataBuffer.append("model:model"+counter+ " | "+modelTitle +ret);
        else metadataBuffer.append("model:model"+counter+ret);
        counter++;
      }
    }

    if (isFolder) {
      try {
        FileUtils.saveToFile(new File(zipFolder,"_metadata.txt"), null, metadataBuffer.toString());
      } catch (IOException e) {
        _ejs.getOutputArea().println(res.getString("Generate.JarFileNotCreated")+ " : "+_targetFile.getName());
        e.printStackTrace();
        JarTool.remove(zipFolder);
        return;
      }
    }
    else {
      htmlBuffer.append("    </div>"+ret); // End of contents
      // ---- Now the logo
      htmlBuffer.append("    <div class=\"signature\">"+ res.getString("Generate.HtmlEjsGenerated") + " "
          + "<a href=\"http://www.um.es/fem/EjsWiki\" target=\"_blank\">Easy Java Simulations</a></div>"+ret);
      htmlBuffer.append("  </body>"+ret);
      htmlBuffer.append("</html>"+ret);

      // Create main html page
      StringBuffer mainHtmlBuffer = new StringBuffer();

      if (JSObfuscator.isGenerateXHTML()) {
        mainHtmlBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
        mainHtmlBuffer.append("<!DOCTYPE html>"+ret);
        mainHtmlBuffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">"+ret);
      }
      else mainHtmlBuffer.append("<html>"+ret);
      mainHtmlBuffer.append("  <head>"+ret);
      mainHtmlBuffer.append("    <title> " + res.getString("Generate.HtmlFor") + " " + title + "</title>"+ret);
      mainHtmlBuffer.append("  </head>"+ret);
      mainHtmlBuffer.append("  <body>"+ret);
      mainHtmlBuffer.append("    <frameset cols=\"25%,*\">"+ret);
      mainHtmlBuffer.append("      <frame src=\"Contents.html\" name=\"contents\" scrolling=\"auto\" target=\"_self\">"+ret);
      if (firstHtmlPage!=null) mainHtmlBuffer.append("      <frame src=\""+firstHtmlPage+"\"");
      else mainHtmlBuffer.append("      <frame src=\"Contents.html\"");
      mainHtmlBuffer.append(" name=\"central\" scrolling=\"auto\" target=\"_self\">"+ret);
      mainHtmlBuffer.append("      <noframes>"+ret);
      mainHtmlBuffer.append("        Gee! Your browser is really old and doesn't support frames. You better update!!!"+ret);
      mainHtmlBuffer.append("      </noframes>"+ret);
      mainHtmlBuffer.append("    </frameset> "+ret);
      mainHtmlBuffer.append("  </body>"+ret);
      mainHtmlBuffer.append("</html>"+ret);

      try {
        FileUtils.saveToFile(new File(zipFolder,"_metadata.txt"), null, metadataBuffer.toString());
        FileUtils.saveToFile(new File(zipFolder,JSObfuscator.isGenerateXHTML() ? "Contexts.xhtml" : "Contents.html"), null, htmlBuffer.toString());
        FileUtils.saveToFile(new File(zipFolder, JSObfuscator.isGenerateXHTML() ? "index.xhtml" : "index.html"), null, mainHtmlBuffer.toString());
      } catch (IOException e) {
        _ejs.getOutputArea().println(res.getString("Generate.JarFileNotCreated")+ " : "+_targetFile.getName());
        e.printStackTrace();
        JarTool.remove(zipFolder);
        return;
      }
    }
    { // save a README file
      StringBuffer buffer = new StringBuffer();
      addToReadMe(buffer,true);
//      for (TwoStrings ts : metaFile) {
//        buffer.append(ts.getFirstString()+": "+ts.getSecondString()+"\n");
//      }
      try {
        FileUtils.saveToFile (new File (zipFolder,"_ejs_README.txt"), null, buffer.toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    // Compress and remove working folder
    JarTool.compress(zipFolder, _targetFile, null);
    JarTool.remove(zipFolder);
    // Done
    _ejs.getOutputArea().println(res.getString("Generate.JarFileCreated")+ " : "+_targetFile.getName());
  }

  


  /**
   * This compresses the simulation and its auxiliary files in EjsS XML format 
   * @param _ejs Osejs
   *
  static public void packageXMLSimulation (Osejs _ejs, String _targetName) {

    java.util.List<LocaleItem> localesDesired = getLocalesDesired(_ejs);
    boolean hasView = !_ejs.getHtmlViewEditor().getPages().isEmpty();

    File xmlFile = _ejs.getCurrentXMLFile();
    if (!xmlFile.exists()) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.NoSimulations"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    if (_ejs.checkChangesAndContinue(false) == false) return; // The user canceled the action

    boolean warnBeforeOverwritting = true;
    File finalFile;
    String filename = _ejs.getSimInfoEditor().getSimulationName();
    if (filename==null) filename = _ejs.getCurrentXMLFilename();
    if (_targetName!=null) { 
      if (!hasView) {
        if (_targetName.startsWith("ejss_model_")) {
          System.out.print("Package name changed from "+_targetName);
          _targetName = "ejss_info_" + _targetName.substring(11);
          System.out.println(" to "+_targetName);
        }
      }
      finalFile = new File(_ejs.getExportDirectory(),_targetName);
    }
    else { // Select the target
      File currentFile = new File(_ejs.getExportDirectory(),hasView ? (_ejs.supportsJava() ? "ejsh_model_"+filename+".zip" : "ejss_model_"+filename+".zip") : "ejss_info_"+filename+".zip");
      _targetName = FileChooserUtil.chooseFilename(_ejs, currentFile, "ZIP", new String[]{"zip"}, true);
      if (_targetName==null) {
        _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
        return;
      }
      if (! (_targetName.toLowerCase().endsWith(".zip")) ) _targetName = _targetName + ".zip";
      else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
      finalFile = new File(_targetName);
      if (warnBeforeOverwritting && finalFile.exists()) {
        int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
            finalFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
            DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
        if (selected != JOptionPane.YES_OPTION) {
          _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
          return;
        }
      }
    }

    // Create a working temporary directory
    File zipFolder;
    try {
      zipFolder = File.createTempFile("EjsPackage", ".tmp", _ejs.getExportDirectory()); // Get a unique name for our temporary directory
      zipFolder.delete();        // remove the created file
      zipFolder.mkdirs();
    } catch (Exception exc) { 
      exc.printStackTrace();
      JOptionPane.showMessageDialog(_ejs.getMainPanel(),res.getString("Package.JarFileNotCreated"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    // Prepare the simulation
    SimulationXML simulation = _ejs.getSimulationXML(filename);
    if (simulation.getInformation(INFORMATION.TITLE)==null) simulation.setInformation(INFORMATION.TITLE,filename);

    // Prepare the metadata information
    ArrayList<TwoStrings> metaFile = new ArrayList<TwoStrings>();
    { // author information
      String versionInfo = "Easy Java/Javascript Simulations, version "+_EjsSConstants.VERSION+", build "+_EjsSConstants.VERSION_DATE+". Visit "+_EjsSConstants.WEB_SITE;
      metaFile.add(new TwoStrings("generated-with", versionInfo));
      if (_ejs.getSimInfoEditor().fixedNavigationBar()) metaFile.add(new TwoStrings("fixed-navbar", "true"));
      metaFile.add(new TwoStrings("title", simulation.getInformation(INFORMATION.TITLE)));
      String image = simulation.getInformation(INFORMATION.LOGO_IMAGE);
      addToMetafile(metaFile,"logo-image", image!=null ? image : "");
      metaFile.add(new TwoStrings("author", simulation.getInformation(INFORMATION.AUTHOR)));
      metaFile.add(new TwoStrings("copyright", simulation.getInformation(INFORMATION.COPYRIGHT)));
      image = simulation.getInformation(INFORMATION.AUTHOR_IMAGE);
      
      {
        OneView oneViewPage = ((OneView) _ejs.getHtmlViewEditor().getCurrentPage());
        String width = oneViewPage.getPreferredWidth();
        String height = oneViewPage.getPreferredHeight();
        if (width.length()>0 && height.length()>0) {
          metaFile.add(new TwoStrings("preferred-width", width));
          metaFile.add(new TwoStrings("preferred-height", height));
        }
      }
      
      addToMetafile(metaFile,"author-image", image!=null ? image : "");
      if (_ejs.getOptions().includeModel()) {
        TwoStrings fileNameAndAxtension = FileUtils.getPlainNameAndExtension(xmlFile);
        String modelName = fileNameAndAxtension.getFirstString()+"."+fileNameAndAxtension.getSecondString(); // Use same extension as the original
        addToMetafile(metaFile,"source", modelName);
        JarTool.copy(xmlFile,new File(zipFolder,modelName));
      }
    }

    File javascriptDir = new File(_ejs.getBinDirectory(),"javascript/lib");
    File viewFolder = zipFolder;
    if (hasView) { // Tailor the view and create the Simulation files
      if (_ejs.supportsJava()) simulation.setViewOnly("8800"); // prepare for web server version
      if (_ejs.getOptions().autoSelectView()) simulation.setInformation(INFORMATION.AUTOSELECT_VIEW, "true");
      if (_ejs.supportsJava()) { // generate the view in a _view subfolder
          viewFolder = new File(zipFolder,"_view");
          viewFolder.mkdirs();
      }

      // Generate the HTML file for the currently selected HTML View and Locales
      OneView oneViewPage = ((OneView) _ejs.getHtmlViewEditor().getCurrentPage());
      String viewDesired = oneViewPage.getName();
      String libPath = FileUtils.getPath(javascriptDir);
      { // Main simulation and script
        String localeString;
        String available_languages = "";
        for (LocaleItem locale : localesDesired) {
          String localeSuffix ="";
          if (locale.isDefaultItem()) {
            localeString = SimulationXML.sDEFAULT_LOCALE;
          }
          else {
            localeString = locale.getKeyword();
            localeSuffix = "_" + localeString;
            if (available_languages.length()>0) available_languages += ","+localeString;
            else available_languages = localeString; 
          }

          File htmlFile = new File (viewFolder,simulation.getName()+ (JSObfuscator.isGenerateXHTML() ? "_Simulation"+localeSuffix+".xhtml" : "_Simulation"+localeSuffix+".html"));
          boolean ok = XMLTransformerJava.saveHTMLFile(_ejs,libPath,htmlFile, // output info
              simulation, viewDesired, localeString, "_ejs_library/css/ejss.css","_ejs_library", null,
              _ejs.getOptions().separateJSfile(),_ejs.getOptions().useFullLibrary()); // generation info
          if (!ok) {
            _ejs.getOutputArea().println(res.getString("Generate.JarFileNotCreated")+ " : "+_targetName);
            return;
          }
          String filepath = _ejs.getOptions().separateJSfile() ? org.colos.ejs.library.utils.FileUtils.getPlainName(htmlFile) + ".js" : "";
          // add main script entry to metadata file
          if (locale.isDefaultItem()) {
            addToMetafile(metaFile, "main-script", filepath);
            addToMetafile(metaFile, "main-simulation", htmlFile.getName());
          }
          else {
            addToMetafile(metaFile, locale.getKeyword()+"-main-script", filepath);
            addToMetafile(metaFile, locale.getKeyword()+"-main-simulation", htmlFile.getName());
          }
        }
        addToMetafile(metaFile, "available-languages", available_languages);
      }
    }

    // Copy auxiliary files
    Set<PathAndFile> list = new HashSet<PathAndFile>();

    if ( _ejs.supportsJava()) { 
      { // need to add the JAR for the server
        File jarFolder = new File(zipFolder,"_jar");
        jarFolder.mkdirs();
        File jarFile = new File (jarFolder,"model.jar");
        _ejs.forceCompilation();
        if (_ejs.firstCompile(8800)) Generate.packageCurrentSimulation(_ejs, jarFile);
        else JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Generate.CantCompileError"),
            res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
      }
      { // create index.html file
        StringBuffer buffer = new StringBuffer();
        if (JSObfuscator.isGenerateXHTML()) {
          buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
          buffer.append("<!DOCTYPE html>\n");
          buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
        }
        else buffer.append("<html>\n");
        buffer.append("  <head>\n");
        buffer.append("    <title>"+simulation.getInformation(INFORMATION.TITLE)+"</title>\n");
        buffer.append("  </head>\n");
        buffer.append("  <body> \n");
        buffer.append("    <strong>"+simulation.getInformation(INFORMATION.TITLE)+"</strong>\n");
        String abstractStr = simulation.getInformation(INFORMATION.ABSTRACT);
        if (abstractStr!=null) {
          StringTokenizer tkn = new StringTokenizer(abstractStr,"\n");
          while (tkn.hasMoreTokens()) {
            buffer.append("    <p>\n");
            buffer.append("      "+tkn.nextToken()+"\n");
            buffer.append("    </p>\n");
          }
        }
        String logo = simulation.getInformation(INFORMATION.LOGO_IMAGE);
        if (logo!=null) {
          buffer.append("    <p align=\"center\">\n");
          buffer.append("      <img src=\"_view/"+logo+"\"> ");
          buffer.append("    </p>\n");
        }

        buffer.append("    <p>\n");
        buffer.append("      <strong>&copy; "+Calendar.getInstance().get(Calendar.YEAR)+" "+simulation.getInformation(INFORMATION.AUTHOR)+"</strong>\n");
        buffer.append("    </p>\n");
        String authorLogo = simulation.getInformation(INFORMATION.AUTHOR_IMAGE);
        if (authorLogo!=null) {
          buffer.append("    <p align=\"left\">\n");
          buffer.append("      <img src=\"_view/"+authorLogo+"\">\n");
          buffer.append("    </p>\n");
        }
        buffer.append("</body>\n");
        buffer.append("</html>\n");
        String indexFilename = "index" +(JSObfuscator.isGenerateXHTML() ? ".xhtml" : ".html");
        try {
          FileUtils.saveToFile(new File (zipFolder,indexFilename), null, buffer.toString());
        } catch (IOException exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.JarFileNotCreated")+" : "+ indexFilename,
              res.getString("Osejs.File.SavingError"), JOptionPane.INFORMATION_MESSAGE);
        }
      }
    }

    { // <OpenSocial XML>
      StringBuffer buffer_os = new StringBuffer();
      buffer_os.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      buffer_os.append("<Module>\n");
      buffer_os.append("<ModulePrefs\n");
      buffer_os.append("title=\""+simulation.getName()+"\"\n");
      buffer_os.append("author=\""+simulation.getInformation(INFORMATION.AUTHOR)+"\"\n");
      buffer_os.append("description=\""+simulation.getInformation(INFORMATION.ABSTRACT)+"\"\n");
      buffer_os.append("screenshot=\""+simulation.getInformation(INFORMATION.LOGO_IMAGE)+"\"\n");
      buffer_os.append("thumbnail=\""+simulation.getInformation(INFORMATION.LOGO_IMAGE)+"\">\n");
      buffer_os.append("<Require feature=\"opensocial-0.9\"/>\n");
      buffer_os.append("<Require feature=\"dynamic-height\"/>\n");
      buffer_os.append("</ModulePrefs>\n");
      buffer_os.append("<Content type=\"html\" href=\""+simulation.getName()+"_Simulation.xhtml\"/>\n");
      buffer_os.append("</Module>\n");
      String opensocialFilename = simulation.getName() + "_opensocial.xml";
      try {
        FileUtils.saveToFile(new File (zipFolder,opensocialFilename), null, buffer_os.toString());
      } catch (IOException exc) {
        exc.printStackTrace();
        JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.JarFileNotCreated")+" : "+ opensocialFilename,
            res.getString("Osejs.File.SavingError"), JOptionPane.INFORMATION_MESSAGE);
      }
    } // </OpenSocial XML>
    
    // Add description files
    String fileSuffix = JSObfuscator.isGenerateXHTML() ? ".xhtml" : ".html";
    Hashtable<String,Boolean> isXhtmlTable = new Hashtable<String,Boolean>();
    
    for (LocaleItem item : localesDesired)  {
      String localePrefix = item.isDefaultItem() ? "" : item.getKeyword()+"-";
      String localeSuffix = item.isDefaultItem() ? "" : "_"+item.getKeyword();
      Hashtable<String,StringBuffer> htmlTable = new Hashtable<String,StringBuffer>();
      addFramesHtml(htmlTable, _ejs,item,filename,null,simulation.getInformation(SimulationXML.INFORMATION.MODEL_TAB_TITLE),"",null,hasView); 
      addDescriptionHtml(htmlTable, _ejs,item,filename,"");
      // Now, save them all
      try {
        for (String key : htmlTable.keySet()) {
          String htmlCode = htmlTable.get(key).toString();
          String postFix;
          if (htmlCode.startsWith("<?xml ")) {
            postFix = key + localeSuffix + ".xhtml";
            isXhtmlTable.put(key, true);
          }
          else {
            postFix = key + localeSuffix + ".html";
            isXhtmlTable.put(key, false);
          }
          FileUtils.saveToFile (new File (viewFolder,filename+postFix), null,htmlCode);
          String metadataCode;
          if (key.length()==0) metadataCode = localePrefix+"html-main";
          else if (key.startsWith("_Contents"))  metadataCode = localePrefix+"html-contents";
          else metadataCode = localePrefix+"html-description";
          addToMetafile(metaFile,metadataCode, filename+postFix);
        }
      } catch (IOException e) {
        e.printStackTrace();
        _ejs.getOutputArea().println(res.getString("Generate.JarFileNotCreated")+ " : "+_targetName);
        return;
      }

      // Add table of contents
      Vector<Editor> pageList = _ejs.getDescriptionEditor().getPages();
      int counter = 0;
      int simTab = _ejs.getSimInfoEditor().getSimulationTab();
      boolean simulationAdded = false;

      String firstDescriptionFilename=null;
      for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements(); ) {
        HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
        if (htmlEditor.isActive() && !htmlEditor.isInternal()) {
          counter++;
          if (counter==simTab && hasView) {
            simulationAdded = true;
            String link = filename+"_Simulation"+ localeSuffix + fileSuffix;
            addToMetafile(metaFile,localePrefix+"page-title", simulation.getInformation(INFORMATION.MODEL_TAB_TITLE)) ; //res.getString("Generate.HtmlSimulation"));
            addToMetafile(metaFile,localePrefix+"page-index", link);
          }

          LocaleItem actualItem = item;
          OneHtmlPage htmlPage = htmlEditor.getHtmlPage(actualItem);
          if (htmlPage==null) {
            if (!actualItem.isDefaultItem()) htmlPage =  htmlEditor.getHtmlPage(actualItem = LocaleItem.getDefaultItem());
          }
          if (htmlPage==null || htmlPage.isEmpty()) continue;
          String link;
          if (htmlPage.isExternal()) link = htmlPage.getPlainCode();
          else {
            String key = getIntroductionPageKey(counter,null);
            if (actualItem==item) link = filename + key + localeSuffix;
            else link = filename + key;
//            System.err.println("Checking key "+key+ " for locale "+item.getKeyword());
            if (isXhtmlTable.get(key)) link += ".xhtml";
            else link += ".html";
          }
          addToMetafile(metaFile,localePrefix+"page-title", htmlPage.getTitle());
          addToMetafile(metaFile,localePrefix+"page-index", link);
          if (firstDescriptionFilename==null) firstDescriptionFilename = link;
        }
      } // end of for
      if (hasView) {
        if (!simulationAdded) {
          String link = filename+"_Simulation"+ localeSuffix + fileSuffix;
          addToMetafile(metaFile,localePrefix+"page-title", simulation.getInformation(INFORMATION.MODEL_TAB_TITLE)) ; //res.getString("Generate.HtmlSimulation"));
          addToMetafile(metaFile,localePrefix+"page-index", link);
        }
      }
      else { // has no view
        addToMetafile(metaFile,localePrefix+"main-script", "");
        if (firstDescriptionFilename!=null) addToMetafile(metaFile,localePrefix+"main-simulation", firstDescriptionFilename);
        else addToMetafile(metaFile,localePrefix+"main-simulation", "");
      }
    } // end of for Locales

    Set<String> missingAuxFiles = new HashSet<String>();
    Set<String> absoluteAuxFiles = new HashSet<String>();
    for (PathAndFile paf : _ejs.getSimInfoEditor().getAuxiliaryPathAndFiles(true)) {
      if (paf.getFile().isDirectory()) { // It is a complete directory
        if (!paf.getFile().exists()) missingAuxFiles.add(paf.getPath());
        else {
          String prefix = paf.getPath();
          if (!prefix.endsWith("/")) prefix += "/";
          if (prefix.startsWith("./")) prefix = prefix.substring(2);
          else absoluteAuxFiles.add(prefix);
          for (File file : JarTool.getContents(paf.getFile())) {
            String filepath = prefix+FileUtils.getRelativePath(file,paf.getFile(),false);
            //            System.out.println("Adding "+filepath);
            list.add(new PathAndFile(filepath,file));
            addToMetafile(metaFile,"resource", filepath);
          }
        }
      }
      else {
        if (paf.getPath().startsWith("/")) {
          String absolutePath = "images"+paf.getPath();
          //          System.out.println ("Copying absolute aux file "+absolutePath);
          File absoluteFile = new File (javascriptDir,absolutePath);
          if (!absoluteFile.exists()) {
            System.err.println ("Absolute file does not exist "+absoluteFile.getAbsolutePath());
            missingAuxFiles.add(paf.getPath());
          }
          else list.add(new PathAndFile("_ejs_library/"+absolutePath,absoluteFile));
        }
        else {
          if (!paf.getFile().exists()) missingAuxFiles.add(paf.getPath());
          else {
            list.add(paf);
            addToMetafile(metaFile,"resource", paf.getPath());
          }
        }
      }
    }
     
    { // Add common script
      File originalScriptFile = new File(javascriptDir,JSObfuscator.sCOMMON_SCRIPT);
      String destScriptFilename = "_ejs_library/"+JSObfuscator.sCOMMON_SCRIPT;
      boolean done = false;
      if (_ejs.getOptions().convertUserFilesToBase64()) { // Add common script together with Base64Images, if any
        Set<String> base64Images = _ejs.getSimInfoEditor().getBase64Images();
        if (base64Images!=null && !base64Images.isEmpty()) { 
          try {
            String base64Text = XMLTransformerJava.getJavascriptForBase64Images(_ejs, base64Images, FileUtils.readTextFile(originalScriptFile, null));
            FileUtils.saveToFile(new File(viewFolder,destScriptFilename), XMLTransformerJava.UTF8_CHARSET, base64Text);
            done = true;
          } 
          catch (IOException exc) { exc.printStackTrace(); }
        }
      }
      if (!done) list.add(new PathAndFile(destScriptFilename,originalScriptFile)); // Just copy the standard common script
    }
    list.add(new PathAndFile("_ejs_library/"+JSObfuscator.sTEXTRESIZEDETECTOR_SCRIPT,new File(javascriptDir,JSObfuscator.sTEXTRESIZEDETECTOR_SCRIPT))); // Just copy the resize font detector script
    
    if (_ejs.getOptions().useFullLibrary()) {
      String whichJSLib = JSObfuscator.whichJSLibrary(javascriptDir);
      list.add(new PathAndFile("_ejs_library/"+whichJSLib,new File(javascriptDir,whichJSLib)));
    }
    
    // First add files in the library directory with user defined files (if any)
    String configDirPath = FileUtils.getPath(_ejs.getConfigDirectory());
    for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
      String destName = FileUtils.getRelativePath(file, configDirPath, false);
      list.add(new PathAndFile(destName,file));
    }

    // If not there, add all the files in the library directory 
    File binConfigDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
    String binConfigDirPath = FileUtils.getPath(binConfigDir);
    for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
      String destName = FileUtils.getRelativePath(file, binConfigDirPath, false);
      list.add(new PathAndFile(destName,file));
    }

    // Finally of all, copy the simulation CSS files, if any
    String cssFilename = _ejs.getSimInfoEditor().getCSSFile();
    if (cssFilename.length()>0) {
      File cssFile = new File(_ejs.getCurrentDirectory(),cssFilename);
      removeFromPafList(list,"_ejs_library/css/ejss.css");
      list.add(new PathAndFile("_ejs_library/css/ejss.css",cssFile));
    }

//    String cssPath = _ejs.getSimInfoEditor().getCSSFolder();
//    if (cssPath.length()>0) {
//      File cssFolder = new File(_ejs.getCurrentDirectory(),cssPath);
//      for (File file : JarTool.getContents(cssFolder)) {
//        String destName = "_ejs_library/css/"+FileUtils.getRelativePath(file, cssFolder, false);
//        list.add(new PathAndFile(destName,file));
//      }
//    }

    // Now copy all files in the list
    for (PathAndFile paf : list){
//            System.out.println ("Copying file "+paf.getFile().getAbsolutePath() +" to " + paf.getPath());
      JarTool.copy(paf.getFile(),new File(viewFolder,paf.getPath()));
    }

    if (!_ejs.supportsJava()) { // save the metadata file
      StringBuffer buffer = new StringBuffer();
      for (TwoStrings ts : metaFile) {
        buffer.append(ts.getFirstString()+": "+ts.getSecondString()+"\n");
      }
      try {
        FileUtils.saveToFile (new File (viewFolder,"_metadata.txt"), null, buffer.toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    { // save a README file
      StringBuffer buffer = new StringBuffer();
      addToReadMe(buffer,false);
//      for (TwoStrings ts : metaFile) {
//        buffer.append(ts.getFirstString()+": "+ts.getSecondString()+"\n");
//      }
      try {
        FileUtils.saveToFile (new File (viewFolder,"_ejs_README.txt"), null, buffer.toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Compress and remove working folder
    JarTool.compress(zipFolder, finalFile, null);
    JarTool.remove(zipFolder);
    // Done
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
    _ejs.getOutputArea().println(res.getString("Generate.JarFileCreated")+ " : "+finalFile.getName());
  }
*/
  
  static void removeFromPafList (Set<PathAndFile> list, String path) {
    for (PathAndFile paf : list) { 
      if (paf.getPath().equals(path)) {
        list.remove(paf);
        return;
      }
    }
  }
  
  static void addToMetafile(ArrayList<TwoStrings> list, String key, String filepath) {
    if (OSPRuntime.isMac() && filepath.endsWith(".DS_Store")) return;
    if (!filepath.startsWith("/")) {
      if (filepath.startsWith("./")) filepath = filepath.substring(2);
    }
    for (TwoStrings ts : list) {
      if (ts.getFirstString().equals(key) && ts.getSecondString().equals(filepath)) return;
    }
    list.add(new TwoStrings(key,filepath));
  }

  static public void addToReadMe(StringBuffer buffer, boolean multiPackage) {
    buffer.append("<"+res.getString("Osejs.File.FileVersion")+" "+_EjsSConstants.VERSION+" (build "+_EjsSConstants.VERSION_DATE+")>\n\n");
    buffer.append(res.getString("Osejs.Generate.README_1")+" ");
    if (multiPackage) buffer.append(res.getString("Osejs.Generate.README_PACKAGE_CONTAINS"));
    else              buffer.append(res.getString("Osejs.Generate.README_SINGLE_CONTAINS"));
    buffer.append(res.getString("Osejs.Generate.README_2"));
    if (multiPackage) buffer.append(res.getString("Osejs.Generate.README_PACKAGE_STRUCTURE"));
    buffer.append(res.getString("Osejs.Generate.README_HOWTOOPEN"));
    buffer.append(res.getString("Osejs.Generate.README_METADATA"));
    buffer.append(res.getString("Osejs.Generate.README_READER_INFO"));
  }

  /**
   * This compresses the simulation XML file and its auxiliary files
   * @param _ejs Osejs
   * @param _targetFile File
   */
  static public void zipCurrentSimulation(Osejs _ejs, File targetFile) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist

    File xmlFile = _ejs.getCurrentXMLFile();
    if (!xmlFile.exists()) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.NoSimulations"),
          res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    _ejs.getOutputArea().message("Package.PackagingJarFile",targetFile.getName());

    Set<PathAndFile> list = new HashSet<PathAndFile>();
    list.add(new PathAndFile(xmlFile.getName(),xmlFile));

    Set<String> missingAuxFiles = new HashSet<String>();
    Set<String> absoluteAuxFiles = new HashSet<String>();
    for (PathAndFile paf : _ejs.getSimInfoEditor().getAuxiliaryPathAndFiles(false)) {
      if (!paf.getFile().exists()) missingAuxFiles.add(paf.getPath());
      else if (paf.getFile().isDirectory()) { // It is a complete directory
        String prefix = paf.getPath();
        if (!prefix.endsWith("/")) prefix += "/";
        if (prefix.startsWith("./")) prefix = prefix.substring(2);
        else absoluteAuxFiles.add(prefix);
        for (File file : JarTool.getContents(paf.getFile())) {
          list.add(new PathAndFile(prefix+FileUtils.getRelativePath(file,paf.getFile(),false),file));
        }
      }
      else {
        if (paf.getPath().startsWith("./")) list.add(new PathAndFile(paf.getPath().substring(2),paf.getFile()));
        else {
          absoluteAuxFiles.add(paf.getPath());
          list.add(paf);
        }
      }
    }

    //    for (PathAndFile paf : list) System.out.println("Will zip: "+paf.getPath()+" : "+paf.getFile());

    if (org.opensourcephysics.tools.minijar.MiniJar.compress(list, targetFile, null)) {
      _ejs.getOutputArea().println(res.getString("Package.JarFileCreated")+" "+targetFile.getName());
    }
    else _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),absoluteAuxFiles,"Generate.AbsolutePathsFound");
  }

  /**
   * This compresses several simulation XML files and their auxiliary files
   * @param _ejs Osejs
   */
  static public void zipSeveralSimulations(Osejs _ejs) {
    // Select simulation files or directories to ZIP 
    JFileChooser chooser=OSPRuntime.createChooser(res.getString("View.FileDescription.xmlfile"), new String[]{"xml","ejs", "ejss", "ejsh"},_ejs.getSourceDirectory().getParentFile());
    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.setMultiSelectionEnabled(true);
    chooser.setCurrentDirectory(_ejs.getFileDialog(null).getCurrentDirectory());
    if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
    final File[] dirs = chooser.getSelectedFiles();
    if (dirs==null || dirs.length<=0) {
      System.out.println (res.getString("ProcessCanceled"));
      return;
    }

    File baseDir = chooser.getCurrentDirectory();
    java.util.List<PathAndFile> list = new ArrayList<PathAndFile>();
    for (int i=0,n=dirs.length; i<n; i++) {
      File file = dirs[i];
      if (file.isDirectory()) {
        for (File subFile : JarTool.getContents(file)) 
          if (OsejsCommon.isEJSfile(subFile)) list.add(new PathAndFile(FileUtils.getRelativePath (subFile, baseDir, false),subFile));
      }
      else {
        if (OsejsCommon.isEJSfile(file)) list.add(new PathAndFile(FileUtils.getRelativePath (file, baseDir, false),file));
      }
    }

    java.util.List<?> confirmedList = EjsTool.ejsConfirmList(null,res.getDimension("Package.ConfirmList.Size"),
        res.getString("Package.CompressList"),res.getString("Package.CompressTitle"),list,null);
    if (confirmedList==null || confirmedList.isEmpty()) return;

    // Choose target
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist

    File targetFile;
    switch (_ejs.getProgrammingLanguage()) {
      default :
      case JAVA           : targetFile = new File(_ejs.getExportDirectory(),"ejs_src_simulations.zip"); break;
      case JAVASCRIPT     : targetFile = new File(_ejs.getExportDirectory(),"ejss_src_simulations.zip"); break;
      case JAVA_PLUS_HTML : targetFile = new File(_ejs.getExportDirectory(),"ejsh_src_simulations.zip"); break;
    }
    String targetName = FileChooserUtil.chooseFilename(_ejs, targetFile, "ZIP", new String[]{"zip"}, true);
    if (targetName==null) return;
    boolean warnBeforeOverwritting = true;
    if (! targetName.toLowerCase().endsWith(".zip")) targetName = targetName + ".zip";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
    targetFile = new File(targetName);
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) return;
    }

    // Now, do it
    _ejs.getOutputArea().message("Package.PackagingJarFile",targetFile.getName());

    Set<String> missingAuxFiles = new HashSet<String>();
    Set<String> absoluteAuxFiles = new HashSet<String>();
    Set<PathAndFile> packingList = new HashSet<PathAndFile>();
    for (Object xmlObject : confirmedList) {
      PathAndFile xmlPaf = (PathAndFile) xmlObject;
      packingList.add(xmlPaf);
      String xmlName = xmlPaf.getFile().getName();
      String xmlPrefix = xmlPaf.getPath().substring(0,xmlPaf.getPath().indexOf(xmlName));
      for (PathAndFile paf : OsejsCommon.getAuxiliaryFiles(xmlPaf.getFile(),_ejs.getSourceDirectory(),_ejs.getMainPanel())) {
        if (!paf.getFile().exists()) missingAuxFiles.add(paf.getPath());
        else if (paf.getFile().isDirectory()) { // It is a complete directory
          String prefix = paf.getPath();
          if (prefix.startsWith("./")) prefix = xmlPrefix + prefix.substring(2);
          else absoluteAuxFiles.add(prefix);
          for (File file : JarTool.getContents(paf.getFile())) {
            packingList.add(new PathAndFile(prefix+FileUtils.getRelativePath(file,paf.getFile(),false),file));
          }
        }
        else {
          if (paf.getPath().startsWith("./")) packingList.add(new PathAndFile(xmlPrefix+paf.getPath().substring(2),paf.getFile()));
          else {
            absoluteAuxFiles.add(paf.getPath());
            packingList.add(paf);
          }
        }
      }
    }

    //    for (PathAndFile paf : packingList) System.out.println ("Will pack: "+paf.getPath());

    if (org.opensourcephysics.tools.minijar.MiniJar.compress(packingList, targetFile, null)) {
      _ejs.getOutputArea().println(res.getString("Package.JarFileCreated")+" "+targetFile.getName());
    }
    else _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),absoluteAuxFiles,"Generate.AbsolutePathsFound");
  }


  static public void cleanSimulations(Osejs _ejs) {
    java.util.List<PathAndFile> list = OsejsCommon.getSimulationsMetadataFiles(_ejs.getMainPanel(),_ejs.getOutputDirectory(),
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.CleanSimulationsMessage"),
        res.getString("Package.CleanSimulations"),true,false);
    if (list==null || list.size()<=0) return;
    boolean result = true;
    for (PathAndFile paf : list) {
      Metadata metadata = Metadata.readFile(paf.getFile(), null);
      // Clean all files created during the compilation process
      for (String filename : metadata.getFilesCreated()) {
        File file = new File (paf.getFile().getParentFile(),filename);
        if (!file.delete()) {
          JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
              res.getString("Package.CouldNotDeleteDir")+" "+FileUtils.getPath(file),
              res.getString("Package.Error"), JOptionPane.INFORMATION_MESSAGE);
          result = false;
        }
      }
    }
    if (result) {
      if (_ejs.getOutputDirectory().exists()) FileUtils.removeEmptyDirs(_ejs.getOutputDirectory(),false); // false = Do not remove the "output" directory itself.
      _ejs.getOutputArea().println(res.getString("Package.SimulationsDeleted"));
    }
    else _ejs.getOutputArea().println(res.getString("Package.SimulationsNotDeleted"));
  }

  static public void packageSeveralSimulations(Osejs _ejs) {
    java.util.List<PathAndFile> list = OsejsCommon.getSimulationsMetadataFiles (_ejs.getMainPanel(),_ejs.getOutputDirectory(),
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.PackageAllSimulationsMessage"),
        res.getString("Package.PackageAllSimulations"),true,false);
    if (list==null || list.size()==0) return;

    // Choose target
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    String targetName = FileChooserUtil.chooseFilename(_ejs, new File(_ejs.getExportDirectory(),"ejs_package.jar"), 
        "JAR", new String[]{"jar"}, true);
    if (targetName==null) return;
    boolean warnBeforeOverwritting = true;
    if (! targetName.toLowerCase().endsWith(".jar")) targetName = targetName + ".jar";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
    File targetFile = new File(targetName);
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) return;
    }
    // do it
    packageSeveralSimulations(_ejs,list,targetFile,false);
  }

  static public void packageSeveralXMLSimulations(Osejs _ejs, CreateListDialog.LIST_TYPE listType) {
    CreateListDialog.ListInformation listInfo = CreateListDialog.createZIPFileList(_ejs, _ejs.getMainFrame(), listType);
    if (listInfo.getList().isEmpty()) return;

    // Choose target
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    
    File targetFile;
    String targetName;
    boolean warnBeforeOverwritting = true;
    
    switch(listType) {
      case EPUB : 
        targetFile = new File(_ejs.getExportDirectory(), "ejss_model_package.epub");
        targetName = FileChooserUtil.chooseFilename(_ejs, targetFile, "ePub", new String[]{"epub"}, true);
        if (targetName==null) return;
        if (! targetName.toLowerCase().endsWith(".epub")) targetName = targetName + ".epub";
        else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
        break;
      default :
      case BOOK_APP : 
      case OTHER    :
        targetFile = new File(_ejs.getExportDirectory(), listType==CreateListDialog.LIST_TYPE.OTHER ? "ejss_model_package.zip" : "ejss_bookapp.zip");
        targetName = FileChooserUtil.chooseFilename(_ejs, targetFile, "ZIP", new String[]{"zip"}, true);
        if (targetName==null) return;
        if (! targetName.toLowerCase().endsWith(".zip")) targetName = targetName + ".zip";
        else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
        break;
    }
    targetFile = new File(targetName);
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) return;
    }
    // do it
    switch(listType) {
      case EPUB     : EPub.epubSeveralXMLSimulations(_ejs,listInfo,targetFile); break;
      case BOOK_APP : BookApp.packageSeveralXMLSimulations(_ejs,listInfo,targetFile); break; 
      default :
      case OTHER    : packageSeveralXMLSimulations(_ejs,listInfo,targetFile); break;
    }
  }


  static public void packageAllSimulations(Osejs _ejs) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    File target = new File ( _ejs.getExportDirectory(),"ejs_launcher.jar");
    java.util.List<PathAndFile> list = OsejsCommon.getSimulationsMetadataFiles (_ejs.getMainPanel(),_ejs.getOutputDirectory(),
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.PackageAllSimulationsMessage"),
        res.getString("Package.PackageAllSimulations"),true,false);
    if (list!=null && list.size()>0) {
      PackagerBuilder.create(list, _ejs.getBinDirectory().getParentFile(), _ejs.getSourceDirectory().getParentFile(), 
          target, _ejs.getProcessDialog(), _ejs.getOutputArea().textArea(), _ejs.getMainPanel(),_ejs.getMainFrame());
    }
  }

  static public void createGroupHTML(Osejs _ejs) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    java.util.List<PathAndFile> list = OsejsCommon.getSimulationsMetadataFiles (_ejs.getMainPanel(),_ejs.getOutputDirectory(),
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.CreateGroupHTMLMessage"),
        res.getString("Package.CreateGroupHTML"),true,true);
    if (list==null || list.size()<=0) return;

    JPanel fullJarPanel = new JPanel(new GridLayout(0,1));
    JLabel fullJarLabel = new JLabel(res.getString("Package.HTMLJARCreate"));
    JRadioButton fullJarButton = new JRadioButton(res.getString("Package.IndependentJAR"),true);
    JRadioButton commonJarButton = new JRadioButton(res.getString("Package.CommonJAR"),false);
    fullJarPanel.add(fullJarLabel);
    fullJarPanel.add(fullJarButton);
    fullJarPanel.add(commonJarButton);
    ButtonGroup fullJarGroup = new ButtonGroup();
    fullJarGroup.add(fullJarButton);
    fullJarGroup.add(commonJarButton);

    JPanel fullJarTopPanel = new JPanel (new BorderLayout());
    fullJarTopPanel.add(fullJarPanel,BorderLayout.NORTH);

    // Choose the target HTML file
    File targetFile = new File (_ejs.getExportDirectory(),"ejs_group.html");
    JFileChooser chooser=OSPRuntime.createChooser("HTML",new String[]{"html", "htm"},_ejs.getSourceDirectory().getParentFile());
    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
    chooser.setCurrentDirectory(targetFile.getParentFile());
    chooser.setSelectedFile(targetFile);
    chooser.setAccessory(fullJarTopPanel);

    String targetFilename = OSPRuntime.chooseFilename(chooser,_ejs.getMainPanel(),true);
    if (targetFilename==null) return;

    // Checking for existence of the target HTML file and associated directory
    boolean warnBeforeOverwritting = true;
    if (! (targetFilename.toLowerCase().endsWith(".html") || targetFilename.toLowerCase().endsWith(".htm")) ) 
      targetFilename = targetFilename + ".html";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
    targetFile = new File(targetFilename);
    File groupDirectory = new File (targetFile.getParent(),org.colos.ejs.library.utils.FileUtils.getPlainName(targetFile)+".files");
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFile.getName() + DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"),JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) {
        _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
        return;
      }
    }

    // Ready to do it
    if (groupDirectory.exists()) JarTool.remove(groupDirectory);
    groupDirectory.mkdirs();

    try { 
      FileUtils.saveToFile(targetFile, null, generateGroupHtml(_ejs, list, groupDirectory, org.colos.ejs.library.utils.FileUtils.getPlainName(targetFile),fullJarButton.isSelected())); 
    }
    catch (Exception exc) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Osejs.File.CantCreateFile"),
          res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
      exc.printStackTrace();
      return;
    }

    // Copy all files in the library directory 
    File binConfigDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
    String binConfigDirPath = FileUtils.getPath(binConfigDir);
    for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
      File destFile = new File(groupDirectory,FileUtils.getRelativePath(FileUtils.getPath(file), binConfigDirPath, false));
      if (!JarTool.copy(file,destFile)) {
        JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Osejs.File.CantCreateFile")+" "+destFile.getAbsolutePath(),
            res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
        _ejs.getOutputArea().message("Osejs.File.CantCreateFile",destFile.getAbsolutePath());
        return;
      }
    }

    // But overwrite them with user defined library files
    String configDirPath = FileUtils.getPath(_ejs.getConfigDirectory());
    for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
      File destFile = new File(groupDirectory,FileUtils.getRelativePath(FileUtils.getPath(file), configDirPath, false));
      if (!JarTool.copy(file,destFile)) {
        JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Osejs.File.CantCreateFile")+" "+destFile.getAbsolutePath(),
            res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
        _ejs.getOutputArea().message("Osejs.File.CantCreateFile",destFile.getAbsolutePath());
        return;
      }
    }

    // And overwrite CSS filed with the simulation CSS files, if any
    String cssFilename = _ejs.getSimInfoEditor().getCSSFile();
    if (cssFilename.length()>0) {
      File cssFile = new File(_ejs.getCurrentDirectory(),cssFilename);
      File destFile = new File(groupDirectory,"_ejs_library/css/ejss.css");
      if (!JarTool.copy(cssFile,destFile)) {
        JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Osejs.File.CantCreateFile")+" "+destFile.getAbsolutePath(),
            res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
        _ejs.getOutputArea().message("Osejs.File.CantCreateFile",destFile.getAbsolutePath());
        return;
      }
    }

/*    
    String cssPath = _ejs.getSimInfoEditor().getCSSFolder();
    if (cssPath.length()>0) {
      File cssFolder = new File(_ejs.getCurrentDirectory(),cssPath);
      for (File file : JarTool.getContents(cssFolder)) {
        File destFile = new File(groupDirectory,"_ejs_library/css/"+FileUtils.getRelativePath(file, cssFolder, false));
        if (!JarTool.copy(file,destFile)) {
          JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Osejs.File.CantCreateFile")+" "+destFile.getAbsolutePath(),
              res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
          _ejs.getOutputArea().message("Osejs.File.CantCreateFile",destFile.getAbsolutePath());
          return;
        }
      }
    }
*/
    // Done
    _ejs.getOutputArea().message("Package.GroupHTMLOk",FileUtils.getRelativePath(targetFile,_ejs.getExportDirectory(),false));
  }

  // ----------------------------
  // Utilities
  // ----------------------------


  /**
   * Gets the package for the java file
   * @param _className String The valid plain name, i.e. Whatever_sim
   * @param _fullName String The relative full path, i.e. _users/murcia/fem/Whatever sim.xml
   * @return String
   */
  static private String getPackageName (File _xmlFile, String _className, String _relativePath) {
    //String lastPackageName = "_"+OsejsCommon.firstToLower(_className)+"_";
    String lastPackageName = _className+"_pkg";
    // Avoid conflicts with existing directories (or files)
    // Commented out because this prevented the Translated properties files to be added correctly
    //    int counter = 0;
    //    while (new File (_xmlFile.getParentFile(),lastPackageName).exists() && counter<50) lastPackageName = _className+"_pkg_" + (++counter);

    if (_relativePath.length()<=0) return lastPackageName;
    String packageName = new String();
    StringTokenizer tkn = new StringTokenizer(_relativePath.replace('.','_'),"/");
    while (tkn.hasMoreTokens()) packageName += OsejsCommon.getValidIdentifier(tkn.nextToken()) + ".";
    return packageName + lastPackageName;
  }

  /**
   * Get the class path for compilation
   * @param _list List The list of all user required jar files
   * @param _generationDir File The base directory for generated files
   * @return String
   */
  static private String getClasspath (File _binDir, String _binDirPath, String _srcDirPath, Collection<PathAndFile> _list) {
    StringBuffer textList = new StringBuffer();
    textList.append(_binDirPath+"osp.jar"+ File.pathSeparatorChar);
    textList.append(_binDirPath+"ejs_lib.jar"+ File.pathSeparatorChar);
    String extDirPath = _binDirPath + OsejsCommon.EXTENSIONS_DIR_PATH + "/";
    for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (_binDir,OsejsCommon.EXTENSIONS_DIR_PATH))) {
      textList.append(extDirPath+paf.getPath()+File.pathSeparatorChar);
    }
    for (PathAndFile paf : _list) textList.append(_srcDirPath+paf.getPath()+File.pathSeparatorChar);
    return textList.toString().replace('/',File.separatorChar);
  }


  /**
   * Converts a collection of filenames into a list of MyPathAndFile
   * @param _ejs
   * @param _xmlFile
   * @param _relativeParent
   * @param _pathList
   * @return
   */
  static public Set<PathAndFile> getPathAndFile (Osejs _ejs, File _parentDir, String _relativeParent, Collection<String> _pathList) {
    Set<PathAndFile> list = new HashSet<PathAndFile>();
    for (String path : _pathList) {
      String fullPath = path;
      File file;
      if (fullPath.startsWith("./")) {
        file = new File (_parentDir,fullPath); // Search relatively to the xmlFile location
        fullPath = _relativeParent + fullPath.substring(2); // complete the relative path 
      }
      else file = new File (_ejs.getSourceDirectory(),fullPath); // Search absolutely in EJS user directory
      if (file.exists()) list.add(new PathAndFile(fullPath,file));
      else _ejs.getOutputArea().println(res.getString("Generate.JarFileResourceNotFound")+": "+path);
    }
    return list;
  }


  // ----------------------------------------------------
  // Generation of HTML code
  // ----------------------------------------------------

  static private class SimInfo {
    String name;
	String fullPath;
	String path;
	String classpath;
	String jarPath;  
  }

  static private String generateGroupHtml (Osejs _ejs, java.util.List<PathAndFile> _metadataFilesList, File _groupDirectory, 
      String _targetName, boolean _fullJars) {
    String ret = System.getProperty("line.separator");
    File binDir = _ejs.getBinDirectory();
    String binDirPath = FileUtils.getPath(binDir);
    String outputDirPath = FileUtils.getPath(_ejs.getOutputDirectory());

    // Create the empty list of common files and a dictionary with information
    Set<PathAndFile> commonList = null;
    Set<SimInfo> infoList = new HashSet<SimInfo>();

    // --- Begin of the HTML page for the table of contents
    StringBuffer code = new StringBuffer();
    code.append("<html>"+ret);
    code.append("  <head>"+ret);
    code.append("    <title>Group page</title>"+ret);
    code.append("    <base target=\"_self\">" + ret);
    code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_groupDirectory.getName()+"/_ejs_library/css/ejsGroupPage.css\"></link>" + ret);
    code.append("  </head>"+ret);
    code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
    code.append("    <h1>" + _targetName + "</h1>"+ret);
    code.append("    <div class=\"contents\">"+ret);

    // Possible missing auxiliary files
    Set<String> missingAuxFiles = new HashSet<String>();

    for (PathAndFile metadataPathAndFile : _metadataFilesList) {
      SimInfo info = new SimInfo();
      File generatedDir = metadataPathAndFile.getFile().getParentFile();
      info.fullPath = FileUtils.getPath(generatedDir);
      info.name = org.colos.ejs.library.utils.FileUtils.getPlainName(metadataPathAndFile.getFile());
      info.path = FileUtils.getRelativePath(info.fullPath,outputDirPath,false);
      info.jarPath = FileUtils.getPath(new File(generatedDir,info.name+".jar"));

      // See if there are HTML files for this simulation
      File simHTMLFile = new File (_ejs.getOutputDirectory(),info.path+info.name+".html");
      if (!simHTMLFile.exists()) {
        _ejs.getOutputArea().message("Package.IgnoringSimulation",info.path+info.name);
        continue;
      }
      _ejs.getOutputArea().message("Package.ProcessingSimulation",info.path+info.name);

      // read the meta data
      Metadata metadata = Metadata.readFile(metadataPathAndFile.getFile(), info.path);

      // Process the class path
      info.classpath = metadata.getClassname().replace('.','/');

      // Save the information
      infoList.add(info);

      // Create the entry for this simulation in the common HTML file
      code.append("      <div class=\"simulation\"><b>"+info.name // +res.getString("Generate.HtmlSimulation") 
          +":</b> <a href=\""+ _groupDirectory.getName()+ "/" + info.path + info.name+".html\" target=\"blank\">" + info.name+"</a></div>"+ret);

      String abstractText = metadata.getAbstract();
      //if (abstractText.length()>0) 
      code.append("      <div class=\"abstract\"><b>"+res.getString("SimInfoEditor.Abstract")+"</b> " + abstractText+"</div>"+ret);

      // Add the HTML files
      for (String filename : metadata.getFilesCreated())
        if (filename.endsWith(".html") || filename.endsWith(".xhtml")) {
          File file = new File (generatedDir,filename);
          File targetFile = new File(_groupDirectory,info.path+filename);
          if (_fullJars && (filename.endsWith("_Simulation.html") || filename.endsWith("_Simulation.xhtml"))) { // remove the common.jar entry in the archive parameter of the applet tag 
            String content = FileUtils.readTextFile(file,null);
            content = FileUtils.replaceString(content, "archive=\"common.jar,", "archive=\"");
            try { FileUtils.saveToFile(targetFile,null,content); }
            catch (IOException ioe) {
              _ejs.getOutputArea().println(res.getString("Package.CopyError") + " " + FileUtils.getPath(file));
              return null;
            }
          }
          else if (!JarTool.copy(file,targetFile)) {
            _ejs.getOutputArea().println(res.getString("Package.CopyError") + " " + FileUtils.getPath(file));
            return null;
          }
        }

      // Copy auxiliary files
      for (String auxPath : metadata.getAuxiliaryFiles()) {
        File auxFile = new File(_ejs.getSourceDirectory(),auxPath);
        if (!auxFile.exists()) {
          _ejs.getOutputArea().println(res.getString("Generate.JarFileResourceNotFound")+": "+auxPath);
          missingAuxFiles.add(auxPath);
        }
        else if (auxFile.isDirectory()) { // It is a complete directory
          for (File file : JarTool.getContents(auxFile)) {
            if (!JarTool.copy(file, new File(_groupDirectory,FileUtils.getRelativePath(file,_ejs.getSourceDirectory(),false)))) {
              _ejs.getOutputArea().println(res.getString("Package.CopyError") + " " + FileUtils.getPath(auxFile));
              return null;
            }
          }
        }
        else if (!JarTool.copy(auxFile, new File(_groupDirectory,auxPath))) {
          _ejs.getOutputArea().println(res.getString("Package.CopyError") + " " + FileUtils.getPath(auxFile));
          return null;
        }
      }

      if (!_fullJars) { // Do this only if you don't want to extract the common.jar
        // Create an instance of MiniJar for this simulation
        MiniJar minijarParticular= new MiniJar();
        minijarParticular.addExclude ("++Thumbs.db");
        minijarParticular.addExclude ("++.DS_Store");
        addResources(_ejs,minijarParticular);

        // Add resources needed by the view elements when packaging. For example, CamImage adds  "com/charliemouse/++.gif"
        //        for (String resource : metadata.getResourcePatterns()) minijarParticular.addDesired(resource);

        minijarParticular.addSourcePath(binDirPath+"osp.jar");
        minijarParticular.addSourcePath(binDirPath+"ejs_lib.jar");
        for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (binDir,OsejsCommon.EXTENSIONS_DIR_PATH))) {
          minijarParticular.addSourcePath(FileUtils.getPath(paf.getFile()));
        }
        minijarParticular.addSourcePath(info.jarPath);

        // Add the applet class for this simulation
        minijarParticular.addDesired(info.classpath+"Applet.class");
        //        if (_ejs.getSimInfoEditor().addTranslatorTool()) minijarParticular.addDesired(info.classpath+"+.properties");


        //CJB for collaborative
        if(_ejs.getSimInfoEditor().addAppletColSupport()){
          File collabDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/Collaborative");
          String collabDirPath = FileUtils.getPath(collabDir);
          minijarParticular.addDesired(info.classpath+"AppletStudent.class");
          minijarParticular.addDesired("org/colos/ejs/library/collaborative/images/++");
          for (File file : JarTool.getContents(collabDir)) {
            //System.err.println ("Copiando "+file.getAbsolutePath());
            String destName = FileUtils.getRelativePath(file, collabDirPath, false);
            JarTool.copy(file, new File (_groupDirectory,destName));
          }
        }
        //CJB for collaborative

        // get the matches
        Set<PathAndFile> matches = minijarParticular.getMatches();

        // Check against the common MiniJar
        Set<PathAndFile> newList = new HashSet<PathAndFile>();
        String packagePath = info.classpath;

        if (commonList==null) { // Exclude the files particular to this simulation
          for (PathAndFile paf : matches) if (!paf.getPath().startsWith(packagePath)) newList.add(paf);  
        }
        else { // Exclude the files particular to this simulation
          for (PathAndFile paf : matches) {
            if (paf.getPath().startsWith(packagePath)) continue; // exclude those of this simulation
            if (commonList.contains(paf)) newList.add(paf);  
          }
        }
        commonList = newList;
      }

    } // end of first 'for' for simulations

    File commonJarFile = new File(_groupDirectory,"common.jar");
    if (!_fullJars) { // Create the common jar using MiniJar
      //      Set<String> extraInfo = new HashSet<String>();
      //      extraInfo.add("Permissions: sandbox"); //$NON-NLS-1$
      //      extraInfo.add("Codebase: *\n"); //$NON-NLS-1$
      MiniJar.compress(commonList, commonJarFile, MiniJar.createManifest(".","org.colos.ejs.osejs._EjsSConstants")); //, extraInfo);
    }

    // Now do a second for to create the individual jar files
    for (SimInfo info : infoList) {
      // Create an instance of MiniJar for this simulation
      MiniJar minijarParticular= new MiniJar();
      minijarParticular.setOutputFile(new File (_groupDirectory,info.path+info.name+".jar"));
      //minijarParticular.setManifestFile(null);
      minijarParticular.addExclude ("++Thumbs.db");
      minijarParticular.addExclude ("++.DS_Store");
      addResources(_ejs,minijarParticular);
      minijarParticular.addSourcePath(binDirPath+"osp.jar");
      minijarParticular.addSourcePath(binDirPath+"ejs_lib.jar");
      for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (binDir,OsejsCommon.EXTENSIONS_DIR_PATH))) {
        minijarParticular.addSourcePath(FileUtils.getPath(paf.getFile()));
      }
      minijarParticular.addSourcePath(info.jarPath);
      if (!_fullJars) minijarParticular.addExclude (commonJarFile);

      // Add resources needed by the view elements when packaging. For example, CamImage adds  "com/charliemouse/++.gif"
      //for (String resource : metadata.getResourcePatterns()) minijar.addDesired(resource);

      // Add the applet class for this simulation
      minijarParticular.addDesired(info.classpath+"Applet.class");
      if (_ejs.getSimInfoEditor().addTranslatorTool()) minijarParticular.addDesired(info.classpath+"+.properties");

      //CJB for collaborative
      if(_ejs.getSimInfoEditor().addAppletColSupport()){
        minijarParticular.addDesired(info.classpath+"AppletStudent.class");
        minijarParticular.addDesired("org/colos/ejs/library/collaborative/images/++");
        File collabDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/Collaborative");
        String collabDirPath = FileUtils.getPath(collabDir);
        for (File file : JarTool.getContents(collabDir)) {
          //System.err.println ("Copiando "+file.getAbsolutePath());
          String destName = FileUtils.getRelativePath(file, collabDirPath, false);
          JarTool.copy(file, new File (_groupDirectory,destName));
        }
      }

      //CJB for collaborative

      // get the matches and compress
      minijarParticular.compress();
    }

    code.append("    </div>"+ret); // End of contents
    code.append("  </body>"+ret);
    code.append("</html>"+ret);
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
    return code.toString();
  }

  /**
   * Returns the list of locales desired
   * @param _ejs
   * @return
   */
  static java.util.List<LocaleItem> getLocalesDesired (Osejs _ejs) {
    if (_ejs.getSimInfoEditor().addTranslatorTool()) return _ejs.getTranslationEditor().getDesiredTranslations();
    java.util.List<LocaleItem> localesDesired = new java.util.ArrayList<LocaleItem>();
    localesDesired.add(LocaleItem.getDefaultItem());
    return localesDesired;
  }

  /**
   * Adds the set of HTML files required for a frame access to the simulation description pages.
   * The key in the table is the name of the HTML file. The calling method must process these files.
   * @param _htmlTable Hashtable<String,StringBuffer> The table of HTML files
   * @param _ejs Osejs The calling Ejs
   * @param _simulationName String The name of the simulation
   * @param _filename String The base name for the HTML files (different from _simulationName if generating in a server)
   * @param _classpath String The classpath required to run the simulation.
   * @return Hashtable<String,String>
   * @throws IOException
   */
  
  static private void addFramesHtml (Hashtable<String,StringBuffer> _htmlTable, Osejs _ejs,
      String _simulationName, String _javaName, String _packageName, // _javaName is null for pure javascript pages 
      String _pathToLib, String _archiveStr, boolean hasView) {
    for (LocaleItem item : getLocalesDesired(_ejs)) addFramesHtml(_htmlTable, _ejs, item,_simulationName, _javaName, _packageName, _pathToLib, _archiveStr, hasView);
  }
 
  /*
  static private void addFramesHtml_OLD (Hashtable<String,StringBuffer> _htmlTable, Osejs _ejs,
      String _simulationName, String _javaName, String _packageName, // _javaName is null for pure javascript pages 
      String _pathToLib, String _archiveStr, boolean hasView) {
    String ret = System.getProperty("line.separator");
    boolean left = true;  // Whether to place the content frame at the left frame (true) or at the top frame (false)
    if (_ejs.getOptions().generateHtml()==EjsOptions.GENERATE_TOP_FRAME) left = false;
    //    System.out.println("Generate "+_ejs.getOptions().generateHtml()+ " left = "+left);

    // --- BEGIN OF the HTML page for the table of contents
    StringBuffer code = new StringBuffer();
    if (JSObfuscator.isGenerateXHTML()) {
      code.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
      code.append("<!DOCTYPE html>"+ret);
      code.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">"+ret);
    }
    else code.append("<html>"+ret);
    code.append("  <head>"+ret);
    code.append("    <title>Contents</title>"+ret);
    code.append("    <base target=\"_self\" />" + ret);
    if (left) {
      code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsContentsLeft.css\"></link>" + ret);
    }
    else {
      code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsContentsTop.css\"></link>" + ret);
    }
    code.append("  </head>"+ret);
    String bodyOptions = _ejs.getOptions().getHtmlBody();
    if (bodyOptions.length()>0) code.append("  <body "+_ejs.getOptions().getHtmlBody()+">"+ret);
    else code.append("  <body>"+ret);
    code.append("    <h1>" + ((_javaName!=null) ? _simulationName : _packageName) + "</h1>"+ret);
    code.append("    <h2>" + res.getString("Generate.HtmlContents") + "</h2>"+ret);
    code.append("    <div class=\"contents\">"+ret);

    // Add an entry for each Introduction page created by the user
    String firstPageLink = null;
    Vector<Editor> pageList = _ejs.getDescriptionEditor().getPages();
    int counter = 0;
    int simTab = (_javaName!=null) ? -1 : _ejs.getSimInfoEditor().getSimulationTab();
    boolean simulationAdded = false;

    Set<LocaleItem> localesDesired = getLocalesDesired(_ejs);
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements(); ) {
      HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
      if (htmlEditor.isActive() && !htmlEditor.isInternal()) {
        counter++;
        if (counter==simTab && hasView) {
          simulationAdded = true;
          // And an extra link for the simulation itself!
          String link = _simulationName+(JSObfuscator.isGenerateXHTML() ? "_Simulation.xhtml" : "_Simulation.html");
          code.append("      <div class=\"simulation\"><a href=\""+ link + "\" target=\"central\">" 
              + ((_javaName!=null) ? _ejs.getSimInfoEditor().getSimulationTabTitle() : _packageName) // + res.getString("Generate.HtmlSimulation")
              +"</a></div>"+ret);
          if (firstPageLink==null) firstPageLink = link;
        }
        for (LocaleItem item : localesDesired) {
          OneHtmlPage htmlPage = htmlEditor.getHtmlPage(item);
          if (htmlPage==null || htmlPage.isEmpty()) continue;
          String link;
          if (htmlPage.isExternal()) link = (_javaName!=null) ? _pathToLib + htmlPage.getLink() : _pathToLib + htmlPage.getPlainCode();
          else link = _simulationName + getIntroductionPageKey(counter,item) + ".html"; //(JSObfuscator.isGenerateXHTML() ? ".xhtml" : ".html");
          if (firstPageLink==null) firstPageLink = link;
          code.append("      <div class=\"intro\"><a href=\""+ link+"\" target=\"central\">" + htmlPage.getTitle()+"</a></div>"+ret);
        }
      }
    } // end of for

    if (hasView && !simulationAdded) {
      String link = _simulationName+(JSObfuscator.isGenerateXHTML() ? "_Simulation.xhtml" : "_Simulation.html");
      // And an extra link for the simulation itself!
      code.append("      <div class=\"simulation\"><a href=\""+ link+"\" target=\"central\">" 
          + ((_javaName!=null) ?  _ejs.getSimInfoEditor().getSimulationTabTitle() : _packageName) // + res.getString("Generate.HtmlSimulation")
          +"</a></div>"+ret);
      if (firstPageLink==null) firstPageLink = link;
    }
    code.append("    </div>"+ret); // End of contents
    // ---- Now the logo
    code.append("    <div class=\"signature\">"+ res.getString("Generate.HtmlEjsGenerated") + " "
        + "<a href=\"http://www.um.es/fem/EjsWiki\" target=\"_blank\">Easy Java Simulations</a></div>"+ret);
    code.append("  </body>"+ret);
    code.append("</html>"+ret);

    _htmlTable.put ("_Contents",code);

    // --- END OF the HTML page for the table of contents

    // --- The main HTML page
    code = new StringBuffer();
    if (JSObfuscator.isGenerateXHTML()) {
      code.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
      code.append("<!DOCTYPE html>"+ret);
      code.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">"+ret);
    }
    else code.append("<html>"+ret);
    code.append("  <head>"+ret);
    code.append("    <title> " + res.getString("Generate.HtmlFor") + " " + _simulationName + "</title>"+ret);
    code.append("  </head>"+ret);
    code.append("  <body>"+ret);
    if (left) code.append("    <frameset cols=\"25%,*\">"+ret);
    else      code.append("    <frameset rows=\"90,*\">"+ret);
    code.append("      <frame src=\""+_simulationName+"_Contents."+(JSObfuscator.isGenerateXHTML() ? "xhtml" : "html")+ "\" name=\"contents\" scrolling=\"auto\" target=\"_self\" />"+ret);
    //    if (firstOne!=null) code.append("    <frame src=\""+_filename+"_"+firstOne+".html\"");
    if (firstPageLink!=null) {
      code.append("      <frame src=\""+firstPageLink+"\"");
    }
    else code.append("      <frame src=\""+_simulationName+(JSObfuscator.isGenerateXHTML() ? "_Simulation.xhtml" : "_Simulation.html")+"\"");
    code.append(" name=\"central\" scrolling=\"auto\" target=\"_self\" />"+ret);
    code.append("      <noframes>"+ret);
    code.append("        Gee! Your browser is really old and doesn't support frames. You better update!!!"+ret);
    code.append("      </noframes>"+ret);
    code.append("    </frameset> "+ret);
    code.append("  </body>"+ret);
    code.append("</html>"+ret);

    _htmlTable.put ("",code);

    // --- An HTML page for the simulation itself
    if (hasView && _javaName!=null) {
      code = new StringBuffer();
      if (JSObfuscator.isGenerateXHTML()) {
        code.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
        code.append("<!DOCTYPE html>"+ret);
        code.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">"+ret);
      }
      else code.append("<html>"+ret);
      code.append("  <head>"+ret);
      //code.append("    <base href=\".\" />"+ret);
      code.append("    <title> " + res.getString("Generate.HtmlFor") + " " + _simulationName + "</title>"+ret);
      code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejss.css\"></link>" + ret);
      code.append("  </head>"+ret);
      code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
      code.append(generateHtmlForSimulation (_ejs, _simulationName, _javaName, _packageName, _pathToLib, _archiveStr));
      code.append("  </body>"+ret);
      code.append("</html>"+ret);

      _htmlTable.put ("_Simulation",code);
    }

    // And this makes the set of html files required for a frame configuration
  }
*/

  static void addFramesHtml (Hashtable<String,StringBuffer> _htmlTable, Osejs _ejs, LocaleItem _localeItem,
      String _simulationName, String _javaName, String _packageName, // _javaName is null for pure javascript pages 
      String _pathToLib, String _archiveStr, boolean hasView) {
    String ret = System.getProperty("line.separator");
    boolean left = true;  // Whether to place the content frame at the left frame (true) or at the top frame (false)
    if (_ejs.getOptions().generateHtml()==EjsOptions.GENERATE_TOP_FRAME) left = false;

    //    System.out.println("Generate "+_ejs.getOptions().generateHtml()+ " left = "+left);

    String localeSuffix = _localeItem.isDefaultItem() ? "" : "_"+_localeItem.getKeyword();
    String fileSuffix = JSObfuscator.isGenerateXHTML() ? ".xhtml" : ".html";

    // --- BEGIN OF the HTML page for the table of contents
    StringBuffer code = new StringBuffer();
    if (JSObfuscator.isGenerateXHTML()) {
      code.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
      code.append("<!DOCTYPE html>"+ret);
      code.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">"+ret);
    }
    else {
      code.append("<html>"+ret);
    }
    code.append("  <head>"+ret);
    code.append("    <title>Contents</title>"+ret);
    code.append("    <base target=\"_self\" />" + ret);
    if (left) {
      code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsContentsLeft.css\"></link>" + ret);
    }
    else {
      code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsContentsTop.css\"></link>" + ret);
    }
    code.append("  </head>"+ret);
    String bodyOptions = _ejs.getOptions().getHtmlBody();
    if (bodyOptions.length()>0) code.append("  <body "+_ejs.getOptions().getHtmlBody()+">"+ret);
    else code.append("  <body>"+ret);
    code.append("    <h1>" + ((_javaName!=null) ? _simulationName : _packageName) + "</h1>"+ret);
    code.append("    <h2>" + res.getString("Generate.HtmlContents") + "</h2>"+ret);
    code.append("    <div class=\"contents\">"+ret);

    // Add an entry for each Introduction page created by the user
    String firstPageLink = null;
    Vector<Editor> pageList = _ejs.getDescriptionEditor().getPages();
    int counter = 0;
    int simTab = (_javaName!=null) ? -1 : _ejs.getSimInfoEditor().getSimulationTab();
    boolean simulationAdded = false;
        
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements(); ) {
      HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
      if (htmlEditor.isActive() && !htmlEditor.isInternal()) {
        counter++;
        if (counter==simTab && hasView) {
          simulationAdded = true;
          // And an extra link for the simulation itself!
          String link = (_ejs.getOptions().indexSimFile())? "index.html" : _simulationName+"_Simulation"+ localeSuffix + fileSuffix;
          code.append("      <div class=\"simulation\"><a href=\""+ link + "\" target=\"central\">" 
              + ((_javaName!=null) ? _ejs.getSimInfoEditor().getSimulationTabTitle() : _packageName) // + res.getString("Generate.HtmlSimulation")
              +"</a></div>"+ret);
          if (firstPageLink==null) firstPageLink = link;
        }
        LocaleItem item = _localeItem;
        OneHtmlPage htmlPage = htmlEditor.getHtmlPage(item);
        if (htmlPage==null) {
          if (!_localeItem.isDefaultItem()) htmlPage =  htmlEditor.getHtmlPage(item = LocaleItem.getDefaultItem());
        }
        if (htmlPage==null || htmlPage.isEmpty()) continue;
        String link;
        if (htmlPage.isExternal()) link = (_javaName!=null) ? _pathToLib + htmlPage.getLink() : _pathToLib + htmlPage.getPlainCode();
        else link = _simulationName + getIntroductionPageKey(counter,item) + ".html"; //(JSObfuscator.isGenerateXHTML() ? ".xhtml" : ".html");
        if (firstPageLink==null) firstPageLink = link;
        code.append("      <div class=\"intro\"><a href=\""+ link+"\" target=\"central\">" + htmlPage.getTitle()+"</a></div>"+ret);
      }
    } // end of for

    if (hasView && !simulationAdded) {
      String link = (_ejs.getOptions().indexSimFile())? "index.html" : _simulationName+"_Simulation"+ localeSuffix + fileSuffix;
      // And an extra link for the simulation itself!
      code.append("      <div class=\"simulation\"><a href=\""+ link+"\" target=\"central\">" 
          + ((_javaName!=null) ?  _ejs.getSimInfoEditor().getSimulationTabTitle() : _packageName) // + res.getString("Generate.HtmlSimulation")
          +"</a></div>"+ret);
      if (firstPageLink==null) firstPageLink = link;
    }
    code.append("    </div>"+ret); // End of contents
    // ---- Now the logo
    code.append("    <div class=\"signature\">"+ res.getString("Generate.HtmlEjsGenerated") + " "
        + "<a href=\"http://www.um.es/fem/EjsWiki\" target=\"_blank\">Easy Java Simulations</a></div>"+ret);
    code.append("  </body>"+ret);
    code.append("</html>"+ret);

    _htmlTable.put ("_Contents",code);

    // --- END OF the HTML page for the table of contents

    // --- The main HTML page
    code = new StringBuffer();
    if (JSObfuscator.isGenerateXHTML()) {
      code.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
      code.append("<!DOCTYPE html>"+ret);
      code.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">"+ret);
    }
    else code.append("<html>"+ret);
    code.append("  <head>"+ret);
    code.append("    <title> " + res.getString("Generate.HtmlFor") + " " + _simulationName + "</title>"+ret);
    code.append("  </head>"+ret);
    code.append("  <body>"+ret);
    if (left) code.append("    <frameset cols=\"25%,*\">"+ret);
    else      code.append("    <frameset rows=\"90,*\">"+ret);
    code.append("      <frame src=\""+_simulationName+"_Contents"+localeSuffix+fileSuffix+ "\" name=\"contents\" scrolling=\"auto\" target=\"_self\" />"+ret);
    if (firstPageLink!=null) {
      code.append("      <frame src=\""+firstPageLink+"\"");
    }
    else {
      String link = (_ejs.getOptions().indexSimFile())? "index.html" : _simulationName+"_Simulation"+ localeSuffix + fileSuffix;
      code.append("      <frame src=\""+link+"\"");
    }
    code.append(" name=\"central\" scrolling=\"auto\" target=\"_self\" />"+ret);
    code.append("      <noframes>"+ret);
    code.append("        Gee! Your browser is really old and doesn't support frames. You better update!!!"+ret);
    code.append("      </noframes>"+ret);
    code.append("    </frameset> "+ret);
    code.append("  </body>"+ret);
    code.append("</html>"+ret);

    _htmlTable.put ("",code);

    // --- An HTML page for the simulation itself
    if (hasView && _javaName!=null) {
      code = new StringBuffer();
      if (JSObfuscator.isGenerateXHTML()) {
        code.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
        code.append("<!DOCTYPE html>"+ret);
        code.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">"+ret);
      }
      else code.append("<html>"+ret);
      code.append("  <head>"+ret);
      //code.append("    <base href=\".\" />"+ret);
      code.append("    <title> " + res.getString("Generate.HtmlFor") + " " + _simulationName + "</title>"+ret);
      code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejss.css\"></link>" + ret);
      code.append("  </head>"+ret);
      code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
      code.append(generateHtmlForSimulation (_ejs, _simulationName, _javaName, _packageName, _pathToLib, _archiveStr));
      code.append("  </body>"+ret);
      code.append("</html>"+ret);

      _htmlTable.put ("_Simulation",code);
    }

    // And this makes the set of html files required for a frame configuration
  }

  static public String getIntroductionPageKey(int counter, LocaleItem localeItem) {
    return "_Intro_"+counter+ ( (localeItem==null || localeItem.isDefaultItem()) ? "" : "_"+localeItem.getKeyword());
  }
  
  /**
   * Adds the set of HTML files for the simulation description pages.
   * The key in the table is the name of the HTML file. The calling method must process these files.
   * @param _htmlTable Hashtable<String,StringBuffer> The table of HTML files
   * @param _ejs Osejs The calling Ejs
   * @param _simulationName String The name of the simulation
   * @param _filename String The base name for the HTML files (different from _simulationName if generating in a server)
   * @param _classpath String The classpath required to run the simulation.
   * @return Hashtable<String,String>
   * @throws IOException
   */
  static void addDescriptionHtml (Hashtable<String,StringBuffer> _htmlTable, Osejs _ejs, String _simulationName, String _pathToLib) {
    java.util.List<LocaleItem> localesDesired = getLocalesDesired(_ejs);
    for (LocaleItem item : localesDesired) addDescriptionHtml (_htmlTable, _ejs, item, _simulationName, _pathToLib);
  }

  static void addDescriptionHtml (Hashtable<String,StringBuffer> _htmlTable, Osejs _ejs, LocaleItem _localeItem,
      String _simulationName, String _pathToLib) {

    String ret = System.getProperty("line.separator");
    // --- An HTML page for each introduction page
    int counter = 0;
    for (java.util.Enumeration<Editor> e = _ejs.getDescriptionEditor().getPages().elements(); e.hasMoreElements(); ) {
      HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
      if (htmlEditor.isInternal()) continue;
      counter++;
      OneHtmlPage htmlPage = htmlEditor.getHtmlPage(_localeItem);
      if (htmlPage==null || htmlPage.isExternal() || htmlPage.isEmpty()) continue;
      StringBuffer code = new StringBuffer();
      code.append("<html>"+ret);
      code.append("  <head>"+ret);
      code.append("    <title> " + htmlPage.getTitle() + "</title>"+ret);
      code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejss.css\"></link>" + ret);
      for (String filename : _ejs.getSimInfoEditor().getMoreCSSFiles()) {
        code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+filename+"\"></link>" + ret);
      }
      code.append("  </head>"+ret);
      code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
      code.append(htmlPage.getHtmlCode (null));
      code.append("  </body>"+ret);
      code.append("</html>"+ret);
      _htmlTable.put (getIntroductionPageKey(counter,null),code);
    }
  }
  
  /**
   * Generates the code for a single HTML page with everything, and adds it to a table of HTML pages
   * @param _htmlTable Hashtable<String,StringBuffer> The table of html pages 
   * @param _ejs Osejs The calling Ejs
   * @param _simulationName String The name of the simulation
   * @param _filename String The base name for the HTML files (different from _name if generating in a server)
   * @param _classpath String The classpath required to run the simulation.
   * @return StringBuffer A StringBuffer with the code
   * @throws IOException
   */
  static private void addNoFramesHtml (Hashtable<String,StringBuffer> _htmlTable, Osejs _ejs, String _simulationName, String _javaName, String _packageName,
      String _pathToLib, String _archiveStr) {
    String ret = System.getProperty("line.separator");
    java.util.List<LocaleItem> localesDesired = getLocalesDesired(_ejs);

    StringBuffer code = new StringBuffer();
    if (JSObfuscator.isGenerateXHTML()) {
      code.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
      code.append("<!DOCTYPE html>"+ret);
      code.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">"+ret);
    }
    else code.append("<html>"+ret);
    code.append("  <head>"+ret);
    //code.append("    <base href=\""+_pathToLib+"\" />"+ret);
    code.append("    <title> " + res.getString("Generate.HtmlFor") + " " + _simulationName + "</title>"+ret);
    code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejss.css\"></link>" + ret);
    code.append("  </head>"+ret);
    code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
    code.append(res.getString("Generate.HtmlUserCode")+ret);

    // --- An HTML page for each introduction page
    //    int counter = 0;
    String info = "<!--- " + res.getString("Osejs.Main.Description") + ".";
    String separator = ret + "<br><hr width=\"100%\" size=\"2\"><br>" + ret;
    for (java.util.Enumeration<Editor> e = _ejs.getDescriptionEditor().getPages().elements(); e.hasMoreElements(); ) {
      HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
      if (!htmlEditor.isActive() || htmlEditor.isInternal()) continue;
      //      counter++;
      for (LocaleItem item : localesDesired) {
        OneHtmlPage htmlPage = htmlEditor.getHtmlPage(item);
        if (htmlPage==null || htmlPage.isExternal() || htmlPage.isEmpty()) continue;
        code.append(info);
        code.append(htmlEditor.getName() + (item.isDefaultItem() ? "" : item.toString()) + " --->" + ret);
        code.append(htmlPage.getHtmlCode (null));
        code.append(separator);
      }
    }
    //    code.append(_ejs.getDescriptionEditor().generateCode(Editor.GENERATE_CODE,res.getString("Osejs.Main.Description")));

    if (_javaName!=null) {
      code.append(res.getString("Generate.HtmlHereComesTheApplet")+ret);
      code.append(generateHtmlForSimulation (_ejs,_simulationName, _javaName, _packageName,_pathToLib,_archiveStr));
    }
    code.append("  </body>"+ret);
    code.append("</html>"+ret);
    _htmlTable.put ("",code);
  }

  /**
   * Generates the code for the applet tag which includes the simulation in an HTML page
   * @param _ejs Osejs The calling EJS
   * @param _simulationName String The name of the simulation
   * @param _packageName String The package of the class
   * @param _classpath String The class path required to run the simulation as an applet
   * @return StringBuffer A StringBuffer with the code
   */
  static private String generateHtmlForSimulation (Osejs _ejs, String _simulationName, String _javaName, String _packageName,
      String _pathToLib, String _archiveStr) {
    String ret = System.getProperty("line.separator");
    String captureTxt = _ejs.getViewEditor().generateCode(Editor.GENERATE_CAPTURE_WINDOW,"").toString();
    StringBuffer code = new StringBuffer();
    code.append("    <div class=\"appletSection\">"+ret);
    if (captureTxt.trim().length()<=0) code.append("      <h3>"+res.getString("Generate.HtmlHereItIsNot")+"</h3>"+ret);
    else code.append("      <h3>"+res.getString("Generate.HtmlHereItIs")+"</h3>"+ret);
    code.append("      <applet code=\""+ _packageName + "." + _javaName+"Applet.class\"" +ret);

    if (_pathToLib.length()<=0) code.append("              codebase=\".\"");
    else code.append("              codebase=\""+_pathToLib+"\"");
    code.append(" archive=\""+_archiveStr +"\"" +ret);
    code.append("              name=\"" + _javaName + "\"  id=\"" + _javaName + "\""+ret);
    if (captureTxt.trim().length()<=0) code.append("              width=\"0\" height=\"0\"");
    else code.append("      "+captureTxt);

    code.append(">"+ret);
    code.append("        <param name=\"draggable\" value=\"true\" />"+ret);
    code.append("        <param name=\"permissions\" value=\"sandbox\" />"+ret);
    code.append("      </applet>"+ret);
    code.append("    </div>"+ret);

    //    if (_ejs.getOptions().experimentsEnabled() && _ejs.getExperimentEditor().getActivePageCount()>0) {
    //      code.append("    <div class=\"experimentsSection\">"+ret);
    //      code.append("      <h3>" + res.getString("Generate.TheExperiments")+"</h3>"+ret);
    //      code.append(_ejs.getExperimentEditor().generateCode(Editor.GENERATE_DECLARATION,_javaName));
    //      code.append("      <div class=\"killExperiment\"><a href=\"javascript:document."+_javaName+"._simulation.killExperiment();\">"+
    //                      ToolsRes.getString("MenuItem.KillExperiment") + "</a></div>"+ret);
    //      code.append("    </div>"+ret);
    //    }

//    code.append("      "+res.getString("Generate.HtmlHereComesTheJavaScript")+ret);
    code.append("    <div class=\"jsSection\">"+ret);
    code.append("      <h3>" + res.getString("Generate.HtmlJSControl")+"</h3>"+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlPlay","_play()")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlPause","_pause()")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlReset","_reset()")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlStep","_step()")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlSlow","_setDelay(1000)")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlFast","_setDelay(100)")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlFaster","_setDelay(10)")+ret);
    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
      code.append(jsCommand(_javaName,"Generate.HtmlStartCollaboration","_simulation.startColMethod()")+ret);
    //CJB for collaborative
    code.append("    </div>"+ret);
    return code.toString();
  }

  static private String jsCommand (String _javaName, String _label, String _method) {
    //    return "<input type=\"BUTTON\" value=\"" + res.getString(_label)  +
    //      "\" onclick=\"document." + _javaName + "."+_method+";document." + _javaName + "._simulation.update();\";>";
    return "      <a href=\"javascript:document."+_javaName+"."+_method+";document."+_javaName+"._simulation.update();\">"+
    res.getString(_label)  + "</a> ";
  }

  /**
   * Generates the JNLP file required to run the simulation using Java Web Start
   * @param _simulationName String The name of the simulation
   * @param _filename String The base name for the HTML files (different from _name if generating in a server)
   * @param _jarList AbstractList The list of jar files required to run the simulation
   * @param _jnlpURL String The URL for the Java Web Start server
   * @return StringBuffer A StringBuffer with the code
   *
  static private String generateJNLP (Osejs _ejs, String _path, 
                                            String _simulationName, String _classname, String _packageName,
                                            List<PathAndFile> _jarList) {
    String jnlpURL = FileUtils.correctUrlString(_ejs.getOptions().jnlpURL());
    StringBuffer code = new StringBuffer();
    String pathCorrected = FileUtils.correctUrlString(_path);
    String ret = System.getProperty("line.separator");
    if (jnlpURL.length()<=0) jnlpURL = "http://localhost/jaws";
    code.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
    code.append("<jnlp spec=\"1.0\" codebase=\""+jnlpURL+"/"+pathCorrected+
      "\" href=\"" + FileUtils.correctUrlString(_simulationName)+".jnlp\">"+ret);
    code.append("  <information>"+ret);
    code.append("    <title>"+_simulationName+"</title>"+ret);
    code.append("    <vendor>"+res.getString("Generate.HtmlEjsGenerated")+ " Ejs</vendor>"+ret);
    code.append("    <homepage href=\"http://fem.um.es/Ejs\"/>"+ret);
    code.append("    <description>"+res.getString("Generate.HtmlEjsGenerated")+ " Ejs</description>"+ret);
    code.append("    <icon href=\"_library/EjsIcon.gif\"/>"+ret);
    code.append("    <offline-allowed/>"+ret);
    code.append("  </information>"+ret);
    code.append("  <resources>"+ret);
    code.append("    <j2se version=\"1.4.2+\" href=\"http://java.sun.com/products/autodl/j2se\"/>"+ret);
    code.append("    <jar href=\""+FileUtils.correctUrlString(OsejsCommon.firstToLower(_simulationName))+".jar\"/>"+ret);
    for (Iterator it = _jarList.iterator(); it.hasNext(); ) {
      code.append("    <jar href=\"" + FileUtils.correctUrlString((String) it.next()) + "\"/>" + ret);
    }
    code.append("    <!-- Simulations use the following codebase to find resources -->"+ret);
    code.append("    <property name=\"jnlp.codebase\" value=\""+jnlpURL+"/"+pathCorrected+"\"/>"+ret);
    code.append("  </resources>"+ret);
    code.append("  <application-desc main-class=\""+ _packageName+"."+_classname+"\"/>"+ret);
    code.append("</jnlp>"+ret);
    return  code.toString();
  }
   */
  // ----------------------------------------------------
  // Generation of Java code
  // ----------------------------------------------------

  /**
   * Generates the header for the Java classes
   * @param _filename String The name of the simulation file
   * @param _suffix String Either "" for the simulation or "Applet" for the applet
   * @return StringBuffer
   */
  static private String generateHeader (Osejs _ejs, String _classname, String _packageName, String _whichClass, boolean _addImports) {
    StringBuffer txt = new StringBuffer();
    txt.append("/*\n");
    txt.append(" * Class : "+_classname+_whichClass+".java\n");
    txt.append(" *  Generated using ");
    txt.append(" *  Easy Java/Javascript Simulations Version "+_EjsSConstants.VERSION+", build "+_EjsSConstants.VERSION_DATE+". Visit "+_EjsSConstants.WEB_SITE+"\n");
    txt.append(" */ \n\n");
    txt.append("package " + _packageName + ";\n\n");
    txt.append("import org.colos.ejs.library._EjsConstants;\n\n");
    if (!_addImports) return txt.toString();
    // Add imports
    for (String anImport : _ejs.getSimInfoEditor().getImportsList()) txt.append("import "+anImport+"\n");
    String modelElementsImports = _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_IMPORT_STATEMENTS,"").toString();
    if (modelElementsImports!=null) {
      txt.append("// Imports suggested by Model Elements:\n");
      StringTokenizer tkn = new StringTokenizer(modelElementsImports,";");
      while(tkn.hasMoreTokens()) {
        String anImport = tkn.nextToken().trim();
        if (anImport.length()>0) txt.append("import "+anImport+";\n");
      }
      txt.append("// End of imports from Model Elements\n\n");
    }

    return txt.toString();
  }


  static private void addCheckPasswordCode(Osejs _ejs, String _xmlName, String _returnValue, StringBuffer _buffer) {
    String execPassword = _ejs.getSimInfoEditor().getExecPassword();
    if (execPassword.length()>0) {
      String codedPassword = new Cryptic(execPassword).getCryptic();
      _buffer.append("      try {\n");
      _buffer.append("        boolean __identified=false;\n");
      _buffer.append("        String __codedPassword = System.getProperty(\"launcher.password\");\n");
      //      _buffer.append("        System.out.println (\"Password is \"+__codedPassword);\n");
      _buffer.append("        if (__codedPassword!=null) {\n");
      //      _buffer.append("          __codedPassword = __codedPassword.substring(1,__codedPassword.length()-1);\n");
      //      _buffer.append("          System.out.println (\"Final password is \"+__codedPassword);\n");
      _buffer.append("          __identified = \""+codedPassword+"\".equals(__codedPassword);\n");
      _buffer.append("        }\n");
      _buffer.append("        if (!__identified) { // Ask the user for the password\n");
      _buffer.append("          java.awt.Window infoWindow = null;\n");

      Vector<Editor> pageList = _ejs.getDescriptionEditor().getPages();
      //      int counter = 0;
      for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements(); ) {
        HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
        if (!htmlEditor.isInternal()) {
          //          counter++;
          java.awt.Dimension size = htmlEditor.getComponent().getSize();
          if (size.width<=0 || size.height<=0) size = _ejs.getOptions().getHtmlPanelSize(); // This happens in batch compilation
          _buffer.append("          org.colos.ejs.library.utils.HtmlPageInfo htmlPageInfo = "+_xmlName+"._getHtmlPageClassInfo(\""+htmlEditor.getName()+"\", org.colos.ejs.library.utils.LocaleItem.getDefaultItem());\n");
          _buffer.append("          if (htmlPageInfo!=null) org.colos.ejs.library.utils.PasswordDialog.showInformationPage (\""+_xmlName+"\", htmlPageInfo.getTitle(),htmlPageInfo.getLink(),"+
              //          _buffer.append("          infoWindow = org.colos.ejs.library.utils.PasswordDialog.showInformationPage(\""+_xmlName+"\",\""+htmlEditor.getName()+"\","+
              size.width+","+size.height+");\n");
          break; // Only one page
        }
      }
      _buffer.append("          if (org.colos.ejs.library.utils.PasswordDialog.checkPassword(\""+codedPassword+"\",null,infoWindow)==null) return "+_returnValue+";\n");
      _buffer.append("        }\n");
      _buffer.append("      } catch (Exception exc) {} // do nothing on error\n");
    }
  }

  /**
   * Generates the Java code for the applet
   * @param _filename String The name of the simulation file
   * @param _mainFrame String The name of the main frame in Ejs' view
   * @return StringBuffer
   */
  static private String generateApplet (Osejs _ejs, String _classname, String _packageName, String _parentPath, String _mainFrame) {
    StringBuffer code = new StringBuffer();
    code.append(generateHeader(_ejs, _classname, _packageName, "Applet", true));
    if(_ejs.getSimInfoEditor().addAppletColSupport()) code.append("public class " + _classname + "Applet extends org.colos.ejs.library.collaborative.LauncherAppletCollaborative {\n\n");
    else code.append("public class " + _classname + "Applet extends org.colos.ejs.library.LauncherApplet {\n\n");

    code.append("  static {\n");
    //    if (_ejs.getSimInfoEditor().addTranslatorTool()) code.append("    org.opensourcephysics.display.OSPRuntime.loadTranslatorTool = !org.opensourcephysics.display.OSPRuntime.appletMode;\n");
    //    else code.append("    org.opensourcephysics.display.OSPRuntime.loadTranslatorTool = false;\n");
    code.append("    org.opensourcephysics.display.OSPRuntime.loadTranslatorTool = false;\n");
    if (!_ejs.getSimInfoEditor().addCaptureTools())   code.append("    org.opensourcephysics.display.OSPRuntime.loadVideoTool = false;\n");
    if (!_ejs.getSimInfoEditor().addToolsForData())   {
      code.append("    org.opensourcephysics.display.OSPRuntime.loadDataTool = false;\n");
      //      code.append("    org.opensourcephysics.display.OSPRuntime.loadFourierTool = false;\n");
    }
    code.append("    org.opensourcephysics.display.OSPRuntime.loadExportTool = false;\n");
    code.append("  }\n\n");

    //code.append("  private java.awt.Component mainComponent=null;\n\n");
    code.append("  public void init () {\n");
    code.append("    super.init();\n");
    code.append("    org.opensourcephysics.tools.ResourceLoader.addAppletSearchPath(\"/"+_parentPath +"\");\n");
    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(getCodeBase()+\""+_parentPath+"\"); // This is for relative files\n");
    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+_parentPath+"\"); // This is for relative files, too\n");
    code.append("    //org.colos.ejs.library.Simulation.setPathToLibrary(getCodeBase()); // This is for classes (such as EjsMatlab) which needs to know where the library is\n");
    addHtmlPagesMapCode (_ejs,_classname, code);
    addCheckPasswordCode (_ejs,_classname,"",code);

    code.append("    if (getParentFrame()!=null) {\n");
    code.append("      _model = new "+_classname+" ("+_mainFrame+",getParentFrame(),getCodeBase(),this,(String[])null,true);\n");
    if(_ejs.getSimInfoEditor().addAppletColSupport()) code.append("      _simulation = _model.getSimulationCollaborative();\n");
    else code.append("      _simulation = _model._getSimulation();\n");
    code.append("      _view = _model._getView();\n");
    //code.append("      mainComponent = captureWindow (_model._getView(),"+_mainFrame+");\n");
    code.append("    }\n");
    code.append("    else {\n");
    code.append("      _model = new "+_classname+" (null,null,getCodeBase(),this,(String[])null,true);\n");
    if(_ejs.getSimInfoEditor().addAppletColSupport()) code.append("      _simulation = _model.getSimulationCollaborative();\n");
    else code.append("      _simulation = _model._getSimulation();\n");
    code.append("      _view = _model._getView();\n");
    code.append("    }\n");
    //    code.append("    try {\n");
    //    code.append("      String param = getParameter (\"init\");\n");
    //    code.append("      if (param!=null) {\n");
    //    code.append("         (("+_name+")_model).__initMethod = new org.colos.ejs.library.control.MethodWithOneParameter (0,_model,param,null,null,this);\n");
    //    code.append("         (("+_name+")_model).__initMethod.invoke(0,this);\n");
    //    code.append("      }\n");
    //    code.append("    }\n");
    //    code.append("    catch (Exception e) { e.printStackTrace (); }\n");
    //code.append("    _simulation.setParentComponent(mainComponent);\n");
    code.append("    _simulation.initMoodle();\n");
    if(_ejs.getSimInfoEditor().addAppletColSupport()) code.append("    _simulation.startColMoodle();\n");
    code.append("  }\n");
    //    code.append("  public java.awt.Component _getMainComponent() { return mainComponent; }\n");
    //    code.append("  public void _setMainComponent(java.awt.Component _comp) { mainComponent = _comp; }\n");
    code.append("  public void _reset() { (("+_classname+")_model)._reset(); }\n");
    code.append("  public void _initialize() { (("+_classname+")_model)._initialize(); }\n");
    //    code.append("  public void stop() { (("+_classname+")_model)._onExit(); }\n");
    code.append("  public void stop() { _model.getSimulation().onExit(); }\n");
    code.append("} // End of class " + _classname + "Applet\n\n");
    return code.toString();
  }

  //CJB for collaborative
  /**
   * Generates the Java code for the applet student collaborative
   * @param _filename String The name of the simulation file
   * @param _mainFrame String The name of the main frame in Ejs' view
   * @return String
   */
  static private String generateAppletStudent(Osejs _ejs, String _classname, String _packageName, String _parentPath, String _mainFrame) {
    StringBuffer code = new StringBuffer();
    code.append(generateHeader(_ejs, _classname, _packageName, "AppletStudent", true));
    code.append("public class " + _classname + "AppletStudent extends org.colos.ejs.library.collaborative.LauncherAppletCollaborative {\n\n");
    code.append("  static {\n");

    code.append("    org.opensourcephysics.display.OSPRuntime.loadTranslatorTool = false;\n");
    if (!_ejs.getSimInfoEditor().addCaptureTools())   code.append("    org.opensourcephysics.display.OSPRuntime.loadVideoTool = false;\n");
    if (!_ejs.getSimInfoEditor().addToolsForData())   {
      code.append("    org.opensourcephysics.display.OSPRuntime.loadDataTool = false;\n");
      //	      code.append("    org.opensourcephysics.display.OSPRuntime.loadFourierTool = false;\n");
    }
    code.append("    org.opensourcephysics.display.OSPRuntime.loadExportTool = false;\n");
    code.append("  }\n\n");

    code.append("  public void init () {\n");
    code.append("    super.init();\n");
    code.append("    org.opensourcephysics.tools.ResourceLoader.addAppletSearchPath(\"/"+_parentPath +"\");\n");
    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(getCodeBase()+\""+_parentPath+"\"); // This is for relative files\n");
    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+_parentPath+"\"); // This is for relative files, too\n");
    addHtmlPagesMapCode (_ejs,_classname, code);
    addCheckPasswordCode (_ejs,_classname,"",code);

    code.append("    if (getParentFrame()!=null) {\n");
    code.append("      _model = new "+_classname+" ("+_mainFrame+",getParentFrame(),getCodeBase(),this,(String[])null, true);\n");
    code.append("      _simulation = _model.getSimulationCollaborative();\n");
    code.append("      _view = _model.getView();\n");
    code.append("    }\n");
    code.append("    else {\n");
    code.append("      _model = new "+_classname+" (null,null,getCodeBase(),this,(String[])null, true);\n");
    code.append("      _simulation = _model.getSimulationCollaborative();\n");
    code.append("      _view = _model.getView();\n");
    code.append("    }\n");
    code.append("    _simulation.initMoodle();\n");
    code.append("    try {\n");
    code.append("      String IP_Teacher = getParameter (\"IP_Teacher\");\n");
    code.append("      String Port_Teacher = getParameter (\"Port_Teacher\");\n");
    code.append("      String Package_Teacher = getParameter (\"Package_Teacher\");\n");
    code.append("      String MainFrame_Teacher = getParameter (\"MainFrame_Teacher\");\n");
    code.append("      String[] _argsCol = {IP_Teacher, Port_Teacher, Package_Teacher, MainFrame_Teacher};\n");
    //code.append("      _simulation.setParentComponent(mainComponent);\n");
    //code.append("      _simulation.init();\n");
    code.append("      _simulation.setIsThereTeacher(true);\n");
    code.append("      _simulation.setStudentSim(true);\n");
    code.append("      _simulation.setArgsCol(_argsCol);\n");
    code.append("      _simulation.startColMoodle();\n"); //CJB. Moodle implementation
    code.append("    }\n");
    code.append("    catch (Exception e) { e.printStackTrace (); }\n");
    code.append("  }\n");
    code.append("  public void _reset() { (("+_classname+")_model)._reset(); }\n");
    code.append("  public void _initialize() { (("+_classname+")_model)._initialize(); }\n");
    //	    code.append("  public void stop() { (("+_classname+")_model)._onExit(); }\n");
    code.append("  public void stop() { _model.getSimulation().onExit(); }\n");
    code.append("} // End of class " + _classname + "AppletStudent\n\n");
    return code.toString();
  }
  //CJB for collaborative


  /**
   * Generates the Java code for the model of the simulation
   */
  static private String generateModel (Osejs _ejs, String _classname, String _packageName, 
      String _sourceXML, String _parentPath, 
      File _generatedDirectory, Collection<PathAndFile> _resList, String _mainFrame, 
      boolean _serverMode, int _port) {

    StringBuffer code = new StringBuffer();
    code.append(generateHeader(_ejs,_classname,_packageName,"", true)); 

    TabbedEvolutionEditor evolutionEditor = _ejs.getModelEditor().getEvolutionEditor();

    Editor initializationEditor = _ejs.getModelEditor().getInitializationEditor();
    Editor constraintsEditor = _ejs.getModelEditor().getConstraintsEditor();
    Editor varTableEditor = _ejs.getModelEditor().getVariablesEditor();

    code.append("import javax.swing.event.*;\n");
    code.append("import javax.swing.*;\n");
    code.append("import java.awt.event.*;\n");
    code.append("import java.awt.*;\n");
    code.append("import java.net.*;\n");
    code.append("import java.util.*;\n");
    code.append("import java.io.*;\n");
    code.append("import java.lang.*;\n\n");
    code.append("import javax.json.*;\n\n");
    if (_serverMode || _ejs.getViewEditor().isEmpty()) code.append("import org.colos.ejs.library.View;\n\n"); // JSV
    //for (String anImport : _ejs.getSimInfoEditor().getImportsList()) code.append("import "+anImport+"\n");
    //code.append("@SuppressWarnings(\"unchecked\")\n");

    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
      code.append("public class " + _classname + " extends org.colos.ejs.library.collaborative.AbstractModelCollaborative {\n\n");
    else
      code.append("public class " + _classname + " extends org.colos.ejs.library.Model {\n\n");
    //CJB for collaborative

    code.append("  static {\n");
    if (_ejs.getSimInfoEditor().useMacScreenMenuBar()) {
      code.append("    try {\n");
      code.append("      if (System.getProperty(\"os.name\", \"\").toLowerCase().startsWith(\"mac\")) {\n"); // Do not use OSPRuntime because it is too late
      code.append("        System.setProperty(\"apple.laf.useScreenMenuBar\", \"true\");\n");
      code.append("        System.setProperty(\"com.apple.mrj.application.apple.menu.about.name\", \""+_classname+"\");\n");
      code.append("      }\n");
      code.append("    } catch(Exception exc) {} // Do nothing\n");
    }
    if (_ejs.getSimInfoEditor().addToolsForData()) {
      code.append("    org.opensourcephysics.tools.ToolForData.setTool(new org.opensourcephysics.tools.ToolForDataFull());\n");
    }
    if (_ejs.getSimInfoEditor().addTranslatorTool() && (_serverMode==false)) {
      code.append("    __translatorUtil = new org.colos.ejs.library.utils.TranslatorResourceUtil(\""+_packageName+"."+_classname+"\");\n");
      for (LocaleItem item : _ejs.getTranslationEditor().getDesiredTranslations()) 
        if (!item.isDefaultItem()) code.append("    __translatorUtil.addTranslation(\""+item.getKeyword()+"\");\n");
    }
    else {
      code.append("    __translatorUtil = new org.colos.ejs.library.utils.TranslatorUtil();\n");
      for (TwoStrings ts : _ejs.getTranslationEditor().getResourceDefaultPairs()) 
        code.append ("    __translatorUtil.addString(\""+ts.getFirstString()+"\",\""+ts.getSecondString()+"\");\n");
    }
    code.append("  }\n\n");

    //if (_serverMode) 
    {
      code.append("  static public boolean _sSwingView = true;\n\n");
      code.append("  static public int _sServerPort = "+_port+";\n");
      code.append("  static public int _getServerPort() { return _sServerPort; }\n\n");
    }

    //    code.append("  static {\n");
    //    if (_ejs.getOptions().addPrintTool()) code.append("    printUtil = new org.colos.ejs.library.utils.PrintUtilClass();\n");
    //    else                                  code.append("    printUtil = new org.colos.ejs.library.utils.PrintUtil();\n");
    //    code.append("  }\n\n");

    code.append("  public " + _classname + "Simulation _simulation=null;\n");
    if (_serverMode) {
      code.append("  public " + "View _view=null;\n"); // JSV
      // code.append("  public " + _classname + "SimulationView _view=null;\n");
      code.append("  public " + _classname + "ServerView _htmlView=null;\n");
    }
    else if (_ejs.getViewEditor().isEmpty()) code.append("  public " + "View _view=null;\n");
    else code.append("  public " + _classname + "View _view=null;\n");

    code.append("  public " + _classname + " _model=this;\n\n");

    code.append("  // -------------------------- \n");
    code.append("  // Information on HTML pages\n");
    code.append("  // -------------------------- \n\n");

    code.append("  static private java.util.Map<String,java.util.Set<org.colos.ejs.library.utils.HtmlPageInfo>> __htmlPagesMap =\n" +
        "    new java.util.HashMap<String,java.util.Set<org.colos.ejs.library.utils.HtmlPageInfo>>();\n\n");

    code.append("  /**\n");
    code.append("   * Adds info about an html on the model\n");
    code.append("   */\n");
    code.append("  static public void _addHtmlPageInfo(String _pageName, String _localeStr, String _title, String _link) {\n");
    code.append("    java.util.Set<org.colos.ejs.library.utils.HtmlPageInfo> pages = __htmlPagesMap.get(_pageName);\n");
    code.append("    if (pages==null) {\n");
    code.append("      pages = new java.util.HashSet<org.colos.ejs.library.utils.HtmlPageInfo>();\n");
    code.append("      __htmlPagesMap.put(_pageName, pages);\n");
    code.append("    }\n");
    code.append("    org.colos.ejs.library.utils.LocaleItem item = org.colos.ejs.library.utils.LocaleItem.getLocaleItem(_localeStr);\n");
    code.append("    if (item!=null) pages.add(new org.colos.ejs.library.utils.HtmlPageInfo(item, _title, _link));\n");
    code.append("  }\n\n");

    code.append("  /**\n");
    code.append("   * Returns info about an html on the model\n");
    code.append("   */\n");
    code.append("  static public org.colos.ejs.library.utils.HtmlPageInfo _getHtmlPageClassInfo(String _pageName, org.colos.ejs.library.utils.LocaleItem _item) {\n");
    code.append("    java.util.Set<org.colos.ejs.library.utils.HtmlPageInfo> pages = __htmlPagesMap.get(_pageName);\n");
    code.append("    if (pages==null) return null;\n");
    code.append("    org.colos.ejs.library.utils.HtmlPageInfo defaultInfo=null;\n");
    code.append("    for (org.colos.ejs.library.utils.HtmlPageInfo info : pages) {\n");
    code.append("      if (info.getLocaleItem().isDefaultItem()) defaultInfo = info;\n");
    code.append("      if (info.getLocaleItem().equals(_item)) return info;\n");
    code.append("    }\n");
    code.append("    return defaultInfo;\n");
    code.append("  }\n\n");

    code.append("  public org.colos.ejs.library.utils.HtmlPageInfo _getHtmlPageInfo(String _pageName, org.colos.ejs.library.utils.LocaleItem _item) { return _getHtmlPageClassInfo(_pageName,_item); }\n\n");

    code.append("  // -------------------------- \n");
    code.append("  // static methods \n");
    code.append("  // -------------------------- \n\n");

    if (_ejs.getOptions().includeModel()) {
      if (_sourceXML!=null) {
        if (!_sourceXML.startsWith("/")) _sourceXML = "/"+_sourceXML; // Introduced because of Wolfgang's changes to ResourceLoader (Nov 2009)
        //        if (_sourceXML.indexOf('/')<0) _sourceXML = "./"+_sourceXML; // This is now unnecesary
        code.append("  static public String _getEjsModel() { return \""+_sourceXML+"\"; }\n\n");
      }
    }
    code.append("  static public String _getModelDirectory() { return \""+_parentPath+"\"; }\n\n");
    
    Dimension dim = _ejs.getViewEditor().getTree().getMainWindowDimension();
    if (dim!=null) {
      code.append("  static public java.awt.Dimension _getEjsAppletDimension() {\n");
      code.append("    return new java.awt.Dimension(" + dim.width + ","+ dim.height + ");\n");
      code.append("  }\n\n");
    }
    if (_resList!=null) { // Include resList even if model is not included (for the sake of Description pages)
      code.append("  static public java.util.Set<String> _getEjsResources() {\n");
      code.append("    java.util.Set<String> list = new java.util.HashSet<String>();\n");
      for (PathAndFile paf : _resList) {
        if (paf.getFile().isDirectory()) {
          String dirPath = paf.getPath();
          if (!dirPath.startsWith("/")) dirPath = "/"+dirPath; // Introduced because of Wolfgang's changes to ResourceLoader (Nov 2009)
          if (!dirPath.endsWith("/")) dirPath += "/";
          for (File file : JarTool.getContents(paf.getFile()))  {
            String relPath = dirPath + FileUtils.getRelativePath(file,paf.getFile(),false);
            code.append("    list.add(\"" + relPath + "\");\n");
          }
        }
        else {
          String path = paf.getPath();
          if (!path.startsWith("/")) path = "/"+path; // Introduced because of Wolfgang's changes to ResourceLoader (Nov 2009)
          code.append("    list.add(\"" + path + "\");\n");
        }
      }
      code.append("    return list;\n");
      code.append("  };\n\n");
    }

    String libraryConfigPath = FileUtils.getAbsolutePath(OsejsCommon.CONFIG_DIR_PATH, _ejs.getBinDirectory());

    code.append("  static public boolean _common_initialization(String[] _args) {\n");
    code.append("    String lookAndFeel = null;\n"); //+_ejs.getLookAndFeel()+"\";\n");
    code.append("    boolean decorated = true;\n"); //+OSPRuntime.isDefaultLookAndFeelDecorated()+";\n");
    code.append("    if (_args!=null) for (int i=0; i<_args.length; i++) {\n");
    code.append("      if      (_args[i].equals(\"-_lookAndFeel\"))          lookAndFeel = _args[++i];\n");
    code.append("      else if (_args[i].equals(\"-_decorateWindows\"))      decorated = true;\n");
    code.append("      else if (_args[i].equals(\"-_doNotDecorateWindows\")) decorated = false;\n");
    //if (_serverMode) 
    {
      code.append("      else if (_args[i].equals(\"-_noSwingView\")) _sSwingView = false;\n");
      code.append("      else if (_args[i].equals(\"-_serverPort\")) try { _sServerPort = Integer.parseInt(_args[++i]); } catch (Exception exc) { _sServerPort = -1; exc.printStackTrace(); } \n");
    }
    code.append("    }\n");
    code.append("    if (lookAndFeel!=null) org.opensourcephysics.display.OSPRuntime.setLookAndFeel(decorated,lookAndFeel);\n");
    String searchPath = _parentPath.length()>0 ? _parentPath : ".";
    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+searchPath+"\"); // This is for relative resources\n");
    //    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+_outputDirectoryPath+"\"); // This is for absolute resources in Launcher packages\n");
    //    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+_outputDirectoryPath+"/"+searchPath+"\"); // This is for relative resources in Launcher packages\n");

    code.append("    boolean pathsSet = false, underEjs = false;\n");
    code.append("    try { // in case of security problems\n");
    code.append("      if (\"true\".equals(System.getProperty(\"org.osp.launcher\"))) { // Running under Launcher\n");
    code.append("        org.opensourcephysics.display.OSPRuntime.setLauncherMode(true);\n");
    code.append("      }\n");
    code.append("    }\n");
    code.append("    catch (Exception _exception) { } // do not complain\n");
    code.append("    try { // in case of security problems\n");
    //code.append("      System.setProperty(\"osp_defaultLookAndFeel\",\""+OSPRuntime.isDefaultLookAndFeelDecorated()+"\");\n");
    //code.append("      System.setProperty(\"osp_lookAndFeel\",\""+_ejs.getLookAndFeel()+"\");\n");
    code.append("      if (System.getProperty(\"osp_ejs\")!=null) { // Running under EJS\n"); // TODO Can I remove this check
    code.append("        underEjs = true;\n");
    code.append("        org.colos.ejs.library.Simulation.setPathToLibrary(\""+libraryConfigPath+"\"); // This is for classes (such as EjsMatlab) which needs to know where the library is\n");
    code.append("        pathsSet = true;\n");
    code.append("      }\n");
    code.append("    }\n");
    code.append("    catch (Exception _exception) { pathsSet = false; } // maybe an unsigned Web start?\n");
    code.append("    try { org.colos.ejs.library.control.EjsControl.setDefaultScreen(Integer.parseInt(System.getProperty(\"screen\"))); } // set default screen \n");
    code.append("    catch (Exception _exception) { } // Ignore any error here\n");
    code.append("    if (!pathsSet) {\n");
    code.append("      org.colos.ejs.library.Simulation.setPathToLibrary(\""+libraryConfigPath+"\"); // This is for classes (such as EjsMatlab) which needs to know where the library is\n");
    code.append("    }\n");
    addHtmlPagesMapCode (_ejs,_classname,code);
    code.append("    if (!underEjs) {\n");
    addCheckPasswordCode(_ejs, _classname, "false", code);
    //      code.append("      if (org.colos.ejs.library.utils.PasswordDialog.checkPassword(\""+codedPassword+"\",null)==null) return false;\n");
    code.append("    }\n");

    code.append("    return true; // Everything went ok\n");
    code.append("  }\n\n");

    code.append("  static public void main (String[] _args) {\n");
    //    code.append("    if (org.opensourcephysics.display.OSPRuntime.isMac()) {\n");
    //    code.append("      System.setProperty(\"apple.laf.useScreenMenuBar\", \"true\");\n");
    //    code.append("    }\n\n");
    code.append("    if (!_common_initialization(_args)) {\n");
    code.append("      if (org.opensourcephysics.display.OSPRuntime.isLauncherMode()) return;\n");
    code.append("      System.exit(-1);\n");
    code.append("    }\n\n");
    code.append("    "+_classname+ " __theModel = new " + _classname + " (_args);\n");
    if (_ejs.getViewEditor().isEmpty()) {
      code.append("      while (true) { // Start a silly thread so that it won't exit\n");
      code.append("        try { Thread.sleep(1000); } catch (Exception exc) { exc.printStackTrace(); }\n");
      code.append("      }\n");
    }
    code.append("  }\n\n");
    
    code.append("  static public javax.swing.JComponent getModelPane(String[] _args, javax.swing.JFrame _parentFrame) {\n");
    code.append("    if (!_common_initialization(_args)) return null;\n");
    code.append("    "+_classname+ " __theModel = new " +_classname+" ("+_mainFrame+",_parentFrame,null,null,_args,true);\n");
    code.append("    return (javax.swing.JComponent) __theModel._getView().getComponent("+_mainFrame+");\n");
    code.append("  }\n\n");

//Adding some Metadata methods to interact with Clients 
    if (_serverMode) {
      //System.out.println("All vars: " + varTableEditor.generateCode(Editor.GENERATE_PLAINTEXT_VARIABLES,""));
      //System.out.println("All meths: " + _ejs.getModelEditor().getLibraryEditor().generateCode(Editor.GENERATE_PLAIN_TEXT_ACTIONS,""));
      
      code.append("static public JsonArray getVarMetadata(String type){\n"
        + "\tStringBuffer publicVarInfo = new StringBuffer(); \n"
        + "\tStringBuffer inVarInfo = new StringBuffer(); \n"
        + "\tStringBuffer outVarInfo = new StringBuffer(); \n"
        + "\tpublicVarInfo.append( \"" + varTableEditor.generateCode(Editor.GENERATE_JSON_VARIABLES_PUBLIC,"").toString().replace("\"", "\\\"") + " \"); \n" 
        + "\tinVarInfo.append( \"" + varTableEditor.generateCode(Editor.GENERATE_JSON_VARIABLES_IN,"").toString().replace("\"", "\\\"") + " \"); \n" 
        + "\toutVarInfo.append( \"" + varTableEditor.generateCode(Editor.GENERATE_JSON_VARIABLES_OUT,"").toString().replace("\"", "\\\"") + " \"); \n" 
        + "JsonReader jsonReader = Json.createReader(new StringReader(\"\"));\n");
      code.append("  if (type.equals(\"public\"))\t"
          + "jsonReader = Json.createReader(new StringReader(publicVarInfo.toString())); \n");
      code.append("  else if (type.equals(\"in\"))\t"
          + "jsonReader = Json.createReader(new StringReader(inVarInfo.toString())); \n");
      code.append("  else if (type.equals(\"out\"))\t"
          + "jsonReader = Json.createReader(new StringReader(outVarInfo.toString())); \n");
      code.append("  JsonArray jsonVars = jsonReader.readArray();\n");
      code.append("  jsonReader.close();\n");
      
      code.append("  return jsonVars; \n");
      code.append("  }\n\n");
      
      code.append("static public JsonObject getMetadata(){\n"
          + "\tStringBuffer varInfo = new StringBuffer(); \n"
          + "\tvarInfo.append(\"" + generateMetadataStructure(_ejs).replace("\"", "\\\"") + " \"); \n");
      code.append("  JsonReader jsonReader = Json.createReader(new StringReader(varInfo.toString())); \n");
      code.append("  JsonObject jsonMeth = jsonReader.readObject();\n");
      code.append("  jsonReader.close();\n");
      code.append("  return jsonMeth; \n");
      code.append("  }\n\n");
          
      code.append("static public JsonArray getMethMetadata(){\n"
          + "  StringBuffer varInfo = new StringBuffer(); \n"
          + "  varInfo.append(\"" + _ejs.getModelEditor().getLibraryEditor().generateCode(Editor.GENERATE_JSON_ACTIONS,"").toString().replace("\n", "").replace("\"", "\\\"") + "\"); \n");
      code.append("   JsonReader jsonReader = Json.createReader(new StringReader(varInfo.toString()));\n");
      code.append("   JsonArray jsonMeths = jsonReader.readArray();\n");
      code.append("   return jsonMeths;\n");
        code.append("  }\n\n");
    }
    code.append("  public " + _classname + " () { this (null, null, null,null,null,false); } // slave application\n\n");
    code.append("  public " + _classname + " (String[] _args) { this (null, null, null,null,_args,true); }\n\n");
    code.append("  public " + _classname + " (String _replaceName, java.awt.Frame _replaceOwnerFrame, java.net.URL _codebase,"
        +  " org.colos.ejs.library.LauncherApplet _anApplet, String[] _args, boolean _allowAutoplay) {\n");
    code.append("    org.colos.ejs.library.control.swing.ControlWindow.setKeepHidden(true);\n");
    code.append("    __theArguments = _args;\n");
    code.append("    __theApplet = _anApplet;\n");
    code.append("    java.text.NumberFormat _Ejs_format = java.text.NumberFormat.getInstance();\n");
    code.append("    if (_Ejs_format instanceof java.text.DecimalFormat) {\n");
    code.append("      ((java.text.DecimalFormat) _Ejs_format).getDecimalFormatSymbols().setDecimalSeparator('.');\n");
    code.append("    }\n");
    code.append("    _simulation = new " + _classname + "Simulation (this,_replaceName,_replaceOwnerFrame,_codebase,_allowAutoplay);\n");
    //    code.append("    _view = (" + _classname + "View) _simulation.getView();\n");
    //    if (_serverVersion) code.append("    _htmlView = (" + _classname + "ServerView) _simulation.getServerView();\n");
    code.append("    _simulation.processArguments(_args);\n");
    if (!_ejs.getViewEditor().isEmpty()) { 
      //if (_serverMode)
      code.append("    if (_sSwingView) "); 
      code.append("      org.colos.ejs.library.control.swing.ControlWindow.setKeepHidden(false);\n");
    }
    code.append("  }\n\n");

    code.append(" // -------------------------------------------\n");
    code.append(" // Abstract part of Model \n");
    code.append(" // -------------------------------------------\n\n");

    if (_ejs.getOptions().includeModel()) {
      if (_sourceXML!=null) code.append("  public String _getClassEjsModel() { return _getEjsModel(); }\n\n");
    }
    // Include this even if model is not (for the sake of Description pages)
    if (_resList!=null)   code.append("  public java.util.Set<String> _getClassEjsResources() { return _getEjsResources(); }\n\n");
    code.append("  public String _getClassModelDirectory() { return _getModelDirectory(); }\n\n");

    code.append("  public org.colos.ejs.library.View _getView() { return _view; }\n\n");
    if (_serverMode) code.append("  public org.colos.ejs.library.View _getHtmlView() { return _htmlView; }\n\n");
    code.append("  public org.colos.ejs.library.Simulation _getSimulation() { return _simulation; }\n\n");

    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
      code.append("  public org.colos.ejs.library.collaborative.SimulationCollaborative getSimulationCollaborative() { return _simulation; }\n\n");
    //CJB for collaborative

    code.append("  public int _getPreferredStepsPerDisplay() { return "+_ejs.getModelEditor().getStepsPerDisplay()+"; }\n\n");

    code.append("  public void _resetModel () {\n");
    code.append(     initializationEditor.generateCode(Editor.GENERATE_RESET_ENABLED_CONDITION,""));
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_RESET_ENABLED_CONDITION,""));
    code.append(     constraintsEditor.generateCode(Editor.GENERATE_RESET_ENABLED_CONDITION,""));

    code.append(    _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_CODE,""));
    code.append(    _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_CODE,""));
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_SOURCECODE,""));
    //    code.append("    System.gc(); // Free memory from unused old arrays\n"
    code.append("  }\n\n");
    
    if (evolutionEditor.hasODEpages()) {
      code.append("  public void _initializeSolvers () { for (org.opensourcephysics.numerics.ode_solvers.EjsS_ODE __pode : _privateOdesList.values()) __pode.initializeSolver(); }\n\n");
    }
    else code.append("  public void _initializeSolvers () { } // Do nothing \n\n");

    code.append("  public void _initializeModel () {\n");
    code.append("    __shouldBreak = false;\n");
    code.append(     initializationEditor.generateCode(Editor.GENERATE_ENABLED_MEMORY,""));
    //    code.append("    \n");
    //    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_INITIALIZATION_DECLARATION,""));
    code.append(     initializationEditor.generateCode(Editor.GENERATE_DECLARATION,""));
    code.append("    _initializeSolvers();\n");
    //    if (evolutionEditor.hasODEpages()) code.append("    for (org.opensourcephysics.numerics.EJSODE __pode : _privateOdesList.values()) __pode.initializeSolver();\n"); 
    //    code.append("    _automaticResetSolvers();\n"); // 051112
    code.append("  }\n\n");
    code.append("  public void _automaticResetSolvers() { \n");
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_AUTOMATIC_RESET_SOLVER,""));
    //    code.append("    _external.resetIC();\n");
    code.append("  }\n");
    code.append("  public void _resetSolvers() { \n");
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_RESET_SOLVER,""));
    //    code.append("    _external.resetIC();\n");
    code.append("  }\n");
    code.append("  public void _stepModel () {\n");
    code.append("    __shouldBreak = false;\n");
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_ENABLED_MEMORY,""));
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_DECLARATION,""));
    code.append("  }\n\n");
    code.append("  public void _updateModel () {\n");
    //    code.append("    if (__memoryFred) return; // ignore if memory was already fred \n");
    code.append("    __shouldBreak = false;\n");
    code.append(     constraintsEditor.generateCode(Editor.GENERATE_ENABLED_MEMORY,""));
    code.append(    constraintsEditor.generateCode(Editor.GENERATE_DECLARATION,""));
    //    if (_ejs.getOptions().experimentsEnabled() && _ejs.getExperimentEditor().getActivePageCount()>0) {
    //      code.append("    java.util.List<_ScheduledConditionClass> _toExecute = new java.util.ArrayList<_ScheduledConditionClass>();\n");
    //      code.append("    for (java.util.Iterator<?> it=_scheduledConditionsList.iterator(); it.hasNext();) {\n");
    //      code.append("      _ScheduledConditionClass _scheduledCondition = (_ScheduledConditionClass) it.next();\n");
    //      code.append("      if (_scheduledCondition.condition()) _toExecute.add(_scheduledCondition);\n");
    //      code.append("    }\n");
    //      code.append("    for (java.util.Iterator<_ScheduledConditionClass> it=_toExecute.iterator(); it.hasNext();) {\n");
    //      code.append("      _ScheduledConditionClass _scheduledCondition = it.next();\n");
    //      code.append("      _scheduledConditionsList.remove(_scheduledCondition);\n");
    //      code.append("      _scheduledCondition.action();\n");
    //      code.append("    }\n");
    //    }
    code.append("  }\n\n");

    code.append("  public void _readFromViewAfterUpdate () {\n");
    code.append(    _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_VIEW_READ,""));
    code.append("  }\n\n");

    code.append("  public void _freeMemory () {\n");
    code.append("    getSimulation().setEnded(); // Signal that the simulation ended already\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_DESTRUCTION,""));
    code.append(    _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_DESTRUCTION,""));
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_DESTRUCTION,""));
    code.append("    System.gc(); // Free memory from unused old arrays\n");
    code.append("  }\n\n");

    if (!_ejs.getModelEditor().checkRTV()) {
      code.append("  protected double _getRealTime() {\n");
      code.append( "    return "+_ejs.getModelEditor().getRealtimeVariable()+";\n");
      code.append("  }\n\n");
    }

    if (evolutionEditor.hasODEpages()){
      code.append(" // -------------------------------------------\n");
      code.append(" // ODEs declaration \n");
      code.append(" // -------------------------------------------\n\n");
      code.append("  protected java.util.Hashtable<String,org.opensourcephysics.numerics.ode_solvers.EjsS_ODE> _privateOdesList = new java.util.Hashtable<String,org.opensourcephysics.numerics.ode_solvers.EjsS_ODE>();\n\n");
      code.append("  public org.opensourcephysics.numerics.ode_solvers.EjsS_ODE _getODE(String _odeName) {\n");
      code.append("    try { return _privateOdesList.get(_odeName); }\n");
      code.append("    catch (Exception __exc) { return null; }\n");
      code.append("  }\n\n");
      code.append("  public org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver _getEventSolver(String _odeName) {\n");
      code.append("    try { return _privateOdesList.get(_odeName).getEventSolver(); }\n");
      code.append("    catch (Exception __exc) { return null; }\n");
      code.append("  }\n\n");
      code.append("  public void _setSolverClass (String _odeName, Class<?> _solverClass) { // Change the solver in run-time\n");
      code.append("    try { _privateOdesList.get(_odeName).setSolverClass(_solverClass); }\n");
      code.append("    catch (Exception __exc) { System.err.println (\"There is no ODE with this name \"+_odeName); }\n");
      code.append("  }\n\n");
      code.append("  public String _setSolverClass (String _odeName, String _solverClassName) { // Change the solver in run-time\n");
      code.append("    if (_solverClassName==null) { System.err.println (\"Null solver class name!\"); return null; }\n");
      code.append("    try { return _privateOdesList.get(_odeName).setSolverClass(_solverClassName); }\n");
      code.append("    catch (Exception __exc) { System.err.println (\"There is no ODE with this name \"+_odeName); return null; }\n");
      code.append("  }\n\n");
    }

    //    code.append(" // -------------------------------------------\n");
    //    code.append(" // Implementation of ExternalClient \n");
    //    code.append(" // -------------------------------------------\n\n");
    //    
    //    code.append("  public String _externalInitCommand(String _applicationFile) { return null;}\n");
    //    code.append("  public void _externalSetValues(boolean _any, org.colos.ejs.library.external.ExternalApp _application) {} \n");
    //    code.append("  public void _externalGetValues(boolean _any, org.colos.ejs.library.external.ExternalApp _application) {} \n");

    /*
    code.append("  public String _externalInitCommand(String _applicationFile) { \n");
    code.append("    StringBuffer _external_initCommand=new StringBuffer();\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_INITIALIZATION,""));
    code.append("    return _external_initCommand.toString();\n");
    code.append("  }\n\n");

    code.append("  public void _externalSetValues(boolean _any, org.colos.ejs.library.external.ExternalApp _application) { \n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_IN,"")); // Includes conversion
    code.append("  }\n\n");
    code.append("  public void _externalGetValues(boolean _any, org.colos.ejs.library.external.ExternalApp _application) { \n");
    code.append(    _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_OUT,""));
    code.append("  }\n\n");

     */

    code.append(" // -------------------------------------------\n");
    code.append(" // Variables defined by the user\n");
    code.append(" // -------------------------------------------\n\n");

    //    code.append("  private org.colos.ejs.library.external.ExternalAppsHandler _external"); //Gonzalo 090610  
    //    code.append(" = new org.colos.ejs.library.external.ExternalAppsHandler(this); // List of possible external applications\n\n");

    code.append(  _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_DECLARATION,""));
    code.append(  _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_DECLARATION,""));

    code.append("\n");

    code.append(" // -------------------------------------------\n");
    code.append(" // Enabled condition of pages \n");
    code.append(" // -------------------------------------------\n\n");

    code.append( initializationEditor.generateCode(Editor.GENERATE_ENABLED_CONDITION,""));
    code.append( evolutionEditor.generateCode(Editor.GENERATE_ENABLED_CONDITION,""));
    code.append( constraintsEditor.generateCode(Editor.GENERATE_ENABLED_CONDITION,""));
    code.append("\n");

    code.append("  public void _setPageEnabled(String _pageName, boolean _enabled) { // Sets the enabled state of a page\n");
    code.append("    boolean _pageFound = false;\n");
    code.append( initializationEditor.generateCode(Editor.GENERATE_CHANGE_ENABLED_CONDITION,""));
    code.append( evolutionEditor.generateCode(Editor.GENERATE_CHANGE_ENABLED_CONDITION,""));
    code.append( constraintsEditor.generateCode(Editor.GENERATE_CHANGE_ENABLED_CONDITION,""));
    code.append("    if (!_pageFound) System.out.println (\"_setPageEnabled() warning. Page not found: \"+_pageName);\n");
    code.append("  }\n\n");

    code.append(" // -------------------------------------------\n");
    code.append(" // Methods defined by the user \n");
    code.append(" // -------------------------------------------\n\n");

    code.append(" // --- Initialization\n\n");    
    //    code.append( _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_INITIALIZATION,"")); //Gonzalo 090610

    code.append(  initializationEditor.generateCode(Editor.GENERATE_CODE,""));

    code.append(" // --- Evolution\n\n");
    code.append(  evolutionEditor.generateCode(Editor.GENERATE_CODE,""));
    code.append(" // --- Constraints\n\n");
    code.append(  constraintsEditor.generateCode(Editor.GENERATE_CODE,""));
    code.append(" // --- Custom\n\n");
    code.append(  _ejs.getModelEditor().getLibraryEditor().generateCode(Editor.GENERATE_CODE,""));
    code.append(" // --- Methods for view elements\n\n");
    code.append(  _ejs.getViewEditor().generateCode(Editor.GENERATE_VIEW_EXPRESSIONS,""));
    if (_serverMode) {
      OneView oneViewPage = ((OneView) _ejs.getHtmlViewEditor().getCurrentPage());
      //      LocaleItem locale = oneViewPage.getLocale();
      //      String localeString = locale.isDefaultItem() ? SimulationXML.sDEFAULT_LOCALE : locale.getKeyword();
      code.append(" // --- Methods for html view actions\n\n");
      code.append(  oneViewPage.generateCode(Editor.GENERATE_VIEW_LISTENERS,""));
      code.append(" // --- Methods for html view expressions\n\n");
      code.append(" public void _addToHTMLOutputData(Map<String, Object> dataMap) {\n");
      code.append("   boolean _isPlaying = _isPlaying();\n");
      code.append("   boolean _isPaused = _isPaused();\n");
      code.append("   dataMap.put(\"_isPlaying\", _isPlaying);\n");
      code.append("   dataMap.put(\"_isPaused\",  _isPaused);\n");
      code.append(  oneViewPage.generateCode(Editor.GENERATE_VIEW_EXPRESSIONS,""));
      code.append(" }\n\n");
    }

    //    if (_ejs.getOptions().experimentsEnabled()) {
    //      code.append("\n  // -----------------------------\n");
    //      code.append("  //     Code for Experiments     \n");
    //      code.append("  // -----------------------------\n\n");
    //      code.append(_ejs.getExperimentEditor().generateCode(Editor.GENERATE_CODE,""));
    //
    //      code.append("  public java.util.List<org.colos.ejs.library.Experiment> _getExperiments () { // Creates a list of experiments\n");
    //      code.append("    java.util.ArrayList<org.colos.ejs.library.Experiment> actions = new java.util.ArrayList<org.colos.ejs.library.Experiment>();\n");
    //      code.append(_ejs.getExperimentEditor().generateCode(Editor.GENERATE_LIST_ACTIONS,_classname));
    //      code.append("    return actions;\n");
    //      code.append("  }\n\n");
    //      code.append("  public org.colos.ejs.library.Experiment _createExperiment (String _experimentName) { // gets an experiment by name\n");
    //      code.append(_ejs.getExperimentEditor().generateCode(Editor.GENERATE_LIST_VARIABLES,_classname));
    //      code.append("    return null;");
    //      code.append("  }\n\n");
    //      code.append("  // ------------------------------------\n");
    //      code.append("  //     End of Code for Experiments     \n");
    //      code.append("  // ------------------------------------\n\n");
    //    }

    code.append("} // End of class " + _classname + "Model\n\n");
    return code.toString();
  }

  /**
   * Adds the map of html pages for different locales
   */
  static private void addHtmlPagesMapCode(Osejs _ejs, String _classname, StringBuffer _buffer) {
    String prefix = "    "+_classname+"._addHtmlPageInfo";

    java.util.List<LocaleItem> localesDesired = getLocalesDesired(_ejs);
    int counter = 0;
    for (java.util.Enumeration<Editor> e = _ejs.getDescriptionEditor().getPages().elements(); e.hasMoreElements(); ) {
      HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
      if (htmlEditor.isInternal()) continue;
      counter++;
      for (LocaleItem item : localesDesired) {
        OneHtmlPage htmlPage = htmlEditor.getHtmlPage(item);
        if (htmlPage==null || htmlPage.isEmpty()) continue;
        String link;
        if (htmlPage.isExternal()) {
          link = htmlPage.getLink();
          if (!link.startsWith("/")) link = "/"+link;
        }
        else  link = "./"+_classname+ getIntroductionPageKey(counter,item) + ".html"; //(JSObfuscator.isGenerateXHTML() ? ".xhtml" : ".html");
        java.awt.Dimension size = htmlEditor.getComponent().getSize();
        if (size.width<=0 || size.height<=0) size = _ejs.getOptions().getHtmlPanelSize(); // This happens in batch compilation
        _buffer.append(prefix+"(\""+htmlEditor.getName()+"\",\""+item.getKeyword()+"\",\""+htmlPage.getTitle()+"\",\""+link+"\");\n");
      } // end of for
    }
  }

  /**
   * Generates the Java code for the simulation part itself
   * @param _ejs Osejs The calling Ejs
   * @param _filename String The name of the simulation file
   * @return StringBuffer
   */
  static private String generateSimulation (Osejs _ejs, String _classname, String _packageName, String _xmlName, String _mainFrame, boolean _serverMode) {
    StringBuffer code = new StringBuffer ();
    code.append(generateHeader(_ejs,_classname,_packageName,"Simulation", true));
    //JSV Changes for Java-Javascript
    if (_serverMode) {
      code.append("import org.colos.ejs.library.Model;\n\n");
      code.append("import java.util.Vector;\n\n");
      code.append("import java.util.List;\n\n");
      code.append("import java.lang.reflect.Method;\n\n");
      code.append("import java.lang.reflect.InvocationTargetException;\n\n");
    
      code.append("import org.colos.ejs.library.View;\n\n");
      code.append("import org.colos.ejs.library.server.SocketViewElement;\n\n");
    }
    else if (_ejs.getViewEditor().isEmpty()) {
      code.append("import org.colos.ejs.library.View;\n\n");
    }
    //JSV End
    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
      code.append("class " + _classname + "Simulation extends org.colos.ejs.library.collaborative.SimulationCollaborative { \n\n");
    else
      code.append("class " + _classname + "Simulation extends org.colos.ejs.library.Simulation { \n\n");
    //CJB for collaborative
    //JSV Line changed
    //if (_serverMode) code.append("  private "+_classname+"SimulationView mMainView;\n\n");
    if (_serverMode || _ejs.getViewEditor().isEmpty()) code.append("  private View mMainView;\n\n");
    // Error de JSV??? else      code.append("class " + _classname + "Simulation extends org.colos.ejs.library.Simulation { \n\n");
    else code.append("  private "+_classname+"View mMainView;\n\n");
    //if (_serverMode) code.append("  private "+_classname+"SimulationView mMainView;\n\n");
    //else             code.append("  private "+_classname+"View mMainView;\n\n");
    
    //end JSV


    code.append("  public " + _classname + "Simulation (" + _classname + " _model, String _replaceName, java.awt.Frame _replaceOwnerFrame, java.net.URL _codebase, boolean _allowAutoplay) {\n");

    if (_ejs.getSimInfoEditor().addCaptureTools()) code.append("    videoUtil = new org.colos.ejs.library.utils.VideoUtilClass();\n");
    else code.append("    videoUtil = new org.colos.ejs.library.utils.VideoUtil();\n");
    //    code.append("    org.opensourcephysics.controls.OSPLog.setLevel(java.util.logging.Level.ALL);\n"); // Debug
    //    code.append("    org.opensourcephysics.controls.OSPLog.showLog();\n"); // Debug
    code.append("    try { setUnderEjs(\"true\".equals(System.getProperty(\"osp_ejs\"))); }\n");      
    code.append("    catch (Exception exc) { setUnderEjs(false); } // in case of applet security\n");      
    code.append("    setCodebase (_codebase);\n");
    code.append("    setModel (_model);\n");
    code.append("    _model._simulation = this;\n");
    if (_serverMode) {
      code.append("    _model._htmlView = new "+_classname+"ServerView(this);\n");
      code.append("    if ("+_classname+"._sSwingView) mMainView = _model._view = new "+_classname+"View(this,_replaceName, _replaceOwnerFrame);\n");
      code.append("    else mMainView = _model._view = new "+_classname+"DummyView(this);\n");
      code.append("    setView (new org.colos.ejs.library.MultipleView(_model._view,_model._htmlView));\n");
    }
    else if (_ejs.getViewEditor().isEmpty()) {
      code.append("    mMainView = _model._view = new "+_classname+"DummyView(this);\n");
      code.append("    setView (_model._view);\n");
    }
    else {
      code.append("    mMainView = _model._view = new "+_classname+"View(this,_replaceName, _replaceOwnerFrame);\n");
      code.append("    setView (_model._view);\n");
    }
    code.append("    if (_model._isApplet()) _model._getApplet().captureWindow (_model,"+_mainFrame+");\n");
    code.append(     _ejs.getModelEditor().generateCode(Editor.GENERATE_SIMULATION_STATE,""));
    //    int htmlOption = _ejs.getOptions().generateHtml();
    //    if (htmlOption==org.colos.ejs.osejs.EjsOptions.GENERATE_TOP_FRAME  || htmlOption==org.colos.ejs.osejs.EjsOptions.GENERATE_LEFT_FRAME) 
    //    if (!_serverVersion) 
    if (!_ejs.getViewEditor().isEmpty()) {
      java.awt.Dimension size = _ejs.getDescriptionEditor().getEditorSize();
      if (size.width<=0 || size.height<=0) size = _ejs.getOptions().getHtmlPanelSize(); // This happens in batch compilation
      for (Editor htmlEditor : _ejs.getDescriptionEditor().getPages()) {
        if (!htmlEditor.isInternal()) code.append("    addDescriptionPage(\""+htmlEditor.getName()+"\","+size.width+","+size.height+","+htmlEditor.isActive()+");\n");
      }
      code.append("    recreateDescriptionPanel();\n");
    }
    code.append("    if (_model._getApplet()!=null && _model._getApplet().getParameter(\"locale\")!=null) {\n");
    //    code.append("      System.err.println (\"locale param = \"+_model._getApplet().getParameter(\"locale\"));\n");
    code.append("      setLocaleItem(org.colos.ejs.library.utils.LocaleItem.getLocaleItem(_model._getApplet().getParameter(\"locale\")),false);\n");
    code.append("    }\n");
    code.append("    else setLocaleItem(getLocaleItem(),false); // false so that not to reset the model twice at start-up\n");
    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
      code.append("    setParamDialog (\"http://\",\"50000\");\n");
    //CJB for collaborative
    code.append("  }\n\n");

    if (!_ejs.getModelEditor().checkSPD()) {
      code.append("  public void step() {\n");
      code.append( "    setStepsPerDisplay(model._getPreferredStepsPerDisplay());\n");
      code.append( "    super.step();\n");
      code.append("  }\n\n");
    }

    //    code.append("  public void onExit() {\n");
    //    code.append(    _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_DESTRUCTION,""));
    //    code.append( "    super.onExit();\n");
    //    code.append("  }\n\n");


    /*
    code.append("  public void rebuildView() {\n");
    code.append("    "+_classname+" theModel = ("+_classname+") getModel();\n");
    code.append("    setView (theModel._view = new "+_classname+"View(this,theModel._view.getReplaceOwnerName(), theModel._view.getReplaceOwnerFrame()));\n");
    code.append("  }\n\n");
     */

    code.append("  public java.util.List<String> getWindowsList() {\n");
    code.append( "    java.util.List<String> windowList = new java.util.ArrayList<String>();\n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_WINDOW_LIST,""));
    code.append( "    return windowList;\n");
    code.append("  }\n\n");
    code.append("  public String getMainWindow() {\n");
    code.append( "    return "+_mainFrame+";\n");
    code.append("  }\n\n");
    code.append("  protected void setViewLocale() { // Overwrite its parent's dummy method with real actions \n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_CHANGE_LOCALE,""));
    code.append("    super.setViewLocale();\n");
    code.append("  }\n\n");
    //    if (_ejs.getOptions().experimentsEnabled() && _ejs.getExperimentEditor().getActivePageCount()>0) {
    //      code.append("  public java.util.List<org.colos.ejs.library.Experiment> getExperiments () { // Add the experiments to the menu\n");
    //      code.append("    return (("+_classname+") getModel())._getExperiments();\n");
    //      code.append("  }\n\n");
    //      code.append("  public org.colos.ejs.library.Experiment createExperiment (String _experimentName) { // creates an experiment\n");
    //      code.append("    return (("+_classname+") getModel())._createExperiment(_experimentName);\n");
    //      code.append("  }\n\n");
    //    }
    code.append("  public org.colos.ejs.library.LauncherApplet initMoodle () {\n");
    code.append("    org.colos.ejs.library.LauncherApplet applet = super.initMoodle();\n");
    //LDLTorre for Moodle support
    code.append("    if (applet!=null && applet.getParameter(\"moodle_upload_file\")!=null &&\n" +
        "        applet.getParameter(\"ejsapp_id\")!=null  && applet.getParameter(\"user_id\")!=null &&\n"+
        "        applet.getParameter(\"context_id\")!=null && applet.getParameter(\"language\")!=null &&\n" +
        "        applet.getParameter(\"username\")!=null)\n" +
        "        moodle = new org.colos.ejs.library.MoodleConnection (applet,this);\n");
    //LDLTorre for Moodle support
    code.append("    return applet;\n");
    code.append("  }\n\n");
    if (_serverMode){
      code.append("public "+ _classname +"View"+ " sendViewCommand(String name,SocketViewElement eleHtml, String command, Object... params) {\n");
      code.append("    "+ _classname +"View"+ " view = ("+ _classname +"View) getView();\n");
      code.append("    eleHtml.executeMethodWithObject(command,params);\n");
      code.append("    invokeMethodWithArgument(name, command, params);\n");
      code.append("    return view;\n");
      code.append("}\n\n");
      
      code.append("// Method to invoke java methods using reflection \n");
      code.append("private void invokeMethodWithArgument(String elementName,String methodName, Object... arg1) { \n");
      code.append("    Class<?>[] argList = new Class[] {Object.class};\n");
      code.append("    Object[] parameterList;\n");
      code.append("    Class<?> pClass[] = new Class<?>[arg1.length];\n");
      code.append("    List params2send = new Vector();\n");
      code.append("    for (int i = 0; i<arg1.length;i++){\n");
      code.append("        pClass[i] = arg1[i].getClass(); \n");
      code.append("        if(pClass[i].toString().contains(\"Double\")){     pClass[i] = double.class; params2send.add(Double.valueOf(arg1[i].toString()));\n");
      code.append("        }else if(pClass[i].toString().contains(\"Integer\")){  pClass[i] = int.class; params2send.add(Integer.valueOf(arg1[i].toString()));\n");
      code.append("        }else if(pClass[i].toString().contains(\"Boolean\")){  pClass[i] = boolean.class; params2send.add(Boolean.valueOf(arg1[i].toString()));\n");
      code.append("        }else if(pClass[i].toString().contains(\"Float\")){    pClass[i] = float.class; params2send.add(Float.valueOf(arg1[i].toString()));\n");
      code.append("        }else if(pClass[i].toString().contains(\"Char\")){   pClass[i] = char.class; params2send.add(arg1[i]);\n");
      code.append("        }else if(pClass[i].toString().contains(\"Long\")){   pClass[i] = long.class; params2send.add(Long.valueOf(arg1[i].toString()));\n");
      code.append("        }else if(pClass[i].toString().contains(\"Byte\")){   pClass[i] = byte.class; params2send.add(Byte.valueOf(arg1[i].toString()));\n");
      code.append("        }else {\n");
      code.append("            params2send.add(arg1[i]);//if (arg1[i] instanceof double[]){System.out.println(\"Is an Array: \");}//System.out.println(\"Is an Object: \");\n");
      code.append("        }\n");
      code.append("    }\n");
      code.append("    argList = pClass;\n");
      code.append("    parameterList = params2send.toArray();\n");
      code.append("    Model model = getModel();\n");
      code.append("    Class<?> theClass = model._getView().getElement(elementName).getObject().getClass();\n");
      code.append("    Method method;\n");
      code.append("    try {\n");
      code.append("        method = theClass.getMethod(methodName, argList);\n");
      code.append("        method.invoke(model._getView().getElement(elementName).getObject(), parameterList );\n");
      code.append("    } catch (SecurityException e) {\n");
      code.append("        System.err.println (\"SocketView invokeMethod security exception error : Method : \"+methodName+\" ( \"+arg1+\")\");\n");
      code.append("        e.printStackTrace();\n");
      code.append("    } catch (NoSuchMethodException e) {\n");
      code.append("        System.err.println (\"SocketView invokeMethod error : Method not found: \"+methodName+\" ( \"+arg1+\")\");\n");
      code.append("        e.printStackTrace();\n");
      code.append("    } catch (IllegalArgumentException e) {\n");
      code.append("        System.err.println (\"SocketView invokeMethod error : Illegal argument for method: \"+methodName+\" ( \"+arg1+\")\");\n");
      code.append("        e.printStackTrace();\n");
      code.append("    } catch (IllegalAccessException e) {\n");
      code.append("        System.err.println (\"SocketView invokeMethod error : Illegal access for method: \"+methodName+\" ( \"+arg1+\")\");\n");
      code.append("        e.printStackTrace();\n");
      code.append("    } catch (InvocationTargetException e) {\n");
      code.append("        System.err.println (\"SocketView invokeMethod error : Invocation target error for method: \"+methodName+\" ( \"+arg1+\")\");\n");
      code.append("        e.printStackTrace();\n");
      code.append("    } catch (Exception e) {\n");
      code.append("        e.printStackTrace();\n");
      code.append("    }\n");
      code.append("}\n");
      code.append("\n");
    }
    code.append("} // End of class "+_classname+"Simulation\n\n");

    //if (_serverMode) code.append("interface " + _classname + "SimulationView extends org.colos.ejs.library.View { }\n\n");

    return code.toString();
  }

  /**
   * Generates the Java code for the view of the simulation
   * @param _ejs Osejs The calling Ejs
   * @param _filename String The name of the simulation file
   * @return StringBuffer
   */
  static private String generateSwingView (Osejs _ejs, String _classname, String _packageName, boolean _serverMode) {
    StringBuffer code = new StringBuffer();
    code.append(generateHeader(_ejs,_classname,_packageName, "View", true));
    code.append("import javax.swing.event.*;\n");
    code.append("import javax.swing.*;\n");
    code.append("import java.awt.event.*;\n");
    code.append("import java.awt.*;\n");
    code.append("import java.net.*;\n");
    code.append("import java.util.*;\n");
    code.append("import java.io.*;\n");
    code.append("import java.lang.*;\n\n");
    code.append("import org.colos.ejs.library.View;\n\n");

    code.append("class " + _classname + "View extends org.colos.ejs.library.control.EjsControl ");
    //JSV Line changed
    //if (_serverMode) code.append("implements "+ _classname+ "SimulationView {\n");   
    if (_serverMode) code.append("implements View {\n");
    else             code.append("implements org.colos.ejs.library.View {\n");
    code.append("  private "+_classname+"Simulation _simulation=null;\n");
    code.append("  private "+_classname+" _model=null;\n\n");
    code.append("  // Public variables for wrapped view elements:\n");
    code.append(_ejs.getViewEditor().generateCode(Editor.GENERATE_DECLARATION,""));
    code.append("\n  // private variables to block changes in the view variables:\n");
    code.append(_ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_UPDATE_BOOLEANS,""));
    code.append("\n");

    code.append("// ---------- Class constructor -------------------\n\n");
    code.append("  public "+_classname+"View ("+_classname+"Simulation _sim, String _replaceName, java.awt.Frame _replaceOwnerFrame) {\n");
    code.append("    super(_sim,_replaceName,_replaceOwnerFrame);\n");
    code.append("    _simulation = _sim;\n");
    code.append("    _model = ("+_classname+") _sim.getModel();\n");
    code.append("    _model._view = this;\n");
    code.append("    addTarget(\"_simulation\",_simulation);\n");
    code.append("    addTarget(\"_model\",_model);\n");
    code.append("    _model._resetModel();\n");
    code.append("    initialize();\n");
    code.append("    setUpdateSimulation(false);\n");
    code.append("    // The following is used by the JNLP file for the simulation to help find resources\n");
    code.append("    try { setUserCodebase(new java.net.URL(System.getProperty(\"jnlp.codebase\"))); }\n");
    code.append("    catch (Exception exc) { } // Do nothing and keep quiet if it fails\n");
    code.append("    update();\n");
    code.append("    if (javax.swing.SwingUtilities.isEventDispatchThread()) createControl();\n");
    code.append("    else try {\n");
    code.append("      javax.swing.SwingUtilities.invokeAndWait(new Runnable() {\n");
    code.append("        public void run () { \n");
    code.append("          createControl();\n");
    code.append("        }\n");
    code.append("      });\n");
    code.append("    } catch (java.lang.reflect.InvocationTargetException it_exc) { it_exc.printStackTrace(); \n");
    code.append("    } catch (InterruptedException i_exc) { i_exc.printStackTrace(); };\n");
    //    code.append("    // The following is used by the JNLP file for the simulation to help find resources\n");
    //    code.append("    try { setUserCodebase(new java.net.URL(System.getProperty(\"jnlp.codebase\"))); }\n");
    //    code.append("    catch (Exception exc) { } // Do nothing and keep quiet if it fails\n");
    code.append("    addElementsMenuEntries();\n");
    //    code.append("    _model._resetModel();\n");
    //    code.append("    initialize();\n");
    //    code.append("    _model._initializeSolvers();\n"); 
    code.append("    update();\n");
    code.append("    setUpdateSimulation(true);\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_LISTENERS,""));
    //    code.append("    setUpdateSimulation(true);\n");
    code.append("  }\n\n");
    code.append("// ---------- Implementation of View -------------------\n\n");
    code.append("  public void read() {\n");
    code.append("    // Ejs requires no read(). Actually, having it might cause problems!\n");
    code.append("  }\n\n");
    code.append("  @SuppressWarnings(\"unchecked\")\n");
    code.append("  public void read(String _variable) {\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_READ_ONE,""));
    code.append("  }\n\n");
    code.append("  public void propagateValues () {\n"); // Do NOT synchronize!!!
    code.append("    setValue (\"_isPlaying\",_simulation.isPlaying());\n");
    code.append("    setValue (\"_isPaused\", _simulation.isPaused());\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_UPDATE,""));
    //    code.append("    super.update();\n");
    code.append("  }\n\n");
    code.append("  @SuppressWarnings(\"unchecked\")\n");
    code.append("  public void blockVariable(String _variable) {\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_BLOCK_VARIABLES,""));
    code.append("  }\n\n");
    code.append("// ---------- Creation of the interface  -------------------\n\n");
    code.append("  private void createControl() {\n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_CODE,""));
    code.append("  }\n\n");
    code.append("// ---------- Resetting the interface  -------------------\n\n");
    code.append("  public void reset() {\n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_VIEW_RESET,""));
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_RESET,""));
    code.append("    super.reset();\n");
    code.append("  }\n\n");
    code.append("} // End of class " + _classname + "View\n\n");
    return code.toString();
  }

  /**
   * Generates the Java code for the view of the simulation
   * @param _ejs Osejs The calling Ejs
   * @param _filename String The name of the simulation file
   * @return StringBuffer
   */
  static private String generateServerView (Osejs _ejs, String _classname, String _packageName) {
    StringBuffer code = new StringBuffer();
    code.append(generateHeader(_ejs,_classname,_packageName, "View", true));
    code.append("import java.net.*;\n");
    code.append("import java.util.*;\n");
    code.append("import java.io.*;\n");
    code.append("import java.lang.*;\n\n");
    code.append("import javax.json.*;\n\n");
    code.append("import org.colos.ejs.library.View;\n\n");
    //    code.append("import org.colos.ejs.library.server.DummyViewElement;\n\n");

    code.append("class " + _classname + "ServerView extends org.colos.ejs.library.server.SocketView {\n");
    code.append("  private "+_classname+"Simulation _simulation=null;\n");
    code.append("  private "+_classname+" _model=null;\n\n");
    code.append("  // Public variables for wrapped view elements:\n");
    code.append(_ejs.getViewEditor().generateCode(Editor.GENERATE_SERVER_DECLARATION,""));
    code.append("\n  // private variables to block changes in the view variables:\n");
    code.append(_ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_UPDATE_BOOLEANS,""));
    code.append("\n");

    code.append("// ---------- Class constructor -------------------\n\n");
    code.append("  public "+_classname+"ServerView ("+_classname+"Simulation _sim) {\n");
    code.append("    super("+_classname+"._sServerPort,_sim);\n");
    code.append("    _simulation = _sim;\n");
    code.append("    _model = ("+_classname+") _sim.getModel();\n");
    code.append("    _model._htmlView = this;\n");
    //    code.append("    addTarget(\"_simulation\",_simulation);\n");
    //    code.append("    addTarget(\"_model\",_model);\n");
    code.append("    _model._resetModel();\n");
    code.append("    initialize();\n");
    code.append("    setUpdateSimulation(false);\n");
    code.append("    update();\n");
    code.append("    createControl();\n");
    code.append("    update();\n");
    code.append("    setUpdateSimulation(true);\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_LISTENERS,""));
    //    code.append("    setUpdateSimulation(true);\n");
    code.append("  }\n\n");
    code.append("// ---------- Implementation of View -------------------\n\n");
    code.append("  public void read() {\n");
    code.append("    // Ejs requires no read(). Actually, having it might cause problems!\n");
    code.append("  }\n\n");
    code.append("  @SuppressWarnings(\"unchecked\")\n");
    code.append("  public void read(String _variable) {\n");
    //    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_READ_ONE,""));
    code.append("  }\n\n");
    //    code.append("  public void propagateValues () {\n"); // Do NOT synchronize!!!
    //    code.append("    setValue (\"_isPlaying\",_simulation.isPlaying());\n");
    //    code.append("    setValue (\"_isPaused\", _simulation.isPaused());\n");
    //    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_UPDATE,""));
    ////    code.append("    super.update();\n");
    //    code.append("  }\n\n");
    code.append("  @SuppressWarnings(\"unchecked\")\n");
    code.append("  public void blockVariable(String _variable) {\n");
    //    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_BLOCK_VARIABLES,""));
    code.append("  }\n\n");
    
    code.append("// ---------- Trying Metadata ------------------------------  \n");
    code.append("//JSV_Changes\n");
    code.append("/***\n");
    code.append("* Inspect the declared variables and returns all of them with no _ in their names\n");
    code.append("* @return JsonArray easy to map into json\n");
    code.append(" */\n");
    code.append("public JsonArray getModelVarsList(String type){\n");
    code.append("JsonArray varArray = "+ _classname +".getVarMetadata(type);\n");
    code.append("return varArray;\n");
    code.append("}\n");
    code.append("\n\n");
    code.append("public JsonArray getModelMethList(){\n");
    code.append("JsonArray metArray ="+ _classname +".getMethMetadata();\n");
    code.append("return metArray;\n");
    code.append("}\n");
    code.append("\n\n");
    code.append("public JsonObject getMetadata(){\n");
    code.append("JsonObject metadata ="+ _classname +".getMetadata();\n");
    code.append("return metadata;\n");
    code.append("}\n");
    code.append("\n\n");
    
    code.append("// ---------- Creation of the interface  -------------------\n\n");
    code.append("  private void createControl() {\n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_SERVER_CODE,""));
    code.append("  }\n\n");
    code.append("// ---------- Resetting the interface  -------------------\n\n");
    code.append("  public void reset() {\n");
    //    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_VIEW_RESET,""));
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_RESET,""));
    code.append("    super.reset();\n");
    code.append("  }\n\n");
    code.append("} // End of class " + _classname + "View\n\n");
    //    return code.toString();
    //  }
    //
    //  /**
    //   * Generates the Java code for the view of the simulation
    //   * @param _ejs Osejs The calling Ejs
    //   * @param _filename String The name of the simulation file
    //   * @return StringBuffer
    //   */
    //  static private String generateDummyView (Osejs _ejs, String _classname, String _packageName) {
    //    StringBuffer code = new StringBuffer();
    //    code.append(generateHeader(_ejs,_classname,_packageName, "View", true));
    ////    code.append("import org.colos.ejs.library.server.DummyViewElement;\n\n");
    
    //JSv Line changed
    //code.append("class " + _classname + "DummyView extends org.colos.ejs.library.server.DummyView implements "+ _classname + "SimulationView {\n");
    code.append("class " + _classname + "DummyView extends org.colos.ejs.library.server.DummyView implements View {\n");
    code.append("  private "+_classname+" _model=null;\n\n");
    code.append("  // Public variables for wrapped view elements:\n");
    code.append(_ejs.getViewEditor().generateCode(Editor.GENERATE_SERVER_DUMMY_DECLARATION,""));

    code.append("// ---------- Class constructor -------------------\n\n");
    code.append("  public "+_classname+"DummyView ("+_classname+"Simulation _sim) {\n");
    code.append("    super(_sim);\n");
    code.append("    _model = ("+_classname+") _sim.getModel();\n");
    code.append("    _model._view = this;\n");
    code.append("    createControl();\n");
    code.append("  }\n\n");
    code.append("// ---------- Implementation of View -------------------\n\n");
    code.append("  public void read() { }\n\n");
    code.append("  public void read(String _variable) { }\n\n");
    code.append("  public void blockVariable(String _variable) { }\n\n");
    code.append("// ---------- Creation of the interface  -------------------\n\n");
    code.append("  private void createControl() {\n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_SERVER_DUMMY_CODE,""));
    code.append("  }\n\n");
    code.append("// ---------- Resetting the interface  -------------------\n\n");
    code.append("  public void reset() { }\n\n");
    code.append("} // End of class " + _classname + "View\n\n");
    return code.toString();
  }

  
  /**
   * Generates the Java code for a dummy view for the simulation
   * @param _ejs Osejs The calling Ejs
   * @param _classname String The name of the model class
   * @return String
   */
  static private String generateEmptyView (Osejs _ejs, String _classname, String _packageName) {
    StringBuffer code = new StringBuffer();
    code.append(generateHeader(_ejs,_classname,_packageName, "DummyView", false));
    
    code.append("class " + _classname + "DummyView extends org.colos.ejs.library.server.DummyView {\n");
    code.append("  private "+_classname+" _model=null;\n\n");

    code.append("// ---------- Class constructor -------------------\n\n");
    code.append("  public "+_classname+"DummyView ("+_classname+"Simulation _sim) {\n");
    code.append("    super(_sim);\n");
    code.append("    _model = ("+_classname+") _sim.getModel();\n");
    code.append("    _model._view = this;\n");
    code.append("  }\n\n");
    code.append("} // End of class " + _classname + "View\n\n");
    return code.toString();
  }

  
  static private String generateMetadataStructure(Osejs _ejs){
    StringBuffer code = new StringBuffer();
    MetadataBuilder fullMetadata = new MetadataBuilder("","","");
    String binDirPath = FileUtils.getPath(_ejs.getBinDirectory());
    JsonReader jsonMaker;
    try {
      InputStream fis = new FileInputStream(binDirPath + "/javascript/API_MODELS_Metadata.json");
      jsonMaker = Json.createReader(fis);
      JsonObject objectMaker = jsonMaker.readObject();
      jsonMaker.close();
      fullMetadata.load(objectMaker);
      code.append(fullMetadata.getJSON());
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return code.toString();
  }
} // end of class

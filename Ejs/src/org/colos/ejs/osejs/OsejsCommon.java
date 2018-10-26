/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Revised February 2006 F. Esquembre
 */

package org.colos.ejs.osejs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import org.colos.ejs.osejs.utils.*;
import org.opensourcephysics.tools.EjsTool;
import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.minijar.PathAndFile;;


/**
 * Contains common utilities and data used by Ejs and EjsConsole.
 * This data and tools have been isolated here to minimize dependencies between Ejs and the console
 * (thus reducing the size of the independent EjsConsole.jar file).
 * @author Francisco Esquembre
 * @version: January 2008
 *
 */
public class OsejsCommon {
  static public final String OSEJS_NAME = "Osejs";
  static public final String EJS_SERVER = "www.um.es/fem/EjsWiki";


  static public final String BIN_DIR_PATH = "bin";
  static public final String DOC_DIR_PATH = "doc";
  static public final String EXTENSIONS_DIR_PATH = "extensions";
  
  static public final String USER_DIR_PATH = "workspace";
  static public final String CONFIG_DIR_PATH = "config";
  static public final String EXPORT_DIR_PATH = "export";
  static public final String OUTPUT_DIR_PATH = "output";
  static public final String SOURCE_DIR_PATH = "source";
  static public final String CUSTOM_ELEMENTS_DIR_PATH = "CustomElements";
  static public final String CUSTOM_HTML_ELEMENTS_DIR_PATH = "CustomHtmlElements";
  static public final String EJS_LIBRARY_DIR_PATH = "_ejs_library";

  static public final String PROGRAMMING_JAVA           = "JAVA";
  static public final String PROGRAMMING_JAVASCRIPT     = "JAVASCRIPT";
  static public final String PROGRAMMING_JAVA_PLUS_HTML = "JAVA_PLUS_HTML";
  
  static private Charset UTF16_CHARSET;
  static private Charset UTF8_CHARSET;
  
  static private ResourceUtil res = new ResourceUtil("Resources");
  
  static {
    try { UTF16_CHARSET = Charset.forName("UTF-16"); }
    catch (Exception exc) { UTF16_CHARSET = null; }
    try { UTF8_CHARSET = Charset.forName("UTF-8"); }
    catch (Exception exc) { UTF8_CHARSET = null; }
  }
    
  static public Charset getUTF8() { return UTF8_CHARSET; }
  static public Charset getUTF16() { return UTF16_CHARSET; }
  
  static public String[] sAllowedEJSExtensions = { ".ejs", ".ejss", ".ejsh", ".xml" };
  /**
   * Returns the charset used by a file, depending on its extension
   * @param _file
   * @return
   */
  static public Charset charsetOfFile(File _file) {
    String extension = _file.getName().toLowerCase();
    if (extension.endsWith(".xml")) return null; // special case among the allowed extensions
    for (String suffix : sAllowedEJSExtensions) if (extension.endsWith(suffix)) return UTF16_CHARSET;
    return null;
  }

  static public boolean isEJSfilename(String nameToLower) {
    for (String suffix : sAllowedEJSExtensions) if (nameToLower.endsWith(suffix)) return true;
    return false;
  }

  /**
   * Returns the bounds of a given screen number
   * @param _screen
   * @return
   */
  static public Rectangle getScreenBounds (int _screen) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gds = ge.getScreenDevices();
    if (_screen>=gds.length || _screen<0) return ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    return gds[_screen].getDefaultConfiguration().getBounds();
  }
  
  /**
   * Whether this file contains an EJS simulation
   * @param _xmlFile
   * @return
   */
  static public boolean isEJSfile(File _xmlFile) {
    String nameToLower = _xmlFile.getName().toLowerCase();
    if (!isEJSfilename(nameToLower)) return false;
    String input = FileUtils.readTextFile(_xmlFile,charsetOfFile(_xmlFile));
    if (input==null) return false;
    int begin = input.indexOf("<Osejs");
    if (begin<0) return false;
    int end = input.indexOf("</Osejs>");
    if (begin>=end) return false;
    return true;
  }
  
  static public String getDate() {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR) - 2000;
    int month = calendar.get(Calendar.MONTH)+1;
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    String today = "";
    if (year<10) today += "0"+year;
    else today += year;
    if (month<10) today += "0"+month;
    else today += month;
    if (day<10) today += "0"+day;
    else today += day;
    return today;
  }
  
  static public String getLongDate() {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH)+1;
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    String today = ""+year;
    if (month<10) today += "-0"+month;
    else today += "-"+month;
    if (day<10) today += "-0"+day;
    else today += "-"+day;
    return today;
  }

  static public String getLongTime() {
    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    int second = calendar.get(Calendar.SECOND);
    String time = "";
    if (hour<10) time += "0"+hour;
    else time += hour;
    if (minute<10) time += ":0"+minute;
    else time += ":"+minute;
    if (second<10) time += ":0"+second;
    else time += ":"+second;
    return time;
  }

  /**
   * Returns the Java version
   * Can be one of 8.0, 9.0, ...
   */
  static public double getJavaVersion () {
    String version = System.getProperty("java.version");
    int pos = version.indexOf('.');
    pos = version.indexOf('.', pos+1);
    return Double.parseDouble (version.substring (0, pos));
  }

  /**
   * Returns the Java version
   * Can be one of 5, 6, or 7.
   */
  static public int getJavaVersion(String javaRoot) {
    String javaVersion = System.getProperty("java.version");
    if (javaRoot!=null && javaRoot.length()>0) { // User-provided JVM 
      // Run "java -version" and read its output
      GeneratedOutput errorOut =null;
      Process proc=null;
      try {
        // Prepare the process
        File rootPath = new File(javaRoot+"/bin/java");
        String[] cmdArray = new String[] {rootPath.getCanonicalPath(), "-version" }; // ,">", tempFile.getCanonicalPath()};
        // get ready to read its output
        ByteArrayOutputStream baeStream = new ByteArrayOutputStream();
        errorOut = new GeneratedOutput(baeStream,proc = Runtime.getRuntime().exec(cmdArray, null),true);
        Thread thread = new Thread(errorOut);
        thread.setPriority(java.lang.Thread.NORM_PRIORITY);
        thread.start();
        // See what came out
        int error = proc.waitFor();
        if (error!=0) System.err.println ("ERROR: Cannot determine JAVA version!");
        errorOut.stop();
        javaVersion = baeStream.toString();
        proc.destroy();
      } catch (Exception exc) {
        System.err.println ("ERROR: Cannot determine JAVA version!");
        exc.printStackTrace();
        if (errorOut!=null) errorOut.stop();
        // very rough guessing
        if (javaRoot.indexOf('9')>0) return 9;
        if (javaRoot.indexOf('8')>0) return 8;
        if (javaRoot.indexOf('7')>0) return 7;
        if (javaRoot.indexOf('6')>0) return 6;
        if (javaRoot.indexOf('5')>0) return 5;
        return 8;
      }
      if (proc!=null) proc.destroy();
    }
    if (javaVersion.indexOf("9")>=0) return 9;
    if (javaVersion.indexOf("1.8")>=0) return 8;
    if (javaVersion.indexOf("1.7")>=0) return 7;
    if (javaVersion.indexOf("1.6")>=0) return 6;
    if (javaVersion.indexOf("1.5")>=0) return 5;
    return 6;
  }

  
  /**
   * Reads the list of auxiliary files required by a simulation file
   * @param _file
   * @return
   */
  static public Set<PathAndFile> getAuxiliaryFiles (File _xmlFile, File _sourceDir, Component _parentComponent) {
    String input = FileUtils.readTextFile(_xmlFile,charsetOfFile(_xmlFile));
    // Read the information about this simulation
    String infoText = null;
    int begin = input.indexOf("<"+OSEJS_NAME+".Information>\n");
    if (begin>=0) infoText = input.substring(begin+OSEJS_NAME.length()+15,input.indexOf("</"+OSEJS_NAME+".Information>\n"));
    if (infoText!=null) return readAuxiliaryFiles(_sourceDir,_xmlFile.getParentFile(),infoText,_parentComponent);
    return new HashSet<PathAndFile>();
  }

  /**
   * Reads the set of files required by the simulation as prescribed on the given file.
   * This is used when importing a file from another directory.
   */
  static private Set<PathAndFile> readAuxiliaryFiles (File _sourceDir, File _parentDir, String _input, Component _parentComponent) {
    Set<PathAndFile> set = new HashSet<PathAndFile>();
    String auxFiles = OsejsCommon.getPiece(_input,"<AuxiliaryFiles><![CDATA[","]]></AuxiliaryFiles>",false);
    String detectedFiles = OsejsCommon.getPiece(_input,"<DetectedFiles><![CDATA[","]]></DetectedFiles>",false);
    if (detectedFiles!=null) auxFiles = detectedFiles + auxFiles;
    String missingFiles = "";
    StringTokenizer tkn = new StringTokenizer(auxFiles,";\n");
    while (tkn.hasMoreTokens()) {
      String filename = tkn.nextToken().trim();
      if (filename.length()<=0) continue;
      File file;
      if (filename.startsWith("./")) file = new File (_parentDir,filename.substring(2));
      else file = new File (_sourceDir,filename);
      if (file.exists()) {
        if (file.isDirectory()) {
          String dirpath = FileUtils.getPath(_parentDir);
          for (File subfile : JarTool.getContents(file)) set.add(new PathAndFile(FileUtils.getRelativePath(subfile,dirpath,true),subfile));
        }
        else set.add(new PathAndFile(filename,file));
      }
      else missingFiles += "  " + filename + "\n";
    }
    if (missingFiles.length()>0) JOptionPane.showMessageDialog(_parentComponent, 
        res.getString("SimInfoEditor.RequiredFileNotFound")+"\n"+missingFiles, 
        res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    return set;
  }
  
  /**
   * A static utility that helps split code.
   * Returns null if the txt does not contain the begin or end tags
   */
  static public String getPiece (String _text, String _begtag, String _endtag, boolean _includeTags) {
    int begin = _text.indexOf(_begtag);
    if (begin<0) return null;
    int end = _text.indexOf(_endtag,begin);
    if (end<begin) return null;
    if (_includeTags) return _text.substring(begin,end+_endtag.length());
    return _text.substring(begin+_begtag.length(),end);
  }

  /**
   * Gets all pieces of text limited by the tags 
   * @param _source
   * @param _beginTag
   * @param _endTag
   * @return
   */
  static public java.util.List<String> getAllPieces(String _source, String _beginTag, String _endTag, boolean _includeTags) {
    int beginlen = _beginTag.length();
    int endlen = _endTag.length();
    java.util.List<String> list = new ArrayList<String>();
    int index = _source.indexOf(_beginTag);
    while (index>=0) {
      int endIndex = _source.indexOf(_endTag, index);
      if (endIndex<0) return list;
      if (_includeTags) list.add(_source.substring(index,endIndex+endlen));
      else list.add(_source.substring(index+beginlen,endIndex));
      // Next entry
      _source = _source.substring(endIndex+endlen);
      index = _source.indexOf(_beginTag);
    }
    return list;
  }

  static String getHTMLFilenamePrefix(File _ejsFile) {
    return getValidIdentifier(org.colos.ejs.library.utils.FileUtils.getPlainName(_ejsFile))+"_Intro_";
  }
  
  
  /**
   * Changes the first letter of the name to lower case
   * @param _name String The given name
   * @return String
   */
  static public String firstToLower (String _name) {
    return _name.substring(0,1).toLowerCase()+_name.substring(1);
  }

  /**
   * Changes the first uppercase letters (except the last one) of the name to lower case
   * @param _name String The given name
   * @return String
   */
  static public String beginningToLower (String _name) {
    int lastBeginningUppercase = -1;
    for (int i=0, n=_name.length(); i<n; i++) {
      if (Character.isUpperCase(_name.charAt(i))) lastBeginningUppercase = i;
      else break;
    }
//    System.err.println ("Input was : "+_name);
//    System.err.println ("Last uppercase at  : "+lastBeginningUppercase);
    if (lastBeginningUppercase>0) _name = _name.substring(0,lastBeginningUppercase).toLowerCase()+_name.substring(lastBeginningUppercase);
    else _name = _name.substring(0,1).toLowerCase()+_name.substring(1);
//    System.err.println ("Output is : "+_name);
    return _name;
  }

  
  /**
   * Gets the list of all simulations meta data files in a given directory (without user confirmation)
   * @param _parentComponent
   * @param _rootDir
   * @param _outputDir
   * @param _size
   * @param _message
   * @param _title
   * @param _abortOnEmpty
   * @return The list of all the meta data files found
   */
  static public java.util.List<PathAndFile> getUnconfirmedSimulationsMetadataFiles (Component _parentComponent, File _outputDir,
      boolean _abortOnEmpty, boolean _mustHaveHTML) {
    
    java.util.List<PathAndFile> list = null;
 // Get the list of simulations generated
    String outPath = FileUtils.getPath(_outputDir);
    if (_outputDir.exists() && _outputDir.isDirectory()) 
      list = recursiveGetSimulations(outPath,_outputDir,FileSystemView.getFileSystemView(),_mustHaveHTML);
    if (list==null || list.size()<=0) {
      if (_abortOnEmpty) {
        JOptionPane.showMessageDialog(_parentComponent, res.getString("Package.NoSimulations"),
            res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
        return null;
      }
      return new ArrayList<PathAndFile>();
    }
    Collections.sort(list);
    return list;
  }

  /**
   * Gets the list of simulations meta data files in a given directory after user confirmation
   * @param _parentComponent
   * @param _rootDir
   * @param _outputDir
   * @param _size
   * @param _message
   * @param _title
   * @param _abortOnEmpty
   * @return The list of all the meta data files found
   */
  static public java.util.List<PathAndFile> getSimulationsMetadataFiles (Component _parentComponent, File _outputDir,
      java.awt.Dimension _size, String _message, String _title, boolean _abortOnEmpty, boolean _mustHaveHTML) {
    
    java.util.List<PathAndFile> list = getUnconfirmedSimulationsMetadataFiles(_parentComponent, _outputDir, _abortOnEmpty, _mustHaveHTML);
    if (list==null) return null;
    
    // Ask the user for confirmation
    java.util.List<Object> confirmedList = EjsTool.ejsConfirmList(_parentComponent,_size,_message,_title,list);
    if (confirmedList==null) return null;

    // Create the list to return
    java.util.List<PathAndFile> newList = new ArrayList<PathAndFile>();
    for (Object object : confirmedList) newList.add((PathAndFile) object);
    return newList;
  }

  /**
   * Finds the generated simulation by searching for meta data files
   * Utility needed by getSimulationsList()
   * @param _rootDirPath
   * @param directory
   * @param fsView
   * @return
   */
  static private java.util.List<PathAndFile> recursiveGetSimulations (String _rootDirPath, File directory, FileSystemView fsView,boolean _mustHaveHTML) {
    File files[] = fsView.getFiles(directory, false);
    int l = Metadata.EXTENSION.length();
    java.util.List<PathAndFile> list = new ArrayList<PathAndFile>();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) list.addAll(recursiveGetSimulations(_rootDirPath,files[i], fsView, _mustHaveHTML));
      else if (files[i].getName().endsWith(Metadata.EXTENSION)) {
        if (_mustHaveHTML) { // Add only if there is a companion HTML file
          File htmlFile = new File (files[i].getParentFile(),org.colos.ejs.library.utils.FileUtils.getPlainName(files[i])+".html");
          if (!htmlFile.exists()) continue;
        }
        String relPath = FileUtils.getRelativePath(files[i], _rootDirPath, false);
        list.add (new PathAndFile(relPath.substring(0,relPath.length()-l),files[i])); // trim out the meta data extension
      }
    }
    return list;
  }

  /**
   * Returns a name which can be used as a valid identifier (for a variable, method, etc.).
   * It replaces illegal characters with '_'
   * @param _name String
   * @return String
   */
  static public String getValidIdentifier (String _name) {
    char[] chars = _name.toCharArray();
    if (!Character.isJavaIdentifierStart(chars[0])) chars[0] = '_';
    for (int i=1; i<chars.length; i++) {
      if (!Character.isJavaIdentifierPart(chars[i])) chars[i] = '_';
    }
    return new String(chars);
  }
  
  /**
   * Gets the list of .zip or .jar files in the given directory. 
   * The file names are relative to the given directory
   * @param _extensionsDirectory
   * @return
   */
  static public java.util.List<PathAndFile> getLibraryFiles (File _directory) {
    java.util.List<PathAndFile> list = new ArrayList<PathAndFile>();
    String extPath = FileUtils.getPath(_directory);
    for (File file : JarTool.getContents(_directory)) {
      String filename = file.getName().toLowerCase();
      if (filename.endsWith(".jar") || filename.endsWith(".zip")) {
        String relPath = FileUtils.getRelativePath(file, extPath, false);
        list.add(new PathAndFile(relPath,file));
      }
    }
    return list;
  }


  /**
   * A utility that warns about potential problems with a set of files
   * @param _ejs the calling EJS
   * @param _paths a Collection of problematic file paths
   * @param _reason a String which indicates the potential problem
   */
  static public void warnAboutFiles (JComponent _parentComponent, Collection<String> _paths, String _reason) {
    if (_paths.size()>0) {
      StringBuffer message = new StringBuffer(res.getString(_reason)+"\n");
      for (String path : _paths) message.append(" - " + path+"\n");
      JOptionPane.showMessageDialog(_parentComponent, message,res.getString("Warning"), JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * A utility that returns the bounds of the screen in which the window provided is displayed
   * @param window
   * @return
   */
  static public Rectangle getScreenBounds (Window window) {
    Point loc = window.getLocation();
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gds = ge.getScreenDevices();
    for(int i = 0; i<gds.length; i++) {
      Rectangle bounds = gds[i].getDefaultConfiguration().getBounds();
      if (loc.x>=bounds.x && loc.x<bounds.x+bounds.width) return bounds;
    }
    return ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
  }

  /**
   * A utility that returns the screen number in which the window provided is displayed
   * @param window
   * @return
   */
  static public int getScreenNumber (Window window) {
    Point loc = window.getLocation();
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gds = ge.getScreenDevices();
    for(int i = 0; i<gds.length; i++) {
      Rectangle bounds = gds[i].getDefaultConfiguration().getBounds();
      if (loc.x>=bounds.x && loc.x<bounds.x+bounds.width) return i;
    }
    return 0;
  }

} 

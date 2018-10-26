/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Revision: March 2008
 */

package org.colos.ejs.osejs.utils;

import java.io.*;
import java.nio.charset.Charset;

import javax.swing.filechooser.FileSystemView;

public class FileUtils {

  //  /**
  //   * Returns the name (without the extension) of a given file
  //   */
  //  static public String getPlainName (File _file) {
  //    String name = _file.getName();
  //    int index = name.lastIndexOf('.');
  //    if (index>=0) name = name.substring(0,index);
  //    return name;
  //  }

  /**
   * Returns the extension of a given file
   */
  static public TwoStrings getPlainNameAndExtension (File _file) {
    String name = _file.getName();
    int index = name.lastIndexOf('.');
    if (index<0) return new TwoStrings(name,"");
    return new TwoStrings(name.substring(0,index),name.substring(index+1));
  }

  /**
   * Gets the path of a file in standard form.
   * If it is a directory, the path ends in "/"
   */
  static public String getPath (File _file) {
    String path;
    try { path = _file.getCanonicalPath(); }
    catch (Exception exc) { path = _file.getAbsolutePath(); }
    if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
      path = path.replace('\\','/');
      // Sometimes the system provides c:, sometimes C:\
      int a = path.indexOf(':');
      if (a>0) path = path.substring(0,a).toUpperCase()+path.substring(a);
    }
    if (_file.isDirectory() && !path.endsWith("/")) path = path + "/";
    return path;
  }

  /**
   * Whether the given file is relative to the base directory
   */
  static public boolean isRelative (File _file, File _baseDir) {
    String filePath = getPath(_file), baseDirPath = getPath(_baseDir);
    if (filePath.equals(baseDirPath)) return true;
    return getRelativePath (filePath,baseDirPath,true).startsWith("./");
  }

  /**
   * Make the path of a file relative to a given base directory.
   * If the file is not under the base directory, the original path is returned.
   * If the file is a (relative) resource file, the relative path is prepended a "./"
   */
  static public String getRelativePath (String _filePath, File _baseDir, boolean _isResource) {
    return getRelativePath (_filePath, getPath(_baseDir),_isResource);
  }

  /**
   * Make the path of a file relative to a given base directory.
   * If the file is not under the base directory, the original path is returned.
   * If the file is a (relative) resource file, the relative path is prepended a "./"
   */
  static public String getRelativePath (File _file, File _baseDir, boolean _isResource) {
    return getRelativePath (getPath(_file), getPath(_baseDir),_isResource);
  }

  /**
   * Make the path of a file relative to a given base directory.
   * If the file is not under the base directory, the original path is returned.
   * If the file is a (relative) resource file, the relative path is prepended a "./"
   */
  static public String getRelativePath (File _file, String _baseDirPath, boolean _isResource) {
    return getRelativePath (getPath(_file), _baseDirPath,_isResource);
  }

  /**
   * Make the path of a file relative to a given base directory path.
   * If the file is not under the base directory, the original path is returned.
   * If the file is a (relative) resource file, the relative path is prepended a "./"
   */
  static public String getRelativePath (String _filePath, String _baseDirPath, boolean _isResource) {
    if (_filePath==null) return "";
    if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
      // Sometimes the system provides c:, sometimes C:\
      int index = _filePath.indexOf(':');
      if (index>0) _filePath = _filePath.substring(0,index).toUpperCase()+_filePath.substring(index);
      _filePath = _filePath.replace('\\','/');
    }
    if (_filePath.startsWith (_baseDirPath)) {
      _filePath = _filePath.substring(_baseDirPath.length());
      if (_isResource) _filePath = "./" + _filePath;
    }
    return _filePath;
  }

  /**
   * Returns the absolute path of a file under the given base directory.
   * If the file is not under this directory, the original path is given instead,
   * assuming it was absolute already.
   */
  static public String getAbsolutePath (String _path, File _baseDir) {
    File file = new File (_baseDir,_path);
    if (file.exists()) return getPath (file);
    return _path;
  }

  /**
   * Corrects a URL by changing spaces to "%20"
   * @param _urlStr String
   * @return String
   */
  static public String correctUrlString (String _urlStr) {
    String noSpaces = ""; // Replace spaces by "%20"
    java.util.StringTokenizer tkn = new java.util.StringTokenizer (_urlStr, " ",true);
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (token.equals(" ")) noSpaces += "%20";
      //else if (token.equals("&")) aux += "%26";
      else noSpaces += token;
    }
    String noAnds = ""; // Replace "&" by "%26"
    tkn = new java.util.StringTokenizer (noSpaces, "&",true);
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (token.equals("&")) noAnds += "%26";
      else noAnds += token;
    }
    return noAnds;
  }

  static public String correctUrlString(File _file) {
    return correctUrlString (getPath(_file));
  }

  /**
   * Uncorrect a URL by changing "%20" back to spaces
   * @param _urlStr String
   * @return String
   */
  static public String uncorrectUrlString (String _urlStr) {
    int index = _urlStr.indexOf("%20");
    while (index>=0) {
      _urlStr = _urlStr.substring(0,index)+" "+_urlStr.substring(index+3);
      index = _urlStr.indexOf("%20");
    }
    return _urlStr;
  }

  /**
   * Removes any leading of trailing quotes from the sring
   * @param _value
   * @return
   */
  static public String removeQuotes (String _value) {
    if (_value.startsWith("\"")) _value = _value.substring(1);
    if (_value.endsWith("\""))   _value = _value.substring(0,_value.length()-1);
    return _value;
  }

  /**
   * Replaces a given substring in a string
   */
  static public String replaceString (String _source, String _original, String _replacement) {
    int length = _original.length();
    StringBuffer buffer = new StringBuffer ();
    int index = _source.indexOf(_original);
    while (index>=0) {
      buffer.append(_source.substring(0,index));
      buffer.append(_replacement);
      _source = _source.substring(index+length);
      index = _source.indexOf(_original);
    }
    buffer.append(_source);
    return buffer.toString();
  }

  //  /**
  //   * Read the text inside a file.
  //   * @param _file File
  //   * @return String null if failed
  //   */
  //  static public String readTextFile(File _file) {
  //    return readTextFile(_file, (Charset) null);
  //  }

  /**
   * Read the text inside a file.
   * @param _file File
   * @return String null if failed
   */
  static public String readTextFile(File _file, Charset _charset) {
    if (!_file.exists()) return null;
    try {
      //      System.out.println("Reading "+ _file.getName()+ " with charset "+_charset);
      Reader reader;
      if (_charset==null) reader = new FileReader(_file);
      else reader = new InputStreamReader(new FileInputStream(_file),_charset);
      LineNumberReader l = new LineNumberReader(reader);
      StringBuffer buffer = new StringBuffer();
      String sl = l.readLine();
      while (sl != null) { buffer.append(sl+"\n"); sl = l.readLine(); }
      reader.close();
      return buffer.toString();
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  //  /**
  //   * Read the text inside a file.
  //   * @param _file File
  //   * @return String null if failed
  //   */
  //  static public String readXMLFile(File _file, Charset _charset) {
  //    if (_charset!=null) return readTextFile (_file,_charset);
  //    String text = readTextFile (_file,null);
  //    return text;
  //  }
  //  

  //  /**
  //   * Saves a String to file
  //   * @param _filename String The name of the file to save
  //   * @param _content String The content to be saved
  //   * @throws IOException
  //   * @return File the file created
  //   */
  //  static public File saveToFile (File _file, String _content) throws IOException {
  //    return saveToFile(_file, (Charset) null, _content);
  //  }


  /**
   * Saves a String to file
   * @param _filename String The name of the file to save
   * @param _content String The content to be saved
   * @throws IOException
   * @return File the file created
   */
  static public File saveToFile (File _file, Charset _charset, String _content) throws IOException {
    _file.getParentFile().mkdirs();
    if (_charset==null) {
      FileWriter fout = new FileWriter(_file);
      fout.write(_content);
      fout.close();
    }
    else {
      Writer writer = new OutputStreamWriter(new FileOutputStream(_file),_charset);
      writer.write(_content,0,_content.length());
      writer.flush();
      writer.close();
    }
    return _file;
  }

  /**
   * Copies from an InputStream into a target file
   * @param input
   * @param target
   * @return
   */
  static public boolean copy (InputStream input, File target) {
    try {
      target.getParentFile().mkdirs();
      OutputStream output = new FileOutputStream(target);
      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = input.read(buf)) > 0) output.write(buf, 0, len);
      input.close();
      output.close();
      return true;
    }
    catch (Exception exc) { exc.printStackTrace(); return false; }
  }

  static public boolean copy (File inputFile, File target) {
    try {
      return copy(new FileInputStream(inputFile),target);
    }
    catch (Exception exc) { exc.printStackTrace(); return false; }
  }

  static public boolean isWritable (File _file) {
    File dir;
    if (_file.isDirectory()) dir = _file;
    else dir = _file.getParentFile();
    try {
      long random = Math.round(Math.random()*1e+7);
      File tmp = new File(dir,"_ejs_tmp_TestFile"+random+".xml"); // File.createTempFile("_ejs_tmp_", ".xml", dir);
//      System.err.println("Temp file = "+tmp.getAbsolutePath());
      FileWriter fout = new FileWriter(tmp);
      fout.write("test");
      fout.close();
      tmp.delete();
      return true;
    }
    catch (Exception exc) { return false; }
  }



  /**
   * Deletes the subdirectories of a directory and, optionally, the directory
   * itself, if they contain no files.
   * @param _directory File The base directory
   * @param _thisIncluded boolean whether to remove the base directory
   */
  static public void removeEmptyDirs (File _directory, boolean _thisIncluded) {
    if (! (_directory.isDirectory() && _directory.exists())) return;
    FileSystemView fsView = FileSystemView.getFileSystemView();
    if (_thisIncluded) recursiveRemoveEmptyDirs(_directory,fsView);
    else { // Remove only its subdirectories
      File files[] = fsView.getFiles(_directory, false);
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) recursiveRemoveEmptyDirs (files[i], fsView);
      }
    }
  }

  /**
   * Deletes a directory and its subdirectories if they contain no file
   * @param directory File
   * @param fsView FileSystemView
   */
  static private void recursiveRemoveEmptyDirs (File directory, FileSystemView fsView) {
    File files[] = fsView.getFiles(directory, false);
    if (files.length==0) { directory.delete(); return; }
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) recursiveRemoveEmptyDirs (files[i], fsView);
    }
    // Check again
    files = fsView.getFiles(directory, false);
    if (files.length==0) directory.delete();
  }

}  // end of Class

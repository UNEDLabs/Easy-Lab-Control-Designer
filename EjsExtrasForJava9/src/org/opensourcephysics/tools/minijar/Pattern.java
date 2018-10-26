/**
 * The minijar package contains utilities to create the minimal
 * jar file that contains a series of classes and their dependencies.
 * Copyright (c) January 2008 F. Esquembre
 * @author F. Esquembre (http://www.um.es/fem).
 */

package org.opensourcephysics.tools.minijar;

import java.io.File;

/**
 * Creates and uses a file matching pattern to locate files in a directory
 * or in a compressed (ZIP or JAR) file.
 * Patterns for files in compressed files are very simple. The search is true if the
 * file name coincides with an entry in the compressed file.
 * Patterns for regular files (in disk directories) can be more sophisticated by 
 * making use of a MAGIC (wild char) character.
 * @author Francisco Esquembre
 *
 */
public class Pattern {
  static public final String MAGIC = "+";
  static public final String DOUBLE_MAGIC = "++";
  
  private boolean isPattern, isRecursive;
  private int baseLength, extensionLength;
  private String base, extension;
  private File compressedFile=null;
  
  /**
   * Constructs a Pattern object for a given pattern.
   * Subsequent searches will be positive if the path provided matches the pattern.
   * @param pattern A String of any of the following forms:
   * <ul>
   *   <li> <tt>dir1/dir2/filename.ext</tt> : The file in the dir1/dir2 directory with the given name and extension.
   *   <li> <tt>dir1/dir2/+.ext</tt> : Any file in the dir1/dir2 directory with the given extension.
   *   <li> <tt>dir1/dir2/whatever+.ext</tt> : Any file in the dir1/dir2 directory, with a name 
   *            that starts with 'whatever', and with the given extension.  
   *   <li> <tt>dir1/dir2/++.ext</tt> : Any file at any level under the dir1/dir2 directory with the given extension.
   *   <li> <tt>dir1/dir2/++</tt> : Any file at any level under the dir1/dir2 directory.
   * </ul>
   * In all cases, lower and upper case letters are considered different.
   * (Note: We don't use the standard wild char '*' because the operating system converts it before
   * passing it to the java program.)
   */
  public Pattern (String pattern) {
	  base = pattern.replace(File.separatorChar, '/');
    int index1 = base.indexOf(MAGIC);
    if (index1<0) isPattern = false; // it is a single file
    else {
      isPattern = true;
      extension = "";
      int index2 = base.indexOf(DOUBLE_MAGIC);
      if (index2<0) { // It is not a recursive search
        isRecursive = false;
        if (index1+2<=base.length()) extension = base.substring(index1+1);
        if (index1>0) base = base.substring(0,index1);
        else base = "";
      }
      else {
        isRecursive = true;
        if (index2+3<=base.length()) extension = base.substring(index2+2);
        if (index2>0) base = base.substring(0,index2);
        else base = "";
      }
      baseLength = base.length();
      extensionLength = extension.length();
    }
  }

  /**
   * Constructs a pattern consisting of a compressed ZIP or JAR file.
   * Subsequent searches for paths will be positive if the compressed file contains an entry
   * with the given path
   * @param file
   */
  public Pattern (File file) {
	isPattern = false;
	compressedFile = file;
  }

  /**
   * Prints a verbose description of the pattern
   */
  public String toString() {
	 return "isPattern = "+isPattern+", isRecursive = "+isRecursive+"\n"+
	        "base = "+base+", extension = "+extension;
  }
  
  /**
   * Whether the given file name matches the pattern
   * @param filename
   * @return
   */
  public boolean matches (String filename) {
    filename = filename.replace(File.separatorChar, '/');
    if (isPattern) {
      if (baseLength>0 && !filename.startsWith(base)) {
        return false;
      }
      if (extensionLength>0 && !filename.endsWith(extension)) {
        return false;
      }
      if (isRecursive) {
        return true;
      }
      filename = filename.substring(baseLength,filename.length()-extensionLength);
      return filename.indexOf('/')<0;
    }
    else if (compressedFile!=null) return MiniJar.sourceFind(compressedFile, filename)!=null;
    else {
      if (extension!=null && extension.endsWith("properties") && filename.endsWith("properties")) System.out.println (filename.equals(base));
      return filename.equals(base);
    }
  }
  
}


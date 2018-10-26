/**
 * The minijar package contains utilities to create the minimal
 * jar file that contains a series of classes and their dependencies.
 * Copyright (c) January 2008 F. Esquembre
 * @author F. Esquembre (http://www.um.es/fem).
 */

package org.opensourcephysics.tools.minijar;

import java.io.*;
import java.util.zip.*;

/**
 * A simple class that stores a relative path and a File.
 * If the file is inside a compressed (ZIP or JAR) file, the PathAndFile object 
 * stores information about the source compressed file and the entry of the file inside it.
 * @author Francisco Esquembre
 *
 */
public class PathAndFile implements Comparable<PathAndFile> {
  private String path=null;
  private File file=null;
  private ZipEntry zipEntry=null;

  /**
   * Constructor for a regular (in a directory) file
   * @param relativePath
   * @param file
   */
  public PathAndFile (String relativePath, File file) {
    this.path = relativePath;
    this.file = file;
  }

  /**
   * Constructor for a file inside a compressed (ZIP or JAR) file
   * @param relativePath
   * @param compressedFile
   * @param entry
   */
  public PathAndFile (String relativePath, File compressedFile, ZipEntry entry) {
    this.path = relativePath;
    this.file = compressedFile;
    this.zipEntry = entry;
  }

  // public boolean equals (PathAndFile second) { return path.equals(second.path); }

  public boolean equals (Object second) { return ((second instanceof PathAndFile) && path.equals(((PathAndFile) second).path) ); }

  public int hashCode () { return path.hashCode(); }
  
  public String toString () { return path; }

  public String getPath () { return path; }

  public InputStream getInputStream () {
    try {
      if (zipEntry!=null) return new ZipFile(file).getInputStream(zipEntry);
      return new FileInputStream(file);
    }
    catch (Exception exc) {
      System.out.println ("ERROR: opening file "+toString());
      exc.printStackTrace();
      return null;
    }
  }

  public File getFile () { return this.file; }

  public int compareTo(PathAndFile arg0) {
    return path.compareTo(arg0.path);
  }

}

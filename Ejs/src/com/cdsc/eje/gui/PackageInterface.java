package com.cdsc.eje.gui;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Lists the Java elements that conform the interface of a class.
 * This includes only static methods and attributes.
 * Author: F. Esquembre: http://fem.um.es
 */

@SuppressWarnings("serial")
public class PackageInterface
    extends JavaInterface {
  /**
   * Lists the static methods and attributes of a given class
   */
  @SuppressWarnings("unchecked")
  public PackageInterface(String keyword) {
    try {
      AbstractList<String> classes = getClassesForPackage(keyword);
      if (classes != null) {
        for (Iterator<String> it = classes.iterator(); it.hasNext(); ) {
          String aClass = it.next();
          MemberLine memberLine = new MemberLine(aClass, "class");
          this.addElement(memberLine);
          int memberLength = memberLine.toString().length();
          if (memberLength > rowLength) {
            rowLength = memberLength;
          }
        }
      }
    }
    catch (Exception exc) {}

    keyword = keyword + ".";
    int l = keyword.length();
    ArrayList<String> list = new ArrayList<String>();
    rowLength = 0;
    Package[] packages = Package.getPackages();
    for (int i = 0; i < packages.length; i++) {
      String packageName = packages[i].getName();
      if (packageName.startsWith(keyword)) {
        int index = packageName.indexOf('.', l);
        if (index > l) {
          packageName = packageName.substring(l, index);
        }
        else {
          packageName = packageName.substring(l);
        }
        if (!list.contains(packageName)) {
          list.add(packageName);
        }
      }
    }
    for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
      String packageName = it.next();
      MemberLine memberLine = new MemberLine(packageName, "package");
      this.addElement(memberLine);
      int memberLength = memberLine.toString().length();
      if (memberLength > rowLength) {
        rowLength = memberLength;
      }
    }
    for (int i = 0; i < this.size(); i++) {
      this.setElementAt( ( (MemberLine) elementAt(i)).formatToString(), i);
    }
    Collections.sort(this);
  }

  // The next method does not work!
  /**
   * Attempts to list all the classes in the specified package as determined
   * by the context class loader
   *
   * @param pckgname
   *            the package name to search
   * @return a list of classes that exist within that package
   * @throws ClassNotFoundException
   *             if something went wrong
   */
//  @SuppressWarnings("unchecked")
  public static AbstractList<String> getClassesForPackage(String pckgname) throws
      ClassNotFoundException {
    // This will hold a list of directories matching the pckgname. There may be more than one if a package is split over multiple jars/paths
    ArrayList<File> directories = new ArrayList<File>();
    try {
      ClassLoader cld = ClassLoader.getSystemClassLoader(); //Thread.currentThread().getContextClassLoader();
      if (cld == null) {
        throw new ClassNotFoundException("Can't get class loader.");
      }
      String path = pckgname.replace('.', '/');
      // Ask for all resources for the path
      Enumeration<URL> resources = cld.getResources(path);
      while (resources.hasMoreElements()) {
        URL aUrl = resources.nextElement();
        //System.out.println("Adding " + aUrl.getPath());
        directories.add(new File(URLDecoder.decode(aUrl.getPath(), "UTF-8")));
      }
    }
    catch (NullPointerException x) {
      throw new ClassNotFoundException(pckgname +
          " does not appear to be a valid package (Null pointer exception)");
    }
    catch (UnsupportedEncodingException encex) {
      throw new ClassNotFoundException(pckgname +
          " does not appear to be a valid package (Unsupported encoding)");
    }
    catch (IOException ioex) {
      throw new ClassNotFoundException(
          "IOException was thrown when trying to get all resources for " +
          pckgname);
    }

    ArrayList<String> classes = new ArrayList<String>();
    // For every directory identified capture all the .class files
    for (Iterator<File> it = directories.iterator(); it.hasNext(); ) {
      File directory = it.next();
      //System.out.println("Trying in directory " + directory.getAbsolutePath());
      if (directory.exists()) {
        // Get the list of the files contained in the package
        String[] files = directory.list();
        for (int i = 0; i < files.length; i++) {
          // we are only interested in .class files
          if (files[i].endsWith(".class")) {
            // removes the .class extension
            classes.add(files[i].substring(0, files[i].length() - 6));
          }
        }
      }
      else {
        throw new ClassNotFoundException(pckgname + " (" + directory.getPath() +
            ") does not appear to be a valid package");
      }
    }
    return classes;
  }

}

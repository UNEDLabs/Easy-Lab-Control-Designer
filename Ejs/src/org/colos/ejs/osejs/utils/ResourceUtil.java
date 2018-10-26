/**
 * The resource package contains utils and definitions for
 * multilingual use of the whole project
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.util.*;
import java.awt.*;
import java.net.URL;

public class ResourceUtil {
  static private Locale myLocale=null;
  static private Hashtable<String,ResourceBundle> resourceTable = new Hashtable<String,ResourceBundle>();

  private ResourceBundle resources;
  private String name;

  static public void setLocale (Locale _locale) { myLocale = _locale; }
  static public void resetTable () { resourceTable = new Hashtable<String,ResourceBundle>(); }

  public ResourceUtil (String _resourceName) { this (_resourceName,true); }

  public ResourceUtil (String _resourceName, boolean verbose) {
    name = _resourceName;
    if (_resourceName.indexOf('.')<0) _resourceName = "org.colos.ejs.osejs.resources."+_resourceName;
    try {
      ResourceBundle res = resourceTable.get(_resourceName);
      if (res==null) {
//        System.err.println("Loading resource "+_resourceName);
//        if (myLocale!=null) System.err.println ("Locale is "+myLocale.getCountry()+ "  for "+_resourceName);
//        else System.err.println ("Locale is null for "+_resourceName);
        if (myLocale!=null) res = ResourceBundle.getBundle(_resourceName,myLocale);
        else res = ResourceBundle.getBundle(_resourceName,Locale.getDefault());
        resourceTable.put(_resourceName,res);
      }
//      else System.err.println("Reusing resource "+_resourceName);
      resources = res;
    }
    catch (MissingResourceException mre) {
      if (verbose) System.err.println("Warning! : Resource class <" + _resourceName + "> not found!");
      resources = null;
    }
  }

  public boolean isNotAccesible() { return resources==null; }

  public String getOptionalString (String _keyword) {
//    System.err.println ("Trying "+_keyword);
    if (resources==null) return null;
    try { return resources.getString(_keyword); }
    catch (MissingResourceException mre) {
//      System.err.println ("Notfound "+_keyword);
      return null; 
    }
  }

  public String getString (String _keyword) {
    if (resources==null) {
      System.err.print  ("Warning! : Can't find resource <" + _keyword + ">. ");
      System.err.println("Resource class <" + name + "> was not initialized!");
      return _keyword;
    }
    try { return resources.getString(_keyword); }
    catch (MissingResourceException mre) {
      mre.printStackTrace();
      System.err.println("Warning! : Resource <" + _keyword + "> not found in <"+ name+">.");
      return _keyword;
    }
  }

  public int getInteger (String _keyword) {
    String str = getString(_keyword);
    if (str.equals(_keyword)) return 0;
    return Integer.parseInt(str);
  }

//  public boolean getBoolean (String _keyword, boolean _default) {
//    String str = getOptionalString(_keyword);
//    if (str==null || str.equals(_keyword)) return _default;
//    else return Boolean.getBoolean(str);
//  }

//  public Rectangle getRectangle (String _keyword) {
//    String str = getString(_keyword);
//    if (str.equals(_keyword)) return new Rectangle (0,0,10,10);
//    String[] b = tokenizeString(str);
//    return new Rectangle(Integer.parseInt(b[0]), Integer.parseInt(b[1]),
//                         Integer.parseInt(b[2]), Integer.parseInt(b[3]));
//  }

  public Dimension getDimension (String _keyword) {
    String str = getString(_keyword);
    if (str.equals(_keyword)) return new Dimension (10,10);
    String[] b = tokenizeString(str);
    return new Dimension(Integer.parseInt(b[0]), Integer.parseInt(b[1]));
  }

  public URL getURL (String _keyword, String _prefix) {
    String str = getString(_keyword);
    if (str.equals(_keyword)) return null;
    URL url = this.getClass().getResource(_prefix+str);
    return url;
  }

// --- a Utility

  static public String[] tokenizeString(String _input) {
    if (_input==null) { return null; }
    StringTokenizer t = new StringTokenizer(_input," ");
    String cmd[] = new String[t.countTokens()];
    for (int i = 0; i < cmd.length; i++) cmd[i] = t.nextToken();
    return cmd;
  }

} // end of class ResourceUtil

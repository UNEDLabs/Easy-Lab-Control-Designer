/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

public class TwoStrings implements Comparable<TwoStrings> {
  private String firstString=null, secondString=null;

  public TwoStrings (String _first, String _second) {
    firstString  = _first;
    secondString = _second;
  }

  public void   setFirstString(String _str) { firstString = new String(_str); }
  public String getFirstString() { return firstString; }

  public void   setSecondString(String _str) { secondString = new String(_str); }
  public String getSecondString() { return secondString; }

  public void   setStrings(String _first, String _second) {
    firstString  = _first;
    secondString = _second;
  }

  public String toString() { return firstString; }

  public int compareTo(TwoStrings arg0) {
    if (firstString==null) return 1;
    return firstString.compareTo(arg0.getFirstString());
  }

}

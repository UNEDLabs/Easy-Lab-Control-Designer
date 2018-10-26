/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

public class StringAndInteger {
  private String theString=null;
  private int theInteger=0;

  public StringAndInteger (String _str, int _integer) {
    theString  = _str;
    theInteger = _integer;
  }

  public String getString() { return theString; }

  public int getInteger() { return theInteger; }

  public String toString() { return theString; }

}

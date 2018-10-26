/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.io.*;

public class MyClassLoader extends ClassLoader {
    File rootDir=null;

    public MyClassLoader (File _rootDir) {
      rootDir = _rootDir;
    }

    public Class<?> findClass(String name) {
        byte[] b = loadClassData(name);
        return defineClass(name, b, 0, b.length);
    }

    private byte[] loadClassData(String name) {
      try {
        String filename = name; //.substring(0,name.lastIndexOf('.'));
        filename = filename.replace('.','/') + ".class";
        File source = new File (rootDir,filename);
        InputStream input = new FileInputStream(source);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = input.read(buf)) > 0) output.write(buf, 0, len);
        input.close();
        output.close();
        return output.toByteArray();
      } catch (Exception ex) { ex.printStackTrace(); return null; }
    }

} // end of class


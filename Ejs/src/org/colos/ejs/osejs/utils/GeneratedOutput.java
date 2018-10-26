
/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.io.OutputStream;

/**
 * A class to process output from other processes 
 */
public class GeneratedOutput implements Runnable {
  private java.io.InputStream stream = null;
  private java.io.OutputStream outputStream;
  public boolean goOn = true; //, error = true;

  public GeneratedOutput (Process aProc, boolean errorType) {
    //      outputStream = output; // But this nearly kills the app when Ejs is exited
    if (errorType) {
      stream = aProc.getErrorStream();
      outputStream = System.err;
    }
    else {
      stream = aProc.getInputStream();
      outputStream = System.out;
    }
  }

  public GeneratedOutput (OutputStream output, Process aProc, boolean errorType) {
    outputStream = output; // But this nearly kills the app when Ejs is exited
    if (errorType) {
      stream = aProc.getErrorStream();
    }
    else {
      stream = aProc.getInputStream();
    }
  }

  public void stop() { goOn = false; }

  public synchronized void run () {
    int b = -1;
    while (goOn)
      try {
        b = stream.read();
        if (b >= 0) {
          outputStream.write(b);
          outputStream.flush();
        }
      } catch (Exception exc) {} ;
  }


} // End of class


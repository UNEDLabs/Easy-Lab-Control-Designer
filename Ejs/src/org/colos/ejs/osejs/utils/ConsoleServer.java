/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

/**
 * The RMI remote interface to re-use a running console
 */
public interface ConsoleServer extends java.rmi.Remote {
  /**
   * Processes the given arguments
   * @param args String[]
   */
  public void processArgs (String[] args) throws java.rmi.RemoteException;
}


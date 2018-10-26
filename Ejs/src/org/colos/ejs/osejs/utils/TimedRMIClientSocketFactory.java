package org.colos.ejs.osejs.utils;

import java.rmi.server.RMIClientSocketFactory;
import java.net.*;
import java.io.*;

public class TimedRMIClientSocketFactory implements RMIClientSocketFactory, Serializable {
  protected int clientTimeout;

  public TimedRMIClientSocketFactory(int clientTimeout) {
   this.clientTimeout = clientTimeout;
  }

  public Socket createSocket(String host, int port) throws IOException {
     Socket s = new Socket();
     s.connect(new InetSocketAddress(host, port), clientTimeout);
     return s;
  }

  public Socket createSocket(int port) throws IOException {
    Socket s = new Socket();
    s.connect(new InetSocketAddress(port), clientTimeout);
    return s;
 }

}
/*
 * Copyright (C) 2012 Francisco Esquembre / Andres Mejias / Marco A. Marquez   
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * This software is based on Arduino library for Processing 
 * (Processing code to communicate with the ArduinoRPU Firmata 2 firmware) by David A. Mellis
 */
 
package es.uhu.hardware.utils;

import java.io.IOException;

import es.uhu.hardware.utils.NetworkClient;

/**
 * EthernetConnection allows you to communicate to a device attached to an ethernet (internet) IP and port, from a Java program.
 * 
 * @author Andres Mejias
 * @author Marco A. Marquez
 * @author Francisco Esquembre
 * @version 1.0 December 2012
 */
public class EthernetConnection implements Connection {
  
  private NetworkClient mClient;
  private String mIdString = "No ethernet connection open";
  
  // ---------------------------------
  // Implementation of Connection
  //----------------------------------
  
  public String[] listConnections() { return new String[] {}; }
    
  public String toString() {
    return mIdString;
  }

  /**
   * Opens the connection at the given IP and port
   * @param device the object that will deal with the connected device
   * @param ip the IP address of the ArduinoRPU Ethernet 
   * @param portnumber the port number asociated to the IP address (< 0 for a default value of 1024)
   */
  public void openConnection(String ip, Object portnumber) throws Exception {
    int port = 1024;
    if (portnumber instanceof Integer) port = ((Integer) portnumber).intValue();
    mIdString = "No ethernet connection open";
    mClient = new NetworkClient(ip, port);
    mIdString = "Ethernet connection to "+ip + ":"+port;  
  }
  
  public void closeConnection() throws Exception {
    if (mClient!=null) mClient.dispose();
    mClient = null;
    mIdString = "No ethernet connection open";
  }
  
  public void writeString(String text) throws IOException {
    mClient.write(text);
  }
  
  public void writeInt(int data) throws IOException {
    mClient.write(data);
  }
  
  public int available() {
    return mClient.available();
  }
  
  public int readInt() {
    return mClient.read();
  }
  
}

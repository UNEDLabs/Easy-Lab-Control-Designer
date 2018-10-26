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

/**
 * SerialConnection allows you to communicate to a device attached to a serial local (USB) port, from a Java program.[]
 * 
 * @author Andres Mejias
 * @author Marco A. Marquez
 * @author Francisco Esquembre
 * @version 1.0 December 2012
 */
public class SerialConnection implements Connection {

  private Serial mSerialConnection; // connection through mSerialConnection
  private String mIdString = "No serial connection open";

  // ---------------------------------
  // Implementation of Connection
  //----------------------------------
  
  public String[] listConnections() { return Serial.list(); }
  
  public String toString() {
    return mIdString;  
  }

  /**
   * Connects to the given port at the given baudrate
   * @param device the object that will deal with the connected device
   * @param port the port to which the device board is connected (something like COM3 on Windows, /dev/tty.usbmodem1411 on a Mac)
   * @param baudrate an Integer object with the desired baudrate (if null, or not anInteger object, a default of 57600 is used)
   * 
   */
  public void openConnection(String port, Object baudrate) throws Exception {
    int bauds = 57600;
    if (baudrate instanceof Integer) bauds = ((Integer) baudrate).intValue();
    mIdString = "No serial connection open";
    mSerialConnection = Serial.getSerial(port, bauds);
    mIdString = "Serial connection on " + port + " ("+bauds+" b/s)";
  }
  
  public void closeConnection() throws Exception {
    if (mSerialConnection!=null) mSerialConnection.dispose();
    mSerialConnection = null;
    mIdString = "No serial connection open";
  }
  
  public void writeInt(int data) throws IOException {
    mSerialConnection.write(data);
  }
  
  public int available() {
    return mSerialConnection.available();
  }

  public int readInt() {
    return mSerialConnection.read();
  }
  
}

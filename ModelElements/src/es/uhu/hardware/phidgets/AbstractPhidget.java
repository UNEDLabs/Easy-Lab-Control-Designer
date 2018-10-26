/*
 * Copyright (C) 2012 Francisco Esquembre / Marco A. Marquez / Andres Mejias  
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
 */

package es.uhu.hardware.phidgets;

import com.phidgets.*;
import com.phidgets.event.*;

import javax.swing.JOptionPane;

/**
 * <p> Title: InterfacePowerControl </p>
 * <p>Description: An abstract class, base of all other Phidgets
 * @author Francisco Esquembre
 * @author Marco Marquez
 * @author Andres Mejias
 * @version June 2012
 */
public abstract class AbstractPhidget {
  static public boolean sVerbose = true;
  static public int sTimeOut = 3000;

  private Phidget mPhidget;
  
  /**
   * Sets the verbosity of the phidgets when there are errors
   * @param verbose
   */
  static public void setVerbose (boolean verbose) { sVerbose = verbose; }

  /**
   * Whether to display error messages
   * @return
   */
  static public boolean isVerbose () { return sVerbose; }
  
  /**
   * Sets the time out used when trying to connect to a phidget
   * @param time
   */
  static public void setTimeOut (int time) { sTimeOut = time; }

  abstract protected Phidget createPhidget() throws PhidgetException;

  public AbstractPhidget() {
    try {
      mPhidget = createPhidget();
    } catch (PhidgetException ex) {
      errorMessage("Initialization Error");
    }
  }

  /**
   * Connects to the phidget using local USB and waits for the class time out
   * @param serialNumber The serial number of the interface
   * @return true if successful, false otherwise
   */
  public boolean connect(int serialNumber){
    try {
//      mPhidget.addAttachListener(new AttachListener() {
//        public void attached(AttachEvent ae) {
//          warningMessage("Attachment of " + ae);
//        }
//      });
//      mPhidget.addDetachListener(new DetachListener() {
//        public void detached(DetachEvent ae) {
//          warningMessage("Detachment of " + ae);
//        } 
//      });
      mPhidget.open(serialNumber);
      mPhidget.waitForAttachment(sTimeOut);
      return true;
    } catch (PhidgetException ex) {
      if (sVerbose) errorMessage("Connection Error");
      return false;
    }
  }

  /**
   * Connects to phidget through the network
   * @param hostIP The ip address of the server computer
   * @param portNumber The port number for the connection
   * @param password Password for accessing the interface, if required
   * @param serialNumber The serial number of the interface
   * @return true if successful, false otherwise
   */
  public boolean connect(int serialNumber,String hostIP,int portNumber,String password){
    final String mhostIP = hostIP;
    try {
      mPhidget.addServerConnectListener(new ServerConnectListener() {
        public void serverConnected(ServerConnectEvent ae) {
          //warningMessage("Connected to server " + mhostIP + ", " + ae);
          System.out.println("Connected to server " + mhostIP + ", " + ae);
        }
      });
      mPhidget.addServerDisconnectListener(new ServerDisconnectListener() {
        public void serverDisconnected(ServerDisconnectEvent ae) {
          //warningMessage("Disconnected from server " + mhostIP + ", " + ae);
          System.out.println("Disconnected from server " + mhostIP + ", " + ae);
        }
      });
      mPhidget.addAttachListener(new AttachListener() {
        public void attached(AttachEvent ae) {
          //warningMessage("Attachment of " + ae);
          System.out.println("Attachment of " + ae);
        }
      });
      mPhidget.addDetachListener(new DetachListener() {
        public void detached(DetachEvent ae) {
          //warningMessage("Detachment of " + ae);
          System.out.println("Detachment of " + ae);
        } 
      });
      mPhidget.open(serialNumber,hostIP,portNumber,password);
      mPhidget.waitForAttachment(sTimeOut);
      return true;
    } catch (PhidgetException ex) {
      if (sVerbose) errorMessage("Setup");
      return false;
    }
  }

  /**
   * Closed the connection to the phidget
   * @return true if successful, false otherwise
   */
  public boolean close() {        
    try { 
      mPhidget.close();
      return true;
    } catch (PhidgetException ex) {
      if (sVerbose) errorMessage("Close Error");
      return false;
    }
  }

  static protected void errorMessage(String message) {
    JOptionPane.showMessageDialog(null, message,"Phidget Interface Error",JOptionPane.ERROR_MESSAGE);
  }

  static protected void warningMessage(String message) {
    JOptionPane.showMessageDialog(null, message,"Phidget Interface Message",JOptionPane.INFORMATION_MESSAGE);
  }

}

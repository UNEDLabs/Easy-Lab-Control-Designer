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

import com.phidgets.AnalogPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;

/**
 * <p> Title: InterfaceAnalogOut </p>
 * <p>Description: A class to access PhidgetInterfaceAnalogOut devices, boards 
 * with analog outputs. 
 * Phidget Devices: 1002.
 * The PhidgetAnalog 4-Output (1002) produces a voltage over -10V to +10V. 
 * This voltage will be asserted up to +-20mA. An error will be reported when this current is exceeded, 
 * and the voltage the customer is getting is no longer accurate. The voltage is produced with 12 bit resolution (4.8mV).
 * The board is not isolated and all 4 channels share a common ground.
 * All the power is supplied by the USB bus. </p>
 * @author Francisco Esquembre
 * @author Marco Marquez
 * @author Andres Mejias
 * @version October 2012
 * <br>
*/

public class InterfaceAnalogOut extends AbstractPhidget {
  private AnalogPhidget mDevice;

  protected Phidget createPhidget() throws PhidgetException {
    return mDevice = new AnalogPhidget();
  }  
  
  // -----------------------------------
  // Methods particular of this Phidget
  // -----------------------------------

  
    /**
     * Enables an analog output. Setting this to true will activate the output, false is the default state. 
     * @param ind Index of the output
     * @param value State to set the output to 
     * @return true if successful, false otherwise
     */
    public boolean enableAnalogOutput(int ind,boolean value){
        try {
          mDevice.setEnabled(ind, value);
          return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Setting Error enabling analog out");
          return false;
        }
    }
    
    /**
     * Gets the enabled state for an output. 
     * When enabled, the output drives at the set Voltage, up to 20mA. When disabled, the output is tied to ground via a 4K pull-down. 
     * @param ind Index of the output
     * @return true if enabled, false otherwise
     */
    public boolean getAnalogStatus(int ind){
        try {
          return mDevice.getEnabled(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Getting Error reading status of analog out");
          return false;
        }
    }    
    
    /**
     * Sets the voltage output setting for an analog output. 
     * The range is VoltageMinVoltageMax. Voltage is not actually applied until Enabled is set to true.  
     * @param ind Index of the output
     * @param value Voltage to set the output to 
     * @return true if successful, false otherwise
     */
    public boolean setVoltage(int ind,double  value){
        try {
          mDevice.setVoltage(ind, value);
          return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Setting Error in analog out");
          return false;
        }
    }
    
    /**
     * Gets the voltage of an analog output.   
     * @param ind Index of the output
     * @return Voltage of an analog output, NaN if unsuccessful.
     */
    public double getVoltage(int ind){
        try {
          return mDevice.getVoltage(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error reading output value");
          return Double.NaN; 
        }
    }
    
    /**
     * Gets the minimum voltage for an analog output.   
     * @param ind Index of the output
     * @return Minimum voltage of an analog output, NaN if unsuccessful.
     */
    public double getAnalogMin(int ind){
        try {
          return mDevice.getVoltageMin(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error reading output minimum value");
          return Double.NaN; 
        }
    }
    
    /**
     * Gets the maximum voltage for an analog output.   
     * @param ind Index of the output
     * @return Maximum voltage of an analog output, NaN if unsuccessful.
     */
    public double getAnalogMax(int ind){
        try {
          return mDevice.getVoltageMax(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error reading output maximum value");
          return Double.NaN; 
        }
    }
         
    /**
     * Gets the number of analog outputs supported by the PhidgetAnalog.    
     * @return Number of analog outputs, -1 if unsuccessful.
     */
    public int getOutputCount() {
        try {
          return mDevice.getOutputCount();
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error reading number of analog outputs");
          return -1; 
        }
    }
    
     
    /**
     * Test main method
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        
      InterfaceAnalogOut ap =new InterfaceAnalogOut();
      ap.connect(129875);// edit with your serial number
      System.out.println("* * PhidgetAnalog * *");
      // enables all outputs
      for (int i = 0; i < ap.getOutputCount() - 1; i++) {
        ap.enableAnalogOutput(i, true);
      }
      double newVoltage = 2;        
      System.out.println("Setting the Voltage of the first Analog Output to " + newVoltage + "...");
      ap.setVoltage(0, newVoltage);
      System.out.println("Output status (0): " + ap.getAnalogStatus(0)); 
      Thread.sleep(30000);   
      //closing
      ap.setVoltage(0, 0);
      for (int i = 0; i < ap.getOutputCount() - 1; i++) {
          ap.enableAnalogOutput(i, false);
      }

    }

}

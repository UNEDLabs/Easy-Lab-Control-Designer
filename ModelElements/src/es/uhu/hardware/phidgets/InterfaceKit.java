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

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.Phidget;

/**
 * <p> Title: InterfaceKit </p>
 * <p>Description: A class to access PhidgetInterfaceKit devices, I/O boards 
 * with digital inputs, digital outputs and/or analog outputs. 
 * Phidget Devices: 1010, 1011, 1012, 1018, 1019 and 1203.
 * The analog inputs are used to measure continuous quantities, such as temperature,
 * humidity, position, pressure, etc.
 * The digital outputs can be used to drive LEDs, solid state relays, transistors;
 * in fact, anything that will accept a CMOS signal. 
 * The digital inputs can be used to convey the state of devices such as push buttons, 
 * limit switches, relays, and logic levels. </p>
 * @author Francisco Esquembre
 * @author Marco Marquez
 * @author Andres Mejias
 * @version June 2012
 * <br>
*/
public class InterfaceKit extends AbstractPhidget {
  private InterfaceKitPhidget mDevice;

  protected Phidget createPhidget() throws PhidgetException {
    return mDevice = new InterfaceKitPhidget();
  }


  // -----------------------------------
  // Methods particular of this Phidget
  // -----------------------------------

  
  /**
   * Returns the number of analog inputs on the Interface Kit. 
   * @return Number of analog inputs
   */
  public int getAnalogCount(){
    try {           
      return mDevice.getSensorCount();   
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting the number of analog inputs");
      return -1;
    }
  }
  
  /**
   * Returns the number of digital inputs on the Interface Kit. 
   * @return Number of digital inputs
   */
  public int getDigitalInCount(){
    try {           
      return mDevice.getInputCount();   
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting the number of digital inputs");
      return -1;
    }
  }
  
  /**
   * Returns the number of digital outputs on the Interface Kit. 
   * @return Number of digital outputs
   */
  public int getDigitalOutCount(){
    try {           
      return mDevice.getOutputCount();   
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting the number of digital outputs");
      return -1;
    }
  }
  
  /**
   * Sets the state of a digital output. Setting this to true will activate the output, False is the default state. 
   * @param ind Index of the output
   * @param value State to set the output to 
   * @return true if successful, false otherwise
   */
  public boolean setDigitalOut(int ind,boolean value){
    try {
      mDevice.setOutputState(ind, value);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error setting digital value");
      return false;
    }
  }

  /**
   * Returns the state of a digital output. Depending on the Phidget, this value may be either the value that
   * you last wrote out to the Phidget, or the value that the Phidget last returned. This is because some Phidgets
   * return their output state and others do not. This means that with some devices, reading the output state of a
   * pin directly after setting it, may not return the value that you just set. 
   * @param ind Index of the output
   * @return State of the output 
   */
  public boolean digitalOutputStatus(int ind){
    try {
      return mDevice.getOutputState(ind);
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading digital output status");
      return false;
    }
  }


  /**
   * Returns the state of a digital input. Digital inputs read True where they are activated and false when they are in
   * their default state.
   * @param ind Index of the input
   * @return State of the input 
   */
  public boolean getDigital(int ind){
    try {
      return  mDevice.getInputState(ind);
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading digital input value");
      return false;
    }
  }

  /**
   * Returns the value of a analog input. The analog inputs are where analog sensors are attached on the InterfaceKit 8/8/8. 
   * On the Linear and Circular touch sensor Phidgets, analog input 0 represents position on the slider. The valid range is 0-1000.
   * In the case of a sensor, this value can be converted to an actual sensor value using the formulas provided in:
   * http://www.phidgets.com/documentation/Sensors.pdf 
   * @param ind Index of the sensor 
   * @return Sensor value 
   */
  public int readAnalogSensorValue(int ind){
    try {           
      return mDevice.getSensorValue(ind);            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading analog input value");
      return -1;
    }
  }

  /**
   * Returns the raw value of a analog input. This is a more accurate version of readAnalogSensorValue. 
   * The valid range is 0-4095 (12-bits). 
   * @param ind Index of the sensor
   * @return Sensor value
   */
  public int readAnalogADValue(int ind){
    try {           
      return mDevice.getSensorRawValue(ind);            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading analog input value");
      return -1;
    }
  }

  /**
   * Sets the data rate of a sensor, in milliseconds. 
   * @param ind Index of the sensor
   * @param value data rate
   * @return true if successful, false otherwise
   */
  public boolean setDataRate(int ind,int value){
    try {
      mDevice.setDataRate(ind, value);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error Setting the data rate");
      return false;
    }
  }

  /**
   * Gets the data rate of a sensor, in milliseconds. 
   * @param ind Index of the sensor
   * @return data rate
   */
  public int getDataRate(int ind){
    try {           
      return mDevice.getDataRate(ind);   
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting the data rate");
      return -1;
    }
  }

  /**
   * Gets the maximum data rate of a sensor, in milliseconds. 
   * @param ind Index of the sensor
   * @return data rate
   */
  public int getDataRateMax(int ind){
    try {           
      return mDevice.getDataRateMax(ind);            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting the maximum data rate");
      return -1;
    }
  }

  /**
   * Gets the minimum data rate of a sensor, in milliseconds. 
   * @param ind Index of the sensor
   * @return data rate
   */
  public int getDataRateMin(int ind){
    try {           
      return mDevice.getDataRateMin(ind);            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting the minimum data rate");
      return -1;
    }
  }


  /**
   * Test main method
   * @param args
   */
  public static final void main(String args[]) throws Exception {
    InterfaceKit ik =new InterfaceKit();
    ik.connect(120672);// edit with your serial number
    System.out.println("* * Phidgets InterfaceKit * *");
    ik.setDigitalOut(7, true);
    Thread.sleep(30000);
    ik.setDigitalOut(7, false);
    ik.setDigitalOut(1, !ik.getDigital(7));
    System.out.println("Output status (7): " + ik.digitalOutputStatus(7));
    System.out.println("Output value (7): " + ik.getDigital(7));
    System.out.println("Analog Input (0): " + ik.readAnalogSensorValue(0));
    System.out.println("Raw analog output (0): " + ik.readAnalogADValue(0));
    System.out.println("Maximum data rate (0): " + ik.getDataRateMax(0));
    System.out.println("Number of analog inputs: " + ik.getAnalogCount());
    System.out.println("Number of digital inputs: " + ik.getDigitalInCount());
    System.out.println("Number of digital outputs: " + ik.getDigitalOutCount()); 
    ik.close();
  }
}

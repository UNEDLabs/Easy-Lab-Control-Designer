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
package es.uhu.hardware.arduino;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import es.uhu.hardware.utils.Connection;
import es.uhu.hardware.utils.EthernetConnection;
import es.uhu.hardware.utils.SerialConnection;

/**
 * Together with the Firmata 2 firmware (an Arduino sketch uploaded to the
 * Arduino board), this class allows you to control the Arduino board from
 * a Java program. You will be able to:
 * <ul>
 *   <li>read from and write to the digital pins</li>
 *   <li>read the analog inputs</li>
 *   <li>control servo motors</li>
 * </ul>
 * In the constructor, you must provide a connection object to your Arduino board.
 * There are currently two types of connections supported: 
 * <ul>
 *   <li>ethernet connection, which requires the StandarFirmataEthernet sketch</li>
 *   <li>serial (USB) connection, with requires the StandardFirmata sketch</li>
 * </ul>
 * @author Andres Mejias
 * @author Marco A. Marquez
 * @author Francisco Esquembre
 * @version 1.0 December 2012
 */
public class Arduino {
  // Constants used in a call to pinMode()) (operation modes).
  static public final int INPUT  = 0; 
  static public final int OUTPUT = 1;
  static public final int ANALOG = 2;
  static public final int PWM    = 3;
  static public final int SERVO  = 4;
  static public final int SHIFT  = 5;
  static public final int I2C    = 6;

  static private final int WAIT_FOR_BOARD = 3000; // Milliseconds to wait for the board to connect 
  static private final int DELAY = 100; // Milliseconds to wait when expecting a quick reply from the board

  // Constants used for communication
  static protected final int DIGITAL_MESSAGE          = 0x90; // send data for a digital port
  static protected final int ANALOG_MESSAGE           = 0xE0; // send data for an analog pin (or PWM)
  static protected final int REPORT_ANALOG            = 0xC0; // enable analog input by pin #
  static protected final int REPORT_DIGITAL           = 0xD0; // enable digital input by port
  static protected final int SET_PIN_MODE             = 0xF4; // set a pin to INPUT/OUTPUT/PWM/etc
  static protected final int REPORT_VERSION           = 0xF9; // report firmware version
  static protected final int START_SYSEX              = 0xF0; // start a MIDI SysEx message
  static protected final int END_SYSEX                = 0xF7; // end a MIDI SysEx message
  static protected final int SERVO_CONFIG             = 0x70; // servo config message
  static protected final int I2C_CONFIG               = 0x78; // I2C config
  static protected final int I2C_REQUEST              = 0x76; // I2C request
  static protected final int I2C_REPLY_DECIMAL        = 119;  // I2C reply (0x77)
  static protected final int PIN_STATE_QUERY          = 0x6D; // pin state query in a SysEx command (firmata version 2.2)
  static protected final int ANALOG_MAPPING_QUERY     = 0x69; // Information about analog channels
  static protected final int ANALOG_MAPPING_QUERY_DECIMAL      = 106; // Information about analog channels

  /**
   * Get a list of the available Arduino boards attached to the given connection
   * @param connection the desired connection (f.i. getDevices(new SerialConnection()) to list all devices connected to a serial port) 
   */
  static public List<String> getDevices(Connection connection) {
    List<String> list = new ArrayList<String>();
    Arduino testArduino = new Arduino();
    for (String port : connection.listConnections()) {
      System.out.println ("Testing device at connection "+port);
//      testArduino.configureConnection(port,57600);
      if (testArduino.connectSerial(port,57600)) {
        System.out.println ("Arduino board present at port "+port);
        testArduino.close();
        list.add(port);
      }
      else System.out.println ("No Arduino board at port "+port);
    }
    return list;
  }

  private Connection mConnection;
  
//  private int mMinorVersion = 0;
//  private int mMajorVersion = 0;
  private String mVersionInfo=null;
  private String mAnalogMap=null;

  // Place holders the last values from the Arduino board 
  private int[] mDigitalOutputData = new int[16]; // { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
  private int[] mDigitalInputData  = new int[16]; // { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
  private int[] mAnalogInputData   = new int[16]; // { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

  // ---------------------------------
  // Constructor and configuration
  //---------------------------------
  
  /**
   * Default constructor
   */
  public Arduino() {}

  // --------------------------------------------
  // Connection, information, and disconnection
  //---------------------------------------------

  /**
   * Connects to an Arduino board on a serial (USB) port at the default baudrate of 57600. Waits some seconds for the board to start up.
   * The board must be loaded with the StandardFirmata sketch of the Firmata 2 firmware.
   * @param commPort The String that identifies the serial port (something like COM3 on Windows, /dev/tty.usbmodem1411 on a Mac)
   * @return true if successful, false if there was any connection error
   */
  public boolean connectSerial(String commPort) {
    return connectSerial(commPort, 57600);
  }
  
  /**
   * Connects to an Arduino board on a serial (USB) port. Waits some seconds for the board to start up.
   * The board must be loaded with the StandardFirmata sketch of the Firmata 2 firmware with th eprescribed baudrate in it.
   * @param commPort The String that identifies the serial port (something like COM3 on Windows, /dev/tty.usbmodem1411 on a Mac)
   * @param baudrate The desired baudrate for a serial connection.
   * @return true if successful, false if there was any connection error
   */
  public boolean connectSerial(String commPort, int baudate) {
    if (!(mConnection instanceof SerialConnection)) {
      close();
      mConnection = new SerialConnection();
    }
    return connect(commPort,baudate);
  }

  /**
   * Connects to an Arduino board on the Ethernet. Waits some seconds for the board to start up.
   * The board must be loaded with the StandardFirmataEthernet sketch of the Firmata 2 firmware with the corresponding IP address and port.
   * @param ipAddress the IP address to which the Arduino board is connected
   * @param portnumber The port number for an ethernet connection
   * @return true if successful, false if there was any connection error
   */
  public boolean connectEthernet(String ipAddress, int portnumber) {
    if (!(mConnection instanceof EthernetConnection)) {
      close();
      mConnection = new EthernetConnection();
    }
    return connect(ipAddress,portnumber);
  }

  /**
   * Connects to the Arduino board. Waits some seconds for the board to start up.
   * @param commId The String that identifies the connection line. It can be:
   * <ul>
   *   <li>On a serial connection: the port to which the Arduino board is connected (something like COM3 on Windows, /dev/tty.usbmodem1411 on a Mac)</li>
   *   <li>On an ethernet connection: the IP to which the Arduino board is connected</li>
   * </ul>
   * @param commParameter A parameter object for the connection. It can be:
   * <ul>
   *   <li>The desired baudrate for a serial connection</li>
   *   <li>The port number for an ethernet connection</li>
   * </ul>
   * @return true if successful, false if there was any connection error
   */
  private boolean connect(String commId, int commParameter) {
    close();
    if (commId==null) return false;
    try {
      mConnection.openConnection(commId, commParameter);
      if (mConnection instanceof EthernetConnection) ((EthernetConnection) mConnection).writeString("H"); 
      try { Thread.sleep(WAIT_FOR_BOARD); } // wait for the board to setup itself 
      catch (InterruptedException iexc) {}
//      readInput();
//      resetInputVariables(); // reset input variables
      for (int i = 0; i < 6; i++) {
        mConnection.writeInt(REPORT_ANALOG | i);
        mConnection.writeInt(1);
      }
      for (int i = 0; i < 2; i++) {
        mConnection.writeInt(REPORT_DIGITAL | i);
        mConnection.writeInt(1);
      }
      return true;
    } 
    catch (Exception exc) {
      //exc.printStackTrace();
      errorMessage("Arduino not found at " + commId + ". Please check the port name and the USB connection.");
      return false;
    }
  }

  /**
   * Closes the connection to the board
   * @return true if successful, false if there was any connection error
   */
  public boolean close() {
    try {
      if (mConnection!=null) {
        mConnection.closeConnection();
//        readInput(); // read pending input
//        resetInputVariables(); // reset input variables
      }
      return true;
    }
    catch (Exception exc) {
      //exc.printStackTrace();
      warningMessage("Error found closing the connection. Is the Arduino board connected to the computer?.");
      return false;
    }
  }

//  public void clear() { 
//    if (mConnection!=null) readInput();
//    resetInputVariables();
//  }
  
  /**
   * Provides information about firmata firmware
   * @return String a message with the Firmata version, null if there was any connection error
   */
  public String readFirmwareInformation() {
    mVersionInfo = null;
    try { // query the board
      mConnection.writeInt(REPORT_VERSION);
      mConnection.writeInt(0);
      mConnection.writeInt(0);
      int counter = 0;
      do { // wait for the board reply
        try { Thread.sleep(20); } catch (InterruptedException iexc) {}
        counter++;
        readInput();
      } while (mVersionInfo==null && counter<100); // Not more than 2 seconds!
//      System.out.println("Version info took "+counter+ " attempts");
      return mVersionInfo;
    } catch (IOException e) {
    	errorMessage("Error reading the firmware information.");
      return null;
    }
  }

  /**
   * Provides information about which pins correspond to analog channels
   * @return String a string with the analog mapping, null if there was any connection error
   */
  public String readMappingInformation() {
    mAnalogMap = null; // ask always 
    try { // query the board
      mConnection.writeInt(START_SYSEX);
      mConnection.writeInt(ANALOG_MAPPING_QUERY);
      mConnection.writeInt(END_SYSEX);
      int counter = 0;
      do { // wait for the board reply
        try { Thread.sleep(20); } catch (InterruptedException iexc) {}
        counter++;
        readInput();
      } while (mAnalogMap==null && counter<100); // Not more than 2 seconds!
//      System.out.println("Mapping info took "+counter+ " attempts");
      return mAnalogMap;
    } catch (IOException e) {
      errorMessage("Error reading the mapping information.");
      //e.printStackTrace();
      return null;
    }
  }
  
  public String toString() {
    return mConnection.toString();  
  }
  
  // --------------------------------------------
  // Configuring the digital pins
  //---------------------------------------------

  /**
   * Sets the mode of a digital pin.
   * @param digitalPin the digital pin to set (from 2 to 13)
   * @param mode One of:
   * <ul>
   *   <li>0 = INPUT</li>
   *   <li>1 = OUPUT</li>
   *   <li>2 = ANALOG</li>
   *   <li>3 = PWM</li>
   *   <li>4 = SERVO</li>
   * </ul>
   * @return true if successful, false otherwise
   */
  public boolean setDigitalMode(int digitalPin, int mode) {
    try {
      mConnection.writeInt(SET_PIN_MODE);
      mConnection.writeInt(digitalPin);
      mConnection.writeInt(mode);
      return true;
    } catch (IOException e) {
    	errorMessage("Error setting the digital mode on pin " + digitalPin);
    	//e.printStackTrace();
      return false;
    }
  }

  /**
   * Returns the mode of a digital pin. One of:
   * <ul>
   *   <li>0 = INPUT</li>
   *   <li>1 = OUPUT</li>
   *   <li>2 = ANALOG</li>
   *   <li>3 = PWM</li>
   *   <li>4 = SERVO</li>
   * </ul>
   * @param digitalPin the digital pin to query (from 2 to 13)
   * @return int The pin mode, or -1 if there was any error 
   */
  public int getDigitalMode(int digitalPin) {
    try {
      queryPins(digitalPin);
      waitAndReadInput(DELAY);
      return storedInputData[2];
    } catch (IOException e) {
      errorMessage("Error reading the digital mode on pin " + digitalPin);      
      //e.printStackTrace();
      return -1;
    }
  }

  // --------------------------------------------
  // Configuring a servo motor
  //---------------------------------------------

  /**
   * Configures a digital pin to control a servo           
   * @param digitalPin the digital pin to which the servo is connected (from 2 to 13)
   * @param minPulse The pulse width, in microseconds, corresponding to the minimum (0 degree) angle on the servo
   * @param maxPulse The pulse width, in microseconds, corresponding to the maximum (180 degrees) angle on the servo 
   */
  public boolean setDigitalServoMode(int digitalPin, int minPulse, int maxPulse) {
    try { // Config message to servo support
      mConnection.writeInt(START_SYSEX);
      mConnection.writeInt(SERVO_CONFIG);
      mConnection.writeInt(digitalPin);
      mConnection.writeInt(minPulse & 0x7F); // LSB bits 0 a 6 minPulse
      mConnection.writeInt(minPulse >> 7); //MSB bits 7-13
      mConnection.writeInt(maxPulse & 0x7F); // LSB bits 0 a 6 minPulse
      mConnection.writeInt(maxPulse >> 7); //MSB bits 7-13
      mConnection.writeInt(END_SYSEX);
      return setDigitalMode(digitalPin, SERVO); // set pin to SERVO mode
    } catch (IOException e) {
      errorMessage("Error setting the servo mode on pin " + digitalPin);
      //e.printStackTrace();
      return false;
    }
  }    

  /**
   * Configures a digital pin to control a servo using default values for minimum (544) and maximum (2400) pulse         
   * @param digitalPin the digital pin to which the servo is connected (from 2 to 13)
   */
  public boolean setDigitalServoMode(int digitalPin) {
    int defaultMinPulse = 544;
    int defaultMaxPulse = 2400;
    return setDigitalServoMode(digitalPin, defaultMinPulse, defaultMaxPulse);
  }

  // ---------------------------------
  // Writing to the digital pins
  //---------------------------------

  /**
   * Sets a digital pin On or Off. 
   * @param digitalPin the digital pin to set (from 2 to 13)
   * @param on Whether the pin must be set to HIGH (5 volts) - true - or LOW (0 volts) - false -. 
   * @return true if successful, false otherwise
   */
  public boolean writeDigital(int digitalPin, boolean on) {
    setDigitalMode(digitalPin, OUTPUT);
    int portNumber = (digitalPin >> 3) & 0x0F; //Calculates the portNumber of the pin: digital pins 0 to 7 and digital pins 8 to 13
    if (on) mDigitalOutputData[portNumber] |=  (1 << (digitalPin & 0x07)); // sets to HIGH the pin position in the port register.
    else    mDigitalOutputData[portNumber] &= ~(1 << (digitalPin & 0x07)); // sets to LOW the pin position in the port register.
    try {
      mConnection.writeInt(DIGITAL_MESSAGE | portNumber);
      mConnection.writeInt(mDigitalOutputData[portNumber] & 0x7F); // LSB bits 0 to 6
      mConnection.writeInt(mDigitalOutputData[portNumber] >> 7); // MSB bits 7 to 13
      return true;
    } catch (IOException e) {
      //e.printStackTrace();
      errorMessage("Error writing boolean value on pin " + digitalPin);
      return false;
    }
  }

  /**
   * Sets the (PWM-wave) value of a digital pin.
   * @param digitalPin the digital pin to set (from 2 to 13), but only those which support PWM hardware
   * @param value: the value desired, from 0=LOWEST (always off) to 255=HIGHEST (always on)
   * @return true if successful, false otherwise
   */
  public boolean writeDigitalValue(int digitalPin, int value) {
    setDigitalMode(digitalPin, PWM);
    try {
      mConnection.writeInt(ANALOG_MESSAGE | (digitalPin & 0x0F));
      mConnection.writeInt(value & 0x7F); // value LSB
      mConnection.writeInt(value >> 7); // value MSB
      return true;
    } catch (IOException e) {
      //e.printStackTrace();
      errorMessage("Error writing PWM value on pin " + digitalPin);
      return false;
    }
  }

  /**
   * Sets the position of the servo (in degrees)
   * @param digitalPin the digital pin to which the servo is connected (from 2 to 13). Must be previously configured using setDigitalServo()
   * @param angle The angle desired for the servo, in degrees, from 0 to 180.
   */
  public boolean writeDigitalServo(int digitalPin, int angle) {
    try {
      mConnection.writeInt(ANALOG_MESSAGE | (digitalPin & 0x0F));
      mConnection.writeInt(angle & 0x7F); // value LSB
      mConnection.writeInt(angle >> 7); // value MSB
      return true;
    } catch (IOException e) {
      //e.printStackTrace();
      errorMessage("Error writing the servo angle on pin " + digitalPin);
      return false;
    }
  }
  
  // ---------------------------------
  // Reading from the digital PINs
  //----------------------------------

  /**
   * Returns whether a digital pin is on
   * @param digitalPin the digital pin to query (from 2 to 13)
   * @return true if on = HIGH (5 volts), false if off = LOW (0 volts)
   */
  public boolean readDigital(int digitalPin) {
    readInput();
    return ( (mDigitalInputData[digitalPin >> 3] >> (digitalPin & 0x07)) & 0x01) != 0;
  }
  
  /**
   * Returns the value of a digital pin
   * @param digitalPin the digital pin to query (from 2 to 13)
   * @return int the value (from 0: LOWEST to 255: HIGHEST), -1 if there was any error 
   */
  public int readDigitalValue(int digitalPin) {
    try {
      queryPins(digitalPin);
      waitAndReadInput(DELAY);
      int value = storedInputData[3]; // Bits 0-6 of pin state
      if (storedInputDataLength > 4) {
        for (int i = 4; i < storedInputDataLength; i++) {
          value += storedInputData[i]*(128*(i-3)); //Adds bits 7-13... of pin state. See firmata protocol (Pin State response)
        }
      }
      return value;
    } catch (IOException e) {
      //e.printStackTrace();
      errorMessage("Error reading the digital value on pin " + digitalPin);
      return -1; // Paco: Es -1 un valor posible?
    }
  }

  // ---------------------------------
  // Reading from the analog pins
  //---------------------------------

  /**
   * Returns the value read from an analog pin
   * @param analogPin the analog pin to query (from 0 to 5)
   * @return the value read, from 0=LOWEST (0 volts) to 1023=HIGHEST (5 volts)
   */
  public int readAnalog(int analogPin) {
    readInput();
    return mAnalogInputData[analogPin];
  }

  /**
   * Gets the voltage read on an analog pin
   * @param analogPin the analog pin to query (from 0 to 5)
   * @return the value read, from 0 to 5 volts
   */
  public double readAnalogVoltage(int pin) {
    return readAnalogVoltage(pin, 5.0);
  }

  /**
   * Gets the voltage read on an analog pin in a given scale
   * @param analogPin the analog pin to query (from 0 to 5)
   * @param voltageReference The voltage applied to AREF on the ArduinoRPU board 
   * @return the value read, from 0 to voltageReference volts
   */
  public double readAnalogVoltage(int pin, double voltageReference) {
    readInput();
    return (mAnalogInputData[pin]*voltageReference)/1023.0;
  }

  // ---------------------------------
  // Controlling the I2C
  //---------------------------------

  /**
   * i2cConfig with default parameters (no power pins and delay = 19 microseconds)
   */
  public boolean i2cActivate() {
    return i2cActivate(false, 19);
  }

  /**
   * Configuration and activation of I2C
   * @param powerPin If the power pins must be set to On or OFF
   * @param delay Sampling interval, how often analog data and I2C data is reported to the client
   */
  public boolean i2cActivate (boolean powerPin, int delay) {
    //    delayTime = delay;
    try {
      if (dataI2C==null) dataI2C = new int[256][256][MAX_DATA_BYTES];
      mConnection.writeInt(START_SYSEX);
      mConnection.writeInt(I2C_CONFIG);
      mConnection.writeInt(powerPin ? 0x01 : 0x00);
      mConnection.writeInt(delay & 0x7F); // delay value LSB
      mConnection.writeInt(delay >> 7); // delay value MSB
      mConnection.writeInt(END_SYSEX);
      return true;
    } catch (IOException e) {
      //e.printStackTrace();
      errorMessage("Error configuring the I2C.");
      return false;
    }
  }

  /**
   * Write data to an I2C device
   * @param slaveAddress
   * @param slaveRegister
   * @param dataToWrite
   * @return int[] the data read, null if there was any error
   */
  public int[] i2cWrite (int slaveAddress, int slaveRegister, int dataToWrite) {
    byte mode = 0x00; // write, 7-bits mode
    return i2cRequest(slaveAddress, mode, slaveRegister, dataToWrite);
  }

  /**
   * Reading from a I2C devices
   * @param slaveAddress
   * @param slaveRegister
   * @param bytesToRead
   * @return int[] the data read, null if there was any error
   */
  public int[] i2cRead (int slaveAddress, int slaveRegister, int bytesToRead) {
    byte mode = 0x08; // read once, 7-bits mode
    return i2cRequest(slaveAddress, mode, slaveRegister, bytesToRead);
  }

  /**
   * Reading continuosly from an I2C device
   * @param slaveAddress
   * @param slaveRegister
   * @param bytesToRead
   * @return int[] the data read, null if there was any error
   */
  public int[] i2cReadContinuously (int slaveAddress, int slaveRegister, int bytesToRead) {
    byte mode = 0x10; // read continuosly, 7-bits mode
    return i2cRequest(slaveAddress, mode, slaveRegister, bytesToRead);
  }

  /**
   * Stop reading continuously
   * @param slaveAddress
   * @param slaveRegister
   * @param bytesToRead
   * @return int[] the data read, null if there was any error
   */
  public int[] i2cStopReading (int slaveAddress, int slaveRegister, int bytesToRead) {
    byte mode = 0x18; // stop reading, 7-bits mode
    return i2cRequest(slaveAddress, mode, slaveRegister, bytesToRead);
  }

  // ---------------------------------
  // Private methods
  //---------------------------------

  static private final int MAX_DATA_BYTES = 128; // Max size for data reads

  private boolean parsingSysex=false;
  private int waitForData = 0;
  private int executeMultiByteCommand = 0;
  private int multiByteChannel = 0;
  private int sysexBytesRead = 0;
  private int storedInputDataLength = 0;
  private int[] storedInputData = new int[MAX_DATA_BYTES];
  private int dataI2C[][][] = null; // new int[256][256][MAX_DATA_BYTES];

  //  private int delayTime = 19; // default delay of 19 ms

//  private void resetInputVariables() {
////    mMinorVersion = 0;
////    mMajorVersion = 0;
//    mVersionInfo=null;
//    mAnalogMap=null;
//
//    // Place holders the last values from the Arduino board 
////    mDigitalOutputData = new int[16];
////    mDigitalInputData  = new int[16];
////    mAnalogInputData   = new int[16];
//    
//    parsingSysex=false;
//    waitForData = 0;
//    executeMultiByteCommand = 0;
//    multiByteChannel = 0;
//    sysexBytesRead = 0;
//    storedInputDataLength = 0;
//    storedInputData = new int[MAX_DATA_BYTES];
//  }
//  
  /**
   * Allows to read the current configuration of any pin
   * @param pin The pin 
   * @throws IOException 
   */
  private void queryPins(int pin) throws IOException {
    mConnection.writeInt(START_SYSEX);
    mConnection.writeInt(PIN_STATE_QUERY);
    mConnection.writeInt(pin);
    mConnection.writeInt(END_SYSEX);
  }
  
  private void readInput() {
    while (mConnection.available()>0) processInput();
  }

  private void waitAndReadInput(int delay) {
    try { Thread.sleep(delay); } 
    catch (InterruptedException iex) {}
    readInput();
  }
  
  private void setVersion(int minorVersion, int majorVersion) {
//    mMinorVersion = minorVersion;
//    mMajorVersion = majorVersion;
    mVersionInfo = "Firmata version: " + majorVersion + "." + minorVersion;
  }

  private void setAnalogMapping() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("* * * ARDUINO Analog Mapping * * *\n");
    for (int i= 1; i < sysexBytesRead; i++) {
      if (storedInputData[i] == 127) buffer.append("Pin " + i + " does not support analog\n");
      else                           buffer.append("Pin " + i + " is analog channel " + storedInputData[i]+"\n");
    }
    mAnalogMap = buffer.toString();
  }


  /**
   * I2C Request (read or write)
   * @param slaveAddress
   * @param mode. Available modes: 
   * slave address (MSB) + read/write and address mode bits
   * {7: always 0} + {6: reserved} + {5: address mode, 1 means 10-bit mode} +
   * {4-3: read/write, 00 => write, 01 => read once, 10 => read continuously, 11 => stop reading} +
   * {2-0: slave address MSB in 10-bit mode, not used in 7-bit mode} --> 000010000
   * 10-bit mode not supported by standardfirmata.
   * @param slaveRegister
   * @param dataIO
   * @return int[] the data read, null if there was any error
   */
  private int[] i2cRequest (int slaveAddress, byte mode, int slaveRegister, int dataIO) {
    // Available modes
    try {
      mConnection.writeInt(START_SYSEX);
      mConnection.writeInt(I2C_REQUEST);
      mConnection.writeInt(slaveAddress & 0x7F); // slave address value LSB
      mConnection.writeInt(mode); 
      // data
      if (slaveRegister != 0) {
        mConnection.writeInt(slaveRegister & 0x7F); // register to access
        mConnection.writeInt(slaveRegister >> 7); // 
      }
      mConnection.writeInt(dataIO & 0x7F); // number of bytes to read
      mConnection.writeInt(dataIO >> 7); // 
      mConnection.writeInt(END_SYSEX);
      if (mode!=0x00) { 
        while (mConnection.available()==0) {}
        try { Thread.sleep(DELAY); } 
        catch (InterruptedException ie) {
          //System.out.println(ie.getMessage());
          errorMessage("I2C request Error.");
        }
        while (mConnection.available()!=0) { processInput(); }
      }
      int nData = dataI2C[slaveAddress][slaveRegister][0];
      if (nData!=0) {
        int[] dataReply = new int[nData];
        //      System.arraycopy(dataI2C[slaveAddress][slaveRegister],1,dataReply,0,nData); try changing to this
        int[] reply = dataI2C[slaveAddress][slaveRegister];
        for (int i=0; i<nData; i++) dataReply[i] = reply[i+1];
        return dataReply;
      }
      else return new int[] {0};
    } catch (IOException e) {
      //e.printStackTrace();
      errorMessage("I2C reading Error.");
      return null;
    }
  }

  /**
   * Process I2C reply from Arduino board
   */
  private void processI2cReply(){
    // extract address
    int address = (storedInputData[1] & 0x7F) | (storedInputData[2] << 7);
    //extract register number
    int register = (storedInputData[3] & 0x7F) | (storedInputData[4] << 7);
    int k=1;
    int[] reply = dataI2C[address][register];
    for (int i = 5; i<storedInputDataLength; i+=2) {
      reply[k] = (storedInputData[i] & 0xFF) | (storedInputData[i+1] << 7);
      k+=1;
    }
    reply[0] = k-1; // Position [0] gets number of retrieved data
  }
  
  /**
   * Retrieves data from ArduinoRPU
   */
  private void processInput() {
    int inputData = mConnection.readInt();

    if (parsingSysex) {
      if (inputData == END_SYSEX) {
        parsingSysex = false;
        storedInputDataLength = sysexBytesRead;
//        System.out.println("hex code: " +  "0x" + Integer.toHexString(storedInputData[0]));
        switch (storedInputData[0]) {
          case ANALOG_MAPPING_QUERY_DECIMAL : setAnalogMapping(); break;
          case I2C_REPLY_DECIMAL            : processI2cReply();  break;
          // TODO: others firmata messages 
        }
        sysexBytesRead = 0;
      } 
      else if (sysexBytesRead<MAX_DATA_BYTES) { // Paco: Not adding this, sometimes causes an OutOfBoundsException
        storedInputData[sysexBytesRead] = inputData;
        sysexBytesRead++;
      }
    } 
    else if (waitForData > 0 && inputData < 128) {
      waitForData--;
      storedInputData[waitForData] = inputData;

      if (executeMultiByteCommand != 0 && waitForData == 0) { // Gets everything
//        System.out.println("Command hex code: " +  "0x" + Integer.toHexString(executeMultiByteCommand));
        switch (executeMultiByteCommand) {
          case DIGITAL_MESSAGE: mDigitalInputData[multiByteChannel] = (storedInputData[0] << 7) + storedInputData[1]; break; // setDigitalInputs(multiByteChannel, (storedInputData[0] << 7) + storedInputData[1]);
          case ANALOG_MESSAGE:  mAnalogInputData[multiByteChannel]  = (storedInputData[0] << 7) + storedInputData[1]; break; //  setAnalogInput();
          case REPORT_VERSION:  setVersion(storedInputData[0],storedInputData[1]); break;
        }
      }
    } 
    else {
      int command;
      if (inputData < 0xF0) {
        command = inputData & 0xF0;
        multiByteChannel = inputData & 0x0F;
      } else {
        command = inputData;
        // commands in the 0xF* range don't use channel data
      }
      switch (command) {
        case START_SYSEX: parsingSysex = true; 
        case DIGITAL_MESSAGE: 
        case ANALOG_MESSAGE:
        case REPORT_VERSION:
          waitForData = 2;
          executeMultiByteCommand = command;
          break;      
      }
    }
  }

  static protected void errorMessage(String message) {
	    JOptionPane.showMessageDialog(null, message,"Arduino Error",JOptionPane.ERROR_MESSAGE);
	  }

  static protected void warningMessage(String message) {
	    JOptionPane.showMessageDialog(null, message,"Arduino Message",JOptionPane.INFORMATION_MESSAGE);
	  }

  
}

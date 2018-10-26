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

import es.uhu.hardware.utils.Serial;
import es.uhu.hardware.utils.NetworkClient;

/**
 * Together with the Firmata 2 firmware (an Arduino sketch uploaded to the
 * Arduino board), this class allows you to control the Arduino board from
 * Easy Java Simulations: reading from and writing to the digital pins, reading the
 * analog inputs and controlling servo motors. Two types of connection: ethernet 
 * (with StandarFirmataEthernet sketch) and serial (with StandardFirmata sketch)
 */
public class ArduinoOld {
  /**
   * Constants used in a call to pinMode()) (operation modes).
   */
  public static final int INPUT = 0; 
  public static final int OUTPUT = 1;
  public static final int ANALOG = 2;
  public static final int PWM = 3;
  public static final int SERVO = 4;
  public static final int SHIFT = 5;
  public static final int I2C = 6;

  /**
   * Low and High digital values
   */
  public static final int LOW = 0;
  public static final int HIGH = 1;
  
  private final int MAX_DATA_BYTES = 128;
  
  private final int DIGITAL_MESSAGE          = 0x90; // send data for a digital port
  private final int ANALOG_MESSAGE           = 0xE0; // send data for an analog pin (or PWM)
  private final int REPORT_ANALOG            = 0xC0; // enable analog input by pin #
  private final int REPORT_DIGITAL           = 0xD0; // enable digital input by port
  private final int SET_PIN_MODE             = 0xF4; // set a pin to INPUT/OUTPUT/PWM/etc
  private final int REPORT_VERSION           = 0xF9; // report firmware version
  private final int START_SYSEX              = 0xF0; // start a MIDI SysEx message
  private final int END_SYSEX                = 0xF7; // end a MIDI SysEx message
  private final int SERVO_CONFIG             = 0x70; // servo config message
  private final int I2C_CONFIG               = 0x78; // I2C config
  private final int I2C_REQUEST              = 0x76; // I2C request
  private final int I2C_REPLY_DECIMAL        = 119;  // I2C reply (0x77)
  private final int PIN_STATE_QUERY          = 0x6D; // pin state query in a SysEx command (firmata version 2.2)
  private final int ANALOG_MAPPING_QUERY     = 0x69; // Information about analog channels
  private final int ANALOG_MAPPING_QUERY_DECIMAL      = 106; // Information about analog channels
  
  NetworkClient netClient; // connection through ethernet port
  Serial serial; // connection through serial
  public Boolean ethConnect;
      
  int waitForData = 0;
  int executeMultiByteCommand = 0;
  int multiByteChannel = 0;
  int[] storedInputData = new int[MAX_DATA_BYTES];
  boolean parsingSysex;
  int sysexBytesRead = 0;
  int storedInputDataLength = 0;

  // I2C  
  int address = 0;
  int register = 0;
  int dataI2C[][][] = new int[256][256][MAX_DATA_BYTES];
  int delaySerial = 50;
  int delayEthernet = 50;
  int delayTime = 19; // default delay of 19 ms
  // 
  // To hold the last values from Arduino 
  int[] digitalOutputData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
  int[] digitalInputData  = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
  int[] analogInputData   = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

  int majorVersion = 0;
  int minorVersion = 0;
  
  /**
   * Close netClient or serial port
   */
  public void dispose() {
    if (ethConnect) {
      this.netClient.dispose();
    } else
    {
      this.serial.dispose();
    }
  }
  
  /**
   * Returns number of bytes available on netClient or serial port
   * @return Number of bytes available
   */
  public int available() {
    if (ethConnect) {
      return netClient.available();
    } else {
      return serial.available();
    }
  }
  
  /**
   * Get a list of the available Arduino boards connected to serial ports; currently all serial devices
   * (i.e. the same as Serial.list()). 
   */
  public String[] list() {
    if (!ethConnect) {
      return Serial.list();
    } else return null;
  }
  
  /**
   * Initializes the access to an Arduino board running the Firmata 2 firmware with ethernet support.
   *
   * @param ip the IP address of the ArduinoRPU Ethernet 
   * @param port the port asociated to the IP address.
   * IP address and port must be fixed in StandardFirmataEthernet sketch loaded in ArduinoRPU
   */
  public void setArduinoEthernet(String ip, int port) {
    ethConnect = true;
    netClient = new NetworkClient(ip, port);
    netClient.write("H");  
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {}
    try {
      for (int i = 0; i < 6; i++) {
        netClient.write(REPORT_ANALOG | i);
        netClient.write(1);
      }
      for (int i = 0; i < 2; i++) {
        netClient.write(REPORT_DIGITAL | i);
        netClient.write(1);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  /**
   * Initializes the access to an Arduino board running the Firmata 2 firmware.
   *
   * @param iname the name of the netClient device associated with the Arduino
   * board (e.g. one the elements of the array returned by Arduino.list())
   * @param irate the baud rate to use to communicate with the Arduino board
   * (the firmata library defaults to 57600, and the examples use this rate,
   * but other firmwares may override it)
   */
  public void setArduino(String iname, int irate) {
    ethConnect = false;
    try {
      serial = Serial.getSerial(iname, irate);
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {}
      
      for (int i = 0; i < 6; i++) {
        serial.write(REPORT_ANALOG | i);
        serial.write(1);
      }
      for (int i = 0; i < 2; i++) {
        serial.write(REPORT_DIGITAL | i);
        serial.write(1);
      }
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }
  
  /**
   * Initializes the access to an Arduino board running the Firmata 2 firmware at the
   * default baud rate of 57600.
   * @param iname the name of the netClient device associated with the Arduino
   * board (e.g. one the elements of the array returned by Arduino.list())
   */
  public void setArduino(String iname) {
    this.setArduino(iname, 57600);
  }
  
  /**
   * Returns the last known value read from the digital pin: HIGH or LOW.
   *
   * @param pin the digital pin whose value should be returned (from 2 to 13,
   * since pins 0 and 1 are used for netClient communication)
   */
  public int digitalRead(int pin) {
    return (digitalInputData[pin >> 3] >> (pin & 0x07)) & 0x01;
  }

  /**
   * Returns the last known value read from the analog pin: 0 (0 volts) to
   * 1023 (5 volts).
   *
   * @param pin the analog pin whose value should be returned (from 0 to 5)
   */
  public int analogRead(int pin) {
   return analogInputData[pin];
  }

  /**
   * Returns the last known value read from the analog pin in VOLTS: 0 to
   * 5 volts.
   *
   * @param pin the analog pin whose value should be returned (from 0 to 5)
   */
  public double analogReadVoltage(int pin) {
    return this.analogReadVoltage(pin, 5.0);
  }
  
  /**
   * Returns the last known value read from the analog pin in VOLTS: 0 to
   * vRef volts.
   *
   * @param pin the analog pin whose value should be returned (from 0 to 5)
   * @param vRef The voltage applied to AREF on the ArduinoRPU board 
   */
  public double analogReadVoltage(int pin, double vRef) {
    return (double) analogInputData[pin]*vRef/1023.0;
  }
  
  /**
   * Set a digital pin to input or output mode.
   *
   * @param pin the pin whose mode to set (from 2 to 13)
   * @param mode either Arduino.INPUT or Arduino.OUTPUT
   */
  public void pinMode(int pin, int mode) {
    try {
      if (ethConnect) {
        netClient.write(SET_PIN_MODE);
        netClient.write(pin);
        netClient.write(mode);
      } else {
        serial.write(SET_PIN_MODE);
        serial.write(pin);
        serial.write(mode);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Write to a digital pin (the pin must have been put into output mode with
   * pinMode()).
   *
   * @param pin the pin to write to (from 2 to 13)
   * @param value the value to write: Arduino.LOW (0 volts) or Arduino.HIGH (5 volts)
   */
  public void digitalWrite(int pin, int value) {
    int portNumber = (pin >> 3) & 0x0F; //Calculates the portNumber of the pin: digital pins 0 to 7 and digital pins 8 to 13

    if (value == 0)
      digitalOutputData[portNumber] &= ~(1 << (pin & 0x07)); // sets to LOW the pin position in the port register.
    else
      digitalOutputData[portNumber] |= (1 << (pin & 0x07)); // sets to HIGH the pin position in the port register.

    try {
      if (ethConnect) {
        netClient.write(DIGITAL_MESSAGE | portNumber);
        netClient.write(digitalOutputData[portNumber] & 0x7F); // LSB bits 0 to 6
        netClient.write(digitalOutputData[portNumber] >> 7); // MSB bits 7 to 13
      } else {
        serial.write(DIGITAL_MESSAGE | portNumber);
        serial.write(digitalOutputData[portNumber] & 0x7F); // LSB bits 0 to 6
        serial.write(digitalOutputData[portNumber] >> 7); // MSB bits 7 to 13
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * Write an analog value (PWM-wave) to a digital pin.
   *
   * @param pin the pin to write to (only ones which support hardware pwm)
   * @param the value: 0 being the lowest (always off), and 255 the highest
   * (always on)
   */
  public void analogWrite(int pin, int value) {
    pinMode(pin, PWM);
    try {
      if (ethConnect) {
        netClient.write(ANALOG_MESSAGE | (pin & 0x0F));
        netClient.write(value & 0x7F); // value LSB
        netClient.write(value >> 7); // value MSB
      } else {
        serial.write(ANALOG_MESSAGE | (pin & 0x0F));
        serial.write(value & 0x7F); // value LSB
        serial.write(value >> 7); // value MSB
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Attach a servo connected to "pin"           
   * @param pin Pin connected to the control line of the servo
   * @param minPulse The pulse width, in microseconds, corresponding to the minimum (0 degree) angle on the servo
   * @param maxPulse The pulse width, in microseconds, corresponding to the maximum (180 degree) angle on the servo 
   */
  public void servoAttach(int pin, int minPulse, int maxPulse) {
    //
    // Config message to servo support
    //
    try {
      if (ethConnect) {
        netClient.write(START_SYSEX);
        netClient.write(SERVO_CONFIG);
        netClient.write(pin);
        netClient.write(minPulse & 0x7F); // LSB bits 0 a 6 minPulse
        netClient.write(minPulse >> 7); //MSB bits 7-13
        netClient.write(maxPulse & 0x7F); // LSB bits 0 a 6 minPulse
        netClient.write(maxPulse >> 7); //MSB bits 7-13
        netClient.write(END_SYSEX);
      } else {
        serial.write(START_SYSEX);
        serial.write(SERVO_CONFIG);
        serial.write(pin);
        serial.write(minPulse & 0x7F); // LSB bits 0 a 6 minPulse
        serial.write(minPulse >> 7); //MSB bits 7-13
        serial.write(maxPulse & 0x7F); // LSB bits 0 a 6 minPulse
        serial.write(maxPulse >> 7); //MSB bits 7-13
        serial.write(END_SYSEX);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // sets pin to SERVO mode
    pinMode(pin, SERVO);
  }    
  
  /**
   * Attach a servo connected to "pin" using default values for minPulse (544) and maxPulse (2400)
   * @param pin Pin connected to the control line of the servo
   */
  public void servoAttach(int pin) {
    int defaultMinPulse = 544;
    int defaultMaxPulse = 2400;
    this.servoAttach(pin, defaultMinPulse, defaultMaxPulse);
  }
  
  /**
   * Detachs servo configuring the pin to OUTPUT
   * @param pin Pin connected to the control line of the servo
   */
  public void servoDetach(int pin) {
    pinMode(pin, OUTPUT);
  }
      
  /**
   * Sets the position of the servo (in degrees)
   * @param pin Pin connected to the control line of the servo
   * @param angle The value to write to the servo, from 0 to 180 degrees
   */
  public void servoWrite (int pin, int angle) {
    try {
      if (ethConnect) {
        netClient.write(ANALOG_MESSAGE | (pin & 0x0F));
        netClient.write(angle & 0x7F); // value LSB
        netClient.write(angle >> 7); // value MSB
      } else {
        serial.write(ANALOG_MESSAGE | (pin & 0x0F));
        serial.write(angle & 0x7F); // value LSB
        serial.write(angle >> 7); // value MSB
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  //
  // I2C
  //
  
  /**
   * Configuration and activation of I2C
   * @param powerPin If the power pins must be set to On or OFF
   * @param delay Sampling interval, how often analog data and I2C data is reported to the client
   */
  public void i2cConfig (boolean powerPin, int delay) {
    delayTime = delay;
    try {
      if (ethConnect) {
        netClient.write(START_SYSEX);
        netClient.write(I2C_CONFIG);
        if (powerPin==true) netClient.write(0x01); else netClient.write(0x00);
        netClient.write(delay & 0x7F); // delay value LSB
        netClient.write(delay >> 7); // delay value MSB
        netClient.write(END_SYSEX);
      } else {
        serial.write(START_SYSEX);
        serial.write(I2C_CONFIG);
        if (powerPin==true) serial.write(0x01); else serial.write(0x00);
        serial.write(delay & 0x7F); // delay value LSB
        serial.write(delay >> 7); // delay value MSB
        serial.write(END_SYSEX);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * i2cConfig with default parameters (no power pins and delay = 19 microseconds)
   */
  public void i2cConfig() {
    this.i2cConfig(false, 19);
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
   * @return
   */
  private int[] i2cRequest (int slaveAddress, byte mode, int slaveRegister, int dataIO) {
    int dataReply[]; 
    //
    // Available modes
    //

    try {
      if (ethConnect) {
        netClient.write(START_SYSEX);
        netClient.write(I2C_REQUEST);
        netClient.write(slaveAddress & 0x7F); // slave address value LSB
        netClient.write(mode); 
        //
        // data
        //
        if (slaveRegister != 0) {
          netClient.write(slaveRegister & 0x7F); // register to access
          netClient.write(slaveRegister >> 7); // 
        }
        netClient.write(dataIO & 0x7F); // number of bytes to read
        netClient.write(dataIO >> 7); // 
        netClient.write(END_SYSEX);
        if (mode!=0x00) { 
          while (netClient.available()==0) {
          }
          try {
            Thread.sleep(delayEthernet);
          } catch (InterruptedException ie) {
            System.out.println(ie.getMessage());
          }
          while (netClient.available()!=0) {
            this.processInput();
          }
        }
        if (dataI2C[slaveAddress][slaveRegister][0]!= 0) {
          dataReply = new int[dataI2C[slaveAddress][slaveRegister][0]];
          for (int i=0; i<dataI2C[slaveAddress][slaveRegister][0]; i++) {
            dataReply[i] = dataI2C[slaveAddress][slaveRegister][i+1];
          }
          return dataReply;
        } else {
          dataReply = new int[] {0};
          return dataReply;
        }
      } else {
        serial.write(START_SYSEX);
        serial.write(I2C_REQUEST);
        serial.write(slaveAddress & 0x7F); // slave address value LSB
        serial.write(mode); 
        //
        // data
        //
        if (slaveRegister != 0) {
          serial.write(slaveRegister & 0x7F); // register to access
          serial.write(slaveRegister >> 7); // 
        }
        serial.write(dataIO & 0x7F); // number of bytes to read
        serial.write(dataIO >> 7); // 
        serial.write(END_SYSEX);
        if (mode!=0x00) {

          while (serial.available()==0) {
          }
          try {
            Thread.sleep(delaySerial);
          } catch (InterruptedException ie) {
            System.out.println(ie.getMessage());
          }
          while (serial.available()!=0) {
            this.processInput();
          }
        }
        if (dataI2C[slaveAddress][slaveRegister][0]!= 0) {
          dataReply = new int[dataI2C[slaveAddress][slaveRegister][0]];
          for (int i=0; i<dataI2C[slaveAddress][slaveRegister][0]; i++) {
            dataReply[i] = dataI2C[slaveAddress][slaveRegister][i+1];
          }
          return dataReply;
        } else {
          dataReply = new int[] {0};
          return dataReply;
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return new int[] {0};
    }
  }
  
  /**
   * Reading from a I2C devices
   * @param slaveAddress
   * @param slaveRegister
   * @param bytesToRead
   * @return Data from device
   */
  public int[] i2cRead (int slaveAddress, int slaveRegister, int bytesToRead) {
    byte mode = 0x08; // read once, 7-bits mode
    return this.i2cRequest(slaveAddress, mode, slaveRegister, bytesToRead);
  }
  
  /**
   * Reading continuosly from an I2C device
   * @param slaveAddress
   * @param slaveRegister
   * @param bytesToRead
   * @return Data from device
   */
  public int[] i2cReadCont (int slaveAddress, int slaveRegister, int bytesToRead) {
    byte mode = 0x10; // read continuosly, 7-bits mode
    return this.i2cRequest(slaveAddress, mode, slaveRegister, bytesToRead);
  }
  
  /**
   * Stop reading continously
   * @param slaveAddress
   * @param slaveRegister
   * @param bytesToRead
   * @return
   */
  public int[] i2cStopReading (int slaveAddress, int slaveRegister, int bytesToRead) {
    byte mode = 0x18; // stop reading, 7-bits mode
    return this.i2cRequest(slaveAddress, mode, slaveRegister, bytesToRead);
  }
  
  /**
   * Write data to an I2C device
   * @param slaveAddress
   * @param slaveRegister
   * @param dataToWrite
   * @return
   */
  public int[] i2cWrite (int slaveAddress, int slaveRegister, int dataToWrite) {
    byte mode = 0x00; // write, 7-bits mode
    return this.i2cRequest(slaveAddress, mode, slaveRegister, dataToWrite);
  }
  
  /**
   * Process I2C reply from Arduino board
   */
  private void processI2cReply(){
    // extract address
    address = (storedInputData[1] & 0x7F) | (storedInputData[2] << 7);
    //extract register number
    register = (storedInputData[3] & 0x7F) | (storedInputData[4] << 7);
    int k=1;
    for (int i = 5; i<storedInputDataLength; i+=2) {
      dataI2C[address][register][k] = (storedInputData[i] & 0xFF) | (storedInputData[i+1] << 7);
      k+=1;
    }
    dataI2C[address][register][0] = k-1; // Position [0] gets number of retrieved data
  }
  
  /**
   * Allows to read the current configuration of any pin
   * @param pin The pin 
   */
  public void pinQuery(int pin) {
    try {
      if (ethConnect) {
        netClient.write(START_SYSEX);
        netClient.write(PIN_STATE_QUERY);
        netClient.write(pin);
        netClient.write(END_SYSEX);
      } else {
        serial.write(START_SYSEX);
        serial.write(PIN_STATE_QUERY);
        serial.write(pin);
        serial.write(END_SYSEX);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Provides information about which pins correspond to analog channels and 
   * wich pins support analog
   */
  public void analogMappingQuery() {
    try {
      if (ethConnect) {
        netClient.write(START_SYSEX);
        netClient.write(ANALOG_MAPPING_QUERY);
        netClient.write(END_SYSEX);
      } else {
        serial.write(START_SYSEX);
        serial.write(ANALOG_MAPPING_QUERY);
        serial.write(END_SYSEX);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Returns the pin mode (0 -> INPUT, 1 -> OUTPUT, 2 -> ANALOG, 3 -> PWM, 4 -> SERVO) 
   * @param pin The pin 
   */
  public int getPinMode(int pin) {
    pinQuery(pin);
    return storedInputData[2];
  }
  
  /**
   * Returns the pin Value  
   * @param pin The pin 
   */
  public int getPinValue(int pin) {
    int value;
    pinQuery(pin);
    value = storedInputData[3]; // Bits 0-6 of pin state
    if (storedInputDataLength > 4) {
      for (int i = 4; i < storedInputDataLength; i++) {
        value += storedInputData[i]*(128*(i-3)); //Adds bits 7-13... of pin state. See firmata protocol (Pin State response)
      }
    }
    return value;
  }
  
  /**
   * Provides information about firmata firmware
   */
  public void firmwareQuery() {
    try {
      if (ethConnect) {
        netClient.write(REPORT_VERSION);
        netClient.write(majorVersion);
        netClient.write(minorVersion);
      } else {
        serial.write(REPORT_VERSION);
        serial.write(majorVersion);
        serial.write(minorVersion);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  //
  // Utlities used by processInput()
  //
  private void setDigitalInputs(int portNumber, int portData) {
    //System.out.println("digital port " + portNumber + " is " + portData);
    digitalInputData[portNumber] = portData;
  }

  private void setAnalogInput(int pin, int value) {
    //System.out.println("analog pin " + pin + " is " + value);
    analogInputData[pin] = value;
  }

  private void setVersion(int majorVersion, int minorVersion) {
    System.out.println("Firmata version: " + majorVersion + "." + minorVersion);
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
  }
  
  private void processAnalogMappingQuery() {
    System.out.println("* * * ARDUINO Analog Mapping * * *");
    System.out.println("**********************************");
    for (int i= 1; i < sysexBytesRead; i++) {
      if (storedInputData[i] == 127) {
        System.out.println("Pin " + i + " does not support analog");
      } else {
        System.out.println("Pin " + i + " is analog channel " + storedInputData[i]);
      }
    }
    System.out.println("**********************************");
  }

  /**
   * Retrieves data from ArduinoRPU
   */
  public void processInput() {
    int inputData;
    if (ethConnect) {
      inputData = netClient.read();
    } else {
      inputData = serial.read();
    }
	  int command;
    
    if (parsingSysex) {
      if (inputData == END_SYSEX) {
        parsingSysex = false;
        storedInputDataLength = sysexBytesRead;
        sysexBytesRead = 0;
        //System.out.println("hex code: " +  "0x" + Integer.toHexString(storedInputData[0]).toUpperCase().trim());
        switch (storedInputData[0]) {
          case (ANALOG_MAPPING_QUERY_DECIMAL): processAnalogMappingQuery();
          break;
          // TODO: others firmata messages 
          case (I2C_REPLY_DECIMAL): 
            processI2cReply();
          break;
        } 
      } else {
        storedInputData[sysexBytesRead] = inputData;
        sysexBytesRead++;
      }
    } else if (waitForData > 0 && inputData < 128) {
      waitForData--;
      storedInputData[waitForData] = inputData;
      
      if (executeMultiByteCommand != 0 && waitForData == 0) {
        // Gets everything
        switch(executeMultiByteCommand) {
        case DIGITAL_MESSAGE:
          setDigitalInputs(multiByteChannel, (storedInputData[0] << 7) + storedInputData[1]);
          break;
        case ANALOG_MESSAGE:
          setAnalogInput(multiByteChannel, (storedInputData[0] << 7) + storedInputData[1]);
          break;
        case REPORT_VERSION:
          setVersion(storedInputData[1], storedInputData[0]);
          break;
        }
      }
    } else {
      if(inputData < 0xF0) {
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
}

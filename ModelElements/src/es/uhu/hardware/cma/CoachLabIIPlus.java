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
 */

package es.uhu.hardware.cma;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;

import es.uhu.hardware.utils.Serial;

/**
 * <p> Title: CoachLabIIplus </p>
 * <p>Description: A class to access the CoachLab II+ interface, a multifunctional interface for computerized measurement and control.</p>
 * @author Francisco Esquembre
 * @author Andres Mejias
 * @author Marco Marquez
 * @version May 2012
 * <p>Sensors implemented sensors in this version:</p>
 * <table>
 * <tr> <th scope="col">Identification</th> <th scope="col">Description</th>    </tr>
 * <tr> <td>10</td> <td>0135i Thermocouple (-20 .. 110 °C)</td>                </tr>
 * <tr> <td>12</td> <td>0135i Thermocouple (-200 .. 1300 °C)</td>              </tr>
 * <tr> <td>20</td> <td>0663i Force Sensor (-5 .. +5N)</td>                     </tr>
 * <tr> <td>22</td> <td>0663i Force Sensor (-50..+50N)</td>                     </tr>
 * <tr> <td>30</td> <td>024i Magnetic Field sensor (-10 .. 50 mT)</td>          </tr>
 * <tr> <td>32</td> <td>024i Magnetic Field sensor (-100 .. 500 mT)</td>        </tr>
 * <tr> <td>41</td> <td>0222i Differential Current sensor (-500 .. +500mA)</td> </tr>
 * <tr> <td>51</td> <td>0210i Differential Voltage sensor (-10 .. +10V)</td>    </tr>
 * </table>
 */
public class CoachLabIIPlus {
  // Communication commands (implemented in this version)
  static private final int COMMAND_MEASUREMENT     =  0;
  static private final int COMMAND_SET_POWER_LEVEL =  2;
  static private final int COMMAND_SINGLE_READING  =  3;
  static private final int COMMAND_SEND_ID_STRING  =  4;
  static private final int COMMAND_REBOOT          = 12;
  static private final int COMMAND_ON_FOR          = 13;
  static private final int COMMAND_SET_BITS        = 16;
  static private final int COMMAND_RESET_BITS      = 17;
  static private final int COMMAND_GET_OUTPUTS     = 18;

  /**
   * Default baud rate
   */
  static public final int COACH_DEFAULT_BAUDRATE = 300;

  // Sensors implemented
  static public final int SENSOR_ADC_OUTPUT                     =  0;
  static public final int SENSOR_GENERIC_SENSOR_m10M10V         =  1;
  static public final int SENSOR_GENERIC_SENSOR_05V             =  2;
  static public final int SENSOR_THERMOCOUPLE_0135i_m20_M110C   = 10;
  static public final int SENSOR_THERMOCOUPLE_0135i_m200_M1300C = 12;
  static public final int SENSOR_FORCE_0663i_mM5N               = 20;
  static public final int SENSOR_FORCE_0663i_mM50N              = 22;
  static public final int SENSOR_MAGNETIC_FIELD_024i_m10M50mT   = 30;
  static public final int SENSOR_MAGNETIC_FIELD_024i_m100M500mT = 32;
  static public final int SENSOR_CURRENT_m500M500mA             = 41;
  static public final int SENSOR_DIFFERENTIAL_VOLTAGE_m10M10V   = 51;

  // Sensors (Complete list)
  //  ID      Description
  // ***********************************************************************************
  //          013 Angle sensor (0 .. 240°)
  //          014 Light sensor (0 .. 200 lx)
  // 10 12    0135i Thermocouple (with two ranges -20 .. 110 °C and -200 .. 1300 °C)
  //          0141i Light sensor (0 .. 10 lx)
  //          0142i Light sensor (0 .. 200 lx)
  //          0143i Light sensor (0 .. 150 klx)
  //          015 Sound sensor (-5 .. 5 Pa)
  //          016 Temperature sensor (-18 - 110 °C) 
  //          017i Sound sensor (-45 .. 45 Pa)
  // 51       0210i Differential Voltage sensor (-10 .. +10V)
  //          0212i Differential Voltage sensor (-500 .. +500mV)
  //          0221i Differential Current sensor (-5 .. +5A)
  // 41       0222i Differential Current sensor (-500 .. +500mA)
  //          023i Pressure sensor (0..7 bar)
  // 30 32    024i Magnetic Field sensor (-100 .. 500 mT and -10 .. 50 mT)
  //          025i Relative Humidity sensor (0 .. 100%)
  //          0265i Oxygen Gas Sensor (0 .. 100 %)
  //          0266i Dissolved Oxygen Sensor (for liquids) (0.. 14 mg/l)
  //          027i Heart-rate Sensor (with ear clip) (0 .. 100 %)
  //          028 EKG-set in combination with light sensor (014 or 142i)
  //          029 Geiger-Müller Ionizing radiation sensor (for beta and gamma radiation)
  //          030i + 31 Ph-sensor (ph amplifier 030i + pH electrode 031) (0 .. 14 pH)
  //          0313i ORP Sensor (-450 mV .. 1100 mV)
  //          032 Baro sensor (0-1100 mbar)
  //          033 Light sensor with 3 ranges (0-600, 0-6000, 0-150000 lx)
  //          0341 Gas Pressure sensor (0-210 kPa)
  //          035 Thermocouple temperature sensor (-30 - +1400°C)
  //          03517 Motion Detector (0.4 .. 6 m)
  //          0357 CO2 sensor (0 .. 5000 ppm)
  //          0358 Colorimeter (red, green, blue)
  //          0361i Charge sensor
  //          0362 Force sensor with two ranges (±5N and ±50N)
  //          0377i Blood Pressure Sensor
  //          037 Heartbeat sensor (0 .. 100%)
  //          0375 Exercises Heart Monitor sensor
  //          0376 Dissolved Oxygen sensor (0 .. 14 mg/l)
  //          0381 Relative Humidity sensor (0 .. 100%)
  //          0382 Conductivity sensor (0 .. 200 μS, 0 .. 2000 μS, 0 .. 20000 μS)
  //          0384 Current sensor (-0.8 .. 0.6mA) (I/V set)
  //          0384 Voltage sensor (-8 .. 6V) (I/V set)
  //          0385 Low g accelerometer (± 5g)
  //          0386 Smart Pulley (includes of photogate)
  //          0387i Flow Rate Sensor (0 .. 4.0 m/s)
  //          0391NH4 Ammonium (NH4+) electrode with integrated amplifier
  //          0391Ca Calcium (Ca2+) electrode with integrated amplifier
  //          0391Cl Chloride (Cl-) electrode with integrated amplifier
  //          0391NO3 Nitrate (NO3-) electrode with integrated amplifier
  //          0511 Temperature sensor (-20 - 125 °C)
  //          0513 Light sensor (0 .. 10 W/m2)
  //          0515 Voltage sensor (-10.. 10V)
  //          0661i CO2 sensor (0 .. 5000 ppm)
  // 20 22    0663i Force Sensor (-5 .. +5N, -50..+50N)

  // --------------------------------
  // Static methods and Constructor
  // --------------------------------
  
  static private java.util.List<Serial> sCoachInterfaces; 
  static private int sCurrentBaudRate=0;
  static public boolean sVerbose = true;

  /**
   * Sort of factory method to access to the connected interfaces 
   * @param baudRate
   * @return
   */
  static private Serial getAvailableInterface(int baudRate) {
    if (sCurrentBaudRate!=baudRate) {
      if (sCoachInterfaces!=null) for (Serial port : sCoachInterfaces) port.stop();
      sCoachInterfaces=null;
    }
    if (sCoachInterfaces==null) {
      sCoachInterfaces = new java.util.ArrayList<Serial>();
      String[] ports = Serial.list();
      int nPorts = ports.length;
      for (int i=0; i<nPorts; i++) {
        try {
          Serial port = Serial.getSerial(ports[i], baudRate, 'N', 8, 1);
          port.write(COMMAND_SEND_ID_STRING);
          delay(100);
          String reply = port.readString();
          if (reply!=null && reply.startsWith("-CoachLab")) sCoachInterfaces.add(port);
          else port.dispose();
        } catch (Exception e) { } // just ignore 
      }
    }
    if (sCoachInterfaces.isEmpty()) {
      if (sVerbose) errorMessage("Unable to locate CoachLab II+ interface!");
      return null;
    }
    // for (Serial port : sCoachInterfaces) System.out.println ("CoachLab 2+ found at port "+port);
    // TODO: Andr�s: Let the user choose when there are more than one inerfaces available
    return sCoachInterfaces.get(0);
  }
  
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
   * Test main method
   * @param args
   */
  static public void main(String[] args) {
    // System.out.println ("Library path = "+System.getProperty("java.library.path"));
    if (sCoachInterfaces.isEmpty()) System.out.println ("No CoachLab module found!");
    for (Serial port : sCoachInterfaces) System.out.println ("CoachLab 2+ found at port "+port);
  }

  // ------------------------------
  // Instance variables 
  // ------------------------------
  
  private Serial mSerial;   // The serial port
  private long mTimeInit=0; // Initial time (in millis) used to correct sampled times
  
  private int mTriggerChannel = 0; // Trigger channel: no trigger by default
  private int mTriggerADC;         // Trigger ADC
  private byte mTriggerHysteresis; // Trigger hysteresis
  private Method mTriggerMethod;   // Trigger notification method
  private Object mTriggerTarget;   // The object with the public trigget method

  private double[]   mSampledTimes; // Array of sampled times
  private double[][] mSampledData;  // Sampled data 

  // ------------------------------
  // Configuration methods 
  // ------------------------------

  /**
   * Reset the interface at the default baud rate
   */
  public boolean connect() { return connect (COACH_DEFAULT_BAUDRATE); }
  
  /**
   * Reset the interface at a given baud rate
   * @param baudRate One of:
   * <ul>
   *  <li> 2400 </li>  <li> 9600</li>   <li> 19200</li>     <li> 38400</li> 
   *  <li> 57600</li>  <li> 115200</li> <li> 230400 bd</li> <li> 500000 bd</li>
   *  <li> 300 (833333 bd) (Cygnal, aliased)</li>
   *  <li> 600 (1250000 bd)(Cygnal, aliased, outside specification)</li>
   * </ul>
   * Note.: CoachLab II+ starts at a default baud rate of 833333 bd.
   */
  public boolean connect(int baudRate) {
    mSerial = getAvailableInterface(baudRate);
    // Andr�s: c�mo podr�amos asegurarnos de que la interface se reinicializa completamente. reboot() causa una null exception
    if (mSerial!=null) {
      resetInitialTime();
      return true;
    }
    return false;
  }

  /**
   * Soft power-on reset of the interface
   */
  public boolean reboot() { 
    if (mSerial==null) return false;
    try {
      mSerial.write(COMMAND_REBOOT); // power-on reset
      return true;
    } catch (IOException e) {
      return false;
    }
  } 
  
  /** 
   * Stop data communication to the port 
   */
  public boolean stop() { 
    if (mSerial==null) return false;
    mSerial.stop();
    return true;
  }

  /**
   * Set the initial time. This is needed so that values read in sequence bear the correct time stamp.
   */
  public void resetInitialTime() { mTimeInit = System.currentTimeMillis(); }
  
  /**
   * Free the communication channel
   */
  public boolean close() {
    if (mSerial==null) return false;
    System.out.println ("Disposing CoachLab II+ interface at "+mSerial);
    setSwitch("A1",false);
    setSwitch("A2",false);
    setSwitch("B1",false);
    setSwitch("B2",false);
    setSwitch("C1",false);
    setSwitch("C2",false);
    setSwitch("D1",false);
    setSwitch("D2",false);
    setOutputEnabled("A1",false);
    setOutputEnabled("A2",false);
    setOutputEnabled("B1",false);
    setOutputEnabled("B2",false);
    setOutputEnabled("C1",false);
    setOutputEnabled("C2",false);
    setOutputEnabled("D1",false);
    setOutputEnabled("D2",false);
    System.out.println ("Done disposing CoachLab II+ interface at "+mSerial);
    
    mSerial.dispose();
    mSerial = null;
    return true;
  }
  
  // ------------------------------
  // Input methods 
  // ------------------------------

  /**
   * Read an analog channel (single reading)
   * @param channel channels 1 - 4 of CoachLab II+
   * @param sensorType The type of the sensor connected to that channel: one of the SENSOR_ static constants of the class
   * @return Normalized value of the measurement (V, N, A, mT...) or Double.NaN if failed to read 
   */
  public double readValue (int channel, int sensorType) {
    if (mSerial==null) return Double.NaN;
    if (channel < 1 || channel > 4) { // channels always 1-4. Channels 5 y 6 not implemented (in this version)
      if (sVerbose) errorMessage("Channel "+channel + " invalid!\nChoose one from 1 to 4");
      return Double.NaN;
    } 
    mSerial.write(new byte[] { COMMAND_SINGLE_READING, (byte) normChannel(channel, sensorType) });
    while (true) {  
      if (mSerial.available()==2) {
        byte[] inBuffer = new byte[2];
        mSerial.readBytes(inBuffer);
        return normValue(inBuffer[0]*64+inBuffer[1], sensorType); // Each byte contains 6 data bits (bits 0-5)
      }
    }
  }

  /**
   * Sets the target object for trigger notifications
   * @param target Any object that allows reflection to detect a public method with no parameters
   */
  public void setTargetObject(Object target) { mTriggerTarget = target; }
 
  /**
   * Configure trigger options for subsequent reading of sequences of data.
   * When the signal reaches the trigger level, a sequence of data will be read. 
   * The object can wait in the background until the data is ready, and then invoke a method of the target object 
   * (see setTargetObject()) given by the <i>methodName</i> parameter.
   * But, if no notification method is provided, the program blocks the execution and waits until the data is ready.
   * @param channel the channel used to trigger the reading (0 if no trigger)
   * @param sensorType The type of the sensor connected to that channel: one of the SENSOR_ static constants of the class
   * @param level the value of the magnitude that will trigger the reading
   * @param goingUp trigger direction (true -> UP, false -> DOWN)
   * @param hysteresis The value of hysteresis (from 0 to 127)
   * @param methodName The name of a public method(void) in the target object
   */
  public boolean setTrigger (int channel, int sensorType, double level, boolean goingUp, int hysteresis, String methodName) {
    if (methodName==null) return false;
    mTriggerChannel = normChannel(channel, sensorType);
    mTriggerADC = magnitudeToADC(level, sensorType);
    hysteresis = Math.max(0, Math.min(hysteresis,127));
    mTriggerHysteresis = (byte) (hysteresis & 0x007F); // hysteresis = bits 0-6
    if (goingUp) {
      if ((sensorType%2)!=0) mTriggerHysteresis = (byte) (mTriggerHysteresis | -128); // bit 7 = 1
    } 
    else {   
      if ((sensorType%2)==0) mTriggerHysteresis = (byte) (mTriggerHysteresis | -128); // bit 7 = 1
    }
    int index = methodName.indexOf('(');
    if (index>0) methodName = methodName.substring(0,index).trim(); // remove the parentheses
    try {
      for (Method method :  mTriggerTarget.getClass().getMethods()) {
        if (method.getName().equals(methodName) && method.getParameterTypes().length==0) { // This is it!
          mTriggerMethod = method;
          return true;
        }
      }
    }
    catch (Exception exc) { exc.printStackTrace(); }
    if (sVerbose) errorMessage("No such method: "+methodName + "(void) found in target object!\n");
    mTriggerMethod = null;
    return false;
  }
  
  /**
   * Reads one sequence of data from a single channel. If successful, the data can be obtained using:
   * <ul>
   * <li><i>getSampledTimes()</i> : sampled times</li>
   * <li><i>getSampledData(0)</i> : sampled data</li>
   * </ul>
   * @param channel The channel to read from (1-4)
   * @param sensorType The type of the sensor connected to that channel: one of the SENSOR_ static constants of the class
   * @param nPoints Number of points to sample from sensor
   * @param totalTime Total time of the sample
   * @return true if successful, false otherwise
   */
  public boolean readSequence(int channel, int sensorType, int nPoints, double totalTime) {
    return readSequence(new int[] {channel, 0, 0, 0},new int[] {sensorType, 0, 0, 0},nPoints,totalTime);
  }

  /**
   * Reads one sequence of data from the given channels. If successful, the data can be obtained using:
   * <ul>
   * <li><i>getSampledTimes()</i> : sampled times</li>
   * <li><i>getSampledData(i)</i> : sampled data on the i-th channel (from 0 to 3)</li>
   * </ul> 
   * @param channel An int[4] array of channels to read from (in the range of 1-4, 0 if no reading from that channel)
   * @param sensorType An int[4] array of types of the sensor connected to the corresponding channel: one of the SENSOR_ static constants of the class
   * @param nPoints Number of points to sample from sensor
   * @param totalTime Total time of the sample
   * @return true if successful, false otherwise
   */
  public boolean readSequence(final int[] channels, final int[] sensors, int nPoints, double totalTime){
    if (mSerial==null) return false;
    if (channels==null || channels.length!=4 || sensors==null || sensors.length!=4) {
      if (sVerbose) errorMessage("readSequence() error:\nInvalid channel or sensor array");
      return false;
    }
    // How many channels are desired?
    int nChannels=0;
    for (int i=0; i<channels.length; i++) if (channels[i]!=0) nChannels++;
    final int totalPoints = nPoints*nChannels;
    if (totalPoints<1 || totalPoints>16384) {
      if (sVerbose) errorMessage("readSequence() error:\nInvalid number of points to read: "+nPoints+"\nMaximum points allowed for "+nChannels+" is "+(16384/nChannels));
      return false;
    }
    final double sampleTime = totalTime/nPoints;
    if (sampleTime<0.1 && sampleTime!=0.01 && sampleTime!=0.025 && sampleTime!=0.05) {
      if (sVerbose) errorMessage("readSequence() error:\nInvalid sampling time: "+sampleTime+"\nValid times (below 0.1 ms): 0.01, 0.025, 0.05");
      return false;
    }
    
    mSampledTimes = new double[nPoints];
    mSampledData  = new double[4][];
    int[] normalizedChannels = new int[] {0,0,0,0};
    for (int i=0; i<channels.length; i++) {
      if (channels[i]!=0) {
        normalizedChannels[i] = normChannel(channels[i],sensors[i]);
        mSampledData[i] = new double[nPoints];
      }
    }

    // Build the command array
    byte[] coachCommand = new byte[20];
    coachCommand[0] = COMMAND_MEASUREMENT;

    // Set the sampling time
    int coachSamplingTime=0;
    if(sampleTime>=0.1) coachSamplingTime=(int)(sampleTime*10);
    else {
      if (sampleTime==0.01)  coachSamplingTime =  0; // sampleTime between points = 10 us
      if (sampleTime==0.025) coachSamplingTime = -2; // sampleTime between points = 25 us
      if (sampleTime==0.05)  coachSamplingTime = -3; // sampleTime between points = 50 us
    }
    coachCommand[1] = (byte)  (coachSamplingTime & 0xFF);
    coachCommand[2] = (byte) ((coachSamplingTime & 0xFF00)>>8);
    coachCommand[3] = (byte) ((coachSamplingTime & 0xFF0000)>>16);

    // Set the post-trigger points and the total number of points. We use the same value for both
    coachCommand[4] = (byte)  (totalPoints & 0xFF);
    coachCommand[5] = (byte) ((totalPoints & 0xFF00)>>8);
    coachCommand[6] = (byte)  (totalPoints & 0xFF);
    coachCommand[7] = (byte) ((totalPoints & 0xFF00)>>8);
    
    coachCommand[8] = (byte) normalizedChannels[0];
    coachCommand[9] = (byte) normalizedChannels[1];
    coachCommand[10] =(byte) normalizedChannels[2];
    coachCommand[11] =(byte) normalizedChannels[3];
    
    coachCommand[12] = 0; // Channels 5 to 8 not implemented
    coachCommand[13] = 0;
    coachCommand[14] = 0;
    coachCommand[15] = 0;
  
    // Set the trigger
    coachCommand[16] = (byte) mTriggerChannel;
    coachCommand[17] = (byte)  (mTriggerADC & 0xFF);
    coachCommand[18] = (byte) ((mTriggerADC & 0xFF00)>>8);
    coachCommand[19] = mTriggerHysteresis;

    mSerial.clear(); // clear the buffer
    final long t0 = System.currentTimeMillis()-mTimeInit; 
    System.out.println ("t0 ="+t0);
    mSerial.write(coachCommand);
    
    final int doublePoints = 2*totalPoints;
    if (mTriggerMethod==null) { // Wait until reading
      while (true) {
        if (mSerial.available()==doublePoints) {
          readSampledData(channels, sensors, totalPoints, sampleTime, t0);
          return true;
        }
      }
    }
    // else, differ the reading in a background thread
    Thread thread = new Thread(new Runnable() {
      long SLEEP_TIME = 100;
      public void run() {
        while (true) {
          if (mSerial.available()==doublePoints) {
            readSampledData(channels, sensors, totalPoints, sampleTime, t0);
            try { mTriggerMethod.invoke(mTriggerTarget, new Object[] {}); }
            catch (Exception exc) { exc.printStackTrace(); }
            return;
          }
          if (SLEEP_TIME<10) Thread.yield();
          else {
            try { Thread.sleep(SLEEP_TIME); } 
            catch(InterruptedException ie) {}
          }
        }
      }
    });
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.setDaemon(true);
    thread.start();
    return true;
  }
  
  /**
   * Does the reading
   * @param channels
   * @param sensors
   * @param totalPoints
   * @param sampleTime
   * @param t0
   */
  private boolean readSampledData(int channels[], int sensors[], int totalPoints, double sampleTime, double t0) {
    if (mSerial==null) return false;
    byte[] inBuffer = new byte[totalPoints*2];
    int[] valuesADCAux = new int[totalPoints];

    int nSensors=0;
    for (int ch=0, n=channels.length; ch<n; ch++) if (channels[ch]!=0) nSensors++;
    
    mSerial.readBytes(inBuffer);       
    for (int i=0,j=0, max=totalPoints*2; i<max; i+=2,j++) { // ADC output (0 - 4095)
      valuesADCAux[j] = (inBuffer[i]+128)*64+inBuffer[i+1];
    }
    for (int i=0,l=0; i<totalPoints; i+=nSensors,l++) {
      mSampledTimes[l]   = (double) (sampleTime*(l+1));
      mSampledTimes[l] += t0; // time intervals of sampled data from sensors
      for (int ch=0; ch<4; ch++) if (channels[ch]!=0) mSampledData[ch][l] = normValue(valuesADCAux[i+ch],sensors[ch]) ;
    }
    return true;
  }
    
  /**
   * Return, after a call to readSequence(), the double[] array of times at which the sampled data was taken
   * @return An array with the sampled times
   */
  public double[] getSampledTimes() { return mSampledTimes; }
  
  /**
   * Return, after a call to readSequence(), the data sampled from the sensor attached to that channel
   * @param channel the number of the channel (in the range 1-4)
   * @return An array with the sampled data
   */
  public double[] getSampledData(int channel) {
    if (mSampledData==null || channel<0 || channel>=mSampledData.length) return null;
    return mSampledData[channel];
  }

  // ------------------------------
  // Output methods 
  // ------------------------------

  /**
   * Enable/Disable one of the output channels 
   * @param channel The output channel: one of A1,A2,B1,B2,C1,C2,D1,D2
   * @param enabled true or false
   * @return true if successful, false otherwise
   */
  public boolean setOutputEnabled(String channel, boolean enabled) {
    if (mSerial==null) return false;
    int channelNumber = ChannelNumber(channel); 
    if (channelNumber<0) {
      if (sVerbose) errorMessage("setOutputEnabled(): Invalid output channel "+channel);
      return false;
    }
    if (enabled) mSerial.write(new byte[] {COMMAND_SET_BITS,   (byte) (1 << channelNumber)});
    else         mSerial.write(new byte[] {COMMAND_RESET_BITS, (byte) (1 << channelNumber)});
    return true;
  }

  /**
   * Enable several outputs at once. 
   * The mask is taken a a series of 0s and 1s, one for each of the outputs, in the same order as listed above.
   * Outputs which correspond to a '1' in the mask are set. 
   * Outputs corresponding to a '0' in the mask will remain unchanged.
   * @param mask Integer from 0 to 255
   * @return true if successful, false otherwise
   */
  public boolean setOutputMaskEnabled(int mask, boolean enabled) {
    if (mSerial==null) return false;
    if (mask<0 || mask>255) { // limit the mask
      if (sVerbose) errorMessage("setOutputMaskEnabled(): Invalid output mask "+mask+"\nValid values in the range 0 - 255.");
      return false;
    }
    if (enabled) mSerial.write(new byte[] {COMMAND_SET_BITS,   (byte) (mask & 0xFF)});
    else         mSerial.write(new byte[] {COMMAND_RESET_BITS, (byte) (mask & 0xFF)});
    return true;
  }

  /**
   * Return a number between 0 and 255 with the current status of all the outputs
   * @return an integer representing the output status, -1 if there is any error
   */
  public int getOutputStatus() {
    if (mSerial==null) return -1;
    try {
      mSerial.write(COMMAND_GET_OUTPUTS);
    } catch (IOException e) {
      return -1;
    }
    while (true) {
      if (mSerial.available()==1) return mSerial.read();
    }
  }
  
  /**
   * Turn on/of the switch module on the given channel
   * @param channel String The channel to set the switch: one of A1,A2,B1,B2,C1,C2,D1,D2
   * @param on boolean true if ON, false if OFF
   * @return true if successful, false otherwise
   */
  public boolean setSwitch(String channel, boolean on) {
    if (mSerial==null) return false;
    int channelNumber = ChannelNumber(channel); 
    if (channelNumber<0) {
      if (sVerbose) errorMessage("setSwitch(): Invalid output channel "+channel);
      return false;
    }
    if (on) mSerial.write(new byte[] {COMMAND_SET_POWER_LEVEL, (byte) (240 + channelNumber)});
    else    mSerial.write(new byte[] {COMMAND_SET_POWER_LEVEL, (byte) channelNumber});
    return true;
  }

  /**
   * Set the PWM output level for a given channel. Outputs are dutycycled with a frecuency of 625 Hz in 16 levels
   * @param channel String The channel to set the PWM for: one of A1,A2,B1,B2,C1,C2,D1,D2
   * @param level Duty cycle (from 0 to 15)
   * @return true if successful, false otherwise
   */
  public boolean setPWM(String channel, int level) {
    if (mSerial==null) return false;
    int channelNumber = ChannelNumber(channel); 
    if (channelNumber<0) {
      if (sVerbose) errorMessage("setPWM(): Invalid output channel "+channel);
      return false;
    }
    level = Math.max(0,Math.min(level,15)); // limit the level of PWM
    // The lower three bits define the output that is set (0-7), the highest 4 bits 
    // determine the duty cicle: 0 = 1/16,... 15=16/16
    mSerial.write(new byte[] {COMMAND_SET_POWER_LEVEL, (byte) (16*level+channelNumber)});
    return true;
}
   
  /**
   * Enables all outputs up to (and including) the given channel for a given period of time 
   * @param lastChannel String The last of the channel to enable: One of A1,A2,B1,B2,C1,C2,D1,D2
   * @param time time in milliseconds for which to enable the outputs (max value: 52 seconds)
   * @return true if successful, false otherwise
   */
  public boolean setAllOutputsEnabledTemporarily(String lastChannel, double time){
    if (mSerial==null) return false;
    int channelNumber = ChannelNumber(lastChannel); 
    if (channelNumber<0) {
      if (sVerbose) errorMessage("setAllOutputsEnabledTemporarily(): Invalid last output channel "+lastChannel);
      return false;
    }
    int timeParameter = Math.min(65535, ((int) time*10) >> 3); // div by 8, units in coach: 0.8 ms
    mSerial.write(new byte[] { COMMAND_ON_FOR, (byte) channelNumber, (byte) (timeParameter & 0xFF), (byte)((timeParameter & 0xFF00)>>8)});     
    return true;
  }

  // -------------------------------
  // Utility methods
  // -------------------------------

  /**
   * For debugging 
   * @param command the commnad to send
   * @param result number of bytes returned from coach
   */
  public byte[] command(int command, int result){
    byte[] buffer = new byte[result];
    try {
      mSerial.write((byte) command);
      mSerial.write(0);
    } catch (IOException e) {
      return buffer;
    }
    while (true) {
      if (mSerial.available()==result) {
        mSerial.readBytes(buffer);
        System.out.println(buffer);    
        break;
      }
    }      
    return buffer;
  }
  
  // -------------------------------
  // Static utility methods
  // -------------------------------

  /**
   * Specifies a delay (in milliseconds)
   * @param t time (milliseconds)
   */
  static private void delay(int t) {
    try { Thread.sleep(t); } 
    catch (InterruptedException ex) { errorMessage("Error while waiting for reply"); }
  }
  
  static private void errorMessage(String message) {
    JOptionPane.showMessageDialog(null, message,"CoachLab II+ Interface Error",JOptionPane.ERROR_MESSAGE);
  }
  
  static private int ChannelNumber(String channel) {
    if (channel==null) return -1;
    String uppercaseChannel = channel.toUpperCase();
    if (uppercaseChannel.equals("A1")) return 0;
    if (uppercaseChannel.equals("A2")) return 1;
    if (uppercaseChannel.equals("B1")) return 2; 
    if (uppercaseChannel.equals("B2")) return 3;
    if (uppercaseChannel.equals("C1")) return 4;
    if (uppercaseChannel.equals("C2")) return 5;
    if (uppercaseChannel.equals("D1")) return 6;
    if (uppercaseChannel.equals("D2")) return 7;
    JOptionPane.showMessageDialog(null, "Output channel "+channel + " invalid!\nChoose one from: A1,A2,B1,B2,C1,C2,D1,D2",
        "Error", JOptionPane.ERROR_MESSAGE);
    return -1;
  }
  
  /**
   * Calculates the channel (CoachLabII+)
   * ADC codes 0-4095 (12 bits ADC), as follows:
   * 1 = input 1, -10 / +10 V
   * 2 = Input 1 = 1, 0-5V
   * 4 = input 2, -10 / +10 V
   * 5 = input 2, 0-5V
   * 7 = Input 3, -10 / +10 V
   * 8 = input 3, 0-5V
   * 9 = input 4, -10 / +10 V
   * 10 = input 4, 0-5V
   * For the 0-5V channels 0 corresponds to 0V, 4095 with 5 V.
   * the -10 / +10 V channels 0 corresponds to +10 V, 4095 to -10V.
   * @param channel Channel 1-4 of CoachLabII+ (User channel)
   * @param sensorType Type of sensor (see list of sensors)
   * @return Channel (CoachLabII+) 
   */
  static private int normChannel(int channel, int sensorType){
    if (sensorType==0) sensorType=2;
    switch(channel) {
      case 1: return (sensorType%2==0) ?  2 : 1;
      case 2: return (sensorType%2==0) ?  5 : 4;
      case 3: return (sensorType%2==0) ?  8 : 7;
      case 4: return (sensorType%2==0) ? 10 : 9;
      default : return 0;
    }
  }    

  /**
   * Calculates Vout of sensor from ADC output (0-4095) in -10/+10V channels 
   * @param value Decimal output from ADC (0-4095)
   * @return Vout of sensor (Volts)
   */ 
  static private double normVm10M10(int value) { return  (-20.0/4095.0*(value) + 10); }

  /**
   * Calculates Vout of sensor from ADC output (0-4095) in 0-5V channels 
   * @param value (ecimal output from ADC (0-4095)
   * @return Vout of sensor (Volts)
   */
  static private double normV05(int value) { return  (5.0/4095.0*(value)); }

  /**
   * Calculates the magnitude measured by the sensor from ADC output
   * @param value ADC output 
   * @param sensorType An integer representing the sensor (see list of sensors)
   * @return measurement from sensor
   */
  static private double normValue(int value,int sensorType){
    switch(sensorType){
      case SENSOR_ADC_OUTPUT : return value; // AD converter output 
      
      case SENSOR_GENERIC_SENSOR_m10M10V : return normVm10M10(value); // Generic -10..+10 Volt 
      case SENSOR_GENERIC_SENSOR_05V :     return normV05(value);     // Generic 0..5 Volt
      
      case SENSOR_THERMOCOUPLE_0135i_m20_M110C :   return 29.093*normV05(value)-26.33; // Thermocouple (D0135i2)(-20..110 ºC)    
      case SENSOR_THERMOCOUPLE_0135i_m200_M1300C : return 316.9*normV05(value)-221;  // Thermocouple (D0135i2)(-200..1300 ºC)
      
      case SENSOR_FORCE_0663i_mM5N :  return -2.45*normV05(value)+5.98; // Force sensor (D0663i) -5 N .. +5 N
      case SENSOR_FORCE_0663i_mM50N : return -24.4*normV05(value)+61;   // Force sensor (D0663i) -50 N .. +50 N
      
      case SENSOR_MAGNETIC_FIELD_024i_m10M50mT :   return 20.0*normV05(value)-10;    // Magnetic Field sensor (D024i) -10 .. +50 mT
      case SENSOR_MAGNETIC_FIELD_024i_m100M500mT : return 200.0*normV05(value)-100;  // Magnetic Field sensor (D024i) -100 .. +500 mT
      
      case SENSOR_CURRENT_m500M500mA : return 78.125*normVm10M10(value)-0.47; //Current sensor (D0222i) -500 .. +500 mA
      
      case SENSOR_DIFFERENTIAL_VOLTAGE_m10M10V : return 1.616*normVm10M10(value)-0.0081; // Differential voltage sensor (D0210i) -10 .. +10 V.
    }     
    return Double.NaN;
  } 


  /**
   * Calculates the ADC output (0-4095) from a magnitude
   * @param magnitude Value in volts, mA, Nw,...etc. to convert 
   * @param sensorType An integer representing the sensor (see list of sensors)
   * @return the ADC output (0-4095) 
   */
  static private int magnitudeToADC(double magnitude,int sensorType){
    switch (sensorType){
      case SENSOR_GENERIC_SENSOR_m10M10V : return (int) ((4095/20)*(10-magnitude)); // Generic -10..+10 Volt 
      case SENSOR_GENERIC_SENSOR_05V :     return (int) ((4095/5)*magnitude);       // Generic 0..5 Volt

      case SENSOR_THERMOCOUPLE_0135i_m20_M110C :   return (int) (4095*(magnitude+26.33)/(29.093*5)); // Thermocouple (D0135i2)(-20..110 ºC)    
      case SENSOR_THERMOCOUPLE_0135i_m200_M1300C : return (int) (4095*(magnitude+221)/(316.9*5));    // Thermocouple (D0135i2)(-200..1300 ºC)       

      case SENSOR_FORCE_0663i_mM5N :  return (int) (4095*(magnitude-5.98)/(-2.45*5)); // Force sensor (D0663i) -5 N .. +5 N
      case SENSOR_FORCE_0663i_mM50N : return (int) (4095*(magnitude-61)/(-24.4*5));   // Force sensor (D0663i) -50 N .. +50 N
      
      case SENSOR_MAGNETIC_FIELD_024i_m10M50mT   : return (int) (4095*(magnitude+10)/(20*5));   // Magnetic Field sensor (D024i) -10 .. +50 mT
      case SENSOR_MAGNETIC_FIELD_024i_m100M500mT : return (int) (4095*(magnitude+100)/(200*5)); // Magnetic Field sensor (D024i) -100 .. +500 mT

      case SENSOR_CURRENT_m500M500mA : return (int) (4095*(10-(magnitude+0.47)/78.125)/20); //Current sensor (D0222i) -500 .. +500 mA

      case SENSOR_DIFFERENTIAL_VOLTAGE_m10M10V : return (int) (4095*(10-(magnitude+0.0081)/1.616)/20); // Differential voltage sensor (D0210i) -10 .. +10 V.
    }
    return -1;
  }

  
}

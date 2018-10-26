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

import com.phidgets.MotorControlPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;

/**
 * <p> Title: MotorControlHC </p>
 * <p>Description: A class to access The PhidgetMotorControl HC (1064). It allows  
 * to control the angular velocity and acceleration of up to two high-current DC motors,
 * as well as other devices like small solenoids, incandescent light bulbs, and hydraulic 
 * or pneumatic devices like small pumps and valves.</p>
 * @author Francisco Esquembre
 * @author Marco Marquez
 * @author Andres Mejias
 * @version June 2012
 * <table>
 * <tr> <th scope="col">Characteristic</th> <th scope="col">Value</th>    </tr>
 * <tr> <td>Output Controller Update Rate</td> <td>50 updates / second</td>     </tr>
 * <tr> <td>Velocity Resolution</td> <td>1.5%</td>  </tr>
 * <tr> <td>Acceleration Resolution</td> <td> 1.5% Velocity / Second^2</td>     </tr>
 * <tr> <td>Acceleration Limit (-100% to +100% velocity)</td> <td>120 ms</td>   </tr>
 * <tr> <td>PWM Frequency</td> <td>20 khz</td>          </tr>
 * <tr> <td>Minimum Power Supply Voltage</td> <td>6V</td>        </tr>
 * <tr> <td>Maximun Power Supply Voltage</td> <td>15V</td>        </tr>
 * <tr> <td>Continuous Motor Current</td> <td>14A</td> </tr>
 * <tr> <td>Peak Motor Current - 60 seconds</td> <td>19A</td>    </tr>
 * <tr> <td>Peak Motor Current - 10 seconds</td> <td>25A</td>    </tr>
 * <tr> <td>Peak Motor Current - 2 seconds</td> <td>32A</td>    </tr>
 * <tr> <td>Motor Overcurrent Trigger on Load < 100mΩ</td> <td>50A typical</td>          </tr>
 * <tr> <td>Device Active Current Consumption</td> <td>100 mA max</td>        </tr>
 * <tr> <td>Operating Temperature</td> <td>0 - 70°C</td> </tr>
 * </table>
 */
public class MotorControlHC extends AbstractPhidget {
    
  private MotorControlPhidget mDevice;

  protected Phidget createPhidget() throws PhidgetException {
    return mDevice = new MotorControlPhidget();
  }

  @Override
  public boolean close() {
    try { mDevice.setVelocity(0, 0); } catch (PhidgetException ex) { } // Do not complain
    try { mDevice.setVelocity(1, 0); } catch (PhidgetException ex) { } // Do not complain
    try { mDevice.setVelocity(2, 0); } catch (PhidgetException ex) { } // Do not complain
    try { mDevice.setVelocity(3, 0); } catch (PhidgetException ex) { } // Do not complain
    return super.close();
  }

  // -----------------------------------
  // Methods particular of this Phidget
  // -----------------------------------

  /**
   * Set the PWM value. It is the percentage of time the load (motor, light. etc.) is being powered for. 
   * and can be set between –100 and +100. If the output is connected to a motor, –100 corresponds 
   * to the motor being driven 100% of the time in reverse, +100 driven 100% of the time forward. 0 is off.
   * @param ind The output (0 or 1)
   * @param value PWM value
   * @return true if successful, false otherwise
   */
  public boolean setPowerPWM(int ind,double  value) {
    try { 
      mDevice.setVelocity(ind, value);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error allocating power");
      return false;
    }
  }

  /**
   * get actual PWM power (see SetPowerPWM) in an output
   * @param ind The output of the interface (0 or 1)
   * @return PWM power in <b>ind</b> output, Double.NaN if there was any error
   */
  public double getPowerPWM(int ind){
    try {
      return mDevice.getVelocity(ind);
    } catch (PhidgetException ex) {           
      if (isVerbose()) errorMessage("Reading error of output power");
      return Double.NaN;
    }
  }

  /**
   * Determines how fast a motor (a load) will be accelerated (powered) between given 
   * velocities (max and min ramping values). 
   * The valid range is between OutputRampingMax and OutputRampingMin.
   * This parameter is measured in percent, where 100% is the fastest output ramping
   * @param ind The output of the interface (0 or 1)
   * @param percentage Rate of change in applying the power output
   * @return true if successful, false otherwise
   */
  public boolean setOutputRamping(int ind,double  percentage){
    try {
      mDevice.setAcceleration(ind, percentage);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("SetRamping error");
      return false;
    }
  }

  /**
   * Get the actual output ramping (see setOutputRamping)
   * @param ind The output of the interface (0 or 1)
   * @return Output ramping (percent), Double.NaN if there was any error
   */
  public double getOutputRamping(int ind){
    try {
      return mDevice.getAcceleration(ind);
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("GetRamping error");
      return Double.NaN;                    
    }
  }

  /**
   * Returns the max value of output ramping
   * @param ind The output of the interface (0 or 1)
   * @return max output ramping (percent), Double.NaN if there was any error
   */
  public double getOutputRampingMax(int ind){
    try {
      return mDevice.getAccelerationMax(ind);
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("get max output ramping Error");
      return Double.NaN;                    
    }
  }

  /**
   * Returns the min value of output ramping
   * @param ind The output of the interface (0 or 1)
   * @return min output ramping (percent), Double.NaN if there was any error
   */
  public double getOutputRampingMin(int ind){
    try {
      return mDevice.getAccelerationMin(ind);
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("get min output ramping Error");
      return Double.NaN;                    
    }
  }

  /**
   * Returns Returns the current (Amps) being consumed by a load connected to the interface.
   * @param ind The output of the interface (0 or 1)
   * @return The current (Amps.), Double.NaN if there was any error
   */
  public double getCurrent(int ind){
    try {
      return mDevice.getCurrent(ind);
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("getCurrent Error");
      return Double.NaN;                    
    }
  }

  /**
   * Test main method
   * @param args
   */
  static public void main(String args[]) throws Exception {

    MotorControlHC  myPWM = new MotorControlHC();
    // My MotorControlHC (edit with your communication parameters)
    // myPWM.setup("192.168.1.8", 5001,164951,"12345");
    myPWM.connect(164860);
    myPWM.setPowerPWM(0, 0);
    myPWM.setOutputRamping(0, 100);
    System.out.println(" * * Phidgets MotorControlHC (output number 0) * *");
    System.out.println("output level (PWM): " + myPWM.getPowerPWM(0));
    System.out.println("ramping: " + myPWM.getOutputRamping(0));
    System.out.println("max ramping: " + myPWM.getOutputRampingMax(0));
    System.out.println("min ramping: " + myPWM.getOutputRampingMin(0));
    System.out.println("current: " + myPWM.getCurrent(0));



  }

}

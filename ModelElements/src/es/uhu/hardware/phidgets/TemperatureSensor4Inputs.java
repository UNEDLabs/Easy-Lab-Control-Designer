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

import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.TemperatureSensorPhidget;

/**
 * <p> Title: TemperatureSensor4Inputs </p>
 * <p>Description: A class to access The PhidgetTemperatureSensor 4-Input (1048_0). It allows  
 * to get temperature (in degree Celsius) from up to 4 thermocouples. 
 * Supports up to four J, K, E and T-type thermocouples. The insulating sheath that surrounds
 * the thermocouple is the limiting factor in the true temperature range. For
 * example, a K-type thermocouple insulated with teflon is good up to +200°C. 
 * The same thermocouple using fiberglass is good to 480°C.</p>
 * @author Francisco Esquembre
 * @author Marco Marquez
 * @author Andres Mejias
 * @version June 2012
 * <table>
 * <tr> <th scope="col">Thermocouple Type</th> <th scope="col">Temperature Range (ºC)</th>    </tr>
 * <tr> <td>K</td> <td>-200 to +1250</td></tr>
 * <tr> <td>J</td> <td>0 to +750</td></tr>
 * <tr> <td>T</td> <td>-200 to +350</td>     </tr>
 * <tr> <td>E</td> <td>-200 to +900</td>   </tr>
 * </table>
 */
public class TemperatureSensor4Inputs extends AbstractPhidget {
    
  private TemperatureSensorPhidget mDevice;

  protected Phidget createPhidget() throws PhidgetException {
    return mDevice = new TemperatureSensorPhidget();
  }

  // -----------------------------------
  // Methods particular of this Phidget
  // -----------------------------------

  /**
   * Returns the number of thermocouples present
   * @return int the number of thermocouples available, -1 if there was any error
   */
  public double getSensorsCount(){
    try {
      return mDevice.getTemperatureInputCount();
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting number of sensors");
      return -1;                    
    }
  }
  
  /**
   * Set the thermocouple type for an input. The possible values are J, K, E, and T, 
   * corresponding to K, E, J and T-Type Thermocouples. Support for other thermocouple types,
   * and voltage sources other than thermocouples in the valid range 
   * (between getPotentialMin and getPotentialMax) can be achieved using getPotential.
   * @param index input number (0 to 3)
   * @param value 'K', 'J', 'E', 'T'
   */
  public boolean setThermocoupleType(int index, char value) {
    int type = 1;
    switch(value) {
      case 'k' : case 'K' : type = 1; break;
      case 'j' : case 'J' : type = 2; break;
      case 'e' : case 'E' : type = 3; break;
      case 't' : case 'T' : type = 4; break;
      default : return false; 
    }
    try {
      mDevice.setThermocoupleType(index, type);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error setting thermocoulpe type at index "+index);
      return false;
    }
  }  

  /**
   * Get the thermocouple type for an input.   
   * @param index input number (0 to 3)
   * @return thermocouple type (one of 'K', 'J', 'E', 'T'. Will return '?' if there is any error.)
   */
  public char getThermocoupleType(int index){
    try {           
      switch(mDevice.getThermocoupleType(index)) {
        case 1: return 'K';
        case 2: return 'J';
        case 3: return 'E';
        case 4: return 'T';
      }      
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting thermocoulpe type at index "+index);
    }
    return '?';
  }

  /**
   * Get the temperature measured by a thermocouple at the given index
   * @param index The index of the thermocuple (0 to 3)
   * @return temperature in degrees Celsius, Double.NaN if there was any error
   */
  public double getTemperature(int index){
    try {
      return mDevice.getTemperature(index);
    } catch (PhidgetException ex) {           
      if (isVerbose()) errorMessage("Error reading temperature from sensor number "+index);
      return Double.NaN;
    }
  }

  /**
   * Returns the minimum temperature provided by the type of sensor at the given index
   * @param index The index of the thermocuple (0 to 3)
   * @return the temperature in degrees Celsius, Double.NaN if there was any error
   */
  public double getMinimumTemperature(int index){
    try {
      return mDevice.getTemperatureMin(index);
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading minimum temperature from sensor number "+index);
      return Double.NaN;                    
    }
  }

  /**
   * Returns the maximum temperature provided by the type of sensor at the given index
   * @param index The index of the thermocuple (0 to 3)
   * @return the temperature in degrees Celsius, Double.NaN if there was any error
   */
  public double getMaximumTemperature(int index){
    try {
      return mDevice.getTemperatureMax(index);
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading minimum temperature from sensor number "+index);
      return Double.NaN;                    
    }
  }

  /**
   * Returns the potential of a thermocouple input. 
   * This value is returned in millivolts, and will always be between
   * getMinimumPotential and getMaximumPotential. This is the value that is internally used to calculate temperature
   * @param index input number (0 to 3)
   * @return potential in millivolts. Double.Nan if there is any error
   */
  public double getPotential (int index) {
    try {           
      return mDevice.getTemperature(index);            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading potential from sensor number "+index);
      return Double.NaN;
    }
  }
  
  /**
   * Returns the minimum voltage that can be measured by the 1048_0.
   * @param index input number (0 to 3)
   * @return minimum voltage that can be measured by the interface. Double.Nan if there is any error 
   */ 
  public double getMinimumPotential (int index) {
    try {           
      return mDevice.getPotentialMin(index);            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading minimum potential from sensor number "+index);
      return Double.NaN;
    }
  }

  /**
   * Returns the maximum voltage that can be measured by the 1048_0.
   * @param index input number (0 to 3)
   * @return maximum voltage that can be measured by the interface. Double.Nan if there is any error 
   */ 
  public double getMaximumPotential (int index) {
    try {           
      return mDevice.getPotentialMax(index);            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading maximum potential from sensor number "+index);
      return Double.NaN;
    }
  }
  
  /**
   * Returns the temperature of the 1048 board, measured near the thermocouple 
   * connector. This temperature is used as a reference for the thermocouple voltage. 
   * This value will always be between getAmbientTemperatureMin and getAmbientTemperatureMax.
   * @return Ambient temperature (in degrees Celsius) 
   */
  public double getAmbientTemperature (){
    try {           
      return mDevice.getAmbientTemperature();            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading ambient temperature from sensor number");
      return Double.NaN;
    }
  }

  /**
   * Returns the minimum temperature that can be returned by the ambient sensor.
   * @return temperature (in degrees Celsius) 
   */
  public double getMinimumAmbientTemperature (){
    try {           
      return mDevice.getAmbientTemperatureMin();            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error reading minimum ambient temperature from sensor number");
      return 0;
    }
  }

  /**
   * Returns the maximum temperature that can be returned by the ambient sensor.
   * @return temperature (in degrees Celsius)  
   */
  public double getMaximumAmbientTemperature (){
      try {           
          return mDevice.getAmbientTemperatureMax();            
      } catch (PhidgetException ex) {
        if (isVerbose()) errorMessage("Error reading maximum ambient temperature from sensor number");
          return Double.NaN;
      }
  }
  
  
}

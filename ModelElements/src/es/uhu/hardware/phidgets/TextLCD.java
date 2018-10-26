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

import com.phidgets.PhidgetException;
import com.phidgets.Phidget;
import com.phidgets.TextLCDPhidget;

/**
 * <p> Title: TextLCD </p>
 * <p>Description: A class to access PhidgetTextLCD devices, like the 1203.
 * This class only allows access to the LCD funtionality.
 * </p>
 * @author Francisco Esquembre
 * @author Marco Marquez
 * @author Andres Mejias
 * @version June 2012
 * <br>
*/
public class TextLCD extends AbstractPhidget {
  private TextLCDPhidget lcd;

  protected Phidget createPhidget() throws PhidgetException {
      return lcd = new TextLCDPhidget();
  }
  

  // -----------------------------------
  // Methods particular of this Phidget
  // -----------------------------------
  
  /**
   * Returns the number of rows available on the display.
   * @return number of rows
   */
  public int getRowCount(){
    try {           
      return lcd.getRowCount();            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting number of rows");
      return -1;
    }
  }
  
  /**
   * Returns the number of columns (characters per row) available on the display.
   * @return number of columns
   */
  public int getColumnCount(){
    try {           
      return lcd.getColumnCount();            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting number of columns");
      return -1;
    }
  }
  
  /**
   * Sets the status of the backlight. The backlight is by default turned on. 
   * @param value True turns the backlight on, False turns it off.
   * @return true if successful, false otherwise
   */
  public boolean setBacklight(boolean value){
    try {
      lcd.setBacklight(value);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error setting backlight level");
      return false;
    }
  }

  /**
   * Returns the status of the backlight. The backlight is by default turned on. 
   * @return True indicated that the backlight is on, False indicated that it is off.
   */
  public boolean getBacklight(){
    try {           
      return lcd.getBacklight();            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting backlight level");
      return false;
    }
  }


  /**
   * Sets the brightness of the display. Changing the brightness can increase the readability of the display 
   * in certain viewing situation, such as at an odd angle. Not all TextLCDs support this method. 
   * @param value The valid range is 0-255.
   * @return true if successful, false otherwise
   */
  public boolean setBrightness(int value){
    try {
      lcd.setBrightness(value);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error setting brightness");
      return false;
    }
  }

  /**
   * Returns the brightness of the display. This is the brightness of the backlight. 
   * Not all TextLCDs support this method. 
   * @return brightness of the display (0-255)
   */
  public int getBrightness(){
    try {           
      return lcd.getBrightness();            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting brightness");
      return -1;
    }
  }

  /**
   * Sets the contrast of the display. Changing the contrast can increase the readability 
   * of the display in certain viewing situation, such as at an odd angle. 
   * @param value The valid range is 0-255.
   * @return true if successful, false otherwise
   */
  public boolean setContrast(int value){
    try {
      lcd.setContrast(value);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error setting contrast");
      return false;
    }
  }  

  /**
   * Returns the contrast of the display.   
   * @return contrast of the display (0-255)
   */
  public int getContrast(){
    try {           
      return lcd.getContrast();            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting contrast");
      return -1;
    }
  }

  /**
   * Sets the state of the cursor. True indicates that the cursor on, False indicates that it is off. 
   * The cursor is an underscore which appears directly to the right of the last entered character on the display. 
   * The cursor is by default disabled. 
   * @param value true (cursor on) or false
   * @return true if successful, false otherwise
   */
  public boolean setCursor(boolean  value){
    try {
      lcd.setCursor(value);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error setting cursor");
      return false;
    }
  }  

  /**
   * Returns the status of the cursor.
   * @return state of the cursor
   */
  public boolean getCursor(){
    try {           
      return lcd.getCursor();            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting cursor");
      return false;
    }
  }

  /**
   * Sets the state of the cursor blink. True indicates that the cursor blink is on, False indicates that it is off. 
   * The cursor blink is an flashing box which appears directly to the right of the last entered character on the display, 
   * in the same spot as the cursor if it is enabled. The cursor blink is by default disabled. 
   * @param value true (blinking on) or false
   * @return true if successful, false otherwise
   */
  public boolean setCursorBlink(boolean  value){
    try {
      lcd.setCursorBlink(value);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error setting cursor blinking");
      return false;
    }
  }  

  /**
   * Returns the status of the cursor blink.
   * @return status of the cursor blink (true or false)
   */
  public boolean getCursorBlink(){
    try {           
      return lcd.getCursorBlink();            
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error getting cursor blinking");
      return false;
    }
  }

  /**
   * Sets the display string of a certain row. 
   * @param row Row
   * @param str String
   * @return true if successful, false otherwise
   */
  public boolean setDisplayString(int row,String str){
    try {
      lcd.setDisplayString(row, str);
      return true;
    } catch (PhidgetException ex) {
      if (isVerbose()) errorMessage("Error setting display string");
      return false;
    }
  }

  /** 
   * Sets the character to display at a certain row and column. 
   * @param rowIndex Row
   * @param columIndex Column
   * @param ch character
   * @return true if successful, false otherwise
   */
//  public boolean setDisplayCharacter(int rowIndex, int columIndex,char  ch){
//    try {
//      lcd.setDisplayCharacter(rowIndex,columIndex, ch);
//      return true;
//    } catch (PhidgetException ex) {
//      if (isVerbose()) errorMessage("Error setting display character");
//      return false;
//    }
//  }

  /**
   * Test main method
   * @param args
   * @throws Exception
   */
  public static final void main(String args[]) throws Exception {
     
    int nCol, nRow;
    TextLCD ikLCD =new TextLCD();
    ikLCD.connect(120672);// edit with your serial number
    // testing LCD
    nCol = ikLCD.getColumnCount();
    nRow = ikLCD.getRowCount();
    ikLCD.setContrast(100);
    ikLCD.setBrightness(50);
    ikLCD.setCursor(true);
    ikLCD.setCursorBlink(true);
    ikLCD.setDisplayString(0, "Hello from EJS...");
    ikLCD.setDisplayString(1, "Cols: "+ nCol+", rows: " + nRow);
    ikLCD.setBacklight(true);
  }
}

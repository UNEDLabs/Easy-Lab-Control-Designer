/*
 * Copyright (C) 2014 Francisco Esquembre / Andres Mejias   
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
 */
package es.uhu.hardware.mindwave;

import java.net.ConnectException;

/**
 * This EJS element allows you to control the NeuroSky MindWave through the
 * ThinkGear connector (via socket). You will be able to:
 * <ul>
 * <li>read attention level</li>
 * <li>read meditation level</li>
 * <li>read blink strength</li>
 * <li>read EEG data</li>
 * <li>read signal level
 * <li>detect double blinks with programmable delay between the two single blinks</li>
 * </ul>
 * 
 * @author Andres Mejias
 * @author Francisco Esquembre
 * @version 1.0 March 2014
 */
public class MindWave extends ThinkGearSocket {

	//
	// Variables to hold the Mindwave data
	//
	private int attention = 0;
	private int meditation = 0;
	private int blink = 0;
	private int signal = 200;
	private int deltaValue = 0;
	private int thetaValue = 0;
	private int low_alphaValue = 0;
	private int high_alphaValue = 0;
	private int low_betaValue = 0;
	private int high_betaValue = 0;
	private int low_gammaValue = 0;
	private int high_gammaValue = 0;
	private int[] rawData = new int[512]; 
	//
	// Variables to detect double blink
	//
	private long currentBlinkTime = 1000000;
	private long previousBlinkTime = 0;
	private long distanceBlink = 1000000;

	/*
	 * Reads the signal level
	 */
	public void poorSignalEvent(int sig) {
		signal = sig;
	}

	/**
	 * Returns the signal level [0, 200]. The greater the value, the more noise
	 * is detected in the signal. 200 is a special value that means that the
	 * ThinkGear contacts are not touching the skin.
	 * 
	 * @return the signal level (from 0 to 200).
	 */
	public int getSignalLevel() {
		return signal;
	}

	/*
	 * Reads the attention level. This is an eSense attribute from NeuroSky.
	 */
	 public void attentionEvent(int attentionLevel) {
		attention = attentionLevel;
	}

	/**
	 * Returns the current attention level [0, 100]. Values in [1, 20] are
	 * considered strongly lowered. Values in [20, 40] are considered reduced
	 * levels. Values in [40, 60] are considered neutral. Values in [60, 80] are
	 * considered slightly elevated. Values in [80, 100] are considered
	 * elevated.
	 * @return the attention level [0-100].
	 */
	public int getAttentionLevel() {
		return attention;
	}

	/*
	 * Reads the meditation level. This is a eSense attribute from NeuroSky.
	 */
	public void meditationEvent(int meditationLevel) {
		meditation = meditationLevel;
	}

	/**
	 * Returns the current meditation level [0, 100]. Values in [1, 20] are
	 * considered strongly lowered. Values in [20, 40] are considered reduced
	 * levels. Values in [40, 60] are considered neutral. Values in [60, 80] are
	 * considered slightly elevated. Values in [80, 100] are considered
	 * elevated.
	 * @return the meditation level [0-100].
	 */
	public int getMeditationLevel() {
		return meditation;
	}

	/*
	 * Reads the strength of a detected blink.
	 */
	public void blinkEvent(int blinkStrength) {
		blink = blinkStrength;
		previousBlinkTime = currentBlinkTime;
		currentBlinkTime = System.currentTimeMillis();
		//System.out.println("Previous blink time: " + previousBlinkTime);
		//System.out.println("Current blink time: " + currentBlinkTime);
		distanceBlink = currentBlinkTime - previousBlinkTime;
		//System.out.println("Blink distance: " + distanceBlink);
	}

	/**
	 * Returns the strength of a detected blink. This is an integer in the range
	 * of 0-255.
	 * @return the strength of a blink, in the range 0-255.
	 */
	public int getBlinkLevel() {
		int lastBlink = blink;
		blink = 0;
		return lastBlink;
	}

	/**
	 * Returns if a double blink with a duration between two values has been detected.
	 * @param minDistance (min time interval) in milliseconds
	 * @param maxDistance (max time interval) in milliseconds
	 * @return true if successful
	 */
	public boolean getDoubleBlink(int minDistance, int maxDistance) {
		if ((distanceBlink >= minDistance) & (distanceBlink <= maxDistance)) {
			// Resets Blink times and sets a big blink distance to start the detection again
			previousBlinkTime = 0;
			currentBlinkTime = 1000000;
			distanceBlink = 1000000;
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Reads the EEG powers.
	 */
	public void eegEvent(int delta, int theta, int low_alpha, int high_alpha,
			int low_beta, int high_beta, int low_gamma, int high_gamma) {
		deltaValue = delta;
		thetaValue = theta;
		low_alphaValue = low_alpha;
		high_alphaValue = high_alpha;
		low_betaValue = low_beta;
		high_betaValue = high_beta;
		low_gammaValue = low_gamma;
		high_gammaValue = high_gamma;
	}

	/**
	 * Returns the EEG data. The values have no units. delta: the "delta" band
	 * of EEG. theta: the "theta" band of EEG. lowAlpha: the "low alpha" band of
	 * EEG. highAlpha: the "high alpha" band of EEG. lowBeta: the "low beta"
	 * band of EEG. highBeta: the "high beta" band of EEG. lowGamma: the
	 * "low gamma" band of EEG. highGamma: the "high gamma" band of EEG.
	 * 
	 * @return an array with the EEG data (int)
	 */
	public int[] getEEGValue() {
		int[] eegValues = new int[8];
		eegValues[0] = deltaValue;
		eegValues[1] = thetaValue;
		eegValues[2] = low_alphaValue;
		eegValues[3] = high_alphaValue;
		eegValues[4] = low_betaValue;
		eegValues[5] = high_betaValue;
		eegValues[6] = low_gammaValue;
		eegValues[7] = high_gammaValue;
		return eegValues;
	}

	/**
	 * Returns the delta band of EEG *
	 * @return the delta band value.
	 */
	public int getDelta() {
		return deltaValue;
	}

	/**
	 * Returns the theta band of EEG 
	 * @return the theta band value.
	 */
	public int getTheta() {
		return thetaValue;
	}

	/**
	 * Returns the low alpha band of EEG *
	 * @return the low alpha band value.
	 */
	public int getLowAplpha() {
		return low_alphaValue;
	}

	/**
	 * Returns the high alpha band of EEG 
	 * @return the high alpha band value.
	 */
	public int getHighAlpha() {
		return high_alphaValue;
	}

	/**
	 * Returns the low beta band of EEG 
	 * @return the low beta band value.
	 */
	public int getLowBeta() {
		return low_betaValue;
	}

	/**
	 * Returns the high beta band of EEG 
	 * @return the high beta band value.
	 */
	public int getHighBeta() {
		return high_betaValue;
	}

	/**
	 * Returns the low gamma band of EEG 
	 * @return the low gamma band value.
	 */
	public int getLowGamma() {
		return low_gammaValue;
	}

	/**
	 * Returns the high gamma band of EEG 
	 * @return the high gamma band value.
	 */
	public int getHighGamma() {
		return high_gammaValue;
	}
    
	/*
	 * Raw data event from headset.
	 */
	public void rawEvent(int[] raw) {
		rawData = raw;
	}
	
	/**
	 * Returns the raw data from the MindWave Sensor
	 * @return the high gamma band value.
	 */
	public int[] getRawData(){
		return rawData;
	}
	
	/**
	 * Starts the connection to Neurosky Mindwave
	 * @return true if successful, false if there was any connection error
	 */
	public boolean connect() {
		 try {
			this.start();
			return true;
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	 }
	
	/**
	   * Closes the connection to the MindWave headset
	   * @return true if successful, false if there was any connection error
	   */
	  public boolean close() {
	    try {
	       this.stop();
	       return true;
	      }
	      catch (Exception exc) {
	      exc.printStackTrace();
	      return false;
	    }
	  }
	  
	public static void main(String[] args) {
		MindWave neuroSocket = new MindWave();
		neuroSocket.connect();
		//
		// Some examples
		do {
			System.out.println("Attention level: " + neuroSocket.getAttentionLevel());
			System.out.println("MeditationLevel: " + neuroSocket.getMeditationLevel());
			System.out.println("Blink level: " + neuroSocket.getBlinkLevel());
			System.out.println("signal level: " + neuroSocket.getSignalLevel());
			System.out.println("Delta Value: " + neuroSocket.getDelta());
			System.out.println("---------------------------------------------------------");
			System.out.println("Double blink with interval 300 - 1000?: " + neuroSocket.getDoubleBlink(300, 1000));
			System.out.println("------***********************************------");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  } while (true);
	}

}

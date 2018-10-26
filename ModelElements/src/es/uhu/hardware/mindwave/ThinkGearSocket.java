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
 * This is a modified version of the thinkgear library by Andreas Borg (http://crea.tion.to/processing/thinkgear-java-socket)
 * 
 * This provides a simple socket connector to the NeuroSky MindWave ThinkGear connector.
 */

package es.uhu.hardware.mindwave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * 
 * Data is passed back to the application via the following callback methods:
 * 
 * 
 * public void attentionEvent(int attentionLevel)
 * Returns the current attention level [0, 100].
 * Values in [1, 20] are considered strongly lowered.
 * Values in [20, 40] are considered reduced levels.
 * Values in [40, 60] are considered neutral.
 * Values in [60, 80] are considered slightly elevated.
 * Values in [80, 100] are considered elevated.
 * 
 * public void meditationEvent(int meditationLevel)
 * Returns the current meditation level [0, 100].
 * The interpretation of the values is the same as for the attentionLevel.
 * 
 * 
 * public void poorSignalEvent(int signalLevel)
 * Returns the signal level [0, 200]. The greater the value, the more noise is detected in the signal.
 * 200 is a special value  that means that the ThinkGear contacts are not touching the skin.
 * 
 * 
 * public void eegEvent(int delta, int theta, int low_alpha, int high_alpha, int low_beta, int high_beta, int low_gamma, int mid_gamma) </code><br>
 * Returns the EEG data. The values have no units.
 * 
 * 
 * public void rawEvent(int [])
 * Returns the the current 512 raw signal samples [-32768, 32767]. 
 * 
 * 
 */
public class ThinkGearSocket implements Runnable {

	public Socket neuroSocket;
	public OutputStream outStream;
	public InputStream inStream;
	public BufferedReader stdIn;
	private Method attentionEventMethod = null;
	private Method meditationEventMethod = null;
	private Method poorSignalEventMethod = null;
	private Method blinkEventMethod = null;
	private Method eegEventMethod = null;
	private Method rawEventMethod = null;
	public String appName = "";
	public String appKey = "";
	private Thread t;

	private int raw[] = new int[512];
	private int index = 0;
	public final static String VERSION = "1.0";
	private boolean running = true;

	public ThinkGearSocket() {
		
		try {
			attentionEventMethod = this.getClass().getMethod("attentionEvent",
					new Class[] { int.class });

		} catch (Exception e) {
			System.err.println("attentionEvent() method not defined. ");
		}

		try {
			meditationEventMethod = this.getClass().getMethod(
					"meditationEvent", new Class[] { int.class });
		} catch (Exception e) {
			System.err.println("meditationEvent() method not defined. ");
		}
		try {
			poorSignalEventMethod = this.getClass().getMethod(
					"poorSignalEvent", new Class[] { int.class });
		} catch (Exception e) {
			System.err.println("poorSignalEvent() method not defined. ");
		}

		try {
			blinkEventMethod = this.getClass().getMethod("blinkEvent",
					new Class[] { int.class });
		} catch (Exception e) {
			System.err.println("blinkEvent() method not defined. ");
		}
		try {
			eegEventMethod = this.getClass().getMethod(
					"eegEvent",
					new Class[] { int.class, int.class, int.class, int.class,
							int.class, int.class, int.class, int.class });
		} catch (Exception e) {
			System.err.println("eegEvent() method not defined. ");
		}

		try {
			rawEventMethod = this.getClass().getMethod("rawEvent",
					new Class[] { int[].class });
		} catch (Exception e) {
			System.err.println("rawEvent() method not defined. ");
		}
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	public void start() throws ConnectException {

		try {
			neuroSocket = new Socket("127.0.0.1", 13854);
			System.out.println("ThinkGear running");
		} catch (ConnectException e) {
			System.out.println("Is ThinkGear running?");
			running = false;
			throw e;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			inStream = neuroSocket.getInputStream();
			outStream = neuroSocket.getOutputStream();
			stdIn = new BufferedReader(new InputStreamReader(
					neuroSocket.getInputStream()));
			running = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (appName != "" && appKey != "") {
			JSONObject appAuth = new JSONObject();
			try {
				appAuth.put("appName", appName);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				appAuth.put("appKey", appKey);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// throws some error
			sendMessage(appAuth.toString());
			System.out.println("appAuth" + appAuth);
		}

		JSONObject format = new JSONObject();
		try {
			format.put("enableRawOutput", true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("raw error");
			e.printStackTrace();
		}
		try {
			format.put("format", "Json");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("Json error");
			e.printStackTrace();
		}
		// System.out.println("format "+format);
		sendMessage(format.toString());
		t = new Thread(this);
		t.start();

	}

	public void stop() {

		if (running) {
			t.interrupt();
			try {
				neuroSocket.close();
				inStream.close();
				outStream.close();
				stdIn.close();
				stdIn = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// System.out.println("Socket close issue");
			}
		}
		running = false;
	}

	public void sendMessage(String msg) {
		PrintWriter out = new PrintWriter(outStream, true);
		// System.out.println("sendmsg");
		out.println(msg);
	}

	public void run() {
		if (running && neuroSocket.isConnected()) {
			String userInput;

			try {
				while ((userInput = stdIn.readLine()) != null) {

					String[] packets = userInput.split("/\r/");
					for (int s = 0; s < packets.length; s++) {
						if (((String) packets[s]).indexOf("{") > -1) {
							JSONObject obj = new JSONObject((String) packets[s]);
							parsePacket(obj);
						}

						// String name = obj.get("name").toString();
					}

				}
			} catch (SocketException e) {
				// System.out.println("For some reason stdIn throws error even if closed");
				// maybe it takes a cycle to close properly?
				// e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				// TODO Auto-generated catch block
			}
		} else {
			running = false;
		}
	}

	private void triggerAttentionEvent(int attentionLevel) {
		if (attentionEventMethod != null) {
			try {
				attentionEventMethod.invoke(this,
						new Object[] { attentionLevel });
			} catch (Exception e) {
				System.err
						.println("Disabling attentionEvent()  because of an error.");
				e.printStackTrace();
				attentionEventMethod = null;
			}
		}
	}

	private void triggerMeditationEvent(int meditationLevel) {
		if (meditationEventMethod != null) {
			try {
				meditationEventMethod.invoke(this,
						new Object[] { meditationLevel });
				
			} catch (Exception e) {
				System.err
						.println("Disabling meditationEvent()  because of an error.");
				e.printStackTrace();
				meditationEventMethod = null;
			}
		}
	}

	private void triggerPoorSignalEvent(int poorSignalLevel) {
		if (poorSignalEventMethod != null) {
			try {
				poorSignalEventMethod.invoke(this,
						new Object[] { poorSignalLevel });
				
			} catch (Exception e) {
				System.err
						.println("Disabling meditationEvent()  because of an error.");
				e.printStackTrace();
				poorSignalEventMethod = null;
			}
		}
	}

	private void triggerBlinkEvent(int blinkStrength) {
		if (blinkEventMethod != null) {
			try {
				blinkEventMethod.invoke(this, new Object[] { blinkStrength });
			} catch (Exception e) {
				System.err
						.println("Disabling blinkEvent()  because of an error.");
				e.printStackTrace();
				blinkEventMethod = null;
			}
		}
	}

	private void triggerEEGEvent(int delta, int theta, int low_alpha,
			int high_alpha, int low_beta, int high_beta, int low_gamma,
			int mid_gamma) {
		if (eegEventMethod != null) {
			try {
				eegEventMethod.invoke(this, new Object[] { delta, theta,
						low_alpha, high_alpha, low_beta, high_beta, low_gamma,
						mid_gamma });
			} catch (Exception e) {
				System.err
						.println("Disabling eegEvent()  because of an error.");
				e.printStackTrace();
				eegEventMethod = null;
			}
		}
	}

	private void triggerRawEvent(int[] values) {
		if (rawEventMethod != null) {
			try {
				rawEventMethod.invoke(this, new Object[] { values });
			} catch (Exception e) {
				System.err
						.println("Disabling rawEvent()  because of an error.");
				e.printStackTrace();
				rawEventMethod = null;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void parsePacket(JSONObject data) {
		Iterator itr = data.keys();
		while (itr.hasNext()) {

			Object e = itr.next();
			String key = e.toString();

			try {

				if (key.matches("poorSignalLevel")) {
					triggerPoorSignalEvent(data.getInt(e.toString()));

				}
				if (key.matches("rawEeg")) {
					int rawValue = (Integer) data.get("rawEeg");
					raw[index] = rawValue;
					index++;
					if (index == 512) {
						index = 0;
						int rawCopy[] = new int[512];
						// parent.arrayCopy(raw, rawCopy);
						//
						System.arraycopy(raw, 0, rawCopy, 0, raw.length);
						//
						//
						triggerRawEvent(rawCopy);
					}
				}
				if (key.matches("blinkStrength")) {
					triggerBlinkEvent(data.getInt(e.toString()));

				}

				if (key.matches("eSense")) {
					JSONObject esense = data.getJSONObject("eSense");
					triggerAttentionEvent(esense.getInt("attention"));
					triggerMeditationEvent(esense.getInt("meditation"));

				}
				if (key.matches("eegPower")) {
					JSONObject eegPower = data.getJSONObject("eegPower");
					triggerEEGEvent(eegPower.getInt("delta"),
							eegPower.getInt("theta"),
							eegPower.getInt("lowAlpha"),
							eegPower.getInt("highAlpha"),
							eegPower.getInt("lowBeta"),
							eegPower.getInt("highBeta"),
							eegPower.getInt("lowGamma"),
							eegPower.getInt("highGamma"));

					// System.out.println(key);
				}

			} catch (Exception ex) {

				ex.printStackTrace();
			}
		}

		//
	}
}

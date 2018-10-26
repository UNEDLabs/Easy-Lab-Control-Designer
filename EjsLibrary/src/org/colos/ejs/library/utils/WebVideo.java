package org.colos.ejs.library.utils;

import java.util.ArrayList;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

/**
 * WebVideo obtains an image from a web cam and passes it to a listener
 * Adapted from code by Hector Vargas, UNED - 2006.
 * @author Francisco Esquembre May 2012
 */
public class WebVideo {
    
  private boolean mEnabled = false;
  private boolean mMPEGformat = false;
  private long mDelay = 100;
  private String mURL = null;
  private Thread mRunningThread;
  private WebVideoMPEGutil mMPEGutil = null;
  private ArrayList<WebVideoListener> mListeners = new ArrayList<WebVideoListener>();

  // -----------------------------------
  // Configuration
  // -----------------------------------
  
  public boolean addListener(WebVideoListener listener) {
    if (!mListeners.contains(listener)) return mListeners.add(listener);
    return false;
  }
  
  public String getURL() { return mURL; }

  public void setURL(String url) { 
    if (url==null || !url.equals(mURL)) stopRunning();
    mURL = url;
    startRunning();
  }

  public boolean isMJPEGFormat() { return mMPEGformat; }

  public void setMJPEGFormat(boolean mpegFormat) {
    if (mMPEGformat!=mpegFormat) {
      stopRunning();
      mMPEGformat = mpegFormat;
      startRunning();
    }
  }

  public long getDelay() { return mDelay; }

  public void setDelay(long delay) { mDelay = Math.max(0, delay); }

  public boolean isEnabled () { return mEnabled; }

  public void setEnabled(boolean enabled) { 
    this.mEnabled = enabled;
    if (mEnabled) startRunning();
    else stopRunning();
  }

  // -----------------------------------
  // Configuration
  // -----------------------------------
  
  void sendErrorMessage(String message) {
    for (WebVideoListener listener : mListeners) listener.connectionError(message);
  }
  
  void sendImage(Image image) {
    for (WebVideoListener listener : mListeners) listener.imageChanged(image);
  }
  
  // -----------------------------------
  // Standard JPEG format
  // -----------------------------------
  
  private void startRunning() {
    if (!mEnabled) return;
    if (mURL==null) return;
    if (mRunningThread!=null) return; // thread is already running
    if (mMPEGformat) {
      if (mMPEGutil==null) mMPEGutil = new WebVideoMPEGutil(this);
      mRunningThread = new Thread(new Runnable() {
        public void run() { mMPEGutil.readMJPGStream(); }
      });
    }
    else mRunningThread = new Thread(new Runnable() {
      public void run() { readJPEG(); }
    });
    // Standard JPEG
    mRunningThread.setPriority(Thread.MIN_PRIORITY);
    mRunningThread.setDaemon(true);
    mRunningThread.start();
  }

  public void stopRunning() {
    if (mRunningThread==null) {
      sendImage(null);
      return; // thread is not running
    }
    if (mMPEGutil!=null) mMPEGutil.stop();
    Thread tempThread = mRunningThread; // local reference
    mRunningThread = null;
    if (Thread.currentThread()==tempThread) {
      sendImage(null);
      return; // cannot join with own thread so return
    }
    try {                      // guard against an exception in applet mode
      tempThread.interrupt(); // get out of a sleep state
      tempThread.join(100);  // wait up to 1 second for animation thread to stop
    } catch(Exception e) {
      // System.out.println("excetpion in stop animation"+e);
    }
    sendImage(null);
  }

  public void readJPEG() { // Standard JPEG camera
//    System.out.println ("Running loop");
    DataInputStream dis = null;
    HttpURLConnection huc=null;
    String errorMessage=null;
    URL url=null;
    try { url = new URL(mURL); } 
    catch (Exception mue) { 
      sendErrorMessage("Incorrect URL address");
      stopRunning();
      return;
    }

    // JPEG camera
    while (mRunningThread==Thread.currentThread()) {
      try {
        huc = (HttpURLConnection) url.openConnection();
        InputStream is = huc.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        dis = new DataInputStream(bis);
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(dis);
        Image image = decoder.decodeAsBufferedImage();
        System.err.println("Image read ok "+image);
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        System.out.println ("Image = "+imageWidth+","+imageHeight);
        sendImage(image);
        if (mDelay<10) Thread.yield();
        else {
          try { Thread.sleep(mDelay); } 
          catch(InterruptedException ie) {}
        }
        dis.close();
      }
      catch (IOException e) { errorMessage = "Connection failed"; } 
      catch (Exception exc) { 
        exc.printStackTrace();
        errorMessage = "Error when reading image from camera";
      }
      finally {
        try {
          huc.disconnect();
          dis.close();
        }
        catch (Exception fExc) {}
      }
      if (errorMessage!=null) {
        sendErrorMessage(errorMessage);
        stopRunning();
        return;
      }
    }
//    System.out.println ("End of running loop");
    sendImage(null);
  }

}

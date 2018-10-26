
package com.charliemouse.cambozola;

import java.net.*;
import com.sun.image.codec.jpeg.*;
import java.io.*;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;


public class CamImage extends Thread {

  private ExceptionReporter m_reporter = null;
  private Vector<ImageChangeListener> m_listeners;
  private boolean m_isDefunct = false;
  private Object m_lock = new Object();
  private Image m_imain = null;
  //	private URL m_stream;
  //	private byte[] m_rawImage;
  private int m_retryCount = 1;
  private int m_retryDelay = 1000;
  private String m_mainURL = null;
  private DataInputStream dis = null;
  private boolean m_connected = false;
  private HttpURLConnection huc = null;
  private int m_delayImage;



  public CamImage(URL url,  int retryCount, int retryDelay, int imageDelay, ExceptionReporter reporter)	{

    m_delayImage = imageDelay;

    m_mainURL = url.toString();
    m_listeners = new Vector<ImageChangeListener>();
    m_isDefunct = false;
    m_retryCount = retryCount;
    m_retryDelay = retryDelay;
  }


  public Image getCurrent()
  {
    synchronized (m_lock) {
      return m_imain;
    }
  }




  public void addImageChangeListener(ImageChangeListener cl)
  {
    m_listeners.addElement(cl);
  }


  public void removeImageChangeListener(ImageChangeListener cl)
  {
    m_listeners.removeElement(cl);
  }



  private void fireImageChange()
  {
    ImageChangeEvent ce = new ImageChangeEvent(this);
    for (Enumeration<ImageChangeListener> e = m_listeners.elements(); e.hasMoreElements();) e.nextElement().imageChanged(ce);
  }


  public void run()
  {
    try {
      do {
        connect();
        readJPG();
        disconnect();
        sleep (m_delayImage);
      } while (!m_isDefunct);
    } catch (Exception e) {
      m_reporter.reportError(e);
    } finally {
      unhook();
    }
  }



  public void connect(){

    try {
      int tryIndex = 0;
      int retryCount = m_retryCount;
      int retryDelay = m_retryDelay;
      String connectionError = null;
      do {
        tryIndex++;
        URL u = new URL(m_mainURL);
        huc = (HttpURLConnection) u.openConnection();
        InputStream is = huc.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        dis= new DataInputStream(bis);
        m_connected = true;

        if (!m_connected) {
          connectionError = "Failed to connect to server (denied?)";
          m_reporter.reportFailure(connectionError);
          huc.disconnect();
          sleep(retryDelay);
        }

      } while (!m_connected & tryIndex < retryCount);
    } catch (SocketException e) {
      if (!m_isDefunct) {
        m_reporter.reportError(e);
      }
    } catch (Exception e) {
      m_reporter.reportError(e);
    }

  }



  public void disconnect(){
    try{
      if(m_connected){
        dis.close();
        m_connected = false;
      }
    }catch(Exception e){;}
  }




  public void readJPG(){
    try{
      synchronized (m_lock) {
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(dis);
        m_imain = decoder.decodeAsBufferedImage();
      }
      fireImageChange();
    } catch(Exception e) { 
      //e.printStackTrace();
      disconnect();
    }
  }



  public void readLine(int n, DataInputStream _dis){ //used to strip out the header lines
    for (int i=0; i<n;i++){
      readLine(_dis);
    }
  }


  public void readLine(DataInputStream _dis){
    try{
      boolean end = false;
      String lineEnd = "\n"; //assumes that the end of the line is marked with this
      byte[] lineEndBytes = lineEnd.getBytes();
      byte[] byteBuf = new byte[lineEndBytes.length];

      while(!end){
        _dis.read(byteBuf,0,lineEndBytes.length);
        String t = new String(byteBuf);
        if(t.equals(lineEnd)) end=true;
      }
    }catch(Exception e){e.printStackTrace();}
  }


  public void unhook()
  {
    m_isDefunct = true;
    m_connected = false;
    try {
      if (dis != null)
      {
        dis.close();
      }
      dis = null;
    } catch (Exception e) {
    }
  }


}

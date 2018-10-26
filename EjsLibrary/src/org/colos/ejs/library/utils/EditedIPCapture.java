package org.colos.ejs.library.utils;
import ipcapture.Base64Encoder;
import java.net.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import processing.core.*;

/* Edited IPCapture.java
 * Added a case to a connection without user name and password
 * */

public class EditedIPCapture extends PImage implements Runnable {
  private PApplet parent;
  private String urlString, user, pass;
  private byte[] curFrame;
  private boolean frameAvailable;
  private Thread streamReader;
  private HttpURLConnection conn;
  private BufferedInputStream httpIn;
  private ByteArrayOutputStream jpgOut;
  private volatile boolean keepAlive;
  
  public final static String VERSION = "0.2.0";
  
  public EditedIPCapture(PApplet parent) {
    this(parent, "", "", "");
  }

  public EditedIPCapture(PApplet parent, String urlString, String user, String pass) {
    super();
    this.parent = parent;
    parent.registerDispose(this);
    this.urlString = urlString;
    this.user = user;
    this.pass = pass;
    this.curFrame = new byte[0];
    this.frameAvailable = false;
    this.keepAlive = false;
  }

  public PApplet getParent() {
	    return this.parent;
  }

  public boolean isAlive() {
    return streamReader.isAlive();
  }

  public boolean isAvailable() {
    return frameAvailable;
  }
  
  public void start() {
    if (streamReader != null && streamReader.isAlive()) {
      System.out.println("Camera already started");
      return;
    }
    streamReader = new Thread(this, "HTTP Stream reader");
    keepAlive = true;
    streamReader.start();
  }
  
  public void start(String urlString, String user, String pass) {
    this.urlString = urlString;
    this.user = user;
    this.pass = pass;
    this.start();
  }

  public void stop() {
    if (streamReader == null || !streamReader.isAlive()) {
      System.out.println("Camera already stopped");
      return;
    }
    keepAlive = false;
    try {
      streamReader.join();
    }
    catch (InterruptedException e) {
      System.err.println(e.getMessage());
    }
  }
  
  public void dispose() {
    stop();
  }
  
  public void run() {
    URL url;
    Base64Encoder base64 = new Base64Encoder();
    
    try {
      url = new URL(urlString);
    }
    catch (MalformedURLException e) {
      System.err.println("Invalid URL");
      return;
    }
    //JSV: Added a case to connection without user name and password
    try {
      conn = (HttpURLConnection)url.openConnection();
      if((user == null && pass == null) || ((user == "" && pass == ""))){
    	  System.err.println("Connecting without user name & password");
      }else{
    	  conn.setRequestProperty("Authorization", "Basic " + base64.encode(user + ":" + pass));
      }
      httpIn = new BufferedInputStream(conn.getInputStream(), 8192);
    }
    catch (IOException e) {
      System.err.println("Unable to connect: " + e.getMessage());
      return;
    }
    
    int prev = 0;
    int cur = 0;
    
    try {
      while (keepAlive && (cur = httpIn.read()) >= 0) {
        if (prev == 0xFF && cur == 0xD8) {
          jpgOut = new ByteArrayOutputStream(8192);
          jpgOut.write((byte)prev);
        }
        if (jpgOut != null) {
          jpgOut.write((byte)cur);
        }
        if (prev == 0xFF && cur == 0xD9) {
          synchronized(curFrame) {
            curFrame = jpgOut.toByteArray();
          }
          frameAvailable = true;
          jpgOut.close();
        }
        prev = cur;
      }
    }
    catch (IOException e) {
      System.err.println("I/O Error: " + e.getMessage());
    }
    try {
      jpgOut.close();
      httpIn.close();
    }
    catch (IOException e) {
      System.err.println("Error closing streams: " + e.getMessage());
    }
    conn.disconnect();
  }
  
  public void read() {
    try {
      ByteArrayInputStream jpgIn = new ByteArrayInputStream(curFrame);
      BufferedImage bufImg = ImageIO.read(jpgIn);
      jpgIn.close();
      int w = bufImg.getWidth();
      int h = bufImg.getHeight();
      if (w != this.width || h != this.height) {
        this.resize(bufImg.getWidth(),bufImg.getHeight());
      }
      bufImg.getRGB(0, 0, w, h, this.pixels, 0, w);
      this.updatePixels();
      frameAvailable = false;
    }
    catch (IOException e) {
      System.err.println("Error acquiring the frame: " + e.getMessage());
    }
  }
}
/**
 ** com/charliemouse/cambozola/shared/StreamSplit.java
 **  Copyright (C) Andy Wilcock, 2001.
 **  Available from http://www.charliemouse.com
 **
 ** This file m_inputStream part of the CamViewer package (c) Andy Wilcock, 2001.
 ** Available from http://www.charliemouse.com
 **
 **  Cambozola m_inputStream free software; you can redistribute it and/or modify
 **  it under the terms of the GNU General Public License as published by
 **  the Free Software Foundation; either version 2 of the License, or
 **  (at your option) any later version.
 **
 **  Cambozola m_inputStream distributed in the hope that it will be useful,
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 **  GNU General Public License for more details.
 **
 **  You should have received a copy of the GNU General Public License
 **  along with Cambozola; if not, write to the Free Software
 **  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **/
package org.colos.ejs.library.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Hashtable;

import javax.imageio.ImageIO;

public class WebVideoMPEGutil {
  static private final int IMG_FLUFF_FACTOR = 1;

  private WebVideo mWebVideo;
  private Object mLock = new Object();

  private boolean mCollecting = false;
  private boolean mStreamEnd = false;
  private boolean mMustStop = false;
  private int mRetryCount = 1;
  private int mImgidx = 0;
  private long mRetryDelay = 1000;
  private long mStartTime = 0;
  private String mDocBase=null;

  private char mLineBuffer[];

  public WebVideoMPEGutil(WebVideo webVideo) { mWebVideo = webVideo; }
  
  public void stop() { 
    mMustStop = true;
  }
  
  public String readMJPGStream() { //preprocess the mjpg stream to remove the mjpg encapsulation

    DataInputStream dis=null;
    DataOutputStream dos=null;
    try {
      String encpasswd = null;
      String connectionError = null;
      String ctype = null;
      Hashtable<String, String> headers = null;
      int tryIndex = 0;

      URL url=null;
      try { // Check connection
         url = new URL(mWebVideo.getURL());
      } 
      catch (MalformedURLException mue) { return "Incorrect URL address"; }
      catch (Exception e) { return "Error when connecting to camera"; }
      
      int port = url.getPort();
      if (port == -1) port = 80;
      mMustStop = false;
      Socket websock;
      do {
        // Keep track of how many times we tried.
        tryIndex++;
        websock = new Socket(url.getHost(), port);
        dos = new DataOutputStream(websock.getOutputStream());
        dis = new DataInputStream(new BufferedInputStream(websock.getInputStream()));

        StringBuffer request = new StringBuffer();
        request.append("GET " + url.getFile() + " HTTP/1.0\r\n");
        if (mDocBase != null) request.append("Referer: " + mDocBase + "\r\n");
        request.append("Authorization: Basic " + encpasswd + "\r\n");
        request.append("Host: " + url.getHost() + "\r\n");
        request.append("\r\n");
        dos.writeBytes(request.toString());

        mCollecting = true;
        // Read Headers for the main thing...
        headers = readHeaders(dis);
        // Work out the content type/boundary.
        connectionError = null;
        ctype = headers.get("content-type");
        if (ctype == null) connectionError = "No main content type";
         else if (ctype.indexOf("text") != -1) {
           String response = null;
           while ((response = readLine(dis)) != null) System.err.println(response);
           connectionError = "Failed to connect to server (denied?)";
         }
         if (connectionError==null) break; // Yay!! got one.
         if (!this.mWebVideo.isEnabled()) {
           websock.close();
           return null;
         }
         mWebVideo.sendErrorMessage(connectionError);
         if (mRetryDelay<10) Thread.yield();
         else {
           try { Thread.sleep(mRetryDelay); } 
           catch(InterruptedException ie) {}
         }
         if (mMustStop) {
           websock.close();
           return null;
         }
      } while (tryIndex < mRetryCount);
      websock.close();
      
      if (connectionError!=null) return connectionError;

      int bidx = ctype.indexOf("boundary=");
      String boundary = null;
      if (bidx != -1) {
        boundary = ctype.substring(bidx + 9);
        ctype = ctype.substring(0, bidx);
        if (!boundary.startsWith("--")) {
          boundary = "--" + boundary;
        }
      }
      // Now if we have a boundary, read up to that.
      if (ctype.startsWith("multipart/x-mixed-replace")) {
        skipToBoundary(boundary,dis);
      }

      do {
        if (mCollecting) {
          // Now we have the real type...
          //  More headers (for the part), then the object...
          if (boundary != null) {
            headers = readHeaders(dis);
            // Are we at the end of the m_stream?
            if (this.mStreamEnd) {
              //break;
            }
            ctype = headers.get("content-type");
            if (ctype == null) {
              throw new Exception("No part content type");
            }
          }
          // Mixed Type -> just skip...
          if (ctype.startsWith("multipart/x-mixed-replace")) {
            // Skip
            bidx = ctype.indexOf("boundary=");
            boundary = ctype.substring(bidx + 9);

            skipToBoundary(boundary,dis);
          } else {
            // Something we want to keep...
            byte[] img = readToBoundary(boundary,dis);
            if (img.length == 0) {
              //break;
              System.out.println ("Can't read image");
              continue;
            }
            // FPS counter.
            if (mImgidx > IMG_FLUFF_FACTOR && mStartTime == 0) {
              mStartTime = System.currentTimeMillis();
            }
            // Update the image
            synchronized (mLock) {
              BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(img));
              Image image = imagen; //Toolkit.getDefaultToolkit().createImage(img);
              mWebVideo.sendImage(image);
            }
          }
        }

        long delay = mWebVideo.getDelay();
        if (delay<10) Thread.yield();
        else {
          try { Thread.sleep(delay); } 
          catch(InterruptedException ie) {}
        }
        if (mMustStop) return null;
      } while (mWebVideo.isEnabled());

    } 
    catch (SocketException e) { return "Socket exception when reading from camera"; }
    catch (Exception e) { return "Exception when reading from camera"; } 
    finally { // unhook
      mWebVideo.setEnabled(false);
      mCollecting = false;
      try {
          if (dis!=null) dis.close();
          if (dos!=null) dos.close();
          return "Error when connecting to comera";
      } catch (Exception e) { }
    }
    // At this point, the m_stream m_inputStream done
    // [could dispplay a that's all folks - leaving it as it m_inputStream
    //  will leave the last frame up]
    return null;
  }

  private String readLine(InputStream in) throws IOException {
    char buf[] = mLineBuffer;
    if (buf == null) buf = mLineBuffer = new char[128];
    int room = buf.length;
    int offset = 0;
    int c;

    loop:
      while (true) {
        switch (c = in.read()) {
          case -1:
          case '\n':
            break loop;

          case '\r':
            int c2 = in.read();
            if ((c2 != '\n') && (c2 != -1)) {
              if (!(in instanceof PushbackInputStream)) {
                //this.in = new PushbackInputStream(in);
              }
              ((PushbackInputStream) in).unread(c2);
            }
            break loop;

          default:
            if (--room < 0) {
              buf = new char[offset + 128];
              room = buf.length - offset - 1;
              System.arraycopy(mLineBuffer, 0, buf, 0, offset);
              mLineBuffer = buf;
            }
            buf[offset++] = (char) c;
            break;
        }
      }
    if ((c == -1) && (offset == 0)) {
      return null;
    }
    return String.copyValueOf(buf, 0, offset);
  }


  private Hashtable<String, String> readHeaders(DataInputStream dis) throws IOException {
    Hashtable<String, String> ht = new Hashtable<String, String>();
    String response = null;
    boolean satisfied = false;

    do {
      //response = m_dis.readLine();
      response = readLine(dis);
      if (response == null) {
        mStreamEnd = true;
        break;
      } else if (response.equals("")) {
        if (satisfied) {
          break;
        }
        //                else {
        //                    // Carry on...
        //                }
      } else {
        satisfied = true;
      }
      int idx = response.indexOf(":");
      if (idx == -1) {
        continue;
      }
      String tag = response.substring(0, idx);
      String val = response.substring(idx + 1).trim();
      ht.put(tag.toLowerCase(), val);
    } while (true);
    return ht;
  }

  private void skipToBoundary(String boundary, DataInputStream dis) throws IOException {
    readToBoundary(boundary, dis);
  }

  private byte[] readToBoundary(String boundary, DataInputStream dis) throws IOException {
    ResizableByteArrayOutputStream baos = new ResizableByteArrayOutputStream();
    StringBuffer lastLine = new StringBuffer();
    int lineidx = 0;
    int chidx = 0;
    byte ch;
    do {
      try {
        ch = dis.readByte();
      } catch (EOFException e) {
        mStreamEnd = true;
        break;
      }
      if (ch == '\n' || ch == '\r') {
        //
        // End of line...
        //
        String lls = lastLine.toString();
        if (boundary != null && lls.startsWith(boundary)) {
          String btest = lls.substring(boundary.length());
          if (btest.equals("--")) {
            mStreamEnd = true;
          }
          chidx = lineidx;
          break;
        }
        lastLine = new StringBuffer();
        lineidx = chidx + 1;
      } else {
        lastLine.append((char) ch);
      }
      chidx++;
      baos.write(ch);
    } while (true);
    //
    baos.close();
    baos.resize(chidx);
    return baos.toByteArray();
  }

}

class ResizableByteArrayOutputStream extends ByteArrayOutputStream {

  public ResizableByteArrayOutputStream() {
    super();
  }

  public void resize(int size) {
    count = size;
  }
}

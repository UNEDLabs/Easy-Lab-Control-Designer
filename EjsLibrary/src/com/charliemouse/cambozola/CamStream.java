/**
 ** com/charliemouse/cambozola/shared/CamStream.java
 **  Copyright (C) Andy Wilcock, 2001.
 **  Available from http://www.charliemouse.com
 **
 ** This file m_inputStream part of the Cambozola package (c) Andy Wilcock, 2001.
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
package com.charliemouse.cambozola;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.io.IOException;

public class CamStream extends Thread {

    private static final int IMG_FLUFF_FACTOR = 1;
    private ExceptionReporter m_reporter = null;
    private Vector<ImageChangeListener> m_listeners;
    private Object m_lock = new Object();
    private Image m_imain = null;
    private Toolkit m_tk;
    private URL m_stream;
    private URL m_docBase;
    private DataInputStream m_inputStream = null;
    private DataOutputStream m_outputStream = null;
    private char lineBuffer[];//Hector
    private boolean m_isDefunct = false;
    private boolean m_collecting = false;
    private byte[] m_rawImage;
    private String m_imageType = "image/jpeg";
    private long m_startTime = 0;
    private int m_imgidx = 0;
    private int m_retryCount = 1;
    private int m_retryDelay = 1000;
//	private String m_appName = "";
    private int m_delayImage;

    public CamStream(URL strm, URL docBase, int retryCount, int retryDelay, int imageDelay, ExceptionReporter reporter) {
        // BEGIN JOSE
        m_delayImage = imageDelay;
        // END JOSE

        m_tk = Toolkit.getDefaultToolkit();
        m_listeners = new Vector<ImageChangeListener>();
        m_stream = strm;

        m_reporter = reporter;
        m_isDefunct = false;
        m_docBase = docBase;
        m_retryCount = retryCount;
        m_retryDelay = retryDelay;
    }

    public Image getCurrent() {
        synchronized (m_lock) {
            return m_imain;
        }
    }

    public byte[] getRawImage() {
        synchronized (m_lock) {
            return m_rawImage;
        }
    }

    public int getIndex() {
        synchronized (m_lock) {
            return m_imgidx;
        }
    }

    public String getType() {
        synchronized (m_lock) {
            return m_imageType;
        }
    }

    public URL getStreamURL() {
        return m_stream;
    }

    public double getFPS() {
        if (m_startTime == 0) {
            return 0.0;
        }
        long currTime = System.currentTimeMillis();
        return (1000.0 * (m_imgidx - IMG_FLUFF_FACTOR)) / (currTime - m_startTime);
    }

    public void addImageChangeListener(ImageChangeListener cl) {
        m_listeners.addElement(cl);
    }

    public void removeImageChangeListener(ImageChangeListener cl) {
        m_listeners.removeElement(cl);
    }

    private void fireImageChange() {
        ImageChangeEvent ce = new ImageChangeEvent(this);
        for (Enumeration<ImageChangeListener> e = m_listeners.elements(); e.hasMoreElements();) {
            e.nextElement().imageChanged(ce);
        }
    }

    private final String readLine(InputStream in) throws IOException {
        char buf[] = lineBuffer;

        if (buf == null) {
            buf = lineBuffer = new char[128];
        }

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
                        System.arraycopy(lineBuffer, 0, buf, 0, offset);
                        lineBuffer = buf;
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

    public void run() {
        StreamSplit ssplit = null;
        try {

            int port = m_stream.getPort();
            if (port == -1) {
                port = 80;
            //String specHost = m_stream.getHost();
            //
            // The 'getUserInfo' m_inputStream only usable in 1.2 and later - used for
            // password accessible streams.
            //
            //String userInfo = null; //m_stream.getUserInfo();
            }
            String encpasswd = null;
            // if (userInfo != null && !userInfo.equals("")) {
            //     encpasswd = new sun.misc.BASE64Encoder().encode (userInfo.getBytes());
            // }

            //
            // Loop for a while until we either give up (hit m_retryCount), or
            // get a connection.... Sleep inbetween.
            //
            String connectionError = null;
            String ctype = null;
            Hashtable<?, ?> headers = null;
            int tryIndex = 0;
            int retryCount = m_retryCount;
            int retryDelay = m_retryDelay;
            //
            Socket websock;
            do {

                //
                // Keep track of how many times we tried.
                //
                tryIndex++;
                websock = new Socket(m_stream.getHost(), port);
                m_outputStream = new DataOutputStream(websock.getOutputStream());
                m_inputStream = new DataInputStream(new BufferedInputStream(websock.getInputStream()));
                StringBuffer request = new StringBuffer();
                request.append("GET " + m_stream.getFile() + " HTTP/1.0\r\n");
                if (m_docBase != null) {
                    request.append("Referer: " + m_docBase + "\r\n");
                }
                //	request.append("User-Agent: " + m_appName + "\r\n");
//				if (encpasswd != null) // Paco removed this 
                {
                    request.append("Authorization: Basic " + encpasswd + "\r\n");
                }
                request.append("Host: " + m_stream.getHost() + "\r\n");
                request.append("\r\n");
                m_outputStream.writeBytes(request.toString());
                m_collecting = true;
                //
                // Read Headers for the main thing...
                //

                ssplit = new StreamSplit(m_inputStream);
                headers = ssplit.readHeaders();
                //
                // Work out the content type/boundary.
                //
                connectionError = null;
                ctype = (String) headers.get("content-type");
                if (ctype == null) {
                    connectionError = "No main content type";
                } else if (ctype.indexOf("text") != -1) {
                    String response = null;
                    while ((response = readLine(m_inputStream)) != null) {
                        System.out.println(response);
                    }
                    connectionError = "Failed to connect to server (denied?)";
                }
                if (connectionError == null) {

                    break; // Yay!! got one.
                } else if (m_isDefunct) {
                    //
                    // Not wanted any more...
                    //

                  websock.close();
                    return;
                } else {
                    //
                    // Wait a while before retrying...
                    //
                    m_reporter.reportFailure(connectionError);
                    sleep(retryDelay);
                }
            } while (tryIndex < retryCount);
            websock.close();
            //
            if (connectionError != null) {
                return;
            }
            //
            //
            int bidx = ctype.indexOf("boundary=");
            String boundary = null;
            if (bidx != -1) {
                boundary = ctype.substring(bidx + 9);
                ctype = ctype.substring(0, bidx);
                if (!boundary.startsWith("--")) {
                    boundary = "--" + boundary;
                }
            }
            //
            // Now if we have a boundary, read up to that.
            //
            if (ctype.startsWith("multipart/x-mixed-replace")) {
                ssplit.skipToBoundary(boundary);
            }

            do {
                if (m_collecting) {
                    //
                    // Now we have the real type...
                    //  More headers (for the part), then the object...
                    //
                    if (boundary != null) {

                        headers = ssplit.readHeaders();

                        //
                        // Are we at the end of the m_stream?
                        //
                        if (ssplit.isAtStreamEnd()) {

                            break;
                        }
                        ctype = (String) headers.get("content-type");
                        if (ctype == null) {

                            throw new Exception("No part content type");
                        }
                    }

                    //
                    // Mixed Type -> just skip...
                    //
                    if (ctype.startsWith("multipart/x-mixed-replace")) {
                        //
                        // Skip
                        //
                        bidx = ctype.indexOf("boundary=");
                        boundary = ctype.substring(bidx + 9);
                        //
                        ssplit.skipToBoundary(boundary);
                    } else {
                        //
                        // Something we want to keep...
                        //
                        byte[] img = ssplit.readToBoundary(boundary);
                        if (img.length == 0) {
                            break;
                        }
                        //
                        // FPS counter.
                        //
                        if (m_imgidx > IMG_FLUFF_FACTOR && m_startTime == 0) {
                            m_startTime = System.currentTimeMillis();
                        }
                        //
                        // Update the image [fores events off]
                        //
                        updateImage(ctype, img);
                    }
                }
                try {
                    Thread.sleep(m_delayImage);
                } catch (InterruptedException ie) {
                }
            } while (!m_isDefunct);
        } catch (SocketException e) {
            if (!m_isDefunct) {
                m_reporter.reportError(e);
            }
        } catch (Exception e) {
            m_reporter.reportError(e);
        } finally {
            unhook();
        }
    //
    // At this point, the m_stream m_inputStream done
    // [could dispplay a that's all folks - leaving it as it m_inputStream
    //  will leave the last frame up]
    //
    }

    private void updateImage(String ctype, byte[] img) {
        //
        // Update our image...
        //

        synchronized (m_lock) {
            m_imageType = ctype;
            m_imain = m_tk.createImage(img);
            m_rawImage = img;
            m_imgidx++;
        }
        fireImageChange();
    }

    public void finalize() {
        unhook();
    }

    public void unhook() {
        m_collecting = false;
        m_isDefunct = true;
        try {
            if (m_inputStream != null) {
                m_inputStream.close();
            }
            m_inputStream = null;
            if (m_outputStream != null) {
                m_outputStream.close();
            }
            m_outputStream = null;
        } catch (Exception e) {
        }
    }
}

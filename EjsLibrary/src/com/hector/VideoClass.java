package com.hector;
/*
 * VideoClass.java
 *
 * Created on January 28, 2006, 21:13 PM
 */

import java.net.*;
import com.sun.image.codec.jpeg.*;
import java.io.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import com.charliemouse.cambozola.*;

import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 *
 * @author Hector Vargas, UNED - 2006.
 */
public class VideoClass extends Thread {

    private String stringURL = null;
    private DataInputStream dis;
    private DataOutputStream dos;
    private char lineBuffer[];//Hector
    private Image image = null;
    private boolean connected = false;
    private HttpURLConnection huc = null;
    private boolean m_collecting = false;
    //private Toolkit m_tk = Toolkit.getDefaultToolkit();
//        private String m_imageType = "image/jpeg";
//        private byte[] m_rawImage;
    private int m_imgidx = 0;
    private int m_retryCount = 1;
    private int m_retryDelay = 1000;
    private static final int IMG_FLUFF_FACTOR = 1;
    private long m_startTime = 0;
    private ExceptionReporter m_reporter = null;
    private URL m_docBase = null;
    private URL u;
    private Vector<ImageChangeListener> m_listeners = new Vector<ImageChangeListener>();
    private boolean okURL; // monitorea si la URL es correcta o no
    private int delayImage = 0; // milisegundos
    private boolean useMJPGStream = true; // es MJPEG ?
    private boolean running; // variables para parar la ejecucion
    private Contenedor contenedor;

    /** Creates a new instance of VideoClass */
    public VideoClass(Contenedor c) {
        contenedor = c;
    }

    public void connect() {
        try {
            u = new URL(stringURL);
            huc = (HttpURLConnection) u.openConnection();
            InputStream is = huc.getInputStream();
            connected = true;
            BufferedInputStream bis = new BufferedInputStream(is);
            dis = new DataInputStream(bis);
            okURL = true;
            running = true;
        //System.out.println("la variable running: "+running);
        } catch (IOException e) { //incase no connection exists wait and try again, instead of printing the error
            huc.disconnect();
            okURL = false;
            System.out.println(e);
            System.out.println("error: Verify the URL to format especified or connection");
        } catch (Exception e) {
            System.out.println(e);
            okURL = false;
        }
    }

    public void disconnect() {
        try {
            if (connected) {
                dis.close();
                connected = false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void readStream() { //the basic method to continuously read the stream
        try {
            if (useMJPGStream) {
                readMJPGStream();
            } else {
                while (okURL & running) {
                    connect();
                    //System.out.println("imagen leida con un retardo "+delayImage+" msec");
                    readJPG();
                    delay(delayImage);
                    disconnect();
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
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

    public void readMJPGStream() { //preprocess the mjpg stream to remove the mjpg encapsulation

        StreamSplit ssplit = null;
        try {

            int port = u.getPort();
            if (port == -1) {
                port = 80;
            }
            String encpasswd = null;
            String connectionError = null;
            String ctype = null;
            Hashtable<String, String> headers = null;
            int tryIndex = 0;
            int retryCount = m_retryCount;
            int retryDelay = m_retryDelay;
            Socket websock;
            do {
                // Keep track of how many times we tried.
                tryIndex++;
                websock = new Socket(u.getHost(), port);
                dos = new DataOutputStream(websock.getOutputStream());
                dis = new DataInputStream(new BufferedInputStream(websock.getInputStream()));

                StringBuffer request = new StringBuffer();
                request.append("GET " + u.getFile() + " HTTP/1.0\r\n");
                if (m_docBase != null) {
                    request.append("Referer: " + m_docBase + "\r\n");
                }
                //request.append("User-Agent: " + m_appName + "\r\n");
//                                if (encpasswd != null) // Paco removed this 
                {
                    request.append("Authorization: Basic " + encpasswd + "\r\n");
                }
                request.append("Host: " + u.getHost() + "\r\n");
                request.append("\r\n");
                dos.writeBytes(request.toString());

                m_collecting = true;
                // Read Headers for the main thing...
                ssplit = new StreamSplit(dis);
                headers = ssplit.readHeaders();
                // Work out the content type/boundary.
                connectionError = null;
                ctype = headers.get("content-type");


                if (ctype == null) {
                    connectionError = "No main content type";

                } else if (ctype.indexOf("text") != -1) {
                    String response = null;
                    while ((response = readLine(dis)) != null) {
                        System.out.println(response);
                    }
                    connectionError = "Failed to connect to server (denied?)";
                }
                if (connectionError == null) {
                    break; // Yay!! got one.
                } else if (!running) {
                    // Not wanted any more...
                    websock.close();
                    return;
                } else {
                    // Wait a while before retrying...
                    m_reporter.reportFailure(connectionError);
                    delay(retryDelay);
                }


            } while (tryIndex < retryCount);
            websock.close();

            if (connectionError != null) {
                return;
            }
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
                ssplit.skipToBoundary(boundary);
            }

            do {
                if (m_collecting) {
                    // Now we have the real type...
                    //  More headers (for the part), then the object...
                    if (boundary != null) {
                        headers = ssplit.readHeaders();
                        // Are we at the end of the m_stream?
                        if (ssplit.isAtStreamEnd()) {
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

                        ssplit.skipToBoundary(boundary);
                    } else {
                        // Something we want to keep...
                        byte[] img = ssplit.readToBoundary(boundary);
                        if (img.length == 0) {
                            //break;
                        }
                        // FPS counter.
                        if (m_imgidx > IMG_FLUFF_FACTOR && m_startTime == 0) {
                            m_startTime = System.currentTimeMillis();
                        }
                        // Update the image [fores events off]
                        updateImage(ctype, img);
                    }
                }

                delay(delayImage);
            //System.out.println("la variable running: "+running);
            //System.out.println("stream leido con un retardo "+delayImage+" msec");

            } while (running);

        } catch (SocketException e) {
            if (running) {
                m_reporter.reportError(e);
            }
        } catch (Exception e) {
            m_reporter.reportError(e);
        } finally {
            unhook();
        }
    // At this point, the m_stream m_inputStream done
    // [could dispplay a that's all folks - leaving it as it m_inputStream
    //  will leave the last frame up]

    }

    private void fireImageChange() {
        ImageChangeEvent ce = new ImageChangeEvent(this);
        for (Enumeration<ImageChangeListener> e = m_listeners.elements(); e.hasMoreElements();) {
            e.nextElement().imageChanged(ce);
        }
    }

    private void updateImage(String ctype, byte[] img) {
        // Update our image...

//                m_imageType = ctype;

        try {
            BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(img));
            image = imagen;
            contenedor.put(image);
        } catch (IOException ex) {
        }
        //image = m_tk.createImage(img);
//                m_rawImage = img;
        m_imgidx++;
        fireImageChange();

    }

    public void unhook() {
        m_collecting = false;
        running = false;
        okURL = false;
        try {
            if (dis != null) {
                dis.close();
            }
            dis = null;
            if (dos != null) {
                dos.close();
            }
            dos = null;
            connected = false;
            System.out.println("error: Verify the URL to format especified or connection de unhook()");
        } catch (Exception e) {
        }
    }

    public void readJPG() { //read the embedded jpeg image
        try {
            JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(dis);
            image = decoder.decodeAsBufferedImage();
            contenedor.put(image);
        } catch (Exception e) {
            System.out.println("error: Verify the URL to format especified or connection");
            //e.printStackTrace();
            okURL = false;
            disconnect();
        }
    }

    public void setURL(String _url) {
        stringURL = _url;
    }

    public void setMJPEGFormat(boolean MJPEG) {
        useMJPGStream = MJPEG;
    }

    public void setImageDelay(int delay) { // delay en milisegundos
        if (delay < 0) {
            delayImage = 0;
        }
        delayImage = delay;
    }

    private void delay(int mseconds) {
        try {
            //java.lang.Thread.sleep(mseconds);
            Thread.sleep(mseconds);    // Inserted delay
        } catch (InterruptedException e) {
            //do nothing!
            System.out.println("Delay interrupted!");
        }
    }

    public String getURL() {
        return stringURL;
    }

    public boolean getMJPEG() {
        return useMJPGStream;
    }

    public int getDelay() {
        return delayImage;
    }

    public Image getImage() {
        return image;
    }

    public void stopRunning() {
        running = false;
    }

    public boolean getConnected() {
        return connected;
    }

    public void run() {

        connect();
//                System.out.println("el run() de VideoClass ha empezado");
        if (okURL) {
            readStream();
        }
//                System.out.println("el run() de VideoClass ha finalizado");

    }
}

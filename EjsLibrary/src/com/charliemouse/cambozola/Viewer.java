package com.charliemouse.cambozola;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/*import com.charliemouse.cambozola.shared.AppID;
import com.charliemouse.cambozola.shared.CamStream;
import com.charliemouse.cambozola.shared.ExceptionReporter;
import com.charliemouse.cambozola.shared.ImageChangeEvent;
import com.charliemouse.cambozola.shared.ImageChangeListener;
import com.charliemouse.cambozola.watermark.Watermark;
import com.charliemouse.cambozola.watermark.WatermarkCollection;
 */

public class Viewer extends java.applet.Applet implements MouseListener, MouseMotionListener, KeyListener, ImageChangeListener, ExceptionReporter, ViewerAttributeInterface
{
  private static final long serialVersionUID = 1L;

  private static final String PAR_FAILUREIMAGE    = "failureimage";
  private static final String PAR_DELAY           = "delay";
  private static final String PAR_RETRIES         = "retries";
  private static final String PAR_URL             = "url";
  private static final String PAR_ACCESSORIES     = "accessories";
  private static final String PAR_WATERMARK       = "watermarks";
  private static final String PAR_ACCESSORYSTYLE  = "accessorystyle";


  private static final int VAL_STYLE_INDENT       = 0;
  private static final int VAL_STYLE_OVERLAY      = 1;
  private static final int VAL_STYLE_ALWAYSON     = 2;

  //private static boolean  ms_standalone           = false;
  private Properties  m_parameters            = null;
  private URL         m_documentBase          = null;
  private URL         m_codeBase              = null;
  private URL         m_mainURL               = null;
  private Vector<URL> m_alternateURLs         = null;

  // BEGIN JOSE
  private CamStream   m_imgStream             = null;
  private CamImage    m_imgStill              = null;
  private boolean		  isMJPEG			            = false;
  private int         m_delayImage            = 20;
  // END JOSE

  @SuppressWarnings("unused")
  private String      m_msg                   = null;
  private boolean     m_displayAccessories    = false;
  private int         m_accessoryStyle        = VAL_STYLE_INDENT;
  private PercentArea m_area                  = new PercentArea();
  private Vector<Accessory>      m_accessories           = new Vector<Accessory>();
  private Image       m_offscreenAccBar       = null;
  private Image       m_backingStore          = null;
  private boolean     m_readingStream         = false;
  private int         m_retryCount            = 1;
  private int         m_retryDelay            = 1000;
  private Image       m_failureImage          = null;
  private boolean     m_loadFailure           = false;
  private Watermark   m_wmHit                 = null;
  private WatermarkCollection m_wmCollection  = null;



  public Viewer()
  {

    m_alternateURLs = new Vector<URL>();
    m_parameters = new Properties();

    m_parameters.put(PAR_ACCESSORYSTYLE, "indent");
    m_parameters.put(PAR_ACCESSORIES, "none");

    m_wmCollection = new WatermarkCollection();
  }


  public void init()
  {

    //
    // Init!
    //
    m_alternateURLs = new Vector<URL>();
    //
    String wmarks = getParameterValue(PAR_WATERMARK);
    if (wmarks != null) {
      m_wmCollection.populate(wmarks, m_documentBase);
    }

    //
    // Load the failure Image.
    //
    String fistr = getParameterValue(PAR_FAILUREIMAGE);
    if (fistr != null && !fistr.equals("")) {
      try {
        URL fiurl = new URL(m_documentBase, fistr);
        setFailureImageURL(fiurl);
      } catch (MalformedURLException mfe) {
        System.err.println("Unable to access URL for failure image -" + fistr);
      }
    }
    //
    String delay = getParameterValue(PAR_DELAY);
    if (delay != null && !delay.equals("")) {
      try {
        int di= Integer.parseInt(delay);
        setRetryDelay(di);
      } catch (Exception e) {
        System.err.println("Unable to set retry delay");
      }
    }
    String retries = getParameterValue(PAR_RETRIES);
    if (retries != null && !retries.equals("")) {
      try {
        int ri = Integer.parseInt(retries);
        setRetryCount(ri);
      } catch (Exception e) {
        System.err.println("Unable to set retry count");
      }
    }
    //
    String appurl = getParameterValue(PAR_URL);
    if (appurl == null || appurl.equals("")) {
      throw new IllegalArgumentException("Missing URL");
    }
    m_mainURL = null;
    //
    // Break apart the URLs - separator is |
    //
    StringTokenizer st = new StringTokenizer(appurl, "|");
    while(st.hasMoreTokens())
    {
      try {
        URL alt = new URL(m_codeBase, st.nextToken());
        m_alternateURLs.addElement(alt);
        if (m_mainURL == null)
        {
          m_mainURL = alt;
        }
      } catch (MalformedURLException mfe) {
        reportError(mfe);
      }
    }
    setCurrentURL(m_mainURL);
    setAlternateURLs(m_alternateURLs);
    configureAccessories(getParameterValue(PAR_ACCESSORIES));

    setBackground(Color.white);
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
  }



  private String getParameterValue(String key)
  {
    return m_parameters.getProperty(key, null);
  }

  public void setParameterValue(String key, String value)
  {
    m_parameters.setProperty (key, value);
  }


  // BEGIN JOSE
  public void setImageDelay(int delay)
  {
    if (delay < 0)
    {
      m_delayImage = 0;
    }
    else m_delayImage = delay;
  }


  public void setMJPEGFormat (boolean MJPEG)
  {
    isMJPEG = MJPEG;
  }
  // END JOSE






  void setFailureImageURL(URL fistr)
  {
    try {
      m_failureImage = createImage((ImageProducer)fistr.getContent());
      m_failureImage.getWidth(this);
    } catch (IOException ie) {
      System.err.println("Unable to access failure image contents - " + ie);
    }
  }

  void setRetryCount(int rc)
  {
    if (rc < 1)
    {
      return;
    }
    m_retryCount = rc;
  }

  void setRetryDelay(int delay)
  {
    if (delay < 0)
    {
      return;
    }
    m_retryDelay = delay;
  }

  private void configureAccessories(String acclist)
  {
    //
    // Set up the accessory style.
    //
    String as = getParameterValue(PAR_ACCESSORYSTYLE);
    if (as != null) {
      if (as.equalsIgnoreCase("indent"))
      {
        m_accessoryStyle = VAL_STYLE_INDENT;
      } else if (as.equalsIgnoreCase("overlay")) {
        m_accessoryStyle = VAL_STYLE_OVERLAY;
      } else if (as.equalsIgnoreCase("always")) {
        m_accessoryStyle = VAL_STYLE_ALWAYSON;
      }
    }
    //
    // Set up the accessories (the things on the LHS).
    //
    if (acclist == null || acclist.equals("") || acclist.equalsIgnoreCase("default"))
    {
      //
      // Default list.
      //
      acclist = "Home,ZoomOut,ZoomIn,Pan,ChangeStream,Info,WWWHelp";
    } else if (acclist.equalsIgnoreCase("none")) {
      //
      // Explicitly none...
      //
      acclist = "";
    }
    StringTokenizer st = new StringTokenizer(acclist, ", ");
    while(st.hasMoreTokens())
    {
      String tok = st.nextToken();
      try {
        Class<?> accClazz = Class.forName("" + tok + "Accessory");
        //Class accClazz = Class.forName("com.charliemouse.cambozola.accessories." + tok + "Accessory");
        Accessory acc = (Accessory)accClazz.newInstance();
        acc.setViewerAttributes(this);
        acc.getIconImage();
        //
        // MS JVM in IE is really picky about this code, and used to crash if
        // it used a 'continue'...
        //
        if (acc.isEnabled())
        {
          m_accessories.addElement(acc);
        }
      } catch (Exception exc) {
        System.err.println("Unable to load accessory - " + tok);
        exc.printStackTrace();
      }
    }
  }

  public synchronized void reportError(Throwable t)
  {
    reportNote(t.getMessage());

    stop();
  }

  public synchronized void reportFailure(String s)
  {
    m_loadFailure = true;
    reportNote(s);
  }

  public synchronized void reportNote(String s)
  {
    System.err.println(s);
    setMessage(s);
    m_readingStream = false;
    repaint();
  }

  private synchronized void setMessage(String s)
  {
    m_msg = s;
  }

  public void start()
  {
  }


  public void stop()
  {
    if (m_imgStream != null) {
      m_imgStream.unhook();
      m_imgStream = null;
    }
    m_readingStream = false;
    for (Enumeration<Accessory> e = m_accessories.elements();e.hasMoreElements();)
    {
      e.nextElement().terminate();
    }
  }

  public void setCurrentURL(URL loc)
  {
    m_loadFailure = false;
    //
    // Set up the identifying User-Agent
    //
    //String userAgent = m_props.getAppNameVersion() + "/Java " + System.getProperty("java.version") + " " + System.getProperty("java.vendor");
    //
    m_mainURL = loc;

    if (isMJPEG) {
      if (m_imgStream != null) {
        m_msg = "";
        m_imgStream.removeImageChangeListener(this);
        m_imgStream.unhook();
      }
      m_imgStream = new CamStream(m_mainURL, m_documentBase, m_retryCount, m_retryDelay, m_delayImage, this);
      m_imgStream.addImageChangeListener(this);
      m_imgStream.start();
    } else {
      if (m_imgStill != null) {
        m_msg = "";
        m_imgStill.removeImageChangeListener(this);
        m_imgStill.unhook();
      }

      m_imgStill = new CamImage(m_mainURL, m_retryCount, m_retryDelay, m_delayImage,this);
      m_imgStill.addImageChangeListener(this);
      m_imgStill.start();
    }
  }

  public void displayURL(URL url, String target)
  {
    if (target == null)
    {
      getAppletContext().showDocument(url);
    } else {
      getAppletContext().showDocument(url, target);
    }
  }

  public Vector<URL> getAlternateURLs()
  {
    return m_alternateURLs;
  }

  public void setAlternateURLs(Vector<URL> v)
  {
    m_alternateURLs = v;
  }

  public void imageChanged(ImageChangeEvent ce)
  {
    update(getGraphics());
    getToolkit().sync();


  }


  public void paint(Graphics g)
  {
    update(g);
  }


  public void update(Graphics g)
  {
    if (g == null) return;
    Dimension d = getSize();
    if (m_backingStore == null || m_backingStore.getWidth(this) != d.width || m_backingStore.getHeight(this) != d.height) {
      m_backingStore = createImage(d.width, d.height);
      //
      // Size has changed, recalculate the hit areas
      //
      m_wmCollection.recalculateLocations(d);
    }
    Graphics gg2 = m_backingStore.getGraphics();
    if (m_loadFailure && m_failureImage != null) {
      //
      // Draw the failure image.
      //
      paintFrame(gg2,m_failureImage, d, null);
    } else if (!m_readingStream) {
      gg2.setPaintMode();
      if (isDisplayingAccessories() && m_accessoryStyle == VAL_STYLE_INDENT)
      {
        gg2.setColor(Color.white);
        gg2.fillRect(Accessory.BUTTON_SIZE, 0, d.width, d.height);
      }
      //
      /*FontMetrics fm = gg2.getFontMetrics();
            int width = fm.stringWidth(m_msg);
            gg2.setColor(Color.black);
            gg2.drawString(m_msg, (d.width - width) / 2, d.height / 2);
       */
      //
      // Draw the accessories...
      //
      paintAccessories(gg2);
    }

    if (isMJPEG) {
      if (m_imgStream != null) {

        Image img = m_imgStream.getCurrent();
        if (img != null) {
          m_loadFailure = false;
          m_readingStream = true;
          paintFrame(gg2,img, d, m_wmCollection);
        }}
    } else {
      if (m_imgStill != null) {

        Image img = m_imgStill.getCurrent();
        if (img != null) {
          m_loadFailure = false;
          m_readingStream = true;
          paintFrame(gg2,img, d, m_wmCollection);
        }}
    }
    g.drawImage(m_backingStore,0,0,null);
    gg2.dispose();
  }


  public void paintFrame(Graphics g, Image img, Dimension d, WatermarkCollection wmc)
  {
    //
    // Draw the main image...
    //
    int indent = 0;
    if (isDisplayingAccessories() && m_accessoryStyle == VAL_STYLE_INDENT)
    {
      indent = Accessory.BUTTON_SIZE;
    }
    int iw = img.getWidth(this);
    int ih = img.getHeight(this);
    if (iw == -1 || ih == -1) return; // No size for the image, no zoom.
    //
    //
    // Work out the area to zoom into.
    //
    Rectangle imgarea = m_area.getArea(iw, ih);
    g.drawImage(img, indent, 0, d.width, d.height, imgarea.x, imgarea.y, imgarea.x + imgarea.width, imgarea.y + imgarea.height, this);
    //
    // Draw the watermark
    //
    if (wmc != null)
    {
      wmc.paint(g);
    }
    //
    // Draw the accessories...
    //
    paintAccessories(g);
  }

  private void paintAccessories(Graphics g)
  {
    Dimension d = getSize();
    int asize = m_accessories.size();
    if (isDisplayingAccessories() && Accessory.BUTTON_SIZE > 0 && asize > 0) {
      //
      // First time - build up, store in image, and reuse...
      //
      if (m_offscreenAccBar == null) {
        m_offscreenAccBar = createImage(Accessory.BUTTON_SIZE, m_accessories.size() * Accessory.BUTTON_SIZE);
        Graphics accessoryBar = m_offscreenAccBar.getGraphics();
        //
        int idx = 0;
        for (Enumeration<Accessory> accEnum = m_accessories.elements(); accEnum.hasMoreElements();)
        {
          accessoryBar.setColor(Color.lightGray);
          Accessory acc = accEnum.nextElement();
          int yoffset = idx*Accessory.BUTTON_SIZE;
          accessoryBar.fill3DRect(0,yoffset, Accessory.BUTTON_SIZE, Accessory.BUTTON_SIZE, true);
          accessoryBar.drawImage(acc.getIconImage(), Accessory.ICON_INDENT, yoffset+Accessory.ICON_INDENT,
              new ImageObserver()
          {
            public boolean imageUpdate(Image img, int infoflags,
                int x, int y, int width, int height)
            {
              return true;
            }
          });
          idx++;
        }
        accessoryBar.dispose();
      }
      //
      g.drawImage(m_offscreenAccBar,0,0,null);
      //
      // Draw the white box only if we are indenting...
      //
      if (m_accessoryStyle == VAL_STYLE_INDENT)
      {
        int fluff = (m_accessories.size() * Accessory.BUTTON_SIZE);
        g.setColor(Color.white);
        g.fillRect(0,fluff,Accessory.BUTTON_SIZE,d.height);
      }
    }
  }


  public void keyPressed(KeyEvent ke)
  {
    if (!m_readingStream)
    {
      return;
    }
    if (ke.getKeyCode() == KeyEvent.VK_HOME)
    {
      m_area.reset();
    } else if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP)
    {
      m_area.zoomIn();
    } else if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
    {
      m_area.zoomOut();
    } else if (ke.getKeyCode() == KeyEvent.VK_LEFT)
    {
      m_area.panHorizontal(PercentArea.LEFT);
    } else if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
    {
      m_area.panHorizontal(PercentArea.RIGHT);
    } else if (ke.getKeyCode() == KeyEvent.VK_UP)
    {
      m_area.panVertical(PercentArea.UP);
    } else if (ke.getKeyCode() == KeyEvent.VK_DOWN)
    {
      m_area.panVertical(PercentArea.DOWN);
    }
  }

  public void keyTyped(KeyEvent e)
  {
  }

  public void keyReleased(KeyEvent e)
  {
  }

  public void mouseEntered(MouseEvent me)
  {
  }


  public void mouseExited(MouseEvent me)
  {
    if (isDisplayingAccessories())
    {
      setDisplayingAccessories(false);
      repaint();
    }
  }

  public boolean isDisplayingAccessories()
  {
    return m_displayAccessories || m_accessoryStyle == VAL_STYLE_ALWAYSON;
  }

  public void setDisplayingAccessories(boolean b)
  {
    m_displayAccessories = b;
  }

  public void mouseClicked(MouseEvent me)
  {
    if (!isDisplayingAccessories() && m_wmHit != null)
    {
      //
      // Go to the url.
      //
      displayURL(m_wmHit.getURL(), null);
      return;
    }
    if (me.getX() >= Accessory.BUTTON_SIZE)
    {
      return;
    }
    int idx = (me.getY()/Accessory.BUTTON_SIZE);
    if (idx >= m_accessories.size())
    {
      // System.err.println("Out of range for accessories");
    } else {
      //
      // Get the local location...
      //
      Point p = new Point(me.getX(), me.getY() - (idx * Accessory.BUTTON_SIZE));
      m_accessories.elementAt(idx).actionPerformed(p);
    }
  }


  public void mousePressed(MouseEvent me)
  {
  }


  public void mouseReleased(MouseEvent me)
  {
  }

  public void mouseDragged(MouseEvent me)
  {
  }

  public void mouseMoved(MouseEvent me)
  {
    boolean needRepaint = false;
    Point p = me.getPoint();
    //
    boolean previously = isDisplayingAccessories();
    //
    if (p.x < Accessory.BUTTON_SIZE) {
      if (m_accessories.size() > 0) {
        setDisplayingAccessories(true);
        int idx = (me.getY()/Accessory.BUTTON_SIZE);
        //
        String statusFeedback = "";
        if (idx < m_accessories.size())
        {
          String desc = m_accessories.elementAt(idx).getDescription();
          if (desc != null) {
            statusFeedback = desc;
          }
        }
        showStatus(statusFeedback);
      }
    } else {
      setDisplayingAccessories(false);
    }
    //
    // Only clickable in a web page.
    //
    if (m_displayAccessories == false)
    {
      //
      // Are we over a Clickable hit point?
      //
      Watermark pwnew = m_wmCollection.isOverClickableWatermark(p);
      if (pwnew != m_wmHit)
      {
        m_wmHit = pwnew;
        needRepaint = true;
        setCursor((m_wmHit != null)?Cursor.getPredefinedCursor(Cursor.HAND_CURSOR):Cursor.getDefaultCursor());
      }
    }
    if (isDisplayingAccessories() != previously)
    {
      needRepaint = true;
    }
    if (needRepaint)
    {
      repaint();
    }
  }

  public void showStatus(String s)
  {
    System.out.println (s);
  }

  public PercentArea getViewArea()
  {
    return m_area;
  }


  public CamStream getStream()
  {
    return null; //m_imgStream;
  }



  public boolean isStandalone()
  {
    return false;
  }

  public Vector<Accessory> getAccessories()
  {
    return m_accessories;
  }


  /* public static void usage()
    {
        System.err.println("Usage: WebCamURL [otherURLs] [-accessories=comma separated accessory list]");
        System.err.println("Current set of accessories are:");
        System.err.println(" o ZoomIn       - Zooms in to the image");
        System.err.println(" o ZoomOut      - Zooms out of the image");
        System.err.println(" o Home         - Shows all the image");
        System.err.println(" o Pan          - Pan around a zoomed-in image");
	    System.err.println(" o ChangeStream - Swap to a different stream (if > 1 listed)");
        System.err.println(" o Info         - Displays information about the stream");
        System.err.println(" o WWWHelp      - Displays a web page showing help");
        System.err.println("");
        System.err.println(" -noaccessories              Will not display any accessories");
        System.err.println(" -accessories=none           Will not display any accessories");
        System.err.println(" -accessories=default        Will display the default set of accessories");
        System.err.println(" -accessorystyle={see below} Defines how the accessories will appear on top-left");
        System.err.println("   indent                      Will squeeze the image [default]");
        System.err.println("   overlay                     Will overlay the accessories onto the image");
        System.err.println("   always                      Always display the accessories (overlaid)");
	    System.err.println(" -retries={num}              The number of retries (default = 1)");
	    System.err.println(" -delay={num}                The number of milliseconds between retries");
	    System.err.println(" -failureimage={url}         Image to display if failure to connect");
        System.err.println(" -watermark={see below}      List of watermarks, separated by '|'");
        System.err.println("   imageURL|corner|linkURL     Watermark information, separated by '|'");
    }
   */
}

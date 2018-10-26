package org.colos.ejss.xml;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.osejs.utils.GeneratedUtil;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.utils.TwoStrings;
import org.colos.ejss.xml.SimulationXML;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.opensourcephysics.controls.OSPLog;
import org.w3c.dom.Element;

public class EmulatorWebSocket extends Emulator {
  static private final ResourceUtil res = new ResourceUtil("Resources");

  private Osejs mEjs;
  private File mMainFile;
  private File mTempFile;
  private String mTempFilename; 
  private int mWidth, mHeight;
  private WebSocketServer mCommSocket;
  private WebSocket mConnection;
  private boolean mVisible=false;
  private OneView mHtmlView;
  
  public EmulatorWebSocket (Osejs ejs, OneView htmlView, final int width, final int height) {
    mEjs = ejs;
    mHtmlView = htmlView;
    mWidth = width;
    mHeight = height;
    try {  
      mMainFile = File.createTempFile("ejs_main_", ".xhtml");
      mMainFile.deleteOnExit();
      mTempFile = File.createTempFile("ejs_tmp_", ".xhtml");
      mTempFilename = "file:///"+FileUtils.correctUrlString(FileUtils.getPath(mTempFile));
      mTempFile.deleteOnExit();
    }
    catch (Exception exc) {
      exc.printStackTrace();
      mEjs.getOutputArea().println(res.getString("Osejs.File.CantCreateFile")+" EJSSimulationList.xml");
    }
  }

  // -----------------------------
  // Interface
  //-----------------------------

  @Override
  public void setVisible (boolean visible) {
    if (visible) {
      if (!mVisible) {
        mVisible = true; // Required to refresh the emulator
        mHtmlView.refreshEmulator();
      }
      if (mConnection==null) {
        showMainFile();
      }
      else {
        mConnection.send("S"+mWidth + " "+mHeight);
        mConnection.send("R"+mTempFilename);
      }
    }
    else {
      clear();
    }
    mVisible = visible;
  }
  
  @Override
  public boolean isVisible () {
    return mVisible;
  }

  
  @Override
  public void clear() {
    if (mConnection!=null) {
      mConnection.send("Q");
      try {
        mCommSocket.stop();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    mConnection = null;
    mMainFile.delete();
    mHtmlView.setViewHidden();
  }

  @Override
  public void setSize(int width, int height) {
    mWidth = width;
    mHeight = height;
    if (mConnection!=null) mConnection.send("S"+mWidth + " "+mHeight);
  }

  @Override
  public void setSimulation(Osejs ejs, SimulationXML sim, String viewDesired, String locale) {
    Element viewSelected = sim.getViewSelected(viewDesired);
    XMLTransformerJava transformer = super.lintSimulation(ejs, sim, viewSelected, locale);

    try {
      int width = Integer.parseInt(BasicElement.evaluateNode(viewSelected,"width"));
      int height = Integer.parseInt(BasicElement.evaluateNode(viewSelected,"height"));
      mWidth = width;
      mHeight = height;
    }
    catch (Exception exc) { } // Do not complain

    final String htmlCode = transformer.toSimulationHTMLForEmulator(viewSelected,locale,XMLTransformerJava.getCssFilenameList(mCssFilename,sim), mLibPath, mHTMLPath);
    try {
      XMLTransformerJava.saveToFile(mTempFile, htmlCode);
      OSPLog.fine("Output file generated "+mTempFilename);
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
   
    if (mVisible && mConnection!=null) {
      mConnection.send("S"+mWidth + " "+mHeight);
      mConnection.send("R"+mTempFilename);
    }
    
  }

  private void showMainFile() {
    final int freePort = Osejs.getFreePort();
    
    File frameLoaderFile = new File(mEjs.getBinDirectory(),"javascript/lib/scripts/frame_loader.js");
    
    TwoStrings borderColors = mEjs.getOptions().getBorderColors();
    int borderWidth = mEjs.getOptions().getBorderWidth();
    
    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    buffer.append("<!DOCTYPE html>\n");
    buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n");
    buffer.append("  <head>\n");
    buffer.append("    <meta charset=\"utf-8\" />\n");
    buffer.append("    <title>EjsS preview page</title>\n");
    buffer.append("    <script src=\"file:///"+FileUtils.correctUrlString(FileUtils.getPath(frameLoaderFile))+"\"></script>\n");
    buffer.append("     </head>\n");
    buffer.append("  <body>\n");// bgcolor=\"rgb(200,220,208)\">\n");
    buffer.append("    <h1 style=\"color:"+borderColors.getFirstString()+"\">EjsS - "+res.getString("EjsOptions.Preview")+"</h1>\n");
    buffer.append("    <iframe style=\"border-style:solid; border-width:"+borderWidth+"px; border-color:"+borderColors.getSecondString()+"\" id=\"webview\" width=\""+mWidth+"\" height=\""+mHeight+"\" src=\""+mTempFilename+"\"></iframe>\n");
    buffer.append("    <script type=\"text/javascript\"><!--//--><![CDATA[//><!--\n");
    buffer.append("      window.addEventListener('load', function () { frame_loader("+freePort+"); }, false);\n");
    buffer.append("    //--><!]]></script>\n");
    buffer.append("  </body>\n");
    buffer.append("</html>\n");
        
    try {  // save it for debugging purposes
      XMLTransformerJava.saveToFile(mMainFile, buffer.toString());
      OSPLog.fine("Main file generated "+mMainFile.getAbsolutePath());
      GeneratedUtil.openBrowser(mEjs, mMainFile);
    } 
    catch (Exception e) {
      mEjs.getOutputArea().println("Cannot generate main file: "+mMainFile.getAbsolutePath());
      e.printStackTrace();
    }
    
    // Check the connection
    mCommSocket = new WebSocketServer(new InetSocketAddress(freePort)) {
      public void onMessage(WebSocket conn, String message) { 
        OSPLog.fine("Websocker at port "+freePort + ": Input received: "+ message); 
      }
      public void onOpen(WebSocket conn, ClientHandshake handshake) {
        OSPLog.fine("Connected to "+ conn.getRemoteSocketAddress().getAddress().getHostAddress());
        mConnection = conn;
        mConnection.send("S"+mWidth + " "+mHeight);
      }
      public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        OSPLog.fine("Connection closed by "+conn.getRemoteSocketAddress().getAddress().getHostAddress());
        mConnection = null;
        clear();
      }
      public void onError(WebSocket conn, Exception ex) {
        if (conn==null && mCommSocket.connections().isEmpty()) { 
          OSPLog.fine("General communication error!");
        }
        else {
          OSPLog.fine("Communication error with "+conn.getRemoteSocketAddress().getAddress().getHostAddress());
        }
//          ex.printStackTrace();
        mConnection = null;
        clear();
      }
    };
    mCommSocket.start();

  }

}

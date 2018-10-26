package org.colos.ejs.model_elements.input_output;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.utils.ModelElementsUtilities;

/**
 * Encapsulates access to a web server
 * @author Francisco Esquembre
 * @version 1.0, August 2010
 *
 */
public class WebServerReader {
  private Model model;
  private String serverAddress;
  
  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @param _serverAdress The base server address
   * @see #setServerAddress(String)
   */
  public WebServerReader(Model _model, String _serverAdress) {
    this.model = _model;
    setServerAddress(_serverAdress);
  }

  /**
   * Sets the server address to a constant String (such as "http://www.um.es") 
   * or links it to a String model variable (such as "%myURLStringVariable%") which will need to provide the correct URL value.
   * @return
   */
  public void setServerAddress(String _serverAddress) {
    this.serverAddress = _serverAddress;
  }

  /**
   * Returns the actual server address used in connections.
   * @return
   */
  public String getServerAddress() {
    return ModelElementsUtilities.getValue(model,serverAddress);
  }

  /**
   * Reads the ouptput from a given page or command from the server.
   * @return null if failed
   */
  public String readOutput(String _command) {
    if (serverAddress!=null) {
      String urlAddress = getServerAddress();
      if (urlAddress.endsWith("/") || _command.startsWith("/")) _command = urlAddress + _command;
      else _command = urlAddress + "/" + _command;
    }
    _command = correctUrlString(_command);
    try { // Now, do it
      URL url = new URL(_command);
      Reader reader = new InputStreamReader(url.openStream());
      LineNumberReader l = new LineNumberReader(reader);
      StringBuffer buffer = new StringBuffer();
      String sl = l.readLine();
      while (sl != null) { buffer.append(sl+"\n"); sl = l.readLine(); }
      reader.close();
      return buffer.toString();
    }
    catch (Exception exc) {
      System.err.println ("Error reading command <"+_command+">");
      exc.printStackTrace();
      return null;
    }
  }
  
  
  /**
   * Corrects a URL by changing spaces to "%20" and '&' to "%26"
   * @param _urlStr String
   * @return String
   */
  static public String correctUrlString (String _urlStr) {
    String noSpaces = ""; // Replace spaces by "%20"
    java.util.StringTokenizer tkn = new java.util.StringTokenizer (_urlStr, " ",true);
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (token.equals(" ")) noSpaces += "%20";
      //else if (token.equals("&")) aux += "%26";
      else noSpaces += token;
    }
    String noAnds = ""; // Replace "&" by "%26"
    tkn = new java.util.StringTokenizer (noSpaces, "&",true);
    while (tkn.hasMoreTokens()) {
      String token = tkn.nextToken();
      if (token.equals("&")) noAnds += "%26";
      else noAnds += token;
    }
    return noAnds;
  }
}

package org.colos.freefem;

/* The org.colos.freefem package contains Java classes that allow interfacing with a loal or server
 * FreeFem++ installation.
 * 
 * @author Mar�a Jos� Cano, Universidad de Murcia, Murcia, Spain
 * @author Francisco Esquembre, Universidad de Murcia, Murcia, Spain <fem@um.es>
 * @author Invaluable help provided by Antoine Le Hyaric (Lab Jacques-Louis Lions, Paris, France)
 * @version 0.0 May 2012
 * @version 1.0 Dec 2012
 */

import java.io.File;

import org.colos.freefem.utils.Connection;
import org.colos.freefem.utils.LocalConnection;
import org.colos.freefem.utils.ServerConnection;

/**
 * A class to run a FreeFem++ script, either using a server or a local installation
 */
public class FreeFem {
  public enum ErrorCode  { NO_ERROR, UNKNWON_HOST_ERROR, CONNECTION_ERROR, INPUT_OUTPUT_ERROR, INCORRECT_VERSION_ERROR, FIRST_LINE_ERROR, PROCESSING_INPUT_ERROR }
  static private File mTempDirectory = null;

  // Configuration variables
  private String mUrl;
  private int mPortNumber = 12345;
  private String mUserName = null;
  private String mUserPassword = null;
  private String mUserDirectory = ".";

  // Implementation variables
  private Connection mConnection;

  // --------------------------------------
  // Constructors and static methods
  //--------------------------------------

  /**
   * Default constructor (uses a local installation of FreeFem)
   */
  public FreeFem() {
    setServer(null,0);
  }

  /**
   * Constructs an instance that will use a FreeFem server
   * @param url the url of the server (the port is, so far, fixed at 12345)
   */
  public FreeFem(String url, int port) {
    setServer(url, port);
  }

  static public void setTempDirectory(File directory) {
	  if (directory.isDirectory()) mTempDirectory = directory;
	  else mTempDirectory = directory.getParentFile();
  }
  
  static public File getTempDirectory() { return mTempDirectory; }
  

  // --------------------------------------
  // Setter and getter methods
  //--------------------------------------

  /**
   * Sets the URL for the server.
   * @param url the URL of a running FreeFem++-cs server. Null for a local installation
   */
  public void setServer (String url, int portNumber) {
    mUrl = url;
    mPortNumber = (portNumber>0) ? portNumber : 12345; 
    boolean useScriptDir = (mConnection==null) ? false : mConnection.getOutputToScriptDirectory();
    if (url==null || url.trim().length()<1) {
      mUrl = null;
      if (! (mConnection instanceof LocalConnection)) mConnection = new LocalConnection();
    }
    else {
      mUrl = url.trim();
      if (! (mConnection instanceof ServerConnection)) mConnection = new ServerConnection(this);
    }
    mConnection.setOutputToScriptDirectory(useScriptDir);
  }

  /**
   * Returns the server URL
   * @return
   */
  public String getServerURL() {
    return mUrl;
  }

  /**
   * Returns the server port number
   * @return
   */
  public int getServerPortNumber() {
    return mPortNumber;
  }
  
  /**
   * Sets the user directory
   * @param dir
   */
  public void setUserDirectory(String dir) { 
    mUserDirectory = dir; 
  }
  
  /**
   * Returns the user directory set by the user
   * @return
   */
  public String getUserDirectory() { 
    return mUserDirectory; 
  }

  /**
   * Sets the user directory
   * @param dir
   */
  public void setOutputToScriptDirectory(boolean doIt) { mConnection.setOutputToScriptDirectory(doIt); }
  
  public File getLastScriptDirectory() { return mConnection.getLastScriptDirectory(); }
  
  /**
   * Set the user and password used to connect to the server
   * @param username
   * @param password
   */
  public void setUserAndPassword(String username, String password) {
    mUserName = username;
    mUserPassword = password;
  }
  
  /**
   * Returns the user name set by the user
   * @return
   */
  public String getUserName() { 
    return mUserName; 
  }

  /**
   * Returns the user password as set by the user
   * @return
   */
  public String getUserPassword() { 
    return mUserPassword; 
  }

  // --------------------------------------
  // Operation
  //--------------------------------------
  
  /**
   * Runs the given script on a single process
   * @param script the script to run
   * @return the ScriptOutput with the result of the PLOT commands found so far in the script, null if there was any error
   * @see ScriptOutput
   * @see getErrorCode
   */
  public ScriptOutput runScript(String script) {
    return runScript(script,0);
  }

  /**
   * Runs the given script
   * @param script the script to run
   * @param nProcesses number of processes in an MPI implementation
   * @return the ScriptOutput with the result of the PLOT commands found so far in the script, null if there was any error
   * @see ScriptOutput
   */
  public ScriptOutput runScript(String script, int nProcesses) {
    mConnection.clearError();
    return mConnection.runScript(script,nProcesses);
  }

  /**
   * Continues reading a script that is waiting (as a result of a wait=1 instruction)
   * @param output the output that is waiting
   * @return the ScriptOutput with the result of the PLOT commands found in the script, 
   * or null if there was any error, or the unchanged output if it was not waiting
   */
  public ScriptOutput continueReading(ScriptOutput output) {
    if (output==null || !output.isWaiting()) {
      return output;
    }
    mConnection.clearError();
    return mConnection.continueReading(output);
  }
  
  /**
   * Returns the last error code produced by a call to runScript()
   * @return FreeFem.ErrorCode.NO_ERROR if the connection worked OK
   */
  public ErrorCode getErrorCode() { 
    return mConnection.getErrorCode(); 
  }
  
}
